package org.applied_geodesy.adjustment.network.observation.group;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.observation.HorizontalDistance;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.network.parameter.ZeroPointOffset;

public class HorizontalDistanceGroup extends ObservationGroup {
	
	private Scale scale = new Scale();
	private ZeroPointOffset add = new ZeroPointOffset();
	
	public HorizontalDistanceGroup(int id) {
		super(id, DefaultUncertainty.getUncertaintyDistanceZeroPointOffset(), DefaultUncertainty.getUncertaintyDistanceSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyDistanceDistanceDependent(), Epoch.REFERENCE);
	}
	
	public HorizontalDistanceGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
	}

	@Override
	public void add( Observation distance2d ) {
		HorizontalDistance obs = (HorizontalDistance)distance2d;
		obs.setScale(this.scale);
		obs.setZeroPointOffset(this.add);
		super.add( obs );
	}
		
	@Override
	public double getStdA(Observation observation) {
		return this.getStdA();
	}
	
	@Override
	public double getStdB(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = observation.getCalculatedAprioriDistance2D();
		return this.getStdB() * Math.sqrt(dist);
	}
	
	@Override
	public double getStdC(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS)
			dist = observation.getCalculatedAprioriDistance2D();
		return this.getStdC() * dist;
	}

	public Scale getScale() {
		return this.scale;
	}
	
	public ZeroPointOffset getZeroPointOffset() {
		return this.add;
	}
		
	@Override
	public int numberOfAdditionalUnknownParameter() {
		int num = 0;
		if (this.add.isEnable())
			num++;
		if (this.scale.isEnable())
			num++;
		return num;
	}
}
