package org.applied_geodesy.jag3d.ui.graphic.util;

public class GraphicUtils {
	private GraphicUtils() {}
	
	
	public static double toPixelCoordinate(double coord, double ref, double scale) {
		return (coord - ref) / scale;
	}

	public static double toWorldCoordinate(double coord, double ref, double scale) {
		return coord * scale + ref;
	}
}
