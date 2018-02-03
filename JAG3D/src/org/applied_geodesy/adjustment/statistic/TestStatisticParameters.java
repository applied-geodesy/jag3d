package org.applied_geodesy.adjustment.statistic;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class TestStatisticParameters  {
	
	private class KeySet implements Comparable<KeySet> {
		public final double f1,f2;
		public KeySet(double f1, double f2){
			this.f1 = f1;
			this.f2 = f2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(f1);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(f2);
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
			if (Double.doubleToLongBits(f1) != Double.doubleToLongBits(other.f1))
				return false;
			if (Double.doubleToLongBits(f2) != Double.doubleToLongBits(other.f2))
				return false;
			return true;
		}
		private TestStatisticParameters getOuterType() {
			return TestStatisticParameters.this;
		}
		@Override
		public int compareTo(KeySet keySet) {
			if (this.equals(keySet))
				return 0;
			
			if (this.f1 < keySet.f1 || this.f1 == keySet.f1 && this.f2 > keySet.f2)
				return -1;
								
			if (this.f1 > keySet.f1 || this.f1 == keySet.f1 && this.f2 < keySet.f2)
				return  1;

			return 0;
		}
	}

	public TestStatistic testStatistic;
	private SortedMap<KeySet, TestStatisticParameterSet> params = Collections.synchronizedSortedMap(new TreeMap<KeySet, TestStatisticParameterSet>());
	public TestStatisticParameters(TestStatistic testStatistic) {
		this.testStatistic = testStatistic;
	}
	
	public TestStatistic getTestStatistic() {
		return this.testStatistic;
	}
	
	public TestStatisticParameterSet[] getTestStatisticParameterSets() {
		TestStatisticParameterSet[] arr = new TestStatisticParameterSet[this.params.size()];
		int i=0;
		for (TestStatisticParameterSet set : this.params.values()) {
			arr[i++] = set;
		}
		return arr;
	}
	
	public TestStatisticParameterSet getTestStatisticParameter(double f1, double f2) {
		KeySet key = new KeySet(f1,f2);
		if (!this.params.containsKey(key))
			this.params.put(key, this.testStatistic.adjustTestStatisticParameter(new TestStatisticParameterSet(f1,f2)));
		return this.params.get(key);
	}
}
