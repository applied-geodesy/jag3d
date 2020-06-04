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
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class QuadraticSurfaceFeature extends SurfaceFeature {
	private final QuadraticSurface quadraticSurface;
	private final static double SQRT2 = Math.sqrt(2.0);
	
	public QuadraticSurfaceFeature() {
		super(true);
		
		this.quadraticSurface = new QuadraticSurface();
		
		UnknownParameter vectorLength = this.quadraticSurface.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		vectorLength.setVisible(false);
		
		this.add(this.quadraticSurface);
	}
	
	public QuadraticSurface getQuadraticSurface() {
		return this.quadraticSurface;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, QuadraticSurfaceFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.quadraticSurface);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, QuadraticSurface quadraticSurface) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (quadraticSurface.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + quadraticSurface.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 9)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 9 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		UpperSymmPackMatrix A1TA1 = new UpperSymmPackMatrix(6);
		UpperSymmPackMatrix A2TA2 = new UpperSymmPackMatrix(4);
		DenseMatrix A1TA2 = new DenseMatrix(6,4);
		
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
			
			A1TA1.add(4,4, 2.0 * xz* xz);
			A1TA1.add(4,5, 2.0 * xz* yz);
			
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
				
		// H = P - P * A2 * inv(A2' * A2) * A2' * P
		// T = A1' * H * A1
		// ignoring stochastic properties, i.e. P = I
		// T = A1' * A1  -  A1' * A2 * inv(A2' * A2) * A2' * A1
		MathExtension.inv(A2TA2);
		Matrix invA2TA2timesA1TA2 = new DenseMatrix(4,6);
		A2TA2.transBmult(A1TA2, invA2TA2timesA1TA2);
		
		UpperSymmPackMatrix A1TA2invA2TA2timesA1TA2 = new UpperSymmPackMatrix(6);
		A1TA2.mult(invA2TA2timesA1TA2, A1TA2invA2TA2timesA1TA2);
		A1TA1.add(-1.0, A1TA2invA2TA2timesA1TA2);
		
		
		// solving eigen-system
		SymmPackEVD evd = new SymmPackEVD(6, true, true);
		evd.factor(A1TA1);
		
		Matrix evec = evd.getEigenvectors();
		double eval[] = evd.getEigenvalues();
		
		int indexMinEval = 0;
		double minEval = Math.abs(eval[indexMinEval]);
		for (int idx = indexMinEval + 1; idx < eval.length; idx++) {
			if (minEval > Math.abs(eval[idx])) {
				minEval = Math.abs(eval[idx]);
				indexMinEval = idx;
			}
		}
		
		DenseVector u1 = new DenseVector(6);
		DenseVector u2 = new DenseVector(4);
		
		for (int i = 0; i < 6; i++) {
			double x = evec.get(i, indexMinEval);
			u1.set(i, x);
		}
		
		invA2TA2timesA1TA2.mult(-1.0, u1, u2);
		
		double a = u1.get(0);
		double b = u1.get(1);
		double c = u1.get(2);
		double d = u1.get(3);
		double e = u1.get(4);
		double f = u1.get(5);
		
		double g = u2.get(0);
		double h = u2.get(1);
		double i = u2.get(2);

		double length = u2.get(3);
		
		// remove center of mass
		g = g - 2.0 * (a * x0 + d / SQRT2 * y0 + e / SQRT2 * z0);
		h = h - 2.0 * (d / SQRT2 * x0 + b * y0 + f / SQRT2 * z0);
		i = i - 2.0 * (e / SQRT2 * x0 + f / SQRT2 * y0 + c * z0);
		length = length - (a * x0*x0 + b * y0*y0 + c * z0*z0 + d * SQRT2 * x0*y0 + e * SQRT2 * x0*z0 + f * SQRT2 * y0*z0 + g * x0 + h * y0 + i * z0);
		
		quadraticSurface.setInitialGuess(a, b, c, d, e, f, g, h, i, length);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.quadraticSurface.getFeaturePoints(), this.quadraticSurface);
	}
}
