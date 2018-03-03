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

public class VarianceComponentRow extends Row {
	private ObjectProperty<VarianceComponentType> varianceComponentType = new SimpleObjectProperty<VarianceComponentType>(this, "varianceComponentType");
	private ObjectProperty<Integer> numberOfObservations = new SimpleObjectProperty<Integer>(this, "numberOfObservations");
	private ObjectProperty<Double> redundancy = new SimpleObjectProperty<Double>(this, "redundancy");
	private ObjectProperty<Double> omega = new SimpleObjectProperty<Double>(this, "omega");
	private ObjectProperty<Double> sigma2aposteriori = new SimpleObjectProperty<Double>(this, "sigma2aposteriori");
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
	
	public final void setNumberOfObservations(final Integer numberOfObservations) {
		this.numberOfObservationsProperty().set(numberOfObservations);
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
	
	public final ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public final Double getOmega() {
		return this.omegaProperty().get();
	}
	
	public final void setOmega(final Double omega) {
		this.omegaProperty().set(omega);
	}
	
	public final ObjectProperty<Double> sigma2aposterioriProperty() {
		return this.sigma2aposteriori;
	}
	
	public final Double getSigma2aposteriori() {
		return this.sigma2aposterioriProperty().get();
	}
	
	public final void setSigma2aposteriori(final Double sigma2aposteriori) {
		this.sigma2aposterioriProperty().set(sigma2aposteriori);
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
