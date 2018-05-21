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

package org.applied_geodesy.util.io;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreviewFileReader extends LockFileReader {
	private final static int MAX_LINES = 15;
	private int maxLines = PreviewFileReader.MAX_LINES, lineCounter = 0;
	private List<String> lines = new ArrayList<String>();

	public PreviewFileReader(File sf) {
		this(sf, MAX_LINES);
	}
	
	public PreviewFileReader(File sf, int maxLines) {
		super(sf);
		this.maxLines = maxLines;
	}
	
	public void reset() {
		this.lineCounter = 0;
		this.lines.clear();
	}

	@Override
	public void read() throws IOException, SQLException {
		this.reset();
		super.read();
	}

	@Override
	public void parse(String line) {
		if (this.lines.add(line))
			this.lineCounter++;

		if (this.lineCounter >= this.maxLines)
			super.interrupt();
	}
	
	public List<String> getLines() {
		return this.lines;
	}
}