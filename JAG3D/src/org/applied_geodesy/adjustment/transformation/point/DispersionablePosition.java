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
	
	default public void checkDispersionMatrix(Matrix dispersion) throws IllegalArgumentException {
		if (!dispersion.isSquare() || this.getDimension() != dispersion.numColumns())
			throw new IllegalArgumentException("Error, dispersion matrix must be a squared matrix of dimension " + this.getDimension() + " x " + this.getDimension() + "!");
			
		if (!(dispersion instanceof UpperSymmBandMatrix) && !(dispersion instanceof UnitUpperTriangBandMatrix) && !(dispersion instanceof UpperSymmPackMatrix))
			throw new IllegalArgumentException("Error, dispersion matrix must be of type UpperSymmBandMatrix, UnitUpperTriangBandMatrix, or UpperSymmPackMatrix!");
		
		
		if ((dispersion instanceof UpperSymmBandMatrix && ((UpperSymmBandMatrix)dispersion).numSuperDiagonals() != 0 ) || 
				(dispersion instanceof UnitUpperTriangBandMatrix) && ((UnitUpperTriangBandMatrix)dispersion).numSuperDiagonals() != 0)
			throw new IllegalArgumentException("Error, dispersion matrix must be a diagonal matrix, if BandMatrix type is used!");
	
		
		if (dispersion instanceof UpperSymmBandMatrix || dispersion instanceof UpperSymmPackMatrix) {
			for (int row = 0; row < dispersion.numRows(); row++) {
				double varI = dispersion.get(row, row);
				if (varI <= 0)
					throw new IllegalArgumentException("Error, element (" + row + ", " + row + ") on main diagonal of dispersion matrix is less than or equal to zero but must be greater than zero!");

				if (dispersion instanceof UpperSymmPackMatrix) {
					for (int col = row + 1; col < dispersion.numColumns(); col++) {
						double varJ    = dispersion.get(col, col);
						double covarIJ = dispersion.get(row, col);

						if (varJ <= 0)
							throw new IllegalArgumentException("Error, element (" + col + ", " + col + ": " + varJ + ") on main diagonal of dispersion matrix is less than or equal to zero but must be greater than zero!");

						if (Math.abs(covarIJ) >= Math.sqrt(varI * varJ))
							throw new IllegalArgumentException("Error, invalid dispersion matrix, as correlation coefficient of element (" + row + ", " + col + ") is not between -1 and 1!");
					}
				}
			}
		}
	}
}
