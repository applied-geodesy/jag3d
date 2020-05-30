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
import java.util.HashSet;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.curve.CircleFeature;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Circle;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Sphere;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class SpatialCircleFeature extends SurfaceFeature {
	private final Sphere sphere;
	private final Plane plane;
	
	public SpatialCircleFeature() {
		super(true);
		
		this.sphere = new Sphere();
		this.plane  = new Plane();
		
		UnknownParameter nx = this.plane.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter ny = this.plane.getUnknownParameter(ParameterType.VECTOR_Y);
		UnknownParameter nz = this.plane.getUnknownParameter(ParameterType.VECTOR_Z);
		UnknownParameter d  = this.plane.getUnknownParameter(ParameterType.LENGTH);
		UnknownParameter n  = this.plane.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		UnknownParameter x0 = this.sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter y0 = this.sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter z0 = this.sphere.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Z);

		n.setVisible(false);
		
		List<UnknownParameter> normalVector = List.of(nx, ny, nz);
		List<UnknownParameter> centerPoint  = List.of(x0, y0, z0);
		
		ProductSumRestriction centerInPlaneRestriction = new ProductSumRestriction(true, normalVector, centerPoint, d);
		
		this.add(this.sphere);
		this.add(this.plane);
		
		this.getRestrictions().add(centerInPlaneRestriction);
	}
	
	public Plane getPlane() {
		return this.plane;
	}

	public Sphere getSphere() {
		return this.sphere;
	}
			
	public static void deriveInitialGuess(Collection<FeaturePoint> points, SpatialCircleFeature feature) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.sphere, feature.plane);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Sphere sphere, Plane plane) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		// estimate plane of the circle
		PlaneFeature.deriveInitialGuess(points, plane);
		double nx = plane.getUnknownParameter(ParameterType.VECTOR_X).getValue0(); 
		double ny = plane.getUnknownParameter(ParameterType.VECTOR_Y).getValue0();
		double nz = plane.getUnknownParameter(ParameterType.VECTOR_Z).getValue0();
		double d  = plane.getUnknownParameter(ParameterType.LENGTH).getValue0();

		Quaternion q = FeatureUtil.getQuaternionHz(new double[] {nx, ny, nz});
		Collection<FeaturePoint> points2D = FeatureUtil.getRotatedFeaturePoints(points, new double[] {0, 0, 0}, q);

		// estimate circle parameters
		Circle circle = new Circle();
		CircleFeature.deriveInitialGuess(points2D, circle);

		double x0 = circle.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0(); 
		double y0 = circle.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0(); 
		double r0 = circle.getUnknownParameter(ParameterType.RADIUS).getValue0(); 

		double horizontalCircleCenter[] = new double[] {x0, y0, d};
		Quaternion cq = q.conj();
		Quaternion qR = cq.rotate(horizontalCircleCenter);

		x0 = qR.getQ1();
		y0 = qR.getQ2();	
		double z0 = qR.getQ3();

		sphere.setInitialGuess(x0, y0, z0, r0);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		HashSet<FeaturePoint> uniquePointSet = new HashSet<FeaturePoint>();
		uniquePointSet.addAll(this.sphere.getFeaturePoints());
		uniquePointSet.addAll(this.plane.getFeaturePoints());
		deriveInitialGuess(uniquePointSet, this.sphere, this.plane);
	}
}
