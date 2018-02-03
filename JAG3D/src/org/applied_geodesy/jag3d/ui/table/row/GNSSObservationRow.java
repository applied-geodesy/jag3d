package org.applied_geodesy.jag3d.ui.table.row;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class GNSSObservationRow extends ObservationRow {
	private ObjectProperty<Double> xApriori = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> yApriori = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> zApriori = new SimpleObjectProperty<Double>(0.0);
	
	private ObjectProperty<Double> sigmaXapriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYapriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaZapriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> xAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> yAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> zAposteriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> sigmaXaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaZaposteriori = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> influenceOnPointPositionX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> influenceOnPointPositionY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> influenceOnPointPositionZ = new SimpleObjectProperty<Double>();

	public ObjectProperty<Double> xAprioriProperty() {
		return this.xApriori;
	}
	
	public Double getXApriori() {
		return this.xAprioriProperty().get();
	}
	
	public void setXApriori(final Double xApriori) {
		this.xAprioriProperty().set(xApriori);
	}
	
	public ObjectProperty<Double> yAprioriProperty() {
		return this.yApriori;
	}
	
	public Double getYApriori() {
		return this.yAprioriProperty().get();
	}
	
	public void setYApriori(final Double yApriori) {
		this.yAprioriProperty().set(yApriori);
	}
	
	public ObjectProperty<Double> zAprioriProperty() {
		return this.zApriori;
	}
	
	public Double getZApriori() {
		return this.zAprioriProperty().get();
	}
	
	public void setZApriori(final Double zApriori) {
		this.zAprioriProperty().set(zApriori);
	}
	
	public ObjectProperty<Double> sigmaXaprioriProperty() {
		return this.sigmaXapriori;
	}
	
	public Double getSigmaXapriori() {
		return this.sigmaXaprioriProperty().get();
	}

	public void setSigmaXapriori(final Double sigmaXapriori) {
		this.sigmaXaprioriProperty().set(sigmaXapriori);
	}
	
	public ObjectProperty<Double> sigmaYaprioriProperty() {
		return this.sigmaYapriori;
	}
	
	public Double getSigmaYapriori() {
		return this.sigmaYaprioriProperty().get();
	}

	public void setSigmaYapriori(final Double sigmaYapriori) {
		this.sigmaYaprioriProperty().set(sigmaYapriori);
	}
	
	public ObjectProperty<Double> sigmaZaprioriProperty() {
		return this.sigmaZapriori;
	}
	
	public Double getSigmaZapriori() {
		return this.sigmaZaprioriProperty().get();
	}
	
	public void setSigmaZapriori(final Double sigmaZapriori) {
		this.sigmaZaprioriProperty().set(sigmaZapriori);
	}
	
	public ObjectProperty<Double> xAposterioriProperty() {
		return this.xAposteriori;
	}
	
	public Double getXAposteriori() {
		return this.xAposterioriProperty().get();
	}
	
	public void setXAposteriori(final Double xAposteriori) {
		this.xAposterioriProperty().set(xAposteriori);
	}
	
	public ObjectProperty<Double> yAposterioriProperty() {
		return this.yAposteriori;
	}
	
	public Double getYAposteriori() {
		return this.yAposterioriProperty().get();
	}
	
	public void setYAposteriori(final Double yAposteriori) {
		this.yAposterioriProperty().set(yAposteriori);
	}
	
	public ObjectProperty<Double> zAposterioriProperty() {
		return this.zAposteriori;
	}
	
	public Double getZAposteriori() {
		return this.zAposterioriProperty().get();
	}
	
	public void setZAposteriori(final Double zAposteriori) {
		this.zAposterioriProperty().set(zAposteriori);
	}
	
	public ObjectProperty<Double> sigmaXaposterioriProperty() {
		return this.sigmaXaposteriori;
	}
	
	public Double getSigmaXaposteriori() {
		return this.sigmaXaposterioriProperty().get();
	}
	
	public void setSigmaXaposteriori(final Double sigmaXaposteriori) {
		this.sigmaXaposterioriProperty().set(sigmaXaposteriori);
	}
	
	public ObjectProperty<Double> sigmaYaposterioriProperty() {
		return this.sigmaYaposteriori;
	}
	
	public Double getSigmaYaposteriori() {
		return this.sigmaYaposterioriProperty().get();
	}
	
	public void setSigmaYaposteriori(final Double sigmaYaposteriori) {
		this.sigmaYaposterioriProperty().set(sigmaYaposteriori);
	}
	
	public ObjectProperty<Double> sigmaZaposterioriProperty() {
		return this.sigmaZaposteriori;
	}
	
	public Double getSigmaZaposteriori() {
		return this.sigmaZaposterioriProperty().get();
	}
	
	public void setSigmaZaposteriori(final Double sigmaZaposteriori) {
		this.sigmaZaposterioriProperty().set(sigmaZaposteriori);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasXProperty() {
		return this.minimalDetectableBiasX;
	}
	
	public Double getMinimalDetectableBiasX() {
		return this.minimalDetectableBiasXProperty().get();
	}
	
	public void setMinimalDetectableBiasX(final Double minimalDetectableBiasX) {
		this.minimalDetectableBiasXProperty().set(minimalDetectableBiasX);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasYProperty() {
		return this.minimalDetectableBiasY;
	}
	
	public Double getMinimalDetectableBiasY() {
		return this.minimalDetectableBiasYProperty().get();
	}
	
	public void setMinimalDetectableBiasY(final Double minimalDetectableBiasY) {
		this.minimalDetectableBiasYProperty().set(minimalDetectableBiasY);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasZProperty() {
		return this.minimalDetectableBiasZ;
	}
	
	public Double getMinimalDetectableBiasZ() {
		return this.minimalDetectableBiasZProperty().get();
	}
	
	public void setMinimalDetectableBiasZ(final Double minimalDetectableBiasZ) {
		this.minimalDetectableBiasZProperty().set(minimalDetectableBiasZ);
	}
	
	public ObjectProperty<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public Double getRedundancyX() {
		return this.redundancyXProperty().get();
	}
	
	public void setRedundancyX(final Double redundancyX) {
		this.redundancyXProperty().set(redundancyX);
	}
	
	public ObjectProperty<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public Double getRedundancyY() {
		return this.redundancyYProperty().get();
	}
	
	public void setRedundancyY(final Double redundancyY) {
		this.redundancyYProperty().set(redundancyY);
	}
	
	public ObjectProperty<Double> redundancyZProperty() {
		return this.redundancyZ;
	}
	
	public Double getRedundancyZ() {
		return this.redundancyZProperty().get();
	}
	
	public void setRedundancyZ(final Double redundancyZ) {
		this.redundancyZProperty().set(redundancyZ);
	}
	
	public ObjectProperty<Double> grossErrorXProperty() {
		return this.grossErrorX;
	}
	
	public Double getGrossErrorX() {
		return this.grossErrorXProperty().get();
	}
	
	public void setGrossErrorX(final Double nablaX) {
		this.grossErrorXProperty().set(nablaX);
	}
	
	public ObjectProperty<Double> grossErrorYProperty() {
		return this.grossErrorY;
	}
	
	public Double getGrossErrorY() {
		return this.grossErrorYProperty().get();
	}
	
	public void setGrossErrorY(final Double nablaY) {
		this.grossErrorYProperty().set(nablaY);
	}
	
	public ObjectProperty<Double> grossErrorZProperty() {
		return this.grossErrorZ;
	}
	
	public Double getGrossErrorZ() {
		return this.grossErrorZProperty().get();
	}
	
	public void setGrossErrorZ(final Double nablaZ) {
		this.grossErrorZProperty().set(nablaZ);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionXProperty() {
		return this.influenceOnPointPositionX;
	}
	
	public Double getInfluenceOnPointPositionX() {
		return this.influenceOnPointPositionXProperty().get();
	}
	
	public void setInfluenceOnPointPositionX(final Double influenceOnPointPositionX) {
		this.influenceOnPointPositionXProperty().set(influenceOnPointPositionX);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionYProperty() {
		return this.influenceOnPointPositionY;
	}
	
	public Double getInfluenceOnPointPositionY() {
		return this.influenceOnPointPositionYProperty().get();
	}
	
	public void setInfluenceOnPointPositionY(final Double influenceOnPointPositionY) {
		this.influenceOnPointPositionYProperty().set(influenceOnPointPositionY);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionZProperty() {
		return this.influenceOnPointPositionZ;
	}
	
	public Double getInfluenceOnPointPositionZ() {
		return this.influenceOnPointPositionZProperty().get();
	}
	
	public void setInfluenceOnPointPositionZ(final Double influenceOnPointPositionZ) {
		this.influenceOnPointPositionZProperty().set(influenceOnPointPositionZ);
	}

	public static GNSSObservationRow cloneRowApriori(GNSSObservationRow row) {
		GNSSObservationRow clone = new GNSSObservationRow();
		
		clone.setId(-1);
		clone.setEnable(row.isEnable());
		
		clone.setStartPointName(row.getStartPointName());
		clone.setEndPointName(row.getEndPointName());
		
		clone.setXApriori(row.getXApriori());
		clone.setYApriori(row.getYApriori());
		clone.setZApriori(row.getZApriori());
		
		clone.setSigmaXapriori(row.getSigmaXapriori());
		clone.setSigmaYapriori(row.getSigmaYapriori());
		clone.setSigmaZapriori(row.getSigmaZapriori());

		return clone;
	}
}
