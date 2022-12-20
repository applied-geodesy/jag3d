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

package org.applied_geodesy.adjustment.network;

public class VarianceComponent {
	private final VarianceComponentType type;
	private int numberOfObservations = 0, numberOfEffectiveObservations = 0, numberOfNegativeResiduals = 0;
	private double redundancy = 0, omega = 0;
	
	VarianceComponent(VarianceComponentType type) {
		this.type = type;
	}
	
	public double getRedundancy() {
		return this.redundancy;
	}
	
	public void setRedundancy(double redundancy) {
		this.redundancy = redundancy;
	}
	
	public double getOmega() {
		return this.omega;
	}
	
	public void setOmega(double omega) {
		this.omega = omega;
	}
	
	public int getNumberOfObservations() {
		return this.numberOfObservations;
	}
	
	public void setNumberOfObservations(int noo) {
		this.numberOfObservations = noo;
	}
	
	public int getNumberOfEffectiveObservations() {
		return this.numberOfEffectiveObservations;
	}

	public void setNumberOfEffectiveObservations(int noeo) {
		this.numberOfEffectiveObservations = noeo;
	}
	
	public int getNumberOfNegativeResiduals() {
		return this.numberOfNegativeResiduals;
	}
	
	public void setNumberOfNegativeResiduals(int nopr) {
		this.numberOfNegativeResiduals = nopr;
	}
	
	public VarianceComponentType getVarianceComponentType() {
		return this.type;
	}
	
	public double getVarianceFactorAposteriori() {
		return this.redundancy > 0 ? this.omega / this.redundancy : 0.0;
	}

	public String toString() {
		return this.type+"\r\nr="+this.getRedundancy()+
				"\r\nomega="+this.getOmega()+
				"\r\nsigma="+this.getVarianceFactorAposteriori()+
				"\r\nnumObs="+this.getNumberOfObservations();
	}
}
