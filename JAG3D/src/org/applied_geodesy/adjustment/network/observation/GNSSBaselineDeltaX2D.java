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

package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.point.Point;

public class GNSSBaselineDeltaX2D extends GNSSBaseline2D {

	public GNSSBaselineDeltaX2D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, observation, sigma);
	}
	
	@Override
	public double diffXs() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		return -m*Math.cos(phi);
	}
	
	@Override
	public double diffYs() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		return -m*Math.sin(phi);
	}
	
	@Override
	public double diffRotZ() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();

		return m*(Math.cos(phi)*dY-Math.sin(phi)*dX);
	}

	@Override
	public double diffScale() {
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();

		return Math.sin(phi)*dY+Math.cos(phi)*dX;
	}
	 
	@Override
	public double getValueAposteriori() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();

	    return m * (Math.sin(phi)*dY + Math.cos(phi)*dX);
	}

	@Override
	public double diffZs() {
		return 0;
	}

	@Override
	public ComponentType getComponent() {
		return ComponentType.X;
	}
}
