package org.applied_geodesy.adjustment.network;

public enum Epoch {
	REFERENCE(true),
	CONTROL(false);
	
	private boolean id;
	private Epoch(boolean id) {
		this.id = id;
	}

	public final boolean getId() {
		return id;
	}

	public static Epoch getEnumByValue(boolean value) {
		for(Epoch element : Epoch.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}