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

public class Line extends Curve {

	private Map<ParameterType, UnknownParameter> parameters = null;
	private ProductSumRestriction vectorLengthRestriction;
	
	public Line() {
		this.init();
	}

	public void setInitialGuess(double nx, double ny, double d, double l) throws IllegalArgumentException {
		// line parameters 
		UnknownParameter Nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter Ny = this.parameters.get(ParameterType.VECTOR_Y);
		
		UnknownParameter D0 = this.parameters.get(ParameterType.LENGTH);
		UnknownParameter L0 = this.parameters.get(ParameterType.VECTOR_LENGTH);
		
		// overwriting of a-priori values for parameters to be estimated (i.e. not fixed)
		if (Nx.getProcessingType() == ProcessingType.ADJUSTMENT)
			Nx.setValue0(nx);
		
		if (Ny.getProcessingType() == ProcessingType.ADJUSTMENT)
			Ny.setValue0(ny);
		
		
		if (D0.getProcessingType() == ProcessingType.ADJUSTMENT)
			D0.setValue0(d);
		
		if (L0.getProcessingType() == ProcessingType.ADJUSTMENT)
			L0.setValue0(l);
	}
	
	@Override
	public void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex) {
		Point centerOfMass = this.getCenterOfMass();

		//Schwerpunktreduktion
		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();

		// Parameter der Geraden
		UnknownParameter nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter ny = this.parameters.get(ParameterType.VECTOR_Y);
		
		if (Jx != null) {
			UnknownParameter d  = this.parameters.get(ParameterType.LENGTH);
			if (nx.getColumn() >= 0)
				Jx.set(rowIndex, nx.getColumn(),  xi);
			if (ny.getColumn() >= 0)
				Jx.set(rowIndex, ny.getColumn(),  yi);
			if (d.getColumn() >= 0)
				Jx.set(rowIndex, d.getColumn(),  -1.0);
		}

		if (Jv != null) {
			Jv.set(rowIndex, 0, nx.getValue());
			Jv.set(rowIndex, 1, ny.getValue());
		}
	}

	@Override
	public double getMisclosure(FeaturePoint point) {
		Point centerOfMass = this.getCenterOfMass();

		double xi = point.getX() - centerOfMass.getX0();
		double yi = point.getY() - centerOfMass.getY0();
		
		double nx = this.parameters.get(ParameterType.VECTOR_X).getValue();
		double ny = this.parameters.get(ParameterType.VECTOR_Y).getValue();
		double d  = this.parameters.get(ParameterType.LENGTH).getValue();

		return nx * xi + ny * yi - d;
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
		
		double nx = this.parameters.get(ParameterType.VECTOR_X).getValue();
		double ny = this.parameters.get(ParameterType.VECTOR_Y).getValue();
		UnknownParameter d = this.parameters.get(ParameterType.LENGTH);
	
		double dist = d.getValue();
		dist += (nx * prevCenterOfMass.getX0() + ny * prevCenterOfMass.getY0());
		dist -= (nx * currCenterOfMass.getX0() + ny * currCenterOfMass.getY0());
		d.setValue(dist);	
	}

	@Override
	public void reverseCenterOfMass(UpperSymmPackMatrix Dp) {
		Point centerOfMass = this.getCenterOfMass();

		UnknownParameter nx = this.parameters.get(ParameterType.VECTOR_X);
		UnknownParameter ny = this.parameters.get(ParameterType.VECTOR_Y);
		UnknownParameter d  = this.parameters.get(ParameterType.LENGTH);
		
		if (Dp != null) {
			int nou = Dp.numColumns();
			Matrix J = Matrices.identity(nou);
			
			if (d.getColumn() >= 0) {
				if (nx.getColumn() >= 0)
					J.set(d.getColumn(), nx.getColumn(), centerOfMass.getX0());
				if (ny.getColumn() >= 0)
					J.set(d.getColumn(), ny.getColumn(), centerOfMass.getY0());
				J.set(d.getColumn(), d.getColumn(),  1.0);

				Matrix JDp = new DenseMatrix(nou, nou);
				J.mult(Dp, JDp);
				JDp.transBmult(J, Dp);
			}
		}
		
		double dist = d.getValue() + nx.getValue() * centerOfMass.getX0() + ny.getValue() * centerOfMass.getY0(); 
		d.setValue(dist);
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
		this.parameters.put(ParameterType.VECTOR_X, new UnknownParameter(ParameterType.VECTOR_X, true, 0));
		this.parameters.put(ParameterType.VECTOR_Y, new UnknownParameter(ParameterType.VECTOR_Y, true, 1));
		this.parameters.put(ParameterType.LENGTH,   new UnknownParameter(ParameterType.LENGTH,   true));
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));
		
		if (this.vectorLengthRestriction == null) {
			UnknownParameter normalX = this.parameters.get(ParameterType.VECTOR_X);
			UnknownParameter normalY = this.parameters.get(ParameterType.VECTOR_Y);
			UnknownParameter vectorLength = this.parameters.get(ParameterType.VECTOR_LENGTH);
			
			List<UnknownParameter> normalVector = List.of(normalX, normalY);
			this.vectorLengthRestriction = new ProductSumRestriction(true, normalVector, normalVector, vectorLength);
		}
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.LINE;
	}

	@Override
	public String toLaTex() {
		return "$\\mathbf{n^\\mathrm{T}} \\mathbf{P}_i = d"
				+ " \\\\ "
				+ "\\mathbf{n} = \\left( \\begin{array}{c} n_x \\\\ n_y \\end{array} \\right)$";
	}
}
