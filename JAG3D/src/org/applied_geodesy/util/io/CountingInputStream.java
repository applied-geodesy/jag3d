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

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream implements AutoCloseable {
	// https://stackoverflow.com/questions/41980738/how-do-i-determine-progress-when-reading-a-file
    private long readedBytes = 0;
    private final InputStream stream;

    public CountingInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        int result = this.stream.read();
        if (result != -1) {
        	this.readedBytes++;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.stream.close();
        this.readedBytes = 0; 
    }

    public long getReadedBytes() {
        return this.readedBytes;
    }
}
