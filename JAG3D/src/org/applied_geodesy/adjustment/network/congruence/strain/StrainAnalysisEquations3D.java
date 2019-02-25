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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameter;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterQ0;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterQ1;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterQ2;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterQ3;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterRotationX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterRotationY;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterRotationZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS11;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS12;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS13;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS22;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS23;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterS33;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleY;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterShearX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterShearY;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterShearZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationY;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationZ;
import org.applied_geodesy.adjustment.point.Point;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class StrainAnalysisEquations3D extends StrainAnalysisEquations {

	@Override
	StrainParameter[] initStrainParameters() {
		return new StrainParameter[] {
				new StrainParameterTranslationX(),
				new StrainParameterTranslationY(),
				new StrainParameterTranslationZ(),
				new StrainParameterQ0(),
				new StrainParameterQ1(),
				new StrainParameterQ2(),
				new StrainParameterQ3(),
				new StrainParameterS11(),
				new StrainParameterS12(),
				new StrainParameterS13(),
				new StrainParameterS22(),
				new StrainParameterS23(),
				new StrainParameterS33()
		};
	}

	@Override
	public boolean isSupportedRestriction(RestrictionType restriction) {
		return restriction == RestrictionType.UNIT_QUATERNION || 
				restriction == RestrictionType.FIXED_TRANSLATION_X || 
				restriction == RestrictionType.FIXED_TRANSLATION_Y ||
				restriction == RestrictionType.FIXED_TRANSLATION_Z ||

				restriction == RestrictionType.FIXED_ROTATION_X ||
				restriction == RestrictionType.FIXED_ROTATION_Y ||
				restriction == RestrictionType.FIXED_ROTATION_Z ||

				restriction == RestrictionType.FIXED_SHEAR_X ||
				restriction == RestrictionType.FIXED_SHEAR_Y ||
				restriction == RestrictionType.FIXED_SHEAR_Z ||

				restriction == RestrictionType.FIXED_SCALE_X ||
				restriction == RestrictionType.FIXED_SCALE_Y ||
				restriction == RestrictionType.FIXED_SCALE_Z ||

				restriction == RestrictionType.IDENT_SCALES_XY || 
				restriction == RestrictionType.IDENT_SCALES_XZ ||
				restriction == RestrictionType.IDENT_SCALES_YZ;
	}

	@Override
	public double diff(StrainParameter parameter, Point p1, Equation equation) {
		double elmA = 0;

		// Schwerpunkt
		//		double xC1 = this.centerPoints.getX1();
		//		double yC1 = this.centerPoints.getY1();
		//		double zC1 = this.centerPoints.getZ1();

		double x1 = p1.getX();
		double y1 = p1.getY();
		double z1 = p1.getZ();

		//Schwerpunktreduktion
		//		double xP = x1 - xC1;
		//		double yP = y1 - yC1;
		//		double zP = z1 - zC1;

		double xP = x1;
		double yP = y1;
		double zP = z1;

		// Transformationsparameter
		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();

		// Rotationsmatrix
		double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
		double r12 = 2.0*(q1*q2-q0*q3);
		double r13 = 2.0*(q1*q3+q0*q2);

		double r21 = 2.0*(q1*q2+q0*q3);
		double r22 = 2.0*q0*q0-1.0+2.0*q2*q2;
		double r23 = 2.0*(q2*q3-q0*q1);

		double r31 = 2.0*(q1*q3-q0*q2);
		double r32 = 2.0*(q2*q3+q0*q1);
		double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;

		double smxP = s11*xP+s12*yP+s13*zP;
		double smyP = s22*yP+s23*zP;
		double smzP = s33*zP;

		if (equation == Equation.X) {
			switch(parameter.getParameterType()) {
			// X0
			case STRAIN_TRANSLATION_X:
				elmA = 1.0;
				break;
				// Y0
			case STRAIN_TRANSLATION_Y:
				elmA = 0.0;
				break;
				// Z0
			case STRAIN_TRANSLATION_Z:
				elmA = 0.0;
				break;
				// q0
			case STRAIN_Q0:
				elmA =  4.0*q0*smxP-2.0*q3*smyP+2.0*q2*smzP;
				break;
				// q1
			case STRAIN_Q1:
				elmA =  4.0*q1*smxP+2.0*q2*smyP+2.0*q3*smzP;
				break;
				// q2
			case STRAIN_Q2:
				elmA =  2.0*q1*smyP+2.0*q0*smzP;
				break;
				// q3
			case STRAIN_Q3:
				elmA = -2.0*q0*smyP+2.0*q1*smzP;
				break;
				// s11
			case STRAIN_S11:
				elmA = r11*xP;
				break;
				// s12
			case STRAIN_S12:
				elmA = r11*yP;
				break;
				// s13
			case STRAIN_S13:
				elmA = r11*zP;
				break;
				// s22
			case STRAIN_S22:
				elmA = r12*yP;
				break;
				// s23
			case STRAIN_S23:
				elmA = r12*zP;
				break;
				// s33
			case STRAIN_S33:
				elmA = r13*zP;
				break;
			default:
				elmA = 0.0;
				break;
			}
		}
		else if (equation == Equation.Y) {
			switch(parameter.getParameterType()) {
			// X0
			case STRAIN_TRANSLATION_X:
				elmA = 0.0;
				break;
				// Y0
			case STRAIN_TRANSLATION_Y:
				elmA = 1.0;
				break;
				// Z0
			case STRAIN_TRANSLATION_Z:
				elmA = 0.0;
				break;
				// q0
			case STRAIN_Q0:
				elmA = 2.0*q3*smxP+4.0*q0*smyP-2.0*q1*smzP;
				break;
				// q1
			case STRAIN_Q1:
				elmA = 2.0*q2*smxP-2.0*q0*smzP;
				break;
				// q2
			case STRAIN_Q2:
				elmA = 2.0*q1*smxP+4.0*q2*smyP+2.0*q3*smzP;
				break;
				// q3
			case STRAIN_Q3:
				elmA = 2.0*q0*smxP+2.0*q2*smzP;
				break;
				// s11
			case STRAIN_S11:
				elmA = r21*xP;
				break;
				// s12
			case STRAIN_S12:
				elmA = r21*yP;
				break;
				// s13
			case STRAIN_S13:
				elmA = r21*zP;
				break;
				// s22
			case STRAIN_S22:
				elmA = r22*yP;
				break;
				// s23
			case STRAIN_S23:
				elmA = r22*zP;
				break;
				// s33
			case STRAIN_S33:
				elmA = r23*zP;
				break;
			default:
				elmA = 0.0;
				break;
			}
		}
		else if (equation == Equation.Z) {
			switch(parameter.getParameterType()) {
			// X0
			case STRAIN_TRANSLATION_X:
				elmA = 0.0;
				break;
				// Y0
			case STRAIN_TRANSLATION_Y:
				elmA = 0.0;
				break;
				// Z0
			case STRAIN_TRANSLATION_Z:
				elmA = 1.0;
				break;
				// q0
			case STRAIN_Q0:
				elmA = -2.0*q2*smxP+2.0*q1*smyP+4.0*q0*smzP;
				break;
				// q1
			case STRAIN_Q1:
				elmA =  2.0*q3*smxP+2.0*q0*smyP;
				break;
				// q2
			case STRAIN_Q2:
				elmA = -2.0*q0*smxP+2.0*q3*smyP;
				break;
				// q3
			case STRAIN_Q3:
				elmA =  2.0*q1*smxP+2.0*q2*smyP+4.0*q3*smzP;
				break;
				// s11
			case STRAIN_S11:
				elmA = r31*xP;
				break;
				// s12
			case STRAIN_S12:
				elmA = r31*yP;
				break;
				// s13
			case STRAIN_S13:
				elmA = r31*zP;
				break;
				// s22
			case STRAIN_S22:
				elmA = r32*yP;
				break;
				// s23
			case STRAIN_S23:
				elmA = r32*zP;
				break;
				// s33
			case STRAIN_S33:
				elmA = r33*zP;
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
		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();

		// Rotationsmatrix
		double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
		double r12 = 2.0*(q1*q2-q0*q3);
		double r13 = 2.0*(q1*q3+q0*q2);

		double r21 = 2.0*(q1*q2+q0*q3);
		double r22 = 2.0*q0*q0-1.0+2.0*q2*q2;
		double r23 = 2.0*(q2*q3-q0*q1);

		double r31 = 2.0*(q1*q3-q0*q2);
		double r32 = 2.0*(q2*q3+q0*q1);
		double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;

		double elmB = 0.0;
		if (equation == Equation.X) {
			switch(component) {
			// xS-Wert
			case X1:
				elmB = r11*s11;
				break;
				// yS-Wert
			case Y1:
				elmB = r11*s12+r12*s22;
				break;
				// zS-Wert
			case Z1:
				elmB = r11*s13+r12*s23+r13*s33;
				break;
				// xT-Wert
			case X2:
				elmB = -1.0;
				break;
			default:
				elmB =  0.0;
				break;

			}
		}
		else if (equation == Equation.Y) {
			switch(component) {
			// xS-Wert
			case X1:
				elmB = r21*s11;
				break;
				// yS-Wert
			case Y1:
				elmB = r21*s12+r22*s22;
				break;
				// zS-Wert
			case Z1:
				elmB = r21*s13+r22*s23+r23*s33;
				break;
				// yT-Wert
			case Y2:
				elmB = -1.0;
				break;
			default:
				elmB =  0.0;
				break;

			}
		}
		else if (equation == Equation.Z) {
			switch(component) {
			// xS-Wert
			case X1:
				elmB = r31*s11;
				break;
				// yS-Wert
			case Y1:
				elmB = r31*s12+r32*s22;
				break;
				// zS-Wert
			case Z1:
				elmB = r31*s13+r32*s23+r33*s33;
				break;
				// zT-Wert
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
		//		double xC1 = this.centerPoints.getX1();
		//		double yC1 = this.centerPoints.getY1();
		//		double zC1 = this.centerPoints.getZ1();
		//
		//		double xC2 = this.centerPoints.getX2();
		//		double yC2 = this.centerPoints.getY2();
		//		double zC2 = this.centerPoints.getZ2();

		double x1 = p1.getX();
		double y1 = p1.getY();
		double z1 = p1.getZ();

		double x2 = p2.getX();
		double y2 = p2.getY();
		double z2 = p2.getZ();

		//Schwerpunktreduktion
		//		double xP = x1 - xC1;
		//		double yP = y1 - yC1;
		//		double zP = z1 - zC1;
		//		
		//		double XP = x2 - xC2;
		//		double YP = y2 - yC2;
		//		double ZP = z2 - zC2;

		double xP = x1;
		double yP = y1;
		double zP = z1;

		double XP = x2;
		double YP = y2;
		double ZP = z2;

		// Transformationsparameter
		double x0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_X).getValue();
		double y0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Y).getValue();
		double z0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Z).getValue();

		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();

		// Rotationsmatrix
		double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
		double r12 = 2.0*(q1*q2-q0*q3);
		double r13 = 2.0*(q1*q3+q0*q2);

		double r21 = 2.0*(q1*q2+q0*q3);
		double r22 = 2.0*q0*q0-1.0+2.0*q2*q2;
		double r23 = 2.0*(q2*q3-q0*q1);

		double r31 = 2.0*(q1*q3-q0*q2);
		double r32 = 2.0*(q2*q3+q0*q1);
		double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;

		double smxP = s11*xP+s12*yP+s13*zP;
		double smyP = s22*yP+s23*zP;
		double smzP = s33*zP;

		if (equation == Equation.X) 
			return (x0 + (r11*smxP + r12*smyP + r13*smzP)) - XP;
		else if (equation == Equation.Y) 
			return (y0 + (r21*smxP + r22*smyP + r23*smzP)) - YP;
		return (z0 + (r31*smxP + r32*smyP + r33*smzP)) - ZP;
	}

	@Override
	public double diff(StrainParameter parameter, RestrictionType restriction) {
		double elmR = 0.0;

		// Transformationsparameter
		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();

		if (restriction == RestrictionType.UNIT_QUATERNION) {
			switch(parameter.getParameterType()) {
			// q0
			case STRAIN_Q0:
				elmR = 2.0*q0;
				break;
				// q1
			case STRAIN_Q1:
				elmR = 2.0*q1;
				break;
				// q2
			case STRAIN_Q2:
				elmR = 2.0*q2;
				break;
				// q3
			case STRAIN_Q3:
				elmR = 2.0*q3;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_TRANSLATION_X) {
			switch(parameter.getParameterType()) {
			// x0
			case STRAIN_TRANSLATION_X:
				elmR = 1.0;
				break;	
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_TRANSLATION_Y) {
			switch(parameter.getParameterType()) {
			// y0
			case STRAIN_TRANSLATION_Y:
				elmR = 1.0;
				break;	
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_TRANSLATION_Z) {
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
		else if (restriction == RestrictionType.FIXED_ROTATION_X) {
			switch(parameter.getParameterType()) {
			// q0
			case STRAIN_Q0:
				elmR = -q1;
				break;
				// q1
			case STRAIN_Q1:
				elmR = -q0;
				break;
				// q2
			case STRAIN_Q2:
				elmR = q3;
				break;
				// q3
			case STRAIN_Q3:
				elmR = q2;
				break;	
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_ROTATION_Y) {
			switch(parameter.getParameterType()) {
			// q0
			case STRAIN_Q0:
				elmR = q2;
				break;
				// q1
			case STRAIN_Q1:
				elmR = q3;
				break;
				// q2
			case STRAIN_Q2:
				elmR = q0;
				break;
				// q3
			case STRAIN_Q3:
				elmR = q1;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_ROTATION_Z) {
			switch(parameter.getParameterType()) {
			// q0
			case STRAIN_Q0:
				elmR = -q3;
				break;
				// q1
			case STRAIN_Q1:
				elmR =  q2;
				break;
				// q2
			case STRAIN_Q2:
				elmR =  q1;
				break;
				// q3
			case STRAIN_Q3:
				elmR = -q0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_X) {
			switch(parameter.getParameterType()) {
			// s11
			case STRAIN_S11:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_Y) {
			switch(parameter.getParameterType()) {
			// s12
			case STRAIN_S12:
				elmR = 2.0*s12;
				break;
				// s22
			case STRAIN_S22:
				elmR = 2.0*s22;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_Z) {
			switch(parameter.getParameterType()) {
			// s13
			case STRAIN_S13:
				elmR = 2.0*s13;
				break;
				// s23
			case STRAIN_S23:
				elmR = 2.0*s23;
				break;
				// s33
			case STRAIN_S33:
				elmR = 2.0*s33;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SHEAR_X) {
			switch(parameter.getParameterType()) {
			// s23
			case STRAIN_S23:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SHEAR_Y) {
			switch(parameter.getParameterType()) {
			// s13
			case STRAIN_S13:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SHEAR_Z) {
			switch(parameter.getParameterType()) {
			// s12
			case STRAIN_S12:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.IDENT_SCALES_XY) {
			switch(parameter.getParameterType()) {
			// s11
			case STRAIN_S11:
				elmR = -2.0*s11;
				break;
				// s12
			case STRAIN_S12:
				elmR =  2.0*s12;
				break;
				// s22
			case STRAIN_S22:
				elmR =  2.0*s22;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.IDENT_SCALES_XZ) {
			switch(parameter.getParameterType()) {
			// s11
			case STRAIN_S11:
				elmR = -2.0*s11;
				break;
				// s13
			case STRAIN_S13:
				elmR = 2.0*s13;
				break;
				// s23
			case STRAIN_S23:
				elmR = 2.0*s23;
				break;
				// s33
			case STRAIN_S33:
				elmR = 2.0*s33;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.IDENT_SCALES_YZ) {
			switch(parameter.getParameterType()) {
			// s12
			case STRAIN_S12:
				elmR = -2.0*s12;
				break;
				// s22
			case STRAIN_S22:
				elmR = -2.0*s22;
				break;

				// s13
			case STRAIN_S13:
				elmR = 2.0*s13;
				break;
				// s23
			case STRAIN_S23:
				elmR = 2.0*s23;
				break;
				// s33
			case STRAIN_S33:
				elmR = 2.0*s33;
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
		double x0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_X).getValue();
		double y0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Y).getValue();
		double z0 = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Z).getValue();

		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();

		switch(restriction) {
		case UNIT_QUATERNION:
			return (q0*q0 + q1*q1 + q2*q2 + q3*q3) - 1.0;
		case FIXED_TRANSLATION_X: 
			return x0; 
		case FIXED_TRANSLATION_Y: 
			return y0; 
		case FIXED_TRANSLATION_Z: 
			return z0; 
		case FIXED_ROTATION_X: 
			return q2*q3-q0*q1;	
		case FIXED_ROTATION_Y: 
			return q1*q3+q0*q2;	
		case FIXED_ROTATION_Z: 
			return q1*q2-q0*q3;
		case FIXED_SCALE_X: 
			return s11 - 1.0;
		case FIXED_SCALE_Y: 
			return s12*s12 + s22*s22 - 1.0;
		case FIXED_SCALE_Z: 
			return s13*s13 + s23*s23 + s33*s33 - 1.0;
		case FIXED_SHEAR_X: 
			return s23;
		case FIXED_SHEAR_Y: 
			return s13; 
		case FIXED_SHEAR_Z: 
			return s12; 
		case IDENT_SCALES_XY: 
			return s12*s12 + s22*s22 - s11*s11;
		case IDENT_SCALES_XZ: 
			return s13*s13 + s23*s23 + s33*s33 - s11*s11;
		case IDENT_SCALES_YZ: 
			return s13*s13 + s23*s23 + s33*s33 - s12*s12 - s22*s22;
		default:
			return 0;
		}
	}
	
	@Override
	void initDefaultRestictions() {
		this.addRestriction(RestrictionType.UNIT_QUATERNION);
	}

	@Override
	public void expandParameters(double sigma2apost, Matrix Quu, boolean applyAposterioriVarianceOfUnitWeight) {
		int addPar = this.numberOfExpandedParameters();
		// Transformationsparameter
		double q0 = this.getParameterByType(ParameterType.STRAIN_Q0).getValue();
		double q1 = this.getParameterByType(ParameterType.STRAIN_Q1).getValue();
		double q2 = this.getParameterByType(ParameterType.STRAIN_Q2).getValue();
		double q3 = this.getParameterByType(ParameterType.STRAIN_Q3).getValue();

		double s11 = this.getParameterByType(ParameterType.STRAIN_S11).getValue();
		double s12 = this.getParameterByType(ParameterType.STRAIN_S12).getValue();
		double s13 = this.getParameterByType(ParameterType.STRAIN_S13).getValue();

		double s22 = this.getParameterByType(ParameterType.STRAIN_S22).getValue();
		double s23 = this.getParameterByType(ParameterType.STRAIN_S23).getValue();

		double s33 = this.getParameterByType(ParameterType.STRAIN_S33).getValue();
	    
	    // Rotationsmatrix
	    double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
	    double r12 = 2.0*(q1*q2-q0*q3);
	    double r13 = 2.0*(q1*q3+q0*q2);
	    
	    double r23 = 2.0*(q2*q3-q0*q1);
	    
	    double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;

	    double mx = Math.abs(s11);
		double my = Math.hypot(s12, s22);
		double mz = Math.sqrt(s13*s13 + s23*s23 + s33*s33);
		
		double rx = MathExtension.MOD( r33 != 0 ? Math.atan(r23/ r33) : 0.0, 2.0*Math.PI );
		double ry = MathExtension.MOD( Math.asin(-r13)     , 2.0*Math.PI );
		double rz = MathExtension.MOD( r11 != 0 ? Math.atan(r12/ r11) : 0.0, 2.0*Math.PI );

		double sx = MathExtension.MOD( s33 != 0 ? Math.atan(-s23/ s33) : 0.0, 2.0*Math.PI );
		double sy = MathExtension.MOD( Math.hypot(s23, s33) > 0.0 ? Math.atan(-s13/ Math.hypot(s23, s33)) : 0.0, 2.0*Math.PI );
		double sz = MathExtension.MOD( s22 != 0 ? Math.atan(-s12/ s22) : 0.0, 2.0*Math.PI );

		int nou = this.numberOfParameters();
		Matrix A = new DenseMatrix(nou+addPar, nou);
		
		for (int i=0; i<nou; i++) {
			A.set(i,i, 1.0);
		}
		
		// mx
		A.set(nou, 7,  1.0 );
		
		// my
		A.set(nou+1,  8,  s12/my );
		A.set(nou+1, 10,  s22/my );
		
		// mz
		A.set(nou+2,  9,  s13/mz );
		A.set(nou+2, 11,  s23/mz );
		A.set(nou+2, 12,  s33/mz );
		
		// rx
		A.set(nou+3,  3, -2.0*(q1*r33+2.0*r23*q0)/(r33*r33+r23*r23) );
		A.set(nou+3,  4, -2.0*q0*r33/(r33*r33+r23*r23) );
		A.set(nou+3,  5,  2.0*q3*r33/(r33*r33+r23*r23) );
		A.set(nou+3,  6,  2.0*(q2*r33-2.0*r23*q3)/(r33*r33+r23*r23) );
		
		// ry
		A.set(nou+4,  3, -2.0*q2/Math.sqrt(1.0-(r13*r13)) );
		A.set(nou+4,  4, -2.0*q3/Math.sqrt(1.0-(r13*r13)) );
		A.set(nou+4,  5, -2.0*q0/Math.sqrt(1.0-(r13*r13)) );
		A.set(nou+4,  6, -2.0*q1/Math.sqrt(1.0-(r13*r13)) );
		
		// rz
		A.set(nou+5,  3, -2.0*(q3*r11+2.0*r12*q0)/(r11*r11+r12*r12) );
		A.set(nou+5,  4,  2.0*(q2*r11-2.0*r12*q1)/(r11*r11+r12*r12) );
		A.set(nou+5,  5,  2.0*q1*r11/(r11*r11+r12*r12) );
		A.set(nou+5,  6, -2.0*q0*r11/(r11*r11+r12*r12) );
		
		// sx
		A.set(nou+6, 11, -s33/(s33*s33+s23*s23) );
		A.set(nou+6, 12,  s23/(s33*s33+s23*s23) );
		
		// sy
		if ((s23*s23+s33*s33+s13*s13) > 0) {
			double d1 = Math.hypot(s23, s33);
			double d2 = s23*s23 + s33*s33 + s13*s13;
			
			A.set(nou+7, 9, -d1/d2);
			if (d1 > 0) {
				A.set(nou+7, 11,  s13/d1 * s23/d2);
				A.set(nou+7, 12,  s13/d1 * s33/d2);
			}
		}
		
		// sz
		A.set(nou+8,  8, -s22/(s22*s22+s12*s12) );
		A.set(nou+8, 10,  s12/(s22*s22+s12*s12) );

		
		Matrix AQuu = new DenseMatrix(nou+addPar, nou);
		A.mult(Quu, AQuu);
		Matrix AQuuAT = new UpperSymmPackMatrix(nou+addPar);
		AQuu.transBmult(A, AQuuAT);
		
		StrainParameterScaleX scaleX = new StrainParameterScaleX(mx);
		StrainParameterScaleY scaleY = new StrainParameterScaleY(my);
		StrainParameterScaleZ scaleZ = new StrainParameterScaleZ(mz);
		
		StrainParameterRotationX rotatX = new StrainParameterRotationX(rx);
		StrainParameterRotationY rotatY = new StrainParameterRotationY(ry);
		StrainParameterRotationZ rotatZ = new StrainParameterRotationZ(rz);
		
		StrainParameterShearX shearX = new StrainParameterShearX(sx);
		StrainParameterShearY shearY = new StrainParameterShearY(sy);
		StrainParameterShearZ shearZ = new StrainParameterShearZ(sz);
		
		StrainParameter strainParameters[] = new StrainParameter[nou + addPar];
		System.arraycopy(this.strainParameters, 0, strainParameters, 0, nou);
		strainParameters[nou + 0] = scaleX;
		strainParameters[nou + 1] = scaleY;
		strainParameters[nou + 2] = scaleZ;
		
		strainParameters[nou + 3] = rotatX;
		strainParameters[nou + 4] = rotatY;
		strainParameters[nou + 5] = rotatZ;
		
		strainParameters[nou + 6] = shearX;
		strainParameters[nou + 7] = shearY;
		strainParameters[nou + 8] = shearZ;
		
		this.strainParameters = strainParameters;
		
		for (int i=0; i<this.strainParameters.length; i++) {
			StrainParameter param = this.strainParameters[i];
			this.setStochasticParameters(param, sigma2apost, AQuuAT.get(i, i), applyAposterioriVarianceOfUnitWeight);
		}
	}
	
	@Override
	public int numberOfExpandedParameters() {
		return 9;
	}
}
