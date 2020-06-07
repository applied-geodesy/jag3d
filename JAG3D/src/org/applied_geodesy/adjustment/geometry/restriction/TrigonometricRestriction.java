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

package org.applied_geodesy.adjustment.geometry.restriction;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.Matrix;

public class TrigonometricRestriction extends Restriction {
	public enum TrigonometricFunctionType {
		 SINE, COSINE, TANGENT, COTANGENT
	}
	
	private ObjectProperty<TrigonometricFunctionType> trigonometricFunctionType = new SimpleObjectProperty<TrigonometricFunctionType>(this, "trigonometricFunctionType", TrigonometricFunctionType.TANGENT);
	private ObjectProperty<UnknownParameter> regressor = new SimpleObjectProperty<UnknownParameter>(this, "regressor");
	private ObjectProperty<Boolean> invert = new SimpleObjectProperty<Boolean>(this, "invert", Boolean.FALSE);
	
	public TrigonometricRestriction() {
		this(false, TrigonometricFunctionType.TANGENT, Boolean.FALSE, null, null);
	}
	
	/**
	 * tri(a) ==  c
	 */
	public TrigonometricRestriction(boolean indispensable, TrigonometricFunctionType trigonometricFunctionType, boolean invert, UnknownParameter regressor, UnknownParameter regressand) {
		super(RestrictionType.TRIGONOMERTIC_FUNCTION, indispensable);
		this.setRegressand(regressand);
		this.setRegressor(regressor);
		this.setInvert(invert);
		this.setTrigonometricFunctionType(trigonometricFunctionType);
	}
	
	public UnknownParameter getRegressor() {
		return this.regressor.get();
	}
	
	public void setRegressor(UnknownParameter regressor) {
		this.regressor.set(regressor);
	}
	
	public ObjectProperty<UnknownParameter> regressorProperty() {
		return this.regressor;
	}
	
	public TrigonometricFunctionType getTrigonometricFunctionType() {
		return this.trigonometricFunctionType.get();
	}
	
	public void setTrigonometricFunctionType(TrigonometricFunctionType trigonometricFunctionType) {
		this.trigonometricFunctionType.set(trigonometricFunctionType);
	}
	
	public ObjectProperty<TrigonometricFunctionType> trigonometricFunctionTypeProperty() {
		return this.trigonometricFunctionType;
	}
	
	public void setInvert(boolean invert) {
		this.invert.set(invert);
	}
	
	public boolean isInvert() {
		return this.invert.get();
	}
	
	public ObjectProperty<Boolean> invertProperty() {
		return this.invert;
	}
	
	@Override
	public double getMisclosure() {
		double a = this.regressor.get().getValue();
		double c = this.regressand.get().getValue();
		TrigonometricFunctionType type = this.trigonometricFunctionType.get();
		
		switch(type) {
		case SINE:
			return this.isInvert() ? Math.asin(a) - c : Math.sin(a) - c;
			
		case COSINE:
			return this.isInvert() ? Math.acos(a) - c : Math.cos(a) - c;
		
		case TANGENT:
			return this.isInvert() ? Math.atan(a) - c : Math.tan(a) - c;
			
		case COTANGENT:
			return this.isInvert() ? MathExtension.acot(a) - c : 1.0 / MathExtension.cot(a) - c;
		}
		
		throw new IllegalArgumentException("Error, unsupported trigonometric function type " + this.getTrigonometricFunctionType() + "!");
	}
	
	@Override
	public void transposedJacobianElements(Matrix JrT) {
		int rowIndex = this.getRow();
		TrigonometricFunctionType type = this.trigonometricFunctionType.get();
		
		if (this.regressor.get().getColumn() >= 0) {
			int columnIndex = this.regressor.get().getColumn();
			double a = this.regressor.get().getValue();
			
			switch(type) {
			case SINE:
				JrT.add(columnIndex, rowIndex, this.isInvert() ?  1.0 / Math.sqrt(1.0 - a*a) :  Math.cos(a) );
				break;
				
			case COSINE:
				JrT.add(columnIndex, rowIndex, this.isInvert() ? -1.0 / Math.sqrt(1.0 - a*a) : -Math.sin(a) );
				break;
			
			case TANGENT:
				JrT.add(columnIndex, rowIndex, this.isInvert() ?  1.0 / (1.0 + a*a) : 1.0 / Math.cos(a) / Math.cos(a) );
				break;
				
			case COTANGENT:
				JrT.add(columnIndex, rowIndex, this.isInvert() ? -1.0 / (1.0 + a*a) : -1.0 / Math.sin(a) / Math.sin(a) );
				break;

			default:
				throw new IllegalArgumentException("Error, unsupported trigonometric function type " + this.getTrigonometricFunctionType() + "!");
			}
		}

		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
	}
	
	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.regressor.get() == object;
	}
	
	@Override
	public String toLaTex() {
		return "$\\mathrm{trigon} \\left( a \\right) = c$";
	}
}
