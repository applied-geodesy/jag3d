package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.point.Point;

public abstract class Deflection extends UnknownParameter {
	private final Point point;

	private int rowInJacobiMatrix = -1;
	
	private double redundancy      =  0.0,
            	   sigma0          = -1.0,
            	   sigma           =  0.0,
            	   omega           =  0.0,
            	   value0, value, nabla, grzw, confidence;

	public Deflection(Point point) {
		this(point, 0.0);
	}

	public Deflection(Point point, double value) {
		this.point = point;
		this.setValue0(value);
	}

	public Deflection(Point point, double value, double std) {
		this.setValue0(value);
		this.point = point;
		this.setStd(std);
	}

	public final Point getPoint() {
		return this.point;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue0(double value0) {
		this.value0 = value0;
		this.setValue(value0);
	}

	public double getValue0() {
		return this.value0;
	}
	
	@Override
	public ObservationGroup getObservations() {
		return this.point.getObservations();
	}

	public void setStd(double std) {
		this.sigma0 = (this.sigma0 <= 0 && std>0)?std:this.sigma0;
		this.sigma  = std > 0 ? std : this.sigma;
	}

	public double getStd() {
		return this.sigma;
	}
	
	public double getStdApriori() {
		return this.sigma0;
	}

	public void setStdApriori(double std) {
		if (std > 0)
			this.sigma0 = std;
	}

	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}

	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}

	public double getRedundancy() {
		return redundancy;
	}

	public void setRedundancy(double redundancy) {
		this.redundancy = redundancy;
	}

	public void setOmega(double omega) {
		this.omega = omega;
	}

	public double getOmega() {
		return this.omega;
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
}