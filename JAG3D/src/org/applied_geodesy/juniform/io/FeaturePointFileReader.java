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
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.ObservableUniqueList;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.stage.FileChooser.ExtensionFilter;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class FeaturePointFileReader extends SourceFileReader<ObservableUniqueList<FeaturePoint>> {
	private final FeatureType featureType;
	private ObservableUniqueList<FeaturePoint> points;
	
	public FeaturePointFileReader(FeatureType featureType) {
		this.featureType = featureType;
		this.reset();
	}
	
	public FeaturePointFileReader(String fileName, FeatureType featureType) {
		this(new File(fileName).toPath(), featureType);
	}

	public FeaturePointFileReader(File sf, FeatureType featureType) {
		this(sf.toPath(), featureType);
	}
	
	public FeaturePointFileReader(Path path, FeatureType featureType) {
		super(path);
		this.featureType = featureType;
		this.reset();
	}

	public FeatureType getFeatureType() {
		return this.featureType;
	}
	
	@Override
	public ObservableUniqueList<FeaturePoint> readAndImport() throws IOException, SQLException {
		this.ignoreLinesWhichStartWith("#");
		super.read();
		if (this.isInterrupted())
			this.points.clear();
		return this.points;
	}

	@Override
	public void reset() {
		if (this.points == null)
			 this.points = new ObservableUniqueList<FeaturePoint>(10000);
		this.points.clear();
	}

	@Override
	public void parse(String line) {
		line = line.trim();
		FeaturePoint point = null;
		switch(this.featureType) {
		case CURVE:
			point = scanCurvePoint(line);
			break;
		case SURFACE:
			point = scanSurfacePoint(line);
			break;		
		}

		if (point != null)
			this.points.add(point);
	}
	
	private static FeaturePoint scanCurvePoint(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		String columns[] = str.trim().split("[\\s;]+");
		
		if (columns.length < 4)
			return null;
		
		String name = columns[0]; 
		double x = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.'))); 
		double y = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.')));
		
		FeaturePoint point = new FeaturePoint(name, x, y);
		if (columns.length < 4)
			return point;
		
		double sigmaX, sigmaY;
		sigmaX = sigmaY = options.convertLengthToModel(Double.parseDouble(columns[3].replace(',', '.'))); 
		
		if (columns.length < 5) {
			if (sigmaX <= 0 || sigmaY <= 0)
				return point;
			
			Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			point.setDispersionApriori(dispersion);
			return point;
		}
		
		sigmaY = options.convertLengthToModel(Double.parseDouble(columns[4].replace(',', '.'))); 
		
		if (columns.length < 6) {
			if (sigmaX <= 0 || sigmaY <= 0)
				return point;
			
			Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			point.setDispersionApriori(dispersion);
			return point;
		}

		// first two values == first row/column
		double varX  = options.convertLengthToModel(sigmaX);
		double covXY = options.convertLengthToModel(sigmaY);

		double varY  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[5].replace(',', '.'))));

		if (varX <= 0 || varY <= 0)
			return point;

		Matrix dispersion = new UpperSymmPackMatrix(point.getDimension());
		dispersion.set(0, 0, varX);
		dispersion.set(0, 1, covXY);
		dispersion.set(1, 1, varY);
		point.setDispersionApriori(dispersion);
		return point;
	}
	
	private static FeaturePoint scanSurfacePoint(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		String columns[] = str.trim().split("[\\s;]+");
		
		if (columns.length < 4)
			return null;
		
		String name = columns[0]; 
		double x = options.convertLengthToModel(Double.parseDouble(columns[1].replace(',', '.'))); 
		double y = options.convertLengthToModel(Double.parseDouble(columns[2].replace(',', '.')));
		double z = options.convertLengthToModel(Double.parseDouble(columns[3].replace(',', '.')));
		
		FeaturePoint point = new FeaturePoint(name, x, y, z);
		if (columns.length < 5)
			return point;
		
		double sigmaX, sigmaY, sigmaZ;
		sigmaX = sigmaY = sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[4].replace(',', '.'))); 
		
		if (columns.length < 6) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return point;
			
			Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaY * sigmaY);
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			point.setDispersionApriori(dispersion);
			return point;
		}
		
		sigmaY = sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[5].replace(',', '.'))); 
		
		if (columns.length < 7) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return point;
			
			Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaX * sigmaX);
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			point.setDispersionApriori(dispersion);
			return point;
		}
		
		sigmaZ = options.convertLengthToModel(Double.parseDouble(columns[6].replace(',', '.'))); 
		
		if (columns.length < 10) {
			if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
				return point;
			
			Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
			dispersion.set(0, 0, sigmaX * sigmaX);
			dispersion.set(1, 1, sigmaX * sigmaX);
			dispersion.set(2, 2, sigmaZ * sigmaZ);
			point.setDispersionApriori(dispersion);
			return point;
		}
		
		// first three values == first row/column
		double varX  = options.convertLengthToModel(sigmaX);
		double covXY = options.convertLengthToModel(sigmaY);
		double covXZ = options.convertLengthToModel(sigmaZ);

		double varY  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[7].replace(',', '.'))));
		double covYZ = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[8].replace(',', '.'))));

		double varZ  = options.convertLengthToModel(options.convertLengthToModel(Double.parseDouble(columns[9].replace(',', '.'))));

		if (varX <= 0 || varY <= 0 || varZ <= 0)
			return point;

		Matrix dispersion = new UpperSymmPackMatrix(point.getDimension());
		dispersion.set(0, 0, varX);
		dispersion.set(0, 1, covXY);
		dispersion.set(0, 2, covXZ);
		dispersion.set(1, 1, varY);
		dispersion.set(1, 2, covYZ);
		dispersion.set(2, 2, varZ);
		point.setDispersionApriori(dispersion);
		return point;
	}
	
//	private static FeaturePoint scanCurvePoint(String str) {
//		FormatterOptions options = FormatterOptions.getInstance();
//		Scanner scanner = new Scanner( str.trim() );
//		try {
//			scanner.useLocale( Locale.ENGLISH );
//			String name; 
//			double y = 0.0, x = 0.0;
//			double sigmaY = 0.0, sigmaX = 0.0;
//
//			// Name of point
//			if (!scanner.hasNext())
//				return null;
//			name = scanner.next();
//			
//			// x-component 		
//			if (!scanner.hasNextDouble())
//				return null;
//			x = options.convertLengthToModel(scanner.nextDouble());
//
//			// y-component 		
//			if (!scanner.hasNextDouble())
//				return null;
//			y = options.convertLengthToModel(scanner.nextDouble());
//			
//			FeaturePoint point = new FeaturePoint(name, x, y);
//
//			// uncertainty: sigma X (or sigmaX/Y)
//			if (!scanner.hasNextDouble()) 
//				return point;
//			
//			sigmaY = sigmaX = options.convertLengthToModel(scanner.nextDouble());
//
//			// uncertainty: sigma Y
//			if (!scanner.hasNextDouble()) {
//				if (sigmaX <= 0 || sigmaY <= 0)
//					return point;
//				
//				Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
//				dispersion.set(0, 0, sigmaX * sigmaX);
//				dispersion.set(1, 1, sigmaY * sigmaY);
//				point.setDispersionApriori(dispersion);
//				return point;
//			}
//			
//			sigmaY = options.convertLengthToModel(scanner.nextDouble());
//			
//			// fully populated co-variance: varX covXY varY
//			// correlation: XY
//			if (!scanner.hasNextDouble()) {
//				if (sigmaX <= 0 || sigmaY <= 0)
//					return point;
//				
//				Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
//				dispersion.set(0, 0, sigmaX * sigmaX);
//				dispersion.set(1, 1, sigmaY * sigmaY);
//				point.setDispersionApriori(dispersion);
//				return point;
//			}
//			
//			// first two values == first row/column
//			double varX  = options.convertLengthToModel(sigmaX);
//			double covXY = options.convertLengthToModel(sigmaY);
//
//			double varY  = options.convertLengthToModel(options.convertLengthToModel(scanner.nextDouble()));
//			
//			if (varX <= 0 || varY <= 0)
//				return point;
//			
//			Matrix dispersion = new UpperSymmPackMatrix(point.getDimension());
//			dispersion.set(0, 0, varX);
//			dispersion.set(0, 1, covXY);
//			dispersion.set(1, 1, varY);
//			point.setDispersionApriori(dispersion);
//			return point;
//			
//		}
//		finally {
//			scanner.close();
//		}
//	}
	
//	private static FeaturePoint scanSurfacePoint(String str) {
//		FormatterOptions options = FormatterOptions.getInstance();
//		Scanner scanner = new Scanner( str.trim() );
//		try {
//			scanner.useLocale( Locale.ENGLISH );
//			String name; 
//			double y = 0.0, x = 0.0, z = 0.0;
//			double sigmaY = 0.0, sigmaX = 0.0, sigmaZ = 0.0;
//
//			// Name of point
//			if (!scanner.hasNext())
//				return null;
//			name = scanner.next();
//			
//			// x-component 		
//			if (!scanner.hasNextDouble())
//				return null;
//			x = options.convertLengthToModel(scanner.nextDouble());
//
//			// y-component		
//			if (!scanner.hasNextDouble())
//				return null;
//			y = options.convertLengthToModel(scanner.nextDouble());
//
//			// z-component 		
//			if (!scanner.hasNextDouble())
//				return null;
//			z = options.convertLengthToModel(scanner.nextDouble());
//
//			FeaturePoint point = new FeaturePoint(name, x, y, z);
//			
//			// uncertainty: sigma X (or sigma x/y/z)
//			if (!scanner.hasNextDouble()) 
//				return point;
//
//			sigmaX = sigmaY = sigmaZ = options.convertLengthToModel(scanner.nextDouble());
//			
//			// uncertainty: sigma Y (or sigma Z)
//			if (!scanner.hasNextDouble()) {
//				if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
//					return point;
//
//				Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
//				dispersion.set(0, 0, sigmaX * sigmaX);
//				dispersion.set(1, 1, sigmaY * sigmaY);
//				dispersion.set(2, 2, sigmaZ * sigmaZ);
//				point.setDispersionApriori(dispersion);
//				return point;
//			}
//
//			sigmaY = sigmaZ = options.convertLengthToModel(scanner.nextDouble());
//
//			// uncertainty: sigma Z
//			if (!scanner.hasNextDouble()) {
//				if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
//					return point;
//				
//				Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
//				dispersion.set(0, 0, sigmaX * sigmaX);
//				dispersion.set(1, 1, sigmaX * sigmaX);
//				dispersion.set(2, 2, sigmaZ * sigmaZ);
//				point.setDispersionApriori(dispersion);
//				return point;
//			}
//
//			sigmaZ = options.convertLengthToModel(scanner.nextDouble());
//
//			// fully populated co-variance: varX covXY covXZ varY covYZ varZ
//			// correlation: XYZ
//			if (!scanner.hasNextDouble()) {
//				if (sigmaX <= 0 || sigmaY <= 0 || sigmaZ <= 0)
//					return point;
//				
//				Matrix dispersion = new UpperSymmBandMatrix(point.getDimension(), 0);
//				dispersion.set(0, 0, sigmaX * sigmaX);
//				dispersion.set(1, 1, sigmaY * sigmaY);
//				dispersion.set(2, 2, sigmaZ * sigmaZ);
//				point.setDispersionApriori(dispersion);
//				return point;
//			}
//			
//			// first three values == first row/column
//			double varX  = options.convertLengthToModel(sigmaX);
//			double covXY = options.convertLengthToModel(sigmaY);
//			double covXZ = options.convertLengthToModel(sigmaZ);
//			
//			double varY  = options.convertLengthToModel(options.convertLengthToModel(scanner.nextDouble()));
//			double covYZ = options.convertLengthToModel(options.convertLengthToModel(scanner.nextDouble()));
//			
//			double varZ  = options.convertLengthToModel(options.convertLengthToModel(scanner.nextDouble()));
//
//			if (varX <= 0 || varY <= 0 || varZ <= 0)
//				return point;
//			
//			Matrix dispersion = new UpperSymmPackMatrix(point.getDimension());
//			dispersion.set(0, 0, varX);
//			dispersion.set(0, 1, covXY);
//			dispersion.set(0, 2, covXZ);
//			dispersion.set(1, 1, varY);
//			dispersion.set(1, 2, covYZ);
//			dispersion.set(2, 2, varZ);
//			point.setDispersionApriori(dispersion);
//			return point;
//
//		}
//		finally {
//			scanner.close();
//		}
//	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("FeaturePointFileReader.extension.description", "All files"),	"*.*")
		};
	}
}
