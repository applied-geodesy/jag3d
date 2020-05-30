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

package org.applied_geodesy.adjustment.geometry;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class VarianceComponent {
	private ObjectProperty<Boolean> applyAposterioriVarianceOfUnitWeight = new SimpleObjectProperty<Boolean>(this, "applyAposterioriVarianceOfUnitWeight", Boolean.TRUE);
	private ObjectProperty<Double> variance0     = new SimpleObjectProperty<Double>(this, "variance0", 1.0);
	private ObjectProperty<Double> redundancy    = new SimpleObjectProperty<Double>(this, "redundancy", 0.0);
	private ObjectProperty<Double> omega         = new SimpleObjectProperty<Double>(this, "omega", 0.0);
	private ObjectProperty<Boolean> significant  = new SimpleObjectProperty<Boolean>(this, "significant", Boolean.FALSE);
	private ObjectBinding<Double> variance;
	
	public VarianceComponent() {
		this.variance = new ObjectBinding<Double>() {
			{
                super.bind(redundancy, omega);
            }
 
            @Override
            protected Double computeValue() {
                return omega.get() > 0 && redundancy.get() > 0 ? omega.get() / redundancy.get() : 1.0;
            }
        };
	}
	
	public double getVariance0() {
		return this.variance0.get();
	}
	
	public void setVariance0(double variance0) {
		this.variance0.set(variance0);
	}
	
	public double getVariance() {
		return this.variance.get();
	}
	
	public double getOmega() {
		return this.omega.get();
	}
	
	public void setOmega(double omega) {
		this.omega.set(omega);
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
	
	public ObjectBinding<Double> varianceProperty() {
		return this.variance;
	}
	
	public ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public ObjectProperty<Double> redundancyProperty() {
		return this.redundancy;
	}
	
	public ObjectProperty<Boolean> applyAposterioriVarianceOfUnitWeightProperty() {
		return this.applyAposterioriVarianceOfUnitWeight;
	}
	
	public ObjectProperty<Boolean> significantProperty() {
		return this.significant;
	}
}
