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

package org.applied_geodesy.adjustment.geometry.curve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.CurveFeature;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Line;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class LineFeature extends CurveFeature {
	private final Line line;
	
	public LineFeature() {
		super(true);
		
		this.line = new Line();
		
		UnknownParameter ux = this.line.getUnknownParameter(ParameterType.VECTOR_X);
		UnknownParameter uy = this.line.getUnknownParameter(ParameterType.VECTOR_Y);

		UnknownParameter du = this.line.getUnknownParameter(ParameterType.LENGTH);
		
		ux.setVisible(false);
		uy.setVisible(false);
		du.setVisible(false);
		
		UnknownParameter xOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_X, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yOrigin = new UnknownParameter(ParameterType.ORIGIN_COORDINATE_Y, false, 0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter xNormal = new UnknownParameter(ParameterType.VECTOR_X, false, 0, true, ProcessingType.POSTPROCESSING);
		UnknownParameter yNormal = new UnknownParameter(ParameterType.VECTOR_Y, false, 0, true, ProcessingType.POSTPROCESSING);
		
		UnknownParameter positiveOne = new UnknownParameter(ParameterType.CONSTANT, true,  1.0, false, ProcessingType.FIXED);
		UnknownParameter negativeOne = new UnknownParameter(ParameterType.CONSTANT, true, -1.0, false, ProcessingType.FIXED);
		
		ProductSumRestriction xOriginRestriction = new ProductSumRestriction(false, List.of(ux), List.of(du), xOrigin);
		ProductSumRestriction yOriginRestriction = new ProductSumRestriction(false, List.of(uy), List.of(du), yOrigin);
		
		ProductSumRestriction xNormalRestriction = new ProductSumRestriction(false, List.of(uy), List.of(positiveOne), xNormal);
		ProductSumRestriction yNormalRestriction = new ProductSumRestriction(false, List.of(ux), List.of(negativeOne), yNormal);
		
		this.add(this.line);
		
		List<UnknownParameter> newOrderedUnknownParameters = new ArrayList<UnknownParameter>();
		newOrderedUnknownParameters.add(xOrigin);
		newOrderedUnknownParameters.add(yOrigin);
		
		newOrderedUnknownParameters.add(xNormal);
		newOrderedUnknownParameters.add(yNormal);
		
		newOrderedUnknownParameters.addAll(this.getUnknownParameters());
		
		newOrderedUnknownParameters.add(positiveOne);
		newOrderedUnknownParameters.add(negativeOne);
		
		this.getUnknownParameters().setAll(newOrderedUnknownParameters);
		
		
		this.getPostProcessingCalculations().addAll(
				xOriginRestriction,
				yOriginRestriction,
				xNormalRestriction,
				yNormalRestriction
		);
	}
	
	public Line getLine() {
		return this.line;
	}

	public static void deriveInitialGuess(Collection<FeaturePoint> points, LineFeature feature) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(points, feature.line);
	}
	
	public static void deriveInitialGuess(Collection<FeaturePoint> points, Line line) throws IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		int nop = 0;
		double x0 = 0, y0 = 0;
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			nop++;
			x0 += point.getX0();
			y0 += point.getY0();

			if (line.getDimension() > point.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + line.getDimension() + " != " + point.getDimension());
		}
		
		if (nop < 2)
			throw new IllegalArgumentException("Error, the number of points is not sufficient; at least 2 points are needed.");
		
		x0 /= nop;
		y0 /= nop;
		
		UpperSymmPackMatrix H = new UpperSymmPackMatrix(2);
		
		for (FeaturePoint point : points) {
			if (!point.isEnable())
				continue;
			
			double xi = point.getX0() - x0;
			double yi = point.getY0() - y0;
			
			for (int i = 0; i < 2; i++) {
				double hi = 0;
				
				if (i == 0)
					hi = xi; 
				else 
					hi = yi;
				
				for (int j = i; j < 2; j++) {
					double hj = 0;

					if (j == 0)
						hj = xi; 
					else
						hj = yi;
					
					H.set(i, j, H.get(i,j) + hi * hj);
				}
			}
		}

		SymmPackEVD evd = new SymmPackEVD(2, true, true);
		evd.factor(H);
		
		Matrix eigVec = evd.getEigenvectors();
		double eigVal[] = evd.getEigenvalues();

		int indexMinEigVal = 0;
		double minEigVal = eigVal[indexMinEigVal];
		for (int i = indexMinEigVal + 1; i < eigVal.length; i++) {
			if (minEigVal > eigVal[i]) {
				minEigVal = eigVal[i];
				indexMinEigVal = i;
			}
		}

		// Normal vector n of the line is eigenvector which corresponds to the smallest eigenvalue 
		double nx = eigVec.get(0, indexMinEigVal);
		double ny = eigVec.get(1, indexMinEigVal);

		double d  = nx * x0 + ny * y0;

		// Change orientation so that d is a positive distance
		if (d < 0) {
			nx = -nx;
			ny = -ny;
			d  = -d;
		}
		
		line.setInitialGuess(nx, ny, d, 1.0);
	}
	
	@Override
	public void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException {
		deriveInitialGuess(this.line.getFeaturePoints(), this.line);
	}
}
