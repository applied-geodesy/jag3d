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

package org.applied_geodesy.adjustment.transformation.equation.affin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.transformation.equation.TransformationEquations;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.AdjustablePosition;
import org.applied_geodesy.adjustment.transformation.point.DispersionablePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.PositionPair;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.adjustment.transformation.restriction.ProductSumRestriction.SignType;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public class SpatialAffinEquations extends TransformationEquations {
	private Map<ParameterType, UnknownParameter> parameters = null;
	
	private ProductSumRestriction quaternionLengthRestriction;

	public SpatialAffinEquations() {
		this.init();
	}
	
	public void setInitialGuess(double tx, double ty, double tz, double q0, double q1, double q2, double q3, double s11, double s12, double s13, double s22, double s23, double s33) throws IllegalArgumentException {
		this.parameters.get(ParameterType.SHIFT_X).setValue0(tx);
		this.parameters.get(ParameterType.SHIFT_Y).setValue0(ty);
		this.parameters.get(ParameterType.SHIFT_Z).setValue0(tz);
		
		this.parameters.get(ParameterType.QUATERNION_Q0).setValue0(q0);
		this.parameters.get(ParameterType.QUATERNION_Q1).setValue0(q1);
		this.parameters.get(ParameterType.QUATERNION_Q2).setValue0(q2);
		this.parameters.get(ParameterType.QUATERNION_Q3).setValue0(q3);
		
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S11).setValue0(s11);
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S12).setValue0(s12);
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S13).setValue0(s13);
		
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S22).setValue0(s22);
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S23).setValue0(s23);
		
		this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S33).setValue0(s33);
	}
	
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
		UnknownParameter Tz = this.parameters.get(ParameterType.SHIFT_Z);
		
		UnknownParameter Q0 = this.parameters.get(ParameterType.QUATERNION_Q0);
		UnknownParameter Q1 = this.parameters.get(ParameterType.QUATERNION_Q1);
		UnknownParameter Q2 = this.parameters.get(ParameterType.QUATERNION_Q2);
		UnknownParameter Q3 = this.parameters.get(ParameterType.QUATERNION_Q3);
		
		UnknownParameter S11 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S11);
		UnknownParameter S12 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S12);
		UnknownParameter S13 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S13);
		
		UnknownParameter S22 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S22);
		UnknownParameter S23 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S23);
		
		UnknownParameter S33 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S33);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();
		double tz = Tz.getValue();

		// Quaternion
		double q0 = Q0.getValue();
		double q1 = Q1.getValue();
		double q2 = Q2.getValue();
		double q3 = Q3.getValue();

		// Elements of scale and shear matrix
		double s11 = S11.getValue();
		double s12 = S12.getValue();
		double s13 = S13.getValue();

		double s22 = S22.getValue();
		double s23 = S23.getValue();

		double s33 = S33.getValue();

		// Elements of rotation matrix
		double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
		double r12 = 2.0 * (q1*q2 - q0*q3);
		double r13 = 2.0 * (q1*q3 + q0*q2);
		
		double r21 = 2.0 * (q1*q2 + q0*q3);
		double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
		double r23 = 2.0 * (q2*q3 - q0*q1);
		
		double r31 = 2.0 * (q1*q3 - q0*q2);
		double r32 = 2.0 * (q2*q3 + q0*q1);
		double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;
		
		double dxi = -(prevCenterOfMasses.getSourceSystemPosition().getX() - centerOfMasses.getSourceSystemPosition().getX());
		double dyi = -(prevCenterOfMasses.getSourceSystemPosition().getY() - centerOfMasses.getSourceSystemPosition().getY());
		double dzi = -(prevCenterOfMasses.getSourceSystemPosition().getZ() - centerOfMasses.getSourceSystemPosition().getZ());
		
		double dXi = prevCenterOfMasses.getTargetSystemPosition().getX() - centerOfMasses.getTargetSystemPosition().getX();
		double dYi = prevCenterOfMasses.getTargetSystemPosition().getY() - centerOfMasses.getTargetSystemPosition().getY();
		double dZi = prevCenterOfMasses.getTargetSystemPosition().getZ() - centerOfMasses.getTargetSystemPosition().getZ();

		double smxP = s11*dxi + s12*dyi + s13*dzi;
		double smyP = s22*dyi + s23*dzi;
		double smzP = s33*dzi;

		// Set shift vector w.r.t. new center of masses 
		Tx.setValue( tx + dXi + (r11*smxP + r12*smyP + r13*smzP) );
		Ty.setValue( ty + dYi + (r21*smxP + r22*smyP + r23*smzP) );
		Tz.setValue( tz + dZi + (r31*smxP + r32*smyP + r33*smzP) );
	}

	@Override
	public void reverseCenterOfMasses(UpperSymmPackMatrix Dp) {
		SimplePositionPair centerOfMasses = this.getCenterOfMasses();

		double xi = -centerOfMasses.getSourceSystemPosition().getX();
		double yi = -centerOfMasses.getSourceSystemPosition().getY();
		double zi = -centerOfMasses.getSourceSystemPosition().getZ();
		
		double Xi =  centerOfMasses.getTargetSystemPosition().getX();
		double Yi =  centerOfMasses.getTargetSystemPosition().getY();
		double Zi =  centerOfMasses.getTargetSystemPosition().getZ();
				
		UnknownParameter Tx = this.parameters.get(ParameterType.SHIFT_X);
		UnknownParameter Ty = this.parameters.get(ParameterType.SHIFT_Y);
		UnknownParameter Tz = this.parameters.get(ParameterType.SHIFT_Z);
		
		UnknownParameter Q0 = this.parameters.get(ParameterType.QUATERNION_Q0);
		UnknownParameter Q1 = this.parameters.get(ParameterType.QUATERNION_Q1);
		UnknownParameter Q2 = this.parameters.get(ParameterType.QUATERNION_Q2);
		UnknownParameter Q3 = this.parameters.get(ParameterType.QUATERNION_Q3);
		
		UnknownParameter S11 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S11);
		UnknownParameter S12 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S12);
		UnknownParameter S13 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S13);
		
		UnknownParameter S22 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S22);
		UnknownParameter S23 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S23);
		
		UnknownParameter S33 = this.parameters.get(ParameterType.SCALE_SHEAR_COMPONENT_S33);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();
		double tz = Tz.getValue();

		// Quaternion
		double q0 = Q0.getValue();
		double q1 = Q1.getValue();
		double q2 = Q2.getValue();
		double q3 = Q3.getValue();

		// Elements of scale and shear matrix
		double s11 = S11.getValue();
		double s12 = S12.getValue();
		double s13 = S13.getValue();

		double s22 = S22.getValue();
		double s23 = S23.getValue();

		double s33 = S33.getValue();

		// Elements of rotation matrix
		double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
		double r12 = 2.0 * (q1*q2 - q0*q3);
		double r13 = 2.0 * (q1*q3 + q0*q2);
		
		double r21 = 2.0 * (q1*q2 + q0*q3);
		double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
		double r23 = 2.0 * (q2*q3 - q0*q1);
		
		double r31 = 2.0 * (q1*q3 - q0*q2);
		double r32 = 2.0 * (q2*q3 + q0*q1);
		double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;

		double smxP = s11*xi + s12*yi + s13*zi;
		double smyP = s22*yi + s23*zi;
		double smzP = s33*zi;
		
		// Set inverted center of maas reduction 
		Tx.setValue( tx + Xi + (r11*smxP + r12*smyP + r13*smzP) );
		Ty.setValue( ty + Yi + (r21*smxP + r22*smyP + r23*smzP) );
		Tz.setValue( tz + Zi + (r31*smxP + r32*smyP + r33*smzP) );

		centerOfMasses.getSourceSystemPosition().setX(0);
		centerOfMasses.getSourceSystemPosition().setY(0);
		centerOfMasses.getSourceSystemPosition().setZ(0);
		
		centerOfMasses.getTargetSystemPosition().setX(0);
		centerOfMasses.getTargetSystemPosition().setY(0);
		centerOfMasses.getTargetSystemPosition().setZ(0);
		
		// fill jacobian A to transform shift vector		
		int nou = Dp.numColumns();
		Matrix Jx = Matrices.identity(nou);
		this.normalEquationElements(new HomologousFramePositionPair(centerOfMasses.getName(), xi, yi, zi,  Xi, Yi, Zi), Jx, null, null, null);

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
		double zi = pointSourceCRS.getZ() - centerOfMasses.getSourceSystemPosition().getZ();
		
		double Xi = pointTargetCRS.getX() - centerOfMasses.getTargetSystemPosition().getX();
		double Yi = pointTargetCRS.getY() - centerOfMasses.getTargetSystemPosition().getY();
		double Zi = pointTargetCRS.getZ() - centerOfMasses.getTargetSystemPosition().getZ();
		
		UnknownParameter Tx = this.getUnknownParameter(ParameterType.SHIFT_X);
		UnknownParameter Ty = this.getUnknownParameter(ParameterType.SHIFT_Y);
		UnknownParameter Tz = this.getUnknownParameter(ParameterType.SHIFT_Z);
		
		UnknownParameter Q0 = this.getUnknownParameter(ParameterType.QUATERNION_Q0);
		UnknownParameter Q1 = this.getUnknownParameter(ParameterType.QUATERNION_Q1);
		UnknownParameter Q2 = this.getUnknownParameter(ParameterType.QUATERNION_Q2);
		UnknownParameter Q3 = this.getUnknownParameter(ParameterType.QUATERNION_Q3);
		
		UnknownParameter S11 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S11);
		UnknownParameter S12 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S12);
		UnknownParameter S13 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S13);
		
		UnknownParameter S22 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S22);
		UnknownParameter S23 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S23);
		
		UnknownParameter S33 = this.getUnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S33);

		// Shift vector
		double tx = Tx.getValue();
		double ty = Ty.getValue();
		double tz = Tz.getValue();
		
		// Quaternion
		double q0 = Q0.getValue();
		double q1 = Q1.getValue();
		double q2 = Q2.getValue();
		double q3 = Q3.getValue();

		// Elements of scale and shear matrix
		double s11 = S11.getValue();
		double s12 = S12.getValue();
		double s13 = S13.getValue();

		double s22 = S22.getValue();
		double s23 = S23.getValue();

		double s33 = S33.getValue();

		// Elements of rotation matrix
		double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
		double r12 = 2.0 * (q1*q2 - q0*q3);
		double r13 = 2.0 * (q1*q3 + q0*q2);
		
		double r21 = 2.0 * (q1*q2 + q0*q3);
		double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
		double r23 = 2.0 * (q2*q3 - q0*q1);
		
		double r31 = 2.0 * (q1*q3 - q0*q2);
		double r32 = 2.0 * (q2*q3 + q0*q1);
		double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;

		double smxP = s11*xi + s12*yi + s13*zi;
		double smyP = s22*yi + s23*zi;
		double smzP = s33*zi;
		
		if (Jx != null) {
			int rowIndex = 0;
			
			if (Tx.getColumn() >= 0)
				Jx.set(rowIndex, Tx.getColumn(), 1.0);
			if (Ty.getColumn() >= 0)
				Jx.set(rowIndex, Ty.getColumn(), 0.0);
			if (Tz.getColumn() >= 0)
				Jx.set(rowIndex, Tz.getColumn(), 0.0);
			
			if (Q0.getColumn() >= 0)
				Jx.set(rowIndex, Q0.getColumn(), 2.0 * (2.0*q0*smxP - q3*smyP + q2*smzP));
			if (Q1.getColumn() >= 0)
				Jx.set(rowIndex, Q1.getColumn(), 2.0 * (2.0*q1*smxP + q2*smyP + q3*smzP));
			if (Q2.getColumn() >= 0)
				Jx.set(rowIndex, Q2.getColumn(), 2.0 * (q1*smyP + q0*smzP));
			if (Q3.getColumn() >= 0)
				Jx.set(rowIndex, Q3.getColumn(), 2.0 * (q1*smzP - q0*smyP));
			
			if (S11.getColumn() >= 0)
				Jx.set(rowIndex, S11.getColumn(), r11*xi);
			if (S12.getColumn() >= 0)
				Jx.set(rowIndex, S12.getColumn(), r11*yi);
			if (S13.getColumn() >= 0)
				Jx.set(rowIndex, S13.getColumn(), r11*zi);
			
			if (S22.getColumn() >= 0)
				Jx.set(rowIndex, S22.getColumn(), r12*yi);
			if (S23.getColumn() >= 0)
				Jx.set(rowIndex, S23.getColumn(), r12*zi);
			
			if (S33.getColumn() >= 0)
				Jx.set(rowIndex, S33.getColumn(), r13*zi);	
			
			rowIndex++;
			
			if (Tx.getColumn() >= 0)
				Jx.set(rowIndex, Tx.getColumn(), 0.0);
			if (Ty.getColumn() >= 0)
				Jx.set(rowIndex, Ty.getColumn(), 1.0);
			if (Tz.getColumn() >= 0)
				Jx.set(rowIndex, Tz.getColumn(), 0.0);
			
			if (Q0.getColumn() >= 0)
				Jx.set(rowIndex, Q0.getColumn(), 2.0 * (q3*smxP + 2.0*q0*smyP - q1*smzP));
			if (Q1.getColumn() >= 0)
				Jx.set(rowIndex, Q1.getColumn(), 2.0 * (q2*smxP - q0*smzP));
			if (Q2.getColumn() >= 0)
				Jx.set(rowIndex, Q2.getColumn(), 2.0 * (q1*smxP + 2.0*q2*smyP + q3*smzP));
			if (Q3.getColumn() >= 0)
				Jx.set(rowIndex, Q3.getColumn(), 2.0 * (q0*smxP + q2*smzP));
			
			if (S11.getColumn() >= 0)
				Jx.set(rowIndex, S11.getColumn(), r21*xi);
			if (S12.getColumn() >= 0)
				Jx.set(rowIndex, S12.getColumn(), r21*yi);
			if (S13.getColumn() >= 0)
				Jx.set(rowIndex, S13.getColumn(), r21*zi);
			
			if (S22.getColumn() >= 0)
				Jx.set(rowIndex, S22.getColumn(), r22*yi);
			if (S23.getColumn() >= 0)
				Jx.set(rowIndex, S23.getColumn(), r22*zi);
			
			if (S33.getColumn() >= 0)
				Jx.set(rowIndex, S33.getColumn(), r23*zi);
			
			rowIndex++;
			
			if (Tx.getColumn() >= 0)
				Jx.set(rowIndex, Tx.getColumn(), 0.0);
			if (Ty.getColumn() >= 0)
				Jx.set(rowIndex, Ty.getColumn(), 0.0);
			if (Tz.getColumn() >= 0)
				Jx.set(rowIndex, Tz.getColumn(), 1.0);
			
			if (Q0.getColumn() >= 0)
				Jx.set(rowIndex, Q0.getColumn(), 2.0 * (q1*smyP - q2*smxP + 2.0*q0*smzP));
			if (Q1.getColumn() >= 0)
				Jx.set(rowIndex, Q1.getColumn(), 2.0 * (q3*smxP + q0*smyP));
			if (Q2.getColumn() >= 0)
				Jx.set(rowIndex, Q2.getColumn(), 2.0 * (q3*smyP - q0*smxP));
			if (Q3.getColumn() >= 0)
				Jx.set(rowIndex, Q3.getColumn(), 2.0 * (q1*smxP + q2*smyP + 2.0*q3*smzP));
			
			if (S11.getColumn() >= 0)
				Jx.set(rowIndex, S11.getColumn(), r31*xi);
			if (S12.getColumn() >= 0)
				Jx.set(rowIndex, S12.getColumn(), r31*yi);
			if (S13.getColumn() >= 0)
				Jx.set(rowIndex, S13.getColumn(), r31*zi);
			
			if (S22.getColumn() >= 0)
				Jx.set(rowIndex, S22.getColumn(), r32*yi);
			if (S23.getColumn() >= 0)
				Jx.set(rowIndex, S23.getColumn(), r32*zi);
			
			if (S33.getColumn() >= 0)
				Jx.set(rowIndex, S33.getColumn(), r33*zi);	
		}
		
		// source system observation
		if (JvSrc != null) {
			int rowIndex = 0;

			JvSrc.set(rowIndex, 0, r11*s11);
			JvSrc.set(rowIndex, 1, r11*s12 + r12*s22);
			JvSrc.set(rowIndex, 2, r11*s13 + r12*s23 + r13*s33);

			rowIndex++;

			JvSrc.set(rowIndex, 0, r21*s11);
			JvSrc.set(rowIndex, 1, r21*s12 + r22*s22);
			JvSrc.set(rowIndex, 2, r21*s13 + r22*s23 + r23*s33);

			rowIndex++;

			JvSrc.set(rowIndex, 0, r31*s11);
			JvSrc.set(rowIndex, 1, r31*s12 + r32*s22);
			JvSrc.set(rowIndex, 2, r31*s13 + r32*s23 + r33*s33);		
		}
		
		// target system observation
		if (JvTrg != null) {
			int rowIndex = 0;

			JvTrg.set(rowIndex, 0, -1.0);
			JvTrg.set(rowIndex, 1,  0.0);
			JvTrg.set(rowIndex, 2,  0.0);

			rowIndex++;

			JvTrg.set(rowIndex, 0,  0.0);
			JvTrg.set(rowIndex, 1, -1.0);
			JvTrg.set(rowIndex, 2,  0.0);

			rowIndex++;

			JvTrg.set(rowIndex, 0,  0.0);
			JvTrg.set(rowIndex, 1,  0.0);
			JvTrg.set(rowIndex, 2, -1.0);			
		}
		
		if (w != null) {
			int rowIndex = 0;
			w.set(rowIndex++, tx + (r11*smxP + r12*smyP + r13*smzP) - Xi);
			w.set(rowIndex++, ty + (r21*smxP + r22*smyP + r23*smzP) - Yi);
			w.set(rowIndex++, tz + (r31*smxP + r32*smyP + r33*smzP) - Zi);
		}
	}

	private void init() {
		this.parameters = new LinkedHashMap<ParameterType, UnknownParameter>();
		this.parameters.put(ParameterType.SHIFT_X, new UnknownParameter(ParameterType.SHIFT_X, true));
		this.parameters.put(ParameterType.SHIFT_Y, new UnknownParameter(ParameterType.SHIFT_Y, true));
		this.parameters.put(ParameterType.SHIFT_Z, new UnknownParameter(ParameterType.SHIFT_Z, true));
		
		this.parameters.put(ParameterType.QUATERNION_Q0, new UnknownParameter(ParameterType.QUATERNION_Q0, true));
		this.parameters.put(ParameterType.QUATERNION_Q1, new UnknownParameter(ParameterType.QUATERNION_Q1, true));
		this.parameters.put(ParameterType.QUATERNION_Q2, new UnknownParameter(ParameterType.QUATERNION_Q2, true));
		this.parameters.put(ParameterType.QUATERNION_Q3, new UnknownParameter(ParameterType.QUATERNION_Q3, true));
		
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S11, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S11, true));
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S12, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S12, true));
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S13, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S13, true));
		
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S22, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S22, true));
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S23, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S23, true));
		
		this.parameters.put(ParameterType.SCALE_SHEAR_COMPONENT_S33, new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S33, true));
		
		this.parameters.put(ParameterType.VECTOR_LENGTH, new UnknownParameter(ParameterType.VECTOR_LENGTH, true, 1.0, true, ProcessingType.FIXED));
		
		UnknownParameter q0 = this.parameters.get(ParameterType.QUATERNION_Q0);
		UnknownParameter q1 = this.parameters.get(ParameterType.QUATERNION_Q1);
		UnknownParameter q2 = this.parameters.get(ParameterType.QUATERNION_Q2);
		UnknownParameter q3 = this.parameters.get(ParameterType.QUATERNION_Q3);
		UnknownParameter quaternionLength = this.parameters.get(ParameterType.VECTOR_LENGTH);
		
		List<UnknownParameter> unitQuaternion = List.of(q0, q1, q2, q3);
		this.quaternionLengthRestriction = new ProductSumRestriction(true, unitQuaternion, unitQuaternion, List.of(SignType.PLUS, SignType.PLUS, SignType.PLUS, SignType.PLUS), quaternionLength);
	}
	
	@Override
	public Collection<Restriction> getRestrictions() {
		return List.of(this.quaternionLengthRestriction);
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
	public final int getDimension() {
		return 3;
	}
}
