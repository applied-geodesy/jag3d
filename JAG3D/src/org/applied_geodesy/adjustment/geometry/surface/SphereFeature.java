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
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class SphereFeature extends SurfaceFeature {
	private final Sphere sphere;
	
	public SphereFeature() {
		super(true);
		
		this.sphere = new Sphere();
		this.add(this.sphere);
	}

	public Sphere getSphere() {
		return this.sphere;
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, SphereFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.sphere);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Sphere sphere) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			
			if (sphere.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + sphere.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 4)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 4 points are needed.");

		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		UpperSymmPackMatrix N = new UpperSymmPackMatrix(4);
		DenseVector n = new DenseVector(4);

		double r0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;
			double zi = point.getZ0() - z0;
			
			double xxi = xi * xi;
			double yyi = yi * yi;
			double zzi = zi * zi;
			double xxyyzzi = xxi + yyi + zzi;
			
			N.set(0,0, N.get(0,0) + xxi);
			N.set(0,1, N.get(0,1) + xi * yi);
			N.set(0,2, N.get(0,2) + xi * zi);
			N.set(0,3, N.get(0,3) + xi);
			
			N.set(1,1, N.get(1,1) + yyi);
			N.set(1,2, N.get(1,2) + yi * zi);
			N.set(1,3, N.get(1,3) + yi);
			
			N.set(2,2, N.get(2,2) + zzi);
			N.set(2,3, N.get(2,3) + zi);
			
			N.set(3,3, N.get(3,3) + 1.0);
			
			n.set(0, n.get(0) + xi * xxyyzzi);
			n.set(1, n.get(1) + yi * xxyyzzi);
			n.set(2, n.get(2) + zi * xxyyzzi);
			n.set(3, n.get(3) +      xxyyzzi);

			r0 += Math.sqrt(xxyyzzi);
		}
		r0 /= nop;
		
		MathExtension.solve(N, n, false);
		r0 = Math.sqrt(Math.abs(0.25*(n.get(0)*n.get(0) + n.get(1)*n.get(1) + n.get(2)*n.get(2)) + n.get(3)));

		x0 = x0 + 0.5 * n.get(0);
		y0 = y0 + 0.5 * n.get(1); 
		z0 = z0 + 0.5 * n.get(2); 

		sphere.setInitialGuess(x0, y0, z0, r0);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.sphere.getFeaturePoints(), this.sphere);
	}
}
