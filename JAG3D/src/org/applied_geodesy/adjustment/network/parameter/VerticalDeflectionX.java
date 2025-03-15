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

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.point.Point;

public class VerticalDeflectionX extends VerticalDeflection {
	private boolean significant = false;
	private final static double ZERO = Math.sqrt(Constant.EPS);
	private double nablaCoVarNable =  0.0, tPrio = 0.0, tPost = 0.0, pPrio = 0.0, pPost = 0.0;
	public VerticalDeflectionX(Point point) {
		super(point);
	}
	
	public VerticalDeflectionX(Point point, double value) {
		super(point, value);
	}
	
	public VerticalDeflectionX(Point point, double value, double std) {
		super(point, value, std);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.VERTICAL_DEFLECTION_X;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}	

	public boolean isSignificant() {
		return this.significant;
	}

	public void setNablaCoVarNabla(double ncn) {
		if (ncn>=0)
			this.nablaCoVarNable = ncn;
	}
	
	public void setPprio(double p) {
		this.pPrio = p;
	}

	public void setPpost(double p) {
		this.pPost = p;
	}

	public double getTprio() {
		return this.tPrio;
	}

	public double getTpost() {
		return this.tPost;
	}

	public double getPprio() {
		return this.pPrio;
	}

	public double getPpost() {
		return this.pPost;
	}

	public void calcStochasticParameters(double sigma2apost, int redundancy, boolean applyAposterioriVarianceOfUnitWeight) {
		// Bestimmung der Testgroessen
		final int dim = 2;
		double omega = sigma2apost*(double)redundancy;
		double sigma2apostDeflection = (redundancy - dim) > 0 ? (omega - this.nablaCoVarNable)/(redundancy - dim) : 0.0;
		
		this.tPrio = this.nablaCoVarNable/dim;
		this.tPost = applyAposterioriVarianceOfUnitWeight && (redundancy - dim) > 0 && sigma2apostDeflection > VerticalDeflectionX.ZERO ? this.tPrio/sigma2apostDeflection : 0.0;
	}
	
	@Override
	public String toString() {
		return "VerticalDeflectionX [point=" + this.getPoint() + ", value0=" + this.getValue0() + ", value=" + this.getValue() + "]";
	}
}
