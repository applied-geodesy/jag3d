package org.applied_geodesy.adjustment.network;

public enum ObservationType {
	LEVELING(1),
	DIRECTION(2),
	HORIZONTAL_DISTANCE(3),
	SLOPE_DISTANCE(4),
	ZENITH_ANGLE(5),
	GNSS1D(6), // 50
	GNSS2D(7), // 60
	GNSS3D(8); // 70

	private int id;
	private ObservationType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ObservationType getEnumByValue(int value) {
		for(ObservationType element : ObservationType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}