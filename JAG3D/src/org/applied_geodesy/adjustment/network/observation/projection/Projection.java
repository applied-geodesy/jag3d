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

package org.applied_geodesy.adjustment.network.observation.projection;

public class Projection {

	private ProjectionType type;
	private double referenceHeight = 0.0;
	
	public Projection(ProjectionType type) {
		this.setType(type);
	}

	public void setType(ProjectionType type) {
		this.type = type;
	}

	public ProjectionType getType() {
		return this.type;
	}

	public void setReferenceHeight(double referenceHeight) {
		this.referenceHeight = referenceHeight;
	}

	public double getReferenceHeight() {
		return this.referenceHeight;
	}

	public boolean isHeightReduction() {
		return (this.type == ProjectionType.HEIGHT_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_REDUCTION ||
    			this.type == ProjectionType.HEIGHT_GK_REDUCTION || this.type == ProjectionType.HEIGHT_UTM_REDUCTION ||
    			this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}

	public boolean isGaussKruegerReduction() {
		return (this.type == ProjectionType.HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_GK_REDUCTION ||
				this.type == ProjectionType.GAUSS_KRUEGER_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION);
	}

	public boolean isUTMReduction() {
		return (this.type == ProjectionType.HEIGHT_UTM_REDUCTION || this.type == ProjectionType.DIRECTION_UTM_REDUCTION ||
				this.type == ProjectionType.UTM_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}

	public boolean isDirectionReduction() {
		return (this.type == ProjectionType.DIRECTION_GK_REDUCTION || this.type == ProjectionType.DIRECTION_UTM_REDUCTION ||
				this.type == ProjectionType.DIRECTION_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_REDUCTION || 
				this.type == ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION || this.type == ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION);
	}
	
	public String toString() {
		return  "Type : " + this.type + "\n" +
				"Hm: " + this.referenceHeight;
	}
}
