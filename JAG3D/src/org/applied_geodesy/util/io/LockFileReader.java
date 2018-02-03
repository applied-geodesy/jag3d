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
	
	public ExtensionFilter getExtensionFilter() {
		return new ExtensionFilter(
				I18N.getInstance().getString("LockFileReader.extension.description", "All files"),
				"*.*"
		);
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

		BufferedReader reader = null;
		boolean isFirstLine = true;
		try{
			FileInputStream inputStream = new FileInputStream( this.sourceFilePath.toFile() );
			inputStream.getChannel().lock(0, Long.MAX_VALUE, true);
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
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
} 