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

public class PrincipalPoint  {
	private double x = 0, y = 0, z = 0;
	private double latitude  = 0, longitude = 0, height = 0;
	private double R[][] = new double[][] {
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
	};
	
	public PrincipalPoint() {
		this(0, 0, 0, 0, 0, 0);
	}
	
	public PrincipalPoint(double x, double y, double z, double latitude, double longitude, double height) {
		this.setCoordinates(x, y, z, latitude, longitude, height);
	}
	
	public void setCoordinates(double x, double y, double z, double latitude, double longitude, double height) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.latitude  = latitude;
		this.longitude = longitude;
		this.height    = height;
		
		double sLatitude = Math.sin(this.latitude);
		double cLatitude = Math.cos(this.latitude);
		
		double sLongitude = Math.sin(this.longitude);
		double cLongitude = Math.cos(this.longitude);
		
		// https://en.wikipedia.org/wiki/Geographic_coordinate_conversion#From_ECEF_to_ENU
		this.R = new double[][] {
			{            -sLongitude,              cLongitude,       0.0},
			{-sLatitude * cLongitude, -sLatitude * sLongitude, cLatitude},
			{ cLatitude * cLongitude,  cLatitude * sLongitude, sLatitude}
		};		
	}
	
	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}
	
	public double getHeight() {
		return this.height;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}
	
	public double getZ() {
		return this.z;
	}
	
	public double[][] getRotationSequenceXYZtoENU() {
		return this.R;
	}
}