package org.applied_geodesy.adjustment.network.observation.group;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.observation.DeltaZ;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.Scale;

public class DeltaZGroup extends ObservationGroup {
	private Scale scale = new Scale();
	public DeltaZGroup(int id) {
		this(id, DefaultUncertainty.getUncertaintyLevelingZeroPointOffset(), DefaultUncertainty.getUncertaintyLevelingSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyLevelingDistanceDependent(), Epoch.REFERENCE);
	}
	
	public DeltaZGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
	}
	
	@Override
	public void add( Observation deltaZ ) {
		DeltaZ obs = (DeltaZ)deltaZ;
		obs.setScale(this.scale);
		super.add( obs );
	}

	public Scale getScale() {
		return this.scale;
	}
		
	@Override
	public double getStdA(Observation observation) {
		return this.getStdA();
	}
	
	@Override
	public double getStdB(Observation observation) {
		double distKM = observation.getDistanceForUncertaintyModel()/1000.0; // [km]
		if (distKM < Constant.EPS) // Strecke aus Koordinaten, wenn keine gegeben ist; 
			distKM = observation.getCalculatedAprioriDistance2D()/1000.0;
		return this.getStdB() * Math.sqrt(distKM);
	}
	
	@Override
	public double getStdC(Observation observation) {
		double distKM = observation.getDistanceForUncertaintyModel()/1000.0; // [km]
		if (distKM < Constant.EPS) // Strecke aus Koordinaten, wenn keine gegeben ist
			distKM = observation.getCalculatedAprioriDistance2D()/1000.0;
		return this.getStdC() * distKM;
	}

	@Override
	public int numberOfAdditionalUnknownParameter() {
		return this.scale.isEnable() ? 1 : 0;
	}
}
