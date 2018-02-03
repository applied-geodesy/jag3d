package org.applied_geodesy.adjustment.network.observation.group;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.ZenithAngle;
import org.applied_geodesy.adjustment.network.parameter.RefractionCoefficient;

public class ZenithAngleGroup extends ObservationGroup {
	private RefractionCoefficient refractionCoefficient = new RefractionCoefficient();

	public ZenithAngleGroup(int id) {
		super(id, DefaultUncertainty.getUncertaintyAngleZeroPointOffset(), DefaultUncertainty.getUncertaintyAngleSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyAngleDistanceDependent(), Epoch.REFERENCE);
	}
	
	public ZenithAngleGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
	}
	
	@Override
	public double getStdA(Observation observation) {
		return this.getStdA();
	}
	
	@Override
	public double getStdB(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = observation.getCalculatedAprioriDistance3D();
		return dist > 0 ? this.getStdB()/Math.sqrt(dist) : 0.0;
	}
	
	@Override
	public double getStdC(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = observation.getCalculatedAprioriDistance3D();
		return dist > 0 ? this.getStdC()/dist : 0.0;
	}
 
	@Override
	public void add( Observation zenithangle ) {
		ZenithAngle obs = (ZenithAngle)zenithangle;
		obs.setRefractionCoefficient( this.refractionCoefficient );
		super.add( obs );
	}

	public RefractionCoefficient getRefractionCoefficient() {
		return this.refractionCoefficient;
	}
	
	@Override
	public int numberOfAdditionalUnknownParameter() {
		return this.refractionCoefficient.isEnable() ? 1 : 0;
	}
}
