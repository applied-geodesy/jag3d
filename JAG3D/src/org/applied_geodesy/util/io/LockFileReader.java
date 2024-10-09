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
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.applied_geodesy.util.io.FileProgressEvent.FileProgressEventType;

public abstract class LockFileReader {
	private Path sourceFilePath = null;
	private String ignoreStartString = new String();
	public static final String UTF8_BOM = "\uFEFF";
	private boolean interrupt = false;
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
	LockFileReader() {}
	
	LockFileReader(String fileName) {
		this(new File(fileName).toPath());
	}

	LockFileReader(File sf) {
		this(sf.toPath());
	}
	
	LockFileReader(Path path) {
		setPath(path);
	}
	
	void setPath(Path path) {
		this.sourceFilePath = path;
	}
	
	public Path getPath() {
		return this.sourceFilePath;
	}

	public abstract void parse(String line) throws SQLException; 

	public void ignoreLinesWhichStartWith(String str) {
		this.ignoreStartString = str;
	}
  
	public void read() throws IOException, SQLException {
		if (this.sourceFilePath == null || 
				!Files.exists(this.sourceFilePath) ||
				!Files.isRegularFile(this.sourceFilePath) ||
				!Files.isReadable(this.sourceFilePath))
			return;
		
		this.interrupt = false;
		BufferedReader reader = null;
		boolean isFirstLine = true;
		try{
			long totalBytes = Files.size(this.sourceFilePath);
			File sourceFile = this.sourceFilePath.toFile();
			FileInputStream inputStream = new FileInputStream( sourceFile );
			inputStream.getChannel().lock(0, Long.MAX_VALUE, true);
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")), 1024*64);

			long readedBytes = 0L;
			String currentLine = null;
			while (!this.interrupt && (currentLine = removeNonBMPCharacters(reader.readLine())) != null) {
				readedBytes += currentLine.length();

				if (isFirstLine && currentLine.startsWith(UTF8_BOM)) {
					currentLine = currentLine.substring(1); 
					isFirstLine = false;
				}
				
				if (!currentLine.isBlank() && (this.ignoreStartString.isEmpty() || !currentLine.startsWith( this.ignoreStartString )))
					this.parse(currentLine);

				this.fireFileProgressChanged(sourceFile, FileProgressEventType.READ_LINE, readedBytes, totalBytes);
			}
			this.fireFileProgressChanged(sourceFile, FileProgressEventType.READ_LINE, totalBytes, totalBytes);
		}
		finally {
			try {
				// closed all other streams
				// https://stackoverflow.com/questions/24362980/when-i-close-a-bufferedinputstream-is-the-underlying-inputstream-also-closed
				if (reader != null)
					reader.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String removeNonBMPCharacters(final String str) {
		if (str == null || str.isBlank())
			return str;
		
		final int len = str.length();
		if (len == 0)
			return str;
		
		// https://stackoverflow.com/questions/6198986/how-can-i-replace-non-printable-unicode-characters-in-java
		StringBuilder stringBuilder = new StringBuilder(len);
		for (int offset = 0; offset < len;) {
		    int codePoint = str.codePointAt(offset);
		    offset += Character.charCount(codePoint);

		    // Replace invisible control characters and unused code points
		    switch (Character.getType(codePoint)) {
		        case Character.CONTROL:     // \p{Cc}
		        case Character.FORMAT:      // \p{Cf}
		        case Character.PRIVATE_USE: // \p{Co}
		        case Character.SURROGATE:   // \p{Cs}
		        case Character.UNASSIGNED:  // \p{Cn}
		            break;
		        default:
		        	stringBuilder.append(Character.toChars(codePoint));
		            break;
		    }
		}

	    return stringBuilder.toString();
	}

	public void interrupt() {
		this.interrupt = true;
	}
	
	public boolean isInterrupted() {
		return this.interrupt;
	}
	
	public void addFileProgressChangeListener(FileProgressChangeListener l) {
		this.listenerList.add(l);
	}
	
	public void removeFileProgressChangeListener(FileProgressChangeListener l) {
		this.listenerList.remove(l);
	}
	
	private void fireFileProgressChanged(File file, FileProgressEventType eventType, long readedBytes, long totalBytes) {
		FileProgressEvent evt = new FileProgressEvent(file, eventType, Math.min(readedBytes, totalBytes), Math.max(readedBytes, totalBytes));
		Object listeners[] = this.listenerList.toArray();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] instanceof FileProgressChangeListener)
				((FileProgressChangeListener)listeners[i]).fileProgressChanged(evt);
		}
	}
} 