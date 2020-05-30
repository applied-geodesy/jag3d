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

package org.applied_geodesy.adjustment.geometry.curve;

import java.util.Collection;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.CurveFeature;
import org.applied_geodesy.adjustment.geometry.curve.primitive.QuadraticCurve;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class QuadraticCurveFeature extends CurveFeature {
	private final QuadraticCurve quadraticCurve;
	private final static double SQRT2 = Math.sqrt(2.0);
	
	public QuadraticCurveFeature() {
		super(true);
		
		this.quadraticCurve = new QuadraticCurve();
		
		UnknownParameter vectorLength = this.quadraticCurve.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		vectorLength.setVisible(false);
		
		this.add(this.quadraticCurve);
	}
	
	public QuadraticCurve getQuadraticCurve() {
		return this.quadraticCurve;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, QuadraticCurveFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.quadraticCurve);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, QuadraticCurve quadraticCurve) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();

			if (quadraticCurve.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + quadraticCurve.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 5)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 5 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		
		UpperSymmPackMatrix A1TA1 = new UpperSymmPackMatrix(3);
		UpperSymmPackMatrix A2TA2 = new UpperSymmPackMatrix(3);
		DenseMatrix A1TA2 = new DenseMatrix(3,3);
		
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;

			double xx = xi * xi;
			double yy = yi * yi;
			
			double xy = xi * yi;
			
			A1TA1.add(0,0, xx * xx);
			A1TA1.add(0,1, xx * yy);
			A1TA1.add(0,2, xx * SQRT2 * xy);

			A1TA1.add(1,1, yy * yy);
			A1TA1.add(1,2, yy * SQRT2 * xy);
			
			A1TA1.add(2,2, 2.0 * xy * xy);
					
			
			A2TA2.add(0,0, xx);
			A2TA2.add(0,1, xy);
			A2TA2.add(0,2, xi);
			
			A2TA2.add(1,1, yy);
			A2TA2.add(1,2, yi);
			
			A2TA2.add(2,2, 1.0);
			
			
			A1TA2.add(0,0, xx * xi);
			A1TA2.add(0,1, xx * yi);
			A1TA2.add(0,2, xx);
			
			A1TA2.add(1,0, yy * xi);
			A1TA2.add(1,1, yy * yi);
			A1TA2.add(1,2, yy);
			
			A1TA2.add(2,0, SQRT2 * xy * xi);
			A1TA2.add(2,1, SQRT2 * xy * yi);
			A1TA2.add(2,2, SQRT2 * xy);
		}
				
		// H = P - P * A2 * inv(A2' * A2) * A2' * P
		// T = A1' * H * A1
		// ignoring stochastic properties, i.e. P = I
		// T = A1' * A1  -  A1' * A2 * inv(A2' * A2) * A2' * A1
		MathExtension.inv(A2TA2);
		Matrix invA2TA2timesA1TA2 = new DenseMatrix(3,3);
		A2TA2.transBmult(A1TA2, invA2TA2timesA1TA2);
		
		UpperSymmPackMatrix A1TA2invA2TA2timesA1TA2 = new UpperSymmPackMatrix(3);
		A1TA2.mult(invA2TA2timesA1TA2, A1TA2invA2TA2timesA1TA2);
		A1TA1.add(-1.0, A1TA2invA2TA2timesA1TA2);
		
		
		// solving eigen-system
		SymmPackEVD evd = new SymmPackEVD(3, true, true);
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
		
		DenseVector u1 = new DenseVector(3);
		DenseVector u2 = new DenseVector(3);
		
		for (int i = 0; i < 3; i++) {
			double x = evec.get(i, indexMinEval);
			u1.set(i, x);
		}
		
		invA2TA2timesA1TA2.mult(-1.0, u1, u2);
		
		double a = u1.get(0);
		double b = u1.get(1);
		double c = u1.get(2);
		
		double d = u2.get(0);
		double e = u2.get(1);

		double length = u2.get(1);
		
		// remove center of mass
		d = d - 2.0 * (a * x0 + c / SQRT2 * y0);
		e = e - 2.0 * (c / SQRT2 * x0 + b * y0);
		length = length - (a * x0*x0 + b * y0*y0 + c * SQRT2 * x0*y0 + d * x0 + e * y0);

		quadraticCurve.setInitialGuess(a, b, c, d, e, length);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.quadraticCurve.getFeaturePoints(), this.quadraticCurve);
	}
}
