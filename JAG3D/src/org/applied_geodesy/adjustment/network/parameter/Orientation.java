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
	
	public Orientation() {
		this(0.0, true);
	}
	
	public Orientation(boolean estimateApproximationValue) {
		this(0.0, estimateApproximationValue);
	}

	public Orientation(double ori, boolean estimateApproximationValue) {
		super(ori);
		this.estimateApproximationValue = estimateApproximationValue;
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
		
		// Zuschlag zur ggf. bereits vorgegebenen Orientierung 
		double deltaOri = 0;
		
		if (this.isEnable() && this.estimateApproximationValue)
			deltaOri = this.advancedOrientation();

		int length = this.getObservations().size();
	    for (int i = 0; i < length; i++) {
	    	Direction dir = (Direction)this.getObservations().get(i);
	    	// a-posteriori Wert beruecksichtigt bereits die vorgegebene a-priori Orientierung, 
	    	// sodass nur das Delta zu beruecksichtigen ist bei der a-priori Beobachtung
	    	double azimuthMeasuredFace1 = deltaOri + dir.getValueApriori();
	    	double azimuthCalculated    = dir.getValueAposteriori();
	    	azimuthMeasuredFace1        = MathExtension.MOD( azimuthMeasuredFace1, 2.0*Math.PI );
	    	double azimuthMeasuredFace2 = MathExtension.MOD( azimuthMeasuredFace1+Math.PI, 2.0*Math.PI );
	    	double face1 = Math.min(Math.abs(azimuthCalculated - azimuthMeasuredFace1), Math.abs(Math.abs(azimuthCalculated - azimuthMeasuredFace1) - 2.0*Math.PI) );
	    	double face2 = Math.min(Math.abs(azimuthCalculated - azimuthMeasuredFace2), Math.abs(Math.abs(azimuthCalculated - azimuthMeasuredFace2) - 2.0*Math.PI) );
	    	
	    	if ( face1 > face2 ) {
	    		dir.setValueApriori( MathExtension.MOD( dir.getValueApriori() + Math.PI, 2.0*Math.PI ) );
	    		dir.setFace(dir.getFace() == FaceType.ONE ? FaceType.TWO : FaceType.ONE);
	    	}
	    }
	    
	    if (this.isEnable())
	    	this.setValue( MathExtension.MOD(this.getValue() + this.advancedOrientation(), 2*Math.PI) );
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