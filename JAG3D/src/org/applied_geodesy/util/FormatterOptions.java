/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.util;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.unit.AngleUnit;
import org.applied_geodesy.util.unit.LengthUnit;
import org.applied_geodesy.util.unit.ScaleUnit;
import org.applied_geodesy.util.unit.Unit;
import org.applied_geodesy.util.unit.UnitType;

public class FormatterOptions {	
	public class FormatterOption {
		private final CellValueType type;
		private NumberFormat format;
		private Unit unit = null;
		private FormatterOption(CellValueType type, NumberFormat format, Unit unit) {
			this.type = type;
			this.format = format;
			this.unit = unit;
		}
		public void setUnit(Unit unit) {
			if (this.unit == null || this.unit.getClass().equals(unit.getClass())) {
				Unit oldUnit = this.unit;
				if (!this.unit.equals(unit)) {
					this.unit = unit;
					fireUnitChanged(this.type, oldUnit, unit, this.getFractionDigits());
				}
			}
		}
		public Unit getUnit() {
			return this.unit;
		}
		public int getFractionDigits() {
			return this.format.getMaximumFractionDigits();
		}
		public final CellValueType getType() {
			return this.type;
		}
		public void setFractionDigits(int d) {
			if (d < 0)
				return;
			int o = this.format.getMaximumFractionDigits();
			if (o != d) {
				this.format.setMaximumFractionDigits(d);
				this.format.setMinimumFractionDigits(d);
				fireResolutionChanged(this.type, this.unit, o, d);
			}
		}
		public Number parse(String source, ParsePosition parsePosition) throws ParseException {
			return this.format.parse(source, parsePosition);
		}
		public Number parse(String source) throws ParseException {
			return this.format.parse(source);
		}
		public NumberFormat getFormatter() {
			return this.format;
		}
	}

	private List<EventListener> listenerList = new ArrayList<EventListener>();
	private Map<CellValueType, FormatterOption> formatterOptions = new HashMap<CellValueType, FormatterOption>();	
	private static FormatterOptions options = new FormatterOptions();
	
	private FormatterOptions() {
		this.init();
	}
	
	private void init() {
		LengthUnit LENGTH_UNIT = LengthUnit.METER;
		AngleUnit  ANGLE_UNIT  = AngleUnit.GRADIAN;
		ScaleUnit  SCALE_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ONE;
		LengthUnit VECTOR_UNIT = LengthUnit.METER;
		
		LengthUnit LENGTH_UNCERTAINTY_UNIT = LengthUnit.MILLIMETER;
		AngleUnit  ANGLE_UNCERTAINTY_UNIT  = AngleUnit.MILLIGRADIAN;
		ScaleUnit  SCALE_UNCERTAINTY_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ZERO;
		LengthUnit VECTOR_UNCERTAINTY_UNIT = LengthUnit.MILLIMETER;
		
		LengthUnit LENGTH_RESIDUAL_UNIT = LengthUnit.MILLIMETER;
		AngleUnit  ANGLE_RESIDUAL_UNIT  = AngleUnit.MILLIGRADIAN;
		ScaleUnit  SCALE_RESIDUAL_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ZERO;

		final NumberFormat lengthFormatter = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat angleFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat scaleFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat vectorFormatter = NumberFormat.getInstance(Locale.ENGLISH);

		final NumberFormat lengthUncertaintyFormatter = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat angleUncertaintyFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat scaleUncertaintyFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat vectorUncertaintyFormatter = NumberFormat.getInstance(Locale.ENGLISH);
		
		final NumberFormat lengthResidualFormatter = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat angleResidualFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		final NumberFormat scaleResidualFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		
		final NumberFormat statisticFormatter  = NumberFormat.getInstance(Locale.ENGLISH);
		
		lengthFormatter.setGroupingUsed(false);
		angleFormatter.setGroupingUsed(false);
		scaleFormatter.setGroupingUsed(false);
		vectorFormatter.setGroupingUsed(false);
		statisticFormatter.setGroupingUsed(false);
		
		lengthUncertaintyFormatter.setGroupingUsed(false);
		angleUncertaintyFormatter.setGroupingUsed(false);
		scaleUncertaintyFormatter.setGroupingUsed(false);
		vectorUncertaintyFormatter.setGroupingUsed(false);

		lengthResidualFormatter.setGroupingUsed(false);
		angleResidualFormatter.setGroupingUsed(false);
		scaleResidualFormatter.setGroupingUsed(false);
		
		lengthFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		angleFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		scaleFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		vectorFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		statisticFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		
		lengthUncertaintyFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		angleUncertaintyFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		scaleUncertaintyFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		vectorUncertaintyFormatter.setRoundingMode(RoundingMode.HALF_EVEN);

		lengthResidualFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		angleResidualFormatter.setRoundingMode(RoundingMode.HALF_EVEN);
		scaleResidualFormatter.setRoundingMode(RoundingMode.HALF_EVEN);

		this.setFractionDigits(lengthFormatter,    4);
		this.setFractionDigits(angleFormatter,     5);
		this.setFractionDigits(scaleFormatter,     2);
		this.setFractionDigits(vectorFormatter,    7);
		this.setFractionDigits(statisticFormatter, 2);
		
		this.setFractionDigits(lengthUncertaintyFormatter, 1);
		this.setFractionDigits(angleUncertaintyFormatter,  2);
		this.setFractionDigits(scaleUncertaintyFormatter,  1);
		this.setFractionDigits(vectorUncertaintyFormatter, 1);
				
		this.setFractionDigits(lengthResidualFormatter, 1);
		this.setFractionDigits(angleResidualFormatter,  2);
		this.setFractionDigits(scaleResidualFormatter,  2);
		
		this.formatterOptions.put(CellValueType.LENGTH,             new FormatterOption(CellValueType.LENGTH,             lengthFormatter, LENGTH_UNIT));
		this.formatterOptions.put(CellValueType.LENGTH_UNCERTAINTY, new FormatterOption(CellValueType.LENGTH_UNCERTAINTY, lengthUncertaintyFormatter, LENGTH_UNCERTAINTY_UNIT));
		this.formatterOptions.put(CellValueType.LENGTH_RESIDUAL,    new FormatterOption(CellValueType.LENGTH_RESIDUAL,    lengthResidualFormatter, LENGTH_RESIDUAL_UNIT));
		
		this.formatterOptions.put(CellValueType.ANGLE,              new FormatterOption(CellValueType.ANGLE,              angleFormatter, ANGLE_UNIT));
		this.formatterOptions.put(CellValueType.ANGLE_UNCERTAINTY,  new FormatterOption(CellValueType.ANGLE_UNCERTAINTY,  angleUncertaintyFormatter, ANGLE_UNCERTAINTY_UNIT));
		this.formatterOptions.put(CellValueType.ANGLE_RESIDUAL,     new FormatterOption(CellValueType.ANGLE_RESIDUAL,     angleResidualFormatter, ANGLE_RESIDUAL_UNIT));
				
		this.formatterOptions.put(CellValueType.SCALE,              new FormatterOption(CellValueType.SCALE,              scaleFormatter, SCALE_UNIT));
		this.formatterOptions.put(CellValueType.SCALE_UNCERTAINTY,  new FormatterOption(CellValueType.SCALE_UNCERTAINTY,  scaleUncertaintyFormatter, SCALE_UNCERTAINTY_UNIT));
		this.formatterOptions.put(CellValueType.SCALE_RESIDUAL,     new FormatterOption(CellValueType.SCALE_RESIDUAL,     scaleResidualFormatter, SCALE_RESIDUAL_UNIT));
		
		this.formatterOptions.put(CellValueType.VECTOR,             new FormatterOption(CellValueType.VECTOR,             vectorFormatter, VECTOR_UNIT));
		this.formatterOptions.put(CellValueType.VECTOR_UNCERTAINTY, new FormatterOption(CellValueType.VECTOR_UNCERTAINTY, vectorUncertaintyFormatter, VECTOR_UNCERTAINTY_UNIT));
		
		this.formatterOptions.put(CellValueType.STATISTIC,          new FormatterOption(CellValueType.STATISTIC,          statisticFormatter, null));
	}
	
	private void setFractionDigits(NumberFormat format, int d) {
		if (d < 0)
			return;
		format.setMaximumFractionDigits(d);
		format.setMinimumFractionDigits(d);
	}
	
	public Map<CellValueType, FormatterOption> getFormatterOptions() {
		return this.formatterOptions;
	}

	public static FormatterOptions getInstance() {
		return options;
	}
		
	public double convertScaleToView(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE).getUnit()).fromUnitless(d);
	}
	
	public double convertScaleToModel(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE).getUnit()).toUnitless(d);
	}
	
	public double convertLengthToView(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH).getUnit()).fromMeter(d);
	}
	
	public double convertLengthToModel(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH).getUnit()).toMeter(d);
	}
	
	public double convertLengthResidualToView(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH_RESIDUAL).getUnit()).fromMeter(d);
	}
	
	public double convertLengthResidualToModel(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH_RESIDUAL).getUnit()).toMeter(d);
	}
	
	public double convertAngleToView(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE).getUnit()).fromRadian(d);
	}
	
	public double convertAngleToModel(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE).getUnit()).toRadian(d);
	}
	
	public double convertAngleResidualToView(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE_RESIDUAL).getUnit()).fromRadian(d);
	}
	
	public double convertAngleResidualToModel(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE_RESIDUAL).getUnit()).toRadian(d);
	}
	
	public double convertVectorToView(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.VECTOR).getUnit()).fromMeter(d);
	}
	
	public double convertVectorToModel(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.VECTOR).getUnit()).toMeter(d);
	}
	
	public double convertLengthUncertaintyToView(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH_UNCERTAINTY).getUnit()).fromMeter(d);
	}
	
	public double convertLengthUncertaintyToModel(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.LENGTH_UNCERTAINTY).getUnit()).toMeter(d);
	}
	
	public double convertAngleUncertaintyToView(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE_UNCERTAINTY).getUnit()).fromRadian(d);
	}
	
	public double convertAngleUncertaintyToModel(double d) {
		return ((AngleUnit)this.formatterOptions.get(CellValueType.ANGLE_UNCERTAINTY).getUnit()).toRadian(d);
	}
	
	public double convertScaleUncertaintyToView(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE_UNCERTAINTY).getUnit()).fromUnitless(d);
	}
	
	public double convertScaleUncertaintyToModel(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE_UNCERTAINTY).getUnit()).toUnitless(d);
	}
	
	public double convertScaleResidualToView(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE_RESIDUAL).getUnit()).fromUnitless(d);
	}
	
	public double convertScaleResidualToModel(double d) {
		return ((ScaleUnit)this.formatterOptions.get(CellValueType.SCALE_RESIDUAL).getUnit()).toUnitless(d);
	}

	public double convertVectorUncertaintyToView(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.VECTOR_UNCERTAINTY).getUnit()).fromMeter(d);
	}
	
	public double convertVectorUncertaintyToModel(double d) {
		return ((LengthUnit)this.formatterOptions.get(CellValueType.VECTOR_UNCERTAINTY).getUnit()).toMeter(d);
	}
	
	public String toAngleFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.ANGLE, this.convertAngleToView(d), displayUnit);
	}
	
	public String toAngleUncertaintyFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.ANGLE_UNCERTAINTY, this.convertAngleUncertaintyToView(d), displayUnit);
	}
	
	public String toAngleResidualFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.ANGLE_RESIDUAL, this.convertAngleResidualToView(d), displayUnit);
	}
	
	public String toLengthFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.LENGTH, this.convertLengthToView(d), displayUnit);
	}
	
	public String toScaleFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.SCALE, this.convertScaleToView(d), displayUnit);
	}
	
	public String toVectorFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.VECTOR, this.convertVectorToView(d), displayUnit);
	}
	
	public String toStatisticFormat(double d) {
		return this.formatterOptions.get(CellValueType.STATISTIC).getFormatter().format(d).trim();
	}
	
	public String toLengthUncertaintyFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.LENGTH_UNCERTAINTY, this.convertLengthUncertaintyToView(d), displayUnit);
	}
	
//	public String toSquareRootLengthUncertaintyFormat(double d, boolean displayUnit) {
//		return String.format(Locale.ENGLISH, "%s %s",
//				this.formatterOptions.get(CellValueType.LENGTH_UNCERTAINTY).getFormatter().format(this.convertLengthUncertaintyToView(d)), 
//				displayUnit ? "\u221A"+this.formatterOptions.get(CellValueType.LENGTH_UNCERTAINTY).getUnit().getAbbreviation():"").trim();
//	}
	
	public String toLengthResidualFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.LENGTH_RESIDUAL, this.convertLengthResidualToView(d), displayUnit);
	}
	
	public String toScaleUncertaintyFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.SCALE_UNCERTAINTY, this.convertScaleUncertaintyToView(d), displayUnit);
	}
	
	public String toScaleResidualFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.SCALE_RESIDUAL, this.convertScaleResidualToView(d), displayUnit);
	}
	
//	public String toVectorUncertaintyFormat(double d, boolean displayUnit) {
//		return String.format(Locale.ENGLISH, "%s %s",
//				this.formatterOptions.get(CellValueType.VECTOR_UNCERTAINTY).getFormatter().format(this.convertVectorUncertaintyToView(d)), 
//				displayUnit ? this.formatterOptions.get(CellValueType.SCALE_UNCERTAINTY).getUnit().getAbbreviation():"").trim();
//	}
	
	public String toVectorUncertaintyFormat(double d, boolean displayUnit) {
		return this.toViewFormat(CellValueType.VECTOR_UNCERTAINTY, this.convertVectorUncertaintyToView(d), displayUnit);
	}
	
	private String toViewFormat(CellValueType type, double d, boolean displayUnit) {
		Unit unit = this.formatterOptions.get(type).getUnit();
		if (unit.getType() == UnitType.DEGREE_SEXAGESIMAL) {
			double dms[] = this.toSexagesimalDegree(d);
			
			int degrees    = (int)dms[0];
			int minutes    = (int)dms[1];
			double seconds = dms[2]; 
			
			return String.format(Locale.ENGLISH, "%d \u00B7 %02d \u00B7 %s %s",
					degrees,
					minutes,
					this.formatterOptions.get(type).getFormatter().format(100+seconds).substring(1), // add 100 to get a leading zero (removed by substring)
					displayUnit ? unit.getAbbreviation():"").trim();
		}
		
		// in any other case
		return String.format(Locale.ENGLISH, "%s %s",
				this.formatterOptions.get(type).getFormatter().format(d), 
				displayUnit ? this.formatterOptions.get(type).getUnit().getAbbreviation():"").trim();
	}
	
	private double[] toSexagesimalDegree(double d) {
		double sign = Math.signum(d);
		d = sign * d;
		int degrees = (int) Math.floor(d);
		double fractions =  ((d - degrees) * 60.0);
		int minutes = (int) Math.floor(fractions);
		double seconds = (fractions - minutes) * 60.0;

        if (seconds == 60.0) {
        	minutes++;
        	seconds = 0;
        }
        if (minutes == 60.0) {
        	degrees++;
        	minutes = 0;
        }
		
		return new double[] {
				sign * degrees,
				minutes,
				seconds
		};
	}
	
	protected void fireUnitChanged(CellValueType type, Unit oldUnit, Unit newUnit, int res) {
		FormatterEvent evt = new FormatterEvent(this, FormatterEventType.UNIT_CHANGED, type, oldUnit, newUnit, res);
		Object listeners[] = this.listenerList.toArray();
		for (int i=0; i<listeners.length; i++) {
			if (listeners[i] instanceof FormatterChangedListener) {
				((FormatterChangedListener)listeners[i]).formatterChanged(evt);
			}
		}
	}
	
	protected void fireResolutionChanged(CellValueType type, Unit unit, int oldValue, int newValue) {
		FormatterEvent evt = new FormatterEvent(this, FormatterEventType.RESOLUTION_CHANGED, type, unit, oldValue, newValue);
		Object listeners[] = this.listenerList.toArray();
		for (int i=0; i<listeners.length; i++) {
			if (listeners[i] instanceof FormatterChangedListener) {
				((FormatterChangedListener)listeners[i]).formatterChanged(evt);
			}
		}
	}
	
	public void addFormatterChangedListener(FormatterChangedListener l) {
		this.listenerList.add(l);
	}
	
	public void removeFormatterChangedListener(FormatterChangedListener l) {
		this.listenerList.remove(l);
	}
}
