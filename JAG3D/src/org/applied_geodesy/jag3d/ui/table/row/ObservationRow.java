package org.applied_geodesy.jag3d.ui.table.row;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ObservationRow extends Row {
	private ObjectProperty<String> startPointName = new SimpleObjectProperty<String>(); 
	private ObjectProperty<String> endPointName   = new SimpleObjectProperty<String>();
	
	private ObjectProperty<Double> influenceOnNetworkDistortion = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> omega                    = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> testStatisticApriori     = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> testStatisticAposteriori = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> pValueApriori            = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> pValueAposteriori        = new SimpleObjectProperty<Double>();
	
	private BooleanProperty significant = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty enable  = new SimpleBooleanProperty(Boolean.TRUE);
		
	public ObjectProperty<String> startPointNameProperty() {
		return this.startPointName;
	}
	
	public String getStartPointName() {
		return this.startPointNameProperty().get();
	}
	
	public void setStartPointName(final String startPointName) {
		this.startPointNameProperty().set(startPointName);
	}
	
	public ObjectProperty<String> endPointNameProperty() {
		return this.endPointName;
	}
	
	public String getEndPointName() {
		return this.endPointNameProperty().get();
	}
	
	public void setEndPointName(final String endPointName) {
		this.endPointNameProperty().set(endPointName);
	}
	
	public ObjectProperty<Double> influenceOnNetworkDistortionProperty() {
		return this.influenceOnNetworkDistortion;
	}
	
	public Double getInfluenceOnNetworkDistortion() {
		return this.influenceOnNetworkDistortionProperty().get();
	}
	
	public void setInfluenceOnNetworkDistortion(final Double influenceOnNetworkDistortion) {
		this.influenceOnNetworkDistortionProperty().set(influenceOnNetworkDistortion);
	}
	
	public ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public Double getOmega() {
		return this.omegaProperty().get();
	}
	
	public void setOmega(final Double omega) {
		this.omegaProperty().set(omega);
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

	public BooleanProperty enableProperty() {
		return this.enable;
	}
	
	public boolean isEnable() {
		return this.enableProperty().get();
	}
	
	public void setEnable(final boolean enable) {
		this.enableProperty().set(enable);
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
