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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.transformation.equation.HeightEquations;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.util.ObservableUniqueList;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class HeightTransformation extends Transformation {

	private final Map<ParameterRestrictionType, ParameterType> restrictionToParameterMap = Map.ofEntries(
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_Z, ParameterType.SHIFT_Z),
			Map.entry(ParameterRestrictionType.FIXED_SCALE_Z, ParameterType.SCALE_Z)
	);
	
	private final HeightEquations heightEquations;
	
	public HeightTransformation() {
		this.heightEquations = new HeightEquations();
		
		UnknownParameter shiftZ = this.heightEquations.getUnknownParameter(ParameterType.SHIFT_Z);
		UnknownParameter scaleZ = this.heightEquations.getUnknownParameter(ParameterType.SCALE_Z);

		UnknownParameter zero = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, false, ProcessingType.FIXED);
		UnknownParameter one  = new UnknownParameter(ParameterType.CONSTANT, false, 1.0, false, ProcessingType.FIXED);

		/* Restriction to fix parameters e.g. scale == 1.0 */
		// Fixed shift
		Restriction fixedShiftZRestriction = new AverageRestriction(false, List.of(shiftZ), zero);
		
		// Fixed scale 
		Restriction fixedScaleZRestriction = new AverageRestriction(false, List.of(scaleZ), one);
		
		List<UnknownParameter> unknownParameters = new ArrayList<UnknownParameter>();
		unknownParameters.add(shiftZ);
		unknownParameters.add(scaleZ);
		
		unknownParameters.add(zero);
		unknownParameters.add(one);
				
		this.set(this.heightEquations);
		this.getUnknownParameters().setAll(unknownParameters);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_Z, fixedShiftZRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_Z, fixedScaleZRestriction);
		
			
//		this.getPostProcessingCalculations().addAll(
//		);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, HeightTransformation transformation, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, transformation.heightEquations, parameterRestrictions);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		
		ObservableUniqueList<Restriction> restrictions = this.getRestrictions();
		Set<ParameterRestrictionType> parameterRestrictions = new HashSet<ParameterRestrictionType>(restrictions.size());
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_Z);

		deriveInitialGuess(this.heightEquations.getHomologousFramePositionPairs(), this.heightEquations, parameterRestrictions);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, HeightEquations heightEquations, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		double tz = 0;
		double mz = 1;

		double z0 = 0, Z0 = 0;
		int nop = 0;
		for (HomologousFramePositionPair HomologousFramePositionPair : points) {
			if (!HomologousFramePositionPair.isEnable())
				continue;
			
			HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
			HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();
			
			z0 += pointSrc.getZ0();
			Z0 += pointTrg.getZ0();
			
			nop++;
		}
		
		if (nop <= 0)
			throw new IllegalArgumentException("Error, the number of points zero.");
		
		z0 /= nop;
		Z0 /= nop;

		if (parameterRestrictions != null && parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_Z)) {
			mz = 1;
		} 
		else {
			mz = 0.0;
			nop = 0;
			for (HomologousFramePositionPair HomologousFramePositionPair : points) {
				if (!HomologousFramePositionPair.isEnable())
					continue;

				HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
				HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();

				double dz = pointSrc.getZ0() - z0;
				double dZ = pointTrg.getZ0() - Z0;

				mz += Math.abs(dz/dZ);
				nop++;
			}
			
			if (mz <= Constant.EPS)
				throw new MatrixSingularException("Error, system of equations is singular.");
			
			mz /= nop;		}

		if (parameterRestrictions != null && !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Z))
			tz = Z0 - (mz*z0);

		heightEquations.setInitialGuess(tz, mz);	
	}
	

	@Override
	Map<ParameterRestrictionType, ParameterType> getRestrictionToParameterMap() {
		return this.restrictionToParameterMap;
	}
}