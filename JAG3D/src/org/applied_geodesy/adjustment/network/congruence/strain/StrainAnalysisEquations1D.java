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

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameter;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationZ;
import org.applied_geodesy.adjustment.point.Point;

import no.uib.cipr.matrix.Matrix;

public class StrainAnalysisEquations1D extends StrainAnalysisEquations {

	@Override
	StrainParameter[] initStrainParameters() {
		return new StrainParameter[] {
				new StrainParameterTranslationZ(),
				new StrainParameterScaleZ()
		};
	}
	
	@Override
	public boolean isSupportedRestriction(RestrictionType restriction) {
		return restriction == RestrictionType.FIXED_TRANSLATION_Z || restriction == RestrictionType.FIXED_SCALE_Z;
	}

	@Override
	public double diff(StrainParameter parameter, Point p1, Equation equation) {
		double elmA = 0;
		//double zC1 = this.centerPoints.getZ1();
		double z1 = p1.getZ();

		//Schwerpunktreduktion
		//double zP = z1 - zC1;
		double zP = z1;

		if (equation == Equation.Z) {
			switch(parameter.getParameterType()) {
			// z0
			case STRAIN_TRANSLATION_Z:
				elmA = 1.0;
				break;
				// mz
			case STRAIN_SCALE_Z:
				elmA =  zP;
				break;
			default:
				elmA = 0.0;
				break;
			}
		}
		return elmA;
	}

	@Override
	public double diff(Point p1, Point p2, CoordinateComponent component, Equation equation) {
		// Transformationsparameter
		double mz = this.getParameterByType(ParameterType.STRAIN_SCALE_Z).getValue();
		double elmB = 0.0;
		if (equation == Equation.Z) {
			switch(component) {
			// zS
			case Z1:
				elmB =  mz;
				break;
				// zT
			case Z2:
				elmB = -1.0;
				break;
			default:
				elmB =  0.0;
				break;
			}	    	
		}
		return elmB;
	}

	@Override
	public double getContradiction(Point p1, Point p2, Equation equation) {
		//		double zC1 = this.centerPoints.getZ1();
		//		double zC2 = this.centerPoints.getZ2();

		double z1 = p1.getZ();
		double z2 = p2.getZ();

		//Schwerpunktreduktion
		//		double zP = z1 - zC1;
		//		double ZP = z2 - zC2;
		double zP = z1;
		double ZP = z2;

		// Transformationsparameter
		double z0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Z).getValue();
		double mz  = this.getParameterByType(ParameterType.STRAIN_SCALE_Z).getValue();

		return (z0 + mz*zP) - ZP;
	}

	@Override
	public double diff(StrainParameter parameter, RestrictionType restriction) {
		double elmR = 0.0;

		if (restriction == RestrictionType.FIXED_TRANSLATION_Z) {
			switch(parameter.getParameterType()) {
			// z0
			case STRAIN_TRANSLATION_Z:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_Z) {
			switch(parameter.getParameterType()) {
			// mz
			case STRAIN_SCALE_Z:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		return elmR;
	}

	@Override
	public double getContradiction(RestrictionType restriction) {
		// Transformationsparameter
		double z0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Z).getValue();
		double mz  = this.getParameterByType(ParameterType.STRAIN_SCALE_Z).getValue();

		switch(restriction) {
		case FIXED_TRANSLATION_Z:
			return z0;
		case FIXED_SCALE_Z:
			return mz - 1.0;
		default:
			return 0.0;
		}
	}

	@Override
	void initDefaultRestictions() {}
	
	@Override
	public void expandParameters(double sigma2apost, Matrix Quu, boolean applyAposterioriVarianceOfUnitWeight) {
		for (int i=0; i<this.strainParameters.length; i++) {
			StrainParameter param = this.strainParameters[i];
			this.setStochasticParameters(param, sigma2apost, Quu.get(i, i), applyAposterioriVarianceOfUnitWeight);
		}
	}
	
	@Override
	public int numberOfExpandedParameters() {
		return 0;
	}
}
