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

package org.applied_geodesy.juniform.test.tracim;

import java.util.List;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

class TraCIMTest {

	public Feature adjust(Feature feature, List<FeaturePoint> points) {
		FeatureAdjustment adjustment = new FeatureAdjustment();

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
			e.printStackTrace();
		}

		return null;
	}
	
	double getLambda() {
		return 0.0;
	}
}
