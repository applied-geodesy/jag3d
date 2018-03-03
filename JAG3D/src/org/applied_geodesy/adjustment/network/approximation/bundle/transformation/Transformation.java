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

package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;

public interface Transformation {

	public boolean transformL2Norm();

	public boolean transformLMS();

	public double getOmega();

	public void setFixedParameter(TransformationParameterType type, boolean fixed);

	public int getDimension();

	public int numberOfIdenticalPoints();

	public int numberOfRequiredPoints();
	
	public Point transformPoint2TargetSystem(Point point);

	public Point transformPoint2SourceSystem(Point point);

	public PointBundle getTransformdPoints();

	public TransformationParameterSet getTransformationParameterSet();
}
