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

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.io.reader.ImportOption;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.ui.dialog.OptionDialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ImportOptionDialog {
	private I18N i18n = I18N.getInstance();
	private static ImportOptionDialog importOptionDialog = new ImportOptionDialog();
	private Dialog<ImportOption> dialog = null;
	private Window window;
	private CheckBox levelingCheckBox; 
	private CheckBox directionCheckBox, horizontalDistanceCheckBox;
	private CheckBox slopeDistanceCheckBox, zenithAngleCheckBox;
	private CheckBox gnss1DCheckBox, gnss2DCheckBox, gnss3DCheckBox;
	private ImportOptionDialog() {}

	public static void setOwner(Window owner) {
		importOptionDialog.window = owner;
	}

	public static Optional<ImportOption> showAndWait() {
		importOptionDialog.init();
		importOptionDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					importOptionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) importOptionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return importOptionDialog.dialog.showAndWait();
	}
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<ImportOption>();
		this.dialog.setTitle(i18n.getString("ImportOptionDialog.title", "Import preferences"));
		this.dialog.setHeaderText(i18n.getString("ImportOptionDialog.header", "Grouping observations by station-ids"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, ImportOption>() {
			@Override
			public ImportOption call(ButtonType buttonType) {
				ImportOption importOption = ImportOption.getInstance();
				if (buttonType == ButtonType.OK) {
					save();
				}
				return importOption;
			}
		});
	}
	
	private Node createPane() {
		String labelLevelingSeparation   = i18n.getString("ImportOptionDialog.separation.leveling.label",   "Height differences");
		String tooltipLevelingSeparation = i18n.getString("ImportOptionDialog.separation.leveling.tooltip", "If checked, height differences are grouped by station-ids");
		
		String labelDirectionSeparation   = i18n.getString("ImportOptionDialog.separation.direction.label",   "Directions");
		String tooltipDirectionSeparation = i18n.getString("ImportOptionDialog.separation.direction.tooltip", "If checked, set of directions are grouped by station-ids");
		
		String labelHorizontalDistanceSeparation   = i18n.getString("ImportOptionDialog.separation.horizontal_distance.label",   "Horizontal distances");
		String tooltipHorizontalDistanceSeparation = i18n.getString("ImportOptionDialog.separation.horizontal_distance.tooltip", "If checked, horizontal distances are grouped by station-ids");

		String labelSlopeDistanceSeparation   = i18n.getString("ImportOptionDialog.separation.slope_distance.label",   "Slope distances");
		String tooltipSlopeDistanceSeparation = i18n.getString("ImportOptionDialog.separation.slope_distance.tooltip", "If checked, slope distances are grouped by station-ids");

		String labelZenithAngleSeparation   = i18n.getString("ImportOptionDialog.separation.zenith_angle.label",   "Zenith angles");
		String tooltipZenithAngleSeparation = i18n.getString("ImportOptionDialog.separation.zenith_angle.tooltip", "If checked, zenith angles are grouped by station-ids");
		
		String labelGNSS1DSeparation   = i18n.getString("ImportOptionDialog.separation.gnss.1d.label",   "GNSS baselines 1D");
		String tooltipGNSS1DSeparation = i18n.getString("ImportOptionDialog.separation.gnss.1d.tooltip", "If checked, vertical GNSS baselines are grouped by station-ids");
		
		String labelGNSS2DSeparation   = i18n.getString("ImportOptionDialog.separation.gnss.2d.label",   "GNSS baselines 2D");
		String tooltipGNSS2DSeparation = i18n.getString("ImportOptionDialog.separation.gnss.2d.tooltip", "If checked, horizontal GNSS baselines are grouped by station-ids");
		
		String labelGNSS3DSeparation   = i18n.getString("ImportOptionDialog.separation.gnss.3d.label",   "GNSS baselines 3D");
		String tooltipGNSS3DSeparation = i18n.getString("ImportOptionDialog.separation.gnss.3d.tooltip", "If checked, spatial GNSS baselines are grouped by station-ids");

		this.levelingCheckBox = this.createCheckBox(labelLevelingSeparation, tooltipLevelingSeparation);
		this.levelingCheckBox.setPadding(new Insets(0,0,10,0));
		
		this.directionCheckBox          = this.createCheckBox(labelDirectionSeparation, tooltipDirectionSeparation);
		this.horizontalDistanceCheckBox = this.createCheckBox(labelHorizontalDistanceSeparation, tooltipHorizontalDistanceSeparation);
		this.horizontalDistanceCheckBox.setPadding(new Insets(0,0,10,0));
		
		this.slopeDistanceCheckBox = this.createCheckBox(labelSlopeDistanceSeparation, tooltipSlopeDistanceSeparation);
		this.zenithAngleCheckBox   = this.createCheckBox(labelZenithAngleSeparation, tooltipZenithAngleSeparation);
		this.zenithAngleCheckBox.setPadding(new Insets(0,0,10,0));
		
		this.gnss1DCheckBox = this.createCheckBox(labelGNSS1DSeparation, tooltipGNSS1DSeparation);
		this.gnss2DCheckBox = this.createCheckBox(labelGNSS2DSeparation, tooltipGNSS2DSeparation);
		this.gnss3DCheckBox = this.createCheckBox(labelGNSS3DSeparation, tooltipGNSS3DSeparation);
				
		
		GridPane gridPane = new GridPane();
//		gridPane.setMinWidth(250);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 15, 5, 15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);

		GridPane.setHgrow(this.levelingCheckBox,           Priority.ALWAYS);
		GridPane.setHgrow(this.directionCheckBox,          Priority.ALWAYS);
		GridPane.setHgrow(this.horizontalDistanceCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.slopeDistanceCheckBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.zenithAngleCheckBox,        Priority.ALWAYS);
		
		GridPane.setHgrow(this.gnss1DCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.gnss2DCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.gnss3DCheckBox, Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(this.levelingCheckBox,           0, ++row);
		gridPane.add(this.directionCheckBox,          0, ++row);
		gridPane.add(this.horizontalDistanceCheckBox, 0, ++row);
		gridPane.add(this.slopeDistanceCheckBox,      0, ++row);
		gridPane.add(this.zenithAngleCheckBox,        0, ++row);
		
		gridPane.add(this.gnss1DCheckBox, 0, ++row);
		gridPane.add(this.gnss2DCheckBox, 0, ++row);
		gridPane.add(this.gnss3DCheckBox, 0, ++row);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				levelingCheckBox.requestFocus();
			}
		});

		return gridPane;
	}

	private void load() {
		try {
			ImportOption importOption = ImportOption.getInstance();
			SQLManager.getInstance().loadImportPreferences();
			
			this.levelingCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.LEVELING));
			
			this.directionCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.DIRECTION));
			this.horizontalDistanceCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.HORIZONTAL_DISTANCE));
			
			this.slopeDistanceCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.SLOPE_DISTANCE));
			this.zenithAngleCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.ZENITH_ANGLE));
			
			this.gnss1DCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.GNSS1D));
			this.gnss2DCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.GNSS2D));
			this.gnss3DCheckBox.setSelected(importOption.isGroupSeparation(ObservationType.GNSS3D));
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					levelingCheckBox.requestFocus();
				}
			});
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ImportOptionDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ImportOptionDialog.message.error.load.exception.header", "Error, could not load import preferences from database."),
							i18n.getString("ImportOptionDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private void save() {
		try {
			ImportOption importOption = ImportOption.getInstance();
			importOption.setGroupSeparation(ObservationType.LEVELING, this.levelingCheckBox.isSelected());
			
			importOption.setGroupSeparation(ObservationType.DIRECTION, this.directionCheckBox.isSelected());
			importOption.setGroupSeparation(ObservationType.HORIZONTAL_DISTANCE, this.horizontalDistanceCheckBox.isSelected());
			
			importOption.setGroupSeparation(ObservationType.SLOPE_DISTANCE, this.slopeDistanceCheckBox.isSelected());
			importOption.setGroupSeparation(ObservationType.ZENITH_ANGLE, this.zenithAngleCheckBox.isSelected());
			
			importOption.setGroupSeparation(ObservationType.GNSS1D, this.gnss1DCheckBox.isSelected());
			importOption.setGroupSeparation(ObservationType.GNSS2D, this.gnss2DCheckBox.isSelected());
			importOption.setGroupSeparation(ObservationType.GNSS3D, this.gnss3DCheckBox.isSelected());
			
			SQLManager.getInstance().saveImportPreferences();
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ImportOptionDialog.message.error.save.exception.title",   "Unexpected SQL-Error"),
							i18n.getString("ImportOptionDialog.message.error.save.exception.header",  "Error, could not save import preferences to database."),
							i18n.getString("ImportOptionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
		finally {
			TableRowHighlight.getInstance().refreshTables();
		}
	}
	
	private CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinHeight(Control.USE_PREF_SIZE);
		checkBox.setMaxHeight(Double.MAX_VALUE);
		return checkBox;
	}
}
