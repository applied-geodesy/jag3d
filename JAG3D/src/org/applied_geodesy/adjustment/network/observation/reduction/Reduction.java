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

import org.applied_geodesy.transformation.datum.Ellipsoid;
import org.applied_geodesy.transformation.datum.PrincipalPoint;

public class Reduction {
	private ProjectionType projectionType = ProjectionType.LOCAL_CARTESIAN;
	private PrincipalPoint principalPoint = new PrincipalPoint();
	private Ellipsoid ellipsoid = Ellipsoid.SPHERE;
	private Set<ReductionTaskType> reductionTypes = new HashSet<ReductionTaskType>(ReductionTaskType.values().length);

	public double getEarthRadius() {
		return this.ellipsoid.getRadiusOfConformalSphere(this.principalPoint.getLatitude());
	}
	
	public PrincipalPoint getPrincipalPoint() {
		return this.principalPoint;
	}
	
	public void setEllipsoid(Ellipsoid ellipsoid) {
		this.ellipsoid = ellipsoid;
	}
	
	public Ellipsoid getEllipsoid() {
		return this.ellipsoid;
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
