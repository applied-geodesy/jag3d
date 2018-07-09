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
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaY3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaZ3D;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.RotationX;
import org.applied_geodesy.adjustment.network.parameter.RotationY;
import org.applied_geodesy.adjustment.network.parameter.RotationZ;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.point.Point;
import org.applied_geodesy.adjustment.point.Point3D;
import org.applied_geodesy.adjustment.point.group.PointGroup;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;

public class GNSSBaseline3DGroup extends ObservationGroup {

	private Scale scale = new Scale();
	private RotationX rx = new RotationX();
	private RotationY ry = new RotationY();
	private RotationZ rz = new RotationZ();
	
	private boolean hasApproxValues = false;
	private double scaleParam = 1.0, rxParam = 0.0, ryParam = 0.0, rzParam = 0.0;
	
	public GNSSBaseline3DGroup(int id) {
		this(id, DefaultUncertainty.getUncertaintyGNSSZeroPointOffset(), DefaultUncertainty.getUncertaintyGNSSSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyGNSSDistanceDependent(), Epoch.REFERENCE);
	}
	
	public GNSSBaseline3DGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
		this.scale.setObservationGroup(this);
		this.rx.setObservationGroup(this);
		this.ry.setObservationGroup(this);
		this.rz.setObservationGroup(this);
	}

	@Override
	public void add( Observation gnss3D ) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " Fehler, GNSS-Basislinien bestehen nicht aus 3D-Beobachtungen!");
	}
	
	private void add( GNSSBaseline3D gnss3D ) {
		gnss3D.setScale(this.scale);
		gnss3D.setRotationX(this.rx);
		gnss3D.setRotationY(this.ry);
		gnss3D.setRotationZ(this.rz);
		super.add( gnss3D );
	}

	public void add( GNSSBaselineDeltaX3D gnssX3D, GNSSBaselineDeltaY3D gnssY3D, GNSSBaselineDeltaZ3D gnssZ3D ) {
		if (gnssX3D.getId() == gnssY3D.getId() && gnssX3D.getId() == gnssZ3D.getId() && 
				gnssX3D.getStartPoint().getName().equals(gnssY3D.getStartPoint().getName()) && 
				gnssX3D.getEndPoint().getName().equals(gnssY3D.getEndPoint().getName()) &&
				gnssX3D.getStartPoint().getName().equals(gnssZ3D.getStartPoint().getName()) && 
				gnssX3D.getEndPoint().getName().equals(gnssZ3D.getEndPoint().getName())) {
			
			this.add(gnssX3D);
			this.add(gnssY3D);
			this.add(gnssZ3D);
			
			gnssX3D.addAssociatedBaselineComponent(gnssY3D);
			gnssX3D.addAssociatedBaselineComponent(gnssZ3D);
			
			gnssY3D.addAssociatedBaselineComponent(gnssX3D);
			gnssY3D.addAssociatedBaselineComponent(gnssZ3D);
			
			gnssZ3D.addAssociatedBaselineComponent(gnssX3D);
			gnssZ3D.addAssociatedBaselineComponent(gnssY3D);
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
		return this.getStdB() * Math.sqrt(dist / 1000.0);// [km]
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

	public RotationZ getRotationZ() {
		return this.rz;
	}
	
	@Override
	public int numberOfAdditionalUnknownParameter() {
		int num = 0;
		if (this.rx.isEnable())
			num++;
		if (this.ry.isEnable())
			num++;
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
					trgPoint = new Point3D(id,0,0,0);
					targetSystem.add(trgPoint);
				}
				
				if (baseline.getComponent() == ComponentType.X)
					trgPoint.setX(baseline.getValueApriori());
				else if (baseline.getComponent() == ComponentType.Y)
					trgPoint.setY(baseline.getValueApriori());
				else if (baseline.getComponent() == ComponentType.Z)
					trgPoint.setZ(baseline.getValueApriori() + baseline.getEndPointHeight() - baseline.getStartPointHeight());
				
				Point srcPoint = sourceSystem.get(id);
				if (srcPoint == null) {	
					Point pS = baseline.getStartPoint();
					Point pE = baseline.getEndPoint();
					srcPoint = new Point3D(id,pE.getX()-pS.getX(),pE.getY()-pS.getY(),pE.getZ()-pS.getZ());
					sourceSystem.add(srcPoint);
				}
			}
			int nop = sourceSystem.size();
			DenseMatrix S = new DenseMatrix(3,3);
			double q[] = new double[4];
			for (int k=0; k<nop; k++) {
				Point pS = sourceSystem.get(k);
				Point pT = targetSystem.get(pS.getName());
				if (pT == null)
					continue;
				
				for (int i=0; i<3; i++) {
					for (int j=0; j<3; j++) {
						double a=0, b=0;
						
						if (i==0)
							a = pS.getX(); 
						else if (i==1)
							a = pS.getY();
						else 
							a = pS.getZ();
						
						if (j==0)
							b = pT.getX(); 
						else if (j==1)
							b = pT.getY();
						else 
							b = pT.getZ();
						
						S.set(i,j, S.get(i,j) + a * b);
					}
				}			
			}
			DenseMatrix N = new DenseMatrix(4,4);
			N.set(0,0, S.get(0,0)+S.get(1,1)+S.get(2,2));
			N.set(0,1, S.get(1,2)-S.get(2,1));
			N.set(0,2, S.get(2,0)-S.get(0,2));
			N.set(0,3, S.get(0,1)-S.get(1,0));
			
			N.set(1,0, S.get(1,2)-S.get(2,1));
			N.set(1,1, S.get(0,0)-S.get(1,1)-S.get(2,2));
			N.set(1,2, S.get(0,1)+S.get(1,0));
			N.set(1,3, S.get(2,0)+S.get(0,2));
			
			N.set(2,0, S.get(2,0)-S.get(0,2));
			N.set(2,1, S.get(0,1)+S.get(1,0));
			N.set(2,2,-S.get(0,0)+S.get(1,1)-S.get(2,2));
			N.set(2,3, S.get(1,2)+S.get(2,1));
			
			N.set(3,0, S.get(0,1)-S.get(1,0));
			N.set(3,1, S.get(2,0)+S.get(0,2));
			N.set(3,2, S.get(1,2)+S.get(2,1));
			N.set(3,3,-S.get(0,0)-S.get(1,1)+S.get(2,2));
			
			EVD evd = new EVD(4);
			try {
				evd.factor(N);
				
				if (evd.hasRightEigenvectors()) {
					Matrix eigVec = evd.getRightEigenvectors();
					double eigVal[] = evd.getRealEigenvalues();
					
					int indexMaxEigVal = 0;
					double maxEigVal = eigVal[indexMaxEigVal];
					for (int i=indexMaxEigVal+1; i<eigVal.length; i++) {
						if (maxEigVal < eigVal[i]) {
							maxEigVal = eigVal[i];
							indexMaxEigVal = i;
						}
					}
					// Setze berechnetes Quaternion ein
					for (int i=0; i<eigVal.length; i++) {
						q[i] = eigVec.get(i, indexMaxEigVal);
					}
				}
				
			} catch (NotConvergedException e) {
				e.printStackTrace();
			}
			
			// Bestimme Massstab
			double m1 = 0, m2 = 0;
			double q0 = q[0];
			double q1 = q[1];
			double q2 = q[2];
			double q3 = q[3];
			
			// Bestimme Drehung
			double r13 = 2.0*(q1*q3+q0*q2);
			double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
			double r12 = 2.0*(q1*q2-q0*q3);
			double r23 = 2.0*(q2*q3-q0*q1);
			double r21 = 2.0*(q1*q2+q0*q3);
			double r22 = 2.0*q0*q0-1.0+2.0*q2*q2;
			double r31 = 2.0*(q1*q3-q0*q2);
			double r32 = 2.0*(q2*q3+q0*q1);
			double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;			
			    
			for (int k=0; k<nop; k++) {
				Point pS = sourceSystem.get(k);
				Point pT = targetSystem.get(pS.getName());
				if (pT == null)
					continue;
			 
				m1 += pT.getX()*(r11*pS.getX() + r12*pS.getY() + r13*pS.getZ());
				m1 += pT.getY()*(r21*pS.getX() + r22*pS.getY() + r23*pS.getZ());
				m1 += pT.getZ()*(r31*pS.getX() + r32*pS.getY() + r33*pS.getZ());
								
				m2 += pS.getX()*pS.getX();
				m2 += pS.getY()*pS.getY();
				m2 += pS.getZ()*pS.getZ();
			}
							
			if (Math.abs(m1) > SQRT_EPS && Math.abs(m2) > SQRT_EPS)
				this.scaleParam = Math.abs(m1/m2);
			else
				this.scaleParam = 1.0;

			this.rxParam = MathExtension.MOD( Math.atan2(-r32, r33), 2.0*Math.PI );
			this.ryParam = MathExtension.MOD( Math.asin(r31)      , 2.0*Math.PI );
			this.rzParam = MathExtension.MOD( Math.atan2(-r21, r11), 2.0*Math.PI );
			this.hasApproxValues = true;
		}
		
		if (param == this.scale)
			param.setValue(this.scaleParam);
		else if (param == this.rx)
			param.setValue(this.rxParam);
		else if (param == this.ry)
			param.setValue(this.ryParam);
		else if (param == this.rz)
			param.setValue(this.rzParam);
		
		return param;
	}
}
