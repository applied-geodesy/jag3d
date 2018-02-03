package org.applied_geodesy.adjustment;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class NormalEquationSystem {
	private final UpperSymmPackMatrix N;
	private final DenseVector n;
	public NormalEquationSystem(UpperSymmPackMatrix N, DenseVector n) {
		this.N = N;
		this.n = n;
	}
	  
	/**
	 * Liefert die Normalgleichung 
	 * 
	 * N = A'*P*A  R'
	 *     R       0
	 * @return N
	 */
	public UpperSymmPackMatrix getMatrix() {
		return this.N;
	}
	 
	/**
	 * Liefert den n-Vektor
	 * 
	 * n = A'*P*w
	 *     r
	 * 
	 * @return n
	 */
	public DenseVector getVector() {
		return this.n;
	}
}
