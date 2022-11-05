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

package org.applied_geodesy.adjustment.network.point.dov;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.network.VerticalDeflectionType;

public class VerticalDeflectionGroup {
	private final int groupId;
	private VerticalDeflectionType verticalDeflectionType;
	private final VerticalDeflectionX verticalDeflectionX;
	private final VerticalDeflectionY verticalDeflectionY;
	private List<VerticalDeflectionX> verticalDeflectionsX = new ArrayList<VerticalDeflectionX>();
	private List<VerticalDeflectionY> verticalDeflectionsY = new ArrayList<VerticalDeflectionY>();
	private Set<VerticalDeflectionRestrictionType> verticalDeflectionRestrictionTypes = new LinkedHashSet<VerticalDeflectionRestrictionType>(1);
	
	
	public VerticalDeflectionGroup(int id, VerticalDeflectionType verticalDeflectionType, double x0, double y0, double sigmaX0, double sigmaY0) {
		this.groupId = id;
		this.verticalDeflectionType = verticalDeflectionType;

		this.verticalDeflectionX = new VerticalDeflectionX(null, x0, sigmaX0);
		this.verticalDeflectionY = new VerticalDeflectionY(null, y0, sigmaY0);
	}
	
	public boolean addVerticalDeflectionRestrictionType(VerticalDeflectionRestrictionType verticalDeflectionRestrictionType) {
		return this.verticalDeflectionRestrictionTypes.add(verticalDeflectionRestrictionType);
	}
	
	public boolean isRestricted(VerticalDeflectionRestrictionType verticalDeflectionRestrictionType) {
		return this.verticalDeflectionRestrictionTypes.contains(verticalDeflectionRestrictionType);
	}
	
	public final VerticalDeflectionX getVerticalDeflectionX() {
		return this.verticalDeflectionX;
	}
	
	public final VerticalDeflectionY getVerticalDeflectionY() {
		return this.verticalDeflectionY;
	}
	
	public void remove(VerticalDeflectionX verticalDeflectionX, VerticalDeflectionY verticalDeflectionY) {
		verticalDeflectionX.setVerticalDeflectionGroup(null);
		verticalDeflectionY.setVerticalDeflectionGroup(null);
		
		this.verticalDeflectionsX.remove(verticalDeflectionX);
		this.verticalDeflectionsY.remove(verticalDeflectionY);
	}
	
	public void add(VerticalDeflectionX verticalDeflectionX, VerticalDeflectionY verticalDeflectionY) {
		if (verticalDeflectionX.getPoint() == verticalDeflectionY.getPoint()) {
			verticalDeflectionX.setVerticalDeflectionGroup(this);
			verticalDeflectionY.setVerticalDeflectionGroup(this);
			
			this.verticalDeflectionsX.add(verticalDeflectionX);
			this.verticalDeflectionsY.add(verticalDeflectionY);
		}
	}
	
	public VerticalDeflectionX getVerticalDeflectionX(int i) {
		return this.verticalDeflectionsX.get(i);
	}
	
	public VerticalDeflectionY getVerticalDeflectionY(int i) {
		return this.verticalDeflectionsY.get(i);
	}
	
	public int size() {
		return this.verticalDeflectionsX.size();
	}
	
	public boolean isEmpty() {
		return this.verticalDeflectionsX.isEmpty();
	}
	
	public VerticalDeflectionType getVerticalDeflectionType() {
		return this.verticalDeflectionType;
	}
	
	public int getId(){
		return this.groupId;
	}
}
