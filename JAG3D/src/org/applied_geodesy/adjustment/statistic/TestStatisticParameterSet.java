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

package org.applied_geodesy.adjustment.statistic;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;

public class TestStatisticParameterSet {
	private ObjectProperty<Double> quantile               = new SimpleObjectProperty<Double>(this, "quantile");
	private ObjectProperty<Double> noncentralityParameter = new SimpleObjectProperty<Double>(this, "noncentralityParameter");
	private ObjectProperty<Double> probabilityValue       = new SimpleObjectProperty<Double>(this, "probabilityValue");
	private ObjectProperty<Double> powerOfTest            = new SimpleObjectProperty<Double>(this, "powerOfTest");
	private ObjectProperty<Double> logPvalue              = new SimpleObjectProperty<Double>(this, "logPvalue");

	private final ReadOnlyObjectProperty<Double> f1, f2;
	private final ReadOnlyObjectProperty<Boolean> globalTestStatistic;
	
	public TestStatisticParameterSet(double f1, double f2, boolean globalTestStatistic) {
		this.f1 = new ReadOnlyObjectWrapper<Double>(this, "numeratorDof",   f1);
		this.f2 = new ReadOnlyObjectWrapper<Double>(this, "denominatorDof", f2);
		this.globalTestStatistic = new ReadOnlyObjectWrapper<Boolean>(this, "globalTestStatistic", globalTestStatistic);
	}
	
	public TestStatisticParameterSet(double f1, double f2) {
		this(f1, f2, Boolean.FALSE);
	}
	
	public ObservableObjectValue<Double> numeratorDofProperty() {
		return this.f1;
	}

	/**
	 * Returns degree of freedom of numerator f1 of F distribution 
	 * @return numerator
	 */
	public double getNumeratorDof() {
		return this.f1.get();
	}
	
	public ObservableObjectValue<Double> denominatorDofProperty() {
		return this.f2;
	}
	
	/**
	 * Returns degree of freedom of denominator f2 of F distribution 
	 * @return numerator
	 */
	public double getDenominatorDof() {
		return this.f2.get();
	}
	
	public ObservableObjectValue<Double> quantileProperty() {
		return this.quantile;
	}
	
	/**
	 * Returns quantile q of F(f1,f2) distribution
	 * @return
	 */
	public double getQuantile() {
		return this.quantile.get();
	}
	
	/**
	 * Sets quantile q of F(f1,f2) distribution
	 * @param quantile
	 */
	public void setQuantile(double quantile) {
		this.quantile.set(quantile);
	}
	
	public ObservableObjectValue<Double> noncentralityParameterProperty() {
		return this.noncentralityParameter;
	}
	
	/**
	 * Returns noncentrality parameter of F
	 * @return noncentralityParameter
	 */
	public double getNoncentralityParameter() {
		return this.noncentralityParameter.get();
	}
	
	/**
	 * Sets noncentrality parameter of F
	 * @param noncentralityParameter
	 */
	public void setNoncentralityParameter(double noncentralityParameter) {
		this.noncentralityParameter.set(noncentralityParameter);
	}
	
	public ObservableObjectValue<Double> probabilityValueProperty() {
		return this.probabilityValue;
	}
	
	/**
	 * Returns probability value &alpha;
	 * @return alpha
	 */
	public double getProbabilityValue() {
		return this.probabilityValue.get();
	}
	
	/**
	 * Sets probability value &alpha;
	 * @param alpha probability
	 */
	public void setProbabilityValue(double alpha) {
		this.probabilityValue.set(alpha);
	}
	
	public ObservableObjectValue<Double> powerOfTestProperty() {
		return this.powerOfTest;
	}
	
	/**
	 * Returns test power &beta;
	 * @return beta
	 */
	public double getPowerOfTest() {
		return this.powerOfTest.get();
	}
	
	/**
	 * Sets test power &beta;
	 * @return beta power
	 */
	public void setPowerOfTest(double beta) {
		this.powerOfTest.set(beta);
	}
	
	public ObservableObjectValue<Double> logarithmicProbabilityValueProperty() {
		return this.logPvalue;
	}
	
	/**
	 * Returns p-Value in log representation, i.e., log(p)
	 * @return log(p)
	 */
	public double getLogarithmicProbabilityValue() {
		return this.logPvalue.get();
	}
	
	/**
	 * Sets p-Value in log representation, i.e., log(p)
	 * @param logP log(p)
	 */
	public void setLogarithmicProbabilityValue(double logP) {
		this.logPvalue.set(logP);
	}
	
	public ObservableObjectValue<Boolean> globalTestStatisticProperty() {
		return this.globalTestStatistic;
	}
	
	/**
	 * Returns true, if test statistic relates to global/family-wise test
	 * @return familyWise
	 */
	public boolean isGlobalTestStatistic() {
		return this.globalTestStatistic.get();
	}

	@Override
	public String toString() {
		return "TestStatisticParameterSet [quantile=" + getQuantile() + ", noncentralityParameter=" + getNoncentralityParameter()
				+ ", probabilityValue=" + getProbabilityValue() + ", powerOfTest=" + getPowerOfTest() + ", logPvalue=" + getLogarithmicProbabilityValue()
				+ ", d1=" + getNumeratorDof() + ", d2=" + getDenominatorDof() + ", globalTestStatistic=" + isGlobalTestStatistic() + "]";
	}
}