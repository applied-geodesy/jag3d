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

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.point.Point;

/**
 * Klasse ist eine Huelle fuer die Y-Komponente. Die statistischen groessen werden 
 * in der X-Komponente abgespeichert, da beide (X und Y) als ein Objekt zu interpretieren sind.
 *
 */
public class VerticalDeflectionY extends VerticalDeflection {
	
	public VerticalDeflectionY(Point point) {
		super(point);
	}
	
	public VerticalDeflectionY(Point point, double value) {
		super(point, value);
	}
	
	public VerticalDeflectionY(Point point, double value, double std) {
		super(point, value, std);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.VERTICAL_DEFLECTION_Y;
	}
	
	@Override
	public void setValue(double value) {
		if (this.isRestrictedGroupParameter())
			this.getVerticalDeflectionGroup().getVerticalDeflectionY().setValue(value);
		else
			super.setValue(value);
	}
	
	@Override
	public double getValue() {
		if (this.isRestrictedGroupParameter())
			return this.getVerticalDeflectionGroup().getVerticalDeflectionY().getValue();
		else
			return super.getValue();
	}
	
	@Override
	public void setColInJacobiMatrix(int col) {
		if (this.isRestrictedGroupParameter())
			this.getVerticalDeflectionGroup().getVerticalDeflectionY().setColInJacobiMatrix(col);
		else 
			super.setColInJacobiMatrix(col);
	}
	
	@Override
	public int getColInJacobiMatrix() {
		if (this.isRestrictedGroupParameter())
			return this.getVerticalDeflectionGroup().getVerticalDeflectionY().getColInJacobiMatrix();
		else
			return super.getColInJacobiMatrix();
	}
	
	@Override
	public String toString() {
		return "VerticalDeflectionY [point=" + this.getPoint() + ", value0=" + this.getValue0() + ", value=" + this.getValue() + "]";
	}
}