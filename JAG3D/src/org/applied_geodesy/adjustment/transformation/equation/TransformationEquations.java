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
import java.util.Collections;
import java.util.Iterator;

import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.AdjustablePosition;
import org.applied_geodesy.adjustment.transformation.point.DispersionablePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.PositionPair;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

public abstract class TransformationEquations implements Iterable<HomologousFramePositionPair> {
	private ObservableUniqueList<HomologousFramePositionPair> homologousPositionPairs = new ObservableUniqueList<HomologousFramePositionPair>();
	private SimplePositionPair centerOfMasses = null;
	private ObjectProperty<String> name = new SimpleObjectProperty<String>(this, "name");
	private static int ID_CNT = 0;
	private ReadOnlyObjectProperty<Integer> id;
	
	public TransformationEquations() {
		this.id = new ReadOnlyObjectWrapper<Integer>(this, "id", ID_CNT++);
	}
	
	public boolean contains(Object object) {
		return this.getUnknownParameters().contains(object);
	}
	
	public abstract void reverseCenterOfMasses(UpperSymmPackMatrix Dp);
	
	public abstract void normalEquationElements(PositionPair<? extends DispersionablePosition, ? extends AdjustablePosition> positionPair, Matrix Jx, Matrix JvSrc, Matrix JvTrg, Vector w);
	
	public abstract TransformationType getTransformationType();
	
	public abstract Matrix getHomogeneousCoordinateTransformationMatrix();
	
	public abstract Matrix getRotationMatrix();
	
	public abstract Collection<UnknownParameter> getUnknownParameters();
	
	public abstract UnknownParameter getUnknownParameter(ParameterType parameterType);
	
	public final int getId() {
		return this.id.get();
	}
	
	public ReadOnlyObjectProperty<Integer> idProperty() {
		return this.id;
	}
	
	public ObservableUniqueList<HomologousFramePositionPair> getHomologousFramePositionPairs() {
		return this.homologousPositionPairs;
	}
		
	public void setCenterOfMasses(SimplePositionPair centerOfMasses) {
		this.centerOfMasses = centerOfMasses;
	}
		
	public SimplePositionPair getCenterOfMasses() {
		if (this.centerOfMasses == null) {
			switch(this.getTransformationType()) {
			case HEIGHT:
				this.centerOfMasses = new SimplePositionPair("CENTER_OF_MASSES",      0,      0);
				break;
			case PLANAR:
				this.centerOfMasses = new SimplePositionPair("CENTER_OF_MASSES",    0,0,    0,0);
				break;
			case SPATIAL:
				this.centerOfMasses = new SimplePositionPair("CENTER_OF_MASSES",  0,0,0,  0,0,0);
				break;			
			}				
		}
		return this.centerOfMasses;
	}
	
	public Collection<Restriction> getRestrictions() {
		return Collections.<Restriction>emptySet();
	}
	
	public int getNumberOfPoints() {
		return this.homologousPositionPairs.size();
	}
		
	@Override
	public Iterator<HomologousFramePositionPair> iterator() {
		return this.homologousPositionPairs.iterator();
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(String name) {
		this.nameProperty().set(name);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	public void transform(PositionPair<? extends DispersionablePosition, ? extends AdjustablePosition> positionPair, UpperSymmPackMatrix Dp) {
		DispersionablePosition pointSrc = positionPair.getSourceSystemPosition();
		AdjustablePosition pointTrg     = positionPair.getTargetSystemPosition();
				
		int dim = this.getTransformationType().getDimension();
		int nop = Dp.numColumns();
		
		Matrix Jx = new DenseMatrix(dim, nop);
		Matrix Jv = new DenseMatrix(dim, dim);
		Vector w  = new DenseVector(dim);
		this.normalEquationElements(positionPair, Jx, Jv, null, w);

		Matrix DpJxT = new DenseMatrix(nop, dim);
		Dp.transBmult(Jx, DpJxT);
		
		Matrix JDJT = new UpperSymmPackMatrix(dim);
		Jx.mult(DpJxT, JDJT);
		DpJxT = null;
		
		Matrix Dsrc = pointSrc.getDispersionApriori();
		Matrix DsrcJvT = new DenseMatrix(dim, dim);
		Dsrc.transBmult(Jv, DsrcJvT);
		Jv.multAdd(DsrcJvT, JDJT);

		if (dim != 1) {
			pointTrg.setX0(w.get(0));
			pointTrg.setY0(w.get(1));
			
			pointTrg.setCofactorX(JDJT.get(0, 0));
			pointTrg.setCofactorY(JDJT.get(1, 1));
		}
		if (dim != 2) {
			pointTrg.setZ0(w.get(dim - 1));
			pointTrg.setCofactorZ(JDJT.get(dim - 1, dim - 1));
		}
	}
}