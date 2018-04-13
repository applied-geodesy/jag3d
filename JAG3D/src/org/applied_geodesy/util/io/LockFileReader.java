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

import org.applied_geodesy.util.i18.I18N;

import javafx.stage.FileChooser.ExtensionFilter;

public abstract class LockFileReader {
	private Path sourceFilePath = null;
	private String ignoreStartString = new String();
	public static final String UTF8_BOM = "\uFEFF";
	private boolean interrupt = false;
	
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
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("LockFileReader.extension.description", "All files"),	"*.*")
		};
	}

	public void ignoreLinesWhichStartWith(String str) {
		this.ignoreStartString = str;
	}
  
	void read() throws IOException, SQLException {
		if (this.sourceFilePath == null || 
				!Files.exists(this.sourceFilePath) ||
				!Files.isRegularFile(this.sourceFilePath) ||
				!Files.isReadable(this.sourceFilePath))
			return;
		
		this.interrupt = false;
		BufferedReader reader = null;
		boolean isFirstLine = true;
		try{
			FileInputStream inputStream = new FileInputStream( this.sourceFilePath.toFile() );
			inputStream.getChannel().lock(0, Long.MAX_VALUE, true);
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

			String currentLine = null;
			while (!this.interrupt && (currentLine = reader.readLine()) != null) {
				if (isFirstLine && currentLine.startsWith(UTF8_BOM)) {
					currentLine = currentLine.substring(1); 
					isFirstLine = false;
				}
				if (!currentLine.trim().isEmpty() && (this.ignoreStartString.isEmpty() || !currentLine.startsWith( this.ignoreStartString )))
					this.parse(currentLine);
			}
		}
		finally {
			try {
				if (reader != null)
					reader.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void interrupt() {
		this.interrupt = true;
	}
	
	public boolean isInterrupted() {
		return this.interrupt;
	}
} 