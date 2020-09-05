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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class MatrixDialog {
	private class MatrixTypeChangeListener implements ChangeListener<Toggle> {

		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
			if (matrixTypeToggleGroup.getSelectedToggle() != null &&  matrixTypeToggleGroup.getSelectedToggle().getUserData() != null && 
					 matrixTypeToggleGroup.getSelectedToggle().getUserData() instanceof MatrixType) {
				MatrixType matrixType = (MatrixType)matrixTypeToggleGroup.getSelectedToggle().getUserData();
				changeMatrixType(matrixType);
            }
		}
	}
	
	private enum MatrixType {
		 IDENTITY, DIAGONAL, DENSE
	}
	private I18N i18n = I18N.getInstance();
	private static MatrixDialog matrixDialog = new MatrixDialog();
	private Dialog<FeaturePoint> dialog = null;
	private DoubleTextField[][] elements;
	private Window window;
	private FeaturePoint featurePoint;
	private ToggleGroup matrixTypeToggleGroup;
	private MatrixType matrixType = null;
	private MatrixDialog() {}

	public static void setOwner(Window owner) {
		matrixDialog.window = owner;
	}

	public static Optional<FeaturePoint> showAndWait(FeaturePoint featurePoint) {
		matrixDialog.init();
		matrixDialog.setFeaturePoint(featurePoint);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					matrixDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) matrixDialog.dialog.getDialogPane().getScene().getWindow();
//					stage.setMinHeight(400);
//					stage.setMinWidth(800);
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return matrixDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<FeaturePoint>();
		this.dialog.setTitle(i18n.getString("MatrixDialog.title", "Dispersion matrix"));
		this.dialog.setHeaderText(String.format(Locale.ENGLISH, i18n.getString("MatrixDialog.header", "Dispersion of point %s"), ""));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, FeaturePoint>() {
			@Override
			public FeaturePoint call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					setMatrix();
					return featurePoint;
				}
				return null;
			}
		});
	}
	
	private void setFeaturePoint(FeaturePoint featurePoint) {
		this.featurePoint = featurePoint;
		this.dialog.setHeaderText(String.format(Locale.ENGLISH, i18n.getString("MatrixDialog.header", "Dispersion of point %s"), this.featurePoint.getName()));
		
		Matrix matrix = this.featurePoint.getDispersionApriori();
		
		if (matrix instanceof UpperSymmPackMatrix)
			this.matrixType = MatrixType.DENSE;
		else if (matrix instanceof UpperSymmBandMatrix)
			this.matrixType = MatrixType.DIAGONAL;
		else
			this.matrixType = MatrixType.IDENTITY;

		for (Toggle toggle : matrixTypeToggleGroup.getToggles()) {
			if (toggle.getUserData() == this.matrixType) {
				toggle.setSelected(true);
				break;
			}
		}
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (i < matrix.numRows() && j < matrix.numColumns()) {
					this.elements[i][j].setVisible(true);
					this.elements[i][j].setManaged(true);
					
					if (i <= j)
						this.elements[i][j].setValue(matrix.get(i, j));
					
					if (this.matrixType == MatrixType.IDENTITY)
						this.elements[i][j].setDisable(true);
					else if (this.matrixType == MatrixType.DIAGONAL)
						this.elements[i][j].setDisable(i != j);
					else
						this.elements[i][j].setDisable(i > j);
				}
				else {
					this.elements[i][j].setVisible(false);
					this.elements[i][j].setManaged(false);
				}
			}
		}
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		this.matrixTypeToggleGroup = new ToggleGroup();
		RadioButton identityRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.identity.label", "Identiy matrix"), 
				i18n.getString("MatrixDialog.matrix.type.identity.tooltip", "If selected, the matrix type is set to the identity type"),
				this.matrixTypeToggleGroup);
		RadioButton diagonalRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.diagonal.label", "Diagonal matrix"), 
				i18n.getString("MatrixDialog.matrix.type.diagonal.tooltip", "If selected, the matrix type is set to the diagonal type"),
				this.matrixTypeToggleGroup);
		RadioButton denseRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.dense.label", "Dense matrix"), 
				i18n.getString("MatrixDialog.matrix.type.dense.tooltip", "If selected, the matrix type is set to the symmetric dense type"),
				this.matrixTypeToggleGroup);
		identityRadioButton.setUserData(MatrixType.IDENTITY);
		diagonalRadioButton.setUserData(MatrixType.DIAGONAL);
		denseRadioButton.setUserData(MatrixType.DENSE);
		
		Region spacer = new Region();
		VBox buttonBox = new VBox(7);
		VBox.setVgrow(identityRadioButton, Priority.NEVER);
		VBox.setVgrow(diagonalRadioButton, Priority.NEVER);
		VBox.setVgrow(denseRadioButton,    Priority.NEVER);
		VBox.setVgrow(spacer,              Priority.ALWAYS);
		buttonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		buttonBox.getChildren().addAll(identityRadioButton, diagonalRadioButton, denseRadioButton, spacer);
		
		this.matrixTypeToggleGroup.selectedToggleProperty().addListener(new MatrixTypeChangeListener());
		
		Insets insetsField   = new Insets(5, 5, 5, 5);
		this.elements = new DoubleTextField[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.elements[i][j] = DialogUtil.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("MatrixDialog.matrix.element.tooltip", "Element"));
				if (i > j)
					this.elements[i][j].numberProperty().bind(this.elements[j][i].numberProperty());
				
				GridPane.setMargin(this.elements[i][j], insetsField);
				gridPane.add(this.elements[i][j], j, i); // column, row, columnspan, rowspan
			}
		}
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		GridPane.setMargin(buttonBox, new Insets(5, 5, 5, 10));

		gridPane.add(buttonBox, 4, 0, 1, 3);
		return gridPane;
	}
	
	private void changeMatrixType(MatrixType matrixType) {
		int dimension = this.featurePoint.getDimension();
		this.matrixType = matrixType;
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {			

				if (this.matrixType == MatrixType.IDENTITY) {
					this.elements[i][j].setDisable(true);
					if (i == j)
						this.elements[i][j].setValue(1.0);
					else if (i < j)
						this.elements[i][j].setValue(0.0);
				}
				else if (this.matrixType == MatrixType.DIAGONAL) {
					this.elements[i][j].setDisable(i != j);
					if (i < j)
						this.elements[i][j].setValue(0.0);
				}
				else
					this.elements[i][j].setDisable(i > j);
			}
		}
	}
	
	private void setMatrix() {
		int dimension = this.featurePoint.getDimension();
		Matrix matrix = null;
		switch(this.matrixType) {
		case DENSE:
			matrix = new UpperSymmPackMatrix(dimension);
			break;
		case DIAGONAL:
			matrix = new UpperSymmBandMatrix(dimension, 0);
			break;
		case IDENTITY:
			matrix = MathExtension.identity(dimension);
			break;
		}
		
		if (matrix == null)
			return;
		
		// copy elements
		if (this.matrixType != MatrixType.IDENTITY) {
			for (MatrixEntry entry : matrix) {
				double value = this.elements[entry.row()][entry.column()].getNumber().doubleValue();
				entry.set(value);
			}
		}
		
		this.featurePoint.setDispersionApriori(matrix);
	}
}
