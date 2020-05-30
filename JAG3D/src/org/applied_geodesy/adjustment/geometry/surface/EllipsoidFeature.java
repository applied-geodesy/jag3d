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

package org.applied_geodesy.adjustment.geometry.surface;

import java.util.Collection;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.SurfaceFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Ellipsoid;
import org.applied_geodesy.adjustment.geometry.surface.primitive.QuadraticSurface;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class EllipsoidFeature extends SurfaceFeature {
	private final Ellipsoid ellipsoid;
	
	public EllipsoidFeature() {
		super(true);
		
		this.ellipsoid = new Ellipsoid();
		this.add(this.ellipsoid);
	}

	public Ellipsoid getEllipsoid() {
		return this.ellipsoid;
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, EllipsoidFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.ellipsoid);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Ellipsoid ellipsoid) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		final int dim = 3;
		final double INV_SQRT2 = Math.sqrt(2.0);
		
		QuadraticSurface quadraticSurface = new QuadraticSurface();
		QuadraticSurfaceFeature.deriveInitialGuess(points, quadraticSurface);
		
		double a = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_A).getValue0();
		double b = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_B).getValue0();
		double c = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_C).getValue0();
		double d = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_D).getValue0();
		double e = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_E).getValue0();
		double f = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_F).getValue0();
		double g = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_G).getValue0();
		double h = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_H).getValue0();
		double i = quadraticSurface.getUnknownParameter(ParameterType.POLYNOMIAL_COEFFICIENT_I).getValue0();
		double length = quadraticSurface.getUnknownParameter(ParameterType.LENGTH).getValue0();
		
		DenseVector invUu = new DenseVector(new double[] {g, h, i}, false);
		DenseVector u     = new DenseVector(new double[] {g, h, i}, false);
		UpperSymmPackMatrix H = new UpperSymmPackMatrix(dim);
		H.set(0, 0, a);
		H.set(0, 1, d / INV_SQRT2);
		H.set(0, 2, e / INV_SQRT2);
		
		H.set(1, 1, b);
		H.set(1, 2, f / INV_SQRT2);
		
		H.set(2, 2, c);
		
		UpperSymmPackMatrix U = new UpperSymmPackMatrix(H);
		
		// estimate shift --> stored in u (U will be overwritten)
		MathExtension.solve(U, invUu, false);
		double x0 = -0.5 * invUu.get(0);
		double y0 = -0.5 * invUu.get(1);
		double z0 = -0.5 * invUu.get(2);
		
		// solving eigen-system
		SymmPackEVD evd = new SymmPackEVD(dim, true, true);
		evd.factor(H);

		Matrix evec = evd.getEigenvectors();
		double eval[] = evd.getEigenvalues();
		
		d = -0.25 * u.dot(invUu) + length; 
		
		// Halbachsen
		a = Math.sqrt(Math.abs(eval[0] / d));
		b = Math.sqrt(Math.abs(eval[1] / d));
		c = Math.sqrt(Math.abs(eval[2] / d));
		
		// transpose of rotation matrix
		double r11 = evec.get(0, 0);
		double r12 = evec.get(1, 0);
		double r13 = evec.get(2, 0);
		
		double r21 = evec.get(0, 1);
		double r22 = evec.get(1, 1);
		double r23 = evec.get(2, 1);
		
		double r31 = evec.get(0, 2);
		double r32 = evec.get(1, 2);
		double r33 = evec.get(2, 2);
		
		// check det(R) = +1
		double det = r11*r22*r33 + r12*r23*r31 + r13*r21*r32 - r13*r22*r31 - r12*r21*r33 - r11*r23*r32;
		if (det < 0) {
			r31 = -r31;
			r32 = -r32;
			r33 = -r33;
		}
		
		ellipsoid.setInitialGuess(x0, y0, z0, a, c, b, r11, r12, r13, r21, r22, r23, r31, r32, r33);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.ellipsoid.getFeaturePoints(), this.ellipsoid);
	}
}
