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

import org.applied_geodesy.adjustment.network.observation.reduction.Reduction;
import org.applied_geodesy.adjustment.network.point.Point;

public class SphericalDeflectionModel {
	private class GeographicParameters {
		private double latitude, longitude, N; //height
		GeographicParameters(double latitude, double longitude, double height, double N) {
			this.latitude = latitude;
			this.longitude = longitude;
			//this.height = height;
			this.N = N;
		}
	}

	private Reduction reductions;
	private double r11 = 1, r12 = 0, r13 = 0;
	private double r21 = 0, r22 = 1, r23 = 0;
	private double r31 = 1, r32 = 0, r33 = 1;
	private double X0 = 0, Y0 = 0, Z0 = 0, d0 = 0;
	
	public SphericalDeflectionModel(Reduction reductions) {
		this.setReduction(reductions);
	}
	
	public void setReduction(Reduction reductions) {
		this.reductions = reductions;
		
		double a = this.reductions.getEllipsoid().getMajorAxis();
		double b = this.reductions.getEllipsoid().getMinorAxis();
				
		double longitude0 = this.reductions.getReferenceLongitude();
		double latitude0  = this.reductions.getReferenceLatitude();
		double h0         = this.reductions.getReferenceHeight();
		
		double sLatitude0 = Math.sin(latitude0);
		double cLatitude0 = Math.cos(latitude0);
		
		double sLongitude0 = Math.sin(longitude0);
		double cLongitude0 = Math.cos(longitude0);
		
		// https://en.wikipedia.org/wiki/Geographic_coordinate_conversion#From_ECEF_to_ENU
		this.r11 = -sLongitude0;
		this.r12 =  cLongitude0;
		this.r13 =  0.0;
		
		this.r21 = -sLatitude0 * cLongitude0;
		this.r22 = -sLatitude0 * sLongitude0;
		this.r23 =  cLatitude0;
		
		this.r31 =  cLatitude0 * cLongitude0;
		this.r32 =  cLatitude0 * sLongitude0;
		this.r33 =  sLatitude0;

	    double c    = a*a / b;
	    double eta2 = this.reductions.getEllipsoid().getSecondSquaredEccentricity() * cLatitude0 * cLatitude0;
	    double V0   = Math.sqrt(1.0 + eta2);
	    double N0   = c / V0;
	 
	    this.X0 = (N0 + h0) * this.r31;
	    this.Y0 = (N0 + h0) * this.r32;
	    this.Z0 = (Math.pow(b/a, 2) * N0 + h0) * this.r33;
	    
	    this.d0 = this.r31 * this.X0 + this.r32 * this.Y0 + this.r33 * this.Z0;
	}
	
	public void setSphericalDeflections(Point point) {
		int dim  = point.getDimension();
		double x = point.getX();
		double y = point.getY();
		double z = dim == 2 ? 0.0 : point.getZ();
		
		double x0 = this.reductions.getLocalPrinciplePoint().getX0();
		double y0 = this.reductions.getLocalPrinciplePoint().getY0();
		double z0 = this.reductions.getLocalPrinciplePoint().getZ0();
		
		double a = this.reductions.getEllipsoid().getMajorAxis();
		double b = this.reductions.getEllipsoid().getMinorAxis();

		GeographicParameters geographicParameters = this.getGeographicParameters(y - y0, x - x0, z - z0);

		double sLatitude = Math.sin(geographicParameters.latitude);
		double cLatitude = Math.cos(geographicParameters.latitude);
		
		double sLongitude = Math.sin(geographicParameters.longitude);
		double cLongitude = Math.cos(geographicParameters.longitude);
		
		double sx = cLatitude * cLongitude;
	    double sy = cLatitude * sLongitude;
	    double sz = sLatitude;

		double rx =   this.r11 * sx + this.r12 * sy + this.r13 * sz;
		double ry = -(this.r21 * sx + this.r22 * sy + this.r23 * sz);


		// Abstand zw. Ellipsoid und Ebene
		double surfX = geographicParameters.N * sx;
	    double surfY = geographicParameters.N * sy;
	    double surfZ = (Math.pow(b/a, 2) * geographicParameters.N) * sz;

	    double h = (this.d0 - this.r31 * surfX - this.r32 * surfY - this.r33 * surfZ) / (this.r31 * sx + this.r32 * sy + this.r33 * sz);

	    point.getSphericalDeflectionParameter().setSphericalDeflectionParameter(rx, ry, h);
	}
	
	private GeographicParameters getGeographicParameters(double east, double north, double up) {
		double a  = this.reductions.getEllipsoid().getMajorAxis();
		double b  = this.reductions.getEllipsoid().getMinorAxis();
		double e1 = this.reductions.getEllipsoid().getFirstSquaredEccentricity();
		double e2 = this.reductions.getEllipsoid().getSecondSquaredEccentricity();

		// global XYZ from local ENU -> XYZ = P0 * R'*ENU
		double X = this.X0 + this.r11 * east + this.r21 * north + this.r31 * up;
		double Y = this.Y0 + this.r12 * east + this.r22 * north + this.r32 * up;
		double Z = this.Z0 + this.r13 * east + this.r23 * north + this.r33 * up;
		
		double c = a*a/b;
		double p = Math.hypot(X, Y);
		double theta = Math.atan2(Z*a, p*b);
		
		double longitude = Math.atan2(Y, X);
		
		double latitude  = Math.atan2(Z + e2 * b * Math.pow(Math.sin(theta), 3), p - e1 * a * Math.pow(Math.cos(theta), 3));
		double cLatitude = Math.cos(latitude);
		
		double V = Math.sqrt(1.0 + e2 * cLatitude * cLatitude);
		double N = c/V;
		
		double h = p/cLatitude - N;
		
		return new GeographicParameters(latitude, longitude, h, N);
	}
}
