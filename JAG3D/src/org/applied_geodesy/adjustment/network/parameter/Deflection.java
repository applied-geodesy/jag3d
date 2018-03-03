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

package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.point.Point;

public abstract class Deflection extends UnknownParameter {
	private final Point point;

	private int rowInJacobiMatrix = -1;
	
	private double redundancy      =  0.0,
            	   sigma0          = -1.0,
            	   sigma           =  0.0,
            	   omega           =  0.0,
            	   value0, value, nabla, grzw, confidence;

	public Deflection(Point point) {
		this(point, 0.0);
	}

	public Deflection(Point point, double value) {
		this.point = point;
		this.setValue0(value);
	}

	public Deflection(Point point, double value, double std) {
		this.setValue0(value);
		this.point = point;
		this.setStd(std);
	}

	public final Point getPoint() {
		return this.point;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue0(double value0) {
		this.value0 = value0;
		this.setValue(value0);
	}

	public double getValue0() {
		return this.value0;
	}
	
	@Override
	public ObservationGroup getObservations() {
		return this.point.getObservations();
	}

	public void setStd(double std) {
		this.sigma0 = (this.sigma0 <= 0 && std>0)?std:this.sigma0;
		this.sigma  = std > 0 ? std : this.sigma;
	}

	public double getStd() {
		return this.sigma;
	}
	
	public double getStdApriori() {
		return this.sigma0;
	}

	public void setStdApriori(double std) {
		if (std > 0)
			this.sigma0 = std;
	}

	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}

	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}

	public double getRedundancy() {
		return redundancy;
	}

	public void setRedundancy(double redundancy) {
		this.redundancy = redundancy;
	}

	public void setOmega(double omega) {
		this.omega = omega;
	}

	public double getOmega() {
		return this.omega;
	}

	public double getGrossError() {
		return nabla;
	}

	public void setGrossError(double nabla) {
		this.nabla = nabla;
	}

	public double getMinimalDetectableBias() {
		return grzw;
	}

	public void setMinimalDetectableBias(double grzw) {
		this.grzw = grzw;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}	
}