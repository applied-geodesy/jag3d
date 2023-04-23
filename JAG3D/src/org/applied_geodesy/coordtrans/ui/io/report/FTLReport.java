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

package org.applied_geodesy.coordtrans.ui.io.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.MathExtension.EulerAngleConventionType;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.VarianceComponent;
import org.applied_geodesy.adjustment.transformation.VarianceComponentType;
import org.applied_geodesy.adjustment.transformation.interpolation.Interpolation;
import org.applied_geodesy.adjustment.transformation.interpolation.InterpolationType;
import org.applied_geodesy.adjustment.transformation.interpolation.InverseDistanceWeighting;
import org.applied_geodesy.adjustment.transformation.interpolation.MultiQuadraticInterpolation;
import org.applied_geodesy.adjustment.transformation.interpolation.SectorInterpolation;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.ObservedFramePosition;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.FormatterOptions.FormatterOption;
import org.applied_geodesy.util.unit.UnitType;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import javafx.application.HostServices;
import no.uib.cipr.matrix.Matrix;

public class FTLReport {
	private final static Version VERSION = Configuration.VERSION_2_3_32;
	private FormatterOptions options = FormatterOptions.getInstance();
	private Template template = null;
	private static HostServices hostServices;
	private Map<String, Object> data = new HashMap<String, Object>();
	public final static String TEMPLATE_PATH = "ftl/ct/";
	private final Configuration cfg = new Configuration(VERSION);
	private TransformationAdjustment adjustment;
	public FTLReport(TransformationAdjustment adjustment) {
		this.adjustment = adjustment;
		this.init();
	}

	private void init() {
		try {
			File path = new File(FTLReport.class.getClassLoader().getResource(TEMPLATE_PATH).toURI());

			this.cfg.setDirectoryForTemplateLoading( path );
			this.cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			this.cfg.setLogTemplateExceptions(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setHostServices(HostServices hostServices) {
		FTLReport.hostServices = hostServices;
	}

	private void setParam(String key, Object value) {
		this.data.put(key, value);
	}

	public void setTemplate(String template) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		this.template = this.cfg.getTemplate(template);
	}

	private void createReport() throws ClassNotFoundException, TemplateModelException {
		this.data.clear();
		BeansWrapper wrapper = new BeansWrapperBuilder(VERSION).build();
		TemplateHashModel staticModels = wrapper.getStaticModels();
		TemplateHashModel mathStatics = (TemplateHashModel) staticModels.get("java.lang.Math");
		this.data.put("Math", mathStatics);

		this.initFormatterOptions();
		this.addMetaData();
		
		this.addTeststatistics();
		this.addVarianceEstimation();
		
		this.addHomologousFramePositionPairs();
		this.addFramePositionPairs();
		this.addUnknownParameters();
		
		this.addCorrelationMatrix();
		this.addHomogeneousCoordinateTransformationMatrix();
		
		this.addEulerAngles();
	}

	public String getSuggestedFileName() {
		return this.adjustment.getTransformation() != null && this.adjustment.getTransformation().getTransformationEquations() != null ? 
				this.adjustment.getTransformation().getTransformationEquations().getTransformationType().name() : null;
	}

	public void toFile(File report) throws ClassNotFoundException, TemplateException, IOException {
		if (report != null) {
			this.createReport();
			Writer file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report), StandardCharsets.UTF_8));
			this.template.process(this.data, file);
			file.flush();
			file.close();

			if (hostServices != null)
				hostServices.showDocument(report.getAbsolutePath());
		}
	}

	private void initFormatterOptions() {
		Map<CellValueType, FormatterOption> options = FormatterOptions.getInstance().getFormatterOptions();

		for (FormatterOption option : options.values()) {
			String keyDigits = null;
			String keyUnit   = null;
			String keySexagesimal = null;
			CellValueType cellValueType = option.getType();
			switch(cellValueType) {
			case ANGLE:
				keyDigits = "digits_angle";
				keyUnit   = "unit_abbr_angle";
				keySexagesimal = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle" : null;
				break;
			case ANGLE_RESIDUAL:
				keyDigits = "digits_angle_residual";
				keyUnit   = "unit_abbr_angle_residual";
				keySexagesimal = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle_residual" : null;
				break;
			case ANGLE_UNCERTAINTY:
				keyDigits = "digits_angle_uncertainty";
				keyUnit   = "unit_abbr_angle_uncertainty";
				keySexagesimal = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle_uncertainty" : null;
				break;

			case LENGTH:
				keyDigits = "digits_length";
				keyUnit   = "unit_abbr_length";
				break;
			case LENGTH_RESIDUAL:
				keyDigits = "digits_length_residual";
				keyUnit   = "unit_abbr_length_residual";
				break;
			case LENGTH_UNCERTAINTY:
				keyDigits = "digits_length_uncertainty";
				keyUnit   = "unit_abbr_length_uncertainty";
				break;

			case SCALE:
				keyDigits = "digits_scale";
				keyUnit   = "unit_abbr_scale";
				break;
			case SCALE_RESIDUAL:
				keyDigits = "digits_scale_residual";
				keyUnit   = "unit_abbr_scale_residual";
				break;
			case SCALE_UNCERTAINTY:
				keyDigits = "digits_scale_uncertainty";
				keyUnit   = "unit_abbr_scale_uncertainty";
				break;
				
			case VECTOR:
				keyDigits = "digits_vector";
				keyUnit   = "unit_abbr_vector";
				break;
			case VECTOR_RESIDUAL:
				keyDigits = "digits_vector_residual";
				keyUnit   = "unit_abbr_vector_residual";
				break;
			case VECTOR_UNCERTAINTY:
				keyDigits = "digits_vector_uncertainty";
				keyUnit   = "unit_abbr_vector_uncertainty";
				break;
				
			case PERCENTAGE:
				keyDigits = "digits_percentage";
				keyUnit   = "unit_abbr_percentage";
				break;

			case STATISTIC:
				keyDigits = "digits_statistic";
				break;
				
			case DOUBLE:
				keyDigits = "digits_double";
				break;

			default:
				continue;
				// break;
			}
			if (keyDigits != null)
				this.setParam(keyDigits,      option.getFormatter().format(0.0) );
			if (keyUnit != null)
				this.setParam(keyUnit,        option.getUnit().getAbbreviation() );
			if (keySexagesimal != null)
				this.setParam(keySexagesimal, Boolean.TRUE );
		}
	}
	
	private void addMetaData() {
		this.setParam("report_creation_date",  new Date(System.currentTimeMillis()));
		this.setParam("version", org.applied_geodesy.version.juniform.Version.get());
		this.setParam("estimation_type", this.adjustment.getEstimationType());
		if (this.adjustment.getTransformation().getInterpolation() == null || this.adjustment.getTransformation().getInterpolation().getInterpolationType() == InterpolationType.NONE) 
			this.setParam("interpolation_type", InterpolationType.NONE.name());
		else {
			Interpolation interpolation = this.adjustment.getTransformation().getInterpolation();
			this.setParam("interpolation_type", interpolation.getInterpolationType().name());
			switch (interpolation.getInterpolationType()) {
			case INVERSE_DISTANCE_WEIGHTING:
				this.setParam("interpolation_idw_exponent",  ((InverseDistanceWeighting)interpolation).getExponent());
				this.setParam("interpolation_idw_smoothing", ((InverseDistanceWeighting)interpolation).getSmoothing());
				break;
			case MULTI_QUADRATIC:
				this.setParam("interpolation_mq_exponent",  ((MultiQuadraticInterpolation)interpolation).getExponent());
				this.setParam("interpolation_mq_smoothing", ((MultiQuadraticInterpolation)interpolation).getSmoothing());
				break;
			case SECTOR:
				this.setParam("interpolation_sect_numerator_exponent",   ((SectorInterpolation)interpolation).getNumeratorExponent());
				this.setParam("interpolation_sect_denominator_exponent", ((SectorInterpolation)interpolation).getDenominatorExponent());
				break;
			default:
				break;
			}
		}
	}

	private void addTeststatistics() {
		TestStatisticDefinition testStatisticDefinition = this.adjustment.getTestStatisticDefinition();
		if (this.adjustment.getTestStatisticDefinition() == null || this.adjustment.getTestStatisticParameters() == null)
			return;
		
		TestStatisticType type  = testStatisticDefinition.getTestStatisticType();
		double powerOfTest      = testStatisticDefinition.getPowerOfTest();
		double probabilityValue = testStatisticDefinition.getProbabilityValue();
		
		this.setParam("test_statistic_method", type.name());
		this.setParam("test_statistic_probability_value", options.convertPercentToView(probabilityValue));
		this.setParam("test_statistic_power_of_test",     options.convertPercentToView(powerOfTest));
		
		List<HashMap<String, Number>> testStatistics = new ArrayList<HashMap<String, Number>>();
		TestStatisticParameterSet[] testStatisticParameterSets = this.adjustment.getTestStatisticParameters().getTestStatisticParameterSets();
		for (TestStatisticParameterSet testStatisticParameterSet : testStatisticParameterSets) {
			if (testStatisticParameterSet.getNumeratorDof() >= 0 && testStatisticParameterSet.getDenominatorDof() > 0) {
				HashMap<String, Number> h = new HashMap<String, Number>();
				h.put("d1", testStatisticParameterSet.getNumeratorDof());
				h.put("d2", testStatisticParameterSet.getDenominatorDof());
				h.put("probability_value", options.convertPercentToView(testStatisticParameterSet.getProbabilityValue()));
				h.put("power_of_test",     options.convertPercentToView(testStatisticParameterSet.getPowerOfTest()));
				h.put("quantile", testStatisticParameterSet.getQuantile());
				h.put("p_value", testStatisticParameterSet.getLogarithmicProbabilityValue());
				h.put("non_centrality_parameter", testStatisticParameterSet.getNoncentralityParameter());
				testStatistics.add(h);
			}
		}
		
		if (!testStatistics.isEmpty()) 
			this.setParam("test_statistic_params", testStatistics);
	}
	
	private void addUnknownParameters() {
		this.setParam("unknown_transformation_parameters", this.getUnknownParameters());
	}
	
	private void addHomogeneousCoordinateTransformationMatrix() {
		Matrix transformationMatrix = this.adjustment.getTransformation().getTransformationEquations().getHomogeneousCoordinateTransformationMatrix();
		if (transformationMatrix == null)
			return;
		
		List<HashMap<String, Object>> matrix = new ArrayList<HashMap<String, Object>>();

		int cols = transformationMatrix.numColumns();
		int rows = transformationMatrix.numRows();
		
		int dimension = this.adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension();
		
		for (int r = 0; r < rows; r++) {
			HashMap<String, Object> row = new HashMap<String, Object>(2);
			List<Double> rowVec = new ArrayList<Double>(rows);
			for (int c = 0; c < cols; c++)
				rowVec.add(transformationMatrix.get(r, c));

			if (dimension != 1 && r < 2)
				row.put("parameter", r == 0 ? 'x' : 'y');
			
			if (dimension != 2 && r == dimension - 1)
				row.put("parameter", 'z');
			
			if (r == dimension)
				row.put("parameter", 'h');
			
			row.put("data", rowVec);
			
			matrix.add(row);
		}

		this.setParam("homogeneous_coordinate_transformation_matrix", matrix);
	}
	
	private void addEulerAngles() {
		if (this.adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension() != 3)
			return;
		
		Matrix R = this.adjustment.getTransformation().getTransformationEquations().getRotationMatrix();
		EulerAngleConventionType eulerAngleConventions[] = MathExtension.EulerAngleConventionType.values();
		
		List<HashMap<String, Object>> conventions = new ArrayList<HashMap<String, Object>>();
		for (EulerAngleConventionType eulerAngleConvention : eulerAngleConventions) {
			double eulerAngles[] = MathExtension.rotationMatrix3D2EulerAngles(R, eulerAngleConvention);
			
			HashMap<String, Object> angles = new HashMap<String, Object>();
			angles.put("convention",   eulerAngleConvention.name());
			angles.put("phi",   options.convertAngleToView(eulerAngles[0]));
			angles.put("theta", options.convertAngleToView(eulerAngles[1]));
			angles.put("psi",   options.convertAngleToView(eulerAngles[2]));
			
			conventions.add(angles);
		}
		this.setParam("euler_angles_conventions", conventions);
	}

	private void addCorrelationMatrix() {
		Matrix correlationMatrix = this.adjustment.getCorrelationMatrix();
		
		if (correlationMatrix == null)
			return;
		
		List<HashMap<String, Object>> matrix = new ArrayList<HashMap<String, Object>>();
		List<UnknownParameter> unknownParameters = this.adjustment.getTransformation().getUnknownParameters();
		
		int cols = correlationMatrix.numColumns();
		int rows = correlationMatrix.numRows();
		
		for (int r = 0; r < rows; r++) {
			HashMap<String, Object> row = new HashMap<String, Object>(2);
			List<Double> rowVec = new ArrayList<Double>(rows);
			for (int c = 0; c < cols; c++) 
				rowVec.add(correlationMatrix.get(r, c));
			
			row.put("parameter", this.getUnknownParameter(unknownParameters.get(r)));
			row.put("data", rowVec);
			
			matrix.add(row);
		}

		this.setParam("correlation_matrix", matrix);
	}
	
	private List<HashMap<String, Object>> getUnknownParameters() {
		List<HashMap<String, Object>> parameters = new ArrayList<HashMap<String, Object>>();
		List<UnknownParameter> unknownParameters = this.adjustment.getTransformation().getUnknownParameters();
		for (UnknownParameter unknownParameter : unknownParameters)
			parameters.add(this.getUnknownParameter(unknownParameter));
		return parameters;
	}
	
	private HashMap<String, Object> getUnknownParameter(UnknownParameter unknownParameter) {
		HashMap<String, Object> parameter = new HashMap<String, Object>();

		ParameterType type = unknownParameter.getParameterType();
		parameter.put("name",            unknownParameter.getName());
		parameter.put("description",     unknownParameter.getDescription());
		parameter.put("processing_type", unknownParameter.getProcessingType().name());
		parameter.put("parameter_type",  type.name());
		parameter.put("visible",         unknownParameter.isVisible());
		parameter.put("indispensable",   unknownParameter.isIndispensable());
		parameter.put("column",          unknownParameter.getColumn());

		double value = unknownParameter.getValue();
		double sigma = unknownParameter.getUncertainty();
				
		switch(type) {

		case SHIFT_X:
		case SHIFT_Y:
		case SHIFT_Z:
			parameter.put("value", options.convertLengthToView(value));
			parameter.put("sigma", options.convertLengthUncertaintyToView(sigma));
			parameter.put("unit_type", "LENGTH");
			break;
		case VECTOR_LENGTH:
		case QUATERNION_Q0:
		case QUATERNION_Q1:
		case QUATERNION_Q2:
		case QUATERNION_Q3:
			parameter.put("value", options.convertVectorToView(value));
			parameter.put("sigma", options.convertVectorUncertaintyToView(sigma));
			parameter.put("unit_type", "VECTOR");
			break;
			
		case EULER_ANGLE_X:
		case EULER_ANGLE_Y:
		case EULER_ANGLE_Z:
		case SHEAR_X:
		case SHEAR_Y:
		case SHEAR_Z:
			parameter.put("value", options.convertAngleToView(value));
			parameter.put("sigma", options.convertAngleUncertaintyToView(sigma));
			parameter.put("unit_type", "ANGLE");
			break;
			
		case SCALE_X:
		case SCALE_Y:
		case SCALE_Z:
			parameter.put("value", options.convertScaleToView(value));
			parameter.put("sigma", options.convertScaleUncertaintyToView(sigma));
			parameter.put("unit_type", "SCALE");
			break;

		default:
			parameter.put("value", value);
			parameter.put("sigma", sigma);
			parameter.put("unit_type", "DOUBLE");
			break;
		}

		return parameter;
	}
	
	private void addFramePositionPairs() {
		HashMap<String, Object> positions = new HashMap<String, Object>();
		List<HashMap<String, Object>> positionList = new ArrayList<HashMap<String, Object>>();
		List<FramePositionPair> framePositionPairs = this.adjustment.getTransformation().getFramePositionPairs();
		
		int dimension = this.adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension();
		for (FramePositionPair positionPair : framePositionPairs) {
			if (!positionPair.isEnable())
				continue;
			
			HashMap<String, Object> h = new HashMap<String, Object>();
			ObservedFramePosition  sourcePosition = positionPair.getSourceSystemPosition();
			EstimatedFramePosition targetPosition = positionPair.getTargetSystemPosition();
			
			h.put("name",        positionPair.getName());
			
			if (dimension != 1) {
				h.put("x", options.convertLengthToView(sourcePosition.getX()));
				h.put("y", options.convertLengthToView(sourcePosition.getY()));
				
				h.put("X", options.convertLengthToView(targetPosition.getX()));
				h.put("Y", options.convertLengthToView(targetPosition.getY()));
				
				h.put("sigma_X", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyX()));
				h.put("sigma_Y", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyY()));
				
				h.put("residual_X", options.convertLengthResidualToView(targetPosition.getResidualX()));
				h.put("residual_Y", options.convertLengthResidualToView(targetPosition.getResidualY()));
			}
			
			if (dimension != 2) {			
				h.put("z", options.convertLengthToView(sourcePosition.getZ()));
				
				h.put("Z", options.convertLengthToView(targetPosition.getZ()));
				
				h.put("sigma_Z", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyZ()));
				
				h.put("residual_Z", options.convertLengthResidualToView(targetPosition.getResidualZ()));
			}
			
			positionList.add(h);
		}
		
		if (positionList != null && !positionList.isEmpty()) {
			positions.put("positions",     positionList);
			positions.put("dimension",     dimension);
			this.setParam("transformed_position_pairs", positions);
		}
	}
	
	private void addHomologousFramePositionPairs() {
		HashMap<String, Object> positions = new HashMap<String, Object>();
		List<HashMap<String, Object>> positionList = new ArrayList<HashMap<String, Object>>();
		List<HomologousFramePositionPair> homologousFramePositionPairs = this.adjustment.getTransformation().getHomologousFramePositionPairs();
		
		int dimension = this.adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension();
		double redundancySourcePositionsX = 0;
		double redundancySourcePositionsY = 0;
		double redundancySourcePositionsZ = 0;
		
		double redundancyTargetPositionsX = 0;
		double redundancyTargetPositionsY = 0;
		double redundancyTargetPositionsZ = 0;
		
		double maxGrossErrorGroupX = 0;
		double maxGrossErrorGroupY = 0;
		double maxGrossErrorGroupZ = 0;
		
		boolean significantGroup = false;
		
		for (HomologousFramePositionPair positionPair : homologousFramePositionPairs) {
			if (!positionPair.isEnable())
				continue;
			
			HashMap<String, Object> h = new HashMap<String, Object>();

			HomologousFramePosition sourcePosition = positionPair.getSourceSystemPosition();
			HomologousFramePosition targetPosition = positionPair.getTargetSystemPosition();

			boolean significant = positionPair.isSignificant();
			
			if (!significantGroup && significant)
				significantGroup = true;
			
			h.put("name",        positionPair.getName());
			
			h.put("t_prio",      positionPair.getTestStatistic().getTestStatisticApriori());
			h.put("t_post",      positionPair.getTestStatistic().getTestStatisticAposteriori());
			
			h.put("p_prio",      positionPair.getTestStatistic().getPValueApriori());
			h.put("p_post",      positionPair.getTestStatistic().getPValueAposteriori());
			
			h.put("significant", significant);
			
			h.put("dimension",   dimension);
						
			if (dimension != 1) {
				h.put("x0", options.convertLengthToView(sourcePosition.getX0()));
				h.put("y0", options.convertLengthToView(sourcePosition.getY0()));
				
				h.put("x", options.convertLengthToView(sourcePosition.getX()));
				h.put("y", options.convertLengthToView(sourcePosition.getY()));
				
				h.put("sigma_x", options.convertLengthUncertaintyToView(sourcePosition.getUncertaintyX()));
				h.put("sigma_y", options.convertLengthUncertaintyToView(sourcePosition.getUncertaintyY()));
				
				h.put("residual_x", options.convertLengthResidualToView(sourcePosition.getResidualX()));
				h.put("residual_y", options.convertLengthResidualToView(sourcePosition.getResidualY()));
				
				
				h.put("X0", options.convertLengthToView(targetPosition.getX0()));
				h.put("Y0", options.convertLengthToView(targetPosition.getY0()));
				
				h.put("X", options.convertLengthToView(targetPosition.getX()));
				h.put("Y", options.convertLengthToView(targetPosition.getY()));
				
				h.put("sigma_X", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyX()));
				h.put("sigma_Y", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyY()));
				
				h.put("residual_X", options.convertLengthResidualToView(targetPosition.getResidualX()));
				h.put("residual_Y", options.convertLengthResidualToView(targetPosition.getResidualY()));
				
				
				h.put("minimal_detectable_bias_x", options.convertLengthResidualToView(positionPair.getMinimalDetectableBiasX()));
				h.put("minimal_detectable_bias_y", options.convertLengthResidualToView(positionPair.getMinimalDetectableBiasY()));
				
				h.put("maximum_tolerable_bias_x", options.convertLengthResidualToView(positionPair.getMaximumTolerableBiasX()));
				h.put("maximum_tolerable_bias_y", options.convertLengthResidualToView(positionPair.getMaximumTolerableBiasY()));
				
				double grossErrorX = options.convertLengthResidualToView(positionPair.getGrossErrorX());
				double grossErrorY = options.convertLengthResidualToView(positionPair.getGrossErrorY());
				
				double sourceRedundancyX = sourcePosition.getRedundancyX();
				double sourceRedundancyY = sourcePosition.getRedundancyY();
				
				double targetRedundancyX = targetPosition.getRedundancyX();
				double targetRedundancyY = targetPosition.getRedundancyY();
				
				h.put("gross_error_x", grossErrorX);
				h.put("gross_error_y", grossErrorY);
				
				h.put("redundancy_x", options.convertPercentToView(sourceRedundancyX));
				h.put("redundancy_y", options.convertPercentToView(sourceRedundancyY));
				
				h.put("redundancy_X", options.convertPercentToView(targetRedundancyX));
				h.put("redundancy_Y", options.convertPercentToView(targetRedundancyY));
				
				redundancySourcePositionsX += sourceRedundancyX;
				redundancySourcePositionsY += sourceRedundancyY;
				
				redundancyTargetPositionsX += targetRedundancyX;
				redundancyTargetPositionsY += targetRedundancyY;
				
				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
			}
			if (dimension != 2) {
				h.put("z0", options.convertLengthToView(sourcePosition.getZ0()));
				
				h.put("z", options.convertLengthToView(sourcePosition.getZ()));
				
				h.put("sigma_z", options.convertLengthUncertaintyToView(sourcePosition.getUncertaintyZ()));
				
				h.put("residual_z", options.convertLengthResidualToView(sourcePosition.getResidualZ()));
				
				h.put("Z0", options.convertLengthToView(targetPosition.getZ0()));
				
				h.put("Z", options.convertLengthToView(targetPosition.getZ()));
				
				h.put("sigma_Z", options.convertLengthUncertaintyToView(targetPosition.getUncertaintyZ()));
				
				h.put("residual_Z", options.convertLengthResidualToView(targetPosition.getResidualZ()));
				
				h.put("minimal_detectable_bias_z", options.convertLengthResidualToView(positionPair.getMinimalDetectableBiasZ()));
				
				h.put("maximum_tolerable_bias_z", options.convertLengthResidualToView(positionPair.getMaximumTolerableBiasZ()));
				
				double grossErrorZ = options.convertLengthResidualToView(positionPair.getGrossErrorZ());
				
				double sourceRedundancyZ = sourcePosition.getRedundancyZ();
				double targetRedundancyZ = targetPosition.getRedundancyZ();
				
				h.put("gross_error_z", grossErrorZ);
				
				h.put("redundancy_z", options.convertPercentToView(sourceRedundancyZ));
				h.put("redundancy_Z", options.convertPercentToView(targetRedundancyZ));
				
				redundancySourcePositionsZ += sourceRedundancyZ;
				redundancyTargetPositionsZ += targetRedundancyZ;
				maxGrossErrorGroupZ = Math.abs(grossErrorZ) > Math.abs(maxGrossErrorGroupZ) ? grossErrorZ : maxGrossErrorGroupZ;
			}
			
			positionList.add(h);
		}
		
		if (positionList != null && !positionList.isEmpty()) {
			positions.put("positions",    positionList);
			positions.put("significant",  significantGroup);
			positions.put("dimension",    dimension);

			positions.put("redundancy_x",  redundancySourcePositionsX);
			positions.put("redundancy_y",  redundancySourcePositionsY);
			positions.put("redundancy_z",  redundancySourcePositionsZ);
			positions.put("redundancy_xyz",    redundancySourcePositionsX+redundancySourcePositionsY+redundancySourcePositionsZ);
			
			positions.put("redundancy_X",  redundancyTargetPositionsX);
			positions.put("redundancy_Y",  redundancyTargetPositionsY);
			positions.put("redundancy_Z",  redundancyTargetPositionsZ);
			positions.put("redundancy_XYZ",    redundancyTargetPositionsX+redundancyTargetPositionsY+redundancyTargetPositionsZ);

			positions.put("max_gross_error_x", maxGrossErrorGroupX);
			positions.put("max_gross_error_y", maxGrossErrorGroupY);
			positions.put("max_gross_error_z", maxGrossErrorGroupZ);
			
			this.setParam("homologous_position_pairs", positions);
		}
	}
	
	private void addVarianceEstimation() {
		if ( this.adjustment.getTestStatisticParameters() == null)
			return;

		List<Map<String, Object>> vces = new ArrayList<Map<String, Object>>();
		for (VarianceComponentType varianceComponentType : VarianceComponentType.values()) {
			Map<String, Object> vce = new HashMap<String, Object>(6);
			VarianceComponent varianceComponentOfUnitWeight = this.adjustment.getVarianceComponent(varianceComponentType);
			double dof = varianceComponentOfUnitWeight.getRedundancy();
			int numberOfPoints = this.adjustment.getTransformation().getHomologousFramePositionPairs().size();
			int dimension = this.adjustment.getTransformation().getTransformationEquations().getTransformationType().getDimension();
			double sigma2apost = varianceComponentOfUnitWeight.getVariance() / varianceComponentOfUnitWeight.getVariance0();
			double omega = varianceComponentOfUnitWeight.getOmega() / varianceComponentOfUnitWeight.getVariance0();
			boolean significant = varianceComponentOfUnitWeight.isSignificant();
			double quantile = this.adjustment.getTestStatisticParameters().getTestStatisticParameter(dof > 0.000001 ? dof : 0, Double.POSITIVE_INFINITY).getQuantile();

			vce.put("type",                    varianceComponentType.name());
			vce.put("omega",                   omega);
			vce.put("number_of_observations",  dimension * numberOfPoints);
			vce.put("redundancy",              dof);
			vce.put("sigma2apost",             sigma2apost);
			vce.put("quantile",                quantile);
			vce.put("significant",             significant);

			vces.add(vce);
		}
		if (vces.size() > 0)
			this.setParam("vce", vces); 
	}

	public static List<File> getTemplates() {
		File root = null;
		try {
			root = new File(FTLReport.class.getClassLoader().getResource(FTLReport.TEMPLATE_PATH).toURI());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		File[] files = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ftlh");
			}
		});

		if (files == null)
			return null;
		List<File> templates = new ArrayList<File>(files.length);
		for (int i=0; i<files.length; i++) {
			if (files[i].exists() && files[i].isFile() && files[i].canRead()) {
				templates.add(files[i]);
			}
		}
		return templates;
	}
}
