package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

public class TransformationParameter {
	private double value;
	private final TransformationParameterType type;
	private int col = -1;
	
	public TransformationParameter(TransformationParameterType type, double value) {
		this.value= value;
		this.type = type;
	}

	public TransformationParameterType getType() {
		return this.type;
	}

	public void setColInJacobiMatrix(int column) {
		this.col = column;
	}

	public int getColInJacobiMatrix() {
		return this.col;
	}

	public boolean isFixed() {
		return this.col < 0;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
