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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.applied_geodesy.adjustment.network.NetworkAdjustment;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameters;
import org.applied_geodesy.adjustment.network.point.Point;

import no.uib.cipr.matrix.Matrix;

public class DefaultNetworkAdjustmentResultWriter extends NetworkAdjustmentResultWriter {

	public DefaultNetworkAdjustmentResultWriter(String exportPathAndFileBaseName) {
		super(exportPathAndFileBaseName);
	}

	@Override
	public void export(NetworkAdjustment networkAdjustment) throws NullPointerException, IllegalArgumentException, IOException {
		if (networkAdjustment == null)
			throw new NullPointerException("Error, network adjustment cannot be null!");
		
		String exportPathAndFileBaseName = this.getExportPathAndFileBaseName();
		
		if (exportPathAndFileBaseName == null)
			throw new NullPointerException("Error, export path cannot be null!");

		this.exportCovarianceInformation(networkAdjustment, new File(exportPathAndFileBaseName + ".info"));
		this.exportCovarianceMatrix(networkAdjustment, new File(exportPathAndFileBaseName + ".cxx"));
	}

	private void exportCovarianceInformation(NetworkAdjustment networkAdjustment, File file) throws IOException {
		if (networkAdjustment.getCofactorMatrix() == null)
			return;
		
		UnknownParameters unknownParameters = networkAdjustment.getUnknownParameters();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			//Name,Type(XYZ),Coord,Row in NES, Num Obs
			String format = "%25s\t%5s\t%35.15f\t%10d\t%10d%n";
			for (UnknownParameter unknownParameter : unknownParameters) {
				if (unknownParameter.getParameterType() != ParameterType.POINT1D && unknownParameter.getParameterType() != ParameterType.POINT2D && unknownParameter.getParameterType() != ParameterType.POINT3D)
					continue;
				
				if (networkAdjustment.isInterrupted())
    				break;
				
				Point point = (Point)unknownParameter;
				
				int colInJacobi = point.getColInJacobiMatrix();
    			int rowInJacobi = point.getRowInJacobiMatrix();
    			int dim = point.getDimension();
    			int numberOfObservations = point.numberOfObservations() + (rowInJacobi >= 0 ? dim : 0);
    			
    			if (colInJacobi < 0)
    				continue;
    			
    			if (dim != 1) {
    				pw.printf(Locale.ENGLISH, format, point.getName(), 'X', point.getX(), colInJacobi++, numberOfObservations);
    				pw.printf(Locale.ENGLISH, format, point.getName(), 'Y', point.getY(), colInJacobi++, numberOfObservations);
    			}
    			if (dim != 2) {
    				pw.printf(Locale.ENGLISH, format, point.getName(), 'Z', point.getZ(), colInJacobi++, numberOfObservations);
    			}
			}
		}
		finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	private void exportCovarianceMatrix(NetworkAdjustment networkAdjustment, File file) throws IOException {
		Matrix cofactor = networkAdjustment.getCofactorMatrix();
		int numberOfUnknownParameters = networkAdjustment.getNumberOfUnknownParameters();
		double varianceOfUnitWeight = networkAdjustment.getVarianceFactorAposteriori();
		
		if (cofactor == null || cofactor.numRows() < numberOfUnknownParameters || varianceOfUnitWeight < 0)
			return;

		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (int i=0; i < numberOfUnknownParameters; i++) {
				if (networkAdjustment.isInterrupted())
    				break;
				
				for (int j=0; j < numberOfUnknownParameters; j++) {
					if (networkAdjustment.isInterrupted())
	    				break;
					
					pw.printf(Locale.ENGLISH, "%+35.25f  ", varianceOfUnitWeight * cofactor.get(i,j));
				}
				pw.println();
			}
		} 
		finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
