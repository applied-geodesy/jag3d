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

package org.applied_geodesy.adjustment;

import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.Vector;

public class ConfidenceRegion {
	private final Matrix covarianceMatrix; 
	private final int dimension;
	private double eigenvalues[], minimalDetectableBias[];
	private Matrix eigenvectors;
	private final int sortOrder[];
	private double helmertEllipseAxes[] = new double[2];
	private double helmertEllipseAngle = 0;
	private TestStatisticParameters testStatisticParameters;

	public ConfidenceRegion(TestStatisticParameters testStatisticParameters, Matrix covarianceMatrix) throws IllegalArgumentException, NotConvergedException {
		this(covarianceMatrix);
		this.testStatisticParameters = testStatisticParameters;
	}
	
	public ConfidenceRegion(Matrix covarianceMatrix) throws IllegalArgumentException, NotConvergedException {
		if (!covarianceMatrix.isSquare())
			throw new IllegalArgumentException(this.getClass() + " Matrix must be a squared matrix!");
		
		for (int i=0; i<covarianceMatrix.numColumns(); i++) {
			for (int j=0; j<covarianceMatrix.numRows(); j++) {
				if (Double.isInfinite(covarianceMatrix.get(i,j)) || Double.isNaN(covarianceMatrix.get(i,j)) ) {
					throw new IllegalArgumentException(this.getClass()+" Matrix contains Inf or NaN Values! " +i+"x"+j+" "+covarianceMatrix.get(i,j));
				}
			}
		}
		
		this.covarianceMatrix   = covarianceMatrix;
		this.dimension     = covarianceMatrix.numColumns();
		
		this.SVD();
		this.sortOrder = this.getSortOrder();
		
		this.calculateHelmertEllipse();
	}
		
	/**
	 * Liefert die Halbachse der Ellipse/des Ellispoids
	 * @param index
	 * @return axis
	 */
	public double getConfidenceAxis(int index) {
		return Math.sqrt( this.getEigenvalue(index) * this.dimension * this.getQuantile(this.dimension) );
	}
	
	/**
	 * Bestimmt die Euler-Winkel aus den Eigenvektoren fuer die Sequenz R = Rx*Ry*Rz
	 * 
	 * Rx = [1    0        0
	 *       0 cos(rx) -sin(rx)
	 *       0 sin(rx)  cos(rx)]
	 * 
	 * Ry = [cos(ry) 0 sin(ry)
	 *         0     1    0
	 *      -sin(ry) 0 cos(ry)];
	 * 
	 * Rz = [cos(rz) -sin(rz) 0
	 *       sin(rz)  cos(rz) 0 
	 *         0          0   1];
	 * 
	 * 
	 * 
	 * @return [rx, ry, rz]
	 */
	public double[] getEulerAngles() {
		if (this.dimension == 1)
			return new double[] {0, 0, 0};
		
		double rx, ry, rz, r11, r12, r13, r23, r33, r21, r22, r31, r32;
		rx = ry = rz = r11 = r12 = r13 = r23 = r33 = r21 = r22 = r31 = r32 = 0;

		r11 = this.eigenvectors.get(0, this.sortOrder[0]);
		r12 = this.eigenvectors.get(0, this.sortOrder[1]);
		
		r21 = this.eigenvectors.get(1, this.sortOrder[0]);
		r22 = this.eigenvectors.get(1, this.sortOrder[1]);
		
		if (this.dimension == 2) {
			double det2 = r11*r22 - r12*r21;
			if (det2 < 0) {
				r11 = -r11;
				r21 = -r21;
			}
			rz = MathExtension.MOD( Math.atan2(-r12, r11), 2.0*Math.PI );
		}
		else if (this.dimension == 3) {
			r13 = this.eigenvectors.get(0, this.sortOrder[2]);
			r23 = this.eigenvectors.get(1, this.sortOrder[2]);
			
			r31 = this.eigenvectors.get(2, this.sortOrder[0]);
			r32 = this.eigenvectors.get(2, this.sortOrder[1]);
			r33 = this.eigenvectors.get(2, this.sortOrder[2]);
						
			double det3 = r11*r22*r33 + r12*r23*r31 + r13*r21*r32 - r13*r22*r31 - r12*r21*r33 - r11*r23*r32;
			
			if (det3 < 0) {
				r11 = -r11;
				r21 = -r21;
				r31 = -r31;
			}
			
			rx = MathExtension.MOD( Math.atan2(-r23, r33), 2.0*Math.PI );
			ry = MathExtension.MOD( Math.atan2(r13, Math.hypot(r23, r33)), 2.0*Math.PI );
			rz = MathExtension.MOD( Math.atan2(-r12, r11), 2.0*Math.PI );
		}
		
		return new double[] {rx, ry, rz};
	}
	
	/**
	 * Liefert den Eigenwert, Eigenwerte sind der Groesse nach geordnet
	 * @param index
	 * @return eigenvalue
	 */
	public double getEigenvalue(int index) {
		return this.eigenvalues[this.sortOrder[index]];
	}
	
	/**
	 * Liefert die Eigenvektoren, sind in Abhaengigkeit der Eigenwerte der Groesse nach geordnet
	 * @param index
	 * @return eigenvector
	 */
	public Vector getEigenVector(int index) {
		Vector eigenvector = new DenseVector(this.dimension);
		for (int i=0; i<this.dimension; i++) {
			eigenvector.set(i, this.eigenvectors.get(i, this.sortOrder[index]));
		}
		return eigenvector;
	}
	
	/**
	 * Liefert die Parameter der Grenzwertellipse/-elliposid.
	 * 
	 * R == Matrix der normierten Eigenvektoren
	 * v == Vektor der zugehoerigen Eigenwerte
	 * 
	 * m = Rv
	 * 
	 * @param index
	 * @return m
	 */
	public double getMinimalDetectableBias(int index) {
		return this.minimalDetectableBias[index];
	}
	
	private void SVD() throws NotConvergedException {
		SVD uwv = SVD.factorize(this.covarianceMatrix);
		this.eigenvectors = uwv.getU();
		this.eigenvalues  = uwv.getS();
 
		// Bestimme den maximalen Eigenwert und den zugehoerigen Index
		double maxEval = 0;
		int indexMaxEval = 0;
		
		for (int i=0; i<this.dimension; i++) {
			double eval = Math.abs(this.eigenvalues[i]);
			if (eval > maxEval) {
				maxEval = eval;
				indexMaxEval = i;
			}
		}

//		// erzeuge einen Punkt auf der Ellipse in Normallage in max. Richtung
//		DenseVector maxEvalVecNormal = new DenseVector(this.dimension);
//		maxEvalVecNormal.set(indexMaxEval, Math.sqrt(maxEval));
//		
//		// rotiere Punkt auf tatsaechliche Lage der Ellipse
//		DenseVector maxEvalVecRot = new DenseVector(this.dimension);
//		this.eigenvectors.mult(maxEvalVecNormal, maxEvalVecRot);		
//		this.minimalDetectableBias = Matrices.getArray(maxEvalVecRot);

		// Spalten == Eigenvektoren
		this.minimalDetectableBias = new double[this.dimension];
		for (int i=0; i<this.dimension; i++) {
			minimalDetectableBias[i] = Math.sqrt(maxEval) * this.eigenvectors.get(i, indexMaxEval);
		}
	}	
	
	/**
	 * Ermittelt die Sortierreihenfolge der Eigenwerte und speichert die Indizes in einem Array 
	 * @return order
	 */
	private final int[] getSortOrder() {
		int order[] = new int[this.dimension];
		if (this.dimension == 2) {
			if (this.eigenvalues[0] > this.eigenvalues[1]) {
				order[0] = 0;
				order[1] = 1;
			}
			else {
				order[0] = 1;
				order[1] = 0;
			}
		}
		if (this.dimension == 3) {
			double maxValue = Math.max(this.eigenvalues[0], Math.max(this.eigenvalues[1], this.eigenvalues[2]));
			double minValue = Math.min(this.eigenvalues[0], Math.min(this.eigenvalues[1], this.eigenvalues[2]));
			for (int i=0; i<this.dimension; i++) {
				if (this.eigenvalues[i] == maxValue) {
					order[0] = i;
				}
				else if (this.eigenvalues[i] == minValue) {
					order[2] = i;
				}
				else {
					order[1] = i;
				}
			}
		}
		return order;
	}
	
	/**
	 * Liefert die Halbachse der 2D-Konfidenzellipse
	 * @param index
	 * @param isHelmertEllipse
	 * @return axis
	 */
	public double getConfidenceAxis2D(int index, boolean isHelmertEllipse) {
		return this.helmertEllipseAxes[index] * (isHelmertEllipse ? 1.0 : Math.sqrt((double)this.dimension * this.getQuantile(this.dimension)));
	}
	
	/**
	 * Liefert dem Drehwinkel der 2D-Konfidenzellipse
	 * @return angle
	 */
	public double getConfidenceAngle2D() {
		return this.helmertEllipseAngle;
	}
	
	/**
	 * Berechnet die einfache Konfidenzellipse nach HELMERT
	 */
	private void calculateHelmertEllipse() {
		if (this.dimension > 1) {
			double qxx = this.covarianceMatrix.get(0, 0);
			double qyy = this.covarianceMatrix.get(1, 1);
			double qxy = this.covarianceMatrix.get(0, 1);
			double w = Math.sqrt( (qxx-qyy)*(qxx-qyy) + 4*qxy*qxy );
			double a = Math.sqrt(0.5*(qxx+qyy+w));
			double b = Math.sqrt(0.5*(qxx+qyy-w));
			this.helmertEllipseAxes[0] = a;
			this.helmertEllipseAxes[1] = b;
			this.helmertEllipseAngle = MathExtension.MOD(0.5*Math.atan2(2.0*qxy, qxx-qyy), 2.0*Math.PI);
		}
		else { // 1D-Fall
			this.helmertEllipseAxes[0] = Math.sqrt(this.covarianceMatrix.get(0, 0));
			this.helmertEllipseAxes[1] = 0.0;
			this.helmertEllipseAngle = 0.0;
		}
	}
	
	/**
	 * Liefert das Quantil in Abhaengigkeit der Dimension
	 * @param dimension
	 * @return quantil
	 */
	private double getQuantile(int dimension) {
		if (this.testStatisticParameters != null)
			return this.testStatisticParameters.getTestStatisticParameter(dimension, Double.POSITIVE_INFINITY).getQuantile();		
		return 1.0;
	}
}
