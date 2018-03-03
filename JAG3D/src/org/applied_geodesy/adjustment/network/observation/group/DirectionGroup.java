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
