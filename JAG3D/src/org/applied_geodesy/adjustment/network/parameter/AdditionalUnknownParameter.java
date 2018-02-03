package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;

public abstract class AdditionalUnknownParameter extends UnknownParameter {
	private double value, tPrio = 0.0, tPost = 0.0, pPrio = 0.0, pPost = 0.0, nabla = 0.0, grzw = 0.0, confidence = 0.0;
	
	// Standardabweichung des Zusatzparameters; -1 == nicht gesetzt
	private double sigma = -1;
	private boolean significant = false;
	private ObservationGroup observationGroup;
	
	public AdditionalUnknownParameter(double value) {
		this.value = value;
	}

	public void setValue(double newValue) {
		this.value = newValue;
	}

	public double getValue() {
		return this.value;
	}

	public double getStd() {
		return this.sigma;
	}

	public void setStd(double std) {
		this.sigma = (std>0)?std:-1.0;
	}

	public boolean isSignificant() {
		return this.significant;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}

	public boolean isEnable() {
		return this.getColInJacobiMatrix() >= 0;
	}

	public void setTprio(double t) {
		this.tPrio = t;
	}

	public void setTpost(double t) {
		this.tPost = t;
	}

	public void setPprio(double p) {
		this.pPrio = p;
	}

	public void setPpost(double p) {
		this.pPost = p;
	}

	public double getTprio() {
		return this.tPrio;
	}

	public double getTpost() {
		return this.tPost;
	}

	public double getPprio() {
		return this.pPrio;
	}

	public double getPpost() {
		return this.pPost;
	}

	public double getGrossError() {
		return nabla;
	}

	public void setGrossError(double nabla) {
		this.nabla = nabla;
	}

	public double getMinimalDetectableBias() {
		return grzw;
	}

	public void setMinimalDetectableBias(double grzw) {
		this.grzw = grzw;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public void setObservationGroup(ObservationGroup obsGroup) {
		this.observationGroup = obsGroup;
	}

	public ObservationGroup getObservationGroup() {
		return this.observationGroup;
	}

	public void setColInJacobiMatrix(int col) {
		super.setColInJacobiMatrix(col);
		if (col >= 0 && this.observationGroup != null)
			this.observationGroup.setApproximatedValue(this);
	}

	public abstract double getExpectationValue();
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+":  "+this.getParameterType()+"  "+this.getValue()+"  "+this.isEnable();
	}
}
