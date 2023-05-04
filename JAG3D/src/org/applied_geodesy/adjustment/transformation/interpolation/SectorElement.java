/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistanceribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distanceributed in the hope that it will be useful,      *
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

package org.applied_geodesy.adjustment.transformation.interpolation;

import org.applied_geodesy.adjustment.MathExtension;

class SectorElement implements Comparable<SectorElement> {
	private final double distance, azimuth;
	private final double residualX, residualY, residualZ;
	private final double weight;
	
	SectorElement(double azimuth, double distance, double residualX, double residualY, double residualZ) {
		this(azimuth, distance, residualX, residualY, residualZ, 0.0);
	}
	
	SectorElement(double azimuth, double distance, double residualX, double residualY, double residualZ, double weight) {
		this.distance = distance;
		this.azimuth = MathExtension.MOD(azimuth, 2.0*Math.PI);
		this.residualX = residualX;
		this.residualY = residualY;
		this.residualZ = residualZ;
		this.weight = weight;
	}
	
	public double getResidualX() {
		return this.residualX;
	}
	
	public double getResidualY() {
		return this.residualY;
	}
	
	public double getResidualZ() {
		return this.residualZ;
	}
	
	public double getDistance() {
		return this.distance;
	}
	
	public double getAzimuth() {
		return this.azimuth;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	@Override
	public int compareTo(SectorElement compareObject) {
		if (this.getAzimuth() < compareObject.getAzimuth())
			return -1;
		else if (this.getAzimuth() > compareObject.getAzimuth())
			return  1;
		return 0;
	}
}
