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

public class HomologousFramePosition extends EstimatedFramePosition implements DispersionablePosition {
	private ObjectProperty<Matrix> dispersionApriori = new SimpleObjectProperty<Matrix>(this, "dispersionApriori");
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>(this, "redundancyX", 0.0);
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>(this, "redundancyY", 0.0);
	private ObjectProperty<Double> redundancyZ = new SimpleObjectProperty<Double>(this, "redundancyZ", 0.0);
		
	HomologousFramePosition(double z0) throws IllegalArgumentException {
		this(z0, MathExtension.identity(1));
	}
	
	HomologousFramePosition(double x0, double y0) throws IllegalArgumentException {
		this(x0, y0, MathExtension.identity(2));
	}
	
	HomologousFramePosition(double x0, double y0, double z0) throws IllegalArgumentException {
		this(x0, y0, z0, MathExtension.identity(3));
	}
	
	HomologousFramePosition(double z0, Matrix dispersion) throws IllegalArgumentException {
		super(z0);
		this.setDispersionApriori(dispersion);
	}

	HomologousFramePosition(double x0, double y0, Matrix dispersion) throws IllegalArgumentException {
		super(x0, y0);
		this.setDispersionApriori(dispersion);
	}

	HomologousFramePosition(double x0, double y0, double z0, Matrix dispersion) throws IllegalArgumentException {
		super(x0, y0, z0);
		this.setDispersionApriori(dispersion);
	}
	
	public double getRedundancyX() {
		return this.redundancyX.get();
	}
	
	public void setRedundancyX(double redundancyX) {
		this.redundancyX.set(redundancyX);
	}
	
	public ObservableObjectValue<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public double getRedundancyY() {
		return this.redundancyY.get();
	}
	
	public void setRedundancyY(double redundancyY) {
		this.redundancyY.set(redundancyY);
	}
	
	public ObservableObjectValue<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public double getRedundancyZ() {
		return this.redundancyZ.get();
	}
	
	public void setRedundancyZ(double redundancyZ) {
		this.redundancyZ.set(redundancyZ);
	}
	
	public ObservableObjectValue<Double> redundancyZProperty() {
		return this.redundancyZ;
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
		this.checkDispersionMatrix(dispersion);
		this.dispersionApriori.set(dispersion);
	}

	@Override
	public void reset() {
		super.reset();
		
		this.setRedundancyX(0);
		this.setRedundancyY(0);
		this.setRedundancyZ(0);
	}
}
