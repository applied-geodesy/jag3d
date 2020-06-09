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

package org.applied_geodesy.juniform.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public abstract class NISTTest {
	// https://www.nist.gov/pml/sensor-science/dimensional-metrology/algorithm-testing
	
	public enum ComponentOrderType {
		XY, XZ, YZ, XYZ
	}
	
	static final double EPS = Math.sqrt(Constant.EPS);
	static final String TEMPLATE = "%30s EST = %+25.16f REF = %+25.16f  DIFF = %+6.3e %s";
	static ComponentOrderType CMP_TYPE = ComponentOrderType.XYZ;
	
	public static List<Double> readFittingResults(Path path) throws IOException, NullPointerException, NumberFormatException {
		List<Double> values = new ArrayList<Double>();
		try(BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))){
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				double value = Double.parseDouble(currentLine.trim());
				values.add(value);
			}
		}
		return values;
	}
	
	public static List<FeaturePoint> readCoordinates(Path path, int dim) throws IOException, NullPointerException, NumberFormatException {
		List<FeaturePoint> points = new ArrayList<FeaturePoint>();

		if (dim == 3)
			CMP_TYPE = ComponentOrderType.XYZ;
		
		int cnt = 1;

		try(BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				String data[] = currentLine.split("\\s+");
				if (data.length >= dim) {

					double x = Double.parseDouble(data[0]);
					double y = Double.parseDouble(data[1]);
					double z = Double.parseDouble(data[2]);
				
					FeaturePoint point = new FeaturePoint(String.valueOf(cnt), x, y, z);
					
					if (point != null)
						points.add(point);
									}
			}
			
			if (dim == 2) {
				List<FeaturePoint> points2d = new ArrayList<FeaturePoint>(points.size());
				FeaturePoint firstPoint = points.get(0);
				FeaturePoint lastPoint  = points.get(points.size() - 1);

				if (Math.abs(firstPoint.getX0() - lastPoint.getX0()) < Math.abs(firstPoint.getY0() - lastPoint.getY0()) &&  Math.abs(firstPoint.getX0() - lastPoint.getX0()) < Math.abs(firstPoint.getZ0() - lastPoint.getZ0()))
					CMP_TYPE = ComponentOrderType.YZ;
				
				if (Math.abs(firstPoint.getY0() - lastPoint.getY0()) < Math.abs(firstPoint.getX0() - lastPoint.getX0()) &&  Math.abs(firstPoint.getY0() - lastPoint.getY0()) < Math.abs(firstPoint.getZ0() - lastPoint.getZ0()))
					CMP_TYPE = ComponentOrderType.XZ;
				
				if (Math.abs(firstPoint.getZ0() - lastPoint.getZ0()) < Math.abs(firstPoint.getX0() - lastPoint.getX0()) &&  Math.abs(firstPoint.getZ0() - lastPoint.getZ0()) < Math.abs(firstPoint.getY0() - lastPoint.getY0()))
					CMP_TYPE = ComponentOrderType.XY;

				for (FeaturePoint point3d : points) {
					if (CMP_TYPE == ComponentOrderType.YZ) {
						points2d.add(new FeaturePoint(point3d.getName(), point3d.getY0(), point3d.getZ0()));
					}
					else if (CMP_TYPE == ComponentOrderType.XZ) {
						points2d.add(new FeaturePoint(point3d.getName(), point3d.getX0(), point3d.getZ0()));
					}
					else if (CMP_TYPE == ComponentOrderType.XY) {
						points2d.add(new FeaturePoint(point3d.getName(), point3d.getX0(), point3d.getY0()));
					}
				}
				
				points = points2d;
			}
		}
		return points;
	}
	
	public void start(String directory) throws Exception {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory), "*.ds")) {
			for (Path pointPath : directoryStream) {
				String pathName = pointPath.toString();
				int pos = pathName.lastIndexOf(".");
				if (pos > 0 && pos < (pathName.length() - 1)) {
					pathName = pathName.substring(0, pos);
				}
				Path resultPath = Paths.get(pathName + ".fit");

				System.out.println("\n==========================\n");
				System.out.println(pointPath.getFileName());
				
				List<FeaturePoint> featurePoints = readCoordinates(pointPath, this.getDimension());
				List<Double> referenceResults    = readFittingResults(resultPath);
				
				Feature feature = this.adjust(featurePoints);
				if (feature == null) {
					System.err.println("Adjustment failed!");
					continue;
				}
				
				List<UnknownParameter> unknownParameters = feature.getUnknownParameters();

				this.compare(referenceResults, unknownParameters);
			}
		} 
	}
	
	public Feature adjust(List<FeaturePoint> points) {
		FeatureAdjustment adjustment = new FeatureAdjustment();

		Feature feature = this.getFeature();

		for (GeometricPrimitive geometricPrimitive : feature)
			geometricPrimitive.getFeaturePoints().addAll(points);


		try {
			// derive parameters for warm start of adjustment
			if (feature.isEstimateInitialGuess())
				feature.deriveInitialGuess();
			adjustment.setLevenbergMarquardtDampingValue(this.getLambda());
			adjustment.setFeature(feature);
			adjustment.init();
			EstimationStateType type = adjustment.estimateModel();

			if (type == EstimationStateType.ERROR_FREE_ESTIMATION)
				return feature;

		} catch (MatrixSingularException | IllegalArgumentException | UnsupportedOperationException | NotConvergedException e) {
			//e.printStackTrace();
		}

		return null;
	}
	
	double getLambda() {
		return 0.0;
	}

	abstract int getDimension();

	abstract Feature getFeature();
	
	abstract void compare(List<Double> referenceResults, List<UnknownParameter> unknownParameters);
}
