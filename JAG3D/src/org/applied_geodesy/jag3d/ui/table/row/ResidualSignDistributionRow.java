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

import org.applied_geodesy.adjustment.network.VarianceComponentType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ResidualSignDistributionRow extends Row {
	private ObjectProperty<VarianceComponentType> varianceComponentType = new SimpleObjectProperty<VarianceComponentType>(this, "varianceComponentType");
	private ObjectProperty<Integer> numberOfObservations = new SimpleObjectProperty<Integer>(this, "numberOfObservations");
	private ObjectProperty<Integer> numberOfEffectiveObservations = new SimpleObjectProperty<Integer>(this, "numberOfEffectiveObservations");
	private ObjectProperty<Integer> numberOfNegativeResiduals = new SimpleObjectProperty<Integer>(this, "numberOfNegativeResiduals");
	private ObjectProperty<Double> redundancy = new SimpleObjectProperty<Double>(this, "redundancy");
	private BooleanProperty significant = new SimpleBooleanProperty(this, "significant");

	
	public final ObjectProperty<VarianceComponentType> varianceComponentTypeProperty() {
		return this.varianceComponentType;
	}
	
	public final VarianceComponentType getVarianceComponentType() {
		return this.varianceComponentTypeProperty().get();
	}
	
	public final void setVarianceComponentType(final VarianceComponentType varianceComponentType) {
		this.varianceComponentTypeProperty().set(varianceComponentType);
	}
	
	public final ObjectProperty<Integer> numberOfObservationsProperty() {
		return this.numberOfObservations;
	}
	
	public final Integer getNumberOfObservations() {
		return this.numberOfObservationsProperty().get();
	}
	
	public final void setNumberOfObservations(final Integer numberOfEffectiveObservations) {
		this.numberOfObservationsProperty().set(numberOfEffectiveObservations);
	}
	
	public final ObjectProperty<Integer> numberOfEffectiveObservationsProperty() {
		return this.numberOfEffectiveObservations;
	}
	
	public final Integer getNumberOfEffectiveObservations() {
		return this.numberOfEffectiveObservationsProperty().get();
	}
	
	public final void setNumberOfEffectiveObservations(final Integer numberOfEffectiveObservations) {
		this.numberOfEffectiveObservationsProperty().set(numberOfEffectiveObservations);
	}
	
	public final ObjectProperty<Integer> numberOfNegativeResidualsProperty() {
		return this.numberOfNegativeResiduals;
	}
	
	public final Integer getNumberOfNegativeResiduals() {
		return this.numberOfNegativeResidualsProperty().get();
	}
	
	public final void setNumberOfNegativeResiduals(final Integer numberOfNegativeResiduals) {
		this.numberOfNegativeResidualsProperty().set(numberOfNegativeResiduals);
	}
	
	public final ObjectProperty<Double> redundancyProperty() {
		return this.redundancy;
	}
	
	public final Double getRedundancy() {
		return this.redundancyProperty().get();
	}
	
	public final void setRedundancy(final Double redundancy) {
		this.redundancyProperty().set(redundancy);
	}
	
	public final BooleanProperty significantProperty() {
		return this.significant;
	}
	
	public final boolean isSignificant() {
		return this.significantProperty().get();
	}
	
	public final void setSignificant(final boolean significant) {
		this.significantProperty().set(significant);
	}
}
