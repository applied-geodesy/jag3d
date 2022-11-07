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

package org.applied_geodesy.adjustment.network.parameter;

import java.util.List;

import org.applied_geodesy.adjustment.network.ParameterType;

import java.util.ArrayList;

public class UnknownParameters {
	private int col = 0;
	private int lastPointColumn = -1, lastParamColumn = -1, leastParamColumn = -1;
	private List<UnknownParameter> parameters = new ArrayList<UnknownParameter>();
	
	/**
	 * Sortiert die Parameter in der Liste nach {Punkte, Zusamtzparameter} ohne 
	 * die innere Reihenfolge der Punkte bzw. Parameter zu aendern 
	 */
	public void resortParameters() {
		if (this.leastParamColumn < this.lastPointColumn && this.leastParamColumn >= 0) {
			List<UnknownParameter> sortedParameters = new ArrayList<UnknownParameter>(this.parameters.size());
			List<UnknownParameter> nonPointParameters = new ArrayList<UnknownParameter>();
			this.col = 0;
			this.leastParamColumn = -1;
			this.lastParamColumn  = -1;
			this.lastPointColumn  = -1;

			for (UnknownParameter parameter : this.parameters) {
				switch (parameter.getParameterType()) {
				case POINT1D:
				case POINT2D:
				case POINT3D:
					parameter.setColInJacobiMatrix(this.col);
					this.increaseColumnCount(parameter.getParameterType());
					sortedParameters.add(parameter);
					break;
				default:
					nonPointParameters.add(parameter);
					break;
				}
			}

			for (UnknownParameter parameter : nonPointParameters) {
				parameter.setColInJacobiMatrix(this.col);
				this.increaseColumnCount(parameter.getParameterType());
				sortedParameters.add(parameter);
			}
			this.parameters = sortedParameters;
		}
	}

	public boolean add(UnknownParameter parameter) {
		if (!this.parameters.contains(parameter)) {
			parameter.setColInJacobiMatrix(this.col);
			this.increaseColumnCount(parameter.getParameterType());
			return this.parameters.add( parameter );
		}
		return false;
	}

	public int columnsOfAddionalParameters() {
		return this.lastParamColumn <= 0 ? 0 : this.lastParamColumn - this.lastPointColumn;
	}

	public int columnsOfPoints() {
		return this.lastPointColumn;
	}

	public int columnsInJacobi() {
		return this.col;
	}

	public boolean contains(Object obj) {
		return this.parameters.contains(obj);
	}
	
	private void increaseColumnCount(ParameterType type) {
		switch(type) {
			case POINT1D:
				this.col += 1;
				this.lastPointColumn = this.col;
			break;
			case POINT2D:
				this.col += 2;
				this.lastPointColumn = this.col;
			break;
			case POINT3D:
				this.col += 3;
				this.lastPointColumn = this.col;
			break;
			default:
				this.col += 1;
				this.lastParamColumn = this.col;
				if (this.leastParamColumn < 0)
					this.leastParamColumn = this.col;
			break;
		}
	}

	public UnknownParameter get(int i) {
		return this.parameters.get(i);
	}

	public int size() {
		return this.parameters.size();
	}
}
