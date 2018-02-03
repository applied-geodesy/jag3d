package org.applied_geodesy.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

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
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(i18n.getString("SourceFileReader.extension.default", "All files"), "*.*")
		};
	}
}