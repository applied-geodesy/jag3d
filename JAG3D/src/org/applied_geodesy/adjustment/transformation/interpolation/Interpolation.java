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

package org.applied_geodesy.adjustment.transformation.interpolation;

import java.util.Collection;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment.Interrupt;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableObjectValue;

public abstract class Interpolation {
	final static double SQRT_EPS = Math.sqrt(Constant.EPS);
	
	private final ReadOnlyObjectProperty<InterpolationType> interpolationType;

	public Interpolation(InterpolationType interpolationType) {
		this.interpolationType = new ReadOnlyObjectWrapper<InterpolationType>(this, "interpolationType", interpolationType);
	}
	
	public abstract void interpolate(Collection<EstimatedFramePosition> estimatedFramePositions, Collection<FramePositionPair> framePositionPairs, Interrupt interrupt);
	
	public final ObservableObjectValue<InterpolationType> interpolationTypeProperty() {
		return this.interpolationType;
	}
	
	public final InterpolationType getInterpolationType() {
		return this.interpolationType.get();
	}
}
