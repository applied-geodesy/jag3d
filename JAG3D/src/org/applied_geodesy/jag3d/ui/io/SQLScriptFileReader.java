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

package org.applied_geodesy.jag3d.ui.io;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.stage.FileChooser.ExtensionFilter;

public class SQLScriptFileReader extends SourceFileReader<Void> {
	private StringBuffer content = new StringBuffer();
	
	public SQLScriptFileReader() {
		this.reset();
	}
	
	public SQLScriptFileReader(String s) {
		this(new File(s));
	}

	public SQLScriptFileReader(File f) {
		this(f.toPath());
	}
	
	public SQLScriptFileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public Void readAndImport() throws Exception {
		this.reset();
		super.read();
		
		try {
			SQLManager.getInstance().executeStatement(this.content.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
		
		this.reset();
		return null;
	}

	@Override
	public void reset() {
		// see https://stackoverflow.com/questions/2242471/clearing-a-string-buffer-builder-after-loop
		this.content.setLength(0);
		
	}

	@Override
	public void parse(String line) throws SQLException {
		this.content.append(line).append("\r\n");
	}

	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("SQLScriptFileReader.extension.sql", "Structured Query Language"), "*.sql")
		};
	}
}
