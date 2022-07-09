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

package org.applied_geodesy.juniform.io.report;

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

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.VarianceComponent;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
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
	private final static Version VERSION = Configuration.VERSION_2_3_31;
	private FormatterOptions options = FormatterOptions.getInstance();
	private Template template = null;
	private static HostServices hostServices;
	private Map<String, Object> data = new HashMap<String, Object>();
	public final static String TEMPLATE_PATH = "ftl/juniform/";
	private final Configuration cfg = new Configuration(VERSION);
	private FeatureAdjustment adjustment;
	public FTLReport(FeatureAdjustment adjustment) {
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
		
		this.addGeometricPrimitives();
		this.addTeststatistics();
		this.addVarianceEstimation();
		
		this.addPoints();
		this.addUnknownFeatureParameters();
		this.addCorrelationMatrix();
	}

	public String getSuggestedFileName() {
		return this.adjustment.getFeature() != null ? this.adjustment.getFeature().getFeatureType().name() : null;
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
	}
	
	private void addGeometricPrimitives() {
		Feature feature = this.adjustment.getFeature();
		List<HashMap<String, Object>> geometries = new ArrayList<HashMap<String, Object>>();
		for (GeometricPrimitive geometricPrimitive : feature) {
			HashMap<String, Object> geometry = new HashMap<String, Object>();
			geometry.put("name", geometricPrimitive.getName());
			geometry.put("type", geometricPrimitive.getPrimitiveType().name());
			geometry.put("unknown_parameters", this.getUnknownParameters());
			geometries.add(geometry);
		}
		this.setParam("geometric_primitives", geometries);
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
	
	private void addUnknownFeatureParameters() {
		this.setParam("unknown_feature_parameters", this.getUnknownParameters());
	}

	private void addCorrelationMatrix() {
		Matrix correlationMatrix = this.adjustment.getCorrelationMatrix();
		
		if (correlationMatrix == null)
			return;
		
		List<HashMap<String, Object>> matrix = new ArrayList<HashMap<String, Object>>();
		List<UnknownParameter> unknownParameters = this.adjustment.getFeature().getUnknownParameters();
		
		int cols = correlationMatrix.numColumns();
		int rows = correlationMatrix.numRows();
		
		for (int r = 0; r < rows; r++) {
			HashMap<String, Object> row = new HashMap<String, Object>(2);
			List<Double> rowVec = new ArrayList<Double>(rows);
			for (int c = 0; c < cols; c++) {
				rowVec.add(correlationMatrix.get(r, c));
			}
			
			row.put("parameter", this.getUnknownParameter(unknownParameters.get(r)));
			row.put("data", rowVec);
			
			matrix.add(row);
		}

		this.setParam("correlation_matrix", matrix);
	}
	
	private List<HashMap<String, Object>> getUnknownParameters() {
		List<HashMap<String, Object>> parameters = new ArrayList<HashMap<String, Object>>();
		List<UnknownParameter> unknownParameters = this.adjustment.getFeature().getUnknownParameters();
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
		case COORDINATE_X:
		case COORDINATE_Y:
		case COORDINATE_Z:
		case LENGTH:
		case RADIUS:
		case ORIGIN_COORDINATE_X:
		case ORIGIN_COORDINATE_Y:
		case ORIGIN_COORDINATE_Z:
		case PRIMARY_FOCAL_COORDINATE_X:
		case PRIMARY_FOCAL_COORDINATE_Y:
		case PRIMARY_FOCAL_COORDINATE_Z:
		case SECONDARY_FOCAL_COORDINATE_X:
		case SECONDARY_FOCAL_COORDINATE_Y:
		case SECONDARY_FOCAL_COORDINATE_Z:
			parameter.put("value", options.convertLengthToView(value));
			parameter.put("sigma", options.convertLengthUncertaintyToView(sigma));
			parameter.put("unit_type", "LENGTH");
			break;
		case VECTOR_LENGTH:
		case VECTOR_X:
		case VECTOR_Y:
		case VECTOR_Z:
			parameter.put("value", options.convertVectorToView(value));
			parameter.put("sigma", options.convertVectorUncertaintyToView(sigma));
			parameter.put("unit_type", "VECTOR");
			break;
			
		case ANGLE:
			parameter.put("value", options.convertAngleToView(value));
			parameter.put("sigma", options.convertAngleUncertaintyToView(sigma));
			parameter.put("unit_type", "ANGLE");
			break;

		default:
			parameter.put("value", value);
			parameter.put("sigma", sigma);
			parameter.put("unit_type", "DOUBLE");
			break;
		}

		return parameter;
	}
	
	private void addPoints() {
		HashMap<String, Object> points = new HashMap<String, Object>();
		List<HashMap<String, Object>> pointList = new ArrayList<HashMap<String, Object>>();
		List<FeaturePoint> featurePoints = this.adjustment.getFeature().getFeaturePoints();
		
		int dimension = -1;
		double redundancyGroupX = 0;
		double redundancyGroupY = 0;
		double redundancyGroupZ = 0;
		
		double maxGrossErrorGroupX = 0;
		double maxGrossErrorGroupY = 0;
		double maxGrossErrorGroupZ = 0;
		
		boolean significantGroup = false;
		
		for (FeaturePoint point : featurePoints) {
			HashMap<String, Object> h = new HashMap<String, Object>();
			dimension = point.getDimension();
			boolean significant = point.isSignificant();
			
			if (!significantGroup && significant)
				significantGroup = true;
			
			h.put("name",        point.getName());
			
			h.put("t_prio",      point.getTestStatistic().getTestStatisticApriori());
			h.put("t_post",      point.getTestStatistic().getTestStatisticAposteriori());
			
			h.put("p_prio",      point.getTestStatistic().getPValueApriori());
			h.put("p_post",      point.getTestStatistic().getPValueAposteriori());
			
			h.put("significant", significant);
			
			h.put("dimension",   dimension);
			
			
			if (point.getDimension() != 1) {
				h.put("x0", options.convertLengthToView(point.getX0()));
				h.put("y0", options.convertLengthToView(point.getY0()));
				
				h.put("x", options.convertLengthToView(point.getX()));
				h.put("y", options.convertLengthToView(point.getY()));
				
				h.put("sigma_x", options.convertLengthUncertaintyToView(point.getUncertaintyX()));
				h.put("sigma_y", options.convertLengthUncertaintyToView(point.getUncertaintyY()));
				
				h.put("residual_x", options.convertLengthResidualToView(point.getResidualX()));
				h.put("residual_y", options.convertLengthResidualToView(point.getResidualY()));
				
				h.put("minimal_detectable_bias_x", options.convertLengthResidualToView(point.getMinimalDetectableBiasX()));
				h.put("minimal_detectable_bias_y", options.convertLengthResidualToView(point.getMinimalDetectableBiasY()));
				
				h.put("maximum_tolerable_bias_x", options.convertLengthResidualToView(point.getMaximumTolerableBiasX()));
				h.put("maximum_tolerable_bias_y", options.convertLengthResidualToView(point.getMaximumTolerableBiasY()));
				
				double grossErrorX = options.convertLengthResidualToView(point.getGrossErrorX());
				double grossErrorY = options.convertLengthResidualToView(point.getGrossErrorY());
				
				double redundancyX = point.getRedundancyX();
				double redundancyY = point.getRedundancyY();
				
				h.put("gross_error_x", grossErrorX);
				h.put("gross_error_y", grossErrorY);
				
				h.put("redundancy_x", options.convertPercentToView(redundancyX));
				h.put("redundancy_y", options.convertPercentToView(redundancyY));
				
				redundancyGroupX += redundancyX;
				redundancyGroupY += redundancyY;
				
				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
				
				
			}
			if (point.getDimension() != 2) {
				h.put("z0", options.convertLengthToView(point.getZ0()));
				
				h.put("z", options.convertLengthToView(point.getZ()));
				
				h.put("sigma_z", options.convertLengthUncertaintyToView(point.getUncertaintyZ()));
				
				h.put("residual_z", options.convertLengthResidualToView(point.getResidualZ()));
				
				h.put("minimal_detectable_bias_z", options.convertLengthResidualToView(point.getMinimalDetectableBiasZ()));
				
				h.put("maximum_tolerable_bias_z", options.convertLengthResidualToView(point.getMaximumTolerableBiasZ()));
				
				double grossErrorZ = options.convertLengthResidualToView(point.getGrossErrorZ());
				
				double redundancyZ = point.getRedundancyZ();
				
				h.put("gross_error_z", grossErrorZ);
				
				h.put("redundancy_z", options.convertPercentToView(redundancyZ));
				
				redundancyGroupZ += redundancyZ;
				maxGrossErrorGroupZ = Math.abs(grossErrorZ) > Math.abs(maxGrossErrorGroupZ) ? grossErrorZ : maxGrossErrorGroupZ;
			}
			
			pointList.add(h);
		}
		
		if (pointList != null && !pointList.isEmpty()) {
			points.put("points",       pointList);
			points.put("significant",  significantGroup);
			points.put("dimension",    dimension);

			points.put("redundancy_x",  redundancyGroupX);
			points.put("redundancy_y",  redundancyGroupY);
			points.put("redundancy_z",  redundancyGroupZ);
			points.put("redundancy",    redundancyGroupX+redundancyGroupY+redundancyGroupZ);

			points.put("max_gross_error_x", maxGrossErrorGroupX);
			points.put("max_gross_error_y", maxGrossErrorGroupY);
			points.put("max_gross_error_z", maxGrossErrorGroupZ);
			
			this.setParam("feature_points", points);
		}
	}
	
	private void addVarianceEstimation() {
		if ( this.adjustment.getTestStatisticParameters() == null)
			return;
		
		List<Map<String, Object>> vces = new ArrayList<Map<String, Object>>();
		Map<String, Object> vce = new HashMap<String, Object>(6);
		VarianceComponent varianceComponentOfUnitWeight = this.adjustment.getVarianceComponentOfUnitWeight();
		int dof = (int)varianceComponentOfUnitWeight.getRedundancy();
		int numberOfPoints = this.adjustment.getFeature().getFeaturePoints().size();
		int dim = this.adjustment.getFeature().getFeatureType() == FeatureType.CURVE ? 2 : 3;
		double sigma2apost = varianceComponentOfUnitWeight.getVariance() / varianceComponentOfUnitWeight.getVariance0();
		double omega = varianceComponentOfUnitWeight.getOmega() / varianceComponentOfUnitWeight.getVariance0();
		boolean significant = varianceComponentOfUnitWeight.isSignificant();
		double quantile = this.adjustment.getTestStatisticParameters().getTestStatisticParameter(dof > 0.000001 ? dof : 0, Double.POSITIVE_INFINITY).getQuantile();
		
		vce.put("type",                    "GLOBAL");
		vce.put("omega",                   omega);
		vce.put("number_of_observations",  dim * numberOfPoints);
		vce.put("redundancy",              dof);
		vce.put("sigma2apost",             sigma2apost);
		vce.put("quantile",                quantile);
		vce.put("significant",             significant);
		
		vces.add(vce);

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
