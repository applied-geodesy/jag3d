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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.network.approximation.sql.SQLApproximationManager;
import org.applied_geodesy.jag3d.sql.PointTypeMismatchException;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.sql.UnderDeterminedPointException;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class ApproximationValuesDialog {

	private class StartAdjustmentEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			process();
			event.consume();
		}
	}

	private class ApproximationEstimationTask extends Task<Boolean> implements PropertyChangeListener {
		private SQLApproximationManager approximationManager;
		private ApproximationEstimationTask(SQLApproximationManager approximationManager) {
			this.approximationManager = approximationManager;
		}

		@Override
		protected Boolean call() throws Exception {
			EstimationStateType status1d = EstimationStateType.ERROR_FREE_ESTIMATION;
			EstimationStateType status2d = EstimationStateType.ERROR_FREE_ESTIMATION;
			this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
			approximationManager.addPropertyChangeListener(this);
			try {
				preventClosing = true;
				okButton.setDisable(true);
				progressIndicatorPane.setVisible(true);
				settingPane.setDisable(true);

				// Global check - an exception occurs, if check fails
				SQLManager.getInstance().checkNumberOfObersvationsPerUnknownParameter();
				
				if (transferDatumAndNewPointsResultsRadioButton.isSelected() || transferNewPointsResultsRadioButton.isSelected())
					approximationManager.transferAposteriori2AprioriValues(transferDatumAndNewPointsResultsRadioButton.isSelected());
				else if (estimateDatumAndNewPointsRadioButton.isSelected() || estimateNewPointsRadioButton.isSelected()) {
					approximationManager.setEstimateDatumsPoints(estimateDatumAndNewPointsRadioButton.isSelected());
					approximationManager.adjustApproximationValues(25.0);
				}

				status2d = approximationManager.getEstimationStatus2D();
				status1d = approximationManager.getEstimationStatus1D();

				return status1d == EstimationStateType.ERROR_FREE_ESTIMATION && status2d == EstimationStateType.ERROR_FREE_ESTIMATION;
			}
			finally {
				approximationManager.removePropertyChangeListener(this);
				okButton.setDisable(false);
				progressIndicatorPane.setVisible(false);
				settingPane.setDisable(false);
				if (status1d == EstimationStateType.ERROR_FREE_ESTIMATION && status2d == EstimationStateType.ERROR_FREE_ESTIMATION)
					preventClosing = false;
			}
		}

		@Override 
		protected void failed() {
			preventClosing = false;
			super.failed();
		}

		@Override
		protected void cancelled() {
			super.cancelled();
			if (this.approximationManager != null)
				this.approximationManager.interrupt();
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
							text.setText(i18n.getString("ApproximationValuesDialog.done.label", "Done"));
							progressIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
						}
					}
				}
			});
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			//System.out.println(evt);
		}
	}

	private I18N i18n = I18N.getInstance();
	private static ApproximationValuesDialog approximationValuesDialog = new ApproximationValuesDialog();
	private Dialog<EstimationStateType> dialog = null;
	private Window window;
	private boolean preventClosing = false;
	private Button okButton;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private Node settingPane, progressIndicatorPane;
	private RadioButton estimateDatumAndNewPointsRadioButton, 
	estimateNewPointsRadioButton, 
	transferDatumAndNewPointsResultsRadioButton, 
	transferNewPointsResultsRadioButton;
	private ApproximationEstimationTask approximationEstimationTask = null;
	private ApproximationValuesDialog() {}

	public static void setOwner(Window owner) {
		approximationValuesDialog.window = owner;
	}

	public static Optional<EstimationStateType> showAndWait() {
		approximationValuesDialog.init();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					approximationValuesDialog.reset();
					approximationValuesDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) approximationValuesDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return approximationValuesDialog.dialog.showAndWait();
	}

	private void reset() {
		this.preventClosing = false;
		this.settingPane.setDisable(false);
		this.progressIndicatorPane.setVisible(false);
		this.okButton.setDisable(false);
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.settingPane = this.createSettingPane();
		this.progressIndicatorPane = this.createProgressIndicatorPane();
		this.progressIndicatorPane.setVisible(false);

		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		stackPane.setMaxSize(Double.MAX_VALUE, Region.USE_PREF_SIZE);
		stackPane.getChildren().addAll(this.settingPane, this.progressIndicatorPane);

		this.dialog = new Dialog<EstimationStateType>();
		this.dialog.setTitle(i18n.getString("ApproximationValuesDialog.title", "Approximation values"));
		this.dialog.setHeaderText(i18n.getString("ApproximationValuesDialog.header", "Estimate approximation values using terrestrial observations"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);

		this.dialog.getDialogPane().setContent(stackPane);
		this.dialog.setResizable(true);

		this.okButton = (Button)this.dialog.getDialogPane().lookupButton(ButtonType.OK);
		this.okButton.addEventFilter(ActionEvent.ACTION, new StartAdjustmentEvent());

		this.dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (preventClosing) {
					event.consume();
					if (approximationEstimationTask != null)
						approximationEstimationTask.cancelled();
				}
			}
		});

		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				if (preventClosing) {
					event.consume();
					if (approximationEstimationTask != null)
						approximationEstimationTask.cancelled();
				}
			}
		});
	}

	private Node createProgressIndicatorPane() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().setAll(this.progressIndicator);
		this.progressIndicator.setMinWidth(50);
		this.progressIndicator.setMinHeight(50);
		return box;
	}

	private Node createSettingPane() {
		String estimateDatumAndNewPointsRadioButtonLabel   = i18n.getString("ApproximationValuesDialog.estimate.datum_and_new.label", "Derive approximations for datum and new points");
		String estimateDatumAndNewPointsRadioButtonTooltip = i18n.getString("ApproximationValuesDialog.estimate.datum_and_new.title", "If checked, datum and new points will be estimated");

		String estimateNewPointsRadioButtonLabel   = i18n.getString("ApproximationValuesDialog.estimate.new.label", "Derive approximations for new points");
		String estimateNewPointsRadioButtonTooltip = i18n.getString("ApproximationValuesDialog.estimate.new.title", "If checked, new points will be estimated");

		String transferDatumAndNewPointsResultsLabel   = i18n.getString("ApproximationValuesDialog.transfer.datum_and_new.label", "Transfer adjusted coordinates of datum and new points as well as additional parameters");
		String transferDatumAndNewPointsResultsTooltip = i18n.getString("ApproximationValuesDialog.transfer.datum_and_new.title", "If checked, adjusted coordinates of datum and new points as well as additional parameters will be transferred to apprixmation values");

		String transferNewPointsResultsLabel   = i18n.getString("ApproximationValuesDialog.transfer.new.label", "Transfer adjusted coordinates of new points as well as additional parameters");
		String transferNewPointsResultsTooltip = i18n.getString("ApproximationValuesDialog.transfer.new.title", "If checked, adjusted coordinates of new points as well as additional parameters will be transferred to apprixmation values");

		this.estimateDatumAndNewPointsRadioButton = this.createRadioButton(estimateDatumAndNewPointsRadioButtonLabel, estimateDatumAndNewPointsRadioButtonTooltip);
		this.estimateNewPointsRadioButton = this.createRadioButton(estimateNewPointsRadioButtonLabel, estimateNewPointsRadioButtonTooltip);
		this.transferDatumAndNewPointsResultsRadioButton = this.createRadioButton(transferDatumAndNewPointsResultsLabel, transferDatumAndNewPointsResultsTooltip);
		this.transferNewPointsResultsRadioButton = this.createRadioButton(transferNewPointsResultsLabel, transferNewPointsResultsTooltip);

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(
				this.estimateDatumAndNewPointsRadioButton,
				this.estimateNewPointsRadioButton,
				this.transferDatumAndNewPointsResultsRadioButton,
				this.transferNewPointsResultsRadioButton			
				);

		this.estimateDatumAndNewPointsRadioButton.setSelected(true);

		VBox box = new VBox(10);
		box.setMaxWidth(Double.MAX_VALUE);
		box.setAlignment(Pos.CENTER_LEFT);
		box.getChildren().addAll(
				this.estimateDatumAndNewPointsRadioButton,
				this.estimateNewPointsRadioButton,
				new Region(),
				this.transferDatumAndNewPointsResultsRadioButton,
				this.transferNewPointsResultsRadioButton				
				);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				estimateDatumAndNewPointsRadioButton.requestFocus();
			}
		});

		return box;
	}

	private RadioButton createRadioButton(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxWidth(Double.MAX_VALUE);
		return radioButton;
	}

	private void process() {
		this.reset();
		// Try to estimate approx. values
		SQLApproximationManager approximationManager = SQLManager.getInstance().getApproximationManager();
		this.approximationEstimationTask = new ApproximationEstimationTask(approximationManager);
		this.approximationEstimationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				if (approximationManager != null) {
					EstimationStateType state1D = approximationManager.getEstimationStatus1D();
					EstimationStateType state2D = approximationManager.getEstimationStatus2D();

					if (state1D != EstimationStateType.INTERRUPT && state2D != EstimationStateType.INTERRUPT) {

						int subCounter1d = approximationManager.getSubSystemsCounter1D();
						int subCounter2d = approximationManager.getSubSystemsCounter2D();

						Set<String> underdeterminedPointNames = new HashSet<String>(approximationManager.getUnderdeterminedPointNames1D());
						underdeterminedPointNames.addAll(approximationManager.getUnderdeterminedPointNames2D());

						Set<String> outliers = new HashSet<String>(approximationManager.getOutliers1D());
						outliers.addAll(approximationManager.getOutliers2D());

						if (subCounter1d > 1 || subCounter2d > 1) {
							OptionDialog.showErrorDialog(
									i18n.getString("ApproximationValuesDialog.message.error.subsystem.title",  "No consistent frame"),
									i18n.getString("ApproximationValuesDialog.message.error.subsystem.header", "Error, could not determine a consistent reference frame."),
									i18n.getString("ApproximationValuesDialog.message.error.subsystem.message", "Not enough pass points to transform detected subsystems to global frame.")	
									);
						}
						else if (!underdeterminedPointNames.isEmpty()) {
							TextArea textArea = new TextArea();
							int len = underdeterminedPointNames.size();
							int cnt = 1;
							for (String name : underdeterminedPointNames) {
								textArea.appendText(name);
								if (cnt++ < len)
									textArea.appendText(", ");
							}
							textArea.setEditable(false);
							textArea.setWrapText(true);

							OptionDialog.showContentDialog(AlertType.ERROR,
									i18n.getString("ApproximationValuesDialog.message.error.underdetermination.title",  "Underdetermined points"),
									String.format(i18n.getString("ApproximationValuesDialog.message.error.underdetermination.header", "Error, detect %d underdetermined points."), underdeterminedPointNames.size()),
									i18n.getString("ApproximationValuesDialog.message.error.underdetermination.message", "List of underdetermined points."),
									textArea
									);
						}
						else if (!outliers.isEmpty()) {
							TextArea textArea = new TextArea();
							int len = outliers.size();
							int cnt = 1;
							for (String name : outliers) {
								textArea.appendText(name);
								if (cnt++ < len)
									textArea.appendText(", ");
							}
							textArea.setEditable(false);
							textArea.setWrapText(true);

							OptionDialog.showContentDialog(AlertType.WARNING,
									i18n.getString("ApproximationValuesDialog.message.error.subsystem.exception.title",  "Badly conditioned points"),
									String.format(i18n.getString("ApproximationValuesDialog.message.error.subsystem.exception.header", "Error, detect %d badly conditioned points.\r\nPlease check related observations."), outliers.size()),
									i18n.getString("ApproximationValuesDialog.message.error.subsystem.exception.message", "List of badly conditioned points."),
									textArea
									);
						}
					}
					approximationManager.clearAll();
				}

				MultipleSelectionModel<TreeItem<TreeItemValue>> selectionModel = UITreeBuilder.getInstance().getTree().getSelectionModel();
				TreeItem<TreeItemValue> treeItem = selectionModel.getSelectedItem();
				selectionModel.clearSelection();
				if (treeItem != null)
					selectionModel.select(treeItem);
				else
					selectionModel.select(0);
				dialog.hide();
			}
		});

		this.approximationEstimationTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = approximationEstimationTask.getException(); 
				if (throwable != null) {
					if (throwable instanceof SQLException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.title",  "Unexpected Error"),
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.header", "Error, estimation of approximation values failed."),
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.message", "An exception has occurred during estimation of approximation."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof PointTypeMismatchException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("ApproximationValuesDialog.message.error.pointtypemismatch.exception.title",  "Initialization error"),
										i18n.getString("ApproximationValuesDialog.message.error.pointtypemismatch.exception.header", "Error, the project contains uncombinable point typs, i.e. datum points as well as reference or stochastic points."),
										i18n.getString("ApproximationValuesDialog.message.error.pointtypemismatch.exception.message", "An exception has occurred during estimation of approximation."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof UnderDeterminedPointException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								UnderDeterminedPointException e = (UnderDeterminedPointException)throwable;
								OptionDialog.showThrowableDialog (
										i18n.getString("ApproximationValuesDialog.message.error.underdeterminded.exception.title",  "Initialization error"),
										String.format(i18n.getString("ApproximationValuesDialog.message.error.underdeterminded.exception.header", "Error, the point %s of dimension %d has only %d observations and is indeterminable."), e.getPointName(), e.getDimension(), e.getNumberOfObservations()),
										i18n.getString("ApproximationValuesDialog.message.error.underdeterminded.exception.message", "An exception has occurred during estimation of approximation."),
										throwable
										);
							}
						});
					}
					else {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.title",  "Unexpected Error"),
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.header", "Error, estimation of approximation values failed."),
										i18n.getString("ApproximationValuesDialog.message.error.approximation_value.exception.message", "An exception has occurred during estimation of approximation."),
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

		Thread th = new Thread(this.approximationEstimationTask);
		th.setDaemon(true);
		th.start();
	}
}
