package org.applied_geodesy.adjustment.network.approximation.bundle.intersection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;

public class ForwardIntersectionSet {
	
	private class PointCouple {
		private final Point2D pointA, pointB;
		public PointCouple(Point2D pointA, Point2D pointB) {
			this.pointA = pointA;
			this.pointB = pointB;
		}
		
		public Point2D getPointA() {
			return this.pointA;
		}
		
		public Point2D getPointB() {
			return this.pointB;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			
			if (pointA.getName().compareTo(pointB.getName()) < 0) {
				result = prime * result + pointA.getName().hashCode();
				result = prime * result + pointB.getName().hashCode();
			}
			else {
				result = prime * result + pointB.getName().hashCode();
				result = prime * result + pointA.getName().hashCode();
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			PointCouple other = (PointCouple) obj;
			
			return this.pointA.getName().equals(other.getPointA().getName()) && this.pointB.getName().equals(other.getPointB().getName()) || 
				   this.pointA.getName().equals(other.getPointB().getName()) && this.pointB.getName().equals(other.getPointA().getName());
		}

		private ForwardIntersectionSet getOuterType() {
			return ForwardIntersectionSet.this;
		}
	}

	Map<PointCouple, Map<String, ForwardIntersectionEntry>> forwardIntersectionMap = new LinkedHashMap<PointCouple, Map<String, ForwardIntersectionEntry>>();
		
	public void add(Point2D fixPointA, Point2D fixPointB, String newPointId, double rAB, double rAN, double rBA, double rBN) {
		ForwardIntersectionEntry forwardIntersectionEntry;

		if (fixPointA.getName().compareTo(fixPointB.getName()) > 0) {
			double tmpR = rAB;
			rAB = rBA;
			rBA = tmpR;
			
			tmpR = rAN;
			rAN = rBN;
			rBN = tmpR;
			
			Point2D tmpPoint = fixPointA;
			fixPointA = fixPointB;
			fixPointB = tmpPoint;
		}
		PointCouple pointCouple = new PointCouple(fixPointA, fixPointB);
		
		double alpha = rAB - rAN;
		double beta  = rBN - rBA;
		
		if (this.forwardIntersectionMap.containsKey(pointCouple)) {
			Map<String, ForwardIntersectionEntry> map = this.forwardIntersectionMap.get(pointCouple);
			if (map.containsKey(newPointId)) {
				forwardIntersectionEntry = map.get(newPointId);
				forwardIntersectionEntry.addAngles(alpha, beta);
			}
			else 
				map.put(newPointId, new ForwardIntersectionEntry(fixPointA, fixPointB, newPointId, alpha, beta));	
		}
		else {
			Map<String, ForwardIntersectionEntry> map = new LinkedHashMap<String, ForwardIntersectionEntry>();
			map.put(newPointId, new ForwardIntersectionEntry(fixPointA, fixPointB, newPointId, alpha, beta));
			this.forwardIntersectionMap.put(pointCouple, map);
		}
	}
	
	public Map<String, ForwardIntersectionEntry> getForwardIntersectionsByFixPoints(Point2D fixPointA, Point2D fixPointB) {
		PointCouple pointCouple = new PointCouple(fixPointA, fixPointB);
		return this.forwardIntersectionMap.get(pointCouple);
	}
	
	public List<Point2D> adjustForwardIntersections() {
		List<Point2D> intersectionPoints = new ArrayList<Point2D>();
		for (Map<String, ForwardIntersectionEntry> forwardIntersections : this.forwardIntersectionMap.values()) {
			for (ForwardIntersectionEntry forwardIntersection : forwardIntersections.values()) {
				Point2D p2d = forwardIntersection.adjust();
				if (p2d != null)
					intersectionPoints.add(p2d);
			}
		}
		return intersectionPoints;
	}
}
