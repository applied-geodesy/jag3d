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

package org.applied_geodesy.ui.table;

import java.util.Comparator;

/** 
 * NaturalOrderComparator.java -- Perform 'natural order' comparisons of strings in Java.
 * Copyright (C) 2003 by Pierre-Luc Paour <natorder@paour.com>
 * Based on the C version by Martin Pool, of which this is more or less a straight conversion.
 * Copyright (C) 2000 by Martin Pool <mbp@humbug.org.au>
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 *
 * Source: https://github.com/paour/natorder/blob/master/NaturalOrderComparator.java
 **/
public class NaturalOrderComparator<T> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		// check against null values
		if (o1 == null && o2 == null)
			return 0;
		else if (o1 == null)
			return -1;
		else if (o2 == null)
			return +1;
		
		String a = o1.toString();
		String b = o2.toString();
		
		int ia = 0, ib = 0;
		int nza = 0, nzb = 0;
		char ca, cb;

		while (true) {
			// Only count the number of zeroes leading the last number compared
			nza = nzb = 0;

			ca = this.charAt(a, ia);
			cb = this.charAt(b, ib);

			// skip over leading spaces or zeros
			while (Character.isSpaceChar(ca) || ca == '0') {
				if (ca == '0') {
					nza++;
				} else {
					// Only count consecutive zeroes
					nza = 0;
				}

				ca = this.charAt(a, ++ia);
			}

			while (Character.isSpaceChar(cb) || cb == '0') {
				if (cb == '0') {
					nzb++;
				} else {
					// Only count consecutive zeroes
					nzb = 0;
				}

				cb = this.charAt(b, ++ib);
			}

			// Process run of digits
			if (Character.isDigit(ca) && Character.isDigit(cb)) {
				int bias = compareRight(a.substring(ia), b.substring(ib));
				if (bias != 0) {
					return bias;
				}
			}

			if (ca == 0 && cb == 0) {
				// The strings compare the same. Perhaps the caller
				// will want to call strcmp to break the tie.
				return this.compareEqual(a, b, nza, nzb);
			}
			if (ca < cb) {
				return -1;
			}
			if (ca > cb) {
				return +1;
			}

			++ia;
			++ib;
		}
	}
	
	private int compareRight(String a, String b) {
		int bias = 0, ia = 0, ib = 0;

		// The longest run of digits wins. That aside, the greatest
		// value wins, but we can't know that it will until we've scanned
		// both numbers to know that they have the same magnitude, so we
		// remember it in BIAS.
		for (;; ia++, ib++) {
			char ca = this.charAt(a, ia);
			char cb = this.charAt(b, ib);

			if (!this.isDigit(ca) && !this.isDigit(cb)) {
				return bias;
			}
			if (!this.isDigit(ca)) {
				return -1;
			}
			if (!this.isDigit(cb)) {
				return +1;
			}
			if (ca == 0 && cb == 0) {
				return bias;
			}

			if (bias == 0) {
				if (ca < cb) {
					bias = -1;
				} else if (ca > cb) {
					bias = +1;
				}
			}
		}
	}

	private boolean isDigit(char c) {
		return Character.isDigit(c) || c == '.' || c == ',';
	}

	private char charAt(String s, int i) {
		return i >= s.length() ? 0 : s.charAt(i);
	}

	private int compareEqual(String a, String b, int nza, int nzb) {
		if (nza - nzb != 0)
			return nza - nzb;

		if (a.length() == b.length())
			return a.compareTo(b);

		return a.length() - b.length();
	}
}
