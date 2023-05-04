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

import javafx.beans.value.ObservableObjectValue;

public interface Positionable {

	public int getDimension();
	public ObservableObjectValue<Integer> dimensionProperty();
	
	public double getX();
	public double getY();
	public double getZ();
	
	public ObservableObjectValue<Double> xProperty();
	public ObservableObjectValue<Double> yProperty();
	public ObservableObjectValue<Double> zProperty();
	
	default public boolean equalsPosition(Positionable position) {
		if (this == position)
			return true;
		if (position == null)
			return false;
		if (this.getDimension() != position.getDimension())
			return false;
		if (this.getX() != position.getX())
			return false;
		if (this.getY() != position.getY())
			return false;
		if (this.getZ() != position.getZ())
			return false;
		return true;
	}
}
