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

package org.applied_geodesy.adjustment.transformation;

import org.applied_geodesy.adjustment.Constant;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TestStatistic {
	private ObjectProperty<VarianceComponent> varianceComponent = new SimpleObjectProperty<VarianceComponent>(this, "varianceComponent", new VarianceComponent());
	
	private ObjectProperty<Double> fisherTestNumerator  = new SimpleObjectProperty<Double>(this, "fisherTestNumerator", 0.0);
	private ObjectProperty<Integer> degreeOfFreedom     = new SimpleObjectProperty<Integer>(this, "degreeOfFreedom", 0);
	private ObjectProperty<Boolean> applyBiasCorrection = new SimpleObjectProperty<Boolean>(this, "applyBiasCorrection", Boolean.TRUE);
	
	private ObjectBinding<Double> testStatisticApriori;
	private ObjectBinding<Double> testStatisticAposteriori;
	
	private ObjectBinding<Double> pValueApriori;
	private ObjectBinding<Double> pValueAposteriori;
	
	public TestStatistic() {
		this.testStatisticApriori = new ObjectBinding<Double>() {
			{
                super.bind(fisherTestNumerator, degreeOfFreedom);
            }
 
            @Override
            protected Double computeValue() {
            	double sigma2aprio = varianceComponent.get().getVariance0();
                return degreeOfFreedom.get() > 0 ? Math.abs(fisherTestNumerator.get() / (double)degreeOfFreedom.get() / sigma2aprio) : 0.0;
            }
        };
        
        this.pValueApriori = new ObjectBinding<Double>() {
			{
                super.bind(testStatisticApriori, degreeOfFreedom);
            }
 
            @Override
            protected Double computeValue() {
                return org.applied_geodesy.adjustment.statistic.TestStatistic.getLogarithmicProbabilityValue(testStatisticApriori.get(), degreeOfFreedom.get());
            }
        };
        
        this.testStatisticAposteriori = new ObjectBinding<Double>() {
			{
                super.bind(applyBiasCorrection, fisherTestNumerator, degreeOfFreedom, varianceComponent);
            }
 
            @Override
            protected Double computeValue() {
            	if (degreeOfFreedom.get() == 0 || fisherTestNumerator.get() == 0 ||	!varianceComponent.get().isApplyAposterioriVarianceOfUnitWeight())
        			return 0.0;

            	double unbiasedVariance = 0.0;
            	
            	if (applyBiasCorrection.get()) {
            		double omega       = varianceComponent.get().getOmega();
            		double redundancy  = varianceComponent.get().getRedundancy();

            		if (redundancy - (double)degreeOfFreedom.get() <= 0)
            			return 0.0;
            		
            		unbiasedVariance = (omega - fisherTestNumerator.get()) / (redundancy - (double)degreeOfFreedom.get());
            	}
            	else
            		unbiasedVariance = varianceComponent.get().getVariance();
            	
        		if (unbiasedVariance < Math.sqrt(Constant.EPS))
        			return 0.0;
        		
        		return degreeOfFreedom.get() > 0 ? Math.abs(fisherTestNumerator.get() / (unbiasedVariance * (double)degreeOfFreedom.get())) : 0.0; 
            }
        };
        
        this.pValueAposteriori = new ObjectBinding<Double>() {
			{
                super.bind(testStatisticAposteriori, degreeOfFreedom);
            }
 
            @Override
            protected Double computeValue() {
            	if (degreeOfFreedom.get() == 0 || fisherTestNumerator.get() == 0 ||	!varianceComponent.get().isApplyAposterioriVarianceOfUnitWeight())
        			return 0.0;
            	
            	double redundancy = varianceComponent.get().getRedundancy();
                return org.applied_geodesy.adjustment.statistic.TestStatistic.getLogarithmicProbabilityValue(testStatisticApriori.get(), degreeOfFreedom.get(), redundancy - (double)degreeOfFreedom.get());
            }
        };
	}

	public void setVarianceComponent(VarianceComponent varianceComponent) {
		this.varianceComponent.set(varianceComponent);
	}
	
	public void setFisherTestNumerator(double numerator) {
		this.fisherTestNumerator.set(numerator);
	}
	
	public void setDegreeOfFreedom(int degreeOfFreedom) {
		this.degreeOfFreedom.set(degreeOfFreedom);
	}
	
	public void setApplyBiasCorrection(boolean applyBiasCorrection) {
		this.applyBiasCorrection.set(applyBiasCorrection);
	}
	
	public double getPValueApriori() {
		return this.pValueApriori.get();
	}
	
	public double getPValueAposteriori() {
		return this.pValueAposteriori.get();
	}
	
	public double getTestStatisticApriori() {
		//return this.degreeOfFreedom > 0 ? this.fisherTestNumerator / this.degreeOfFreedom : 0;
		return testStatisticApriori.get();
	}
	
	public double getTestStatisticAposteriori() {
//		if (this.degreeOfFreedom == 0 || this.fisherTestNumerator == 0 ||
//				!this.varianceComponent.isApplyAposterioriVarianceOfUnitWeight())
//			return 0;
//		
//		double omega = this.varianceComponent.getOmega();
//		double redundancy = this.varianceComponent.getRedundancy();
//		
//		double unbiasedVariance = (omega - this.fisherTestNumerator) / (redundancy - this.degreeOfFreedom);
//		if (unbiasedVariance < Math.sqrt(Constant.EPS))
//			return 0;
//		
//		return this.degreeOfFreedom > 0 ? this.fisherTestNumerator / (unbiasedVariance * this.degreeOfFreedom) : 0; 
		return testStatisticAposteriori.get();
	}
	
	public ObjectProperty<Double> fisherTestNumeratorProperty() {
		return this.fisherTestNumerator;
	}
	
	public ObjectProperty<Integer> degreeOfFreedomProperty() {
		return this.degreeOfFreedom;
	}
	
	public ObjectProperty<Boolean> applyBiasCorrectionProperty() {
		return this.applyBiasCorrection;
	}
		
	public ObjectProperty<VarianceComponent> varianceComponentProperty() {
		return this.varianceComponent;
	}
	
	public ObjectBinding<Double> testStatisticAprioriProperty() {
		return this.testStatisticApriori;
	}
	
	public ObjectBinding<Double> testStatisticAposterioriProperty() {
		return this.testStatisticAposteriori;
	}
	
	public ObjectBinding<Double> pValueAprioriProperty() {
		return this.pValueApriori;
	}
	
	public ObjectBinding<Double> pValueAposterioriProperty() {
		return this.pValueAposteriori;
	}
}
