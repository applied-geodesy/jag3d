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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.observation.ComponentType;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;

public class ObservationGroup  {

	private int groupId;
	private List<Observation> observations = new ArrayList<Observation>();
	private final Epoch epoch;
	private final double sigmaA, sigmaB, sigmaC;
	private Set<Observation> excludedObservationsDuringAvaraging = null;
	
	public ObservationGroup(int id, double sigmaA, double sigmaB, double sigmaC, Epoch epoch) {
		if (sigmaA<=0)
			throw new IllegalArgumentException(this.getClass() + " Fehler, Gruppen ID oder Standardabweichung unzulassig!");
		this.groupId = id;	
		this.sigmaA = sigmaA;
		this.sigmaB = sigmaB>0?sigmaB:0.0;
		this.sigmaC = sigmaC>0?sigmaC:0.0;
		this.epoch = epoch;
	}

	public int getId(){
		return this.groupId;
	}

	public Epoch getEpoch() {
		return this.epoch;
	}

	public void add( Observation observation ) {
		// Sollte die Beobachtung keine a-priori Genauigkeit haben, 
		// bekommt sie die Gruppengenauigkeit
		if (observation.getStd() <= 0) {
			observation.setStd( this.getStd(observation) );
			observation.useGroupUncertainty(true);
		}
		// Gehoert die Beobachtung noch keiner Gruppe an, 
		// so wird sie dieser hinzugefuegt - Referenz wird abgelegt
		if (this.getId() >= 0 && (observation.getObservationGroup() == null || observation.getObservationGroup().getId() < 0))
			observation.setObservationGroup(this);
		this.observations.add( observation );
	}

	public Observation get( int index ) {
		return this.observations.get(index);
	}

	public int size() {
		return this.observations.size();
	}

	double getStdA() {
		return this.sigmaA;
	}

	double getStdB() {
		return this.sigmaB;
	}

	double getStdC() {
		return this.sigmaC;
	}

	public double getStdA(Observation observation) {
		return this.getStdA();
	}

	public double getStdB(Observation observation) {
		return this.getStdB();
	}

	public double getStdC(Observation observation) {
		return this.getStdC();
	}

	public double getStd(Observation observation) {
		double sigmaA = this.getStdA(observation);
		double sigmaB = this.getStdB(observation);
		double sigmaC = this.getStdC(observation);
		
		return Math.sqrt(sigmaA * sigmaA + sigmaB * sigmaB + sigmaC * sigmaC);
	}

	public int numberOfAdditionalUnknownParameter() {
		return 0;
	}

	protected boolean removeObservation(Observation observation) {
		return this.observations.remove(observation);
	}

	protected void clearObservations() {
		this.observations.clear();
	}

	public AdditionalUnknownParameter setApproximatedValue(AdditionalUnknownParameter param) {
		return param;
	}

	public void averageDetermination(double threshold) {
		this.excludedObservationsDuringAvaraging = new LinkedHashSet<Observation>(Math.max(10, this.size()/4));
		for (int i=0; i<this.size(); i++) {
			Observation avgObs = this.get(i);
			String startPointId = avgObs.getStartPoint().getName();
			String endPointId   = avgObs.getEndPoint().getName();
			List<Observation> avgObservations = new ArrayList<Observation>(Math.max(10, this.size()/4));
			
			double ih = avgObs.getStartPointHeight();
			double th = avgObs.getEndPointHeight();
			
			// Mittlerer Messwert
			// double avarage = avgObs.getObservationValue();
			avgObservations.add(avgObs);
			// Type der Beobachtung
			ObservationType avgType = avgObs.getObservationType();
			ComponentType avgSubType = null;

			if (avgType == ObservationType.GNSS1D || avgType == ObservationType.GNSS2D || avgType == ObservationType.GNSS3D)
				avgSubType = ((GNSSBaseline)avgObs).getComponent();
			
			for (int j=i+1; j<this.size(); j++) {
				Observation obs = this.get(j);
				String obsStartPointId = obs.getStartPoint().getName();
				String obsEndPointId   = obs.getEndPoint().getName();
				ObservationType obsType = obs.getObservationType();
				ComponentType obsSubType = null;
				if (obsType == ObservationType.GNSS1D || obsType == ObservationType.GNSS2D || obsType == ObservationType.GNSS3D)
					obsSubType = ((GNSSBaseline)obs).getComponent();
				
				double obsIh = obs.getStartPointHeight();
				double obsTh = obs.getEndPointHeight();
			
				if (obsStartPointId.equals(startPointId) && obsEndPointId.equals(endPointId) && obsIh == ih && obsTh == th && 
						(obsSubType == null && avgSubType == null || obsSubType != null && avgSubType != null && obsSubType == avgSubType)) {
					avgObservations.add(obs);
					// avarage += obs.getObservationValue();
					
					// entferne Beobachtung und korrigiere Laufindex der Schleife
					this.removeObservation(obs);
					j--;
				}
			}
			
			if (avgObservations.size() > 1) {
				// sortiere die Liste und bestimme den Median
				Collections.sort(avgObservations, new Comparator<Observation> () {
					@Override
					public int compare(Observation o1, Observation o2) {
						return o1.getValueApriori() > o2.getValueApriori() ? 1 : -1;
					}
				});				
				// Mittlere Strecke 
				double avgDist = 0.0;
				double avgValue = 0.0;
				Observation medianObs = avgObservations.get((avgObservations.size() - 1)/2);
				double median = medianObs.getValueApriori();
				
				// Tausche Werte, da nur noch die Beobachtung avgObs in der Gruppe verbleibt
				if (avgObs != medianObs) {
					double value = avgObs.getValueApriori();
					medianObs.setValueApriori(value);
					avgObs.setValueApriori(median);
				}
				
				int counter = 0;

				for (Observation obs : avgObservations) {
					double value = obs.getValueApriori();
					// Richtungen werden auf die I. Lage reduziert beim Bestimmen der Orientierung und liegen immer zw. 0 und 400
					// Dadurch kann der Median bei ~ 0... liegen und die (reduzierte) Richtung der I. Lage bei 399... liegen und umgedreht
					// Der Messwert wird daher an den Median angepasst durch +/-400, sodass sich aus 399... auch negative -0... Werte ergeben 
					if (obs.getObservationType() == ObservationType.DIRECTION && Math.abs(value - median) > Math.abs(Math.abs(value - median) - 2.0*Math.PI)) {
						if (value > median)
							value = value - 2.0*Math.PI;
						else
							value = value + 2.0*Math.PI;
					}
					if (Math.abs(value - median) <= threshold) {					
						avgValue += value;
						avgDist += obs.getDistanceForUncertaintyModel();
						counter++;
					}
					else {
						obs.setGrossError(value - median);
						this.excludedObservationsDuringAvaraging.add(obs);
					}
				}
				avgValue /= counter;
				avgDist  /= counter;
				
				if (avgObs.getObservationType() == ObservationType.DIRECTION || avgObs.getObservationType() == ObservationType.ZENITH_ANGLE)
					avgValue = MathExtension.MOD(avgValue, 2.0*Math.PI);
				
				avgObs.setValueApriori(avgValue);
				// Setze mittlere Strecke fuer Unsicherheitsbestimmung
				if (avgDist > 0) {
					avgObs.setDistanceForUncertaintyModel(avgDist);
				}				
			}
		}

		if (this.excludedObservationsDuringAvaraging.size() == 0)
			this.excludedObservationsDuringAvaraging = null;
	}
	
	public boolean isEmpty() {
		return this.observations.isEmpty();
	}

	public Set<Observation> getExcludedObservationsDuringAvaraging() {
		return this.excludedObservationsDuringAvaraging;
	}
	
	@Override
	public String toString() {
		return new String(this.getClass() + " Observation-Id: " + 
				this.getId() + " Number of Observations " + this.size());
	}
}
