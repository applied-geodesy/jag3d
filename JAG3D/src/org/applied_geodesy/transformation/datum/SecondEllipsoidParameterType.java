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

package org.applied_geodesy.transformation.datum;

enum SecondEllipsoidParameterType {
	MINOR_AXIS(1),
	INVERSE_FLATTENING(2),
	SQUARED_ECCENTRICITY(3);

	private final int id;
	private SecondEllipsoidParameterType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public static SecondEllipsoidParameterType getParameterById(int id) {
		SecondEllipsoidParameterType[] params = SecondEllipsoidParameterType.values();
		for (SecondEllipsoidParameterType param : params)
			if (param.getId() == id)
				return param;
		return null;
	}
}