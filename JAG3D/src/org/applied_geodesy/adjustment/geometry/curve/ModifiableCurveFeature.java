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

package org.applied_geodesy.adjustment.geometry.curve;

import org.applied_geodesy.adjustment.geometry.CurveFeature;

import javafx.beans.property.ObjectProperty;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class ModifiableCurveFeature extends CurveFeature {

	public ModifiableCurveFeature() {
		super(false);
	}

	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Error, a modifiable feature does not provide a method for deriving an initial guess!");
	}
	
	@Override
	public ObjectProperty<Boolean> estimateInitialGuessProperty() {
		throw new UnsupportedOperationException("Error, a modifiable feature does not provide a method for deriving an initial guess!");
	}
	
	@Override
	public void setEstimateInitialGuess(boolean estimateInitialGuess) {
		throw new UnsupportedOperationException("Error, a modifiable feature does not provide a method for deriving an initial guess!");
	}
	
	@Override
	public boolean isEstimateInitialGuess() {
		return Boolean.FALSE;
	}
	
	@Override
	public ObjectProperty<Boolean> estimateCenterOfMassProperty() {
		throw new UnsupportedOperationException("Error, a modifiable feature does not provide center of mass reduction!");
	}
	
	@Override
	public void setEstimateCenterOfMass(boolean estimateCenterOfMass) {
		throw new UnsupportedOperationException("Error, a modifiable feature does not provide center of mass reduction!");
	}
	
	@Override
	public boolean isEstimateCenterOfMass() {
		return Boolean.FALSE;
	}
}
