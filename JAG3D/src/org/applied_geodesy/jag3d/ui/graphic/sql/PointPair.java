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

public class PointPair {
	private GraphicPoint startPoint, endPoint;
	private boolean significant = false, grossErrorExceeded = false;
	private double redundancy = 1.0, influenceOnPosition = 0.0, pPrio = 0;
	
	public PointPair(GraphicPoint startPoint, GraphicPoint endPoint) {
		this.startPoint = startPoint;
		this.endPoint   = endPoint;
	}
	
	public GraphicPoint getStartPoint() {
		return this.startPoint;
	}
	
	public GraphicPoint getEndPoint() {
		return this.endPoint;
	}
	
	public boolean isSignificant() {
		return this.significant;
	}
	
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}
	
	public boolean isGrossErrorExceeded() {
		return this.grossErrorExceeded;
	}
	
	public void setGrossErrorExceeded(boolean grossErrorExceeded) {
		this.grossErrorExceeded = grossErrorExceeded;
	}
	
	public double getRedundancy() {
		return this.redundancy;
	}
	
	public void setRedundancy(double redundancy) {
		this.redundancy = redundancy;
	}
	
	public double getInfluenceOnPosition() {
		return this.influenceOnPosition;
	}
	
	public void setInfluenceOnPosition(double influenceOnPosition) {
		this.influenceOnPosition = influenceOnPosition;
	}
	
	public double getPprio() {
		return this.pPrio;
	}
	
	public void setPprio(double pPrio) {
		this.pPrio = pPrio;
	}
}
