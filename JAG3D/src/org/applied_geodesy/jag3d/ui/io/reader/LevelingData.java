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

package org.applied_geodesy.jag3d.ui.io.reader;

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
		else if (!isFirstForeSightReading && (name == null || name.isBlank() || this.endPointName.equals(name))) {
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
		else if (!isFirstBackSightReading && (name == null || name.isBlank() || this.startPointName.equals(name))) {
			this.r2  = r;
			this.dr2 = d;
		}
	}
	
	public double getDistance() {
		double dr = 0;
		double dv = 0;
		
		if (this.dr1 != null && this.dr2 != null && this.dr1 > 0 && this.dr2 > 0)
			dr = 0.5 * (this.dr1 + this.dr2);
		else if (this.dr1 != null && this.dr1 > 0)
			dr = this.dr1;
		
		if (this.dv1 != null && this.dv2 != null && this.dv1 > 0 && this.dv2 > 0)
			dv = 0.5 * (this.dv1 + this.dv2);
		else if (this.dv1 != null && this.dv1 > 0)
			dv = this.dv1;

		return dr + dv;
	}
	
	public double getDeltaH() {
		double r = 0;
		double v = 0;
		
		if (this.r1 != null && this.r2 != null)
			r = 0.5 * (this.r1 + this.r2);
		else if (this.r1 != null)
			r = this.r1;
		
		if (this.v1 != null && this.v2 != null)
			v = 0.5 * (this.v1 + this.v2);
		else if (this.v1 != null)
			v = this.v1;

		return r - v;	
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
	
	public boolean hasFirstBackSightReading() {
		return this.r1 != null;
	}
	
	public boolean hasSecondBackSightReading() {
		return this.r2 != null;
	}
	
	public boolean hasFirstForeSightReading() {
		return this.v1 != null;
	}
	
	public boolean hasSecondForeSightReading() {
		return this.v2 != null;
	}
}
