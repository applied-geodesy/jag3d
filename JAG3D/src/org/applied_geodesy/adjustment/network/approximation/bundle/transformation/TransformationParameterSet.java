package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

import java.util.Map;

public class TransformationParameterSet {

	private Map<TransformationParameterType, TransformationParameter> transformationParameters = Map.of(
			TransformationParameterType.TRANSLATION_X, new TransformationParameter(TransformationParameterType.TRANSLATION_X, 0.0),
			TransformationParameterType.TRANSLATION_Y, new TransformationParameter(TransformationParameterType.TRANSLATION_Y, 0.0),
			TransformationParameterType.TRANSLATION_Z, new TransformationParameter(TransformationParameterType.TRANSLATION_Z, 0.0),

			TransformationParameterType.ROTATION_X, new TransformationParameter(TransformationParameterType.ROTATION_X, 0.0),
			TransformationParameterType.ROTATION_Y, new TransformationParameter(TransformationParameterType.ROTATION_Y, 0.0),
			TransformationParameterType.ROTATION_Z, new TransformationParameter(TransformationParameterType.ROTATION_Z, 0.0),

			TransformationParameterType.SCALE, new TransformationParameter(TransformationParameterType.SCALE, 1.0)
	);
	
	public TransformationParameterSet() { }

	public TransformationParameterSet(double tx, double ty, double tz, double rx, double ry, double rz, double m) {
		this.setParameterValue(TransformationParameterType.TRANSLATION_X, tx);
		this.setParameterValue(TransformationParameterType.TRANSLATION_Y, ty);
		this.setParameterValue(TransformationParameterType.TRANSLATION_Z, tz);

		this.setParameterValue(TransformationParameterType.ROTATION_X, rx);
		this.setParameterValue(TransformationParameterType.ROTATION_Y, ry);
		this.setParameterValue(TransformationParameterType.ROTATION_Z, rz);

		this.setParameterValue(TransformationParameterType.SCALE, m);
	}

	public void setParameterValue(TransformationParameterType type, double value) {
		this.transformationParameters.get(type).setValue(value);
	}

	public double getParameterValue(TransformationParameterType type) {
		return this.transformationParameters.get(type).getValue();
	}

	public TransformationParameter get(TransformationParameterType type) {
		return this.transformationParameters.get(type);
	}
}
