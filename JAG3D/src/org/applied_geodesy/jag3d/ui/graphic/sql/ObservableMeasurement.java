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
