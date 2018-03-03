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
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.point.Point;

public class DeltaZ extends Observation {
	private Scale scale = new Scale();
	
	public DeltaZ(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double observation, double sigma, double distanceForUncertaintyModel) {
		super(id, startPoint, endPoint, startPointHeight, endPointHeight, observation, sigma, distanceForUncertaintyModel);
	}

	@Override
	public double diffXs() {
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double scale = this.scale.getValue();
		
		return (crxs*srys)/scale;
	}

	@Override
	public double diffYs() {	
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double srxs = Math.sin(rxs);
		double scale = this.scale.getValue();
		
		return -srxs/scale;
	}
	
	@Override
	public double diffZs() {
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double crys = Math.cos(rys);
		double crxs = Math.cos(rxs);
		double scale = this.scale.getValue();
		
		return -(crxs*crys)/scale;
	}
	
	@Override
	public double diffDeflectionXs() {
		double xs = this.getStartPoint().getX();
		double ys = this.getStartPoint().getY();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ye = this.getEndPoint().getY();
		double ze = this.getEndPoint().getZ();

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
		
		double scale = this.scale.getValue();
		
		return (crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs))/scale;
	}
	
	@Override
	public double diffDeflectionYs() {
		double xs = this.getStartPoint().getX();
		double zs = this.getStartPoint().getZ();
		
		double xe = this.getEndPoint().getX();
		double ze = this.getEndPoint().getZ();

		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();
		
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		
		double scale = this.scale.getValue();
		
		return -(th*(crxe*crxs*crye*srys - crxe*crxs*crys*srye) + crxs*crys*(xe - xs) + crxs*srys*(ze - zs))/scale;
	}

	@Override
	public double diffDeflectionXe() {
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
		
		double scale = this.scale.getValue();
		
		return -(th*(crxs*crye*crys*srxe - crxe*srxs + crxs*srxe*srye*srys))/scale;
	}
	
	@Override
	public double diffDeflectionYe() {
		double th = this.getEndPointHeight();
		
		double rxs = this.getStartPoint().getDeflectionX().getValue();
		double rys = this.getStartPoint().getDeflectionY().getValue();
		
		double rxe = this.getEndPoint().getDeflectionX().getValue();
		double rye = this.getEndPoint().getDeflectionY().getValue();

		double crxs = Math.cos(rxs);
		double crxe = Math.cos(rxe);
		
		double scale = this.scale.getValue();
		
		return -(th*Math.sin(rye - rys)*crxe*crxs)/scale;
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
		
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);
		double scale = this.scale.getValue();
		return -w/(scale*scale);
	}

	public AdditionalUnknownParameter getScale() {
		return this.scale;
	}	

	public void setScale(Scale newScale) {
		this.scale = newScale;
		this.scale.setObservation( this );
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
		
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		return w/this.scale.getValue();
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.LEVELING;
	}
}
