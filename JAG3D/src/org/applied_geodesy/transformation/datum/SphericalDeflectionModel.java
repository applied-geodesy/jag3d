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
	private double X0 = 0, Y0 = 0, Z0 = 0, d0 = 0;
	
	public SphericalDeflectionModel(Reduction reductions) {
		this.setReduction(reductions);
	}
	
	private void setReduction(Reduction reductions) {
		this.reductions = reductions;
		
		double a = this.reductions.getEllipsoid().getMajorAxis();
		double b = this.reductions.getEllipsoid().getMinorAxis();
			
		double R[][]    = this.reductions.getPrincipalPoint().getRotationSequenceXYZtoENU();
		double latitude = this.reductions.getPrincipalPoint().getLatitude();
		double h0       = this.reductions.getPrincipalPoint().getHeight();
		
		double cLatitude = Math.cos(latitude);
		double r31 = R[2][0];
		double r32 = R[2][1];
		double r33 = R[2][2];

	    double c    = a*a / b;
	    double eta2 = this.reductions.getEllipsoid().getSecondSquaredEccentricity() * cLatitude * cLatitude;
	    double V0   = Math.sqrt(1.0 + eta2);
	    double N0   = c / V0;
	 
	    this.X0 = (N0 + h0) * r31;
	    this.Y0 = (N0 + h0) * r32;
	    this.Z0 = (Math.pow(b/a, 2) * N0 + h0) * r33;
	    // Abstand vom Ursprung zur Tangentialebene im Fundamentalpunkt
	    this.d0 = r31 * this.X0 + r32 * this.Y0 + r33 * this.Z0;
	}
	
	public void setSphericalDeflections(Point point) {
		int dim  = point.getDimension();
		double x = point.getX();
		double y = point.getY();
		double z = dim == 2 ? 0.0 : point.getZ();
		
		double x0    = this.reductions.getPrincipalPoint().getX();
		double y0    = this.reductions.getPrincipalPoint().getY();
		double z0    = this.reductions.getPrincipalPoint().getZ();
		double R[][] = this.reductions.getPrincipalPoint().getRotationSequenceXYZtoENU();
		
		double a = this.reductions.getEllipsoid().getMajorAxis();
		double b = this.reductions.getEllipsoid().getMinorAxis();

		GeographicParameters geographicParameters = this.getGeographicParameters(y - y0, x - x0, z - z0);

		double sLatitude = Math.sin(geographicParameters.latitude);
		double cLatitude = Math.cos(geographicParameters.latitude);
		
		double sLongitude = Math.sin(geographicParameters.longitude);
		double cLongitude = Math.cos(geographicParameters.longitude);
		
		// Hofmann-Wellenhof et al. 1994, Gl (3.11)
		double sx = cLatitude * cLongitude;
	    double sy = cLatitude * sLongitude;
	    double sz = sLatitude;
	    
	    double r11 = R[0][0];
	    double r12 = R[0][1];
	    double r13 = R[0][2];
	    
	    double r21 = R[1][0];
	    double r22 = R[1][1];
	    double r23 = R[1][2];
	    
	    double r31 = R[2][0];
	    double r32 = R[2][1];
	    double r33 = R[2][2];

	    double dy = r11 * sx + r12 * sy + r13 * sz;
		double dx = r21 * sx + r22 * sy + r23 * sz;
		double dz = r31 * sx + r32 * sy + r33 * sz;
		
		// Approx. fuer kleine Netze
//		double rx =   r11 * sx + r12 * sy + r13 * sz;
//		double ry = -(r21 * sx + r22 * sy + r23 * sz);

		// Sequence Ry*Rx
		double rx =  Math.asin(dy);
		double ry = -Math.atan2(dx, dz);
		
		// Abstand zw. Ellipsoid und Ebene - Hofmann-Wellenhof et al. 1994, Gl (3.1)
		double surfX = geographicParameters.N * sx;
	    double surfY = geographicParameters.N * sy;
	    double surfZ = (Math.pow(b/a, 2) * geographicParameters.N) * sz;

//	    // Abstand zwischen Punkt auf Ellipsoid und Ebene; entlang des geoz. Richtungsvektors des Punktes
//	    double h = (this.d0 - r31 * surfX - r32 * surfY - r33 * surfZ) / (r31 * sx + r32 * sy + r33 * sz);
	    // Abstand zwischen Punkt auf Ellipsoid und Ebene; entlang der Normalen des tangentialen Systems
	    double h = (this.d0 - r31 * surfX - r32 * surfY - r33 * surfZ);

	    point.getSphericalDeflectionParameter().setSphericalDeflectionParameter(rx, ry, h);
	}
	
	/**
	 * Bestimmt aus den kartesitschen Koordinaten east/north die zugehoerigen
	 * geographischen Koordinaten, die ellips. Hoehe und den Normalkruemmungsradius
	 * @param east
	 * @param north
	 * @param up
	 * @return
	 */
	private GeographicParameters getGeographicParameters(double east, double north, double up) {
		double a  = this.reductions.getEllipsoid().getMajorAxis();
		double b  = this.reductions.getEllipsoid().getMinorAxis();
		double e1 = this.reductions.getEllipsoid().getFirstSquaredEccentricity();
		double e2 = this.reductions.getEllipsoid().getSecondSquaredEccentricity();
		
		double R[][] = this.reductions.getPrincipalPoint().getRotationSequenceXYZtoENU();
		double r11 = R[0][0];
	    double r12 = R[0][1];
	    double r13 = R[0][2];
	    
	    double r21 = R[1][0];
	    double r22 = R[1][1];
	    double r23 = R[1][2];
	    
	    double r31 = R[2][0];
	    double r32 = R[2][1];
	    double r33 = R[2][2];

		// global XYZ from local ENU -> XYZ = P0 * R'*ENU - Hofmann-Wellenhof et al. (1994), S. 32f
		double X = this.X0 + r11 * east + r21 * north + r31 * up;
		double Y = this.Y0 + r12 * east + r22 * north + r32 * up;
		double Z = this.Z0 + r13 * east + r23 * north + r33 * up;
		
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
