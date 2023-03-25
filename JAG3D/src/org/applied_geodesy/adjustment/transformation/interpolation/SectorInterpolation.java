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
import java.util.Collections;
import java.util.List;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SectorInterpolation extends Interpolation {

	private ObjectProperty<Double> numeratorExponent   = new SimpleObjectProperty<Double>(this, "numeratorExponent",  10.0);
	private ObjectProperty<Double> denominatorExponent = new SimpleObjectProperty<Double>(this, "exponentDenominator", 4.0);
	
	public SectorInterpolation() {
		super(InterpolationType.SECTOR);
	}
	
	public void setNumeratorExponent(double exponent) {
		this.numeratorExponent.set(exponent);
	}
	
	public double getNumeratorExponent() {
		return this.numeratorExponent.get();
	}
	
	public ObjectProperty<Double> numeratorExponentProperty() {
		return this.numeratorExponent;
	}
	
	public void setDenominatorExponent(double exponent) {
		this.denominatorExponent.set(exponent);
	}
	
	public double getDenominatorExponent() {
		return this.denominatorExponent.get();
	}
	
	public ObjectProperty<Double> denominatorExponentProperty() {
		return this.denominatorExponent;
	}

	@Override
	public void interpolate(Collection<EstimatedFramePosition> estimatedFramePositions, Collection<FramePositionPair> framePositionPairs) {
		double a = this.getNumeratorExponent();
		double b = this.getDenominatorExponent();
		
		int numberOfEstimatedFramePositions = estimatedFramePositions.size();
		
		for (FramePositionPair framePositionPair : framePositionPairs) {
			if (!framePositionPair.isEnable())
				continue;
			
			List<SectorElement> sectorElements        = new ArrayList<SectorElement>(estimatedFramePositions.size());
			List<SectorElement> adaptedSectorElements = new ArrayList<SectorElement>(estimatedFramePositions.size());
			EstimatedFramePosition targetSystemPosition = framePositionPair.getTargetSystemPosition();
			
			for (EstimatedFramePosition estimatedFramePosition : estimatedFramePositions) {

				double dx = estimatedFramePosition.getX0() - targetSystemPosition.getX();
				double dy = estimatedFramePosition.getY0() - targetSystemPosition.getY();
				double dz = estimatedFramePosition.getZ0() - targetSystemPosition.getZ();
				
				double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
				double azimuth  = Math.atan2(dy, dx);

				if (distance < SQRT_EPS) 
					distance = SQRT_EPS;

				distance /= 1000.0; // in [km] um kleine Zahlen zu vermeiden
				sectorElements.add( new SectorElement(azimuth, distance, -estimatedFramePosition.getResidualX(), -estimatedFramePosition.getResidualY(), -estimatedFramePosition.getResidualZ()) );
			}
			// Sorting list w.r.t. azimuth
			Collections.sort(sectorElements);
			
			for (SectorElement sectorElementA : sectorElements) {
				double azimuthA  = sectorElementA.getAzimuth();
				double distanceA = sectorElementA.getDistance();
				double sumWeights = 0.0, maxWeight = 0.0;
				double ux = 0.0, uy = 0.0, uz = 0.0;
				
				for (SectorElement sectorElementB : sectorElements) {
					double azimuthB  = sectorElementB.getAzimuth();
					double distanceB = sectorElementB.getDistance();

					double angle  = MathExtension.MOD(azimuthB - azimuthA, 2.0*Math.PI);
					double weight = Math.pow((Math.PI-angle)/Math.PI, a) / Math.pow(distanceB, b);
					
					if (Double.isInfinite(weight) || Double.isNaN(weight))
						weight = 0;
						
					maxWeight  = Math.max(maxWeight, weight);
					sumWeights += weight;
					
					ux += weight * sectorElementB.getResidualX();
					uy += weight * sectorElementB.getResidualY();
					uz += weight * sectorElementB.getResidualZ();
				}
				
				if (sumWeights != 0.0) {
					ux /= sumWeights;
					uy /= sumWeights;
					uz /= sumWeights;
				}
				else
					ux = uy = uz = 0.0;

				double weight = (maxWeight != 0.0) ? Math.pow(maxWeight, 1.0/b) : 0.0;	
				adaptedSectorElements.add( new SectorElement(azimuthA, distanceA, ux, uy, uz, weight) );
			}
			
			sectorElements.clear();
			
			double ux = 0.0, uy = 0.0, uz = 0.0, sumWeights = 0.0;
			for (int i = 0, j = i+1; i < numberOfEstimatedFramePositions; i++, j++) {
				if (j == numberOfEstimatedFramePositions)
					j = 0;
				
				SectorElement sectorElementA = adaptedSectorElements.get(i);
				SectorElement sectorElementB = adaptedSectorElements.get(j);
				
				double uxA      = sectorElementA.getResidualX();
				double uyA      = sectorElementA.getResidualY();
				double uzA      = sectorElementA.getResidualZ();
				double weightA  = sectorElementA.getWeight();
				double azimuthA = sectorElementA.getAzimuth();
				
				double uxB      = sectorElementB.getResidualX();
				double uyB      = sectorElementB.getResidualY();
				double uzB      = sectorElementB.getResidualZ();
				double weightB  = sectorElementB.getWeight();
				double azimuthB = sectorElementB.getAzimuth();

				double angle = MathExtension.MOD(azimuthB - azimuthA, 2.0*Math.PI);

				ux += ((uxB * weightB + uxA * weightA) * angle);
				uy += ((uyB * weightB + uyA * weightA) * angle);
				uz += ((uzB * weightB + uzA * weightA) * angle);
				sumWeights += ((weightB + weightA) * angle);
			}
			
			if (sumWeights != 0.0) {
				ux /= sumWeights;
				uy /= sumWeights;
				uz /= sumWeights;
			}
			else
				ux = uy = uz = 0.0;
			
			targetSystemPosition.setResidualX(ux);
			targetSystemPosition.setResidualY(uy);
			targetSystemPosition.setResidualZ(uz);
		}
	}
}
