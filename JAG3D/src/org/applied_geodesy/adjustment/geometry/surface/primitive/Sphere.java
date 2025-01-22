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
import java.util.Map;

import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;


public class Sphere extends Surface {
	private Map<ParameterType, UnknownParameter> parameters;
	
	public Sphere() {
		this.init();
	}

	public void setInitialGuess(double x0, double y0, double z0, double r0) throws IllegalArgumentException {
		// sphere parameters 
		UnknownParameter X0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter Y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter Z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);
		UnknownParameter R0 = this.parameters.get(ParameterType.RADIUS);
		
		// overwriting of a-priori values for parameters to be estimated (i.e. not fixed)
		if (X0.getProcessingType() == ProcessingType.ADJUSTMENT)
			X0.setValue0(x0);
		
		if (Y0.getProcessingType() == ProcessingType.ADJUSTMENT)
			Y0.setValue0(y0);
		
		if (Z0.getProcessingType() == ProcessingType.ADJUSTMENT)
			Z0.setValue0(z0);
		
		if (R0.getProcessingType() == ProcessingType.ADJUSTMENT)
			R0.setValue0(r0);		
	}
		
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		//Schwerpunktreduktion
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();

		// Parameter der Kugel
		UnknownParameter x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);

		if (Jx != null) {
			UnknownParameter r  = this.parameters.get(ParameterType.RADIUS);
			if (x0.getColumn() >= 0)
				Jx.set(rowIndex, x0.getColumn(), -2.0 * (xi - x0.getValue()));
			if (y0.getColumn() >= 0)
				Jx.set(rowIndex, y0.getColumn(), -2.0 * (yi - y0.getValue()));
			if (z0.getColumn() >= 0)
				Jx.set(rowIndex, z0.getColumn(), -2.0 * (zi - z0.getValue()));
			if (r.getColumn() >= 0)
				Jx.set(rowIndex, r.getColumn(),  -2.0 * r.getValue());
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, 2.0 * (xi - x0.getValue()));
			Jv.set(rowIndex, 1, 2.0 * (yi - y0.getValue()));
			Jv.set(rowIndex, 2, 2.0 * (zi - z0.getValue()));
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();

		double x0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
		double y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
		double z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();
		double r  = this.parameters.get(ParameterType.RADIUS).getValue();
				
		return ((xi-x0)*(xi-x0) + (yi-y0)*(yi-y0) + (zi-z0)*(zi-z0)) - r*r;
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
		this.parameters.put(ParameterType.RADIUS, new UnknownParameter(ParameterType.RADIUS, true));
	}
	
	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.SPHERE;
	}
	
	@Override
	public String toLaTex() {
		return "$\\vert \\mathbf{P}_i - \\mathbf{P}_0 \\vert = r"
				+ " \\\\ "
				+ "\\mathbf{P}_0 = \\left( \\begin{array}{c} x_0 \\\\ y_0 \\\\ z_0 \\end{array} \\right)$";
	}
}
