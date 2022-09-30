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

public class Point3D extends Point{

	public Point3D(String id, double x, double y, double z) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
	}

	public Point3D(String id, double x, double y, double z, double sX, double sY, double sZ) {
		this(id, x, y, z);
		
		this.setStdX(sX);
		this.setStdY(sY);
		this.setStdZ(sZ);
	}
	
	public final int getDimension() {
		return 3; 
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.getName() + ": " + this.getX() + "/" + this.getY() + "/" + this.getZ());
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.POINT3D;
	}
}