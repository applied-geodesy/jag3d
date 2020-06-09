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

package org.applied_geodesy.adjustment.geometry.surface;

import java.util.Collection;
import java.util.List;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.TrigonometricRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.TrigonometricRestriction.TrigonometricFunctionType;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cone;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class CircularConeFeature extends SurfaceFeature {
	
	private final Cone cone;

	public CircularConeFeature() {
		super(true);
		
		this.cone = new Cone();
		
		UnknownParameter A = this.cone.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT);
		UnknownParameter C = this.cone.getUnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT);
		
		UnknownParameter R21 = this.cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R21);
		
		A.setVisible(false);
		C.setVisible(false);
		R21.setProcessingType(ProcessingType.FIXED);

		UnknownParameter invB    = new UnknownParameter(ParameterType.MIDDLE_AXIS_COEFFICIENT, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter oneHalf = new UnknownParameter(ParameterType.CONSTANT,                false, 0.5, false, ProcessingType.FIXED);
		UnknownParameter phi     = new UnknownParameter(ParameterType.ANGLE,                   false, 0.0, true,  ProcessingType.POSTPROCESSING);

		AverageRestriction AequalsCRestriction            = new AverageRestriction(true, List.of(A), C);
		ProductSumRestriction invertedBRestriction        = new ProductSumRestriction(false, List.of(A, C), List.of(oneHalf, oneHalf), -1.0, Boolean.TRUE, invB);
		TrigonometricRestriction trigonometricRestriction = new TrigonometricRestriction(false, TrigonometricFunctionType.TANGENT, Boolean.TRUE, invB, phi);
		
		this.add(this.cone);
		
		this.getUnknownParameters().addAll(phi, invB, oneHalf);
		this.getRestrictions().add(AequalsCRestriction);
		this.getPostProcessingCalculations().addAll(
				invertedBRestriction,
				trigonometricRestriction
		);
	}
	
	public Cone getCone() {
		return this.cone;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, CircularConeFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.cone);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Cone cone) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			if (cone.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + cone.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 6)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 6 points are needed.");

		
		// derive initial guess
		if (nop > 8) {
			deriveInitialGuessByQuadraticFunction(points, cone);
//			UnknownParameter A = cone.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT);
//			UnknownParameter C = cone.getUnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT);
//			
//			UnknownParameter R11 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R11);
//			UnknownParameter R12 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R12);
//			UnknownParameter R13 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R13);
//			
//			UnknownParameter R21 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R21);
//			UnknownParameter R22 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R22);
//			UnknownParameter R23 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R23);
//			
//			UnknownParameter R31 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R31);
//			UnknownParameter R32 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R32);
//			UnknownParameter R33 = cone.getUnknownParameter(ParameterType.ROTATION_COMPONENT_R33);
//			
//			double b = 0.5 * (A.getValue0() + C.getValue0());
//			
//			double rx = Math.atan2( R32.getValue0(), R33.getValue0());
//			double ry = Math.atan2(-R31.getValue0(), Math.hypot(R32.getValue0(), R33.getValue0()));
//						
//			// derive rotation sequence without rz, i.e., rz = 0 --> R = Ry*Rx
//			double r11 = Math.cos(ry);
//			double r12 = Math.sin(rx)*Math.sin(ry);
//			double r13 = Math.cos(rx)*Math.sin(ry);
//			
//			double r21 = 0.0;
//			double r22 = Math.cos(rx);
//			double r23 =-Math.sin(rx);
//			
//			double r31 =-Math.sin(ry);
//			double r32 = Math.sin(rx)*Math.cos(ry);
//			double r33 = Math.cos(rx)*Math.cos(ry);
//			
//			A.setValue0(b);
//			C.setValue0(b);
//			
//			R11.setValue0(r11);
//			R12.setValue0(r12);
//			R13.setValue0(r13);
//			
//			R21.setValue0(r21);
//			R22.setValue0(r22);
//			R23.setValue0(r23);
//			
//			R31.setValue0(r31);
//			R32.setValue0(r32);
//			R33.setValue0(r33);
		}
		else 
			deriveInitialGuessByCircle(points,  cone);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.cone.getFeaturePoints(), this.cone);
	}
	
	private static void deriveInitialGuessByQuadraticFunction(Collection<FeaturePoint> points, Cone cone) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Sphere sphere = new Sphere();
		SphereFeature.deriveInitialGuess(points, sphere);
		
		double xS = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0();
		double yS = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0();
		double zS = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Z).getValue0();
		
		final int dim = 3;
		final double INV_SQRT2 = Math.sqrt(2.0);
		
		QuadraticSurface quadraticSurface = new QuadraticSurface();
		QuadraticSurfaceFeature.deriveInitialGuess(points, quadraticSurface);
		
		double a = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue0();
		double b = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue0();
		double c = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue0();
		double d = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_D).getValue0();
		double e = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_E).getValue0();
		double f = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_F).getValue0();
		double g = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_G).getValue0();
		double h = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_H).getValue0();
		double i = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_I).getValue0();
		
		DenseVector u = new DenseVector(new double[] {g, h, i}, false);
		UpperSymmPackMatrix H = new UpperSymmPackMatrix(dim);
		H.set(0, 0, a);
		H.set(0, 1, d / INV_SQRT2);
		H.set(0, 2, e / INV_SQRT2);
		
		H.set(1, 1, b);
		H.set(1, 2, f / INV_SQRT2);
		
		H.set(2, 2, c);
		
		UpperSymmPackMatrix U = new UpperSymmPackMatrix(H);
		
		// estimate shift --> stored in u (U will be overwritten)
		MathExtension.solve(U, u, false);
		double x0 = -0.5 * u.get(0);
		double y0 = -0.5 * u.get(1);
		double z0 = -0.5 * u.get(2);
		
		
		// solving eigen-system
		SymmPackEVD evd = new SymmPackEVD(dim, true, true);
		evd.factor(H);

		Matrix evec = evd.getEigenvectors();
		double eval[] = evd.getEigenvalues();
		
		// estimate principle axis via sphere center
		double wx = xS - x0;
		double wy = yS - y0;
		double wz = zS - z0;
		double normw = Math.sqrt(wx * wx + wy * wy + wz * wz);
		wx /= normw;
		wy /= normw;
		wz /= normw;
		
		DenseMatrix w = new DenseMatrix(1, 3, new double[] {wx, wy, wz}, true);
		SVD svd = SVD.factorize(w);
		Matrix V = svd.getVt();
		
		double ux = V.get(0, 1);
		double uy = V.get(1, 1);
		double uz = V.get(2, 1);
		
		double vx = V.get(0, 2);
		double vy = V.get(1, 2);
		double vz = V.get(2, 2);
		
		double det = ux*vy*wz + vx*wy*uz + wx*uy*vz - wx*vy*uz - vx*uy*wz - ux*wy*vz;
		if (det < 0) {
			ux = -ux;
			uy = -uy;
			uz = -uz;
		}
	
		double rx = Math.atan2( wy, wz);
		double ry = Math.atan2(-wx, Math.hypot(wy, wz));

		// derive rotation sequence without rz, i.e., rz = 0 --> R = Ry*Rx
		ux = Math.cos(ry);
		uy = Math.sin(rx)*Math.sin(ry);
		uz = Math.cos(rx)*Math.sin(ry);

		vx = 0.0;
		vy = Math.cos(rx);
		vz =-Math.sin(rx);

		wx =-Math.sin(ry);
		wy = Math.sin(rx)*Math.cos(ry);
		wz = Math.cos(rx)*Math.cos(ry);
		
		// lambda1 > 0, lambda2 > 0 and lamda3 < 0
		// evaluate signum of eigenvalues --> main axis of cone corresponds to lamda3
		int order[] = new int[]{0,1,2};
		if (Math.signum(eval[0]) == Math.signum(eval[1])) {
			a = Math.sqrt(Math.abs(eval[0]/eval[2]));
			c = Math.sqrt(Math.abs(eval[1]/eval[2]));
			if (a > c)
				order = new int[]{0,1,2};
			else {
				order = new int[]{1,0,2};
				double tmp = a;
				a = c;
				c = tmp;
			}
		}
		else if (Math.signum(eval[0]) == Math.signum(eval[2])) {
			a = Math.sqrt(Math.abs(eval[0]/eval[1]));
			c = Math.sqrt(Math.abs(eval[2]/eval[1]));
			if (a > c)
				order = new int[]{0,2,1};
			else {
				order = new int[]{2,0,1};
				double tmp = a;
				a = c;
				c = tmp;
			}
		}
		else { //if (Math.signum(eigVal[1]) == Math.signum(eigVal[2])) 
			a = Math.sqrt(Math.abs(eval[1]/eval[0]));
			c = Math.sqrt(Math.abs(eval[2]/eval[0]));
			if (a > c)
				order = new int[]{1,2,0};
			else {
				order = new int[]{2,1,0};
				double tmp = a;
				a = c;
				c = tmp;
			}
		}
		
		// interchange eigen-vectors depending on the order of the eigen-values 
		Matrix rotation = new DenseMatrix(dim, dim);
		for (int row = 0; row < dim; row++) {
			for (int column = 0; column < dim; column++) {
				int idx = order[column];
				rotation.set(row, column, evec.get(row, idx));
			}
		}
		
		// transpose of rotation matrix
		double r11 = rotation.get(0, 0);
		double r12 = rotation.get(1, 0);
		double r13 = rotation.get(2, 0);

		double r21 = rotation.get(0, 1);
		double r22 = rotation.get(1, 1);
		double r23 = rotation.get(2, 1);

		double r31 = rotation.get(0, 2);
		double r32 = rotation.get(1, 2);
		double r33 = rotation.get(2, 2);
		
		rx = Math.atan2( r32, r33);
		ry = Math.atan2(-r31, Math.hypot(r32, r33));
					
		// derive rotation sequence without rz, i.e., rz = 0 --> R = Ry*Rx
		r11 = Math.cos(ry);
		r12 = Math.sin(rx)*Math.sin(ry);
		r13 = Math.cos(rx)*Math.sin(ry);
		
		r21 = 0.0;
		r22 = Math.cos(rx);
		r23 =-Math.sin(rx);
		
		r31 =-Math.sin(ry);
		r32 = Math.sin(rx)*Math.cos(ry);
		r33 = Math.cos(rx)*Math.cos(ry);
		
		b = 0.5 * (a + c);

		// circular cone was not detected, use sphere approach
		if (Math.abs(Math.abs(a) - Math.abs(b)) > Math.pow(Constant.EPS, 1.0/3.0))
			cone.setInitialGuess(x0, y0, z0, b, b, ux, uy, uz, vx, vy, vz, wx, wy, wz);
		else
			cone.setInitialGuess(x0, y0, z0, b, b, r11, r12, r13, r21, r22, r23, r31, r32, r33);
	}
	
	private static void deriveInitialGuessByCircle(Collection<FeaturePoint> points, Cone cone) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Sphere sphere = new Sphere();
		Plane plane = new Plane();
		SpatialCircleFeature.deriveInitialGuess(points, sphere, plane);
		
		double x0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0();
		double y0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0();
		double z0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Z).getValue0();

		double wx = plane.getUnknownParameter(ParameterType.VECTOR_X).getValue0();
		double wy = plane.getUnknownParameter(ParameterType.VECTOR_Y).getValue0();
		double wz = plane.getUnknownParameter(ParameterType.VECTOR_Z).getValue0();
		
		DenseMatrix w = new DenseMatrix(1, 3, new double[] {wx, wy, wz}, true);
		SVD svd = SVD.factorize(w);
		Matrix V = svd.getVt();
		
		double ux = V.get(0, 1);
		double uy = V.get(1, 1);
		double uz = V.get(2, 1);
		
		double vx = V.get(0, 2);
		double vy = V.get(1, 2);
		double vz = V.get(2, 2);
		
		double det = ux*vy*wz + vx*wy*uz + wx*uy*vz - wx*vy*uz - vx*uy*wz - ux*wy*vz;
		if (det < 0) {
			wx = -wx;
			wy = -wy;
			wz = -wz;
		}
	
		double rx = Math.atan2( wy, wz);
		double ry = Math.atan2(-wx, Math.hypot(wy, wz));

		// derive rotation sequence without rz, i.e., rz = 0 --> R = Ry*Rx
		ux = Math.cos(ry);
		uy = Math.sin(rx)*Math.sin(ry);
		uz = Math.cos(rx)*Math.sin(ry);

		vx = 0.0;
		vy = Math.cos(rx);
		vz =-Math.sin(rx);

		wx =-Math.sin(ry);
		wy = Math.sin(rx)*Math.cos(ry);
		wz = Math.cos(rx)*Math.cos(ry);
		
		Quaternion q = FeatureUtil.getQuaternionHz(new double[] {wx, wy, wz});
		Collection<FeaturePoint> rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {0, 0, 0}, q);
		Quaternion rotatedApexQ = q.rotate(new double[] { x0, y0, z0} );
		double rx0 = rotatedApexQ.getQ1();
		double ry0 = rotatedApexQ.getQ2();
		double rz0 = rotatedApexQ.getQ3();
		double r = 0;
		int nop = 0;
		for (FeaturePoint point : rotatedPoints) {
			if (!point.isEnable())
				continue;
			
			nop++;
			
			double rxi = point.getX0();
			double ryi = point.getY0();
			double rzi = point.getZ0();
			
			double dx = rxi - rx0;
			double dy = ryi - ry0;
			double dz = rzi - rz0;
			
			r += Math.abs(dz*dz / (dx*dx + dy*dy));
		}
		r = Math.sqrt(r/nop);
		cone.setInitialGuess(x0, y0, z0, r, r, ux, uy, uz, vx, vy, vz, wx, wy, wz);
	}
}