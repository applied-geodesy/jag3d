package org.applied_geodesy.adjustment.network.parameter;

import java.util.List;

import org.applied_geodesy.adjustment.network.ParameterType;

import java.util.ArrayList;

public class UnknownParameters {
	private int col = 0;
	private int lastPointColumn = -1, lastParamColumn = -1, leastParamColumn = -1;
	private List<UnknownParameter> parameters = new ArrayList<UnknownParameter>();
	
	/**
	 * Sortiert die Parameter in der Liste nach {Punkte, Zusamtzparameter} ohne 
	 * die innere Reihenfolge der Punkte bzw. Parameter zu aendern 
	 */
	public void resortParameters() {
		if (this.leastParamColumn < this.lastPointColumn && this.leastParamColumn >= 0) {
			List<UnknownParameter> sortedParameters = new ArrayList<UnknownParameter>(this.parameters.size());
			List<UnknownParameter> nonPointParameters = new ArrayList<UnknownParameter>();
			this.col = 0;
			this.leastParamColumn = -1;
			this.lastParamColumn  = -1;
			this.lastPointColumn  = -1;

			for (UnknownParameter parameter : this.parameters) {
				switch (parameter.getParameterType()) {
				case POINT1D:
				case POINT2D:
				case POINT3D:
					parameter.setColInJacobiMatrix(this.col);
					this.increaseColumnCount(parameter.getParameterType());
					sortedParameters.add(parameter);
					break;
				default:
					nonPointParameters.add(parameter);
					break;
				}
			}

			for (UnknownParameter parameter : nonPointParameters) {
				parameter.setColInJacobiMatrix(this.col);
				this.increaseColumnCount(parameter.getParameterType());
				sortedParameters.add(parameter);
			}
			this.parameters = sortedParameters;
		}
	}

	public boolean add(UnknownParameter parameter) {
		if (!this.parameters.contains(parameter)) {
			parameter.setColInJacobiMatrix(this.col);
			this.increaseColumnCount(parameter.getParameterType());
			return this.parameters.add( parameter );
		}
		return false;
	}

	public int columnsOfAddionalParameters() {
		return this.lastParamColumn <= 0 ? 0 : this.lastParamColumn - this.lastPointColumn;
	}

	public int columnsOfPoints() {
		return this.lastPointColumn;
	}

	public int columnsInJacobi() {
		return this.col;
	}

	public boolean contains(Object obj) {
		return this.parameters.contains(obj);
	}
	
	private void increaseColumnCount(ParameterType type) {
		switch(type) {
			case POINT1D:
				this.col += 1;
				this.lastPointColumn = this.col;
			break;
			case POINT2D:
				this.col += 2;
				this.lastPointColumn = this.col;
			break;
			case POINT3D:
				this.col += 3;
				this.lastPointColumn = this.col;
			break;
			default:
				this.col += 1;
				this.lastParamColumn = this.col;
				if (this.leastParamColumn < 0)
					this.leastParamColumn = this.col;
			break;
		}
	}

	public UnknownParameter get(int i) {
		return this.parameters.get(i);
	}

	public int size() {
		return this.parameters.size();
	}
}
