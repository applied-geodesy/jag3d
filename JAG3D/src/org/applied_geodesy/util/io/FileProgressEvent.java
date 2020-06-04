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
import java.util.EventObject;

public class FileProgressEvent extends EventObject {

	public enum FileProgressEventType {
		READ_LINE  
	}
	
	private static final long serialVersionUID = 1580425438267707405L;
	private long readedBytes = 0, totalBytes = 0;
	
	private final FileProgressEventType type;
	FileProgressEvent(File file, FileProgressEventType type, long readedBytes, long totalBytes) {
		super(file);
		this.type = type;
		this.readedBytes = readedBytes;
		this.totalBytes = totalBytes;
	}
	
	@Override
	public File getSource() {
		return (File)super.getSource();
	}
	
	public FileProgressEventType getEventType() {
		return this.type;
	}
	
	public long getReadedBytes() {
		return this.readedBytes;
	}
	
	public long getTotalBytes() {
		return this.totalBytes;
	}
}
