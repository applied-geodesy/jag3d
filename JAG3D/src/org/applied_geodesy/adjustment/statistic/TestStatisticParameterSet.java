package org.applied_geodesy.adjustment.statistic;

public class TestStatisticParameterSet {
	private double quantile, ncp, alpha, beta, logP;
	private final double f1, f2;
	
	public TestStatisticParameterSet(double f1, double f2) {
		this.f1 = f1;
		this.f2 = f2;
	}
	
	public double getQuantile() {
		return this.quantile;
	}
	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}
	public double getNoncentralityParameter() {
		return this.ncp;
	}
	public void setNoncentralityParameter(double ncp) {
		this.ncp = ncp;
	}
	public double getProbabilityValue() {
		return this.alpha;
	}
	public void setProbabilityValue(double alpha) {
		this.alpha = alpha;
	}
	public double getPowerOfTest() {
		return this.beta;
	}
	public void setPowerOfTest(double beta) {
		this.beta = beta;
	}
	public double getNumeratorDof() {
		return this.f1;
	}
	public double getDenominatorDof() {
		return this.f2;
	}
	public void setLogarithmicProbabilityValue(double logP) {
		this.logP = logP;
	}
	public double getLogarithmicProbabilityValue() {
		return this.logP;
	}
	@Override
	public String toString() {
		return "TestStatisticParameterSet [quantile=" + quantile + ", ncp="
				+ ncp + ", alpha=" + alpha + ", beta=" + beta + ", logP="
				+ logP + ", f1=" + f1 + ", f2=" + f2 + "]";
	}
}
