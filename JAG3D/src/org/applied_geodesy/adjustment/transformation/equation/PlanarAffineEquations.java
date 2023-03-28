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

package org.applied_geodesy.adjustment.transformation.equation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.AdjustablePosition;
import org.applied_geodesy.adjustment.transformation.point.DispersionablePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.PositionPair;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public class PlanarAffineEquations extends TransformationEquations {
	private Map<ParameterType, UnknownParameter> parameters = null;

	public PlanarAffineEquations() {
		this.init();
	}
	
	public void setInitialGuess(double tx, double ty, double a11, double a12, double a21, double a22) throws IllegalArgumentException {
		this.parameters.get(ParameterType.SHIFT_X).setValue0(tx);
		this.parameters.get(ParameterType.SHIFT_Y).setValue0(ty);
		
		this.parameters.get(ParameterType.AUXILIARY_ELEMENT_11).setValue0(a11);
		this.parameters.get(ParameterType.AUXILIARY_ELEMENT_12).setValue0(a12);
		
		this.parameters.get(ParameterType.AUXILIARY_ELEMENT_21).setValue0(a21);
		this.parameters.get(ParameterType.AUXILIARY_ELEMENT_22).setValue0(a22);		
	}
	
	@Override
	public void setCenterOfMasses(SimplePositionPair centerOfMasses) {
		
		// get previous center of mass
		SimplePositionPair prevCenterOfMasses = this.getCenterOfMasses();
		
		// check, if components are equal to previous point to avoid unnecessary operations
		boolean equalComponents = centerOfMasses.equalsCoordinateComponents(prevCenterOfMasses);
		super.setCenterOfMasses(centerOfMasses);
		
		if (equalComponents)
			return;
		
		UnknownParameter Tx = this.parameters.get(ParameterType.SHIFT_X);
		UnknownParameter Ty = this.parameters.get(ParameterType.SHIFT_Y);
		
		UnknownParameter A11 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_11);
		UnknownParameter A12 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_12);
		
		UnknownParameter A21 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_21);
		UnknownParameter A22 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_22);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();

		// Elements of linear matrix
		double a11 = A11.getValue();
		double a12 = A12.getValue();
		
		double a21 = A21.getValue();
		double a22 = A22.getValue();

		double dxi = -(prevCenterOfMasses.getSourceSystemPosition().getX() - centerOfMasses.getSourceSystemPosition().getX());
		double dyi = -(prevCenterOfMasses.getSourceSystemPosition().getY() - centerOfMasses.getSourceSystemPosition().getY());
		
		double dXi = prevCenterOfMasses.getTargetSystemPosition().getX() - centerOfMasses.getTargetSystemPosition().getX();
		double dYi = prevCenterOfMasses.getTargetSystemPosition().getY() - centerOfMasses.getTargetSystemPosition().getY();

		// Set shift vector w.r.t. new center of masses 
		Tx.setValue( tx + dXi + a11*dxi - a12*dyi );
		Ty.setValue( ty + dYi + a21*dxi + a22*dyi );
	}

	@Override
	public void reverseCenterOfMasses(UpperSymmPackMatrix Dp) {
		SimplePositionPair centerOfMasses = this.getCenterOfMasses();

		double xi = -centerOfMasses.getSourceSystemPosition().getX();
		double yi = -centerOfMasses.getSourceSystemPosition().getY();
		
		double Xi =  centerOfMasses.getTargetSystemPosition().getX();
		double Yi =  centerOfMasses.getTargetSystemPosition().getY();
				
		UnknownParameter Tx = this.parameters.get(ParameterType.SHIFT_X);
		UnknownParameter Ty = this.parameters.get(ParameterType.SHIFT_Y);
		
		UnknownParameter A11 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_11);
		UnknownParameter A12 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_12);
		
		UnknownParameter A21 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_21);
		UnknownParameter A22 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_22);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();

		// Elements of linear matrix
		double a11 = A11.getValue();
		double a12 = A12.getValue();

		double a21 = A21.getValue();
		double a22 = A22.getValue();
		
		// Set inverted center of maas reduction 
		Tx.setValue( tx + Xi + a11*xi - a12*yi );
		Ty.setValue( ty + Yi + a21*xi + a22*yi );

		centerOfMasses.getSourceSystemPosition().setX(0);
		centerOfMasses.getSourceSystemPosition().setY(0);
		
		centerOfMasses.getTargetSystemPosition().setX(0);
		centerOfMasses.getTargetSystemPosition().setY(0);
		
		// fill jacobian A to transform shift vector		
		int nou = Dp.numColumns();
		Matrix Jx = Matrices.identity(nou);
		this.normalEquationElements(new HomologousFramePositionPair(centerOfMasses.getName(), xi, yi,  Xi, Yi), Jx, null, null, null);

		Matrix DpJxT = new DenseMatrix(nou,nou);
		Dp.transBmult(Jx, DpJxT);
		Jx.mult(DpJxT, Dp);
	}
	
	@Override
	public void normalEquationElements(PositionPair<? extends DispersionablePosition, ? extends AdjustablePosition> positionPair, Matrix Jx, Matrix JvSrc, Matrix JvTrg, Vector w) {
		SimplePositionPair centerOfMasses = this.getCenterOfMasses();

		DispersionablePosition pointSourceCRS = positionPair.getSourceSystemPosition();
		AdjustablePosition pointTargetCRS = positionPair.getTargetSystemPosition();
		
		double xi = pointSourceCRS.getX() - centerOfMasses.getSourceSystemPosition().getX();
		double yi = pointSourceCRS.getY() - centerOfMasses.getSourceSystemPosition().getY();

		double Xi = pointTargetCRS.getX() - centerOfMasses.getTargetSystemPosition().getX();
		double Yi = pointTargetCRS.getY() - centerOfMasses.getTargetSystemPosition().getY();
		
		UnknownParameter Tx = this.parameters.get(ParameterType.SHIFT_X);
		UnknownParameter Ty = this.parameters.get(ParameterType.SHIFT_Y);
		
		UnknownParameter A11 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_11);
		UnknownParameter A12 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_12);
		
		UnknownParameter A21 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_21);
		UnknownParameter A22 = this.parameters.get(ParameterType.AUXILIARY_ELEMENT_22);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();

		// Elements of linear matrix
		double a11 = A11.getValue();
		double a12 = A12.getValue();

		double a21 = A21.getValue();
		double a22 = A22.getValue();
		
		if (Jx != null) {
			int rowIndex = 0;
			
			if (Tx.getColumn() >= 0)
				Jx.set(rowIndex, Tx.getColumn(), 1.0);
			if (Ty.getColumn() >= 0)
				Jx.set(rowIndex, Ty.getColumn(), 0.0);
			
			if (A11.getColumn() >= 0)
				Jx.set(rowIndex, A11.getColumn(),  xi);
			if (A12.getColumn() >= 0)
				Jx.set(rowIndex, A12.getColumn(), -yi);
						
			rowIndex++;
			
			if (Tx.getColumn() >= 0)
				Jx.set(rowIndex, Tx.getColumn(), 0.0);
			if (Ty.getColumn() >= 0)
				Jx.set(rowIndex, Ty.getColumn(), 1.0);

			if (A21.getColumn() >= 0)
				Jx.set(rowIndex, A21.getColumn(), xi);
			if (A22.getColumn() >= 0)
				Jx.set(rowIndex, A22.getColumn(), yi);
		}
		
		// source system observation
		if (JvSrc != null) {
			int rowIndex = 0;

			JvSrc.set(rowIndex, 0,  a11);
			JvSrc.set(rowIndex, 1, -a12);

			rowIndex++;

			JvSrc.set(rowIndex, 0, a21);
			JvSrc.set(rowIndex, 1, a22);
		}
		
		// target system observation
		if (JvTrg != null) {
			int rowIndex = 0;

			JvTrg.set(rowIndex, 0, -1.0);
			JvTrg.set(rowIndex, 1,  0.0);

			rowIndex++;

			JvTrg.set(rowIndex, 0,  0.0);
			JvTrg.set(rowIndex, 1, -1.0);		
		}
		
		if (w != null) {
			int rowIndex = 0;
			w.set(rowIndex++, tx + a11*xi - a12*yi - Xi);
			w.set(rowIndex++, ty + a21*xi + a22*yi - Yi);
		}
	}
	
	private void init() {
		this.parameters = new LinkedHashMap<ParameterType, UnknownParameter>();
		this.parameters.put(ParameterType.SHIFT_X, new UnknownParameter(ParameterType.SHIFT_X, true, 0.0));
		this.parameters.put(ParameterType.SHIFT_Y, new UnknownParameter(ParameterType.SHIFT_Y, true, 0.0));
		
		this.parameters.put(ParameterType.AUXILIARY_ELEMENT_11, new UnknownParameter(ParameterType.AUXILIARY_ELEMENT_11, true, 1.0));
		this.parameters.put(ParameterType.AUXILIARY_ELEMENT_12, new UnknownParameter(ParameterType.AUXILIARY_ELEMENT_12, true, 0.0));
		
		this.parameters.put(ParameterType.AUXILIARY_ELEMENT_21, new UnknownParameter(ParameterType.AUXILIARY_ELEMENT_21, true, 1.0));
		this.parameters.put(ParameterType.AUXILIARY_ELEMENT_22, new UnknownParameter(ParameterType.AUXILIARY_ELEMENT_22, true, 1.0));
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
	public TransformationType getTransformationType() {
		return TransformationType.PLANAR;
	}
}