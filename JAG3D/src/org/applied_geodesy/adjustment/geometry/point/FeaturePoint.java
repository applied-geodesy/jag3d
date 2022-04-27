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

package org.applied_geodesy.adjustment.geometry.point;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.TestStatistic;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.UnitUpperTriangBandMatrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class FeaturePoint extends Point implements Iterable<GeometricPrimitive> {
	private ReadOnlyObjectProperty<TestStatistic> testStatistic = new ReadOnlyObjectWrapper<TestStatistic>(this, "testStatistic", new TestStatistic());
	
	private ObjectProperty<Boolean> enable = new SimpleObjectProperty<Boolean>(this, "enable", Boolean.TRUE);
	
	private ObjectProperty<Double> residualX = new SimpleObjectProperty<Double>(this, "residualX", 0.0);
	private ObjectProperty<Double> residualY = new SimpleObjectProperty<Double>(this, "residualY", 0.0);
	private ObjectProperty<Double> residualZ = new SimpleObjectProperty<Double>(this, "residualZ", 0.0);
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>(this, "redundancyX", 0.0);
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>(this, "redundancyY", 0.0);
	private ObjectProperty<Double> redundancyZ = new SimpleObjectProperty<Double>(this, "redundancyZ", 0.0);
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>(this, "grossErrorX", 0.0);
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>(this, "grossErrorY", 0.0);
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>(this, "grossErrorZ", 0.0);
	
	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasX", 0.0);
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasY", 0.0);
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasZ", 0.0);
	
	private ObjectProperty<Double> maximumTolerableBiasX = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasX", 0.0);
	private ObjectProperty<Double> maximumTolerableBiasY = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasY", 0.0);
	private ObjectProperty<Double> maximumTolerableBiasZ = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasZ", 0.0);
	
	private ObjectProperty<Double> cofactorX = new SimpleObjectProperty<Double>(this, "cofactorX", 0.0);
	private ObjectProperty<Double> cofactorY = new SimpleObjectProperty<Double>(this, "cofactorY", 0.0);
	private ObjectProperty<Double> cofactorZ = new SimpleObjectProperty<Double>(this, "cofactorZ", 0.0);
	
	private ObjectBinding<Double> x;
	private ObjectBinding<Double> y;
	private ObjectBinding<Double> z;
	
	private ObjectBinding<Double> uncertaintyX;
	private ObjectBinding<Double> uncertaintyY;
	private ObjectBinding<Double> uncertaintyZ;
	
	private ReadOnlyObjectWrapper<Double> testStatisticApriori     = new ReadOnlyObjectWrapper<Double>(this, "testStatisticApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> testStatisticAposteriori = new ReadOnlyObjectWrapper<Double>(this, "testStatisticAposteriori", 0.0);
	
	private ReadOnlyObjectWrapper<Double> pValueApriori     = new ReadOnlyObjectWrapper<Double>(this, "pValueApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> pValueAposteriori = new ReadOnlyObjectWrapper<Double>(this, "pValueAposteriori", 0.0);
	
	private ObjectBinding<Boolean> significant;

	private ObjectProperty<Matrix> dispersionApriori = new SimpleObjectProperty<Matrix>(this, "dispersionApriori");
	
	private ObjectProperty<Double> fisherQuantileApriori = new SimpleObjectProperty<Double>(this, "fisherQuantileApriori", Double.MAX_VALUE);
	private ObjectProperty<Double> fisherQuantileAposteriori = new SimpleObjectProperty<Double>(this, "fisherQuantileAposteriori", Double.MAX_VALUE);

	private Set<GeometricPrimitive> geometries = new LinkedHashSet<GeometricPrimitive>();
	
	public FeaturePoint(String name, double x0, double y0) throws IllegalArgumentException {
		this(name, x0, y0, MathExtension.identity(2));
	}
	
	public FeaturePoint(String name, double x0, double y0, Matrix dispersion) throws IllegalArgumentException {
		super(name, x0, y0);
		this.setDispersionApriori(dispersion);
		this.init(this.getDimension());
	}
	
	public FeaturePoint(String name, double x0, double y0, double z0) throws IllegalArgumentException {
		this(name, x0, y0, z0, MathExtension.identity(3));
	}
	
	public FeaturePoint(String name, double x0, double y0, double z0, Matrix dispersion) throws IllegalArgumentException {
		super(name, x0, y0, z0);
		this.setDispersionApriori(dispersion);
		this.init(this.getDimension());
	}
	
	private void init(int dim) {
		this.x = new ObjectBinding<Double>() {
			{
                super.bind(x0Property(), residualX);
            }
 
            @Override
            protected Double computeValue() {
                return x0Property().get() + residualX.get();
            }
        };
        
        this.y = new ObjectBinding<Double>() {
			{
                super.bind(y0Property(), residualY);
            }
 
            @Override
            protected Double computeValue() {
                return y0Property().get() + residualY.get();
            }
        };
        
        this.z = new ObjectBinding<Double>() {
			{
                super.bind(z0Property(), residualZ);
            }
 
            @Override
            protected Double computeValue() {
                return z0Property().get() + residualZ.get();
            }
        };
        
        this.uncertaintyX = new ObjectBinding<Double>() {
			{
                super.bind(cofactorX, testStatistic);
            }
 
            @Override
            protected Double computeValue() {
            	if (testStatistic.get().varianceComponentProperty().get().isApplyAposterioriVarianceOfUnitWeight())
            		return Math.sqrt(Math.abs(cofactorX.get() * testStatistic.get().varianceComponentProperty().get().varianceProperty().get()));
            	return Math.sqrt(Math.abs(cofactorX.get()));
            }
        };
        
        this.uncertaintyY = new ObjectBinding<Double>() {
			{
                super.bind(cofactorY, testStatistic);
            }
 
            @Override
            protected Double computeValue() {
            	if (testStatistic.get().varianceComponentProperty().get().isApplyAposterioriVarianceOfUnitWeight())
            		return Math.sqrt(Math.abs(cofactorY.get() * testStatistic.get().varianceComponentProperty().get().varianceProperty().get()));
            	return Math.sqrt(Math.abs(cofactorY.get()));
            }
        };
        
        this.uncertaintyZ = new ObjectBinding<Double>() {
			{
                super.bind(cofactorZ, testStatistic);
            }
 
            @Override
            protected Double computeValue() {
            	if (testStatistic.get().varianceComponentProperty().get().isApplyAposterioriVarianceOfUnitWeight())
            		return Math.sqrt(Math.abs(cofactorZ.get() * testStatistic.get().varianceComponentProperty().get().varianceProperty().get()));
            	return Math.sqrt(Math.abs(cofactorZ.get()));
            }
        };
        
        this.testStatisticApriori.bind(this.testStatistic.get().testStatisticAprioriProperty());
        this.testStatisticAposteriori.bind(this.testStatistic.get().testStatisticAposterioriProperty());
        
        this.pValueApriori.bind(this.testStatistic.get().pValueAprioriProperty());
        this.pValueAposteriori.bind(this.testStatistic.get().pValueAposterioriProperty());
        
        this.significant = new ObjectBinding<Boolean>() {
        	{
                super.bind(fisherQuantileApriori, testStatisticApriori, fisherQuantileAposteriori, testStatisticAposteriori);
            }
        	
        	@Override
            protected Boolean computeValue() {
        		boolean significant = testStatisticApriori.get() > fisherQuantileApriori.get();

        		if (testStatistic.get().varianceComponentProperty().get().isApplyAposterioriVarianceOfUnitWeight())
        			return significant || testStatisticAposteriori.get() > fisherQuantileAposteriori.get();

        		return significant;
        	}
        };
	}
	
	public double getX() {
		return this.x.get();
	}
	
	public ObjectBinding<Double> xProperty() {
		return this.x;
	}
	
	public double getY() {
		return this.y.get();
	}
	
	public ObjectBinding<Double> yProperty() {
		return this.y;
	}
	
	public double getZ() {
		return this.z.get();
	}
	
	public ObjectBinding<Double> zProperty() {
		return this.z;
	}
	
	public boolean isEnable() {
		return this.enable.get();
	}
	
	public void setEnable(boolean enable) {
		this.enable.set(enable);
	}
	
	public ObjectProperty<Boolean> enableProperty() {
		return this.enable;
	}

	public double getResidualX() {
		return this.residualX.get();
	}
	
	public void setResidualX(double residualX) {
		this.residualX.set(residualX);
	}
	
	public ObjectProperty<Double> residualXProperty() {
		return this.residualX;
	}
	
	public double getResidualY() {
		return this.residualY.get();
	}
	
	public void setResidualY(double residualY) {
		this.residualY.set(residualY);
	}
	
	public ObjectProperty<Double> residualYProperty() {
		return this.residualY;
	}
	
	public double getResidualZ() {
		return this.residualZ.get();
	}
	
	public void setResidualZ(double residualZ) {
		this.residualZ.set(residualZ);
	}
	
	public ObjectProperty<Double> residualZProperty() {
		return this.residualZ;
	}
	
	public double getRedundancyX() {
		return this.redundancyX.get();
	}
	
	public void setRedundancyX(double redundancyX) {
		this.redundancyX.set(redundancyX);
	}
	
	public ObjectProperty<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public double getRedundancyY() {
		return this.redundancyY.get();
	}
	
	public void setRedundancyY(double redundancyY) {
		this.redundancyY.set(redundancyY);
	}
	
	public ObjectProperty<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public double getRedundancyZ() {
		return this.redundancyZ.get();
	}
	
	public void setRedundancyZ(double redundancyZ) {
		this.redundancyZ.set(redundancyZ);
	}
	
	public ObjectProperty<Double> redundancyZProperty() {
		return this.redundancyZ;
	}
	
	public double getMinimalDetectableBiasX() {
		return this.minimalDetectableBiasX.get();
	}
	
	public void setMinimalDetectableBiasX(double minimalDetectableBiasX) {
		this.minimalDetectableBiasX.set(minimalDetectableBiasX);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasXProperty() {
		return this.minimalDetectableBiasX;
	}
	
	public double getMinimalDetectableBiasY() {
		return this.minimalDetectableBiasY.get();
	}
	
	public void setMinimalDetectableBiasY(double minimalDetectableBiasY) {
		this.minimalDetectableBiasY.set(minimalDetectableBiasY);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasYProperty() {
		return this.minimalDetectableBiasY;
	}
	
	public double getMinimalDetectableBiasZ() {
		return this.minimalDetectableBiasZ.get();
	}
	
	public void setMinimalDetectableBiasZ(double minimalDetectableBiasZ) {
		this.minimalDetectableBiasZ.set(minimalDetectableBiasZ);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasZProperty() {
		return this.minimalDetectableBiasZ;
	}
	
	public double getMaximumTolerableBiasX() {
		return this.minimalDetectableBiasX.get();
	}
	
	public void setMaximumTolerableBiasX(double maximumTolerableBiasX) {
		this.maximumTolerableBiasX.set(maximumTolerableBiasX);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasXProperty() {
		return this.maximumTolerableBiasX;
	}
	
	public double getMaximumTolerableBiasY() {
		return this.minimalDetectableBiasY.get();
	}
	
	public void setMaximumTolerableBiasY(double maximumTolerableBiasY) {
		this.maximumTolerableBiasY.set(maximumTolerableBiasY);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasYProperty() {
		return this.maximumTolerableBiasY;
	}
	
	public double getMaximumTolerableBiasZ() {
		return this.minimalDetectableBiasZ.get();
	}
	
	public void setMaximumTolerableBiasZ(double maximumTolerableBiasZ) {
		this.maximumTolerableBiasZ.set(maximumTolerableBiasZ);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasZProperty() {
		return this.maximumTolerableBiasZ;
	}
	
	public double getGrossErrorX() {
		return this.grossErrorX.get();
	}
	
	public void setGrossErrorX(double grossErrorX) {
		this.grossErrorX.set(grossErrorX);
	}
	
	public ObjectProperty<Double> grossErrorXProperty() {
		return this.grossErrorX;
	}
	
	public double getGrossErrorY() {
		return this.grossErrorY.get();
	}
	
	public void setGrossErrorY(double grossErrorY) {
		this.grossErrorY.set(grossErrorY);
	}
	
	public ObjectProperty<Double> grossErrorYProperty() {
		return this.grossErrorY;
	}
	
	public double getGrossErrorZ() {
		return this.grossErrorZ.get();
	}
	
	public void setGrossErrorZ(double grossErrorZ) {
		this.grossErrorZ.set(grossErrorZ);
	}
	
	public ObjectProperty<Double> grossErrorZProperty() {
		return this.grossErrorZ;
	}
	
	public ObjectProperty<Matrix> dispersionAprioriProperty() {
		return this.dispersionApriori;
	}
	
	public Matrix getDispersionApriori() {
		return this.dispersionApriori.get();
	}
	
	public double getUncertaintyX() {
		return this.uncertaintyX.get();
	}
	
	public ObjectBinding<Double> uncertaintyXProperty() {
		return this.uncertaintyX;
	}
	
	public double getUncertaintyY() {
		return this.uncertaintyY.get();
	}
	
	public ObjectBinding<Double> uncertaintyYProperty() {
		return this.uncertaintyY;
	}
	
	public double getUncertaintyZ() {
		return this.uncertaintyZ.get();
	}
	
	public ObjectBinding<Double> uncertaintyZProperty() {
		return this.uncertaintyZ;
	}
	
	public double getCofactorX() {
		return this.cofactorX.get();
	}
	
	public void setCofactorX(double cofactorX) {
		this.cofactorX.set(cofactorX);
	}
	
	public ObjectProperty<Double> cofactorXProperty() {
		return this.cofactorX;
	}
	
	public double getCofactorY() {
		return this.cofactorY.get();
	}
	
	public void setCofactorY(double cofactorY) {
		this.cofactorY.set(cofactorY);
	}
	
	public ObjectProperty<Double> cofactorYProperty() {
		return this.cofactorY;
	}
	
	public double getCofactorZ() {
		return this.cofactorZ.get();
	}
	
	public void setCofactorZ(double cofactorZ) {
		this.cofactorZ.set(cofactorZ);
	}
	
	public ObjectProperty<Double> cofactorZProperty() {
		return this.cofactorZ;
	}
	
	public void setDispersionApriori(Matrix dispersion) throws IllegalArgumentException {
		if (!dispersion.isSquare() || this.getDimension() != dispersion.numColumns())
			throw new IllegalArgumentException("Error, dispersion matrix must be a squared matrix of dimension " + this.getDimension() + " x " + this.getDimension() + "!");
			
		if (!(dispersion instanceof UpperSymmBandMatrix) && !(dispersion instanceof UnitUpperTriangBandMatrix) && !(dispersion instanceof UpperSymmPackMatrix))
			throw new IllegalArgumentException("Error, dispersion matrix must be of type UpperSymmBandMatrix, UnitUpperTriangBandMatrix, or UpperSymmPackMatrix!");
		
		
		if ((dispersion instanceof UpperSymmBandMatrix && ((UpperSymmBandMatrix)dispersion).numSuperDiagonals() != 0 ) || 
				(dispersion instanceof UnitUpperTriangBandMatrix) && ((UnitUpperTriangBandMatrix)dispersion).numSuperDiagonals() != 0)
			throw new IllegalArgumentException("Error, dispersion matrix must be a diagonal matrix, if BandMatrix type is used!");

		this.dispersionApriori.set(dispersion);
	}
	
	public Matrix getInvertedDispersion(boolean inplace) throws MatrixSingularException, IllegalArgumentException {
		Matrix dispersionApriori = this.getDispersionApriori();
		int size = dispersionApriori.numColumns();

		if (dispersionApriori instanceof UnitUpperTriangBandMatrix) 
			return dispersionApriori; //inplace ? this.dispersion : new UnitUpperTriangBandMatrix(size, 0);

		else if (dispersionApriori instanceof UpperSymmBandMatrix) {
			Matrix W = inplace ? dispersionApriori : new UpperSymmBandMatrix(size, 0);
			for (MatrixEntry entry : dispersionApriori) {
				double value = entry.get();
				if (value <= 0)
					throw new MatrixSingularException("Error, matrix is a singular matrix!");
				W.set(entry.row(), entry.column(), 1.0 / value);
			} 
			return W;
		}
		else if (dispersionApriori instanceof UpperSymmPackMatrix) {
			UpperSymmPackMatrix W = inplace ? (UpperSymmPackMatrix)dispersionApriori : new UpperSymmPackMatrix(dispersionApriori, true);
			MathExtension.inv(W);
			return W;
		}
		
		throw new IllegalArgumentException("Error, dispersion matrix must be of type UpperSymmBandMatrix, UnitUpperTriangBandMatrix, or UpperSymmPackMatrix!");		 
	}
	
	public boolean add(GeometricPrimitive geometry) {
		return this.geometries.add(geometry);
	}
	
	public boolean remove(GeometricPrimitive geometry) {
		return this.geometries.remove(geometry);
	}
	
	public int getNumberOfGeomtries() {
		return this.geometries.size();
	}
	
	public TestStatistic getTestStatistic() {
		return this.testStatistic.get();
	}
	
	public ReadOnlyObjectProperty<TestStatistic> testStatisticProperty() {
		return this.testStatistic;
	}
	
	public ReadOnlyObjectWrapper<Double> testStatisticAprioriProperty() {
		return this.testStatisticApriori;
	}
	
	public ReadOnlyObjectWrapper<Double> testStatisticAposterioriProperty() {
		return this.testStatisticAposteriori;
	}
	
	public ReadOnlyObjectWrapper<Double> pValueAprioriProperty() {
		return this.pValueApriori;
	}
	
	public ReadOnlyObjectWrapper<Double> pValueAposterioriProperty() {
		return this.pValueAposteriori;
	}
	
	public ObjectBinding<Boolean> significantProperty() {
		return this.significant;
	}
	
	public boolean isSignificant() {
		return this.significant.get();
	}
	
	public void setFisherQuantileApriori(double fisherQuantileApriori) {
		this.fisherQuantileApriori.set(fisherQuantileApriori);
	}
	
	public double getFisherQuantileApriori() {
		return this.fisherQuantileApriori.get();
	}
	
	public ObjectProperty<Double> fisherQuantileAprioriProperty() {
		return this.fisherQuantileApriori;
	}
	
	public void setFisherQuantileAposteriori(double fisherQuantileAposteriori) {
		this.fisherQuantileAposteriori.set(fisherQuantileAposteriori);
	}
	
	public double getFisherQuantileAposteriori() {
		return this.fisherQuantileAposteriori.get();
	}
	
	public ObjectProperty<Double> fisherQuantileAposterioriProperty() {
		return this.fisherQuantileAposteriori;
	}
	
	@Override
	public Iterator<GeometricPrimitive> iterator() {
		return this.geometries.iterator();
	}

	public void reset() {
		this.setResidualX(0);
		this.setResidualY(0);
		this.setResidualZ(0);
		
		this.setRedundancyX(0);
		this.setRedundancyY(0);
		this.setRedundancyZ(0);
		
		this.setGrossErrorX(0);
		this.setGrossErrorY(0);
		this.setGrossErrorZ(0);
		
		this.setMinimalDetectableBiasX(0);
		this.setMinimalDetectableBiasY(0);
		this.setMinimalDetectableBiasZ(0);
		
		this.setCofactorX(0);
		this.setCofactorY(0);
		this.setCofactorZ(0);
		
		this.testStatistic.get().setFisherTestNumerator(0);
		this.testStatistic.get().setDegreeOfFreedom(0);
	}
	
	public void clear() {
		this.reset();
		this.geometries.clear();
	}
}
