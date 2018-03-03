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

import org.applied_geodesy.adjustment.network.ParameterType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AdditionalParameterRow extends Row {

	private ObjectProperty<Double> valueAposteriori             = new SimpleObjectProperty<Double>(this, "valueAposteriori");
	
	private ObjectProperty<Double> grossError                   = new SimpleObjectProperty<Double>(this, "grossError");
	private ObjectProperty<Double> minimalDetectableBias        = new SimpleObjectProperty<Double>(this, "minimalDetectableBias");

	private ObjectProperty<ParameterType> parameterType         = new SimpleObjectProperty<ParameterType>(this, "parameterType");
	
	private ObjectProperty<Double> sigmaAposteriori             = new SimpleObjectProperty<Double>(this, "sigmaAposteriori");
	private ObjectProperty<Double> confidence                   = new SimpleObjectProperty<Double>(this, "confidence");
	
	private ObjectProperty<Double> testStatisticApriori         = new SimpleObjectProperty<Double>(this, "testStatisticApriori");
	private ObjectProperty<Double> testStatisticAposteriori     = new SimpleObjectProperty<Double>(this, "testStatisticAposteriori");
	private ObjectProperty<Double> pValueApriori                = new SimpleObjectProperty<Double>(this, "pValueApriori");
	private ObjectProperty<Double> pValueAposteriori            = new SimpleObjectProperty<Double>(this, "pValueAposteriori");

	private BooleanProperty significant                         = new SimpleBooleanProperty(this, "significant", Boolean.FALSE);

	
	public ObjectProperty<Double> valueAposterioriProperty() {
		return this.valueAposteriori;
	}
	
	public Double getValueAposteriori() {
		return this.valueAposterioriProperty().get();
	}
	
	public void setValueAposteriori(final Double valueAposteriori) {
		this.valueAposterioriProperty().set(valueAposteriori);
	}
	
	public ObjectProperty<Double> grossErrorProperty() {
		return this.grossError;
	}
	
	public Double getGrossError() {
		return this.grossErrorProperty().get();
	}
	
	public void setGrossError(final Double grossError) {
		this.grossErrorProperty().set(grossError);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasProperty() {
		return this.minimalDetectableBias;
	}
	
	public Double getMinimalDetectableBias() {
		return this.minimalDetectableBiasProperty().get();
	}
	
	public void setMinimalDetectableBias(final Double minimalDetectableBias) {
		this.minimalDetectableBiasProperty().set(minimalDetectableBias);
	}
	
	public ObjectProperty<ParameterType> parameterTypeProperty() {
		return this.parameterType;
	}
	
	public ParameterType getParameterType() {
		return this.parameterTypeProperty().get();
	}
	
	public void setParameterType(final ParameterType parameterType) {
		this.parameterTypeProperty().set(parameterType);
	}
	
	public ObjectProperty<Double> sigmaAposterioriProperty() {
		return this.sigmaAposteriori;
	}
	

	public Double getSigmaAposteriori() {
		return this.sigmaAposterioriProperty().get();
	}
	
	public void setSigmaAposteriori(final Double sigmaAposteriori) {
		this.sigmaAposterioriProperty().set(sigmaAposteriori);
	}
	
	public ObjectProperty<Double> confidenceProperty() {
		return this.confidence;
	}
	
	public Double getConfidence() {
		return this.confidenceProperty().get();
	}
	
	public void setConfidence(final Double confidence) {
		this.confidenceProperty().set(confidence);
	}
	
	public ObjectProperty<Double> testStatisticAprioriProperty() {
		return this.testStatisticApriori;
	}
	
	public Double getTestStatisticApriori() {
		return this.testStatisticAprioriProperty().get();
	}
	
	public void setTestStatisticApriori(final Double testStatisticApriori) {
		this.testStatisticAprioriProperty().set(testStatisticApriori);
	}
	
	public ObjectProperty<Double> testStatisticAposterioriProperty() {
		return this.testStatisticAposteriori;
	}
	
	public Double getTestStatisticAposteriori() {
		return this.testStatisticAposterioriProperty().get();
	}
	
	public void setTestStatisticAposteriori(final Double testStatisticAposteriori) {
		this.testStatisticAposterioriProperty().set(testStatisticAposteriori);
	}
	
	public ObjectProperty<Double> pValueAprioriProperty() {
		return this.pValueApriori;
	}
	
	public Double getPValueApriori() {
		return this.pValueAprioriProperty().get();
	}
	

	public void setPValueApriori(final Double pValueApriori) {
		this.pValueAprioriProperty().set(pValueApriori);
	}
	
	public ObjectProperty<Double> pValueAposterioriProperty() {
		return this.pValueAposteriori;
	}

	public Double getPValueAposteriori() {
		return this.pValueAposterioriProperty().get();
	}

	public void setPValueAposteriori(final Double pValueAposteriori) {
		this.pValueAposterioriProperty().set(pValueAposteriori);
	}
	
	public BooleanProperty significantProperty() {
		return this.significant;
	}
	
	public boolean isSignificant() {
		return this.significantProperty().get();
	}
	
	public void setSignificant(final boolean significant) {
		this.significantProperty().set(significant);
	}
}
