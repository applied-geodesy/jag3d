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

public class BundleTransformation1D extends BundleTransformation {

	public BundleTransformation1D(double threshold, List<PointBundle> sourceSystems) {
		super(threshold, sourceSystems);
	}

	public BundleTransformation1D(double threshold, PointBundle sourceSystem, PointBundle targetSystem) {
		super(threshold, sourceSystem, targetSystem);
	}

	public BundleTransformation1D(double threshold, List<PointBundle> sourceSystems, PointBundle targetSystem) {
		super(threshold, sourceSystems, targetSystem);
	}
	
	@Override
	public Transformation1D getSimpleTransformationModel(PointBundle b1, PointBundle b2) {
		return new Transformation1D(b1, b2);
	}

	@Override
	public Transformation1D getSimpleTransformationModel(TransformationParameterSet transParameter) {
		return new Transformation1D(transParameter);
	}

	@Override
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
	    	TransformationParameter tZ = parameters.get(TransformationParameterType.TRANSLATION_Z);

	    	if (this.interrupt)
	    		return null;
	    	
	    	for (int i=0; i<sourceSystem.size(); i++) {
	    		Point pointSource = sourceSystem.get(i);
	    		String pointId = pointSource.getName();
	    		int row = pointSource.getRowInJacobiMatrix();
	    		Point pointTarget = targetSystem.get(pointId);
	    		if (row < 0 || pointTarget == null)
	    			continue;
	    		
	    		double vz = (pointSource.getZ() - (m.getValue()*pointTarget.getZ() + tZ.getValue()));
				double pz = pointSource.getWeightedZ() > 0 && Math.abs(vz) < s0 ? 1.0 : 0.0;
				
				if (pz == 0.0) {
					pointSource.isOutlier(true);
					pointSource.setWeightedZ(pz);
					this.addOutlierPoint(pointSource);
				}
				else if(pointSource.isOutlier()) {
					pointSource.isOutlier(false);
					this.removeOutlierPoint(pointSource);
				}

	    		if (!m.isFixed()) {
	    			int col = m.getColInJacobiMatrix();
	    			A.set(row, col, pz*pointTarget.getZ() );
	    		}

	    		if (!tZ.isFixed()) {
	    			int col = tZ.getColInJacobiMatrix();
	    			A.set(row, col, pz*1.0);
	    		}

	    		if (pointSource.getColInJacobiMatrix()>=0) {
	    			int col = pointTarget.getColInJacobiMatrix();
	    			A.set(row, col, pz*m.getValue() );
	    		}

	    		// Widerspruchsvektor
	    		w.set(row, pz*(pointSource.getZ() - (m.getValue()*pointTarget.getZ() + tZ.getValue())) );
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
