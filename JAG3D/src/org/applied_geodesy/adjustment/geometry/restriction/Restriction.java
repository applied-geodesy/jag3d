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

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.Matrix;

public abstract class Restriction {
	private ObjectProperty<RestrictionType> restrictionType = new SimpleObjectProperty<RestrictionType>(this, "restrictionType");
	ObjectProperty<UnknownParameter> regressand = new SimpleObjectProperty<UnknownParameter>(this, "regressand");
	private ObjectProperty<String> description = new SimpleObjectProperty<String>(this, "description", null);
	private ReadOnlyObjectProperty<Boolean> indispensable;
	private int row = -1;
	
	Restriction(RestrictionType restrictionType, boolean indispensable) {
		this.setRestrictionType(restrictionType);
		this.indispensable = new ReadOnlyObjectWrapper<Boolean>(this, "indispensable", indispensable);
	}
	
	/**
	 * @deprecated
	 * @param restrictionType
	 * @param indispensable
	 * @param regressand
	 */
	Restriction(RestrictionType restrictionType, boolean indispensable, UnknownParameter regressand) {
		this.setRestrictionType(restrictionType);
		this.setRegressand(regressand);
		this.indispensable = new ReadOnlyObjectWrapper<Boolean>(this, "indispensable", indispensable);
	}
	
	public ReadOnlyObjectProperty<Boolean> indispensableProperty() {
		return this.indispensable;
	}
	
	public boolean isIndispensable() {
		return this.indispensable.get();
	}
	
	public void setRestrictionType(RestrictionType restrictionType) {
		this.restrictionType.set(restrictionType);
	}
	
	public RestrictionType getRestrictionType() {
		return this.restrictionType.get();
	}
	
	public ObjectProperty<RestrictionType> restrictionTypeProperty() {
		return this.restrictionType;
	}
	
	public void setRow(int row) {
		this.row = row;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public UnknownParameter getRegressand() {
		return this.regressand.get();
	}
	
	public void setRegressand(UnknownParameter regressand) {
		this.regressand.set(regressand);
	}
	
	public ObjectProperty<UnknownParameter> regressandProperty() {
		return this.regressand;
	}
	
	public void setDescription(String description) {
		this.description.set(description);
	}
	
	public String getDescription() {
		return this.description.get();
	}
	
	public ObjectProperty<String> descriptionProperty() {
		return this.description;
	}
	
	public abstract String toLaTex();

	public abstract double getMisclosure();

	public abstract void transposedJacobianElements(Matrix JrT);
	
	public abstract boolean contains(Object object);
}
