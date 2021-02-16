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

package org.applied_geodesy.jag3d.ui.table.column;

public enum TableContentType {
	UNSPECIFIC(0),
	
	LEVELING(1),
	DIRECTION(2),
	HORIZONTAL_DISTANCE(3),
	SLOPE_DISTANCE(4),
	ZENITH_ANGLE(5),

	GNSS_1D(6),
	GNSS_2D(7),
	GNSS_3D(8),

	REFERENCE_POINT_1D(10),
	REFERENCE_POINT_2D(11),
	REFERENCE_POINT_3D(12),
	
	STOCHASTIC_POINT_1D(20),
	STOCHASTIC_POINT_2D(21),
	STOCHASTIC_POINT_3D(22),
	
	DATUM_POINT_1D(30),
	DATUM_POINT_2D(31),
	DATUM_POINT_3D(32),
	
	NEW_POINT_1D(40),
	NEW_POINT_2D(41),
	NEW_POINT_3D(42),

	CONGRUENCE_ANALYSIS_1D(50),
	CONGRUENCE_ANALYSIS_2D(51),
	CONGRUENCE_ANALYSIS_3D(52),
	
	REFERENCE_DEFLECTION(60),
	STOCHASTIC_DEFLECTION(61),
	UNKNOWN_DEFLECTION(62),
	
	ADDITIONAL_PARAMETER(70),
	
	SELECTED_GROUP_VARIANCE_COMPONENTS(80),

	;

	private int id;
	private TableContentType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static TableContentType getEnumByValue(int value) {
		for(TableContentType element : TableContentType.values()) {
			if(element.id == value)
				return element;
		}
		return UNSPECIFIC;
	}  
}
