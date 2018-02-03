package org.applied_geodesy.util.unit;

public enum UnitType {
	METER (10),
	MILLIMETER(11),
	MICROMETER(12),
	INCH(13),
	
	UNITLESS(20),
	PARTS_PER_MILLION_WRT_ONE(21),
	PARTS_PER_MILLION_WRT_ZERO(22),
	
	RADIAN(30),
	DEGREE(31),
	GRADIAN(32),
	MILLIRADIAN(33),
	ARCSECOND(34),
	MILLIGRADIAN(35),
	MIL6400(36),

	;

	private int id;
	private UnitType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}
	
	public static UnitType getEnumByValue(int value) {
		for(UnitType element : UnitType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
