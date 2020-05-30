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

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class NormalEquationSystem {
	private final UpperSymmPackMatrix N;
	private final DenseVector n;
	private UpperSymmBandMatrix V;
	
	public NormalEquationSystem(UpperSymmPackMatrix N, DenseVector n) {
		this(N, n, null);
	}
	
	public NormalEquationSystem(UpperSymmPackMatrix N, DenseVector n, UpperSymmBandMatrix V) {
		this.N = N;
		this.n = n;
		this.V = V;
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
	
	public UpperSymmBandMatrix getPreconditioner() {
		return this.V;
	}
}
