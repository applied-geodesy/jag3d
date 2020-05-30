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
import java.util.List;

import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.curve.CircleFeature;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Circle;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Cylinder;

import javafx.util.Pair;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class CircularCylinderFeature extends SurfaceFeature {
	
	private final Cylinder cylinder;

	public CircularCylinderFeature() {
		super(true);
		
		this.cylinder = new Cylinder();
		
		UnknownParameter xFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal1 = this.cylinder.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter xFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal2 = this.cylinder.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter majorAxis    = this.cylinder.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT);
		UnknownParameter vectorLength = this.cylinder.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		xFocal1.setVisible(false);
		yFocal1.setVisible(false);
		zFocal1.setVisible(false);
		
		xFocal2.setVisible(false);
		yFocal2.setVisible(false);
		zFocal2.setVisible(false);
		
		majorAxis.setVisible(false);
		vectorLength.setVisible(false);

		UnknownParameter xOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter radius  = new UnknownParameter(ParameterType.RADIUS,              false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		// F1 == F1 (condition on z-component is already full-filled, cf. 
		// Loesler, M.: Modellierung und Bestimmung eines elliptischen Zylinders. 
		// avn, Vol. 127(2), S. 87-93, 2020.
		AverageRestriction xFocalRestriction = new AverageRestriction(true, List.of(xFocal1), xFocal2);
		AverageRestriction yFocalRestriction = new AverageRestriction(true, List.of(yFocal1), yFocal2);
				
		// origin is identical to F1 and F2
		AverageRestriction xOriginRestriction = new AverageRestriction(false, List.of(xFocal1, xFocal2), xOrigin);
		AverageRestriction yOriginRestriction = new AverageRestriction(false, List.of(yFocal1, yFocal2), yOrigin);
		AverageRestriction zOriginRestriction = new AverageRestriction(false, List.of(zFocal1, zFocal2), zOrigin);
		AverageRestriction radiusRestriction  = new AverageRestriction(false, List.of(majorAxis), radius);
		
		this.add(this.cylinder);
		
		List<UnknownParameter> newOrderedUnknownParameters = new ArrayList<UnknownParameter>();
		newOrderedUnknownParameters.add(xOrigin);
		newOrderedUnknownParameters.add(yOrigin);
		newOrderedUnknownParameters.add(zOrigin);

		newOrderedUnknownParameters.addAll(this.getUnknownParameters());
		newOrderedUnknownParameters.add(radius);
		
		this.getUnknownParameters().setAll(newOrderedUnknownParameters);
		
		
		this.getRestrictions().addAll(
				xFocalRestriction,
				yFocalRestriction
		);
		
		this.getPostProcessingCalculations().addAll(
				xOriginRestriction,
				yOriginRestriction,
				zOriginRestriction,
				radiusRestriction
		);
	}
	
	public Cylinder getCylinder() {
		return this.cylinder;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, CircularCylinderFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.cylinder);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Cylinder cylinder) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (cylinder.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + cylinder.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 5)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 5 points are needed.");
		
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
		
		Matrix evec = evd.getEigenvectors();

		// rotate points to xy-plane to derive axis of cylinder
		Collection<FeaturePoint> rotatedPoints;
		
		Quaternion q1 = FeatureUtil.getQuaternionHz(new double[] {evec.get(0,0), evec.get(1,0), evec.get(2,0)});
		Quaternion q2 = FeatureUtil.getQuaternionHz(new double[] {evec.get(0,1), evec.get(1,1), evec.get(2,1)});
		Quaternion q3 = FeatureUtil.getQuaternionHz(new double[] {evec.get(0,2), evec.get(1,2), evec.get(2,2)});
		
		rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {x0, y0, z0}, q1);
		Pair<Double, Circle> approximation1 = getCircle(rotatedPoints);
		
		rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {x0, y0, z0}, q2);
		Pair<Double, Circle> approximation2 = getCircle(rotatedPoints);
		
		rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {x0, y0, z0}, q3);
		Pair<Double, Circle> approximation3 = getCircle(rotatedPoints);
		
		rotatedPoints.clear();
		rotatedPoints = null;
		
		Quaternion cq;
		Pair<Double, Circle> bestApproximation;
		double principleAxis[];
		// first principle axis == main axis
		if (approximation1.getKey() < approximation2.getKey() && approximation1.getKey() < approximation3.getKey()) {
			cq = q1.conj();
			principleAxis = new double[] {evec.get(0,0), evec.get(1,0), evec.get(2,0)};
			bestApproximation = approximation1;
		}
		// second principle axis == main axis
		else if (approximation2.getKey() < approximation1.getKey() && approximation2.getKey() < approximation3.getKey()) {
			cq = q2.conj();
			principleAxis = new double[] {evec.get(0,1), evec.get(1,1), evec.get(2,1)};
			bestApproximation = approximation2;
		}
		// third principle axis == main axis
		else { // if (approximation3.getKey() < approximation1.getKey() && approximation3.getKey() < approximation2.getKey())
			cq = q3.conj();
			principleAxis = new double[] {evec.get(0,2), evec.get(1,2), evec.get(2,2)};
			bestApproximation = approximation3;
		}

		q1 = q2 = q3 = null;
		approximation1 = approximation2 = approximation3 = null;
		
		// rotate the focal points back to the spatial case 
		Circle circle = bestApproximation.getValue();
		double x1 = circle.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_X).getValue0();
		double y1 = circle.getUnknownParameter(ParameterType.ORIGIN_COORDINATE_Y).getValue0();
		double z1 = 0.0;
		double r  = circle.getUnknownParameter(ParameterType.RADIUS).getValue0();
		
		Quaternion qf1 = cq.rotate(new double[] {x1, y1, z1});
		double f13d[] = new double[] {
				qf1.getQ1() + x0,
				qf1.getQ2() + y0,
				qf1.getQ3() + z0,
		};
				
		// estimate focal points which are closest to the origin
		double s1 = 0, tmp = 0;
		for (int i = 0; i < 3; i++) {
			s1 += principleAxis[i] * f13d[i];
			tmp += principleAxis[i] * principleAxis[i];
		}
		
		// scale parameter
		s1 = -s1 / tmp;
		
		double nx = principleAxis[0];
		double ny = principleAxis[1];
		double nz = principleAxis[2];
		
		x1 = f13d[0] + s1 * nx;
		y1 = f13d[1] + s1 * ny;
		z1 = f13d[2] + s1 * nz;
		
		cylinder.setInitialGuess(x1, y1, z1, x1, y1, z1, nx, ny, nz, r);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.cylinder.getFeaturePoints(), this.cylinder);
	}
	
	private static Pair<Double, Circle> getCircle(Collection<FeaturePoint> points) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Circle circle = new Circle();
		CircleFeature.deriveInitialGuess(points, circle);
		
		Collection<UnknownParameter> parameters = circle.getUnknownParameters();
		for (UnknownParameter parameter : parameters) {
			parameter.setValue(parameter.getValue0());
		}
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double res = circle.getMisclosure(point);
			epsilon += res * res;
		}

		return new Pair<Double, Circle>(epsilon, circle);
	}
	
	
}
