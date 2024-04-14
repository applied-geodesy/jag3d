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

import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.io.writer.ExportOption;
import org.applied_geodesy.jag3d.ui.io.writer.ExportOption.ExportResultType;
import org.applied_geodesy.ui.dialog.OptionDialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ExportOptionDialog {
	
	private I18N i18n = I18N.getInstance();
	private static ExportOptionDialog exportOptionDialog = new ExportOptionDialog();
	private Dialog<ExportOption> dialog = null;
	private Window window;
	private RadioButton noneRadioButton, asciiRadioButton, matlabRadioButton;
	
	public static void setOwner(Window owner) {
		exportOptionDialog.window = owner;
	}

	public static Optional<ExportOption> showAndWait() {
		exportOptionDialog.init();
		exportOptionDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					exportOptionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) exportOptionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return exportOptionDialog.dialog.showAndWait();
	}
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<ExportOption>();
		this.dialog.setTitle(i18n.getString("ExportOptionDialog.title", "Export preferences"));
		this.dialog.setHeaderText(i18n.getString("ExportOptionDialog.header", "Export format of adjustment results"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, ExportOption>() {
			@Override
			public ExportOption call(ButtonType buttonType) {
				ExportOption exportOption = ExportOption.getInstance();
				if (buttonType == ButtonType.OK) {
					save();
				}
				return exportOption;
			}
		});
	}
	
	private Node createPane() {
		String labelNone   = i18n.getString("ExportOptionDialog.export.none.label",   "No data export");
		String tooltipNone = i18n.getString("ExportOptionDialog.export.none.tooltip", "If selected, no adjustment results are exported");
		
		String labelASCII   = i18n.getString("ExportOptionDialog.export.ascii.label",   "ASCII text files (*.info, *.cxx)");
		String tooltipASCII = i18n.getString("ExportOptionDialog.export.ascii.tooltip", "If selected, adjustment results are exported as simple text files in ASCII format");
		
		String labelMatlab   = i18n.getString("ExportOptionDialog.export.matlab.label",   "Binary Matlab file (*.mat)");
		String tooltipMatlab = i18n.getString("ExportOptionDialog.export.matlab.tooltip", "If selected, adjustment results are exported in Matlab specific binary format");
		
		
		this.noneRadioButton = this.createRadioButton(labelNone, tooltipNone);
//		this.noneRadioButton.setPadding(new Insets(0,0,10,0));
		
		this.asciiRadioButton = this.createRadioButton(labelASCII, tooltipASCII);
//		this.asciiRadioButton.setPadding(new Insets(0,0,10,0));
		
		this.matlabRadioButton = this.createRadioButton(labelMatlab, tooltipMatlab);
//		this.matlabRadioButton.setPadding(new Insets(0,0,10,0));
		
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(this.noneRadioButton, this.asciiRadioButton, this.matlabRadioButton);
		this.noneRadioButton.setSelected(Boolean.TRUE);
		
		GridPane gridPane = new GridPane();
//		gridPane.setMinWidth(250);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 15, 5, 15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);

		GridPane.setHgrow(this.noneRadioButton,        Priority.ALWAYS);
		GridPane.setHgrow(this.asciiRadioButton,       Priority.ALWAYS);
		GridPane.setHgrow(this.matlabRadioButton,      Priority.ALWAYS);
		
		
		int row = 0;
		gridPane.add(this.noneRadioButton,   0, ++row);
		gridPane.add(this.asciiRadioButton,  0, ++row);
		gridPane.add(this.matlabRadioButton, 0, ++row);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				noneRadioButton.requestFocus();
			}
		});

		return gridPane;
	}
	
	private void load() {
		try {
			ExportOption exportOption = ExportOption.getInstance(); 
			SQLManager.getInstance().loadExportPreferences();
			ExportResultType exportResultType = exportOption.getExportResultType();
			
			this.noneRadioButton.setSelected(exportResultType == ExportResultType.NONE);
			this.asciiRadioButton.setSelected(exportResultType == ExportResultType.ASCII);
			this.matlabRadioButton.setSelected(exportResultType == ExportResultType.MATLAB);
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ExportOptionDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ExportOptionDialog.message.error.load.exception.header", "Error, could not load export preferences from database."),
							i18n.getString("ExportOptionDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private void save() {
		try {
			ExportOption exportOption = ExportOption.getInstance(); 
			
			ExportResultType exportResultType = ExportResultType.NONE;
			if (this.asciiRadioButton.isSelected())
				exportResultType = ExportResultType.ASCII;
			else if (this.matlabRadioButton.isSelected())
				exportResultType = ExportResultType.MATLAB;
			else
				exportResultType = ExportResultType.NONE;
			
			exportOption.setExportResultType(exportResultType);

			SQLManager.getInstance().saveExportPreferences();
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ExportOptionDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ExportOptionDialog.message.error.save.exception.header", "Error, could not save export options to database."),
							i18n.getString("ExportOptionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
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
		radioButton.setMaxHeight(Double.MAX_VALUE);
		return radioButton;
	}
}
