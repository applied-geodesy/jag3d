package org.applied_geodesy.util.unit;

import java.util.LinkedHashMap;
import java.util.Map;

public class PercentUnit extends Unit {
	public static PercentUnit UNITLESS = new PercentUnit(UnitType.UNITLESS);
	public static PercentUnit PERCENT  = new PercentUnit(UnitType.PERCENT);
	
	public static final Map<UnitType, PercentUnit> UNITS;
	static {
		UNITS = new LinkedHashMap<UnitType, PercentUnit>();
		UNITS.put(UnitType.UNITLESS, UNITLESS);
		UNITS.put(UnitType.PERCENT,  PERCENT);
	}
	
	private double conversionFactorToUnitless = 1.0;

	private PercentUnit(UnitType type) {
		super(type);
		switch (type) {
		case UNITLESS:
			this.name         = i18n.getString("Unit.unitless.name", "Unitless");
			this.abbreviation = i18n.getString("Unit.unitless.abbreviation", "\u2014");
			this.conversionFactorToUnitless = 1.0;
			break;
		case PERCENT:
			this.name         = i18n.getString("Unit.percent.name", "Percent");
			this.abbreviation = i18n.getString("Unit.percent.abbreviation", "\u0025");
			this.conversionFactorToUnitless = 1.0/100.0;
			break;
		default:
			break;
		}
	}
	
	public final double toUnitless(double d) {
		return d * this.conversionFactorToUnitless;
	}
	
	public final double fromUnitless(double d) {
		return d / this.conversionFactorToUnitless;
	}
	
	public static PercentUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}