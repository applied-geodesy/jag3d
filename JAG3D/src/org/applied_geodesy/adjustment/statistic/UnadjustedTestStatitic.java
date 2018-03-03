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

public class UnadjustedTestStatitic extends TestStatistic {
	private final double alpha, beta;
	
	public UnadjustedTestStatitic() {
		this(0.1, 80.0);
	}
	
	public UnadjustedTestStatitic(double alpha, double beta) {
		super(TestStatisticType.NONE, alpha, beta);
		this.beta  = beta;
		this.alpha = alpha;
	}

	@Override
	public TestStatisticParameterSet[] adjustTestStatisticParameters(TestStatisticParameterSet[] testStatisticParameterSet) {
		int l = testStatisticParameterSet.length;

		for (int i=0; i<l; i++) {
			TestStatisticParameterSet parameter = testStatisticParameterSet[i];
			double n2 = parameter.getNumeratorDof();
			double m2 = parameter.getDenominatorDof();

			parameter.setProbabilityValue(this.alpha);
			parameter.setPowerOfTest(this.beta);
			if (Double.isInfinite(m2)) {
				double quantile	= TestStatistic.getQuantile(n2, alpha);
				double ncp      = TestStatistic.getNoncentralityParameter(n2, alpha, this.beta);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2);
				parameter.setQuantile(quantile);
				parameter.setNoncentralityParameter(ncp);
				parameter.setLogarithmicProbabilityValue(logP);
			}
			else {
				double quantile	= TestStatistic.getQuantile(n2, m2, alpha);
				double ncp      = TestStatistic.getNoncentralityParameter(n2, m2, alpha, this.beta);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2, m2);
				parameter.setQuantile(quantile);
				parameter.setNoncentralityParameter(ncp);
				parameter.setLogarithmicProbabilityValue(logP);
			}
		}
		return testStatisticParameterSet;
	}
	
	public static void main(String args[]) {
		int dof = 10000000;

		UnadjustedTestStatitic bMeth = new UnadjustedTestStatitic(0.1, 80.0);
		
		TestStatisticParameterSet set[] = new TestStatisticParameterSet[] {
				new TestStatisticParameterSet(1, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(1, dof-1),
				
				new TestStatisticParameterSet(2, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(2, dof-2),
				
				new TestStatisticParameterSet(3, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(3, dof-3),
				
				new TestStatisticParameterSet(dof, Double.POSITIVE_INFINITY),
		};
		bMeth.adjustTestStatisticParameters(set);
		
		for (TestStatisticParameterSet s : set)
			System.out.println(s);
		
	}

}
