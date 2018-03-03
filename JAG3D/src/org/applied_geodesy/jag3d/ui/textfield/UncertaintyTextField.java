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

public class UncertaintyTextField extends DoubleTextField {
	
	public UncertaintyTextField(Double value, CellValueType type) {
		this(value, type, false);
	}

	public UncertaintyTextField(Double value, CellValueType type, boolean displayUnit) {
		this(value, type, displayUnit, ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO);
	}
	
	public UncertaintyTextField(Double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport) {
		super(value, type, displayUnit, valueSupport);
		
		if (valueSupport != ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO && valueSupport != ValueSupport.GREATER_THAN_ZERO)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, uncertainty value must be greater than or equal to zeros, or greater than zero " + valueSupport);
	}
}

