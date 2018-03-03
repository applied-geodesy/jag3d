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

package org.applied_geodesy.adjustment.network.congruence.strain;

public enum RestrictionType {
	UNIT_QUATERNION(0),

	FIXED_TRANSLATION_X(1),
	FIXED_TRANSLATION_Y(2),
	FIXED_TRANSLATION_Z(3),

	FIXED_ROTATION_X(4),
	FIXED_ROTATION_Y(5),
	FIXED_ROTATION_Z(6),

	FIXED_SCALE_X(7),
	FIXED_SCALE_Y(8),
	FIXED_SCALE_Z(9),

	FIXED_SHEAR_X(10),
	FIXED_SHEAR_Y(11),
	FIXED_SHEAR_Z(12),

	IDENT_SCALES_XY(78),
	IDENT_SCALES_XZ(79),
	IDENT_SCALES_YZ(89);

	private final int id;
	private RestrictionType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}

	public static RestrictionType getEnumByValue(int id) {
		RestrictionType[] restrictions = RestrictionType.values();
		for (RestrictionType restriction : restrictions) {
			if (restriction.getId() == id)
				return restriction;
		}
		return null;
	}
}
