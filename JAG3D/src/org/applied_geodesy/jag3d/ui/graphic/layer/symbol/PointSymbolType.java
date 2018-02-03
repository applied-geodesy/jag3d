package org.applied_geodesy.jag3d.ui.graphic.layer.symbol;

public enum PointSymbolType {
	DOT(1),
	X_CROSS(2),
	PLUS_CROSS(3),
	
	STROKED_CIRCLE(101),
	STROKED_UPRIGHT_TRIANGLE(102),
	STROKED_DOWNRIGHT_TRIANGLE(103),
	STROKED_SQUARE(104),
	STROKED_STAR(105),
	STROKED_PENTAGON(106),
	STROKED_HEXAGON(107),
	STROKED_HEPTAGON(108),
	STROKED_OCTAGON(109),
	STROKED_DIAMAND(110),
	
	CROSSED_CIRCLE(4),
	CROSSED_SQUARE(5),
	
	FILLED_CIRCLE(201),
	FILLED_UPRIGHT_TRIANGLE(202),
	FILLED_DOWNRIGHT_TRIANGLE(203),
	FILLED_SQUARE(204),
	FILLED_STAR(205),
	FILLED_PENTAGON(206),
	FILLED_HEXAGON(207),
	FILLED_HEPTAGON(208),
	FILLED_OCTAGON(209),
	FILLED_DIAMAND(210),

	;
	
	private int id;
	private PointSymbolType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static PointSymbolType getEnumByValue(int value) {
		for(PointSymbolType element : PointSymbolType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
