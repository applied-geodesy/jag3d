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

package org.applied_geodesy.adjustment.geometry.curve.primitive;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class Ellipse extends Curve {

	private Map<ParameterType, UnknownParameter> parameters = null;
	
	public Ellipse() {
		this.init();
	}

	public void setInitialGuess(double x1, double y1, double x2, double y2, double a) throws IllegalArgumentException {
		// ellipse parameters 
		UnknownParameter X1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter Y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		
		UnknownParameter X2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter Y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		
		UnknownParameter A  = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);

		// overwriting of a-priori values for parameters to be estimated (i.e. not fixed)
		if (X1.getProcessingType() == ProcessingType.ADJUSTMENT)
			X1.setValue0(x1);
		
		if (Y1.getProcessingType() == ProcessingType.ADJUSTMENT)
			Y1.setValue0(y1);
		
		
		if (X2.getProcessingType() == ProcessingType.ADJUSTMENT)
			X2.setValue0(x2);
		
		if (Y2.getProcessingType() == ProcessingType.ADJUSTMENT)
			Y2.setValue0(y2);
		
		
		if (A.getProcessingType() == ProcessingType.ADJUSTMENT)
			A.setValue0(a);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		// center of mass
		Point centerOfMass = this.getCenterOfMass();

		// reduce to center of mass
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		
		// focal points 
		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		
		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);

		double dx1 = x1.getValue() - xi;
		double dy1 = y1.getValue() - yi;
		
		double dx2 = x2.getValue() - xi;
		double dy2 = y2.getValue() - yi;
		
		double s1 = Math.hypot( dx1, dy1 );
        double s2 = Math.hypot( dx2, dy2 );
		
		if (Jx != null) {
			// major axis
			UnknownParameter a = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);

			if (x1.getColumn() >= 0)
				Jx.set(rowIndex, x1.getColumn(), dx1 / s1);
			if (y1.getColumn() >= 0)
				Jx.set(rowIndex, y1.getColumn(), dy1 / s1);
			if (x2.getColumn() >= 0)
				Jx.set(rowIndex, x2.getColumn(), dx2 / s2);
			if (y2.getColumn() >= 0)
				Jx.set(rowIndex, y2.getColumn(), dy2 / s2);
			if (a.getColumn() >= 0)
				Jx.set(rowIndex, a.getColumn(),  -2.0);
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, -dx1/s1 - dx2/s2);
			Jv.set(rowIndex, 1, -dy1/s1 - dy2/s2);
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		// center of mass
		Point centerOfMass = this.getCenterOfMass();

		// rduce to center of mass
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		
		// ellipse parameters 
		double x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X).getValue();
		double y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y).getValue();
		
		double x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X).getValue();
		double y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y).getValue();
		
		double a  = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue();
		
		double s1 = Math.hypot(xi - x1, yi - y1);
		double s2 = Math.hypot(xi - x2, yi - y2);

		return s1 + s2 - 2.0 * a;
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

		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		
		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);
		
		x1.setValue( x1.getValue() + prevCenterOfMass.getX0() - currCenterOfMass.getX0() );
		y1.setValue( y1.getValue() + prevCenterOfMass.getY0() - currCenterOfMass.getY0() );
		
		x2.setValue( x2.getValue() + prevCenterOfMass.getX0() - currCenterOfMass.getX0() );
		y2.setValue( y2.getValue() + prevCenterOfMass.getY0() - currCenterOfMass.getY0() );
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
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		Point centerOfMass = this.getCenterOfMass();

		UnknownParameter x1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_X);
		UnknownParameter y1 = this.parameters.get(ParameterType.PRIMARY_FOCAL_COORDINATE_Y);
		
		UnknownParameter x2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_X);
		UnknownParameter y2 = this.parameters.get(ParameterType.SECONDARY_FOCAL_COORDINATE_Y);

		x1.setValue( x1.getValue() + centerOfMass.getX0() );
		y1.setValue( y1.getValue() + centerOfMass.getY0() );
		
		x2.setValue( x2.getValue() + centerOfMass.getX0() );
		y2.setValue( y2.getValue() + centerOfMass.getY0() );
	}

	private void init() {		
		this.parameters = new LinkedHashMap<ParameterType, UnknownParameter>();
		
		this.parameters.put(ParameterType.PRIMARY_FOCAL_COORDINATE_X, new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_X, true));
		this.parameters.put(ParameterType.PRIMARY_FOCAL_COORDINATE_Y, new UnknownParameter(ParameterType.PRIMARY_FOCAL_COORDINATE_Y, true));
		
		this.parameters.put(ParameterType.SECONDARY_FOCAL_COORDINATE_X, new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_X, true));
		this.parameters.put(ParameterType.SECONDARY_FOCAL_COORDINATE_Y, new UnknownParameter(ParameterType.SECONDARY_FOCAL_COORDINATE_Y, true));

		this.parameters.put(ParameterType.MAJOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT, true));
	}
	
	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.ELLIPSE;
	}

	@Override
	public String toLaTex() {
		return "$\\sum_{j=1}^2 s_j = 2 a"
				+ " \\\\ "
				+ "s_j = \\vert \\mathbf{P}_i - \\mathbf{F}_j \\vert$";
	}
}
