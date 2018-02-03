package org.applied_geodesy.jag3d.ui.graphic.layer;

public enum LayerType {
	MOUSE(-1),
	
	ABSOLUTE_CONFIDENCE(41),
	RELATIVE_CONFIDENCE(42),
	
	OBSERVATION_APRIORI(31),
	OBSERVATION_APOSTERIORI(32),
	
	REFERENCE_POINT_APRIORI(11),
	STOCHASTIC_POINT_APRIORI(12),
	DATUM_POINT_APRIORI(13),
	NEW_POINT_APRIORI(14),

	ARROW(51),
	
	REFERENCE_POINT_APOSTERIORI(21),
	STOCHASTIC_POINT_APOSTERIORI(22),
	DATUM_POINT_APOSTERIORI(23),
	NEW_POINT_APOSTERIORI(24),
	
	;
	
	private int id;
	private LayerType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static LayerType getEnumByValue(int value) {
		for(LayerType element : LayerType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}	
