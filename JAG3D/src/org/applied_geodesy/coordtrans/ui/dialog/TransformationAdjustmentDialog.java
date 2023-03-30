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

package org.applied_geodesy.coordtrans.ui.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.menu.UIMenuBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;

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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class TransformationAdjustmentDialog {

	private class AdjustmentTask extends Task<EstimationStateType> implements PropertyChangeListener {
		private final String iterationTextTemplate;
		private final String convergenceTextTemplate;
		private TransformationAdjustment adjustment;
		private double processState = 0.0;
		private double finalStepProcesses = 0.0;
		private boolean updateProgressOnIterate = true;
		private AdjustmentTask(TransformationAdjustment adjustment) {
			this.adjustment = adjustment;

			this.iterationTextTemplate   = i18n.getString("TransformationAdjustmentDialog.iteration.label",   "%d. iteration step of maximal %d \u2026");
			this.convergenceTextTemplate = i18n.getString("TransformationAdjustmentDialog.convergence.label", "Convergence max|dx| = %.2e");
		}
		
		@Override
		protected EstimationStateType call() throws Exception {
			try {
				preventClosing = true;
				
				UIMenuBuilder.getInstance().setReportMenuDisable(true);

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.initialize.label", "Initialize process\u2026"));
				this.updateIterationProgressMessage(i18n.getString("TransformationAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);

				this.finalStepProcesses = 0.25 / 3.0; // Nenner == Anzahl + 1 an zusaetzlichen Tasks (INVERT_NORMAL_EQUATION_MATRIX, ESTIAMTE_STOCHASTIC_PARAMETERS)
				this.adjustment.addPropertyChangeListener(this);

				this.processState = 0.0;
				this.updateProgress(this.processState, 1.0);

				if (this.isCancelled())
					return EstimationStateType.INTERRUPT;

				Transformation transformation = this.adjustment.getTransformation();

				// derive parameters for warm start of adjustment
				if (transformation.isEstimateInitialGuess())
					transformation.deriveInitialGuess();

				this.adjustment.init();
				EstimationStateType returnType = this.adjustment.estimateModel();

				if (this.isCancelled())
					return EstimationStateType.INTERRUPT;

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.save.label", "Save results\u2026"));
				this.updateIterationProgressMessage(i18n.getString("TransformationAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							UIHomologousFramePositionPairTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
							UIHomologousFramePositionPairTableBuilder.getInstance().getTable().refresh();
							UIHomologousFramePositionPairTableBuilder.getInstance().getTable().sort();
							
							UIParameterTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
							UIParameterTableBuilder.getInstance().getTable().refresh();
							UIParameterTableBuilder.getInstance().getTable().sort();
							
							UIFramePositionPairTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
							UIFramePositionPairTableBuilder.getInstance().getTable().refresh();
							UIFramePositionPairTableBuilder.getInstance().getTable().sort();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				return returnType;
			}
			finally {
				this.destroyFeatureAdjustment();
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
							text.setText(i18n.getString("TransformationAdjustmentDialog.done.label", "Done"));
							progressIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
						}
					}
				}
			});
		}

		private void destroyFeatureAdjustment() {
			if (this.adjustment != null) {
				this.adjustment.removePropertyChangeListener(this);
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
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.busy.label", "Parameter adjustment in process\u2026"));
				this.updateIterationProgressMessage(null);
				this.updateConvergenceProgressMessage(null);
				break;

			case CONVERGENCE:
				if (oldValue != null && newValue != null && oldValue instanceof Double && newValue instanceof Double) {
					double current = (Double)newValue;
					double minimal = (Double)oldValue;
					if (this.updateProgressOnIterate) {
						double frac = 0.75 * Math.min(minimal / current, 1.0);
						this.processState = Math.max(this.processState, frac);
						this.updateProgress(this.processState, 1.0);
					}
					this.updateConvergenceProgressMessage(String.format(Locale.ENGLISH, this.convergenceTextTemplate, current, minimal));
				}
				break;

			case ITERATE:
				if (oldValue != null && newValue != null && oldValue instanceof Integer && newValue instanceof Integer) {
					int current = (Integer)newValue;
					int maximal = (Integer)oldValue;
					if (this.updateProgressOnIterate) {
						double frac = 0.75 * Math.min((double)current / (double)maximal, 1.0);
						this.processState = Math.max(this.processState, frac);
						this.updateProgress(this.processState, 1.0);
					}
					this.updateIterationProgressMessage(String.format(Locale.ENGLISH, this.iterationTextTemplate, current, maximal));
				}
				break;
				
			case INVERT_NORMAL_EQUATION_MATRIX:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.invert_normal_equation_matrix.label", "Invert normal equation matrix\u2026"));
				break;

			case ESTIAMTE_STOCHASTIC_PARAMETERS:
				this.processState += this.finalStepProcesses;
				this.updateProgress(this.processState, 1.0);
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.estimate_stochastic_parameters.label", "Estimate stochastic parameters\u2026"));
				break;

			case ERROR_FREE_ESTIMATION:
				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.error_free_estimation.label", "Parameter adjustment finished\u2026"));
				break;

			case INTERRUPT:
				this.updateMessage(i18n.getString("TransformationAdjustmentDialog.interrupt.label", "Terminate adjustment process\u2026"));
				this.updateIterationProgressMessage(i18n.getString("TransformationAdjustmentDialog.pleasewait.label", "Please wait\u2026"));
				this.updateConvergenceProgressMessage(null);
				break;

			// unused cases
			case UNSCENTED_TRANSFORMATION_STEP:
			case PRINCIPAL_COMPONENT_ANALYSIS:
			case EXPORT_COVARIANCE_MATRIX:
			case EXPORT_COVARIANCE_INFORMATION:
				break;
				
			// adjustment failed (exception) 
			case NOT_INITIALISED:
			case NO_CONVERGENCE:
			case OUT_OF_MEMORY:
			case ROBUST_ESTIMATION_FAILED:
			case SINGULAR_MATRIX:
				break;

			}
		}
	}
	
	private static TransformationAdjustmentDialog adjustmentDialog = new TransformationAdjustmentDialog();
	private Window window;
	private AdjustmentTask adjustmentTask;
	private boolean preventClosing = false;
	private I18N i18n = I18N.getInstance();
	private Dialog<EstimationStateType> dialog = null;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private Label iterationLabel = new Label();
	private Label progressLabel = new Label();
	private EstimationStateType result;

	private TransformationAdjustmentDialog() {}

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
		this.dialog.setTitle(i18n.getString("TransformationAdjustmentDialog.title", "Parameter adjustment"));
		this.dialog.setHeaderText(i18n.getString("TransformationAdjustmentDialog.header", "Transformation parameter adjustment is processing\u2026"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
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
		
		this.dialog.setResultConverter(new Callback<ButtonType, EstimationStateType>() {
			@Override
			public EstimationStateType call(ButtonType buttonType) {
				return result;
			}
		});

		this.dialog.getDialogPane().setContent(vbox);
	}

	private void reset() {
		this.progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		this.iterationLabel.setText(null);
		this.progressLabel.setText(null);
		this.preventClosing = false;
		this.result = EstimationStateType.NOT_INITIALISED;
	}

	private Node createProgressPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMinWidth(400);
		gridPane.setHgap(20);
		gridPane.setVgap(15);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		this.progressIndicator.setMinWidth(50);
		this.progressIndicator.setMinHeight(50);

		gridPane.add(this.progressIndicator, 0, 0, 1, 3); // col, row, colspan, rowspan
		gridPane.add(this.iterationLabel,    1, 0);
		gridPane.add(this.progressLabel,     1, 1);

		return gridPane;
	}

	private void process() {
		this.reset();
		TransformationAdjustment adjustment = UITreeBuilder.getInstance().getTransformationAdjustment();
		this.adjustmentTask = new AdjustmentTask(adjustment);
		this.adjustmentTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				result = adjustmentTask.getValue();
				UIMenuBuilder.getInstance().setReportMenuDisable(false);
				UITreeBuilder.getInstance().handleTreeSelections();
			}
		});

		this.adjustmentTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = adjustmentTask.getException(); 
				if (throwable != null) {
					if (throwable instanceof NotConvergedException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.noconvergence.title",  "Parameter adjustment failed"),
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.noconvergence.header", "Iteration process diverges"),
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.noconvergence.message", "Error, iteration limit of adjustment process reached but without satisfactory convergence."),
								throwable
								);
							}
						});
					}
					
					else if (throwable instanceof MatrixSingularException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.singularmatrix.title",  "Parameter adjustment failed"),
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.singularmatrix.header", "Singular normal euqation matrix"),
								i18n.getString("TransformationAdjustmentDialog.message.error.failed.singularmatrix.message", "Error, could not invert normal equation matrix."),
								throwable
								);
							}
						});
					}

					else if (throwable instanceof OutOfMemoryError) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.outofmemory.title",  "Parameter adjustment failed"),
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.outofmemory.header", "Out of memory"),
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.outofmemory.message", "Error, not enough memory to adjust transformtion parameters. Please allocate more memory."),
								throwable
								);
							}
						});
					}
					
					else {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.exception.title", "Parameter adjustment failed"),
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.exception.header", "Error, could not adjust transformation parameters."),
										i18n.getString("TransformationAdjustmentDialog.message.error.failed.exception.message", "An exception has occurred during parameter adjustment."),
										throwable
										);
							}
						});
					}
					throwable.printStackTrace();
				}
				try {
					UIHomologousFramePositionPairTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
					UIHomologousFramePositionPairTableBuilder.getInstance().getTable().refresh();
					UIHomologousFramePositionPairTableBuilder.getInstance().getTable().sort();

					UIParameterTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
					UIParameterTableBuilder.getInstance().getTable().refresh();
					UIParameterTableBuilder.getInstance().getTable().sort();
					
					UIFramePositionPairTableBuilder.getInstance().getTable().getSelectionModel().clearSelection();
					UIFramePositionPairTableBuilder.getInstance().getTable().refresh();
					UIFramePositionPairTableBuilder.getInstance().getTable().sort();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Thread th = new Thread(this.adjustmentTask);
		th.setDaemon(true);
		th.start();
	}
}
