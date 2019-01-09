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

package org.applied_geodesy.adjustment.network.approximation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.BundleTransformation;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.BundleTransformation1D;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.BundleTransformation2D;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation;

public class AutomatedApproximationAdjustment implements PropertyChangeListener {
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	private List<PointBundle> systems;
	private BundleTransformation bundleTransformation = null;
	private EstimationStateType currentEstimationStatus = EstimationStateType.BUSY;
	private int systemsCounter = 0;
	private PointBundle targetSystem;
	private double threshold = 15.0;
	private boolean estimateDatumPoints = false,
					freeNetwork         = true;
	private Set<String> outliers = new HashSet<String>();
	
	public AutomatedApproximationAdjustment(PointBundle targetSystem, List<Point> points) {
		this.systems = this.getBundles(points);
		this.targetSystem = targetSystem;
	}
	
	public void addSystems(List<PointBundle> bundles) {
		this.systems.addAll(bundles);
	}
	
	public Set<String> getOutliers() {
		return this.outliers;
	}
	
	public void setFreeNetwork(boolean freeNetwork) {
		this.freeNetwork = freeNetwork;
	}
	
	public void setEstimateDatumPoints(boolean estimateDatumPoints) {
		this.estimateDatumPoints = estimateDatumPoints;
	}
	
	public PointBundle getTargetSystem() {
		return this.targetSystem;
	}
	
	public EstimationStateType getCurrentEstimationStatus() {
		return this.currentEstimationStatus;
	}
	
	public int getSubSystemsCounter() {
		return this.systemsCounter;
	}
	
	private List<PointBundle> getBundles(List<Point> points) {
		List<PointBundle> systems = new ArrayList<PointBundle>();
		for (Point p : points) {
			List<PointBundle> bundles = p.getPointBundles();
			for (PointBundle bundle : bundles)
				systems.add( bundle );
		}
		return systems;
	}
	
	public EstimationStateType estimateApproximatedValues() {
		EstimationStateType status = EstimationStateType.BUSY;
		this.bundleTransformation = null;
		
		if (this.systems != null && this.systems.size() > 0) {
			if (this.targetSystem != null && (!this.freeNetwork || (this.freeNetwork && !this.estimateDatumPoints)))
				this.systems.add(this.targetSystem);
			
			this.systemsCounter = 0;
			
			if (this.systems == null || this.systems.size() < 1)
				return EstimationStateType.NOT_INITIALISED;
			else if (this.systems.size() == 1 && this.targetSystem == null) {
				this.targetSystem = this.systems.get(0);
				return EstimationStateType.ERROR_FREE_ESTIMATION;
			}
			int dim = this.systems.get(0).getDimension();

			while(this.systemsCounter != this.systems.size() && this.systems.size() > 1) {
				this.systemsCounter = this.systems.size();
				if (dim == 1)
					this.bundleTransformation = new BundleTransformation1D(this.threshold, this.systems);
				else if (dim == 2)
					this.bundleTransformation = new BundleTransformation2D(this.threshold, this.systems);
				else 
					return EstimationStateType.NOT_INITIALISED;
				
				this.bundleTransformation.addPropertyChangeListener(this);
				status = this.bundleTransformation.estimateModel();

				this.systems = this.bundleTransformation.getExcludedSystems();
				this.systems.add(this.bundleTransformation.getTargetSystem());
				
				Set<String> outliers = this.bundleTransformation.getOutliers();
				if (outliers.size() > 0)
					this.outliers.addAll(outliers);
		    }
			this.systemsCounter = this.systems.size();
		    
		    if (this.bundleTransformation != null && this.systems.size() > 0) {
		    	// Wenn es eine freie AGL ist,
		    	// nimm das groesste verbleibende System als 
		    	// finales Zielsystem
		    	if (this.freeNetwork && this.estimateDatumPoints || this.targetSystem == null) {
			    	this.targetSystem = this.getLargestPointBundle(); //systems.get(0);
		    	}
		    	// Wenn Anschluss vorgegeben, dann
		    	// transformiere auf FP-Feld
		    	else {
		    		Transformation trans = this.bundleTransformation.getSimpleTransformationModel(this.getLargestPointBundle(), this.targetSystem);
			    	
			    	if (trans != null && trans.transformL2Norm()) {
		    			this.targetSystem = trans.getTransformdPoints();
			    	}
			    	else {
		    			this.targetSystem = null;
			    	}
		    	}
			}

		    if (this.bundleTransformation != null)
		    	this.bundleTransformation.removePropertyChangeListener(this);
		    this.bundleTransformation = null;
		    
		    return status;
		}
		else 
			status = EstimationStateType.ERROR_FREE_ESTIMATION;

		return status;
	}
	
	public PointBundle getLargestPointBundle() {
		PointBundle maxBundle = this.systems.get(0);
		for (PointBundle bundle : this.systems)
			if (maxBundle.size() < bundle.size())
				maxBundle = bundle;
		return maxBundle;
	}

	public List<PointBundle> getSystems() {
		return this.systems;
	}

	public void interrupt() {
		if (this.bundleTransformation != null)
			this.bundleTransformation.interrupt();
	}
	
	public void setThreshold(double t) {
		this.threshold = t >= 1.0 ? t:1.0;
	}
	
	public double getThreshold() {
		return this.threshold;
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