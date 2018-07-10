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

public class TestStatisticParameterSet {
	private double quantile, ncp, alpha, beta, logP;
	private final double f1, f2;
	private final boolean isGlobalTestStatistic;
	
	public TestStatisticParameterSet(double f1, double f2, boolean isGlobalTestStatistic) {
		this.f1 = f1;
		this.f2 = f2;
		this.isGlobalTestStatistic = isGlobalTestStatistic;
	}
	public TestStatisticParameterSet(double f1, double f2) {
		this(f1, f2, Boolean.FALSE);
	}
	public double getQuantile() {
		return this.quantile;
	}
	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}
	public double getNoncentralityParameter() {
		return this.ncp;
	}
	public void setNoncentralityParameter(double ncp) {
		this.ncp = ncp;
	}
	public double getProbabilityValue() {
		return this.alpha;
	}
	public void setProbabilityValue(double alpha) {
		this.alpha = alpha;
	}
	public double getPowerOfTest() {
		return this.beta;
	}
	public void setPowerOfTest(double beta) {
		this.beta = beta;
	}
	public double getNumeratorDof() {
		return this.f1;
	}
	public double getDenominatorDof() {
		return this.f2;
	}
	public void setLogarithmicProbabilityValue(double logP) {
		this.logP = logP;
	}
	public double getLogarithmicProbabilityValue() {
		return this.logP;
	}
	public boolean isGlobalTestStatistic() {
		return this.isGlobalTestStatistic;
	}
	@Override
	public String toString() {
		return "TestStatisticParameterSet [quantile=" + quantile + ", ncp="
				+ ncp + ", alpha=" + alpha + ", beta=" + beta + ", logP="
				+ logP + ", f1=" + f1 + ", f2=" + f2 + "]";
	}
}