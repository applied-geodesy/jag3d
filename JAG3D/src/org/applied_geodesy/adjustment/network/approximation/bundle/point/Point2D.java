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

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation2D;

public class Point2D extends Point {

	public Point2D (String id, double x, double y) {
		super(id);
		this.setX(x);
		this.setY(y);
	}

	@Override
	public final int getDimension() {
		return 2;
	}

	public void addObservedPoint(String id, double dir, double dist2d) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		PointBundle currentBundle = this.getCurrentBundle();
		Point2D p = ClassicGeodeticComputation.POLAR(this, id, dir, dist2d);
		currentBundle.addPoint(p);
	}

	public void addObservedPoint(String id, double dir, double dist3d, double ihDist, double thDist, double zenith, double ihZenith, double thZenith) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		double slopeDist = ClassicGeodeticComputation.SLOPEDISTANCE(dist3d, ihDist, thDist, zenith, ihZenith, thZenith);
		PointBundle currentBundle = this.getCurrentBundle();
		Point2D p = ClassicGeodeticComputation.POLAR(this, id, dir, slopeDist, zenith);
		currentBundle.addPoint(p);
	}

	@Override
	public Transformation2D getTransformation(PointBundle b1, PointBundle b2) {
		return new Transformation2D(b1, b2);
	}

	@Override
	public String toString() {
		return this.getName()+"  "+this.getX()+"    "+this.getY()+" ";
	}
}

