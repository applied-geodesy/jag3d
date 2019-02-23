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

package org.applied_geodesy.adjustment.network.approximation.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.approximation.AutomatedApproximationAdjustment;
import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.intersection.ForwardIntersectionEntry;
import org.applied_geodesy.adjustment.network.approximation.bundle.intersection.ForwardIntersectionSet;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.ClassicGeodeticComputation;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point1D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.util.sql.DataBase;

public class SQLApproximationManager implements PropertyChangeListener {
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	private final DataBase dataBase;
	private boolean estimateDatumPoints = false;
	private boolean freeNetwork = true, interrupt = false;
	private Set<String> underdeterminedPointNames1D = new HashSet<String>();
	private Set<String> underdeterminedPointNames2D = new HashSet<String>();
	private Set<String> completestationNames = new HashSet<String>();
	private Set<String> outlier1d = new HashSet<String>();
	private Set<String> outlier2d = new HashSet<String>();
	private Map<String, Set<String>> directionLinks = new LinkedHashMap<String, Set<String>>();
	private PointBundle targetSystem1d, targetSystem2d;
	private int subSystemsCounter1d = 0, subSystemsCounter2d = 0; 
	private EstimationStateType estimationStatus1D = EstimationStateType.ERROR_FREE_ESTIMATION;
	private EstimationStateType estimationStatus2D = EstimationStateType.ERROR_FREE_ESTIMATION;
	private AutomatedApproximationAdjustment approximationAdjustment = null;
	
	public SQLApproximationManager(DataBase dataBase) {
		if (dataBase == null || !dataBase.isOpen())
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database must be open! " + dataBase);
		this.dataBase = dataBase;
	}

	public void clearAll() {
		this.subSystemsCounter1d = this.subSystemsCounter2d = 0;
		this.underdeterminedPointNames1D.clear();
		this.underdeterminedPointNames2D.clear();
		this.completestationNames.clear();
		this.outlier1d.clear();
		this.outlier2d.clear();
		this.directionLinks.clear();
		this.targetSystem1d = null;
		this.targetSystem2d = null;
		this.approximationAdjustment = null;
	}

	public int getSubSystemsCounter1D() {
		return this.subSystemsCounter1d;
	}

	public int getSubSystemsCounter2D() {
		return this.subSystemsCounter2d;
	}

	public Set<String> getOutliers1D() {
		return this.outlier1d;
	}

	public Set<String> getOutliers2D() {
		return this.outlier2d;
	}

	public void adjustApproximationValues(double threshold) throws SQLException {
		if (this.dataBase == null)
			return;

		this.clearAll();
		
		// Suche alle Fest- und Anschlusspunkte,
		// da diese bereits als Zielsystem genutzt
		// werden - eine Berechnung dieser Punkte
		// entfaellt!
		this.interrupt = false;
		this.freeNetwork = false;
		this.initTargetSystems(this.freeNetwork);
		this.freeNetwork = this.targetSystem1d == null && this.targetSystem2d == null;
		if (this.freeNetwork && !this.estimateDatumPoints) {
			this.initTargetSystems(true);
		}
		
		// Nur um Punkt, die nicht bestimmbar sind, auszugeben
		//this.initPointIds(this.isFreeNet && this.estimateDatumPoints);
		

		// Ermittle alle Punktnummern, die Standpunkte sind
		this.completestationNames = this.getCompletestationNames();

		// Bestimme alle Punktnummern im Netz, um spaeter zu kontrollieren, welche noch nicht berechnet wurden
		this.initNonEstimatedPointNames();

		// Initialisiere Lageausgleichung
		List<Point> points2d = this.getStationPointsWithSubSystems(2);
		if (points2d != null && points2d.size() > 0) {
			int targetCounter = -1;
			boolean converge = false;
			List<PointBundle> notTransFormedBundles = new ArrayList<PointBundle>(1);
			do {
				converge = false;
				this.approximationAdjustment = new AutomatedApproximationAdjustment(this.targetSystem2d, points2d);
				this.approximationAdjustment.addPropertyChangeListener(this);

				if (this.interrupt) {
					this.estimationStatus2D = EstimationStateType.INTERRUPT;
					this.approximationAdjustment.interrupt();
					this.interrupt = false;
					return;
				}

				if (notTransFormedBundles != null && notTransFormedBundles.size() > 0) {
					this.approximationAdjustment.addSystems(notTransFormedBundles);
				}
				this.approximationAdjustment.setEstimateDatumPoints( this.estimateDatumPoints );
				this.approximationAdjustment.setFreeNetwork(this.freeNetwork);
				this.approximationAdjustment.setThreshold(threshold);
				this.estimationStatus2D = this.approximationAdjustment.estimateApproximatedValues();
				if (this.approximationAdjustment.getTargetSystem() != null)
					this.targetSystem2d = this.approximationAdjustment.getTargetSystem();

				if (this.approximationAdjustment.getSystems().size() > 1)
					notTransFormedBundles = this.approximationAdjustment.getSystems();
				else
					notTransFormedBundles = new ArrayList<PointBundle>(1);

				if (this.targetSystem2d != null) {
					for (int i=0; i<this.targetSystem2d.size(); i++) {
						Point p = this.targetSystem2d.get(i);
						if (this.underdeterminedPointNames2D.contains(p.getName()))
							this.underdeterminedPointNames2D.remove(p.getName());
					}

					Set<String> estimatedPointIds = new HashSet<String>(this.underdeterminedPointNames2D.size());
					for (String newPointId : this.underdeterminedPointNames2D) {
						Point2D p = this.restockBundle(newPointId, this.targetSystem2d);

						for (PointBundle localBundle : notTransFormedBundles)
							this.restockBundle(newPointId, localBundle);

						if (p != null)
							estimatedPointIds.add(newPointId);
					}

					for (String estimatedPointId : estimatedPointIds)
						this.underdeterminedPointNames2D.remove(estimatedPointId);

					this.savePoints(this.targetSystem2d);
				}
				if (this.targetSystem2d != null && this.approximationAdjustment.getSystems().size() > 1 && this.targetSystem2d.size() != targetCounter) {
					targetCounter = this.targetSystem2d.size();
					points2d = this.getStationPointsWithSubSystems(2);
					converge = true;
				}

				this.approximationAdjustment.removePropertyChangeListener(this);
			}
			while (converge);			

			if (this.approximationAdjustment != null) {
				this.subSystemsCounter2d = this.approximationAdjustment.getSubSystemsCounter();
				this.outlier2d = this.approximationAdjustment.getOutliers();
			}
			
			points2d = null;
		}

		List<Point> points1d = this.getStationPointsWithSubSystems(1);
		if (points1d != null && points1d.size() > 0) {
			this.approximationAdjustment = new AutomatedApproximationAdjustment(this.targetSystem1d, points1d);
			this.approximationAdjustment.addPropertyChangeListener(this);

			if (this.interrupt) {
				this.estimationStatus1D = EstimationStateType.INTERRUPT;
				this.approximationAdjustment.interrupt();
				this.interrupt = false;
				return;
			}

			this.approximationAdjustment.setEstimateDatumPoints( this.estimateDatumPoints );
			this.approximationAdjustment.setFreeNetwork(this.freeNetwork);
			this.approximationAdjustment.setThreshold(threshold);
			this.estimationStatus1D = this.approximationAdjustment.estimateApproximatedValues();
			this.targetSystem1d = this.approximationAdjustment.getTargetSystem();
			if (this.targetSystem1d != null) {
				for (int i=0; i<this.targetSystem1d.size(); i++) {
					Point p = this.targetSystem1d.get(i);
					if (this.underdeterminedPointNames1D.contains(p.getName()))
						this.underdeterminedPointNames1D.remove(p.getName());
				}
				this.savePoints(this.targetSystem1d);
			}

			if (this.approximationAdjustment != null) {
				this.subSystemsCounter1d = this.approximationAdjustment.getSubSystemsCounter();
				this.outlier1d = this.approximationAdjustment.getOutliers();
			}
			this.approximationAdjustment.removePropertyChangeListener(this);
			points1d = null;
		}
	}

	public void interrupt() {
		this.interrupt = true;
		if (this.approximationAdjustment != null)
			this.approximationAdjustment.interrupt();
	}

	private Point2D restockBundle(String newPointId, PointBundle bundle) throws SQLException {
		if (bundle.contains(newPointId))
			return (Point2D)bundle.get(newPointId);

		Point2D p = this.getForwardIntersectionPoint(newPointId, bundle);

		if (p == null)
			p = this.getArcIntersectionPoint(newPointId, bundle);

		if (p == null)
			p = this.getBackwardIntersectionPoint(newPointId, bundle);

		if (p != null)
			bundle.addPoint(p);

		return p;
	}

	private Set<String> getCompletestationNames() throws SQLException {
		Set<String> completestationNames = new HashSet<String>();
		List<String> stationNames = this.getPointNames(1, true);
		for (String pointName : stationNames) 
			completestationNames.add(pointName);
		stationNames = this.getPointNames(2, true);
		for (String pointName : stationNames) 
			completestationNames.add(pointName);
		stationNames = this.getPointNames(3, true);
		for (String pointName : stationNames) 
			completestationNames.add(pointName);
		return completestationNames;
	}

	private void initNonEstimatedPointNames() throws SQLException {
		List<String> stationNames = this.getPointNames(1, false);
		for (String pointName : stationNames) 
			this.underdeterminedPointNames1D.add(pointName);
		stationNames = this.getPointNames(2, false);
		for (String pointName : stationNames) 
			this.underdeterminedPointNames2D.add(pointName);
		stationNames = this.getPointNames(3, false);
		for (String pointName : stationNames) { 
			this.underdeterminedPointNames1D.add(pointName);
			this.underdeterminedPointNames2D.add(pointName);
		}
	}

	private List<Point> getStationPointsWithSubSystems(int dim) throws SQLException {
		// Speichere alle Punktnummern von Standpunkten
		List<String> stationNames;
		
		List<Point> pointList = new ArrayList<Point>();
		Map<String, Point> pointMap = new LinkedHashMap<String, Point>();

		
		if (dim != 2) {
			// Ermittle die Standpunkte und ihre zugehoerigen Beobachtungen fuer Hoehe
			stationNames = this.getPointNames(1, true);
			for (String pointName : stationNames) {
				Point1D point = new Point1D(pointName, 0.0);
				pointList.add(point);
				pointMap.put(pointName, point);

				this.addObservation(point);
			}

			stationNames = this.getPointNames(3, true);
			for (String pointName : stationNames) {
				Point1D point1d = new Point1D(pointName, 0.0);
				pointList.add( point1d );
				pointMap.put(pointName, point1d);

				this.addObservation(point1d); 
			}
		}
		else if (dim != 1) {
			// Ermittle die Standpunkte und ihre zugehoerigen Beobachtungen fuer Lage
			stationNames = this.getPointNames(2, true);
			for (String pointName : stationNames) {
				Point2D point = new Point2D(pointName, 0.0, 0.0);
				pointList.add(point);
				pointMap.put(pointName, point);

				this.addObservation(point); 
			}

			stationNames = this.getPointNames(3, true);
			for (String pointName : stationNames) {
				Point2D point2d = new Point2D(pointName, 0.0, 0.0);
				pointList.add( point2d );
				pointMap.put(pointName, point2d);

				this.addObservation(point2d); 
			}

			// Fuege Systeme hinzu, die per Vorwaertsschnitt entstehen
			for (Map.Entry<String, Set<String>> directionLink : this.directionLinks.entrySet()) {
				String fixPointIdA = directionLink.getKey();
				Point fixPointA = pointMap.get(fixPointIdA);
				for (String fixPointIdB : directionLink.getValue()) {
					if (this.directionLinks.containsKey(fixPointIdB) && this.directionLinks.get(fixPointIdB).contains(fixPointIdA)) {
						// Suche nach Vorwaertsschnitten
						//System.out.println(fixPointIdA+"  "+fixPointIdB);
						Point fixPointB = pointMap.get(fixPointIdB);
						this.addForwardIntersectionCombinations(fixPointA, fixPointB);
					}
				}
				// Entferne die abgearbeiteten Verknüpfungen um Rückverlinkungen zu unterbinden.
				directionLink.getValue().clear();
				fixPointA.joinBundles();
			}
			this.directionLinks.clear();
		}
		
		pointMap.clear();
		return pointList;
	}

	private void addObservation(Point point) throws SQLException {
		int dim = -1;
		if (point instanceof Point1D)
			dim = 1;
		else if (point instanceof Point2D)
			dim = 2;
		else
			return;

		String pointName = point.getName();
		List<TerrestrialObservationRow> deltaH = null, distances2D = null, distances3D = null, zenithangle = null;

		if (dim != 2)
			deltaH = this.getTerrestrialObservationsIgnoreGroups(pointName, ObservationType.LEVELING);
		else if (dim != 1)
			distances2D = this.getTerrestrialObservationsIgnoreGroups(pointName, ObservationType.HORIZONTAL_DISTANCE);

		distances3D = this.getTerrestrialObservationsIgnoreGroups(pointName, ObservationType.SLOPE_DISTANCE);
		zenithangle = this.getTerrestrialObservationsIgnoreGroups(pointName, ObservationType.ZENITH_ANGLE);

		// Ergaenze Pseudobeobachtungen Strecke2D zw. Festpunkten
		if (dim != 1 && this.targetSystem2d != null && this.targetSystem2d.get(pointName) != null) {
			Point startPoint = this.targetSystem2d.get(pointName);
			for (int i=0; i<this.targetSystem2d.size(); i++) {
				Point endPoint = this.targetSystem2d.get(i);

				// Standpunkt == Zielpunkt oder Dimensionsfehler, dann abbrechen
				if (endPoint.getName().equals(pointName) || endPoint.getDimension() != 1) 
					continue;

				// Pruefe, ob es bereits eine Streckenmessung zwischen den Punkten gibt
				boolean hasObservedDistance = false;
				for (TerrestrialObservationRow dist2d : distances2D) {
					if (dist2d.getStartPointName().equals(startPoint.getName()) && 
							dist2d.getEndPointName().equals(endPoint.getName()) || 
							dist2d.getStartPointName().equals(endPoint.getName()) && 
							dist2d.getEndPointName().equals(startPoint.getName())) {
						hasObservedDistance = true;
						break;
					}
				}

				// Wenn Streckenmessung noch nicht vorhanden, dann speichern
				if (!hasObservedDistance) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(startPoint.getName());
					obs.setEndPointName(endPoint.getName());
					obs.setValueApriori(startPoint.getDistance2D(endPoint));
					distances2D.add(obs);
				}
			}
		}

		// Ergaenze Pseudobeobachtungen deltaH zw. Festpunkten
		else if (dim != 2 && this.targetSystem1d != null && this.targetSystem1d.get(pointName) != null) {
			Point startPoint = this.targetSystem1d.get(pointName);
			for (int i=0; i<this.targetSystem1d.size(); i++) {
				Point endPoint = this.targetSystem1d.get(i);

				// Standpunkt == Zielpunkt oder Dimensionsfehler, dann abbrechen
				if (endPoint.getName().equals(pointName) || endPoint.getDimension() != 1) 
					continue;

				// Pruefe, ob es bereits ein Hoehenunterschied zwischen den Punkten gibt
				boolean hasObservedDeltaH = false;
				for (TerrestrialObservationRow dH : deltaH) {
					if (dH.getStartPointName().equals(startPoint.getName()) && 
							dH.getEndPointName().equals(endPoint.getName()) || 
							dH.getStartPointName().equals(endPoint.getName()) && 
							dH.getEndPointName().equals(startPoint.getName())) {
						hasObservedDeltaH = true;
						break;
					}
				}

				// Wenn Hoehenunterschied noch nicht vorhanden, dann speichern
				if (!hasObservedDeltaH) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(startPoint.getName());
					obs.setEndPointName(endPoint.getName());
					obs.setValueApriori(endPoint.getZ() - startPoint.getZ());
					deltaH.add(obs);
				}
			}		

		}

		Map<String, Double> medianDeltaH = null, medianDistances2D = null;
		if (dim != 2)
			medianDeltaH      = this.getMedianDeltaH(pointName, deltaH, distances3D, zenithangle);
		else if (dim != 1)
			medianDistances2D = this.getMedianDistance2D(pointName, distances2D, distances3D, zenithangle);

		deltaH = distances2D = distances3D = zenithangle = null;


		if (dim != 2 && medianDeltaH != null) {
			Point1D point1d = (Point1D)point;
			point1d.addBundle();
			for ( Map.Entry<String, Double> e : medianDeltaH.entrySet() ) {
				String endPointName = e.getKey();
				double dh = e.getValue().doubleValue();
				point1d.addObservedPoint(endPointName, dh);
			}
			point1d.joinBundles();

		}

		else if (dim != 1) {
			Point2D point2d = (Point2D)point;
			Map<Integer, List<TerrestrialObservationRow>> directions = this.getObservations(pointName, null, ObservationType.DIRECTION, -1);
			// Ergaenze Pseudobeobachtungen Richtung zw. Festpunkten
			if (this.targetSystem2d != null && this.targetSystem2d.get(point2d.getName()) != null) {
				Point startPoint = this.targetSystem2d.get(point2d.getName());
				List<TerrestrialObservationRow> dirSet = new ArrayList<TerrestrialObservationRow>();
				for (int i=0; i<this.targetSystem2d.size(); i++) {
					Point endPoint = this.targetSystem2d.get(i);
					if (!endPoint.getName().equals(point2d.getName()) && endPoint.getDimension() != 1) {
						double dy = endPoint.getY() - startPoint.getY();
						double dx = endPoint.getX() - startPoint.getX();

						TerrestrialObservationRow obs = new TerrestrialObservationRow();
						obs.setStartPointName(startPoint.getName());
						obs.setEndPointName(endPoint.getName());
						obs.setValueApriori(MathExtension.MOD(Math.atan2(dy, dx), 2.0*Math.PI));
						dirSet.add(obs);
					}
				}
				if (dirSet.size()>1)
					directions.put(-1, dirSet);
			}

			for (List<TerrestrialObservationRow> dirSet : directions.values()) {
				Map<String, Double> medianDirection = this.getMedianDirections(dirSet);

				point2d.addBundle();
				for ( Map.Entry<String, Double> elm : medianDirection.entrySet() ) {
					String endPointName = elm.getKey();
					double dir = elm.getValue().doubleValue();

					if (!this.directionLinks.containsKey(point2d.getName()))
						this.directionLinks.put(point2d.getName(), new HashSet<String>());

					if (this.completestationNames.contains(endPointName))
						this.directionLinks.get(point2d.getName()).add(endPointName);

					if (medianDistances2D != null && medianDistances2D.containsKey(endPointName)) {
						double dist2d = medianDistances2D.get(endPointName);
						point2d.addObservedPoint(endPointName, dir, dist2d);
					}
				}
			}
			point2d.joinBundles();
		}
	}

	private Map<String, Double> getMedianDeltaH(String startPointName, List<TerrestrialObservationRow> deltaH, List<TerrestrialObservationRow> distances3D, List<TerrestrialObservationRow> zenithangles) {
		Map<String, List<Double>> deltaHList = new LinkedHashMap<String, List<Double>>();

		if (deltaH != null) {
			for (TerrestrialObservationRow dh : deltaH) {
				String endPointName = dh.getEndPointName();
				double h =  ClassicGeodeticComputation.ORTHO_HEIGHT(dh.getValueApriori(), dh.getInstrumentHeight(), dh.getReflectorHeight());

				if (!(deltaHList.containsKey(endPointName)))
					deltaHList.put(endPointName, new ArrayList<Double>());

				deltaHList.get(endPointName).add(h);
			}
		}

		if (distances3D != null && zenithangles != null) {

			for (int j=0; j<zenithangles.size(); j++) {
				String endPointName = zenithangles.get(j).getEndPointName();

				double zenith = zenithangles.get(j).getValueApriori();
				double ihZenith = zenithangles.get(j).getInstrumentHeight();
				double thZenith = zenithangles.get(j).getReflectorHeight();

				for (int i=0; i<distances3D.size(); i++) {
					if (!endPointName.equals(distances3D.get(i).getEndPointName())) 
						continue;

					double dist3d = distances3D.get(i).getValueApriori();
					double ihDist = distances3D.get(i).getInstrumentHeight();
					double thDist = distances3D.get(i).getReflectorHeight();

					double slopeDist = ClassicGeodeticComputation.SLOPEDISTANCE(dist3d, ihDist, thDist, zenith, ihZenith, thZenith);
					double h = ClassicGeodeticComputation.TRIGO_HEIGHT_3D(slopeDist, zenith, ihZenith, thZenith);

					if (!(deltaHList.containsKey(endPointName)))
						deltaHList.put(endPointName, new ArrayList<Double>());

					deltaHList.get(endPointName).add(h);
				}
				// Bestimme die Horizontalstrecke zusaetzlich aus Koordinaten um den Hoehenunterschied ueber den Z-Winkel noch abzuleiten
				if (this.targetSystem2d != null && this.targetSystem2d.contains(startPointName) && this.targetSystem2d.contains(endPointName) && !this.outlier2d.contains(startPointName) && !this.outlier2d.contains(endPointName)) {
					double dist2d = this.targetSystem2d.get(startPointName).getDistance2D(this.targetSystem2d.get(endPointName));
					double h = ClassicGeodeticComputation.TRIGO_HEIGHT_2D(dist2d, zenith, ihZenith, thZenith);
					if (!(deltaHList.containsKey(endPointName)))
						deltaHList.put(endPointName, new ArrayList<Double>());

					deltaHList.get(endPointName).add(h);
				}
			}
		}
		Map<String, Double> deltaHmap = new LinkedHashMap<String, Double>();
		for ( Map.Entry<String, List<Double>> e : deltaHList.entrySet() ) {
			List<Double> list = e.getValue();

			if (list.size()>0) {
				Collections.sort(list);
				deltaHmap.put(e.getKey(), list.get((int)((list.size() - 1)/2)));
			}
		}
		return deltaHmap;
	}

	private Map<String, Double> getMedianDistance2D(String startPointName, List<TerrestrialObservationRow> distances2D, List<TerrestrialObservationRow> distances3D, List<TerrestrialObservationRow> zenithangles) {
		Map<String, List<Double>> dist2dList = new LinkedHashMap<String, List<Double>>();

		if (distances2D != null) {
			for (TerrestrialObservationRow dist : distances2D) {
				String endPointName = dist.getEndPointName();

				if (!(dist2dList.containsKey(endPointName)))
					dist2dList.put(endPointName, new ArrayList<Double>());

				dist2dList.get(endPointName).add(dist.getValueApriori());
			}
		}

		if (distances3D != null && zenithangles != null) {
			for (int i=0; i<distances3D.size(); i++) {
				String endPointName = distances3D.get(i).getEndPointName();
				double dist3d = distances3D.get(i).getValueApriori();
				double ihDist = distances3D.get(i).getInstrumentHeight();
				double thDist = distances3D.get(i).getReflectorHeight();
				for (int j=0; j<zenithangles.size(); j++) {
					if (!endPointName.equals(zenithangles.get(j).getEndPointName())) 
						continue;

					double zenith = zenithangles.get(j).getValueApriori();
					double ihZenith = zenithangles.get(j).getInstrumentHeight();
					double thZenith = zenithangles.get(j).getReflectorHeight();

					double slopeDist = ClassicGeodeticComputation.SLOPEDISTANCE(dist3d, ihDist, thDist, zenith, ihZenith, thZenith);
					double dist2d    = ClassicGeodeticComputation.DISTANCE2D(slopeDist, zenith);

					if (!(dist2dList.containsKey(endPointName)))
						dist2dList.put(endPointName, new ArrayList<Double>());

					dist2dList.get(endPointName).add(dist2d);
				}
			}
		}
		Map<String, Double> dist2D = new LinkedHashMap<String, Double>();
		for ( Map.Entry<String, List<Double>> e : dist2dList.entrySet() ) {
			List<Double> list = e.getValue();
			if (list.size()>0) {
				Collections.sort(list);
				dist2D.put(e.getKey(), list.get((int)((list.size() - 1)/2)));
			}
		}
		return dist2D;
	}

	private Map<String, Double> getMedianDirections(List<TerrestrialObservationRow> observations) {
		Map<String, List<Double>> obsToPoint = new LinkedHashMap<String, List<Double>>(); 
		for (TerrestrialObservationRow observation : observations) {
			String pointName = observation.getEndPointName();
			if (obsToPoint.containsKey(pointName)) {
				double refValue = obsToPoint.get(pointName).get(0);
				obsToPoint.get(pointName).add(this.formingDirectionToFaceI(refValue, observation.getValueApriori()));
			}
			else {
				List<Double> l = new ArrayList<Double>();
				l.add(observation.getValueApriori());
				obsToPoint.put(pointName, l);
			}
		}
		// Erzeuge neue Liste
		Map<String, Double> dir = new LinkedHashMap<String, Double>();
		//for (TerrestrialObservationRow observation : observations) {
		for ( Map.Entry<String, List<Double>> elm : obsToPoint.entrySet() ) {
			List<Double> list = elm.getValue();
			if (list.size()>0) {
				Collections.sort(list);
				double median = list.get((int)((list.size() - 1)/2));
				dir.put(elm.getKey(), median);
			}
		}
		return dir;
	}

	private double formingDirectionToFaceI(double refValue, double dir) {
		double azimuthMeasuredFace1 = dir;
		double azimuthMeasuredFace2 = MathExtension.MOD( dir + Math.PI, 2*Math.PI);
		double face1 = Math.min(Math.abs(refValue - azimuthMeasuredFace1), Math.abs(Math.abs(refValue - azimuthMeasuredFace1) - 2*Math.PI) );
		double face2 = Math.min(Math.abs(refValue - azimuthMeasuredFace2), Math.abs(Math.abs(refValue - azimuthMeasuredFace2) - 2*Math.PI) );

		if (face1 > face2)
			return azimuthMeasuredFace2;
		else
			return azimuthMeasuredFace1;
	}

	private Point2D getArcIntersectionPoint(String pointName, PointBundle bundle) throws SQLException {
		if (bundle.isIntersection())
			return null;

		List<Point2D> startPoints = new ArrayList<Point2D>(); 
		List<TerrestrialObservationRow> distances2d = this.getTerrestrialObservationsIgnoreGroups(pointName, ObservationType.HORIZONTAL_DISTANCE);
		Map<String, Double> medianDistance2d = this.getMedianDistance2D(pointName, distances2d, null, null);

		List<Point2D> endPoints = new ArrayList<Point2D>(medianDistance2d.size());
		List<Double> observations = new ArrayList<Double>(medianDistance2d.size());

		for (Map.Entry<String, Double> dist2d : medianDistance2d.entrySet()) {
			if (!bundle.contains(dist2d.getKey()))
				continue;

			endPoints.add((Point2D)bundle.get(dist2d.getKey()));
			observations.add(dist2d.getValue());
		}

		if (endPoints.size() < 3)
			return null;

		// Permutieren
		for (int k=0; k<endPoints.size(); k++) {
			Point2D A = endPoints.get(k);
			double s1 = observations.get(k);
			for (int l=k+1; l<endPoints.size(); l++) {
				int m = l+1<endPoints.size()?l+1:0;
				Point2D B = endPoints.get(l);
				Point2D C = endPoints.get(m);

				double s2 = observations.get(l);
				double s3 = observations.get(m);
				double s = A.getDistance2D(B);

				if (2.0*s*s1 == 0)
					continue;

				double tAB = ClassicGeodeticComputation.DIRECTION(A, B);
				double alpha = Math.acos((s*s + s1*s1 - s2*s2)/(2.0*s*s1));

				Point2D tmp1 = ClassicGeodeticComputation.POLAR(A, pointName, tAB + alpha, s1);
				Point2D tmp2 = ClassicGeodeticComputation.POLAR(A, pointName, tAB - alpha, s1);

				if (Math.abs(C.getDistance2D(tmp1) - s3) < Math.abs(C.getDistance2D(tmp2) - s3))
					startPoints.add(tmp1);
				else
					startPoints.add(tmp2);
			}
		}
		return (Point2D)this.getMedianPoint(startPoints);
	}

	private Point2D getBackwardIntersectionPoint(String startPointName, PointBundle bundle) throws SQLException {
		List<Point2D> startPoints = new ArrayList<Point2D>(); 
		Map<Integer, List<TerrestrialObservationRow>> directions = this.getObservations(startPointName, null, ObservationType.DIRECTION, -1);
		for (List<TerrestrialObservationRow> directionSets : directions.values()) {
			Map<String, Double> medianDirection = this.getMedianDirections(directionSets);

			List<Point2D> endPoints = new ArrayList<Point2D>(medianDirection.size());
			List<Double> observations = new ArrayList<Double>(medianDirection.size());

			for (Map.Entry<String, Double> dir : medianDirection.entrySet()) {
				if (!bundle.contains(dir.getKey()))
					continue;

				endPoints.add((Point2D)bundle.get(dir.getKey()));
				observations.add(dir.getValue());
			}

			if (endPoints.size() < 3)
				return null;

			// Permutieren
			for (int k=0; k<endPoints.size(); k++) {
				Point2D A = endPoints.get(k);
				double rA = observations.get(k);
				for (int l=k+1; l<endPoints.size(); l++) {
					Point2D B = endPoints.get(l);
					double rB = observations.get(l);
					double distAB = A.getDistance2D(B);
					double tAB = ClassicGeodeticComputation.DIRECTION(A, B);
					for (int m=l+1; m<endPoints.size(); m++) {
						Point2D C = endPoints.get(m);
						double rC = observations.get(m);

						double distAC = A.getDistance2D(C);
						double tAC = ClassicGeodeticComputation.DIRECTION(A, C);

						double alpha = MathExtension.MOD(rB - rA, 2.0*Math.PI);
						double beta  = MathExtension.MOD(rC - rB, 2.0*Math.PI);


						if (alpha + beta > Math.PI) {
							alpha -= Math.PI;
							beta  -= Math.PI;
						}

						double sinAlphaBeta = Math.sin(alpha+beta);

						if (sinAlphaBeta == 0)
							continue;

						double gamma  = tAB - tAC;

						double distGH = -distAC * Math.sin(alpha)/sinAlphaBeta*Math.sin(beta);
						double distAG =  distAC * Math.sin(alpha)/sinAlphaBeta*Math.cos(beta);   

						double distFB = distAB * Math.sin(gamma);
						double distAF = distAB * Math.cos(gamma);

						double delta  = Math.atan2(distGH-distFB, distAG-distAF);
						if (delta>Math.PI)
							delta = delta - Math.PI;

						double tAN  = tAC + delta - alpha;
						double distAN = distAC * Math.sin(delta+beta)/sinAlphaBeta;

						Point2D N = ClassicGeodeticComputation.POLAR(A, startPointName, tAN, distAN);
						startPoints.add(N);						
					}
				}
			}

		}
		return (Point2D)this.getMedianPoint(startPoints);
	}

	private Point2D getForwardIntersectionPoint(String newPointId, PointBundle bundle) throws SQLException {
		String sql = "SELECT DISTINCT \"start_point_name\", \"group_id\" FROM \"ObservationApriori\" AS \"o1\" " +
				"JOIN \"ObservationGroup\" AS \"g1\" ON \"g1\".\"id\" = \"o1\".\"group_id\" AND \"g1\".\"enable\" = TRUE " +
				"JOIN \"PointApriori\" ON \"PointApriori\".\"name\" = \"o1\".\"start_point_name\" AND \"PointApriori\".\"enable\" = TRUE " +
				"JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE " +
				"WHERE \"o1\".\"enable\" = TRUE AND \"g1\".\"type\" = ? AND \"o1\".\"end_point_name\" = ?";

		// Gruppen ID als Index
		List<Integer> groups = new ArrayList<Integer>();
		// Gruppen ID als Index mit zugehoerigem Endpunkt und Messung
		Map<Integer, TerrestrialObservationRow> directionSets = new LinkedHashMap<Integer, TerrestrialObservationRow>();
		// Ermittel gemeinsame Startpunkte von N
		PreparedStatement statement = this.dataBase.getPreparedStatement(sql);
		statement.setInt(1, ObservationType.DIRECTION.getId());
		statement.setString(2, newPointId);

		ResultSet result = statement.executeQuery();
		while (result.next()) {
			int groupId = result.getInt("group_id");
			String startPointName = result.getString("start_point_name");

			// Wenn der Standpunkt unbekannt ist, ignoriere diesen
			if (!bundle.contains(startPointName))
				continue;

			// ermittel den Richtungssatz der Gruppe - Es kann nur EIN Satz sein, da groupID uebergeben wurde
			Map<Integer, List<TerrestrialObservationRow>> directions = this.getObservations(startPointName, null, ObservationType.DIRECTION, groupId);
			TerrestrialObservationRow orientatedDirectionAngle = this.getOrientatedDirectionAngle(newPointId, directions.get(groupId), bundle);
			if (orientatedDirectionAngle != null) {
				directionSets.put(groupId, orientatedDirectionAngle);
				groups.add(groupId);
			}
		}

		ForwardIntersectionSet forwardIntersections = new ForwardIntersectionSet();

		for (int i=0; i<groups.size(); i++) {
			TerrestrialObservationRow directionAngleA = directionSets.get(groups.get(i));
			String fixPointIdA = directionAngleA.getStartPointName();
			Point2D fixPointA = (Point2D)bundle.get(fixPointIdA);
			if (fixPointIdA == null)
				continue;

			for (int j=i+1; j<groups.size(); j++) {
				TerrestrialObservationRow directionAngleB = directionSets.get(groups.get(j));
				String fixPointIdB = directionAngleB.getStartPointName();
				Point2D fixPointB = (Point2D)bundle.get(fixPointIdB);
				if (fixPointB == null || fixPointIdA.equals(fixPointIdB))
					continue;

				double rAB = ClassicGeodeticComputation.DIRECTION(fixPointA, fixPointB);
				double rAN = directionAngleA.getValueApriori();
				double rBA = MathExtension.MOD(rAB + Math.PI, 2.0*Math.PI);
				double rBN = directionAngleB.getValueApriori();

				forwardIntersections.add(fixPointA, fixPointB, newPointId, rAB, rAN, rBA, rBN);
			}
		}

		List<Point2D> points = forwardIntersections.adjustForwardIntersections();
		return (Point2D)this.getMedianPoint(points);
	}

	/**
	 * Erzeugt Sub-Systeme, die per Vorwaertsschnitt entstehen. Berechnet den Neupunkt.
	 * Benoetigt die beiden Festpunkte. Der Festpunkt B wird tmp. auf 10/20 gesetzt, sodass
	 * gegenueber dem globalen System ein Massstab zu schaetzen ist!
	 * Der Neupunkt wird als Sub-Set an A und B gefuegt.
	 * 
	 * @param fixPointA
	 * @param fixPointB
	 */
	private void addForwardIntersectionCombinations(Point referencePointA, Point referencePointB) throws SQLException {
		int offsetX = 10, offsetY = 20;

		ForwardIntersectionSet forwardIntersections = new ForwardIntersectionSet();

		String referencePointNameA = referencePointA.getName();  
		String referencePointNameB = referencePointB.getName();

		Point2D tmpReferencePointA = new Point2D(referencePointNameA, 0, 0);
		Point2D tmpReferencePointB = new Point2D(referencePointNameB, offsetX, offsetY);

		// SQL gemeinsame Endpunkte von A und B
		String sqlPoint = "SELECT DISTINCT \"end_point_name\" FROM \"ObservationApriori\" AS \"o1\" " +
				"JOIN \"PointApriori\" AS \"pa1\" ON \"o1\".\"end_point_name\" = \"pa1\".\"name\" AND \"pa1\".\"enable\" = TRUE " +
				"JOIN \"PointGroup\" AS \"pg1\" ON \"pa1\".\"group_id\" = \"pg1\".\"id\" AND \"pg1\".\"enable\" = TRUE " +
				"JOIN \"ObservationApriori\" AS \"o2\" ON \"o1\".\"end_point_name\" = \"o2\".\"end_point_name\" AND \"o1\".\"start_point_name\" != \"o2\".\"start_point_name\" " +
				"JOIN \"ObservationGroup\" AS \"g1\" ON \"g1\".\"id\" = \"o1\".\"group_id\" AND \"g1\".\"enable\" = TRUE " +
				"JOIN \"ObservationGroup\" AS \"g2\" ON \"g2\".\"id\" = \"o2\".\"group_id\" AND \"g1\".\"type\" = \"g2\".\"type\" AND \"g2\".\"enable\" = TRUE " +
				"WHERE \"g1\".\"type\" = ? AND \"o1\".\"start_point_name\" = ? AND  \"o2\".\"start_point_name\" = ?  AND \"o1\".\"enable\" = TRUE AND \"o2\".\"enable\" = TRUE"; 

		// SQL Richtungen von A nach B und <EndPunkt>
		String sqlObservation = "SELECT \"group_id\", \"end_point_name\", \"value_0\" FROM \"ObservationApriori\" " +
				"JOIN \"ObservationGroup\" ON \"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" AND \"ObservationGroup\".\"enable\" = TRUE AND \"ObservationGroup\".\"type\" = ? " +
				"WHERE \"enable\" = TRUE AND \"start_point_name\" = ? AND \"end_point_name\" IN (?, ?) " +
				"ORDER BY \"group_id\", \"end_point_name\", \"id\"";

		Set<String> identEndPoints = new LinkedHashSet<String>();

		// Ermittel gemeinsame Endpunkte von A und B und speichere in identEndPoints
		PreparedStatement statement = this.dataBase.getPreparedStatement(sqlPoint);
		statement.setInt(1, ObservationType.DIRECTION.getId());
		statement.setString(2, referencePointNameA);
		statement.setString(3, referencePointNameB);

		ResultSet result = statement.executeQuery();
		while (result.next()) {
			String pointName = result.getString("end_point_name");
			if (!referencePointA.containsPointInBundle(pointName) || !referencePointB.containsPointInBundle(pointName))
				identEndPoints.add(pointName);
		}

		statement = this.dataBase.getPreparedStatement(sqlObservation);
		statement.setInt(1, ObservationType.DIRECTION.getId());

		for (String pointName : identEndPoints) {
			statement.setString(4, pointName);

			Map<Integer, ArrayList<TerrestrialObservationRow>> observationsA = new LinkedHashMap<Integer, ArrayList<TerrestrialObservationRow>>();
			Map<Integer, ArrayList<TerrestrialObservationRow>> observationsB = new LinkedHashMap<Integer, ArrayList<TerrestrialObservationRow>>();

			// Ermittle Richtungen von A nach B und identEndPoints-Punkten
			statement.setString(2, referencePointNameA);
			statement.setString(3, referencePointNameB);
			result = statement.executeQuery();
			while (result.next()) {
				int groupId = result.getInt("group_id");
				String endPointName = result.getString("end_point_name");
				double value = result.getDouble("value_0");

				if (!observationsA.containsKey(groupId))
					observationsA.put(groupId, new ArrayList<TerrestrialObservationRow>());

				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(referencePointNameA);
				obs.setEndPointName(endPointName);
				obs.setValueApriori(value);

				observationsA.get(groupId).add(obs);
			}

			// Ermittle Richtungen von B nach A und identEndPoints-Punkten
			statement.setString(2, referencePointNameB);
			statement.setString(3, referencePointNameA);
			result = statement.executeQuery();
			while (result.next()) {
				int groupId = result.getInt("group_id");
				String endPointName = result.getString("end_point_name");
				double value = result.getDouble("value_0");

				if (!observationsB.containsKey(groupId))
					observationsB.put(groupId, new ArrayList<TerrestrialObservationRow>());

				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(referencePointNameB);
				obs.setEndPointName(endPointName);
				obs.setValueApriori(value);

				observationsB.get(groupId).add(obs);
			}

			// erzeuge ein Vorwaertsschnitt-Obj
			Map<Integer, Map<String, Double>> medianObsA = new LinkedHashMap<Integer, Map<String, Double>>();
			Map<Integer, Map<String, Double>> medianObsB = new LinkedHashMap<Integer, Map<String, Double>>();
			for ( Map.Entry<Integer, ArrayList<TerrestrialObservationRow>> groupObsA : observationsA.entrySet() ) {
				int groupId = groupObsA.getKey();
				List<TerrestrialObservationRow> obsA = groupObsA.getValue();
				Map<String, Double> median = this.getMedianDirections(obsA);
				if (median.containsKey(referencePointNameB) && median.containsKey(pointName))
					medianObsA.put(groupId, median);
			}

			for ( Map.Entry<Integer, ArrayList<TerrestrialObservationRow>> groupObsB : observationsB.entrySet() ) {
				int groupId = groupObsB.getKey();
				List<TerrestrialObservationRow> obsB = groupObsB.getValue();
				Map<String, Double> median = this.getMedianDirections(obsB);
				if (median.containsKey(referencePointNameA) && median.containsKey(pointName))
					medianObsB.put(groupId, median);
			}

			for (Map<String, Double> medianA : medianObsA.values()) {
				double rAB = medianA.get(referencePointNameB);
				double rAN = medianA.get(pointName);
				for (Map<String, Double> medianB : medianObsB.values()) {
					double rBA = medianB.get(referencePointNameA);
					double rBN = medianB.get(pointName);

					forwardIntersections.add(tmpReferencePointA, tmpReferencePointB, pointName, rAB, rAN, rBA, rBN);
				}
			}
		}

		Map<String, ForwardIntersectionEntry> forwardIntersectionMap = forwardIntersections.getForwardIntersectionsByFixPoints(tmpReferencePointA, tmpReferencePointB);
		if (forwardIntersectionMap != null) {
			for ( ForwardIntersectionEntry forwardIntersection : forwardIntersectionMap.values() ) {
				Point2D intersectionPoint = forwardIntersection.adjust();

				referencePointA.addBundle(true);
				PointBundle bundle = referencePointA.getCurrentBundle();
				bundle.addPoint( intersectionPoint );
				bundle.addPoint( tmpReferencePointB );

				referencePointB.addBundle(true);
				bundle = referencePointB.getCurrentBundle();
				bundle.addPoint( new Point2D(intersectionPoint.getName(), intersectionPoint.getX() - offsetX, intersectionPoint.getY() - offsetY));
				bundle.addPoint( new Point2D(tmpReferencePointA.getName(), tmpReferencePointA.getX() - offsetX, tmpReferencePointA.getY() - offsetY));
			}
		}
	}


	/**
	 * Liefert die Beobachtungen mit beruecksichtigung der Gruppen. Sinnvoll bspw. bei Richtungen, da hier die 
	 * Saetze eine Orientierung haben. Die Reihenfolge Start-/Zielpunkt wird bei Strecken und Hoehenunterschieden 
	 * nicht beachtet!
	 * 
	 * @param startPointName
	 * @param endPointName
	 * @param type
	 * @return observations
	 */
	private Map<Integer, List<TerrestrialObservationRow>> getObservations(String startPointName, String endPointName, ObservationType type, int groupId) throws SQLException {
		Map<Integer, List<TerrestrialObservationRow>> observations = new LinkedHashMap<Integer, List<TerrestrialObservationRow>>();
		boolean isAngle = type == ObservationType.DIRECTION || type == ObservationType.ZENITH_ANGLE;
		String sqlFormatObs[] = new String[isAngle ? 1 : 2]; 

		sqlFormatObs[0] = "SELECT " +
				"\"ObservationApriori\".\"end_point_name\" AS \"end_point_name\", " +
				"\"ObservationApriori\".\"group_id\" AS \"group_id\", " +

				"\"ObservationApriori\".\"id\" AS \"id\", " +

				"\"ObservationApriori\".\"instrument_height\" AS \"instrument_height\", " +
				"\"ObservationApriori\".\"reflector_height\" AS \"reflector_height\", " +

				"\"ObservationApriori\".\"value_0\" AS \"value_0\" " +

				"FROM \"ObservationApriori\" INNER JOIN \"ObservationGroup\" ON " +
				"\"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" AND " +
				"\"ObservationGroup\".\"type\" = ? AND " +
				"\"ObservationApriori\".\"start_point_name\" = ? AND " +
				(endPointName!=null?"\"ObservationApriori\".\"end_point_name\" = ? AND ":"") +
				(groupId>=0?"\"ObservationApriori\".\"group_id\" = ? AND ":"") +
				"\"ObservationApriori\".\"enable\" = TRUE AND " +
				"\"ObservationGroup\".\"enable\" = TRUE " +

				"INNER JOIN \"PointApriori\" AS \"ps\" " +
				"ON \"ObservationApriori\".\"start_point_name\" = \"ps\".\"name\" AND \"ps\".\"enable\" = TRUE " +
				"INNER JOIN \"PointApriori\" AS \"pe\" " +
				"ON \"ObservationApriori\".\"end_point_name\" = \"pe\".\"name\" AND \"pe\".\"enable\" = TRUE " +

				"INNER JOIN \"PointGroup\" AS \"gs\" " +
				"ON \"ps\".\"group_id\" = \"gs\".\"id\" AND \"gs\".\"enable\" = TRUE " +
				"INNER JOIN \"PointGroup\" AS \"ge\" " +
				"ON \"pe\".\"group_id\" = \"ge\".\"id\" AND \"ge\".\"enable\" = TRUE " +						

				"ORDER BY \"ObservationApriori\".\"id\" ASC"; 

		// Abstaende (Strecken und Hoehenunterschiede) werden im Hin- und Rueckweg ausgelesen,
		// beim Nivellement wird der Wert negiert --> -dH
		// SQL ist identisch mit o.g.; vertauscht jedoch Start- und Zielpunkt; 
		if (!isAngle) {
			sqlFormatObs[1] = "SELECT " +
					"\"ObservationApriori\".\"start_point_name\" AS \"end_point_name\", " +
					"\"ObservationApriori\".\"group_id\" AS \"group_id\", " +

					"\"ObservationApriori\".\"id\" AS \"id\", " +

					"\"ObservationApriori\".\"instrument_height\" AS \"reflector_height\", " +
					"\"ObservationApriori\".\"reflector_height\" AS \"instrument_height\", " +

					(type == ObservationType.LEVELING?"-":"") + "\"ObservationApriori\".\"value_0\" AS \"value_0\" " +

					"FROM \"ObservationApriori\" INNER JOIN \"ObservationGroup\" ON " +
					"\"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" AND " +
					"\"ObservationGroup\".\"type\" = ? AND " +
					"\"ObservationApriori\".\"end_point_name\" = ? AND " +
					(endPointName!=null?"\"ObservationApriori\".\"start_point_name\" = ? AND ":"") +
					(groupId>=0?"\"ObservationApriori\".\"group_id\" = ? AND ":"") +
					"\"ObservationApriori\".\"enable\" = TRUE AND " +
					"\"ObservationGroup\".\"enable\" = TRUE " +

					"INNER JOIN \"PointApriori\" AS \"ps\" " +
					"ON \"ObservationApriori\".\"start_point_name\" = \"ps\".\"name\" AND \"ps\".\"enable\" = TRUE " +
					"INNER JOIN \"PointApriori\" AS \"pe\" " +
					"ON \"ObservationApriori\".\"end_point_name\" = \"pe\".\"name\" AND \"pe\".\"enable\" = TRUE " +

					"INNER JOIN \"PointGroup\" AS \"gs\" " +
					"ON \"ps\".\"group_id\" = \"gs\".\"id\" AND \"gs\".\"enable\" = TRUE " +
					"INNER JOIN \"PointGroup\" AS \"ge\" " +
					"ON \"pe\".\"group_id\" = \"ge\".\"id\" AND \"ge\".\"enable\" = TRUE " +						

					"ORDER BY \"ObservationApriori\".\"id\" ASC";
		}

		PreparedStatement statementPointObservations = null;
		for (String sqlFormatOb : sqlFormatObs) {
			int col = 1;
			statementPointObservations = this.dataBase.getPreparedStatement(sqlFormatOb);
			statementPointObservations.setInt(col++, type.getId());
			statementPointObservations.setString(col++, startPointName);
			if (endPointName != null)
				statementPointObservations.setString(col++, endPointName);
			if (groupId >= 0)
				statementPointObservations.setInt(col++, groupId);

			ResultSet obsSet = statementPointObservations.executeQuery();
			while (obsSet.next()) {
				endPointName = obsSet.getString("end_point_name");
				int group_id = obsSet.getInt("group_id");
				double value = obsSet.getDouble("value_0");
				double ih = obsSet.getDouble("instrument_height");
				double th = obsSet.getDouble("reflector_height");

				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(startPointName);
				obs.setEndPointName(endPointName);
				obs.setInstrumentHeight(ih);
				obs.setReflectorHeight(th);
				obs.setValueApriori(value);

				if (observations.containsKey(group_id)) {
					observations.get(group_id).add(obs);
				}
				else {
					List<TerrestrialObservationRow> obsList = new ArrayList<TerrestrialObservationRow>();
					obsList.add(obs);
					observations.put(group_id, obsList);
				}
			}
		}
		return observations;
	}

	/**
	 * Liefert die terr. Beobachtungen *ohne* Beruecksichtigung der Gruppen. Die Reihenfolge 
	 * Start-/Zielpunkt wird bei Strecken und Hoehenunterschieden nicht beachtet!
	 * 
	 * @param startPointName
	 * @param endPointName
	 * @param type
	 * @return observations
	 */
	private List<TerrestrialObservationRow> getTerrestrialObservationsIgnoreGroups(String startPointName, ObservationType type) throws SQLException {
		List<TerrestrialObservationRow> observations = new ArrayList<TerrestrialObservationRow>();

		String sqlFormatObs = "SELECT " +
				"\"ObservationApriori\".\"end_point_name\" AS \"end_point_name\", " +
				"\"ObservationApriori\".\"group_id\" AS \"group_id\", " +

				"\"ObservationApriori\".\"instrument_height\" AS \"instrument_height\", " +
				"\"ObservationApriori\".\"reflector_height\" AS \"reflector_height\", " +

				"\"ObservationApriori\".\"value_0\" AS \"value_0\" " +

				"FROM \"ObservationApriori\" JOIN \"ObservationGroup\" ON " +
				"\"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" AND " +
				"\"ObservationGroup\".\"type\" = ? AND " +
				"\"ObservationApriori\".\"start_point_name\" = ? AND " +
				"\"ObservationApriori\".\"enable\" = TRUE AND " +
				"\"ObservationGroup\".\"enable\" = TRUE " +

				"JOIN \"PointApriori\" AS \"ps\" " +
				"ON \"ObservationApriori\".\"start_point_name\" = \"ps\".\"name\" AND \"ps\".\"enable\" = TRUE " +
				"JOIN \"PointApriori\" AS \"pe\" " +
				"ON \"ObservationApriori\".\"end_point_name\" = \"pe\".\"name\" AND \"pe\".\"enable\" = TRUE " +

				"JOIN \"PointGroup\" AS \"gs\" " +
				"ON \"ps\".\"group_id\" = \"gs\".\"id\" AND \"gs\".\"enable\" = TRUE " +
				"JOIN \"PointGroup\" AS \"ge\" " +
				"ON \"pe\".\"group_id\" = \"ge\".\"id\" AND \"ge\".\"enable\" = TRUE";

		// Abstaende (Strecken und Hoehenunterschiede) werden im Hin- und Rueckweg ausgelesen,
		// beim Nivellement wird der Wert negiert --> -dH
		// Fuer Zenitwinkel wird die Gegenvisur bestimmt --> z = 200 - zHin
		// SQL ist identisch mit o.g.; vertauscht jedoch Start- und Zielpunkt; 
		if (type != ObservationType.DIRECTION) {
			sqlFormatObs += " UNION ALL SELECT " +
					"\"ObservationApriori\".\"start_point_name\" AS \"end_point_name\", " +
					"\"ObservationApriori\".\"group_id\" AS \"group_id\", " +

					"\"ObservationApriori\".\"instrument_height\" AS \"reflector_height\", " +
					"\"ObservationApriori\".\"reflector_height\" AS \"instrument_height\", " +

					(type == ObservationType.LEVELING ? "-" : type == ObservationType.ZENITH_ANGLE ? "PI()-" : "") + "\"ObservationApriori\".\"value_0\" AS \"value_0\" " +

					"FROM \"ObservationApriori\" JOIN \"ObservationGroup\" ON " +
					"\"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" AND " +
					"\"ObservationGroup\".\"type\" = ? AND " +
					"\"ObservationApriori\".\"end_point_name\" = ? AND " +
					"\"ObservationApriori\".\"enable\" = TRUE AND " +
					"\"ObservationGroup\".\"enable\" = TRUE " +

					"JOIN \"PointApriori\" AS \"ps\" " +
					"ON \"ObservationApriori\".\"start_point_name\" = \"ps\".\"name\" AND \"ps\".\"enable\" = TRUE " +
					"JOIN \"PointApriori\" AS \"pe\" " +
					"ON \"ObservationApriori\".\"end_point_name\" = \"pe\".\"name\" AND \"pe\".\"enable\" = TRUE " +

					"JOIN \"PointGroup\" AS \"gs\" " +
					"ON \"ps\".\"group_id\" = \"gs\".\"id\" AND \"gs\".\"enable\" = TRUE " +
					"JOIN \"PointGroup\" AS \"ge\" " +
					"ON \"pe\".\"group_id\" = \"ge\".\"id\" AND \"ge\".\"enable\" = TRUE";
		}

		PreparedStatement statementPointObservations = null;
		statementPointObservations = this.dataBase.getPreparedStatement(sqlFormatObs);				
		statementPointObservations.setInt(1, type.getId());
		statementPointObservations.setString(2, startPointName);
		if (type != ObservationType.DIRECTION) {
			statementPointObservations.setInt(3, type.getId());
			statementPointObservations.setString(4, startPointName);
		}

		ResultSet obsSet = statementPointObservations.executeQuery();
		while (obsSet.next()) {
			String endPointName = obsSet.getString("end_point_name");
			double value = obsSet.getDouble("value_0");
			double ih = obsSet.getDouble("instrument_height");
			double th = obsSet.getDouble("reflector_height");
			TerrestrialObservationRow obs = new TerrestrialObservationRow();
			obs.setStartPointName(startPointName);
			obs.setEndPointName(endPointName);
			obs.setInstrumentHeight(ih);
			obs.setReflectorHeight(th);
			obs.setValueApriori(value);
			observations.add(obs);
		}
		return observations;
	}

	private List<String> getPointNames(int dim, boolean stationsOnly) throws SQLException {
		List<String> pointNames = new ArrayList<String>();
		String sqlFormatPoints;

		if (stationsOnly) {
			sqlFormatPoints = "SELECT DISTINCT \"start_point_name\" AS \"name\" FROM \"ObservationApriori\" " +
					"JOIN \"PointApriori\" ON \"PointApriori\".\"name\" = \"ObservationApriori\".\"start_point_name\" " +
					"JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" " +
					"WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE AND \"PointGroup\".\"enable\" = TRUE AND " +
					"\"PointGroup\".\"dimension\" = ?";
		}
		else 
			sqlFormatPoints = "SELECT \"name\" FROM \"PointApriori\" " +
					"JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" " +
					"WHERE \"PointGroup\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE AND " +
					"\"PointGroup\".\"dimension\" = ?";

		PreparedStatement statementPoints = this.dataBase.getPreparedStatement(sqlFormatPoints);
		statementPoints.setInt(1, dim);
		ResultSet pointNameSet = statementPoints.executeQuery();
		while (pointNameSet.next()) {
			String pointName = pointNameSet.getString("name");
			pointNames.add(pointName);
		}
		return pointNames;
	}

	private void initTargetSystems(boolean isFreeNet) throws SQLException {
		String sqlGroup = "SELECT \"id\" FROM \"PointGroup\" WHERE (\"type\" = ? OR \"type\" = ?) AND \"dimension\" = ? AND \"enable\" = TRUE ORDER BY \"id\" ASC";
		String sqlPoint = "SELECT \"name\", \"x0\", \"y0\", \"z0\" FROM \"PointApriori\" WHERE \"group_id\" = ? AND \"enable\" = TRUE ORDER BY \"id\" ASC";
		PointBundle targetSystem1d = new PointBundle(1), targetSystem2d = new PointBundle(2);

		PreparedStatement statementGroup = this.dataBase.getPreparedStatement(sqlGroup);
		PreparedStatement statementPoint = this.dataBase.getPreparedStatement(sqlPoint);

		for (int pointDim = 1; pointDim < 4; pointDim++) {
			if (!isFreeNet) {
				statementGroup.setInt(1, PointType.REFERENCE_POINT.getId());
				statementGroup.setInt(2, PointType.STOCHASTIC_POINT.getId());
			}
			else {
				statementGroup.setInt(1, PointType.DATUM_POINT.getId());
				statementGroup.setInt(2, PointType.DATUM_POINT.getId());
			}
			statementGroup.setInt(3, pointDim);

			ResultSet groupSet = statementGroup.executeQuery();
			while (groupSet.next()) {
				statementPoint.setInt(1, groupSet.getInt("id"));

				ResultSet pointsSet = statementPoint.executeQuery();
				while (pointsSet.next()) {
					if (pointDim == 1) {
						Point point = new Point1D(
								pointsSet.getString("name"), 
								pointsSet.getDouble("z0")
								);
						if (targetSystem1d.get(point.getName()) == null)
							targetSystem1d.addPoint(point);
					}
					else if (pointDim == 2) {
						Point point = new Point2D(
								pointsSet.getString("name"), 
								pointsSet.getDouble("x0"),
								pointsSet.getDouble("y0")
								);
						if (targetSystem2d.get(point.getName()) == null)
							targetSystem2d.addPoint(point);
					}
					else if (pointDim == 3) {
						Point point1d = new Point1D(
								pointsSet.getString("name"), 
								pointsSet.getDouble("z0")												
								);

						Point point2d = new Point2D(
								pointsSet.getString("name"), 
								pointsSet.getDouble("x0"),
								pointsSet.getDouble("y0")											
								);
						if (targetSystem1d.get(point1d.getName()) == null)
							targetSystem1d.addPoint(point1d);
						if (targetSystem2d.get(point2d.getName()) == null)
							targetSystem2d.addPoint(point2d);
					}
				}
			}
		}

		if (targetSystem1d != null && targetSystem1d.size() > 0)
			this.targetSystem1d = targetSystem1d;

		if (targetSystem2d != null && targetSystem2d.size() > 0)
			this.targetSystem2d = targetSystem2d;
	}

	private void savePoints(PointBundle bundle) throws SQLException {
		for (int i=0; bundle != null && i<bundle.size(); i++)
			this.savePoint(bundle.get(i));
	}

	private void savePoint(Point p) throws SQLException {
		if (p == null)
			return;

		String sqlPoint = null;

		if (p.getDimension() == 1)
			sqlPoint = "UPDATE \"PointApriori\" SET \"z0\" = ? WHERE \"name\" = ?";
		else if (p.getDimension() == 2)
			sqlPoint = "UPDATE \"PointApriori\" SET \"x0\" = ?, \"y0\" = ? WHERE \"name\" = ?";
		else
			return;

		PreparedStatement statementPoint = this.dataBase.getPreparedStatement(sqlPoint);
		if (p.getDimension() == 1) {
			statementPoint.setDouble(1,p.getZ());
			statementPoint.setString(2,p.getName());
		}
		else if (p.getDimension() == 2) {
			statementPoint.setDouble(1,p.getX());
			statementPoint.setDouble(2,p.getY());
			statementPoint.setString(3,p.getName());
		}	

		statementPoint.execute();			 
	}

	public void transferAposteriori2AprioriValues() throws SQLException {
		String sql = "UPDATE \"AdditionalParameterApriori\" "
				+ "SET \"value_0\" = "
				+ "CASE WHEN "
				+ "(SELECT \"value\" FROM \"AdditionalParameterAposteriori\" WHERE \"AdditionalParameterApriori\".\"id\" = \"AdditionalParameterAposteriori\".\"id\")  "
				+ "ELSE \"value_0\" "
				+ "END "
				+ "WHERE \"enable\" = TRUE";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.execute();
	}
	
	public void transferAposteriori2AprioriValues(boolean transferDatumPoints) throws SQLException {
		String sql = "UPDATE \"AdditionalParameterApriori\" "
				+ "SET \"value_0\" = IFNULL("
				+ "(SELECT \"value\" FROM \"AdditionalParameterAposteriori\" WHERE \"AdditionalParameterApriori\".\"id\" = \"AdditionalParameterAposteriori\".\"id\"),  "
				+ "\"value_0\") "
				+ "WHERE \"enable\" = TRUE";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.execute();

		sql = "UPDATE \"PointApriori\" SET "
				+ "\"x0\" = IFNULL("
				+ "(SELECT \"x\" FROM \"PointAposteriori\" JOIN \"PointGroup\" ON \"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE AND \"PointGroup\".\"type\" IN (?, ?) WHERE \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" AND \"enable\" = TRUE), "
				+ "\"x0\"), "
				+ "\"y0\" = IFNULL("
				+ "(SELECT \"y\" FROM \"PointAposteriori\" JOIN \"PointGroup\" ON \"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE AND \"PointGroup\".\"type\" IN (?, ?) WHERE \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" AND \"enable\" = TRUE), "
				+ "\"y0\"), "
				+ "\"z0\" = IFNULL("
				+ "(SELECT \"z\" FROM \"PointAposteriori\" JOIN \"PointGroup\" ON \"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE AND \"PointGroup\".\"type\" IN (?, ?) WHERE \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" AND \"enable\" = TRUE), "
				+ "\"z0\"), "
				+ "\"dy0\" = IFNULL("
				+ "(SELECT \"dy\" FROM \"DeflectionAposteriori\" JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE  AND \"PointGroup\".\"dimension\" = 3 AND \"PointGroup\".\"type\" IN (?, ?) WHERE \"DeflectionAposteriori\".\"id\" = \"PointApriori\".\"id\"), "
				+ "\"dy0\"), "
				+ "\"dx0\" = IFNULL("
				+ "(SELECT \"dx\" FROM \"DeflectionAposteriori\" JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" AND \"PointGroup\".\"enable\" = TRUE  AND \"PointGroup\".\"dimension\" = 3 AND \"PointGroup\".\"type\" IN (?, ?) WHERE \"DeflectionAposteriori\".\"id\" = \"PointApriori\".\"id\"), "
				+ "\"dx0\") "
				+ "WHERE \"enable\" = TRUE";

		stmt = this.dataBase.getPreparedStatement(sql);
		for (int i=1, j=1; i <= 5; i++) {
			stmt.setInt(j++, PointType.NEW_POINT.getId());
			stmt.setInt(j++, transferDatumPoints ? PointType.DATUM_POINT.getId() : PointType.NEW_POINT.getId());
		}

		stmt.execute();

		this.estimationStatus1D = EstimationStateType.ERROR_FREE_ESTIMATION;
		this.estimationStatus2D = EstimationStateType.ERROR_FREE_ESTIMATION;
	}

	/**
	 * Liefert das Azimut von einem Punkt - Resultierend aus einem Richtungssatz und einem Bezugssystem
	 * @param newPointId
	 * @param directionSet
	 * @param bundle
	 * @return
	 */
	private TerrestrialObservationRow getOrientatedDirectionAngle(String newPointId, List<TerrestrialObservationRow> directionSet, PointBundle bundle) {
		Map<String, Double> medianDirectionSet = this.getMedianDirections(directionSet);
		int count = medianDirectionSet.size();

		String startPointName = directionSet.get(0).getStartPointName();
		Point2D startPoint = (Point2D)bundle.get(startPointName);

		if (startPoint == null)
			return null;

		List<Double> o = new ArrayList<Double>(count);
		int i=0;
		for (Map.Entry<String, Double> direction : medianDirectionSet.entrySet()) {
			String endPointName = direction.getKey();
			Point2D endPoint = (Point2D)bundle.get(endPointName);

			if (endPoint == null)
				continue;

			double obsDir = direction.getValue();
			double calDir = ClassicGeodeticComputation.DIRECTION(startPoint, endPoint);

			double tmp_o = obsDir - calDir;
			tmp_o = MathExtension.MOD(tmp_o, 2.0*Math.PI);
			if (i>0 && (2.0*Math.PI)-Math.abs(o.get(i-1)-tmp_o)<0.5)
				if (tmp_o<o.get(i-1))
					tmp_o += 2.0*Math.PI;
				else
					tmp_o -= 2.0*Math.PI;
			o.add(tmp_o);  
			i++;
		}

		if (o.size() == 0)
			return null;

		Collections.sort(o);
		double ori = o.get(o.size()/2);

		if (medianDirectionSet.containsKey(newPointId)) {
			TerrestrialObservationRow row = new TerrestrialObservationRow();
			row.setStartPointName(startPointName);
			row.setEndPointName(newPointId);
			row.setValueApriori(MathExtension.MOD(medianDirectionSet.get(newPointId) - ori, 2.0*Math.PI));
			return row;
		}
		return null;
	}

	private Point getMedianPoint(List<? extends Point> points) {
		if (points.size() == 1)
			return points.get(0);
		else if (points.size() > 1) {
			int index = 0;

			List<Double> medianX = new ArrayList<Double>();
			List<Double> medianY = new ArrayList<Double>();
			List<Double> medianZ = new ArrayList<Double>();

			for (int i=0; i<points.size(); i++) {
				medianX.add(points.get(i).getX());
				medianY.add(points.get(i).getY());
				medianZ.add(points.get(i).getZ());
			}

			Collections.sort(medianX);
			Collections.sort(medianY);
			Collections.sort(medianZ);

			double x0 = medianX.get(medianX.size()/2);
			double y0 = medianY.get(medianX.size()/2);
			double z0 = medianZ.get(medianX.size()/2);

			double norm2 = Double.MAX_VALUE;
			for (int i=0; i<points.size(); i++) {
				double x = points.get(i).getX() - x0;
				double y = points.get(i).getY() - y0;
				double z = points.get(i).getZ() - z0;

				double norm = Math.sqrt(x*x + y*y + z*z);
				if (norm < norm2) {
					norm2 = norm;
					index = i;
				}
			}
			return points.get(index);
		}
		return null;
	}

	public EstimationStateType getEstimationStatus1D() {
		return this.estimationStatus1D;
	}

	public EstimationStateType getEstimationStatus2D() {
		return this.estimationStatus2D;
	}

	public Set<String> getUnderdeterminedPointNames1D() {
		return this.underdeterminedPointNames1D;
	}

	public Set<String> getUnderdeterminedPointNames2D() {
		return this.underdeterminedPointNames2D;
	}

	public void setEstimateDatumsPoints(boolean estimate) {
		this.estimateDatumPoints = estimate;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.change.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		this.change.firePropertyChange(event);
	}
}
