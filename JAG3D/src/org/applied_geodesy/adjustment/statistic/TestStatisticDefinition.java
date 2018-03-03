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

import org.applied_geodesy.adjustment.DefaultValue;

public class TestStatisticDefinition {
	private TestStatisticType testStatisticType;
	private double probabilityValue, powerOfTest;
	boolean familywiseErrorRate = false;
	
	public TestStatisticDefinition() {
		this(TestStatisticType.BAARDA_METHOD, DefaultValue.getProbabilityValue(), DefaultValue.getPowerOfTest(), false);
	}
	
	public TestStatisticDefinition(TestStatisticType testStatisticType, double probabilityValue, double powerOfTest, boolean familywiseErrorRate) {
		this.testStatisticType = testStatisticType == null ? TestStatisticType.BAARDA_METHOD : testStatisticType;
		this.probabilityValue  = probabilityValue > 0 && probabilityValue < 100 ? probabilityValue : DefaultValue.getProbabilityValue();
		this.powerOfTest       = powerOfTest > 0 && powerOfTest < 100 ? powerOfTest : DefaultValue.getPowerOfTest();
		this.familywiseErrorRate = familywiseErrorRate;
	}

	public TestStatisticType getTestStatisticType() {
		return testStatisticType;
	}

	public double getProbabilityValue() {
		return probabilityValue;
	}

	public double getPowerOfTest() {
		return powerOfTest;
	}

	public boolean isFamilywiseErrorRate() {
		return this.familywiseErrorRate;
	}
	
	@Override
	public String toString() {
		return "TestStatisticDefinition [testStatisticType="
				+ testStatisticType + ", probabilityValue=" + probabilityValue + ", powerOfTest=" + powerOfTest
				+ ", familywiseErrorRate=" + familywiseErrorRate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(probabilityValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(powerOfTest);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (familywiseErrorRate ? 1231 : 1237);
		result = prime
				* result
				+ ((testStatisticType == null) ? 0 : testStatisticType
						.hashCode());
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
		TestStatisticDefinition other = (TestStatisticDefinition) obj;
		if (Double.doubleToLongBits(probabilityValue) != Double
				.doubleToLongBits(other.probabilityValue))
			return false;
		if (Double.doubleToLongBits(powerOfTest) != Double
				.doubleToLongBits(other.powerOfTest))
			return false;
		if (familywiseErrorRate != other.familywiseErrorRate)
			return false;
		if (testStatisticType != other.testStatisticType)
			return false;
		return true;
	}
}
