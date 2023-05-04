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

package org.applied_geodesy.adjustment.transformation.restriction;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.uib.cipr.matrix.Matrix;

public class QuaternionEulerAngleRestriction extends Restriction {
	private ObservableList<UnknownParameter> quaternion = FXCollections.<UnknownParameter>observableArrayList();
	private ObjectProperty<EulerAxisType> eulerAxisType = new SimpleObjectProperty<EulerAxisType>(this, "eulerAxisType");
	public QuaternionEulerAngleRestriction() {
		this(
				false,
				EulerAxisType.Z_AXIS,
				new UnknownParameter(ParameterType.QUATERNION_Q0, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.QUATERNION_Q1, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.QUATERNION_Q2, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.QUATERNION_Q3, Boolean.FALSE, Boolean.FALSE),
				null);
	}
	
	public QuaternionEulerAngleRestriction(boolean indispensable, EulerAxisType eulerAxisType, UnknownParameter q0, UnknownParameter q1, UnknownParameter q2, UnknownParameter q3, UnknownParameter regressand) {
		super(RestrictionType.QUATERNION_EULER_ANGLE, indispensable);
		this.setRegressand(regressand);
		
		this.eulerAxisType.set(eulerAxisType);
		this.quaternion.setAll(q0, q1, q2, q3);
	}
	
	public ObservableList<UnknownParameter> getQuaternion() {
		return this.quaternion;
	}
	
	public EulerAxisType getEulerAngleAxisType() {
		return this.eulerAxisType.get();
	}
	
	public void setEulerAxisType(EulerAxisType eulerAxisType) {
		this.eulerAxisType.set(eulerAxisType);
	}
	
	public ObjectProperty<EulerAxisType> eulerAxisTypeProperty() {
		return this.eulerAxisType;
	}

	@Override
	public double getMisclosure() {
		double q0 = this.quaternion.get(0).getValue();
		double q1 = this.quaternion.get(1).getValue();
		double q2 = this.quaternion.get(2).getValue();
		double q3 = this.quaternion.get(3).getValue();
		
		// Elements of rotation matrix
		double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
		double r12 = 2.0 * (q1*q2 - q0*q3);
		double r13 = 2.0 * (q1*q3 + q0*q2);

		double r21 = 2.0 * (q1*q2 + q0*q3);
		double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
		double r23 = 2.0 * (q2*q3 - q0*q1);

		//double r31 = 2.0 * (q1*q3 - q0*q2);
		//double r32 = 2.0 * (q2*q3 + q0*q1);
		double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;

	    /**
	     * 

cy = Math.hypot(r33, r23);
			if (cy > 16.0 * eps) {
				phi   = -Math.atan2( r23, r33);
				theta = -Math.atan2(-r13, cy);
				psi   = -Math.atan2( r12, r11);
			}
			else {
				phi   = 0.0;
				theta = -Math.atan2(-r13, cy);
				psi   = -Math.atan2(-r21, r22);
			}

	     */
	    
	    
	    
	    EulerAxisType eulerAxisType = this.eulerAxisType.get();
	    double c = MathExtension.MOD(this.regressand.get().getValue(), 2.0*Math.PI);
	    double angle = 0;
	    double cy = Math.hypot(r33, r23);
	    if (cy > 16.0 * Constant.EPS) {
	    	switch (eulerAxisType) {
	    	case X_AXIS:
	    		angle = MathExtension.MOD(-Math.atan2( r23, r33), 2.0*Math.PI);
	    		break;
	    	case Y_AXIS:
	    		angle = MathExtension.MOD(-Math.atan2(-r13, cy), 2.0*Math.PI);
	    		break;
	    	case Z_AXIS:
	    		angle = MathExtension.MOD(-Math.atan2( r12, r11), 2.0*Math.PI);
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Error, unsupported angle type " + this.getEulerAngleAxisType() + "!");
	    	}
	    }
	    else {
	    	switch (eulerAxisType) {
	    	case X_AXIS:
	    		angle = 0.0;
	    		break;
	    	case Y_AXIS:
	    		angle = MathExtension.MOD(-Math.atan2(-r13, cy), 2.0*Math.PI);
	    		break;
	    	case Z_AXIS:
	    		angle = MathExtension.MOD(-Math.atan2(-r21, r22), 2.0*Math.PI);
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Error, unsupported angle type " + this.getEulerAngleAxisType() + "!");
	    	}
	    }
	    
	    //return Math.abs(angle - c) < Math.abs((2.0*Math.PI - angle) - c) ? angle - c : (2.0*Math.PI - angle) - c; 
	    return MathExtension.MOD(angle - c, 2.0*Math.PI);
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		int rowIndex = this.getRow();
		
		UnknownParameter Q0 = this.quaternion.get(0);
		UnknownParameter Q1 = this.quaternion.get(1);
		UnknownParameter Q2 = this.quaternion.get(2);
		UnknownParameter Q3 = this.quaternion.get(3);
		
		double q0 = Q0.getValue();
		double q1 = Q1.getValue();
		double q2 = Q2.getValue();
		double q3 = Q3.getValue();
		
		// Elements of rotation matrix
		double r11 = 2.0 * q0*q0 - 1.0 + 2.0*q1*q1;
		double r12 = 2.0 * (q1*q2 - q0*q3);
		double r13 = 2.0 * (q1*q3 + q0*q2);

		double r21 = 2.0 * (q1*q2 + q0*q3);
		double r22 = 2.0 * q0*q0 - 1.0 + 2.0*q2*q2;
		double r23 = 2.0 * (q2*q3 - q0*q1);

//		double r31 = 2.0 * (q1*q3 - q0*q2);
//		double r32 = 2.0 * (q2*q3 + q0*q1);
		double r33 = 2.0 * q0*q0 - 1.0 + 2.0*q3*q3;

		
	    EulerAxisType eulerAxisType = this.eulerAxisType.get();
	    double cy = Math.hypot(r33, r23);

	    switch (eulerAxisType) {
	    case X_AXIS:
	    	if (Q0.getColumn() >= 0) {
	    		int columnIndex = Q0.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex,  2.0*(q1*r33+2.0*r23*q0)/(r33*r33+r23*r23));
	    		else
	    			JrT.add(columnIndex, rowIndex,  0);
	    	}
	    	if (Q1.getColumn() >= 0) {
	    		int columnIndex = Q1.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex,  2.0*q0*r33/(r33*r33+r23*r23));
	    		else
	    			JrT.add(columnIndex, rowIndex,  0);
	    	}
	    	if (Q2.getColumn() >= 0) {
	    		int columnIndex = Q2.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex, -2.0*q3*r33/(r33*r33+r23*r23));
	    		else
	    			JrT.add(columnIndex, rowIndex,  0);
	    	}
	    	if (Q3.getColumn() >= 0) {
	    		int columnIndex = Q3.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex, -2.0*(q2*r33-2.0*r23*q3)/(r33*r33+r23*r23));
	    		else
	    			JrT.add(columnIndex, rowIndex,  0);
	    	}

	    	break;
	    case Y_AXIS:
	    	double tmp = cy * (r13*r13 + r23*r23 + r33*r33);
	    	if (Q0.getColumn() >= 0) {
	    		int columnIndex = Q0.getColumn();
	    		JrT.add(columnIndex, rowIndex, (2.0*(q2*r23*r23 + q1*r13*r23 + q2*r33*r33 - 2.0*q0*r13*r33))/tmp   );
	    	}
	    	if (Q1.getColumn() >= 0) {
	    		int columnIndex = Q1.getColumn();
	    		JrT.add(columnIndex, rowIndex, (2.0*(q3*r23*r23 + q0*r13*r23 + q3*r33*r33))/tmp );
	    	}
	    	if (Q2.getColumn() >= 0) {
	    		int columnIndex = Q2.getColumn();
	    		JrT.add(columnIndex, rowIndex, (2.0*(q0*r23*r23 - q3*r13*r23 + q0*r33*r33))/tmp  );
	    	}
	    	if (Q3.getColumn() >= 0) {
	    		int columnIndex = Q3.getColumn();
	    		JrT.add(columnIndex, rowIndex, (2.0*(q1*r23*r23 - q2*r13*r23 + q1*r33*r33 - 2.0*q3*r13*r33))/tmp  );
	    	}

	    	break;
	    case Z_AXIS:
	    	if (Q0.getColumn() >= 0) {
	    		int columnIndex = Q0.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex,  2.0*(q3*r11+2.0*r12*q0)/(r11*r11+r12*r12));
	    		else
	    			JrT.add(columnIndex, rowIndex, (2.0*q3*r22 - 4.0*q0*r21)/(r21*r21 + r22*r22));
	    	}
	    	if (Q1.getColumn() >= 0) {
	    		int columnIndex = Q1.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex, -2.0*(q2*r11-2.0*r12*q1)/(r11*r11+r12*r12));
	    		else
	    			JrT.add(columnIndex, rowIndex, (2.0*q2*r22)/(r21*r21 + r22*r22));
	    	}
	    	if (Q2.getColumn() >= 0) {
	    		int columnIndex = Q2.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex, -2.0*q1*r11/(r11*r11+r12*r12));
	    		else
	    			JrT.add(columnIndex, rowIndex, (2.0*q1*r22 - 4.0*q2*r21)/(r21*r21 + r22*r22));
	    	}
	    	if (Q3.getColumn() >= 0) {
	    		int columnIndex = Q3.getColumn();
	    		if (cy > 16.0 * Constant.EPS) 
	    			JrT.add(columnIndex, rowIndex,  2.0*q0*r11/(r11*r11+r12*r12));
	    		else
	    			JrT.add(columnIndex, rowIndex, (2.0*q0*r22)/(r21*r21 + r22*r22));
	    	}
	    	break;    
	    }

		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
	}

	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.quaternion.contains(object);
	}
	
	
	@Override
	public String toLaTex() {
		return null;
	}
}
