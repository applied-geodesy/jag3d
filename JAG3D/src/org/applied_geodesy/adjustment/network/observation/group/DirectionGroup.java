package org.applied_geodesy.adjustment.network.observation.group;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.Orientation;

public class DirectionGroup extends ObservationGroup {
	
	private Orientation orientation = new Orientation(true);

	public DirectionGroup(int id) {
		this(id, DefaultUncertainty.getUncertaintyAngleZeroPointOffset(), DefaultUncertainty.getUncertaintyAngleSquareRootDistanceDependent(), DefaultUncertainty.getUncertaintyAngleDistanceDependent(), Epoch.REFERENCE);	
	}

	public DirectionGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		super(id, sigmaA, sigmaB, sigmaC, epoch);
	}

	@Override
	public double getStdA(Observation observation) {
		return this.getStdA();
	}
	
	@Override
	public double getStdB(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS) {
			if (observation.getStartPoint().getDimension() == 3 && observation.getEndPoint().getDimension() == 3)
				dist = observation.getCalculatedAprioriDistance3D();
			else
				dist = observation.getCalculatedAprioriDistance2D();
		}
		return dist > 0 ? this.getStdB()/Math.sqrt(dist) : 0.0;
	}
	
	@Override
	public double getStdC(Observation observation) {
		double dist = observation.getDistanceForUncertaintyModel();
		if (dist < Constant.EPS) {
			if (observation.getStartPoint().getDimension() == 3 && observation.getEndPoint().getDimension() == 3)
				dist = observation.getCalculatedAprioriDistance3D();
			else
				dist = observation.getCalculatedAprioriDistance2D();
		}
		return dist > 0 ? this.getStdC()/dist : 0.0;
	}

	public Orientation getOrientation() {		
		return this.orientation;
	}

	@Override
	public void add( Observation direction ) {
		Direction obs = (Direction)direction;
		obs.setOrientation( this.orientation );
		super.add( obs );
	}

	@Override
	public int numberOfAdditionalUnknownParameter() {
		return this.orientation.isEnable() ? 1 : 0;
	}
}
