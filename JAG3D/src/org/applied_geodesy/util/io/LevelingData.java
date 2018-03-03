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

package org.applied_geodesy.util.io;

class LevelingData {
	private Double r1 = null, r2 = null, dr1 = null, dr2 = null;
	private Double v1 = null, v2 = null, dv1 = null, dv2 = null;
	private String startPointName = null, endPointName = null;
	
	public void addBackSightReading(String name, double r, double d) {
		this.addBackSightReading(name, r, d, this.r1 == null);
	}
	
	public void addForeSightReading(String name, double v, double d) {
		this.addForeSightReading(name, v, d, this.v1 == null);
	}
	
	public void addForeSightReading(String name, double v, double d, boolean isFirstForeSightReading) {
		if (isFirstForeSightReading) {
			this.endPointName = name;
			this.v1  = v;
			this.dv1 = d;
		}
		else if (!isFirstForeSightReading && this.endPointName.equals(name)) {
			this.v2  = v;
			this.dv2 = d;
		}
	}
	
	public void addBackSightReading(String name, double r, double d, boolean isFirstBackSightReading) {
		if (isFirstBackSightReading) {
			this.startPointName = name;
			this.r1  = r;
			this.dr1 = d;
		}
		else if (!isFirstBackSightReading && this.startPointName.equals(name)) {
			this.r2  = r;
			this.dr2 = d;
		}
	}
	
	public double getDistance() {
		double dist = 0;
		if (this.dr1 != null && this.dv1 != null)
			dist = dr1 + dv1;
		if (this.dr2 != null && this.dv2 != null) {
			dist += this.dr2 + this.dv2;
			dist *= 0.5;
		}
		return dist;
	}
	
	public double getDeltaH() {
		double dh = 0;
		if (this.r1 != null && this.v1 != null)
			dh = this.r1 - this.v1;
		if (this.r2 != null && this.v2 != null) {
			dh += this.r2 - this.v2;
			dh *= 0.5;
		}
		return dh;	
	}
	
	public String getStartPointName() {
		return this.startPointName;
	}
	
	public String getEndPointName() {
		return this.endPointName;
	}

	@Override
	public String toString() {
		return "LevelingData [startPointName=" + startPointName + " - endPointName=" + endPointName
				+ ", r1=" + r1 + ", r2=" + r2 + ", dr1=" + dr1
				+ ", dr2=" + dr2 + ", v1=" + v1 + ", v2=" + v2 + ", dv1=" + dv1
				+ ", dv2=" + dv2 + "]";
	}
	
	
}
