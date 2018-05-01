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

package org.applied_geodesy.jag3d.ui.textfield;

import org.applied_geodesy.jag3d.ui.table.CellValueType;

public class MinMaxDoubleTextField extends DoubleTextField {
	private double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
	private boolean exclusiveMin = true, exclusiveMax = true;
	
	public MinMaxDoubleTextField(Double value, double min, double max, CellValueType type, boolean displayUnit, boolean exclusiveMin, boolean exclusiveMax) {
		super(value, type, displayUnit, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.exclusiveMin = exclusiveMin;
		this.exclusiveMax = exclusiveMax;
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		this.setNumber(value);
		this.setText(this.getRendererFormat(value));
	}
	
	@Override
	public boolean check(Double value) {
		boolean validNumber = super.check(value);

		if (!validNumber)
			return false;
		
		if (value == null)
			return true;
		
		return (this.exclusiveMin ? this.min < value : this.min <= value) && (this.exclusiveMax ? value < this.max : value <= this.max);
	}
}
