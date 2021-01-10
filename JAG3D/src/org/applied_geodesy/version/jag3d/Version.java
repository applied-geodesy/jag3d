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

package org.applied_geodesy.version.jag3d;

import java.util.Map;

import org.applied_geodesy.version.VersionType;

public class Version {
	private final static Map<VersionType, Integer> versions = Map.of(
			VersionType.ADJUSTMENT_CORE,   20200927,
			VersionType.DATABASE,          20201221,
			VersionType.USER_INTERFACE,    20201220
	);
	
	private Version() {}
	
	public static Integer get(VersionType type) {
		return versions.get(type);
	}
	
	public static Integer get() {
		int max = 0;
		for (Integer version : versions.values())
			max = Math.max(max, version);
		return max;
	}
	
	public static boolean isReleaseCandidate() {
		return Boolean.FALSE;
	}
}