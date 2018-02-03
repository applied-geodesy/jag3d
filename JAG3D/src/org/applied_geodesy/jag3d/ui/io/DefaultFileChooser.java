package org.applied_geodesy.jag3d.ui.io;

import java.io.File;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class DefaultFileChooser {
	private static Window window;
	private static File lastSelectedDirectory = null;
	private static String lastSelectedFileName = "";
	private static final FileChooser fileChooser = new FileChooser();
	DefaultFileChooser() {}
		
	public static void setStage(Window parent) {
		window = parent;
	}

	private static void prepareFileChooser(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		if (lastSelectedDirectory == null)
			lastSelectedDirectory = new File("DB"); //new File(System.getProperty("user.home"));
		fileChooser.setInitialDirectory(lastSelectedDirectory);  
		if (initialFileName == null)
			fileChooser.setInitialFileName(lastSelectedFileName);
		fileChooser.setTitle(title);
		if (extensionFilters != null && extensionFilters.length > 0)
			fileChooser.getExtensionFilters().setAll(extensionFilters);
		else
			fileChooser.getExtensionFilters().clear();
	}
	
	public static File showOpenDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);
		File file = fileChooser.showOpenDialog(window);
		lastSelectedDirectory = file == null || file.getParentFile() == null ? lastSelectedDirectory : file.getParentFile();
		lastSelectedFileName = file == null ? lastSelectedFileName : file.getName().replaceFirst(".[^.]+$", "");
		return file;
	}
	
	public static List<File> showOpenMultipleDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);
		List<File> files = fileChooser.showOpenMultipleDialog(window);
		lastSelectedDirectory = files == null || files.isEmpty() || files.get(0) == null || files.get(0).getParentFile() == null ? lastSelectedDirectory : files.get(0).getParentFile();
		lastSelectedFileName = files == null || files.get(0) == null ? lastSelectedFileName : files.get(0).getName().replaceFirst(".[^.]+$", "");
		return files;
	}
	
	public static File showSaveDialog(String title, String initialFileName, ExtensionFilter... extensionFilters) {
		prepareFileChooser(title, initialFileName, extensionFilters);

//		File selectedFile = null;
//		do {
//			//selectedFile = this.fileChooser.showSaveDialog(window);
//			selectedFile = this.fileChooser.showOpenDialog(window);
//			
//			if (selectedFile == null || !Files.exists(selectedFile.toPath(), LinkOption.NOFOLLOW_LINKS))
//				break;
//			
//			I18N i18n = I18N.getInstance();
//			Alert alert = new Alert(AlertType.CONFIRMATION);
//			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//			alert.setTitle(i18n.getString("DefaultFileChooser.confirmation.overwrite.title", "Overwrite existing file?"));
//			alert.setHeaderText(i18n.getString("DefaultFileChooser.confirmation.overwrite.header", "The selected file already exists."));
//			alert.setContentText(i18n.getString("DefaultFileChooser.confirmation.overwrite.header", "The selected file already exists. Do you want to overwrite it?"));
//			Optional<ButtonType> result = alert.showAndWait();
//				if (result.get() == ButtonType.YES)
//					break;
//				else if (result.get() == ButtonType.CANCEL)
//					continue;
//				else if (result.get() == ButtonType.NO)
//					return null;	
//
//		} while (true);
		
		//return selectedFile;
		File file = fileChooser.showSaveDialog(window);
		lastSelectedDirectory = file == null || file.getParentFile() == null ? lastSelectedDirectory : file.getParentFile();
		lastSelectedFileName = file == null ? lastSelectedFileName : file.getName().replaceFirst(".[^.]+$", "");
		
		ExtensionFilter extensionFilter = fileChooser.getSelectedExtensionFilter();
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
}
