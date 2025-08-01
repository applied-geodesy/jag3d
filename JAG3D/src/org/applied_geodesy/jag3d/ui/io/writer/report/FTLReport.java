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

package org.applied_geodesy.jag3d.ui.io.writer.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.ProjectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.ReductionTaskType;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.transformation.datum.Ellipsoid;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.FormatterOptions.FormatterOption;
import org.applied_geodesy.util.sql.DataBase;
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

public class FTLReport {
	private final static Version VERSION = Configuration.VERSION_2_3_33;
	private FormatterOptions options = FormatterOptions.getInstance();
	private Template template = null;
	private final DataBase dataBase;
	private final HostServices hostServices;
	private Map<String, Object> data = new HashMap<String, Object>();
	public final static String TEMPLATE_PATH = "ftl/jag3d/";
	private final Configuration cfg = new Configuration(VERSION);

	public FTLReport(DataBase dataBase, HostServices hostServices) {
		if (dataBase == null || !dataBase.isOpen())
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database must be open! " + dataBase);
		this.dataBase = dataBase;
		this.hostServices = hostServices;
		this.init();
	}

	private void setDataBaseSchema() throws SQLException {
		this.dataBase.getPreparedStatement("SET SCHEMA \"OpenAdjustment\"").execute();
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

	private void setParam(String key, Object value) {
		this.data.put(key, value);
	}

	public void setTemplate(String template) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		this.template = this.cfg.getTemplate(template);
	}

	private void createReport() throws ClassNotFoundException, SQLException, TemplateModelException {
		this.data.clear();
		BeansWrapper wrapper = new BeansWrapperBuilder(VERSION).build();
		TemplateHashModel staticModels = wrapper.getStaticModels();
		TemplateHashModel mathStatics = (TemplateHashModel) staticModels.get("java.lang.Math");
		this.data.put("Math", mathStatics);

		this.setDataBaseSchema();
		this.initFormatterOptions();
		this.addAdjustmentDefinitions(); 
		
		this.addVersion();
		this.addMetaData();
		
		this.addRankDefect();
		this.addProjectionAndReductions();
		
		this.addPrincipalComponent();
		this.addTeststatistics();
		this.addVarianceComponents();
		
		this.addPointGroups();
		this.addObservations();
		this.addCongruenceAnalysis();
		this.addVerticalDefelctionGroups();
		
		this.addChartAndStatisticValues();
		this.addLayerProperties();
		this.addVectorScale();
	}

	public String getSuggestedFileName() {
		return this.dataBase != null ? this.dataBase.getURI() : null;
	}

	public void toFile(File report, boolean openFile) throws ClassNotFoundException, SQLException, TemplateException, IOException {
		if (report != null) {
			this.createReport();
			Writer file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report), StandardCharsets.UTF_8));
			this.template.process(this.data, file);
			file.flush();
			file.close();

			if (this.hostServices != null && openFile)
				this.hostServices.showDocument(report.getAbsolutePath());
		}
	}


	/******** Datenbankabfragen *************/

	private void initFormatterOptions() throws SQLException {
		FormatterOptions options = FormatterOptions.getInstance();
		Map<CellValueType, FormatterOption> formatterOptions = options.getFormatterOptions();

		for (FormatterOption option : formatterOptions.values()) {
			String keyUnitType       = null;
			String keyUnitConversion = null;
			String keyUnitDigits     = null;
			String keyUnitAbbr       = null;
			String keySexagesimal    = null;
			double conversionFactor  = 1.0;
			CellValueType cellValueType = option.getType();
			switch(cellValueType) {
			case ANGLE:
				keyUnitType       = "unit_type_angle";
				keyUnitConversion = "unit_conversion_angle";
				keyUnitDigits     = "digits_angle";
				keyUnitAbbr       = "unit_abbr_angle";
				conversionFactor  = options.convertAngleToModel(1.0);
				keySexagesimal    = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle" : null;
				break;
			case ANGLE_RESIDUAL:
				keyUnitType       = "unit_type_angle_residual";
				keyUnitConversion = "unit_conversion_angle_residual";
				keyUnitDigits     = "digits_angle_residual";
				keyUnitAbbr       = "unit_abbr_angle_residual";
				conversionFactor  = options.convertAngleResidualToModel(1.0);
				keySexagesimal    = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle_residual" : null;
				break;
			case ANGLE_UNCERTAINTY:
				keyUnitType       = "unit_type_angle_uncertainty";
				keyUnitConversion = "unit_conversion_angle_uncertainty";
				keyUnitDigits     = "digits_angle_uncertainty";
				keyUnitAbbr       = "unit_abbr_angle_uncertainty";
				conversionFactor  = options.convertAngleUncertaintyToModel(1.0);
				keySexagesimal    = option.getUnit().getType() == UnitType.DEGREE_SEXAGESIMAL ? "sexagesimal_angle_uncertainty" : null;
				break;

			case LENGTH:
				keyUnitType       = "unit_type_length";
				keyUnitConversion = "unit_conversion_length";
				keyUnitDigits     = "digits_length";
				keyUnitAbbr       = "unit_abbr_length";
				conversionFactor  = options.convertLengthToModel(1.0);
				break;
			case LENGTH_RESIDUAL:
				keyUnitType       = "unit_type_length_residual";
				keyUnitConversion = "unit_conversion_length_residual";
				keyUnitDigits     = "digits_length_residual";
				keyUnitAbbr       = "unit_abbr_length_residual";
				conversionFactor  = options.convertLengthResidualToModel(1.0);
				break;
			case LENGTH_UNCERTAINTY:
				keyUnitType       = "unit_type_length_uncertainty";
				keyUnitConversion = "unit_conversion_length_uncertainty";
				keyUnitDigits     = "digits_length_uncertainty";
				keyUnitAbbr       = "unit_abbr_length_uncertainty";
				conversionFactor  = options.convertLengthUncertaintyToModel(1.0);
				break;

			case SCALE:
				keyUnitType       = "unit_type_scale";
				keyUnitConversion = "unit_conversion_scale";
				keyUnitDigits     = "digits_scale";
				keyUnitAbbr       = "unit_abbr_scale";
				conversionFactor  = options.convertScaleToModel(1.0);
				break;
			case SCALE_RESIDUAL:
				keyUnitType       = "unit_type_scale_residual";
				keyUnitConversion = "unit_conversion_scale_residual";
				keyUnitDigits     = "digits_scale_residual";
				keyUnitAbbr       = "unit_abbr_scale_residual";
				conversionFactor  = options.convertScaleResidualToModel(1.0);
				break;
			case SCALE_UNCERTAINTY:
				keyUnitType       = "unit_type_scale_uncertainty";
				keyUnitConversion = "unit_conversion_scale_uncertainty";
				keyUnitDigits     = "digits_scale_uncertainty";
				keyUnitAbbr       = "unit_abbr_scale_uncertainty";
				conversionFactor  = options.convertScaleUncertaintyToModel(1.0);
				break;
				
			case PERCENTAGE:
				keyUnitType       = "unit_type_percentage";
				keyUnitConversion = "unit_conversion_percentage";
				keyUnitDigits     = "digits_percentage";
				keyUnitAbbr       = "unit_abbr_percentage";
				conversionFactor  = options.convertPercentToModel(1.0);
				break;

			case STATISTIC:
				keyUnitDigits     = "digits_statistic";
				keyUnitConversion = "unit_conversion_statistic";
				conversionFactor  = 1.0;
				break;

			default:
				continue;
				// break;
			}

			if (keyUnitType != null && option.getUnit() != null)
				this.setParam(keyUnitType,       option.getUnit().getType().name() );
			if (keyUnitConversion != null)
				this.setParam(keyUnitConversion, conversionFactor );
			if (keyUnitDigits != null)
				this.setParam(keyUnitDigits,     option.getFormatter().format(0.0) );
			if (keyUnitAbbr != null && option.getUnit() != null)
				this.setParam(keyUnitAbbr,       option.getUnit().getAbbreviation() );
			if (keySexagesimal != null)
				this.setParam(keySexagesimal,    Boolean.TRUE );
		}
	}

	private void addAdjustmentDefinitions() throws SQLException {
		String sql = "SELECT "
				+ "\"type\", \"number_of_iterations\", "
				+ "\"robust_estimation_limit\", \"estimate_direction_set_orientation_approximation\", "
				+ "\"congruence_analysis\", \"confidence_level\" "
				+ "FROM \"AdjustmentDefinition\" WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			EstimationType estimationType = EstimationType.getEnumByValue(rs.getInt("type"));
			if (estimationType == null)
				estimationType = EstimationType.L2NORM;

			this.setParam("estimation_type", estimationType.name()); // L1NORM, L2NORM or SIMULATION	
			this.setParam("robust_estimation_limit", rs.getDouble("robust_estimation_limit"));
			this.setParam("congruence_analysis", rs.getBoolean("congruence_analysis"));
			this.setParam("number_of_iterations", rs.getBoolean("number_of_iterations"));
			this.setParam("confidence_level", options.convertPercentToView(rs.getDouble("confidence_level")));

		}
	}

	private void addVersion() throws SQLException {
		String sql = "SELECT "
				+ "MAX(\"version\") AS \"version\" "
				+ "FROM \"Version\"";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			this.setParam("version", rs.getInt("version"));
		}	
	}
	
	private void addMetaData() throws SQLException {
		String sql = "SELECT "
				+ "\"name\", \"operator\", "
				+ "\"description\", \"date\", "
				+ "\"customer_id\", \"project_id\" "
				+ "FROM \"ProjectMetadata\" WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			this.setParam("project_name",        rs.getString("name"));
			this.setParam("project_operator",    rs.getString("operator"));
			this.setParam("project_description", rs.getString("description"));
			this.setParam("project_customer_id", rs.getString("customer_id"));
			this.setParam("project_project_id",  rs.getString("project_id"));
			this.setParam("project_date",        new Date(rs.getTimestamp("date").getTime()));
		}
		
		this.setParam("report_creation_date",  new Date(System.currentTimeMillis()));
	}

	private void addPrincipalComponent() throws SQLException {
		List<HashMap<String, Number>> principalComponents = new ArrayList<HashMap<String, Number>>();
		String sql = "SELECT \"index\", SQRT(ABS(\"value\")) AS \"value\", \"ratio\" FROM \"PrincipalComponent\" ORDER BY \"index\" DESC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			HashMap<String, Number> component = new HashMap<String, Number>();
			int index    = rs.getInt("index");
			double value = options.convertLengthResidualToView(rs.getDouble("value"));
			double ratio = options.convertPercentToView(rs.getDouble("ratio"));
			component.put("index", index);
			component.put("value", value);
			component.put("ratio", ratio);
			principalComponents.add(component);
		}
		
		if (!principalComponents.isEmpty())
			this.setParam("principal_components",  principalComponents);
	} 

	private void addProjectionAndReductions() throws SQLException {
		String sql = "SELECT "
				+ "\"projection_type\", "
				+ "\"reference_latitude\", \"reference_longitude\", \"reference_height\", "
				+ "\"major_axis\", \"minor_axis\", "
				+ "\"x0\", \"y0\", \"z0\", \"type\" AS \"task_type\" "
				+ "FROM \"ReductionTask\" "
				+ "RIGHT JOIN \"ReductionDefinition\" ON \"ReductionTask\".\"reduction_id\" = \"ReductionDefinition\".\"id\" "
				+ "WHERE \"ReductionDefinition\".\"id\" = 1";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			ProjectionType projectionType = ProjectionType.getEnumByValue(rs.getInt("projection_type"));
			ReductionTaskType taskType    = ReductionTaskType.getEnumByValue(rs.getInt("task_type"));
			double referenceLatitude      = rs.getDouble("reference_latitude");
			double referenceLongitude     = rs.getDouble("reference_longitude");
			double referenceHeight        = rs.getDouble("reference_height");
			double majorAxis              = rs.getDouble("major_axis");
			double minorAxis              = rs.getDouble("minor_axis");
			double principalPointX0       = rs.getDouble("x0");
			double principalPointY0       = rs.getDouble("y0");
			double principalPointZ0       = rs.getDouble("z0");

			// set default value
			this.setParam("projection_type",   ProjectionType.LOCAL_CARTESIAN);
			
			if (projectionType == ProjectionType.LOCAL_ELLIPSOIDAL) {
				this.setParam("projection_type",                projectionType.name());
				this.setParam("projection_reference_latitude",  options.convertAngleToView(referenceLatitude));
				this.setParam("projection_reference_longitude", options.convertAngleToView(referenceLongitude));
				this.setParam("projection_reference_height",    options.convertLengthToView(referenceHeight));
				this.setParam("projection_major_axis",          options.convertLengthToView(majorAxis));
				this.setParam("projection_minor_axis",          options.convertLengthToView(minorAxis));
				this.setParam("projection_principal_point_x0",  options.convertLengthToView(principalPointX0));
				this.setParam("projection_principal_point_y0",  options.convertLengthToView(principalPointY0));
				this.setParam("projection_principal_point_z0",  options.convertLengthToView(principalPointZ0));
			}
			else {			
				if (taskType == null) 
					continue;

				this.setParam("projection_type",               projectionType.name());
				this.setParam("projection_reference_height",   options.convertLengthToView(referenceHeight));
				this.setParam("projection_reference_latitude", options.convertAngleToView(referenceLatitude));
				this.setParam("projection_earth_radius",       options.convertLengthToView(Ellipsoid.createEllipsoidFromMinorAxis(majorAxis, minorAxis).getRadiusOfConformalSphere(referenceLatitude)));

				switch(taskType) {
				case DIRECTION:
					this.setParam("reduction_direction", projectionType != ProjectionType.LOCAL_CARTESIAN);
					break;
				case DISTANCE:
					this.setParam("reduction_distance", projectionType != ProjectionType.LOCAL_CARTESIAN);
					break;
				case EARTH_CURVATURE:
					this.setParam("reduction_earth_curvature", Boolean.TRUE);
					break;
				case HEIGHT:
					this.setParam("reduction_height", Boolean.TRUE);
					break;			
				}
			}
		} 
	}

	private void addRankDefect() throws SQLException {
		String sql = "SELECT \"ty\",\"tx\",\"tz\",\"ry\",\"rx\",\"rz\",\"sy\",\"sx\",\"sz\",\"my\",\"mx\",\"mz\",\"mxy\",\"mxyz\" FROM \"RankDefect\" WHERE \"id\" = 1 LIMIT 1";
		Map<String, Boolean> defects = new HashMap<String, Boolean>(14);
		int count = 0;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cnt = rsmd.getColumnCount();
			for (int i=1; i<=cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				boolean defect = rs.getBoolean(key);
				defects.put(key, defect);
				count += defect ? 1 : 0;
			}
			this.setParam("rank_defect", defects);
		}

		if (count > 0)
			this.setParam("rank_defect_count", count);	
	}

	private void addTeststatistics() throws SQLException {
		String sqlDefinition = "SELECT \"type\", \"probability_value\", \"power_of_test\" FROM \"TestStatisticDefinition\" WHERE \"id\" = 1 LIMIT 1";

		String sqlTestStatistic = "SELECT "
				+ "ABS(\"d1\") AS \"d1\", ABS(\"d2\") AS \"d2\","
				+ "\"probability_value\",\"power_of_test\","
				+ "\"quantile\",\"non_centrality_parameter\",\"p_value\" "
				+ "FROM \"TestStatistic\" "
				+ "ORDER BY ABS(\"d1\") ASC, ABS(\"d2\") DESC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sqlDefinition);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			TestStatisticType type = TestStatisticType.getEnumByValue(rs.getInt("type"));
			double probabilityValue = options.convertPercentToView(rs.getDouble("probability_value"));
			double powerOfTest      = options.convertPercentToView(rs.getDouble("power_of_test"));
			List<HashMap<String, Number>> testStatistics = new ArrayList<HashMap<String, Number>>();
			stmt = this.dataBase.getPreparedStatement(sqlTestStatistic);
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int cnt = rsmd.getColumnCount();
			while (rs.next()) {
				HashMap<String, Number> h = new HashMap<String, Number>();
				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {
					case "probability_value":
					case "power_of_test":
						h.put(key, options.convertPercentToView(rs.getDouble(i)) );
						break;
					default:
						h.put(key, rs.getDouble(i) );
						break;
					}
				}					
				testStatistics.add(h);
			}
			this.setParam("test_statistic_method", type.name());
			this.setParam("test_statistic_probability_value", probabilityValue);
			this.setParam("test_statistic_power_of_test", powerOfTest);

			if (!testStatistics.isEmpty()) 
				this.setParam("test_statistic_params", testStatistics);
		}
	}

	private void addVarianceComponents() throws SQLException {
		List<Map<String, Object>> varianceComponents = new ArrayList<Map<String, Object>>();

		String sql = "SELECT "
				+ "\"type\",\"redundancy\",\"omega\",\"sigma2apost\",\"number_of_observations\", \"quantile\", "
				+ "\"sigma2apost\" > \"quantile\" AS \"significant\" "
				+ "FROM \"VarianceComponent\" "
				+ "JOIN \"TestStatistic\" ON \"VarianceComponent\".\"redundancy\" = \"TestStatistic\".\"d1\" "
				+ "WHERE \"redundancy\" > 0 "
				+ "AND \"d2\" + 1 = \"d2\" " // Workaround to select Infinity-Values
				+ "ORDER BY \"type\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cnt = rsmd.getColumnCount();

		while (rs.next()) {
			VarianceComponentType varianceComponentType = VarianceComponentType.getEnumByValue(rs.getInt("type"));
			if (varianceComponentType == null)
				continue;

			Map<String, Object> varianceComponent = new HashMap<String, Object>(6);
			for (int i=1; i<=cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				int type = rsmd.getColumnType(i);
				if(type == Types.INTEGER)
					varianceComponent.put(key, rs.getInt(key));
				else if(type == Types.DOUBLE)
					varianceComponent.put(key, rs.getDouble(key));	
				else if(type == Types.BOOLEAN)
					varianceComponent.put(key, rs.getBoolean(key));
				else if(type == Types.VARCHAR)
					varianceComponent.put(key, rs.getString(key));	
			}
			varianceComponent.put("type", varianceComponentType.name());
			varianceComponents.add(varianceComponent);
		}
		if (!varianceComponents.isEmpty())
			this.setParam("vce", varianceComponents); 

	}

	private void addPointGroups() throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		groups.addAll(this.getPointGroups(1, PointType.REFERENCE_POINT));
		groups.addAll(this.getPointGroups(2, PointType.REFERENCE_POINT));
		groups.addAll(this.getPointGroups(3, PointType.REFERENCE_POINT));

		groups.addAll(this.getPointGroups(1, PointType.STOCHASTIC_POINT));
		groups.addAll(this.getPointGroups(2, PointType.STOCHASTIC_POINT));
		groups.addAll(this.getPointGroups(3, PointType.STOCHASTIC_POINT));

		groups.addAll(this.getPointGroups(1, PointType.DATUM_POINT));
		groups.addAll(this.getPointGroups(2, PointType.DATUM_POINT));
		groups.addAll(this.getPointGroups(3, PointType.DATUM_POINT));

		groups.addAll(this.getPointGroups(1, PointType.NEW_POINT));
		groups.addAll(this.getPointGroups(2, PointType.NEW_POINT));
		groups.addAll(this.getPointGroups(3, PointType.NEW_POINT));

		this.setParam("point_groups", groups);
	}
	
	private void addVerticalDefelctionGroups() throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();
		
		groups.addAll(this.getVerticalDeflectionGroups(VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION));
		groups.addAll(this.getVerticalDeflectionGroups(VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION));
		groups.addAll(this.getVerticalDeflectionGroups(VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION));
		
		this.setParam("vertical_deflection_groups", groups);
	}

	private void addObservations() throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		groups.addAll(this.getObservationGroups(ObservationType.LEVELING));

		groups.addAll(this.getObservationGroups(ObservationType.DIRECTION));
		groups.addAll(this.getObservationGroups(ObservationType.HORIZONTAL_DISTANCE));

		groups.addAll(this.getObservationGroups(ObservationType.SLOPE_DISTANCE));
		groups.addAll(this.getObservationGroups(ObservationType.ZENITH_ANGLE));

		groups.addAll(this.getGNSSObservationGroups(ObservationType.GNSS1D));
		groups.addAll(this.getGNSSObservationGroups(ObservationType.GNSS2D));
		groups.addAll(this.getGNSSObservationGroups(ObservationType.GNSS3D));

		this.setParam("observation_groups", groups);
	}

	private void addCongruenceAnalysis() throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		groups.addAll(this.getCongruenceAnalysisPointPairGroups(1));
		groups.addAll(this.getCongruenceAnalysisPointPairGroups(2));
		groups.addAll(this.getCongruenceAnalysisPointPairGroups(3));

		this.setParam("congruence_analysis_groups", groups);
	}
	
	private void addChartAndStatisticValues() throws SQLException {
		ObservationType[] terrestrialObservationTypes = new ObservationType[] {
				ObservationType.LEVELING,
				ObservationType.DIRECTION,
				ObservationType.HORIZONTAL_DISTANCE,
				ObservationType.SLOPE_DISTANCE,
				ObservationType.ZENITH_ANGLE
		};
		
		HashMap<String, ArrayList<HashMap<String, Object>>> chartValues = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		HashMap<String, ArrayList<HashMap<String, Object>>> reliabilitySummary = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		chartValues.put("redundancy",            new ArrayList<HashMap<String, Object>>(10));
		chartValues.put("p_prio",                new ArrayList<HashMap<String, Object>>(10));
		chartValues.put("influence_on_position", new ArrayList<HashMap<String, Object>>(10));
		
		reliabilitySummary.put("redundancy",            new ArrayList<HashMap<String, Object>>(10));
		reliabilitySummary.put("p_prio",                new ArrayList<HashMap<String, Object>>(10));
		reliabilitySummary.put("influence_on_position", new ArrayList<HashMap<String, Object>>(10));
		
		for (ObservationType observationType : terrestrialObservationTypes) {
			chartValues.get("redundancy").add(this.getRelativeAndAbsoluteFrequency(observationType, TableRowHighlightType.REDUNDANCY));
			chartValues.get("p_prio").add(this.getRelativeAndAbsoluteFrequency(observationType, TableRowHighlightType.P_PRIO_VALUE));
			chartValues.get("influence_on_position").add(this.getRelativeAndAbsoluteFrequency(observationType, TableRowHighlightType.INFLUENCE_ON_POSITION));
			
			reliabilitySummary.get("redundancy").addAll(this.getReliabilitySummary(observationType, TableRowHighlightType.REDUNDANCY));
			reliabilitySummary.get("p_prio").addAll(this.getReliabilitySummary(observationType, TableRowHighlightType.P_PRIO_VALUE));
			reliabilitySummary.get("influence_on_position").addAll(this.getReliabilitySummary(observationType, TableRowHighlightType.INFLUENCE_ON_POSITION));
		}
		
		this.setParam("chart_values", chartValues);
		this.setParam("reliability_summary", reliabilitySummary);
	}
	
	private void addVectorScale() throws SQLException {
		String sql = "SELECT "
				+ "\"value\" "
				+ "FROM \"LayerEllipseScale\" WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		this.setParam("vector_scale", rs.next() ? rs.getDouble("value") : LayerManager.DEFAULT_ELLIPSE_SCALE);
	}
	
	private void addLayerProperties() throws SQLException {
		LinkedHashMap<String, HashMap<String, Object>> layers = new LinkedHashMap<String, HashMap<String, Object>>();
		
		String sql = "SELECT "
				+ "\"type\", \"visible\", \"order\" "
				+ "FROM \"Layer\" "
				+ "ORDER BY \"order\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			LayerType layerType = LayerType.getEnumByValue(rs.getInt("type"));
			if (layerType == null)
				continue;
			
			boolean visible = rs.getBoolean("visible") && !rs.wasNull();
			int order = rs.getInt("order");
			
			HashMap<String, Object> layer = new HashMap<String, Object>();
			layer.put("visible", visible);
			layer.put("order",   order);

			layers.put(layerType.name(), layer);
		}
		
		this.setParam("layers", layers);
	}
	
	private boolean isPointDimensionVisibility(PointType pointType, int dim) throws SQLException {
		LayerType layerType = null;
		
		if (pointType == PointType.REFERENCE_POINT)
			layerType = LayerType.REFERENCE_POINT_APOSTERIORI;
		else if (pointType == PointType.STOCHASTIC_POINT)
			layerType = LayerType.STOCHASTIC_POINT_APOSTERIORI;
		else if (pointType == PointType.DATUM_POINT)
			layerType = LayerType.DATUM_POINT_APOSTERIORI;
		else if (pointType == PointType.NEW_POINT)
			layerType = LayerType.NEW_POINT_APOSTERIORI;
		
		if (layerType != null) {
			String sql = "SELECT "
					+ "\"point_1d_visible\", \"point_2d_visible\", \"point_3d_visible\" "
					+ "FROM \"PointLayerProperty\" WHERE \"layer\" = ? LIMIT 1";

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			stmt.setInt(1, layerType.getId());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				if (dim == 1)
					return rs.getBoolean("point_1d_visible");
				else if (dim == 2)
					return rs.getBoolean("point_2d_visible");
				else if (dim == 3)
					return rs.getBoolean("point_3d_visible");
			}
		}
		return false;
	}
	
	private List<HashMap<String, Object>> getReliabilitySummary(ObservationType observationType, TableRowHighlightType tableRowHighlightType) throws SQLException {
		List<HashMap<String, Object>> summary = new ArrayList<HashMap<String, Object>>();
		
		Map<TableRowHighlightType, String[]> subQueryParts = Map.of(
				TableRowHighlightType.REDUNDANCY, new String[] {
						"\"redundancy\"",
						"\"redundancy\"",
						"MIN(\"redundancy\")",
						"AVG(\"redundancy\")",
						"\"redundancy\" > 0"
				}, 
				
				TableRowHighlightType.P_PRIO_VALUE, new String[] {
						"\"p_prio\"",
						"\"p_prio\"",
						"MIN(\"p_prio\")",
						"AVG(\"p_prio\")",
						""
				}, 
				TableRowHighlightType.INFLUENCE_ON_POSITION, new String[] {
						"\"influence_on_position\"",
						"ABS(\"influence_on_position\")",
						"MAX(ABS(\"influence_on_position\"))",
						"AVG(ABS(\"influence_on_position\"))",
						""
				}
		);
		
		if (!subQueryParts.containsKey(tableRowHighlightType))
			return summary;
		
		String type       = subQueryParts.get(tableRowHighlightType)[0];
		String selectType = subQueryParts.get(tableRowHighlightType)[1];
		String minMaxType = subQueryParts.get(tableRowHighlightType)[2];
		String avgType    = subQueryParts.get(tableRowHighlightType)[3];
		String constraint = subQueryParts.get(tableRowHighlightType)[4];
		
		if (constraint == null || constraint.isEmpty())
			constraint = "";
		else
			constraint = " AND " + constraint + " ";
		
		String template = "SELECT "
				+ "\"start_point_name\", "
				+ "\"end_point_name\", "
				+ "%s AS \"value\", "
				+ "\"name\" AS \"group_name\", "
				+ "(SELECT %s FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? %s) AS \"average\" "
				+ "FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? %s AND "
				+ "%s = (SELECT %s FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? %s)";

		String sql = String.format(Locale.ENGLISH, template, 
				type,
				avgType,
				constraint,
				constraint,
				selectType,
				minMaxType,
				constraint
		);
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, observationType.getId());
		stmt.setInt(idx++, observationType.getId());
		stmt.setInt(idx++, observationType.getId());
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			HashMap<String, Object> statistics = new HashMap<String, Object>();
			statistics.put("start_point_name", rs.getString("start_point_name"));
			statistics.put("end_point_name",   rs.getString("end_point_name"));
			statistics.put("group_name",       rs.getString("group_name"));
			statistics.put("type",             observationType.name());
			
			if (tableRowHighlightType == TableRowHighlightType.INFLUENCE_ON_POSITION) {
				statistics.put("value",   options.convertLengthResidualToView(rs.getDouble("value")));
				statistics.put("average", options.convertLengthResidualToView(rs.getDouble("average")));
			}
			else if (tableRowHighlightType == TableRowHighlightType.REDUNDANCY) {
				statistics.put("value",   options.convertPercentToView(rs.getDouble("value")));
				statistics.put("average", options.convertPercentToView(rs.getDouble("average")));
			}
			else {
				statistics.put("value",   rs.getDouble("value"));
				statistics.put("average", rs.getDouble("average"));
			}
	
			summary.add(statistics);
		}
		
		return summary;
	}
	
	private double[] getTableRowHighlightRange(TableRowHighlightType tableRowHighlightType) throws SQLException {
		String sql = "SELECT "
				+ "\"left_boundary\", \"right_boundary\" "
				+ "FROM \"TableRowHighlightRange\" "
				+ "WHERE \"type\" = ? LIMIT 1";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, tableRowHighlightType.getId());

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			double leftBoundary  = rs.getDouble("left_boundary");
			double rightBoundary = rs.getDouble("right_boundary");
			return new double[] {leftBoundary, rightBoundary};
		}
		return null;
	}
	
	private HashMap<String, Object> getRelativeAndAbsoluteFrequency(ObservationType observationType, TableRowHighlightType tableRowHighlightType) throws SQLException {
		HashMap<String, Object> part = new HashMap<String, Object>();
		
		Map<TableRowHighlightType, String> columnNames = Map.of(
				TableRowHighlightType.REDUNDANCY,            "\"redundancy\"", 
				TableRowHighlightType.P_PRIO_VALUE,          "\"p_prio\"", 
				TableRowHighlightType.INFLUENCE_ON_POSITION, "ABS(\"influence_on_position\")"
		);
		double range[] = this.getTableRowHighlightRange(tableRowHighlightType);
		if (range == null || range.length < 2 || !columnNames.containsKey(tableRowHighlightType))
			return part;
		
		part.put("type", observationType.name());
		part.put("left_boundary",  tableRowHighlightType == TableRowHighlightType.INFLUENCE_ON_POSITION ? options.convertLengthResidualToView(range[0]) : options.convertPercentToView(range[0]));
		part.put("right_boundary", tableRowHighlightType == TableRowHighlightType.INFLUENCE_ON_POSITION ? options.convertLengthResidualToView(range[1]) : options.convertPercentToView(range[1]));
		
		if (tableRowHighlightType == TableRowHighlightType.P_PRIO_VALUE) {
			range[0] = Math.log(range[0]);
			range[1] = Math.log(range[1]);
		}

		String sql = "SELECT COUNT(*) AS \"value\" FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? "
				+ "UNION ALL "
				+ "SELECT COUNT(*) FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? AND " + columnNames.get(tableRowHighlightType) + " < ? "
				+ "UNION ALL "
				+ "SELECT COUNT(*) FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? AND " + columnNames.get(tableRowHighlightType) + " BETWEEN ? AND ? "
				+ "UNION ALL "
				+ "SELECT COUNT(*) FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"type\" = ? AND " + columnNames.get(tableRowHighlightType) + " > ? ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, observationType.getId()); // number of values
		
		stmt.setInt(idx++, observationType.getId()); // inadequate values
		stmt.setDouble(idx++, range[0]);

		stmt.setInt(idx++, observationType.getId()); // satisfactory values
		stmt.setDouble(idx++, range[0]);
		stmt.setDouble(idx++, range[1]);
		
		stmt.setInt(idx++, observationType.getId()); // excellent values
		stmt.setDouble(idx++, range[1]);
		
		ResultSet rs = stmt.executeQuery();
		
		int absoluteFrequency[] = new int[4];
		int i=0;
		while (rs.next() && i<absoluteFrequency.length) {
			absoluteFrequency[i++] = rs.getInt("value");
		}
		
		int numberOfValues = absoluteFrequency[0];
		part.put("number_of_observations", numberOfValues);
		
		for (int j=1; numberOfValues > 0 && j<absoluteFrequency.length; j++) {
			part.put("absolute_frequency_part_"+j, absoluteFrequency[j]);
			part.put("relative_frequency_part_"+j, numberOfValues > 0 ? (100.0*absoluteFrequency[j])/(double)numberOfValues : 0 );
		}

		return part;
	}

	private List<HashMap<String, Object>> getPointGroups(int dim, PointType pointType) throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();
		
		String sqlGroup = "SELECT \"id\", \"name\" FROM \"PointGroup\" WHERE \"dimension\" = ? AND \"type\" = ? AND \"enable\" = TRUE";
		
		String sqlUncertainty = "SELECT \"type\", \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = ?";

		String sqlPoint = "SELECT " +
				"\"name\", \"code\", " +
				"\"x0\", \"y0\", \"z0\", " +
				"\"PointAposteriori\".\"sigma_x0\" AS \"sigma_x0\", " +
				"\"PointAposteriori\".\"sigma_y0\" AS \"sigma_y0\", " +
				"\"PointAposteriori\".\"sigma_z0\" AS \"sigma_z0\", " +
				"\"x\", \"y\", \"z\", " +
				"CASEWHEN(\"sigma_x\" < 0, 0.0, \"sigma_x\") AS \"sigma_x\", " +
				"CASEWHEN(\"sigma_y\" < 0, 0.0, \"sigma_y\") AS \"sigma_y\", " +
				"CASEWHEN(\"sigma_z\" < 0, 0.0, \"sigma_z\") AS \"sigma_z\", " +
				"\"confidence_ellipse_major_axis\", \"confidence_ellipse_minor_axis\", \"confidence_ellipse_angle\", " + 
				"\"confidence_major_axis\", \"confidence_middle_axis\", \"confidence_minor_axis\", " +
				"\"confidence_alpha\", \"confidence_beta\", \"confidence_gamma\", " +
				"\"redundancy_x\", \"redundancy_y\", \"redundancy_z\", \"number_of_observations\", " +
				"\"residual_y\", \"residual_x\", \"residual_z\", " + 
				"\"gross_error_x\", \"gross_error_y\", \"gross_error_z\", " +
				"\"influence_on_position_x\", \"influence_on_position_y\", \"influence_on_position_z\", \"influence_on_network_distortion\", " +
				"\"minimal_detectable_bias_x\", \"minimal_detectable_bias_y\", \"minimal_detectable_bias_z\", " +
				"\"maximum_tolerable_bias_x\", \"maximum_tolerable_bias_y\", \"maximum_tolerable_bias_z\", " +
				"\"first_principal_component_y\", \"first_principal_component_x\", \"first_principal_component_z\", " +
				"\"omega\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"significant\" " +
				"FROM \"PointApriori\" " +
				"JOIN \"PointAposteriori\" ON \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" " +
				"WHERE \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE " + 
				"ORDER BY \"PointApriori\".\"id\" ASC"; 

		PreparedStatement stmtGroup       = this.dataBase.getPreparedStatement(sqlGroup);
		PreparedStatement stmtPoint       = this.dataBase.getPreparedStatement(sqlPoint);
		PreparedStatement stmtUncertainty = this.dataBase.getPreparedStatement(sqlUncertainty);
		
		stmtGroup.setInt(1, dim);
		stmtGroup.setInt(2, pointType.getId());

		ResultSet groupSet = stmtGroup.executeQuery();
		while (groupSet.next()) {
			double omegaGroup = 0.0, redundancyGroupX = 0.0, redundancyGroupY = 0.0, redundancyGroupZ = 0.0;
			double maxGrossErrorGroupX = 0.0, maxGrossErrorGroupY = 0.0, maxGrossErrorGroupZ = 0.0;
			double maxResidualGroupX = 0.0, maxResidualGroupY = 0.0, maxResidualGroupZ = 0.0;
			boolean significantGroup = false;
			int groupId = groupSet.getInt("id");
			stmtPoint.setInt(1, groupId);
			stmtUncertainty.setInt(1, groupId);
			
			HashMap<String, Object> groupParam         = new HashMap<String, Object>();
			HashMap<String, Object> groupUncertainties = new HashMap<String, Object>();
			List<HashMap<String, Object>> points       = new ArrayList<HashMap<String, Object>>();

			if (pointType == PointType.STOCHASTIC_POINT) {
				ResultSet uncertaintySet = stmtUncertainty.executeQuery();
				while (uncertaintySet.next()) {
					PointGroupUncertaintyType uncertaintyType = PointGroupUncertaintyType.getEnumByValue(uncertaintySet.getInt("type"));
					double value = uncertaintySet.getDouble("value");
					if (uncertaintyType != null && value > 0)
						groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertLengthUncertaintyToView(value));
				}
			}

			ResultSet pointSet = stmtPoint.executeQuery();
			ResultSetMetaData rsmd = pointSet.getMetaData();
			int cnt = rsmd.getColumnCount();

			while (pointSet.next()) {
				HashMap<String, Object> h = new HashMap<String, Object>();

				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {						
					case "x0":
					case "y0":
					case "z0":
					case "x":
					case "y":
					case "z":
						h.put(key, options.convertLengthToView(pointSet.getDouble(i)));
						break;

					case "sigma_x0":
					case "sigma_y0":
					case "sigma_z0":
					case "sigma_x":
					case "sigma_y":
					case "sigma_z":
					case "confidence_major_axis":
					case "confidence_middle_axis":
					case "confidence_minor_axis":
					case "confidence_ellipse_major_axis":
					case "confidence_ellipse_minor_axis":
						h.put(key, options.convertLengthUncertaintyToView(pointSet.getDouble(i)));
						break;

					case "confidence_alpha":
					case "confidence_beta":
					case "confidence_gamma":
					case "confidence_ellipse_angle":
						h.put(key, options.convertAngleToView(pointSet.getDouble(i)));
						break;

					case "residual_x":
					case "residual_y":
					case "residual_z": 
					case "gross_error_x":
					case "gross_error_y":
					case "gross_error_z":
					case "influence_on_position_x":
					case "influence_on_position_y":
					case "influence_on_position_z":
					case "influence_on_network_distortion":
					case "minimal_detectable_bias_x":
					case "minimal_detectable_bias_y":
					case "minimal_detectable_bias_z":
					case "maximum_tolerable_bias_x":
					case "maximum_tolerable_bias_y":
					case "maximum_tolerable_bias_z":
					case "first_principal_component_x":
					case "first_principal_component_y":
					case "first_principal_component_z":
						h.put(key, options.convertLengthResidualToView(pointSet.getDouble(i)));
						break;	
						
					case "redundancy_x":
					case "redundancy_y":
					case "redundancy_z":
						h.put(key, options.convertPercentToView(pointSet.getDouble(i)));
						break;

					case "significant":
						h.put(key, pointSet.getBoolean(i));
						break;

					default: // Name, code, statistics, etc.
						int type = rsmd.getColumnType(i);
						if(type == Types.CHAR || type==Types.VARCHAR)
							h.put(key, pointSet.getString(i));
						else if(type == Types.INTEGER)
							h.put(key, pointSet.getInt(i) );
						else if(type == Types.DOUBLE)
							h.put(key, pointSet.getDouble(i) );
						else if(type == Types.BOOLEAN)
							h.put(key, pointSet.getBoolean(i) );
						break;
					}
				}

				boolean significant = pointSet.getBoolean("significant");

				double redundancyX = pointSet.getDouble("redundancy_x");
				double redundancyY = pointSet.getDouble("redundancy_y");
				double redundancyZ = pointSet.getDouble("redundancy_z");

				double grossErrorX = pointSet.getDouble("gross_error_x");
				double grossErrorY = pointSet.getDouble("gross_error_y");
				double grossErrorZ = pointSet.getDouble("gross_error_z");

				double residualX = pointSet.getDouble("residual_x");
				double residualY = pointSet.getDouble("residual_y");
				double residualZ = pointSet.getDouble("residual_z");
				
				omegaGroup += pointSet.getDouble("omega");
				redundancyGroupX += redundancyX;
				redundancyGroupY += redundancyY;
				redundancyGroupZ += redundancyZ;

				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
				maxGrossErrorGroupZ = Math.abs(grossErrorZ) > Math.abs(maxGrossErrorGroupZ) ? grossErrorZ : maxGrossErrorGroupZ;
				
				maxResidualGroupX = Math.abs(residualX) > Math.abs(maxResidualGroupX) ? residualX : maxResidualGroupX;
				maxResidualGroupY = Math.abs(residualY) > Math.abs(maxResidualGroupY) ? residualY : maxResidualGroupY;
				maxResidualGroupZ = Math.abs(residualZ) > Math.abs(maxResidualGroupZ) ? residualZ : maxResidualGroupZ;

				if (!significantGroup && significant)
					significantGroup = true;

				points.add(h);
			}

			if (points != null && points.size() > 0) {
				groupParam.put("id",           groupId);
				groupParam.put("name",         groupSet.getString("name"));
				groupParam.put("points",       points);
				groupParam.put("omega",        omegaGroup);
				groupParam.put("significant",  significantGroup);
				groupParam.put("dimension",    dim);
				groupParam.put("type",         pointType.name());

				groupParam.put("redundancy_x",  redundancyGroupX);
				groupParam.put("redundancy_y",  redundancyGroupY);
				groupParam.put("redundancy_z",  redundancyGroupZ);
				groupParam.put("redundancy",    redundancyGroupX+redundancyGroupY+redundancyGroupZ);

				groupParam.put("max_gross_error_x", options.convertLengthResidualToView(maxGrossErrorGroupX));
				groupParam.put("max_gross_error_y", options.convertLengthResidualToView(maxGrossErrorGroupY));
				groupParam.put("max_gross_error_z", options.convertLengthResidualToView(maxGrossErrorGroupZ));
				
				groupParam.put("max_residual_x", options.convertLengthResidualToView(maxResidualGroupX));
				groupParam.put("max_residual_y", options.convertLengthResidualToView(maxResidualGroupY));
				groupParam.put("max_residual_z", options.convertLengthResidualToView(maxResidualGroupZ));
				
				if (groupUncertainties != null && !groupUncertainties.isEmpty())
					groupParam.put("uncertainties", groupUncertainties);
				
				groupParam.put("visible", this.isPointDimensionVisibility(pointType, dim));

				groups.add(groupParam);			
			}
		}

		return groups;
	}

	private List<HashMap<String, Object>> getVerticalDeflectionGroups(VerticalDeflectionType verticalDeflectionType) throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();
		
		String sqlGroup = "SELECT \"id\", \"name\" FROM \"VerticalDeflectionGroup\" WHERE \"type\" = ? AND \"enable\" = TRUE";
		
		String sqlUncertainty = "SELECT \"type\", \"value\" FROM \"VerticalDeflectionGroupUncertainty\" WHERE \"group_id\" = ?";
		
		String sqlDeflection = "SELECT"
				+ "\"name\", "
				
				+ "\"x0\", \"y0\", "
				+ "\"x\",  \"y\", "
				
				+ "\"VerticalDeflectionAposteriori\".\"sigma_x0\" AS \"sigma_x0\", "
				+ "\"VerticalDeflectionAposteriori\".\"sigma_y0\" AS \"sigma_y0\", "

				+ "\"redundancy_x\", \"redundancy_y\", "
				+ "\"gross_error_x\", \"gross_error_y\", "

				+ "\"residual_x\", \"residual_y\", "
				+ "\"minimal_detectable_bias_x\", \"minimal_detectable_bias_y\", "
				+ "\"maximum_tolerable_bias_x\", \"maximum_tolerable_bias_y\", " 

				+ "CASEWHEN(\"sigma_x\" < 0, 0.0, \"sigma_x\") AS \"sigma_x\", "
				+ "CASEWHEN(\"sigma_y\" < 0, 0.0, \"sigma_y\") AS \"sigma_y\", "

				+ "\"confidence_major_axis\", \"confidence_minor_axis\", "
				+ "\"omega\", \"t_prio\", \"t_post\", \"p_prio\", \"p_post\", \"significant\" "
				
				+ "FROM \"VerticalDeflectionApriori\" "
				
				+ "JOIN \"VerticalDeflectionAposteriori\" ON \"VerticalDeflectionApriori\".\"id\" = \"VerticalDeflectionAposteriori\".\"id\" "
				
				+ "WHERE \"VerticalDeflectionApriori\".\"enable\" = TRUE AND \"VerticalDeflectionApriori\".\"group_id\" = ? "
				+ "ORDER BY \"id\" ASC"; 
		

		PreparedStatement stmtGroup       = this.dataBase.getPreparedStatement(sqlGroup);
		PreparedStatement stmtDeflection  = this.dataBase.getPreparedStatement(sqlDeflection);
		PreparedStatement stmtUncertainty = this.dataBase.getPreparedStatement(sqlUncertainty);
		
		stmtGroup.setInt(1, verticalDeflectionType.getId());

		ResultSet groupSet = stmtGroup.executeQuery();
		while (groupSet.next()) {
			double omegaGroup = 0.0, redundancyGroupX = 0.0, redundancyGroupY = 0.0;
			double maxGrossErrorGroupX = 0.0, maxGrossErrorGroupY = 0.0;
			double maxResidualGroupX = 0.0, maxResidualGroupY = 0.0;
			boolean significantGroup = false;
			int groupId = groupSet.getInt("id");

			HashMap<String, Object> groupParam         = new HashMap<String, Object>();
			HashMap<String, Object> groupUncertainties = new HashMap<String, Object>();
			List<HashMap<String, Object>> deflections  = new ArrayList<HashMap<String, Object>>();
			
			stmtDeflection.setInt(1, groupId);
			stmtUncertainty.setInt(1, groupId);
			
			if (verticalDeflectionType == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION) {
				ResultSet uncertaintySet = stmtUncertainty.executeQuery();
				while (uncertaintySet.next()) {
					VerticalDeflectionGroupUncertaintyType uncertaintyType = VerticalDeflectionGroupUncertaintyType.getEnumByValue(uncertaintySet.getInt("type"));
					double value = uncertaintySet.getDouble("value");
					if (uncertaintyType != null && value > 0)
						groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertAngleUncertaintyToView(value));
				}
			}

			ResultSet verticalDeflectionSet = stmtDeflection.executeQuery();
			ResultSetMetaData rsmd = verticalDeflectionSet.getMetaData();

			int cnt = rsmd.getColumnCount();
			while (verticalDeflectionSet.next()) {
				HashMap<String, Object> h = new HashMap<String, Object>();
				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {
					case "sigma_x0":
					case "sigma_y0":
					case "sigma_x":
					case "sigma_y":
					case "confidence_major_axis":
					case "confidence_minor_axis":
						h.put(key, options.convertAngleUncertaintyToView(verticalDeflectionSet.getDouble(i)));
						break;

					case "x0":
					case "y0":
					case "x":
					case "y":
					case "residual_y":
					case "residual_x":
					case "gross_error_x":
					case "gross_error_y":
					case "minimal_detectable_bias_x":
					case "minimal_detectable_bias_y":
					case "maximum_tolerable_bias_x":
					case "maximum_tolerable_bias_y":
						h.put(key, options.convertAngleResidualToView(verticalDeflectionSet.getDouble(i)));
						break;
						
					case "redundancy_x":
					case "redundancy_y":
						h.put(key, options.convertPercentToView(verticalDeflectionSet.getDouble(i)));
						break;

					default: // Statistics
						int type = rsmd.getColumnType(i);
						if(type == Types.CHAR || type==Types.VARCHAR)
							h.put(key, verticalDeflectionSet.getString(i));
						else if(type == Types.INTEGER)
							h.put(key, verticalDeflectionSet.getInt(i));
						else if(type == Types.DOUBLE)
							h.put(key, verticalDeflectionSet.getDouble(i));
						else if(type == Types.BOOLEAN)
							h.put(key, verticalDeflectionSet.getBoolean(i));
						break;
					}
				}

				boolean significant = verticalDeflectionSet.getBoolean("significant");

				double redundancyX = verticalDeflectionSet.getDouble("redundancy_x");
				double redundancyY = verticalDeflectionSet.getDouble("redundancy_y");

				double grossErrorX = verticalDeflectionSet.getDouble("gross_error_x");
				double grossErrorY = verticalDeflectionSet.getDouble("gross_error_y");
				
				double residualX = verticalDeflectionSet.getDouble("residual_x");
				double residualY = verticalDeflectionSet.getDouble("residual_y");


				omegaGroup += verticalDeflectionSet.getDouble("omega");
				redundancyGroupX += redundancyX;
				redundancyGroupY += redundancyY;

				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
				
				maxResidualGroupX = Math.abs(residualX) > Math.abs(maxResidualGroupX) ? residualX : maxResidualGroupX;
				maxResidualGroupY = Math.abs(residualY) > Math.abs(maxResidualGroupY) ? residualY : maxResidualGroupY;

				if (!significantGroup && significant)
					significantGroup = true;

				deflections.add(h);
			}

			if (deflections != null && !deflections.isEmpty()) {
				groupParam.put("id",           groupId);
				groupParam.put("name",         groupSet.getString("name"));
				groupParam.put("deflections",  deflections);
				groupParam.put("omega",        omegaGroup);
				groupParam.put("significant",  significantGroup);
				groupParam.put("type",         verticalDeflectionType.name());

				groupParam.put("redundancy_x", redundancyGroupX);
				groupParam.put("redundancy_y", redundancyGroupY);
				groupParam.put("redundancy",   redundancyGroupX+redundancyGroupY);

				groupParam.put("max_gross_error_x", options.convertAngleResidualToView(maxGrossErrorGroupX));
				groupParam.put("max_gross_error_y", options.convertAngleResidualToView(maxGrossErrorGroupY));
				
				groupParam.put("max_residual_x", options.convertAngleResidualToView(maxResidualGroupX));
				groupParam.put("max_residual_y", options.convertAngleResidualToView(maxResidualGroupY));
				
				if (groupUncertainties != null && !groupUncertainties.isEmpty())
					groupParam.put("uncertainties", groupUncertainties);

				groups.add(groupParam);					
			}
		}	
		return groups;
	}

	private List<HashMap<String, Object>> getObservationGroups(ObservationType obsType) throws SQLException {
		boolean isGNSS = obsType == ObservationType.GNSS1D || obsType == ObservationType.GNSS2D || obsType == ObservationType.GNSS3D;
		if (isGNSS)
			return this.getGNSSObservationGroups(obsType);

		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		String sqlGroup = "SELECT \"id\", \"name\" FROM \"ObservationGroup\" WHERE \"type\" = ? AND \"enable\" = TRUE";
		
		String sqlUncertainty = "SELECT \"type\", \"value\" FROM \"ObservationGroupUncertainty\" WHERE \"group_id\" = ?";

		String sqlObservation = "SELECT "
				+ "\"start_point_name\",\"end_point_name\",\"instrument_height\",\"reflector_height\", "
				+ "\"value_0\",\"distance_0\", "
				+ "\"ObservationAposteriori\".\"sigma_0\" AS \"sigma_0\", "
				+ "\"value\",\"redundancy\",\"residual\",\"gross_error\",\"minimal_detectable_bias\",\"maximum_tolerable_bias\", "
				+ "CASEWHEN(\"sigma\" < 0, 0.0, \"sigma\") AS \"sigma\", "
				+ "\"influence_on_position\",\"influence_on_network_distortion\", "
				+ "\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" "
				+ "WHERE \"ObservationApriori\".\"group_id\" = ? "
				+ "AND \"ObservationApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"ObservationApriori\".\"id\" ASC";

		PreparedStatement stmtGroup       = this.dataBase.getPreparedStatement(sqlGroup);
		PreparedStatement stmtObservation = this.dataBase.getPreparedStatement(sqlObservation);
		PreparedStatement stmtUncertainty = this.dataBase.getPreparedStatement(sqlUncertainty);
		
		stmtGroup.setInt(1, obsType.getId());

		ResultSet groupSet = stmtGroup.executeQuery();
		while (groupSet.next()) {
			boolean significantGroup = false;
			double omegaGroup = 0.0, redundancyGroup = 0.0;
			double maxGrossErrorGroup = 0.0, maxResidualGroup = 0.0;
			HashMap<String, Object> groupParam = new HashMap<String, Object>();
			List<HashMap<String, Object>> observations = new ArrayList<HashMap<String, Object>>();
			int groupId = groupSet.getInt("id");
			stmtObservation.setInt(1, groupId);
			stmtUncertainty.setInt(1, groupId);
			
			ResultSet uncertaintySet = stmtUncertainty.executeQuery();
			HashMap<String, Object> groupUncertainties = new HashMap<String, Object>();
			while (uncertaintySet.next()) {
				ObservationGroupUncertaintyType uncertaintyType = ObservationGroupUncertaintyType.getEnumByValue(uncertaintySet.getInt("type"));
				if (uncertaintyType != null) {
					double value = uncertaintySet.getDouble("value");
					if (value <= 0)
						continue;
					
					switch(obsType) {
					case DIRECTION:
					case ZENITH_ANGLE:
						switch (uncertaintyType) {
						case ZERO_POINT_OFFSET:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertAngleUncertaintyToView(value));
							break;
						
						case SQUARE_ROOT_DISTANCE_DEPENDENT:
						case DISTANCE_DEPENDENT:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertLengthUncertaintyToView(value));
							break;					
						}

						break;
					case LEVELING:
					case HORIZONTAL_DISTANCE:
					case SLOPE_DISTANCE:
						switch (uncertaintyType) {
						case ZERO_POINT_OFFSET:
						case SQUARE_ROOT_DISTANCE_DEPENDENT:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertLengthUncertaintyToView(value));
							break;
							
						case DISTANCE_DEPENDENT:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertScaleUncertaintyToView(value));
							break;					
						}
					
						break;
					default: // GNSS
						break;
					}
				}
			}
			
			ResultSet observationSet = stmtObservation.executeQuery();
			ResultSetMetaData rsmd = observationSet.getMetaData();
			int cnt = rsmd.getColumnCount();

			while (observationSet.next()) {
				HashMap<String, Object> h = new HashMap<String, Object>();
				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {

					case "instrument_height":
					case "reflector_height":
					case "distance_0":
						h.put(key, options.convertLengthToView(observationSet.getDouble(i)));
						break;

					case "value_0":
					case "value":
						switch(obsType) {
						case DIRECTION:
						case ZENITH_ANGLE:
							h.put(key, options.convertAngleToView(observationSet.getDouble(i)));
							break;
						default:
							h.put(key, options.convertLengthToView(observationSet.getDouble(i)));
							break;
						}
						break;

					case "sigma_0":
					case "sigma":
						switch(obsType) {
						case DIRECTION:
						case ZENITH_ANGLE:
							h.put(key, options.convertAngleUncertaintyToView(observationSet.getDouble(i)));
							break;
						default:
							h.put(key, options.convertLengthUncertaintyToView(observationSet.getDouble(i)));
							break;
						}
						break;

					case "gross_error":
					case "minimal_detectable_bias":
					case "maximum_tolerable_bias":
					case "residual":
						switch(obsType) {
						case DIRECTION:
						case ZENITH_ANGLE:
							h.put(key, options.convertAngleResidualToView(observationSet.getDouble(i)));
							break;
						default:
							h.put(key, options.convertLengthResidualToView(observationSet.getDouble(i)));
							break;
						}
						break;

					case "influence_on_position":
					case "influence_on_network_distortion":
						h.put(key, options.convertLengthResidualToView(observationSet.getDouble(i)));
						break;
						
					case "redundancy":
						h.put(key, options.convertPercentToView(observationSet.getDouble(i)));
						break;

					default: // Point names, statistics, etc.
						int type = rsmd.getColumnType(i);
						if(type == Types.CHAR || type==Types.VARCHAR)
							h.put(key, observationSet.getString(i));
						else if(type == Types.INTEGER)
							h.put(key, observationSet.getInt(i));
						else if(type == Types.DOUBLE)
							h.put(key, observationSet.getDouble(i));
						else if(type == Types.BOOLEAN)
							h.put(key, observationSet.getBoolean(i));
						break;
					}
				}

				boolean significant = observationSet.getBoolean("significant");
				double redundancy   = observationSet.getDouble("redundancy");
				double grossError   = observationSet.getDouble("gross_error");
				double residual     = observationSet.getDouble("residual");

				omegaGroup        += observationSet.getDouble("omega");
				redundancyGroup   += redundancy;
				maxGrossErrorGroup = Math.abs(grossError) > Math.abs(maxGrossErrorGroup) ? grossError : maxGrossErrorGroup;
				maxResidualGroup   = Math.abs(residual)   > Math.abs(maxResidualGroup)   ? residual : maxResidualGroup;
				if (!significantGroup && significant)
					significantGroup = true;

				observations.add(h);
			}

			if (observations != null && !observations.isEmpty()) {
				switch(obsType) {
				case DIRECTION:
				case ZENITH_ANGLE:
					groupParam.put("type", obsType.name());
					groupParam.put("max_gross_error", options.convertAngleResidualToView(maxGrossErrorGroup));
					groupParam.put("max_residual", options.convertAngleResidualToView(maxResidualGroup));
					break;
					
				case LEVELING:
				case HORIZONTAL_DISTANCE:
				case SLOPE_DISTANCE:
					groupParam.put("type", obsType.name());
					groupParam.put("max_gross_error", options.convertLengthResidualToView(maxGrossErrorGroup));
					groupParam.put("max_residual", options.convertLengthResidualToView(maxResidualGroup));
					break;
				default:
					continue;
					//break;
				}
				
				groupParam.put("id",              groupId);
				groupParam.put("name",            groupSet.getString("name"));
				groupParam.put("observations",    observations);
				groupParam.put("dimension",       1);
				groupParam.put("omega",           omegaGroup);
				groupParam.put("redundancy",      redundancyGroup);
				groupParam.put("significant",     significantGroup);

				if (groupUncertainties != null && !groupUncertainties.isEmpty())
					groupParam.put("uncertainties", groupUncertainties);

				List<HashMap<String, Object>> parameters = this.getAddionalParameters(groupId);

				if (parameters != null && !parameters.isEmpty())
					groupParam.put("unknown_parameters", parameters);

				groups.add(groupParam);
			}
		}				
		return groups;
	}

	private List<HashMap<String, Object>> getGNSSObservationGroups(ObservationType obsType) throws SQLException {
		int dim = obsType == ObservationType.GNSS1D ? 1 : obsType == ObservationType.GNSS2D ? 2 : 3;
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		String sqlGroup = "SELECT \"id\", \"name\" FROM \"ObservationGroup\" WHERE \"type\" = ? AND \"enable\" = TRUE";
		
		String sqlUncertainty = "SELECT \"type\", \"value\" FROM \"ObservationGroupUncertainty\" WHERE \"group_id\" = ?";

		String sqlObservation = "SELECT "
				+ "\"start_point_name\",\"end_point_name\",\"y0\",\"x0\",\"z0\", "
				+ "0 AS \"instrument_height\", "
				+ "0 AS \"reflector_height\", "
				+ "SQRT(\"y0\"*\"y0\" + \"x0\"*\"x0\" + \"z0\"*\"z0\") AS \"distance_0\", "
				+ "\"GNSSObservationAposteriori\".\"sigma_x0\" AS \"sigma_x0\", "
				+ "\"GNSSObservationAposteriori\".\"sigma_y0\" AS \"sigma_y0\", "
				+ "\"GNSSObservationAposteriori\".\"sigma_z0\" AS \"sigma_z0\", "
				+ "CASEWHEN(\"sigma_x\" < 0, 0.0, \"sigma_x\") AS \"sigma_x\", "
				+ "CASEWHEN(\"sigma_y\" < 0, 0.0, \"sigma_y\") AS \"sigma_y\", "
				+ "CASEWHEN(\"sigma_z\" < 0, 0.0, \"sigma_z\") AS \"sigma_z\", "
				+ "\"y\",\"x\",\"z\","
				+ "\"residual_y\", \"residual_x\", \"residual_z\", "
				+ "\"redundancy_y\",\"redundancy_x\",\"redundancy_z\","
				+ "\"gross_error_y\",\"gross_error_x\",\"gross_error_z\","
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\","
				+ "\"maximum_tolerable_bias_y\",\"maximum_tolerable_bias_x\",\"maximum_tolerable_bias_z\","
				+ "\"influence_on_position_y\",\"influence_on_position_x\",\"influence_on_position_z\","
				+ "\"influence_on_network_distortion\","
				+ "\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"GNSSObservationApriori\" "
				+ "JOIN \"GNSSObservationAposteriori\" ON \"GNSSObservationApriori\".\"id\" = \"GNSSObservationAposteriori\".\"id\" "
				+ "WHERE \"GNSSObservationApriori\".\"group_id\" = ? "
				+ "AND \"GNSSObservationApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"GNSSObservationApriori\".\"id\" ASC"; 

		PreparedStatement stmtGroup = this.dataBase.getPreparedStatement(sqlGroup);
		PreparedStatement stmtObservation = this.dataBase.getPreparedStatement(sqlObservation);
		PreparedStatement stmtUncertainty = this.dataBase.getPreparedStatement(sqlUncertainty);
		
		stmtGroup.setInt(1, obsType.getId());

		ResultSet groupSet = stmtGroup.executeQuery();
		while (groupSet.next()) {
			double omegaGroup = 0.0, redundancyGroupX = 0.0, redundancyGroupY = 0.0, redundancyGroupZ = 0.0;
			double maxGrossErrorGroupX = 0.0, maxGrossErrorGroupY = 0.0, maxGrossErrorGroupZ = 0.0;
			double maxResidualGroupX = 0.0, maxResidualGroupY = 0.0, maxResidualGroupZ = 0.0;
			boolean significantGroup = false;
			HashMap<String, Object> groupParam = new HashMap<String, Object>();
			List<HashMap<String, Object>> observations = new ArrayList<HashMap<String, Object>>();

			int groupId = groupSet.getInt("id");
			stmtObservation.setInt(1, groupId);
			stmtUncertainty.setInt(1, groupId);
		
			ResultSet uncertaintySet = stmtUncertainty.executeQuery();
			HashMap<String, Object> groupUncertainties = new HashMap<String, Object>();
			while (uncertaintySet.next()) {
				ObservationGroupUncertaintyType uncertaintyType = ObservationGroupUncertaintyType.getEnumByValue(uncertaintySet.getInt("type"));
				if (uncertaintyType != null) {
					double value = uncertaintySet.getDouble("value");
					if (value <= 0)
						continue;
					
					switch(obsType) {
					case GNSS1D:
					case GNSS2D:
					case GNSS3D:
						switch (uncertaintyType) {
						case ZERO_POINT_OFFSET:
						case SQUARE_ROOT_DISTANCE_DEPENDENT:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertLengthUncertaintyToView(value));
							break;
							
						case DISTANCE_DEPENDENT:
							groupUncertainties.put(uncertaintyType.name().toLowerCase(), options.convertScaleUncertaintyToView(value));
							break;
						}

						break;
					default: // terrestrial observations
						break;
					}
				}
			}

			ResultSet observationSet = stmtObservation.executeQuery();
			ResultSetMetaData rsmd = observationSet.getMetaData();
			int cnt = rsmd.getColumnCount();

			while (observationSet.next()) {

				HashMap<String, Object> h = new HashMap<String, Object>();
				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {
					case "instrument_height":
					case "reflector_height":
					case "distance_0":							
					case "x0":
					case "y0":
					case "z0":
					case "x":
					case "y":
					case "z":
						h.put(key, options.convertLengthToView(observationSet.getDouble(i)));
						break;

					case "sigma_x0":
					case "sigma_y0":
					case "sigma_z0":
					case "sigma_x":
					case "sigma_y":
					case "sigma_z":
						h.put(key, options.convertLengthUncertaintyToView(observationSet.getDouble(i)));
						break;

					case "gross_error_x":
					case "gross_error_y":
					case "gross_error_z":
					case "residual_x":
					case "residual_y":
					case "residual_z":
					case "minimal_detectable_bias_x":
					case "minimal_detectable_bias_y":
					case "minimal_detectable_bias_z":
					case "maximum_tolerable_bias_x":
					case "maximum_tolerable_bias_y":
					case "maximum_tolerable_bias_z":
					case "influence_on_position_x":
					case "influence_on_position_y":
					case "influence_on_position_z":
					case "influence_on_network_distortion":
						h.put(key, options.convertLengthResidualToView(observationSet.getDouble(i)));
						break;
						
					case "redundancy_x":
					case "redundancy_y":
					case "redundancy_z":
						h.put(key, options.convertPercentToView(observationSet.getDouble(i)));
						break;

					default: // Point names, statistics, etc.
						int type = rsmd.getColumnType(i);
						if(type == Types.CHAR || type==Types.VARCHAR)
							h.put(key, observationSet.getString(i));
						else if(type == Types.INTEGER)
							h.put(key, observationSet.getInt(i));
						else if(type == Types.DOUBLE)
							h.put(key, observationSet.getDouble(i));
						else if(type == Types.BOOLEAN)
							h.put(key, observationSet.getBoolean(i));
						break;
					}
				}

				boolean significant = observationSet.getBoolean("significant");

				double redundancyX = observationSet.getDouble("redundancy_x");
				double redundancyY = observationSet.getDouble("redundancy_y");
				double redundancyZ = observationSet.getDouble("redundancy_z");

				double grossErrorX = observationSet.getDouble("gross_error_x");
				double grossErrorY = observationSet.getDouble("gross_error_y");
				double grossErrorZ = observationSet.getDouble("gross_error_z");
				
				double residualX = observationSet.getDouble("residual_x");
				double residualY = observationSet.getDouble("residual_y");
				double residualZ = observationSet.getDouble("residual_z");

				omegaGroup += observationSet.getDouble("omega");
				redundancyGroupX += redundancyX;
				redundancyGroupY += redundancyY;
				redundancyGroupZ += redundancyZ;

				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
				maxGrossErrorGroupZ = Math.abs(grossErrorZ) > Math.abs(maxGrossErrorGroupZ) ? grossErrorZ : maxGrossErrorGroupZ;
				
				maxResidualGroupX = Math.abs(residualX) > Math.abs(maxResidualGroupX) ? residualX : maxResidualGroupX;
				maxResidualGroupY = Math.abs(residualY) > Math.abs(maxResidualGroupY) ? residualY : maxResidualGroupY;
				maxResidualGroupZ = Math.abs(residualZ) > Math.abs(maxResidualGroupZ) ? residualZ : maxResidualGroupZ;

				if (!significantGroup && significant)
					significant = true;

				observations.add(h);
			}

			if (observations != null && !observations.isEmpty()) {
				groupParam.put("id",            groupId);
				groupParam.put("name",          groupSet.getString("name"));
				groupParam.put("observations",  observations);
				groupParam.put("omega",         omegaGroup);
				groupParam.put("significant",   significantGroup);
				groupParam.put("dimension",     dim);

				switch(obsType) {
				case GNSS1D:
				case GNSS2D:
				case GNSS3D:
					groupParam.put("type", obsType.name());
					break;
				default:
					continue;
					//break;
				}

				groupParam.put("redundancy_x", redundancyGroupX);
				groupParam.put("redundancy_y", redundancyGroupY);
				groupParam.put("redundancy_z", redundancyGroupZ);
				groupParam.put("redundancy",   redundancyGroupX+redundancyGroupY+redundancyGroupZ);

				groupParam.put("max_gross_error_x", options.convertLengthResidualToView(maxGrossErrorGroupX));
				groupParam.put("max_gross_error_y", options.convertLengthResidualToView(maxGrossErrorGroupY));
				groupParam.put("max_gross_error_z", options.convertLengthResidualToView(maxGrossErrorGroupZ));
				
				groupParam.put("max_residual_x", options.convertLengthResidualToView(maxResidualGroupX));
				groupParam.put("max_residual_y", options.convertLengthResidualToView(maxResidualGroupY));
				groupParam.put("max_residual_z", options.convertLengthResidualToView(maxResidualGroupZ));

				if (groupUncertainties != null && !groupUncertainties.isEmpty())
					groupParam.put("uncertainties", groupUncertainties);
				
				List<HashMap<String, Object>> parameters = this.getAddionalParameters(groupId);

				if (parameters != null && !parameters.isEmpty())
					groupParam.put("unknown_parameters", parameters);

				groups.add(groupParam);
			}
		}
		return groups;
	}

	private List<HashMap<String, Object>> getAddionalParameters(int groupId) throws SQLException {
		List<HashMap<String, Object>> parameters = new ArrayList<HashMap<String, Object>>(5);

		String sql = "SELECT "
				+ "\"type\",\"value_0\", "
				+ "\"value\", \"sigma\",\"confidence\",\"gross_error\",\"minimal_detectable_bias\","
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"AdditionalParameterApriori\" "
				+ "JOIN \"AdditionalParameterAposteriori\" "
				+ "ON \"AdditionalParameterApriori\".\"id\" = \"AdditionalParameterAposteriori\".\"id\" "
				+ "WHERE \"AdditionalParameterApriori\".\"group_id\" = ? "
				+ "AND \"AdditionalParameterApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"AdditionalParameterApriori\".\"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setInt(idx++, groupId);
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cnt = rsmd.getColumnCount();

		while (rs.next()) {
			ParameterType parameterType = ParameterType.getEnumByValue(rs.getInt("type"));
			if (parameterType == null)
				continue;

			HashMap<String, Object> h = new HashMap<String, Object>();
			for(int i = 1; i <= cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				switch(key) {
				case "type":
					switch(parameterType) {
					case ORIENTATION:
						h.put(key, "ORIENTATION");
						break;

					case REFRACTION_INDEX:
						h.put(key, "REFRACTION_INDEX");
						break;

					case SCALE:
						h.put(key, "SCALE");
						break;

					case ZERO_POINT_OFFSET:
						h.put(key, "ZERO_POINT_OFFSET");
						break;

					case ROTATION_X:
						h.put(key, "ROTATION_X");
						break;

					case ROTATION_Y:
						h.put(key, "ROTATION_Y");
						break;

					case ROTATION_Z:
						h.put(key, "ROTATION_Z");
						break;						

					default:
						break;

					}
					break;

				case "value_0":
				case "value":
					switch(parameterType) {
					case ORIENTATION:
					case ROTATION_X:
					case ROTATION_Y:
					case ROTATION_Z:
						h.put(key, options.convertAngleToView(rs.getDouble(i)));
						break;

					case ZERO_POINT_OFFSET:
						h.put(key, options.convertLengthToView(rs.getDouble(i)));
						break;

					case SCALE:
						h.put(key, options.convertScaleToView(rs.getDouble(i)));
						break;

					default:
						h.put(key,rs.getDouble(i));
						break;	
					}

					break;

				case "sigma":
				case "confidence":
					switch(parameterType) {
					case ORIENTATION:
					case ROTATION_X:
					case ROTATION_Y:
					case ROTATION_Z:
						h.put(key, options.convertAngleUncertaintyToView(rs.getDouble(i)));
						break;

					case ZERO_POINT_OFFSET:
						h.put(key, options.convertLengthUncertaintyToView(rs.getDouble(i)));
						break;

					case SCALE:
						h.put(key, options.convertScaleUncertaintyToView(rs.getDouble(i)));
						break;

					default:
						h.put(key,rs.getDouble(i));
						break;	
					}

					break;

				case "gross_error":
				case "minimal_detectable_bias":
					switch(parameterType) {
					case ORIENTATION:
					case ROTATION_X:
					case ROTATION_Y:
					case ROTATION_Z:
						h.put(key, options.convertAngleResidualToView(rs.getDouble(i)));
						break;

					case ZERO_POINT_OFFSET:
						h.put(key, options.convertLengthResidualToView(rs.getDouble(i)));
						break;

					case SCALE:
						h.put(key, options.convertScaleResidualToView(rs.getDouble(i)));
						break;

					default:
						h.put(key,rs.getDouble(i));
						break;	
					}

					break;

				default: // statistics, etc.
					int type = rsmd.getColumnType(i);
					if(type == Types.CHAR || type==Types.VARCHAR)
						h.put(key, rs.getString(i));
					else if(type == Types.INTEGER)
						h.put(key, rs.getInt(i));
					else if(type == Types.DOUBLE)
						h.put(key, rs.getDouble(i));
					else if(type == Types.BOOLEAN)
						h.put(key, rs.getBoolean(i));
					break;
				}
			}

			parameters.add(h);
		}

		return parameters;
	}

	private List<HashMap<String, Object>> getCongruenceAnalysisPointPairGroups(int dim) throws SQLException {
		List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>();

		String sqlGroup = "SELECT \"id\", \"name\" "
				+ "FROM \"CongruenceAnalysisGroup\" "
				+ "WHERE \"enable\" = TRUE AND \"dimension\" = ?";

		String sqlPointPairs = "SELECT "
				+ "\"start_point_name\", \"end_point_name\", "
				+ "\"y\",\"x\",\"z\","
				+ "\"sigma_y\",\"sigma_x\",\"sigma_z\","
				+ "\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\","
				+ "\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\","
				+ "\"confidence_ellipse_major_axis\",\"confidence_ellipse_minor_axis\","
				+ "\"confidence_ellipse_angle\","
				+ "\"gross_error_y\",\"gross_error_x\",\"gross_error_z\","
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\","
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"CongruenceAnalysisPointPairApriori\" "
				+ "JOIN \"CongruenceAnalysisPointPairAposteriori\" ON \"CongruenceAnalysisPointPairApriori\".\"id\" = \"CongruenceAnalysisPointPairAposteriori\".\"id\" "
				+ "WHERE \"CongruenceAnalysisPointPairApriori\".\"group_id\" = ? "
				+ "AND \"CongruenceAnalysisPointPairApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"CongruenceAnalysisPointPairApriori\".\"id\" ASC"; 

		PreparedStatement stmtGroup = this.dataBase.getPreparedStatement(sqlGroup);
		stmtGroup.setInt(1, dim);

		ResultSet groupSet = stmtGroup.executeQuery();
		while (groupSet.next()) {
			boolean significantGroup = false;
			double maxGrossErrorGroupX = 0, maxGrossErrorGroupY = 0, maxGrossErrorGroupZ = 0;
			HashMap<String, Object> groupParam = new HashMap<String, Object>();
			int groupId = groupSet.getInt("id");

			List<HashMap<String, Object>> pointPairs = new ArrayList<HashMap<String, Object>>();
			PreparedStatement stmtPointPair = this.dataBase.getPreparedStatement(sqlPointPairs);
			stmtPointPair.setInt(1, groupId);

			ResultSet pointPairSet = stmtPointPair.executeQuery();
			ResultSetMetaData rsmd = pointPairSet.getMetaData();
			int cnt = rsmd.getColumnCount();

			while (pointPairSet.next()) {
				HashMap<String, Object> h = new HashMap<String, Object>();

				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					switch(key) {						
					case "x":
					case "y":
					case "z":
						h.put(key, options.convertLengthToView(pointPairSet.getDouble(i)));
						break;

					case "sigma_x":
					case "sigma_y":
					case "sigma_z":
					case "confidence_major_axis":
					case "confidence_middle_axis":
					case "confidence_minor_axis":
					case "confidence_ellipse_major_axis":
					case "confidence_ellipse_minor_axis":
						h.put(key, options.convertLengthUncertaintyToView(pointPairSet.getDouble(i)));
						break;

					case "confidence_alpha":
					case "confidence_beta":
					case "confidence_gamma":
					case "confidence_ellipse_angle":
						h.put(key, options.convertAngleToView(pointPairSet.getDouble(i)));
						break;

					case "gross_error_x":
					case "gross_error_y":
					case "gross_error_z":
					case "minimal_detectable_bias_x":
					case "minimal_detectable_bias_y":
					case "minimal_detectable_bias_z":
						h.put(key, options.convertLengthResidualToView(pointPairSet.getDouble(i)));
						break;

					default: // Point names, statistics, etc.
						int type = rsmd.getColumnType(i);
						if(type == Types.CHAR || type==Types.VARCHAR)
							h.put(key, pointPairSet.getString(i));
						else if(type == Types.INTEGER)
							h.put(key, pointPairSet.getInt(i));
						else if(type == Types.DOUBLE)
							h.put(key, pointPairSet.getDouble(i));
						else if(type == Types.BOOLEAN)
							h.put(key, pointPairSet.getBoolean(i));
						break;
					}
				}

				boolean significant = pointPairSet.getBoolean("significant");

				double grossErrorX = pointPairSet.getDouble("gross_error_x");
				double grossErrorY = pointPairSet.getDouble("gross_error_y");
				double grossErrorZ = pointPairSet.getDouble("gross_error_z");

				maxGrossErrorGroupX = Math.abs(grossErrorX) > Math.abs(maxGrossErrorGroupX) ? grossErrorX : maxGrossErrorGroupX;
				maxGrossErrorGroupY = Math.abs(grossErrorY) > Math.abs(maxGrossErrorGroupY) ? grossErrorY : maxGrossErrorGroupY;
				maxGrossErrorGroupZ = Math.abs(grossErrorZ) > Math.abs(maxGrossErrorGroupZ) ? grossErrorZ : maxGrossErrorGroupZ;

				if (!significantGroup && significant)
					significantGroup = true;
				
				pointPairs.add(h);
			}
			
			if (pointPairs != null && !pointPairs.isEmpty()) {
				groupParam.put("id",                  groupId);
				groupParam.put("name",                groupSet.getString("name"));
				groupParam.put("dimension",           dim);
				groupParam.put("point_pairs",         pointPairs);
				groupParam.put("significant",         significantGroup );
				groupParam.put("max_gross_error_x",   options.convertLengthResidualToView(maxGrossErrorGroupX));
				groupParam.put("max_gross_error_y",   options.convertLengthResidualToView(maxGrossErrorGroupY));
				groupParam.put("max_gross_error_z",   options.convertLengthResidualToView(maxGrossErrorGroupZ));

				List<HashMap<String, Object>> strainParams = this.getStrainParameters(groupId);
				if (strainParams != null && !strainParams.isEmpty())
					groupParam.put("strain_parameters", strainParams);

				groups.add(groupParam);
			}
		}
		return groups;
	}

	private List<HashMap<String, Object>> getStrainParameters(int groupId) throws SQLException {
		List<HashMap<String, Object>> params = new ArrayList<HashMap<String, Object>>();

		String sql = "SELECT "
				+ "\"type\",\"value\",\"sigma\",\"confidence\","
				+ "\"gross_error\",\"minimal_detectable_bias\","
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"CongruenceAnalysisStrainParameterAposteriori\" "
				+ "WHERE \"group_id\" = ? ORDER BY \"type\" ASC";


		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setInt(idx++, groupId);

		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cnt = rsmd.getColumnCount();
		
		while (rs.next()) {
			HashMap<String, Object> h = new HashMap<String, Object>();
			
			ParameterType parameterType = ParameterType.getEnumByValue(rs.getInt("type"));
			if (parameterType == null)
				continue;
			
			for(int i = 1; i <= cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				switch(key) {
					
				case "type":
					h.put(key, parameterType.name());
					break;
					
				case "value":
					switch (parameterType) {
					case STRAIN_ROTATION_X:
					case STRAIN_ROTATION_Y:
					case STRAIN_ROTATION_Z:
					case STRAIN_SHEAR_X:
					case STRAIN_SHEAR_Y:
					case STRAIN_SHEAR_Z:
						h.put(key, options.convertAngleToView(rs.getDouble(i)));
						break;
						
					case STRAIN_SCALE_X:
					case STRAIN_SCALE_Y:
					case STRAIN_SCALE_Z:
						h.put(key, options.convertScaleToView(rs.getDouble(i)));
						break;

					case STRAIN_TRANSLATION_X:
					case STRAIN_TRANSLATION_Y:
					case STRAIN_TRANSLATION_Z:
						h.put(key, options.convertLengthToView(rs.getDouble(i)));
						break;
						
					default:
						continue;
						// break;
					}
					break;
					
				case "sigma":
				case "confidence":
					switch (parameterType) {
					case STRAIN_ROTATION_X:
					case STRAIN_ROTATION_Y:
					case STRAIN_ROTATION_Z:
					case STRAIN_SHEAR_X:
					case STRAIN_SHEAR_Y:
					case STRAIN_SHEAR_Z:
						h.put(key, options.convertAngleUncertaintyToView(rs.getDouble(i)));
						break;
						
					case STRAIN_SCALE_X:
					case STRAIN_SCALE_Y:
					case STRAIN_SCALE_Z:
						h.put(key, options.convertScaleUncertaintyToView(rs.getDouble(i)));
						break;

					case STRAIN_TRANSLATION_X:
					case STRAIN_TRANSLATION_Y:
					case STRAIN_TRANSLATION_Z:
						h.put(key, options.convertLengthUncertaintyToView(rs.getDouble(i)));
						break;
						
					default:
						continue;
						// break;
					}
					break;
					
				case "gross_error":
				case "minimal_detectable_bias":
					switch (parameterType) {
					case STRAIN_ROTATION_X:
					case STRAIN_ROTATION_Y:
					case STRAIN_ROTATION_Z:
					case STRAIN_SHEAR_X:
					case STRAIN_SHEAR_Y:
					case STRAIN_SHEAR_Z:
						h.put(key, options.convertAngleResidualToView(rs.getDouble(i)));
						break;
						
					case STRAIN_SCALE_X:
					case STRAIN_SCALE_Y:
					case STRAIN_SCALE_Z:
						h.put(key, options.convertScaleResidualToView(rs.getDouble(i)));
						break;

					case STRAIN_TRANSLATION_X:
					case STRAIN_TRANSLATION_Y:
					case STRAIN_TRANSLATION_Z:
						h.put(key, options.convertLengthResidualToView(rs.getDouble(i)));
						break;
						
					default:
						continue;
						// break;
					}
					break;
				
				default: // Point names, statistics, etc.
					int type = rsmd.getColumnType(i);
					if(type == Types.CHAR || type==Types.VARCHAR)
						h.put(key, rs.getString(i));
					else if(type == Types.INTEGER)
						h.put(key, rs.getInt(i));
					else if(type == Types.DOUBLE)
						h.put(key, rs.getDouble(i));
					else if(type == Types.BOOLEAN)
						h.put(key, rs.getBoolean(i));
					break;
				}
			}
			params.add(h);
		}
		return params;
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
