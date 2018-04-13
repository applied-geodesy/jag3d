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

package org.applied_geodesy.util.io.csv;

public class ColumnRange {
	private int c1, c2;
	private CSVColumnType type;
	
	public ColumnRange(CSVColumnType type, int c1, int c2) {
		this.type = type;
		this.c1 = c1;
		this.c2 = c2;
	}
		
	public int getColumnStart() {
		return this.c1;
	}
	
	public int getColumnEnd() {
		return this.c2;
	}
	
	public CSVColumnType getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return this.type + "[" + this.c1 + ", " + this.c2 + "]"; 
	}
	
}
