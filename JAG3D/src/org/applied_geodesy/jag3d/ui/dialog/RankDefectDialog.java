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

import org.applied_geodesy.adjustment.network.DefectType;
import org.applied_geodesy.adjustment.network.RankDefect;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class RankDefectDialog {

	private class ScaleSelectionChangeListener implements ChangeListener<Boolean> {
		private CheckBox checkBox;
		private ScaleSelectionChangeListener(CheckBox checkBox) {
			this.checkBox = checkBox; 
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (this.checkBox == userDefinedCheckBox) {
				disableCheckBoxes(!userDefinedCheckBox.isSelected());
			}
			else {
				if ((this.checkBox == scaleXYCheckBox || this.checkBox == scaleXYZCheckBox) && this.checkBox.isSelected()) {
					scaleXCheckBox.setSelected(false);
					scaleYCheckBox.setSelected(false);
					if (scaleXYZCheckBox == this.checkBox) {
						scaleZCheckBox.setSelected(false);
						scaleXYCheckBox.setSelected(false);
					}
					else
						scaleXYZCheckBox.setSelected(false);
				}

				if ((this.checkBox == scaleXCheckBox || this.checkBox == scaleYCheckBox || this.checkBox == scaleZCheckBox) && this.checkBox.isSelected()) {
					scaleXYZCheckBox.setSelected(false);
					if (scaleZCheckBox != this.checkBox)
						scaleXYCheckBox.setSelected(false);
				}
			}
		}
	}

	private I18N i18n = I18N.getInstance();
	private static RankDefectDialog rankDefectDialog = new RankDefectDialog();
	private Dialog<RankDefect> dialog = null;
	private Window window;
	private CheckBox userDefinedCheckBox;
	private CheckBox translationXCheckBox, translationYCheckBox, translationZCheckBox;
	private CheckBox rotationXCheckBox, rotationYCheckBox, rotationZCheckBox;
	private CheckBox shearXCheckBox, shearYCheckBox, shearZCheckBox;
	private CheckBox scaleXCheckBox, scaleYCheckBox, scaleZCheckBox, scaleXYCheckBox, scaleXYZCheckBox;
	private Accordion accordion;
	private RankDefectDialog() {}

	public static void setOwner(Window owner) {
		rankDefectDialog.window = owner;
	}

	public static Optional<RankDefect> showAndWait() {
		rankDefectDialog.init();
		rankDefectDialog.load();
		rankDefectDialog.accordion.setExpandedPane(rankDefectDialog.accordion.getPanes().get(0));
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					rankDefectDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) rankDefectDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return rankDefectDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<RankDefect>();
		this.dialog.setTitle(i18n.getString("RankDefectDialog.title", "Rank defect"));
		this.dialog.setHeaderText(i18n.getString("RankDefectDialog.header", "User defined rank defect properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);

		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, RankDefect>() {
			@Override
			public RankDefect call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					RankDefect rankDefect = new RankDefect();
					if (userDefinedCheckBox.isSelected()) {
						if (translationYCheckBox.isSelected())
							rankDefect.setTranslationYDefectType(DefectType.FREE);

						if (translationXCheckBox.isSelected())
							rankDefect.setTranslationXDefectType(DefectType.FREE);

						if (translationZCheckBox.isSelected())
							rankDefect.setTranslationZDefectType(DefectType.FREE);

						if (rotationYCheckBox.isSelected())
							rankDefect.setRotationYDefectType(DefectType.FREE);

						if (rotationXCheckBox.isSelected())
							rankDefect.setRotationXDefectType(DefectType.FREE);

						if (rotationZCheckBox.isSelected())
							rankDefect.setRotationZDefectType(DefectType.FREE);

						if (shearYCheckBox.isSelected())
							rankDefect.setShearYDefectType(DefectType.FREE);

						if (shearXCheckBox.isSelected())
							rankDefect.setShearXDefectType(DefectType.FREE);

						if (shearZCheckBox.isSelected())
							rankDefect.setShearZDefectType(DefectType.FREE);

						if (scaleYCheckBox.isSelected())
							rankDefect.setScaleYDefectType(DefectType.FREE);

						if (scaleXCheckBox.isSelected())
							rankDefect.setScaleXDefectType(DefectType.FREE);

						if (scaleZCheckBox.isSelected())
							rankDefect.setScaleZDefectType(DefectType.FREE);

						if (scaleXYCheckBox.isSelected())
							rankDefect.setScaleXYDefectType(DefectType.FREE);

						if (scaleXYZCheckBox.isSelected())
							rankDefect.setScaleXYZDefectType(DefectType.FREE);
					}
					save(rankDefect);				
				}
				return null;
			}
		});
		
		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				accordion.setExpandedPane(accordion.getPanes().get(0));
			}
		});
	}

	private Node createPane() {
		VBox box = this.createVbox();

		String labelUserDefined = i18n.getString("RankDefectDialog.userdefined.title", "User defined defect analysis");
		String tooltipUserDefined = i18n.getString("RankDefectDialog.userdefined.tooltip", "If checked, user defined condition equations will be applied (without examination) to free network adjustment");

		this.userDefinedCheckBox = this.createCheckBox(labelUserDefined, tooltipUserDefined);
		this.userDefinedCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.userDefinedCheckBox));

		this.accordion = new Accordion();
		this.accordion.getPanes().addAll(
				this.createTranslationPane(),
				this.createRotationPane(),
				this.createScalePane(),
				this.createShearPane()
				);
		this.accordion.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				userDefinedCheckBox.requestFocus();
			}
		});
		
		box.getChildren().addAll(this.userDefinedCheckBox, this.accordion);
		return box;
	}


	private TitledPane createTranslationPane() {
		String title   = i18n.getString("RankDefectDialog.translation.title", "Translations");
		String tooltip = i18n.getString("RankDefectDialog.translation.tooltip", "Condition equation for network translations");
		
		String labelY   = i18n.getString("RankDefectDialog.translation.y.label", "Translation y");
		String tooltipY = i18n.getString("RankDefectDialog.translation.y.tooltip", "If checked, condition equation for y-translation will be applied to free network adjustment");

		String labelX   = i18n.getString("RankDefectDialog.translation.x.label", "Translation x");
		String tooltipX = i18n.getString("RankDefectDialog.translation.x.tooltip", "If checked, condition equation for x-translation will be applied to free network adjustment");

		String labelZ   = i18n.getString("RankDefectDialog.translation.z.label", "Translation z");
		String tooltipZ = i18n.getString("RankDefectDialog.translation.z.tooltip", "If checked, condition equation for z-translation will be applied to free network adjustment");

		this.translationYCheckBox = this.createCheckBox(labelY, tooltipY);
		this.translationXCheckBox = this.createCheckBox(labelX, tooltipX);
		this.translationZCheckBox = this.createCheckBox(labelZ, tooltipZ);
		
		VBox box = createVbox();
		box.getChildren().addAll(this.translationYCheckBox, this.translationXCheckBox, this.translationZCheckBox);

		return this.createTitledPane(title, tooltip, box);
	}

	private TitledPane createRotationPane() {
		String title   = i18n.getString("RankDefectDialog.rotation.title", "Rotations");
		String tooltip = i18n.getString("RankDefectDialog.rotation.tooltip", "Condition equation for network rotations");
		
		String labelY   = i18n.getString("RankDefectDialog.rotation.y.label", "Rotation y");
		String tooltipY = i18n.getString("RankDefectDialog.rotation.y.tooltip", "If checked, condition equation for y-rotation will be applied to free network adjustment");

		String labelX   = i18n.getString("RankDefectDialog.rotation.x.label", "Rotation x");
		String tooltipX = i18n.getString("RankDefectDialog.rotation.x.tooltip", "If checked, condition equation for x-rotation will be applied to free network adjustment");

		String labelZ   = i18n.getString("RankDefectDialog.rotation.z.label", "Rotation z");
		String tooltipZ = i18n.getString("RankDefectDialog.rotation.z.tooltip", "If checked, condition equation for z-rotation will be applied to free network adjustment");

		this.rotationYCheckBox = this.createCheckBox(labelY, tooltipY);
		this.rotationXCheckBox = this.createCheckBox(labelX, tooltipX);
		this.rotationZCheckBox = this.createCheckBox(labelZ, tooltipZ);

		VBox box = createVbox();
		box.getChildren().addAll(this.rotationYCheckBox, this.rotationXCheckBox, this.rotationZCheckBox);

		return this.createTitledPane(title, tooltip, box);
	}

	private TitledPane createShearPane() {
		String title   = i18n.getString("RankDefectDialog.shear.title", "Shears");
		String tooltip = i18n.getString("RankDefectDialog.shear.tooltip", "Condition equation for network shears (unusual conditions)");
		
		String labelY   = i18n.getString("RankDefectDialog.shear.y.label", "Shear y");
		String tooltipY = i18n.getString("RankDefectDialog.shear.y.tooltip", "If checked, condition equation for y-shear will be applied to free network adjustment");

		String labelX   = i18n.getString("RankDefectDialog.shear.x.label", "Shear x");
		String tooltipX = i18n.getString("RankDefectDialog.shear.x.tooltip", "If checked, condition equation for x-shear will be applied to free network adjustment");

		String labelZ   = i18n.getString("RankDefectDialog.shear.z.label", "Shear z");
		String tooltipZ = i18n.getString("RankDefectDialog.shear.z.tooltip", "If checked, condition equation for z-shear will be applied to free network adjustment");

		this.shearYCheckBox = this.createCheckBox(labelY, tooltipY);
		this.shearXCheckBox = this.createCheckBox(labelX, tooltipX);
		this.shearZCheckBox = this.createCheckBox(labelZ, tooltipZ);

		VBox box = createVbox();
		box.getChildren().addAll(this.shearYCheckBox, this.shearXCheckBox, this.shearZCheckBox);

		return this.createTitledPane(title, tooltip, box);
	}

	private TitledPane createScalePane() {
		String title   = i18n.getString("RankDefectDialog.scale.title", "Scales");
		String tooltip = i18n.getString("RankDefectDialog.scale.tooltip", "Condition equation for network scales");
		
		String labelY   = i18n.getString("RankDefectDialog.scale.y.label", "Scale y");
		String tooltipY = i18n.getString("RankDefectDialog.scale.y.tooltip", "If checked, condition equation for y-scale will be applied to free network adjustment");

		String labelX   = i18n.getString("RankDefectDialog.scale.x.label", "Scale x");
		String tooltipX = i18n.getString("RankDefectDialog.scale.x.tooltip", "If checked, condition equation for x-scale will be applied to free network adjustment");

		String labelZ   = i18n.getString("RankDefectDialog.scale.z.label", "Scale z");
		String tooltipZ = i18n.getString("RankDefectDialog.scale.z.tooltip", "If checked, condition equation for z-scale will be applied to free network adjustment");

		String labelXY   = i18n.getString("RankDefectDialog.scale.xy.label", "Scale y, x");
		String tooltipXY = i18n.getString("RankDefectDialog.scale.xy.tooltip", "If checked, condition equation for horizontal scale will be applied to free network adjustment");

		String labelXYZ   = i18n.getString("RankDefectDialog.scale.xyz.label", "Scale y, x, z");
		String tooltipXYZ = i18n.getString("RankDefectDialog.scale.xyz.tooltip", "If checked, condition equation for spatial scale will be applied to free network adjustment");


		this.scaleYCheckBox = this.createCheckBox(labelY, tooltipY);
		this.scaleYCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.scaleYCheckBox));

		this.scaleXCheckBox = this.createCheckBox(labelX, tooltipX);
		this.scaleXCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.scaleXCheckBox));

		this.scaleZCheckBox = this.createCheckBox(labelZ, tooltipZ);
		this.scaleZCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.scaleZCheckBox));

		this.scaleXYCheckBox = this.createCheckBox(labelXY, tooltipXY);
		this.scaleXYCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.scaleXYCheckBox));

		this.scaleXYZCheckBox = this.createCheckBox(labelXYZ, tooltipXYZ);
		this.scaleXYZCheckBox.selectedProperty().addListener(new ScaleSelectionChangeListener(this.scaleXYZCheckBox));

		VBox leftBox = createVbox();
		leftBox.getChildren().addAll(this.scaleYCheckBox, this.scaleXCheckBox, this.scaleZCheckBox);
		
		VBox rightBox = createVbox();
		rightBox.getChildren().addAll(this.scaleXYCheckBox, this.scaleXYZCheckBox);
		
		HBox hbox = new HBox(0);
		hbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		hbox.setPadding(new Insets(0, 0, 0, 0));
		hbox.getChildren().addAll(leftBox, rightBox);

		return this.createTitledPane(title, tooltip, hbox);
	}

	private TitledPane createTitledPane(String title, String tooltip, Node content) {
		TitledPane titledPane = new TitledPane();
		titledPane.setCollapsible(true);
		titledPane.setAnimated(true);
		titledPane.setContent(content);
		titledPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
		//titledPane.setText(title);
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		titledPane.setGraphic(label);
		return titledPane;
	}
	
	private CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxWidth(Double.MAX_VALUE);
		return checkBox;
	}

	private VBox createVbox() {
		VBox vBox = new VBox();
		vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		vBox.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links
		vBox.setSpacing(10);
		return vBox;
	}

	private void disableCheckBoxes(boolean disable) {
		this.translationYCheckBox.setDisable(disable);
		this.translationXCheckBox.setDisable(disable);
		this.translationZCheckBox.setDisable(disable);

		this.rotationYCheckBox.setDisable(disable);
		this.rotationXCheckBox.setDisable(disable);
		this.rotationZCheckBox.setDisable(disable);

		this.scaleYCheckBox.setDisable(disable);
		this.scaleXCheckBox.setDisable(disable);
		this.scaleZCheckBox.setDisable(disable);

		this.shearYCheckBox.setDisable(disable);
		this.shearXCheckBox.setDisable(disable);
		this.shearZCheckBox.setDisable(disable);

		this.scaleXYCheckBox.setDisable(disable);
		this.scaleXYZCheckBox.setDisable(disable);
	}

	private void save(RankDefect rankDefect) {
		try {
			SQLManager.getInstance().save(this.userDefinedCheckBox.isSelected() || rankDefect.getDefect() == 0, rankDefect);
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("RankDefectDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("RankDefectDialog.message.error.save.exception.header", "Error, could not save user-defined rank defect properties to database."),
							i18n.getString("RankDefectDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}

	private void load() {
		try {
			RankDefect rankDefect = SQLManager.getInstance().getRankDefectDefinition();
			this.userDefinedCheckBox.setSelected(rankDefect.isUserDefinedRankDefect());
			this.disableCheckBoxes(!rankDefect.isUserDefinedRankDefect());
			if (rankDefect.isUserDefinedRankDefect()) {
				this.translationYCheckBox.setSelected(rankDefect.estimateTranslationY());
				this.translationXCheckBox.setSelected(rankDefect.estimateTranslationX());
				this.translationZCheckBox.setSelected(rankDefect.estimateTranslationZ());

				this.rotationYCheckBox.setSelected(rankDefect.estimateRotationY());
				this.rotationXCheckBox.setSelected(rankDefect.estimateRotationX());
				this.rotationZCheckBox.setSelected(rankDefect.estimateRotationZ());

				this.shearYCheckBox.setSelected(rankDefect.estimateShearY());
				this.shearXCheckBox.setSelected(rankDefect.estimateShearX());
				this.shearZCheckBox.setSelected(rankDefect.estimateShearZ());

				this.scaleYCheckBox.setSelected(rankDefect.estimateScaleY());
				this.scaleXCheckBox.setSelected(rankDefect.estimateScaleX());
				this.scaleZCheckBox.setSelected(rankDefect.estimateScaleZ());

				this.scaleXYCheckBox.setSelected(rankDefect.estimateScaleXY());
				this.scaleXYZCheckBox.setSelected(rankDefect.estimateScaleXYZ());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("RankDefectDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("RankDefectDialog.message.error.load.exception.header", "Error, could not load user-defined rank defect properties from database."),
							i18n.getString("RankDefectDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
