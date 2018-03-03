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

package org.applied_geodesy.jag3d.sql;

public class UnderDeterminedPointException extends Exception {
	private static final long serialVersionUID = 3497901439178351156L;
	private int dimension = -1;
	private int numberOfObservations = 0;
	private String pointName;
	
	public UnderDeterminedPointException(String message, String pointName, int dimension, int numberOfObservations) { 
		super(message); 
		this.pointName = pointName;
		this.dimension = dimension;
		this.numberOfObservations = numberOfObservations;
	}
	
	public String getPointName() {
		return pointName;
	}

	public int getDimension() {
		return dimension;
	}

	public int getNumberOfObservations() {
		return numberOfObservations;
	}
}