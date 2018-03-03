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

package org.applied_geodesy.adjustment.network.approximation.bundle.point;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point1D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;

public abstract class ClassicGeodeticComputation {
	
	/**
	 * Berechnet den Richtungswinkel zwischen zwei Punkten.
	 * @param startPoint Standpunkt
	 * @param endPoint Zielpunkt
	 * @return direction Richtungswinkel
	 */
	public static double DIRECTION(Point2D startPoint, Point2D endPoint) {
		return MathExtension.MOD(Math.atan2( (endPoint.getY()-startPoint.getY()), (endPoint.getX()-startPoint.getX()) ), 2.0*Math.PI);
	}
	
	/**
	 * Bestimmt einen 2D-Polarpunkt aus einem horizontalen Winkel und einer 2D-Strecke 
	 * @param station
	 * @param id
	 * @param epsilon
	 * @param dist2d
	 * @return point2d
	 */
	public static Point2D POLAR(Point2D station, String id, double epsilon, double dist2d) {
		double y = station.getY() + dist2d * Math.sin( epsilon );
		double x = station.getX() + dist2d * Math.cos( epsilon );
		return new Point2D(id, x, y);
	}

	/**
	 * Bestimmt einen 2D-Polarpunkt aus einem horizontalen Winkel und einer 2D-Strecke,
	 * die aus der 3D-Strecke und dem Zenitwinkel abgeleitet wird.
	 * @param station
	 * @param id
	 * @param epsilon
	 * @param dist3d
	 * @param zenith
	 * @return point2d
	 */
	public static Point2D POLAR(Point2D station, String id, double epsilon, double dist3d, double zenith) {
		double dist2d = ClassicGeodeticComputation.DISTANCE2D(dist3d, zenith);
		return ClassicGeodeticComputation.POLAR(station, id, epsilon, dist2d);
	}
		  
	/**
	 * Liefert die 3D-Strecke, die sich auf das Niveau des Zenitwinkels bezieht
	 * @param dist3d
	 * @param ihDist
	 * @param thDist
	 * @param zenith
	 * @param ihZenith
	 * @param thZenith
	 * @return slopeDist
	 */
	public static double SLOPEDISTANCE(double dist3d, double ihDist, double thDist, double zenith, double ihZenith, double thZenith) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		double dh = thZenith - (ihZenith - ihDist) - thDist;
		double alpha = Math.asin(dh*Math.sin(zenith)/Math.abs(dist3d));
		double beta = Math.PI - alpha - zenith;
		    
		return Math.sqrt(dist3d*dist3d + dh*dh - 2.0*dist3d*dh*Math.cos(beta));
	}
		  	
	/**
	 * Berechnet den trigon. Hoehenunterschied aus 3D-Strecke und Zenitwinkel unter
	 * Beruecksichtungung der Stand- und Zielpunkthoehe
	 * @param station
	 * @param id
	 * @param dist3d
	 * @param zenith
	 * @param ih
	 * @param th
	 * @return point1d
	 */
	public static Point1D TRIGO_HEIGHT_3D(Point1D station, String id, double dist3d, double zenith, double ih, double th) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		return new Point1D(id, station.getZ() + Math.abs( dist3d ) * Math.cos( zenith ) + ih - th);
	}
	
	/**
	 * Berechnet den trigon. Hoehenunterschied aus 2D-Strecke und Zenitwinkel unter
	 * Beruecksichtungung der Stand- und Zielpunkthoehe beim Zenitwinkel
	 * @param station
	 * @param id
	 * @param dist2d
	 * @param zenith
	 * @param ih
	 * @param th
	 * @return point1d
	 */
	public static Point1D TRIGO_HEIGHT_2D(Point1D station, String id, double dist2d, double zenith, double ih, double th) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		double dh = zenith == 0.0 || zenith == Math.PI ? 0.0 : Math.abs( dist2d ) * Math.cos( zenith ) / Math.sin( zenith );
		return new Point1D(id, station.getZ() + dh + ih - th);
	}
		
	/**
	 * Liefert den geo. Hoehenunterschied
	 * @param station
	 * @param id
	 * @param deltaH
	 * @param ih
	 * @param th
	 * @return point 1d
	 */
	public static Point1D ORTHO_HEIGHT(Point1D station, String id, double deltaH, double ih, double th) {
		return new Point1D(id, station.getZ() + deltaH + ih - th);
	}
	
	/**
	 * Berechnet den trigon. Hoehenunterschied aus 3D-Strecke und Zenitwinkel unter
	 * Beruecksichtungung der Stand- und Zielpunkthoehe
	 * 
	 * @param dist3d
	 * @param zenith
	 * @param ih
	 * @param th
	 * @return h
	 */
	public static double TRIGO_HEIGHT_3D(double dist3d, double zenith, double ih, double th) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		return Math.abs(dist3d) * Math.cos( zenith ) + ih - th;
	}
	
	/**
	 * Berechnet den trigon. Hoehenunterschied aus 2D-Strecke und Zenitwinkel unter
	 * Beruecksichtungung der Stand- und Zielpunkthoehe fuer den Zenitwinkel
	 * 
	 * @param dist2d
	 * @param zenith
	 * @param ih
	 * @param th
	 * @return h
	 */
	public static double TRIGO_HEIGHT_2D(double dist2d, double zenith, double ih, double th) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		return zenith == 0.0 || zenith == Math.PI ? 0.0 : Math.abs( dist2d ) * Math.cos( zenith ) / Math.sin( zenith )  + ih - th;
	}
	
	/**
	 * Liefert den geo. Hoehenunterschied
	 * @param deltaH
	 * @param ih
	 * @param th
	 * @return h
	 */
	public static double ORTHO_HEIGHT(double deltaH, double ih, double th) {
		return deltaH + ih - th;
	}
	
	/**
	 * Liefert die 2D-Strecke, die sich aus der Raumstrecke und dem Zenitwinkel ergibt.
	 * @param dist3d
	 * @param zenith
	 * @return dist2d
	 */
	public static double DISTANCE2D(double dist3d, double zenith) {
		if (zenith > Math.PI)
			zenith = 2.0*Math.PI - zenith;
		return Math.abs(dist3d) * Math.sin( zenith );
	}
}
