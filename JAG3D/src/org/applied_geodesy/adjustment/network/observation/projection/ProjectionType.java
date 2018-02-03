package org.applied_geodesy.adjustment.network.observation.projection;

public enum ProjectionType {
	NONE(0),
	DIRECTION_REDUCTION(2),
	HEIGHT_REDUCTION(3),
	GAUSS_KRUEGER_REDUCTION(4),
	UTM_REDUCTION(5),
	DIRECTION_HEIGHT_REDUCTION(23),
	DIRECTION_GK_REDUCTION(24),
	DIRECTION_UTM_REDUCTION(25),
	HEIGHT_GK_REDUCTION(34),
	HEIGHT_UTM_REDUCTION(35),
	DIRECTION_HEIGHT_GK_REDUCTION(234),
	DIRECTION_HEIGHT_UTM_REDUCTION(235); 

	private int id;
	private ProjectionType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ProjectionType getEnumByValue(int value) {
		for(ProjectionType element : ProjectionType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}