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

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.uib.cipr.matrix.Matrix;

public class ProductSumRestriction extends Restriction {
	
	private ObservableList<UnknownParameter> regressorsA = FXCollections.<UnknownParameter>observableArrayList();
	private ObservableList<UnknownParameter> regressorsB = FXCollections.<UnknownParameter>observableArrayList();
	private ObjectProperty<Double> exponent = new SimpleObjectProperty<Double>(this, "exponent", 1.0);
	private ObjectProperty<Boolean> sumArithmetic = new SimpleObjectProperty<Boolean>(this, "sumArithmetic", Boolean.TRUE);
	
	public ProductSumRestriction() {
		this(false, new ArrayList<UnknownParameter>(0), new ArrayList<UnknownParameter>(0), 1.0, Boolean.TRUE, null);
	}
	
	public ProductSumRestriction(boolean indispensable, List<UnknownParameter> regressorsA, List<UnknownParameter> regressorsB, UnknownParameter regressand) {
		this(indispensable, regressorsA, regressorsB, 1.0, Boolean.TRUE, regressand);
	}
	
	public ProductSumRestriction(boolean indispensable, List<UnknownParameter> regressorsA, List<UnknownParameter> regressorsB, double exponent, UnknownParameter regressand) {
		this(indispensable, regressorsA, regressorsB, exponent, Boolean.TRUE, regressand);
	}
	
	public ProductSumRestriction(boolean indispensable, List<UnknownParameter> regressorsA, List<UnknownParameter> regressorsB, boolean sumArithmetic, UnknownParameter regressand) {
		this(indispensable, regressorsA, regressorsB, 1.0, sumArithmetic, regressand);
	}
	
	/**
	 * (A1 * B1 +/- A2 * B2 +/- ... +/- An * Bn) ^ (EXP)  ==  C
	 */
	public ProductSumRestriction(boolean indispensable, List<UnknownParameter> regressorsA, List<UnknownParameter> regressorsB, double exponent, boolean sumArithmetic, UnknownParameter regressand) {
		super(RestrictionType.PRODUCT_SUM, indispensable);

		this.setRegressand(regressand);
		this.regressorsA.setAll(regressorsA);
		this.regressorsB.setAll(regressorsB);
		this.setExponent(exponent);
		this.setSumArithmetic(sumArithmetic);
		
		this.check();
	}
	
	public void setExponent(double exponent) {
		this.exponent.set(exponent);
	}
	
	public double getExponent() {
		return this.exponent.get();
	}
	
	public ObjectProperty<Double> exponentProperty() {
		return this.exponent;
	}
	
	public void setSumArithmetic(boolean sumArithmetic) {
		this.sumArithmetic.set(sumArithmetic);
	}
	
	public boolean isSumArithmetic() {
		return this.sumArithmetic.get();
	}
	
	public ObjectProperty<Boolean> sumArithmeticProperty() {
		return this.sumArithmetic;
	}
	
	public ObservableList<UnknownParameter> getRegressorsA() {
		return this.regressorsA;
	}
	
	public ObservableList<UnknownParameter> getRegressorsB() {
		return this.regressorsB;
	}
	
	@Override
	public double getMisclosure() {
		this.check();
		
		double sign = this.isSumArithmetic() ? +1.0 : -1.0;
		double d = 0;
		int length = this.regressorsA.size();
		for (int i = 0; i < length; i++)
			d += (i == 0 ? +1.0 : sign) * this.regressorsA.get(i).getValue() * this.regressorsB.get(i).getValue();

		return Math.pow(d, this.exponent.get()) - this.regressand.get().getValue();
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		this.check();
		
		int rowIndex = this.getRow();
		double sign = this.isSumArithmetic() ? +1.0 : -1.0;
		int length = this.regressorsA.size();
		
		// inner part
		for (int i = 0; i < length; i++) {
			// (A1 * B1 +/- A2 * B2 +/- ... +/- An * Bn) ^ (EXP)  ==  C
			if (this.regressorsA.get(i).getColumn() >= 0) 
				JrT.add(this.regressorsA.get(i).getColumn(), rowIndex, (i == 0 ? +1.0 : sign) * this.regressorsB.get(i).getValue());
			
			if (this.regressorsB.get(i).getColumn() >= 0) 
				JrT.add(this.regressorsB.get(i).getColumn(), rowIndex, (i == 0 ? +1.0 : sign) * this.regressorsA.get(i).getValue());
		}
		
		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
		
		// inner and outer
		if (this.exponent.get() != 1) {
			// outer part
			double outer = 0;
			for (int i = 0; i < length; i++)
				outer += (i == 0 ? +1.0 : sign) * this.regressorsA.get(i).getValue() * this.regressorsB.get(i).getValue();
			outer = this.exponent.get() * Math.pow(outer, this.exponent.get() - 1.0);

			// inner * outer
			for (int columnIndex = 0; columnIndex < JrT.numRows(); columnIndex++) 
				JrT.set(columnIndex, rowIndex, JrT.get(columnIndex, rowIndex) * outer);
		}
	}

	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.regressorsA.contains(object) || this.regressorsB.contains(object);
	}
	
	@Override
	public String toLaTex() {
		return "$\\left(a_1 b_1 \\pm a_i b_i \\pm \\cdots \\pm a_n b_n\\right)^{k} = c$";
	}
	
	private void check() {
		if (this.regressorsA.size() != this.regressorsB.size())
			throw new IllegalArgumentException("Error, unequal size of factorsA and factorsB " + this.regressorsA.size() + " != " + this.regressorsB.size());
	}
}