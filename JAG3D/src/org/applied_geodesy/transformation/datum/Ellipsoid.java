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

package org.applied_geodesy.transformation.datum;

import org.applied_geodesy.adjustment.Constant;

public class Ellipsoid {
	private final double majorAxis, minorAxis, inverseFlattening, flattening;
	private double firstSquaredEccentricity, secondSquaredEccentricity;
	private boolean sphereModel = Boolean.FALSE;
	
	public final static Ellipsoid SPHERE     = Ellipsoid.createEllipsoidFromMinorAxis(Constant.EARTH_RADIUS, Constant.EARTH_RADIUS);
	public final static Ellipsoid WGS84      = Ellipsoid.createEllipsoidFromSquaredEccentricity(6378137.0, 0.00669437999013);
	public final static Ellipsoid GRS80      = Ellipsoid.createEllipsoidFromSquaredEccentricity(6378137.0, 0.00669438002290);
	public final static Ellipsoid BESSEL1941 = Ellipsoid.createEllipsoidFromMinorAxis(Math.exp( 6.8046434637 * Math.log(10) ), Math.exp( 6.8031892839 * Math.log(10) ));
	public final static Ellipsoid KRASSOWSKI = Ellipsoid.createEllipsoidFromInverseFlattening(6378245.0, 298.3);
	public final static Ellipsoid HAYFORD    = Ellipsoid.createEllipsoidFromInverseFlattening(6378388.0, 297.0);
		
	private Ellipsoid(double majorAxis, double secondEllipsoidParameterValue, SecondEllipsoidParameterType parameterType) {	
		this.majorAxis  = majorAxis;

		switch (parameterType) {
		case SQUARED_ECCENTRICITY:
			this.firstSquaredEccentricity  = secondEllipsoidParameterValue;
			this.sphereModel               = this.firstSquaredEccentricity == 0;
			this.minorAxis                 = this.sphereModel ? this.majorAxis : this.majorAxis * Math.sqrt(1.0 - this.firstSquaredEccentricity);
			this.flattening                = this.sphereModel ? 0.0 : 1.0 - Math.sqrt(1.0 - this.firstSquaredEccentricity);
			this.inverseFlattening         = this.sphereModel ? Double.POSITIVE_INFINITY : 1.0 / (1.0 - Math.sqrt(1.0 - firstSquaredEccentricity));
			this.secondSquaredEccentricity = this.sphereModel ? 0.0 : this.firstSquaredEccentricity / (1.0 - this.firstSquaredEccentricity);
			break;

		case INVERSE_FLATTENING:
			this.inverseFlattening         = secondEllipsoidParameterValue;
			this.sphereModel               = Double.POSITIVE_INFINITY == this.inverseFlattening;
			this.flattening                = this.sphereModel ? 0.0 : 1.0 / this.inverseFlattening;
			this.minorAxis                 = this.sphereModel ? this.majorAxis : this.majorAxis - this.majorAxis / this.inverseFlattening;
			this.firstSquaredEccentricity  = this.sphereModel ? 0.0 : (2.0 - 1.0 / this.inverseFlattening) / this.inverseFlattening;
			this.secondSquaredEccentricity = this.sphereModel ? 0.0 : this.flattening * (2.0 - this.flattening) / Math.pow(1.0 - this.flattening, 2);
			break;

		default: // MINOR_AXIS
			this.minorAxis                 = secondEllipsoidParameterValue;
			this.sphereModel               = this.minorAxis == this.majorAxis;
			this.flattening                = this.sphereModel ? 0.0 : 1.0 - this.minorAxis / this.majorAxis;
			this.inverseFlattening         = this.sphereModel ? Double.POSITIVE_INFINITY : this.majorAxis / (this.majorAxis - this.minorAxis);
			this.firstSquaredEccentricity  = this.sphereModel ? 0.0 : 1.0 - ((this.minorAxis * this.minorAxis) / (this.majorAxis * this.majorAxis));
			this.secondSquaredEccentricity = this.sphereModel ? 0.0 : (this.majorAxis * this.majorAxis - this.minorAxis * this.minorAxis) / (this.minorAxis * this.minorAxis);

			break;
		}
	}
	
	public boolean isSphereModel() {
		return this.sphereModel;
	}

	public static Ellipsoid createEllipsoidFromMinorAxis(double majorAxis, double minorAxis) {
		return new Ellipsoid(majorAxis, minorAxis, SecondEllipsoidParameterType.MINOR_AXIS);		
	}

	public static Ellipsoid createEllipsoidFromInverseFlattening(double majorAxis, double inverseFlattening) {
		return new Ellipsoid(majorAxis, inverseFlattening, SecondEllipsoidParameterType.INVERSE_FLATTENING);
	}

	public static Ellipsoid createEllipsoidFromSquaredEccentricity(double majorAxis, double eccentricity) {
		return new Ellipsoid(majorAxis, eccentricity, SecondEllipsoidParameterType.SQUARED_ECCENTRICITY);
	}
	
	public double getRadiusOfConformalSphere(double latitude) {
		if (this.isSphereModel())
			return this.majorAxis;
		
		double sin = Math.sin(latitude);
		double e2 = this.getFirstSquaredEccentricity();
		return this.majorAxis * Math.sqrt(1.0 - e2) / (1.0 - e2 * sin * sin);
	}
		
	public double getFirstSquaredEccentricity() {
		return this.firstSquaredEccentricity;
	}
	
	public double getSecondSquaredEccentricity() {
		return this.secondSquaredEccentricity;
	}
	
	public double getFlattening() {
		return this.flattening;
	}
	
	public double getInverseFlattening() {
		return this.inverseFlattening;
	}

	public final double getMajorAxis() {
		return this.majorAxis;
	}

	public final double getMinorAxis() {
		return this.minorAxis;
	}

	@Override
	public String toString() {
		return "Ellipsoid: [" + this.majorAxis + ", " + this.minorAxis + "]";
	}
}
