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

package org.applied_geodesy.adjustment.network.observation.reduction;

import java.util.HashSet;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.network.point.Point3D;

public class Reduction {
	private ProjectionType projectionType = ProjectionType.LOCAL_CARTESIAN;
	private double earthRadius = Constant.EARTH_RADIUS;
	private Point3D pivotPoint = new Point3D("PIVOT_POINT_3D", 0.0, 0.0, 0.0);
	private double referenceHeight = 0.0;
	private Set<ReductionTaskType> reductionTypes = new HashSet<ReductionTaskType>(ReductionTaskType.values().length);
	
	public void setReferenceHeight(double referenceHeight) {
		this.referenceHeight = referenceHeight;
	}

	public double getReferenceHeight() {
		return this.referenceHeight;
	}
	
	public void setEarthRadius(double earthRadius) {
		this.earthRadius = earthRadius;
	}

	public double getEarthRadius() {
		return this.earthRadius;
	}
	
	public Point3D getPivotPoint() {
		return this.pivotPoint;
	}
	
	public void clear() {
		this.reductionTypes.clear();
	}
	
	public boolean applyReductionTask(ReductionTaskType type) {
		return this.reductionTypes.contains(type);
	}
	
	public boolean addReductionTaskType(ReductionTaskType type) {
		if (type != null)
			return this.reductionTypes.add(type);
		return false;
	}
	
	public boolean removeReductionTaskType(ReductionTaskType type) {
		return this.reductionTypes.remove(type);
	}
	
	public void setProjectionType(ProjectionType projectionType) {
		this.projectionType = projectionType;
	}
	
	public ProjectionType getProjectionType() {
		return this.projectionType;
	}
	
	public int size() {
		return this.reductionTypes.size();
	}
}
