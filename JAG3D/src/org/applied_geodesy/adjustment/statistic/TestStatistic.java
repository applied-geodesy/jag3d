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

import jdistlib.ChiSquare;
import jdistlib.F;
import jdistlib.NonCentralChiSquare;
import jdistlib.NonCentralF;
import jdistlib.Normal;

public abstract class TestStatistic {
	private final TestStatisticType type;
	private final double alpha, beta;
	public TestStatistic (TestStatisticType type, double alpha, double beta) {
		this.type = type;
		this.alpha = alpha;
		this.beta = beta;
	};
	
	public double getProbabilityValue() {
		return this.alpha;
	}
	
	public double getTestPower() {
		return this.beta;
	}
	
	public TestStatisticType getTestStatisticType() {
		return this.type;
	}
	
	public TestStatisticParameterSet adjustTestStatisticParameter (TestStatisticParameterSet testStatisticParameterSet) {
		TestStatisticParameterSet[] set = new TestStatisticParameterSet[] { testStatisticParameterSet };
		this.adjustTestStatisticParameters(set);
		return set[0];
	}
	
	/**
	 * Bestimmt die Parameter der Teststatistik
	 * @param testStatisticParameterSet
	 * @return testStatisticParameterSet
	 */
	public abstract TestStatisticParameterSet[] adjustTestStatisticParameters (TestStatisticParameterSet testStatisticParameterSet[]);

	/**
	 * Liefert das F-Quantil fuer F'(n2, &infin;, ncp, &beta;)
	 * @param n2
	 * @param ncp
	 * @param beta
	 * @return q
	 */
	public static double getQuantileViaNCP(double n2, double ncp, double beta) {
		if (n2 <= 0)
			return Double.POSITIVE_INFINITY;
		return NonCentralChiSquare.quantile(1.0-0.01*beta, n2, ncp, true, false)/n2;
	}
	
	/**
	 * Liefert das F-Quantil fuer F'(n2, m2, ncp, &beta;)
	 * @param n2
	 * @param m2
	 * @param ncp
	 * @param beta
	 * @return q
	 */
	public static double getQuantileViaNCP(double n2, double m2, double ncp, double beta) {
		if (n2 <= 0 || m2 <= 0)
			return Double.POSITIVE_INFINITY;
		
		if (Double.isInfinite(ncp))
			return TestStatistic.getQuantileViaNCP(n2, ncp, beta);
		
		return NonCentralF.quantile(1.0-0.01*beta, n2, m2, ncp, true, false);
	}
	
	/**
	 * Liefert das F-Quantil fuer F(n1, &infin;, &alpha;)
	 * @param n1
	 * @param alpha
	 * @return q
	 */
	public static double getQuantile(double n1, double alpha) {
		if (n1 <= 0)
			return Double.POSITIVE_INFINITY;
		return ChiSquare.quantile(1.0-0.01*alpha, n1, true, false)/n1;
	}
	
	/**
	 * Liefert das F-Quantil fuer F(n1, m1, &alpha;)
	 * @param n1
	 * @param m1
	 * @param alpha
	 * @return q
	 */
	public static double getQuantile(double n1, double m1, double alpha) {
		if (n1 <= 0 || m1 <= 0) 
			return Double.POSITIVE_INFINITY;
		
		if (Double.isInfinite(m1))
			return TestStatistic.getQuantile(n1, alpha);
		
		return F.quantile(1.0-0.01*alpha, n1, m1, true, false);
	}
	
	/**
	 * Liefert das auf F(n1, &infin;, &alpha;) abgestimmte F-Quantil fuer F(n2, &infin;, &alpha;')
	 * @param n1
	 * @param n2
	 * @param alpha
	 * @param beta
	 * @return q
	 */
	public static double getQuantile(double n1, double n2, double alpha, double beta) {
		if (n1 <= 0 || n2 <= 0)
			return Double.POSITIVE_INFINITY;
		
		if (n1 == n2) {
			return ChiSquare.quantile(1.0-0.01*alpha, n1, true, false)/n1;
			//return F.quantile(1.0-0.01*alpha, n1, 1000000, true, false);
		}
		else {
			//double ncp = TestStatistic.getNoncentralityParameter(n1, 1000000, alpha, beta);
			double ncp = TestStatistic.getNoncentralityParameter(n1, alpha, beta);
			if (Double.isInfinite(ncp))
				return Double.POSITIVE_INFINITY;
			return NonCentralChiSquare.quantile(1.0-0.01*beta, n2, ncp, true, false)/n2;
		}
	}
	
	/**
	 * Liefert das auf F(n1, &infin;, &alpha;) abgestimmte F-Quantil fuer F(n2, m2, &alpha;')
	 * @param n1
	 * @param n2
	 * @param m2
	 * @param alpha
	 * @param beta
	 * @return q
	 */
	public static double getQuantile(double n1, double n2, double m2, double alpha, double beta){	
		if (m2 <= 0 || n1 <= 0 || n2 <= 0) 
			return Double.POSITIVE_INFINITY;
		
		if (Double.isInfinite(m2))
			return TestStatistic.getQuantile(n1, n2, alpha, beta);

		double ncp = TestStatistic.getNoncentralityParameter(n1, alpha, beta);
		if (Double.isInfinite(ncp))
			return Double.POSITIVE_INFINITY;

		return NonCentralF.quantile(1.0-0.01*beta, n2, m2, ncp, true, false);
	}
	
	/**
	 * Liefert das auf F(n1, m1, &alpha;) abgestimmte F-Quantil fuer F(n2, m2, &alpha;')
	 * @param n1
	 * @param m1
	 * @param n2
	 * @param m2
	 * @param alpha
	 * @param beta
	 * @return q
	 */
	public static double getQuantile(double n1, double m1, double n2, double m2, double alpha, double beta){	
		if (m2 <= 0 || n1 <= 0 || n2 <= 0) 
			return Double.POSITIVE_INFINITY;

		double ncp = TestStatistic.getNoncentralityParameter(n1, m1, alpha, beta);
		if (Double.isInfinite(ncp))
			return Double.POSITIVE_INFINITY;
		
		return NonCentralF.quantile(1.0-0.01*beta, n2, m2, ncp, true, false);
	}

	/**
	 * Liefert den Nichtzentralitaetsparameter &lamda;(n, &infin;, &alpha;, &beta;)
	 * 
	 * C. Aydin, H. Demirel: Computation of Baarda's lower bound of the non-centrality parameter. JGeo, p437-441, 2004.
	 * 
	 * @param n
	 * @param alpha
	 * @param beta
	 * @return &lamda;
	 */
	public static double getNoncentralityParameter(double n, double alpha, double beta){
		if (n <= 0) 
			return 0.0;
	
		alpha = Math.max(alpha, 1.0E-10);
		beta  = 0.01*beta;
		if (n == 1)
			return Math.pow(
						Normal.quantile(1.0-0.005*alpha, 0.0, 1.0, true, false) + 
						Normal.quantile(beta, 0.0, 1.0, true, false), 
					2);
		
		int itr = 0;
		double EPS_APPROX = 0.0001;
		double EPS_NEWTON = EPS_APPROX*EPS_APPROX;
		double q1 = ChiSquare.quantile(1.0-0.01*alpha, n, true, false);

		double lowerNCP = 0;
		double upperNCP = 50;

		double x = Math.pow(2.0*(n+upperNCP)*q1/(n+2.0*upperNCP), 0.5) - Math.pow(2.0*(n+upperNCP)*(n+upperNCP)/(n+2.0*upperNCP)-1.0, 0.5);
		if (Double.isInfinite(x) || Double.isNaN(x))
			return Double.POSITIVE_INFINITY;
		
		while (Normal.cumulative(x, 0.0, 1.0, false, false) < beta) {
			upperNCP += 50;
			x = Math.pow(2.0*(n+upperNCP)*q1/(n+2*upperNCP), 0.5) - Math.pow(2.0*(n+upperNCP)*(n+upperNCP)/(n+2.0*upperNCP)-1.0, 0.5);
			if (Double.isInfinite(x) || Double.isNaN(x))
				return Double.POSITIVE_INFINITY;
		}
		lowerNCP = upperNCP - 50;

		while (Math.abs(upperNCP - lowerNCP) > EPS_APPROX && itr++ < 200) {
			double ncp = 0.5*(lowerNCP + upperNCP);
			x = Math.pow(2.0*(n+ncp)*q1/(n+2.0*ncp), 0.5) - Math.pow(2.0*(n+ncp)*(n+ncp)/(n+2.0*ncp)-1.0, 0.5);
			if (Double.isInfinite(x) || Double.isNaN(x))
				return Double.POSITIVE_INFINITY;
			
			if (Normal.cumulative(x, 0.0, 1.0, false, false) < beta)
				lowerNCP = ncp;
			else
				upperNCP = ncp;
		}
		
		double ncp = 0.5*(lowerNCP + upperNCP);
		double ncp0 = ncp - 1.0;

		itr = 0;
		while (Math.abs(ncp - ncp0) > EPS_NEWTON && itr++ < 200) {
			ncp0 = ncp;
			double f1 = NonCentralChiSquare.cumulative(q1, n, ncp, true, false);
			double f2 = -0.5*f1 + 0.5*NonCentralChiSquare.cumulative(q1, n+2, ncp, true, false);
			f1 -= (1.0-beta);
			ncp = ncp-f1/f2;
		}
		
		if (Math.abs(ncp - ncp0) > EPS_NEWTON || itr++ >= 200)
			System.err.println("TestStatitic, Fehler beim Bestimmen des Nichtzentralitaetsparameters! Iterationsanzahl = " + itr + ", epsilon = " + Math.abs(ncp - ncp0));
		
		return ncp;
	}
	
	/**
	 * Liefert den Nichtzentralitaetsparameter &lamda;(n, m, &alpha;, &beta;)
	 * 
	 * O. Heunecke u.a.: Auswertung geodaetischer Ueberwachungsmessungen. Wichmann, p.154-156, 2013.
	 * 
	 * @param n
	 * @param m
	 * @param alpha
	 * @param beta
	 * @return &lamda;
	 */
	public static double getNoncentralityParameter(double n, double m, double alpha, double beta) {
		if (n <= 0 || m <= 0) 
			return 0.0;
		
		if (Double.isInfinite(m))
			return TestStatistic.getNoncentralityParameter(n, alpha, beta); 
		
		alpha = Math.max(alpha, 1.0E-10);
		beta = 0.01*beta;
		
		double EPS_APPROX = 0.0001;
		double EPS = EPS_APPROX*EPS_APPROX;
		int itr = 0;
		
		double q1 = F.quantile(1.0-0.01*alpha, n, m, true, false);
		double lowerNCP = 0;
		double upperNCP = 50;
		
		double n1 = (n+upperNCP)*(n+upperNCP)/((n+2.0*upperNCP));
		double f2 = n/(n+upperNCP)*q1;
		
		double beta2 = F.cumulative(f2, n1, m, false, false);
		while (beta2 < beta) {
			upperNCP += 50;
			n1 = (n+upperNCP)*(n+upperNCP)/((n+2.0*upperNCP));
			f2 = n/(n+upperNCP)*q1;
			beta2 = F.cumulative(f2, n1, m, false, false);
		}
		lowerNCP = upperNCP - 50; 
		
//		while (Math.abs(beta2 - 0.01*beta) > EPS_APPROX && itr++ < 200) {
//			double ncp = 0.5*(lowerNCP + upperNCP);
//			n1 = (n+ncp)*(n+ncp)/((n+2.0*ncp));
//			f2 = n/(n+ncp)*q1;
//			beta2 = F.cumulative(f2, n1, m, false, false);
//			
//			if (beta2 > 0.01*beta) 
//				upperNCP = ncp;
//			else
//				lowerNCP = ncp;
//			System.out.println("1. "+beta2+"  "+ncp);
//		}

		itr = 0;
		while (Math.abs(beta2 - beta) > EPS && itr++ < 200) {
			double ncp = 0.5*(lowerNCP + upperNCP);
			beta2 = NonCentralF.cumulative(q1, n, m, ncp, false, false);			
			if (beta2 > beta) 
				upperNCP = ncp;
			else
				lowerNCP = ncp;
		}		

		if (Math.abs(beta2 - beta) > EPS || itr++ >= 200)
			System.err.println("TestStatitic, Fehler beim Bestimmen des Nichtzentralitaetsparameters! Iterationsanzahl = " + itr + ", epsilon = " + Math.abs(beta2 - beta));
		
		return 0.5*(lowerNCP + upperNCP);
	}
	
	/**
	 * Liefert die zum kritischen Wert gehoehrende Irrtumswahrscheinlichkeit &alpha; [%] der <em>F</em>-Verteilung.
	 * 
	 * Die Dimension des Nenners ist &infin;
	 * @param x    kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @return &alpha;(n1, &infin;) [%]
	 */
	public static double getProbabilityValue(double x, double n1) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0)
			return 100.0;
		return ChiSquare.cumulative(n1*x, n1, false, false)*100.0;
	}
	
	/**
	 * Liefert die zum kritischen Wert gehoehrende Irrtumswahrscheinlichkeit &alpha; [%] der <em>F</em>-Verteilung
	 * 
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param m1  Dimension des Nenners
	 * @return &alpha;(n1, m1) [%]
	 */
	public static double getProbabilityValue(double x, double n1, double m1) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0 || m1 <= 0)
			return 100.0;
		if (Double.isInfinite(m1))
			return TestStatistic.getProbabilityValue(x, n1);
		return F.cumulative(x, n1, m1, false, false)*100.0;
	}
	
	/**
	 * Liefert die Teststaerke &beta; [%] zurueck.
	 * 
	 * Die Dimension des Nenners ist &infin;
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param ncp Nicht-Zentralitaetsparameter
	 * @return &beta;(n1, &infin;, ncp) [%]
	 */
	public static double getPowerOfTest(double x, double n1, double ncp) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0)
			return 100.0;
		return NonCentralChiSquare.cumulative(n1*x, n1, ncp, false, false)*100.0;
	}
	
	/**
	 * Liefert die Teststaerke &beta; [%] zurueck.
	 * 
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param m1  Dimension des Nenners
	 * @return &beta;(n1, m1, ncp) [%]
	 */
	public static double getPowerOfTest(double x, double n1, double m1, double ncp) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0 || m1 <= 0)
			return 100.0;
		if (Double.isInfinite(m1))
			return TestStatistic.getPowerOfTest(x, n1, ncp);
		return NonCentralF.cumulative(x, n1, m1, ncp, false, false)*100.0;
	}
	
	/**
	 * Liefert die zum kritischen Wert gehoehrende Irrtumswahrscheinlichkeit log(&alpha;) der <em>F</em>-Verteilung.
	 * 
	 * Die Dimension des Nenners ist &infin;
	 * @param x    kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @return log(&alpha;(n1, &infin;))
	 */
	public static double getLogarithmicProbabilityValue(double x, double n1) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0)
			return 0.0;
		return ChiSquare.cumulative(n1*x, n1, false, true);
	}
	
	/**
	 * Liefert die zum kritischen Wert gehoehrende Irrtumswahrscheinlichkeit log(&alpha;) der <em>F</em>-Verteilung
	 * 
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param m1  Dimension des Nenners
	 * @return log(&alpha;(n1, m1))
	 */
	public static double getLogarithmicProbabilityValue(double x, double n1, double m1) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0 || m1 <= 0)
			return 0.0;
		if (Double.isInfinite(m1))
			return TestStatistic.getLogarithmicProbabilityValue(x, n1);
		return F.cumulative(x, n1, m1, false, true);
	}
	
	/**
	 * Liefert die Teststaerke log(&beta;) zurueck.
	 * 
	 * Die Dimension des Nenners ist &infin;
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param ncp Nicht-Zentralitaetsparameter
	 * @return log(&beta;(n1, &infin;, ncp))
	 */
	public static double getLogarithmicPowerOfTest(double x, double n1, double ncp) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0)
			return 0.0;
		return NonCentralChiSquare.cumulative(n1*x, n1, ncp, false, true);
	}
	
	/**
	 * Liefert die Teststaerke log(&beta;) zurueck.
	 * 
	 * @param x   kritischen Wert (Quantil)
	 * @param n1  Dimension des Zaehlers
	 * @param m1  Dimension des Nenners
	 * @return log(&beta;(n1, m1, ncp))
	 */
	public static double getLogarithmicPowerOfTest(double x, double n1, double m1, double ncp) {
		if (Double.isNaN(x) || Double.isInfinite(x) || x < 0 || n1 <= 0 || m1 <= 0)
			return 0.0;
		if (Double.isInfinite(m1))
			return TestStatistic.getLogarithmicPowerOfTest(x, n1, ncp);
		return NonCentralF.cumulative(x, n1, m1, ncp, false, true);
	}
}
