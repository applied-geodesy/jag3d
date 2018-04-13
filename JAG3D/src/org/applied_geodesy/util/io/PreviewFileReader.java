package org.applied_geodesy.util.io;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreviewFileReader extends LockFileReader {
	private final static int MAX_LINES = 15;
	private int maxLines = PreviewFileReader.MAX_LINES, lineCounter = 0;
	private List<String> lines = new ArrayList<String>();

	public PreviewFileReader(File sf) {
		this(sf, MAX_LINES);
	}
	
	public PreviewFileReader(File sf, int maxLines) {
		super(sf);
		this.maxLines = maxLines;
	}
	
	public void reset() {
		this.lineCounter = 0;
		this.lines.clear();
	}

	@Override
	public void read() throws IOException, SQLException {
		this.reset();
		super.read();
	}

	@Override
	public void parse(String line) {
		if (this.lines.add(line))
			this.lineCounter++;

		if (this.lineCounter > this.maxLines)
			super.interrupt();
	}
	
	public List<String> getLines() {
		return this.lines;
	}
}