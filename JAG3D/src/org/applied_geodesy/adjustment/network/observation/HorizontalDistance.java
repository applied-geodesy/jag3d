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

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.observation.reduction.ProjectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.Reduction;
import org.applied_geodesy.adjustment.network.observation.reduction.ReductionTaskType;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.network.parameter.ZeroPointOffset;
import org.applied_geodesy.adjustment.network.point.Point;

public class HorizontalDistance extends Observation {
	
	private Scale scale = new Scale();
	private ZeroPointOffset add = new ZeroPointOffset();

	public HorizontalDistance(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double measuringElement, double sigma, double distanceForUncertaintyModel) {
		super(id, startPoint, endPoint, startPointHeight, endPointHeight, measuringElement, sigma, distanceForUncertaintyModel);
	}

	@Override
	public double diffXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();

		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);

		if (dist2D == 0.0)
			return 0.0;
		return -(crys*u + srxs*srys*v)/dist2D;
	}

	@Override
	public double diffYs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return -(crxs*v)/dist2D;
	}

	@Override
	public double diffZs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return -(srys*u - crys*srxs*v)/dist2D;
	}
	
	@Override
	public double diffVerticalDeflectionXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double ih = this.getStartPointHeight();
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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
		
		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return -(v*(w+ih))/dist2D;
	}
	
	@Override
	public double diffVerticalDeflectionYs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return (u*(crys*(ze - zs) - srys*(xe - xs) + th*Math.cos(rye - rys)*crxe) + v*srxs*(crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe))/dist2D;
	}
	
	@Override
	public double diffVerticalDeflectionXe() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return (th*v*(crxe*crxs + crye*crys*srxe*srxs + srxe*srxs*srye*srys) + th*u*Math.sin(rye - rys)*srxe)/dist2D;
	}
	
	@Override
	public double diffVerticalDeflectionYe() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();
		
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			rxs += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rys += this.getStartPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();

			rxe += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionX();
			rye += this.getEndPoint().getSphericalDeflectionParameter().getSphericalDeflectionY();
		}
		
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

		double scale = this.scale.getValue();
		double dist2D = scale * Math.hypot(u, v);
		
		if (dist2D == 0.0)
			return 0.0;
		return -th*crxe*(u*Math.cos(rye - rys) - v*Math.sin(rye - rys)*srxs)/dist2D;
	}

	@Override
	public double diffAdd() {
	    return -1.0/this.scale.getValue();
	}

	@Override
	public double diffScale() {
	    double add   = this.add.getValue();
	    double scale = this.scale.getValue();
	    double sH    = this.getCalculatedDistance2D();
		return (-sH + add) / Math.pow(scale, 2);
	}

	@Override
	public double getValueAposteriori() {
		double scale = this.scale.getValue();
		double add   = this.add.getValue();
		double sH    = this.getCalculatedDistance2D();
	    return 1.0/scale * (sH - add);
	}

	@Override
	public double getObservationalError() {
		double calDist = this.getValueAposteriori();
		double obsDist = this.getValueApriori();
		
		Reduction reductions = this.getReductions();
		double R = reductions.getEarthRadius();
		
		// Hoehenreduktion - Rueeger 1996, S. 99, Gl. 7.56
		if (reductions.applyReductionTask(ReductionTaskType.HEIGHT)) {
	    	double h0 = reductions.getPrincipalPoint().getHeight();
	    	obsDist = obsDist * R/(R+h0);
	    }
		
		// Erdkruemmungsreduktion - Rueeger 1996, S. 90, Gl. 7.29
		if (reductions.applyReductionTask(ReductionTaskType.EARTH_CURVATURE)) {
			obsDist = 2.0 * R * Math.asin(0.5 * obsDist / R);
		}
		
		// Streckenreduktion bzgl. Projektion
	    if ((reductions.getProjectionType() == ProjectionType.GAUSS_KRUEGER || reductions.getProjectionType() == ProjectionType.UTM) && reductions.applyReductionTask(ReductionTaskType.DISTANCE)) {
	    	double m0 = reductions.getProjectionType() == ProjectionType.UTM ? 0.9996 : 1.0;
	    	double yS = this.getStartPoint().getY();
	    	double yE = this.getEndPoint().getY();

	    	// Reduziere Rechtswert
	    	yS = ((yS/1000000.0)%1)*1000000.0-500000.0;
	    	yE = ((yE/1000000.0)%1)*1000000.0-500000.0;

	    	double k = (yS*yS + yS*yE + yE*yE)/6.0/R/R;
	    	obsDist = m0 * (1.0 + k) * obsDist;
	    }
		return obsDist - calDist;
	}

	public void setScale(Scale newScale) {
		this.scale = newScale;
		this.scale.setObservation( this );
	}

	public AdditionalUnknownParameter getScale() {
		return this.scale;
	}

	public void setZeroPointOffset(ZeroPointOffset newAdd) {
		this.add = newAdd;
		this.add.setObservation( this );
	}

	public AdditionalUnknownParameter getZeroPointOffset() {
		return this.add;
	}
	
	@Override
	public int getColInJacobiMatrixFromScale() {
		return this.scale.getColInJacobiMatrix();
	}
	
	@Override
	public int getColInJacobiMatrixFromAdd() {
		return this.add.getColInJacobiMatrix();
	}

	@Override
	public ObservationType getObservationType() {
		return ObservationType.HORIZONTAL_DISTANCE;
	}
}
