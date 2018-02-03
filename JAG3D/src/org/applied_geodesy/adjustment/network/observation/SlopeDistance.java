package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.network.parameter.ZeroPointOffset;
import org.applied_geodesy.adjustment.point.Point;

public class SlopeDistance extends Observation {
	
	private Scale scale = new Scale();
	private ZeroPointOffset add = new ZeroPointOffset();

	public SlopeDistance(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double measuringElement, double sigma, double distanceForUncertaintyModel) {
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

		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;
		return -(xe - xs + ih*crxs*srys - th*crxe*srye) / dist3D;
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;
		return -(ye - ys - ih*srxs + th*srxe) / dist3D;
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;
		return -(ze - zs - ih*crxs*crys + th*crxe*crye) / dist3D;
	}
	
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;		
		return -ih*(crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs)) / dist3D;
	}
	
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;		
		return ih*crxs*(crys*(xe - xs) + srys*(ze - zs) - th*Math.sin(rye - rys)*crxe) / dist3D;
	}
	
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;
		return th*(crxe*(ye - ys) + ih*(crxs*crye*crys*srxe - crxe*srxs + crxs*srxe*srye*srys) - crye*srxe*(ze - zs) + srxe*srye*(xe - xs)) / dist3D;
	}
	
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
		
		double scale = this.scale.getValue();
		double dist3D = scale * Math.sqrt(u*u + v*v + w*w);
		
		if (dist3D == 0.0)
			return 0.0;
		return -th*crxe*(crye*(xe - xs) + srye*(ze - zs) - ih*Math.sin(rye - rys)*crxs) / dist3D;
	}

	@Override
	public double diffAdd() {
	    return -1.0/this.scale.getValue();
	}

	@Override
	public double diffScale() {
		double sR    = this.getCalculatedDistance3D();
	    double add   = this.add.getValue();
	    double scale = this.scale.getValue();
	    return (-sR + add) / Math.pow(scale, 2);
	}

	@Override
	public double getValueAposteriori() {
		double scale = this.scale.getValue();
		double add   = this.add.getValue();
		double sR    = this.getCalculatedDistance3D();
	    return 1.0/scale * (sR - add);
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
		return ObservationType.SLOPE_DISTANCE;
	}
}
