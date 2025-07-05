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

import org.applied_geodesy.adjustment.DefaultValue;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;

public class VarianceComponent {
	private ReadOnlyObjectProperty<VarianceComponentType> varianceComponentType;
	private ObjectProperty<Boolean> applyAposterioriVarianceOfUnitWeight = new SimpleObjectProperty<Boolean>(this, "applyAposterioriVarianceOfUnitWeight", DefaultValue.applyVarianceOfUnitWeight());
	private ObjectProperty<Double> variance0     = new SimpleObjectProperty<Double>(this, "variance0", 1.0);
	private ObjectProperty<Double> redundancy    = new SimpleObjectProperty<Double>(this, "redundancy", 0.0);
	private ObjectProperty<Integer> numberOfObservations = new SimpleObjectProperty<Integer>(this, "numberOfObservations", 0);
	private ObjectProperty<Double> omega         = new SimpleObjectProperty<Double>(this, "omega", 0.0);
	private ObjectProperty<Boolean> significant  = new SimpleObjectProperty<Boolean>(this, "significant", Boolean.FALSE);
	private ObjectBinding<Double> variance;
	
	private ObjectBinding<Double> unitVariance;
	private ObjectBinding<Double> unitOmega;
	
	public VarianceComponent(VarianceComponentType varianceComponentType) {
		this.varianceComponentType = new ReadOnlyObjectWrapper<VarianceComponentType>(this, "varianceComponentType", varianceComponentType);
		this.variance = new ObjectBinding<Double>() {
			{
                super.bind(redundancy, omega, variance0);
            }
 
            @Override
            protected Double computeValue() {
                return omega.get() > 0 && redundancy.get() > 0 ? omega.get() / redundancy.get() : variance0.get();
            }
        };
        
        this.unitVariance = new ObjectBinding<Double>() {
			{
                super.bind(variance, variance0);
            }
 
            @Override
            protected Double computeValue() {
                return variance.get() / variance0.get();
            }
        };
        
        this.unitOmega = new ObjectBinding<Double>() {
			{
                super.bind(omega, variance0);
            }
 
            @Override
            protected Double computeValue() {
                return omega.get() / variance0.get();
            }
        };
	}
	
	public ObservableObjectValue<VarianceComponentType> varianceComponentTypeProperty() {
		return this.varianceComponentType;
	}
	
	public double getVariance0() {
		return this.variance0.get();
	}
	
	public void setVariance0(double variance0) {
		this.variance0.set(variance0);
	}
	
	public double getUnitVariance() {
		return this.unitVariance.get();
	}
	
	public double getVariance() {
		return this.variance.get();
	}
	
	public double getUnitOmega() {
		return this.unitOmega.get();
	}
	
	public double getOmega() {
		return this.omega.get();
	}
	
	public void setOmega(double omega) {
		this.omega.set(omega);
	}
	
	public int getNumberOfObservations() {
		return this.numberOfObservations.get();
	}
	
	public void setNumberOfObservations(int numberOfObservations) {
		this.numberOfObservations.set(numberOfObservations);
	}
	
	public double getRedundancy() {
		return this.redundancy.get();
	}
	
	public void setRedundancy(double redundancy) {
		this.redundancy.set(redundancy);
	}

	public void setSignificant(boolean significant) {
		this.significant.set(significant);
	}
	
	public boolean isSignificant() {
		return this.significant.get();
	}

	public boolean isApplyAposterioriVarianceOfUnitWeight() {
		return this.applyAposterioriVarianceOfUnitWeight.get();
	}
	
	public void setApplyAposterioriVarianceOfUnitWeight(boolean apply) {
		this.applyAposterioriVarianceOfUnitWeight.set(apply);
	}
	
	public ObjectProperty<Double> variance0Property() {
		return this.variance0;
	}
	
	public ObservableObjectValue<Double> varianceProperty() {
		return this.variance;
	}
	
	public ObservableObjectValue<Double> unitVarianceProperty() {
		return this.unitVariance;
	}
	
	public ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public ObservableObjectValue<Double> unitOmegaProperty() {
		return this.unitOmega;
	}
	
	public ObjectProperty<Double> redundancyProperty() {
		return this.redundancy;
	}
	
	public ObjectProperty<Integer> numberOfObservationsProperty() {
		return this.numberOfObservations;
	}
	
	public ObjectProperty<Boolean> applyAposterioriVarianceOfUnitWeightProperty() {
		return this.applyAposterioriVarianceOfUnitWeight;
	}
	
	public ObjectProperty<Boolean> significantProperty() {
		return this.significant;
	}
	
	public VarianceComponentType getVarianceComponentType() {
		return this.varianceComponentType.get();
	}
}
