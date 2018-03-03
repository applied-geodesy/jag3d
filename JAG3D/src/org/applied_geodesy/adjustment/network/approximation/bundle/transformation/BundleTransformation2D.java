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

package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

import java.util.List;

import org.applied_geodesy.adjustment.NormalEquationSystem;
import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

public class BundleTransformation2D extends BundleTransformation {
	public BundleTransformation2D(double threshold, List<PointBundle> sourceSystems) {
		super(threshold, sourceSystems);
	}
	  
	public BundleTransformation2D(double threshold, PointBundle sourceSystem, PointBundle targetSystem) {
		super(threshold, sourceSystem, targetSystem);
	}

	public BundleTransformation2D(double threshold, List<PointBundle> sourceSystems, PointBundle targetSystem) {
		super(threshold, sourceSystems, targetSystem);
	}
	  
	public Transformation2D getSimpleTransformationModel(PointBundle b1, PointBundle b2) {
		return new Transformation2D(b1, b2);
	}
	  
	public Transformation2D getSimpleTransformationModel(TransformationParameterSet transParameter) {
		return new Transformation2D(transParameter);
	}

	protected NormalEquationSystem createNormalEquationSystem() {
		PointBundle targetSystem = this.getTargetSystem();
		List<PointBundle> sourceSystems = this.getSourceSystems();
    
		Matrix A = new LinkedSparseMatrix(this.numberOfObservations(), this.numberOfUnknowns());
		Vector w = new DenseVector(A.numRows());
	  
		double s0 = this.getScaleEstimate();
		
		for (PointBundle sourceSystem : sourceSystems) {
			// Transformationsparameter des lokalen Systems
			TransformationParameterSet parameters = sourceSystem.getTransformationParameterSet();
			TransformationParameter m  = parameters.get(TransformationParameterType.SCALE);
			TransformationParameter rZ = parameters.get(TransformationParameterType.ROTATION_Z);
			TransformationParameter tX = parameters.get(TransformationParameterType.TRANSLATION_X);
			TransformationParameter tY = parameters.get(TransformationParameterType.TRANSLATION_Y);
			
			if (this.interrupt)
	    		return null;

			for (int i=0; i<sourceSystem.size(); i++) {
				Point pointSource = sourceSystem.get(i);
				String pointId = pointSource.getName();
				int row = pointSource.getRowInJacobiMatrix();
				Point pointTarget = targetSystem.get(pointId);
				if (row < 0 || pointTarget == null)
					continue;
				
				double vx = (pointSource.getX() - (Math.cos(rZ.getValue())*m.getValue()*pointTarget.getX() - Math.sin(rZ.getValue())*m.getValue()*pointTarget.getY() + tX.getValue()));
				double vy = (pointSource.getY() - (Math.sin(rZ.getValue())*m.getValue()*pointTarget.getX() + Math.cos(rZ.getValue())*m.getValue()*pointTarget.getY() + tY.getValue()));

				double px = pointSource.getWeightedX() > 0 && Math.abs(vx) <s0 ? 1.0 : 0.0;
				double py = pointSource.getWeightedY() > 0 && Math.abs(vy) <s0 ? 1.0 : 0.0;
				
				if (px == 0.0 || py == 0.0) {
					pointSource.isOutlier(true);
					pointSource.setWeightedX(px);
					pointSource.setWeightedY(py);
					this.addOutlierPoint(pointTarget);
				}
				else if(pointSource.isOutlier()) {
					pointSource.isOutlier(false);
					this.removeOutlierPoint(pointTarget);
				}
								
				if (!m.isFixed()) {
					int col = m.getColInJacobiMatrix();
					A.set(row,   col, px*(Math.cos(rZ.getValue())*pointTarget.getX()-Math.sin(rZ.getValue())*pointTarget.getY()) );
					A.set(row+1, col, py*(Math.sin(rZ.getValue())*pointTarget.getX()+Math.cos(rZ.getValue())*pointTarget.getY()) );
				}

				if (!rZ.isFixed()) {
					int col = rZ.getColInJacobiMatrix();
					A.set(row,   col, px*(-Math.sin(rZ.getValue())*m.getValue()*pointTarget.getX()-Math.cos(rZ.getValue())*m.getValue()*pointTarget.getY()) );
					A.set(row+1, col, py*( Math.cos(rZ.getValue())*m.getValue()*pointTarget.getX()-Math.sin(rZ.getValue())*m.getValue()*pointTarget.getY()) );
				}

				if (!tX.isFixed()) {
					int col = tX.getColInJacobiMatrix();
					A.set(row, col, px*1.0);
				}

				if (!tY.isFixed()) {
					int col = tY.getColInJacobiMatrix();
					A.set(row+1, col, py*1.0);
				}

				if (pointSource.getColInJacobiMatrix()>=0) {
					//int col = pointSource.getColInJacobiMatrix();					
					int col = pointTarget.getColInJacobiMatrix();
					A.set(row, col+0, px*( Math.cos(rZ.getValue())*m.getValue()) );
					A.set(row, col+1, py*(-Math.sin(rZ.getValue())*m.getValue()) );

					A.set(row+1, col+0, px*(Math.sin(rZ.getValue())*m.getValue()) );
					A.set(row+1, col+1, py*(Math.cos(rZ.getValue())*m.getValue()) );
				}

				// Widerspruchsvektor
				w.set(row,   px*(pointSource.getX() - (Math.cos(rZ.getValue())*m.getValue()*pointTarget.getX() - Math.sin(rZ.getValue())*m.getValue()*pointTarget.getY() + tX.getValue())) );
				w.set(row+1, py*(pointSource.getY() - (Math.sin(rZ.getValue())*m.getValue()*pointTarget.getX() + Math.cos(rZ.getValue())*m.getValue()*pointTarget.getY() + tY.getValue())) );
				
			}
		}
		
		UpperSymmPackMatrix ATA = new UpperSymmPackMatrix(this.numberOfUnknowns());
		DenseVector ATw = new DenseVector(this.numberOfUnknowns());
		
		if (this.interrupt)
    		return null;

	    if (this.numberOfUnknowns() > 0) {
	    	A.transAmult(A, ATA);
	    	A.transMult(w, ATw);
	    }

	    return new NormalEquationSystem(ATA, ATw);
	}
}
