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

package org.applied_geodesy.jag3d.ui.dialog.chart;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.VarianceComponentType;

enum TerrestrialObservationType {
	ALL(null),
	LEVELING(ObservationType.LEVELING),
	DIRECTION(ObservationType.DIRECTION),
	HORIZONTAL_DISTANCE(ObservationType.HORIZONTAL_DISTANCE),
	SLOPE_DISTANCE(ObservationType.SLOPE_DISTANCE),
	ZENITH_ANGLE(ObservationType.ZENITH_ANGLE),
	;

	private final ObservationType observationType;
	private final VarianceComponentType varianceComponentType;
	private TerrestrialObservationType(ObservationType observationType) {
		this.observationType       = observationType;
		this.varianceComponentType = VarianceComponentType.getVarianceComponentTypeByObservationType(observationType);
	}

	public final ObservationType getObservationType() {
		return observationType;
	}
	
	public final VarianceComponentType getVarianceComponentType() {
		return varianceComponentType;
	}

	public static TerrestrialObservationType getEnumByValue(ObservationType observationType) {
		for(TerrestrialObservationType element : TerrestrialObservationType.values()) {
			if(element.observationType != null && element.observationType == observationType)
				return element;
		}
		return null;
	} 
	
	public static TerrestrialObservationType getEnumByValue(VarianceComponentType varianceComponentType) {
		for(TerrestrialObservationType element : TerrestrialObservationType.values()) {
			if(element.varianceComponentType != null && element.varianceComponentType == varianceComponentType)
				return element;
		}
		return null;
	}
}
