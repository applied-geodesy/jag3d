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

import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class PlaneFeature extends SurfaceFeature {
	private final Plane plane;
	
	public PlaneFeature() {
		super(true);
		
		this.plane = new Plane();
		
		UnknownParameter vectorLength = this.plane.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		vectorLength.setVisible(false);
		
		this.add(this.plane);
	}
	
	public Plane getPlane() {
		return this.plane;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, PlaneFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.plane);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Plane plane) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (plane.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + plane.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 3)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 3 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		UpperSymmPackMatrix H = new UpperSymmPackMatrix(3);
		
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;
			double zi = point.getZ0() - z0;
			
			for (int i = 0; i < 3; i++) {
				double hi = 0;
				
				if (i == 0)
					hi = xi; 
				else if (i == 1)
					hi = yi;
				else 
					hi = zi;
				
				for (int j = i; j < 3; j++) {
					double hj = 0;

					if (j == 0)
						hj = xi; 
					else if (j == 1)
						hj = yi;
					else 
						hj = zi;
					
					H.set(i, j, H.get(i,j) + hi * hj);
				}
			}
		}

		SymmPackEVD evd = new SymmPackEVD(3, true, true);
		evd.factor(H);
		
		Matrix eigVec = evd.getEigenvectors();
		double eigVal[] = evd.getEigenvalues();

		int indexMinEigVal = 0;
		double minEigVal = eigVal[indexMinEigVal];
		for (int i = indexMinEigVal + 1; i < eigVal.length; i++) {
			if (minEigVal > eigVal[i]) {
				minEigVal = eigVal[i];
				indexMinEigVal = i;
			}
		}

		// Normal vector n of the plane is eigenvector which corresponds to the smallest eigenvalue 
		double nx = eigVec.get(0, indexMinEigVal);
		double ny = eigVec.get(1, indexMinEigVal);
		double nz = eigVec.get(2, indexMinEigVal);
		double d  = nx * x0 + ny * y0 + nz * z0;

		// Change orientation so that d is a positive distance
		if (d < 0) {
			nx = -nx;
			ny = -ny;
			nz = -nz;
			d  = -d;
		}
		
		plane.setInitialGuess(nx, ny, nz, d);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.plane.getFeaturePoints(), this.plane);
	}
}
