package org.applied_geodesy.adjustment;

public enum EstimationType {
	L1NORM(1),
	L2NORM(2),
	SIMULATION(3);

	private int id;
	private EstimationType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static EstimationType getEnumByValue(int value) {
		for(EstimationType element : EstimationType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}