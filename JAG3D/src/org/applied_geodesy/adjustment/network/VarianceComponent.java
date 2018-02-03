package org.applied_geodesy.adjustment.network;

public class VarianceComponent {
	private final VarianceComponentType type;
	private int numberOfObservations = 0;
	private double redundancy = 0, omega = 0;
	private double kGroup = Double.POSITIVE_INFINITY;
	
	VarianceComponent(VarianceComponentType type) {
		this.type = type;
	}
	
	public double getRedundancy() {
		return this.redundancy;
	}
	
	public void setRedundancy(double redundancy) {
		this.redundancy = redundancy;
	}
	
	public double getOmega() {
		return this.omega;
	}
	
	public void setOmega(double omega) {
		this.omega = omega;
	}
	
	public int getNumberOfObservations() {
		return this.numberOfObservations;
	}
	
	public void setNumberOfObservations(int noo) {
		this.numberOfObservations = noo;
	}
	
	public VarianceComponentType getVarianceComponentType() {
		return this.type;
	}
	
	public double getVarianceFactorAposteriori() {
		if (this.getRedundancy()>0)
			return this.getOmega()/this.getRedundancy();
		return 0.0;
	}
	
	public void setKprioGroup(double k) {
		this.kGroup = k;
	}

	public double getKprioGroup() {
		return this.kGroup;
	}
	
	public double getTprioGroup() {
		return this.redundancy>0?this.omega/this.redundancy:1.0;
	}
	
	public boolean isRejected() {
		return this.getTprioGroup() > this.kGroup;
	}
	
	public String toString() {
		return this.type+"\r\nr="+this.getRedundancy()+
				"\r\nomega="+this.getOmega()+
				"\r\nsigma="+this.getVarianceFactorAposteriori()+
				"\r\nnumObs="+this.getNumberOfObservations();
	}
}
