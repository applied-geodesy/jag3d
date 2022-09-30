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

package org.applied_geodesy.adjustment.network.point;

import org.applied_geodesy.adjustment.network.ParameterType;

public class Point2D extends Point{

	public Point2D(Point3D p3d) {
		this(p3d.getName(), p3d.getX(), p3d.getY(), p3d.getZ());
		if (p3d.getStdX() > 0)
			this.setStdX( p3d.getStdX() );
		if (p3d.getStdY() > 0)
			this.setStdY( p3d.getStdY() );
	}

	public Point2D(String id, double x, double y, double z, double sX, double sY) {
		this(id, x, y);
		
		this.setStdX(sX);
		this.setStdY(sY);
	}

	public Point2D(String id, double x, double y, double sX, double sY) {
		this(id, x, y, 0, sX, sY);
	}

	public Point2D(String id, double x, double y) {
		this(id, x, y, 0);
	}
	
	public Point2D(String id, double x, double y, double z) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.getName() + ": " + this.getX() + "/" + this.getY());
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.POINT2D;
	}

	@Override
	public int getDimension() {
		return 2;
	}
}
