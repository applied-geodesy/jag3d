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

package org.applied_geodesy.adjustment.geometry.restriction;

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.uib.cipr.matrix.Matrix;

public class AverageRestriction extends Restriction {
	private ObservableList<UnknownParameter> regressors = FXCollections.<UnknownParameter>observableArrayList();
	
	public AverageRestriction() {
		this(false, new ArrayList<UnknownParameter>(0), null);
	}
	
	/**
	 * (A1 + A2 + ... + An) / n ==  C
	 */
	public AverageRestriction(boolean indispensable, List<UnknownParameter> regressors, UnknownParameter regressand) {
		super(RestrictionType.AVERAGE, indispensable);
		this.setRegressand(regressand);
		this.regressors.setAll(regressors);
	}
	
	public ObservableList<UnknownParameter> getRegressors() {
		return this.regressors;
	}

	@Override
	public double getMisclosure() {
		double sum = 0;
		double length = this.regressors.size();
		for (int i = 0; i < length; i++)
			sum += this.regressors.get(i).getValue();

		return sum / length - this.regressand.get().getValue();
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		int rowIndex = this.getRow();
		double length = this.regressors.size();
		
		// inner part
		for (int i = 0; i < length; i++) {
			// (A1 + A2 + ... + An) / n  ==  C
			if (this.regressors.get(i).getColumn() >= 0) 
				JrT.add(this.regressors.get(i).getColumn(), rowIndex, 1.0 / length);
		}

		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
	}

	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.regressors.contains(object);
	}
	
	@Override
	public String toLaTex() {
		return "$\\frac{1}{n} \\left(a_1 + a_i + \\cdots + a_n\\right) = c$";
	}
}
