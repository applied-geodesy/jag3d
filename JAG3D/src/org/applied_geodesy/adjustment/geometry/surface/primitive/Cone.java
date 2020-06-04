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

public class Cone extends Surface {
	private Map<ParameterType, UnknownParameter> parameters;
	
	private ProductSumRestriction crossXYRestriction;
	private ProductSumRestriction crossXZRestriction;
	private ProductSumRestriction crossYZRestriction;
	
	private ProductSumRestriction xNormalRestriction;
	private ProductSumRestriction yNormalRestriction;
	private ProductSumRestriction zNormalRestriction;

	public Cone() {
		this.init();
	}
	
	public void setInitialGuess(double x0, double y0, double z0, double a, double c, double r11, double r12, double r13, double r21, double r22, double r23, double r31, double r32, double r33) throws IllegalArgumentException {
		this.parameters.get(ParameterType.ORIGIN_COORDINATE_X).setValue0(x0);
		this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y).setValue0(y0);
		this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z).setValue0(z0);
		
		this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).setValue0(a);
		this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT).setValue0(c);
		
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R11).setValue0(r11);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R12).setValue0(r12);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R13).setValue0(r13);
		
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R21).setValue0(r21);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R22).setValue0(r22);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R23).setValue0(r23);
		
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R31).setValue0(r31);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R32).setValue0(r32);
		this.parameters.get(ParameterType.ROTATION_COMPONENT_R33).setValue0(r33);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();
		
		UnknownParameter X0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_X);
		UnknownParameter Y0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Y);
		UnknownParameter Z0 = this.parameters.get(ParameterType.ORIGIN_COORDINATE_Z);
		
		UnknownParameter A = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT);
		UnknownParameter C = this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT);
		
		UnknownParameter R11 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R11);
		UnknownParameter R12 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R12);
		UnknownParameter R13 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R13);
		
		UnknownParameter R21 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R21);
		UnknownParameter R22 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R22);
		UnknownParameter R23 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R23);
		
		UnknownParameter R31 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R31);
		UnknownParameter R32 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R32);
		UnknownParameter R33 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R33);
		
		
		double x0 = X0.getValue();
		double y0 = Y0.getValue();
		double z0 = Z0.getValue();
		
		double a = A.getValue();
		double c = C.getValue();
		
		double r11 = R11.getValue();
		double r12 = R12.getValue();
		double r13 = R13.getValue();
		
		double r21 = R21.getValue();
		double r22 = R22.getValue();
		double r23 = R23.getValue();
		
		double r31 = R31.getValue();
		double r32 = R32.getValue();
		double r33 = R33.getValue();
		
		double ui = r11 * (xi - x0) + r12 * (yi - y0) + r13 * (zi - z0);
		double vi = r21 * (xi - x0) + r22 * (yi - y0) + r23 * (zi - z0);
		double wi = r31 * (xi - x0) + r32 * (yi - y0) + r33 * (zi - z0);

		if (Jx != null) {
			if (X0.getColumn() >= 0)
				Jx.set(rowIndex, X0.getColumn(), -2.0 * (r11 * ui * a*a  +  r21 * vi * c*c  -  r31 * wi));
			if (Y0.getColumn() >= 0)
				Jx.set(rowIndex, Y0.getColumn(), -2.0 * (r12 * ui * a*a  +  r22 * vi * c*c  -  r32 * wi));
			if (Z0.getColumn() >= 0)
				Jx.set(rowIndex, Z0.getColumn(), -2.0 * (r13 * ui * a*a  +  r23 * vi * c*c  -  r33 * wi));
			
			if (A.getColumn() >= 0)
				Jx.set(rowIndex, A.getColumn(),  2.0 * a * ui * ui);
			if (C.getColumn() >= 0)
				Jx.set(rowIndex, C.getColumn(),  2.0 * c * vi * vi);
			
			if (R11.getColumn() >= 0)
				Jx.set(rowIndex, R11.getColumn(), 2.0 * a*a * ui * (xi - x0));
			if (R12.getColumn() >= 0)
				Jx.set(rowIndex, R12.getColumn(), 2.0 * a*a * ui * (yi - y0));
			if (R13.getColumn() >= 0)
				Jx.set(rowIndex, R13.getColumn(), 2.0 * a*a * ui * (zi - z0));
			
			if (R21.getColumn() >= 0)
				Jx.set(rowIndex, R21.getColumn(), 2.0 * c*c * vi * (xi - x0));
			if (R22.getColumn() >= 0)
				Jx.set(rowIndex, R22.getColumn(), 2.0 * c*c * vi * (yi - y0));
			if (R23.getColumn() >= 0)
				Jx.set(rowIndex, R23.getColumn(), 2.0 * c*c * vi * (zi - z0));
			
			if (R31.getColumn() >= 0)
				Jx.set(rowIndex, R31.getColumn(), -2.0 * wi * (xi - x0));
			if (R32.getColumn() >= 0)
				Jx.set(rowIndex, R32.getColumn(), -2.0 * wi * (yi - y0));
			if (R33.getColumn() >= 0)
				Jx.set(rowIndex, R33.getColumn(), -2.0 * wi * (zi - z0));			
		}
		
		if (Jv != null) {
			Jv.set(rowIndex, 0, 2.0 * (r11 * ui * a*a  +  r21 * vi * c*c  -  r31 * wi));
			Jv.set(rowIndex, 1, 2.0 * (r12 * ui * a*a  +  r22 * vi * c*c  -  r32 * wi));
			Jv.set(rowIndex, 2, 2.0 * (r13 * ui * a*a  +  r23 * vi * c*c  -  r33 * wi));
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
		
		double a = this.parameters.get(ParameterType.MAJOR_AXIS_COEFFICIENT).getValue();
		double c = this.parameters.get(ParameterType.MINOR_AXIS_COEFFICIENT).getValue();
		
		double r11 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R11).getValue();
		double r12 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R12).getValue();
		double r13 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R13).getValue();
		
		double r21 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R21).getValue();
		double r22 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R22).getValue();
		double r23 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R23).getValue();
		
		double r31 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R31).getValue();
		double r32 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R32).getValue();
		double r33 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R33).getValue();
		
		double ui = r11 * (xi - x0) + r12 * (yi - y0) + r13 * (zi - z0);
		double vi = r21 * (xi - x0) + r22 * (yi - y0) + r23 * (zi - z0);
		double wi = r31 * (xi - x0) + r32 * (yi - y0) + r33 * (zi - z0);
		
		return a * a * ui * ui + c * c * vi * vi - wi * wi; 
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
		return List.of(
				this.crossXYRestriction,
				this.crossXZRestriction,
				this.crossYZRestriction,
				this.xNormalRestriction,
				this.yNormalRestriction,
				this.zNormalRestriction
		);
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
		
		this.parameters.put(ParameterType.MAJOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MAJOR_AXIS_COEFFICIENT, true));
		this.parameters.put(ParameterType.MINOR_AXIS_COEFFICIENT, new UnknownParameter(ParameterType.MINOR_AXIS_COEFFICIENT, true));

		this.parameters.put(ParameterType.ROTATION_COMPONENT_R11, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R11, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R12, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R12, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R13, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R13, true));
		
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R21, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R21, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R22, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R22, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R23, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R23, true));
		
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R31, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R31, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R32, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R32, true));
		this.parameters.put(ParameterType.ROTATION_COMPONENT_R33, new UnknownParameter(ParameterType.ROTATION_COMPONENT_R33, true));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, false, ProcessingType.FIXED));
		this.parameters.put(ParameterType.CONSTANT, new UnknownParameter(ParameterType.CONSTANT, true, 0.0, false, ProcessingType.FIXED));

		UnknownParameter R11 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R11);
		UnknownParameter R12 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R12);
		UnknownParameter R13 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R13);
		
		UnknownParameter R21 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R21);
		UnknownParameter R22 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R22);
		UnknownParameter R23 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R23);
		
		UnknownParameter R31 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R31);
		UnknownParameter R32 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R32);
		UnknownParameter R33 = this.parameters.get(ParameterType.ROTATION_COMPONENT_R33);
		
		UnknownParameter zero = this.parameters.get(ParameterType.CONSTANT);
		UnknownParameter one  = this.parameters.get(ParameterType.VECTOR_LENGTH);
			
		List<UnknownParameter> xNormalVector = List.of(R11, R12, R13);
		List<UnknownParameter> yNormalVector = List.of(R21, R22, R23);
		List<UnknownParameter> zNormalVector = List.of(R31, R32, R33);
		
		this.xNormalRestriction = new ProductSumRestriction(true, xNormalVector, xNormalVector, one);
		this.yNormalRestriction = new ProductSumRestriction(true, yNormalVector, yNormalVector, one);
		this.zNormalRestriction = new ProductSumRestriction(true, zNormalVector, zNormalVector, one);
		
		this.crossXYRestriction = new ProductSumRestriction(true, xNormalVector, yNormalVector, zero);
		this.crossXZRestriction = new ProductSumRestriction(true, xNormalVector, zNormalVector, zero);
		this.crossYZRestriction = new ProductSumRestriction(true, yNormalVector, zNormalVector, zero);
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.CONE;
	}
}