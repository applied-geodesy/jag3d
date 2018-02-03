package org.applied_geodesy.util.unit;

import java.util.Map;

public class LengthUnit extends Unit {
	public static LengthUnit METER      = new LengthUnit(UnitType.METER);
	public static LengthUnit MILLIMETER = new LengthUnit(UnitType.MILLIMETER);
	public static LengthUnit MICROMETER = new LengthUnit(UnitType.MICROMETER);
	public static LengthUnit INCH       = new LengthUnit(UnitType.INCH);

	public static final Map<UnitType, LengthUnit> UNITS = Map.of(
			UnitType.METER,      METER,
			UnitType.MILLIMETER, MILLIMETER,
			UnitType.MICROMETER, MICROMETER,
			UnitType.INCH,       INCH
	);
		
	private double conversionFactorToMeter = 1.0;
	
	private LengthUnit(UnitType type) {
		super(type);
		switch (type) {
		case METER:
			this.name         = i18n.getString("Unit.meter.name", "Meter");
			this.abbreviation = i18n.getString("Unit.meter.abbreviation", "m");
			this.conversionFactorToMeter = 1.0;
			break;
		case MILLIMETER:
			this.name         = i18n.getString("Unit.millimeter.name", "Millimeter");
			this.abbreviation = i18n.getString("Unit.millimeter.abbreviation", "mm");
			this.conversionFactorToMeter = 1E-3;
			break;
		case MICROMETER:
			this.name         = i18n.getString("Unit.micrometer.name", "Micrometer");
			this.abbreviation = i18n.getString("Unit.micrometer.abbreviation", "\u03BCm");
			this.conversionFactorToMeter = 1E-6;
			break;
		case INCH:
			this.name         = i18n.getString("Unit.inch.name", "Inch");
			this.abbreviation = i18n.getString("Unit.inch.abbreviation", "in");
			this.conversionFactorToMeter = 0.0254;
			break;
		default:
			break;
		}
	}
	
	public final double toMeter(double d) {
		return d * this.conversionFactorToMeter;
	}
	
	public final double fromMeter(double d) {
		return d / this.conversionFactorToMeter;
	}
	
	public static LengthUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}
