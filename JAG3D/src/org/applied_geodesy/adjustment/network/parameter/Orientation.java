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

package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.DefaultAverageThreshold;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.FaceType;
import org.applied_geodesy.adjustment.network.observation.Observation;

public class Orientation extends AdditionalUnknownParameter {
	private boolean estimateApproximationValue = true;
	
	public Orientation(boolean estimateApproximationValue) {
		super(0.0);
	}

	public Orientation(double ori, boolean estimateApproximationValue) {
		super(ori);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.ORIENTATION;
	}
	
	@Override
	public void setColInJacobiMatrix(int col) {
		super.setColInJacobiMatrix( col );
		this.checkFace();
	}
	
	private void checkFace() {
		if (this.getObservations().size() <= 0 || !(this.getObservations().get(0) instanceof Direction)) 
			return;
		double ori = this.getValue();
		
		if (this.isEnable() && this.estimateApproximationValue)
			ori += this.advancedOrientation();
		
		int length = this.getObservations().size();
	    for (int i = 0; i < length; i++) {
	    	Direction dir = (Direction)this.getObservations().get(i);
	    	double azimuthMeasuredFace1 = ori + dir.getValueApriori();
	    	double azimuthCalculated    = dir.getValueAposteriori();
	    	azimuthMeasuredFace1        = MathExtension.MOD( azimuthMeasuredFace1, 2.0*Math.PI );
	    	double azimuthMeasuredFace2 = MathExtension.MOD( azimuthMeasuredFace1+Math.PI, 2.0*Math.PI );
	    	double face1 = Math.min(Math.abs(azimuthCalculated - azimuthMeasuredFace1), Math.abs(Math.abs(azimuthCalculated - azimuthMeasuredFace1) - 2.0*Math.PI) );
	    	double face2 = Math.min(Math.abs(azimuthCalculated - azimuthMeasuredFace2), Math.abs(Math.abs(azimuthCalculated - azimuthMeasuredFace2) - 2.0*Math.PI) );

	    	if ( face1 > face2 ) {
	    		dir.setValueApriori( MathExtension.MOD( azimuthMeasuredFace2-ori, 2.0*Math.PI ) );
	    		dir.setFace(dir.getFace() == FaceType.ONE ? FaceType.TWO : FaceType.ONE);
	    	}
	    }
	    
	    if (this.isEnable())
	    	this.setValue( this.getValue() + this.advancedOrientation() );
	}
	
	/**
	 * Bestimmung der genaeherten Orientierungsunbekannten
	 * durch einen Abriss des gemessenen Satzes.
	 * Um Einfluss von groben Messfehlern klein zu halten,
	 * wird der Median zurueck gegeben.
	 * 
	 * @return advancedOrientation
	 */
	private double advancedOrientation() {
		int length = this.getObservations().size();

		if (!(this.getObservations().get(0) instanceof Direction) || length == 0)
			return 0.0;

		double averageOrientation = 0;
		double maxUncertainty = Double.MIN_VALUE;
		double o[] = new double[length];
		for (int i = 0; i < length; i++) {
			Observation observation = this.getObservations().get(i);
			double tmp_o = observation.getValueAposteriori() - observation.getValueApriori();
			maxUncertainty = Math.max(observation.getStdApriori(), maxUncertainty);
			tmp_o = MathExtension.MOD(tmp_o, 2.0*Math.PI);
			if (i > 0 && (2.0*Math.PI) - Math.abs(o[i-1] - tmp_o) < 0.5) {
				if (tmp_o < o[i-1])
					tmp_o += 2.0*Math.PI;
				else
					tmp_o -= 2.0*Math.PI;
			}
			o[i] = tmp_o;
			averageOrientation += tmp_o;
		}
		
		java.util.Arrays.sort(o);
		double medianOrientation = o[(int)((length - 1)/2)];
		averageOrientation = averageOrientation / (double)length;
		maxUncertainty = Math.max(DefaultAverageThreshold.getThresholdDirection(), 100.0 * maxUncertainty);
		if (Math.abs(averageOrientation - medianOrientation) < maxUncertainty)
			return averageOrientation;
		
		averageOrientation = 0;
		int count = 0;
		for (int i = 0; i < length; i++) {
			if (Math.abs(o[i] - medianOrientation) < maxUncertainty) {
				averageOrientation += o[i];
				count++;
			}
		}

		if (count > 0)
			return averageOrientation / (double)count;
		
		return medianOrientation;
	}
	
	@Override
	public double getExpectationValue() {
		return 0.0; 
	}
	
	public void setEstimateApproximationValue(boolean estimateApproximationValue) {
		this.estimateApproximationValue = estimateApproximationValue;
	}
}