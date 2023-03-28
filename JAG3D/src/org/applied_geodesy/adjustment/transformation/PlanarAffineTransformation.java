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

import org.applied_geodesy.adjustment.transformation.equation.PlanarAffineEquations;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.InverseTangentRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction.SignType;
import org.applied_geodesy.util.ObservableUniqueList;

import com.derletztekick.tools.geodesy.Constant;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class PlanarAffineTransformation extends Transformation {

	private final Map<ParameterRestrictionType, ParameterType> restrictionToParameterMap = Map.ofEntries(
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_X, ParameterType.SHIFT_X),
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_Y, ParameterType.SHIFT_Y),
			
			Map.entry(ParameterRestrictionType.FIXED_SCALE_X, ParameterType.SCALE_X),
			Map.entry(ParameterRestrictionType.FIXED_SCALE_Y, ParameterType.SCALE_Y),
			
			Map.entry(ParameterRestrictionType.FIXED_SHEAR_Z, ParameterType.SHEAR_Z),
			Map.entry(ParameterRestrictionType.FIXED_ROTATION_Z, ParameterType.EULER_ANGLE_Z)
	);
	
	private final PlanarAffineEquations planarAffineEquations;
	
	public PlanarAffineTransformation() {
		this.planarAffineEquations = new PlanarAffineEquations();
		
		UnknownParameter shiftX = this.planarAffineEquations.getUnknownParameter(ParameterType.SHIFT_X);
		UnknownParameter shiftY = this.planarAffineEquations.getUnknownParameter(ParameterType.SHIFT_Y);
		
		UnknownParameter a11 = this.planarAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_11);
		UnknownParameter a12 = this.planarAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_12);
		
		UnknownParameter a21 = this.planarAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_21);
		UnknownParameter a22 = this.planarAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_22);
		
		UnknownParameter eulerAngleZ = new UnknownParameter(ParameterType.EULER_ANGLE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter tmpScaleX = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 1.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter tmpScaleY = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 1.0, false, ProcessingType.POSTPROCESSING);
		
		UnknownParameter tmpShearX = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter tmpShearY = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 0.0, false, ProcessingType.POSTPROCESSING);
		
		UnknownParameter scaleX = new UnknownParameter(ParameterType.SCALE_X, false, 1.0, 1.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter scaleY = new UnknownParameter(ParameterType.SCALE_Y, false, 1.0, 1.0, true, ProcessingType.POSTPROCESSING);

		UnknownParameter shearZ = new UnknownParameter(ParameterType.SHEAR_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter zero = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, false, ProcessingType.FIXED);
		UnknownParameter one  = new UnknownParameter(ParameterType.CONSTANT, false, 1.0, false, ProcessingType.FIXED);

		/* Readable transformation parameters */
		// Derive Euler angles from quaternion
		Restriction eulerAngleZRestriction = new InverseTangentRestriction(false, a21, a11, eulerAngleZ);
		
		// Derive scale
		Restriction tmpScaleXRestriction = new ProductSumRestriction(false, List.of(a11, a21), List.of(a11, a21), 0.5, List.of(SignType.PLUS, SignType.PLUS), tmpScaleX);
		Restriction tmpScaleYRestriction = new ProductSumRestriction(false, List.of(a12, a22), List.of(a12, a22), 0.5, List.of(SignType.PLUS, SignType.PLUS), tmpScaleY);

		Restriction scaleXRestriction = new ProductSumRestriction(false, List.of(tmpScaleX, one), List.of(one, one), List.of(SignType.PLUS, SignType.PLUS), scaleX);
		Restriction scaleYRestriction = new ProductSumRestriction(false, List.of(tmpScaleY, one), List.of(one, one), List.of(SignType.PLUS, SignType.PLUS), scaleY);
	
		// Derive shear
		Restriction numeratorShearRestriction   = new ProductSumRestriction(false, List.of(a11, a21), List.of(a12, a22), List.of(SignType.MINUS, SignType.PLUS), tmpShearX);
		Restriction denominatorShearRestriction = new ProductSumRestriction(false, List.of(a21, a11), List.of(a12, a22), List.of(SignType.PLUS,  SignType.PLUS), tmpShearY);		
		Restriction shearZRestriction = new InverseTangentRestriction(false, tmpShearX, tmpShearY, shearZ);
		
		/* Restriction to fix parameters e.g. scale == 1.0 */
		// Fixed shift
		Restriction fixedShiftXRestriction = new AverageRestriction(false, List.of(shiftX), zero);
		Restriction fixedShiftYRestriction = new AverageRestriction(false, List.of(shiftY), zero);
		
		// Fixed rotation
		Restriction fixedRotationZRestriction = new AverageRestriction(false, List.of(a21), zero);
		
		// Fixed scale (a11*a11 + a21*a21) - 1.0; (a12*a12 + a22*a22) - 1.0
		Restriction fixedScaleXRestriction = new ProductSumRestriction(false, List.of(a11, a21), List.of(a11, a21), List.of(SignType.PLUS, SignType.PLUS), one);
		Restriction fixedScaleYRestriction = new ProductSumRestriction(false, List.of(a12, a22), List.of(a12, a22), List.of(SignType.PLUS, SignType.PLUS), one);

		// Fixed shear -a11*a12 + a21*a22
		Restriction fixedShearZRestriction = new ProductSumRestriction(false, List.of(a11, a21), List.of(a12, a22), List.of(SignType.MINUS, SignType.PLUS), zero);
		
		// Identical scales x == y (a11*a11 + a21*a21) - (a12*a12 + a22*a22) == a11*a11 + a21*a21 - a12*a12 - a22*a22
		Restriction identicalScalesXYRestriction = new ProductSumRestriction(false, List.of(a11, a21, a12, a22), List.of(a11, a21, a12, a22), List.of(SignType.PLUS, SignType.PLUS, SignType.MINUS, SignType.MINUS), zero);

		List<UnknownParameter> unknownParameters = new ArrayList<UnknownParameter>();
		unknownParameters.add(shiftX);
		unknownParameters.add(shiftY);
		
		unknownParameters.add(tmpScaleX);
		unknownParameters.add(tmpScaleY);
		
		unknownParameters.add(scaleX);
		unknownParameters.add(scaleY);

		unknownParameters.add(eulerAngleZ);
		
		unknownParameters.add(tmpShearX);
		unknownParameters.add(tmpShearY);
		unknownParameters.add(shearZ);
		
		unknownParameters.add(a11);
		unknownParameters.add(a12);
		
		unknownParameters.add(a21);
		
		unknownParameters.add(a22);
		
		unknownParameters.add(zero);
		unknownParameters.add(one);
				
		this.set(this.planarAffineEquations);
		this.getUnknownParameters().setAll(unknownParameters);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_X, fixedShiftXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_Y, fixedShiftYRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_X, fixedScaleXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_Y, fixedScaleYRestriction);

		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_ROTATION_Z, fixedRotationZRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHEAR_Z,    fixedShearZRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.IDENTICAL_SCALE_XY, identicalScalesXYRestriction);
			
		this.getPostProcessingCalculations().addAll(
				eulerAngleZRestriction,
				
				tmpScaleXRestriction,
				tmpScaleYRestriction,
				
				scaleXRestriction,
				scaleYRestriction,

				numeratorShearRestriction,
				denominatorShearRestriction,
				
				shearZRestriction
		);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, PlanarAffineTransformation transformation, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, transformation.planarAffineEquations, parameterRestrictions);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		
		ObservableUniqueList<Restriction> restrictions = this.getRestrictions();
		Set<ParameterRestrictionType> parameterRestrictions = new HashSet<ParameterRestrictionType>(restrictions.size());
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_Y);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_Y);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHEAR_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_ROTATION_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_ROTATION_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_XY)))
			parameterRestrictions.add(ParameterRestrictionType.IDENTICAL_SCALE_XY);
		
		deriveInitialGuess(this.planarAffineEquations.getHomologousFramePositionPairs(), this.planarAffineEquations, parameterRestrictions);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, PlanarAffineEquations planarAffineEquations, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		double tx = 0;
		double ty = 0;
		
		double a11 = 1;
		double a12 = 0;
		double a21 = 0;
		double a22 = 1;
		
		double x0 = 0, y0 = 0;
		double X0 = 0, Y0 = 0;
		
		int nop = 0;
		for (HomologousFramePositionPair HomologousFramePositionPair : points) {
			if (!HomologousFramePositionPair.isEnable())
				continue;
			
			HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
			HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();
			
			x0 += pointSrc.getX0();
			y0 += pointSrc.getY0();
			
			X0 += pointTrg.getX0();
			Y0 += pointTrg.getY0();
			
			nop++;
		}
		
		if (nop <= 0)
			throw new IllegalArgumentException("Error, the number of points zero.");
		
		x0 /= nop;
		y0 /= nop;
		
		X0 /= nop;
		Y0 /= nop;

		if (parameterRestrictions != null && parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_Z) && parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHEAR_Z)) {
			a11 = 1;
			a22 = 1;
		} 
		else {
			double o = 0.0, a = 0.0, oa = 0.0;
			for (HomologousFramePositionPair HomologousFramePositionPair : points) {
				if (!HomologousFramePositionPair.isEnable())
					continue;

				HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
				HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();

				double x = pointSrc.getX0() - x0;
				double y = pointSrc.getY0() - y0;

				double X = pointTrg.getX0() - X0;
				double Y = pointTrg.getY0() - Y0;

				o  += x * Y - y * X;
				a  += x * X + y * Y;
				
				oa += x*x + y*y;
			}
			
			if (oa <= Constant.EPS)
				throw new MatrixSingularException("Error, system of equations is singular.");
			
			o /= oa;
			a /= oa;
			
			a11 = a;
			a12 = o;
			a21 = o;
			a22 = a;
		}

		if (parameterRestrictions != null && !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_X))
			tx = X0 - (a11*x0 - a12*y0);

		if (parameterRestrictions != null && !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Y))
			ty = Y0 - (a21*x0 + a22*y0);

		planarAffineEquations.setInitialGuess(tx, ty, a11, a12, a21, a22);	
	}
	

	@Override
	Map<ParameterRestrictionType, ParameterType> getRestrictionToParameterMap() {
		return this.restrictionToParameterMap;
	}
}
