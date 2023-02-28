package org.applied_geodesy.adjustment.transformation.point;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.applied_geodesy.adjustment.transformation.TestStatistic;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.Matrix;

public class HomologousFramePositionPair extends PositionPair<HomologousFramePosition, HomologousFramePosition> implements Iterable<HomologousFramePosition> {

	private ReadOnlyObjectProperty<TestStatistic> testStatistic = new ReadOnlyObjectWrapper<TestStatistic>(this, "testStatistic", new TestStatistic());
		
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>(this, "grossErrorX", 0.0);
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>(this, "grossErrorY", 0.0);
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>(this, "grossErrorZ", 0.0);
	
	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasX", 0.0);
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasY", 0.0);
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasZ", 0.0);
	
	private ObjectProperty<Double> maximumTolerableBiasX = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasX", 0.0);
	private ObjectProperty<Double> maximumTolerableBiasY = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasY", 0.0);
	private ObjectProperty<Double> maximumTolerableBiasZ = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasZ", 0.0);
	
	private ReadOnlyObjectWrapper<Double> testStatisticApriori     = new ReadOnlyObjectWrapper<Double>(this, "testStatisticApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> testStatisticAposteriori = new ReadOnlyObjectWrapper<Double>(this, "testStatisticAposteriori", 0.0);
	
	private ReadOnlyObjectWrapper<Double> pValueApriori     = new ReadOnlyObjectWrapper<Double>(this, "pValueApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> pValueAposteriori = new ReadOnlyObjectWrapper<Double>(this, "pValueAposteriori", 0.0);
	
	private ObjectBinding<Boolean> significant;

	private ObjectProperty<Double> fisherQuantileApriori     = new SimpleObjectProperty<Double>(this, "fisherQuantileApriori", Double.MAX_VALUE);
	private ObjectProperty<Double> fisherQuantileAposteriori = new SimpleObjectProperty<Double>(this, "fisherQuantileAposteriori", Double.MAX_VALUE);

	
//	public HomologousFramePositionPair(HomologousFramePositionPair positionPair) {
//		this(positionPair.getSourceSystem(), positionPair.getTargetSystem());
//		this.setEnable(positionPair.isEnable());
//	}
	
	public HomologousFramePositionPair(String name, double zSrc, double zTrg) {
		this(name, new HomologousFramePosition(zSrc), new HomologousFramePosition(zTrg));
	}
	
	public HomologousFramePositionPair(String name, double xSrc, double ySrc, double xTrg, double yTrg) {
		this(name, new HomologousFramePosition(xSrc, ySrc), new HomologousFramePosition(xTrg, yTrg));
	}
	
	public HomologousFramePositionPair(String name, double xSrc, double ySrc, double zSrc, double xTrg, double yTrg, double zTrg) {
		this(name, new HomologousFramePosition(xSrc, ySrc, zSrc), new HomologousFramePosition(xTrg, yTrg, zTrg));
	}
	
	public HomologousFramePositionPair(String name, double zSrc, Matrix dispersionSrc, double zTrg, Matrix dispersionTrg) {
		this(name, new HomologousFramePosition(zSrc, dispersionSrc), new HomologousFramePosition(zTrg, dispersionTrg));
	}
	
	public HomologousFramePositionPair(String name, double xSrc, double ySrc, Matrix dispersionSrc, double xTrg, double yTrg, Matrix dispersionTrg) {
		this(name, new HomologousFramePosition(xSrc, ySrc, dispersionSrc), new HomologousFramePosition(xTrg, yTrg, dispersionTrg));
	}
	
	public HomologousFramePositionPair(String name, double xSrc, double ySrc, double zSrc, Matrix dispersionSrc, double xTrg, double yTrg, double zTrg, Matrix dispersionTrg) {
		this(name, new HomologousFramePosition(xSrc, ySrc, zSrc, dispersionSrc), new HomologousFramePosition(xTrg, yTrg, zTrg, dispersionTrg));
	}
	
	private HomologousFramePositionPair(String name, HomologousFramePosition pointSrc, HomologousFramePosition pointTrg) {
		this(name, pointSrc, pointTrg, Boolean.FALSE);
	}
	
	private HomologousFramePositionPair(String name, HomologousFramePosition pointSrc, HomologousFramePosition pointTrg, boolean deep) {
		super(
				name,
				!deep ? pointSrc : 
				pointSrc.getDimension() == 1 ? new HomologousFramePosition(pointSrc.getZ0(), pointSrc.getDispersionApriori()) : 
				pointSrc.getDimension() == 2 ? new HomologousFramePosition(pointSrc.getX0(), pointSrc.getY0(), pointSrc.getDispersionApriori()) :
				new HomologousFramePosition(pointSrc.getX0(), pointSrc.getY0(), pointSrc.getZ0(), pointSrc.getDispersionApriori()), 
	
				!deep ? pointTrg :
				pointTrg.getDimension() == 1 ? new HomologousFramePosition(pointTrg.getZ0(), pointTrg.getDispersionApriori()) :
				pointTrg.getDimension() == 2 ? new HomologousFramePosition(pointTrg.getX0(), pointTrg.getY0(), pointTrg.getDispersionApriori()) :
				new HomologousFramePosition(pointTrg.getX0(), pointTrg.getY0(), pointTrg.getZ0(), pointTrg.getDispersionApriori())
		);

		this.init();
	}
	
	private void init() {
		this.getSourceSystemPosition().varianceComponentProperty().bind(testStatistic.get().varianceComponentProperty());
		this.getTargetSystemPosition().varianceComponentProperty().bind(testStatistic.get().varianceComponentProperty());
		
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
		return this.maximumTolerableBiasX.get();
	}
	
	public void setMaximumTolerableBiasX(double maximumTolerableBiasX) {
		this.maximumTolerableBiasX.set(maximumTolerableBiasX);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasXProperty() {
		return this.maximumTolerableBiasX;
	}
	
	public double getMaximumTolerableBiasY() {
		return this.maximumTolerableBiasY.get();
	}
	
	public void setMaximumTolerableBiasY(double maximumTolerableBiasY) {
		this.maximumTolerableBiasY.set(maximumTolerableBiasY);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasYProperty() {
		return this.maximumTolerableBiasY;
	}
	
	public double getMaximumTolerableBiasZ() {
		return this.maximumTolerableBiasZ.get();
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
	public void reset() {
		super.reset();
		
		this.getSourceSystemPosition().reset();
		this.getTargetSystemPosition().reset();
		
		this.setGrossErrorX(0);
		this.setGrossErrorY(0);
		this.setGrossErrorZ(0);
		
		this.setMinimalDetectableBiasX(0);
		this.setMinimalDetectableBiasY(0);
		this.setMinimalDetectableBiasZ(0);
		
		this.testStatistic.get().setFisherTestNumerator(0);
		this.testStatistic.get().setDegreeOfFreedom(0);
	}
	
	@Override
	public Iterator<HomologousFramePosition> iterator() {
		return new Iterator<HomologousFramePosition>() {
			HomologousFramePosition currentPosition = getSourceSystemPosition(); 
		      
		    // Checks if the next element exists
		    public boolean hasNext() {
		    	return this.currentPosition != null;
		    }
		      
		    // moves the cursor/iterator to next element
		    public HomologousFramePosition next() {
		    	if (!this.hasNext()) 
	                throw new NoSuchElementException();
		    	
		    	if (this.currentPosition == getSourceSystemPosition()) {
		    		this.currentPosition = getTargetSystemPosition();
		    		return getSourceSystemPosition();
		    	}
		    	else if (this.currentPosition == getTargetSystemPosition()) {
		    		this.currentPosition = null;
		    		return getTargetSystemPosition();
		    	}
		    	
	            return null;
		    }
		};
	}
}
