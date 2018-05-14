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

package org.applied_geodesy.adjustment;

public enum EstimationStateType {
	ERROR_FREE_ESTIMATION(1),
	BUSY(0),
	ITERATE(0),
	CONVERGENCE(0),
	PRINCIPAL_COMPONENT_ANALYSIS(0),
	ESTIAMTE_STOCHASTIC_PARAMETERS(0),
	INVERT_NORMAL_EQUATION_MATRIX(0),
	EXPORT_COVARIANCE_MATRIX(0),
	EXPORT_COVARIANCE_INFORMATION(0),
	INTERRUPT(-1),
	SINGULAR_MATRIX(-2),
	ROBUST_ESTIMATION_FAILD(-3),
	NO_CONVERGENCE(-4),
	NOT_INITIALISED(-5),
	OUT_OF_MEMORY(-6);
	
	private int id;
	private EstimationStateType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static EstimationStateType getEnumByValue(int value) {
		for(EstimationStateType element : EstimationStateType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
