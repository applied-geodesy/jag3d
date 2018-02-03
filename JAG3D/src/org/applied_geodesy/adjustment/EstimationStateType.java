package org.applied_geodesy.adjustment;

public enum EstimationStateType {
	ERROR_FREE_ESTIMATION(1),
	BUSY(0),
	ITERATE(0),
	CONVERGENCE(0),
	PRINCIPAL_COMPONENT_ANALYSIS(0),
	ESTIAMTE_STOCHASTIC_PARAMETERS(0),
	INVERT_NORMAL_EQUATION_MATRIX(0),
	EXPORT_COVARIANCE_MATRIX(0),
	INTERRUPT(-1),
	SINGULAR_MATRIX(-2),
	ROBUST_ESTIMATION_FAILD(-3),
	NO_CONVERGENCE(-4),
	NOT_INITIALISED(-5),
	OUT_OF_MEMORY(-6);
	
	private int id;
	private EstimationStateType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static EstimationStateType getEnumByValue(int value) {
		for(EstimationStateType element : EstimationStateType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
