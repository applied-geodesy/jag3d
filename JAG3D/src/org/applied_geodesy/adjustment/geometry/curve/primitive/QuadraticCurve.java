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

public class QuadraticCurve extends Curve {
	private Map<ParameterType, UnknownParameter> parameters;
	private final static double SQRT2 = Math.sqrt(2.0);
	
	private ProductSumRestriction normalizeRestriction;

	public QuadraticCurve() {
		this.init();
	}
	
	public void setInitialGuess(double a, double b, double c, double d, double e, double length) throws IllegalArgumentException {
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).setValue0(a);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).setValue0(b);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).setValue0(c);
		
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D).setValue0(d);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E).setValue0(e);

		this.parameters.get(ParameterType.LENGTH).setValue0(length);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		
		UnknownParameter a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		
		UnknownParameter d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);

		UnknownParameter length = this.parameters.get(ParameterType.LENGTH);
		
		if (Jx != null) {
			if (a.getColumn() >= 0)
				Jx.set(rowIndex, a.getColumn(), xi * xi);
			if (b.getColumn() >= 0)
				Jx.set(rowIndex, b.getColumn(), yi * yi);
			if (c.getColumn() >= 0)
				Jx.set(rowIndex, c.getColumn(), SQRT2 * xi * yi);
			
			if (d.getColumn() >= 0)
				Jx.set(rowIndex, d.getColumn(), xi);
			if (e.getColumn() >= 0)
				Jx.set(rowIndex, e.getColumn(), yi);
			
			if (length.getColumn() >= 0)
				Jx.set(rowIndex, length.getColumn(), 1.0);
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, 2.0 * a.getValue() * xi + c.getValue() * SQRT2 * yi + d.getValue());
			Jv.set(rowIndex, 1, 2.0 * b.getValue() * yi + c.getValue() * SQRT2 * xi + e.getValue());
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		
		double a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue();
		double b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue();
		double c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue();
		
		double d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D).getValue();
		double e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E).getValue();
		
		double length = this.parameters.get(ParameterType.LENGTH).getValue();
		
		return a * xi*xi + b * yi*yi + c * SQRT2 * xi*yi + d * xi + e * yi + length;
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
		
		UnknownParameter D = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter E = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);
		
		UnknownParameter Length = this.parameters.get(ParameterType.LENGTH);
		
		double a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue();
		double b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue();
		double c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue();
				
		// remove prev center of mass
		double x0 = prevCenterOfMass.getX0();
		double y0 = prevCenterOfMass.getY0();

		double d = D.getValue() - 2.0 * (a * x0 + c / SQRT2 * y0);
		double e = E.getValue() - 2.0 * (c / SQRT2 * x0 + b * y0);
		double length = Length.getValue() - (a * x0*x0 + b * y0*y0 + c * SQRT2 * x0*y0 + d * x0 + e * y0);
		
		// set new center of mass
		x0 = currCenterOfMass.getX0();
		y0 = currCenterOfMass.getY0();
		
		length = length + (a * x0*x0 + b * y0*y0 + c * SQRT2 * x0*y0 + d * x0 + e * y0);
		d = d + 2.0 * (a * x0 + c / SQRT2 * y0);
		e = e + 2.0 * (c / SQRT2 * x0 + b * y0);
				
		D.setValue(d);
		E.setValue(e);
		Length.setValue(length);
	}
	
	@Override
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		Point centerOfMass = this.getCenterOfMass();
		
		UnknownParameter a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		
		UnknownParameter d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);

		UnknownParameter length = this.parameters.get(ParameterType.LENGTH);
		
		double x0 = centerOfMass.getX0();
		double y0 = centerOfMass.getY0();
		
		if (Dp != null) {
			int nou = Dp.numColumns();
			Matrix J = Matrices.identity(nou);
			if (d.getColumn() >= 0) {
				if (a.getColumn() >= 0)
					J.set(d.getColumn(), a.getColumn(), -2.0 * x0);
				if (c.getColumn() >= 0)
					J.set(d.getColumn(), c.getColumn(), -2.0 / SQRT2 * y0);
				J.set(d.getColumn(), d.getColumn(),  1.0);
			}
			if (e.getColumn() >= 0) {
				if (c.getColumn() >= 0)
					J.set(e.getColumn(), c.getColumn(), -2.0 / SQRT2 * x0);
				if (b.getColumn() >= 0)
					J.set(e.getColumn(), b.getColumn(), -2.0 * y0);
				J.set(e.getColumn(), e.getColumn(),  1.0);
			} 
			if (length.getColumn() >= 0) {
				if (a.getColumn() >= 0)
					J.set(length.getColumn(), a.getColumn(),  x0*x0);
				if (b.getColumn() >= 0)
					J.set(length.getColumn(), b.getColumn(),  y0*y0);
				if (c.getColumn() >= 0)
					J.set(length.getColumn(), c.getColumn(),  SQRT2 * x0*y0);
				if (d.getColumn() >= 0)
					J.set(length.getColumn(), d.getColumn(), -x0);
				if (e.getColumn() >= 0)
					J.set(length.getColumn(), e.getColumn(), -y0);
				J.set(length.getColumn(), length.getColumn(), 1.0);
			}

			Matrix JDp = new DenseMatrix(nou, nou);
			J.mult(Dp, JDp);
			JDp.transBmult(J, Dp);
		}
		
		length.setValue(length.getValue() + a.getValue() * x0*x0 + b.getValue() * y0*y0 + c.getValue() * SQRT2 * x0*y0 - d.getValue() * x0 - e.getValue() * y0);
		d.setValue(d.getValue() - 2.0 * (a.getValue() * x0 + c.getValue() / SQRT2 * y0));
		e.setValue(e.getValue() - 2.0 * (c.getValue() / SQRT2 * x0 + b.getValue() * y0));
	}
	
	@Override
	public Collection<Restriction> getRestrictions() {
		return List.of(this.normalizeRestriction);
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

		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_A, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_A, true));
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_B, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_B, true));
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_C, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_C, true));
		
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_D, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_D, true));
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_E, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_E, true));
		
		this.parameters.put(ParameterType.LENGTH, new UnknownParameter(ParameterType.LENGTH, true));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));

		UnknownParameter A = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter B = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter C = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		
		UnknownParameter one = this.parameters.get(ParameterType.VECTOR_LENGTH);
			
		List<UnknownParameter> quadraticCoeficients = List.of(A, B, C);

		this.normalizeRestriction = new ProductSumRestriction(true, quadraticCoeficients, quadraticCoeficients, one);
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.QUADRATIC_CURVE;
	}

	@Override
	public String toLaTex() {
		return "$\\mathbf{P}^\\mathrm{T}_i \\mathbf{U} \\mathbf{P}_i + \\mathbf{P}^\\mathrm{T}_i \\mathbf{u} + u_0 = 0"
				+ " \\\\ "
				+ "\\mathbf{U} = \\left( \\begin{array}{cc} a & \\frac{c}{\\sqrt{2}} \\\\ \\frac{c}{\\sqrt{2}} & b \\end{array} \\right);"
				+ " \\\\ "
				+ "\\mathbf{u} = \\left( \\begin{array}{c} d \\\\ e \\end{array} \\right)$";
	}
}