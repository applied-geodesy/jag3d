package org.applied_geodesy.jag3d.ui.table;

public enum CellValueType {
	OBJECT(0),
	BOOLEAN(1),
	INTEGER(2),
	DOUBLE(3),
	STRING(4),
	DATE(5),
	IMAGE(6),

	ANGLE(10),
	ANGLE_UNCERTAINTY(11),
	ANGLE_RESIDUAL(12),
	
	LENGTH(20),
	LENGTH_UNCERTAINTY(21),
	LENGTH_RESIDUAL(22),
	
	SCALE(30),
	SCALE_UNCERTAINTY(31),
	SCALE_RESIDUAL(32),

	VECTOR(40),
	VECTOR_UNCERTAINTY(41),
	
	STATISTIC(50),

	;

	private int id;
	private CellValueType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}
	
	public static CellValueType getEnumByValue(int value) {
		for(CellValueType element : CellValueType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
