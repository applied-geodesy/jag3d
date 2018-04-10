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

import org.netlib.util.intW;

import com.github.fommil.netlib.LAPACK;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.UnitUpperTriangBandMatrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;

public final class MathExtension {
	/**
	 * Liefert in Abhaengigkeit vom Vorzeichen von b den Wert a positiv oder negativ.
	 * Das Vorzeichen von a wird ignoriert.
	 * @param a
	 * @param b
	 * @return sign
	 */
	public static final double SIGN (double a, double b) {
		return b >= 0.0 ? Math.abs(a) : -Math.abs(a);
	}

	/**
	 * Winkelreduktion auf ein pos. Intervall
	 * Vergleich zur modularen Operation:
	 * <pre>-50%400 == -50</pre>
	 * <pre>mod(-50,400) == 350</pre>
	 *  
	 * @param x
	 * @param y
	 * @return mod
	 */
	public static final double MOD(double x, double y){
		return x - Math.floor( x / y ) * y;
	}

	/**
	 * Liefert die Kondition einer Matrix c = cond(M) 
	 * mithilfe von SVD
	 * 
	 * [u v w] = svd(M)
	 * c = max(v)/min(v)
	 * 
	 * @param M
	 * @return c = cond(M)
	 * @throws NotConvergedException
	 */
	public static double cond(Matrix M) throws NotConvergedException {
		SVD uwv = SVD.factorize(M);
		double[] s = uwv.getS();
		// vgl. http://www.mathworks.de/help/techdoc/ref/cond.html
		int m = Math.min(M.numColumns(), M.numRows())-1;
		if (s[m]!=0)
			return s[0]/s[m];
		return 0;
	}	

	/**
	 * Liefert die Pseudoinverse Q = M<sup>+1</sup> 
	 * der Matrix M mithilfe von SVD
	 * 
	 * [u v w] = svd(M)
	 * Q = v*w<sup>-1</sup>*u<sup>T</sup> 
	 * 
	 * @param M
	 * @return Q = M<sup>+1</sup>
	 * @throws NotConvergedException
	 */
	public static Matrix pinv(Matrix M) throws NotConvergedException {
		return MathExtension.pinv(M, 0.0);
	}

	/**
	 * Liefert die Pseudoinverse Q = M<sup>+1</sup> 
	 * der Matrix M mithilfe von SVD
	 * 
	 * [u v w] = svd(M)
	 * Q = v*w<sup>-1</sup>*u<sup>T</sup> 
	 * 
	 * @param M
	 * @param tol
	 * @return Q = M<sup>+1</sup>
	 * @throws NotConvergedException
	 */
	public static Matrix pinv(Matrix M, double tol) throws NotConvergedException {
		SVD uwv = SVD.factorize(M);

		Matrix U  = uwv.getU();
		Matrix VT = uwv.getVt();
		double[] s = uwv.getS();
		//Matrix W = new DenseMatrix(VT.numColumns(), U.numColumns());
		Matrix W = new CompDiagMatrix(VT.numColumns(), U.numColumns());
		// Bestimme Toleranz neu
		// vgl. http://www.mathworks.de/help/techdoc/ref/pinv.html
		if (tol < Constant.EPS) {
			double norm2 = 0.0;
			for (int i=0; i<s.length; i++) 
				norm2 = Math.max(norm2, Math.abs(s[i]));

			tol = Math.max(M.numColumns(), M.numRows()) * norm2 * (tol < 0 ? Math.sqrt(Constant.EPS) : Constant.EPS);
		}

		for (int i=0; i<s.length; i++)
			if (Math.abs(s[i]) > tol)
				W.set(i,i, 1.0/s[i]);
		s = null;

		Matrix VW = new DenseMatrix(VT.numRows(), W.numColumns());
		VT.transAmult(W, VW);
		W = null;
		VT = null;

		Matrix Q = new DenseMatrix(M.numColumns(), M.numRows());
		VW.transBmult(U, Q);

		return Q;
	}

	/**
	 * Liefert die Pseudoinverse Q = M<sup>+1</sup> 
	 * der Matrix M mithilfe von SVD, wobei der Defekt
	 * der Matrix durch rank bereits vorgegeben ist.
	 * Es werden nur die ersten n Singulaerwerte beruecksichtigt.
	 * 
	 * [u v w] = svd(M)
	 * Q = v*w<sup>-1</sup>*u<sup>T</sup> 
	 * 
	 * @param M
	 * @param rank
	 * @return Q = M<sup>+1</sup>
	 * @throws NotConvergedException
	 */
	public static Matrix pinv(Matrix M, int rank) throws NotConvergedException {
		if (rank <= 0)
			return MathExtension.pinv(M, (double)rank);

		SVD uwv = SVD.factorize(M);

		Matrix U  = uwv.getU();
		Matrix VT = uwv.getVt();
		double[] s = uwv.getS();
		Matrix W = new CompDiagMatrix(VT.numColumns(), U.numColumns());
		//Matrix W = new DenseMatrix(VT.numColumns(), U.numColumns());
		// Korrigiere das Intervall auf 0 und Anz. Singul.werte
		rank = Math.max(0, Math.min(rank, s.length));

		for (int i=0; i<rank; i++)
			if (Math.abs(s[i]) > 0)
				W.set(i,i, 1.0/s[i]);
		s = null;

		Matrix VW = new DenseMatrix(VT.numRows(), W.numColumns());
		VT.transAmult(W, VW);
		W = null;
		VT = null;

		Matrix Q = new DenseMatrix(M.numColumns(), M.numRows());
		VW.transBmult(U, Q);

		return Q;
	}

	/**
	 * Liefert eine quadratische Einheitsmatrix der Dimension <code>size</code>
	 * @param size
	 * @return I
	 */
	public static Matrix identity(int size) {
		return new UnitUpperTriangBandMatrix(size,0);
	}

	/**
	 * Loest das Gleichungssystem <code>N * x = n</code>. Der Vektor n wird hierbei mit dem Loesungsvektor <code>x</code> ueberschrieben. 
	 * Wenn <code>invert = true</code>, dann wird <code>N</code> mit dessen Inverse ueberschrieben.
	 * 
	 * @param N
	 * @param n
	 * @param invert
	 * @throws MatrixSingularException
	 * @throws IllegalArgumentException
	 */
	public static void solve(UpperSymmPackMatrix N, DenseVector n, boolean invert) throws MatrixSingularException, IllegalArgumentException {
		final String UPLO = "U";
		DenseMatrix nd = new DenseMatrix(n, false);
		double[] na = nd.getData();
		double Nd[] = N.getData();
		int numRows = N.numRows();
		int[] ipiv = new int[numRows];

		intW info = new intW(0);
		
		// http://www.netlib.org/lapack/double/dspsv.f
		LAPACK.getInstance().dspsv(UPLO, numRows, nd.numColumns(), Nd, ipiv, na, Math.max(1, numRows), info);

		if (info.val > 0)
			throw new MatrixSingularException();
		else if (info.val < 0)
			throw new IllegalArgumentException();

		if (invert) {
			double work[] = new double[numRows];

			// http://www.netlib.org/lapack/double/dsptri.f
			LAPACK.getInstance().dsptri(UPLO, numRows, Nd, ipiv, work, info);

			if (info.val > 0)
				throw new MatrixSingularException();
			else if (info.val < 0)
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Liefert die Inverse einer symmetrischen oberen Dreiecksmatrix mittels <code>N = LDL'</code> Zerlegung. <code>N</code> wird hierbei ueberschrieben.
	 * 
	 * @param  N Matrix
	 * @throws MatrixSingularException
	 * @throws IllegalArgumentException
	 */
	public static void inv(UpperSymmPackMatrix N) throws MatrixSingularException, IllegalArgumentException {
		final String UPLO = "U";
		int numRows = N.numRows();
		int[] ipiv = new int[numRows];
		intW info = new intW(0);
		double qd[] = N.getData();

		// http://www.netlib.org/lapack/double/dsptrf.f
		LAPACK.getInstance().dsptrf(UPLO, numRows, qd, ipiv, info);

		if (info.val > 0)
			throw new MatrixSingularException();
		else if (info.val < 0)
			throw new IllegalArgumentException();

		double work[] = new double[numRows];

		// http://www.netlib.org/lapack/double/dsptri.f
		LAPACK.getInstance().dsptri(UPLO, numRows, qd, ipiv, work, info);

		if (info.val > 0)
			throw new MatrixSingularException();
		else if (info.val < 0)
			throw new IllegalArgumentException();
	}
	
	/**
	 * Bestimmt ausgewaehlte Eigenwerte einer symmetrischen oberen Dreiecksmatrix <code>N</code>. Die Indizes der zu bestimmeden
	 * Eigenwerte ergeben sich aus dem Intervall <code>il <= i <= iu</code>, mit <code>il >= 1</code> und <code>ul <= n</code>.
	 * Sie werden in aufsteigender Reihenfolge ermittelt. Ist die Flag <code>vectors = true</code> gesetzt, werden die zugehoerigen
	 * Eigenvektoren mitbestimmt. Durch die Flag <code>n</code> kann die Eigenwert/-vektorbestimmung auf die ersten <code>n</code>-Elemente
	 * begrenzt werden.
	 * 
	 * Die Eigenwerte <code>eval</code> werden als UpperSymmBandMatrix gespeichert, die Eigenvektoren <code>evec</code> in einer DenseMatrix.
	 * 
	 * HINWEIS: Die Matrix <code>N</code> wird bei dieser Zerlegung ueberschrieben!!!
	 * 
	 * @param N
	 * @param n
	 * @param il
	 * @param iu
	 * @param vectors
	 * @return {eval, evec}
	 * @throws NotConvergedException
	 * @throws IllegalArgumentException
	 */
	public static Matrix[] eig(UpperSymmPackMatrix N, int n, int il, int iu, boolean vectors) throws NotConvergedException, IllegalArgumentException {
		n = n < 0 ? N.numRows() : n;
		if (il < 1)
			throw new IllegalArgumentException("Fehler, unterer Eigenwertindex muss il >= 1: il = " + il);
		if (iu > n)
			throw new IllegalArgumentException("Fehler, oberer Eigenwertindex muss iu > n: iu = " + iu + ", n = " + n);
		
		final String jobz  = vectors ? "V" : "N";
		final String range = "I";
		final String uplo  = "U";
        
        double ap[] = N.getData();
        double vl = 0;
        double vu = 0;
        double abstol = 2.0 * LAPACK.getInstance().dlamch("S");
        intW m = new intW(0);
        double evalArray[] = new double[n]; // n because of multiple roots
        //DenseMatrix evec = vectors ? new DenseMatrix(iu-il + 1, n) : new DenseMatrix(0, 0);
        DenseMatrix evec = vectors ? new DenseMatrix(n, iu-il + 1) : new DenseMatrix(0, 0);
        int ldz = Math.max(1,n);
        double work[] = new double[8*n];
        int iwork[] = new int[5*n];
        int ifail[] = vectors ? new int[n] : new int[0];
        intW info = new intW(0);
        
        if (il <= 0 || il > iu && n > 0 || iu > n)
        	throw new IllegalArgumentException();

		// http://www.netlib.org/lapack/double/dspevx.f
		//LAPACK.getInstance().dspevx(jobz, range, uplo, n, ap, vl, vu, il, iu, abstol, m, eval.getData(), evec.getData(), ldz, work, iwork, ifail, info);
        LAPACK.getInstance().dspevx(jobz, range, uplo, n, ap, vl, vu, il, iu, abstol, m, evalArray, evec.getData(), ldz, work, iwork, ifail, info);
		
		if (info.val > 0)
            throw new NotConvergedException(NotConvergedException.Reason.Breakdown);
        else if (info.val < 0)
            throw new IllegalArgumentException("Fehler, Eingangsargumente fehlerhaft!");
		
		work  = null;
		iwork = null;
		ifail = null;
		
		UpperSymmBandMatrix eval = new UpperSymmBandMatrix(iu-il + 1, 0);
		System.arraycopy(evalArray, 0, eval.getData(), 0, iu-il + 1);
		
		return new Matrix[] {
				eval, evec
		};
	}

	/** 
	 * Druckt eine Matrix auf der Konsole aus
	 * @param M
	 */
	public static void print(Matrix M) {
		for (int i=0; i<M.numRows(); i++) {
			for (int j=0; j<M.numColumns(); j++) {
				System.out.print(M.get(i,j)+"  ");
			}
			System.out.println();
		}
	}

	/**
	 * Druckt einen Vektor auf der Konsole aus
	 * @param v
	 */
	public static void print(Vector v) {
		for (int i=0; i<v.size(); i++) {
			System.out.println(v.get(i));
		}
	}

	/**
	 * Liefert das Kreuzprodukt zweier 3x1-Vektoren
	 * @param a
	 * @param b
	 * @return c
	 * @throws IllegalArgumentException Wenn die Anzahl der Elemente in a oder/und b ungleich 3
	 */
	public static DenseVector cross(Vector a, Vector b) throws IllegalArgumentException {
		if (a.size() != 3 || b.size() != 3)
			throw new IllegalArgumentException("Fehler, Kreuzprodukt nur fuer 3x1-Vektoren definiert. "+ a.size() +" und " + b.size());
		DenseVector c = new DenseVector(3);
		c.set(0, a.get(1)*b.get(2) - a.get(2)*b.get(1));
		c.set(1, a.get(2)*b.get(0) - a.get(0)*b.get(2));
		c.set(2, a.get(0)*b.get(1) - a.get(1)*b.get(0));
		return c;
	}

}