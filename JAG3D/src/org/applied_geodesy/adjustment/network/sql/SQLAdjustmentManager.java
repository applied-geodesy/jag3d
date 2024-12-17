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
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.DefaultAverageThreshold;
import org.applied_geodesy.adjustment.network.DefaultUncertainty;
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
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
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
import org.applied_geodesy.adjustment.network.observation.reduction.ProjectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.Reduction;
import org.applied_geodesy.adjustment.network.observation.reduction.ReductionTaskType;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.Orientation;
import org.applied_geodesy.adjustment.network.point.Point;
import org.applied_geodesy.adjustment.network.point.Point1D;
import org.applied_geodesy.adjustment.network.point.Point2D;
import org.applied_geodesy.adjustment.network.point.Point3D;
import org.applied_geodesy.adjustment.statistic.BinomialTestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.BinomialTestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.jag3d.ui.io.writer.AdjustmentResultWritable;
import org.applied_geodesy.jag3d.ui.io.writer.DefaultNetworkAdjustmentResultWriter;
import org.applied_geodesy.jag3d.ui.io.writer.MatlabNetworkAdjustmentResultWriter;
import org.applied_geodesy.jag3d.ui.io.writer.ExportOption.ExportResultType;
import org.applied_geodesy.transformation.datum.Ellipsoid;
import org.applied_geodesy.transformation.datum.SphericalDeflectionModel;
import org.applied_geodesy.util.sql.DataBase;
import org.applied_geodesy.util.sql.HSQLDB;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;
import org.applied_geodesy.version.jag3d.Version;
import org.applied_geodesy.version.VersionType;

public class SQLAdjustmentManager {
	private final DataBase dataBase;

	private Map<String, Point> completePoints    = new LinkedHashMap<String, Point>();
	private Map<String, Point> completeNewPoints = new LinkedHashMap<String, Point>();
	
	private Map<String, Point> completePointsWithReferenceDeflections  = new LinkedHashMap<String, Point>();
	private Map<String, Point> completePointsWithStochasticDeflections = new LinkedHashMap<String, Point>();
	private Map<String, Point> completePointsWithUnknownDeflections    = new LinkedHashMap<String, Point>();

	private Map<Integer, AdditionalUnknownParameter> additionalParametersToBeEstimated = new LinkedHashMap<Integer, AdditionalUnknownParameter>();

	private List<CongruenceAnalysisGroup> congruenceAnalysisGroups = new ArrayList<CongruenceAnalysisGroup>();
	private List<ObservationGroup> completeObservationGroups = new ArrayList<ObservationGroup>();

	private Reduction reductions = new Reduction();

	private NetworkAdjustment networkAdjustment = null;

	private EstimationType estimationType = null;
	
	private boolean freeNetwork = false,
			congruenceAnalysis = false,
			pure1DNetwork = true,
			containsSpatialObservations = false,
			estimateOrientationApproximation = true,
			applicableHorizontalProjection = true;

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
			throw new DatabaseVersionMismatchException("Error, database version of the stored project is unequal to the accepted database version of the application: " + databaseVersion + " != " +  Version.get(VersionType.DATABASE));
	}

	public NetworkAdjustment getNetworkAdjustment() throws SQLException, DatabaseVersionMismatchException, IllegalProjectionPropertyException {
		this.clear();
		
		this.setDataBaseSchema();
		this.checkDatabaseVersion();
		this.setReductionDefinition();

		this.networkAdjustment = new NetworkAdjustment();
		this.addAdjustmentDefinition(this.networkAdjustment);
		this.addExportOptions(this.networkAdjustment);

		// Definition of test statistic
		TestStatisticDefinition testStatisticDefinition = this.getTestStatisticDefinition();
		if (testStatisticDefinition != null)
			this.networkAdjustment.setTestStatisticDefinition(testStatisticDefinition);

		Map<String,Point> newPoints   = this.getPointsByType( PointType.NEW_POINT );
		Map<String,Point> datumPoints = this.getPointsByType( PointType.DATUM_POINT );

		Map<String,Point> stochasticPoints = new LinkedHashMap<String,Point>(0);
		Map<String,Point> referencePoints  = new LinkedHashMap<String,Point>(0);
		this.completeNewPoints.putAll(newPoints);

		this.freeNetwork = datumPoints != null && !datumPoints.isEmpty();
		if (!this.freeNetwork) {
			stochasticPoints = this.getPointsByType( PointType.STOCHASTIC_POINT );
			referencePoints  = this.getPointsByType( PointType.REFERENCE_POINT );
		}
		
		// add vertical deflection to points (if any)
		this.addVerticalDeflections();

		if (this.pure1DNetwork)
			this.applicableHorizontalProjection = false;
		
		// Abbildungsreduktionen sind bei einer Diagnoseauswertung unzulaessig, da es keine realen Beobachtungen gibt
		if (this.estimationType == EstimationType.SIMULATION && this.reductions.size() > 0) 
			throw new IllegalProjectionPropertyException("Projection cannot be applied to pseudo-observations in diagnosis adjustment (simulation)!");

		// wenn 2D Projektionen nicht moeglich sind, werden keine Reduktionen durchgefuehrt
		if (!this.applicableHorizontalProjection && (this.reductions.getProjectionType() == ProjectionType.GAUSS_KRUEGER || this.reductions.getProjectionType() == ProjectionType.UTM) && (this.reductions.applyReductionTask(ReductionTaskType.DIRECTION) || this.reductions.applyReductionTask(ReductionTaskType.DISTANCE))) {
			if (this.pure1DNetwork)
				throw new IllegalProjectionPropertyException("Projection cannot be applied to observations of leveling network! " + this.reductions.getProjectionType());
			else
				throw new IllegalProjectionPropertyException("Projection cannot be applied to observations because the coordinates are invalid, e.g. missing zone number! " + this.reductions.getProjectionType());
		}
		
		if (this.reductions.getProjectionType() == ProjectionType.LOCAL_ELLIPSOIDAL) {
			SphericalDeflectionModel sphericalDeflectionModel = new SphericalDeflectionModel(this.reductions);
			for (Point point : this.completePoints.values())
				sphericalDeflectionModel.setSphericalDeflections(point);
			this.networkAdjustment.setSphericalDeflectionModel(sphericalDeflectionModel);
		}

		// Fuege Beobachtungen zu den Punkten hinzu
		this.completeObservationGroups.addAll(this.getObservationGroups());
		// wenn 2D Projektionen nicht moeglich sind, werden keine Reduktionen durchgefuehrt
		if (this.containsSpatialObservations && (this.reductions.getProjectionType() == ProjectionType.GAUSS_KRUEGER || this.reductions.getProjectionType() == ProjectionType.UTM) && (this.reductions.applyReductionTask(ReductionTaskType.DIRECTION) || this.reductions.applyReductionTask(ReductionTaskType.DISTANCE) || this.reductions.applyReductionTask(ReductionTaskType.HEIGHT) || this.reductions.applyReductionTask(ReductionTaskType.EARTH_CURVATURE)))
			throw new IllegalProjectionPropertyException("Projection defined for horizontal networks cannot be applied to spatial observations such as slope distances or zenith angles! " + this.reductions.getProjectionType());

		// Fuege Punkt der Netzausgleichung zu
		for ( Point point : newPoints.values() ) {
			String name = point.getName();
			int dimension = point.getDimension();
			VerticalDeflectionType type = null;
			if (dimension != 2) { // dimension == 3
				if (this.completePointsWithReferenceDeflections.containsKey(name))
					type = VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION;
				else if (this.completePointsWithStochasticDeflections.containsKey(name))
					type = VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION;
				else if (this.completePointsWithUnknownDeflections.containsKey(name))
					type = VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION;
			}
			this.networkAdjustment.addNewPoint( point, type );
		}

		// Nutze Deformationsvektoren zur Bildung von relativen Konfidenzbereichen (nicht nur bei freier AGL/Defo.-Analyse)
		this.congruenceAnalysisGroups.addAll(this.getCongruenceAnalysisGroups());

		for ( CongruenceAnalysisGroup congruenceAnalysisGroup : this.congruenceAnalysisGroups ) 
			this.networkAdjustment.addCongruenceAnalysisGroup(congruenceAnalysisGroup);

		if (this.freeNetwork) {
			this.addUserDefinedRankDefect(this.networkAdjustment.getRankDefect());
			
			for ( Point point : datumPoints.values() ) {
				String name = point.getName();
				int dimension = point.getDimension();
				VerticalDeflectionType type = null;
				if (dimension != 2) { // dimension == 3
					if (this.completePointsWithReferenceDeflections.containsKey(name))
						type = VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION;
					else if (this.completePointsWithStochasticDeflections.containsKey(name))
						type = VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION;
					else if (this.completePointsWithUnknownDeflections.containsKey(name))
						type = VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION;
				}
				this.networkAdjustment.addDatumPoint( point, type );
			}
			
		}
		else {
			for ( Point point : referencePoints.values() ) {
				String name = point.getName();
				int dimension = point.getDimension();
				VerticalDeflectionType type = null;
				if (dimension != 2) { // dimension == 3
					if (this.completePointsWithReferenceDeflections.containsKey(name))
						type = VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION;
					else if (this.completePointsWithStochasticDeflections.containsKey(name))
						type = VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION;
					else if (this.completePointsWithUnknownDeflections.containsKey(name))
						type = VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION;
				}
				this.networkAdjustment.addReferencePoint( point, type );
			}
			
			for ( Point point : stochasticPoints.values() ) {
				String name = point.getName();
				int dimension = point.getDimension();
				VerticalDeflectionType type = null;
				if (dimension != 2) { // dimension == 3
					if (this.completePointsWithReferenceDeflections.containsKey(name))
						type = VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION;
					else if (this.completePointsWithStochasticDeflections.containsKey(name))
						type = VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION;
					else if (this.completePointsWithUnknownDeflections.containsKey(name))
						type = VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION;
				}
				this.networkAdjustment.addStochasticPoint( point, type );
			}
		}
		// Auszugleichende Zusatzparameter
		for ( AdditionalUnknownParameter parameter : this.additionalParametersToBeEstimated.values() ) 
			this.networkAdjustment.addAdditionalUnknownParameter( parameter );

		return networkAdjustment;
	}
	
	public void clear() {
		this.completePoints.clear();
		this.completeNewPoints.clear();
		this.completePointsWithReferenceDeflections.clear();
		this.completePointsWithStochasticDeflections.clear();
		this.completePointsWithUnknownDeflections.clear();
		
		this.additionalParametersToBeEstimated.clear();
		
		this.congruenceAnalysisGroups.clear();
		this.completeObservationGroups.clear();
		
		this.reductions.setProjectionType(ProjectionType.LOCAL_CARTESIAN);
		this.reductions.getPrincipalPoint().setCoordinates(0, 0, 0,  0, 0, 0);
		this.reductions.clear();

		if (this.networkAdjustment != null) {
			this.networkAdjustment.clearMatrices();
			this.networkAdjustment = null;
		}
	}
	
	private void setReductionDefinition() throws SQLException {
		String sql = "SELECT "
				+ "\"projection_type\", "
				+ "\"reference_latitude\", \"reference_longitude\", \"reference_height\", "
				+ "\"major_axis\", \"minor_axis\", "
				+ "\"x0\", \"y0\", \"z0\", \"type\" AS \"task_type\" "
				+ "FROM \"ReductionTask\" "
				+ "RIGHT JOIN \"ReductionDefinition\" "
				+ "ON \"ReductionTask\".\"reduction_id\" = \"ReductionDefinition\".\"id\" "
				+ "WHERE \"ReductionDefinition\".\"id\" = 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			int taskTypeId = rs.getInt("task_type");
			boolean hasTaskType = !rs.wasNull();
			
			ProjectionType projectionType = ProjectionType.getEnumByValue(rs.getInt("projection_type"));
			double referenceLatitude      = rs.getDouble("reference_latitude");
			double referenceLongitude     = rs.getDouble("reference_longitude");
			double referenceHeight        = rs.getDouble("reference_height");
			double majorAxis              = rs.getDouble("major_axis");
			double minorAxis              = rs.getDouble("minor_axis");
			double x0                     = rs.getDouble("x0");
			double y0                     = rs.getDouble("y0");
			double z0                     = rs.getDouble("z0");

			this.reductions.setProjectionType(projectionType);
			this.reductions.getPrincipalPoint().setCoordinates(x0, y0, z0, referenceLatitude, referenceLongitude, referenceHeight);
			this.reductions.setEllipsoid(Ellipsoid.createEllipsoidFromMinorAxis(Math.max(majorAxis, minorAxis), Math.min(majorAxis, minorAxis)));

			if (hasTaskType) {
				ReductionTaskType taskType = ReductionTaskType.getEnumByValue(taskTypeId);
				
				switch (taskType) {
				case DIRECTION:
				case DISTANCE:
					if (projectionType != ProjectionType.LOCAL_ELLIPSOIDAL && projectionType != ProjectionType.LOCAL_CARTESIAN)
						this.reductions.addReductionTaskType(taskType);	
					break;
				case EARTH_CURVATURE:
				case HEIGHT:
					if (projectionType != ProjectionType.LOCAL_ELLIPSOIDAL)
						this.reductions.addReductionTaskType(taskType);	
					break;			
				}
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
	
	private void addExportOptions(NetworkAdjustment adjustment) throws SQLException {
		if (!(this.dataBase instanceof HSQLDB))
			return;
		
		// export path of covariance matrix
		String dataBaseFilePath = ((HSQLDB)this.dataBase).getDataBaseFileName();
		
		String sql = "SELECT "
				+ "\"type\" "
				+ "FROM \"ExportResult\" "
				+ "WHERE \"id\" = 1 LIMIT 1";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			ExportResultType type = ExportResultType.getEnumByValue(rs.getInt("type"));
			
			AdjustmentResultWritable adjustmentResultWritable = null;
			switch (type) {
			case ASCII:
				adjustmentResultWritable = new DefaultNetworkAdjustmentResultWriter(dataBaseFilePath);
				break;
			case MATLAB:
				adjustmentResultWritable = new MatlabNetworkAdjustmentResultWriter(dataBaseFilePath);
				break;
			case NONE:
				break;			
			}
			adjustment.setAdjustmentResultWritable(adjustmentResultWritable);
		}
	}

	private void addAdjustmentDefinition(NetworkAdjustment adjustment) throws SQLException {
		String sql = "SELECT "
				+ "\"type\", \"number_of_iterations\", \"robust_estimation_limit\", "
				+ "\"number_of_principal_components\", \"apply_variance_of_unit_weight\", "
				+ "\"estimate_direction_set_orientation_approximation\", "
				+ "\"congruence_analysis\", "
				+ "\"scaling\", \"damping\", \"weight_zero\" "
				+ "FROM \"AdjustmentDefinition\" "
				+ "JOIN \"UnscentedTransformation\" "
				+ "ON \"AdjustmentDefinition\".\"id\" = \"UnscentedTransformation\".\"id\" "
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
			boolean applyVarianceOfUnitWeight     = rs.getBoolean("apply_variance_of_unit_weight");
			
			double scalingUT = rs.getDouble("scaling");
			double dampingUT = rs.getDouble("damping");
			double weight0UT = rs.getDouble("weight_zero");

			this.estimationType = type == null ? EstimationType.L2NORM : type;
			// Keine Schaetzung moeglich bei Simulationen 
			//applyVarianceOfUnitWeight = !(this.estimationType == EstimationType.L1NORM || this.estimationType == EstimationType.L2NORM) ? false : applyVarianceOfUnitWeight;
			applyVarianceOfUnitWeight = this.estimationType == EstimationType.SIMULATION ? false : applyVarianceOfUnitWeight;
			
			adjustment.setMaximalNumberOfIterations(maximalNumberOfIterations);
			adjustment.setRobustEstimationLimit(robustEstimationLimit);
			adjustment.setNumberOfPrincipalComponents(numberOfPrincipalComponents);
			adjustment.setEstimationType(this.estimationType);
			adjustment.setCongruenceAnalysis(this.congruenceAnalysis);
			adjustment.setApplyAposterioriVarianceOfUnitWeight(applyVarianceOfUnitWeight);
			
			adjustment.setUnscentedTransformationScaling(scalingUT);
			adjustment.setUnscentedTransformationDamping(dampingUT);
			adjustment.setUnscentedTransformationWeightZero(weight0UT);
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

	private Map<String, Point> getPointsByType(PointType type) throws SQLException {

		String sql = "SELECT \"name\", \"y0\", \"x0\", \"z0\", \"dimension\", "
				+ "IFNULL(CASEWHEN( \"sigma_y0\" > 0, \"sigma_y0\", (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_y0\", "
				+ "IFNULL(CASEWHEN( \"sigma_x0\" > 0, \"sigma_x0\", (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_x0\", "
				+ "IFNULL(CASEWHEN( \"sigma_z0\" > 0, \"sigma_z0\", (SELECT \"value\" FROM \"PointGroupUncertainty\" WHERE \"group_id\" = \"PointApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_z0\"  "
				+ "FROM \"PointApriori\" "
				+ "JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" "
				+ "WHERE \"type\" = ? AND \"PointGroup\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE "
				+ "ORDER BY \"dimension\" ASC, \"PointGroup\".\"id\" ASC, \"PointApriori\".\"id\" ASC";

		Map<String, Point> points = new LinkedHashMap<String,Point>();

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(idx++, PointGroupUncertaintyType.COMPONENT_Y.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyY());

		stmt.setInt(idx++, PointGroupUncertaintyType.COMPONENT_X.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyX());

		stmt.setInt(idx++, PointGroupUncertaintyType.COMPONENT_Z.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyZ());

		stmt.setInt(idx++, type.getId());

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

			Point point = null;

			switch(dimension) {
			case 1:
				point = new Point1D(name, x0, y0, z0, sigmaZ0);
				break;
			case 2:
				point = new Point2D(name, x0, y0, z0, sigmaX0, sigmaY0);
				
				
				this.pure1DNetwork = false;
				if (y0 < 1100000 || y0 > 59800000 )
					this.applicableHorizontalProjection = false;
				break;
			case 3:
				point = new Point3D(name, x0, y0, z0, sigmaX0, sigmaY0, sigmaZ0);

				this.pure1DNetwork = false;
				if (y0 < 1100000 || y0 > 59800000 )
					this.applicableHorizontalProjection = false;
				break;
			}
			if (point != null && !this.completePoints.containsKey(point.getName())) {
				if (this.estimationType == EstimationType.SIMULATION) {
					point.setX(point.getX0());
					point.setY(point.getY0());
					point.setZ(point.getZ0());
				}
				points.put(point.getName(), point);
				this.completePoints.put(point.getName(), point);
			}
		}
		return points;
	}
	
	private void addVerticalDeflections() throws SQLException {
		String sql = "SELECT \"name\", \"y0\", \"x0\", \"VerticalDeflectionGroup\".\"type\" AS \"vertical_deflection_type\", "
				+ "IFNULL(CASEWHEN( \"sigma_y0\" > 0, \"sigma_y0\", (SELECT \"value\" FROM \"VerticalDeflectionGroupUncertainty\" WHERE \"group_id\" = \"VerticalDeflectionApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_y0\", "
				+ "IFNULL(CASEWHEN( \"sigma_x0\" > 0, \"sigma_x0\", (SELECT \"value\" FROM \"VerticalDeflectionGroupUncertainty\" WHERE \"group_id\" = \"VerticalDeflectionApriori\".\"group_id\" AND \"type\" = ?)), ?) AS \"sigma_x0\"  "
				+ "FROM \"VerticalDeflectionApriori\" "
				+ "JOIN \"VerticalDeflectionGroup\" ON \"VerticalDeflectionApriori\".\"group_id\" = \"VerticalDeflectionGroup\".\"id\" "
				+ "JOIN \"PointApriori\" ON \"VerticalDeflectionApriori\".\"name\" = \"PointApriori\".\"name\" "
				+ "JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" "
				+ "WHERE \"VerticalDeflectionGroup\".\"enable\" = TRUE AND \"VerticalDeflectionApriori\".\"enable\" = TRUE "
				+ "AND \"PointGroup\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE AND \"PointGroup\".\"dimension\" IN (1, 3) " // \"PointGroup\".\"dimension\" = 3 
				+ "ORDER BY \"VerticalDeflectionGroup\".\"id\" ASC, \"VerticalDeflectionApriori\".\"id\" ASC";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(idx++, VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyDeflectionY());

		stmt.setInt(idx++, VerticalDeflectionGroupUncertaintyType.DEFLECTION_X.getId());
		stmt.setDouble(idx++, DefaultUncertainty.getUncertaintyDeflectionX());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			VerticalDeflectionType type = VerticalDeflectionType.getEnumByValue(rs.getInt("vertical_deflection_type"));
			if (type == null)
				continue;
			
			String name = rs.getString("name");

			Point point = this.completePoints.get(name);
			if (point == null || point.getDimension() == 2) // point.getDimension() != 3
				continue;

			double y0 = rs.getDouble("y0");
			double x0 = rs.getDouble("x0");

			double sigmaY0 = rs.getDouble("sigma_y0");	
			double sigmaX0 = rs.getDouble("sigma_x0");

			point.getVerticalDeflectionY().setValue0(y0);
			point.getVerticalDeflectionX().setValue0(x0);
			
			point.getVerticalDeflectionY().setStdApriori(sigmaY0);
			point.getVerticalDeflectionX().setStdApriori(sigmaX0);
			
			switch (type) {
			case UNKNOWN_VERTICAL_DEFLECTION:
				this.completePointsWithUnknownDeflections.put(name, point);
				break;
			case REFERENCE_VERTICAL_DEFLECTION:
				this.completePointsWithReferenceDeflections.put(name, point);
				break;
			case STOCHASTIC_VERTICAL_DEFLECTION:
				this.completePointsWithStochasticDeflections.put(name, point);
				break;
			}
		}
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

			boolean containsSpatialObservations = false;
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
				else {
					group = new ZenithAngleGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);
					containsSpatialObservations = true;
				}

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
				else {
					group = new SlopeDistanceGroup(groupId, sigmaZeroPointOffset, sigmaSquareRootDistance, sigmaDistance, epoch);
					containsSpatialObservations = true;
				}

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

			if (group != null && !group.isEmpty()) {
				observationGroups.add(group);
				if (containsSpatialObservations)
					this.containsSpatialObservations = true;
			}
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

			if (this.estimationType == EstimationType.SIMULATION)
				observation.setValueApriori(observation.getValueAposteriori());
			
			if (observation != null) {
				observation.setReduction(this.reductions);
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

				gnssZ.setReduction(this.reductions);
				if (this.estimationType == EstimationType.SIMULATION)
					gnssZ.setValueApriori(gnssZ.getValueAposteriori());

				((GNSSBaseline1DGroup)observationGroup).add(gnssZ);
			}
			else if (observationGroup instanceof GNSSBaseline2DGroup && startPoint.getDimension() != 1 && endPoint.getDimension() != 1) {
				GNSSBaselineDeltaY2D gnssY = new GNSSBaselineDeltaY2D(id, startPoint, endPoint, y0, sigmaY0);
				GNSSBaselineDeltaX2D gnssX = new GNSSBaselineDeltaX2D(id, startPoint, endPoint, x0, sigmaX0);

				gnssX.setReduction(this.reductions);
				gnssY.setReduction(this.reductions);
				if (this.estimationType == EstimationType.SIMULATION) {
					gnssX.setValueApriori(gnssX.getValueAposteriori());
					gnssY.setValueApriori(gnssY.getValueAposteriori());
				}

				((GNSSBaseline2DGroup)observationGroup).add(gnssX, gnssY);

			}
			else if (observationGroup instanceof GNSSBaseline3DGroup && startPoint.getDimension() == 3 && endPoint.getDimension() == 3) {
				GNSSBaselineDeltaY3D gnssY = new GNSSBaselineDeltaY3D(id, startPoint, endPoint, y0, sigmaY0);
				GNSSBaselineDeltaX3D gnssX = new GNSSBaselineDeltaX3D(id, startPoint, endPoint, x0, sigmaX0);
				GNSSBaselineDeltaZ3D gnssZ = new GNSSBaselineDeltaZ3D(id, startPoint, endPoint, z0, sigmaZ0);

				gnssX.setReduction(this.reductions);
				gnssY.setReduction(this.reductions);
				gnssZ.setReduction(this.reductions);
				if (this.estimationType == EstimationType.SIMULATION) {
					gnssX.setValueApriori(gnssX.getValueAposteriori());
					gnssY.setValueApriori(gnssY.getValueAposteriori());
					gnssZ.setValueApriori(gnssZ.getValueAposteriori());
				}

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
					((Orientation)parameter).setEstimateApproximationValue(this.estimateOrientationApproximation && this.estimationType != EstimationType.SIMULATION);
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
				this.saveVerticalDeflections();

				this.saveObservations();
				this.saveAdditionalParameters();

				this.saveCongruenceAnalysisPointPair();
				this.saveStrainParameters();

				this.savePrincipalComponentAnalysis(this.networkAdjustment.getPrincipalComponents());
				this.saveRankDefect(this.networkAdjustment.getRankDefect());
				this.saveTestStatistic(this.networkAdjustment.getSignificanceTestStatisticParameters());
				this.saveBinomialTestStatistic(this.networkAdjustment.getBinomialTestStatisticParameters());
				this.saveVarianceComponents(this.networkAdjustment.getVarianceComponents());
				
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
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"VerticalDeflectionAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"ObservationAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"GNSSObservationAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"AdditionalParameterAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"CongruenceAnalysisPointPairAposteriori\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"CongruenceAnalysisStrainParameterAposteriori\"").execute();

		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"VarianceComponent\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"TestStatistic\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"BinomialTestStatistic\"").execute();
		this.dataBase.getPreparedStatement("TRUNCATE TABLE \"PrincipalComponent\"").execute();
	}

	private void savePoints() throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"PointAposteriori\" (" 
				+ "\"id\",\"y\",\"x\",\"z\"," 
				+ "\"sigma_y0\",\"sigma_x0\",\"sigma_z0\"," 
				+ "\"sigma_y\",\"sigma_x\",\"sigma_z\"," 
				+ "\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\"," 
				+ "\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\"," 
				+ "\"helmert_major_axis\",\"helmert_minor_axis\",\"helmert_alpha\"," 
				+ "\"residual_y\",\"residual_x\",\"residual_z\"," 
				+ "\"redundancy_y\",\"redundancy_x\",\"redundancy_z\"," 
				+ "\"gross_error_y\",\"gross_error_x\",\"gross_error_z\"," 
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\"," 
				+ "\"maximum_tolerable_bias_y\",\"maximum_tolerable_bias_x\",\"maximum_tolerable_bias_z\", "
				+ "\"influence_on_position_y\",\"influence_on_position_x\",\"influence_on_position_z\"," 
				+ "\"influence_on_network_distortion\", " 
				+ "\"first_principal_component_y\",\"first_principal_component_x\",\"first_principal_component_z\","
				+ "\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\",\"number_of_observations\") VALUES (" 
				+ "(SELECT \"id\" FROM \"PointApriori\" WHERE \"name\" = ?), "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
				
				// residuals epsilon = L - L0
				stmt.setDouble(idx++, dimension != 1 ? point.getY() - point.getY0() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getX() - point.getX0() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getZ() - point.getZ0() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getRedundancyY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getRedundancyX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getRedundancyZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getGrossErrorY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getGrossErrorX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getGrossErrorZ() : 0.0);

				stmt.setDouble(idx++, dimension != 1 ? point.getMinimalDetectableBiasY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getMinimalDetectableBiasX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getMinimalDetectableBiasZ() : 0.0);
				
				stmt.setDouble(idx++, dimension != 1 ? point.getMaximumTolerableBiasY() : 0.0);
				stmt.setDouble(idx++, dimension != 1 ? point.getMaximumTolerableBiasX() : 0.0);
				stmt.setDouble(idx++, dimension != 2 ? point.getMaximumTolerableBiasZ() : 0.0);

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
				stmt.setInt(idx++, point.numberOfObservations());

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

	private void saveVerticalDeflections() throws SQLException {
		boolean hasBatch = false;
		String sql = "INSERT INTO \"VerticalDeflectionAposteriori\" ("
				+ "\"id\",\"y\",\"x\"," 
				+ "\"sigma_y0\",\"sigma_x0\"," 
				+ "\"sigma_y\",\"sigma_x\"," 
				+ "\"confidence_major_axis\",\"confidence_minor_axis\"," 
				+ "\"residual_y\",\"residual_x\"," 
				+ "\"redundancy_y\",\"redundancy_x\"," 
				+ "\"gross_error_y\",\"gross_error_x\"," 
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\"," 
				+ "\"maximum_tolerable_bias_y\",\"maximum_tolerable_bias_x\"," 
				+ "\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\") VALUES (" 
				+ "(SELECT \"id\" FROM \"VerticalDeflectionApriori\" WHERE \"name\" = ?), " 
				+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			Map<String,Point> completePointsWithDeflections = new HashMap<String,Point>();
			completePointsWithDeflections.putAll(this.completePointsWithReferenceDeflections);
			completePointsWithDeflections.putAll(this.completePointsWithStochasticDeflections);
			completePointsWithDeflections.putAll(this.completePointsWithUnknownDeflections);
			
			for (Point point : completePointsWithDeflections.values() ) {
				int idx = 1;
				int dimension = point.getDimension();

				if (dimension == 2) // if (dimension != 3)
					continue;
				
				stmt.setString(idx++, point.getName());

				stmt.setDouble(idx++, point.getVerticalDeflectionY().getValue());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getValue());

				stmt.setDouble(idx++, (point.getVerticalDeflectionY().getStdApriori() > 0 ? point.getVerticalDeflectionY().getStdApriori() : 0.0));
				stmt.setDouble(idx++, (point.getVerticalDeflectionX().getStdApriori() > 0 ? point.getVerticalDeflectionX().getStdApriori() : 0.0));

				stmt.setDouble(idx++, point.getVerticalDeflectionY().getStd() > 0 ? point.getVerticalDeflectionY().getStd() : 0.0);
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getStd() > 0 ? point.getVerticalDeflectionX().getStd() : 0.0);

				stmt.setDouble(idx++, Math.max(point.getVerticalDeflectionX().getConfidence(), point.getVerticalDeflectionY().getConfidence()));
				stmt.setDouble(idx++, Math.min(point.getVerticalDeflectionX().getConfidence(), point.getVerticalDeflectionY().getConfidence()));

				// residuals epsilon = L - L0
				stmt.setDouble(idx++, point.getVerticalDeflectionY().getValue() - point.getVerticalDeflectionY().getValue0());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getValue() - point.getVerticalDeflectionX().getValue0());

				stmt.setDouble(idx++, point.getVerticalDeflectionY().getRedundancy());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getRedundancy());

				stmt.setDouble(idx++, point.getVerticalDeflectionY().getGrossError());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getGrossError());

				stmt.setDouble(idx++, point.getVerticalDeflectionY().getMinimalDetectableBias());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getMinimalDetectableBias());
				
				stmt.setDouble(idx++, point.getVerticalDeflectionY().getMaximumTolerableBias());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getMaximumTolerableBias());

				stmt.setDouble(idx++, point.getVerticalDeflectionX().getOmega() + point.getVerticalDeflectionY().getOmega());

				// Statistische Groessen in X abgelegt
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getPprio());
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getPpost());

				stmt.setDouble(idx++, point.getVerticalDeflectionX().getTprio()); 
				stmt.setDouble(idx++, point.getVerticalDeflectionX().getTpost());

				stmt.setBoolean(idx++, point.getVerticalDeflectionX().isSignificant());

				stmt.setInt(idx++, Math.min(point.getVerticalDeflectionX().getColInJacobiMatrix(), point.getVerticalDeflectionY().getColInJacobiMatrix()));

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


	private void saveObservations() throws SQLException {
		boolean hasBatch = false;
		String sqlTerObs = "INSERT INTO \"ObservationAposteriori\" ("
				+ "\"id\", \"value\", "
				+ "\"sigma_0\", \"sigma\", "
				+ "\"residual\", "
				+ "\"redundancy\", "
				+ "\"gross_error\", \"minimal_detectable_bias\", \"maximum_tolerable_bias\", "
				+ "\"influence_on_position\", \"influence_on_network_distortion\", "
				+ "\"omega\", "
				+ "\"p_prio\", \"p_post\", "
				+ "\"t_prio\", \"t_post\", "
				+ "\"significant\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String sqlGNSSObs = "INSERT INTO \"GNSSObservationAposteriori\" ("
				+ "\"id\", \"y\", \"x\", \"z\", "
				+ "\"sigma_y0\", \"sigma_x0\", \"sigma_z0\", "
				+ "\"sigma_y\", \"sigma_x\", \"sigma_z\", "
				+ "\"residual_y\", \"residual_x\", \"residual_z\", "
				+ "\"redundancy_y\", \"redundancy_x\", \"redundancy_z\", "
				+ "\"gross_error_y\", \"gross_error_x\", \"gross_error_z\", "
				+ "\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\", "
				+ "\"maximum_tolerable_bias_y\",\"maximum_tolerable_bias_x\",\"maximum_tolerable_bias_z\", "
				+ "\"influence_on_position_y\", \"influence_on_position_x\", \"influence_on_position_z\", "
				+ "\"influence_on_network_distortion\", "
				+ "\"omega\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"significant\") "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 

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
						
						// residuals epsilon = L - L0
						stmt.setDouble(idx++, gnssY == null ? 0.0 : -gnssY.getObservationalError());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : -gnssX.getObservationalError());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : -gnssZ.getObservationalError());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getRedundancy());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getRedundancy());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getRedundancy());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getGrossError());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getGrossError());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getGrossError());

						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getMinimalDetectableBias());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getMinimalDetectableBias());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getMinimalDetectableBias());
						
						stmt.setDouble(idx++, gnssY == null ? 0.0 : gnssY.getMaximumTolerableBias());
						stmt.setDouble(idx++, gnssX == null ? 0.0 : gnssX.getMaximumTolerableBias());
						stmt.setDouble(idx++, gnssZ == null ? 0.0 : gnssZ.getMaximumTolerableBias());

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

						// residuals epsilon = L - L0
						stmt.setDouble(idx++, -observation.getObservationalError());
						stmt.setDouble(idx++,  observation.getRedundancy());

						stmt.setDouble(idx++, observation.getGrossError());
						stmt.setDouble(idx++, observation.getMinimalDetectableBias());
						stmt.setDouble(idx++, observation.getMaximumTolerableBias());

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
	
	private void saveBinomialTestStatistic(BinomialTestStatisticParameters binomialTestStatisticParameters) throws SQLException {
		boolean hasBatch = false;

		String sql = "INSERT INTO \"BinomialTestStatistic\" ("
				+ "\"number_of_trials\", \"success_probability\", \"probability_value\", \"lower_tail_quantile\", \"upper_tail_quantile\" "
				+ ") VALUES (?,?,?,?,?)";

		try {
			this.dataBase.setAutoCommit(false);

			BinomialTestStatisticParameterSet[] parameters = binomialTestStatisticParameters.getBinomialTestStatisticParameterSets();

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			for ( int i=0; i<parameters.length; i++ ) {
				int idx = 1;
				BinomialTestStatisticParameterSet set = parameters[i];
				stmt.setInt(idx++, set.getNumberOfTrials());
				stmt.setDouble(idx++, set.getSuccessProbability());
				stmt.setDouble(idx++, set.getProbabilityValue());
				stmt.setDouble(idx++, set.getLowerTailQuantile());
				stmt.setDouble(idx++, set.getUpperTailQuantile());

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
				+ "\"type\", \"redundancy\", \"omega\", \"sigma2apost\", \"number_of_observations\", \"number_of_effective_observations\", \"number_of_negative_residuals\" "
				+ ") VALUES (?,?,?,?,?,?,?)";

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
				stmt.setInt(idx++,    vc.getNumberOfEffectiveObservations());
				stmt.setInt(idx++,    vc.getNumberOfNegativeResiduals());

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
