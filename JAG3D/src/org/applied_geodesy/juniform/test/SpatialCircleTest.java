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
import org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

public class SpatialCircleTest extends NISTTest {
	private SpatialCircleTest() {}

	@Override
	Feature getFeature() {
		return new SpatialCircleFeature();
	}

	@Override
	void compare(List<Double> referenceResults, List<UnknownParameter> unknownParameters) {
//		Circles – 7 numbers
//		3 numbers represent the center of the circle
//		3 numbers represent the direction cosines of the normal of the plane containing
//		the circle
//		1 number represents the diameter of the circle
		
		double x0Ref = referenceResults.get(0);
		double y0Ref = referenceResults.get(1);
		double z0Ref = referenceResults.get(2);
		
		double nxRef = referenceResults.get(3);
		double nyRef = referenceResults.get(4);
		double nzRef = referenceResults.get(5);
		
		double rRef  = 0.5 * referenceResults.get(6);
		
		double dRef = nxRef * x0Ref + nyRef * y0Ref + nzRef * z0Ref;

		double references[] = new double[] {
				x0Ref, y0Ref, z0Ref, rRef, nxRef, nyRef, nzRef, dRef	
		};

		List<ParameterType> types = List.of(
				ParameterType.ORIGIN_COORDINATE_X, 
				ParameterType.ORIGIN_COORDINATE_Y, 
				ParameterType.ORIGIN_COORDINATE_Z, 
				ParameterType.RADIUS,
				ParameterType.VECTOR_X, 
				ParameterType.VECTOR_Y, 
				ParameterType.VECTOR_Z, 
				ParameterType.LENGTH);

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
		return 3;
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");
		
		NISTTest test2d = new SpatialCircleTest();
		test2d.start("./nist/Circle2d/");
		
		NISTTest test3d = new SpatialCircleTest();
		test3d.start("./nist/Circle3d_full/");
	}
}
