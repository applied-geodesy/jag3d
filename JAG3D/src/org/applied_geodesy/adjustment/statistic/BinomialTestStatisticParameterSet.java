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

package org.applied_geodesy.adjustment.statistic;

public class BinomialTestStatisticParameterSet {

	private double alpha;
	private final double successProbability;
	private final int numberOfTrials;
	private double lowerTailQuantile = 0;
	
	public BinomialTestStatisticParameterSet(int numberOfTrials, double successProbability) {
		this.numberOfTrials     = numberOfTrials;
		this.successProbability = successProbability > 0 && successProbability < 1.0 ? successProbability : 0.5;
	}
	
	public int getNumberOfTrials() {
		return this.numberOfTrials;
	}
	
	public double getSuccessProbability() {
		return this.successProbability;
	}
	
	public double getProbabilityValue() {
		return this.alpha;
	}
	
	public void setProbabilityValue(double alpha) {
		this.alpha = alpha;
	}
	
	public void setQuantile(double lowerTailQuantile) {
		this.lowerTailQuantile = lowerTailQuantile >= 0 && lowerTailQuantile <= this.numberOfTrials ? lowerTailQuantile : 0;
	}
	
	public double getLowerTailQuantile() {
		return this.lowerTailQuantile;
	}
	
	public double getUpperTailQuantile() {
		return this.numberOfTrials - this.lowerTailQuantile;
	}
}
