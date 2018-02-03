package org.applied_geodesy.jag3d.ui.graphic.sql;

import java.util.LinkedHashSet;
import java.util.Set;

import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;

public class ObservableMeasurement extends PointPair {
	private Set<ObservationType> observationTypesStartPoint = new LinkedHashSet<ObservationType>(10);
	private Set<ObservationType> observationTypesEndPoint   = new LinkedHashSet<ObservationType>(10);
	
	public ObservableMeasurement(GraphicPoint startPoint, GraphicPoint endPoint) {
		super(startPoint, endPoint);
	}
	
	public void addStartPointObservationType(ObservationType type) {
		this.observationTypesStartPoint.add(type);
	}
	
	public void addEndPointObservationType(ObservationType type) {
		this.observationTypesEndPoint.add(type);
	}
	
	public Set<ObservationType> getStartPointObservationType() {
		return this.observationTypesStartPoint;
	}
	
	public Set<ObservationType> getEndPointObservationType() {
		return this.observationTypesEndPoint;
	}
}
