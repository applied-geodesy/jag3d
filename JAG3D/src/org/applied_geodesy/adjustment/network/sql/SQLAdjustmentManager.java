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

package org.applied_geodesy.adjustment.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultAverageThreshold;
import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.DefectType;
import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.NetworkAdjustment;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.PrincipalComponent;
import org.applied_geodesy.adjustment.network.RankDefect;
import org.applied_geodesy.adjustment.network.VarianceComponent;
import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.adjustment.network.congruence.CongruenceAnalysisGroup;
import org.applied_geodesy.adjustment.network.congruence.CongruenceAnalysisPointPair;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameter;
import org.applied_geodesy.adjustment.network.observation.ComponentType;
import org.applied_geodesy.adjustment.network.observation.DeltaZ;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.FaceType;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaY2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaY3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaZ1D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaZ3D;
import org.applied_geodesy.adjustment.network.observation.HorizontalDistance;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.SlopeDistance;
import org.applied_geodesy.adjustment.network.observation.ZenithAngle;
import org.applied_geodesy.adjustment.network.observation.group.DeltaZGroup;
import org.applied_geodesy.adjustment.network.observation.group.DirectionGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline1DGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline2DGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline3DGroup;
import org.applied_geodesy.adjustment.network.observation.group.HorizontalDistanceGroup;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.network.observation.group.SlopeDistanceGroup;
import org.applied_geodesy.adjustment.network.observation.group.ZenithAngleGroup;
import org.applied_geodesy.adjustment.network.observation.projection.Projection;
import org.applied_geodesy.adjustment.network.observation.projection.ProjectionType;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Orientation;
import org.applied_geodesy.adjustment.point.Point;
import org.applied_geodesy.adjustment.point.Point1D;
import org.applied_geodesy.adjustment.point.Point2D;
import org.applied_geodesy.adjustment.point.Point3D;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.util.sql.DataBase;
import org.applied_geodesy.util.sql.HSQLDB;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;
import org.applied_geodesy.version.jag3d.Version;
import org.applied_geodesy.version.jag3d.VersionType;

public class SQLAdjustmentManager {
	private final DataBase dataBase;

	private Map<String, Point> completePoints    = new LinkedHashMap<String, Point>();
	private Map<String, Point> completePointsWithDeflections = new LinkedHashMap<String, Point>();
	private Map<String, Point> completeNewPoints = new LinkedHashMap<String, Point>();

	private Map<Integer, AdditionalUnknownParameter> additionalParametersToBeEstimated = new LinkedHashMap<Integer, AdditionalUnknownParameter>();

	private List<CongruenceAnalysisGroup> congruenceAnalysisGroups = new ArrayList<CongruenceAnalysisGroup>();
	private List<ObservationGroup> completeObservationGroups = new ArrayList<ObservationGroup>();

	private Projection projection = new Projection(ProjectionType.NONE);

	private NetworkAdjustment networkAdjustment = null;

	private boolean freeNetwork = false,
			congruenceAnalysis = false,
			pure1DNetwork = true,
			estimateOrientationApproximation = true,
			applyableProjection = true;

	public SQLAdjustmentManager(DataBase dataBase) {
		if (dataBase == null || !dataBase.isOpen())
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database must be open! " + dataBase);
		this.dataBase = dataBase;
	}

	private void setDataBaseSchema() throws SQLException {
		this.dataBase.getPreparedStatement("SET SCHEMA \"OpenAdjustment\"").execute();
	}
	
	private void checkDatabaseVersion() throws SQLException, DatabaseVersionMismatchException {
		final String sql = "SELECT \"version\" FROM \"Version\" WHERE \"type\" = ? LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, VersionType.DATABASE.getId());

		int databaseVersion = -1;
		ResultSet rs = stmt.executeQuery();
		if (rs.next())
			databaseVersion = rs.getInt("version");
		
		if (databaseVersion != Version.get(VersionType.DATABASE))
			throw new DatabaseVersionMismatchException("Error, database version of the stored project is greater than accepted database version of the application: " + databaseVersion + " > " +  Version.get(VersionType.DATABASE));
	}

	public NetworkAdjustment getNetworkAdjustment() throws SQLException, DatabaseVersionMismatchException, IllegalProjectionPropertyException {
		this.clear();
		
		this.setDataBaseSchema();
		this.checkDatabaseVersion();
		this.setProjection();

		this.networkAdjustment = new NetworkAdjustment();
		this.addAdjustmentDefinition(this.networkAdjustment);

		// Definition of test statistic
		TestStatisticDefinition testStatisticDefinition = this.getTestStatisticDefinition();
		if (testStatisticDefinition != null)
			this.networkAdjustment.setSignificanceTestStatisticDefinition(testStatisticDefinition);

		Map<String,Point> newPointsWithDeflection           = this.getPointsByType( PointType.NEW_POINT,   true );
		Map<String,Point> datumPointsWithDeflection         = this.getPointsByType( PointType.DATUM_POINT, true );
		Map<String,Point> stochasticPointsWithDeflection    = new LinkedHashMap<String,Point>();
		Map<String,Point> fixPointsWithDeflection           = new LinkedHashMap<String,Point>();

		Map<String,Point> newPointsWithoutDeflection        = this.getPointsByType( PointType.NEW_POINT,   false );
		Map<String,Point> datumPointsWithoutDeflection      = this.getPointsByType( PointType.DATUM_POINT, false );
		Map<String,Point> stochasticPointsWithoutDeflection = new LinkedHashMap<String,Point>();
		Map<String,Point> fixPointsWithoutDeflection        = new LinkedHashMap<String,Point>();

		this.completeNewPoints.putAll(newPointsWithDeflection);
		this.completeNewPoints.putAll(newPointsWithoutDeflection);

		this.completePointsWithDeflections.putAll(newPointsWithDeflection);
		this.completePointsWithDeflections.putAll(datumPointsWithDeflection);

		this.freeNetwork = datumPointsWithDeflection != null && !datumPointsWithDeflection.isEmpty() || datumPointsWithoutDeflection != null && !datumPointsWithoutDeflection.isEmpty();
		if (!this.freeNetwork) {
			stochasticPointsWithDeflection    = this.getPointsByType( PointType.STOCHASTIC_POINT, true );
			fixPointsWithDeflection           = this.getPointsByType( PointType.REFERENCE_POINT,  true );

			stochasticPointsWithoutDeflection = this.getPointsByType( PointType.STOCHASTIC_POINT, false );
			fixPointsWithoutDeflection        = this.getPointsByType( PointType.REFERENCE_POINT,  false );

			this.completePointsWithDeflections.putAll(stochasticPointsWithDeflection);
		}

		if (this.pure1DNetwork)
			this.applyableProjection = false;

		// wenn 2D Projektionen nicht moeglich sind, werden keine Reduktionen durchgefuehrt
		if (this.projection.getType() != ProjectionType.NONE && !this.applyableProjection) {
			throw new IllegalProjectionPropertyException("Projection cannot applied to observations! " + this.projection.getType());
		}

		// Fuege Beobachtungen zu den Punkten hinzu
		this.completeObservationGroups.addAll(this.getObservationGroups());

		// Fuege Punkt der Netzausgleichung zu
		for ( Point point : newPointsWithDeflection.values() ) 
			this.networkAdjustment.addNewPoint( point, false );
		for ( Point point : newPointsWithoutDeflection.values() ) 
			this.networkAdjustment.addNewPoint( point, true );

		// Nutze Deformationsvektoren zur Bildung von relativen Konfidenzbereichen (nicht nur bei freier AGL/Defo.-Analyse)
		this.congruenceAnalysisGroups.addAll(this.getCongruenceAnalysisGroups());

		for ( CongruenceAnalysisGroup congruenceAnalysisGroup : this.congruenceAnalysisGroups ) 
			this.networkAdjustment.addCongruenceAnalysisGroup(congruenceAnalysisGroup);

		if (this.freeNetwork) {
			this.addUserDefinedRankDefect(this.networkAdjustment.getRankDefect());
			for ( Point point : datumPointsWithDeflection.values() ) 
				this.networkAdjustment.addDatumPoint( point, false );
			for ( Point point : datumPointsWithoutDeflection.values() ) 
				this.networkAdjustment.addDatumPoint( point, true );
		}
		else {
			for ( Point point : fixPointsWithDeflection.values() ) 
				this.networkAdjustment.addFixPoint( point, false );
			for ( Point point : fixPointsWithoutDeflection.values() ) 
				this.networkAdjustment.addFixPoint( point, true );

			for ( Point point : stochasticPointsWithDeflection.values() ) 
				this.networkAdjustment.addStochasticPoint( point, false );
			for ( Point point : stochasticPointsWithoutDeflection.values() ) 
				this.networkAdjustment.addStochasticPoint( point, true );
		}
		// Auszugleichende Zusatzparameter
		for ( AdditionalUnknownParameter parameter : this.additionalParametersToBeEstimated.values() ) 
			this.networkAdjustment.addAdditionalUnknownParameter( parameter );

		return networkAdjustment;
	}
	
	public void clear() {
		this.completePoints.clear();
		this.completeNewPoints.clear();
		this.completePointsWithDeflections.clear();
		
		this.additionalParametersToBeEstimated.clear();
		
		this.congruenceAnalysisGroups.clear();
		this.completeObservationGroups.clear();

		if (this.networkAdjustment != null) {
			this.networkAdjustment.clearMatrices();
			this.networkAdjustment = null;
		}
	}

	private void setProjection() throws SQLException {
		String sql = "SELECT \"type\", \"reference_height\" FROM \"ProjectionDefinition\" WHERE \"id\" = 1 LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			ProjectionType type    = ProjectionType.getEnumByValue(rs.getInt("type"));
			double referenceHeight = rs.getDouble("reference_height");
			if (type != null) {
				this.projection.setType(type);
				this.projection.setReferenceHeight(referenceHeight);
			}
		}
	}

	private TestStatisticDefinition getTestStatisticDefinition() throws SQLException {
		String sql = "SELECT \"type\", \"probability_value\", \"power_of_test\", \"familywise_error_rate\" FROM \"TestStatisticDefinition\" WHERE \"id\" = 1 LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			TestStatisticType type      = TestStatisticType.getEnumByValue(rs.getInt("type"));
			double probabilityValue     = rs.getDouble("probability_value"); 
			double powerOfTest          = rs.getDouble("power_of_test");
			boolean familywiseErrorRate = rs.getBoolean("familywise_error_rate");

			if (type != null && probabilityValue > 0 && probabilityValue < 100 && powerOfTest > 0 && powerOfTest < 100)
				return new TestStatisticDefinition(type, probabilityValue, powerOfTest, familywiseErrorRate);
		}
		return null;
	}

	private void addAdjustmentDefinition(NetworkAdjustment adjustment) throws SQLException {
		String sql = "SELECT "
				+ "\"type\", \"number_of_iterations\", \"robust_estimation_limit\", "
				+ "\"number_of_principal_components\", \"apply_variance_of_unit_weight\", "
				+ "\"estimate_direction_set_orientation_approximation\", "
				+ "\"congruence_analysis\", \"export_covariance_matrix\" "
				+ "FROM \"AdjustmentDefinition\" "
				+ "WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			EstimationType type                   = EstimationType.getEnumByValue(rs.getInt("type"));
			int maximalNumberOfIterations         = rs.getInt("number_of_iterations");
			double robustEstimationLimit          = rs.getDouble("robust_estimation_limit");
			int numberOfPrincipalComponents       = rs.getInt("number_of_principal_components");
			this.estimateOrientationApproximation = rs.getBoolean("estimate_direction_set_orientation_approximation");
			this.congruenceAnalysis               = rs.getBoolean("congruence_analysis");
			boolean exportCovarianceMatrix        = rs.getBoolean("export_covariance_matrix");
			boolean applyVarianceOfUnitWeight     = rs.getBoolean("apply_variance_of_unit_weight");
			
			adjustment.setMaximalNumberOfIterations(maximalNumberOfIterations);
			adjustment.setRobustEstimationLimit(robustEstimationLimit);
			adjustment.setNumberOfPrincipalComponents(numberOfPrincipalComponents);
			adjustment.setEstimationType(type == null ? EstimationType.L2NORM : type);
			adjustment.setCongruenceAnalysis(this.congruenceAnalysis);
			adjustment.setApplyAposterioriVarianceOfUnitWeight(applyVarianceOfUnitWeight);
			// export path of covariance matrix
			if (exportCovarianceMatrix && this.dataBase instanceof HSQLDB) 
				this.networkAdjustment.setCovarianceExportPathAndBaseName(((HSQLDB)this.dataBase).getDataBaseFileName());
		}
	}

	private void addUserDefinedRankDefect(RankDefect rankDefect) throws SQLException {
		String sql = "SELECT "
				+ "\"user_defined\", "
				+ "\"ty\",\"tx\",\"tz\", "
				+ "\"ry\",\"rx\",\"rz\", "
				+ "\"sy\",\"sx\",\"sz\", "
				+ "\"my\",\"mx\",\"mz\", "
				+ "\"mxy\",\"mxyz\" "
				+ "FROM \"RankDefect\" WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			boolean userDefined = rs.getBoolean("user_defined");
			if (userDefined) {
				rankDefect.reset();

				if (rs.getBoolean("ty"))
					rankDefect.setTranslationYDefectType( DefectType.FREE );
				if (rs.getBoolean("tx"))
					rankDefect.setTranslationXDefectType( DefectType.FREE );
				if (rs.getBoolean("tz"))
					rankDefect.setTranslationZDefectType( DefectType.FREE );

				if (rs.getBoolean("ry"))
					rankDefect.setRotationYDefectType( DefectType.FREE );
				if (rs.getBoolean("rx"))
					rankDefect.setRotationXDefectType( DefectType.FREE );			
				if (rs.getBoolean("rz"))
					rankDefect.setRotationZDefectType( DefectType.FREE );

				if (rs.getBoolean("sy"))
					rankDefect.setShearYDefectType( DefectType.FREE );
				if (rs.getBoolean("sx"))
					rankDefect.setShearXDefectType( DefectType.FREE );
				if (rs.getBoolean("sz"))
					rankDefect.setShearZDefectType( DefectType.FREE );

				if (rs.getBoolean("my"))
					rankDefect.setScaleYDefectType( DefectType.FREE );
				if (rs.getBoolean("mx"))
					rankDefect.setScaleXDefectType( DefectType.FREE );
				if (rs.getBoolean("mz"))
					rankDefect.setScaleZDefectType( DefectType.FREE );

				if (rs.getBoolean("mxy"))
					rankDefect.setScaleXYDefectType( DefectType.FREE );
				if (rs.getBoolean("mxyz"))
					rankDefect.setScaleXYZDefectType( DefectType.FREE );

			}
		}
	}

	private Map<String, Point> getPointsByType (PointType type, boolean enabledDeflection) throws SQLException {

		String sql = "SELECT \"name\", \"y0\", \"x0\", \"z0\", \"dy0\", \"dx0\", \"dimension\", "
				+ "IFNULL(CASEWHEN( \"sigma_y0\"  > 0,  \"sigma_y0\" ,  (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_y0\", "
				+ "IFNULL(CASEWHEN( \"sigma_x0\"  > 0,  \"sigma_x0\" ,  (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_x0\", "
				+ "IFNULL(CASEWHEN( \"sigma_z0\"  > 0,  \"sigma_z0\" ,  (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_z0\", "
				+ "IFNULL(CASEWHEN( \"sigma_dy0\" > 0,  \"sigma_dy0\" , (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_dy0\", "
				+ "IFNULL(CASEWHEN( \"sigma_dx0\" > 0,  \"sigma_dx0\" , (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_dx0\"  "
				+ "FROM \"PointApriori\" "
				+ "JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" "
				+ "WHERE \"type\" = ? AND \"consider_deflection\" = ? AND \"PointGroup\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"dimension\" ASC, \"PointGroup\".\"id\" ASC, \"PointApriori\".\"id\" ASC";

		Map<String, Point> points = new LinkedHashMap<String,Point>();

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(idx++, PointGroupUncertaintyType.CONSTANT_Y.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyY());

		stmt.setInt(idx++, PointGroupUncertaintyType.CONSTANT_X.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyX());

		stmt.setInt(idx++, PointGroupUncertaintyType.CONSTANT_Z.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyZ());

		stmt.setInt(idx++, PointGroupUncertaintyType.DEFLECTION_Y.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyDeflectionY());

		stmt.setInt(idx++, PointGroupUncertaintyType.DEFLECTION_X.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyDeflectionX());

		stmt.setInt(idx++, type.getId());
		stmt.setBoolean(idx++, enabledDeflection);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String name = rs.getString("name");
			int dimension = rs.getInt("dimension");

			double y0 = rs.getDouble("y0");
			double x0 = rs.getDouble("x0");
			double z0 = rs.getDouble("z0");

			double sigmaY0 = rs.getDouble("sigma_y0");	
			double sigmaX0 = rs.getDouble("sigma_x0");		
			double sigmaZ0 = rs.getDouble("sigma_z0");

			double sigmaDeflectionY0 = rs.getDouble("sigma_dy0");
			double sigmaDeflectionX0 = rs.getDouble("sigma_dx0");

			double deflectionY0 = 0;
			double deflectionX0 = 0;

			if (enabledDeflection && dimension == 3) {
				deflectionY0 = rs.getDouble("dy0");
				if (rs.wasNull())
					continue;

				deflectionX0 = rs.getDouble("dx0");
				if (rs.wasNull())
					continue;
			}

			Point point = null;

			switch(dimension) {
			case 1:
				point = new Point1D(name, x0, y0, z0, sigmaZ0);
				break;
			case 2:
				point = new Point2D(name, x0, y0, z0, sigmaX0, sigmaY0);
				this.pure1DNetwork = false;
				if (y0 < 1100000 || y0 > 59800000 )
					this.applyableProjection = false;
				break;
			case 3:
				point = new Point3D(name, x0, y0, z0, sigmaX0, sigmaY0, sigmaZ0);

				if (enabledDeflection) {
					point.getDeflectionX().setValue0(deflectionX0);
					point.getDeflectionX().setStdApriori(sigmaDeflectionX0);
					point.getDeflectionY().setValue0(deflectionY0);
					point.getDeflectionY().setStdApriori(sigmaDeflectionY0);
				}


				this.pure1DNetwork = false;
				if (y0 < 1100000 || y0 > 59800000 )
					this.applyableProjection = false;
				break;
			}
			if (point != null && !this.completePoints.containsKey(point.getName())) {
				points.put(point.getName(), point);
				this.completePoints.put(point.getName(), point);
			}
		}
		return points;
	}


	private List<ObservationGroup> getObservationGroups() throws SQLException {
		List<ObservationGroup> observationGroups = new ArrayList<ObservationGroup>();

		String sql = "SELECT \"id\", \"type\", \"reference_epoch\", "
				+ "\"UncertaintyZP\".\"value\"   AS \"sigma_0_zero_point\", "
				+ "\"UncertaintySRD\".\"value\"  AS \"sigma_0_square_root_distance\", "
				+ "\"UncertaintyDIST\".\"value\" AS \"sigma_0_distance\" "
				+ "FROM \"ObservationGroup\" "
				+ "LEFT JOIN \"ObservationGroupUncertainty\" AS \"UncertaintyZP\"   ON \"UncertaintyZP\".\"group_id\"   = \"ObservationGroup\".\"id\" AND \"UncertaintyZP\".\"type\"   = ? "
				+ "LEFT JOIN \"ObservationGroupUncertainty\" AS \"UncertaintySRD\"  ON \"UncertaintySRD\".\"group_id\"  = \"ObservationGroup\".\"id\" AND \"UncertaintySRD\".\"type\"  = ? "
				+ "LEFT JOIN \"ObservationGroupUncertainty\" AS \"UncertaintyDIST\" ON \"UncertaintyDIST\".\"group_id\" = \"ObservationGroup\".\"id\" AND \"UncertaintyDIST\".\"type\" = ? "
				+ "WHERE \"ObservationGroup\".\"enable\" = TRUE " // AND \"ObservationGroup\".\"type\" = 2
				+ "ORDER BY \"ObservationGroup\".\"type\" ASC, \"ObservationGroup\".\"id\" ASC";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, ObservationGroupUncertaintyType.ZERO_POINT_OFFSET.getId());
		stmt.setInt(idx++, ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT.getId());
		stmt.setInt(idx++, ObservationGroupUncertaintyType.DISTANCE_DEPENDENT.getId());

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			ObservationType type = ObservationType.getEnumByValue(rs.getInt("type"));

			if (type == null)
				continue;

			int groupId = rs.getInt("id");
			Epoch epoch = rs.getBoolean("reference_epoch") ? Epoch.REFERENCE : Epoch.CONTROL;

			double sigmaZeroPointOffset = -1;
			double sigmaSquareRootDistance = -1;
			double sigmaDistance = -1;

			ObservationGroup group = null;
			switch(type) {
			case LEVELING:
				sigmaZeroPointOffset = rs.getDouble("sigma_0_zero_point");
				if (rs.wasNull() || sigmaZeroPointOffset <= 0)
					sigmaZeroPointOffset = DefaultUncertainty.getUncertaintyLevelingZeroPointOffset();

				sigmaSquareRootDistance = rs.getDouble("sigma_0_square_root_distance");
				if (rs.wasNull() || sigmaSquareRootDistance < 0)
					sigmaSquareRootDistance = DefaultUncertainty.getUncertaintyLevelingSquareRootDistanceDependent();

				sigmaDistance = rs.getDouble("sigma_0_distance");
				if (rs.wasNull() || sigmaDistance < 0)
					sigmaDistance = DefaultUncertainty.getUncertaintyLevelingDistanceDependent();

				group = new DeltaZGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);

				this.addAdditionalGroupParameters(group);
				this.addTerrestrialObservations(group);
				break;

			case DIRECTION:
			case ZENITH_ANGLE:
				sigmaZeroPointOffset = rs.getDouble("sigma_0_zero_point");
				if (rs.wasNull() || sigmaZeroPointOffset <= 0)
					sigmaZeroPointOffset = DefaultUncertainty.getUncertaintyAngleZeroPointOffset();

				sigmaSquareRootDistance = rs.getDouble("sigma_0_square_root_distance");
				if (rs.wasNull() || sigmaSquareRootDistance < 0)
					sigmaSquareRootDistance = DefaultUncertainty.getUncertaintyAngleSquareRootDistanceDependent();

				sigmaDistance = rs.getDouble("sigma_0_distance");
				if (rs.wasNull() || sigmaDistance < 0)
					sigmaDistance = DefaultUncertainty.getUncertaintyAngleDistanceDependent();

				if (type == ObservationType.DIRECTION)
					group = new DirectionGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch); 
				else 
					group = new ZenithAngleGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);

				this.addAdditionalGroupParameters(group);
				this.addTerrestrialObservations(group);
				break;

			case HORIZONTAL_DISTANCE:
			case SLOPE_DISTANCE:
				sigmaZeroPointOffset = rs.getDouble("sigma_0_zero_point");
				if (rs.wasNull() || sigmaZeroPointOffset <= 0)
					sigmaZeroPointOffset = DefaultUncertainty.getUncertaintyDistanceZeroPointOffset();

				sigmaSquareRootDistance = rs.getDouble("sigma_0_square_root_distance");
				if (rs.wasNull() || sigmaSquareRootDistance < 0)
					sigmaSquareRootDistance = DefaultUncertainty.getUncertaintyDistanceSquareRootDistanceDependent();

				sigmaDistance = rs.getDouble("sigma_0_distance");
				if (rs.wasNull() || sigmaDistance < 0)
					sigmaDistance = DefaultUncertainty.getUncertaintyDistanceDistanceDependent();

				if (type == ObservationType.HORIZONTAL_DISTANCE)
					group = new HorizontalDistanceGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch); 
				else
					group = new SlopeDistanceGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);

				this.addAdditionalGroupParameters(group);
				this.addTerrestrialObservations(group);
				break;

			case GNSS1D:
			case GNSS2D:
			case GNSS3D:
				sigmaZeroPointOffset = rs.getDouble("sigma_0_zero_point");
				if (rs.wasNull() || sigmaZeroPointOffset <= 0)
					sigmaZeroPointOffset = DefaultUncertainty.getUncertaintyGNSSZeroPointOffset();

				sigmaSquareRootDistance = rs.getDouble("sigma_0_square_root_distance");
				if (rs.wasNull() || sigmaSquareRootDistance < 0)
					sigmaSquareRootDistance = DefaultUncertainty.getUncertaintyGNSSSquareRootDistanceDependent();

				sigmaDistance = rs.getDouble("sigma_0_distance");
				if (rs.wasNull() || sigmaDistance < 0)
					sigmaDistance = DefaultUncertainty.getUncertaintyGNSSDistanceDependent();

				if (type == ObservationType.GNSS1D)
					group = new GNSSBaseline1DGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);
				else if (type == ObservationType.GNSS2D)
					group = new GNSSBaseline2DGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);
				else
					group = new GNSSBaseline3DGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);

				this.addAdditionalGroupParameters(group);
				this.addGNSSObservations(group);
				break;
			}

			if (group != null && !group.isEmpty())
				observationGroups.add(group);
		}

		return observationGroups;
	}

	private void addTerrestrialObservations(ObservationGroup observationGroup) throws SQLException {
		String sql = "SELECT \"id\", \"start_point_name\", \"end_point_name\", \"instrument_height\", \"reflector_height\", \"value_0\", \"sigma_0\", \"distance_0\" "
				+ "FROM \"ObservationApriori\" "
				+ "WHERE \"group_id\" = ? AND \"enable\" = TRUE "
				+ "ORDER BY \"id\" ASC";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, observationGroup.getId());

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			int id = rs.getInt("id");

			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");

			double startPointHeight = rs.getDouble("instrument_height");
			double endPointHeight   = rs.getDouble("reflector_height");

			double observation0 = rs.getDouble("value_0");
			double sigma0       = rs.getDouble("sigma_0");

			double distanceForUncertaintyModel = rs.getDouble("distance_0");

			Point startPoint = this.completePoints.get(startPointName);
			Point endPoint   = this.completePoints.get(endPointName);

			if (startPoint == null || endPoint == null)
				continue;

			Observation observation = null;

			if (observationGroup instanceof DeltaZGroup && startPoint.getDimension() != 2 && endPoint.getDimension() != 2)
				observation = new DeltaZ(id, startPoint, endPoint, startPointHeight, endPointHeight, observation0, sigma0, distanceForUncertaintyModel);

			else if (observationGroup instanceof DirectionGroup && startPoint.getDimension() != 1 && endPoint.getDimension() != 1) 
				observation = new Direction(id, startPoint, endPoint, startPointHeight, endPointHeight, observation0, sigma0, distanceForUncertaintyModel);

			else if (observationGroup instanceof HorizontalDistanceGroup && startPoint.getDimension() != 1 && endPoint.getDimension() != 1) 
				observation = new HorizontalDistance(id, startPoint, endPoint, startPointHeight, endPointHeight, observation0, sigma0, distanceForUncertaintyModel);

			else if (observationGroup instanceof SlopeDistanceGroup && startPoint.getDimension() == 3 && endPoint.getDimension() == 3) 
				observation = new SlopeDistance(id, startPoint, endPoint, startPointHeight, endPointHeight, observation0, sigma0, distanceForUncertaintyModel);

			else if (observationGroup instanceof ZenithAngleGroup && startPoint.getDimension() == 3 && endPoint.getDimension() == 3) 
				observation = new ZenithAngle(id, startPoint, endPoint, startPointHeight, endPointHeight, observation0, sigma0, distanceForUncertaintyModel);

			if (observation != null) {
				observation.setProjectionScheme(this.projection);
				observationGroup.add(observation);
			}
		}
	}

	private void addGNSSObservations(ObservationGroup observationGroup) throws SQLException {
		String sql = "SELECT \"id\", \"start_point_name\", \"end_point_name\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\" "
				+ "FROM \"GNSSObservationApriori\" "
				+ "WHERE \"group_id\" = ? AND \"enable\" = TRUE "
				+ "ORDER BY \"id\" ASC";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, observationGroup.getId());

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			int id = rs.getInt("id");

			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");

			double y0 = rs.getDouble("y0");
			double x0 = rs.getDouble("x0");
			double z0 = rs.getDouble("z0");

			double sigmaY0 = rs.getDouble("sigma_y0");
			double sigmaX0 = rs.getDouble("sigma_x0");
			double sigmaZ0 = rs.getDouble("sigma_z0");

			Point startPoint = this.completePoints.get(startPointName);
			Point endPoint   = this.completePoints.get(endPointName);

			if (startPoint == null || endPoint == null)
				continue;


			if (observationGroup instanceof GNSSBaseline1DGroup && startPoint.getDimension() != 2 && endPoint.getDimension() != 2) {
				GNSSBaselineDeltaZ1D gnssZ = new GNSSBaselineDeltaZ1D(id, startPoint, endPoint, z0, sigmaZ0);

				gnssZ.setProjectionScheme(this.projection);

				((GNSSBaseline1DGroup)observationGroup).add(gnssZ);
			}
			else if (observationGroup instanceof GNSSBaseline2DGroup && startPoint.getDimension() != 1 && endPoint.getDimension() != 1) {
				GNSSBaselineDeltaY2D gnssY = new GNSSBaselineDeltaY2D(id, startPoint, endPoint, y0, sigmaY0);
				GNSSBaselineDeltaX2D gnssX = new GNSSBaselineDeltaX2D(id, startPoint, endPoint, x0, sigmaX0);

				gnssX.setProjectionScheme(this.projection);
				gnssY.setProjectionScheme(this.projection);

				((GNSSBaseline2DGroup)observationGroup).add(gnssX, gnssY);

			}
			else if (observationGroup instanceof GNSSBaseline3DGroup && startPoint.getDimension() == 3 && endPoint.getDimension() == 3) {
				GNSSBaselineDeltaY3D gnssY = new GNSSBaselineDeltaY3D(id, startPoint, endPoint, y0, sigmaY0);
				GNSSBaselineDeltaX3D gnssX = new GNSSBaselineDeltaX3D(id, startPoint, endPoint, x0, sigmaX0);
				GNSSBaselineDeltaZ3D gnssZ = new GNSSBaselineDeltaZ3D(id, startPoint, endPoint, z0, sigmaZ0);

				gnssX.setProjectionScheme(this.projection);
				gnssY.setProjectionScheme(this.projection);
				gnssZ.setProjectionScheme(this.projection);

				((GNSSBaseline3DGroup)observationGroup).add(gnssX, gnssY, gnssZ);
			}
		}
	}

	private void addAdditionalGroupParameters(ObservationGroup observationGroup) throws SQLException {
		String sql = "SELECT \"id\", \"type\", \"value_0\", \"enable\" "
				+ "FROM \"AdditionalParameterApriori\" WHERE \"group_id\" = ? "
				+ "ORDER BY \"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, observationGroup.getId());
		// additionalParametersToBeEstimated
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ParameterType type = ParameterType.getEnumByValue(rs.getInt("type"));

			int id         = rs.getInt("id");
			double value0  = rs.getDouble("value_0");
			boolean enable = rs.getBoolean("enable");

			if (type == null || this.additionalParametersToBeEstimated.containsKey(id))
				continue;

			AdditionalUnknownParameter parameter = null;

			switch(type) {
			case ORIENTATION:
				if (observationGroup instanceof DirectionGroup) {
					parameter = ((DirectionGroup)observationGroup).getOrientation();
					parameter.setValue( value0 );
					((Orientation)parameter).setEstimateApproximationValue(this.estimateOrientationApproximation);
				}
				break;

			case ZERO_POINT_OFFSET:
				if (observationGroup instanceof HorizontalDistanceGroup) {
					parameter = ((HorizontalDistanceGroup)observationGroup).getZeroPointOffset();
					parameter.setValue( value0 );
				}

				else if (observationGroup instanceof SlopeDistanceGroup) {
					parameter = ((SlopeDistanceGroup)observationGroup).getZeroPointOffset();
					parameter.setValue( value0 );
				}
				break;

			case SCALE:
				if (observationGroup instanceof DeltaZGroup) {
					parameter = ((DeltaZGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof HorizontalDistanceGroup) {
					parameter = ((HorizontalDistanceGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof SlopeDistanceGroup) {
					parameter = ((SlopeDistanceGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline1DGroup) {
					parameter = ((GNSSBaseline1DGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline2DGroup) {
					parameter = ((GNSSBaseline2DGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline3DGroup) {
					parameter = ((GNSSBaseline3DGroup)observationGroup).getScale();
					parameter.setValue( value0 );
				}
				break;

			case REFRACTION_INDEX:
				if (observationGroup instanceof ZenithAngleGroup) {
					parameter = ((ZenithAngleGroup)observationGroup).getRefractionCoefficient();
					parameter.setValue( value0 );
				}
				break;

			case ROTATION_X:
				if (observationGroup instanceof GNSSBaseline1DGroup) {
					parameter = ((GNSSBaseline1DGroup)observationGroup).getRotationX();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline3DGroup) {
					parameter = ((GNSSBaseline3DGroup)observationGroup).getRotationX();
					parameter.setValue( value0 );
				}
				break;

			case ROTATION_Y:
				if (observationGroup instanceof GNSSBaseline1DGroup) {
					parameter = ((GNSSBaseline1DGroup)observationGroup).getRotationY();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline3DGroup) {
					parameter = ((GNSSBaseline3DGroup)observationGroup).getRotationY();
					parameter.setValue( value0 );
				}
				break;

			case ROTATION_Z:
				if (observationGroup instanceof GNSSBaseline2DGroup) {
					parameter = ((GNSSBaseline2DGroup)observationGroup).getRotationZ();
					parameter.setValue( value0 );
				}
				else if (observationGroup instanceof GNSSBaseline3DGroup) {
					parameter = ((GNSSBaseline3DGroup)observationGroup).getRotationZ();
					parameter.setValue( value0 );
				}
				break;


			default:
				break;

			}

			if (parameter != null && enable)
				this.additionalParametersToBeEstimated.put(id, parameter);
		}
	}


	private List<CongruenceAnalysisGroup> getCongruenceAnalysisGroups() throws SQLException {
		List<CongruenceAnalysisGroup> congruenceAnalysisGroups = new ArrayList<CongruenceAnalysisGroup>();

		String sql = "SELECT \"id\", \"dimension\" "
				+ "FROM \"CongruenceAnalysisGroup\" "
				+ "WHERE \"enable\" = TRUE";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while(rs.next()) {
			int id        = rs.getInt("id");
			int dimension = rs.getInt("dimension");
			if (dimension > 0 && dimension < 4) {
				CongruenceAnalysisGroup congruenceAnalysisGroup = new CongruenceAnalysisGroup(id, dimension);
				this.addCongruenceAnalysisPointPair(congruenceAnalysisGroup);
				if (!congruenceAnalysisGroup.isEmpty()) {
					if (this.freeNetwork && this.congruenceAnalysis)
						this.setStrainAnalysisRestrictionsToGroup(congruenceAnalysisGroup);
					congruenceAnalysisGroups.add(congruenceAnalysisGroup);
				}
			}
		}

		return congruenceAnalysisGroups;
	}

	private void addCongruenceAnalysisPointPair(CongruenceAnalysisGroup congruenceAnalysisGroup) throws SQLException {
		String sql = "SELECT \"id\", \"start_point_name\", \"end_point_name\" "
				+ "FROM \"CongruenceAnalysisPointPairApriori\" WHERE \"group_id\" = ? AND \"enable\" = TRUE "
				+ "ORDER BY \"id\" ASC";

		int dimension = congruenceAnalysisGroup.getDimension();

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, congruenceAnalysisGroup.getId());
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt("id");
			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");

			if (!this.completePoints.containsKey(startPointName) || !this.completePoints.containsKey(endPointName))
				continue;

			Point startPoint = this.completePoints.get(startPointName);
			Point endPoint   = this.completePoints.get(endPointName);

			if (startPoint.getDimension() < dimension || endPoint.getDimension() < dimension || 
					startPoint.getDimension() == 2 && endPoint.getDimension() == 1 ||
					startPoint.getDimension() == 1 && endPoint.getDimension() == 2) 
				continue;

			CongruenceAnalysisPointPair pointPointPair = new CongruenceAnalysisPointPair(id, dimension, startPoint, endPoint);
			boolean isAnalysable = this.completeNewPoints.containsKey(startPointName) && this.completeNewPoints.containsKey(endPointName);
			congruenceAnalysisGroup.add(pointPointPair, isAnalysable);
		}
	}

	private void setStrainAnalysisRestrictionsToGroup(CongruenceAnalysisGroup group) throws SQLException {
		String sql = "SELECT "
				+ "\"type\", \"enable\" "
				+ "FROM \"CongruenceAnalysisStrainParameterRestriction\" "
				+ "WHERE \"group_id\" = ?";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, group.getId());

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			int type = rs.getInt("type");
			boolean enable = rs.getBoolean("enable");
			RestrictionType restriction = RestrictionType.getEnumByValue(type);
			if (restriction != null && !enable)
				group.setStrainRestrictions(restriction);
		}
	}

	/** SAVE RESULTS **/

	public void saveResults() throws SQLException {
		try {
			if (this.networkAdjustment != null) {
				// Tabelle fuer Daten nach der AGL leeren
				this.clearAposterioriTables();

				this.savePoints();
				this.saveDeflections();

				this.saveObservations();
				this.saveAdditionalParameters();

				this.saveCongruenceAnalysisPointPair();
				this.saveStrainParameters();

				this.savePrincipalComponentAnalysis(this.networkAdjustment.getPrincipalComponents());
				this.saveRankDefect(this.networkAdjustment.getRankDefect());
				this.saveTestStatistic(this.networkAdjustment.getSignificanceTestStatisticParameters());
				this.saveVarianceComponents(this.networkAdjustment.getVarianceComponents());
				this.saveProjection();
				
				this.saveVersion();
			}
		}
		finally {
			this.clear();
		}
	}
	
	private void saveVersion() throws SQLException {
		String sql = "UPDATE \"Version\" SET \"version\" = ? WHERE \"type\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setDouble(idx++, Version.get(VersionType.ADJUSTMENT_CORE));
		stmt.setInt(idx++, VersionType.ADJUSTMENT_CORE.getId());
		stmt.execute();
	}

	private void clearAposterioriTables() throws SQLException {
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"PointAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"DeflectionAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"ObservationAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"GNSSObservationAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"AdditionalParameterAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"CongruenceAnalysisPointPairAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"CongruenceAnalysisStrainParameterAposteriori\"").execute();

		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"VarianceComponent\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"TestStatistic\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"PrincipalComponent\"").execute();
	}

	private void savePoints() throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"PointAposteriori\" (" +
				"\"id\",\"y\",\"x\",\"z\"," +
				"\"sigma_y0\",\"sigma_x0\",\"sigma_z0\"," +
				"\"sigma_y\",\"sigma_x\",\"sigma_z\"," +
				"\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\"," +
				"\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\"," +
				"\"helmert_major_axis\",\"helmert_minor_axis\",\"helmert_alpha\"," +
				"\"residual_y\",\"residual_x\",\"residual_z\"," +
				"\"redundancy_y\",\"redundancy_x\",\"redundancy_z\"," +
				"\"gross_error_y\",\"gross_error_x\",\"gross_error_z\"," +
				"\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\"," +
				"\"influence_on_position_y\",\"influence_on_position_x\",\"influence_on_position_z\"," +
				"\"influence_on_network_distortion\", " + 
				"\"first_principal_component_y\",\"first_principal_component_x\",\"first_principal_component_z\"," +
				"\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\") VALUES (" +
				"(SELECT \"id\" FROM \"PointApriori\" WHERE \"name\" = ?), " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for (Point point : this.completePoints.values() ) {
				int idx = 1;
				int dimension = point.getDimension();

				stmt.setString(idx++, point.getName());

				stmt.setDouble(idx++, point.getY());
				stmt.setDouble(idx++, point.getX());
				stmt.setDouble(idx++, dimension != 2 ? point.getZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 && point.getStdYApriori() > 0 ? point.getStdYApriori() : 0.0);
				stmt.setDouble(idx++, dimension != 1 && point.getStdXApriori() > 0 ? point.getStdXApriori() : 0.0);
				stmt.setDouble(idx++, dimension != 2 && point.getStdZApriori() > 0 ? point.getStdZApriori() : 0.0);

				stmt.setDouble(idx++, dimension != 1 && point.getStdY() > 0 ? point.getStdY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 && point.getStdX() > 0 ? point.getStdX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 && point.getStdZ() > 0 ? point.getStdZ() : 0.0);

				stmt.setDouble(idx++, point.getConfidenceAxis(0));
				stmt.setDouble(idx++, dimension == 3 ? point.getConfidenceAxis(1) : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getConfidenceAxis(dimension-1) : 0.0);		

				stmt.setDouble(idx++, dimension > 2 ? point.getConfidenceAngle(0) : 0.0);
				stmt.setDouble(idx++, dimension > 2 ? point.getConfidenceAngle(1) : 0.0);
				stmt.setDouble(idx++, dimension > 1 ? point.getConfidenceAngle(2) : 0.0);	

				stmt.setDouble(idx++, point.getConfidenceAxis2D(0));
				stmt.setDouble(idx++, dimension != 1 ? point.getConfidenceAxis2D(1) : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getConfidenceAngle2D() : 0.0);
				
				stmt.setDouble(idx++, dimension != 1 ? point.getY0() - point.getY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getX0() - point.getX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getZ0() - point.getZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getRedundancyY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getRedundancyX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getRedundancyZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getGrossErrorY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getGrossErrorX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getGrossErrorZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getMinimalDetectableBiasY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getMinimalDetectableBiasX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getMinimalDetectableBiasZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getInfluenceOnPointPositionY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getInfluenceOnPointPositionX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getInfluenceOnPointPositionZ() : 0.0);

				stmt.setDouble(idx++, point.getInfluenceOnNetworkDistortion());

				stmt.setDouble(idx++, dimension != 1 ? point.getFirstPrincipalComponentY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getFirstPrincipalComponentX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getFirstPrincipalComponentZ() : 0.0);

				stmt.setDouble(idx++, point.getOmega());

				stmt.setDouble(idx++, point.getPprio());
				stmt.setDouble(idx++, point.getPpost());

				stmt.setDouble(idx++, point.getTprio());
				stmt.setDouble(idx++, point.getTpost());

				stmt.setBoolean(idx++, point.isSignificant());

				stmt.setInt(idx++, point.getColInJacobiMatrix());

				stmt.addBatch();
				hasBatch = true;
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveDeflections() throws SQLException {
		boolean hasBatch = false;
		String sql = "INSERT INTO \"DeflectionAposteriori\" (" +
				"\"id\",\"dy\",\"dx\"," +
				"\"sigma_dy0\",\"sigma_dx0\"," +
				"\"sigma_dy\",\"sigma_dx\"," +
				"\"confidence_major_axis\",\"confidence_minor_axis\"," +
				"\"residual_dy\",\"residual_dx\"," +
				"\"redundancy_dy\",\"redundancy_dx\"," +
				"\"gross_error_dy\",\"gross_error_dx\"," +
				"\"minimal_detectable_bias_dy\",\"minimal_detectable_bias_dx\"," +
				"\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\") VALUES (" +
				"(SELECT \"id\" FROM \"PointApriori\" WHERE \"name\" = ?), " +
				"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for (Point point : this.completePointsWithDeflections.values() ) {
				int idx = 1;
				int dimension = point.getDimension();

				if (dimension == 3 && point.considerDeflection()) {
					stmt.setString(idx++, point.getName());

					stmt.setDouble(idx++, point.getDeflectionY().getValue());
					stmt.setDouble(idx++, point.getDeflectionX().getValue());

					stmt.setDouble(idx++, (point.getDeflectionY().getStdApriori() > 0 ? point.getDeflectionY().getStdApriori() : 0.0));
					stmt.setDouble(idx++, (point.getDeflectionX().getStdApriori() > 0 ? point.getDeflectionX().getStdApriori() : 0.0));

					stmt.setDouble(idx++, point.getDeflectionY().getStd() > 0 ? point.getDeflectionY().getStd() : 0.0);
					stmt.setDouble(idx++, point.getDeflectionX().getStd() > 0 ? point.getDeflectionX().getStd() : 0.0);

					stmt.setDouble(idx++, Math.max(point.getDeflectionX().getConfidence(), point.getDeflectionY().getConfidence()));
					stmt.setDouble(idx++, Math.min(point.getDeflectionX().getConfidence(), point.getDeflectionY().getConfidence()));

					stmt.setDouble(idx++, point.getDeflectionY().getValue0() - point.getDeflectionY().getValue());
					stmt.setDouble(idx++, point.getDeflectionX().getValue0() - point.getDeflectionX().getValue());
					
					stmt.setDouble(idx++, point.getDeflectionY().getRedundancy());
					stmt.setDouble(idx++, point.getDeflectionX().getRedundancy());

					stmt.setDouble(idx++, point.getDeflectionY().getGrossError());
					stmt.setDouble(idx++, point.getDeflectionX().getGrossError());

					stmt.setDouble(idx++, point.getDeflectionY().getMinimalDetectableBias());
					stmt.setDouble(idx++, point.getDeflectionX().getMinimalDetectableBias());

					stmt.setDouble(idx++, point.getDeflectionX().getOmega() + point.getDeflectionY().getOmega());

					// Statistische Groessen in X abgelegt
					stmt.setDouble(idx++, point.getDeflectionX().getPprio());
					stmt.setDouble(idx++, point.getDeflectionX().getPpost());

					stmt.setDouble(idx++, point.getDeflectionX().getTprio()); 
					stmt.setDouble(idx++, point.getDeflectionX().getTpost());

					stmt.setBoolean(idx++, point.getDeflectionX().isSignificant());

					stmt.setInt(idx++, Math.min(point.getDeflectionX().getColInJacobiMatrix(), point.getDeflectionY().getColInJacobiMatrix()));

					stmt.addBatch();

					hasBatch = true;
				}
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}


	private void saveObservations() throws SQLException {
		boolean hasBatch = false;
		String sqlTerObs = "INSERT INTO \"ObservationAposteriori\" ("
				+ "\"id\", \"value\", "
				+ "\"sigma_0\", \"sigma\", "
				+ "\"residual\", "
				+ "\"redundancy\", "
				+ "\"gross_error\", \"minimal_detectable_bias\", "
				+ "\"influence_on_position\", \"influence_on_network_distortion\", "
				+ "\"omega\", "
				+ "\"p_prio\", \"p_post\", "
				+ "\"t_prio\", \"t_post\", "
				+ "\"significant\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String sqlGNSSObs = "INSERT INTO \"GNSSObservationAposteriori\" (" +
				"\"id\", \"y\", \"x\", \"z\", " +
				"\"sigma_y0\", \"sigma_x0\", \"sigma_z0\", " +
				"\"sigma_y\", \"sigma_x\", \"sigma_z\", " +
				"\"residual_y\", \"residual_x\", \"residual_z\", " +
				"\"redundancy_y\", \"redundancy_x\", \"redundancy_z\", " +
				"\"gross_error_y\", \"gross_error_x\", \"gross_error_z\", " +
				"\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\", " +
				"\"influence_on_position_y\", \"influence_on_position_x\", \"influence_on_position_z\", " +
				"\"influence_on_network_distortion\", " +
				"\"omega\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"significant\") " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,?, ?, ?, ?, ?, ?, ?)"; 

		try {
			this.dataBase.setAutoCommit(false);
			Set<Integer> gnssObservationIds = new LinkedHashSet<Integer>();
			for (ObservationGroup observationGroup : this.completeObservationGroups ) {
				gnssObservationIds.clear();
				boolean isGNSS = (observationGroup instanceof GNSSBaseline1DGroup || observationGroup instanceof GNSSBaseline2DGroup || observationGroup instanceof GNSSBaseline3DGroup);
				PreparedStatement stmt;
				if (isGNSS)
					stmt = this.dataBase.getPreparedStatement(sqlGNSSObs);
				else
					stmt = this.dataBase.getPreparedStatement(sqlTerObs);

				int len = observationGroup.size();

				for (int i=0; i < len; i++ ) {
					Observation observation = observationGroup.get(i);
					int idx = 1;
					if (isGNSS) {
						if (gnssObservationIds.contains(observation.getId()))
							continue;
						gnssObservationIds.add(observation.getId());

						GNSSBaseline gnssBaseline = (GNSSBaseline)observation;

						GNSSBaseline gnssY = gnssBaseline.getBaselineComponent(ComponentType.Y);
						GNSSBaseline gnssX = gnssBaseline.getBaselineComponent(ComponentType.X);
						GNSSBaseline gnssZ = gnssBaseline.getBaselineComponent(ComponentType.Z);

						double omega = 0;
						omega += gnssY == null ? 0.0 : gnssY.getOmega();
						omega += gnssX == null ? 0.0 : gnssX.getOmega();
						omega += gnssZ == null ? 0.0 : gnssZ.getOmega();

						stmt.setInt(idx++,    gnssBaseline.getId());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getValueAposteriori());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getValueAposteriori());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getValueAposteriori());

						stmt.setDouble(idx++, gnssY == null || gnssY.getStdApriori() < 0 ? 0.0 : gnssY.getStdApriori());
						stmt.setDouble(idx++, gnssX == null || gnssX.getStdApriori() < 0 ? 0.0 : gnssX.getStdApriori());
						stmt.setDouble(idx++, gnssZ == null || gnssZ.getStdApriori() < 0 ? 0.0 : gnssZ.getStdApriori());

						stmt.setDouble(idx++, gnssY == null || gnssY.getStd() < 0 ? 0.0 : gnssY.getStd());
						stmt.setDouble(idx++, gnssX == null || gnssX.getStd() < 0 ? 0.0 : gnssX.getStd());
						stmt.setDouble(idx++, gnssZ == null || gnssZ.getStd() < 0 ? 0.0 : gnssZ.getStd());
						
						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getCorrection());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getCorrection());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getCorrection());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getRedundancy());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getRedundancy());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getRedundancy());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getGrossError());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getGrossError());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getGrossError());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getMinimalDetectableBias());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getMinimalDetectableBias());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getMinimalDetectableBias());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getInfluenceOnPointPosition());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getInfluenceOnPointPosition());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getInfluenceOnPointPosition());

						stmt.setDouble(idx++, gnssBaseline.getInfluenceOnNetworkDistortion());

						stmt.setDouble(idx++, omega);

						stmt.setDouble(idx++,  gnssBaseline.getPprio());
						stmt.setDouble(idx++,  gnssBaseline.getPpost());

						stmt.setDouble(idx++,  gnssBaseline.getTprio());
						stmt.setDouble(idx++,  gnssBaseline.getTpost());

						stmt.setBoolean(idx++, gnssBaseline.isSignificant());
					}
					else {
						double value = observation.getValueAposteriori();

						if (observation.getObservationType() == ObservationType.DIRECTION || observation.getObservationType() == ObservationType.ZENITH_ANGLE) {
							if (observation instanceof Direction) {
								double face = ((Direction)observation).getFace() == FaceType.ONE ? 0.0 : 1.0;
								value = MathExtension.MOD(value - face * Math.PI, 2.0 * Math.PI); 
							}
							else if (observation instanceof ZenithAngle) {
								double face = ((ZenithAngle)observation).getFace() == FaceType.ONE ? 0.0 : 1.0;
								value = Math.abs(face * 2.0 * Math.PI - value); 
							}
						}

						stmt.setInt(idx++, observation.getId());

						stmt.setDouble(idx++, value);
						stmt.setDouble(idx++, observation.getStdApriori() > 0 ? observation.getStdApriori() : 0.0);
						stmt.setDouble(idx++, observation.getStd() > 0 ? observation.getStd() : 0.0);

						stmt.setDouble(idx++, observation.getCorrection());
						stmt.setDouble(idx++, observation.getRedundancy());

						stmt.setDouble(idx++, observation.getGrossError());
						stmt.setDouble(idx++, observation.getMinimalDetectableBias());

						stmt.setDouble(idx++, observation.getInfluenceOnPointPosition());
						stmt.setDouble(idx++, observation.getInfluenceOnNetworkDistortion());

						stmt.setDouble(idx++, observation.getOmega());

						stmt.setDouble(idx++, observation.getPprio());
						stmt.setDouble(idx++, observation.getPpost());

						stmt.setDouble(idx++, observation.getTprio());
						stmt.setDouble(idx++, observation.getTpost());

						stmt.setBoolean(idx++, observation.isSignificant());
					}
					stmt.addBatch();
					hasBatch = true;
				}
				if (hasBatch)
					stmt.executeLargeBatch();
			}
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveAdditionalParameters() throws SQLException {
		boolean hasBatch = false;
		String sql = "INSERT INTO \"AdditionalParameterAposteriori\" ("
				+ "\"id\",\"value\",\"sigma\",\"confidence\",\"gross_error\",\"minimal_detectable_bias\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\""
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

			for ( Map.Entry<Integer, AdditionalUnknownParameter> parameterItem : this.additionalParametersToBeEstimated.entrySet() ) {
				int paramId = parameterItem.getKey();
				AdditionalUnknownParameter parameter = parameterItem.getValue();
				
				if (parameter.getColInJacobiMatrix() <= 0)
					continue;
				
				double value = parameter.getValue();
				switch(parameter.getParameterType()) {
				case ORIENTATION:
				case ROTATION_X:
				case ROTATION_Y:
				case ROTATION_Z:
					value = MathExtension.MOD(value, 2.0*Math.PI);
					break;
				default:
					break;
				}
				
				int idx = 1;

				stmt.setInt(idx++,    paramId);

				stmt.setDouble(idx++, value); // parameter.getValue()
				stmt.setDouble(idx++, parameter.getStd() > 0 ? parameter.getStd() : 0.0);
				stmt.setDouble(idx++, parameter.getConfidence());

				stmt.setDouble(idx++, parameter.getGrossError());
				stmt.setDouble(idx++, parameter.getMinimalDetectableBias());

				stmt.setDouble(idx++, parameter.getPprio());
				stmt.setDouble(idx++, parameter.getPpost());

				stmt.setDouble(idx++, parameter.getTprio());
				stmt.setDouble(idx++, parameter.getTpost());

				stmt.setBoolean(idx++, parameter.isSignificant());

				stmt.addBatch();
				hasBatch = true;
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveTestStatistic(TestStatisticParameters testStatisticParameters) throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"TestStatistic\" ("
				+ "\"d1\", \"d2\", \"probability_value\", \"power_of_test\", \"quantile\", \"non_centrality_parameter\", \"p_value\""
				+ ") VALUES (?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);

			TestStatisticParameterSet[] parameters = testStatisticParameters.getTestStatisticParameterSets();

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for ( int i=0; i<parameters.length; i++ ) {
				int idx = 1;
				TestStatisticParameterSet set = parameters[i];
				stmt.setDouble(idx++, set.getNumeratorDof());
				stmt.setDouble(idx++, set.getDenominatorDof());
				stmt.setDouble(idx++, set.getProbabilityValue());
				stmt.setDouble(idx++, set.getPowerOfTest());
				// Speichere fuer den kritischen Wert des Tests k = max(1.0, k), 
				// da nur damit die Forderung nach sigam2aprio == sigma2apost eingehalten werden kann
				stmt.setDouble(idx++, Math.max(1.0 + Constant.EPS, set.getQuantile()));
				stmt.setDouble(idx++, set.getNoncentralityParameter());
				stmt.setDouble(idx++, set.getLogarithmicProbabilityValue());

				stmt.addBatch();
				hasBatch = true;
			}
			if (hasBatch)
				stmt.executeLargeBatch();

		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveVarianceComponents(Map<VarianceComponentType, VarianceComponent> varianceComponents) throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"VarianceComponent\" ("
				+ "\"type\", \"redundancy\", \"omega\", \"sigma2apost\", \"number_of_observations\""
				+ ") VALUES (?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for ( VarianceComponent vc : varianceComponents.values() ) {
				int idx = 1;
				stmt.setInt(idx++,    vc.getVarianceComponentType().getId());
				stmt.setDouble(idx++, vc.getRedundancy());
				stmt.setDouble(idx++, vc.getOmega());
				stmt.setDouble(idx++, vc.getVarianceFactorAposteriori());
				stmt.setInt(idx++,    vc.getNumberOfObservations());

				stmt.addBatch();
				hasBatch = true;
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void savePrincipalComponentAnalysis(PrincipalComponent[] principalComponents) throws SQLException {
		if (principalComponents == null || principalComponents.length == 0)
			return;
		
		boolean hasBatch = false;

		String sql = "INSERT INTO \"PrincipalComponent\" ("
				+ "\"index\", \"value\", \"ratio\""
				+ ") VALUES (?,?,?)";

		try {
			double traceCxx = this.networkAdjustment.getTraceOfCovarianceMatrixOfPoints();
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for ( PrincipalComponent principalComponent : principalComponents ) {
				int idx = 1;
				
				int index = principalComponent.getIndex();
				double value = principalComponent.getValue();
				double ratio = traceCxx > 0 ? value / traceCxx : 0;
						
				stmt.setInt(idx++, index);
				stmt.setDouble(idx++, value);
				stmt.setDouble(idx++, ratio);
				
				stmt.addBatch();
				hasBatch = true;
			}

			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveRankDefect(RankDefect rankDefect) throws SQLException {
		String sql = "UPDATE \"RankDefect\" SET "
				+ "\"ty\" = ?,\"tx\" = ?,\"tz\" = ?,"
				+ "\"ry\" = ?,\"rx\" = ?,\"rz\" = ?,"
				+ "\"sy\" = ?,\"sx\" = ?,\"sz\" = ?,"
				+ "\"my\" = ?,\"mx\" = ?,\"mz\" = ?,"
				+ "\"mxy\" = ?,\"mxyz\" = ? WHERE \"id\" = 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		
		stmt.setBoolean(idx++, rankDefect.estimateTranslationY());
		stmt.setBoolean(idx++, rankDefect.estimateTranslationX());
		stmt.setBoolean(idx++, rankDefect.estimateTranslationZ());

		stmt.setBoolean(idx++, rankDefect.estimateRotationY());
		stmt.setBoolean(idx++, rankDefect.estimateRotationX());
		stmt.setBoolean(idx++, rankDefect.estimateRotationZ());

		stmt.setBoolean(idx++, rankDefect.estimateShearY());
		stmt.setBoolean(idx++, rankDefect.estimateShearX());
		stmt.setBoolean(idx++, rankDefect.estimateShearZ());

		stmt.setBoolean(idx++, rankDefect.estimateScaleY());
		stmt.setBoolean(idx++, rankDefect.estimateScaleX());
		stmt.setBoolean(idx++, rankDefect.estimateScaleZ());

		stmt.setBoolean(idx++, rankDefect.estimateScaleXY());
		stmt.setBoolean(idx++, rankDefect.estimateScaleXYZ());
		
		stmt.execute();
	}

	private void saveProjection() throws SQLException {
		String sql = "UPDATE \"ProjectionDefinition\" SET \"type\" = ? WHERE \"id\" = 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setInt(idx++, this.projection.getType().getId());
		stmt.execute();
	}
	
	private void saveCongruenceAnalysisPointPair() throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"CongruenceAnalysisPointPairAposteriori\" ("
				+ "\"id\",\"y\",\"x\",\"z\", "
				+ "\"sigma_y\",\"sigma_x\",\"sigma_z\", "
				+ "\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\", "
				+ "\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\", "
				+ "\"confidence_major_axis_2d\",\"confidence_minor_axis_2d\",\"confidence_alpha_2d\", "
				+ "\"gross_error_y\",\"gross_error_x\",\"gross_error_z\", "
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\", "
				+ "\"p_prio\",\"p_post\", "
				+ "\"t_prio\",\"t_post\",\"significant\""
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			boolean analysablePointPairFlag[] = new boolean[] {true, false};
			for (CongruenceAnalysisGroup congruenceAnalysisGroup : this.congruenceAnalysisGroups) {
				for (boolean analysablePointPair : analysablePointPairFlag) {
					int length = congruenceAnalysisGroup.size(analysablePointPair);
					for (int i=0; i<length; i++) {
						CongruenceAnalysisPointPair pointPair = congruenceAnalysisGroup.get(i, analysablePointPair);
						int dimension = pointPair.getDimension();
						int idx = 1;

						stmt.setInt(idx++, pointPair.getId());

						stmt.setDouble(idx++, dimension != 1 ? pointPair.getDeltaY() : 0.0);
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getDeltaX() : 0.0);
						stmt.setDouble(idx++, dimension != 2 ? pointPair.getDeltaZ() : 0.0);

						stmt.setDouble(idx++, dimension != 1 && pointPair.getStdY() > 0 ? pointPair.getStdY() : 0.0);
						stmt.setDouble(idx++, dimension != 1 && pointPair.getStdX() > 0 ? pointPair.getStdX() : 0.0);
						stmt.setDouble(idx++, dimension != 2 && pointPair.getStdZ() > 0 ? pointPair.getStdZ() : 0.0);

						stmt.setDouble(idx++, pointPair.getConfidenceAxis(0));
						stmt.setDouble(idx++, dimension == 3 ? pointPair.getConfidenceAxis(1) : 0.0);
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getConfidenceAxis(dimension-1) : 0.0);		

						stmt.setDouble(idx++, dimension > 2 ? pointPair.getConfidenceAngle(0) : 0.0);
						stmt.setDouble(idx++, dimension > 2 ? pointPair.getConfidenceAngle(1) : 0.0);
						stmt.setDouble(idx++, dimension > 1 ? pointPair.getConfidenceAngle(2) : 0.0);	

						stmt.setDouble(idx++, pointPair.getConfidenceAxis2D(0));
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getConfidenceAxis2D(1) : 0.0);
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getConfidenceAngle2D() : 0.0);

						stmt.setDouble(idx++, dimension != 1 ? pointPair.getGrossErrorY() : 0.0);
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getGrossErrorX() : 0.0);
						stmt.setDouble(idx++, dimension != 2 ? pointPair.getGrossErrorZ() : 0.0);

						stmt.setDouble(idx++, dimension != 1 ? pointPair.getMinimalDetectableBiasY() : 0.0);
						stmt.setDouble(idx++, dimension != 1 ? pointPair.getMinimalDetectableBiasX() : 0.0);
						stmt.setDouble(idx++, dimension != 2 ? pointPair.getMinimalDetectableBiasZ() : 0.0);

						stmt.setDouble(idx++, pointPair.getPprio());
						stmt.setDouble(idx++, pointPair.getPpost());

						stmt.setDouble(idx++, pointPair.getTprio());
						stmt.setDouble(idx++, pointPair.getTpost());

						stmt.setBoolean(idx++, pointPair.isSignificant());

						stmt.addBatch();
						hasBatch = true;
					}
				}
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}

	private void saveStrainParameters() throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"CongruenceAnalysisStrainParameterAposteriori\" ("
				+ "\"group_id\",\"type\","
				+ "\"value\",\"sigma\",\"confidence\","
				+ "\"gross_error\",\"minimal_detectable_bias\","
				+ "\"p_prio\",\"p_post\","
				+ "\"t_prio\",\"t_post\",\"significant\""
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

			for (CongruenceAnalysisGroup congruenceAnalysisGroup : this.congruenceAnalysisGroups ) {
				int dimension = congruenceAnalysisGroup.getDimension();
				StrainAnalysisEquations strainAnalysisEquations = congruenceAnalysisGroup.getStrainAnalysisEquations();

				int nou = strainAnalysisEquations.numberOfParameters() - strainAnalysisEquations.numberOfExpandedParameters();
				int nor = strainAnalysisEquations.numberOfRestrictions();
				int not = congruenceAnalysisGroup.size(true);

				if (this.freeNetwork && this.congruenceAnalysis && strainAnalysisEquations.hasUnconstraintParameters() && not * dimension + nor >= nou) {
					Map<ParameterType, RestrictionType> parameterTyps = new HashMap<ParameterType, RestrictionType>(12);
					if (dimension != 1) {
						parameterTyps.put(ParameterType.STRAIN_TRANSLATION_X, RestrictionType.FIXED_TRANSLATION_X);
						parameterTyps.put(ParameterType.STRAIN_TRANSLATION_Y, RestrictionType.FIXED_TRANSLATION_Y);
						parameterTyps.put(ParameterType.STRAIN_ROTATION_Z, RestrictionType.FIXED_ROTATION_Z);
						parameterTyps.put(ParameterType.STRAIN_SCALE_X, RestrictionType.FIXED_SCALE_X);
						parameterTyps.put(ParameterType.STRAIN_SCALE_Y, RestrictionType.FIXED_SCALE_Y);
						parameterTyps.put(ParameterType.STRAIN_SHEAR_Z, RestrictionType.FIXED_SHEAR_Z);
					}
					if (dimension != 2) {
						parameterTyps.put(ParameterType.STRAIN_TRANSLATION_Z, RestrictionType.FIXED_TRANSLATION_Z);
						parameterTyps.put(ParameterType.STRAIN_SCALE_Z, RestrictionType.FIXED_SCALE_Z);
					}
					if (dimension == 3) {
						parameterTyps.put(ParameterType.STRAIN_ROTATION_X, RestrictionType.FIXED_ROTATION_X);
						parameterTyps.put(ParameterType.STRAIN_ROTATION_Y, RestrictionType.FIXED_ROTATION_Y);
						parameterTyps.put(ParameterType.STRAIN_SHEAR_X, RestrictionType.FIXED_SHEAR_X);
						parameterTyps.put(ParameterType.STRAIN_SHEAR_Y, RestrictionType.FIXED_SHEAR_Y);
					}

					for (int i=0; i<strainAnalysisEquations.numberOfParameters(); i++) {
						StrainParameter strainParameter = strainAnalysisEquations.get(i);
						ParameterType type = strainParameter.getParameterType();
						if (parameterTyps.containsKey(type) && !strainAnalysisEquations.isRestricted(parameterTyps.get(type)) && strainParameter.getStd() >= 0) {
							double value = strainParameter.getValue();
							switch(strainParameter.getParameterType()) {
							case STRAIN_ROTATION_X:
							case STRAIN_ROTATION_Y:
							case STRAIN_ROTATION_Z:
							case STRAIN_SHEAR_X:
							case STRAIN_SHEAR_Y:
							case STRAIN_SHEAR_Z:
								value = MathExtension.MOD(value, 2.0 * Math.PI);
								break;
							default:
								break;
							}
							
							int idx = 1;
							stmt.setInt(idx++, congruenceAnalysisGroup.getId());
							stmt.setInt(idx++, type.getId());
							
							stmt.setDouble(idx++, value); // strainParameter.getValue()
							stmt.setDouble(idx++, strainParameter.getStd() > 0 ? strainParameter.getStd() : 0.0);
							stmt.setDouble(idx++, strainParameter.getConfidence());
							
							stmt.setDouble(idx++, strainParameter.getGrossError());
							stmt.setDouble(idx++, strainParameter.getMinimalDetectableBias());
							
							stmt.setDouble(idx++, strainParameter.getPprio());
							stmt.setDouble(idx++, strainParameter.getPpost());
							
							stmt.setDouble(idx++, strainParameter.getTprio());
							stmt.setDouble(idx++, strainParameter.getTpost());
							
							stmt.setBoolean(idx++, strainParameter.isSignificant());
							
							stmt.addBatch();
							hasBatch = true;
						}
					}
				}
			}
			if (hasBatch)
				stmt.executeLargeBatch();
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}
	
	/**
	 * Average observations per group
	 * @param saveAvarageValues
	 * @return observations
	 * @throws SQLException
	 * @throws IllegalProjectionPropertyException
	 * @throws DatabaseVersionMismatchException 
	 */
	public List<Observation> averageDetermination(boolean saveAvarageValues) throws SQLException, IllegalProjectionPropertyException, DatabaseVersionMismatchException {
		// erzeuge ein Objekt zur Netzausgleichung
		// Hierdurch werden alle Punkte, Beobachtungen und Zusatzparameter 
		// geladen und inizialisiert.
		List<Observation> observations = new ArrayList<Observation>();
		NetworkAdjustment networkAdjustment = this.getNetworkAdjustment();
		if (networkAdjustment == null)
			return observations;
		
		// Mittelwertbildung (gruppenweise)
		for (ObservationGroup observationGroup : this.completeObservationGroups) {
			double threshold = 1.0;
		
			if (observationGroup instanceof GNSSBaseline1DGroup)
				threshold = this.getAverageThreshold(ObservationType.GNSS1D);
			else if (observationGroup instanceof GNSSBaseline2DGroup)
				threshold = this.getAverageThreshold(ObservationType.GNSS2D);
			else if (observationGroup instanceof GNSSBaseline3DGroup)
				threshold = this.getAverageThreshold(ObservationType.GNSS3D);
			else if (observationGroup instanceof DirectionGroup)
				threshold = this.getAverageThreshold(ObservationType.DIRECTION);
			else if (observationGroup instanceof DeltaZGroup)
				threshold = this.getAverageThreshold(ObservationType.LEVELING);
			else if (observationGroup instanceof HorizontalDistanceGroup)
				threshold = this.getAverageThreshold(ObservationType.HORIZONTAL_DISTANCE);
			else if (observationGroup instanceof SlopeDistanceGroup)
				threshold = this.getAverageThreshold(ObservationType.SLOPE_DISTANCE);
			else if (observationGroup instanceof ZenithAngleGroup)
				threshold = this.getAverageThreshold(ObservationType.ZENITH_ANGLE);
			else {
				System.err.println(this.getClass().getSimpleName() + " Fehler, unbekannte Beobachtungsgruppe! " + observationGroup);
				continue;
			}
			observationGroup.averageDetermination(threshold);
			Set<Observation> excludedObservations = observationGroup.getExcludedObservationsDuringAvaraging();
			if (excludedObservations != null && !excludedObservations.isEmpty()) 
				observations.addAll(excludedObservations);
		}
		
		// Speichere nur, wenn der Nutzer dies explizit wollte oder es keinen Konflikt gab mit den Grenzwerten
		if (saveAvarageValues || observations.isEmpty()) {
			for (Observation observation : observations)
				this.deleteObservation(observation);

			// Speichere GNSS-IDs
			Set<Integer> gnssIDs = new HashSet<Integer>();

			// Speichere Ergebnis
			PreparedStatement statement = null;

			// SQL-Statements
			String sqlFormatDelObs = "DELETE FROM \"ObservationApriori\" " +
					"WHERE \"group_id\" = ? AND \"enable\" = TRUE " +
					"AND (SELECT \"enable\" FROM \"ObservationGroup\" WHERE \"id\" = \"group_id\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointApriori\" WHERE \"name\" = \"start_point_name\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointApriori\" WHERE \"name\" = \"end_point_name\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointGroup\" WHERE \"id\" = (SELECT \"group_id\" FROM \"PointApriori\" WHERE \"name\" = \"start_point_name\")) = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointGroup\" WHERE \"id\" = (SELECT \"group_id\" FROM \"PointApriori\" WHERE \"name\" = \"end_point_name\")) = TRUE";

			String sqlFormatDelGNSSObs = "DELETE FROM \"GNSSObservationApriori\" " +
					"WHERE \"group_id\" = ? AND \"enable\" = TRUE " +
					"AND (SELECT \"enable\" FROM \"ObservationGroup\" WHERE \"id\" = \"group_id\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointApriori\" WHERE \"name\" = \"start_point_name\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointApriori\" WHERE \"name\" = \"end_point_name\") = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointGroup\" WHERE \"id\" = (SELECT \"group_id\" FROM \"PointApriori\" WHERE \"name\" = \"start_point_name\")) = TRUE " +
					"AND (SELECT \"enable\" FROM \"PointGroup\" WHERE \"id\" = (SELECT \"group_id\" FROM \"PointApriori\" WHERE \"name\" = \"end_point_name\")) = TRUE";

			String sqlFormatInsObs     = "INSERT INTO \"ObservationApriori\"     (\"group_id\", \"start_point_name\", \"end_point_name\", \"instrument_height\", \"reflector_height\", \"value_0\", \"sigma_0\", \"distance_0\", \"enable\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String sqlFormatInsGNSSObs = "INSERT INTO \"GNSSObservationApriori\" (\"group_id\", \"start_point_name\", \"end_point_name\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\", \"enable\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			for (ObservationGroup observationGroup : this.completeObservationGroups) {
				// Loesche alle Beobachtungen der Gruppe, die aktiv waren/sind
				int groupId = observationGroup.getId();
				boolean isGNSS = observationGroup instanceof GNSSBaseline1DGroup ||observationGroup instanceof GNSSBaseline2DGroup || observationGroup instanceof GNSSBaseline3DGroup;
				if (!isGNSS)
					statement = this.dataBase.getPreparedStatement(sqlFormatDelObs);
				else
					statement = this.dataBase.getPreparedStatement(sqlFormatDelGNSSObs);

				statement.setInt(1, groupId);
				statement.execute();

				// Speichere die gemittelten Daten
				for (int i=0; i<observationGroup.size(); i++) {
					int idx = 1;
					Observation observation = observationGroup.get(i);
					if (!isGNSS) {
						statement = this.dataBase.getPreparedStatement(sqlFormatInsObs);

						statement.setInt(idx++, groupId);
						statement.setString(idx++, observation.getStartPoint().getName());
						statement.setString(idx++, observation.getEndPoint().getName());

						statement.setDouble(idx++, observation.getStartPointHeight());
						statement.setDouble(idx++, observation.getEndPointHeight());

						statement.setDouble(idx++, observation.getValueApriori());
						statement.setDouble(idx++, 0.0);
						statement.setDouble(idx++, observation.getDistanceForUncertaintyModel());

						statement.setBoolean(idx++, true);

						statement.execute();
					}
					else {
						GNSSBaseline gnssBaseline = (GNSSBaseline)observation;
						if (gnssIDs.contains(gnssBaseline.getId()))
							continue;

						gnssIDs.add(gnssBaseline.getId());

						GNSSBaseline gnssY = gnssBaseline.getBaselineComponent(ComponentType.Y);
						GNSSBaseline gnssX = gnssBaseline.getBaselineComponent(ComponentType.X);
						GNSSBaseline gnssZ = gnssBaseline.getBaselineComponent(ComponentType.Z);

						statement = this.dataBase.getPreparedStatement(sqlFormatInsGNSSObs);

						statement.setInt(idx++, groupId);
						statement.setString(idx++, gnssBaseline.getStartPoint().getName());
						statement.setString(idx++, gnssBaseline.getEndPoint().getName());

						statement.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getValueApriori());
						statement.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getValueApriori());
						statement.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getValueApriori());

						statement.setDouble(idx++, 0.0);
						statement.setDouble(idx++, 0.0);
						statement.setDouble(idx++, 0.0);

						statement.setBoolean(idx++, true);

						statement.execute();
					}
				}
			}
		}
		networkAdjustment.clearMatrices();
		networkAdjustment = null;
		return observations;
	}
	
	private void deleteObservation(Observation observation) throws SQLException {
		boolean isGNSS = observation.getObservationType() == ObservationType.GNSS1D || observation.getObservationType() == ObservationType.GNSS2D || observation.getObservationType() == ObservationType.GNSS3D;
		String sql = "DELETE FROM " + (isGNSS ? "\"GNSSObservationApriori\"" : "\"ObservationApriori\"") + " WHERE \"id\" = ? LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setInt(idx++, observation.getId());
		stmt.execute();
	}
	
	private double getAverageThreshold(ObservationType type) throws SQLException {
		String sql = "SELECT \"value\" "
				+ "FROM \"AverageThreshold\" "
				+ "WHERE \"type\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, type.getId());
		ResultSet rs = stmt.executeQuery();
		
		double value = 0;
		if (rs.next()) 
			value = rs.getDouble("value");
		
		return value > 0 ? value : DefaultAverageThreshold.getThreshold(type); 
	}
}
