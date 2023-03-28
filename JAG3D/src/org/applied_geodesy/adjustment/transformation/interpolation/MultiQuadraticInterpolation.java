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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class MultiQuadraticInterpolation extends Interpolation {

	private ObjectProperty<Double> exponent  = new SimpleObjectProperty<Double>(this, "exponent",  0.5);
	private ObjectProperty<Double> smoothing = new SimpleObjectProperty<Double>(this, "smoothing", 0.0);
	
	public MultiQuadraticInterpolation() {
		super(InterpolationType.MULTI_QUADRATIC);
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
	public void interpolate(Collection<EstimatedFramePosition> estimatedFramePositionsCollection, Collection<FramePositionPair> framePositionPairs) {
		double k = this.getExponent();
		double m = this.getSmoothing();
		
		List<EstimatedFramePosition> estimatedFramePositions = new ArrayList<EstimatedFramePosition>(estimatedFramePositionsCollection);
		int numberOfEstimatedFramePosition = estimatedFramePositions.size();
		
		UpperSymmPackMatrix S = new UpperSymmPackMatrix(numberOfEstimatedFramePosition);
		DenseVector vx = new DenseVector(numberOfEstimatedFramePosition);
		DenseVector vy = new DenseVector(numberOfEstimatedFramePosition);
		DenseVector vz = new DenseVector(numberOfEstimatedFramePosition);
		
		for (int i = 0; i < numberOfEstimatedFramePosition; i++) {
			EstimatedFramePosition estimatedFramePositionA = estimatedFramePositions.get(i);
			for (int j = i; j < numberOfEstimatedFramePosition; j++) {
				EstimatedFramePosition estimatedFramePositionB = estimatedFramePositions.get(j);
				
				double dx = estimatedFramePositionB.getX0() - estimatedFramePositionA.getX0();
				double dy = estimatedFramePositionB.getY0() - estimatedFramePositionA.getY0();
				double dz = estimatedFramePositionB.getZ0() - estimatedFramePositionA.getZ0();
				
				double weight = dx*dx + dy*dy + dz*dz + m;
				if (weight != 0)
					weight = Math.pow(weight, k);
				
				S.set(i, j, weight);
			}
			vx.set(i, -estimatedFramePositionA.getResidualX());
			vy.set(i, -estimatedFramePositionA.getResidualY());
			vz.set(i, -estimatedFramePositionA.getResidualZ());
		}

		MathExtension.solve(S, vx, true);
		DenseVector tmp = new DenseVector(vy, true);
		S.mult(tmp, vy);
		tmp = new DenseVector(vz, true);
		S.mult(tmp, vz);
		
		for (FramePositionPair framePositionPair : framePositionPairs) {
			if (!framePositionPair.isEnable())
				continue;
			
			double ux = 0.0;
			double uy = 0.0;
			double uz = 0.0;
			
			EstimatedFramePosition targetSystemPosition = framePositionPair.getTargetSystemPosition();
			
			for (int i = 0; i < numberOfEstimatedFramePosition; i++) {
				EstimatedFramePosition estimatedFramePosition = estimatedFramePositions.get(i);

				double dx = targetSystemPosition.getX() - estimatedFramePosition.getX0();
				double dy = targetSystemPosition.getY() - estimatedFramePosition.getY0();
				double dz = targetSystemPosition.getZ() - estimatedFramePosition.getZ0();
				
				double weight = dx*dx + dy*dy + dz*dz + m;
				if (weight != 0)
					weight = Math.pow(weight, k);

				ux += weight * vx.get(i);
				uy += weight * vy.get(i);
				uz += weight * vz.get(i);
			}

			targetSystemPosition.setResidualX(ux);
			targetSystemPosition.setResidualY(uy);
			targetSystemPosition.setResidualZ(uz);
		}
	}
}
