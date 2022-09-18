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

package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.parameter.UnknownParameter;

public abstract class StrainParameter extends UnknownParameter {
	private double value, tPrio = 0.0, tPost = 0.0, pPrio = 0.0, pPost = 0.0, nabla = 0.0, mdb = 0.0, confidence = 0.0;
	
	private double sigma = -1;
	private boolean significant = false;
	
	public StrainParameter(double value0) {
		this.setValue(value0);
	}

	public void setValue(double newValue) {
		this.value = newValue;
	}

	public double getValue() {
		return this.value;
	}
	
	public double getStd() {
		return this.sigma;
	}

	public void setStd(double std) {
		this.sigma = (std>0)?std:-1.0;
	}

	public boolean isSignificant() {
		return this.significant;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}

	public boolean isFixed() {
		return this.getColInJacobiMatrix() < 0;
	}

	public void setTprio(double t) {
		this.tPrio = t;
	}

	public void setTpost(double t) {
		this.tPost = t;
	}

	public void setPprio(double p) {
		this.pPrio = p;
	}

	public void setPpost(double p) {
		this.pPost = p;
	}

	public double getTprio() {
		return this.tPrio;
	}

	public double getTpost() {
		return this.tPost;
	}

	public double getPprio() {
		return this.pPrio;
	}

	public double getPpost() {
		return this.pPost;
	}

	public double getGrossError() {
		return nabla;
	}

	public void setGrossError(double nabla) {
		this.nabla = nabla;
	}

	public double getMinimalDetectableBias() {
		return mdb;
	}

	public void setMinimalDetectableBias(double mdb) {
		this.mdb = mdb;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public abstract double getExpectationValue();

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [value=" + value + ", sigma=" + sigma + "]";
	}
}
