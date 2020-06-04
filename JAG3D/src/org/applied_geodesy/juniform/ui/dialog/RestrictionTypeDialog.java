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

package org.applied_geodesy.juniform.ui.dialog;

import java.util.Optional;

import org.applied_geodesy.adjustment.geometry.restriction.RestrictionType;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class RestrictionTypeDialog {
	private static I18N i18N = I18N.getInstance();
	private static RestrictionTypeDialog restrictionTypeDialog = new RestrictionTypeDialog();
	private Dialog<RestrictionType> dialog = null;
	private ComboBox<RestrictionType> restrictionTypeComboBox;
	private Window window;
	
	private RestrictionTypeDialog() {}
	
	public static void setOwner(Window owner) {
		restrictionTypeDialog.window = owner;
	}

	public static Optional<RestrictionType> showAndWait() {
		restrictionTypeDialog.init();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					restrictionTypeDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) restrictionTypeDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return restrictionTypeDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<RestrictionType>();
		this.dialog.setTitle(i18N.getString("RestrictionTypeDialog.title", "Restrictions"));
		this.dialog.setHeaderText(i18N.getString("RestrictionTypeDialog.header", "Parameter restrictions"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, RestrictionType>() {
			@Override
			public RestrictionType call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					return restrictionTypeComboBox.getValue();
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label restrictionTypeLabel = new Label(i18N.getString("RestrictionTypeDialog.restriction.type.label",  "Restriction type:"));
		this.restrictionTypeComboBox = DialogUtil.createRestrictionTypeComboBox(createRestrictionTypeStringConverter(), i18N.getString("RestrictionTypeDialog.restriction.type.tooltip", "Select restriction type"));

		restrictionTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		restrictionTypeLabel.setMaxWidth(Double.MAX_VALUE);
		
		restrictionTypeLabel.setLabelFor(this.restrictionTypeComboBox);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(restrictionTypeLabel, insetsLeft);

		GridPane.setMargin(this.restrictionTypeComboBox, insetsRight);
		
		GridPane.setHgrow(restrictionTypeLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.restrictionTypeComboBox, Priority.ALWAYS);
		
		gridPane.add(restrictionTypeLabel,  0, 0); // column, row, columnspan, rowspan,
		gridPane.add(this.restrictionTypeComboBox,  1, 0);
		
		return gridPane;
	}

	static StringConverter<RestrictionType> createRestrictionTypeStringConverter() {
		return new StringConverter<RestrictionType>() {

			@Override
			public String toString(RestrictionType restrictionType) {
				return getRestrictionTypeLabel(restrictionType);
			}

			@Override
			public RestrictionType fromString(String string) {
				return RestrictionType.valueOf(string);
			}
		};
	}
	
	static String getRestrictionTypeLabel(RestrictionType restrictionType) {
		if (restrictionType == null)
			return null;
		
		switch (restrictionType) {
		case AVERAGE:
			return i18N.getString("RestrictionTypeDialog.restriction.type.average", "Average value");

		case PRODUCT_SUM:
			return i18N.getString("RestrictionTypeDialog.restriction.type.productsum", "k-th Power of product sum");
			
		case FEATURE_POINT:
			return i18N.getString("RestrictionTypeDialog.restriction.type.featurepoint", "Feature point");
			
		case VECTOR_ANGLE:
			return i18N.getString("RestrictionTypeDialog.restriction.type.vectorangle", "Vector angle");
		}
		throw new IllegalArgumentException("Error, unknown restriction type " + restrictionType + "!");
	}
}
