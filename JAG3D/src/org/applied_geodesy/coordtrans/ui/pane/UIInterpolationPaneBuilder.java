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

package org.applied_geodesy.coordtrans.ui.pane;

import java.util.Map;

import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.interpolation.Interpolation;
import org.applied_geodesy.adjustment.transformation.interpolation.InterpolationType;
import org.applied_geodesy.adjustment.transformation.interpolation.InverseDistanceWeighting;
import org.applied_geodesy.adjustment.transformation.interpolation.MultiQuadraticInterpolation;
import org.applied_geodesy.adjustment.transformation.interpolation.SectorInterpolation;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.utils.UiUtil;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.util.CellValueType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UIInterpolationPaneBuilder implements TransformationChangeListener {
	
	private class InterpolationTypeChangeListener implements ChangeListener<Toggle> {
		@Override
		public void changed(ObservableValue<? extends Toggle> arg0, Toggle oldValue, Toggle newValue) {
			if (newValue.getUserData() instanceof InterpolationType) {
				InterpolationType interpolationType = (InterpolationType)newValue.getUserData();
				setInterpolation(interpolationType);
			}		
		}
	}

	private static UIInterpolationPaneBuilder interpolationPaneBuilder = new UIInterpolationPaneBuilder();
	private Transformation transformation = null;
	private InterpolationType lastSelectedInterpolationType = InterpolationType.NONE;
	
	private I18N i18n = I18N.getInstance();
	
	private ScrollPane restrictionNode = null;
	private Map<InterpolationType, Interpolation> interpolationApproaches = Map.of(
			InterpolationType.INVERSE_DISTANCE_WEIGHTING, new InverseDistanceWeighting(),
			InterpolationType.MULTI_QUADRATIC, new MultiQuadraticInterpolation(),
			InterpolationType.SECTOR, new SectorInterpolation());
		
	private UIInterpolationPaneBuilder() {
		this.init();
	}

	public static UIInterpolationPaneBuilder getInstance() {
		return interpolationPaneBuilder;
	}
	
	private void init() {
		if (this.restrictionNode != null)
			return;
		
		this.restrictionNode = this.createPane();
		this.restrictionNode.getContent().setDisable(true);
	}
	
	public Node getNode() {
		return this.restrictionNode;
	}
		
	private ScrollPane createPane() {
		Node interPolationPane = this.createInterpolationPane();
		
		GridPane gridPane = UiUtil.createGridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(20);
		gridPane.setPadding(new Insets(20,15,20,15)); // oben, recht, unten, links
		gridPane.setAlignment(Pos.BASELINE_CENTER);

		Region leftSpacer = new Region();
		leftSpacer.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		leftSpacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		Region rightSpacer = new Region();
		rightSpacer.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		rightSpacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		GridPane.setHgrow(leftSpacer,          Priority.ALWAYS);
		GridPane.setHgrow(rightSpacer,         Priority.ALWAYS);
		GridPane.setHgrow(interPolationPane,   Priority.SOMETIMES);
		
		int row = 0;		
		int col = 0;
		gridPane.add(leftSpacer,          col++, row,   1, 1);
		gridPane.add(interPolationPane, col++, row,   1, 1);
		gridPane.add(rightSpacer,         col++, row++, 1, 1);
		
		ScrollPane scrollPane = new ScrollPane(gridPane);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		return scrollPane;
	}
	

	private Node createInterpolationPane() {
		ToggleGroup interpolationGroup  = new ToggleGroup();
		
		Insets radioInsets = new Insets(10,0,0,0);
		Insets labelInsets = new Insets(0,0,0,40);
		
		RadioButton nonRadioButton = this.createInterpolationRadioButton(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.none.label", "No interpolation of residual gaps"), i18n.getString("UIInterpolationPaneBuilder.interpolation.type.none.tooltip", "Selected, if no interpolation approach is prescribed."), InterpolationType.NONE, interpolationGroup);
		RadioButton idwRadioButton = this.createInterpolationRadioButton(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.label", "Inverse distance weighting"), i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.tooltip", "Selected, if inverse distance weighting is to be applied."), InterpolationType.INVERSE_DISTANCE_WEIGHTING, interpolationGroup);
		RadioButton msiRadioButton = this.createInterpolationRadioButton(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.label", "Multiquadratic interpolation"), i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.tooltip", "Selected, if multiquadratic interpolation is to be applied."), InterpolationType.MULTI_QUADRATIC, interpolationGroup);
		RadioButton secRadioButton = this.createInterpolationRadioButton(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.label", "Sector interpolation"), i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.tooltip", "Selected, if sector interpolation is to be applied."), InterpolationType.SECTOR, interpolationGroup);
		
		nonRadioButton.setPadding(radioInsets);
		idwRadioButton.setPadding(radioInsets);
		msiRadioButton.setPadding(radioInsets);
		secRadioButton.setPadding(radioInsets);

		Label idxExponentLabel  = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.parameter.exponent.label", "Exponent: "));
		Label idxSmoothingLabel = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.parameter.smoothing.label", "Smoothing value: "));
		
		Label msiExponentLabel  = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.parameter.exponent.label", "Exponent: "));
		Label msiSmoothingLabel = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.parameter.smoothing.label", "Smoothing value: "));
		
		Label secNumeratorExponentLabel   = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.parameter.exponent.numerator.label", "Exponent of numerator: "));
		Label secDenominatorExponentLabel = new Label(i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.parameter.exponent.denominator.label", "Exponent of denominator: "));
		
		idxExponentLabel.setPadding(labelInsets);
		idxSmoothingLabel.setPadding(labelInsets);
		
		msiExponentLabel.setPadding(labelInsets);
		msiSmoothingLabel.setPadding(labelInsets);
		
		secNumeratorExponentLabel.setPadding(labelInsets);
		secDenominatorExponentLabel.setPadding(labelInsets);
		
		DoubleTextField idxExponentField  = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.parameter.exponent.tooltip", "Exponent of inverse distance weighting"), InterpolationType.INVERSE_DISTANCE_WEIGHTING);
		DoubleTextField idxSmoothingField = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.inverse_distance_weighting.parameter.smoothing.tooltip", "Smoothing value in inverse distance weighting"), InterpolationType.INVERSE_DISTANCE_WEIGHTING);
		
		DoubleTextField msiExponentField  = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.parameter.exponent.tooltip", "Exponent of multiquadratic interpolation"), InterpolationType.MULTI_QUADRATIC);
		DoubleTextField msiSmoothingField = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.multiquadratic.parameter.smoothing.tooltip", "Smoothing value of multiquadratic interpolation"), InterpolationType.MULTI_QUADRATIC);
		
		DoubleTextField secNumeratorExponentField   = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.parameter.exponent.numerator.tooltip", "Numerator exponent of sector interpolation"), InterpolationType.SECTOR);
		DoubleTextField secDenominatorExponentField = this.createDoubleTextField(CellValueType.DOUBLE, 0, i18n.getString("UIInterpolationPaneBuilder.interpolation.type.sector.parameter.exponent.denominator.tooltip", "Denominator exponent of sector interpolation"), InterpolationType.SECTOR);
				
		idxExponentField.numberProperty().bindBidirectional(((InverseDistanceWeighting)this.interpolationApproaches.get(InterpolationType.INVERSE_DISTANCE_WEIGHTING)).exponentProperty());
		idxSmoothingField.numberProperty().bindBidirectional(((InverseDistanceWeighting)this.interpolationApproaches.get(InterpolationType.INVERSE_DISTANCE_WEIGHTING)).smoothingProperty());
		
		msiExponentField.numberProperty().bindBidirectional(((MultiQuadraticInterpolation)this.interpolationApproaches.get(InterpolationType.MULTI_QUADRATIC)).exponentProperty());
		msiSmoothingField.numberProperty().bindBidirectional(((MultiQuadraticInterpolation)this.interpolationApproaches.get(InterpolationType.MULTI_QUADRATIC)).smoothingProperty());
		
		secNumeratorExponentField.numberProperty().bindBidirectional(((SectorInterpolation)this.interpolationApproaches.get(InterpolationType.SECTOR)).numeratorExponentProperty());
		secDenominatorExponentField.numberProperty().bindBidirectional(((SectorInterpolation)this.interpolationApproaches.get(InterpolationType.SECTOR)).denominatorExponentProperty());
		
		idxExponentLabel.setLabelFor(idxExponentField);
		idxSmoothingLabel.setLabelFor(idxSmoothingField);
		
		msiExponentLabel.setLabelFor(msiExponentField);
		msiSmoothingLabel.setLabelFor(msiSmoothingField);
		
		secNumeratorExponentLabel.setLabelFor(secNumeratorExponentField);
		secDenominatorExponentLabel.setLabelFor(secDenominatorExponentField);
		
		nonRadioButton.setSelected(true);
		interpolationGroup.selectedToggleProperty().addListener(new InterpolationTypeChangeListener());
		
		GridPane.setHgrow(idxExponentField,  Priority.ALWAYS);
		GridPane.setHgrow(idxSmoothingField, Priority.ALWAYS);
		
		GridPane.setHgrow(msiExponentField,  Priority.ALWAYS);
		GridPane.setHgrow(msiSmoothingField, Priority.ALWAYS);
		
		GridPane.setHgrow(secNumeratorExponentField,   Priority.ALWAYS);
		GridPane.setHgrow(secDenominatorExponentField, Priority.ALWAYS);
		
		GridPane interpolationApproachGridPane = UiUtil.createGridPane();
		interpolationApproachGridPane.setHgap(50);
		interpolationApproachGridPane.setVgap(10);
		interpolationApproachGridPane.setAlignment(Pos.BASELINE_LEFT);
		
		int row = 0;

		interpolationApproachGridPane.add(nonRadioButton,    0, row++,   2, 1);

		interpolationApproachGridPane.add(idwRadioButton,    0, row++,   2, 1);
		interpolationApproachGridPane.add(idxExponentLabel,  0, row,     1, 1);
		interpolationApproachGridPane.add(idxExponentField,  1, row++,   1, 1);
		interpolationApproachGridPane.add(idxSmoothingLabel, 0, row,     1, 1);
		interpolationApproachGridPane.add(idxSmoothingField, 1, row++,   1, 1);

		interpolationApproachGridPane.add(msiRadioButton,    0, row++,   2, 1);
		interpolationApproachGridPane.add(msiExponentLabel,  0, row,     1, 1);
		interpolationApproachGridPane.add(msiExponentField,  1, row++,   1, 1);
		interpolationApproachGridPane.add(msiSmoothingLabel, 0, row,     1, 1);
		interpolationApproachGridPane.add(msiSmoothingField, 1, row++,   1, 1);

		interpolationApproachGridPane.add(secRadioButton,              0, row++,   2, 1);
		interpolationApproachGridPane.add(secNumeratorExponentLabel,   0, row,     1, 1);
		interpolationApproachGridPane.add(secNumeratorExponentField,   1, row++,   1, 1);
		interpolationApproachGridPane.add(secDenominatorExponentLabel, 0, row,     1, 1);
		interpolationApproachGridPane.add(secDenominatorExponentField, 1, row++,   1, 1);
		
		TitledPane titledPane = UiUtil.createTitledPane(
				i18n.getString("UIInterpolationPaneBuilder.interpolation.title", "Interpolation of residual gaps"), 
				i18n.getString("UIInterpolationPaneBuilder.interpolation.tooltip", "Select interpolation approach"), 
				interpolationApproachGridPane
		);
		titledPane.setCollapsible(false);
		
		return titledPane;
	}
	
	private RadioButton createInterpolationRadioButton(String label, String tooltip, InterpolationType interpolationType, ToggleGroup group) {
		RadioButton radioButton = UiUtil.createRadioButton(label, tooltip, group);
		radioButton.setUserData(interpolationType);
		radioButton.setSelected(false);
		
		return radioButton;
	}
	
	private DoubleTextField createDoubleTextField(CellValueType type, double value, String tooltipText, InterpolationType interpolationType) {
		DoubleTextField field = UiUtil.createDoubleTextField(type, value, tooltipText);
		field.setUserData(interpolationType);
		return field;
	}
	
	private void setInterpolation(InterpolationType interpolationType) {
		if (interpolationType != null)
			this.lastSelectedInterpolationType = interpolationType;
		
		if (this.transformation == null)
			return;
		
		if (interpolationType == null)
			this.transformation.setInterpolation(null);
		else {
			Interpolation interpolation = this.interpolationApproaches.get(interpolationType);
			this.transformation.setInterpolation(interpolation);
		}
	}

	@Override
	public void transformationChanged(TransformationEvent evt) {
		this.restrictionNode.getContent().setDisable(evt.getEventType() != TransformationEventType.TRANSFORMATION_MODEL_ADDED);
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED) {
			this.transformation = evt.getSource();
			this.setInterpolation(this.lastSelectedInterpolationType);
		}
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED) {
			if (this.transformation != null)
				this.setInterpolation(null);
			this.transformation = null;
		}
	}
}
