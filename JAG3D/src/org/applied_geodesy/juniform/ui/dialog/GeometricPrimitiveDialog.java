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

import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Circle;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Ellipse;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Line;
import org.applied_geodesy.adjustment.geometry.curve.primitive.QuadraticCurve;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cone;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cylinder;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Ellipsoid;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Paraboloid;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class GeometricPrimitiveDialog {
	private static I18N i18N = I18N.getInstance();
	private static GeometricPrimitiveDialog geometricPrimitiveDialog = new GeometricPrimitiveDialog();
	private Dialog<GeometricPrimitive> dialog = null;
	private ComboBox<PrimitiveType> primitiveTypeComboBox;
	private TextField nameTextField;
	private Window window;
	
	private GeometricPrimitiveDialog() {}
	
	public static void setOwner(Window owner) {
		geometricPrimitiveDialog.window = owner;
	}

	public static Optional<GeometricPrimitive> showAndWait(FeatureType featureType) {
		geometricPrimitiveDialog.init();
		geometricPrimitiveDialog.setFeatureType(featureType);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					geometricPrimitiveDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) geometricPrimitiveDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return geometricPrimitiveDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<GeometricPrimitive>();
		this.dialog.setTitle(i18N.getString("GeometricPrimitiveDialog.title", "Geometry"));
		this.dialog.setHeaderText(i18N.getString("GeometricPrimitiveDialog.header", "Geometric primitives"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, GeometricPrimitive>() {
			@Override
			public GeometricPrimitive call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					PrimitiveType primitiveType = primitiveTypeComboBox.getValue();
					if (primitiveType != null) {
						GeometricPrimitive geometricPrimitive = getGeometricPrimitive(primitiveType);
						setDefaultName(geometricPrimitive);
						
						if (nameTextField.getText() != null && !nameTextField.getText().isBlank())
							geometricPrimitive.setName(nameTextField.getText().trim());
						
						return geometricPrimitive;
					}
				}
				return null;
			}
		});
	}
	
	private GeometricPrimitive getGeometricPrimitive(PrimitiveType primitiveType) {
		switch (primitiveType) {
		case LINE:
			return new Line();
		case CIRCLE:
			return new Circle();
		case ELLIPSE:
			return new Ellipse();
		case QUADRATIC_CURVE:
			return new QuadraticCurve();
		case PLANE:
			return new Plane();
		case SPHERE:
			return new Sphere();
		case ELLIPSOID:
			return new Ellipsoid();
		case CYLINDER:
			return new Cylinder();
		case CONE:
			return new Cone();
		case PARABOLOID:
			return new Paraboloid();
		case QUADRATIC_SURFACE:
			return new QuadraticSurface();
		}
		throw new IllegalArgumentException("Error, unknown type of geometric primitive " + primitiveType);
	}
	
	private void setFeatureType(FeatureType featureType) {
		this.primitiveTypeComboBox.setItems(FXCollections.observableArrayList( PrimitiveType.values(featureType) ));
		this.primitiveTypeComboBox.getSelectionModel().clearAndSelect(0);
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		Label nameLabel = new Label(i18N.getString("GeometricPrimitiveDialog.primitive.name.label", "Name:"));
		Label typeLabel = new Label(i18N.getString("GeometricPrimitiveDialog.primitive.type.label", "Type:"));
		
		this.nameTextField = DialogUtil.createTextField(i18N.getString("GeometricPrimitiveDialog.primitive.name.tooltip", "Name of geometric primitive"), i18N.getString("GeometricPrimitiveDialog.primitive.name.prompt", "Name of geometry"));
		this.primitiveTypeComboBox = DialogUtil.createPrimitiveTypeComboBox(createPrimitiveTypeStringConverter(), i18N.getString("GeometricPrimitiveDialog.primitive.type.tooltip", "Select geometric primitive type"));
		
		nameLabel.setLabelFor(this.nameTextField);
		typeLabel.setLabelFor(this.primitiveTypeComboBox);
		
		nameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		typeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		nameLabel.setMaxWidth(Double.MAX_VALUE);
		typeLabel.setMaxWidth(Double.MAX_VALUE);
		
		GridPane.setHgrow(nameLabel, Priority.NEVER);
		GridPane.setHgrow(typeLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.nameTextField,         Priority.ALWAYS);
		GridPane.setHgrow(this.primitiveTypeComboBox, Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(nameLabel, insetsLeft);
		GridPane.setMargin(typeLabel, insetsLeft);

		GridPane.setMargin(this.nameTextField,         insetsRight);
		GridPane.setMargin(this.primitiveTypeComboBox, insetsRight);
		
		gridPane.add(typeLabel,                  0, 0); // column, row, columnspan, rowspan,
		gridPane.add(this.primitiveTypeComboBox, 1, 0);
		
		gridPane.add(nameLabel,          0, 1);
		gridPane.add(this.nameTextField, 1, 1);
		
		return gridPane;
	}
	
	static StringConverter<PrimitiveType> createPrimitiveTypeStringConverter() {
		return new StringConverter<PrimitiveType>() {
			@Override
			public String toString(PrimitiveType primitiveType) {
				return getPrimitiveTypeLabel(primitiveType);
			}

			@Override
			public PrimitiveType fromString(String string) {
				return PrimitiveType.valueOf(string);
			}
		};
	}
	
	public static String getPrimitiveTypeLabel(PrimitiveType primitiveType) throws IllegalArgumentException {
		if (primitiveType == null)
			return null;
		
		switch (primitiveType) {
		/** Curves */
		case LINE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.curve.line.label", "Line");
			
		case CIRCLE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.curve.circle.label", "Circle");
			
		case ELLIPSE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.curve.ellipse.label", "Ellipse");
			
		case QUADRATIC_CURVE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.curve.quadratic.label", "Quadratic curve");
			
		/** Surfaces */	
		case PLANE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.plane.label", "Plane");
			
		case SPHERE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.sphere.label", "Sphere");
			
		case ELLIPSOID:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.ellipsoid.label", "Ellipsoid");
			
		case CYLINDER:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.cylinder.label", "Cylinder");
			
		case CONE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.cone.label", "Cone");
			
		case PARABOLOID:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.paraboloid.label", "Paraboloid");
			
		case QUADRATIC_SURFACE:
			return i18N.getString("GeometricPrimitiveDialog.primitive.type.surface.quadratic.label", "Quadratic surface");
		}
		
		throw new IllegalArgumentException("Error, unknown type of geometric primitive " + primitiveType);
	}
	
	public static void setDefaultName(GeometricPrimitive geometricPrimitive) throws IllegalArgumentException {
		String name = getPrimitiveTypeLabel(geometricPrimitive.getPrimitiveType());
		if (name == null)
			throw new NullPointerException("Error, name of geometric primitive cannot be null!");
		
		name = String.format(Locale.ENGLISH, name + " (id: %d)", geometricPrimitive.getId());	
		geometricPrimitive.setName(name);
	}
}
