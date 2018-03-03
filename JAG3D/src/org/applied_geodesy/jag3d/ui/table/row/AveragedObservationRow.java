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

package org.applied_geodesy.jag3d.ui.table.row;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.observation.ComponentType;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.FaceType;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.ZenithAngle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AveragedObservationRow extends Row {
	private ObjectProperty<String> startPointName = new SimpleObjectProperty<String>(this, "startPointName"); 
	private ObjectProperty<String> endPointName   = new SimpleObjectProperty<String>(this, "endPointName");
	
	private ObjectProperty<ObservationType> observationType = new SimpleObjectProperty<ObservationType>(this, "observationType"); 
	private ObjectProperty<ComponentType>   componentType = new SimpleObjectProperty<ComponentType>(this, "componentType"); 
	
	private ObjectProperty<Double> value      = new SimpleObjectProperty<Double>(this, "value");
	private ObjectProperty<Double> grossError = new SimpleObjectProperty<Double>(this, "grossError");
	
	public AveragedObservationRow() {}
	
	public AveragedObservationRow(Observation observation) {
		this.setStartPointName(observation.getStartPoint().getName());
		this.setEndPointName(observation.getEndPoint().getName());

		ComponentType compType = null;
		ObservationType obsType = observation.getObservationType();
		
		double value = observation.getValueApriori();
		if (obsType == ObservationType.DIRECTION && ((Direction)observation).getFace() == FaceType.TWO) 
			value = MathExtension.MOD(value + Math.PI, 2.0 * Math.PI);
		else if (obsType == ObservationType.ZENITH_ANGLE && ((ZenithAngle)observation).getFace() == FaceType.TWO) 
			value = MathExtension.MOD(2.0 * Math.PI - value, 2.0 * Math.PI);
		else if (obsType == ObservationType.GNSS1D || obsType == ObservationType.GNSS2D || obsType == ObservationType.GNSS3D) {
			compType = ((GNSSBaseline)observation).getComponent();
		}
		
		this.setComponentType(compType);
		this.setObservationType(obsType);
		this.setValue(value);
		this.setGrossError(observation.getGrossError());
	}
	
	public final ObjectProperty<String> startPointNameProperty() {
		return this.startPointName;
	}
	
	public final String getStartPointName() {
		return this.startPointNameProperty().get();
	}
	
	public final void setStartPointName(final String startPointName) {
		this.startPointNameProperty().set(startPointName);
	}
	
	public final ObjectProperty<String> endPointNameProperty() {
		return this.endPointName;
	}
	
	public final String getEndPointName() {
		return this.endPointNameProperty().get();
	}
	
	public final void setEndPointName(final String endPointName) {
		this.endPointNameProperty().set(endPointName);
	}
	
	public final ObjectProperty<ObservationType> observationTypeProperty() {
		return this.observationType;
	}
	
	public final ObservationType getObservationType() {
		return this.observationTypeProperty().get();
	}
	
	public final void setObservationType(final ObservationType observationType) {
		this.observationTypeProperty().set(observationType);
	}
	
	public final ObjectProperty<Double> valueProperty() {
		return this.value;
	}
	
	public final Double getValue() {
		return this.valueProperty().get();
	}
	
	public final void setValue(final Double value) {
		this.valueProperty().set(value);
	}
	
	public final ObjectProperty<Double> grossErrorProperty() {
		return this.grossError;
	}
	
	public final Double getGrossError() {
		return this.grossErrorProperty().get();
	}
	
	public final void setGrossError(final Double grossError) {
		this.grossErrorProperty().set(grossError);
	}

	public final ObjectProperty<ComponentType> componentTypeProperty() {
		return this.componentType;
	}
	

	public final ComponentType getComponentType() {
		return this.componentTypeProperty().get();
	}
	

	public final void setComponentType(final ComponentType componentType) {
		this.componentTypeProperty().set(componentType);
	}
	
}
