package org.applied_geodesy.adjustment.transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.transformation.equation.TransformationEquations;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.PositionPair;
import org.applied_geodesy.adjustment.transformation.point.Positionable;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public abstract class Transformation {
	private Map<ParameterRestrictionType, Restriction> supportedParameterRestrictions = new HashMap<ParameterRestrictionType, Restriction>();
	private TransformationEquations transformationEquations;
	private ObservableUniqueList<UnknownParameter> unknownParameters     = null;
	private ObservableUniqueList<Restriction> restrictions               = null;
	private ObservableUniqueList<Restriction> postprocessingCalculations = new ObservableUniqueList<Restriction>();
	private ObjectProperty<Boolean> estimateInitialGuess                 = new SimpleObjectProperty<Boolean>(this, "estimateInitialGuess", Boolean.TRUE);
	private ObjectProperty<Boolean> estimateCenterOfMasses               = new SimpleObjectProperty<Boolean>(this, "estimateCenterOfMasses", Boolean.TRUE);
	
	Transformation() {}
	
//	public abstract TransformationType getTransformationType();
	
	public void prepareIteration() {}
	
	public final Map<ParameterRestrictionType, Restriction> getSupportedParameterRestrictions() {
		return this.supportedParameterRestrictions;
	}
	
	public final ObservableUniqueList<Restriction> getPostProcessingCalculations() {
		return this.postprocessingCalculations;
	}
	
	public ObjectProperty<Boolean> estimateCenterOfMassesProperty() {
		return this.estimateCenterOfMasses;
	}
	
	public void setEstimateCenterOfMasses(boolean estimateCenterOfMasses) {
		this.estimateCenterOfMasses.set(estimateCenterOfMasses);
	}
	
	public boolean isEstimateCenterOfMasses() {
		return this.estimateCenterOfMasses.get();
	}
	
	public ObjectProperty<Boolean> estimateInitialGuessProperty() {
		return this.estimateInitialGuess;
	}
	
	public void setEstimateInitialGuess(boolean estimateInitialGuess) {
		this.estimateInitialGuess.set(estimateInitialGuess);
	}
	
	public boolean isEstimateInitialGuess() {
		return this.estimateInitialGuess.get();
	}

	public final ObservableUniqueList<UnknownParameter> getUnknownParameters() {
		if (this.unknownParameters == null) {
			this.unknownParameters = new ObservableUniqueList<UnknownParameter>();
			this.unknownParameters.addAll(this.transformationEquations.getUnknownParameters());
		}
		return this.unknownParameters;
	}
	
	public final ObservableUniqueList<Restriction> getRestrictions() {
		if (this.restrictions == null) {
			this.restrictions = new ObservableUniqueList<Restriction>();
			this.restrictions.addAll(this.transformationEquations.getRestrictions());
		}
		return this.restrictions;
	}
	
	void set(TransformationEquations equationSet) throws IllegalArgumentException {
		this.transformationEquations = equationSet;
	}
	
	public TransformationEquations getTransformationEquations() {
		return this.transformationEquations;
	}
	
	public List<HomologousFramePositionPair> getHomologousFramePositionPairs() {
		ObservableList<HomologousFramePositionPair> nonUniquePoints = FXCollections.<HomologousFramePositionPair>observableArrayList();
		nonUniquePoints.addAll(this.transformationEquations.getHomologousFramePositionPairs());

		FilteredList<HomologousFramePositionPair> enabledNonUniquePoints = new FilteredList<HomologousFramePositionPair>(nonUniquePoints);
		enabledNonUniquePoints.setPredicate(
				new Predicate<HomologousFramePositionPair>(){
					public boolean test(HomologousFramePositionPair HomologousFramePositionPair){
						return HomologousFramePositionPair.isEnable();
					}
				}
		);
					
		// Unique point list
		return new ArrayList<HomologousFramePositionPair>(new LinkedHashSet<HomologousFramePositionPair>(enabledNonUniquePoints));
	}
	
	public abstract void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException;
	
	public void applyInitialGuess() {
		for (UnknownParameter unknownParameter : this.unknownParameters) {
			// set initial guess x <-- x0 because adjustment process works with x (not with x0)
			unknownParameter.setValue(unknownParameter.getValue0());
		}
	}
	
	public static SimplePositionPair deriveCenterOfMasses(Collection<? extends PositionPair<?,?>> positionPairs) {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		double X0 = 0, Y0 = 0, Z0 = 0;
		for (PositionPair<?,?> positionPair : positionPairs) {
			if (!positionPair.isEnable())
				continue;
			
			Positionable source = positionPair.getSourceSystemPosition();
			Positionable target = positionPair.getSourceSystemPosition();
			
			nop++;
			x0 += source.getX();
			y0 += source.getY();
			z0 += source.getZ();
			
			X0 += target.getX();
			Y0 += target.getY();
			Z0 += target.getZ();
		}
		
		if (nop == 0)
			throw new IllegalArgumentException("Error, could not estimate center of mass because of an empty point list!");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		X0 /= nop;
		Y0 /= nop;
		Z0 /= nop;
				
		return new SimplePositionPair(
				"CENTER_OF_MASSES",
				x0, y0, z0, 
				X0, Y0, Z0
		);
	}
}