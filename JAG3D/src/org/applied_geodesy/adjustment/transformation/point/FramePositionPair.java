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

import java.util.Iterator;
import java.util.NoSuchElementException;

import no.uib.cipr.matrix.Matrix;

public class FramePositionPair extends PositionPair<ObservedFramePosition, EstimatedFramePosition> implements Iterable<Position> {

	public FramePositionPair(String name, double zSrc, double zTrg) {
		this(name, new ObservedFramePosition(zSrc), new EstimatedFramePosition(zTrg));
	}
	
	public FramePositionPair(String name, double xSrc, double ySrc, double xTrg, double yTrg) {
		this(name, new ObservedFramePosition(xSrc, ySrc), new EstimatedFramePosition(xTrg, yTrg));
	}
	
	public FramePositionPair(String name, double xSrc, double ySrc, double zSrc, double xTrg, double yTrg, double zTrg) {
		this(name, new ObservedFramePosition(xSrc, ySrc, zSrc), new EstimatedFramePosition(xTrg, yTrg, zTrg));
	}
	
	public FramePositionPair(String name, double zSrc, Matrix dispersionSrc, double zTrg) {
		this(name, new ObservedFramePosition(zSrc, dispersionSrc), new EstimatedFramePosition(zTrg));
	}
	
	public FramePositionPair(String name, double xSrc, double ySrc, Matrix dispersionSrc, double xTrg, double yTrg) {
		this(name, new ObservedFramePosition(xSrc, ySrc, dispersionSrc), new EstimatedFramePosition(xTrg, yTrg));
	}
	
	public FramePositionPair(String name, double xSrc, double ySrc, double zSrc, Matrix dispersionSrc, double xTrg, double yTrg, double zTrg) {
		this(name, new ObservedFramePosition(xSrc, ySrc, zSrc, dispersionSrc), new EstimatedFramePosition(xTrg, yTrg, zTrg));
	}
	
	private FramePositionPair(String name, ObservedFramePosition pointSrc, EstimatedFramePosition pointTrg) {
		super(name, pointSrc, pointTrg);
	}
	
	@Override
	public Iterator<Position> iterator() {
		return new Iterator<Position>() {
			Position currentPosition = getSourceSystemPosition(); 
		      
		    // Checks if the next element exists
		    public boolean hasNext() {
		    	return this.currentPosition != null;
		    }
		      
		    // moves the cursor/iterator to next element
		    public Position next() {
		    	if (!this.hasNext()) 
	                throw new NoSuchElementException();
		    	
		    	if (this.currentPosition == getSourceSystemPosition()) {
		    		this.currentPosition = getTargetSystemPosition();
		    		return getSourceSystemPosition();
		    	}
		    	else if (this.currentPosition == getTargetSystemPosition()) {
		    		this.currentPosition = null;
		    		return getTargetSystemPosition();
		    	}
		    	
	            return null;
		    }
		};
	}
	
	@Override
	public void reset() {
		super.reset();
		
		this.getSourceSystemPosition().reset();
		this.getTargetSystemPosition().reset();
	}
}
