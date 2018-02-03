package org.applied_geodesy.adjustment.network.observation.projection;

public class Projection {

	private ProjectionType type;
	private double referenceHeight = 0.0;
	
	public Projection(ProjectionType type) {
		this.setType(type);
	}

	public void setType(ProjectionType type) {
		this.type = type;
	}

	public ProjectionType getType() {
		return this.type;
	}

	public void setReferenceHeight(double referenceHeight) {
		this.referenceHeight = referenceHeight;
	}

	public double getReferenceHeight() {
		return this.referenceHeight;
	}

	public boolean isHeightReduction() {
		return (this.type == ProjectionType.HEIGHT_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_REDUCTION ||
    			this.type == ProjectionType.HEIGHT_GK_REDUCTION || this.type == ProjectionType.HEIGHT_UTM_REDUCTION ||
    			this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}

	public boolean isGaussKruegerReduction() {
		return (this.type == ProjectionType.HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_GK_REDUCTION ||
				this.type == ProjectionType.GAUSS_KRUEGER_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION);
	}

	public boolean isUTMReduction() {
		return (this.type == ProjectionType.HEIGHT_UTM_REDUCTION || this.type == ProjectionType.DIRECTION_UTM_REDUCTION ||
				this.type == ProjectionType.UTM_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}

	public boolean isDirectionReduction() {
		return (this.type == ProjectionType.DIRECTION_GK_REDUCTION || this.type == ProjectionType.DIRECTION_UTM_REDUCTION ||
				this.type == ProjectionType.DIRECTION_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_REDUCTION || 
				this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}
	
	public String toString() {
		return  "Type : " + this.type + "\n" +
				"Hm: " + this.referenceHeight;
	}
}
