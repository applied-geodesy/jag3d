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

package org.applied_geodesy.jag3d.ui.io.writer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.network.NetworkAdjustment;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameters;
import org.applied_geodesy.adjustment.network.point.Point;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

public class MatlabNetworkAdjustmentResultWriter extends NetworkAdjustmentResultWriter {

	public MatlabNetworkAdjustmentResultWriter(String exportPathAndFileBaseName) {
		super(exportPathAndFileBaseName);
	}

	@Override
	public void export(NetworkAdjustment networkAdjustment) throws NullPointerException, IllegalArgumentException, IOException {
		if (networkAdjustment == null)
			throw new NullPointerException("Error, network adjustment object cannot be null!");
		
		String exportPathAndFileBaseName = this.getExportPathAndFileBaseName();
		
		if (exportPathAndFileBaseName == null)
			throw new NullPointerException("Error, export path cannot be null!");
		
		int numberOfUnknownParameters = networkAdjustment.getNumberOfUnknownParameters();
		int numberOfObservations      = networkAdjustment.getNumberOfObservations();
		int degreeOfFreedom           = networkAdjustment.degreeOfFreedom();
		double varianceOfUnitWeight   = networkAdjustment.getVarianceFactorAposteriori();
		no.uib.cipr.matrix.Matrix cofactor = networkAdjustment.getCofactorMatrix();
		
		if (cofactor == null || cofactor.numRows() < numberOfUnknownParameters || varianceOfUnitWeight < 0)
			throw new NullPointerException("Error, dipsersion matrix cannot be null!");

		List<Point> points = this.extractPointsFromUnknownParameters(networkAdjustment);
		
		File binFile = new File(exportPathAndFileBaseName + ".mat");
		Struct coordinates = Mat5.newStruct(1, points.size());

		int structIndex = 0;
		for (Point point : points) {	
			if (networkAdjustment.isInterrupted())
				break;
			
			String name = point.getName();
			int dim = point.getDimension();
			int colInJacobi = point.getColInJacobiMatrix();
			
			double x0 = 0, z0 = 0, y0 = 0;
			double x = 0, z = 0, y = 0;
			int columnX = -1, columnY = -1, columnZ = -1;

			if (dim != 1) {
				x0 = point.getX0();
				y0 = point.getY0();
				
				x = point.getX();
				y = point.getY();
				
				columnX = ++colInJacobi;
				columnY = ++colInJacobi;
			}
			if (dim != 2) {
				z0 = point.getZ0();
				z = point.getZ();
				columnZ = ++colInJacobi;
			}
						
			coordinates.set("name", structIndex, Mat5.newString(name));
			coordinates.set("dimension", structIndex, newInteger(dim));
			
			coordinates.set("x0", structIndex, newDouble(x0));
			coordinates.set("y0", structIndex, newDouble(y0));
			coordinates.set("z0", structIndex, newDouble(z0));

			coordinates.set("x", structIndex, newDouble(x));
			coordinates.set("y", structIndex, newDouble(y));
			coordinates.set("z", structIndex, newDouble(z));

			coordinates.set("covx", structIndex, newInteger(columnX));
			coordinates.set("covy", structIndex, newInteger(columnY));
			coordinates.set("covz", structIndex, newInteger(columnZ));
			
			structIndex++;
		}

		Matrix dispersion = Mat5.newMatrix(numberOfUnknownParameters, numberOfUnknownParameters, MatlabType.Double);
		for (int rowIdx = 0; rowIdx < numberOfUnknownParameters; rowIdx++) {
			if (networkAdjustment.isInterrupted())
				break;
			
			double var = cofactor.get(rowIdx, rowIdx);
			dispersion.setDouble(rowIdx, rowIdx, var);
			
			for (int columnIdx = rowIdx + 1; columnIdx < numberOfUnknownParameters; columnIdx++) {
				if (networkAdjustment.isInterrupted())
					break;
				
				double covar = cofactor.get(rowIdx, columnIdx);
				dispersion.setDouble(rowIdx, columnIdx, covar);
				dispersion.setDouble(columnIdx, rowIdx, covar);
			}
		}
		
		MatFile matFile = Mat5.newMatFile();
		
		matFile.addArray("variance_of_unit_weight_prio", newDouble(1.0));
		matFile.addArray("variance_of_unit_weight_post", newDouble(varianceOfUnitWeight));
		matFile.addArray("degree_of_freedom",            newInteger(degreeOfFreedom));
		matFile.addArray("number_of_observations",       newInteger(numberOfObservations));
		matFile.addArray("number_of_unknowns",           newInteger(numberOfUnknownParameters));
		
		matFile.addArray("coordinates", coordinates);
		matFile.addArray("dispersion",  dispersion);
		
		Mat5.writeToFile(matFile, binFile);		
	}
	
	private List<Point> extractPointsFromUnknownParameters(NetworkAdjustment networkAdjustment) {
		UnknownParameters unknownParameters = networkAdjustment.getUnknownParameters();
		List<Point> points = new ArrayList<Point>(unknownParameters.size());
	
		for (UnknownParameter unknownParameter : unknownParameters) {
			if (networkAdjustment.isInterrupted())
				break;
			
			if (unknownParameter.getParameterType() != ParameterType.POINT1D && unknownParameter.getParameterType() != ParameterType.POINT2D && unknownParameter.getParameterType() != ParameterType.POINT3D)
				continue;
			
			Point point = (Point)unknownParameter;
			points.add(point);
		}
		
		return points;
	}
	
	private static Matrix newInteger(int value) {
		Matrix matrix = Mat5.newMatrix(1, 1, MatlabType.Int32);
		matrix.setInt(0, 0, value);
		return matrix;
	}
	
	private static Matrix newDouble(double value) {
		Matrix matrix = Mat5.newMatrix(1, 1, MatlabType.Double);
		matrix.setDouble(0, 0, value);
		return matrix;
	}
}
