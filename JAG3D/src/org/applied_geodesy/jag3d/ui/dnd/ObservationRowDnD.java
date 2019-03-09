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

package org.applied_geodesy.jag3d.ui.dnd;

public abstract class ObservationRowDnD extends TableRowDnD {
	private static final long serialVersionUID = 4855377431348113363L;
	private String startPointName; 
	private String endPointName;
	
	public String getStartPointName() {
		return startPointName;
	}
	void setStartPointName(String startPointName) {
		this.startPointName = startPointName;
	}
	public String getEndPointName() {
		return endPointName;
	}
	void setEndPointName(String endPointName) {
		this.endPointName = endPointName;
	}
}
