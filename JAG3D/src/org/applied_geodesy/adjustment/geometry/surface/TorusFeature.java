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
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Torus;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class TorusFeature extends SurfaceFeature {

	private final Torus torus;
	
	public TorusFeature() {
		super(true);
		
		this.torus = new Torus();
		
		UnknownParameter vectorLength = this.torus.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		vectorLength.setVisible(false);
		
		this.add(this.torus);
	}
	
	public Torus getTorus() {
		return this.torus;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, TorusFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.torus);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Torus torus) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			if (torus.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + torus.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 7)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 7 points are needed.");
		
		Sphere sphere = new Sphere();
		Plane plane = new Plane();
		
		SpatialCircleFeature.deriveInitialGuess(points, sphere, plane);
		
		double x0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0();
		double y0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0();
		double z0 = sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Z).getValue0(); 
		double a  = sphere.getUnknownParameter(ParameterType.RADIUS).getValue0();
		
		double nx = plane.getUnknownParameter(ParameterType.VECTOR_X).getValue0(); 
		double ny = plane.getUnknownParameter(ParameterType.VECTOR_Y).getValue0();
		double nz = plane.getUnknownParameter(ParameterType.VECTOR_Z).getValue0();
		double d  = plane.getUnknownParameter(ParameterType.LENGTH).getValue0();
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double dx = point.getX0() - x0;
			double dy = point.getY0() - y0;
			double dz = point.getZ0() - z0;
			
			double resSphere = Math.sqrt(dx*dx + dy*dy + dz*dz);
			double resPlane  = Math.abs(point.getX0() * nx + point.getY0() * ny + point.getZ0() * nz - d);
			double res = Math.sqrt(Math.abs(resSphere*resSphere - resPlane*resPlane)) - a;
			
			epsilon += res * res;
		}

		double c = epsilon > Math.sqrt(Constant.EPS) ? Math.sqrt(epsilon / nop) : Math.sqrt(Constant.EPS);
		torus.setInitialGuess(x0, y0, z0, nx, ny, nz, a, c);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.torus.getFeaturePoints(), this.torus);
	}
}
