package org.applied_geodesy.jag3d.ui.graphic.layer;

public class ObservationSymbolProperties extends GraphicComponentProperties {
	public enum ObservationType {
		LEVELING(1),
		DIRECTION(2),
		DISTANCE(3),
		ZENITH_ANGLE(4),
		GNSS(5);

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
	
	private final ObservationType observationType;
	
	ObservationSymbolProperties(ObservationType observationType) {
		this.observationType = observationType;
	}

	public ObservationType getObservationType() {
		return this.observationType;
	}
}
