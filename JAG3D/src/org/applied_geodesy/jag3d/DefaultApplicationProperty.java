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

package org.applied_geodesy.jag3d;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Properties;

import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;

public class DefaultApplicationProperty {

	private final static Properties PROPERTIES = new Properties();
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/application.default";
		try {
			if (Layer.class.getResource(path) != null) {
				bis = new BufferedInputStream(Layer.class.getResourceAsStream(path));
				PROPERTIES.load(bis);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private DefaultApplicationProperty() {}
	
	public static boolean startApplicationInFullScreen() {
		return PROPERTIES.getProperty("FULL_SCREEN", "FALSE").equalsIgnoreCase("TRUE");
	}
	
	public static boolean showConfirmDialogOnDelete() {
		return PROPERTIES.getProperty("SHOW_CONFIRM_DIALOG_ON_DELETE", "TRUE").equalsIgnoreCase("TRUE");
	}
	
	public static File getDefaultWorkspacePath() {
		String defaultWorkspace = PROPERTIES.getProperty("WORKSPACE", System.getProperty("user.home", null));
		//if (defaultWorkspace != null && Files.exists(new File(defaultWorkspace).toPath()))
		if (defaultWorkspace != null && Files.exists(Paths.get(defaultWorkspace)) && Files.isDirectory(Paths.get(defaultWorkspace), LinkOption.NOFOLLOW_LINKS))
			return new File(defaultWorkspace);
		
		return new File(System.getProperty("user.home", null));
	}
	
	public static File getDefaultHistoryPath() {
		String defaultHistoryPath = PROPERTIES.getProperty("HISTORY", System.getProperty("user.home", null));
		if (defaultHistoryPath != null && Files.exists(Paths.get(defaultHistoryPath)) && Files.isDirectory(Paths.get(defaultHistoryPath), LinkOption.NOFOLLOW_LINKS))
			return new File(defaultHistoryPath + File.separator + ".jag3d_history");

		return new File(System.getProperty("user.home") + File.separator + ".jag3d_history");
	}
}
