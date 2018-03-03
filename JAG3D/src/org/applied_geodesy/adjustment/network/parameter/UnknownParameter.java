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

import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;

public abstract class UnknownParameter {
	private ObservationGroup observationGroup = new ObservationGroup(-1, Double.NaN, Double.NaN, Double.NaN, Epoch.REFERENCE); 
	
	// Position in Designmatrix; -1 entspricht nicht gesetzt	
	private int colInJacobiMatrix = -1;

	/**
	 * Liefert den Parametertyp
	 * @return typ
	 */
	public abstract ParameterType getParameterType();
	
	/**
	 * Gibt die Spaltenposition
	 * in der Designmatrix A zurueck 
	 * @return row
	 */
	public int getColInJacobiMatrix() {
		return this.colInJacobiMatrix;
	}
	
	/**
	 * Legt die Spalte, in der der Parameter
	 * in der JacobiMatrix steht, fest.
	 * @param col Spalte in Matrix
	 */
	public void setColInJacobiMatrix(int col) {
		this.colInJacobiMatrix = col;
	}
	
	/**
	 * Setzt die Beobachtung, die zum Parameter gehoeren
	 * @param observation
	 */
	public void setObservation(Observation observation) {
		this.observationGroup.add(observation);
	}
	
	/**
	 * Liefert eine allg. Beobachtungsgruppe mit allen Beobachtungen,
	 * die zum Parameter.
	 * @return observationGroup
	 */
	public ObservationGroup getObservations() {
		return this.observationGroup;
	}
	
	/**
	 * Liefert die Anzahl der Beobachtungen des Parameters
	 * @return size
	 */
	public int numberOfObservations() {
		return this.observationGroup.size();
	}
	
}
