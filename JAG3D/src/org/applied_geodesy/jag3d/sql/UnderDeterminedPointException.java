package org.applied_geodesy.jag3d.sql;

public class UnderDeterminedPointException extends Exception {
	private static final long serialVersionUID = 3497901439178351156L;
	private int dimension = -1;
	private int numberOfObservations = 0;
	private String pointName;
	
	public UnderDeterminedPointException(String message, String pointName, int dimension, int numberOfObservations) { 
		super(message); 
		this.pointName = pointName;
		this.dimension = dimension;
		this.numberOfObservations = numberOfObservations;
	}
	
	public String getPointName() {
		return pointName;
	}

	public int getDimension() {
		return dimension;
	}

	public int getNumberOfObservations() {
		return numberOfObservations;
	}
	
}
