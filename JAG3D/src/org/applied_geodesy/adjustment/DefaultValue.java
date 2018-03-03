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

package org.applied_geodesy.adjustment;

public class DefaultValue {
	private final static int MAXIMAL_ITERATIONS = 5000;
	private final static double PROBABILITY_VALUE = 0.1;
	private final static double POWER_OF_TEST = 80.0;
	private final static double ROBUST_ESTIMATION_LIMIT = 3.5;
	
	private DefaultValue() {}
	
	public static int getMaximalNumberOfIterations() {
		return MAXIMAL_ITERATIONS;
	}
	public static double getProbabilityValue() {
		return PROBABILITY_VALUE;
	}
	public static double getPowerOfTest() {
		return POWER_OF_TEST;
	}
	public static double getRobustEstimationLimit() {
		return ROBUST_ESTIMATION_LIMIT;
	}
}
