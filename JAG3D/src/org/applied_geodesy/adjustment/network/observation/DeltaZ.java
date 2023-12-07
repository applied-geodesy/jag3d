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
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.network.point.Point;

public class DeltaZ extends Observation {
	private Scale scale = new Scale();

	public DeltaZ(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double observation, double sigma, double distanceForUncertaintyModel) {
		super(id, startPoint, endPoint, startPointHeight, endPointHeight, observation, sigma, distanceForUncertaintyModel);
	}
		
	@Override
	public double diffXs() {
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();

		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double scale = this.scale.getValue();

		return (crxs*srys)/scale;
	}

	@Override
	public double diffYs() {	
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();

		double srxs = Math.sin(rxs);
		double scale = this.scale.getValue();

		return -srxs/scale;
	}

	@Override
	public double diffZs() {
		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();

		double crys = Math.cos(rys);
		double crxs = Math.cos(rxs);
		double scale = this.scale.getValue();

		return -(crxs*crys)/scale;
	}


	@Override
	public double diffVerticalDeflectionXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();

		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();

		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);

		double scale = this.scale.getValue();

		return -(ys*crxs - zs*crys*srxs + xs*srxs*srys)/scale;
	}

	@Override
	public double diffVerticalDeflectionYs() {
		double xs = this.getStartPoint().getX();
		double zs = this.getStartPoint().getZ();

		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();

		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);

		double scale = this.scale.getValue();

		return (crxs*(xs*crys + zs*srys))/scale;
	}
	
	@Override
	public double diffXe() {
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();

		double srye = Math.sin(rye);
		double crxe = Math.cos(rxe);
		double scale = this.scale.getValue();

		return -(crxe*srye)/scale;
	}
	
	@Override
	public double diffYe() {	
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();

		double srxe = Math.sin(rxe);
		double scale = this.scale.getValue();

		return srxe/scale;
	}
	
	@Override
	public double diffZe() {
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();

		double crye = Math.cos(rye);
		double crxe = Math.cos(rxe);
		double scale = this.scale.getValue();

		return (crxe*crye)/scale;
	}

	@Override
	public double diffVerticalDeflectionXe() {
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();

		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();

		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);

		double scale = this.scale.getValue();

		return (ye*crxe - ze*crye*srxe + xe*srxe*srye)/scale;
	}

	@Override
	public double diffVerticalDeflectionYe() {
		double xe = this.getEndPoint().getX();
		double ze = this.getEndPoint().getZ();

		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();

		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		
		double scale = this.scale.getValue();

		return -(crxe*(xe*crye + ze*srye))/scale;
	}


	@Override
	public double diffScale() {
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

		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);

		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double ws = -xs*crxs*srys + ys*srxs + zs*crxs*crys + ih;
		double we = -xe*crxe*srye + ye*srxe + ze*crxe*crye + th;
		
		double dN = 0;
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			double hs = this.getStartPoint().getSphericalDeflectionParameter().getFrameIntersectionHeight();
			double he = this.getEndPoint().getSphericalDeflectionParameter().getFrameIntersectionHeight();
			dN = he - hs;
		}

		double scale = this.scale.getValue();
		double dh = we - ws + dN;
		return -dh/(scale*scale);
	}

	public AdditionalUnknownParameter getScale() {
		return this.scale;
	}	

	public void setScale(Scale newScale) {
		this.scale = newScale;
		this.scale.setObservation( this );
	}
	
	@Override
	public int getColInJacobiMatrixFromScale() {
		return this.scale.getColInJacobiMatrix();
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

		double rxs = this.getStartPoint().getVerticalDeflectionX().getValue();
		double rys = this.getStartPoint().getVerticalDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getVerticalDeflectionX().getValue();
		double rye = this.getEndPoint().getVerticalDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double ws = -xs*crxs*srys + ys*srxs + zs*crxs*crys + ih;
		double we = -xe*crxe*srye + ye*srxe + ze*crxe*crye + th;
		
		double dN = 0;
		if (this.getReductions().getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			double hs = this.getStartPoint().getSphericalDeflectionParameter().getFrameIntersectionHeight();
			double he = this.getEndPoint().getSphericalDeflectionParameter().getFrameIntersectionHeight();
			dN = he - hs;
		}

		double scale = this.scale.getValue();
//		double dh = ze - zs + dN;
		double dh = we - ws + dN;
		
		return dh / scale;
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.LEVELING;
	}
}
