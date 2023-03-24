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

package org.applied_geodesy.adjustment.transformation.point;

import org.applied_geodesy.adjustment.transformation.VarianceComponent;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;

public class EstimatedFramePosition extends Position implements AdjustablePosition {
	private ObjectProperty<VarianceComponent> varianceComponent = new SimpleObjectProperty<VarianceComponent>(this, "varianceComponent", new VarianceComponent());

	private ObjectProperty<Double> residualX = new SimpleObjectProperty<Double>(this, "residualX", 0.0);
	private ObjectProperty<Double> residualY = new SimpleObjectProperty<Double>(this, "residualY", 0.0);
	private ObjectProperty<Double> residualZ = new SimpleObjectProperty<Double>(this, "residualZ", 0.0);
	
	private ObjectProperty<Double> cofactorX = new SimpleObjectProperty<Double>(this, "cofactorX", 0.0);
	private ObjectProperty<Double> cofactorY = new SimpleObjectProperty<Double>(this, "cofactorY", 0.0);
	private ObjectProperty<Double> cofactorZ = new SimpleObjectProperty<Double>(this, "cofactorZ", 0.0);
	
	private ObjectBinding<Double> x;
	private ObjectBinding<Double> y;
	private ObjectBinding<Double> z;
	
	private ObjectBinding<Double> uncertaintyX;
	private ObjectBinding<Double> uncertaintyY;
	private ObjectBinding<Double> uncertaintyZ;
	
	EstimatedFramePosition(double z0) throws IllegalArgumentException {
		super(z0);
		this.init();
	}
	
	EstimatedFramePosition(double x0, double y0) throws IllegalArgumentException {
		super(x0, y0);
		this.init();
	}
	
	EstimatedFramePosition(double x0, double y0, double z0) throws IllegalArgumentException {
		super(x0, y0, z0);
		this.init();
	}
	
	private void init() {
		this.setX0(super.getX());
		this.setY0(super.getY());
		this.setZ0(super.getZ());
		
		this.x = new ObjectBinding<Double>() {
			{
                super.bind(x0Property(), residualX);
            }
 
            @Override
            protected Double computeValue() {
                return x0Property().get() + residualX.get();
            }
        };
        
        this.y = new ObjectBinding<Double>() {
			{
                super.bind(y0Property(), residualY);
            }
 
            @Override
            protected Double computeValue() {
                return y0Property().get() + residualY.get();
            }
        };
        
        this.z = new ObjectBinding<Double>() {
			{
                super.bind(z0Property(), residualZ);
            }
 
            @Override
            protected Double computeValue() {
                return z0Property().get() + residualZ.get();
            }
        };
        
        this.uncertaintyX = new ObjectBinding<Double>() {
        	{
        		super.bind(cofactorX, varianceComponent);
        	}

        	@Override
        	protected Double computeValue() {
        		if (varianceComponent.get().isApplyAposterioriVarianceOfUnitWeight())
        			return Math.sqrt(Math.abs(cofactorX.get() * varianceComponent.get().varianceProperty().get()));
        		return Math.sqrt(Math.abs(cofactorX.get()));
        	}
        };

        this.uncertaintyY = new ObjectBinding<Double>() {
        	{
        		super.bind(cofactorY, varianceComponent);
        	}

        	@Override
        	protected Double computeValue() {
        		if (varianceComponent.get().isApplyAposterioriVarianceOfUnitWeight())
        			return Math.sqrt(Math.abs(cofactorY.get() * varianceComponent.get().varianceProperty().get()));
        		return Math.sqrt(Math.abs(cofactorY.get()));
        	}
        };

        this.uncertaintyZ = new ObjectBinding<Double>() {
        	{
        		super.bind(cofactorZ, varianceComponent);
        	}

        	@Override
        	protected Double computeValue() {
        		if (varianceComponent.get().isApplyAposterioriVarianceOfUnitWeight())
        			return Math.sqrt(Math.abs(cofactorZ.get() * varianceComponent.get().varianceProperty().get()));
        		return Math.sqrt(Math.abs(cofactorZ.get()));
        	}
        };
	}
	
	@Override
	public double getX() {
		return this.x.get();
	}
	
	@Override
	public ObservableObjectValue<Double> xProperty() {
		return this.x;
	}
	
	@Override
	public double getY() {
		return this.y.get();
	}
	
	@Override
	public ObservableObjectValue<Double> yProperty() {
		return this.y;
	}
	
	@Override
	public double getZ() {
		return this.z.get();
	}
	
	@Override
	public ObservableObjectValue<Double> zProperty() {
		return this.z;
	}

	@Override
	public double getResidualX() {
		return this.residualX.get();
	}
	
	@Override
	public void setResidualX(double residualX) {
		this.residualX.set(residualX);
	}
	
	@Override
	public ObservableObjectValue<Double> residualXProperty() {
		return this.residualX;
	}
	
	@Override
	public double getResidualY() {
		return this.residualY.get();
	}
	
	@Override
	public void setResidualY(double residualY) {
		this.residualY.set(residualY);
	}
	
	@Override
	public ObservableObjectValue<Double> residualYProperty() {
		return this.residualY;
	}
	
	@Override
	public double getResidualZ() {
		return this.residualZ.get();
	}
	
	@Override
	public void setResidualZ(double residualZ) {
		this.residualZ.set(residualZ);
	}
	
	@Override
	public ObservableObjectValue<Double> residualZProperty() {
		return this.residualZ;
	}
		
	@Override
	public double getUncertaintyX() {
		return this.uncertaintyX.get();
	}
	
	@Override
	public ObservableObjectValue<Double> uncertaintyXProperty() {
		return this.uncertaintyX;
	}
	
	@Override
	public double getUncertaintyY() {
		return this.uncertaintyY.get();
	}
	
	@Override
	public ObservableObjectValue<Double> uncertaintyYProperty() {
		return this.uncertaintyY;
	}
	
	@Override
	public double getUncertaintyZ() {
		return this.uncertaintyZ.get();
	}
	
	@Override
	public ObservableObjectValue<Double> uncertaintyZProperty() {
		return this.uncertaintyZ;
	}
	
	@Override
	public double getCofactorX() {
		return this.cofactorX.get();
	}
	
	@Override
	public void setCofactorX(double cofactorX) {
		this.cofactorX.set(cofactorX);
	}
	
	@Override
	public ObservableObjectValue<Double> cofactorXProperty() {
		return this.cofactorX;
	}
	
	@Override
	public double getCofactorY() {
		return this.cofactorY.get();
	}
	
	@Override
	public void setCofactorY(double cofactorY) {
		this.cofactorY.set(cofactorY);
	}
	
	@Override
	public ObservableObjectValue<Double> cofactorYProperty() {
		return this.cofactorY;
	}
	
	@Override
	public double getCofactorZ() {
		return this.cofactorZ.get();
	}
	
	@Override
	public void setCofactorZ(double cofactorZ) {
		this.cofactorZ.set(cofactorZ);
	}
	
	@Override
	public ObservableObjectValue<Double> cofactorZProperty() {
		return this.cofactorZ;
	}
	
	@Override
	public double getX0() {
		return super.xProperty().get();
	}
	
	public void setX0(double x0) {
		((ObjectProperty<Double>)super.xProperty()).set(x0);
	}
	
	@Override
	public ObservableObjectValue<Double> x0Property() {
		return super.xProperty();
	}
	
	@Override
	public double getY0() {
		return super.yProperty().get();
	}
	
	public void setY0(double y0) {
		((ObjectProperty<Double>)super.yProperty()).set(y0);
	}
	
	@Override
	public ObservableObjectValue<Double> y0Property() {
		return super.yProperty();
	}
	
	@Override
	public double getZ0() {
		return super.zProperty().get();
	}
	
	public void setZ0(double z0) {
		((ObjectProperty<Double>)super.zProperty()).set(z0);
	}
	
	@Override
	public ObservableObjectValue<Double> z0Property() {
		return super.zProperty();
	}

	ObjectProperty<VarianceComponent> varianceComponentProperty() {
		return this.varianceComponent;
	}
	
	public VarianceComponent getVarianceComponent() {
		return this.varianceComponent.get();
	}
	
	public void setVarianceComponent(VarianceComponent varianceComponent) {
		this.varianceComponent.set(varianceComponent);
	}
	
	@Override
	public void reset() {
		super.reset();
		
		this.setResidualX(0);
		this.setResidualY(0);
		this.setResidualZ(0);
		
		this.setCofactorX(0);
		this.setCofactorY(0);
		this.setCofactorZ(0);
	}
}
