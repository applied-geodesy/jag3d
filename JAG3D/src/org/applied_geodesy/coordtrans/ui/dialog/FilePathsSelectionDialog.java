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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.coordtrans.ui.CoordTrans;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.io.reader.PositionFileReader;
import org.applied_geodesy.coordtrans.ui.utils.UiUtil;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.io.DefaultFileChooser;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class FilePathsSelectionDialog {
	
	private class TransformationTypeChangeListener implements ChangeListener<Toggle> {

		@Override
		public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
			if (newToggle.getUserData() != null && newToggle.getUserData() instanceof TransformationType)
				setTransformationType((TransformationType)newToggle.getUserData());
		}
	}
	
	private class BrowseActionEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() instanceof Button && ((Button)event.getSource()).getUserData() != null && ((Button)event.getSource()).getUserData() instanceof FrameType) {
				Button button = (Button)event.getSource();
				FrameType frameType = (FrameType)button.getUserData();
				setSelectedFile(frameType);
			}
		}
	}
	
	public class FilePathPair {
		private final Path sourceFilePath, targetFilePath;
		private final TransformationType transformationType;
		private FilePathPair(TransformationType transformationType, Path sourceFilePath, Path targetFilePath) {
			this.sourceFilePath = sourceFilePath;
			this.targetFilePath = targetFilePath;
			this.transformationType = transformationType;
		}
		
		public final Path getSourceFilePath() {
			return this.sourceFilePath;
		}
		
		public final Path getTargetFilePath() {
			return this.targetFilePath;
		}
		
		public final TransformationType getTransformationType() {
			return this.transformationType;
		}
	}

	private I18N i18n = I18N.getInstance();
	private static FilePathsSelectionDialog filePathsSelectionDialog = new FilePathsSelectionDialog();
	private Dialog<FilePathPair> dialog = null;
	private Window window;
	private TransformationType transformationType = TransformationType.SPATIAL;
	private TextField sourceSystemPathTextField, targetSystemPathTextField; 
	
	public static void setOwner(Window owner) {
		filePathsSelectionDialog.window = owner;
	}

	public static Optional<FilePathPair> showAndWait() {
		filePathsSelectionDialog.init();
		
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					filePathsSelectionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) filePathsSelectionDialog.dialog.getDialogPane().getScene().getWindow();
//					stage.setMinHeight(400);
//					stage.setMinWidth(800);
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return filePathsSelectionDialog.dialog.showAndWait();
	}
	
	private void setTransformationType(TransformationType transformationType) {
		this.transformationType = transformationType;
	}

	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<FilePathPair>();
		this.dialog.setTitle(i18n.getString("FilePathsSelectionDialog.title", "File selection"));
		this.dialog.setHeaderText(String.format(Locale.ENGLISH, i18n.getString("FilePathsSelectionDialog.header", "File selection for source and target system"), ""));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, FilePathPair>() {
			@Override
			public FilePathPair call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					FilePathPair filePathPair = getFilePathPair();
					return filePathPair;
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		GridPane gridPane = UiUtil.createGridPane();
	
		Label sourceSystemPathLabel   = new Label(i18n.getString("FilePathsSelectionDialog.frame.source.file.path.label",  "Source system:"));
		Label targetSystemPathLabel   = new Label(i18n.getString("FilePathsSelectionDialog.frame.target.file.path.label",  "Target system:"));
		Label transformationTypeLabel = new Label(i18n.getString("FilePathsSelectionDialog.transformation.type.label",     "Transformation:"));
		sourceSystemPathLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		targetSystemPathLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		transformationTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		this.sourceSystemPathTextField = UiUtil.createTextField(
				i18n.getString("FilePathsSelectionDialog.frame.source.file.path.tooltip", "Selected path to source system file"),
				i18n.getString("FilePathsSelectionDialog.frame.source.file.path.prompt",  "Source system file path")
		);
		
		this.targetSystemPathTextField = UiUtil.createTextField(
				i18n.getString("FilePathsSelectionDialog.frame.target.file.path.tooltip", "Selected path to target system file"),
				i18n.getString("FilePathsSelectionDialog.frame.target.file.path.prompt",  "Target system file path")
		);
		
		Button sourceSystemPathButton = UiUtil.createButton(
				i18n.getString("FilePathsSelectionDialog.frame.source.file.button.label",   "Browse"),
				i18n.getString("FilePathsSelectionDialog.frame.source.file.button.tooltip",  "Select path to source system file")
		);
		
		Button targetSystemPathButton = UiUtil.createButton(
				i18n.getString("FilePathsSelectionDialog.frame.target.file.button.label",   "Browse"),
				i18n.getString("FilePathsSelectionDialog.frame.target.file.button.tooltip",  "Select path to target system file")
		);
		
		ToggleGroup group = new ToggleGroup();
		
		RadioButton heightTransformationRadioButton = UiUtil.createRadioButton(
				i18n.getString("FilePathsSelectionDialog.transformation.type.height.label",   "Height"),
				i18n.getString("FilePathsSelectionDialog.transformation.type.height.tooltip", "If selected, a height transformation using one-dimensional points is performed"),
				group
		);
		heightTransformationRadioButton.setUserData(TransformationType.HEIGHT);
		
		RadioButton planarTransformationRadioButton = UiUtil.createRadioButton(
				i18n.getString("FilePathsSelectionDialog.transformation.type.planar.label",   "Planar"),
				i18n.getString("FilePathsSelectionDialog.transformation.type.planar.tooltip", "If selected, a planar transformation using two-dimensional points is performed"),
				group
		);
		planarTransformationRadioButton.setUserData(TransformationType.PLANAR);
		
		RadioButton spatialTransformationRadioButton = UiUtil.createRadioButton(
				i18n.getString("FilePathsSelectionDialog.transformation.type.spatial.label",   "Spatial"),
				i18n.getString("FilePathsSelectionDialog.transformation.type.spatial.tooltip", "If selected, a spatial transformation using three-dimensional points is performed"),
				group
		);
		spatialTransformationRadioButton.setUserData(TransformationType.SPATIAL);
		spatialTransformationRadioButton.setSelected(true);
		
		HBox groupBox = new HBox(10);
		HBox.setHgrow(heightTransformationRadioButton,  Priority.NEVER);
		HBox.setHgrow(planarTransformationRadioButton,  Priority.NEVER);
		HBox.setHgrow(spatialTransformationRadioButton, Priority.NEVER);
		groupBox.getChildren().setAll(
				heightTransformationRadioButton,
				planarTransformationRadioButton,
				spatialTransformationRadioButton
		);
		
		group.selectedToggleProperty().addListener(new TransformationTypeChangeListener());
		
		sourceSystemPathButton.setUserData(FrameType.SOURCE);
		targetSystemPathButton.setUserData(FrameType.TARGET);

		BrowseActionEventHandler browseActionEventHandler = new BrowseActionEventHandler();
		sourceSystemPathButton.setOnAction(browseActionEventHandler);
		targetSystemPathButton.setOnAction(browseActionEventHandler);
		
		this.sourceSystemPathTextField.setPrefWidth(250);
		this.targetSystemPathTextField.setPrefWidth(250);
		
		sourceSystemPathLabel.setLabelFor(this.sourceSystemPathTextField);
		targetSystemPathLabel.setLabelFor(this.targetSystemPathTextField);
		transformationTypeLabel.setLabelFor(groupBox);
		
		GridPane.setHgrow(sourceSystemPathLabel, Priority.NEVER);
		GridPane.setHgrow(targetSystemPathLabel, Priority.NEVER);
		GridPane.setHgrow(transformationTypeLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.sourceSystemPathTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.targetSystemPathTextField, Priority.ALWAYS);
		
		GridPane.setHgrow(sourceSystemPathButton, Priority.SOMETIMES);
		GridPane.setHgrow(targetSystemPathButton, Priority.SOMETIMES);
		GridPane.setHgrow(groupBox, Priority.SOMETIMES);
		
		GridPane.setMargin(sourceSystemPathLabel, new Insets(5, 5, 5, 0));
		GridPane.setMargin(targetSystemPathLabel, new Insets(5, 5, 5, 0));
		GridPane.setMargin(transformationTypeLabel, new Insets(10, 5, 5, 0));
		
		GridPane.setMargin(this.sourceSystemPathTextField, new Insets(5, 5, 5, 5));
		GridPane.setMargin(this.targetSystemPathTextField, new Insets(5, 5, 5, 5));
		
		GridPane.setMargin(sourceSystemPathButton, new Insets(5, 0, 5, 5));
		GridPane.setMargin(targetSystemPathButton, new Insets(5, 0, 5, 5));
		GridPane.setMargin(groupBox, new Insets(10, 5, 5, 5));
		
		int row = 0;
		gridPane.add(sourceSystemPathLabel,           0, row); // column, row, columnspan, rowspan
		gridPane.add(this.sourceSystemPathTextField,  1, row);
		gridPane.add(sourceSystemPathButton,          2, row++);
		
		gridPane.add(targetSystemPathLabel,           0, row); 
		gridPane.add(this.targetSystemPathTextField,  1, row);
		gridPane.add(targetSystemPathButton,          2, row++);
		
		gridPane.add(transformationTypeLabel,         0, row); 
		gridPane.add(groupBox,                        1, row++, 2, 1); 
		
		return gridPane;
	}
	
	private void setSelectedFile(FrameType frameType) {
		String title = "";
		String fileName = "";
		File selectedFile = null;
		switch (frameType) {
		case SOURCE:
			title = i18n.getString("FilePathsSelectionDialog.frame.source.file.selection.title",   "Source system file");
			fileName = this.sourceSystemPathTextField.getText();
			break;
		case TARGET:
			title = i18n.getString("FilePathsSelectionDialog.frame.target.file.selection.title",   "Target system file");
			fileName = this.targetSystemPathTextField.getText();
			break;	
		}
		
		selectedFile = DefaultFileChooser.showOpenDialog(
				CoordTrans.getStage(), 
				title, 
				fileName, 
				PositionFileReader.getExtensionFilters()
		);
		
		if (selectedFile != null) {
			switch (frameType) {
			case SOURCE:
				this.sourceSystemPathTextField.setText(selectedFile.toPath().toAbsolutePath().normalize().toString());
				break;
			case TARGET:
				this.targetSystemPathTextField.setText(selectedFile.toPath().toAbsolutePath().normalize().toString());
				break;	
			}
		}
	}
	
	private FilePathPair getFilePathPair() {
		try {
			Path sourceFilePath = this.convertString2Path(this.sourceSystemPathTextField.getText());
			Path targetFilePath = this.convertString2Path(this.targetSystemPathTextField.getText());
			
			return new FilePathPair(this.transformationType, sourceFilePath, targetFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("FilePathsSelectionDialog.message.error.load.exception.title", "I/O-Error"),
							i18n.getString("FilePathsSelectionDialog.message.error.load.exception.header", "Error, could not read selected files. Please check file paths."),
							i18n.getString("FilePathsSelectionDialog.message.error.load.exception.message", "An exception has occurred during file import."),
							e
							);
				}
			});
		}
		return null;
	}
	
	private Path convertString2Path(String str) throws IllegalArgumentException, InvalidPathException, SecurityException, IOException {
		
		if (str == null || str.isBlank())
			throw new IllegalArgumentException("Error, selected file is empty!");
		
		Path path = Paths.get(str);
		if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path))
			throw new IOException("Error, cannot access the selected file " + path + "!");
		
		return path;
	}
}
