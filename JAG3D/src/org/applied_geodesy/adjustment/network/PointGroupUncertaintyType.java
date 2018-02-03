package org.applied_geodesy.adjustment.network;

public enum PointGroupUncertaintyType {
	CONSTANT_X(20),
	CONSTANT_Y(21),
	CONSTANT_Z(22),
	
	DEFLECTION_X(31),
	DEFLECTION_Y(32);

	private int id;
	private PointGroupUncertaintyType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static PointGroupUncertaintyType getEnumByValue(int value) {
		for(PointGroupUncertaintyType element : PointGroupUncertaintyType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
