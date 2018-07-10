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

public class SidakTestStatistic extends TestStatistic {
	private final double alphaGlobal, alphaLocal, beta;

	private final int numberOfIndependentHypothesis;
	private double ncp = -1;
	
	public SidakTestStatistic() {
		this(Integer.MAX_VALUE, 0.1, 80.0, false);
	}
	
	public SidakTestStatistic(int numberOfIndependentHypothesis, double alpha, double beta, boolean isAlphaGlobal) {
		super(TestStatisticType.SIDAK, alpha, beta);
		this.beta = beta;
		this.numberOfIndependentHypothesis = numberOfIndependentHypothesis;
		
		if (isAlphaGlobal && this.numberOfIndependentHypothesis > 0) {
			this.alphaGlobal = alpha;
			double d = 1.0 - alpha * 0.01;
			this.alphaLocal = (1.0 - Math.pow(d, 1.0 / this.numberOfIndependentHypothesis)) * 100.0;
			this.ncp = TestStatistic.getNoncentralityParameter(this.numberOfIndependentHypothesis, alpha, this.beta);
		}
		else if (!isAlphaGlobal && this.numberOfIndependentHypothesis > 0) {
			this.alphaLocal = alpha;
			double d = 1.0 - alpha * 0.01;
			this.alphaGlobal = (1.0 - Math.pow(d, this.numberOfIndependentHypothesis)) * 100.0;
			this.ncp = TestStatistic.getNoncentralityParameter(1.0, alpha, this.beta);
		}
		else {
			this.alphaGlobal = this.alphaLocal = alpha;
			this.ncp = TestStatistic.getNoncentralityParameter(1.0, alpha, this.beta);
		}
	}

	@Override
	public TestStatisticParameterSet[] adjustTestStatisticParameters(TestStatisticParameterSet[] testStatisticParameterSet) {
		for (int i = 0; i < testStatisticParameterSet.length; i++) {
			TestStatisticParameterSet parameter = testStatisticParameterSet[i];
			double n2 = parameter.getNumeratorDof();
			double m2 = parameter.getDenominatorDof();

			double alpha = this.alphaLocal;
			
			if (parameter.isGlobalTestStatistic() || n2 >= this.numberOfIndependentHypothesis)
				alpha = this.alphaGlobal;
				
			parameter.setNoncentralityParameter(this.ncp);
			parameter.setProbabilityValue(alpha);
			
			if (Double.isInfinite(m2)) {
				double quantile	= TestStatistic.getQuantile(n2, alpha);
				double beta     = TestStatistic.getPowerOfTest(quantile, n2, this.ncp);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2);
				parameter.setQuantile(quantile);
				parameter.setPowerOfTest(beta);
				parameter.setLogarithmicProbabilityValue(logP);
			}
			else {
				double quantile	= TestStatistic.getQuantile(n2, m2, alpha);
				double beta     = TestStatistic.getPowerOfTest(quantile, n2, m2, this.ncp);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2, m2);
				parameter.setQuantile(quantile);
				parameter.setPowerOfTest(beta);
				parameter.setLogarithmicProbabilityValue(logP);
			}
		}
		return testStatisticParameterSet;
	}

	public static void main(String args[]) {
		int numberOfIndependentHypothesis = 100;

		SidakTestStatistic testStatistic = new SidakTestStatistic(numberOfIndependentHypothesis, 0.1, 80.0, false);
		
		TestStatisticParameterSet set[] = new TestStatisticParameterSet[] {
				new TestStatisticParameterSet(1, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(1, numberOfIndependentHypothesis - 1),
				
				new TestStatisticParameterSet(2, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(2, numberOfIndependentHypothesis - 2),
				
				new TestStatisticParameterSet(3, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(3, numberOfIndependentHypothesis - 3),	
				
				new TestStatisticParameterSet(numberOfIndependentHypothesis, Double.POSITIVE_INFINITY),
		};
		testStatistic.adjustTestStatisticParameters(set);
		for (TestStatisticParameterSet s : set)
			System.out.println(s);
	}
}
