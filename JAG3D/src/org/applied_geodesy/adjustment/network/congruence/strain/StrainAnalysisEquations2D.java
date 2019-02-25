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
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterA11;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterA12;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterA21;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterA22;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterRotationZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterScaleY;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterShearZ;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationX;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameterTranslationY;
import org.applied_geodesy.adjustment.point.Point;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class StrainAnalysisEquations2D extends StrainAnalysisEquations {

	@Override
	StrainParameter[] initStrainParameters() {
		return new StrainParameter[] {
				new StrainParameterTranslationX(),
				new StrainParameterTranslationY(),
				new StrainParameterA11(),
				new StrainParameterA12(),
				new StrainParameterA21(),
				new StrainParameterA22()
		};
	}

	@Override
	public boolean isSupportedRestriction(RestrictionType restriction) {
		return restriction == RestrictionType.FIXED_TRANSLATION_X ||
				restriction == RestrictionType.FIXED_TRANSLATION_Y ||
				restriction == RestrictionType.FIXED_ROTATION_Z ||
				restriction == RestrictionType.FIXED_SHEAR_Z ||
				restriction == RestrictionType.FIXED_SCALE_X ||
				restriction == RestrictionType.FIXED_SCALE_Y ||
				restriction == RestrictionType.IDENT_SCALES_XY;
	}

	@Override
	public double diff(StrainParameter parameter, Point p1, Equation equation) {
		double elmA = 0;
		//		double xC1 = this.centerPoints.getX1();
		//		double yC1 = this.centerPoints.getY1();

		double x1 = p1.getX();
		double y1 = p1.getY();

		//Schwerpunktreduktion
		//		double xP = x1 - xC1;
		//		double yP = y1 - yC1;

		double xP = x1;
		double yP = y1;

		if (equation == Equation.X) {
			switch(parameter.getParameterType()) {
			// x0
			case STRAIN_TRANSLATION_X:
				elmA = 1.0;
				break;
				// a11
			case STRAIN_A11:
				elmA =  xP;
				break;
				// a12
			case STRAIN_A12:
				elmA = -yP;
				break;
			default:
				elmA = 0.0;
				break;
			}
		}
		else if (equation == Equation.Y) {
			switch(parameter.getParameterType()) {
			// y0
			case STRAIN_TRANSLATION_Y:
				elmA = 1.0;
				break;
				// a21
			case STRAIN_A21:
				elmA = xP;
				break;
				// a22
			case STRAIN_A22:
				elmA = yP;
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
		double a11 = this.getParameterByType(ParameterType.STRAIN_A11).getValue();
		double a12 = this.getParameterByType(ParameterType.STRAIN_A12).getValue();
		double a21 = this.getParameterByType(ParameterType.STRAIN_A21).getValue();
		double a22 = this.getParameterByType(ParameterType.STRAIN_A22).getValue();

		double elmB = 0.0;
		if (equation == Equation.X) {
			switch(component) {
			// xS
			case X1:
				elmB =  a11;
				break;
				// yS
			case Y1:
				elmB = -a12;
				break;

				// xT
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
			// xS
			case X1:
				elmB = a21;
				break;
				// yS
			case Y1:
				elmB = a22;
				break;

				// yT
			case Y2:
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
		//
		//		double xC2 = this.centerPoints.getX2();
		//		double yC2 = this.centerPoints.getY2();

		double x1 = p1.getX();
		double y1 = p1.getY();

		double x2 = p2.getX();
		double y2 = p2.getY();

		//Schwerpunktreduktion
		//		double xP = x1 - xC1;
		//		double yP = y1 - yC1;
		//
		//		double XP = x2 - xC2;
		//		double YP = y2 - yC2;

		double xP = x1;
		double yP = y1;

		double XP = x2;
		double YP = y2;

		// Transformationsparameter
		double x0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_X).getValue();
		double y0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Y).getValue();

		double a11 = this.getParameterByType(ParameterType.STRAIN_A11).getValue();
		double a12 = this.getParameterByType(ParameterType.STRAIN_A12).getValue();
		double a21 = this.getParameterByType(ParameterType.STRAIN_A21).getValue();
		double a22 = this.getParameterByType(ParameterType.STRAIN_A22).getValue();

		return equation == Equation.X ? (x0 + a11*xP - a12*yP) - XP  :  (y0 + a21*xP + a22*yP) - YP;
	}

	@Override
	public double diff(StrainParameter parameter, RestrictionType restriction) {
		double elmR = 0.0;

		// Transformationsparameter
		double a11 = this.getParameterByType(ParameterType.STRAIN_A11).getValue();
		double a12 = this.getParameterByType(ParameterType.STRAIN_A12).getValue();
		double a21 = this.getParameterByType(ParameterType.STRAIN_A21).getValue();
		double a22 = this.getParameterByType(ParameterType.STRAIN_A22).getValue();


		if (restriction == RestrictionType.FIXED_TRANSLATION_X) {
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
		else if (restriction == RestrictionType.FIXED_ROTATION_Z) {
			switch(parameter.getParameterType()) {
			// a21
			case STRAIN_A21:
				elmR = 1.0;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SHEAR_Z) {
			switch(parameter.getParameterType()) {
			// a11
			case STRAIN_A11:
				elmR = -a12;
				break;
				// a12
			case STRAIN_A12:
				elmR = -a11;
				break;
				// a21
			case STRAIN_A21:
				elmR =  a22;
				break;
				// a22
			case STRAIN_A22:
				elmR =  a21;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_X) {
			switch(parameter.getParameterType()) {
			// a11
			case STRAIN_A11:
				elmR = 2.0*a11;
				break;
				// a21
			case STRAIN_A21:
				elmR = 2.0*a21;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.FIXED_SCALE_Y) {
			switch(parameter.getParameterType()) {
			// a12
			case STRAIN_A12:
				elmR = 2.0*a12;
				break;
				// a22
			case STRAIN_A22:
				elmR = 2.0*a22;
				break;
			default:
				elmR = 0.0;
				break;
			}
		}
		else if (restriction == RestrictionType.IDENT_SCALES_XY) {
			switch(parameter.getParameterType()) {
			// a11
			case STRAIN_A11:
				elmR =  2.0*a11;
				break;
				// a12
			case STRAIN_A12:
				elmR = -2.0*a12;
				break;
				// a21
			case STRAIN_A21:
				elmR =  2.0*a21;
				break;
				// a22
			case STRAIN_A22:
				elmR = -2.0*a22;
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
		double x0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_X).getValue();
		double y0  = this.getParameterByType(ParameterType.STRAIN_TRANSLATION_Y).getValue();

		double a11 = this.getParameterByType(ParameterType.STRAIN_A11).getValue();
		double a12 = this.getParameterByType(ParameterType.STRAIN_A12).getValue();
		double a21 = this.getParameterByType(ParameterType.STRAIN_A21).getValue();
		double a22 = this.getParameterByType(ParameterType.STRAIN_A22).getValue();


		switch(restriction) {
		case FIXED_TRANSLATION_X:
			return x0;
		case FIXED_TRANSLATION_Y:
			return y0;
		case FIXED_ROTATION_Z:
			return a21;
		case FIXED_SHEAR_Z:
			return -a11*a12 + a21*a22;
		case FIXED_SCALE_X:
			return (a11*a11 + a21*a21) - 1.0;
		case FIXED_SCALE_Y:
			return (a12*a12 + a22*a22) - 1.0;
		case IDENT_SCALES_XY:
			return (a11*a11 + a21*a21) - (a12*a12 + a22*a22);
		default:
			return 0.0;
		}
	}
	
	@Override
	void initDefaultRestictions() {}
	
	@Override
	public void expandParameters(double sigma2apost, Matrix Quu, boolean applyAposterioriVarianceOfUnitWeight) {
		int addPar = this.numberOfExpandedParameters();
		// Transformationsparameter
		double a11 = this.getParameterByType(ParameterType.STRAIN_A11).getValue();
		double a12 = this.getParameterByType(ParameterType.STRAIN_A12).getValue();
		double a21 = this.getParameterByType(ParameterType.STRAIN_A21).getValue();
		double a22 = this.getParameterByType(ParameterType.STRAIN_A22).getValue();
	    
	    double mx = Math.hypot(a11, a21);
	    double my = Math.hypot(a12, a22);
	    
	    double r = MathExtension.MOD(  a11              != 0 ? Math.atan(a21/ a11) : 0.0, 2.0*Math.PI );
	    double s = MathExtension.MOD( (a21*a12+a11*a22) != 0 ? Math.atan((-a11*a12+a21*a22)/ (a21*a12+a11*a22)) : 0.0, 2.0*Math.PI );

	    int nou = this.numberOfParameters();
		Matrix A = new DenseMatrix(nou + addPar, nou);

		for (int i=0; i<nou; i++) {
			A.set(i,i, 1.0);
		}
		
		// mx
		A.set(nou, 2, a11/mx );
		A.set(nou, 4, a21/mx );
		
		// my
		A.set(nou+1, 3, a12/my );
		A.set(nou+1, 5, a22/my );
		
		// r
		A.set(nou+2, 2, -a21/(a11*a11+a21*a21) );
		A.set(nou+2, 4,  a11/(a11*a11+a21*a21) );
		
		// s
		A.set(nou+3, 2, -a21/(a21*a21+a11*a11) ); // a11
		A.set(nou+3, 3, -a22/(a12*a12+a22*a22) ); // a12
		A.set(nou+3, 4,  a11/(a21*a21+a11*a11) ); // a21
		A.set(nou+3, 5,  a12/(a12*a12+a22*a22) ); // a22
		
		Matrix AQuu = new DenseMatrix(nou + addPar, nou);
		A.mult(Quu, AQuu);
		Matrix AQuuAT = new UpperSymmPackMatrix(nou + addPar);
		AQuu.transBmult(A, AQuuAT);
		
		StrainParameterScaleX scaleX = new StrainParameterScaleX(mx);
		StrainParameterScaleY scaleY = new StrainParameterScaleY(my);
		
		StrainParameterRotationZ rotatZ = new StrainParameterRotationZ(r);
		StrainParameterShearZ shearZ = new StrainParameterShearZ(s);
		
		StrainParameter strainParameters[] = new StrainParameter[nou + addPar];
		System.arraycopy(this.strainParameters, 0, strainParameters, 0, nou);
		strainParameters[nou + 0] = scaleX;
		strainParameters[nou + 1] = scaleY;
		
		strainParameters[nou + 2] = rotatZ;
		strainParameters[nou + 3] = shearZ;
		
		
		this.strainParameters = strainParameters;
		
		for (int i=0; i<this.strainParameters.length; i++) {
			StrainParameter param = this.strainParameters[i];
			this.setStochasticParameters(param, sigma2apost, AQuuAT.get(i, i), applyAposterioriVarianceOfUnitWeight);
		}
	}
	
	@Override
	public int numberOfExpandedParameters() {
		return 4;
	}
}
