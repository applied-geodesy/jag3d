package org.applied_geodesy.jag3d.ui.table.row;

import java.util.Locale;
import java.util.Scanner;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TerrestrialObservationRow extends ObservationRow {
	private ObjectProperty<Double> valueApriori     = new SimpleObjectProperty<Double>(this, "valueApriori");
	private ObjectProperty<Double> valueAposteriori = new SimpleObjectProperty<Double>(this, "valueAposteriori");
	private ObjectProperty<Double> distanceApriori  = new SimpleObjectProperty<Double>(this, "distanceApriori");

	private ObjectProperty<Double> instrumentHeight = new SimpleObjectProperty<Double>(this, "instrumentHeight", 0.0);
	private ObjectProperty<Double> reflectorHeight  = new SimpleObjectProperty<Double>(this, "reflectorHeight", 0.0);

	private ObjectProperty<Double> sigmaApriori     = new SimpleObjectProperty<Double>(this, "sigmaApriori");
	private ObjectProperty<Double> sigmaAposteriori = new SimpleObjectProperty<Double>(this, "sigmaAposteriori");

	private ObjectProperty<Double> redundancy                   = new SimpleObjectProperty<Double>(this, "redundancy");
	private ObjectProperty<Double> grossError                   = new SimpleObjectProperty<Double>(this, "grossError");
	private ObjectProperty<Double> influenceOnPointPosition     = new SimpleObjectProperty<Double>(this, "influenceOnPointPosition"); 
	private ObjectProperty<Double> minimalDetectableBias        = new SimpleObjectProperty<Double>(this, "minimalDetectableBias");

	public ObjectProperty<Double> valueAprioriProperty() {
		return this.valueApriori;
	}

	public Double getValueApriori() {
		return this.valueAprioriProperty().get();
	}

	public void setValueApriori(final Double valueApriori) {
		this.valueAprioriProperty().set(valueApriori);
	}

	public ObjectProperty<Double> valueAposterioriProperty() {
		return this.valueAposteriori;
	}

	public Double getValueAposteriori() {
		return this.valueAposterioriProperty().get();
	}

	public void setValueAposteriori(final Double valueAposteriori) {
		this.valueAposterioriProperty().set(valueAposteriori);
	}

	public ObjectProperty<Double> distanceAprioriProperty() {
		return this.distanceApriori;
	}

	public Double getDistanceApriori() {
		return this.distanceAprioriProperty().get();
	}

	public void setDistanceApriori(final Double distanceApriori) {
		this.distanceAprioriProperty().set(distanceApriori);
	}

	public ObjectProperty<Double> instrumentHeightProperty() {
		return this.instrumentHeight;
	}

	public Double getInstrumentHeight() {
		return this.instrumentHeightProperty().get();
	}

	public void setInstrumentHeight(final Double instrumentHeight) {
		this.instrumentHeightProperty().set(instrumentHeight);
	}

	public ObjectProperty<Double> reflectorHeightProperty() {
		return this.reflectorHeight;
	}

	public Double getReflectorHeight() {
		return this.reflectorHeightProperty().get();
	}

	public void setReflectorHeight(final Double reflectorHeight) {
		this.reflectorHeightProperty().set(reflectorHeight);
	}

	public ObjectProperty<Double> sigmaAprioriProperty() {
		return this.sigmaApriori;
	}

	public Double getSigmaApriori() {
		return this.sigmaAprioriProperty().get();
	}

	public void setSigmaApriori(final Double sigmaApriori) {
		this.sigmaAprioriProperty().set(sigmaApriori);
	}

	public ObjectProperty<Double> sigmaAposterioriProperty() {
		return this.sigmaAposteriori;
	}

	public Double getSigmaAposteriori() {
		return this.sigmaAposterioriProperty().get();
	}

	public void setSigmaAposteriori(final Double sigmaAposteriori) {
		this.sigmaAposterioriProperty().set(sigmaAposteriori);
	}

	public ObjectProperty<Double> redundancyProperty() {
		return this.redundancy;
	}

	public Double getRedundancy() {
		return this.redundancyProperty().get();
	}

	public void setRedundancy(final Double redundancy) {
		this.redundancyProperty().set(redundancy);
	}

	public ObjectProperty<Double> grossErrorProperty() {
		return this.grossError;
	}

	public Double getgrossError() {
		return this.grossErrorProperty().get();
	}

	public void setGrossError(final Double nabla) {
		this.grossErrorProperty().set(nabla);
	}

	public ObjectProperty<Double> influenceOnPointPositionProperty() {
		return this.influenceOnPointPosition;
	}

	public Double getInfluenceOnPointPosition() {
		return this.influenceOnPointPositionProperty().get();
	}

	public void setInfluenceOnPointPosition(final Double influenceOnPointPosition) {
		this.influenceOnPointPositionProperty().set(influenceOnPointPosition);
	}

	public ObjectProperty<Double> minimalDetectableBiasProperty() {
		return this.minimalDetectableBias;
	}

	public Double getMinimalDetectableBias() {
		return this.minimalDetectableBiasProperty().get();
	}

	public void setMinimalDetectableBias(final Double minimalDetectableBias) {
		this.minimalDetectableBiasProperty().set(minimalDetectableBias);
	}

	public static TerrestrialObservationRow cloneRowApriori(TerrestrialObservationRow row) {
		TerrestrialObservationRow clone = new TerrestrialObservationRow();

		clone.setId(-1);
		clone.setEnable(row.isEnable());

		clone.setStartPointName(row.getStartPointName());
		clone.setEndPointName(row.getEndPointName());

		clone.setInstrumentHeight(row.getInstrumentHeight());
		clone.setReflectorHeight(row.getReflectorHeight());

		clone.setValueApriori(row.getValueApriori());
		clone.setSigmaApriori(row.getSigmaApriori());
		clone.setDistanceApriori(row.getDistanceApriori());

		return clone;
	}

	public static TerrestrialObservationRow scan(String str, ObservationType type) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String startPointName, endPointName; 
			double ih = 0.0, th = 0.0;
			double value = 0.0; 
			double sigma = 0.0;
			double distance = 0.0;

			TerrestrialObservationRow row = new TerrestrialObservationRow();
			// Startpunktnummer
			if (!scanner.hasNext())
				return null;
			startPointName = scanner.next();

			// Zielpunktnummer
			if (!scanner.hasNext())
				return null;
			endPointName = scanner.next();	

			if (startPointName.equals(endPointName))
				return null;

			row.setStartPointName(startPointName);
			row.setEndPointName(endPointName);

			// Standpunkthoehe (oder bereits der Messwert) 		
			if (!scanner.hasNextDouble())
				return null;
			ih = value = scanner.nextDouble();
			ih = options.convertLengthToModel(ih);
			switch(type) {
			case DIRECTION:
			case ZENITH_ANGLE:
				value = options.convertAngleToModel(value);
				break;
			default:
				value = options.convertLengthToModel(value);
				break;
			}
			row.setValueApriori(value);
			if (type == ObservationType.HORIZONTAL_DISTANCE || type == ObservationType.SLOPE_DISTANCE)
				row.setDistanceApriori(value);

			// Tafelhoehe (oder bereits die Standardabweichung)
			if (!scanner.hasNextDouble()) 
				return row;

			th = sigma = distance = scanner.nextDouble();
			th = options.convertLengthToModel(th);
			distance = options.convertLengthToModel(distance);
			switch(type) {
			case DIRECTION:
			case ZENITH_ANGLE:
				sigma = options.convertAngleToModel(sigma);
				break;
			default:
				sigma = options.convertLengthToModel(sigma);
				break;
			}

			if (distance < 1) {
				row.setSigmaApriori(sigma);
				if (type != ObservationType.HORIZONTAL_DISTANCE && type != ObservationType.SLOPE_DISTANCE)
					row.setDistanceApriori(null);
				else
					row.setDistanceApriori(value);
			}
			else {
				row.setSigmaApriori(null);
				if (type != ObservationType.HORIZONTAL_DISTANCE && type != ObservationType.SLOPE_DISTANCE)
					row.setDistanceApriori(distance);
			}

			// Messwert
			if (!scanner.hasNextDouble())
				return row;

			value = scanner.nextDouble();
			switch(type) {
			case DIRECTION:
			case ZENITH_ANGLE:
				value = options.convertAngleToModel(value);
				break;
			default:
				value = options.convertLengthToModel(value);
				break;
			}
			
			row.setInstrumentHeight(ih);
			row.setReflectorHeight(th);
			row.setValueApriori(value);
			row.setSigmaApriori(null);
			if (type != ObservationType.HORIZONTAL_DISTANCE && type != ObservationType.SLOPE_DISTANCE)
				row.setDistanceApriori(null);
			else
				row.setDistanceApriori(value);
			
			// Standardabweichung
			if (!scanner.hasNextDouble())
				return row;

			sigma = distance = scanner.nextDouble();
			distance = options.convertLengthToModel(distance);
			switch(type) {
			case DIRECTION:
			case ZENITH_ANGLE:
				sigma = options.convertAngleToModel(sigma);
				break;
			default:
				sigma = options.convertLengthToModel(sigma);
				break;
			}

			if (distance < 1) {
				row.setSigmaApriori(sigma);
				if (type != ObservationType.HORIZONTAL_DISTANCE && type != ObservationType.SLOPE_DISTANCE)
					row.setDistanceApriori(null);
				else
					row.setDistanceApriori(value);
			}
			else {
				row.setSigmaApriori(null);
				if (type != ObservationType.HORIZONTAL_DISTANCE && type != ObservationType.SLOPE_DISTANCE)
					row.setDistanceApriori(distance);
			}

			return row;
		}
		finally {
			scanner.close();
		}
	}
}

