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
import org.applied_geodesy.adjustment.network.point.Point;

public class Point1D extends Point{

	public Point1D(Point3D p3d) {
		this(p3d.getName(), p3d.getZ());
		if (p3d.getStdZ() > 0.0)
			this.setStdZ( p3d.getStdZ() );
	}

	public Point1D(String id, double z, double sZ) {
		this(id, z);
		this.setStdZ(sZ);
	}

	public Point1D(String id, double z) {
		this(id, 0.0, 0.0, z);
	}

	public Point1D(String id, double x, double y, double z) {
		this(id, x, y, z, 0.0);
	}

	public Point1D(String id, double x, double y, double z, double sZ) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
		this.setStdZ(sZ);
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.getName() + ": " + this.getX() + "/" + this.getY() + "/" + this.getZ());
	}
	
	public final int getDimension() {
		return 1; 
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.POINT1D;
	}
}
