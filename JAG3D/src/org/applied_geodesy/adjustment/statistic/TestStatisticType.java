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

package org.applied_geodesy.adjustment.statistic;

public enum TestStatisticType {

	NONE(1),
	BAARDA_METHOD(2), 
	SIDAK(3); 
	
	private final int id;
	private TestStatisticType(int id) {
		this.id = id;
	}
		
	public int getId() {
		return this.id;
	}
	
	public static TestStatisticType getEnumByValue(int id) {
		for (TestStatisticType type : TestStatisticType.values()) 
			if (type.getId() == id)
				return type;
		return NONE;
	}
}