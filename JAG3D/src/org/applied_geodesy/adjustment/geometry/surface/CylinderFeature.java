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

import javafx.util.Pair;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class CylinderFeature extends SurfaceFeature {
	
	private final Cylinder cylinder;

	public CylinderFeature() {
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

		vectorLength.setVisible(false);
		
		UnknownParameter xOrigin   = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yOrigin   = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zOrigin   = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Z, false, 0.0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter minorAxis = new UnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT, false, 0.0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xEccentricity = new UnknownParameter(ParameterType.VECTOR_X, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter yEccentricity = new UnknownParameter(ParameterType.VECTOR_Y, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter zEccentricity = new UnknownParameter(ParameterType.VECTOR_Z, false, 0.0, false, ProcessingType.POSTPROCESSING);
		UnknownParameter eccentricity  = new UnknownParameter(ParameterType.LENGTH,   false, 0.0, false, ProcessingType.POSTPROCESSING);
		
		UnknownParameter oneHalf = new UnknownParameter(ParameterType.CONSTANT, false, 0.5, false, ProcessingType.FIXED);
		
		AverageRestriction xOriginRestriction = new AverageRestriction(false, List.of(xFocal1, xFocal2), xOrigin);
		AverageRestriction yOriginRestriction = new AverageRestriction(false, List.of(yFocal1, yFocal2), yOrigin);
		AverageRestriction zOriginRestriction = new AverageRestriction(false, List.of(zFocal1, zFocal2), zOrigin);
		
		List<SignType> signs = List.of(SignType.PLUS, SignType.MINUS);
		ProductSumRestriction xEccentricityRestriction = new ProductSumRestriction(false, List.of(xFocal1, xFocal2), List.of(oneHalf, oneHalf), signs, xEccentricity);
		ProductSumRestriction yEccentricityRestriction = new ProductSumRestriction(false, List.of(yFocal1, yFocal2), List.of(oneHalf, oneHalf), signs, yEccentricity);
		ProductSumRestriction zEccentricityRestriction = new ProductSumRestriction(false, List.of(zFocal1, zFocal2), List.of(oneHalf, oneHalf), signs, zEccentricity);
			
		List<UnknownParameter> eccentricityVector = List.of(xEccentricity, yEccentricity, zEccentricity);
		ProductSumRestriction eccentricityRestriction = new ProductSumRestriction(false, eccentricityVector, eccentricityVector, 0.5, eccentricity);
		ProductSumRestriction minorAxisRestriction    = new ProductSumRestriction(false, List.of(majorAxis, eccentricity), List.of(majorAxis, eccentricity), 0.5, signs, minorAxis);

		this.add(this.cylinder);
		
		List<UnknownParameter> newOrderedUnknownParameters = new ArrayList<UnknownParameter>();
		newOrderedUnknownParameters.add(xOrigin);
		newOrderedUnknownParameters.add(yOrigin);
		newOrderedUnknownParameters.add(zOrigin);

		newOrderedUnknownParameters.addAll(this.getUnknownParameters());
		newOrderedUnknownParameters.add(minorAxis);
		
		newOrderedUnknownParameters.add(eccentricity);
		newOrderedUnknownParameters.add(xEccentricity);
		newOrderedUnknownParameters.add(yEccentricity);
		newOrderedUnknownParameters.add(zEccentricity);
//		
		newOrderedUnknownParameters.add(oneHalf);
		
		this.getUnknownParameters().setAll(newOrderedUnknownParameters);
		
		this.getPostProcessingCalculations().addAll(
				xOriginRestriction,
				yOriginRestriction,
				zOriginRestriction,
				xEccentricityRestriction,
				yEccentricityRestriction,
				zEccentricityRestriction,
				eccentricityRestriction,
				minorAxisRestriction
		);
	}
	
	public Cylinder getCylinder() {
		return this.cylinder;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, CylinderFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
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
		
		if (nop < 7)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 7 points are needed.");
		
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
		Pair<Double, Ellipse> approximation1 = getEllipse(rotatedPoints);
		
		rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {x0, y0, z0}, q2);
		Pair<Double, Ellipse> approximation2 = getEllipse(rotatedPoints);
		
		rotatedPoints = FeatureUtil.getRotatedFeaturePoints(points, new double[] {x0, y0, z0}, q3);
		Pair<Double, Ellipse> approximation3 = getEllipse(rotatedPoints);
		
		rotatedPoints.clear();
		rotatedPoints = null;
		
		Quaternion cq;
		Pair<Double, Ellipse> bestApproximation;
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
		Ellipse ellipse = bestApproximation.getValue();
		double x1 = ellipse.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X).getValue0();
		double y1 = ellipse.getUnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).getValue0();
		double z1 = 0.0;
		double x2 = ellipse.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X).getValue0();
		double y2 = ellipse.getUnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).getValue0();
		double z2 = 0.0;
		double a  = ellipse.getUnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue0();
		
		Quaternion qf1 = cq.rotate(new double[] {x1, y1, z1});
		double f13d[] = new double[] {
				qf1.getQ1() + x0,
				qf1.getQ2() + y0,
				qf1.getQ3() + z0,
		};
		Quaternion qf2 = cq.rotate(new double[] {x2, y2, z2});
		double f23d[] = new double[] {
				qf2.getQ1() + x0,
				qf2.getQ2() + y0,
				qf2.getQ3() + z0,
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
		
		double nx = principleAxis[0];
		double ny = principleAxis[1];
		double nz = principleAxis[2];
		
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
		deriveInitialGuess(this.cylinder.getFeaturePoints(), this.cylinder);
	}
	
	private static Pair<Double, Ellipse> getEllipse(Collection<FeaturePoint> points) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		Ellipse ellipse = new Ellipse();
		EllipseFeature.deriveInitialGuess(points, ellipse);
		
		Collection<UnknownParameter> parameters = ellipse.getUnknownParameters();
		for (UnknownParameter parameter : parameters) {
			parameter.setValue(parameter.getValue0());
		}
		
		double epsilon = 0.0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			double res = ellipse.getMisclosure(point);
			epsilon += res * res;
		}

		return new Pair<Double, Ellipse>(epsilon, ellipse);
	}
}
