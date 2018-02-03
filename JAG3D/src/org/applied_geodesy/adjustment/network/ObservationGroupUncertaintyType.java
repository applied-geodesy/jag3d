package org.applied_geodesy.adjustment.network;

public enum ObservationGroupUncertaintyType {
	ZERO_POINT_OFFSET(10),
	SQUARE_ROOT_DISTANCE_DEPENDENT(11),
	DISTANCE_DEPENDENT(12);
	

	private int id;
	private ObservationGroupUncertaintyType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ObservationGroupUncertaintyType getEnumByValue(int value) {
		for(ObservationGroupUncertaintyType element : ObservationGroupUncertaintyType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}