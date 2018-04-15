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

package org.applied_geodesy.jag3d.ui.graphic.layer;

public enum LayerType {	
	ABSOLUTE_CONFIDENCE(41),
	RELATIVE_CONFIDENCE(42),
	
	OBSERVATION_APRIORI(31),
	OBSERVATION_APOSTERIORI(32),
	
	REFERENCE_POINT_APRIORI(11),
	STOCHASTIC_POINT_APRIORI(12),
	DATUM_POINT_APRIORI(13),
	NEW_POINT_APRIORI(14),

	POINT_SHIFT(51),
	PRINCIPAL_COMPONENT_VERTICAL(52),
	PRINCIPAL_COMPONENT_HORIZONTAL(53),
	
	REFERENCE_POINT_APOSTERIORI(21),
	STOCHASTIC_POINT_APOSTERIORI(22),
	DATUM_POINT_APOSTERIORI(23),
	NEW_POINT_APOSTERIORI(24),
	
	;
	
	private int id;
	private LayerType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static LayerType getEnumByValue(int value) {
		for(LayerType element : LayerType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}	
