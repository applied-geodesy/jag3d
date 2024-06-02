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

package org.applied_geodesy.coordtrans.ui.io.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.VarianceComponent;
import org.applied_geodesy.adjustment.transformation.VarianceComponentType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


public class MatlabTransformationAdjustmentResultWriter {

	public void toFile(File binFile, TransformationAdjustment adjustment) throws NullPointerException, IllegalArgumentException, IOException {
		if (adjustment == null || adjustment.getTransformation() == null)
			throw new NullPointerException("Error, feature object cannot be null!");
		
		if (binFile == null)
			throw new NullPointerException("Error, export path cannot be null!");
		
		List<UnknownParameter> unknownParameters = adjustment.getTransformation().getUnknownParameters();
		Struct parameters = Mat5.newStruct(1, unknownParameters.size());
		int structIndex = 0;
		int numberOfUnknownParameters = 0;
		for (UnknownParameter unknownParameter : unknownParameters) {
			int column = unknownParameter.getColumn();

			parameters.set("name", structIndex, Mat5.newString(unknownParameter.getName()));
			parameters.set("parameter_type", structIndex, Mat5.newString(unknownParameter.getParameterType().name()));
			parameters.set("processing_type", structIndex, Mat5.newString(unknownParameter.getProcessingType().name()));
			parameters.set("value", structIndex, newDouble(unknownParameter.getValue()));
			parameters.set("sigma", structIndex, newDouble(unknownParameter.getUncertainty()));
			
			if (column >= 0) {
				numberOfUnknownParameters++;
				parameters.set("column", structIndex, newInteger(column + 1));
			}
			else
				parameters.set("column", structIndex, newInteger(-1));
			
			structIndex++;
		}
		
		no.uib.cipr.matrix.Matrix correlationMatrix = adjustment.getCorrelationMatrix();
		
		int cols = correlationMatrix.numColumns();
		int rows = correlationMatrix.numRows();
		
		Matrix correlations = Mat5.newMatrix(rows, cols, MatlabType.Double);
		for (int rowIdx = 0; rowIdx < rows; rowIdx++) {
			double corr = correlationMatrix.get(rowIdx, rowIdx);
			correlations.setDouble(rowIdx, rowIdx, corr);
			for (int columnIdx = rowIdx + 1; columnIdx < cols; columnIdx++) {
				corr = correlationMatrix.get(rowIdx, columnIdx);
				correlations.setDouble(rowIdx, columnIdx, corr);
				correlations.setDouble(columnIdx, rowIdx, corr);
			}
		}

		VarianceComponent varianceComponentOfUnitWeight = adjustment.getVarianceComponent(VarianceComponentType.GLOBAL);
		int degreeOfFreedom = (int)Math.rint(varianceComponentOfUnitWeight.getRedundancy());
		int numberOfPoints = adjustment.getTransformation().getHomologousFramePositionPairs().size();
		int dimension = adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension();
		double sigma2aprio = varianceComponentOfUnitWeight.getVariance0();
		double sigma2apost = varianceComponentOfUnitWeight.getVariance();
		
		MatFile matFile = Mat5.newMatFile();
		
		matFile.addArray("variance_of_unit_weight_prio", newDouble(sigma2aprio));
		matFile.addArray("variance_of_unit_weight_post", newDouble(sigma2apost));
		matFile.addArray("degree_of_freedom",            newInteger(degreeOfFreedom));
		matFile.addArray("number_of_observations",       newInteger(dimension * numberOfPoints));
		matFile.addArray("number_of_unknowns",           newInteger(numberOfUnknownParameters));
		
		matFile.addArray("parameters",   parameters);
		matFile.addArray("correlations", correlations);
		
		Mat5.writeToFile(matFile, binFile);		
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
