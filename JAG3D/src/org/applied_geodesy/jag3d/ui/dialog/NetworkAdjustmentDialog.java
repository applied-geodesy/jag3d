/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.jag3d.ui.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.network.NetworkAdjustment;
import org.applied_geodesy.adjustment.network.sql.IllegalProjectionPropertyException;
import org.applied_geodesy.adjustment.network.sql.SQLAdjustmentManager;
import org.applied_geodesy.jag3d.sql.PointTypeMismatchException;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.sql.UnderDeterminedPointException;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class NetworkAdjustmentDialog {

	private class AdjustmentTask extends Task<EstimationStateType> implements PropertyChangeListener {
		private final String iterationTextTemplate;
		private final String convergenceTextTemplate;
		private NetworkAdjustment adjustment;
		private double processState = 0.0;
		private double finalStepProcesses = 0.0;
		private SQLAdjustmentManager dataBaseManager;

		private AdjustmentTask(SQLAdjustmentManager dataBaseManager) {
			this.dataBaseManager = dataBaseManager;

			this.iterationTextTemplate   = i18n.getString("NetworkAdjustmentDialog.iteration.label",   "%d. iteration step of maximal %d \u2026");
			this.convergenceTextTemplate = i18n.getString("NetworkAdjustmentDialog.convergence.label", "Convergence max|dx| = %.2e");
		}

		@Override
		protected EstimationStateType call() throws Exception {
			try {
				preventClosing = true;

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.initialize.label", "Initialize process\u2026"));
				this.updateIterationProgressMessage(i18n.getString("NetworkAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);

				SQLManager.getInstance().checkNumberOfObersvationsPerUnknownParameter();

				this.adjustment = this.dataBaseManager.getNetworkAdjustment();
				this.finalStepProcesses = 0.25 / (this.adjustment.hasCovarianceExportPathAndBaseName() ? 6.0 : 4.0);
				this.adjustment.addPropertyChangeListener(this);

				this.processState = 0.0;
				this.updateProgress(this.processState, 1.0);

				if (this.isCancelled())
					return EstimationStateType.INTERRUPT;

				EstimationStateType returnType = this.adjustment.estimateModel();

				if (this.isCancelled())
					return EstimationStateType.INTERRUPT;

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.save.label", "Save results\u2026"));
				this.updateIterationProgressMessage(i18n.getString("NetworkAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);
				this.dataBaseManager.saveResults();
				this.dataBaseManager.clear();
				return returnType;
			}
			finally {
				this.destroyNetworkAdjustment();
				this.updateIterationProgressMessage(null);
				this.updateConvergenceProgressMessage(null);
				preventClosing = false;
			}
		}

		@Override 
		protected void succeeded() {
			preventClosing = false;
			super.succeeded();
			hideDialog();
		}

		@Override 
		protected void failed() {
			preventClosing = false;
			super.failed();
			hideDialog();
		}

		@Override
		protected void cancelled() {
			super.cancelled();
			if (this.adjustment != null)
				this.adjustment.interrupt();
		}

		//		@Override
		//		protected void done() {	}

		private void hideDialog() {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (!preventClosing)
						dialog.hide();
				}
			});
		}

		private void updateIterationProgressMessage(String message) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					iterationLabel.setText(message);
				}
			});
		}

		private void updateConvergenceProgressMessage(String message) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressLabel.setText(message);
				}
			});
		}

		@Override
		protected void updateMessage(String message) {
			super.updateMessage(message);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					dialog.setHeaderText(message);
				}
			});
		}

		@Override
		protected void updateProgress(double workDone, double max) {
			super.updateProgress(workDone, max);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressIndicator.setProgress(workDone);
					if (workDone >= 1.0) {
						Node node = progressIndicator.lookup(".percentage");
						if (node != null && node instanceof Text) {
							Text text = (Text)node;
							text.setText(i18n.getString("NetworkAdjustmentDialog.done.label", "Done"));
							progressIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
						}
					}
				}
			});
		}

		private void destroyNetworkAdjustment() {
			if (this.adjustment != null) {
				this.adjustment.removePropertyChangeListener(this);
				this.adjustment.clearMatrices();
				this.adjustment = null;
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();

			EstimationStateType state = EstimationStateType.valueOf(name);
			if (state == null)
				return;

			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();

			switch(state) {
			case BUSY:
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.busy.label", "Network adjustment in process\u2026"));
				this.updateIterationProgressMessage(null);
				this.updateConvergenceProgressMessage(null);
				break;

			case CONVERGENCE:
				if (oldValue != null && newValue != null && oldValue instanceof Double && newValue instanceof Double) {
					double current = (Double)newValue;
					double minimal = (Double)oldValue;
					double frac = 0.75 * Math.min(minimal / current, 1.0);
					this.processState = Math.max(this.processState, frac);
					this.updateConvergenceProgressMessage(String.format(Locale.ENGLISH, this.convergenceTextTemplate, current, minimal));
					this.updateProgress(this.processState, 1.0);
				}
				break;

			case ITERATE:
				if (oldValue != null && newValue != null && oldValue instanceof Integer && newValue instanceof Integer) {
					int current = (Integer)newValue;
					int maximal = (Integer)oldValue;
					double frac = 0.75 * Math.min((double)current / (double)maximal, 1.0);
					this.processState = Math.max(this.processState, frac);
					this.updateIterationProgressMessage(String.format(Locale.ENGLISH, iterationTextTemplate, current, maximal));
					this.updateProgress(this.processState, 1.0);
				}
				break;

			case INVERT_NORMAL_EQUATION_MATRIX:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.invert_normal_equation_matrix.label", "Invert normal equation matrix\u2026"));
				break;

			case ESTIAMTE_STOCHASTIC_PARAMETERS:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.estimate_stochastic_parameters.label", "Estimate stochastic parameters\u2026"));
				break;

			case PRINCIPAL_COMPONENT_ANALYSIS:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.principal_component_analysis.label", "Principal component analysis\u2026"));
				break;

			case EXPORT_COVARIANCE_MATRIX:
			case EXPORT_COVARIANCE_INFORMATION:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.export_covariance_matrix.label", "Export covariance matrix\u2026"));
				if (newValue != null) {
					this.updateIterationProgressMessage(newValue.toString());
					this.updateConvergenceProgressMessage(null);
				}
				break;	

			case ERROR_FREE_ESTIMATION:
				this.updateProgress(1.0, 1.0);
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.error_free_estimation.label", "Network adjustment finished\u2026"));
				break;

			case INTERRUPT:
				this.updateMessage(i18n.getString("NetworkAdjustmentDialog.interrupt.label", "Terminate adjustment process\u2026"));
				this.updateIterationProgressMessage(i18n.getString("NetworkAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);
				break;

				// Adjustment faild (wo exception) 
			case NOT_INITIALISED:
			case NO_CONVERGENCE:
			case OUT_OF_MEMORY:
			case ROBUST_ESTIMATION_FAILD:
			case SINGULAR_MATRIX:
				break;

			}
		}
	}

	private static NetworkAdjustmentDialog adjustmentDialog = new NetworkAdjustmentDialog();
	private Window window;
	private AdjustmentTask adjustmentTask;
	private boolean preventClosing = false;
	private I18N i18n = I18N.getInstance();
	private Dialog<EstimationStateType> dialog = null;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private Label iterationLabel = new Label();
	private Label progressLabel = new Label();

	private NetworkAdjustmentDialog() {}

	public static void show() {
		adjustmentDialog.init();
		adjustmentDialog.reset();

		adjustmentDialog.dialog.show();
		adjustmentDialog.process();
		
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					adjustmentDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) adjustmentDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void setOwner(Window owner) {
		adjustmentDialog.window = owner;
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<EstimationStateType>();
		this.dialog.initOwner(window);
		this.dialog.setTitle(i18n.getString("NetworkAdjustmentDialog.title", "Network adjustment"));
		this.dialog.setHeaderText(i18n.getString("NetworkAdjustmentDialog.header", "Network adjustment is processing\u2026"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		VBox vbox = new VBox();
		vbox.getChildren().addAll(this.createProgressPane());

		this.dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (preventClosing) {
					event.consume();
					if (adjustmentTask != null)
						adjustmentTask.cancelled();
				}
			}
		});

		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				if (preventClosing) {
					event.consume();
					if (adjustmentTask != null)
						adjustmentTask.cancelled();
				}
			}
		});

		this.dialog.getDialogPane().setContent(vbox);

		//		this.dialog.setResultConverter(new Callback<ButtonType, EstimationStateType>() {
		//			@Override
		//			public EstimationStateType call(ButtonType buttonType) {
		//				EstimationStateType returnType = null;
		//				if (buttonType == ButtonType.CANCEL) {
		//					if (adjustmentTask != null) {
		//						adjustmentTask.cancelled();
		//						returnType = adjustmentTask.getValue();
		//					}
		//				}
		//				return returnType;
		//			}
		//		});
	}

	private void reset() {
		this.progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		this.iterationLabel.setText(null);
		this.progressLabel.setText(null);
		this.preventClosing = false;
	}

	private Node createProgressPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMinWidth(400);
		gridPane.setHgap(20);
		gridPane.setVgap(15);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, links, unten, rechts

		this.progressIndicator.setMinWidth(50);
		this.progressIndicator.setMinHeight(50);

		gridPane.add(this.progressIndicator, 0, 0, 1, 3); // col, row, colspan, rowspan
		gridPane.add(this.iterationLabel,    1, 0);
		gridPane.add(this.progressLabel,     1, 1);

		return gridPane;
	}

	private void process() {
		this.reset();

		this.adjustmentTask = new AdjustmentTask(SQLManager.getInstance().getAdjustmentManager());
		this.adjustmentTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				EstimationStateType result = adjustmentTask.getValue();
				if (result != null) {
					switch (result) {					
					case NO_CONVERGENCE:
					case ROBUST_ESTIMATION_FAILD:
						OptionDialog.showErrorDialog(
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.noconvergence.title",  "Network adjustment failed"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.noconvergence.header", "Iteration process diverges"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.noconvergence.message", "Error, iteration limit of adjustment process reached but without satisfactory convergence.")
								);
						break;
						
					case NOT_INITIALISED:
					case SINGULAR_MATRIX:
						OptionDialog.showErrorDialog(
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.singularmatrix.title",  "Network adjustment failed"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.singularmatrix.header", "Singular normal euqation matrix"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.singularmatrix.message", "Error, could not invert normal equation matrix.")
								);
						break;

					case OUT_OF_MEMORY:
						OptionDialog.showErrorDialog(
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.outofmemory.title",  "Network adjustment failed"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.outofmemory.header", "Out of memory"),
								i18n.getString("NetworkAdjustmentDialog.message.error.failed.outofmemory.message", "Error, not enough memory to adjust network. Please allocate more memory.")
								);
						break;

					default:
//						System.out.println(NetworkAdjustmentDialog.class.getSimpleName() + " Fishied " + result);
						break;
					}
					
					MultipleSelectionModel<TreeItem<TreeItemValue>> selectionModel = UITreeBuilder.getInstance().getTree().getSelectionModel();
					List<Integer> treeItemIndices = selectionModel.getSelectedIndices();
					
					if (treeItemIndices == null || treeItemIndices.size() == 0) {
						selectionModel.clearSelection();
						selectionModel.select(0);
					}
					else {
						int[] indices = new int[treeItemIndices.size()];
						for (int i=0; i<indices.length; i++)
							indices[i] = treeItemIndices.get(i);
						
						try {
							selectionModel.clearSelection();
							selectionModel.selectIndices(indices[0], indices);
						}
						catch (Exception e) {
							e.printStackTrace();
							
							TreeItem<TreeItemValue> treeItem = selectionModel.getSelectedItem();
							if (treeItem != null)
								selectionModel.select(treeItem);
							else
								selectionModel.select(0);
						}
					}				
				}
			}
		});

		this.adjustmentTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = adjustmentTask.getException(); 
				if (throwable != null) {
					if (throwable instanceof PointTypeMismatchException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("NetworkAdjustmentDialog.message.error.pointtypemismatch.exception.title",  "Initialization error"),
										i18n.getString("NetworkAdjustmentDialog.message.error.pointtypemismatch.exception.header", "Error, the project contains uncombinable point typs, i.e. datum points as well as reference or stochastic points."),
										i18n.getString("NetworkAdjustmentDialog.message.error.pointtypemismatch.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof UnderDeterminedPointException) {
						final UnderDeterminedPointException e = (UnderDeterminedPointException)throwable;
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("NetworkAdjustmentDialog.message.error.underdeterminded.exception.title",  "Initialization error"),
										String.format(i18n.getString("NetworkAdjustmentDialog.message.error.underdeterminded.exception.header", "Error, the point %s of dimension %d has only %d observations and is indeterminable."), e.getPointName(), e.getDimension(), e.getNumberOfObservations()),
										i18n.getString("NetworkAdjustmentDialog.message.error.underdeterminded.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof IllegalProjectionPropertyException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("NetworkAdjustmentDialog.message.error.projection.exception.title",  "Initialization error"),
										i18n.getString("NetworkAdjustmentDialog.message.error.projection.exception.header", "Error, the project contains unsupported projection properties."),
										i18n.getString("NetworkAdjustmentDialog.message.error.projection.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof DatabaseVersionMismatchException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("NetworkAdjustmentDialog.message.error.databaseversion.exception.title",  "Version error"),
										i18n.getString("NetworkAdjustmentDialog.message.error.databaseversion.exception.header", "Error, the database version is unsupported."),
										i18n.getString("NetworkAdjustmentDialog.message.error.databaseversion.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					else {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("NetworkAdjustmentDialog.message.error.failed.exception.title", "Network adjustment failed"),
										i18n.getString("NetworkAdjustmentDialog.message.error.failed.exception.header", "Error, could not adjust network."),
										i18n.getString("NetworkAdjustmentDialog.message.error.failed.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					throwable.printStackTrace();
				}
				UITreeBuilder.getInstance().getTree().getSelectionModel().select(0);
			}
		});

		Thread th = new Thread(this.adjustmentTask);
		th.setDaemon(true);
		th.start();
	}
}
