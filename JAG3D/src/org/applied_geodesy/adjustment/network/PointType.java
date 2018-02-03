package org.applied_geodesy.adjustment.network;

public enum PointType {
	REFERENCE_POINT(1),
	STOCHASTIC_POINT(2),
	DATUM_POINT(3),
	NEW_POINT(4);

	private int id;
	private PointType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static PointType getEnumByValue(int value) {
		for(PointType element : PointType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}