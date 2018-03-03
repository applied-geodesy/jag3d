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

package org.applied_geodesy.adjustment.network.observation.projection;

public enum ProjectionType {
	NONE(0),
	DIRECTION_REDUCTION(2),
	HEIGHT_REDUCTION(3),
	GAUSS_KRUEGER_REDUCTION(4),
	UTM_REDUCTION(5),
	DIRECTION_HEIGHT_REDUCTION(23),
	DIRECTION_GK_REDUCTION(24),
	DIRECTION_UTM_REDUCTION(25),
	HEIGHT_GK_REDUCTION(34),
	HEIGHT_UTM_REDUCTION(35),
	DIRECTION_HEIGHT_GK_REDUCTION(234),
	DIRECTION_HEIGHT_UTM_REDUCTION(235); 

	private int id;
	private ProjectionType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ProjectionType getEnumByValue(int value) {
		for(ProjectionType element : ProjectionType.values()) {
			if(element.id == value)
				return element;
		}
		return NONE;
	}  
}