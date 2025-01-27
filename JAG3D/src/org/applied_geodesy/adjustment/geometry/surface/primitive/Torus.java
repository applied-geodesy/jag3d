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

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class Torus extends Surface {
	private Map<ParameterType, UnknownParameter> parameters;
	
	private ProductSumRestriction vectorLengthRestriction;
	
	public Torus() {
		this.init();
	}
	
	public void setInitialGuess(double x0, double y0, double z0, double nx, double ny, double nz, double a, double c) throws IllegalArgumentException {
		// torus parameters
		UnknownParameter X0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter Y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter Z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);
		
		UnknownParameter Nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter Ny = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter Nz = this.parameters.get(ParameterType.VECTOR_Z);
		
		UnknownParameter A = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);
		UnknownParameter C = this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT);
		
		// overwriting of a-priori values for parameters to be estimated (i.e. not fixed)
		if (X0.getProcessingType() == ProcessingType.ADJUSTMENT)
			X0.setValue0(x0);
		
		if (Y0.getProcessingType() == ProcessingType.ADJUSTMENT)
			Y0.setValue0(y0);
		
		if (Z0.getProcessingType() == ProcessingType.ADJUSTMENT)
			Z0.setValue0(z0);		
		
		
		if (Nx.getProcessingType() == ProcessingType.ADJUSTMENT)
			Nx.setValue0(nx);
		
		if (Ny.getProcessingType() == ProcessingType.ADJUSTMENT)
			Ny.setValue0(ny);
		
		if (Nz.getProcessingType() == ProcessingType.ADJUSTMENT)
			Nz.setValue0(nz);
		
		
		if (A.getProcessingType() == ProcessingType.ADJUSTMENT)
			A.setValue0(a);
		
		if (C.getProcessingType() == ProcessingType.ADJUSTMENT)
			C.setValue0(c);
	}

	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		// center of mass
		Point centerOfMass = this.getCenterOfMass();

		// reduce to center of mass
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();

		// torus parameters 
		UnknownParameter x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);
		
		UnknownParameter nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter ny = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter nz = this.parameters.get(ParameterType.VECTOR_Z);
		
		UnknownParameter a = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);
		UnknownParameter c = this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT);
		
		double dx = xi - x0.getValue();
		double dy = yi - y0.getValue();
		double dz = zi - z0.getValue();
		
		double aa = a.getValue() * a.getValue();
		double cc = c.getValue() * c.getValue();

		double g   = nx.getValue() * dx + ny.getValue() * dy + nz.getValue() * dz;
		double dd  = dx*dx + dy*dy + dz*dz;
		
        double dac = dd + aa - cc;
        double g2  = 2.0 * g;
        double aa4 = 4.0 * aa;

		if (Jx != null) {
			if (x0.getColumn() >= 0)
				Jx.set(rowIndex, x0.getColumn(), aa4 * (2.0*dx - g2*nx.getValue()) - 4.0*dac*dx);
			if (y0.getColumn() >= 0)
				Jx.set(rowIndex, y0.getColumn(), aa4 * (2.0*dy - g2*ny.getValue()) - 4.0*dac*dy);
			if (z0.getColumn() >= 0)
				Jx.set(rowIndex, z0.getColumn(), aa4 * (2.0*dz - g2*nz.getValue()) - 4.0*dac*dz);
					
			if (nx.getColumn() >= 0)
				Jx.set(rowIndex, nx.getColumn(), aa4*g2*dx);
			if (ny.getColumn() >= 0)
				Jx.set(rowIndex, ny.getColumn(), aa4*g2*dy);
			if (nz.getColumn() >= 0)
				Jx.set(rowIndex, nz.getColumn(), aa4*g2*dz);
			
			if (a.getColumn() >= 0)
				Jx.set(rowIndex, a.getColumn(),  4.0 * (g*g2 + dac - 2.0*dd) * a.getValue());
			if (c.getColumn() >= 0)
				Jx.set(rowIndex, c.getColumn(), -4.0 * dac * c.getValue());
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, 4.0*dac*dx - aa4*(2.0*dx - g2*nx.getValue()));
			Jv.set(rowIndex, 1, 4.0*dac*dy - aa4*(2.0*dy - g2*ny.getValue()));
			Jv.set(rowIndex, 2, 4.0*dac*dz - aa4*(2.0*dz - g2*nz.getValue()));
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		// center of mass
		Point centerOfMass = this.getCenterOfMass();

		// reduce to center of mass
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();

		// torus parameters 
		double x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
		double y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
		double z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();

		double nx = this.parameters.get(ParameterType.VECTOR_X).getValue();
		double ny = this.parameters.get(ParameterType.VECTOR_Y).getValue();
		double nz = this.parameters.get(ParameterType.VECTOR_Z).getValue();

		double a = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue();
		double c = this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT).getValue();
		
		double dx = xi - x0;
		double dy = yi - y0;
		double dz = zi - z0;
		
		double u = nz * dy - ny * dz;
		double v = nx * dz - nz * dx;
		double w = ny * dx - nx * dy;
		
		double ff  = u*u + v*v + w*w;
		double dd  = dx*dx + dy*dy + dz*dz;
		double dac = dd + a*a - c*c;
		
		return dac*dac - 4*a*a*ff;
	}
	
	@Override
	public void setCenterOfMass(Point centerOfMass) {
		// get previous center of mass
		Point prevCenterOfMass = this.getCenterOfMass();

		// check, if components are equal to previous point to avoid unnecessary operations
		boolean equalComponents = centerOfMass.equalsCoordinateComponents(prevCenterOfMass);
		super.setCenterOfMass(centerOfMass);
		if (equalComponents)
			return;

		// get current center of mass
		Point currCenterOfMass = this.getCenterOfMass();
		
		UnknownParameter x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);

		x0.setValue( x0.getValue() + prevCenterOfMass.getX0() - currCenterOfMass.getX0() );
		y0.setValue( y0.getValue() + prevCenterOfMass.getY0() - currCenterOfMass.getY0() );
		z0.setValue( z0.getValue() + prevCenterOfMass.getZ0() - currCenterOfMass.getZ0() );
	}
	
	@Override
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		Point centerOfMass = this.getCenterOfMass();

		UnknownParameter x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);
		
		x0.setValue( x0.getValue() + centerOfMass.getX0() );
		y0.setValue( y0.getValue() + centerOfMass.getY0() );
		z0.setValue( z0.getValue() + centerOfMass.getZ0() );
	}
	
	@Override
	public Collection<Restriction> getRestrictions() {
		return List.of(this.vectorLengthRestriction);
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
		this.parameters.put(ParameterType.ORIGIN_COORDINATE_X, new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, true));
		this.parameters.put(ParameterType.ORIGIN_COORDINATE_Y, new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, true));
		this.parameters.put(ParameterType.ORIGIN_COORDINATE_Z, new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Z, true));
		
		this.parameters.put(ParameterType.VECTOR_X, new UnknownParameter(ParameterType.VECTOR_X, true, 0));
		this.parameters.put(ParameterType.VECTOR_Y, new UnknownParameter(ParameterType.VECTOR_Y, true, 0));
		this.parameters.put(ParameterType.VECTOR_Z, new UnknownParameter(ParameterType.VECTOR_Z, true, 1));
		
		this.parameters.put(ParameterType.MAJOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT, true));
		this.parameters.put(ParameterType.MINOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT, true));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));
		
		UnknownParameter normalX = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter normalY = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter normalZ = this.parameters.get(ParameterType.VECTOR_Z);
		UnknownParameter vectorLength = this.parameters.get(ParameterType.VECTOR_LENGTH);

		List<UnknownParameter> normalVector = List.of(normalX, normalY, normalZ);
		this.vectorLengthRestriction = new ProductSumRestriction(true, normalVector, normalVector, vectorLength);
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.TORUS;
	}
	
	@Override
	public String toLaTex() {
		return "$(f_i - a)^2 + g_i^2 = c^2"
				+ " \\\\  \\\\ "
				+ "g_i = \\mathbf{n^\\mathrm{T}} \\left(\\mathbf{P}_i - \\mathbf{P}_0\\right)"
				+ " \\\\ "
				+ "f_i = \\sqrt{\\vert \\mathbf{P}_i - \\mathbf{P}_0 \\vert^2 - g^2}"
				+ " \\\\ \\\\ "
				+ "\\mathbf{P}_0 = \\left( \\begin{array}{c} x_0 \\\\ y_0 \\\\ z_0 \\end{array} \\right), \\mathbf{n} = \\left( \\begin{array}{c} n_x \\\\ n_y \\\\ n_z \\end{array} \\right)$";
	}
}