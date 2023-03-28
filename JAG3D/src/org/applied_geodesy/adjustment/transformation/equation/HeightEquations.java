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

package org.applied_geodesy.adjustment.transformation.equation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.AdjustablePosition;
import org.applied_geodesy.adjustment.transformation.point.DispersionablePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.PositionPair;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public class HeightEquations extends TransformationEquations {
	private Map<ParameterType, UnknownParameter> parameters = null;

	public HeightEquations() {
		this.init();
	}
	
	public void setInitialGuess(double tz, double scale) throws IllegalArgumentException {
		this.parameters.get(ParameterType.SHIFT_Z).setValue0(tz);
		this.parameters.get(ParameterType.SCALE_Z).setValue0(scale);
	}

	@Override
	public void setCenterOfMasses(SimplePositionPair centerOfMasses) {
		
		// get previous center of mass
		SimplePositionPair prevCenterOfMasses = this.getCenterOfMasses();
		
		// check, if components are equal to previous point to avoid unnecessary operations
		boolean equalComponents = centerOfMasses.equalsCoordinateComponents(prevCenterOfMasses);
		super.setCenterOfMasses(centerOfMasses);
		
		if (equalComponents)
			return;
		
		UnknownParameter Tz = this.parameters.get(ParameterType.SHIFT_Z);
		UnknownParameter Mz = this.parameters.get(ParameterType.SCALE_Z);
		
		// Shift
		double tz = Tz.getValue();
		// Scale
		double mz = Mz.getValue();

		double dzi = -(prevCenterOfMasses.getSourceSystemPosition().getZ() - centerOfMasses.getSourceSystemPosition().getZ());
		double dZi =   prevCenterOfMasses.getTargetSystemPosition().getZ() - centerOfMasses.getTargetSystemPosition().getZ();

		// Set shift vector w.r.t. new center of masses 
		Tz.setValue( tz + dZi + mz*dzi );
	}

	@Override
	public void reverseCenterOfMasses(UpperSymmPackMatrix Dp) {
		SimplePositionPair centerOfMasses = this.getCenterOfMasses();

		double zi = -centerOfMasses.getSourceSystemPosition().getZ();
		double Zi =  centerOfMasses.getTargetSystemPosition().getZ();
				
		UnknownParameter Tz = this.parameters.get(ParameterType.SHIFT_Z);
		UnknownParameter Mz = this.parameters.get(ParameterType.SCALE_Z);
		
		// Shift
		double tz = Tz.getValue();
		// Scale
		double mz = Mz.getValue();
		
		// Set inverted center of maas reduction 
		Tz.setValue( tz + Zi + mz*zi );

		centerOfMasses.getSourceSystemPosition().setX(0);
		centerOfMasses.getSourceSystemPosition().setY(0);
		
		centerOfMasses.getTargetSystemPosition().setX(0);
		centerOfMasses.getTargetSystemPosition().setY(0);
		
		// fill jacobian A to transform shift vector		
		int nou = Dp.numColumns();
		Matrix Jx = Matrices.identity(nou);
		this.normalEquationElements(new HomologousFramePositionPair(centerOfMasses.getName(), zi,  Zi), Jx, null, null, null);

		Matrix DpJxT = new DenseMatrix(nou,nou);
		Dp.transBmult(Jx, DpJxT);
		Jx.mult(DpJxT, Dp);
	}
	
	@Override
	public void normalEquationElements(PositionPair<? extends DispersionablePosition, ? extends AdjustablePosition> positionPair, Matrix Jx, Matrix JvSrc, Matrix JvTrg, Vector w) {
		SimplePositionPair centerOfMasses = this.getCenterOfMasses();

		DispersionablePosition pointSourceCRS = positionPair.getSourceSystemPosition();
		AdjustablePosition pointTargetCRS = positionPair.getTargetSystemPosition();
		
		double zi = pointSourceCRS.getX() - centerOfMasses.getSourceSystemPosition().getZ();
		double Zi = pointTargetCRS.getX() - centerOfMasses.getTargetSystemPosition().getZ();
		
		UnknownParameter Tz = this.parameters.get(ParameterType.SHIFT_Z);
		UnknownParameter Mz = this.parameters.get(ParameterType.SCALE_Z);
		
		// Shift
		double tz = Tz.getValue();
		// Scale
		double mz = Mz.getValue();
		
		if (Jx != null) {
			int rowIndex = 0;
			
			if (Tz.getColumn() >= 0)
				Jx.set(rowIndex, Tz.getColumn(), 1.0);
			if (Mz.getColumn() >= 0)
				Jx.set(rowIndex, Tz.getColumn(),  zi);						
		}
		
		// source system observation
		if (JvSrc != null) {
			int rowIndex = 0;

			JvSrc.set(rowIndex, 0,  mz);
		}
		
		// target system observation
		if (JvTrg != null) {
			int rowIndex = 0;

			JvTrg.set(rowIndex, 0, -1.0);
		}
		
		if (w != null) {
			int rowIndex = 0;
			w.set(rowIndex, tz + mz*zi - Zi);
		}
	}
	
	private void init() {
		this.parameters = new LinkedHashMap<ParameterType, UnknownParameter>();
		this.parameters.put(ParameterType.SHIFT_Z, new UnknownParameter(ParameterType.SHIFT_Z, true, 0.0));
		this.parameters.put(ParameterType.SCALE_Z, new UnknownParameter(ParameterType.SCALE_Z, true, 1.0));
	}
	
	@Override
	public Collection<UnknownParameter> getUnknownParameters() {
		return this.parameters.values();
	}

	@Override
	public UnknownParameter getUnknownParameter(ParameterType parameterType) {
		return this.parameters.get(parameterType);
	}
	
	@Override
	public TransformationType getTransformationType() {
		return TransformationType.HEIGHT;
	}
}