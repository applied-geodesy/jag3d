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

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.surface.ConeFeature;

public class ConeTest extends NISTTest {
	private ConeTest() {}

	@Override
	void compare(List<Double> referenceResults, List<UnknownParameter> unknownParameters) {
//		Cones – 8 numbers
//		3 numbers represent a point on the cone axis
//		3 numbers represent the direction cosines of the cone axis
//		1 number represents the orthogonal distance from the reported point on the axis to
//		the surface of the cone.
//		1 number represents the full apex angle of the cone in degrees (less than 180)
		
		double x0Ref = referenceResults.get(0);
		double y0Ref = referenceResults.get(1);
		double z0Ref = referenceResults.get(2);
		
		double nxRef = referenceResults.get(3);
		double nyRef = referenceResults.get(4);
		double nzRef = referenceResults.get(5);
		
		double rRef = referenceResults.get(6);
		double phiRef = 0.5 * referenceResults.get(7) * Constant.RHO_DEG2RAD;
		
		double s = -rRef / Math.sin(phiRef);
		
		double xApexRef = x0Ref + s * nxRef;
		double yApexRef = y0Ref + s * nyRef;
		double zApexRef = z0Ref + s * nzRef;
		
		double dRef = nxRef * x0Ref + nyRef * y0Ref + nzRef * z0Ref;
		
		// position closest to the origin
		x0Ref = x0Ref - dRef * nxRef;
		y0Ref = y0Ref - dRef * nyRef;
		z0Ref = z0Ref - dRef * nzRef;

		double references[] = new double[] {
				x0Ref, y0Ref, z0Ref, xApexRef, yApexRef, zApexRef, nxRef, nyRef, nzRef, 1.0/Math.tan(phiRef), 1.0/Math.tan(phiRef)	
		};

		double xApex = 0, yApex = 0, zApex = 0, x0 = 0, y0 = 0, z0 = 0, nx = 0, ny = 0, nz = 0, a = 0, c = 0;
		for (UnknownParameter unknownParameter : unknownParameters) {
			if (unknownParameter.getParameterType() == ParameterType.ORIGIN_COORDINATE_X)
				xApex = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.ORIGIN_COORDINATE_Y)
				yApex = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.ORIGIN_COORDINATE_Z)
				zApex = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.ROTATION_COMPONENT_R31)
				nx = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.ROTATION_COMPONENT_R32)
				ny = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.ROTATION_COMPONENT_R33)
				nz = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.MAJOR_AXIS_COEFFICIENT)
				a = unknownParameter.getValue();
			else if (unknownParameter.getParameterType() == ParameterType.MINOR_AXIS_COEFFICIENT)
				c = unknownParameter.getValue();
		}

		double d = nx * xApex + ny * yApex + nz * zApex;
		
		// position closest to the origin
		x0 = xApex - d * nx;
		y0 = yApex - d * ny;
		z0 = zApex - d * nz;
		
		double solution[] = new double[] {
				x0, y0, z0, xApex, yApex, zApex, nx, ny, nz, a, c	
		};
		
		List<ParameterType> types = List.of(
				ParameterType.COORDINATE_X, 
				ParameterType.COORDINATE_Y, 
				ParameterType.COORDINATE_Z, 
				ParameterType.ORIGIN_COORDINATE_X, 
				ParameterType.ORIGIN_COORDINATE_Y, 
				ParameterType.ORIGIN_COORDINATE_Z, 
				ParameterType.ROTATION_COMPONENT_R31,
				ParameterType.ROTATION_COMPONENT_R32,
				ParameterType.ROTATION_COMPONENT_R33,
				ParameterType.MAJOR_AXIS_COEFFICIENT,
				ParameterType.MINOR_AXIS_COEFFICIENT
		);

		for (int i = 0; i < types.size(); i++) {
			double diff = Math.abs(solution[i]) - Math.abs(references[i]);
			System.out.println(String.format(Locale.ENGLISH, TEMPLATE, types.get(i), solution[i], references[i], diff, Math.abs(diff) > EPS ? "***" : ""));
		}
	}
	
	@Override
	Feature getFeature() {
		return new ConeFeature();
	}
	
	@Override
	int getDimension() {
		return 3;
	}
	
	@Override
	double getLambda() {
		return 100.0;
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");
		
		NISTTest test = new ConeTest();
		test.start("./nist/Cone/");
	}
}
