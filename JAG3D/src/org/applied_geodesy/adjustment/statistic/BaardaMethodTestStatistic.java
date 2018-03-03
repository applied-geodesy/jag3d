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

import org.applied_geodesy.adjustment.Constant;

public class BaardaMethodTestStatistic extends TestStatistic {
	private final double alpha, beta, n1, m1;
	private double ncp = -1;
	
	public BaardaMethodTestStatistic() {
		this(1, 0.1, 80.0);
	}
	
	public BaardaMethodTestStatistic(double n1, double alpha, double beta) {
		this(n1, Double.POSITIVE_INFINITY, alpha, beta);
	}
	
	public BaardaMethodTestStatistic(double n1, double m1, double alpha, double beta) {
		super(TestStatisticType.BAARDA_METHOD, alpha, beta);
		this.alpha  = alpha;
		this.beta   = beta;
		this.n1     = n1;
		this.m1     = m1;
		this.ncp = Double.isInfinite(this.m1) ? TestStatistic.getNoncentralityParameter(this.n1, this.alpha, this.beta) : TestStatistic.getNoncentralityParameter(this.n1, this.m1, this.alpha, this.beta);
	}
		
	public TestStatisticParameterSet[] adjustTestStatisticParameters (TestStatisticParameterSet testStatisticParameterSet[]) {
		int l = testStatisticParameterSet.length;	
		
		for (int i=0; i<l; i++) {
			TestStatisticParameterSet parameter = testStatisticParameterSet[i];
			double n2 = parameter.getNumeratorDof();
			double m2 = parameter.getDenominatorDof();

			parameter.setNoncentralityParameter(this.ncp);
			parameter.setPowerOfTest(this.beta);
			
			if (Double.isInfinite(m2)) {
				double quantile = this.n1 == n2 && Double.isInfinite(this.m1) ? TestStatistic.getQuantile(n2, this.alpha) : TestStatistic.getQuantileViaNCP(n2, this.ncp, this.beta);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2);
				double alpha    = this.n1 == n2 && Double.isInfinite(this.m1) ? this.alpha : TestStatistic.getProbabilityValue(quantile, n2);				
				alpha = alpha <= 0.0 ? Constant.EPS : alpha >= 100.0 ? 100.0-Math.sqrt(Constant.EPS) : alpha;
				parameter.setProbabilityValue(alpha);
				parameter.setQuantile(quantile);
				parameter.setLogarithmicProbabilityValue(logP);
			}
			else {
				double quantile = this.n1 == n2 && this.m1 == m2 ? TestStatistic.getQuantile(n2, m2, this.alpha) : TestStatistic.getQuantileViaNCP(n2, m2, this.ncp, this.beta);
				double logP     = TestStatistic.getLogarithmicProbabilityValue(quantile, n2, m2);
				double alpha    = this.n1 == n2 && this.m1 == m2 ? this.alpha : TestStatistic.getProbabilityValue(quantile, n2, m2);
				alpha = alpha <= 0.0 ? Constant.EPS : alpha >= 100.0 ? 100.0-Math.sqrt(Constant.EPS) : alpha;
				parameter.setProbabilityValue(alpha);
				parameter.setQuantile(quantile);
				parameter.setLogarithmicProbabilityValue(logP);
			}
		}
		
		return testStatisticParameterSet;
	}
		
	public static void main(String args[]) {
		int dof = 100;
		BaardaMethodTestStatistic bMeth = new BaardaMethodTestStatistic(1, 0.1, 80.0);
		
		TestStatisticParameterSet set[] = new TestStatisticParameterSet[] {
				new TestStatisticParameterSet(1, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(1, dof-1),
				
				new TestStatisticParameterSet(2, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(2, dof-2),
				
				new TestStatisticParameterSet(3, Double.POSITIVE_INFINITY),
				new TestStatisticParameterSet(3, dof-3),
		};
		bMeth.adjustTestStatisticParameters(set);
		
		for (TestStatisticParameterSet s : set)
			System.out.println(s);
	}
}
