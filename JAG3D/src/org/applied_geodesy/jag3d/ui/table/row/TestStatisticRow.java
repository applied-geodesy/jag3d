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

package org.applied_geodesy.jag3d.ui.table.row;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TestStatisticRow extends Row {

	private ObjectProperty<Double> numeratorDegreeOfFreedom = new SimpleObjectProperty<Double>(this, "numeratorDegreeOfFreedom");
	private ObjectProperty<Double> denominatorDegreeOfFreedom = new SimpleObjectProperty<Double>(this, "denominatorDegreeOfFreedom");
	
	private ObjectProperty<Double> powerOfTest      = new SimpleObjectProperty<Double>(this, "powerOfTest");
	private ObjectProperty<Double> probabilityValue = new SimpleObjectProperty<Double>(this, "probabilityValue");
	
	private ObjectProperty<Double> noncentralityParameter = new SimpleObjectProperty<Double>(this, "noncentralityParameter");

	private ObjectProperty<Double> pValue = new SimpleObjectProperty<Double>(this, "pValue");
	private ObjectProperty<Double> quantile = new SimpleObjectProperty<Double>(this, "quantile");
	
	public final ObjectProperty<Double> numeratorDegreeOfFreedomProperty() {
		return this.numeratorDegreeOfFreedom;
	}
	
	public final Double getNumeratorDegreeOfFreedom() {
		return this.numeratorDegreeOfFreedomProperty().get();
	}
	
	public final void setNumeratorDegreeOfFreedom(final Double numeratorDegreeOfFreedom) {
		this.numeratorDegreeOfFreedomProperty().set(numeratorDegreeOfFreedom);
	}
	
	public final ObjectProperty<Double> denominatorDegreeOfFreedomProperty() {
		return this.denominatorDegreeOfFreedom;
	}
	
	public final Double getDenominatorDegreeOfFreedom() {
		return this.denominatorDegreeOfFreedomProperty().get();
	}
	
	public final void setDenominatorDegreeOfFreedom(final Double denominatorDegreeOfFreedom) {
		this.denominatorDegreeOfFreedomProperty().set(denominatorDegreeOfFreedom);
	}
	
	public final ObjectProperty<Double> powerOfTestProperty() {
		return this.powerOfTest;
	}
	
	public final Double getPowerOfTest() {
		return this.powerOfTestProperty().get();
	}
	
	public final void setPowerOfTest(final Double powerOfTest) {
		this.powerOfTestProperty().set(powerOfTest);
	}
	
	public final ObjectProperty<Double> probabilityValueProperty() {
		return this.probabilityValue;
	}
	
	public final Double getProbabilityValue() {
		return this.probabilityValueProperty().get();
	}
	
	public final void setProbabilityValue(final Double probabilityValue) {
		this.probabilityValueProperty().set(probabilityValue);
	}
	
	public final ObjectProperty<Double> noncentralityParameterProperty() {
		return this.noncentralityParameter;
	}
	
	public final Double getNoncentralityParameter() {
		return this.noncentralityParameterProperty().get();
	}
	
	public final void setNoncentralityParameter(final Double noncentralityParameter) {
		this.noncentralityParameterProperty().set(noncentralityParameter);
	}
	
	public final ObjectProperty<Double> pValueProperty() {
		return this.pValue;
	}
	
	public final Double getPValue() {
		return this.pValueProperty().get();
	}
	
	public final void setPValue(final Double pValue) {
		this.pValueProperty().set(pValue);
	}
	
	public final ObjectProperty<Double> quantileProperty() {
		return this.quantile;
	}
	
	public final Double getQuantile() {
		return this.quantileProperty().get();
	}
	
	public final void setQuantile(final Double quantile) {
		this.quantileProperty().set(quantile);
	}
}
