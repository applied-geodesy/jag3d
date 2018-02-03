package org.applied_geodesy.jag3d.ui.dialog;

import java.util.List;
import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.table.UICongruentPointTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class CongruentPointDialog {
	
	private class DimensionChangeListener implements ChangeListener<Boolean> {		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			okButton.setDisable(!dimensionOneCheckBox.isSelected() && !dimensionTwoCheckBox.isSelected() && !dimensionThreeCheckBox.isSelected());
		}
	}
	
	private class ProcessSQLTask extends Task<List<TerrestrialObservationRow>> {

		@Override
		protected List<TerrestrialObservationRow> call() throws Exception {
			try {
				preventClosing = true;
				progressIndicatorPane.setVisible(true);
				settingPane.setDisable(true);
				okButton.setDisable(true);
				double distance = snapDistanceTextField.getNumber();
				boolean dimension1D = dimensionOneCheckBox.isSelected();
				boolean dimension2D = dimensionTwoCheckBox.isSelected();
				boolean dimension3D = dimensionThreeCheckBox.isSelected();
				List<TerrestrialObservationRow> rows = SQLManager.getInstance().getCongruentPoints(distance, dimension1D, dimension2D, dimension3D);
				return rows;
			}
			finally {
				progressIndicatorPane.setVisible(false);
				settingPane.setDisable(false);
				okButton.setDisable(false);
				preventClosing = false;
			}
		}
		
		@Override 
		protected void succeeded() {
			preventClosing = false;
			super.succeeded();
		}

		@Override 
		protected void failed() {
			preventClosing = false;
			super.failed();
		}
	}

	private static I18N i18n = I18N.getInstance();
	private static UICongruentPointTableBuilder tableBuilder = UICongruentPointTableBuilder.getInstance();
	private static CongruentPointDialog congruentPointDialog = new CongruentPointDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private DoubleTextField snapDistanceTextField;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private boolean preventClosing = false;
	private ProcessSQLTask processSQLTask = null;
	private CheckBox dimensionOneCheckBox, dimensionTwoCheckBox, dimensionThreeCheckBox;
	private CongruentPointDialog() {}
	private Button okButton;
	private Node progressIndicatorPane, settingPane;
	
	public static void setOwner(Window owner) {
		congruentPointDialog.window = owner;
	}
	
	public static Optional<Void> showAndWait() {
		congruentPointDialog.init();
		return congruentPointDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("CongruentPointDialog.title", "Congruent points"));
		this.dialog.setHeaderText(i18n.getString("CongruentPointDialog.header", "Find congruent points in project"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
		this.okButton = (Button)this.dialog.getDialogPane().lookupButton(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
//		this.dialog.initStyle(StageStyle.UTILITY);
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

		this.okButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				preventClosing = true;
				process();
				event.consume();
			}
		});
		
		this.settingPane = this.createSettingPane();
		this.progressIndicatorPane = this.createProgressIndicatorPane();
		this.progressIndicatorPane.setVisible(false);
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		stackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		stackPane.getChildren().addAll(this.settingPane, this.progressIndicatorPane);

		this.dialog.getDialogPane().setContent(stackPane);
	}
	
	private Node createSettingPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(5, 5, 5, 5)); // oben, recht, unten, links
		
		String labelSnap   = i18n.getString("CongruentPointDialog.snap.label", "Snap distance:");
		String tooltipSnap = i18n.getString("CongruentPointDialog.snap.tooltip", "Snap distance to find congruent points");
		
		String labelDimension   = i18n.getString("CongruentPointDialog.dimension.label", "Point dimension:");
		String label1D = i18n.getString("CongruentPointDialog.dimension.1d.label", "1D");
		String label2D = i18n.getString("CongruentPointDialog.dimension.2d.label", "2D");
		String label3D = i18n.getString("CongruentPointDialog.dimension.3d.label", "3D");
		
		String tooltip1D = i18n.getString("CongruentPointDialog.dimension.1d.tooltip", "If checked, 1D points will be included into search");
		String tooltip2D = i18n.getString("CongruentPointDialog.dimension.2d.tooltip", "If checked, 2D points will be included into search");
		String tooltip3D = i18n.getString("CongruentPointDialog.dimension.3d.tooltip", "If checked, 3D points will be included into search");
		
		this.snapDistanceTextField = new DoubleTextField(0.15, CellValueType.LENGTH_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.snapDistanceTextField.setTooltip(new Tooltip(tooltipSnap));
		this.snapDistanceTextField.setMinWidth(100);
		this.snapDistanceTextField.setMaxWidth(500);

		this.dimensionOneCheckBox   = new CheckBox(); 
		this.dimensionTwoCheckBox   = new CheckBox(); 
		this.dimensionThreeCheckBox = new CheckBox();
		
		this.dimensionOneCheckBox.setGraphic(new Label(label1D));
		this.dimensionTwoCheckBox.setGraphic(new Label(label2D));
		this.dimensionThreeCheckBox.setGraphic(new Label(label3D));
		
		this.dimensionOneCheckBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.dimensionTwoCheckBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.dimensionThreeCheckBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		DimensionChangeListener dimensionChangeListener = new DimensionChangeListener();
		this.dimensionOneCheckBox.selectedProperty().addListener(dimensionChangeListener);
		this.dimensionTwoCheckBox.selectedProperty().addListener(dimensionChangeListener);
		this.dimensionThreeCheckBox.selectedProperty().addListener(dimensionChangeListener);
		
		this.dimensionOneCheckBox.setTooltip(new Tooltip(tooltip1D));
		this.dimensionTwoCheckBox.setTooltip(new Tooltip(tooltip2D));
		this.dimensionThreeCheckBox.setTooltip(new Tooltip(tooltip3D));
		
		this.dimensionTwoCheckBox.setSelected(true);
		this.dimensionThreeCheckBox.setSelected(true);

		TableView<?> table = tableBuilder.getTable();
		table.setMaxWidth(Double.MAX_VALUE);
		
		Label snapLabel = new Label(labelSnap);
		snapLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label dimensionLabel = new Label(labelDimension);
		dimensionLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		gridPane.add(snapLabel,                   0, 0);
		gridPane.add(this.snapDistanceTextField,  1, 0, 4, 1);
		gridPane.add(dimensionLabel,              0, 1);
		gridPane.add(this.dimensionOneCheckBox,   1, 1);
		gridPane.add(this.dimensionTwoCheckBox,   2, 1);
		gridPane.add(this.dimensionThreeCheckBox, 3, 1);
		
		VBox vbox = new VBox();
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(5, 5, 5, 5)); // oben, recht, unten, links
		vbox.getChildren().setAll(gridPane, table);
		vbox.setPrefHeight(300);
		
		return vbox;
	}
	
	private Node createProgressIndicatorPane() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().setAll(this.progressIndicator);
		return box;
	}
	
	private void process() {
		this.processSQLTask = new ProcessSQLTask();
		this.processSQLTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				List<TerrestrialObservationRow> rows = processSQLTask.getValue();
				if (rows != null && !rows.isEmpty()) {
					tableBuilder.getTable().getItems().setAll(rows);
				}
				else {
					tableBuilder.getTable().getItems().setAll(tableBuilder.getEmptyRow());
				}
			}
		});
		
		this.processSQLTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = processSQLTask.getException(); 
				if (throwable != null) {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							OptionDialog.showThrowableDialog (
									i18n.getString("CongruentPointDialog.message.error.sql.exception.title",  "SQL-Error"),
									i18n.getString("CongruentPointDialog.message.error.sql.exception.header", "Error, database request is failed."),
									i18n.getString("CongruentPointDialog.message.error.sql.exception.message", "An exception occured during network adjustment."),
									throwable
									);
						}
					});
					throwable.printStackTrace();
				}
				UITreeBuilder.getInstance().getTree().getSelectionModel().select(0);
			}
		});

		Thread th = new Thread(this.processSQLTask);
		th.setDaemon(true);
		th.start();
	}
}
