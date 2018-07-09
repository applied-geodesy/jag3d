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

package org.applied_geodesy.adjustment.network.observation.group;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.observation.ComponentType;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaY2D;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.RotationZ;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.point.Point;
import org.applied_geodesy.adjustment.point.Point2D;
import org.applied_geodesy.adjustment.point.group.PointGroup;

public class GNSSBaseline2DGroup extends ObservationGroup {

	private Scale scale = new Scale();
	private RotationZ rz = new RotationZ();
	private boolean hasApproxValues = false;
	private double scaleParam = 1.0, rzParam = 0.0;
	
	public GNSSBaseline2DGroup(int id) {
		this(id, DefaultUncertainty.getUncertaintyGNSSZeroPointOffset(), DefaultUncertainty.getUncertaintyGNSSSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyGNSSDistanceDependent(), Epoch.REFERENCE);
	}
	
	public GNSSBaseline2DGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
		this.scale.setObservationGroup(this);
		this.rz.setObservationGroup(this);
	}

	@Override
	public void add( Observation gnss2D ) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " Fehler, GPS-Basislinien bestehen nicht aus 2D-Beobachtungen!");
	}
	
	private void add( GNSSBaseline2D gnss2D ) {
		gnss2D.setScale(this.scale);
		gnss2D.setRotationZ(this.rz);
		super.add( gnss2D );
	}

	public void add( GNSSBaselineDeltaX2D gpsX2D, GNSSBaselineDeltaY2D gpsY2D ) {
		if (gpsX2D.getId() == gpsY2D.getId() && gpsX2D.getStartPoint().getName().equals(gpsY2D.getStartPoint().getName()) && gpsX2D.getEndPoint().getName().equals(gpsY2D.getEndPoint().getName())) {
			this.add(gpsX2D);
			this.add(gpsY2D);
			gpsX2D.addAssociatedBaselineComponent(gpsY2D);
			gpsY2D.addAssociatedBaselineComponent(gpsX2D);
		}
	}
		
	@Override
	public double getStdA(Observation observation) {
		return this.getStdA();
	}
	
	@Override
	public double getStdB(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = Math.abs(observation.getValueApriori()); // should be equal to distance for uncertainty model
		return this.getStdB() * Math.sqrt(dist / 1000.0); // [km]
	}
	
	@Override
	public double getStdC(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = Math.abs(observation.getValueApriori()); // should be equal to distance for uncertainty model
		return this.getStdC() * dist;
	}

	public Scale getScale() {
		return this.scale;
	}

	public RotationZ getRotationZ() {
		return this.rz;
	}
	
	@Override
	public int numberOfAdditionalUnknownParameter() {
		int num = 0;
		if (this.rz.isEnable())
			num++;
		if (this.scale.isEnable())
			num++;
		return num;
	}
	
	@Override
	public AdditionalUnknownParameter setApproximatedValue(AdditionalUnknownParameter param) {
		if (!this.hasApproxValues) {
			final double SQRT_EPS = Math.sqrt(Constant.EPS);
			PointGroup sourceSystem = new PointGroup(1);
			PointGroup targetSystem = new PointGroup(1);

			for (int i=0; i<this.size(); i++) {
				GNSSBaseline baseline = (GNSSBaseline)this.get(i);
				String id = String.valueOf(baseline.getId());
				
				Point trgPoint = targetSystem.get(id);
				if (trgPoint == null) {
					trgPoint = new Point2D(id,0,0);
					targetSystem.add(trgPoint);
				}
				
				if (baseline.getComponent() == ComponentType.X)
					trgPoint.setX(baseline.getValueApriori());
				else if (baseline.getComponent() == ComponentType.Y)
					trgPoint.setY(baseline.getValueApriori());
				
				Point srcPoint = sourceSystem.get(id);
				if (srcPoint == null) {	
					Point pS = baseline.getStartPoint();
					Point pE = baseline.getEndPoint();
					srcPoint = new Point2D(id,pE.getX()-pS.getX(),pE.getY()-pS.getY());
					sourceSystem.add(srcPoint);
				}
			}

			double o = 0.0, a = 0.0, oa = 0.0;
			for (int i=0; i<sourceSystem.size(); i++) {
				Point pS = sourceSystem.get(i);
				Point pT = targetSystem.get(pS.getName());
				if (pT == null)
					continue;
				
				o  += pS.getX() * pT.getY() - pS.getY() * pT.getX();
				a  += pS.getX() * pT.getX() + pS.getY() * pT.getY();
					
				oa += pS.getX()*pS.getX() + pS.getY()*pS.getY();		
			}
			
			if (oa > SQRT_EPS) {
				o /= oa;
				a /= oa;
				this.scaleParam = Math.hypot(o, a);
				this.scaleParam = this.scaleParam > SQRT_EPS ? this.scaleParam : 1.0;
				this.rzParam    = MathExtension.MOD( -Math.atan2(o, a), 2.0*Math.PI );
			}
			else 
				this.scaleParam = 1.0;

			this.hasApproxValues = true;
		}
		
		if (param == this.scale)
			param.setValue(this.scaleParam);
		else if (param == this.rz)
			param.setValue(this.rzParam);
		
		return param;
	}
}
