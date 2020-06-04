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

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cylinder;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Paraboloid;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class ParaboloidFeature extends SurfaceFeature {
	
	private final Paraboloid paraboloid;

	public ParaboloidFeature() {
		super(true);
		
		this.paraboloid = new Paraboloid();
		
		this.add(this.paraboloid);
	}
	
	public Paraboloid getParaboloid() {
		return this.paraboloid;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, ParaboloidFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.paraboloid);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Paraboloid paraboloid) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			if (paraboloid.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + paraboloid.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 8)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 8 points are needed.");
		
		// derive initial guess
		if (nop > 8) 
			deriveInitialGuessByQuadraticFunction(points, paraboloid);
		else 
			deriveInitialGuessByEllipse(points, paraboloid);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.paraboloid.getFeaturePoints(), this.paraboloid);
	}
	
	private static void deriveInitialGuessByEllipse(Collection<FeaturePoint> points, Paraboloid paraboloid) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Cylinder cylinder = new Cylinder();
		Plane plane = new Plane();
		Sphere sphere = new Sphere();
		SphereFeature.deriveInitialGuess(points, sphere);
		SpatialEllipseFeature.deriveInitialGuess(points, cylinder, plane);
		
		double x0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0();
		double y0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0();
		double z0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Z).getValue0();
		double r  = sphere.getUnknownParameter(ParameterType.RADIUS).getValue0();
		
		double x1 = cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X).getValue0();
		double y1 = cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).getValue0();
		double z1 = cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Z).getValue0();
		
		double x2 = cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X).getValue0();
		double y2 = cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).getValue0();
		double z2 = cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Z).getValue0();
		
		double ac = cylinder.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue0();

		double wx = plane.getUnknownParameter(ParameterType.VECTOR_X).getValue0();
		double wy = plane.getUnknownParameter(ParameterType.VECTOR_Y).getValue0();
		double wz = plane.getUnknownParameter(ParameterType.VECTOR_Z).getValue0();
		
		double ux = x2 - x1;
		double uy = y2 - y1;
		double uz = z2 - z1;
		
		double bc = Math.sqrt(ac*ac - 0.25 * (ux*ux + uy*uy + uz*uz));
		
		double normU = Math.sqrt(ux * ux + uy * uy + uz * uz);
		ux /= normU;
		uy /= normU;
		uz /= normU;
		
		double vx = wy * uz - wz * uy;
		double vy = wz * ux - wx * uz;
		double vz = wx * uy - wy * ux;
		
		double normV = Math.sqrt(vx * vx + vy * vy + vz * vz);
		vx /= normV;
		vy /= normV;
		vz /= normV;
				
//		R = [ux vx wx
//		     uy vy wy
//		     uz vz wz]
		
		double det = ux*vy*wz + vx*wy*uz + wx*uy*vz - wx*vy*uz - vx*uy*wz - ux*wy*vz;
		if (det < 0) {
			wx = -wx;
			wy = -wy;
			wz = -wz;
		}
		
		x0 = x0 - wx*r;
		y0 = y0 - wy*r;
		z0 = z0 - wz*r;


		Quaternion q = FeatureUtil.getQuaternionHz(new double[] {wx, wy, wz});
		Collection<FeaturePoint> rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {0, 0, 0}, q);
		Quaternion rotatedApexQ = q.rotate(new double[] { x0, y0, z0} );
		double rx0 = rotatedApexQ.getQ1();
		double ry0 = rotatedApexQ.getQ2();
		double rz0 = rotatedApexQ.getQ3();
		double a = 0, b = 0;
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
			
			a += Math.abs(dz / (dx*dx + dy*dy));
		}
		a = b = Math.sqrt(a/nop);

		// use ratio ac:bc of cylinder to derive parameter a, b
		double avg = 0.5 * (ac + bc);
		
		a = ac * a / avg;
		b = bc * b / avg;
		
		paraboloid.setInitialGuess(x0, y0, z0, a, b, ux, uy, uz, vx, vy, vz, wx, wy, wz);
	}
	
	private static void deriveInitialGuessByQuadraticFunction(Collection<FeaturePoint> points, Paraboloid paraboloid) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		final int dim = 3;
		final double SQRT2 = Math.sqrt(2.0);
		
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
		double length = quadraticSurface.getUnknownParameter(ParameterType.LENGTH).getValue0();
		
		DenseVector u = new DenseVector(new double[] {g, h, i}, false);
		UpperSymmPackMatrix H = new UpperSymmPackMatrix(dim);
		H.set(0, 0, a);
		H.set(0, 1, d / SQRT2);
		H.set(0, 2, e / SQRT2);
		
		H.set(1, 1, b);
		H.set(1, 2, f / SQRT2);
		
		H.set(2, 2, c);
		
		UpperSymmPackMatrix U = new UpperSymmPackMatrix(H);
		// rank of U should be two
		Matrix invU = MathExtension.pinv(U, 2);

		// estimate shift (two component parts because rank(U) == 2)
		DenseVector invUu = new DenseVector(3);
		invU.mult(u, invUu);
		DenseVector shift = new DenseVector(invUu, true);
		shift.scale(-0.5);
		
		// solving eigen-system
		SymmPackEVD evd = new SymmPackEVD(dim, true, true);
		evd.factor(H);

		Matrix evec = evd.getEigenvectors();
		double eval[] = evd.getEigenvalues();
		
		// lambda1 != 0, lambda2 != 0 and lamda3 == 0
		// evaluate smallest eigenvalues --> main axis of paraboloid
		int minEvalIdx = 0;
		double minEval = Double.MAX_VALUE;
		for (int idx = 0; idx < dim; idx++) {
			if (minEval > Math.abs(eval[idx])) {
				minEval = Math.abs(eval[idx]);
				minEvalIdx = idx;
			}
		}
		
		// rotation matrix
		double r11 = evec.get(0, 0);
		double r12 = evec.get(0, 1);
		double r13 = evec.get(0, 2);

		double r21 = evec.get(1, 0);
		double r22 = evec.get(1, 1);
		double r23 = evec.get(1, 2);

		double r31 = evec.get(2, 0);
		double r32 = evec.get(2, 1);
		double r33 = evec.get(2, 2);

		// check for det(R) = +1
		double det = r11*r22*r33 + r12*r23*r31 + r13*r21*r32 - r13*r22*r31 - r12*r21*r33 - r11*r23*r32;
		if (det < 0) {
			evec.set(minEvalIdx, 0, -evec.get(minEvalIdx, 0));
			evec.set(minEvalIdx, 1, -evec.get(minEvalIdx, 1));
			evec.set(minEvalIdx, 2, -evec.get(minEvalIdx, 2));
		}
		DenseVector minEvec = new DenseVector(new double[] {evec.get(0, minEvalIdx), evec.get(1, minEvalIdx), evec.get(2, minEvalIdx)}, false);

		DenseVector canonicalShift = new DenseVector(3);
		evec.transMult(shift, canonicalShift);

		double m = -u.dot(minEvec);
		m = Math.abs(m) > Math.sqrt(Constant.EPS) ? m : 1.0;
		
		d = -0.25 * u.dot(invUu) + length; 
		canonicalShift.set(minEvalIdx, d/m);

		evec.mult(canonicalShift, shift);

		int order[] = new int[]{0,1,2};
		if (minEvalIdx == 0) {
			if (Math.abs(eval[1]) > Math.abs(eval[2])) {
				order = new int[]{1,2,0};
				a = Math.sqrt(Math.abs(eval[1] / m));
				b = Math.sqrt(Math.abs(eval[2] / m));
			}
			else {
				order = new int[]{2,1,0};
				a = Math.sqrt(Math.abs(eval[2] / m));
				b = Math.sqrt(Math.abs(eval[1] / m));
			}
		}
		else if (minEvalIdx == 1) {
			if (Math.abs(eval[0]) > Math.abs(eval[2])) {
				order = new int[]{0,2,1};
				a = Math.sqrt(Math.abs(eval[0] / m));
				b = Math.sqrt(Math.abs(eval[2] / m));
			}
			else {
				order = new int[]{2,0,1};
				a = Math.sqrt(Math.abs(eval[2] / m));
				b = Math.sqrt(Math.abs(eval[0] / m));
			}
		}
		else { //if (minEvalIdx == 2) {
			if (Math.abs(eval[0]) > Math.abs(eval[1])) {
				order = new int[]{0,1,2};
				a = Math.sqrt(Math.abs(eval[0] / m));
				b = Math.sqrt(Math.abs(eval[1] / m));
			}
			else {
				order = new int[]{1,0,2};
				a = Math.sqrt(Math.abs(eval[1] / m));
				b = Math.sqrt(Math.abs(eval[0] / m));
			}
		}

		// interchange eigen-vectors depending on the order  
		Matrix rotation = new DenseMatrix(dim, dim);
		for (int row = 0; row < dim; row++) {
			for (int column = 0; column < dim; column++) {
				int idx = order[column];
				rotation.set(row, column, evec.get(row, idx));
			}
		}
		
		// transpose of rotation matrix
		r11 = rotation.get(0, 0);
		r12 = rotation.get(1, 0);
		r13 = rotation.get(2, 0);
		
		r21 = rotation.get(0, 1);
		r22 = rotation.get(1, 1);
		r23 = rotation.get(2, 1);
		
		r31 = rotation.get(0, 2);
		r32 = rotation.get(1, 2);
		r33 = rotation.get(2, 2);
		
		double x0 = shift.get(0);
		double y0 = shift.get(1);
		double z0 = shift.get(2);
		
		paraboloid.setInitialGuess(x0, y0, z0, a, b, r11, r12, r13, r21, r22, r23, r31, r32, r33);
	}
}
