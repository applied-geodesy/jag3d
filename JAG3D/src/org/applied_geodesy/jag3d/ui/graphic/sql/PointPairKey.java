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

package org.applied_geodesy.jag3d.ui.graphic.sql;

public class PointPairKey {
	private String startPointName;
	private String endPointName;
	public PointPairKey(String startPointName, String endPointName) {
		boolean interChange = startPointName.compareTo(endPointName) <= 0;
		this.startPointName = interChange ? startPointName : endPointName;
		this.endPointName   = interChange ? endPointName : startPointName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endPointName == null) ? 0 : endPointName.hashCode());
		result = prime * result + ((startPointName == null) ? 0 : startPointName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointPairKey other = (PointPairKey) obj;
		if (this.endPointName != null && other.endPointName != null && this.endPointName.equals(other.endPointName) &&
				this.startPointName != null && other.startPointName != null && this.startPointName.equals(other.startPointName))
			return true;
		return false;
	}
}
