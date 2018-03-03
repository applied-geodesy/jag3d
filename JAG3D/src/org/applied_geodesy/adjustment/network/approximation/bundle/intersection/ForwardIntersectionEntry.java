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

package org.applied_geodesy.adjustment.network.approximation.bundle.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;

public class ForwardIntersectionEntry {
	private final Point2D fixPointA, fixPointB;
	private final String newPointId;
	private List<Double> alpha = new ArrayList<Double>(), beta = new ArrayList<Double>();
	
	public ForwardIntersectionEntry(Point2D fixPointA, Point2D fixPointB, String newPointId, double alpha, double beta) {
		this.fixPointA  = fixPointA;
		this.fixPointB  = fixPointB;
		this.newPointId = newPointId;
		
		this.addAngles(alpha, beta);
	}
	
	public void addAngles(double alpha, double beta) {
		this.alpha.add(MathExtension.MOD(alpha, 2.0*Math.PI));
		this.beta.add(MathExtension.MOD(beta, 2.0*Math.PI));
	}
	
	public void setFixPoint(Point2D p) {
		if (p.getName().equals(this.fixPointA.getName())) {
			this.fixPointA.setX(p.getX());
			this.fixPointA.setY(p.getY());			
		}
		else if (p.getName().equals(this.fixPointB.getName())) {
			this.fixPointB.setX(p.getX());
			this.fixPointB.setY(p.getY());			
		}
	}
	
	public Point2D adjust() {
		if (this.fixPointA == null || this.fixPointB == null)
			return null;
		
		int n = this.alpha.size();
		List<Double> medianX = new ArrayList<Double>(n);
		List<Double> medianY = new ArrayList<Double>(n);
		List<Point2D> intersectPoint = new ArrayList<Point2D>(n);
		
		double xA = this.fixPointA.getX();
		double yA = this.fixPointA.getY();
		
		double xB = this.fixPointB.getX();
		double yB = this.fixPointB.getY();
		
		double sAB = this.fixPointA.getDistance2D(this.fixPointB);
		double tAB = Math.atan2(yB-yA, xB-xA);

		for (int i=0; i<n; i++) {
			double alpha = this.alpha.get(i);
			double beta  = this.beta.get(i);
			
			if (alpha+beta == 0.0)
				continue;
			
			// Strecken zum Neupunkt via SIN-Satz
			double sAN = sAB*Math.sin(beta)/Math.sin(alpha+beta);
			double sBN = sAB*Math.sin(alpha)/Math.sin(alpha+beta);
			
			// Richtungswinkel zum Neupunkt
			double tAN = tAB - alpha;
			double tBN = Math.PI + tAB + beta;

			double yNA = yA + sAN*Math.sin(tAN);
			double xNA = xA + sAN*Math.cos(tAN);
			 
			double yNB = yB + sBN*Math.sin(tBN);
			double xNB = xB + sBN*Math.cos(tBN);
			
			if (Math.abs(yNA-yNB) < 0.1 && Math.abs(xNA-xNB) < 0.1) {
				double yN = 0.5*(yNA+yNB);
				double xN = 0.5*(xNA+xNB);
				medianX.add(xN);
				medianY.add(yN);
				intersectPoint.add(new Point2D(this.newPointId, xN, yN));
			}
		}
		
		Collections.sort(medianX);
		Collections.sort(medianY);
		
		double x0 = medianX.get(medianX.size()/2);
		double y0 = medianY.get(medianX.size()/2);
		
		double norm2 = Double.MAX_VALUE;
		int index = 0;
		for (int i=0; i<intersectPoint.size(); i++) {
			double x = intersectPoint.get(i).getX() - x0;
			double y = intersectPoint.get(i).getY() - y0;
			
			double norm = Math.hypot(x,y);
			if (norm < norm2) {
				norm2 = norm;
				index = i;
			}
		}
		return intersectPoint.get(index);
	}
	
	public Point getReferencePointA() {
		return this.fixPointA;
	}

	public Point getReferencePointB() {
		return this.fixPointB;
	}

	public String getNewPointId() {
		return this.newPointId;
	}
}
