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

import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.util.CellValueType;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
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
		private final FrameType frameType;
		MatrixTypeChangeListener(FrameType frameType) {
			this.frameType = frameType;
		}
		
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
			ToggleGroup matrixTypeToggleGroup = this.frameType == FrameType.SOURCE ? matrixTypeSourceSystemToggleGroup : matrixTypeTargetSystemToggleGroup;

			if (matrixTypeToggleGroup.getSelectedToggle() != null &&  matrixTypeToggleGroup.getSelectedToggle().getUserData() != null && 
					 matrixTypeToggleGroup.getSelectedToggle().getUserData() instanceof MatrixType) {
				MatrixType matrixType = (MatrixType)matrixTypeToggleGroup.getSelectedToggle().getUserData();
				changeMatrixType(matrixType, this.frameType);
            }
		}
	}
	
	private enum MatrixType {
		 IDENTITY, DIAGONAL, DENSE
	}
	
	private I18N i18n = I18N.getInstance();
	private static MatrixDialog matrixDialog = new MatrixDialog();
	private Dialog<HomologousFramePositionPair> dialog = null;
	private Window window;
	private DoubleTextField[][] matrixElementsSourceSystem, matrixElementsTargetSystem;
	private Accordion accordion = null;
	private HomologousFramePositionPair homologousFramePositionPair;
	private ToggleGroup matrixTypeSourceSystemToggleGroup, matrixTypeTargetSystemToggleGroup;
	private MatrixType matrixTypeSourceSystem = null, matrixTypeTargetSystem = null;
	private MatrixDialog() {}

	public static void setOwner(Window owner) {
		matrixDialog.window = owner;
	}

	public static Optional<HomologousFramePositionPair> showAndWait(HomologousFramePositionPair homologousFramePositionPair) {
		matrixDialog.init();
		matrixDialog.setHomologousFramePositionPair(homologousFramePositionPair);
		if (matrixDialog.accordion.getExpandedPane() == null)
			matrixDialog.accordion.setExpandedPane(matrixDialog.accordion.getPanes().get(0));
		
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
		
		this.dialog = new Dialog<HomologousFramePositionPair>();
		this.dialog.setTitle(i18n.getString("MatrixDialog.title", "Dispersion matrix"));
		this.dialog.setHeaderText(String.format(Locale.ENGLISH, i18n.getString("MatrixDialog.header", "Dispersion of point %s"), ""));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, HomologousFramePositionPair>() {
			@Override
			public HomologousFramePositionPair call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					setMatrix(homologousFramePositionPair.getSourceSystemPosition(), FrameType.SOURCE);
					setMatrix(homologousFramePositionPair.getTargetSystemPosition(), FrameType.TARGET);
					
					if (accordion.getExpandedPane() == null)
						accordion.setExpandedPane(accordion.getPanes().get(0));
					
					return homologousFramePositionPair;
				}
				return null;
			}
		});
	}
	
	private void setHomologousFramePositionPair(HomologousFramePositionPair homologousFramePositionPair) {
		this.homologousFramePositionPair = homologousFramePositionPair;
		this.setHomologousFramePosition(this.homologousFramePositionPair.getSourceSystemPosition(), FrameType.SOURCE);
		this.setHomologousFramePosition(this.homologousFramePositionPair.getTargetSystemPosition(), FrameType.TARGET);
	}
	
	private void setHomologousFramePosition(HomologousFramePosition position, FrameType frameType) {
		this.dialog.setHeaderText(String.format(Locale.ENGLISH, i18n.getString("MatrixDialog.header", "Dispersion of point %s"), this.homologousFramePositionPair.getName()));
		
		Matrix matrix = position.getDispersionApriori();
		MatrixType matrixType = null;
		DoubleTextField[][] matrixElements = null;
		ToggleGroup matrixTypeToggleGroup = null;
		
		if (matrix instanceof UpperSymmPackMatrix)
			matrixType = MatrixType.DENSE;
		else if (matrix instanceof UpperSymmBandMatrix)
			matrixType = MatrixType.DIAGONAL;
		else
			matrixType = MatrixType.IDENTITY;
		
		if (frameType == FrameType.SOURCE) {
			this.matrixTypeSourceSystem = matrixType;
			matrixElements        = this.matrixElementsSourceSystem;
			matrixTypeToggleGroup = this.matrixTypeSourceSystemToggleGroup;
		}
		else {
			this.matrixTypeTargetSystem = matrixType;
			matrixElements        = this.matrixElementsTargetSystem;
			matrixTypeToggleGroup = this.matrixTypeTargetSystemToggleGroup;
		}

		for (Toggle toggle : matrixTypeToggleGroup.getToggles()) {
			if (toggle.getUserData() == matrixType) {
				toggle.setSelected(true);
				break;
			}
		}
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (i < matrix.numRows() && j < matrix.numColumns()) {
					matrixElements[i][j].setVisible(true);
					matrixElements[i][j].setManaged(true);
					
					if (i <= j)
						matrixElements[i][j].setValue(matrix.get(i, j));
					
					if (matrixType == MatrixType.IDENTITY)
						matrixElements[i][j].setDisable(true);
					else if (matrixType == MatrixType.DIAGONAL)
						matrixElements[i][j].setDisable(i != j);
					else
						matrixElements[i][j].setDisable(i > j);
				}
				else {
					matrixElements[i][j].setVisible(false);
					matrixElements[i][j].setManaged(false);
				}
			}
		}
	}
	
	private Node createPane() {
		this.accordion = new Accordion();
		this.accordion.getPanes().addAll(
				DialogUtil.createTitledPane(
						i18n.getString("MatrixDialog.matrix.frame.source.title", "Source System"), 
						i18n.getString("MatrixDialog.matrix.frame.source.tooltip", "Define source system dispersion matrix of point"),
						this.createMatrixPane(FrameType.SOURCE)),
				DialogUtil.createTitledPane(
						i18n.getString("MatrixDialog.matrix.frame.target.title", "Target System"), 
						i18n.getString("MatrixDialog.matrix.frame.target.tooltip", "Define target system dispersion matrix of point"),
						this.createMatrixPane(FrameType.TARGET))
		);
	
		return this.accordion;
	}
	
	private Node createMatrixPane(FrameType frameType) {
		GridPane gridPane = DialogUtil.createGridPane();
		
		ToggleGroup matrixTypeToggleGroup = new ToggleGroup();
		RadioButton identityRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.identity.label", "Identiy matrix"), 
				i18n.getString("MatrixDialog.matrix.type.identity.tooltip", "If selected, the matrix type is set to the identity type"),
				matrixTypeToggleGroup);
		RadioButton diagonalRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.diagonal.label", "Diagonal matrix"), 
				i18n.getString("MatrixDialog.matrix.type.diagonal.tooltip", "If selected, the matrix type is set to the diagonal type"),
				matrixTypeToggleGroup);
		RadioButton denseRadioButton = DialogUtil.createRadioButton(i18n.getString("MatrixDialog.matrix.type.dense.label", "Dense matrix"), 
				i18n.getString("MatrixDialog.matrix.type.dense.tooltip", "If selected, the matrix type is set to the symmetric dense type"),
				matrixTypeToggleGroup);
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
		
		Insets insetsField   = new Insets(5, 5, 5, 5);
		DoubleTextField[][] matrixElements = new DoubleTextField[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				matrixElements[i][j] = DialogUtil.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("MatrixDialog.matrix.element.tooltip", "Element"));
				if (i > j)
					matrixElements[i][j].numberProperty().bind(matrixElements[j][i].numberProperty());
				
				GridPane.setMargin(matrixElements[i][j], insetsField);
				gridPane.add(matrixElements[i][j], j, i); // column, row, columnspan, rowspan
			}
		}

		matrixTypeToggleGroup.selectedToggleProperty().addListener(new MatrixTypeChangeListener(frameType));
		
		if (frameType == FrameType.SOURCE) {
			this.matrixElementsSourceSystem        = matrixElements;
			this.matrixTypeSourceSystemToggleGroup = matrixTypeToggleGroup;
		}
		else {
			this.matrixElementsTargetSystem        = matrixElements;
			this.matrixTypeTargetSystemToggleGroup = matrixTypeToggleGroup;
		}
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		GridPane.setMargin(buttonBox, new Insets(5, 5, 5, 10));

		gridPane.add(buttonBox, 4, 0, 1, 3);
		return gridPane;
	}
	
	private void changeMatrixType(MatrixType matrixType, FrameType frameType) {
		
		DoubleTextField[][] matrixElements = null;
		if (frameType == FrameType.SOURCE) {
			this.matrixTypeSourceSystem = matrixType;
			matrixElements = this.matrixElementsSourceSystem;
		}
		else {
			this.matrixTypeTargetSystem = matrixType;
			matrixElements = this.matrixElementsTargetSystem;
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {			

				if (matrixType == MatrixType.IDENTITY) {
					matrixElements[i][j].setDisable(true);
					if (i == j)
						matrixElements[i][j].setValue(1.0);
					else if (i < j)
						matrixElements[i][j].setValue(0.0);
				}
				else if (matrixType == MatrixType.DIAGONAL) {
					matrixElements[i][j].setDisable(i != j);
					if (i < j)
						matrixElements[i][j].setValue(0.0);
				}
				else
					matrixElements[i][j].setDisable(i > j);
			}
		}
	}
	
	private void setMatrix(HomologousFramePosition position, FrameType frameType) {
		int dimension = position.getDimension();
		MatrixType matrixType = null;
		Matrix matrix = null;
		DoubleTextField[][] matrixElements = null;
		
		if (frameType == FrameType.SOURCE) {
			matrixType     = this.matrixTypeSourceSystem;
			matrixElements = this.matrixElementsSourceSystem;
		}
		else {
			matrixType     = this.matrixTypeTargetSystem;
			matrixElements = this.matrixElementsTargetSystem;
		}
		
		switch(matrixType) {
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
		if (matrixType != MatrixType.IDENTITY) {
			for (MatrixEntry entry : matrix) {
				double value = matrixElements[entry.row()][entry.column()].getNumber().doubleValue();
				entry.set(value);
			}
		}
		
		position.setDispersionApriori(matrix);
	}
}
