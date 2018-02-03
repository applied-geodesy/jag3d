package org.applied_geodesy.util.unit;
import org.applied_geodesy.util.i18.I18N;

public abstract class Unit {
	final I18N i18n = I18N.getInstance(); 
	final UnitType type;
	String abbreviation, name;

	Unit(UnitType type) {
		this.type = type;
	}
	
	public String getAbbreviation() {
		return this.abbreviation;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name + " [" + this.abbreviation + "]";
	}
	
	public UnitType getType() {
		return this.type;
	}
}
