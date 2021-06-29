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

package org.applied_geodesy.transformation.datum;

public class SphericalDeflectionParameter {
	private double rx = 0;
	private double ry = 0;
	private double h0 = 0;
	
	public double getSphericalDeflectionX() {
		return this.rx;
	}
	
	public double getSphericalDeflectionY() {
		return this.ry;
	}
	
	public double getFrameIntersectionHeight() {
		return this.h0;
	}
	
	public void setSphericalDeflectionParameter(double rx, double ry, double h0) {
		this.rx = rx;
		this.ry = ry;
		this.h0 = h0;
	}
}
