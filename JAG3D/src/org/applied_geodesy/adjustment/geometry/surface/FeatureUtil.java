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

package org.applied_geodesy.adjustment.geometry.surface;

import java.util.ArrayList;
import java.util.Collection;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.geometry.Quaternion;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

class FeatureUtil {

	private FeatureUtil() {}
	
	/**
	 * estimate the Quaternion, which describes a rotation between the principle-axis and the z-axis
	 * @param principleAxis
	 * @return Quaternion
	 */
	static Quaternion getQuaternionHz(double principleAxis[]) {
		// rotational axis u = cross(n0, z)
		double u[] = new double[] {
				+principleAxis[1],
				-principleAxis[0],
				0.0
		};

		// rotation angle between principle-axis and z-axis
		// phi = acos(nz) 
		double phi = Math.acos( principleAxis[2] );

		// if geometry is already in canonical representation, i.e., n = [0 0 1]', 
		// no orthogonal base can be derived because cross(n,z) = [0 0 0]'
		// --> an arbitrary base is used
		if ( Math.abs(phi) < Constant.EPS || Math.abs(Math.abs(phi) - Math.PI) < Constant.EPS) 
			u = new double[] {1.0, 1.0, 0.0};

		return new Quaternion(u, phi);
	}
	
	/**
	 * rotates the points w.r.t. to z-axis
	 * @param points
	 * @param centerOfMass
	 * @param q
	 * @return horizontal points
	 */
	static Collection<FeaturePoint> getRotatedFeaturePoints(Collection<FeaturePoint> points, double centerOfMass[], Quaternion q) {
		
		Collection<FeaturePoint> rotatedPoints = new ArrayList<FeaturePoint>();
		// rotate points into xy-plane
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - centerOfMass[0];
			double yi = point.getY0() - centerOfMass[1];
			double zi = point.getZ0() - centerOfMass[2];

			Quaternion resultQ = q.rotate(new double[] { xi, yi, zi} );
			
			rotatedPoints.add( new FeaturePoint(point.getName(), resultQ.getQ1(), resultQ.getQ2(), resultQ.getQ3()) );
		}
		return rotatedPoints;
	}
}
