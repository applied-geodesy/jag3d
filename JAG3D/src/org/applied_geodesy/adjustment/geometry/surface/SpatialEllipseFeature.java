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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.curve.EllipseFeature;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Ellipse;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction.SignType;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cylinder;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class SpatialEllipseFeature extends SurfaceFeature {
	private final Cylinder cylinder;
	private final Plane plane;
	
	public SpatialEllipseFeature() {
		super(true);
		
		this.cylinder = new Cylinder();
		this.plane    = new Plane();
		
		UnknownParameter ux = this.plane.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter uy = this.plane.getUnknownParameter(ParameterType.VECTOR_Y);
		UnknownParameter uz = this.plane.getUnknownParameter(ParameterType.VECTOR_Z);
		UnknownParameter du = this.plane.getUnknownParameter(ParameterType.LENGTH);
		UnknownParameter u  = this.plane.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		UnknownParameter xFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter xFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);
				
		UnknownParameter majorAxis = this.cylinder.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT);
		
		UnknownParameter vx = this.cylinder.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter vy = this.cylinder.getUnknownParameter(ParameterType.VECTOR_Y);
		UnknownParameter vz = this.cylinder.getUnknownParameter(ParameterType.VECTOR_Z);
		UnknownParameter v  = this.cylinder.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		v.setProcessingType(ProcessingType.ADJUSTMENT); // because |v| and |u| are redundant restrictions defined by the plane and the cylinder
		
		xFocal1.setVisible(false);
		yFocal1.setVisible(false);
		zFocal1.setVisible(false);
		
		xFocal2.setVisible(false);
		yFocal2.setVisible(false);
		zFocal2.setVisible(false);
		
		vx.setVisible(false);
		vy.setVisible(false);
		vz.setVisible(false);
		
		v.setVisible(false);
		u.setVisible(false);

		UnknownParameter xOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xFocal1InPlane = new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yFocal1InPlane = new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zFocal1InPlane = new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xFocal2InPlane = new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yFocal2InPlane = new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zFocal2InPlane = new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xEccentricity = new UnknownParameter(ParameterType.VECTOR_X, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter yEccentricity = new UnknownParameter(ParameterType.VECTOR_Y, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter zEccentricity = new UnknownParameter(ParameterType.VECTOR_Z, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter eccentricity  = new UnknownParameter(ParameterType.LENGTH,   false, 0.0, false, ProcessingType.POSTPROCESSING);
		
		UnknownParameter minorAxis = new UnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter one     = new UnknownParameter(ParameterType.CONSTANT, false, 1.0, false, ProcessingType.FIXED);
		UnknownParameter oneHalf = new UnknownParameter(ParameterType.CONSTANT, false, 0.5, false, ProcessingType.FIXED);
		
		// normal vector of the plane is identical to the main axis of the cylinder
		AverageRestriction xNormalRestriction = new AverageRestriction(true, List.of(ux), vx);
		AverageRestriction yNormalRestriction = new AverageRestriction(true, List.of(uy), vy);
		AverageRestriction zNormalRestriction = new AverageRestriction(true, List.of(uz), vz);
		
		// move focal points to plane
		ProductSumRestriction xFocal1Restriction = new ProductSumRestriction(false, List.of(xFocal1, du), List.of(one, ux), xFocal1InPlane);
		ProductSumRestriction yFocal1Restriction = new ProductSumRestriction(false, List.of(yFocal1, du), List.of(one, uy), yFocal1InPlane);
		ProductSumRestriction zFocal1Restriction = new ProductSumRestriction(false, List.of(zFocal1, du), List.of(one, uz), zFocal1InPlane);
		
		ProductSumRestriction xFocal2Restriction = new ProductSumRestriction(false, List.of(xFocal2, du), List.of(one, ux), xFocal2InPlane);
		ProductSumRestriction yFocal2Restriction = new ProductSumRestriction(false, List.of(yFocal2, du), List.of(one, uy), yFocal2InPlane);
		ProductSumRestriction zFocal2Restriction = new ProductSumRestriction(false, List.of(zFocal2, du), List.of(one, uz), zFocal2InPlane);
		
		// derive origin of the ellipse
		ProductSumRestriction xOriginRestriction = new ProductSumRestriction(false, List.of(xFocal1, xFocal2, du), List.of(oneHalf, oneHalf, ux), xOrigin);
		ProductSumRestriction yOriginRestriction = new ProductSumRestriction(false, List.of(yFocal1, yFocal2, du), List.of(oneHalf, oneHalf, uy), yOrigin);
		ProductSumRestriction zOriginRestriction = new ProductSumRestriction(false, List.of(zFocal1, zFocal2, du), List.of(oneHalf, oneHalf, uz), zOrigin);
		
		// estimate minor semi-axis, i.e., b = sqrt(a*a - e*e)
		List<SignType> signs = List.of(SignType.PLUS, SignType.MINUS);
		ProductSumRestriction xEccentricityRestriction = new ProductSumRestriction(false, List.of(xFocal1, xFocal2), List.of(oneHalf, oneHalf), signs, xEccentricity);
		ProductSumRestriction yEccentricityRestriction = new ProductSumRestriction(false, List.of(yFocal1, yFocal2), List.of(oneHalf, oneHalf), signs, yEccentricity);
		ProductSumRestriction zEccentricityRestriction = new ProductSumRestriction(false, List.of(zFocal1, zFocal2), List.of(oneHalf, oneHalf), signs, zEccentricity);
			
		List<UnknownParameter> eccentricityVector     = List.of(xEccentricity, yEccentricity, zEccentricity);
		ProductSumRestriction eccentricityRestriction = new ProductSumRestriction(false, eccentricityVector, eccentricityVector, 0.5, eccentricity);
		ProductSumRestriction minorAxisRestriction    = new ProductSumRestriction(false, List.of(majorAxis, eccentricity), List.of(majorAxis, eccentricity), 0.5, signs, minorAxis);

		
		this.add(this.cylinder);
		this.add(this.plane);
		
		List<UnknownParameter> newOrderedUnknownParameters = new ArrayList<UnknownParameter>();
		newOrderedUnknownParameters.add(xOrigin);
		newOrderedUnknownParameters.add(yOrigin);
		newOrderedUnknownParameters.add(zOrigin);
		
		newOrderedUnknownParameters.add(xFocal1InPlane);
		newOrderedUnknownParameters.add(yFocal1InPlane);
		newOrderedUnknownParameters.add(zFocal1InPlane);
		
		newOrderedUnknownParameters.add(xFocal2InPlane);
		newOrderedUnknownParameters.add(yFocal2InPlane);
		newOrderedUnknownParameters.add(zFocal2InPlane);

		newOrderedUnknownParameters.addAll(this.getUnknownParameters());
		newOrderedUnknownParameters.add(minorAxis);
		
		newOrderedUnknownParameters.add(eccentricity);
		newOrderedUnknownParameters.add(xEccentricity);
		newOrderedUnknownParameters.add(yEccentricity);
		newOrderedUnknownParameters.add(zEccentricity);
		
		newOrderedUnknownParameters.add(one);
		newOrderedUnknownParameters.add(oneHalf);
		
		this.getUnknownParameters().setAll(newOrderedUnknownParameters);
		
		this.getRestrictions().addAll(
				xNormalRestriction,
				yNormalRestriction,
				zNormalRestriction
		);
		
		this.getPostProcessingCalculations().addAll(
				xOriginRestriction,
				yOriginRestriction,
				zOriginRestriction,
				xFocal1Restriction,
				yFocal1Restriction,
				zFocal1Restriction,
				xFocal2Restriction,
				yFocal2Restriction,
				zFocal2Restriction,
				xEccentricityRestriction,
				yEccentricityRestriction,
				zEccentricityRestriction,
				eccentricityRestriction,
				minorAxisRestriction
		);
		
	}
	
	public Plane getPlane() {
		return this.plane;
	}

	public Cylinder getCylinder() {
		return this.cylinder;
	}
			
	public static void deriveInitialGuess(Collection<FeaturePoint> points, SpatialEllipseFeature feature) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.cylinder, feature.plane);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Cylinder cylinder, Plane plane) throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		// estimate plane of the circle
		PlaneFeature.deriveInitialGuess(points, plane);
		double nx = plane.getUnknownParameter(ParameterType.VECTOR_X).getValue0(); 
		double ny = plane.getUnknownParameter(ParameterType.VECTOR_Y).getValue0();
		double nz = plane.getUnknownParameter(ParameterType.VECTOR_Z).getValue0();
		double d  = plane.getUnknownParameter(ParameterType.LENGTH).getValue0();

		Quaternion q = FeatureUtil.getQuaternionHz(new double[] {nx, ny, nz});
		Collection<FeaturePoint> rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {0, 0, 0}, q);

		// estimate ellipse parameters
		Ellipse ellipse = new Ellipse();
		EllipseFeature.deriveInitialGuess(rotatedPoints, ellipse);
		
		double x1 = ellipse.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X).getValue0();
		double y1 = ellipse.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).getValue0();
		double x2 = ellipse.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X).getValue0(); 
		double y2 = ellipse.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).getValue0(); 
		double a  = ellipse.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue0(); 

		double z1 = d;
		double z2 = d;
		double principleAxis[] = new double[] {nx, ny, nz};

		Quaternion cq = q.conj();
		Quaternion qf1 = cq.rotate(new double[] {x1, y1, z1});
		double f13d[] = new double[] {
				qf1.getQ1(),
				qf1.getQ2(),
				qf1.getQ3(),
		};
		Quaternion qf2 = cq.rotate(new double[] {x2, y2, z2});
		double f23d[] = new double[] {
				qf2.getQ1(),
				qf2.getQ2(),
				qf2.getQ3(),
		};

		// estimate focal points which are closest to the origin
		double s1 = 0, s2 = 0, tmp = 0;
		for (int i = 0; i < 3; i++) {
			s1 += principleAxis[i] * f13d[i];
			s2 += principleAxis[i] * f23d[i];
			tmp += principleAxis[i] * principleAxis[i];
		}

		// scale parameter
		s1 = -s1 / tmp;
		s2 = -s2 / tmp;
		
		x1 = f13d[0] + s1 * nx;
		y1 = f13d[1] + s1 * ny;
		z1 = f13d[2] + s1 * nz;

		x2 = f23d[0] + s2 * nx;
		y2 = f23d[1] + s2 * ny;
		z2 = f23d[2] + s2 * nz;

		cylinder.setInitialGuess(x1, y1, z1, x2, y2, z2, nx, ny, nz, a);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		HashSet<FeaturePoint> uniquePointSet = new HashSet<FeaturePoint>();
		uniquePointSet.addAll(this.cylinder.getFeaturePoints());
		uniquePointSet.addAll(this.plane.getFeaturePoints());
		deriveInitialGuess(uniquePointSet, this.cylinder, this.plane);
	}
}
