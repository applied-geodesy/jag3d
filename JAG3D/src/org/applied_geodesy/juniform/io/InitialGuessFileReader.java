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

package org.applied_geodesy.juniform.io;

import java.io.File;
import java.nio.file.Path;

import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.stage.FileChooser.ExtensionFilter;

public class InitialGuessFileReader extends SourceFileReader<GeometricPrimitive> {
	private final GeometricPrimitive geometricPrimitive;
	private boolean containsValidContent = false;
	public InitialGuessFileReader(GeometricPrimitive geometricPrimitive) {
		this.geometricPrimitive = geometricPrimitive;
		this.reset();
	}
	
	public InitialGuessFileReader(String fileName, GeometricPrimitive geometricPrimitive) {
		this(new File(fileName).toPath(), geometricPrimitive);
	}

	public InitialGuessFileReader(File sf, GeometricPrimitive geometricPrimitive) {
		this(sf.toPath(), geometricPrimitive);
	}
	
	public InitialGuessFileReader(Path path, GeometricPrimitive geometricPrimitive) {
		super(path);
		this.geometricPrimitive = geometricPrimitive;
		this.reset();
	}

	@Override
	public GeometricPrimitive readAndImport() throws Exception {
		this.ignoreLinesWhichStartWith("#");
		super.read();
		
		if (!this.containsValidContent)
			throw new IllegalArgumentException("Error, selected file does not contain valid initial values!");
		
		return this.geometricPrimitive;
	}

	@Override
	public void reset() {
		this.containsValidContent = false;
	}

	@Override
	public void parse(String line) {
		try {
			if (line == null || line.isBlank())
				return;

			String data[] = line.trim().split("[;=\\s]+");

			if (data.length < 2)
				return;
			ParameterType type = ParameterType.valueOf(data[0]);
			double value0 = Double.parseDouble(data[1]);
			UnknownParameter parameter = this.geometricPrimitive.getUnknownParameter(type);
			
			if (parameter != null) {
				parameter.setValue0(value0);
				this.containsValidContent = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("InitialGuessFileReader.extension.description", "All files"),	"*.*")
		};
	}
}
