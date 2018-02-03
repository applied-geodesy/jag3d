package org.applied_geodesy.jag3d.ui.graphic.sql;

public class PointPair {
	private GraphicPoint startPoint, endPoint;
	private boolean significant;
	
	public PointPair(GraphicPoint startPoint, GraphicPoint endPoint) {
		this.startPoint = startPoint;
		this.endPoint   = endPoint;
	}
	
	public GraphicPoint getStartPoint() {
		return this.startPoint;
	}
	
	public GraphicPoint getEndPoint() {
		return this.endPoint;
	}
	
	public boolean isSignificant() {
		return significant;
	}
	
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}
}
