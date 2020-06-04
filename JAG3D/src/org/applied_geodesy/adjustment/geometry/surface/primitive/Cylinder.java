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

package org.applied_geodesy.adjustment.geometry.surface.primitive;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class Cylinder extends Surface {
	private Map<ParameterType, UnknownParameter> parameters;
	
	private ProductSumRestriction vectorLengthRestriction;
	private ProductSumRestriction primaryFocalRestriction;
	private ProductSumRestriction secondaryFocalRestriction;

	public Cylinder() {
		this.init();
	}
	
	public void setInitialGuess(double x1, double y1, double z1, double x2, double y2, double z2, double nx, double ny, double nz, double a) throws IllegalArgumentException {
		this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X).setValue0(x1);
		this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).setValue0(y1);
		this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z).setValue0(z1);
		
		this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X).setValue0(x2);
		this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).setValue0(y2);
		this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z).setValue0(z2);
		
		this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).setValue0(a);
		
		this.parameters.get(ParameterType.VECTOR_X).setValue0(nx);
		this.parameters.get(ParameterType.VECTOR_Y).setValue0(ny);
		this.parameters.get(ParameterType.VECTOR_Z).setValue0(nz);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();
		
		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter z1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter z2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter a  = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);

		UnknownParameter nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter ny = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter nz = this.parameters.get(ParameterType.VECTOR_Z);
		
		double c11 = nz.getValue() * (yi - y1.getValue()) - ny.getValue() * (zi - z1.getValue());
        double c12 = nx.getValue() * (zi - z1.getValue()) - nz.getValue() * (xi - x1.getValue());
        double c13 = ny.getValue() * (xi - x1.getValue()) - nx.getValue() * (yi - y1.getValue());
        
        double c21 = nz.getValue() * (yi - y2.getValue()) - ny.getValue() * (zi - z2.getValue());
        double c22 = nx.getValue() * (zi - z2.getValue()) - nz.getValue() * (xi - x2.getValue());
        double c23 = ny.getValue() * (xi - x2.getValue()) - nx.getValue() * (yi - y2.getValue());
        
        double PcrossF1 = Math.sqrt(c11 * c11 + c12 * c12 + c13 * c13);
        double PcrossF2 = Math.sqrt(c21 * c21 + c22 * c22 + c23 * c23);

		if (Jx != null) {
			if (x1.getColumn() >= 0)
				Jx.set(rowIndex, x1.getColumn(),  (nz.getValue() * c12 - ny.getValue() * c13) / PcrossF1);
			if (y1.getColumn() >= 0)
				Jx.set(rowIndex, y1.getColumn(),  (nx.getValue() * c13 - nz.getValue() * c11) / PcrossF1);
			if (z1.getColumn() >= 0)
				Jx.set(rowIndex, z1.getColumn(),  (ny.getValue() * c11 - nx.getValue() * c12) / PcrossF1);
			
			if (x2.getColumn() >= 0)
				Jx.set(rowIndex, x2.getColumn(),  (nz.getValue() * c22 - ny.getValue() * c23) / PcrossF2);
			if (y2.getColumn() >= 0)
				Jx.set(rowIndex, y2.getColumn(),  (nx.getValue() * c23 - nz.getValue() * c21) / PcrossF2);
			if (z2.getColumn() >= 0)
				Jx.set(rowIndex, z2.getColumn(),  (ny.getValue() * c21 - nx.getValue() * c22) / PcrossF2);
			
			if (a.getColumn() >= 0)
				Jx.set(rowIndex, a.getColumn(),  -2.0);
			
			if (nx.getColumn() >= 0)
				Jx.set(rowIndex, nx.getColumn(),  ((zi - z2.getValue()) * c22 + (y2.getValue() - yi) * c23) / PcrossF2 + ((zi - z1.getValue()) * c12 + (y1.getValue() - yi) * c13) / PcrossF1);
			if (ny.getColumn() >= 0)
				Jx.set(rowIndex, ny.getColumn(),  ((z2.getValue() - zi) * c21 + (xi - x2.getValue()) * c23) / PcrossF2 + ((z1.getValue() - zi) * c11 + (xi - x1.getValue()) * c13) / PcrossF1);
			if (nz.getColumn() >= 0)
				Jx.set(rowIndex, nz.getColumn(),  ((yi - y2.getValue()) * c21 + (x2.getValue() - xi) * c22) / PcrossF2 + ((yi - y1.getValue()) * c11 + (x1.getValue() - xi) * c12) / PcrossF1);
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, (ny.getValue() * c23 - nz.getValue() * c22) / PcrossF2 + (ny.getValue() * c13 - nz.getValue() * c12) / PcrossF1);
			Jv.set(rowIndex, 1, (nz.getValue() * c21 - nx.getValue() * c23) / PcrossF2 + (nz.getValue() * c11 - nx.getValue() * c13) / PcrossF1);
			Jv.set(rowIndex, 2, (nx.getValue() * c22 - ny.getValue() * c21) / PcrossF2 + (nx.getValue() * c12 - ny.getValue() * c11) / PcrossF1);
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();
		
		double x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X).getValue();
		double y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).getValue();
		double z1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z).getValue();
		
		double x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X).getValue();
		double y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).getValue();
		double z2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z).getValue();
		
		double a  = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue();
		
		double nx = this.parameters.get(ParameterType.VECTOR_X).getValue();
		double ny = this.parameters.get(ParameterType.VECTOR_Y).getValue();
		double nz = this.parameters.get(ParameterType.VECTOR_Z).getValue();

        
        double c11 = nz * (yi - y1) - ny * (zi - z1);
        double c12 = nx * (zi - z1) - nz * (xi - x1);
        double c13 = ny * (xi - x1) - nx * (yi - y1);
        
        double c21 = nz * (yi - y2) - ny * (zi - z2);
        double c22 = nx * (zi - z2) - nz * (xi - x2);
        double c23 = ny * (xi - x2) - nx * (yi - y2);

        double PcrossF1 = Math.sqrt(c11 * c11 + c12 * c12 + c13 * c13);
        double PcrossF2 = Math.sqrt(c21 * c21 + c22 * c22 + c23 * c23);
        
        return PcrossF1 + PcrossF2 - 2.0 * a;
	}
	
	@Override
	public void setCenterOfMass(Point centerOfMass) {
		// get previous center of mass
		Point prevCenterOfMass = this.getCenterOfMass();

		if (centerOfMass.equalsCoordinateComponents(prevCenterOfMass))
			return;

		super.setCenterOfMass(centerOfMass);
		// get current center of mass
		Point currCenterOfMass = this.getCenterOfMass();
		
		// Bestimme Aufpunkt
		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter z1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter z2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);
		
		// Shift
		double xf1 = x1.getValue() + prevCenterOfMass.getX0() - currCenterOfMass.getX0();
		double yf1 = y1.getValue() + prevCenterOfMass.getY0() - currCenterOfMass.getY0();
		double zf1 = z1.getValue() + prevCenterOfMass.getZ0() - currCenterOfMass.getZ0();
		
		double xf2 = x2.getValue() + prevCenterOfMass.getX0() - currCenterOfMass.getX0();
		double yf2 = y2.getValue() + prevCenterOfMass.getY0() - currCenterOfMass.getY0();
		double zf2 = z2.getValue() + prevCenterOfMass.getZ0() - currCenterOfMass.getZ0();
		
		// bestimme Punkt mit kuerzesten Abstand zum Ursprung
		double nx = this.parameters.get(ParameterType.VECTOR_X).getValue();
		double ny = this.parameters.get(ParameterType.VECTOR_Y).getValue();
		double nz = this.parameters.get(ParameterType.VECTOR_Z).getValue();
		
		double dotNormalNormal = nx * nx + ny * ny + nz * nz;
		
		if (dotNormalNormal != 0) {
			double dotNormalF1 = nx * xf1 + ny * yf1 + nz * zf1;
			double dotNormalF2 = nx * xf2 + ny * yf2 + nz * zf2;
			
			double s1 = -dotNormalF1 / dotNormalNormal;
			double s2 = -dotNormalF2 / dotNormalNormal;
			
			xf1 = xf1 + s1 * nx;
			yf1 = yf1 + s1 * ny;
			zf1 = zf1 + s1 * nz;
			
			xf2 = xf2 + s2 * nx;
			yf2 = yf2 + s2 * ny;
			zf2 = zf2 + s2 * nz;
		}
		
		x1.setValue(xf1);
		y1.setValue(yf1);
		z1.setValue(zf1);
		
		x2.setValue(xf2);
		y2.setValue(yf2);
		z2.setValue(zf2);
	}

	@Override
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		int nou = Dp.numColumns();
		Point centerOfMass = this.getCenterOfMass();

		// Bestimme Aufpunkt
		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter z1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);

		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter z2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);
		
		UnknownParameter nX = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter nY = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter nZ = this.parameters.get(ParameterType.VECTOR_Z);

		// Shift
		double xf1 = x1.getValue() + centerOfMass.getX0();
		double yf1 = y1.getValue() + centerOfMass.getY0();
		double zf1 = z1.getValue() + centerOfMass.getZ0();

		double xf2 = x2.getValue() + centerOfMass.getX0();
		double yf2 = y2.getValue() + centerOfMass.getY0();
		double zf2 = z2.getValue() + centerOfMass.getZ0();

		// bestimme Punkt mit kuerzesten Abstand zum Ursprung
		double nx = nX.getValue();
		double ny = nY.getValue();
		double nz = nZ.getValue();
		
		double dotNormalNormal = nx * nx + ny * ny + nz * nz;
		double squaredDotNormalNormal = dotNormalNormal * dotNormalNormal;
		
		double dotNormalF1 = nx * xf1 + ny * yf1 + nz * zf1;
		double dotNormalF2 = nx * xf2 + ny * yf2 + nz * zf2;
		
		double s1 = -dotNormalF1 / dotNormalNormal;
		double s2 = -dotNormalF2 / dotNormalNormal;
		
		Matrix J = Matrices.identity(nou);

		if (x1.getColumn() >= 0) {
			if (x1.getColumn() >= 0)
				J.set(x1.getColumn(), x1.getColumn(), 1.0 - nx * nx / dotNormalNormal);
			if (y1.getColumn() >= 0)
				J.set(x1.getColumn(), y1.getColumn(),     - nx * ny / dotNormalNormal);
			if (z1.getColumn() >= 0)
				J.set(x1.getColumn(), z1.getColumn(),     - nx * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(x1.getColumn(), nX.getColumn(), (2.0 * nx * nx * dotNormalF1) / squaredDotNormalNormal - (nx * xf1) / dotNormalNormal + s1);
			if (nY.getColumn() >= 0)
				J.set(x1.getColumn(), nY.getColumn(), (2.0 * nx * ny * dotNormalF1) / squaredDotNormalNormal - (nx * yf1) / dotNormalNormal);
			if (nZ.getColumn() >= 0)
				J.set(x1.getColumn(), nZ.getColumn(), (2.0 * nx * nz * dotNormalF1) / squaredDotNormalNormal - (nx * zf1) / dotNormalNormal);
		}

		if (y1.getColumn() >= 0) {
			if (x1.getColumn() >= 0)
				J.set(y1.getColumn(), x1.getColumn(),     - ny * nx / dotNormalNormal);
			if (y1.getColumn() >= 0)
				J.set(y1.getColumn(), y1.getColumn(), 1.0 - ny * ny / dotNormalNormal);
			if (z1.getColumn() >= 0)
				J.set(y1.getColumn(), z1.getColumn(),     - ny * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(y1.getColumn(), nX.getColumn(), (2.0 * ny * nx * dotNormalF1) / squaredDotNormalNormal - (ny * xf1) / dotNormalNormal);
			if (nY.getColumn() >= 0)
				J.set(y1.getColumn(), nY.getColumn(), (2.0 * ny * ny * dotNormalF1) / squaredDotNormalNormal - (ny * yf1) / dotNormalNormal + s1);
			if (nZ.getColumn() >= 0)
				J.set(y1.getColumn(), nZ.getColumn(), (2.0 * ny * nz * dotNormalF1) / squaredDotNormalNormal - (ny * zf1) / dotNormalNormal);
		}

		if (z1.getColumn() >= 0) {
			if (x1.getColumn() >= 0)
				J.set(z1.getColumn(), x1.getColumn(),     - nz * nx / dotNormalNormal);
			if (y1.getColumn() >= 0)
				J.set(z1.getColumn(), y1.getColumn(),     - nz * ny / dotNormalNormal);
			if (z1.getColumn() >= 0)
				J.set(z1.getColumn(), z1.getColumn(), 1.0 - nz * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(z1.getColumn(), nX.getColumn(), (2.0 * nz * nx * dotNormalF1) / squaredDotNormalNormal - (nz * xf1) / dotNormalNormal);
			if (nY.getColumn() >= 0)
				J.set(z1.getColumn(), nY.getColumn(), (2.0 * nz * ny * dotNormalF1) / squaredDotNormalNormal - (nz * yf1) / dotNormalNormal);
			if (nZ.getColumn() >= 0)
				J.set(z1.getColumn(), nZ.getColumn(), (2.0 * nz * nz * dotNormalF1) / squaredDotNormalNormal - (nz * zf1) / dotNormalNormal + s1);
		}

		if (x2.getColumn() >= 0) {
			if (x2.getColumn() >= 0)
				J.set(x2.getColumn(), x2.getColumn(), 1.0 - nx * nx / dotNormalNormal);
			if (y2.getColumn() >= 0)
				J.set(x2.getColumn(), y2.getColumn(),     - nx * ny / dotNormalNormal);
			if (z2.getColumn() >= 0)
				J.set(x2.getColumn(), z2.getColumn(),     - nx * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(x2.getColumn(), nX.getColumn(), (2.0 * nx * nx * dotNormalF2) / squaredDotNormalNormal - (nx * xf2) / dotNormalNormal + s2);
			if (nY.getColumn() >= 0)
				J.set(x2.getColumn(), nY.getColumn(), (2.0 * nx * ny * dotNormalF2) / squaredDotNormalNormal - (nx * yf2) / dotNormalNormal);
			if (nZ.getColumn() >= 0)
				J.set(x2.getColumn(), nZ.getColumn(), (2.0 * nx * nz * dotNormalF2) / squaredDotNormalNormal - (nx * zf2) / dotNormalNormal);
		}

		if (y2.getColumn() >= 0) {
			if (x2.getColumn() >= 0)
				J.set(y2.getColumn(), x2.getColumn(),     - ny * nx / dotNormalNormal);
			if (y2.getColumn() >= 0)
				J.set(y2.getColumn(), y2.getColumn(), 1.0 - ny * ny / dotNormalNormal);
			if (z2.getColumn() >= 0)
				J.set(y2.getColumn(), z2.getColumn(),     - ny * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(y2.getColumn(), nX.getColumn(), (2.0 * ny * nx * dotNormalF2) / squaredDotNormalNormal - (ny * xf2) / dotNormalNormal);
			if (nY.getColumn() >= 0)
				J.set(y2.getColumn(), nY.getColumn(), (2.0 * ny * ny * dotNormalF2) / squaredDotNormalNormal - (ny * yf2) / dotNormalNormal + s2);
			if (nZ.getColumn() >= 0)
				J.set(y2.getColumn(), nZ.getColumn(), (2.0 * ny * nz * dotNormalF2) / squaredDotNormalNormal - (ny * zf2) / dotNormalNormal);
		}

		if (z2.getColumn() >= 0) {
			if (x2.getColumn() >= 0)
				J.set(z2.getColumn(), x2.getColumn(),     - nz * nx / dotNormalNormal);
			if (y2.getColumn() >= 0)
				J.set(z2.getColumn(), y2.getColumn(),     - nz * ny / dotNormalNormal);
			if (z2.getColumn() >= 0)
				J.set(z2.getColumn(), z2.getColumn(), 1.0 - nz * nz / dotNormalNormal);

			if (nX.getColumn() >= 0)
				J.set(z2.getColumn(), nX.getColumn(), (2.0 * nz * nx * dotNormalF2) / squaredDotNormalNormal - (nz * xf2) / dotNormalNormal);
			if (nY.getColumn() >= 0)
				J.set(z2.getColumn(), nY.getColumn(), (2.0 * nz * ny * dotNormalF2) / squaredDotNormalNormal - (nz * yf2) / dotNormalNormal);
			if (nZ.getColumn() >= 0)
				J.set(z2.getColumn(), nZ.getColumn(), (2.0 * nz * nz * dotNormalF2) / squaredDotNormalNormal - (nz * zf2) / dotNormalNormal + s2);
		}

		Matrix JDp = new DenseMatrix(nou, nou);
		J.mult(Dp, JDp);
		JDp.transBmult(J, Dp);

		xf1 = xf1 + s1 * nx;
		yf1 = yf1 + s1 * ny;
		zf1 = zf1 + s1 * nz;

		xf2 = xf2 + s2 * nx;
		yf2 = yf2 + s2 * ny;
		zf2 = zf2 + s2 * nz;
		
		x1.setValue(xf1);
		y1.setValue(yf1);
		z1.setValue(zf1);
		
		x2.setValue(xf2);
		y2.setValue(yf2);
		z2.setValue(zf2);
	}
	
	@Override
	public Collection<Restriction> getRestrictions() {
		return List.of(this.primaryFocalRestriction, this.secondaryFocalRestriction, this.vectorLengthRestriction);
	}
	
	@Override
	public Collection<UnknownParameter> getUnknownParameters() {
		return this.parameters.values();
	}

	@Override
	public UnknownParameter getUnknownParameter(ParameterType parameterType) {
		return this.parameters.get(parameterType);
	}
	
	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.parameters.get(((UnknownParameter)object).getParameterType()) == object;
	}
	
	private void init() {
		this.parameters = new LinkedHashMap<ParameterType, UnknownParameter>();
		
		this.parameters.put(ParameterType.PRIMARY_FOCAL_COORDINATE_X, new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X, true));
		this.parameters.put(ParameterType.PRIMARY_FOCAL_COORDINATE_Y, new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y, true));
		this.parameters.put(ParameterType.PRIMARY_FOCAL_COORDINATE_Z, new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Z, true));
		
		this.parameters.put(ParameterType.SECONDARY_FOCAL_COORDINATE_X, new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X, true));
		this.parameters.put(ParameterType.SECONDARY_FOCAL_COORDINATE_Y, new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y, true));
		this.parameters.put(ParameterType.SECONDARY_FOCAL_COORDINATE_Z, new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Z, true));
		
		this.parameters.put(ParameterType.MAJOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT, true));
		
		this.parameters.put(ParameterType.VECTOR_X, new UnknownParameter(ParameterType.VECTOR_X, true, 0));
		this.parameters.put(ParameterType.VECTOR_Y, new UnknownParameter(ParameterType.VECTOR_Y, true, 0));
		this.parameters.put(ParameterType.VECTOR_Z, new UnknownParameter(ParameterType.VECTOR_Z, true, 1));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));
		this.parameters.put(ParameterType.CONSTANT, new UnknownParameter(ParameterType.CONSTANT, true, 0.0, false, ProcessingType.FIXED));
		

		UnknownParameter zero = this.parameters.get(ParameterType.CONSTANT);

		UnknownParameter xFocal1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Z);

		UnknownParameter xFocal2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter yFocal2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		UnknownParameter zFocal2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Z);

		UnknownParameter normalX = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter normalY = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter normalZ = this.parameters.get(ParameterType.VECTOR_Z);
		UnknownParameter vectorLength = this.parameters.get(ParameterType.VECTOR_LENGTH);

		List<UnknownParameter> normalVector = List.of(normalX, normalY, normalZ);
		this.vectorLengthRestriction = new ProductSumRestriction(true, normalVector, normalVector, vectorLength);

		List<UnknownParameter> primaryFocalVector = List.of(xFocal1, yFocal1, zFocal1);
		this.primaryFocalRestriction = new ProductSumRestriction(true, primaryFocalVector, normalVector, zero);

		List<UnknownParameter> secondaryFocalVector = List.of(xFocal2, yFocal2, zFocal2);
		this.secondaryFocalRestriction = new ProductSumRestriction(true, secondaryFocalVector, normalVector, zero);
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.CYLINDER;
	}
}
