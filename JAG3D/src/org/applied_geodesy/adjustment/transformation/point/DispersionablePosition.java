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

package org.applied_geodesy.adjustment.transformation.point;

import org.applied_geodesy.adjustment.MathExtension;

import javafx.beans.value.ObservableObjectValue;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.UnitUpperTriangBandMatrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public interface DispersionablePosition extends Positionable {

	public ObservableObjectValue<Matrix> dispersionAprioriProperty();
	public Matrix getDispersionApriori();
	public void setDispersionApriori(Matrix dispersion) throws IllegalArgumentException;
	
	default public Matrix getInvertedDispersion(boolean inplace) throws MatrixSingularException, IllegalArgumentException {
		Matrix dispersionApriori = this.getDispersionApriori();
		int size = dispersionApriori.numColumns();

		if (dispersionApriori instanceof UnitUpperTriangBandMatrix) 
			return dispersionApriori; //inplace ? dispersionApriori : new UnitUpperTriangBandMatrix(size, 0);

		else if (dispersionApriori instanceof UpperSymmBandMatrix) {
			Matrix W = inplace ? dispersionApriori : new UpperSymmBandMatrix(size, 0);
			for (MatrixEntry entry : dispersionApriori) {
				double value = entry.get();
				if (value <= 0)
					throw new MatrixSingularException("Error, matrix is a singular matrix!");
				W.set(entry.row(), entry.column(), 1.0 / value);
			} 
			return W;
		}
		else if (dispersionApriori instanceof UpperSymmPackMatrix) {
			UpperSymmPackMatrix W = inplace ? (UpperSymmPackMatrix)dispersionApriori : new UpperSymmPackMatrix(dispersionApriori, true);
			MathExtension.inv(W);
			return W;
		}
		
		throw new IllegalArgumentException("Error, dispersion matrix must be of type UpperSymmBandMatrix, UnitUpperTriangBandMatrix, or UpperSymmPackMatrix!");		 
	}
}
