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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultAverageThreshold;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.sql.IllegalProjectionPropertyException;
import org.applied_geodesy.adjustment.network.sql.SQLAdjustmentManager;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.table.UIAverageObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.AveragedObservationRow;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class AverageDialog {

	private class DoubleValueChangeListener implements ChangeListener<Double> {
		private ObservationType observationType;

		private DoubleValueChangeListener(ObservationType observationType) {
			this.observationType = observationType;
		}

		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (!ignoreChanges && newValue != null && newValue > 0)
				save(this.observationType, newValue);
		}
	}

	private class AverageEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			okButton.setDisable(true);
			process();
			event.consume();
		}
	}

	private class AverageTask extends Task<List<Observation>> {
		private SQLAdjustmentManager dataBaseManager;
		private AverageTask(SQLAdjustmentManager dataBaseManager) {
			this.dataBaseManager = dataBaseManager;
		}

		@Override
		protected List<Observation> call() throws Exception {
			List<Observation> observations = null;
			try {
				preventClosing = true;
				progressIndicatorPane.setVisible(true);
				thresholdPane.setDisable(true);

				observations = this.dataBaseManager.averageDetermination(false);
				return observations;
			}

			finally {
				okButton.setDisable(false);
				progressIndicatorPane.setVisible(false);
				thresholdPane.setDisable(false);
				if (observations == null || observations.isEmpty())
					preventClosing = false;
			}
		}
	}

	private I18N i18n = I18N.getInstance();
	private static AverageDialog averageDialog = new AverageDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private boolean ignoreChanges = false;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private boolean preventClosing = false;
	private AverageTask averageTask = null;
	private Button okButton;
	private Map<ObservationType, DoubleTextField> thresholdFieldMap = new HashMap<ObservationType, DoubleTextField>(10);
	private UIAverageObservationTableBuilder tableBuilder = UIAverageObservationTableBuilder.getInstance();

	private Node thresholdPane, progressIndicatorPane;

	private AverageDialog() {}

	public static void setOwner(Window owner) {
		averageDialog.window = owner;
	}

	public static Optional<Void> showAndWait() {
		averageDialog.init();
		averageDialog.load();
		averageDialog.reset();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					averageDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) averageDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return averageDialog.dialog.showAndWait();
	}

	private void reset() {
		this.preventClosing = false;
		this.thresholdPane.setDisable(false);
		this.progressIndicatorPane.setVisible(false);
		this.okButton.setDisable(false);
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("AverageDialog.title", "Averaging"));
		this.dialog.setHeaderText(i18n.getString("AverageDialog.header", "Averaging repeated observations"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
		this.okButton = (Button)this.dialog.getDialogPane().lookupButton(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.setResizable(true);

		this.dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (preventClosing)
					event.consume();
			}
		});

		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				if (preventClosing)
					event.consume();
			}
		});

		this.thresholdPane = this.createThresholdPane();
		this.progressIndicatorPane = this.createProgressIndicatorPane();
		this.progressIndicatorPane.setVisible(false);
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		stackPane.setMaxSize(Double.MAX_VALUE, Region.USE_PREF_SIZE);
		stackPane.getChildren().addAll(this.thresholdPane, this.progressIndicatorPane);

		this.okButton.addEventFilter(ActionEvent.ACTION, new AverageEvent());
		this.dialog.getDialogPane().setContent(stackPane);
	}

	private Node createProgressIndicatorPane() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().setAll(this.progressIndicator);
		return box;
	}

	private VBox createThresholdPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(5, 5, 5, 5)); // oben, recht, unten, links
		gridPane.setAlignment(Pos.TOP_CENTER);

		int row = 0;
		this.addRow(gridPane, row++, ObservationType.LEVELING);
		this.addRow(gridPane, row++, ObservationType.DIRECTION);
		this.addRow(gridPane, row++, ObservationType.HORIZONTAL_DISTANCE);
		this.addRow(gridPane, row++, ObservationType.SLOPE_DISTANCE);
		this.addRow(gridPane, row++, ObservationType.ZENITH_ANGLE);

		this.addRow(gridPane, row++, ObservationType.GNSS1D);
		this.addRow(gridPane, row++, ObservationType.GNSS2D);
		this.addRow(gridPane, row++, ObservationType.GNSS3D);

		Label warningLabel = new Label(i18n.getString("AverageDialog.warning.label", "Please note: Averaging repeated measurements will reduce\r\nthe number of observations and is an irreversible process"));
		warningLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		warningLabel.setWrapText(true);

		VBox box = new VBox();
		VBox.setVgrow(gridPane, Priority.ALWAYS);
		box.setPadding(new Insets(5, 10, 5, 10));
		box.setSpacing(10);
		box.getChildren().addAll(
				warningLabel,
				this.createTitledPane(i18n.getString("AverageDialog.threshold.title", "Threshold w.r.t median"), gridPane)
				);
		return box;
	}

	private void addRow(GridPane parent, int row, ObservationType observationType) {
		CellValueType valueType = null;
		String labelText = null;
		double value = DefaultAverageThreshold.getThreshold(observationType);
		switch(observationType) {
		case LEVELING:
			labelText = i18n.getString("AverageDialog.leveling.label", "Leveling:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;

		case DIRECTION:
			labelText = i18n.getString("AverageDialog.direction.label", "Direction:");
			valueType = CellValueType.ANGLE_RESIDUAL;
			break;

		case ZENITH_ANGLE:
			labelText = i18n.getString("AverageDialog.zenith_angle.label", "Zenith angle:");
			valueType = CellValueType.ANGLE_RESIDUAL;
			break;

		case HORIZONTAL_DISTANCE:
			labelText = i18n.getString("AverageDialog.horizontal_distance.label", "Horizontal distance:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;

		case SLOPE_DISTANCE:
			labelText = i18n.getString("AverageDialog.slope_distance.label", "Slope distance:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;

		case GNSS1D:
			labelText = i18n.getString("AverageDialog.gnss.1d.label", "GNSS baseline 1D:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;

		case GNSS2D:
			labelText = i18n.getString("AverageDialog.gnss.2d.label", "GNSS baseline 2D:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;

		case GNSS3D:
			labelText = i18n.getString("AverageDialog.gnss.3d.label", "GNSS baseline 3D:");
			valueType = CellValueType.LENGTH_RESIDUAL;
			break;
		}

		if (labelText != null && valueType != null && value > 0) {
			DoubleTextField thresholdField = new DoubleTextField(value, valueType, true, ValueSupport.GREATER_THAN_ZERO);
			thresholdField.setMinWidth(100);
			thresholdField.setMaxWidth(Double.MAX_VALUE);
			thresholdField.numberProperty().addListener(new DoubleValueChangeListener(observationType));
			this.thresholdFieldMap.put(observationType, thresholdField);
			Label label = new Label(labelText);
			label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

			GridPane.setHgrow(label, Priority.SOMETIMES);
			GridPane.setHgrow(thresholdField, Priority.ALWAYS);

			parent.add(label,          0, row);
			parent.add(thresholdField, 1, row);
		}
	}

	private TitledPane createTitledPane(String title, Node content) {
		TitledPane titledPane = new TitledPane();
		titledPane.setCollapsible(false);
		titledPane.setAnimated(false);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
		titledPane.setText(title);
		return titledPane;
	}

	private void load() {
		try {
			okButton.setDisable(false);

			this.ignoreChanges = true;
			for (Map.Entry<ObservationType, DoubleTextField> item : this.thresholdFieldMap.entrySet()) {
				ObservationType type = item.getKey();
				DoubleTextField field = item.getValue();
				double value = SQLManager.getInstance().getAverageThreshold(type);
				value = value > 0 ? value : DefaultAverageThreshold.getThreshold(type);
				field.setValue(value);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("AverageDialog.message.error.load.exception.title",  "Unexpected SQL-Error"),
							i18n.getString("AverageDialog.message.error.load.exception.header", "Error, could not load averaging properties from database."),
							i18n.getString("AverageDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
		finally {
			this.ignoreChanges = false;
		}
	}

	private void save(ObservationType observationType, double value) {
		try {
			SQLManager.getInstance().save(observationType, value);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("AverageDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("AverageDialog.message.error.save.exception.header", "Error, could not save averaging properties to database."),
							i18n.getString("AverageDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}

	private void process() {
		this.averageTask = new AverageTask(SQLManager.getInstance().getAdjustmentManager());
		this.averageTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				List<Observation> observations = averageTask.getValue();
				if (observations != null && !observations.isEmpty()) {
					List<AveragedObservationRow> averagedObservationRows = new ArrayList<AveragedObservationRow>(observations.size());
					for (Observation observation : observations)
						averagedObservationRows.add(new AveragedObservationRow(observation));

					tableBuilder.getTable().getItems().setAll(averagedObservationRows);
					tableBuilder.getTable().setPrefHeight(200);
					tableBuilder.getTable().setPrefWidth(250);
					String title   = i18n.getString("AverageDialog.message.error.threshold.title", "Exceeding thresholds");
					String header  = i18n.getString("AverageDialog.message.error.threshold.header", "Error, averaging could not be finished due to exceeded threshold values.\r\nPlease correct the listed observations or increase the threshold value");
					String message = i18n.getString("AverageDialog.message.error.threshold.message","List of exceeded observations");

					Platform.runLater(new Runnable() {
						@Override public void run() {
							OptionDialog.showContentDialog(AlertType.ERROR, title, header, message, tableBuilder.getTable());
						}
					});
				}
				else {
					MultipleSelectionModel<TreeItem<TreeItemValue>> selectionModel = UITreeBuilder.getInstance().getTree().getSelectionModel();
					TreeItem<TreeItemValue> treeItem = selectionModel.getSelectedItem();
					selectionModel.clearSelection();
					if (treeItem != null)
						selectionModel.select(treeItem);
					else
						selectionModel.select(0);
					dialog.hide();
				}
			}

		});

		this.averageTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = averageTask.getException(); 
				if (throwable != null) {
					if (throwable instanceof IllegalProjectionPropertyException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("AverageDialog.message.error.projection.exception.title",  "Initialization error"),
										i18n.getString("AverageDialog.message.error.projection.exception.header", "Error, the project contains unsupported projection properties."),
										i18n.getString("AverageDialog.message.error.projection.exception.message", "An exception has occurred during estimation of approximation."),
										throwable
										);
							}
						});
					}
					else if (throwable instanceof DatabaseVersionMismatchException) {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("AverageDialog.message.error.databaseversion.exception.title",  "Version error"),
										i18n.getString("AverageDialog.message.error.databaseversion.exception.header", "Error, the database version is unsupported."),
										i18n.getString("AverageDialog.message.error.databaseversion.exception.message", "An exception has occurred during network adjustment."),
										throwable
										);
							}
						});
					}
					else {
						Platform.runLater(new Runnable() {
							@Override public void run() {
								OptionDialog.showThrowableDialog (
										i18n.getString("AverageDialog.message.error.averaging.exception.title",  "Unexpected Error"),
										i18n.getString("AverageDialog.message.error.averaging.exception.header", "Error, averaging failed. An unexpected exception occurred."),
										i18n.getString("AverageDialog.message.error.averaging.exception.message", "An exception has occurred during averaging process."),
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



		Thread th = new Thread(this.averageTask);
		th.setDaemon(true);
		th.start();
	}
}
