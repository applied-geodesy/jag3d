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
import java.nio.file.Path;
import java.sql.SQLException;

import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.control.TreeItem;

public abstract class SourceFileReader extends LockFileReader {
	protected static I18N i18n = I18N.getInstance();
	
	protected SourceFileReader() {
		super();
	}
	
	protected SourceFileReader(String fileName) {
		this(new File(fileName).toPath());
	}

	protected SourceFileReader(File sf) {
		this(sf.toPath());
	}
	
	protected SourceFileReader(Path path) {
		super(path);
		this.setPath(path);
	}
	
	@Override
	public void setPath(Path path) {
		this.reset();
		super.setPath(path);
	}
	
	public abstract TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException;

	public abstract void reset();

	public String createItemName(String prefix, String suffix) {
		prefix = prefix == null ? "" : prefix;
		suffix = suffix == null ? "" : suffix;
		String fileName = this.getPath().getFileName().toString();
		if (!fileName.trim().isEmpty()) {
			if (fileName.indexOf('.') > 0)
				fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			return prefix + fileName + suffix;
		}
		return null;
	}
}