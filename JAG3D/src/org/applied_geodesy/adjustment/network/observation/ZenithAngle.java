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

package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.RefractionCoefficient;
import org.applied_geodesy.adjustment.point.Point;

public class ZenithAngle extends Observation {
	private FaceType face = FaceType.ONE;
	private RefractionCoefficient refractionCoefficient = new RefractionCoefficient();
	
	public ZenithAngle(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double measuringElement, double sigma, double distanceForUncertaintyModel) {
		super(id, startPoint, endPoint, startPointHeight, endPointHeight, measuringElement,	sigma, distanceForUncertaintyModel);
		if (Math.abs((2.0*Math.PI - this.getValueApriori()) - this.getValueAposteriori()) < Math.abs(this.getValueApriori() - this.getValueAposteriori())) {
			// Speichere, wie auch bei den Richtungen, den Wert der 1. Lage
			this.setValueApriori(2.0*Math.PI - this.getValueApriori());
			this.setFace(FaceType.TWO);	
		}
	}

	@Override
	public double diffXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);
		
		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return -(v*(crxs*srys*v + w*srxs*srys) + u*(crxs*srys*u + w*crys)) / dist2D_times_distSqr3D;
	}

	@Override
	public double diffYs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return -(v*(crxs*w - srxs*v) - srxs*u*u) / dist2D_times_distSqr3D;
	}

	@Override
	public double diffZs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return (v*(crxs*crys*v + w*crys*srxs) + u*(crxs*crys*u - w*srys)) / dist2D_times_distSqr3D;
	}
	
	@Override
	public double diffDeflectionXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return -v*(u*u + v*v + w*w + w*ih) / dist2D_times_distSqr3D;
	}
	
	@Override
	public double diffDeflectionYs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);
		
		double tmp11 = crys*(xe - xs) + srys*(ze - zs) - th*crxe*crys*srye + th*crxe*crye*srys;
		double tmp21 = crys*(ze - zs) - srys*(xe - xs) + th*crxe*crye*crys + th*crxe*srye*srys;

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return (tmp11*crxs*(u*u + v*v) + w*(tmp11*srxs*v + tmp21*u)) / dist2D_times_distSqr3D;
	}
	
	@Override
	public double diffDeflectionXe() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);
		
		double tmp12 = th*(crxs*crye*crys*srxe - crxe*srxs + crxs*srxe*srye*srys);
		double tmp22 = th*(crxe*crxs + crye*crys*srxe*srxs + srxe*srxs*srye*srys);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return (w*(tmp22*v + th*u*Math.sin(rye - rys)*srxe) + tmp12*(u*u + v*v)) / dist2D_times_distSqr3D;
	}
	
	@Override
	public double diffDeflectionYe() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		double dist2D_times_distSqr3D = Math.hypot(u, v) * (u*u + v*v + w*w);

		if (dist2D_times_distSqr3D == 0)
			return 0.0;
		return ((th*crxe*(Math.sin(rye - rys)*crxs*(u*u + v*v) - w*(Math.cos(rye - rys)*u - Math.sin(rye - rys)*srxs*v)))) / dist2D_times_distSqr3D;
	}

	@Override
	public double getValueAposteriori() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe;
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);
		
		double dist2D = Math.hypot(u, v);
		double k = this.refractionCoefficient.getValue();
	    double geoCorr = -k*dist2D/(2.0*Constant.EARTH_RADIUS);

	    return MathExtension.MOD( Math.atan2( dist2D, w ) + geoCorr, 2.0*Math.PI);
	}

	@Override
	public double getCorrection() {
		double calAngle = this.getValueAposteriori();
		double obsAngle = this.getValueApriori();
				
		// Reduziere auf Lage 1
		if (Math.abs((2.0*Math.PI - obsAngle) - calAngle) < Math.abs(obsAngle - calAngle))
			obsAngle = 2.0*Math.PI - obsAngle;

	    return obsAngle - calAngle;
	}
	
	@Override
	public double diffRefCoeff() {
		double sH = this.getCalculatedDistance2D();
		return -sH/(2.0*Constant.EARTH_RADIUS);
	}

	public FaceType getFace() {
		return this.face;
	}

	public void setFace(FaceType face) {
		this.face = face;
	}

	public void setRefractionCoefficient(RefractionCoefficient k) {
		this.refractionCoefficient = k;
		this.refractionCoefficient.setObservation( this );
	}

	public AdditionalUnknownParameter getRefractionCoefficient() {
		return this.refractionCoefficient;
	}
	
	@Override
	public int getColInJacobiMatrixFromRefCoeff() {
		return this.refractionCoefficient.getColInJacobiMatrix();
	}

	@Override
	public ObservationType getObservationType() {
		return ObservationType.ZENITH_ANGLE;
	}
}
