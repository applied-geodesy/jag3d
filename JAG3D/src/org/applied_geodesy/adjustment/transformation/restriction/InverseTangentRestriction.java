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
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.Matrix;

public class InverseTangentRestriction extends Restriction {
	
	private ObjectProperty<UnknownParameter> regressorA = new SimpleObjectProperty<UnknownParameter>(this, "regressorA");
	private ObjectProperty<UnknownParameter> regressorB = new SimpleObjectProperty<UnknownParameter>(this, "regressorB");

	/**
	 * ATAN2(A,B)  ==  C
	 */
	public InverseTangentRestriction(boolean indispensable, UnknownParameter regressorsA, UnknownParameter regressorsB, UnknownParameter regressand) {
		super(RestrictionType.INVERSE_TANGENT_TWO, indispensable);
		this.setRegressand(regressand);
		this.setRegressorA(regressorsA);
		this.setRegressorB(regressorsB);
	}
	
	public void setRegressorA(UnknownParameter regressorA) {
		this.regressorA.set(regressorA);
	}
	
	public UnknownParameter getRegressorA() {
		return this.regressorA.get();
	}
	
	public ObjectProperty<UnknownParameter> regressorAProperty() {
		return this.regressorA;
	}
	
	public void setRegressorB(UnknownParameter regressorB) {
		this.regressorB.set(regressorB);
	}
	
	public UnknownParameter getRegressorB() {
		return this.regressorB.get();
	}
	
	public ObjectProperty<UnknownParameter> regressorBProperty() {
		return this.regressorB;
	}	

	@Override
	public double getMisclosure() {
		double a = this.regressorA.get().getValue();
		double b = this.regressorB.get().getValue();
		double c = MathExtension.MOD(this.regressand.get().getValue(), 2.0*Math.PI);

		return MathExtension.MOD(Math.atan2(a,b), 2.0*Math.PI) - c;
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		int rowIndex = this.getRow();
		double a = this.regressorA.get().getValue();
		double b = this.regressorB.get().getValue();
		
		double ab = a*a + b*b;
		
		if (this.regressorA.get().getColumn() >= 0) 
			JrT.add(this.regressorA.get().getColumn(), rowIndex, -b/ab);
		
		if (this.regressorB.get().getColumn() >= 0) 
			JrT.add(this.regressorB.get().getColumn(), rowIndex, +a/ab);
		
		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
	}

	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.regressorA.get() == object || this.regressorB.get() == object;
	}

	@Override
	public String toLaTex() {
		return "$\\arctan_2 \\left( y,x \\right) = c$";
	}
}
