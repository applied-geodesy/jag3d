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

package org.applied_geodesy.adjustment.transformation.interpolation;

import java.util.Collection;

import org.applied_geodesy.adjustment.transformation.TransformationAdjustment.Interrupt;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;


public class InverseDistanceWeighting extends Interpolation {

	private ObjectProperty<Double> exponent  = new SimpleObjectProperty<Double>(this, "exponent",  2.0);
	private ObjectProperty<Double> smoothing = new SimpleObjectProperty<Double>(this, "smoothing", 0.0);
	
	public InverseDistanceWeighting() {
		super(InterpolationType.INVERSE_DISTANCE_WEIGHTING);
	}
	
	public void setSmoothing(double smoothing) {
		this.smoothing.set(smoothing);
	}
	
	public double getSmoothing() {
		return this.smoothing.get();
	}
	
	public ObjectProperty<Double> smoothingProperty() {
		return this.smoothing;
	}
	
	public void setExponent(double exponent) {
		this.exponent.set(exponent);
	}
	
	public double getExponent() {
		return this.exponent.get();
	}
	
	public ObjectProperty<Double> exponentProperty() {
		return this.exponent;
	}
	
	@Override
	public void interpolate(Collection<EstimatedFramePosition> estimatedFramePositions, Collection<FramePositionPair> framePositionPairs, Interrupt interrupt) {
		double k = this.getExponent();
		double m = this.getSmoothing();

		for (FramePositionPair framePositionPair : framePositionPairs) {
			if (interrupt.isInterrupted())
				return;
			
			if (!framePositionPair.isEnable())
				continue;
			
			double sumWeight = 0.0;
			double ux = 0.0;
			double uy = 0.0;
			double uz = 0.0;
			
			EstimatedFramePosition targetSystemPosition = framePositionPair.getTargetSystemPosition();
			
			for (EstimatedFramePosition estimatedFramePosition : estimatedFramePositions) {

				double dx = targetSystemPosition.getX() - estimatedFramePosition.getX0();
				double dy = targetSystemPosition.getY() - estimatedFramePosition.getY0();
				double dz = targetSystemPosition.getZ() - estimatedFramePosition.getZ0();
				
				double weight = Math.sqrt(dx*dx + dy*dy + dz*dz);
				
				weight = (weight > SQRT_EPS) ? Math.pow(weight + m, -k) : 1.0/SQRT_EPS;
				sumWeight += weight;
				
				ux += weight * estimatedFramePosition.getResidualX();
				uy += weight * estimatedFramePosition.getResidualY();
				uz += weight * estimatedFramePosition.getResidualZ();
			}
			
			if (sumWeight == 0) 
				continue;
			
			ux /= sumWeight;
			uy /= sumWeight;
			uz /= sumWeight;

			targetSystemPosition.setResidualX(-ux);
			targetSystemPosition.setResidualY(-uy);
			targetSystemPosition.setResidualZ(-uz);
		}
	}
}
