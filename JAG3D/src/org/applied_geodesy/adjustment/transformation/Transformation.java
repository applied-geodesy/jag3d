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

package org.applied_geodesy.adjustment.transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.transformation.equation.TransformationEquations;
import org.applied_geodesy.adjustment.transformation.interpolation.Interpolation;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
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
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public abstract class Transformation {
	private Map<ParameterRestrictionType, Restriction> supportedParameterRestrictions = new HashMap<ParameterRestrictionType, Restriction>();
	private TransformationEquations transformationEquations;
	private ObservableUniqueList<UnknownParameter> unknownParameters     = null;
	private ObservableUniqueList<Restriction> restrictions               = null;
	private ObservableUniqueList<Restriction> postprocessingCalculations = new ObservableUniqueList<Restriction>();
	private ObjectProperty<Boolean> estimateInitialGuess                 = new SimpleObjectProperty<Boolean>(this, "estimateInitialGuess", Boolean.TRUE);
	private ObjectProperty<Boolean> estimateCenterOfMasses               = new SimpleObjectProperty<Boolean>(this, "estimateCenterOfMasses", Boolean.TRUE);
	private ObjectProperty<Interpolation> interpolation                  = new SimpleObjectProperty<Interpolation>(this, "interpolation");
	private ObservableUniqueList<FramePositionPair> framePositionPairs   = new ObservableUniqueList<FramePositionPair>();
	private Set<ParameterType> fixedUnknownParameterTypes                = new HashSet<ParameterType>();	
	Transformation() {}

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
	
	public Interpolation getInterpolation() {
		return this.interpolation.get();
	}
	
	public ObjectProperty<Interpolation> interpolationProperty() {
		return this.interpolation;
	}
	
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation.set(interpolation);
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
	
	public ObservableUniqueList<FramePositionPair> getFramePositionPairs() {
		return this.framePositionPairs;
	}
	
	public void transformFramePositionPairs(UpperSymmPackMatrix Dp) {
		TransformationEquations transformationEquations = this.getTransformationEquations();
		if (transformationEquations != null) {
			int dim = transformationEquations.getTransformationType().getDimension();
			Map<String, EstimatedFramePosition> estimatedTargetPositions = new HashMap<String,EstimatedFramePosition>();
			ObservableUniqueList<HomologousFramePositionPair> homologousFramePositionPairs = transformationEquations.getHomologousFramePositionPairs();

			if (this.interpolation.get() != null) {
				for (HomologousFramePositionPair homologousFramePositionPair : homologousFramePositionPairs) {
					if (homologousFramePositionPair.isEnable()) {
						EstimatedFramePosition estimatedTargetPosition = EstimatedFramePosition.create(dim);
						estimatedTargetPosition.setX0(homologousFramePositionPair.getTargetSystemPosition().getX0());
						estimatedTargetPosition.setY0(homologousFramePositionPair.getTargetSystemPosition().getY0());
						estimatedTargetPosition.setZ0(homologousFramePositionPair.getTargetSystemPosition().getZ0());
						estimatedTargetPositions.put(homologousFramePositionPair.getName(), estimatedTargetPosition);
					}
				}
			}
			
			for (FramePositionPair framePositionPair : this.framePositionPairs) {
				if (framePositionPair.isEnable()) {
					transformationEquations.transform(framePositionPair, Dp);

					if (this.interpolation.get() != null && estimatedTargetPositions.containsKey(framePositionPair.getName())) {
						EstimatedFramePosition estimatedTargetPosition = estimatedTargetPositions.get(framePositionPair.getName());
						estimatedTargetPosition.setResidualX(framePositionPair.getTargetSystemPosition().getX() - estimatedTargetPosition.getX());
						estimatedTargetPosition.setResidualY(framePositionPair.getTargetSystemPosition().getY() - estimatedTargetPosition.getY());
						estimatedTargetPosition.setResidualZ(framePositionPair.getTargetSystemPosition().getZ() - estimatedTargetPosition.getZ());
					}
				}
			}
			
			if (this.interpolation.get() != null) {
				this.interpolation.get().interpolate(estimatedTargetPositions.values(), this.framePositionPairs);
			}
		}
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
	
	public static SimplePositionPair deriveCenterOfMasses(Collection<? extends PositionPair<?,?>> positionPairs, Collection<Restriction> restrictions, Map<ParameterRestrictionType, Restriction> supportedRestrictions) {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		double X0 = 0, Y0 = 0, Z0 = 0;
		for (PositionPair<?,?> positionPair : positionPairs) {
			if (!positionPair.isEnable())
				continue;
			
			Positionable source = positionPair.getSourceSystemPosition();
			Positionable target = positionPair.getTargetSystemPosition();
			
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

		if (restrictions != null && supportedRestrictions != null && 
				(restrictions.contains(supportedRestrictions.get(ParameterRestrictionType.FIXED_SHIFT_X)) ||
						restrictions.contains(supportedRestrictions.get(ParameterRestrictionType.FIXED_SHIFT_Y)) ||
						restrictions.contains(supportedRestrictions.get(ParameterRestrictionType.FIXED_SHIFT_Z)))) {

			x0 = 0;
			X0 = 0;

			y0 = 0;
			Y0 = 0;

			z0 = 0;
			Z0 = 0;
		}
				
		return new SimplePositionPair(
				"CENTER_OF_MASSES",
				x0, y0, z0, 
				X0, Y0, Z0
		);
	}
	
	public boolean addRestriction(ParameterRestrictionType parameterRestrictionType) {
		Map<ParameterRestrictionType, Restriction> supportedRestrictions = this.getSupportedParameterRestrictions();
		if (!supportedRestrictions.containsKey(parameterRestrictionType)) 
			return false;
		
		Restriction restriction = supportedRestrictions.get(parameterRestrictionType);
		
		if (this.getRestrictions().contains(restriction))
			return false;
		
		if (this.getRestrictionToParameterMap().containsKey(parameterRestrictionType))
			this.fixedUnknownParameterTypes.add(this.getRestrictionToParameterMap().get(parameterRestrictionType));
		
		return this.getRestrictions().add(restriction);
	}

	public boolean removeRestriction(ParameterRestrictionType parameterRestrictionType) {
		Map<ParameterRestrictionType, Restriction> supportedRestrictions = this.getSupportedParameterRestrictions();
		if (!supportedRestrictions.containsKey(parameterRestrictionType)) 
			return false;
		
		if (this.getRestrictionToParameterMap().containsKey(parameterRestrictionType))
			this.fixedUnknownParameterTypes.remove(this.getRestrictionToParameterMap().get(parameterRestrictionType));
		
		Restriction restriction = supportedRestrictions.get(parameterRestrictionType);
		return this.getRestrictions().remove(restriction);
	}
	
	public boolean isFixedParameter(UnknownParameter parameter) {
		return parameter.getProcessingType() == ProcessingType.FIXED || this.fixedUnknownParameterTypes.contains(parameter.getParameterType());
	}
	
	abstract Map<ParameterRestrictionType, ParameterType> getRestrictionToParameterMap();
}