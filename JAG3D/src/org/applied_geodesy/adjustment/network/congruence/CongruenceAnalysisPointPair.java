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

package org.applied_geodesy.adjustment.network.congruence;

import org.applied_geodesy.adjustment.ConfidenceRegion;
import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.point.Point;

public class CongruenceAnalysisPointPair {
	private final int id, dimension;
	private final static double ZERO = Math.sqrt(Constant.EPS);
	private final Point p0, p1;
	private double sigma[] = null,
					confidenceAxis[]  = null,
					confidenceAngles[] = new double[3],
					grzw[] = null,
					nabla[] = null,
					confidenceAxis2D[] = new double[2],
					confidenceAngle2D = 0;
	
	private double Tprio =  0.0,
	   			   Tpost =  0.0,
	   			   Pprio =  0.0,
	   			   Ppost =  0.0;
	
	private boolean significant = false;
	
	public CongruenceAnalysisPointPair(int id, int dimension, Point p0, Point p1) {
		if (p0.getDimension() < dimension || p1.getDimension() < dimension  || p0.getDimension() == 2 && p1.getDimension() == 1 || p1.getDimension() == 2 && p0.getDimension() == 1)
			throw new IllegalArgumentException(this.getClass() + " Fehler, Dimension der Punkte nicht valid! " + p0.getDimension()+", " + p1.getDimension()+" vs. "+dimension);
		this.id = id;
		this.p0 = p0;
		this.p1 = p1;
		this.dimension      = dimension;
		this.sigma          = new double[this.getDimension()];
		this.confidenceAxis = new double[this.getDimension()];
        this.grzw           = new double[this.getDimension()];
        this.nabla          = new double[this.getDimension()];
	}

	public void setProbabilityValues(double pPrio, double pPost) {
		this.Pprio = pPrio;
		this.Ppost = pPost;
	}

	public void setTeststatisticValues(double tPrio, double tPost) {
		this.Tprio = tPrio;
		this.Tpost = tPost;
	}

	public Point getStartPoint() {
		return this.p0;
	}

	public Point getEndPoint() {
		return this.p1;
	}
	
	public final int getId() {
		return this.id;
	}

	public final int getDimension() {
		return this.dimension;
	}

	public double getPprio() {
		return this.Pprio;
	}

	public double getPpost() {
		return this.Ppost;
	}

	public double getMagnitude() {
		int dim = this.getDimension();
		if (dim == 1)
			return Math.abs(p1.getZ() - p0.getZ());
		else if (dim == 2)
			return p1.getDistance2D(p0);
		else
			return p1.getDistance3D(p0);
	}

	public double getGrossErrorX() {
		return this.nabla[0];
	}

	public double getGrossErrorY() {
		return this.nabla[1];
	}

	public double getGrossErrorZ(){
		return this.nabla[this.getDimension() - 1];
	}

	public double getDeltaX() {
		return this.p1.getX()-this.p0.getX();
	}

	public double getDeltaY() {
		return this.p1.getY()-this.p0.getY();
	}

	public double getDeltaZ(){
		return this.p1.getZ()-this.p0.getZ();
	}

	public double getStdX() {
		return this.sigma[0];
	}

	public double getStdY() {
		return this.sigma[1];
	}

	public double getStdZ(){
		return this.sigma[this.getDimension() - 1];
	}

	public double getTprio() {
		return this.Tprio < ZERO ? 0.0 : this.Tprio;
	}

	public double getTpost() {
		return this.Tpost < ZERO ? 0.0 : this.Tpost;
	}

	public boolean isSignificant() {
		return this.significant;
	}

	public double getConfidenceAxis(int i) {
		return this.confidenceAxis[i];
	}

	public double getConfidenceAngle(int i) {
		return this.confidenceAngles[i];
	}

	public void setConfidenceRegion(ConfidenceRegion confidence) {
		for (int i=0; i<this.getDimension(); i++) {
			this.confidenceAxis[i] = confidence.getConfidenceAxis(i);
			
			if (this.getDimension() == 1 && i == 0 || this.getDimension() > 1 && i < 2) 
				this.confidenceAxis2D[i] = confidence.getConfidenceAxis2D(i, false);
		}
		
		if (this.getDimension() > 1) {
			this.confidenceAngle2D = confidence.getConfidenceAngle2D();
			this.confidenceAngles  = confidence.getEulerAngles();
		}
	}

	public double getConfidenceAxis2D(int i) {
		return this.confidenceAxis2D[i];
	}

	public double getConfidenceAngle2D() {
		return this.confidenceAngle2D;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}

	public double getMinimalDetectableBiasX() {
		return this.grzw[0];
	}	

	public double getMinimalDetectableBiasY() {
		return this.grzw[1];
	}	
	
	public double getMinimalDetectableBiasZ() {
		return this.grzw[this.getDimension()-1];
	}

	public void setGrossErrors(double[] nabla) {
		if (nabla.length == this.getDimension())
			this.nabla = nabla;
	}

	public void setMinimalDetectableBiases(double[] grzw) {
		if (grzw.length == this.getDimension())
			this.grzw = grzw;
	}

	public void setSigma(double[] sigma) {
		if (sigma.length == this.getDimension())
			this.sigma = sigma;
	}
}
