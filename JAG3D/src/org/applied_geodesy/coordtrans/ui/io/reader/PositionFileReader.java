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

package org.applied_geodesy.coordtrans.ui.io.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.point.ObservedFramePosition;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.stage.FileChooser.ExtensionFilter;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class PositionFileReader extends SourceFileReader<Map<String, ObservedFramePosition>> {
	private static long id = 0;
	private Map<String, ObservedFramePosition> positions;
	private final TransformationType transformationType;
	public PositionFileReader(TransformationType transformationType) {
		this.transformationType = transformationType;
		this.reset();
	}
	
	public PositionFileReader(String fileName, TransformationType transformationType) {
		this(new File(fileName).toPath(), transformationType);
	}

	public PositionFileReader(File sf, TransformationType transformationType) {
		this(sf.toPath(), transformationType);
	}
	
	public PositionFileReader(Path path, TransformationType transformationType) {
		super(path);
		this.transformationType = transformationType;
		this.reset();
	}

	public TransformationType getTransformationType() {
		return this.transformationType;
	}

	@Override
	public Map<String, ObservedFramePosition> readAndImport() throws Exception {
		this.ignoreLinesWhichStartWith("#");
		super.read();
		if (this.isInterrupted())
			this.positions.clear();
		return this.positions;
	}

	@Override
	public void reset() {
		this.positions = new LinkedHashMap<String, ObservedFramePosition>(10000);
		id = 0;
	}

	@Override
	public void parse(String line) {
		line = line.trim();

		try {
			switch(this.transformationType) {
			case HEIGHT:
				scanHeightPosition(this.positions, line);
				break;
			case PLANAR:
				scanPlanarPosition(this.positions, line);
				break;
			case SPATIAL:
				scanSpatialPosition(this.positions, line);
				break;	
			}
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private static boolean scanHeightPosition(Map<String, ObservedFramePosition> positionMap, String str) throws NumberFormatException, IllegalArgumentException {
		FormatterOptions options = FormatterOptions.getInstance();
		String columns[] = str.trim().split("[\\s;]+");

		if (columns.length == 1) {
			String name = String.valueOf(++PositionFileReader.id); 
			double z = options.convertLengthToModel(Double.parseDouble(columns[0].replace(',', '.'))); 
			ObservedFramePosition position = new ObservedFramePosition(z);
			positionMap.put(name, position);
			return true;
		}
		
		if (columns.length < 2)
			return false;
		
		String name = columns[0]; 
		double z = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.'))); 
		
		ObservedFramePosition position = new ObservedFramePosition(z);
		positionMap.put(name, position);
		
		if (columns.length < 3)
			return true;
		
		double sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.'))); 
		
		if (sigmaZ <= 0)
			return true;

		Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
		dispersion.set(0, 0, sigmaZ * sigmaZ);
		position.setDispersionApriori(dispersion);
		return true;
	}
	
	private static boolean scanPlanarPosition(Map<String, ObservedFramePosition> positionMap, String str) throws NumberFormatException, IllegalArgumentException {
		FormatterOptions options = FormatterOptions.getInstance();
		String columns[] = str.trim().split("[\\s;]+");

		if (columns.length == 2) {
			String name = String.valueOf(++PositionFileReader.id); 
			double x = options.convertLengthToModel(Double.parseDouble(columns[0].replace(',', '.'))); 
			double y = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.')));
			ObservedFramePosition position = new ObservedFramePosition(x, y);
			positionMap.put(name, position);
			return true;
		}

		if (columns.length < 3)
			return false;
		
		String name = columns[0]; 
		double x = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.'))); 
		double y = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.')));
		
		ObservedFramePosition position = new ObservedFramePosition(x, y);
		positionMap.put(name, position);
		
		if (columns.length < 4)
			return true;
		
		double sigmaX, sigmaY;
		sigmaX = sigmaY = options.convertLengthToModel(Double.parseDouble(columns[3].replace(',', '.'))); 
		
		if (columns.length < 5) {
			if (sigmaX <= 0 || sigmaY <= 0)
				return true;
			
			Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			position.setDispersionApriori(dispersion);
			return true;
		}
		
		sigmaY = options.convertLengthToModel(Double.parseDouble(columns[4].replace(',', '.'))); 
		
		if (columns.length < 6) {
			if (sigmaX <= 0 || sigmaY <= 0)
				return true;
			
			Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			position.setDispersionApriori(dispersion);
			return true;
		}

		// first two values == first row/column
		double varX  = options.convertLengthToModel(sigmaX);
		double covXY = options.convertLengthToModel(sigmaY);

		double varY  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[5].replace(',', '.'))));

		if (varX <= 0 || varY <= 0)
			return true;
		
		Matrix dispersion = null;
		if (covXY == 0)
			dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
		else {
			dispersion = new UpperSymmPackMatrix(position.getDimension());
			dispersion.set(0, 1, covXY);
		}
		dispersion.set(0, 0, varX);
		dispersion.set(1, 1, varY);
		position.setDispersionApriori(dispersion);
		return true;
	}
	
	private static boolean scanSpatialPosition(Map<String, ObservedFramePosition> positionMap, String str) throws NumberFormatException, IllegalArgumentException {
		FormatterOptions options = FormatterOptions.getInstance();
		String columns[] = str.trim().split("[\\s;]+");
		
		if (columns.length == 3) {
			String name = String.valueOf(++PositionFileReader.id); 
			double x = options.convertLengthToModel(Double.parseDouble(columns[0].replace(',', '.'))); 
			double y = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.')));
			double z = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.')));
			ObservedFramePosition position = new ObservedFramePosition(x, y, z);
			positionMap.put(name, position);
			return true;
		}
		
		if (columns.length < 4)
			return false;
		
		String name = columns[0]; 
		double x = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.'))); 
		double y = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.')));
		double z = options.convertLengthToModel(Double.parseDouble(columns[3].replace(',', '.')));
		
		ObservedFramePosition position = new ObservedFramePosition(x, y, z);
		positionMap.put(name, position);
		
		if (columns.length < 5)
			return true;
		
		double sigmaX, sigmaY, sigmaZ;
		sigmaX = sigmaY = sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[4].replace(',', '.'))); 
		
		if (columns.length < 6) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return true;
			
			Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			position.setDispersionApriori(dispersion);
			return true;
		}
		
		sigmaY = sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[5].replace(',', '.'))); 
		
		if (columns.length < 7) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return true;
			
			Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaX * sigmaX); // if only two uncertainty colums are given, set sx = sy; sz
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			position.setDispersionApriori(dispersion);
			return true;
		}
		
		sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[6].replace(',', '.'))); 
		
		if (columns.length < 10) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return true;
			
			Matrix dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			position.setDispersionApriori(dispersion);
			return true;
		}
		
		// first three values == first row/column
		double varX  = options.convertLengthToModel(sigmaX);
		double covXY = options.convertLengthToModel(sigmaY);
		double covXZ = options.convertLengthToModel(sigmaZ);

		double varY  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[7].replace(',', '.'))));
		double covYZ = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[8].replace(',', '.'))));

		double varZ  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[9].replace(',', '.'))));

		if (varX <= 0 || varY <= 0 || varZ <= 0)
			return true;

		Matrix dispersion = null;
		if (covXY == 0 && covXZ == 0 && covYZ == 0)
			dispersion = new UpperSymmBandMatrix(position.getDimension(), 0);
		else {
			dispersion = new UpperSymmPackMatrix(position.getDimension());
			dispersion.set(0, 1, covXY);
			dispersion.set(0, 2, covXZ);
			dispersion.set(1, 2, covYZ);
		}
		dispersion.set(0, 0, varX);
		dispersion.set(1, 1, varY);
		dispersion.set(2, 2, varZ);
		position.setDispersionApriori(dispersion);
		return true;
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("PositionFileReader.extension.description", "All files"),	"*.*")
		};
	}
}
