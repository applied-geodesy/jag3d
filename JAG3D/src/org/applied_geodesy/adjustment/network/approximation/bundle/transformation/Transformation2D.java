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

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;

public class Transformation2D implements Transformation {
	private PointBundle source = new PointBundle(this.getDimension()),
			target = new PointBundle(this.getDimension()),
			originalTarget = new PointBundle(this.getDimension()),
			pointsToTransform = new PointBundle(this.getDimension());
	private TransformationParameterSet transParameter = null;
	private double omega = 0.0;

	private Map<TransformationParameterType, Boolean> fixedParameters = new HashMap<TransformationParameterType, Boolean>(Map.of(
			TransformationParameterType.TRANSLATION_X, Boolean.FALSE,
			TransformationParameterType.TRANSLATION_Y, Boolean.FALSE,
			TransformationParameterType.TRANSLATION_Z, Boolean.TRUE,
			TransformationParameterType.ROTATION_X, Boolean.TRUE,
			TransformationParameterType.ROTATION_Y, Boolean.TRUE,
			TransformationParameterType.ROTATION_Z, Boolean.FALSE,
			TransformationParameterType.SCALE, Boolean.FALSE
			));

	public Transformation2D(TransformationParameterSet transParameter) {
		this.transParameter = transParameter;
	}

	public Transformation2D(PointBundle source, PointBundle target) {
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
		this.omega = omega;
		this.transParameter = transParameter;

		return this.transParameter != null;
	}

	public boolean transformL2Norm() {
		if (this.numberOfIdenticalPoints() < this.numberOfRequiredPoints())
			return false;

		this.transParameter = null;
		this.transParameter = this.adjustTransformationsParameter(this.source, this.target);
		this.omega = this.getOmega(this.transParameter, this.source, this.target, false);

		return this.transParameter != null;
	}

	private double getOmega(TransformationParameterSet transParameter, PointBundle source, PointBundle target, boolean isLMS) {
		if (transParameter == null)
			return Double.MAX_VALUE;

		double m  = transParameter.getParameterValue(TransformationParameterType.SCALE);
		double rZ = transParameter.getParameterValue(TransformationParameterType.ROTATION_Z);
		double tX = transParameter.getParameterValue(TransformationParameterType.TRANSLATION_X);
		double tY = transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Y);

		double omegaLMS[] = new double[this.getDimension()*source.size()];
		double omegaLS = 0.0;
		for (int i=0, j=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point pT = target.get(pS.getName());
			double x = m*(Math.cos(rZ)*pS.getX() - Math.sin(rZ)*pS.getY()) + tX;
			double y = m*(Math.sin(rZ)*pS.getX() + Math.cos(rZ)*pS.getY()) + tY;

			double vx = pT.getX()-x;
			double vy = pT.getY()-y;

			omegaLMS[j++] = vx*vx;
			omegaLMS[j++] = vy*vy;

			//			omegaLMS[i] = vx*vx + vy*vy;
			omegaLS    += vx*vx + vy*vy;
		}

		if (isLMS) {
			Arrays.sort(omegaLMS);
			if (omegaLMS.length == this.getDimension()*this.numberOfRequiredPoints())
				return omegaLMS[omegaLMS.length-1];
			if (omegaLMS.length < 2*this.getDimension()*this.numberOfRequiredPoints())
				return omegaLMS[this.getDimension()*this.numberOfRequiredPoints()];
			if (omegaLMS.length%2==1)
				return omegaLMS[(int)(omegaLMS.length/2)];
			return 0.5*(omegaLMS[(int)(omegaLMS.length/2)-1]+omegaLMS[(int)(omegaLMS.length/2)]);
		}
		return omegaLS;
	}

	private TransformationParameterSet adjustTransformationsParameter(PointBundle source, PointBundle target) {
		Point centerPointS = source.getCenterPoint();
		Point centerPointT = target.getCenterPoint();

		double o = 0.0, a = 0.0, oa = 0.0;

		for (int i=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point pT = target.get(i);

			o  += (pS.getX()-centerPointS.getX()) * (pT.getY()-centerPointT.getY()) - (pS.getY()-centerPointS.getY()) * (pT.getX()-centerPointT.getX());
			a  += (pS.getX()-centerPointS.getX()) * (pT.getX()-centerPointT.getX()) + (pS.getY()-centerPointS.getY()) * (pT.getY()-centerPointT.getY());

			oa += Math.pow(pS.getX()-centerPointS.getX(),2) + Math.pow(pS.getY()-centerPointS.getY(),2);
		}

		if (oa == 0)
			return null;

		o /= oa;
		a /= oa;

		//		double tX = centerPointT.getX()-a*centerPointS.getX() + o*centerPointS.getY();
		//		double tY = centerPointT.getY()-a*centerPointS.getY() - o*centerPointS.getX();
		double tX = 0.0;
		double tY = 0.0;
		double rZ = 0.0;
		double m  = 1.0;

		TransformationParameterSet transParameter = new TransformationParameterSet();

		if (!this.fixedParameters.get(TransformationParameterType.ROTATION_Z))
			rZ = MathExtension.MOD(Math.atan2(o, a), 2.0*Math.PI);
		if (!this.fixedParameters.get(TransformationParameterType.SCALE))
			m = Math.hypot(a, o);
		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_X))
			tX = centerPointT.getX() - (m*(Math.cos(rZ)*centerPointS.getX() - Math.sin(rZ)*centerPointS.getY()));
		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_Y))
			tY = centerPointT.getY() - (m*(Math.sin(rZ)*centerPointS.getX() + Math.cos(rZ)*centerPointS.getY()));


		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_X))
			transParameter.setParameterValue(TransformationParameterType.TRANSLATION_X, tX);
		if (!this.fixedParameters.get(TransformationParameterType.TRANSLATION_Y))
			transParameter.setParameterValue(TransformationParameterType.TRANSLATION_Y, tY);
		if (!this.fixedParameters.get(TransformationParameterType.ROTATION_Z))
			transParameter.setParameterValue(TransformationParameterType.ROTATION_Z, rZ);
		if (!this.fixedParameters.get(TransformationParameterType.SCALE))
			transParameter.setParameterValue(TransformationParameterType.SCALE, m);

		return transParameter;
	}

	private void init(PointBundle source, PointBundle target) {
		this.originalTarget = target;
		for (int i=0; i<source.size(); i++) {
			Point pS = source.get(i);
			Point tempPS = new Point2D(pS.getName(), pS.getX(), pS.getY());

			this.pointsToTransform.addPoint( tempPS );

			for (int j=0; j<target.size(); j++){
				Point pT = target.get(j);

				// Suche identische Punkte
				if (pS.getName().equals(pT.getName())) {
					Point tempPT = new Point2D(pT.getName(), pT.getX(), pT.getY());
					this.source.addPoint( tempPS );
					this.target.addPoint( tempPT );
					break;
				}
			}
		}
	}

	@Override
	public Point2D transformPoint2SourceSystem(Point point) {
		if (this.transParameter == null)
			return null;

		double m  = this.transParameter.getParameterValue(TransformationParameterType.SCALE);
		double rZ = this.transParameter.getParameterValue(TransformationParameterType.ROTATION_Z);
		double tX = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_X);
		double tY = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Y);

		double x = ( Math.cos(rZ)*(point.getX()-tX) + Math.sin(rZ)*(point.getY()-tY))/m;
		double y = (-Math.sin(rZ)*(point.getX()-tX) + Math.cos(rZ)*(point.getY()-tY))/m;

		return new Point2D(point.getName(), x, y);
	}

	@Override
	public Point2D transformPoint2TargetSystem(Point point) {
		if (this.transParameter == null || point == null)
			return null;

		double m  = this.transParameter.getParameterValue(TransformationParameterType.SCALE);
		double rZ = this.transParameter.getParameterValue(TransformationParameterType.ROTATION_Z);
		double tX = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_X);
		double tY = this.transParameter.getParameterValue(TransformationParameterType.TRANSLATION_Y);

		double x = m*(Math.cos(rZ)*point.getX() - Math.sin(rZ)*point.getY()) + tX;
		double y = m*(Math.sin(rZ)*point.getX() + Math.cos(rZ)*point.getY()) + tY;

		return new Point2D(point.getName(), x, y);
	}

	@Override
	public final int getDimension() {
		return 2;
	}

	@Override
	public int numberOfIdenticalPoints() {
		return this.source.size();
	}

	@Override
	public int numberOfRequiredPoints() {
		return 2;
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
