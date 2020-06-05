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

public class QuadraticSurface extends Surface {
	private Map<ParameterType, UnknownParameter> parameters;
	private final static double SQRT2 = Math.sqrt(2.0);
	
	private ProductSumRestriction normalizeRestriction;

	public QuadraticSurface() {
		this.init();
	}
	
	public void setInitialGuess(double a, double b, double c, double d, double e, double f, double g, double h, double i, double length) throws IllegalArgumentException {
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).setValue0(a);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).setValue0(b);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).setValue0(c);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D).setValue0(d);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E).setValue0(e);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F).setValue0(f);
		
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_G).setValue0(g);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_H).setValue0(h);
		this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_I).setValue0(i);
		
		this.parameters.get(ParameterType.LENGTH).setValue0(length);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();
		
		UnknownParameter a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		UnknownParameter d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);
		UnknownParameter f = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F);
		
		UnknownParameter g = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_G);
		UnknownParameter h = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_H);
		UnknownParameter i = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_I);
		
		UnknownParameter length = this.parameters.get(ParameterType.LENGTH);
		
		if (Jx != null) {
			if (a.getColumn() >= 0)
				Jx.set(rowIndex, a.getColumn(), xi * xi);
			if (b.getColumn() >= 0)
				Jx.set(rowIndex, b.getColumn(), yi * yi);
			if (c.getColumn() >= 0)
				Jx.set(rowIndex, c.getColumn(), zi * zi);
			if (d.getColumn() >= 0)
				Jx.set(rowIndex, d.getColumn(), SQRT2 * xi * yi);
			if (e.getColumn() >= 0)
				Jx.set(rowIndex, e.getColumn(), SQRT2 * xi * zi);
			if (f.getColumn() >= 0)
				Jx.set(rowIndex, f.getColumn(), SQRT2 * yi * zi);
			
			if (g.getColumn() >= 0)
				Jx.set(rowIndex, g.getColumn(), xi);
			if (h.getColumn() >= 0)
				Jx.set(rowIndex, h.getColumn(), yi);
			if (i.getColumn() >= 0)
				Jx.set(rowIndex, i.getColumn(), zi);
			
			if (length.getColumn() >= 0)
				Jx.set(rowIndex, length.getColumn(), 1.0);
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, 2.0 * a.getValue() * xi + d.getValue() * SQRT2 * yi + e.getValue() * SQRT2 * zi + g.getValue());
			Jv.set(rowIndex, 1, 2.0 * b.getValue() * yi + d.getValue() * SQRT2 * xi + f.getValue() * SQRT2 * zi + h.getValue());
			Jv.set(rowIndex, 2, 2.0 * c.getValue() * zi + e.getValue() * SQRT2 * xi + f.getValue() * SQRT2 * yi + i.getValue());
		}
	}
	
	@Override
	public double getMisclosure(FeaturePoint point) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		double zi = point.getZ() - centerOfMass.getZ0();
		
		double a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue();
		double b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue();
		double c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue();
		double d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D).getValue();
		double e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E).getValue();
		double f = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F).getValue();
		
		double g = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_G).getValue();
		double h = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_H).getValue();
		double i = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_I).getValue();
		
		double length = this.parameters.get(ParameterType.LENGTH).getValue();
		
		return a * xi*xi + b * yi*yi + c * zi*zi + d * SQRT2 * xi*yi + e * SQRT2 * xi*zi + f * SQRT2 * yi*zi + g * xi + h * yi + i * zi + length;
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
		
		UnknownParameter G = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_G);
		UnknownParameter H = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_H);
		UnknownParameter I = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_I);
		
		UnknownParameter Length = this.parameters.get(ParameterType.LENGTH);
		
		double a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue();
		double b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue();
		double c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue();
		double d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D).getValue();
		double e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E).getValue();
		double f = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F).getValue();
		
		// remove prev center of mass
		double x0 = prevCenterOfMass.getX0();
		double y0 = prevCenterOfMass.getY0();
		double z0 = prevCenterOfMass.getZ0();

		double g = G.getValue() - 2.0 * (a * x0 + d / SQRT2 * y0 + e / SQRT2 * z0);
		double h = H.getValue() - 2.0 * (d / SQRT2 * x0 + b * y0 + f / SQRT2 * z0);
		double i = I.getValue() - 2.0 * (e / SQRT2 * x0 + f / SQRT2 * y0 + c * z0);
		double length = Length.getValue() - (a * x0*x0 + b * y0*y0 + c * z0*z0 + d * SQRT2 * x0*y0 + e * SQRT2 * x0*z0 + f * SQRT2 * y0*z0 + g * x0 + h * y0 + i * z0);
		
		// set new center of mass
		x0 = currCenterOfMass.getX0();
		y0 = currCenterOfMass.getY0();
		z0 = currCenterOfMass.getZ0();
		
		length = length + (a * x0*x0 + b * y0*y0 + c * z0*z0 + d * SQRT2 * x0*y0 + e * SQRT2 * x0*z0 + f * SQRT2 * y0*z0 + g * x0 + h * y0 + i * z0);
		g = g + 2.0 * (a * x0 + d / SQRT2 * y0 + e / SQRT2 * z0);
		h = h + 2.0 * (d / SQRT2 * x0 + b * y0 + f / SQRT2 * z0);
		i = i + 2.0 * (e / SQRT2 * x0 + f / SQRT2 * y0 + c * z0);
				
		G.setValue(g);
		H.setValue(h);
		I.setValue(i);
		Length.setValue(length);
	}
	
	@Override
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		int nou = Dp.numColumns();
		Point centerOfMass = this.getCenterOfMass();
		
		UnknownParameter a = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter b = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter c = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		UnknownParameter d = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter e = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);
		UnknownParameter f = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F);
		
		UnknownParameter g = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_G);
		UnknownParameter h = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_H);
		UnknownParameter i = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_I);
		
		UnknownParameter length = this.parameters.get(ParameterType.LENGTH);
		
		double x0 = centerOfMass.getX0();
		double y0 = centerOfMass.getY0();
		double z0 = centerOfMass.getZ0();
		
		
		Matrix J = Matrices.identity(nou);
		if (g.getColumn() >= 0) {
			if (a.getColumn() >= 0)
				J.set(g.getColumn(), a.getColumn(), -2.0 * x0);
			if (d.getColumn() >= 0)
				J.set(g.getColumn(), d.getColumn(), -2.0 / SQRT2 * y0);
			if (e.getColumn() >= 0)
				J.set(g.getColumn(), e.getColumn(), -2.0 / SQRT2 * z0);
			J.set(g.getColumn(), g.getColumn(),  1.0);
		}
		if (h.getColumn() >= 0) {
			if (d.getColumn() >= 0)
				J.set(h.getColumn(), d.getColumn(), -2.0 / SQRT2 * x0);
			if (b.getColumn() >= 0)
				J.set(h.getColumn(), b.getColumn(), -2.0 * y0);
			if (f.getColumn() >= 0)
				J.set(h.getColumn(), f.getColumn(), -2.0 / SQRT2 * z0);
			J.set(h.getColumn(), h.getColumn(),  1.0);
		} 
		if (i.getColumn() >= 0) {
			if (d.getColumn() >= 0)
				J.set(i.getColumn(), e.getColumn(), -2.0 / SQRT2 * x0);
			if (b.getColumn() >= 0)
				J.set(i.getColumn(), f.getColumn(), -2.0 / SQRT2 * y0);
			if (f.getColumn() >= 0)
				J.set(i.getColumn(), c.getColumn(), -2.0 * z0);
			J.set(i.getColumn(), i.getColumn(),  1.0);
		}	
		if (length.getColumn() >= 0) {
			if (a.getColumn() >= 0)
				J.set(length.getColumn(), a.getColumn(),  x0*x0);
			if (b.getColumn() >= 0)
				J.set(length.getColumn(), b.getColumn(),  y0*y0);
			if (c.getColumn() >= 0)
				J.set(length.getColumn(), c.getColumn(),  z0*z0);
			if (d.getColumn() >= 0)
				J.set(length.getColumn(), d.getColumn(),  SQRT2 * x0*y0);
			if (e.getColumn() >= 0)
				J.set(length.getColumn(), e.getColumn(),  SQRT2 * x0*z0);
			if (f.getColumn() >= 0)
				J.set(length.getColumn(), f.getColumn(),  SQRT2 * y0*z0);
			if (g.getColumn() >= 0)
				J.set(length.getColumn(), g.getColumn(), -x0);
			if (h.getColumn() >= 0)
				J.set(length.getColumn(), h.getColumn(), -y0);
			if (i.getColumn() >= 0)
				J.set(length.getColumn(), i.getColumn(), -z0);
			J.set(length.getColumn(), length.getColumn(), 1.0);
		}

		Matrix JDp = new DenseMatrix(nou, nou);
		J.mult(Dp, JDp);
		JDp.transBmult(J, Dp);
		
		length.setValue(length.getValue() + (a.getValue() * x0*x0 + b.getValue() * y0*y0 + c.getValue() * z0*z0 + d.getValue() * SQRT2 * x0*y0 + e.getValue() * SQRT2 * x0*z0 + f.getValue() * SQRT2 * y0*z0 - g.getValue() * x0 - h.getValue() * y0 - i.getValue() * z0));
		g.setValue(g.getValue() - 2.0 * (a.getValue() * x0 + d.getValue() / SQRT2 * y0 + e.getValue() / SQRT2 * z0));
		h.setValue(h.getValue() - 2.0 * (d.getValue() / SQRT2 * x0 + b.getValue() * y0 + f.getValue() / SQRT2 * z0));
		i.setValue(i.getValue() - 2.0 * (e.getValue() / SQRT2 * x0 + f.getValue() / SQRT2 * y0 + c.getValue() * z0));
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
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_F, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_F, true));
		
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_G, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_G, true));
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_H, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_H, true));
		this.parameters.put(ParameterType.POLYNOMIAL_COEFFICIENT_I, new UnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_I, true));
		
		this.parameters.put(ParameterType.LENGTH, new UnknownParameter(ParameterType.LENGTH, true));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));

		UnknownParameter A = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_A);
		UnknownParameter B = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_B);
		UnknownParameter C = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_C);
		
		UnknownParameter D = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_D);
		UnknownParameter E = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_E);
		UnknownParameter F = this.parameters.get(ParameterType.POLYNOMIAL_COEFFICIENT_F);
		
		UnknownParameter one = this.parameters.get(ParameterType.VECTOR_LENGTH);
			
		List<UnknownParameter> quadraticCoeficients = List.of(A, B, C, D, E, F);

		this.normalizeRestriction = new ProductSumRestriction(true, quadraticCoeficients, quadraticCoeficients, one);
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.QUADRATIC_SURFACE;
	}
}