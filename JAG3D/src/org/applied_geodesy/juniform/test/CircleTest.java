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

package org.applied_geodesy.juniform.test;

import java.util.List;
import java.util.Locale;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.curve.CircleFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

public class CircleTest extends NISTTest {
	private CircleTest() {}

	@Override
	Feature getFeature() {
		return new CircleFeature();
	}

	@Override
	void compare(List<Double> referenceResults, List<UnknownParameter> unknownParameters) {
//		Circles – 7 numbers
//		3 numbers represent the center of the circle
//		3 numbers represent the direction cosines of the normal of the plane containing
//		the circle
//		1 number represents the diameter of the circle
		
		int idxX = 0; 
		int idxY = 1;
				
		if (CMP_TYPE == ComponentOrderType.XZ) {
			idxX = 0; 
			idxY = 2;
		}
		else if (CMP_TYPE == ComponentOrderType.YZ) {
			idxX = 1; 
			idxY = 2;
		}
		
		double x0Ref = referenceResults.get(idxX);
		double y0Ref = referenceResults.get(idxY);
		
		double rRef  = 0.5 * referenceResults.get(6);

		double references[] = new double[] {
				x0Ref, y0Ref, rRef
		};

		List<ParameterType> types = List.of(
				ParameterType.ORIGIN_COORDINATE_X, 
				ParameterType.ORIGIN_COORDINATE_Y, 
				ParameterType.RADIUS 
		);

		for (int i = 0; i < types.size(); i++) {
			for (UnknownParameter unknownParameter : unknownParameters) {
				if (unknownParameter.getParameterType() == types.get(i)) {
					double diff = Math.abs(unknownParameter.getValue()) - Math.abs(references[i]);
					System.out.println(String.format(Locale.ENGLISH, TEMPLATE, unknownParameter.getParameterType(), unknownParameter.getValue(), references[i], diff, Math.abs(diff) > EPS ? "***" : ""));
					break;
				}
			}
		}
	}
	
	@Override
	int getDimension() {
		return 2;
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");
		
		NISTTest test = new CircleTest();
		test.start("./nist/Circle2d/");
	}
}
