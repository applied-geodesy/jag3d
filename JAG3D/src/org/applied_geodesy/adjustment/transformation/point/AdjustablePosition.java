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

import javafx.beans.value.ObservableObjectValue;

public interface AdjustablePosition extends Positionable {
	public double getX0();
	public double getY0();
	public double getZ0();
	
	public void setX0(double x0);
	public void setY0(double y0);
	public void setZ0(double z0);
	
	public ObservableObjectValue<Double> x0Property();
	public ObservableObjectValue<Double> y0Property();
	public ObservableObjectValue<Double> z0Property();	
	
	public double getUncertaintyX();
	public double getUncertaintyY();
	public double getUncertaintyZ();
	
	public ObservableObjectValue<Double> uncertaintyXProperty();
	public ObservableObjectValue<Double> uncertaintyYProperty();
	public ObservableObjectValue<Double> uncertaintyZProperty();
	
	public double getResidualX();
	public double getResidualY();
	public double getResidualZ();
	
	public void setResidualX(double residualX);
	public void setResidualY(double residualY);
	public void setResidualZ(double residualZ);
	
	public ObservableObjectValue<Double> residualXProperty();
	public ObservableObjectValue<Double> residualYProperty();
	public ObservableObjectValue<Double> residualZProperty();
	
	public double getCofactorX();
	public double getCofactorY();
	public double getCofactorZ();
	
	public void setCofactorX(double cofactorX);
	public void setCofactorY(double cofactorY);
	public void setCofactorZ(double cofactorZ);
	
	public ObservableObjectValue<Double> cofactorXProperty();
	public ObservableObjectValue<Double> cofactorYProperty();
	public ObservableObjectValue<Double> cofactorZProperty();
}
