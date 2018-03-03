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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point1D;

public class Transformation1D implements Transformation {
	public PointBundle 	source = new PointBundle(this.getDimension()),
			target = new PointBundle(this.getDimension()),
			originalTarget = new PointBundle(this.getDimension()),
			pointsToTransform = new PointBundle(this.getDimension());
	public TransformationParameterSet transParameter = null;
	private double omega = 0.0;
	private Map<TransformationParameterType, Boolean> fixedParameters = new HashMap<TransformationParameterType, Boolean>(Map.of(
			TransformationParameterType.TRANSLATION_X, Boolean.TRUE,
			TransformationParameterType.TRANSLATION_Y, Boolean.TRUE,
			TransformationParameterType.TRANSLATION_Z, Boolean.FALSE,
			TransformationParameterType.ROTATION_X, Boolean.TRUE,
			TransformationParameterType.ROTATION_Y, Boolean.TRUE,
			TransformationParameterType.ROTATION_Z, Boolean.TRUE,
			TransformationParameterType.SCALE, Boolean.TRUE
			));

	public Transformation1D(TransformationParameterSet transParameter) {
		this.transParameter = transParameter;
	}

	public Transformation1D(PointBundle source, PointBundle target) {
		if (source.getDimension() != target.getDimension() || source.getDimension() != this.numberOfRequiredPoints() )
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Dimensionsfehler, benoetige d = " + this.getDimension());
		this.init(source, target);
	}

	public boolean transformLMS() {
		TransformationParameterSet transParameter = null;
		double omega = Double.MAX_VALUE;
		for (int i=0; i<this.numberOfIdenticalPoints(); i++) {
			Point pS1 = this.source.get(i);
			Point pT1 = this.target.get(pS1.getName());
			if (this.fixedParameters.get(TransformationParameterType.SCALE)) {
				PointBundle subSource = new PointBundle(this.getDimension());
				PointBundle subTarget = new PointBundle(this.getDimension());

				subSource.addPoint(pS1);
				subTarget.addPoint(pT1);

				TransformationParameterSet subParameter = this.adjustTransformationsParameter(subSource, subTarget);

				double o = this.getOmega(subParameter, this.source, this.target, true);

				if (o<omega) {
					transParameter = subParameter;
					omega = o;
				}
			}
			else {
				for (int j=i+1; j<this.numberOfIdenticalPoints(); j++) {
					Point pS2 = this.source.get(j);
					Point pT2 = this.target.get(pS2.getName());

					PointBundle subSource = new PointBundle(this.getDimension());
					PointBundle subTarget = new PointBundle(this.getDimension());

					subSource.addPoint(pS1);
					subSource.addPoint(pS2);

					subTarget.addPoint(pT1);
					subTarget.addPoint(pT2);

					TransformationParameterSet subParameter = this.adjustTransformationsParameter(subSource, subTarget);

					double o = this.getOmega(subParameter, this.source, this.target, true);

					if (o<omega) {
						transParameter = subParameter;
						omega = o;
					}
				}
			}

		}
		this.omega = omega;
		this.transParameter = transParameter;
		return this.transParameter != null;
	}

	private double getOmega(TransformationParameterSet transParameter, PointBundle source, PointBundle target, boolean isLMS) {
		if (transParameter == null)
			return Double.MAX_VALUE;

		double m  = transParameter.getParameterValue(TransformationParameterType.SCALE);
		double tZ = transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Z);

		double omegaLMS[] = new double[source.size()];
		double omegaLS = 0.0;

		for (int i=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point pT = target.get(pS.getName());

			double z = m*pS.getZ() + tZ;
			double vz = pT.getZ()-z;

			omegaLMS[i] = vz*vz;
			omegaLS    += vz*vz;
		}
		Arrays.sort(omegaLMS);

		if (isLMS) {
			Arrays.sort(omegaLMS);
			if (omegaLMS.length == this.numberOfRequiredPoints())
				return omegaLMS[omegaLMS.length-1];
			if (omegaLMS.length < 2*this.numberOfRequiredPoints())
				return omegaLMS[this.numberOfRequiredPoints()];
			if (omegaLMS.length%2==1)
				return omegaLMS[(int)(omegaLMS.length/2)];
			return 0.5*(omegaLMS[(int)(omegaLMS.length/2)-1]+omegaLMS[(int)(omegaLMS.length/2)]);
		}
		return omegaLS;
	}

	public boolean transformL2Norm() {
		if (this.numberOfIdenticalPoints() < this.numberOfRequiredPoints())
			return false;

		this.transParameter = null;
		//if (this.transParameter != null)
		//	return true;

		this.transParameter = this.adjustTransformationsParameter(this.source, this.target);
		this.omega = this.getOmega(this.transParameter, this.source, this.target, false);

		return this.transParameter != null;
	}

	public TransformationParameterSet adjustTransformationsParameter(PointBundle source, PointBundle target) {

		Point centerPointS = source.getCenterPoint();
		Point centerPointT = target.getCenterPoint();

		double m = 0;
		int k = 0;
		for (int i=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point pT = target.get(i);
			if ((pS.getZ()-centerPointS.getZ()) != 0) {
				m += Math.abs( (pT.getZ()-centerPointT.getZ())/(pS.getZ()-centerPointS.getZ()) );
				k++;
			}
		}
		if (!this.fixedParameters.get(TransformationParameterType.SCALE) && m > 0 && k > 0)
			m /= k;
		else
			m = 1.0;

		double tZ = 0.0;
		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_Z))
			tZ = centerPointT.getZ() - m*centerPointS.getZ();

		TransformationParameterSet transParameter = new TransformationParameterSet();
		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_Z))
			transParameter.setParameterValue(TransformationParameterType.TRANSLATION_Z, tZ);
		if (!this.fixedParameters.get(TransformationParameterType.SCALE))
			transParameter.setParameterValue(TransformationParameterType.SCALE, m);
		return transParameter;
	}

	private void init(PointBundle source, PointBundle target) {
		this.originalTarget = target;
		for (int i=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point tempPS = new Point1D(pS.getName(), pS.getZ());

			this.pointsToTransform.addPoint( tempPS );

			for (int j=0; j<target.size(); j++){
				Point pT = target.get(j);

				// Suche identische Punkte
				if (pS.getName().equals(pT.getName())) {
					Point tempPT = new Point1D(pT.getName(), pT.getZ());
					this.source.addPoint( tempPS );
					this.target.addPoint( tempPT );
					break;
				}
			}
		}
	}

	@Override
	public Point1D transformPoint2SourceSystem(Point point) {
		if (this.transParameter == null)
			return null;

		double m  = this.transParameter.getParameterValue(TransformationParameterType.SCALE);
		double tZ = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Z);

		double z = (point.getZ() - tZ)/m;

		return new Point1D(point.getName(), z);
	}

	@Override
	public Point1D transformPoint2TargetSystem(Point point) {
		if (this.transParameter == null || point == null)
			return null;

		double m  = this.transParameter.getParameterValue(TransformationParameterType.SCALE);
		double tZ = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Z);

		double z = m*point.getZ() + tZ;

		return new Point1D(point.getName(), z);
	}

	@Override
	public final int getDimension() {
		return 1;
	}

	@Override
	public int numberOfIdenticalPoints() {
		return this.source.size();
	}

	@Override
	public int numberOfRequiredPoints() {
		return 1;
	}

	@Override
	public PointBundle getTransformdPoints() {
		if (this.transParameter==null)
			return null;

		PointBundle transformedPoints = new PointBundle(this.getDimension());

		for (int i=0; i<this.originalTarget.size(); i++){
			Point pT = this.originalTarget.get(i);
			transformedPoints.addPoint(pT);
		}

		for (int i=0; i<this.pointsToTransform.size(); i++) {
			Point pS = this.pointsToTransform.get(i);
			Point pT = transformedPoints.get(pS.getName());
			if (pT == null) {
				pT = this.transformPoint2TargetSystem(pS);
				transformedPoints.addPoint(pT);
			}
		}
		transformedPoints.setTransformationParameterSet(this.transParameter);
		return transformedPoints;
	}

	@Override
	public TransformationParameterSet getTransformationParameterSet() {
		return this.transParameter;
	}

	@Override
	public void setFixedParameter(TransformationParameterType type, boolean fixed) {
		this.fixedParameters.put(type, fixed);
	}

	@Override
	public double getOmega() {
		return this.omega;
	}
}
