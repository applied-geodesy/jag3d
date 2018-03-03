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
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.network.observation.projection.Projection;
import org.applied_geodesy.adjustment.network.observation.projection.ProjectionType;
import org.applied_geodesy.adjustment.point.Point;

public abstract class Observation {
	// ID der Beobachtung
	private final int obsID;
	
	// Start und Endpunkt der betrefflichen Beobachtung	
	private final Point startPoint,
				        endPoint;
	
	// Stand- und Zielpunkthoehe
	private double startPointHeight = 0.0,
				   endPointHeight   = 0.0,
				   observation      = 0.0;
	
	// Grenzwert fuer Null
	private final static double ZERO = Math.sqrt(Constant.EPS);
	
	//Beobachtungsgruppe (erste)
	private ObservationGroup observationGroup = null;
	
	// Projektion der Beobachtung
	private Projection projection = new Projection(ProjectionType.NONE);

	private double redundancy      =  0.0,
    			   sigma           = -1.0,
    			   sigma0          = -1.0,
    			   nabla		   =  0.0,
    			   Tprio           =  0.0,
    			   Tpost     	   =  0.0,
    			   Pprio           =  0.0,
    			   Ppost     	   =  0.0,
    			   omega           =  0.0,
    			   grzw			   =  0.0,
    			   ep              =  0.0,
    			   ef              =  0.0;
			
	// Zeile in A-Matrix der Beobachtung; -1 == nicht gesetzt
	private int rowInJacobiMatrix = -1;
		
	private boolean significant = false, useGroupUncertainty = false;
	private double distanceForUncertaintyModel = -1;	
	public Observation(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double observation, double sigma, double distanceForUncertaintyModel) {
		if (startPoint.getName().equals(endPoint.getName()))
			throw new IllegalArgumentException("Fehler, Start- und Zielpunkt sind identisch. " + 
					startPoint.getName() + " / " + endPoint.getName());
		this.obsID				= id;
		this.startPoint 		= startPoint;
		this.endPoint 			= endPoint;
		this.startPointHeight 	= startPointHeight;
		this.endPointHeight 	= endPointHeight;
		this.observation        = observation;
		this.setStd(sigma);
		
		this.startPoint.setObservation( this );
		this.endPoint.setObservation( this );
		this.distanceForUncertaintyModel = distanceForUncertaintyModel > Constant.EPS ? distanceForUncertaintyModel : -1;
	}

	public int getId() {
		return this.obsID;
	}

	public Point getStartPoint() {
		return this.startPoint;
	}

	public Point getEndPoint() {
		return this.endPoint;
	}

	public double getStartPointHeight() {
		return this.startPointHeight;
	}

	public double getEndPointHeight() {
		return this.endPointHeight;
	}

	public double getValueApriori() {
		return this.observation;
	}

	public void setValueApriori(double obsValue) {
		this.observation = obsValue;
	}

	public abstract double getValueAposteriori();
	
	public double getStd() {
		if (this.sigma>0)
			return this.sigma;
		return -1.0;
	}

	public double getStdApriori() {
		if (this.sigma0>0)
			return this.sigma0;
		return -1.0;
	}

	public void setStdApriori(double sigma0) {
		if (sigma0>0)
			this.sigma0 = sigma0;
	}

	public void setStd(double std) {
		this.sigma0 = (this.sigma0 <= 0 && std>0)?std:this.sigma0;
		this.sigma = (std>0)?std:-1;
	}	

	
	public double getCorrection() {
		return this.getValueApriori() - this.getValueAposteriori();
	}
	
	public double getCalculatedDistance2D(){
		double xs = this.startPoint.getX();
		double ys = this.startPoint.getY();
		double zs = this.startPoint.getZ();
		
		double xe = this.endPoint.getX();
		double ye = this.endPoint.getY();
		double ze = this.endPoint.getZ();
		
		double th = this.endPointHeight;
		
		double rxs = this.startPoint.getDeflectionX().getValue();
		double rys = this.startPoint.getDeflectionY().getValue();
		
		double rxe = this.endPoint.getDeflectionX().getValue();
		double rye = this.endPoint.getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = th*(crxe*crye*srys - crxe*crys*srye) + crys*(xe - xs) + srys*(ze - zs);
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		
		return Math.sqrt(u*u + v*v);
	}

	public double getCalculatedAprioriDistance2D(){
		double xs = this.startPoint.getX0();
		double ys = this.startPoint.getY0();
		double zs = this.startPoint.getZ0();
		
		double xe = this.endPoint.getX0();
		double ye = this.endPoint.getY0();
		double ze = this.endPoint.getZ0();
		
		double th = this.endPointHeight;
		
		double rxs = this.startPoint.getDeflectionX().getValue0();
		double rys = this.startPoint.getDeflectionY().getValue0();
		
		double rxe = this.endPoint.getDeflectionX().getValue0();
		double rye = this.endPoint.getDeflectionY().getValue0();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = th*(crxe*crye*srys - crxe*crys*srye) + crys*(xe - xs) + srys*(ze - zs);
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		
		return Math.sqrt(u*u + v*v);
	}

	public double getCalculatedDistance3D(){
		double xs = this.startPoint.getX();
		double ys = this.startPoint.getY();
		double zs = this.startPoint.getZ();
		
		double xe = this.endPoint.getX();
		double ye = this.endPoint.getY();
		double ze = this.endPoint.getZ();
		
		double ih = this.startPointHeight;
		double th = this.endPointHeight;
		
		double rxs = this.startPoint.getDeflectionX().getValue();
		double rys = this.startPoint.getDeflectionY().getValue();
		
		double rxe = this.endPoint.getDeflectionX().getValue();
		double rye = this.endPoint.getDeflectionY().getValue();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = th*(crxe*crye*srys - crxe*crys*srye) + crys*(xe - xs) + srys*(ze - zs);
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		return Math.sqrt(u*u + v*v + w*w);
	}

	public double getCalculatedAprioriDistance3D(){
		double xs = this.startPoint.getX0();
		double ys = this.startPoint.getY0();
		double zs = this.startPoint.getZ0();
		
		double xe = this.endPoint.getX0();
		double ye = this.endPoint.getY0();
		double ze = this.endPoint.getZ0();
		
		double ih = this.startPointHeight;
		double th = this.endPointHeight;
		
		double rxs = this.startPoint.getDeflectionX().getValue0();
		double rys = this.startPoint.getDeflectionY().getValue0();
		
		double rxe = this.endPoint.getDeflectionX().getValue0();
		double rye = this.endPoint.getDeflectionY().getValue0();
		
		double srxs = Math.sin(rxs);
		double srys = Math.sin(rys);
		double crxs = Math.cos(rxs);
		double crys = Math.cos(rys);
		
		double crxe = Math.cos(rxe);
		double crye = Math.cos(rye);
		double srye = Math.sin(rye);
		double srxe = Math.sin(rxe);
		
		double u = th*(crxe*crye*srys - crxe*crys*srye) + crys*(xe - xs) + srys*(ze - zs);
		double v = crxs*(ye - ys) - th*(crxe*crye*crys*srxs - crxs*srxe + crxe*srxs*srye*srys) - crys*srxs*(ze - zs) + srxs*srys*(xe - xs);
		double w = th*(srxe*srxs + crxe*crxs*crye*crys + crxe*crxs*srye*srys) - ih + srxs*(ye - ys) + crxs*crys*(ze - zs) - crxs*srys*(xe - xs);

		return Math.sqrt(u*u + v*v + w*w);
	}

	public void setObservationGroup(ObservationGroup group) {
		if (group.getId() >= 0 && this.observationGroup == null)
			this.observationGroup = group;
	}

	public ObservationGroup getObservationGroup() {
		return this.observationGroup;
	}

	public abstract double diffXs();

	public abstract double diffYs();

	public abstract double diffZs();

	public abstract double diffDeflectionXs();

	public abstract double diffDeflectionYs();

	public abstract double diffDeflectionXe();

	public abstract double diffDeflectionYe();

	public double diffXe() {
		return -this.diffXs();
	}

	public double diffYe() {
		return -this.diffYs();
	}

	public double diffZe() {
		return -this.diffZs();
	}

	public double diffOri() {
		return 0.0;
	}

	public double diffScale() {
	    return 0.0;
	}

	public double diffAdd() {
	    return 0.0;
	}

	public double diffRefCoeff() {
	    return 0.0;
	}

	public double diffRotX() {
	    return 0.0;
	}

	public double diffRotY() {
	    return 0.0;
	}

	public double diffRotZ() {
	    return 0.0;
	}

	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}

	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}

	public int getColInJacobiMatrixFromScale() {
		return -1;
	}

	public int getColInJacobiMatrixFromAdd() {
		return -1;
	}

	public int getColInJacobiMatrixFromRefCoeff() {
		return -1;
	}

	public int getColInJacobiMatrixFromOrientation() {
		return -1;
	}

	public int getColInJacobiMatrixFromRotationX() {
		return -1;
	}

	public int getColInJacobiMatrixFromRotationY() {
		return -1;
	}

	public int getColInJacobiMatrixFromRotationZ() {
		return -1;
	}

	public double getRedundancy() {
		return this.redundancy;
	}

	public void setRedundancy(double r) {
		if (0<=r && r<=1)
			this.redundancy = r;
	}

	public void setTestAndProbabilityValues(double tPrio, double tPost, double pPrio, double pPost) {
		this.Tprio = tPrio;
		this.Tpost = tPost;
		
		this.Pprio = pPrio;
		this.Ppost = pPost;
	}

	public double getTprio() {
		return this.Tprio < ZERO ? 0.0 : this.Tprio;
	}

	public double getTpost() {
		return this.Tpost < ZERO ? 0.0 : this.Tpost;
	}

	public double getPprio() {
		return this.Pprio;
	}

	public double getPpost() {
		return this.Ppost;
	}

	public double getGrossError() {
		return this.nabla;
	}

	public void setGrossError(double nabla) {
		this.nabla = nabla;
	}

	public void setInfluenceOnPointPosition(double ep) {
		this.ep = ep;
	}

	public double getInfluenceOnPointPosition() {
		return this.ep;
	}

	public void setInfluenceOnNetworkDistortion(double ef) {
		this.ef = ef;
	}

	public double getInfluenceOnNetworkDistortion() {
		return this.ef;
	}

	public void setOmega(double omega) {
		this.omega = omega;
	}

	public double getOmega() {
		return this.omega;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}	

	public boolean isSignificant() {
		return this.significant;
	}

	public boolean useGroupUncertainty() {
		return this.useGroupUncertainty;
	}

	public void useGroupUncertainty(boolean useGroupUncertainty) {
		this.useGroupUncertainty = useGroupUncertainty;
	}

	public double getMinimalDetectableBias() {
		return this.grzw;
	}

	public void setMinimalDetectableBias(double grzw) {
		this.grzw = grzw;
	}

	public abstract ObservationType getObservationType();

	public Projection getProjectionScheme() {
		return this.projection;
	}

	public void setProjectionScheme(Projection projection) {
		this.projection = projection;
	}

	public double getDistanceForUncertaintyModel() {
		return this.distanceForUncertaintyModel > Constant.EPS ? this.distanceForUncertaintyModel : 0.0;
	}

	public void setDistanceForUncertaintyModel(double distanceForUncertaintyModel) {
		this.distanceForUncertaintyModel = distanceForUncertaintyModel > Constant.EPS ? distanceForUncertaintyModel : -1;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+": " +this.startPoint.getName()+" - " + this.endPoint.getName() + ": " + this.observation;
	}
					
}
