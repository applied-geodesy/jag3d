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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UnitUpperTriangBandMatrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class ObservedFramePosition extends Position implements DispersionablePosition {
	private ObjectProperty<Matrix> dispersionApriori = new SimpleObjectProperty<Matrix>(this, "dispersionApriori");
	
	ObservedFramePosition(double z0) throws IllegalArgumentException {
		this(z0, MathExtension.identity(1));
	}
	
	ObservedFramePosition(double z0, Matrix dispersion) throws IllegalArgumentException {
		super(z0);
		this.setDispersionApriori(dispersion);
	}

	ObservedFramePosition(double x0, double y0) throws IllegalArgumentException {
		this(x0, y0, MathExtension.identity(2));
	}
	
	ObservedFramePosition(double x0, double y0, Matrix dispersion) throws IllegalArgumentException {
		super(x0, y0);
		this.setDispersionApriori(dispersion);
	}
	
	ObservedFramePosition(double x0, double y0, double z0) throws IllegalArgumentException {
		this(x0, y0, z0, MathExtension.identity(3));
	}
	
	ObservedFramePosition(double x0, double y0, double z0, Matrix dispersion) throws IllegalArgumentException {
		super(x0, y0, z0);
		this.setDispersionApriori(dispersion);
	}
	
	@Override
	public ObservableObjectValue<Matrix> dispersionAprioriProperty() {
		return this.dispersionApriori;
	}
	
	@Override
	public Matrix getDispersionApriori() {
		return this.dispersionApriori.get();
	}
	
	@Override
	public void setDispersionApriori(Matrix dispersion) throws IllegalArgumentException {
		if (!dispersion.isSquare() || this.getDimension() != dispersion.numColumns())
			throw new IllegalArgumentException("Error, dispersion matrix must be a squared matrix of dimension " + this.getDimension() + " x " + this.getDimension() + "!");
			
		if (!(dispersion instanceof UpperSymmBandMatrix) && !(dispersion instanceof UnitUpperTriangBandMatrix) && !(dispersion instanceof UpperSymmPackMatrix))
			throw new IllegalArgumentException("Error, dispersion matrix must be of type UpperSymmBandMatrix, UnitUpperTriangBandMatrix, or UpperSymmPackMatrix!");
		
		
		if ((dispersion instanceof UpperSymmBandMatrix && ((UpperSymmBandMatrix)dispersion).numSuperDiagonals() != 0 ) || 
				(dispersion instanceof UnitUpperTriangBandMatrix) && ((UnitUpperTriangBandMatrix)dispersion).numSuperDiagonals() != 0)
			throw new IllegalArgumentException("Error, dispersion matrix must be a diagonal matrix, if BandMatrix type is used!");

		this.dispersionApriori.set(dispersion);
	}
}
