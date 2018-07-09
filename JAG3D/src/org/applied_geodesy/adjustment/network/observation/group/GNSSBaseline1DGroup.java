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
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaZ1D;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.RotationX;
import org.applied_geodesy.adjustment.network.parameter.RotationY;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.point.Point;
import org.applied_geodesy.adjustment.point.Point1D;
import org.applied_geodesy.adjustment.point.Point3D;
import org.applied_geodesy.adjustment.point.group.PointGroup;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmPackMatrix;


public class GNSSBaseline1DGroup extends ObservationGroup {

	private Scale scale = new Scale();
	private RotationX rx = new RotationX();
	private RotationY ry = new RotationY();
	
	private boolean hasApproxValues = false;
	private double scaleParam = 1.0, rxParam = 0.0, ryParam = 0.0;
	
	public GNSSBaseline1DGroup(int id) {
		this(id, DefaultUncertainty.getUncertaintyGNSSZeroPointOffset(), DefaultUncertainty.getUncertaintyGNSSSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyGNSSDistanceDependent(), Epoch.REFERENCE);
	}
	
	public GNSSBaseline1DGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
		this.scale.setObservationGroup(this);
		this.rx.setObservationGroup(this);
		this.ry.setObservationGroup(this);
	}

	@Override
	public void add( Observation gnss1D ) {
		GNSSBaselineDeltaZ1D gnssZ1D = (GNSSBaselineDeltaZ1D)gnss1D;
		this.add(gnssZ1D);
	}

	public void add( GNSSBaselineDeltaZ1D gnssZ1D ) {
		gnssZ1D.setScale(this.scale);
		gnssZ1D.setRotationX(this.rx);
		gnssZ1D.setRotationY(this.ry);
		super.add( gnssZ1D );
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

	public RotationX getRotationX() {
		return this.rx;
	}

	public RotationY getRotationY() {
		return this.ry;
	}
	
	@Override
	public int numberOfAdditionalUnknownParameter() {
		int num = 0;
		if (this.rx.isEnable())
			num++;
		if (this.ry.isEnable())
			num++;
		if (this.scale.isEnable())
			num++;
		return num;
	}
	
	// fz = m*(  Math.sin(ry)*dX - Math.sin(rx)*Math.cos(ry)*dY + Math.cos(rx)*Math.cos(ry)*dZ)
	// fz = m*(  A*dX + B*dY + C*dZ )
	@Override
	public AdditionalUnknownParameter setApproximatedValue(AdditionalUnknownParameter param) {
		if (!this.hasApproxValues) {			
			PointGroup sourceSystem = new PointGroup(1);
			PointGroup targetSystem = new PointGroup(1);

			for (int i=0; i<this.size(); i++) {
				GNSSBaseline baseline = (GNSSBaseline)this.get(i);
				String id = String.valueOf(baseline.getId());
				
				Point trgPoint = targetSystem.get(id);
				if (trgPoint == null) {
					trgPoint = new Point1D(id,0);
					targetSystem.add(trgPoint);
				}
				
				if (baseline.getComponent() == ComponentType.Z)
					trgPoint.setZ(baseline.getValueApriori() + baseline.getEndPointHeight() - baseline.getStartPointHeight());
				
				Point srcPoint = sourceSystem.get(id);
				if (srcPoint == null) {	
					Point pS = baseline.getStartPoint();
					Point pE = baseline.getEndPoint();
					srcPoint = new Point3D(id,pE.getX()-pS.getX(),pE.getY()-pS.getY(),pE.getZ()-pS.getZ());
					sourceSystem.add(srcPoint);
				}
			}
			UpperSymmPackMatrix F = new UpperSymmPackMatrix(3);
			DenseVector f = new DenseVector(3);
			DenseVector x = new DenseVector(3);
			for (int k=0; k<sourceSystem.size(); k++) {
				Point pS = sourceSystem.get(k);
				Point pT = targetSystem.get(pS.getName());
				if (pT == null)
					continue;
				
				double dx = pS.getX(); 
				double dy = pS.getY();
				double dz = pS.getZ();

				F.add(0, 0, dx*dx);
				F.add(0, 1, dx*dy);
				F.add(0, 2, dx*dz);
				
				F.add(1, 1, dy*dy);
				F.add(1, 2, dy*dz);
				
				F.add(2, 2, dz*dz);
				
				f.add(0, dx*pT.getZ());
				f.add(1, dy*pT.getZ());
				f.add(2, dz*pT.getZ());
			}
			
			try {
				MathExtension.pinv(F, -1).mult(f, x);
				this.rxParam    = Math.atan2(x.get(1), x.get(2));
				this.ryParam    = Math.atan2(x.get(0) * Math.cos(this.rxParam), x.get(2));
				this.scaleParam = Math.cos(rxParam)*Math.cos(ryParam) > Math.sqrt(Constant.EPS) ? x.get(2)/Math.cos(rxParam)*Math.cos(ryParam) : 1.0;
			} catch (NotConvergedException e) {
				this.rxParam    = 0.0;
				this.ryParam    = 0.0;
				this.scaleParam = 1.0;
			}

			this.hasApproxValues = true;
		}
		
		if (param == this.scale)
			param.setValue(this.scaleParam);
		else if (param == this.rx)
			param.setValue(this.rxParam);
		else if (param == this.ry)
			param.setValue(this.ryParam);
		
		return param;
	}
}
