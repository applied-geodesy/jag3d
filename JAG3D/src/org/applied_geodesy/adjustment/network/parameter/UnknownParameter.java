package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;

public abstract class UnknownParameter {
	private ObservationGroup observationGroup = new ObservationGroup(-1, Double.NaN, Double.NaN, Double.NaN, Epoch.REFERENCE); 
	
	// Position in Designmatrix; -1 entspricht nicht gesetzt	
	private int colInJacobiMatrix = -1;

	/**
	 * Liefert den Parametertyp
	 * @return typ
	 */
	public abstract ParameterType getParameterType();
	
	/**
	 * Gibt die Spaltenposition
	 * in der Designmatrix A zurueck 
	 * @return row
	 */
	public int getColInJacobiMatrix() {
		return this.colInJacobiMatrix;
	}
	
	/**
	 * Legt die Spalte, in der der Parameter
	 * in der JacobiMatrix steht, fest.
	 * @param col Spalte in Matrix
	 */
	public void setColInJacobiMatrix(int col) {
		this.colInJacobiMatrix = col;
	}
	
	/**
	 * Setzt die Beobachtung, die zum Parameter gehoeren
	 * @param observation
	 */
	public void setObservation(Observation observation) {
		this.observationGroup.add(observation);
	}
	
	/**
	 * Liefert eine allg. Beobachtungsgruppe mit allen Beobachtungen,
	 * die zum Parameter.
	 * @return observationGroup
	 */
	public ObservationGroup getObservations() {
		return this.observationGroup;
	}
	
	/**
	 * Liefert die Anzahl der Beobachtungen des Parameters
	 * @return size
	 */
	public int numberOfObservations() {
		return this.observationGroup.size();
	}
	
}
