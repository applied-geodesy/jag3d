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

package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.NormalEquationSystem;
import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point1D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point3D;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public abstract class BundleTransformation {
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	public final static double SQRT_EPS = 1.0E-5;
	private Map<String, Integer> pointInSystemsCounter = new LinkedHashMap<String, Integer>();
	private Set<String> outliers = new HashSet<String>();
	private EstimationStateType currentEstimationStatus = EstimationStateType.BUSY;
	private int	maxIteration            = 50,
				numberOfUnknowns        = 0,
				numberOfObservations    = 0;

	private double maxDx = Double.MIN_VALUE;
	private double omega = 0.0, threshold = 15.0;
	boolean interrupt = false;
	private PointBundle targetSystem = null;
	private Set<String> targetSystemPointIds = new HashSet<String>();
	private List<PointBundle> sourceSystems = new ArrayList<PointBundle>();
	private List<PointBundle> excludedSystems = new ArrayList<PointBundle>();

	public BundleTransformation(double threshold, List<PointBundle> sourceSystems) {
		if (sourceSystems.size() < 1)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Fehler, mind. ein System wird benoetigt! " + sourceSystems.size());
		this.setThreshold(threshold);
		this.initSystems(sourceSystems);
		this.init();
	}

	public BundleTransformation(double threshold, PointBundle sourceSystem, PointBundle targetSystem) {
		this.setThreshold(threshold);
		this.targetSystem = targetSystem;
		this.sourceSystems.add(sourceSystem);
		this.init();
	}

	public BundleTransformation(double threshold, List<PointBundle> sourceSystems, PointBundle targetSystem) {
		this.setThreshold(threshold);
		this.targetSystem  = targetSystem;
		this.sourceSystems = sourceSystems;
		this.init();
	}

	public abstract Transformation getSimpleTransformationModel(PointBundle b1, PointBundle b2);

	public abstract Transformation getSimpleTransformationModel(TransformationParameterSet transParameter);
		
	private void initSystems(List<PointBundle> sourceSystems) {
		PointBundle bundle1 = null, bundle2 = null;
		int numIdentPoints = 0;
		for (int i=0; i<sourceSystems.size(); i++) {
			PointBundle b1 = sourceSystems.get(i);

			for (int j=i+1; j<sourceSystems.size(); j++) {
				PointBundle b2 = sourceSystems.get(j);
				Transformation t = this.getSimpleTransformationModel(b1, b2);

				if (t != null && t.numberOfIdenticalPoints() >= t.numberOfRequiredPoints() && t.numberOfIdenticalPoints() >= numIdentPoints) {
					numIdentPoints = t.numberOfIdenticalPoints();
					bundle1 = b1;
					bundle2 = b2;
				}
			}
		}
		if (bundle1 != null && bundle2 != null) {
			if (bundle1.size() > bundle2.size()) {
				this.targetSystem = bundle1;
				sourceSystems.remove(bundle1);
			}
			else {
				this.targetSystem = bundle2;
				sourceSystems.remove(bundle2);
			}
		}
		else {
			this.targetSystem = sourceSystems.get(0);
			sourceSystems.remove(sourceSystems.get(0));
		}
		this.sourceSystems = sourceSystems;
	}

	public final int getDimension() {
		return this.targetSystem.getDimension();
	}

	private void calculateTargetSystemApproximatedCoordinates() {
		List<PointBundle> pointSystems = new ArrayList<PointBundle>();
		int dim = this.getDimension();
		// Fuege alle Quellsysteme hinzu
		for (PointBundle sourceSystem : this.sourceSystems) {
			PointBundle pointBundleDeepCopy = new PointBundle(dim, sourceSystem.isIntersection());
			for (int i=0; i<sourceSystem.size(); i++) {
				Point p = sourceSystem.get(i);
				Point cp = null;
				if (dim == 1) 
					cp = new Point1D(p.getName(), p.getZ());
				else if (dim == 2) 
					cp = new Point2D(p.getName(), p.getX(), p.getY());
				else if (dim == 3) 
					cp = new Point3D(p.getName(), p.getX(), p.getY(), p.getZ());
				if (cp != null)
					pointBundleDeepCopy.addPoint(cp);
			}
			if (pointBundleDeepCopy.size() > 0) {
				pointSystems.add(pointBundleDeepCopy);
			}
		}
		// Fuege Zielsystem hinzu
		PointBundle pointBundleDeepCopy = new PointBundle(dim, this.targetSystem.isIntersection());
		for (int i=0; i<this.targetSystem.size(); i++) {
			Point p = this.targetSystem.get(i);
			String id = p.getName();
			this.targetSystemPointIds.add(id);
			if (this.pointInSystemsCounter.containsKey(id))
				this.pointInSystemsCounter.put(id, pointInSystemsCounter.get(id) + 1);
			else
				this.pointInSystemsCounter.put(id, 1);
			
			Point cp = null;
			if (dim == 1) 
				cp = new Point1D(id, p.getZ());
			else if (dim == 2) 
				cp = new Point2D(id, p.getX(), p.getY());
			else if (dim == 3) 
				cp = new Point3D(id, p.getX(), p.getY(), p.getZ());
			if (cp != null)
				pointBundleDeepCopy.addPoint(cp);
		}
		if (pointBundleDeepCopy.size() > 0) {
			pointSystems.add(pointBundleDeepCopy);
		}
	
		int numberOfIdenticalPoints = 0;
		boolean transformedSystems = true;
		while (transformedSystems && pointSystems.size() > 0) {
			PointBundle maxSRC = null;
			PointBundle maxTRG = null;
			numberOfIdenticalPoints = 0;
			for (int i=0; i<pointSystems.size(); i++) {
				PointBundle bundleOne = pointSystems.get(i);
				
				for (int j=i+1; j<pointSystems.size(); j++) {
					int numIdents = 0;
					PointBundle bundleTwo = pointSystems.get(j);
					// Zaehle die identischen Punkte
					for (int o=0; o<bundleOne.size(); o++) {
						Point pOne = bundleOne.get(o);
						Point pTwo = bundleTwo.get(pOne.getName());
						if (pTwo != null)
							numIdents++;
					}					
					if (numIdents > numberOfIdenticalPoints) {
						maxSRC = bundleOne;
						maxTRG = bundleTwo;
						numberOfIdenticalPoints = numIdents;
					}
				}
			}
			if (maxSRC != null && maxTRG != null) {
				boolean src2trg = false;

				if (!maxSRC.isIntersection() && maxTRG.isIntersection())
					src2trg = true;
				else if (maxSRC.isIntersection() && !maxTRG.isIntersection())
					src2trg = false;
				else if (maxSRC.size() < maxTRG.size())
					src2trg = false;
				else
					src2trg = true;

				// Tausche Systeme
				if (src2trg) {
					PointBundle tmpBundle = maxTRG;
					maxTRG = maxSRC;
					maxSRC = tmpBundle;
				}
				
				Transformation trans = this.getSimpleTransformationModel(maxSRC, maxTRG);

				// Ist Transformation durchfuehrbar und erfolgreich ?
				if (trans != null && trans.numberOfIdenticalPoints() >= trans.numberOfRequiredPoints()) {
					// Halte Massstab fest auf m=1 bei Naeherungswertbestimmung
					trans.setFixedParameter(TransformationParameterType.SCALE, !maxSRC.isIntersection() && !maxTRG.isIntersection());

					// Transformierte per L2Norm und pruefe, ob Ausreisser drin sind, wenn ja fuehre LMS-Bestimmung durch
					if (trans.transformL2Norm() && trans.getOmega() <= this.threshold*Math.sqrt(BundleTransformation.SQRT_EPS) || trans.transformLMS()) {
						for (int o=0; o<maxSRC.size(); o++) {
							Point src = maxSRC.get(o);
							Point trg = maxTRG.get(src.getName());
							if (trg == null) {
								trg = trans.transformPoint2TargetSystem(src);
								maxTRG.addPoint(trg);
							}
						}
					}
				}
				else {
					transformedSystems = false;
				}
				// entferne Quellsystem
				pointSystems.remove(maxSRC);
			}
			else {
				transformedSystems = false;
			}
		}
		
		// Suche das maximale System in pointSystems;
		// tritt nur ein, wenn nicht alle Systeme transformierbar sind!!!!
		PointBundle maxScrSystem = pointSystems.get(0); 
		for (int i=1; i<pointSystems.size(); i++)
			if (pointSystems.get(i).size() > maxScrSystem.size())
				maxScrSystem = pointSystems.get(i);
		// Transformiere Punkte ins Zielsystem als erste Naeherung
		Transformation trans = this.getSimpleTransformationModel(maxScrSystem, this.targetSystem);
		
		if (trans != null && trans.numberOfIdenticalPoints() >= trans.numberOfRequiredPoints()) {
			trans.setFixedParameter(TransformationParameterType.SCALE, !maxScrSystem.isIntersection() && !this.targetSystem.isIntersection());
			// Transformierte per L2Norm und pruefe, ob Ausreißer drin sind, wenn ja fuehre LMS-Bestimmung durch
			if (trans.transformL2Norm() && trans.getOmega() <= this.threshold*Math.sqrt(BundleTransformation.SQRT_EPS) || trans.transformLMS()) {
				for (int o=0; o<maxScrSystem.size(); o++) {
					Point src = maxScrSystem.get(o);
					Point trg = this.targetSystem.get(src.getName());
					if (trg == null) {
						trg = trans.transformPoint2TargetSystem(src);
						this.targetSystem.addPoint(trg);
					}
				}
			}
		}
	}
	
	private void calculateTransformationParameterApproximatedValues() {
		List<PointBundle> srcPointSystems = new ArrayList<PointBundle>();
		for (PointBundle sourceSystem : this.sourceSystems) {
			srcPointSystems.add(sourceSystem);
			
			for (int i=0; i<sourceSystem.size(); i++) {
				Point p = sourceSystem.get(i);
				p.setColInJacobiMatrix(-1);
				p.setRowInJacobiMatrix(-1);
				String id = p.getName();
				if (this.pointInSystemsCounter.containsKey(id))
					this.pointInSystemsCounter.put(id, this.pointInSystemsCounter.get(id) + 1);
				else
					this.pointInSystemsCounter.put(id, 1);
			}
		}
		
		for (int i=0; i<this.targetSystem.size(); i++) {
			Point p = this.targetSystem.get(i);
			p.setColInJacobiMatrix(-1);
			p.setRowInJacobiMatrix(-1);
// 			Wird nun bereits in der Methode calculateTargetSystemApproximatedCoordinates() zugewiesen fuer das Zielsystem
//			String id = p.getName();
//			if (this.pointInSystemsCounter.containsKey(id))
//				this.pointInSystemsCounter.put(id, pointInSystemsCounter.get(id) + 1);
//			else
//				this.pointInSystemsCounter.put(id, 1);
		}
		
		for (PointBundle sourceSystem : srcPointSystems) {
			// Transformiere System
			// --> Naeherungswertbestimmung der Trafo-Parameter
			// Sollte System nicht transformaierbar, entferne es aus Auswahl
			if (sourceSystem == null || !this.setApproximatedValues(sourceSystem)) {
				System.err.println(this.getClass().getSimpleName()+" Fehler, nicht genuegend identische\nPunkte vorhanden oder System nicht transformierbar!");
				if (sourceSystem != null) {
					this.sourceSystems.remove(sourceSystem);
					this.excludedSystems.add(sourceSystem);
				}
			}
		}
		((ArrayList<PointBundle>)this.sourceSystems).trimToSize();
	}

	private boolean setApproximatedValues(PointBundle sourceSystem) {
		int dim = this.getDimension();
		Transformation trans = this.getSimpleTransformationModel(this.targetSystem, sourceSystem);
		// Ist Transformation durchfuehrbar und erfolgreich
		if (trans == null || trans.numberOfIdenticalPoints() < trans.numberOfRequiredPoints())
			return false;
		
		// Halte Massstab fest auf m=1 bei Naeherungswertbestimmung
		trans.setFixedParameter(TransformationParameterType.SCALE, !sourceSystem.isIntersection() && !this.targetSystem.isIntersection());
		// Transformierte per L2Norm und pruefe, ob Ausreißer drin sind, wenn ja fuehre LMS-Bestimmung durch
		if (trans.transformL2Norm() && trans.getOmega() <= this.threshold*Math.sqrt(BundleTransformation.SQRT_EPS) || trans.transformLMS()) {
			this.omega = Math.max(this.omega, trans.getOmega());
			sourceSystem.setTransformationParameterSet(trans.getTransformationParameterSet());
						
			for (int i=0; i<sourceSystem.size(); i++) {
				Point pointSource = sourceSystem.get(i);

				if (this.pointInSystemsCounter.get(pointSource.getName()) <= 1)
					continue;

				Point pointTarget = this.targetSystem.get(pointSource.getName());

				if (pointTarget == null) {
					pointTarget = trans.transformPoint2SourceSystem(pointSource);
					pointTarget.setColInJacobiMatrix(this.numberOfUnknowns);
					pointSource.setColInJacobiMatrix(this.numberOfUnknowns);
					this.targetSystem.addPoint(pointTarget);
					this.numberOfUnknowns += dim;
				}
				else if (!this.targetSystemPointIds.contains(pointSource.getName()) && pointTarget.getColInJacobiMatrix() < 0) {
					pointTarget.setColInJacobiMatrix(this.numberOfUnknowns);
					pointSource.setColInJacobiMatrix(this.numberOfUnknowns);
					this.numberOfUnknowns += dim;
				}
				else {
					pointSource.setColInJacobiMatrix(pointTarget.getColInJacobiMatrix());
				}
				pointSource.setRowInJacobiMatrix(this.numberOfObservations);
				this.numberOfObservations += dim;
			}
			return true;
		}
		return false;
	}

	private void init() {
		this.numberOfUnknowns     = 0;
		this.numberOfObservations = 0;
		int dim = this.getDimension();
		
		//this.calculateApproximatedValues();

		// Bestimme Näherungskoordinatenn im Zielsystem 
		this.calculateTargetSystemApproximatedCoordinates();

		// Bestimme genaeherte Transformationsparameter
		this.calculateTransformationParameterApproximatedValues();
		
		// Ab hier nur noch transformierbare Systeme vorhanden
		for (PointBundle sourceSystem : this.sourceSystems) {
			// 	Hinzufuegen der Transformationsparameter als Unbekannte
			TransformationParameterSet transParameter = sourceSystem.getTransformationParameterSet();
			
			// Keinen Netzmassstab für Gesamtausgleichung bestimmen - sofern es kein Streckenfreies System ist (Vorwaertsschnitt)
			if (sourceSystem.isIntersection()) {
				TransformationParameter m = transParameter.get(TransformationParameterType.SCALE);
				m.setColInJacobiMatrix(this.numberOfUnknowns++);
			}
			
			if (dim != 1) {
				TransformationParameter rz = transParameter.get(TransformationParameterType.ROTATION_Z);
				rz.setColInJacobiMatrix(this.numberOfUnknowns++);
				
				TransformationParameter tx = transParameter.get(TransformationParameterType.TRANSLATION_X);
				tx.setColInJacobiMatrix(this.numberOfUnknowns++);
				
				TransformationParameter ty = transParameter.get(TransformationParameterType.TRANSLATION_Y);
				ty.setColInJacobiMatrix(this.numberOfUnknowns++);
			}

			if (dim == 3) {
				TransformationParameter rx = transParameter.get(TransformationParameterType.ROTATION_X);
				rx.setColInJacobiMatrix(this.numberOfUnknowns++);

				TransformationParameter ry = transParameter.get(TransformationParameterType.ROTATION_Y);
				ry.setColInJacobiMatrix(this.numberOfUnknowns++);
			}

			if (dim != 2) {
				TransformationParameter tz = transParameter.get(TransformationParameterType.TRANSLATION_Z);
				tz.setColInJacobiMatrix(this.numberOfUnknowns++);
			}
		}
	}

	public EstimationStateType estimateModel() {
		boolean isEstimated = false,
		isConverge = true;
		this.currentEstimationStatus = EstimationStateType.BUSY;
		this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);

		int runs = this.maxIteration-1;
		try {
			do {
				this.currentEstimationStatus = EstimationStateType.ITERATE;
				this.change.firePropertyChange(this.currentEstimationStatus.name(), this.maxIteration, this.maxIteration-runs);
				
				if (this.interrupt) {
			    	this.currentEstimationStatus = EstimationStateType.INTERRUPT;
			    	this.interrupt = false;
			    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			    	return this.currentEstimationStatus;
			    }
				
				Vector dx = new DenseVector(this.numberOfUnknowns);
				//	estimateCompleteModel = isEstimate;
				try {
					// Gleichungssystem erzeugen
					NormalEquationSystem NES = this.createNormalEquationSystem();
					if (this.interrupt || NES == null) {
				    	this.currentEstimationStatus = EstimationStateType.INTERRUPT;
				    	this.interrupt = false;
				    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
				    	return this.currentEstimationStatus;
				    }

					UpperSymmPackMatrix N = NES.getMatrix();
					DenseVector n = NES.getVector();

					if (N == null || n == null || this.numberOfObservations() < this.getDimension() || this.numberOfUnknowns() == 0) {
						this.currentEstimationStatus = EstimationStateType.NOT_INITIALISED;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						return this.currentEstimationStatus;
					}

					this.maxDx = Double.MIN_VALUE;
					
					// Loese Nx=n und ueberschreibe n durch die Loesung x
	    			MathExtension.solve(N, n, false);
	    			dx = n;
				}				
				catch (MatrixSingularException | MatrixNotSPDException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			    	e.printStackTrace();
			    	this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
			    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			    	return this.currentEstimationStatus;
			    }
			    catch (Exception e) {
			    	e.printStackTrace();
			    	this.currentEstimationStatus = EstimationStateType.INTERRUPT;
			    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			    	return this.currentEstimationStatus;
			    }

				this.updateUnknownParameters(dx);
				
				this.currentEstimationStatus = EstimationStateType.CONVERGENCE;
		    	this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
				
				if (this.maxDx <= BundleTransformation.SQRT_EPS) {
					isEstimated = true;
					runs--;
				}
				else if (runs-- <= 1) {
					isConverge = false;
					isEstimated = true;
				}
			} while (!isEstimated);
			
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			this.currentEstimationStatus = EstimationStateType.OUT_OF_MEMORY;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			return this.currentEstimationStatus;
		}

		if (!isConverge) {
			this.currentEstimationStatus = EstimationStateType.NO_CONVERGENCE;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
		}
				
		// Bestimme die Punkte, die nur in einem System vorhanden waren.
		for (PointBundle sourceSystem : this.sourceSystems) {
			Transformation trans = this.getSimpleTransformationModel(sourceSystem.getTransformationParameterSet());
			
			for (int i=0; i<sourceSystem.size(); i++) {
				Point pointSource = sourceSystem.get(i);
				Point pointTarget = this.targetSystem.get(pointSource.getName());
				if (pointTarget == null && trans != null) {
					pointTarget = trans.transformPoint2SourceSystem(pointSource);
					this.targetSystem.addPoint(pointTarget);
				}
			}
		}
		if (this.currentEstimationStatus.getId() == EstimationStateType.BUSY.getId()) {
			this.currentEstimationStatus = EstimationStateType.ERROR_FREE_ESTIMATION;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
		}
		return this.currentEstimationStatus;
	}

	private void updateUnknownParameters(Vector dx) {
		for (PointBundle sourceSystem : this.sourceSystems) {
			if (this.interrupt)
				return;
			
			// 	Transformationsparameter des lokalen Systems
			TransformationParameterSet parameters = sourceSystem.getTransformationParameterSet();
			TransformationParameter m = parameters.get(TransformationParameterType.SCALE);

			TransformationParameter rx = parameters.get(TransformationParameterType.ROTATION_X);
			TransformationParameter ry = parameters.get(TransformationParameterType.ROTATION_Y);
			TransformationParameter rz = parameters.get(TransformationParameterType.ROTATION_Z);

			TransformationParameter tx = parameters.get(TransformationParameterType.TRANSLATION_X);
			TransformationParameter ty = parameters.get(TransformationParameterType.TRANSLATION_Y);
			TransformationParameter tz = parameters.get(TransformationParameterType.TRANSLATION_Z);

			if (!m.isFixed()) {
				int col = m.getColInJacobiMatrix();
				double oldValue = m.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				m.setValue( oldValue + dx.get(col) );
			}
			if (!tx.isFixed()) {
				int col = tx.getColInJacobiMatrix();
				double oldValue = tx.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				tx.setValue( oldValue + dx.get(col) );
			}

			if (!ty.isFixed()) {
				int col = ty.getColInJacobiMatrix();
				double oldValue = ty.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				ty.setValue( oldValue + dx.get(col) );
			}

			if (!tz.isFixed()) {
				int col = tz.getColInJacobiMatrix();
				double oldValue = tz.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				tz.setValue( oldValue + dx.get(col) );
			}

			if (!rx.isFixed()) {
				int col = rx.getColInJacobiMatrix();
				double oldValue = rx.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				rx.setValue( MathExtension.MOD(oldValue + dx.get(col), 2*Math.PI) );
			}

			if (!ry.isFixed()) {
				int col = ry.getColInJacobiMatrix();
				double oldValue = ry.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				ry.setValue( MathExtension.MOD(oldValue + dx.get(col), 2*Math.PI) );
			}

			if (!rz.isFixed()) {
				int col = rz.getColInJacobiMatrix();
				double oldValue = rz.getValue();
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				rz.setValue( MathExtension.MOD(oldValue + dx.get(col), 2*Math.PI) );
			}
		}
		int dim = this.getDimension();
		for (int i=0; i<this.targetSystem.size(); i++) {
			if (this.interrupt)
				return;
			
			Point point = this.targetSystem.get(i);
			int col = point.getColInJacobiMatrix();
			if (col < 0)
				continue;
			
			if (dim != 1) {
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				point.setX(point.getX() + dx.get(col++));
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				point.setY(point.getY() + dx.get(col++));
			}
			if (dim != 2) {
				this.maxDx = Math.max(Math.abs(dx.get(col)), this.maxDx);
				point.setZ(point.getZ() + dx.get(col));
			}
		}
	}

	public List<PointBundle> getExcludedSystems() {
		return this.excludedSystems;
	}

	public final int numberOfObservations() {
		return this.numberOfObservations;
	}

	public final int numberOfUnknowns() {
		return this.numberOfUnknowns;
	}

	protected abstract NormalEquationSystem createNormalEquationSystem();

	public PointBundle getTargetSystem() {
		return this.targetSystem;
	}

	public List<PointBundle> getSourceSystems() {
		return this.sourceSystems;
	}
	
	private void setThreshold(double t) {
		this.threshold = t > 1.0 ? t : 1.0;
	}
	
	protected double getScaleEstimate() {
		double SQRT_EPS = Math.sqrt(Constant.EPS);
		double omega = this.omega>SQRT_EPS?this.omega:1.0;
		int dof = this.numberOfObservations()-this.numberOfUnknowns();
		if (dof > 0) {
			return this.threshold * 1.4826022185056*(1.0+5.0/dof)*Math.sqrt(omega);
		}
		return 1.0;
	}
	
	public Set<String> getOutliers() {
		return this.outliers;
	}
	
	protected void removeOutlierPoint(Point point) {
		String pointId = point.getName();
		if (this.outliers.contains(pointId)) {
			this.outliers.remove(pointId);
		}
	}
	
	protected void addOutlierPoint(Point point) {
		String pointId = point.getName();
		if (!this.outliers.contains(pointId)) {
			this.outliers.add(pointId);
		}
	}

	public void interrupt() {
		this.interrupt = true;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.change.removePropertyChangeListener(listener);
	}
}
