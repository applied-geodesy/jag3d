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

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class BinomialTestStatisticParameters {
	private class KeySet implements Comparable<KeySet> {
		public final int numberOfTrials;
		public final double successProbability;
		public KeySet(int numberOfTrials, double successProbability){
			this.numberOfTrials     = numberOfTrials;
			this.successProbability = successProbability;
		}

		private BinomialTestStatisticParameters getOuterType() {
			return BinomialTestStatisticParameters.this;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + numberOfTrials;
			long temp;
			temp = Double.doubleToLongBits(successProbability);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KeySet other = (KeySet) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (numberOfTrials != other.numberOfTrials)
				return false;
			if (Double.doubleToLongBits(successProbability) != Double.doubleToLongBits(other.successProbability))
				return false;
			return true;
		}

		@Override
		public int compareTo(KeySet keySet) {
			if (this.equals(keySet))
				return 0;
			
			if (this.numberOfTrials < keySet.numberOfTrials || this.numberOfTrials == keySet.numberOfTrials && this.successProbability > keySet.successProbability)
				return -1;
								
			if (this.numberOfTrials > keySet.numberOfTrials || this.numberOfTrials == keySet.numberOfTrials && this.successProbability < keySet.successProbability)
				return  1;

			return 0;
		}
	}
	
	public TestStatistic testStatistic;
	private SortedMap<KeySet, BinomialTestStatisticParameterSet> params = Collections.synchronizedSortedMap(new TreeMap<KeySet, BinomialTestStatisticParameterSet>());
	public BinomialTestStatisticParameters(TestStatistic testStatistic) {
		this.testStatistic = testStatistic;
	}

	public BinomialTestStatisticParameterSet[] getBinomialTestStatisticParameterSets() {
		BinomialTestStatisticParameterSet[] arr = new BinomialTestStatisticParameterSet[this.params.size()];
		int i=0;
		for (BinomialTestStatisticParameterSet set : this.params.values()) {
			arr[i++] = set;
		}
		return arr;
	}
	
	public BinomialTestStatisticParameterSet getTestStatisticParameter(int numberOfTrials) {
		return this.getTestStatisticParameter(numberOfTrials, 0.5);
	}
	
	public BinomialTestStatisticParameterSet getTestStatisticParameter(int numberOfTrials, double successProbability) {
		KeySet key = new KeySet(numberOfTrials, successProbability);
		if (!this.params.containsKey(key)) 
			this.params.put(key, this.testStatistic.adjustTestStatisticParameter(new BinomialTestStatisticParameterSet(numberOfTrials, successProbability)));
		return this.params.get(key);
	}
}