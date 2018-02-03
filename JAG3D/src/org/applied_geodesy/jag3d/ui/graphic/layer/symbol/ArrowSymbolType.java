package org.applied_geodesy.jag3d.ui.graphic.layer.symbol;

public enum ArrowSymbolType {
	STROKED_TRIANGLE_ARROW(11),
	STROKED_TETRAGON_ARROW(12),
	
	FILLED_TRIANGLE_ARROW(11),
	FILLED_TETRAGON_ARROW(12),
	
	;
	
	private int id;
	private ArrowSymbolType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ArrowSymbolType getEnumByValue(int value) {
		for(ArrowSymbolType element : ArrowSymbolType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}

