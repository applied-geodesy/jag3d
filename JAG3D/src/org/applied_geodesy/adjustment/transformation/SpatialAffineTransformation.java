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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.equation.SpatialAffineEquations;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.EulerAxisType;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.adjustment.transformation.restriction.ShearAngleRestriction;
import org.applied_geodesy.util.ObservableUniqueList;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction.SignType;
import org.applied_geodesy.adjustment.transformation.restriction.QuaternionEulerAngleRestriction;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.QR;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public class SpatialAffineTransformation extends Transformation {
	
	private final Map<ParameterRestrictionType, ParameterType> restrictionToParameterMap = Map.ofEntries(
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_X, ParameterType.SHIFT_X),
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_Y, ParameterType.SHIFT_Y),
			Map.entry(ParameterRestrictionType.FIXED_SHIFT_Z, ParameterType.SHIFT_Z),
			
			Map.entry(ParameterRestrictionType.FIXED_SCALE_X, ParameterType.SCALE_X),
			Map.entry(ParameterRestrictionType.FIXED_SCALE_Y, ParameterType.SCALE_Y),
			Map.entry(ParameterRestrictionType.FIXED_SCALE_Z, ParameterType.SCALE_Z),
			
			Map.entry(ParameterRestrictionType.FIXED_SHEAR_X, ParameterType.SHEAR_X),
			Map.entry(ParameterRestrictionType.FIXED_SHEAR_Y, ParameterType.SHEAR_Y),
			Map.entry(ParameterRestrictionType.FIXED_SHEAR_Z, ParameterType.SHEAR_Z),
			
			Map.entry(ParameterRestrictionType.FIXED_ROTATION_X, ParameterType.EULER_ANGLE_X),
			Map.entry(ParameterRestrictionType.FIXED_ROTATION_Y, ParameterType.EULER_ANGLE_Y),
			Map.entry(ParameterRestrictionType.FIXED_ROTATION_Z, ParameterType.EULER_ANGLE_Z)
	);
	
	private final SpatialAffineEquations spatialAffineEquations;
	
	public SpatialAffineTransformation() {
		this.spatialAffineEquations = new SpatialAffineEquations();
		
		UnknownParameter shiftX = this.spatialAffineEquations.getUnknownParameter(ParameterType.SHIFT_X);
		UnknownParameter shiftY = this.spatialAffineEquations.getUnknownParameter(ParameterType.SHIFT_Y);
		UnknownParameter shiftZ = this.spatialAffineEquations.getUnknownParameter(ParameterType.SHIFT_Z);
		
		UnknownParameter q0 = this.spatialAffineEquations.getUnknownParameter(ParameterType.QUATERNION_Q0);
		UnknownParameter q1 = this.spatialAffineEquations.getUnknownParameter(ParameterType.QUATERNION_Q1);
		UnknownParameter q2 = this.spatialAffineEquations.getUnknownParameter(ParameterType.QUATERNION_Q2);
		UnknownParameter q3 = this.spatialAffineEquations.getUnknownParameter(ParameterType.QUATERNION_Q3);
		
		UnknownParameter s11 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_11);
		UnknownParameter s12 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_12);
		UnknownParameter s13 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_13);

		UnknownParameter s22 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_22);
		UnknownParameter s23 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_23);

		UnknownParameter s33 = this.spatialAffineEquations.getUnknownParameter(ParameterType.AUXILIARY_ELEMENT_33);
		
		UnknownParameter quaternionLength = this.spatialAffineEquations.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		UnknownParameter eulerAngleX = new UnknownParameter(ParameterType.EULER_ANGLE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter eulerAngleY = new UnknownParameter(ParameterType.EULER_ANGLE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter eulerAngleZ = new UnknownParameter(ParameterType.EULER_ANGLE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter tmpScaleX = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 1.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter tmpScaleY = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 1.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter tmpScaleZ = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, 1.0, false, ProcessingType.POSTPROCESSING);
		
		UnknownParameter scaleX = new UnknownParameter(ParameterType.SCALE_X, false, 1.0, 1.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter scaleY = new UnknownParameter(ParameterType.SCALE_Y, false, 1.0, 1.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter scaleZ = new UnknownParameter(ParameterType.SCALE_Z, false, 1.0, 1.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter shearX = new UnknownParameter(ParameterType.SHEAR_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter shearY = new UnknownParameter(ParameterType.SHEAR_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter shearZ = new UnknownParameter(ParameterType.SHEAR_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter zero = new UnknownParameter(ParameterType.CONSTANT, false, 0.0, false, ProcessingType.FIXED);
		UnknownParameter one  = new UnknownParameter(ParameterType.CONSTANT, false, 1.0, false, ProcessingType.FIXED);

		/* Readable transformation parameters */
		// Derive Euler angles from quaternion
		Restriction eulerAngleXRestriction = new QuaternionEulerAngleRestriction(false, EulerAxisType.X_AXIS, q0, q1, q2, q3, eulerAngleX);
		Restriction eulerAngleYRestriction = new QuaternionEulerAngleRestriction(false, EulerAxisType.Y_AXIS, q0, q1, q2, q3, eulerAngleY);
		Restriction eulerAngleZRestriction = new QuaternionEulerAngleRestriction(false, EulerAxisType.Z_AXIS, q0, q1, q2, q3, eulerAngleZ);

		// Derive scale
		Restriction tmpScaleXRestriction = new ProductSumRestriction(false, List.of(s11), List.of(s11), 0.5, List.of(SignType.PLUS), tmpScaleX);
		Restriction tmpScaleYRestriction = new ProductSumRestriction(false, List.of(s12, s22), List.of(s12, s22), 0.5, List.of(SignType.PLUS, SignType.PLUS), tmpScaleY);
		Restriction tmpScaleZRestriction = new ProductSumRestriction(false, List.of(s13, s23, s33), List.of(s13, s23, s33), 0.5, List.of(SignType.PLUS, SignType.PLUS, SignType.PLUS), tmpScaleZ);
		
		Restriction scaleXRestriction = new ProductSumRestriction(false, List.of(tmpScaleX, one), List.of(one, one), 1.0, List.of(SignType.PLUS, SignType.PLUS), scaleX);
		Restriction scaleYRestriction = new ProductSumRestriction(false, List.of(tmpScaleY, one), List.of(one, one), 1.0, List.of(SignType.PLUS, SignType.PLUS), scaleY);
		Restriction scaleZRestriction = new ProductSumRestriction(false, List.of(tmpScaleZ, one), List.of(one, one), 1.0, List.of(SignType.PLUS, SignType.PLUS), scaleZ);
		
		// Derive shear
		Restriction shearXRestriction = new ShearAngleRestriction(false, EulerAxisType.X_AXIS, s12, s13, s22, s23, s33, shearX);
		Restriction shearYRestriction = new ShearAngleRestriction(false, EulerAxisType.Y_AXIS, s12, s13, s22, s23, s33, shearY);
		Restriction shearZRestriction = new ShearAngleRestriction(false, EulerAxisType.Z_AXIS, s12, s13, s22, s23, s33, shearZ);
		
		/* Restriction to fix parameters e.g. scale == 1.0 */
		// Fixed shift
		Restriction fixedShiftXRestriction = new AverageRestriction(false, List.of(shiftX), zero);
		Restriction fixedShiftYRestriction = new AverageRestriction(false, List.of(shiftY), zero);
		Restriction fixedShiftZRestriction = new AverageRestriction(false, List.of(shiftZ), zero);
		
		// Fixed scale
		Restriction fixedScaleXRestriction = new ProductSumRestriction(false, List.of(s11), List.of(s11), List.of(SignType.PLUS), one);
		Restriction fixedScaleYRestriction = new ProductSumRestriction(false, List.of(s12, s22), List.of(s12, s22), 0.5, List.of(SignType.PLUS, SignType.PLUS), one);
		Restriction fixedScaleZRestriction = new ProductSumRestriction(false, List.of(s13, s23, s33), List.of(s13, s23, s33), 0.5, List.of(SignType.PLUS, SignType.PLUS, SignType.PLUS), one);
		
		// Fixed shear
		Restriction fixedShearXRestriction = new AverageRestriction(false, List.of(s23), zero);
		Restriction fixedShearYRestriction = new AverageRestriction(false, List.of(s13), zero);
		Restriction fixedShearZRestriction = new AverageRestriction(false, List.of(s12), zero);
				
		// Fixed rotation
		Restriction fixedRotationXRestriction = new ProductSumRestriction(false, List.of(q2, q0), List.of(q3, q1), List.of(SignType.PLUS, SignType.MINUS), zero);
		Restriction fixedRotationYRestriction = new ProductSumRestriction(false, List.of(q1, q0), List.of(q3, q2), List.of(SignType.PLUS, SignType.MINUS), zero);
		Restriction fixedRotationZRestriction = new ProductSumRestriction(false, List.of(q1, q0), List.of(q2, q3), List.of(SignType.PLUS, SignType.MINUS), zero);
		
		// Identical scales x == y, x == z, y == z
		Restriction identicalScalesXYRestriction = new ProductSumRestriction(false, List.of(s12, s22, s11), List.of(s12, s22, s11), List.of(SignType.PLUS, SignType.PLUS, SignType.MINUS), zero);
		Restriction identicalScalesXZRestriction = new ProductSumRestriction(false, List.of(s13, s23, s33, s11), List.of(s13, s23, s33, s11), List.of(SignType.PLUS, SignType.PLUS, SignType.PLUS, SignType.MINUS), zero);
		Restriction identicalScalesYZRestriction = new ProductSumRestriction(false, List.of(s13, s23, s33, s12, s22), List.of(s13, s23, s33, s12, s22), List.of(SignType.PLUS, SignType.PLUS, SignType.PLUS, SignType.MINUS, SignType.MINUS), zero);
		
		List<UnknownParameter> unknownParameters = new ArrayList<UnknownParameter>();
		unknownParameters.add(shiftX);
		unknownParameters.add(shiftY);
		unknownParameters.add(shiftZ);
		
		unknownParameters.add(tmpScaleX);
		unknownParameters.add(tmpScaleY);
		unknownParameters.add(tmpScaleZ);
		
		unknownParameters.add(scaleX);
		unknownParameters.add(scaleY);
		unknownParameters.add(scaleZ);
		
		unknownParameters.add(eulerAngleX);
		unknownParameters.add(eulerAngleY);
		unknownParameters.add(eulerAngleZ);
		
		unknownParameters.add(shearX);
		unknownParameters.add(shearY);
		unknownParameters.add(shearZ);
		
		unknownParameters.add(q0);
		unknownParameters.add(q1);
		unknownParameters.add(q2);
		unknownParameters.add(q3);
		
		unknownParameters.add(s11);
		unknownParameters.add(s12);
		unknownParameters.add(s13);
		
		unknownParameters.add(s22);
		unknownParameters.add(s23);
		
		unknownParameters.add(s33);
		
		unknownParameters.add(quaternionLength);
		
		unknownParameters.add(zero);
		unknownParameters.add(one);
				
		this.set(this.spatialAffineEquations);
		this.getUnknownParameters().setAll(unknownParameters);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_X, fixedShiftXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_Y, fixedShiftYRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHIFT_Z, fixedShiftZRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_X, fixedScaleXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_Y, fixedScaleYRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SCALE_Z, fixedScaleZRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_ROTATION_X, fixedRotationXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_ROTATION_Y, fixedRotationYRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_ROTATION_Z, fixedRotationZRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHEAR_X, fixedShearXRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHEAR_Y, fixedShearYRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.FIXED_SHEAR_Z, fixedShearZRestriction);
		
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.IDENTICAL_SCALE_XY, identicalScalesXYRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.IDENTICAL_SCALE_XZ, identicalScalesXZRestriction);
		this.getSupportedParameterRestrictions().put(ParameterRestrictionType.IDENTICAL_SCALE_YZ, identicalScalesYZRestriction);
			
		this.getPostProcessingCalculations().addAll(
				eulerAngleXRestriction,
				eulerAngleYRestriction,
				eulerAngleZRestriction,
				
				tmpScaleXRestriction,
				tmpScaleYRestriction,
				tmpScaleZRestriction,
				
				scaleXRestriction,
				scaleYRestriction,
				scaleZRestriction,
				
				shearXRestriction,
				shearYRestriction,
				shearZRestriction
		);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, SpatialAffineTransformation transformation, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, transformation.spatialAffineEquations, parameterRestrictions);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		
		ObservableUniqueList<Restriction> restrictions = this.getRestrictions();
		Set<ParameterRestrictionType> parameterRestrictions = new HashSet<ParameterRestrictionType>(restrictions.size());
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_Y);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHIFT_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHIFT_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_Y);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SCALE_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHEAR_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHEAR_Y);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_SHEAR_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_ROTATION_X)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_ROTATION_X);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_ROTATION_Y)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_ROTATION_Y);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_ROTATION_Z)))
			parameterRestrictions.add(ParameterRestrictionType.FIXED_ROTATION_Z);
		
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_XY)))
			parameterRestrictions.add(ParameterRestrictionType.IDENTICAL_SCALE_XY);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_XZ)))
			parameterRestrictions.add(ParameterRestrictionType.IDENTICAL_SCALE_XZ);
		if (restrictions.contains(this.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_YZ)))
			parameterRestrictions.add(ParameterRestrictionType.IDENTICAL_SCALE_YZ);
		
		deriveInitialGuess(this.spatialAffineEquations.getHomologousFramePositionPairs(), this.spatialAffineEquations, parameterRestrictions);
	}
	
	public static void deriveInitialGuess(Collection<HomologousFramePositionPair> points, SpatialAffineEquations spatialAffineEquations, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		if (parameterRestrictions == null || (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHEAR_X) && !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHEAR_Y) && !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHEAR_Z)))
			deriveInitialGuessViaQRdecomposition(points, spatialAffineEquations, parameterRestrictions);
		else
			deriveInitialGuessViaEigendecomposition(points, spatialAffineEquations, parameterRestrictions);
	}
	
	private static void deriveInitialGuessViaQRdecomposition(Collection<HomologousFramePositionPair> points, SpatialAffineEquations spatialAffineEquations, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		double tx = 0;
		double ty = 0;
		double tz = 0;
		
		double q0 = 1;
		double q1 = 0;
		double q2 = 0;
		double q3 = 0;
		
		double s11 = 1;
		double s12 = 0;
		double s13 = 0;

		double s22 = 1;
		double s23 = 0;

		double s33 = 1;
		
		Matrix N = new UpperSymmPackMatrix(9);
		Vector n = new DenseVector(N.numRows());
		
		double x0 = 0, y0 = 0, z0 = 0;
		double X0 = 0, Y0 = 0, Z0 = 0;
		
		int nop = 0;
		for (HomologousFramePositionPair HomologousFramePositionPair : points) {
			if (!HomologousFramePositionPair.isEnable())
				continue;
			
			HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
			HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();
			
			x0 += pointSrc.getX0();
			y0 += pointSrc.getY0();
			z0 += pointSrc.getZ0();
			
			X0 += pointTrg.getX0();
			Y0 += pointTrg.getY0();
			Z0 += pointTrg.getZ0();
			
			nop++;
		}
		
		if (nop <= 0)
			throw new IllegalArgumentException("Error, the number of points zero.");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		X0 /= nop;
		Y0 /= nop;
		Z0 /= nop;

		for (HomologousFramePositionPair HomologousFramePositionPair : points) {
			if (!HomologousFramePositionPair.isEnable())
				continue;
			
			HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
			HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();
			
			double x = pointSrc.getX0() - x0;
			double y = pointSrc.getY0() - y0;
			double z = pointSrc.getZ0() - z0;

			double X = pointTrg.getX0() - X0;
			double Y = pointTrg.getY0() - Y0;
			double Z = pointTrg.getZ0() - Z0;
			
			for (int row = 0; row <= 6; row += 3) {
				N.add(row, row,   x*x);
				N.add(row, row+1, x*y);
				N.add(row, row+2, x*z);
				
				N.add(row+1, row+1, y*y);
				N.add(row+1, row+2, y*z);
				
				N.add(row+2, row+2, z*z);
				
				double w = 0;
				if (row == 0)
					w = X;
				else if (row == 3)
					w = Y;
				else
					w = Z;
				
				n.add(row,   x*w);
				n.add(row+1, y*w);
				n.add(row+2, z*w);
			}			
		}

		Vector t = new DenseVector(n.size());

		try {
			N.solve(n, t);
		}
		catch (Exception e) {
			N = MathExtension.pinv(N, -1);
			N.mult(n, t);
		}

		DenseMatrix T = new DenseMatrix(3,3);
		for (int i=0; i<9; i++)
			T.set(i/3, i%3, t.get(i));

		QR qr = QR.factorize(T);
		Matrix Q = qr.getQ();
		Matrix R = qr.getR();
		
//		double qx = (Q.get(1,1) * Q.get(2,2)) - (Q.get(2,1) * Q.get(1,2));
//		double qy = (Q.get(1,0) * Q.get(2,2)) - (Q.get(2,0) * Q.get(1,2));
//		double qz = (Q.get(1,0) * Q.get(2,1)) - (Q.get(2,0) * Q.get(1,1));
//		double det3 = Q.get(0,0) * qx - Q.get(0,1) * qy + Q.get(0,2) * qz;
			
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_X) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_Y) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_Z)) {
			q0 = 0.5*Math.sqrt(1.0 + Q.get(0,0) + Q.get(1,1) + Q.get(2,2));
			q1 = 0.25 * (Q.get(2,1) - Q.get(1,2)) / q0;
			q2 = 0.25 * (Q.get(0,2) - Q.get(2,0)) / q0;
			q3 = 0.25 * (Q.get(1,0) - Q.get(0,1)) / q0;
		}
		
		s11 = R.get(0,0);
		s12 = R.get(0,1);
		s13 = R.get(0,2);
		
		s22 = R.get(1,1);
		s23 = R.get(1,2);
		
		s33 = R.get(2,2);

		double smxP = s11*x0 + s12*y0 + s13*z0;
		double smyP = s22*y0 + s23*z0;
		double smzP = s33*z0;
		
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_X)) {
			// Elements of rotation matrix
			double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
			double r12 = 2.0 * (q1*q2 - q0*q3);
			double r13 = 2.0 * (q1*q3 + q0*q2);
			
			tx = X0 - (r11*smxP + r12*smyP + r13*smzP);
		}
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Y)) {
			// Elements of rotation matrix
			double r21 = 2.0 * (q1*q2 + q0*q3);
			double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
			double r23 = 2.0 * (q2*q3 - q0*q1);

			ty = Y0 - (r21*smxP + r22*smyP + r23*smzP);
		}
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Z)) {
			// Elements of rotation matrix
			double r31 = 2.0 * (q1*q3 - q0*q2);
			double r32 = 2.0 * (q2*q3 + q0*q1);
			double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;
			
			tz = Z0 - (r31*smxP + r32*smyP + r33*smzP);
		}

		spatialAffineEquations.setInitialGuess(tx, ty, tz, q0, q1, q2, q3, s11, s12, s13, s22,  s23,  s33);	
	}
	
	private static void deriveInitialGuessViaEigendecomposition(Collection<HomologousFramePositionPair> points, SpatialAffineEquations spatialAffineEquations, Set<ParameterRestrictionType> parameterRestrictions) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		double tx = 0;
		double ty = 0;
		double tz = 0;
		
		double q0 = 1;
		double q1 = 0;
		double q2 = 0;
		double q3 = 0;
		
		double s11 = 1;
		double s12 = 0;
		double s13 = 0;

		double s22 = 1;
		double s23 = 0;

		double s33 = 1;
		
		double m = 1;
		int dim = 3;

		double x0 = 0, y0 = 0, z0 = 0;
		double X0 = 0, Y0 = 0, Z0 = 0;
		int nop = 0;
		for (HomologousFramePositionPair HomologousFramePositionPair : points) {
			if (!HomologousFramePositionPair.isEnable())
				continue;
			
			HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
			HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();
			
			x0 += pointSrc.getX0();
			y0 += pointSrc.getY0();
			z0 += pointSrc.getZ0();
			
			X0 += pointTrg.getX0();
			Y0 += pointTrg.getY0();
			Z0 += pointTrg.getZ0();

			nop++;
		}
		
//		if (nop < 3)
//			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 3 points are needed. " + nop);
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		X0 /= nop;
		Y0 /= nop;
		Z0 /= nop;
		
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_X) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_Y) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_ROTATION_Z)) {
			DenseMatrix S = new DenseMatrix(3,3);

			for (HomologousFramePositionPair HomologousFramePositionPair : points) {
				if (!HomologousFramePositionPair.isEnable())
					continue;

				HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
				HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();

				double xk = pointSrc.getX0();
				double yk = pointSrc.getY0();
				double zk = pointSrc.getZ0();

				double Xk = pointTrg.getX0();
				double Yk = pointTrg.getY0();
				double Zk = pointTrg.getZ0();

				for (int i = 0; i < dim; i++) {
					double ds = 0;

					if (i==0)
						ds = xk - x0; 
					else if (i==1)
						ds = yk - y0;
					else 
						ds = zk - z0;

					for (int j = 0; j < dim; j++) {
						double dt = 0;

						if (j==0)
							dt = Xk - X0; 
						else if (j==1)
							dt = Yk - Y0;
						else 
							dt = Zk - Z0;

						S.add(i,j, ds * dt);
					}
				}			
			}

			UpperSymmPackMatrix N = new UpperSymmPackMatrix(4);
			N.set(0,0, S.get(0,0)+S.get(1,1)+S.get(2,2));
			N.set(0,1, S.get(1,2)-S.get(2,1));
			N.set(0,2, S.get(2,0)-S.get(0,2));
			N.set(0,3, S.get(0,1)-S.get(1,0));

			//N.set(1,0, S.get(1,2)-S.get(2,1));
			N.set(1,1, S.get(0,0)-S.get(1,1)-S.get(2,2));
			N.set(1,2, S.get(0,1)+S.get(1,0));
			N.set(1,3, S.get(2,0)+S.get(0,2));

			//N.set(2,0, S.get(2,0)-S.get(0,2));
			//N.set(2,1, S.get(0,1)+S.get(1,0));
			N.set(2,2,-S.get(0,0)+S.get(1,1)-S.get(2,2));
			N.set(2,3, S.get(1,2)+S.get(2,1));

			//N.set(3,0, S.get(0,1)-S.get(1,0));
			//N.set(3,1, S.get(2,0)+S.get(0,2));
			//N.set(3,2, S.get(1,2)+S.get(2,1));
			N.set(3,3,-S.get(0,0)-S.get(1,1)+S.get(2,2));

			SymmPackEVD evd = new SymmPackEVD(4, true, true);
			evd.factor(N);

			Matrix eigVec = evd.getEigenvectors();
			double eigVal[] = evd.getEigenvalues();

			int indexMaxEigVal = 0;
			double maxEigVal = eigVal[indexMaxEigVal];
			for (int i=indexMaxEigVal+1; i<eigVal.length; i++) {
				if (maxEigVal < eigVal[i]) {
					maxEigVal = eigVal[i];
					indexMaxEigVal = i;
				}
			}

			q0 = eigVec.get(0, indexMaxEigVal);
			q1 = eigVec.get(1, indexMaxEigVal);
			q2 = eigVec.get(2, indexMaxEigVal);
			q3 = eigVec.get(3, indexMaxEigVal);
		}

		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_X) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_Y) || !parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_Z)) {
			double m1 = 0, m2 = 0;

			for (HomologousFramePositionPair HomologousFramePositionPair : points) {
				if (!HomologousFramePositionPair.isEnable())
					continue;	

				HomologousFramePosition pointSrc = HomologousFramePositionPair.getSourceSystemPosition();
				HomologousFramePosition pointTrg = HomologousFramePositionPair.getTargetSystemPosition();

				double xk = pointSrc.getX0();
				double yk = pointSrc.getY0();
				double zk = pointSrc.getZ0();

				double Xk = pointTrg.getX0();
				double Yk = pointTrg.getY0();
				double Zk = pointTrg.getZ0();

				double dx = xk - x0;
				double dy = yk - y0;
				double dz = zk - z0;

				double dX = Xk - X0;
				double dY = Yk - Y0;
				double dZ = Zk - Z0;

				m1 += dX*((2.0*q0*q0-1.0+2.0*q1*q1)*dx + 2.0*(q1*q2-q0*q3)*dy + 2.0*(q1*q3+q0*q2)*dz);
				m1 += dY*(2.0*(q1*q2+q0*q3)*dx + (2.0*q0*q0-1.0+2.0*q2*q2)*dy + 2.0*(q2*q3-q0*q1)*dz);
				m1 += dZ*(2.0*(q1*q3-q0*q2)*dx + 2.0*(q2*q3+q0*q1)*dy + (2.0*q0*q0-1.0+2.0*q3*q3)*dz);

				m2 += dx*dx;
				m2 += dy*dy;
				m2 += dz*dz;
			}

			if (m1 > 0 && m2 > 0)
				m = m1/m2;

			// setze Scherung/Masstab
			if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_X)) {
				s11 = m;
				s12 = 0;
				s13 = 0;
			}
			if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_Y)) {
				s22 = m;
				s23 = 0;
			}
			if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SCALE_Z)) {
				s33 = m;
			}
		}
		
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_X))
			tx = X0 - m*((2.0*q0*q0-1.0+2.0*q1*q1)*x0 +         2.0*(q1*q2-q0*q3)*y0 +         2.0*(q1*q3+q0*q2)*z0);
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Y))
			ty = Y0 - m*(        2.0*(q1*q2+q0*q3)*x0 + (2.0*q0*q0-1.0+2.0*q2*q2)*y0 +         2.0*(q2*q3-q0*q1)*z0);
		if (!parameterRestrictions.contains(ParameterRestrictionType.FIXED_SHIFT_Z))
			tz = Z0 - m*(        2.0*(q1*q3-q0*q2)*x0 +         2.0*(q2*q3+q0*q1)*y0 + (2.0*q0*q0-1.0+2.0*q3*q3)*z0);

		spatialAffineEquations.setInitialGuess(tx, ty, tz, q0, q1, q2, q3, s11, s12, s13, s22,  s23,  s33);					
	}

	@Override
	Map<ParameterRestrictionType, ParameterType> getRestrictionToParameterMap() {
		return this.restrictionToParameterMap;
	}
}
