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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Ellipsoid;
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;

import javafx.util.Pair;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class EllipsoidFeature extends SurfaceFeature {
	private final static double SQRT2  = Math.sqrt(2.0);
	private final static double SQRT05 = Math.sqrt(0.5);
	private final Ellipsoid ellipsoid;
	
	public EllipsoidFeature() {
		super(true);
		
		this.ellipsoid = new Ellipsoid();
		this.add(this.ellipsoid);
	}

	public Ellipsoid getEllipsoid() {
		return this.ellipsoid;
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, EllipsoidFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.ellipsoid);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Ellipsoid ellipsoid) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Pair<Double, Ellipsoid> approximation1 = deriveInitialGuessViaPanouBalodimouOverLeastSquares(points);
		Pair<Double, Ellipsoid> approximation2 = deriveInitialGuessViaPanouBalodimouOverEig(points);
		Pair<Double, Ellipsoid> approximation3 = deriveInitialGuessViaLiGriffiths(points);

		Pair<Double, Ellipsoid> bestApproximation;
		if (approximation1.getKey() <= approximation2.getKey() && approximation1.getKey() <= approximation3.getKey())
			bestApproximation = approximation1;
		else if (approximation2.getKey() <= approximation1.getKey() && approximation2.getKey() <= approximation3.getKey())
			bestApproximation = approximation2;
		else 
			bestApproximation = approximation3;
		
		Ellipsoid bestInitialGuessEllipsoid = bestApproximation.getValue();
		Collection<UnknownParameter> parameters = ellipsoid.getUnknownParameters();
		for (UnknownParameter parameter : parameters) 
			parameter.setValue0(bestInitialGuessEllipsoid.getUnknownParameter(parameter.getParameterType()).getValue0());
	}
	
	private static Pair<Double, Ellipsoid> deriveInitialGuessViaPanouBalodimouOverEig(Collection<FeaturePoint> points) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Ellipsoid ellipsoid = new Ellipsoid();
		
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
		
		a = -a/length;
		b = -b/length;
		c = -c/length;
		
		d = -d*SQRT2/length;
		e = -e*SQRT2/length;
		f = -f*SQRT2/length;
		
		g = -g/length;
		h = -h/length;
		i = -i/length;
		
		double[] ellispoidParams = deriveInitialGuessViaPanouBalodimou(new double[] {a, b, c, d, e, f, g, h, i});
		
		// Shift
		double x0 = ellispoidParams[0];
		double y0 = ellispoidParams[1];
		double z0 = ellispoidParams[2];
		
		// semi-axes
		double axis1 = ellispoidParams[3];
		double axis2 = ellispoidParams[4];
		double axis3 = ellispoidParams[5];

		// rotation matrix
		double r11 = ellispoidParams[6];
		double r12 = ellispoidParams[7];
		double r13 = ellispoidParams[8];

		double r21 = ellispoidParams[9];
		double r22 = ellispoidParams[10];
		double r23 = ellispoidParams[11];

		double r31 = ellispoidParams[12];
		double r32 = ellispoidParams[13];
		double r33 = ellispoidParams[14];
		
		ellipsoid.setInitialGuess(x0, y0, z0, axis1, axis2, axis3, r11, r12, r13, r21, r22, r23, r31, r32, r33);
		
		Collection<UnknownParameter> parameters = ellipsoid.getUnknownParameters();
		for (UnknownParameter parameter : parameters)
			parameter.setValue(parameter.getValue0());
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double res = ellipsoid.getMisclosure(point);
			epsilon += res * res;
		}
		
		return new Pair<Double, Ellipsoid>(epsilon, ellipsoid);
	}
		
	private static Pair<Double, Ellipsoid> deriveInitialGuessViaPanouBalodimouOverLeastSquares(Collection<FeaturePoint> points) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Ellipsoid ellipsoid = new Ellipsoid();

		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (ellipsoid.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + ellipsoid.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 9)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 9 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		UpperSymmPackMatrix N = new UpperSymmPackMatrix(9);
		DenseVector u = new DenseVector(9);
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;
			double zi = point.getZ0() - z0;
			
			double xx = xi * xi;
			double yy = yi * yi;
			double zz = zi * zi;
			
			double xy = xi * yi;
			double xz = xi * zi;
			double yz = yi * zi;
			
			N.add(0, 0, xx * xx);
			N.add(0, 1, xx * yy);
			N.add(0, 2, xx * zz);
			N.add(0, 3, xx * xy);
			N.add(0, 4, xx * xz);
			N.add(0, 5, xx * yz);
			N.add(0, 6, xx * xi);
			N.add(0, 7, xx * yi);
			N.add(0, 8, xx * zi);

			N.add(1, 1, yy * yy);
			N.add(1, 2, yy * zz);
			N.add(1, 3, yy * xy);
			N.add(1, 4, yy * xz);
			N.add(1, 5, yy * yz);
			N.add(1, 6, yy * xi);
			N.add(1, 7, yy * yi);
			N.add(1, 8, yy * zi);
			
			N.add(2, 2, zz * zz);
			N.add(2, 3, zz * xy);
			N.add(2, 4, zz * xz);
			N.add(2, 5, zz * yz);
			N.add(2, 6, zz * xi);
			N.add(2, 7, zz * yi);
			N.add(2, 8, zz * zi);
			
			N.add(3, 3, xy * xy);
			N.add(3, 4, xy * xz);
			N.add(3, 5, xy * yz);
			N.add(3, 6, xy * xi);
			N.add(3, 7, xy * yi);
			N.add(3, 8, xy * zi);
			
			N.add(4, 4, xz * xz);
			N.add(4, 5, xz * yz);
			N.add(4, 6, xz * xi);
			N.add(4, 7, xz * yi);
			N.add(4, 8, xz * zi);
			
			N.add(5, 5, yz * yz);
			N.add(5, 6, yz * xi);
			N.add(5, 7, yz * yi);
			N.add(5, 8, yz * zi);
			
			N.add(6, 6, xi * xi);
			N.add(6, 7, xi * yi);
			N.add(6, 8, xi * zi);
			
			N.add(7, 7, yi * yi);
			N.add(7, 8, yi * zi);
			
			N.add(8, 8, zi * zi);
			
			u.add(0, xx);
			u.add(1, yy);
			u.add(2, zz);
			u.add(3, xy);
			u.add(4, xz);
			u.add(5, yz);
			u.add(6, xi);
			u.add(7, yi);
			u.add(8, zi);
		}

		MathExtension.solve(N, u, false);
		
		double[] ellispoidParams = deriveInitialGuessViaPanouBalodimou(u.getData());
		
		// Shift
		double dx = ellispoidParams[0];
		double dy = ellispoidParams[1];
		double dz = ellispoidParams[2];
		
		// semi-axes
		double axis1 = ellispoidParams[3];
		double axis2 = ellispoidParams[4];
		double axis3 = ellispoidParams[5];

		// rotation matrix
		double r11 = ellispoidParams[6];
		double r12 = ellispoidParams[7];
		double r13 = ellispoidParams[8];

		double r21 = ellispoidParams[9];
		double r22 = ellispoidParams[10];
		double r23 = ellispoidParams[11];

		double r31 = ellispoidParams[12];
		double r32 = ellispoidParams[13];
		double r33 = ellispoidParams[14];

		x0 = x0 + dx;
		y0 = y0 + dy;
		z0 = z0 + dz;

		ellipsoid.setInitialGuess(x0, y0, z0, axis1, axis2, axis3, r11, r12, r13, r21, r22, r23, r31, r32, r33);
		
		Collection<UnknownParameter> parameters = ellipsoid.getUnknownParameters();
		for (UnknownParameter parameter : parameters)
			parameter.setValue(parameter.getValue0());
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double res = ellipsoid.getMisclosure(point);
			epsilon += res * res;
		}
		
		return new Pair<Double, Ellipsoid>(epsilon, ellipsoid);
	}
	
	private static double[] deriveInitialGuessViaPanouBalodimou(double params[]) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		double cxx = params[0];
		double cyy = params[1];
		double czz = params[2];
		double cxy = params[3];
		double cxz = params[4];
		double cyz = params[5];
		double cx  = params[6];
		double cy  = params[7];
		double cz  = params[8];
		
		/* X,Y,Z - Shift */
		// Panou and Balodimou (2021, Eq. 74)
		double f1 = 4.0*cyy*czz - cyz*cyz;
		double f2 = cxz*cyz - 2.0*cxy*czz;
		double f3 = cxy*cyz - 2.0*cxz*cyy;
		
		// Panou and Balodimou (2021, Eq. 75)
		double g2 = 4.0*cxx*czz - cxz*cxz;
		double g3 = cxy*cxz - 2.0*cxx*cyz;
		double h3 = 4.0*cxx*cyy - cxy*cxy;

		// Panou and Balodimou (2021, Eq. 76)
		double e = 2.0*cxx*f1 + cxy*f2 +cxz*f3;
		
		// Panou and Balodimou (2021, Eqs. 71-73)
		double x0 = -(f1*cx + f2*cy + f3*cz)/e;
		double y0 = -(f2*cx + g2*cy + g3*cz)/e;
		double z0 = -(f3*cx + g3*cy + h3*cz)/e;

		// Panou and Balodimou (2021, Eq. 69)
		double d = 1.0 + cxx*x0*x0 + cyy*y0*y0 + czz*z0*z0 + cxy*x0*y0 + cxz*x0*z0 + cyz*y0*z0;

		// Panou and Balodimou (2021, Eq. 91)
		UpperSymmPackMatrix Q = new UpperSymmPackMatrix(3);
		Q.set(0, 0, 2.0*d*f1/e);
		Q.set(0, 1, 2.0*d*f2/e);
		Q.set(0, 2, 2.0*d*f3/e);

		Q.set(1, 1, 2.0*d*g2/e);
		Q.set(1, 2, 2.0*d*g3/e);

		Q.set(2, 2, 2.0*d*h3/e);

		SymmPackEVD evd = new SymmPackEVD(3, true, true);
		evd.factor(Q);

		Matrix evec   = evd.getEigenvectors();
		double eval[] = evd.getEigenvalues();

		// Semi-axes
		double axis1 = 1.0/Math.sqrt(Math.abs(eval[0]));
		double axis2 = 1.0/Math.sqrt(Math.abs(eval[1]));
		double axis3 = 1.0/Math.sqrt(Math.abs(eval[2]));

		// transpose of rotation matrix
		double r11 = evec.get(0, 0);
		double r12 = evec.get(1, 0);
		double r13 = evec.get(2, 0);

		double r21 = evec.get(0, 1);
		double r22 = evec.get(1, 1);
		double r23 = evec.get(2, 1);

		double r31 = evec.get(0, 2);
		double r32 = evec.get(1, 2);
		double r33 = evec.get(2, 2);

		// check det(R) = +1
		double det = r11*r22*r33 + r12*r23*r31 + r13*r21*r32 - r13*r22*r31 - r12*r21*r33 - r11*r23*r32;
		if (det < 0) {
			r31 = -r31;
			r32 = -r32;
			r33 = -r33;
		}
		
		return new double[] {
				x0, y0, z0,   axis1, axis2, axis3,   r11, r12, r13,  r21, r22, r23, r31, r32, r33
		};
	}
	
	private static Pair<Double, Ellipsoid> deriveInitialGuessViaLiGriffiths(Collection<FeaturePoint> points) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Ellipsoid ellipsoid = new Ellipsoid();

		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (ellipsoid.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + ellipsoid.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 9)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 9 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		UpperSymmPackMatrix A1TA1 = new UpperSymmPackMatrix(6);
		UpperSymmPackMatrix A2TA2 = new UpperSymmPackMatrix(4);
		DenseMatrix A1TA2 = new DenseMatrix(6,4);
		
		final double k = 4;	
		UpperSymmPackMatrix invC = new UpperSymmPackMatrix(6);
		invC.set(0, 0,  (k - 4.0)/(-k*k + 3.0*k));
		invC.set(0, 1, -(k - 2.0)/(-k*k + 3.0*k));
		invC.set(0, 2, -(k - 2.0)/(-k*k + 3.0*k));
		
		invC.set(1, 1,  (k - 4.0)/(-k*k + 3.0*k)); 
		invC.set(1, 2, -(k - 2.0)/(-k*k + 3.0*k));
		
		invC.set(2, 2,  (k - 4.0)/(-k*k + 3.0*k)); 
		
		invC.set(3, 3, -2.0/k);
		invC.set(4, 4, -2.0/k);
		invC.set(5, 5, -2.0/k);

		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;
			double zi = point.getZ0() - z0;
			
			double xx = xi * xi;
			double yy = yi * yi;
			double zz = zi * zi;
			
			double xy = xi * yi;
			double xz = xi * zi;
			double yz = yi * zi;
			
			A1TA1.add(0,0, xx * xx);
			A1TA1.add(0,1, xx * yy);
			A1TA1.add(0,2, xx * zz);
			A1TA1.add(0,3, xx * SQRT2 * xy);
			A1TA1.add(0,4, xx * SQRT2 * xz);
			A1TA1.add(0,5, xx * SQRT2 * yz);

			A1TA1.add(1,1, yy * yy);
			A1TA1.add(1,2, yy * zz);
			A1TA1.add(1,3, yy * SQRT2 * xy);
			A1TA1.add(1,4, yy * SQRT2 * xz);
			A1TA1.add(1,5, yy * SQRT2 * yz);
			
			A1TA1.add(2,2, zz * zz);
			A1TA1.add(2,3, zz * SQRT2 * xy);
			A1TA1.add(2,4, zz * SQRT2 * xz);
			A1TA1.add(2,5, zz * SQRT2 * yz);
			
			A1TA1.add(3,3, 2.0 * xy * xy);
			A1TA1.add(3,4, 2.0 * xy * xz);
			A1TA1.add(3,5, 2.0 * xy * yz);
			
			A1TA1.add(4,4, 2.0 * xz * xz);
			A1TA1.add(4,5, 2.0 * xz * yz);
			
			A1TA1.add(5,5, 2.0 * yz * yz);
			
			
			A2TA2.add(0,0, xx);
			A2TA2.add(0,1, xy);
			A2TA2.add(0,2, xz);
			A2TA2.add(0,3, xi);
			
			A2TA2.add(1,1, yy);
			A2TA2.add(1,2, yz);
			A2TA2.add(1,3, yi);
			
			A2TA2.add(2,2, zz);
			A2TA2.add(2,3, zi);
			
			A2TA2.add(3,3, 1.0);
			
			
			A1TA2.add(0,0, xx * xi);
			A1TA2.add(0,1, xx * yi);
			A1TA2.add(0,2, xx * zi);
			A1TA2.add(0,3, xx);
			
			A1TA2.add(1,0, yy * xi);
			A1TA2.add(1,1, yy * yi);
			A1TA2.add(1,2, yy * zi);
			A1TA2.add(1,3, yy);
			
			A1TA2.add(2,0, zz * xi);
			A1TA2.add(2,1, zz * yi);
			A1TA2.add(2,2, zz * zi);
			A1TA2.add(2,3, zz);
			
			A1TA2.add(3,0, SQRT2 * xy * xi);
			A1TA2.add(3,1, SQRT2 * xy * yi);
			A1TA2.add(3,2, SQRT2 * xy * zi);
			A1TA2.add(3,3, SQRT2 * xy);
			
			A1TA2.add(4,0, SQRT2 * xz * xi);
			A1TA2.add(4,1, SQRT2 * xz * yi);
			A1TA2.add(4,2, SQRT2 * xz * zi);
			A1TA2.add(4,3, SQRT2 * xz);
			
			A1TA2.add(5,0, SQRT2 * yz * xi);
			A1TA2.add(5,1, SQRT2 * yz * yi);
			A1TA2.add(5,2, SQRT2 * yz * zi);
			A1TA2.add(5,3, SQRT2 * yz);
		}
		
		// Li and Griffiths (2004): Least Squares Ellipsoid Specific Fitting
		// Eq. 15
		Matrix invA2TA2 = MathExtension.pinv(A2TA2);
		Matrix invA2TA2timesA1TA2 = new DenseMatrix(4,6);
		invA2TA2.transBmult(A1TA2, invA2TA2timesA1TA2);
		invA2TA2 = null;
		
		UpperSymmPackMatrix A1TA2invA2TA2timesA1TA2 = new UpperSymmPackMatrix(6);
		A1TA2.mult(invA2TA2timesA1TA2, A1TA2invA2TA2timesA1TA2); // A12*inv(A22)-1*A12'
		A1TA1.add(-1.0, A1TA2invA2TA2timesA1TA2); // A11 - A12*inv(A22)-1*A12'
		A1TA2 = null;
		A1TA2invA2TA2timesA1TA2 = null;
		
		DenseMatrix invCtimesA = new DenseMatrix(6,6);
		invC.mult(A1TA1, invCtimesA); // inv(C) * (A11 - A12*inv(A22)-1*A12')
		A1TA1 = null;
		
		// solving eigen-system
		EVD evd = new EVD(6, false, true);
		evd.factor(invCtimesA);
		invCtimesA = null;

		Matrix evec = evd.getRightEigenvectors();
		double eval[] = evd.getRealEigenvalues();
		evd = null;
		
		int indexLargestPosEval = 0;
		double maxEval = eval[indexLargestPosEval];
		for (int idx = indexLargestPosEval + 1; idx < eval.length; idx++) { 
			if (eval[idx] >= maxEval) {
				maxEval = eval[idx];
				indexLargestPosEval = idx;
			}
		}
		
		DenseVector u1 = new DenseVector(6);
		DenseVector u2 = new DenseVector(4);
		
		for (int i = 0; i < 6; i++) {
			double x = evec.get(i, indexLargestPosEval);
			u1.set(i, x);
		}
		
		invA2TA2timesA1TA2.mult(-1.0, u1, u2);
		
		double a = u1.get(0); // xx
		double b = u1.get(1); // yy
		double c = u1.get(2); // zz
		
		double d = u1.get(3); // xy
		double e = u1.get(4); // xz
		double f = u1.get(5); // yz
				
		double g = u2.get(0); // x
		double h = u2.get(1); // y
		double i = u2.get(2); // z
		
		double length = u2.get(3);

		// estimate shift
		DenseVector dT = new DenseVector(new double[] {g, h, i}, false);

		UpperSymmPackMatrix H = new UpperSymmPackMatrix(dT.size());
		H.set(0, 0, a);
		H.set(0, 1, d/SQRT2);
		H.set(0, 2, e/SQRT2);
		
		H.set(1, 1, b);
		H.set(1, 2, f/SQRT2);
		
		H.set(2, 2, c);
		
		// estimated shift --> stored in u (H will be overwritten)
		MathExtension.solve(H, dT, false);
		
		double dx = -0.5 * dT.get(0);
		double dy = -0.5 * dT.get(1);
		double dz = -0.5 * dT.get(2);

		length = a*dx*dx + b*dy*dy + c*dz*dz + SQRT2*(d*dx*dy + e*dx*dz + f*dy*dz) - length;
		
		// Newman & Yi (2000) Compound extraction and fitting method for detecting cardiac ventricle in SPECT data
		// solving eigen-system
		H = new UpperSymmPackMatrix(dT.size());
		H.set(0, 0, a);
		H.set(0, 1, d*SQRT05);
		H.set(0, 2, e*SQRT05);
		
		H.set(1, 1, b);
		H.set(1, 2, f*SQRT05);
		
		H.set(2, 2, c);
		SymmPackEVD sevd = new SymmPackEVD(3, true, true);
		sevd.factor(H);

		evec = sevd.getEigenvectors();
		eval = sevd.getEigenvalues();
		
		x0 = x0 + dx;
		y0 = y0 + dy;
		z0 = z0 + dz;
			
		// Semi-axes
		double axis1 = Math.sqrt(Math.abs(eval[0]/length));
		double axis2 = Math.sqrt(Math.abs(eval[1]/length));
		double axis3 = Math.sqrt(Math.abs(eval[2]/length));

		// transpose of rotation matrix
		double r11 = evec.get(0, 0);
		double r12 = evec.get(1, 0);
		double r13 = evec.get(2, 0);

		double r21 = evec.get(0, 1);
		double r22 = evec.get(1, 1);
		double r23 = evec.get(2, 1);

		double r31 = evec.get(0, 2);
		double r32 = evec.get(1, 2);
		double r33 = evec.get(2, 2);

		// check det(R) = +1
		double det = r11*r22*r33 + r12*r23*r31 + r13*r21*r32 - r13*r22*r31 - r12*r21*r33 - r11*r23*r32;
		if (det < 0) {
			r31 = -r31;
			r32 = -r32;
			r33 = -r33;
		}		
		ellipsoid.setInitialGuess(x0, y0, z0, axis1, axis2, axis3, r11, r12, r13, r21, r22, r23, r31, r32, r33);
		
		Collection<UnknownParameter> parameters = ellipsoid.getUnknownParameters();
		for (UnknownParameter parameter : parameters)
			parameter.setValue(parameter.getValue0());
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double res = ellipsoid.getMisclosure(point);
			epsilon += res * res;
		}
		
		return new Pair<Double, Ellipsoid>(epsilon, ellipsoid);
	}

	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.ellipsoid.getFeaturePoints(), this.ellipsoid);
	}
}
