package org.applied_geodesy.util;

import java.io.InputStream;

import javafx.scene.image.Image;

public class ImageUtils {
	private final static String IMGAGE_PATH = "/org/applied_geodesy/jag3d/ui/gfx/";

	public static Image getImage(String icon) {
		InputStream input = null;
		try {
			input = ImageUtils.class.getResourceAsStream(IMGAGE_PATH + icon);
			Image img = new Image(input);
			return img;
		} 
		finally {
			if (input != null) {
				try {input.close(); } catch (Exception e) {}
			}
		}
	}
}
