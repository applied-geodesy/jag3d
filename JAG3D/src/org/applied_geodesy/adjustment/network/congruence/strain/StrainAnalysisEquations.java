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

package org.applied_geodesy.adjustment.network.congruence.strain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameter;
import org.applied_geodesy.adjustment.point.Point;

import no.uib.cipr.matrix.Matrix;


public abstract class StrainAnalysisEquations {
	StrainParameter[] strainParameters = new StrainParameter[0];
	private List<RestrictionType> restrictions = new ArrayList<RestrictionType>(10);
	private Map<ParameterType, StrainParameter> strainParameterMap = new HashMap<ParameterType, StrainParameter>();
	StrainAnalysisEquations() {
		this.initDefaultRestictions();
		this.strainParameters = this.initStrainParameters();
		for (StrainParameter param : this.strainParameters) 
			strainParameterMap.put(param.getParameterType(), param);
	}
	
	StrainParameter getParameterByType(ParameterType type) {
		return this.strainParameterMap.get(type);
	}
	
	/**
	 * Anzahl der Zusatzparameter, die aus den Hinlfsgroessen gewonnen werden bspw. Drehwinkel aus Quaternion
	 * @return u
	 */
	public abstract int numberOfExpandedParameters();
	
	/**
	 * Anzahl der Parameter
	 * @return u
	 */
	public int numberOfParameters() {
		return this.strainParameters.length;
	}
	
	/**
	 * Liefert den i-ten Parameter
	 * @param i
	 * @return u
	 */
	public StrainParameter get(int i) {
		return this.strainParameters[i];
	}
	
	public static Equation[] getEquations(int dim) {
		switch(dim) {
		case 1:
			return new Equation[] {
					Equation.Z
			};
		case 2:
			return new Equation[] {
					Equation.X,
					Equation.Y,
			};
		case 3:
			return new Equation[] {
					Equation.X,
					Equation.Y,
					Equation.Z
			};
		}
		return null;
	}
	
	public static CoordinateComponent[] getCoordinateComponents(int dim) {
		switch(dim) {
		case 1:
			return new CoordinateComponent[] {
					CoordinateComponent.Z1,

					CoordinateComponent.Z2
			};
		case 2:
			return new CoordinateComponent[] {
					CoordinateComponent.X1,
					CoordinateComponent.Y1,

					CoordinateComponent.X2,
					CoordinateComponent.Y2
			};
		case 3:
			return new CoordinateComponent[] {
					CoordinateComponent.X1,
					CoordinateComponent.Y1,
					CoordinateComponent.Z1,

					CoordinateComponent.X2,
					CoordinateComponent.Y2,
					CoordinateComponent.Z2
			};
		}
		return null;
	}
	
//	/**
//	 * Anzahl der festgehaltenen Parameter
//	 * @return u
//	 */
//	public int numberOfConstraintParameters() {
//		int cnt = 0;
//		for (Restriction restriction : this.restrictions) {
//			if (restriction != Restriction.IDENT_SCALES_XY && restriction != Restriction.IDENT_SCALES_XZ && restriction != Restriction.IDENT_SCALES_YZ && restriction != Restriction.UNIT_QUATERNION)
//				cnt++;
//		}
//		return cnt;
//	}
	
	/**
	 * Liefert true, wenn Gruppenparameter bestimmt werden sollen
	 * @return hasUnconstraintParameters
	 */
	public boolean hasUnconstraintParameters() {
		return this.numberOfParameters() - this.numberOfRestrictions() > 0;
	}
	
	/**
	 * Anzahl der Restriktinen im Modell
	 * @return r
	 */
	public int numberOfRestrictions() {
		return this.restrictions.size();
	}
	
	/**
	 * Liefert <code>true</code>, wenn die <code>restriction</code> im Modell beruecksichtigt wird
	 * @param restriction
	 * @return restricted 
	 */
	public boolean isRestricted(RestrictionType restriction) {
		return this.restrictions.contains(restriction);
	}
	
	/**
	 * Liefert die Restriktion an der Stelle <code>index</code>
	 * @param index
	 * @return restriction
	 */
	public RestrictionType getRestriction(int index) {
		return this.restrictions.get(index);
	}
	
	/**
	 * Fuegt Restriktionen zum Modell hinzu
	 * @param restriction 
	 */
	public void addRestriction(RestrictionType restriction) {
		if (this.isSupportedRestriction(restriction) && !this.restrictions.contains(restriction)) {
			if (restriction == RestrictionType.FIXED_SCALE_X) {
				this.restrictions.remove(RestrictionType.IDENT_SCALES_XY);
				this.restrictions.remove(RestrictionType.IDENT_SCALES_XZ);
			}
			else if (restriction == RestrictionType.FIXED_SCALE_Y) {
				this.restrictions.remove(RestrictionType.IDENT_SCALES_XY);
				this.restrictions.remove(RestrictionType.IDENT_SCALES_YZ);
			}
			else if (restriction == RestrictionType.FIXED_SCALE_Z) {
				this.restrictions.remove(RestrictionType.IDENT_SCALES_XZ);
				this.restrictions.remove(RestrictionType.IDENT_SCALES_YZ);
			}

			else if (restriction == RestrictionType.IDENT_SCALES_XY) {
				this.restrictions.remove(RestrictionType.FIXED_SCALE_X);
				this.restrictions.remove(RestrictionType.FIXED_SCALE_Y);
			}
			else if (restriction == RestrictionType.IDENT_SCALES_XZ) {
				this.restrictions.remove(RestrictionType.FIXED_SCALE_X);
				this.restrictions.remove(RestrictionType.FIXED_SCALE_Z);
			}
			else if (restriction == RestrictionType.IDENT_SCALES_YZ) {
				this.restrictions.remove(RestrictionType.FIXED_SCALE_Y);
				this.restrictions.remove(RestrictionType.FIXED_SCALE_Z);
			}

			this.restrictions.add(restriction);

			// verhindere das Speichern von redundanten Information
			if (this.restrictions.contains(RestrictionType.IDENT_SCALES_XY) && this.restrictions.contains(RestrictionType.IDENT_SCALES_XZ) && this.restrictions.contains(RestrictionType.IDENT_SCALES_YZ)) 
				this.restrictions.remove(RestrictionType.IDENT_SCALES_XY);
		}
	}
	
	/**
	 * Bestimmt stochastische Kenngroessen der einzelnen Parameter und fuegt diese hinzu
	 * @param param
	 * @param sigma2apost
	 * @param qxxPrio
	 * @return param
	 */
	StrainParameter setStochasticParameters(StrainParameter param, double sigma2apost, double qxxPrio, boolean applyAposterioriVarianceOfUnitWeight) {
		qxxPrio        = Math.abs(qxxPrio);
		double value   = param.getValue();
		
		if (param.getParameterType() == ParameterType.STRAIN_ROTATION_X ||
				param.getParameterType() == ParameterType.STRAIN_ROTATION_Y ||
				param.getParameterType() == ParameterType.STRAIN_ROTATION_Z ||
				param.getParameterType() == ParameterType.STRAIN_SHEAR_X ||
				param.getParameterType() == ParameterType.STRAIN_SHEAR_X ||
				param.getParameterType() == ParameterType.STRAIN_SHEAR_Z) {
			value = MathExtension.MOD(value, 2.0*Math.PI);
			if (Math.abs(2.0*Math.PI - value) < Math.abs(value))
				value = value - 2.0*Math.PI;
		}
		
		double qxxPost = sigma2apost * qxxPrio;
		double value0  = param.getExpectationValue();
		double nabla   = value - value0;
		double tPrio   = qxxPrio < Constant.EPS ? Double.POSITIVE_INFINITY : nabla*nabla/qxxPrio;
		double tPost   = qxxPost < Constant.EPS ? Double.POSITIVE_INFINITY : nabla*nabla/qxxPost;
		double nabla0  = Math.signum(nabla) * Math.sqrt(qxxPrio);
		
		param.setTprio(tPrio);
		param.setTpost(applyAposterioriVarianceOfUnitWeight ? tPost : 0.0);
		param.setStd(Math.sqrt(qxxPost));
		param.setGrossError(nabla);
		param.setMinimalDetectableBias(nabla0);
		
		return param;
	}
	
	public abstract void expandParameters(double sigma2apost, Matrix Quu, boolean applyAposterioriVarianceOfUnitWeight);
	abstract void initDefaultRestictions();
	abstract StrainParameter[] initStrainParameters();
	public abstract boolean isSupportedRestriction(RestrictionType restriction);
	
	/**
	 * Liefert die partielle Ableitung der Funktion am entsprechenden Parameter zur Bildung der A-Matrix
	 * @param parameter
	 * @param p1
	 * @param equation
	 * @return diff
	 */
	public abstract double diff(StrainParameter parameter, Point p1, Equation equation);
	/**
	 * Liefert die partielle Ableitung der Funktion am Punkt p1 bzw. p2 zur Bildung der A-Matrix
	 * @param p1
	 * @param p2
	 * @param component
	 * @param equation
	 * @return diff
	 */
	public abstract double diff(Point p1, Point p2, CoordinateComponent component, Equation equation);
	/**
	 * Liefert die partielle Ableitung zur Bildung der R-Matrix fuer die Bedingungen
	 * @param parameter
	 * @param restriction
	 * @return diff
	 */
	public abstract double diff(StrainParameter parameter, RestrictionType restriction);
	public abstract double getContradiction(Point p1, Point p2, Equation equation);
	public abstract double getContradiction(RestrictionType restriction);
}
