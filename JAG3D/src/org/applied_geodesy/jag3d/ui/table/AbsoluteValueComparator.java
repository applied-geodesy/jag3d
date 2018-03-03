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

package org.applied_geodesy.jag3d.ui.table;

import java.util.Comparator;

public class AbsoluteValueComparator implements Comparator<Double> {

	@Override
	public int compare(Double val1, Double val2) {
		if (val1 == null && val2 == null)
			return 0;
		else if (val1 == null)
			return -1;
		else if (val2 == null)
			return +1;
		else if (Math.abs(val1.doubleValue()) < Math.abs(val2.doubleValue()))
			return -1;
		else if(Math.abs(val1.doubleValue()) > Math.abs(val2.doubleValue()))
			return +1;
		else
			return  0;
	}
}
