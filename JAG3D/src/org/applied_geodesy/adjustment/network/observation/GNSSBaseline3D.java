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

package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.parameter.RotationX;
import org.applied_geodesy.adjustment.network.parameter.RotationY;
import org.applied_geodesy.adjustment.point.Point;

public abstract class GNSSBaseline3D extends GNSSBaseline {
	private RotationX rx = new RotationX();
	private RotationY ry = new RotationY();

	public GNSSBaseline3D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, 0, 0, observation, sigma);
	}

	public void setRotationX(RotationX r) {
		this.rx = r;
		this.rx.setObservation( this );
	}

	public void setRotationY(RotationY r) {
		this.ry = r;
		this.ry.setObservation( this );
	}

	public RotationX getRotationX() {
		return this.rx;
	}

	public RotationY getRotationY() {
		return this.ry;
	}

	@Override
	public int getColInJacobiMatrixFromRotationX() {
		return this.rx.getColInJacobiMatrix();
	}
	
	@Override
	public int getColInJacobiMatrixFromRotationY() {
		return this.ry.getColInJacobiMatrix();
	}
	
	@Override
	public int getDimension() {
		return 3;
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.GNSS3D;
	}	
}