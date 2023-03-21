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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.uib.cipr.matrix.Matrix;

public class ShearAngleRestriction extends Restriction {
	private ObservableList<UnknownParameter> shearParameters = FXCollections.<UnknownParameter>observableArrayList();
	private ObjectProperty<EulerAxisType> eulerAxisType = new SimpleObjectProperty<EulerAxisType>(this, "eulerAxisType");
	public ShearAngleRestriction() {
		this(
				false,
				EulerAxisType.Z_AXIS,
				new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S12, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S13, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S22, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S23, Boolean.FALSE, Boolean.FALSE),
				new UnknownParameter(ParameterType.SCALE_SHEAR_COMPONENT_S33, Boolean.FALSE, Boolean.FALSE),
				null);
	}
	
	public ShearAngleRestriction(boolean indispensable, EulerAxisType eulerAxisType, UnknownParameter s12, UnknownParameter s13, UnknownParameter s22, UnknownParameter s23, UnknownParameter s33, UnknownParameter regressand) {
		super(RestrictionType.SHEAR_ANGLE_EXTRACTION, indispensable);
		this.setRegressand(regressand);
		
		this.eulerAxisType.set(eulerAxisType);
		this.shearParameters.setAll(s12, s13, s22, s23, s33);
	}
	
	public ObservableList<UnknownParameter> getShearParameters() {
		return this.shearParameters;
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
		double s12 = this.shearParameters.get(0).getValue(); //s12
		double s13 = this.shearParameters.get(1).getValue(); //s13
		double s22 = this.shearParameters.get(2).getValue(); //s22
		double s23 = this.shearParameters.get(3).getValue(); //s23
		double s33 = this.shearParameters.get(4).getValue(); //s33

	    EulerAxisType eulerAxisType = this.eulerAxisType.get();
	    double c = MathExtension.MOD(this.regressand.get().getValue(), Math.PI); 
	    double angle = 0;
	    switch (eulerAxisType) {
		case X_AXIS:
			angle = MathExtension.MOD( Math.atan2(-s23, s33), Math.PI );
			break;
		case Y_AXIS:
			angle = MathExtension.MOD( Math.hypot(s23, s33) > 0.0 ? Math.atan2(-s13, Math.hypot(s23, s33)) : 0.0, Math.PI );
			break;
		case Z_AXIS:
			angle = MathExtension.MOD( Math.atan2(-s12, s22), Math.PI );
			break;
		default:
			throw new IllegalArgumentException("Error, unsupported angle type " + this.getEulerAngleAxisType() + "!");
		}
	    
	    return MathExtension.MOD(angle - c, Math.PI); 
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		int rowIndex = this.getRow();
		
		UnknownParameter S12 = this.shearParameters.get(0); // S12
		UnknownParameter S13 = this.shearParameters.get(1); // S13
		UnknownParameter S22 = this.shearParameters.get(2); // S22
		UnknownParameter S23 = this.shearParameters.get(3); // S23
		UnknownParameter S33 = this.shearParameters.get(4); // S33
		
		double s12 = S12.getValue(); //s12
		double s13 = S13.getValue(); //s13
		double s22 = S22.getValue(); //s22
		double s23 = S23.getValue(); //s23
		double s33 = S33.getValue(); //s33
		
	    EulerAxisType eulerAxisType = this.eulerAxisType.get();
		
		switch (eulerAxisType) {
		case X_AXIS:
			double dX = s33*s33 + s23*s23;
			if (S23.getColumn() >= 0 && dX > 0) {
				int columnIndex = S23.getColumn();
				JrT.add(columnIndex, rowIndex, -s33/(s33*s33+s23*s23));
			}
			if (S33.getColumn() >= 0 && dX > 0) {
				int columnIndex = S33.getColumn();
				JrT.add(columnIndex, rowIndex,  s23/(s33*s33+s23*s23));
			}
			
			break;
		case Y_AXIS:
			double dY1 = Math.hypot(s23, s33);
			double dY2 = s23*s23 + s33*s33 + s13*s13;
			if (S13.getColumn() >= 0 && dY2 > 0) {
				int columnIndex = S13.getColumn();
				JrT.add(columnIndex, rowIndex, -dY1/dY2);
			}
			if (S23.getColumn() >= 0 && dY1 > 0 && dY2 > 0) {
				int columnIndex = S23.getColumn();
				JrT.add(columnIndex, rowIndex, s13/dY1 * s23/dY2);
			}
			if (S33.getColumn() >= 0 && dY1 > 0 && dY2 > 0) {
				int columnIndex = S33.getColumn();
				JrT.add(columnIndex, rowIndex, s13/dY1 * s33/dY2);
			}
			
			break;
		case Z_AXIS:
			double dZ = s22*s22 + s12*s12;
			if (S12.getColumn() >= 0 && dZ > 0) {
				int columnIndex = S12.getColumn();
				JrT.add(columnIndex, rowIndex, -s22/dZ);
			}
			if (S22.getColumn() >= 0) {
				int columnIndex = S22.getColumn();
				JrT.add(columnIndex, rowIndex,  s12/dZ);
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
		return this.regressand.get() == object || this.shearParameters.contains(object);
	}
	
	
	@Override
	public String toLaTex() {
		// TODO Auto-generated method stub
		return null;
	}
}