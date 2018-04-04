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

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class DefaultFileChooser {
	private static Window window;
	private static File lastSelectedDirectory = null;
	private static ExtensionFilter lastSelectedExtensionFilter = null;
	private static String lastSelectedFileName = "";
	private static final FileChooser fileChooser = new FileChooser();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/paths.default";
		try {
			if (DefaultFileChooser.class.getResource(path) != null) {
				Properties PROPERTIES = new Properties();
				bis = new BufferedInputStream(DefaultFileChooser.class.getResourceAsStream(path));
				PROPERTIES.load(bis);
				String defaultWorkspace = PROPERTIES.getProperty("WORKSPACE", System.getProperty("user.home", null));
				if (defaultWorkspace != null && Files.exists(new File(defaultWorkspace).toPath())) {
					lastSelectedDirectory = new File(defaultWorkspace);
				}
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
		
	private DefaultFileChooser() {}
	
	public static void setStage(Window parent) {
		window = parent;
	}

	private static void prepareFileChooser(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		if (lastSelectedDirectory == null && System.getProperty("user.home", null) != null)
			lastSelectedDirectory = new File(System.getProperty("user.home"));
		fileChooser.setInitialDirectory(lastSelectedDirectory != null && Files.exists(lastSelectedDirectory.toPath()) ? lastSelectedDirectory : null);  
		fileChooser.setInitialFileName(initialFileName == null ? lastSelectedFileName : initialFileName);
		fileChooser.setTitle(title);

		if (extensionFilters != null && extensionFilters.length > 0) {
			
			boolean containsFilter = false;
			for (ExtensionFilter filter : extensionFilters) {
				if (equalExtensionFilters(lastSelectedExtensionFilter, filter)) {
					setLastSelectedExtensionFilter(filter);
					containsFilter = true;
					break;
				}
			}
			
			fileChooser.getExtensionFilters().setAll(extensionFilters);
			
			if (!containsFilter || lastSelectedExtensionFilter == null)
				lastSelectedExtensionFilter = extensionFilters[0];
			fileChooser.setSelectedExtensionFilter(lastSelectedExtensionFilter);
		}
		else
			fileChooser.getExtensionFilters().clear();
	}
	
	private static boolean equalExtensionFilters(ExtensionFilter filter1, ExtensionFilter filter2) {
		if (filter1 == null || filter2 == null || filter1.getExtensions().size() != filter2.getExtensions().size() || !filter1.getDescription().equals(filter2.getDescription()))
			return false;
		
		List<String> extensions = filter1.getExtensions();
		for (String extension : extensions) 
			if (!filter2.getExtensions().contains(extension))
				return false;
		
		return true;
	}
	
	public static File showOpenDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);
		File file = fileChooser.showOpenDialog(window);
		setLastSelectedDirectory(file);
		setLastSelectedExtensionFilter(fileChooser.getSelectedExtensionFilter());
		return file;
	}
	
	public static List<File> showOpenMultipleDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);
		List<File> files = fileChooser.showOpenMultipleDialog(window);
		if (files != null && !files.isEmpty() && files.get(0) != null) {
			setLastSelectedDirectory(files.get(0));
			setLastSelectedExtensionFilter(fileChooser.getSelectedExtensionFilter());
		}

		return files;
	}
	
	public static File showSaveDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);

		File file = fileChooser.showSaveDialog(window);
		ExtensionFilter extensionFilter = fileChooser.getSelectedExtensionFilter();

		if (file != null && file.getParentFile() != null) {
			setLastSelectedDirectory(file.getParentFile());
			setLastSelectedExtensionFilter(extensionFilter);	
		}
		
		if (file != null && extensionFilter != null) {
			boolean hasExtension = false;
			List<String> extensions = extensionFilter.getExtensions();
			if (extensions != null && !extensions.isEmpty()) {
				for (String extension : extensions) {
					// if *.* filter is enabled or file ends with current extension --> true
					if (extension.endsWith("*.*") || file.getName().toLowerCase().endsWith(extension.toLowerCase().substring(1))) {
						hasExtension = true;
						break;
					}
				}
				// add first extension
				if (!hasExtension) 
					file = new File(file.getAbsolutePath() + extensions.get(0).substring(1));
			}
		}

		return file;
	}
	
	public static ExtensionFilter getSelectedExtensionFilter() {
		return fileChooser.getSelectedExtensionFilter();
	}
	
	public static void setLastSelectedExtensionFilter(ExtensionFilter extensionFilter) {
		if (extensionFilter == null)
			return;
		lastSelectedExtensionFilter = extensionFilter;
	}
	
	public static void setLastSelectedDirectory(File file) {
		if (file == null)
			return;

		if (file.isDirectory())
			lastSelectedDirectory = file;
		else if (file.isFile() && file.getParentFile() != null) {
			lastSelectedDirectory = file.getParentFile();
			setLastSelectedFileName(file);
		}
	}
	
	public static void setLastSelectedFileName(File file) {
		if (file == null)
			return;
		
		lastSelectedFileName = file.getName().replaceFirst(".[^.]+$", "");
	}
}
