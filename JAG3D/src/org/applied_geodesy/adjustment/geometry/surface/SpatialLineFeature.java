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

import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction.SignType;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Plane;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class SpatialLineFeature extends SurfaceFeature {
	private final Plane planeU;
	private final Plane planeV;
	
	private UnknownParameter xCoordinateInPlaneU, yCoordinateInPlaneU, zCoordinateInPlaneU;

	public SpatialLineFeature() {
		super(true);
		
		this.planeU = new Plane();
		this.planeV = new Plane();
		
		this.xCoordinateInPlaneU = new UnknownParameter(ParameterType.COORDINATE_X, true, 0, false, ProcessingType.FIXED);
		this.yCoordinateInPlaneU = new UnknownParameter(ParameterType.COORDINATE_Y, true, 0, false, ProcessingType.FIXED);
		this.zCoordinateInPlaneU = new UnknownParameter(ParameterType.COORDINATE_Z, true, 0, false, ProcessingType.FIXED);
		
		UnknownParameter xOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Z, false, 0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xNormal = new UnknownParameter(ParameterType.VECTOR_X, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yNormal = new UnknownParameter(ParameterType.VECTOR_Y, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter zNormal = new UnknownParameter(ParameterType.VECTOR_Z, false, 0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter dotProductUV = new UnknownParameter(ParameterType.LENGTH, false, 0, false, ProcessingType.FIXED);
		
		UnknownParameter ux = this.planeU.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter uy = this.planeU.getUnknownParameter(ParameterType.VECTOR_Y);
		UnknownParameter uz = this.planeU.getUnknownParameter(ParameterType.VECTOR_Z);
		UnknownParameter du = this.planeU.getUnknownParameter(ParameterType.LENGTH);
		UnknownParameter u  = this.planeU.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		UnknownParameter vx = this.planeV.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter vy = this.planeV.getUnknownParameter(ParameterType.VECTOR_Y);
		UnknownParameter vz = this.planeV.getUnknownParameter(ParameterType.VECTOR_Z);
		UnknownParameter dv = this.planeV.getUnknownParameter(ParameterType.LENGTH);
		UnknownParameter v  = this.planeU.getUnknownParameter(ParameterType.VECTOR_LENGTH);
		
		ux.setVisible(false);
		uy.setVisible(false);
		uz.setVisible(false);
		du.setVisible(false);
		u.setVisible(false);
		
		vx.setVisible(false);
		vy.setVisible(false);
		vz.setVisible(false);
		dv.setVisible(false);
		v.setVisible(false);
		
		List<UnknownParameter> normalVectorU  = List.of(ux, uy, uz);
		List<UnknownParameter> normalVectorV  = List.of(vx, vy, vz);
		List<UnknownParameter> arbitraryPoint = List.of(this.xCoordinateInPlaneU, this.yCoordinateInPlaneU, this.zCoordinateInPlaneU);

		ProductSumRestriction arbitraryPointInPlaneURestriction = new ProductSumRestriction(true, normalVectorU, arbitraryPoint, du);
		ProductSumRestriction orthogonalVectorsUVRestriction    = new ProductSumRestriction(true, normalVectorU, normalVectorV, dotProductUV);
		
		this.add(this.planeU);
		this.add(this.planeV);
		
		this.getRestrictions().add(arbitraryPointInPlaneURestriction);
		this.getRestrictions().add(orthogonalVectorsUVRestriction);
		
		List<UnknownParameter> newOrderedUnknownParameters = new ArrayList<UnknownParameter>();
		newOrderedUnknownParameters.add(xOrigin);
		newOrderedUnknownParameters.add(yOrigin);
		newOrderedUnknownParameters.add(zOrigin);
		
		newOrderedUnknownParameters.add(xNormal);
		newOrderedUnknownParameters.add(yNormal);
		newOrderedUnknownParameters.add(zNormal);
		
		newOrderedUnknownParameters.add(dotProductUV);
		
		newOrderedUnknownParameters.add(this.xCoordinateInPlaneU);
		newOrderedUnknownParameters.add(this.yCoordinateInPlaneU);
		newOrderedUnknownParameters.add(this.zCoordinateInPlaneU);
		
		newOrderedUnknownParameters.addAll(this.getUnknownParameters());
		this.getUnknownParameters().setAll(newOrderedUnknownParameters);
		
		ProductSumRestriction multAddX = new ProductSumRestriction(false, List.of(ux, vx), List.of(du, dv), xOrigin);
		ProductSumRestriction multAddY = new ProductSumRestriction(false, List.of(uy, vy), List.of(du, dv), yOrigin);
		ProductSumRestriction multAddZ = new ProductSumRestriction(false, List.of(uz, vz), List.of(du, dv), zOrigin);
		
		// derive direction vector of the line
		// cross product single equation: A1 * B2 - A2 * B1  ==  C
		List<SignType> signs = List.of(SignType.PLUS, SignType.MINUS);
		ProductSumRestriction crossProductX = new ProductSumRestriction(false, List.of(uy, uz), List.of(vz, vy), signs, xNormal);
		ProductSumRestriction crossProductY = new ProductSumRestriction(false, List.of(uz, ux), List.of(vx, vz), signs, yNormal);
		ProductSumRestriction crossProductZ = new ProductSumRestriction(false, List.of(ux, uy), List.of(vy, vx), signs, zNormal);
		
		this.getPostProcessingCalculations().addAll(
				multAddX, multAddY, multAddZ,
				crossProductX, crossProductY, crossProductZ
		);
	}

	public Plane getPlaneU() {
		return this.planeU;
	}

	public Plane getPlaneV() {
		return this.planeV;
	}
	
	@Override
	public void setCenterOfMass(Point centerOfMass) {
		// get previous center of mass
		Point prevCenterOfMass = this.getCenterOfMass();
		if (centerOfMass.equalsCoordinateComponents(prevCenterOfMass))
			return;

		super.setCenterOfMass(centerOfMass);
		this.adaptArbitraryPointInPlaneRestriction();
	}

	@Override
	public void prepareIteration() {
		this.adaptArbitraryPointInPlaneRestriction();
	}
		
	private void adaptArbitraryPointInPlaneRestriction() {
		double ux = this.planeU.getUnknownParameter(ParameterType.VECTOR_X).getValue();
		double uy = this.planeU.getUnknownParameter(ParameterType.VECTOR_Y).getValue();
		double uz = this.planeU.getUnknownParameter(ParameterType.VECTOR_Z).getValue();
		double du = this.planeU.getUnknownParameter(ParameterType.LENGTH).getValue();

		double vx = this.planeV.getUnknownParameter(ParameterType.VECTOR_X).getValue();
		double vy = this.planeV.getUnknownParameter(ParameterType.VECTOR_Y).getValue();
		double vz = this.planeV.getUnknownParameter(ParameterType.VECTOR_Z).getValue();
		double dv = this.planeV.getUnknownParameter(ParameterType.LENGTH).getValue();

		// estimates a point, lying in both planes
		double x0 = ux * du + vx * dv;
		double y0 = uy * du + vy * dv;
		double z0 = uz * du + vz * dv;

		// move this position by vector v --> point lies in plane u but not in v
		x0 = x0 + vx;
		y0 = y0 + vy;
		z0 = z0 + vz;

		this.xCoordinateInPlaneU.setValue(x0);
		this.yCoordinateInPlaneU.setValue(y0);
		this.zCoordinateInPlaneU.setValue(z0);
	}
	
	public static void deriveInitialGuess(Collection<? extends FeaturePoint> points, SpatialLineFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.planeU, feature.planeV);
	}
	
	public static void deriveInitialGuess(Collection<? extends FeaturePoint> points, Plane planeU, Plane planeV) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();
			z0 += point.getZ0();
			if (planeU.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + planeU.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 2)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 2 points are needed.");
		
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
				else if (i == 2) 
					hi = zi;

				for (int j = i; j < 3; j++) {
					double hj = 0;

					if (j == 0)
						hj = xi; 
					else if (j == 1)
						hj = yi;
					else if (j == 2) 
						hj = zi;

					H.set(i, j, H.get(i,j) + hi * hj);
				}
			}
		}

		SymmPackEVD evd = new SymmPackEVD(3, true, true);
		evd.factor(H);

		Matrix eigVec = evd.getEigenvectors();
		double eigVal[] = evd.getEigenvalues();
		int indexMaxEigVal = 0;
		double maxEigVal = eigVal[indexMaxEigVal];
		for (int i=indexMaxEigVal+1; i<eigVal.length; i++) {
			if (maxEigVal < eigVal[i]) {
				maxEigVal = eigVal[i];
				indexMaxEigVal = i;
			}
		}

		// Normal vector w of the line is eigenvector which corresponds to the largest eigenvalue
		// the orthonormal basis u x v is needed
		double u[] = new double[3];
		double v[] = new double[3];

		for (int i = 0; i < 3; i++) {
			if (indexMaxEigVal==0) {
				u[i] = eigVec.get(i, 1);
				v[i] = eigVec.get(i, 2);
			}
			else if (indexMaxEigVal==1) {
				u[i] = eigVec.get(i, 0);
				v[i] = eigVec.get(i, 2);
			}
			else {
				u[i] = eigVec.get(i, 0);
				v[i] = eigVec.get(i, 1);
			}
		}

		double du = u[0] * x0 + u[1] * y0 + u[2] * z0;
		double dv = v[0] * x0 + v[1] * y0 + v[2] * z0;
		
		planeU.setInitialGuess(u[0], u[1], u[2], du);
		planeV.setInitialGuess(v[0], v[1], v[2], dv);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		HashSet<FeaturePoint> uniquePointSet = new HashSet<FeaturePoint>();
		uniquePointSet.addAll(this.planeU.getFeaturePoints());
		uniquePointSet.addAll(this.planeV.getFeaturePoints());
		deriveInitialGuess(uniquePointSet, this.planeU, this.planeV);
	}
}
