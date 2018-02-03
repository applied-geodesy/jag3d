package org.applied_geodesy.adjustment;

import java.io.BufferedInputStream;
import java.util.Properties;

import org.applied_geodesy.adjustment.network.ObservationType;

public class DefaultAverageThreshold {
	static final double LEVELING            = DefaultUncertainty.LEVELING_ZERO_POINT_OFFSET * 10.0;
	
	static final double DIRECTION           = DefaultUncertainty.ANGLE_ZERO_POINT_OFFSET * 10.0;
	static final double ZENITH_ANGLE        = DefaultUncertainty.ANGLE_ZERO_POINT_OFFSET * 10.0;
	
	static final double HORIZONTAL_DISTANCE = DefaultUncertainty.DISTANCE_ZERO_POINT_OFFSET * 10.0;
	static final double SLOPE_DISTANCE      = DefaultUncertainty.DISTANCE_ZERO_POINT_OFFSET * 10.0;

	static final double GNSS1D              = DefaultUncertainty.GNSS_ZERO_POINT_OFFSET * 10.0;
	static final double GNSS2D              = DefaultUncertainty.GNSS_ZERO_POINT_OFFSET * 10.0;
	static final double GNSS3D              = DefaultUncertainty.GNSS_ZERO_POINT_OFFSET * 10.0;
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/averagethresholds.default";
		try {
			if (DefaultUncertainty.class.getResource(path) != null) {
				bis = new BufferedInputStream(DefaultUncertainty.class.getResourceAsStream(path));
				PROPERTIES.load(bis);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private DefaultAverageThreshold() {}

	public static double getThresholdLeveling() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("LEVELING")); } catch (Exception e) {}
		return value > 0 ? value : LEVELING;
	}
	
	public static double getThresholdDirection() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DIRECTION")); } catch (Exception e) {}
		return value > 0 ? value : DIRECTION;
	}
	
	public static double getThresholdZenithAngle() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ZENITH_ANGLE")); } catch (Exception e) {}
		return value > 0 ? value : ZENITH_ANGLE;
	}
	
	public static double getThresholdHorizontalDistance() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("HORIZONTAL_DISTANCE")); } catch (Exception e) {}
		return value > 0 ? value : HORIZONTAL_DISTANCE;
	}
	
	public static double getThresholdSlopeDistance() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("SLOPE_DISTANCE")); } catch (Exception e) {}
		return value > 0 ? value : SLOPE_DISTANCE;
	}
	
	public static double getThresholdGNSSBaseline1D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS1D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS1D;
	}
	
	public static double getThresholdGNSSBaseline2D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS2D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS2D;
	}
	
	public static double getThresholdGNSSBaseline3D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS3D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS3D;
	}
	
	public static double getThreshold(ObservationType type) {
		switch(type) {
		case LEVELING:
			return getThresholdLeveling();
		case DIRECTION:
			return getThresholdDirection();
		case HORIZONTAL_DISTANCE:
			return getThresholdHorizontalDistance();
		case SLOPE_DISTANCE:
			return getThresholdSlopeDistance();
		case ZENITH_ANGLE:
			return getThresholdZenithAngle();
		case GNSS1D:
			return getThresholdGNSSBaseline1D();
		case GNSS2D:
			return getThresholdGNSSBaseline2D();
		case GNSS3D:
			return getThresholdGNSSBaseline3D();
		}
		return 0;
	}
}
