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
import org.applied_geodesy.adjustment.geometry.curve.primitive.Circle;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class CircleFeature extends CurveFeature {
	private final Circle circle;
	
	public CircleFeature() {
		super(true);
		
		this.circle = new Circle();
		this.add(this.circle);
	}

	public Circle getCircle() {
		return this.circle;
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, CircleFeature feature) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.circle);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Circle circle) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			
			if (circle.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + circle.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 3)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 3 points are needed.");
		
		x0 /= nop;
		y0 /= nop;

		UpperSymmPackMatrix N = new UpperSymmPackMatrix(3);
		DenseVector n = new DenseVector(3);

		double r0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;

			double xxi = xi * xi;
			double yyi = yi * yi;
			double xxyyi = xxi + yyi;

			N.set(0,0, N.get(0,0) + xxi);
			N.set(0,1, N.get(0,1) + xi * yi);
			N.set(0,2, N.get(0,2) + xi);

			N.set(1,1, N.get(1,1) + yyi);
			N.set(1,2, N.get(1,2) + yi);

			N.set(2,2, N.get(2,2) + 1.0);

			n.set(0, n.get(0) + xi * xxyyi);
			n.set(1, n.get(1) + yi * xxyyi);
			n.set(2, n.get(2) +      xxyyi);

			r0 += Math.sqrt(xxyyi);
		}
		r0 /= nop;


		MathExtension.solve(N, n, false);
		r0 = Math.sqrt(Math.abs(0.25*(n.get(0)*n.get(0) + n.get(1)*n.get(1)) + n.get(2)));

		x0 = x0 + 0.5 * n.get(0);
		y0 = y0 + 0.5 * n.get(1); 
		
		circle.setInitialGuess(x0, y0, r0);
	}

	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.circle.getFeaturePoints(), this.circle);
	}
}
