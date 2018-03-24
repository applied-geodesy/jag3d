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

package org.applied_geodesy.jag3d.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.util.sql.DataBase;
import org.applied_geodesy.version.jag3d.VersionType;

public class SQLManager3x {
	public final static double DATABASE_VERSION_3x = 1.29;
	public final static double DATABASE_VERSION_FX = 20180107; 
	
	private SQLManager3x() {}
	
	private static void prepareOADBForDataTransfer(DataBase projectDataBase) throws SQLException {
		PreparedStatement stmt = null;
		final String sqlSelectVersion = "SELECT \"version\" FROM \"Version\" WHERE \"type\" = ?";
		final String sqlUpdateVersion = "UPDATE \"Version\" SET \"version\" = ? WHERE \"type\" = ?";

		stmt = projectDataBase.getPreparedStatement(sqlSelectVersion);
		stmt.setInt(1, VersionType.DATABASE.getId());
		ResultSet rs = stmt.executeQuery();

		double databaseVersion = -1;
		if (rs.next()) {
			databaseVersion = rs.getDouble("version");
			if (rs.wasNull()) {
				throw new SQLException(SQLManager3x.class.getSimpleName() + " : Error, could not detect database version. Database update failed!");
			}
			
			Map<Double, String> querys = SQLManager.dataBase();

			for ( Map.Entry<Double, String> query : querys.entrySet() ) {
				double subDBVersion = query.getKey();
				String sql          = query.getValue();

				if (subDBVersion > databaseVersion && subDBVersion <= DATABASE_VERSION_FX) {
					stmt = projectDataBase.getPreparedStatement(sql);
					stmt.execute();

					// Speichere die Version des DB-Updates
					stmt = projectDataBase.getPreparedStatement(sqlUpdateVersion);
					stmt.setDouble(1, subDBVersion);
					stmt.setInt(2, VersionType.DATABASE.getId());
					stmt.execute();
				}
			}
		}
	}
	
	static void transferOADB3xToFX(DataBase projectDataBase) throws SQLException {
		prepareOADBForDataTransfer(projectDataBase);
		
		PreparedStatement stmt = null;
		String querys[] = new String[] {
				/** terrestrial observations **/
				// store observation groups
				"INSERT INTO \"ObservationGroup\" (\"id\", \"name\", \"type\", \"enable\", \"reference_epoch\") "
				+ "SELECT \"id\", \"name\", "
				// check for old GNSS type 6 == 50, 7 == 60, 8 == 70
				+ "CASEWHEN(\"type\" > 10, \"type\" / 10 + 1, \"type\") AS \"type\", "
				+ "\"enable\", \"reference_epoch\" FROM \"PUBLIC\".\"ObservationGroup\"",
				
//				// Epochs
//				"INSERT INTO \"ObservationGroupEpoch\" (\"group_id\", \"reference_epoch\") "
//				+ "SELECT \"id\", \"reference_epoch\" FROM \"PUBLIC\".\"ObservationGroup\"",
				
				// Uncertainties of groups
				"INSERT INTO \"ObservationGroupUncertainty\" (\"group_id\", \"type\", \"value\") "
				+ "SELECT \"id\", " + ObservationGroupUncertaintyType.ZERO_POINT_OFFSET.getId() + " AS \"type\", CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"sigma0_a\" * PI() / 200, \"sigma0_a\") AS \"sigma0_a\" FROM \"PUBLIC\".\"ObservationGroup\" "
				+ "UNION ALL "
				+ "SELECT \"id\", " + ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT.getId() + " AS \"type\", \"sigma0_b\" FROM \"PUBLIC\".\"ObservationGroup\" "
				+ "UNION ALL "
				+ "SELECT \"id\", " + ObservationGroupUncertaintyType.DISTANCE_DEPENDENT.getId() + " AS \"type\", \"sigma0_c\" FROM \"PUBLIC\".\"ObservationGroup\" ",
				
				// additional observation group parameters (a-priori)
				"INSERT INTO \"AdditionalParameterApriori\" (\"id\",\"group_id\",\"type\",\"value_0\",\"enable\") "
				+ "SELECT \"id\",\"group_id\",\"type\","
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"value\" * PI() / 200, \"value\") AS \"value\", "
				+ "CASEWHEN(\"fixed\" = TRUE, FALSE, TRUE) "
				+ "FROM \"PUBLIC\".\"AdditionalParameterApriori\"",
				
				// additional observation group parameters (a-posteriori)
				"INSERT INTO \"AdditionalParameterAposteriori\" (\"id\",\"value\",\"sigma\",\"confidence\",\"gross_error\",\"minimal_detectable_bias\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\") "
				+ "SELECT \"id\","
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"value\" * PI() / 200, \"value\") AS \"value\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"sigma\" * PI() / 200, \"sigma\") AS \"sigma\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"con\"   * PI() / 200, \"con\")   AS \"con\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"nabla\" * PI() / 200, \"nabla\") AS \"nabla\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"grzw\"  * PI() / 200, \"grzw\")  AS \"grzw\", "
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" FROM \"PUBLIC\".\"AdditionalParameterAposteriori\" JOIN \"PUBLIC\".\"AdditionalParameterApriori\" ON \"PUBLIC\".\"AdditionalParameterAposteriori\".\"id\" = \"PUBLIC\".\"AdditionalParameterApriori\".\"id\"",
				
				// store observations (a-priori)
				"INSERT INTO \"ObservationApriori\" (\"id\",\"group_id\",\"start_point_name\",\"end_point_name\",\"instrument_height\",\"reflector_height\",\"value_0\",\"sigma_0\",\"distance_0\",\"enable\") "
				+ "SELECT \"id\",\"group_id\",\"startpoint_id\",\"endpoint_id\",\"instrument_height\",\"reflector_height\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"value\" * PI() / 200, \"value\") AS \"value\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"sigma_0\" * PI() / 200, \"sigma_0\") AS \"sigma_0\", "
				+ "\"distance\",\"enable\" FROM \"PUBLIC\".\"ObservationApriori\" AS \"ObsAprioTab\" JOIN \"PUBLIC\".\"ObservationGroup\" ON \"PUBLIC\".\"ObservationGroup\".\"id\" = \"ObsAprioTab\".\"group_id\"",

				// store observations (a-posteriori)
				"INSERT INTO \"ObservationAposteriori\" (\"id\",\"value\",\"sigma_0\",\"sigma\",\"redundancy\",\"gross_error\",\"minimal_detectable_bias\",\"influence_on_position\",\"influence_on_network_distortion\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\") "
				+ "SELECT \"id\","
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"value\" * PI() / 200, \"value\") AS \"value\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"sigma_0\" * PI() / 200, \"sigma_0\") AS \"sigma_0\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"sigma\" * PI() / 200, \"sigma\") AS \"sigma\", "
				+ "\"redundancy\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"nabla\" * PI() / 200, \"nabla\") AS \"gross_error\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"grzw\" * PI() / 200, \"grzw\") AS \"minimal_detectable_bias\", "
				+ "\"ep\" AS \"influence_on_position\",\"efsp\" AS \"influence_on_network_distortion\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\", \"t_post\",\"outlier\" FROM \"PUBLIC\".\"ObservationAposteriori\" AS \"ObsApostTab\" JOIN \"PUBLIC\".\"ObservationApriori\" ON \"ObsApostTab\".\"id\" = \"PUBLIC\".\"ObservationApriori\".\"id\" JOIN \"PUBLIC\".\"ObservationGroup\" ON \"PUBLIC\".\"ObservationGroup\".\"id\" = \"PUBLIC\".\"ObservationApriori\".\"group_id\"",
				
				/** GNSS **/
				// GNSS (a-priori)
				"INSERT INTO \"GNSSObservationApriori\"(\"id\",\"group_id\",\"start_point_name\",\"end_point_name\",\"y0\",\"x0\",\"z0\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"enable\") "
				+ "SELECT \"id\", \"group_id\", \"startpoint_id\", \"endpoint_id\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\", \"enable\" FROM \"PUBLIC\".\"GNSSObservationApriori\" JOIN  \"PUBLIC\".\"ObservationGroup\" ON  \"PUBLIC\".\"GNSSObservationApriori\".\"group_id\" = \"PUBLIC\".\"ObservationGroup\".\"id\"",
				
				// GNSS (a-posteriori)
				"INSERT INTO \"GNSSObservationAposteriori\"(\"id\",\"y\",\"x\",\"z\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"sigma_y\",\"sigma_x\",\"sigma_z\",\"redundancy_y\",\"redundancy_x\",\"redundancy_z\",\"gross_error_y\",\"gross_error_x\",\"gross_error_z\",\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\",\"influence_on_position_y\",\"influence_on_position_x\",\"influence_on_position_z\",\"influence_on_network_distortion\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\") "
				+ "SELECT \"id\",\"y\",\"x\",\"z\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"sigma_y\",\"sigma_x\",\"sigma_z\",\"redundancy_y\",\"redundancy_x\",\"redundancy_z\",\"nabla_y\",\"nabla_x\",\"nabla_z\",\"grzw_y\",\"grzw_x\",\"grzw_z\",\"ep_y\",\"ep_x\",\"ep_z\",\"efsp\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"outlier\" FROM \"PUBLIC\".\"GNSSObservationAposteriori\" JOIN \"PUBLIC\".\"GNSSObservationApriori\" ON \"PUBLIC\".\"GNSSObservationApriori\".\"id\" = \"PUBLIC\".\"GNSSObservationAposteriori\".\"id\" JOIN \"PUBLIC\".\"ObservationGroup\" ON \"PUBLIC\".\"GNSSObservationApriori\".\"group_id\" = \"PUBLIC\".\"ObservationGroup\".\"id\"",
				
				/** points **/
				// store point group
				"INSERT INTO \"PointGroup\" (\"id\", \"name\", \"type\", \"dimension\", \"enable\", \"consider_deflection\") "
				+ "SELECT \"id\", \"name\", \"type\", \"dimension\", \"enable\", \"consider_deflection\" FROM \"PUBLIC\".\"PointGroup\"",
							
				// Uncertainties of point groups
				"INSERT INTO \"PointGroupUncertainty\" (\"group_id\", \"type\", \"value\") "
				+ "SELECT \"id\", " + PointGroupUncertaintyType.CONSTANT_X.getId() + " AS \"type\", \"sigma_x0\" FROM \"PUBLIC\".\"PointGroup\" WHERE \"PUBLIC\".\"PointGroup\".\"dimension\" <> 1"
				+ "UNION ALL "
				+ "SELECT \"id\", " + PointGroupUncertaintyType.CONSTANT_Y.getId() + " AS \"type\", \"sigma_y0\" FROM \"PUBLIC\".\"PointGroup\" WHERE \"PUBLIC\".\"PointGroup\".\"dimension\" <> 1"
				+ "UNION ALL "
				+ "SELECT \"id\", " + PointGroupUncertaintyType.CONSTANT_Z.getId() + " AS \"type\", \"sigma_z0\" FROM \"PUBLIC\".\"PointGroup\" WHERE \"PUBLIC\".\"PointGroup\".\"dimension\" <> 2"
				+ "UNION ALL "
				+ "SELECT \"id\", " + PointGroupUncertaintyType.DEFLECTION_X.getId() + " AS \"type\", \"sigma_dx0\" * PI() / 200 AS \"sigma_dx0\" FROM \"PUBLIC\".\"PointGroup\" WHERE \"PUBLIC\".\"PointGroup\".\"dimension\" = 3 AND \"PUBLIC\".\"PointGroup\".\"type\" = " + PointType.STOCHASTIC_POINT.getId() 
				+ "UNION ALL "
				+ "SELECT \"id\", " + PointGroupUncertaintyType.DEFLECTION_Y.getId() + " AS \"type\", \"sigma_dy0\" * PI() / 200 AS \"sigma_dy0\" FROM \"PUBLIC\".\"PointGroup\" WHERE \"PUBLIC\".\"PointGroup\".\"dimension\" = 3 AND \"PUBLIC\".\"PointGroup\".\"type\" = " + PointType.STOCHASTIC_POINT.getId(),

				// store points (a-priori)
				"INSERT INTO \"PointApriori\" (\"id\",\"group_id\",\"name\",\"code\",\"y0\",\"x0\",\"z0\", \"dy0\", \"dx0\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"sigma_dy0\",\"sigma_dx0\",\"enable\") "
				+ "SELECT \"id\",\"group_id\",\"point_id\",\"code\",\"y0\",\"x0\",\"z0\", "
				+ "IFNULL(\"dy0\" * PI() / 200, 0) AS \"dy0\","
				+ "IFNULL(\"dx0\" * PI() / 200, 0) AS \"dx0\","
				+ "\"sigma_y0\",\"sigma_x0\",\"sigma_z0\","
				+ "IFNULL(\"sigma_dy0\" * PI() / 200, 0) AS \"sigma_dy0\","
				+ "IFNULL(\"sigma_dx0\" * PI() / 200, 0) AS \"sigma_dx0\","
				+ "\"enable\" FROM \"PUBLIC\".\"PointApriori\" AS \"PointAprioTab\" JOIN \"PUBLIC\".\"PointGroup\" ON \"PUBLIC\".\"PointGroup\".\"id\" = \"PointAprioTab\".\"group_id\" LEFT JOIN \"PUBLIC\".\"DeflectionApriori\" ON \"PUBLIC\".\"DeflectionApriori\".\"id\" = \"PointAprioTab\".\"id\"",
				
				// store points (a-posteriori)
				"INSERT INTO \"PointAposteriori\" (\"id\",\"y\",\"x\",\"z\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"sigma_y\",\"sigma_x\",\"sigma_z\",\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\",\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\",\"helmert_major_axis\",\"helmert_minor_axis\",\"helmert_alpha\",\"redundancy_y\",\"redundancy_x\",\"redundancy_z\",\"gross_error_y\",\"gross_error_x\",\"gross_error_z\",\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\",\"influence_on_position_y\",\"influence_on_position_x\",\"influence_on_position_z\",\"influence_on_network_distortion\",\"first_principal_component_y\",\"first_principal_component_x\",\"first_principal_component_z\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\") "
				+ "SELECT \"id\",\"y\",\"x\",\"z\",\"sigma_y0\",\"sigma_x0\",\"sigma_z0\",\"sigma_y\",\"sigma_x\",\"sigma_z\","
				+ "\"a_con\" AS \"confidence_major_axis\", "
				+ "CASEWHEN(\"dimension\" = 3, \"b_con\", 0) AS \"confidence_middle_axis\", "
				+ "CASEWHEN(\"dimension\" = 3, \"c_con\", CASEWHEN(\"dimension\" = 2, \"b_con\", 0)) AS \"confidence_minor_axis\", "
				+ "\"alpha_con\" * PI() / 200 AS \"alpha_con\",\"beta_con\" * PI() / 200 AS \"beta_con\",\"gamma_con\" * PI() / 200 AS \"gamma_con\", "
				+ "\"a_helmert\",\"b_helmert\",\"alpha_helmert\" * PI() / 200 AS \"alpha_helmert\", "
				+ "\"redundancy_y\",\"redundancy_x\",\"redundancy_z\",\"nabla_y\",\"nabla_x\",\"nabla_z\",\"grzw_x\",\"grzw_y\",\"grzw_z\",\"ep_y\",\"ep_x\",\"ep_z\",\"efsp\",\"fpc_y\",\"fpc_x\",\"fpc_z\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"outlier\",\"covar_index\" "
				+ "FROM \"PUBLIC\".\"PointAposteriori\" JOIN \"PUBLIC\".\"PointApriori\" ON \"PUBLIC\".\"PointAposteriori\".\"id\" = \"PUBLIC\".\"PointApriori\".\"id\" JOIN \"PUBLIC\".\"PointGroup\" ON \"PUBLIC\".\"PointApriori\".\"group_id\" = \"PUBLIC\".\"PointGroup\".\"id\"",
				
				// store deflection of points (a-posteriori)
				"INSERT INTO \"DeflectionAposteriori\"(\"id\",\"dy\",\"dx\",\"sigma_dy0\",\"sigma_dx0\",\"sigma_dy\",\"sigma_dx\",\"confidence_major_axis\",\"confidence_minor_axis\",\"redundancy_dy\",\"redundancy_dx\",\"gross_error_dy\",\"gross_error_dx\",\"minimal_detectable_bias_dy\",\"minimal_detectable_bias_dx\",\"omega\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\",\"covar_index\") "
				+ "SELECT \"id\", \"dy\" * PI() / 200 AS \"dy\", \"dx\" * PI() / 200 AS \"dx\", \"sigma_dy0\" * PI() / 200 AS \"sigma_dy0\", \"sigma_dx0\" * PI() / 200 AS \"sigma_dx0\", \"sigma_dy\" * PI() / 200 AS \"sigma_dy\", \"sigma_dx\" * PI() / 200 AS \"sigma_dx\", \"a_con\" * PI() / 200 AS \"confidence_major_axis\", \"b_con\" * PI() / 200 AS \"confidence_minor_axis\", \"redundancy_dy\", \"redundancy_dx\", \"nabla_dy\" * PI() / 200 AS \"gross_error_dy\", \"nabla_dx\" * PI() / 200 AS \"gross_error_dx\", \"grzw_dy\" * PI() / 200 AS \"minimal_detectable_bias_dy\", \"grzw_dx\" * PI() / 200 AS \"minimal_detectable_bias_dx\", \"omega\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"outlier\", \"covar_index\" FROM \"PUBLIC\".\"DeflectionAposteriori\" JOIN \"PUBLIC\".\"DeflectionApriori\" ON  \"PUBLIC\".\"DeflectionAposteriori\".\"id\" = \"PUBLIC\".\"DeflectionApriori\".\"id\" JOIN \"PUBLIC\".\"PointApriori\" ON \"PUBLIC\".\"PointApriori\".\"id\" = \"PUBLIC\".\"DeflectionAposteriori\".\"id\"",

				/** Congruence analysis **/
				// store groups for deformation analysis
				"INSERT INTO \"CongruenceAnalysisGroup\"(\"id\",\"name\",\"dimension\",\"enable\") "
				+ "SELECT \"id\", \"name\", \"dimension\", \"enable\" FROM \"PUBLIC\".\"DeformationAnalysisTieGroup\" ",

				// store nexos of object points for deformation analysis (a-priori)
				"INSERT INTO \"CongruenceAnalysisPointPairApriori\"(\"id\",\"group_id\",\"start_point_name\",\"end_point_name\",\"enable\") "
				+ "SELECT \"id\",\"group_id\",\"startpoint_id\",\"endpoint_id\",\"enable\" FROM \"PUBLIC\".\"DeformationAnalysisTieApriori\" JOIN \"PUBLIC\".\"DeformationAnalysisTieGroup\" ON \"PUBLIC\".\"DeformationAnalysisTieApriori\".\"group_id\" = \"PUBLIC\".\"DeformationAnalysisTieGroup\".\"id\" ",
				
				// store nexos of object points for deformation analysis (a-posteriori)
				"INSERT INTO \"CongruenceAnalysisPointPairAposteriori\"(\"id\",\"y\",\"x\",\"z\",\"sigma_y\",\"sigma_x\",\"sigma_z\",\"confidence_major_axis\",\"confidence_middle_axis\",\"confidence_minor_axis\",\"confidence_alpha\",\"confidence_beta\",\"confidence_gamma\",\"confidence_major_axis_2d\",\"confidence_minor_axis_2d\",\"confidence_alpha_2d\",\"gross_error_y\",\"gross_error_x\",\"gross_error_z\",\"minimal_detectable_bias_y\",\"minimal_detectable_bias_x\",\"minimal_detectable_bias_z\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\") "
				+ "SELECT \"id\",\"delta_y\",\"delta_x\",\"delta_z\",\"sigma_y\",\"sigma_x\",\"sigma_z\","
				+ "\"a_con\" AS \"confidence_major_axis\", "
				+ "CASEWHEN(\"dimension\" = 3, \"b_con\", 0) AS \"confidence_middle_axis\", "
				+ "CASEWHEN(\"dimension\" = 3, \"c_con\", CASEWHEN(\"dimension\" = 2, \"b_con\", 0)) AS \"confidence_minor_axis\", "
				+ "\"alpha_con\" * PI() / 200 AS \"alpha_con\",\"beta_con\" * PI() / 200 AS \"beta_con\",\"gamma_con\" * PI() / 200 AS \"gamma_con\", "
				+ "\"a_con_2d\" AS \"confidence_major_axis_2d\", "
				+ "\"b_con_2d\" AS \"confidence_minor_axis_2d\", "
				+ "\"alpha_con_2d\" * PI() / 200 AS \"confidence_alpha_2d\", "
				+ "\"nabla_y\",\"nabla_x\",\"nabla_z\",\"grzw_y\",\"grzw_x\",\"grzw_z\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" FROM \"PUBLIC\".\"DeformationAnalysisTieAposteriori\" JOIN \"PUBLIC\".\"DeformationAnalysisTieApriori\" ON \"PUBLIC\".\"DeformationAnalysisTieAposteriori\".\"id\" = \"PUBLIC\".\"DeformationAnalysisTieApriori\".\"id\" JOIN \"PUBLIC\".\"DeformationAnalysisTieGroup\" ON \"PUBLIC\".\"DeformationAnalysisTieApriori\".\"group_id\" = \"PUBLIC\".\"DeformationAnalysisTieGroup\".\"id\" ",

				// store strain restrictions
				"INSERT INTO \"CongruenceAnalysisStrainParameterRestriction\"(\"group_id\",\"type\",\"enable\") "
				+ "SELECT \"group_id\",\"type\","
				+ "CASEWHEN(\"fixed\" = TRUE, FALSE, TRUE) "
				+ "FROM \"PUBLIC\".\"StrainParameterRestriction\" JOIN \"PUBLIC\".\"DeformationAnalysisTieGroup\" ON \"PUBLIC\".\"StrainParameterRestriction\".\"group_id\" = \"PUBLIC\".\"DeformationAnalysisTieGroup\".\"id\" ",
				
				// store a-posteriori strain parameters 
				"INSERT INTO \"CongruenceAnalysisStrainParameterAposteriori\"(\"group_id\",\"type\",\"value\",\"sigma\",\"confidence\",\"gross_error\",\"minimal_detectable_bias\",\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\") "
				+ "SELECT \"group_id\",\"type\","
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"value\" * PI() / 200, \"value\") AS \"value\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"sigma\" * PI() / 200, \"sigma\") AS \"sigma\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"con\" * PI() / 200, \"con\") AS \"con\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"nabla\" * PI() / 200, \"nabla\") AS \"nabla\", "
				+ "CASEWHEN(\"type\" IN (" + ParameterType.ORIENTATION.getId() + ", " + ParameterType.ROTATION_X.getId()  + ", " + ParameterType.ROTATION_Y.getId()  + ", " + ParameterType.ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_ROTATION_X.getId()  + ", " + ParameterType.STRAIN_ROTATION_Y.getId()  + ", " + ParameterType.STRAIN_ROTATION_Z.getId()  + ", " + ParameterType.STRAIN_SHEAR_X.getId()  + ", " + ParameterType.STRAIN_SHEAR_Y.getId()  + ", " + ParameterType.STRAIN_SHEAR_Z.getId() + "), \"grzw\" * PI() / 200, \"grzw\") AS \"grzw\", "
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" FROM \"PUBLIC\".\"StrainParameterAposteriori\" JOIN \"PUBLIC\".\"DeformationAnalysisTieGroup\" ON \"PUBLIC\".\"StrainParameterAposteriori\".\"group_id\" = \"PUBLIC\".\"DeformationAnalysisTieGroup\".\"id\" ",

				/** store project metadata **/
				"MERGE INTO \"ProjectMetadata\" USING ( "
				+ "SELECT \"id\", \"project_name\", \"operator\", \"project_description\", \"date\", '' AS \"customer_id\", '' AS \"project_id\" FROM \"PUBLIC\".\"GeneralSetting\" WHERE \"id\" = 1 LIMIT 1 "
				+ ") AS \"vals\" (\"id\", \"name\", \"operator\", \"description\", \"date\", \"customer_id\", \"project_id\") "
				+ "ON \"ProjectMetadata\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ProjectMetadata\".\"name\"        = \"vals\".\"name\", "
				+ "\"ProjectMetadata\".\"operator\"    = \"vals\".\"operator\", "
				+ "\"ProjectMetadata\".\"description\" = \"vals\".\"description\", "
				+ "\"ProjectMetadata\".\"date\"        = \"vals\".\"date\", "
				+ "\"ProjectMetadata\".\"customer_id\" = \"vals\".\"customer_id\", "
				+ "\"ProjectMetadata\".\"project_id\"  = \"vals\".\"project_id\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"operator\", "
				+ "\"vals\".\"description\", "
				+ "\"vals\".\"date\", "
				+ "\"vals\".\"customer_id\", "
				+ "\"vals\".\"project_id\" ",
				
				/** stochastic parameters and adjustment settings **/
				// adjustment settings
				"MERGE INTO \"AdjustmentDefinition\" USING ( "
				+ "SELECT \"id\", \"adjustment_type\", \"iteration\", \"bounded_influence\", 1 AS \"number_of_principal_components\", TRUE AS \"estimate_direction_set_orientation_approximation\", \"deformation_analysis\", \"export_covar\" FROM \"PUBLIC\".\"LeastSquareSetting\" JOIN \"PUBLIC\".\"GeneralSetting\" ON \"PUBLIC\".\"LeastSquareSetting\".\"id\" = \"PUBLIC\".\"GeneralSetting\".\"id\" WHERE \"PUBLIC\".\"LeastSquareSetting\".\"id\" = 1 LIMIT 1 "
				+ ") AS \"vals\" (\"id\", \"type\", \"number_of_iterations\", \"robust_estimation_limit\", \"number_of_principal_components\", \"estimate_direction_set_orientation_approximation\", \"congruence_analysis\", \"export_covariance_matrix\") "
				+ "ON \"AdjustmentDefinition\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"AdjustmentDefinition\".\"type\"                           = \"vals\".\"type\", "
				+ "\"AdjustmentDefinition\".\"number_of_iterations\"           = \"vals\".\"number_of_iterations\", "
				+ "\"AdjustmentDefinition\".\"robust_estimation_limit\"        = \"vals\".\"robust_estimation_limit\", "
				+ "\"AdjustmentDefinition\".\"number_of_principal_components\" = \"vals\".\"number_of_principal_components\", "
				+ "\"AdjustmentDefinition\".\"estimate_direction_set_orientation_approximation\" = \"vals\".\"estimate_direction_set_orientation_approximation\", "
				+ "\"AdjustmentDefinition\".\"congruence_analysis\"            = \"vals\".\"congruence_analysis\", "
				+ "\"AdjustmentDefinition\".\"export_covariance_matrix\"       = \"vals\".\"export_covariance_matrix\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"number_of_iterations\", "
				+ "\"vals\".\"robust_estimation_limit\", "
				+ "\"vals\".\"number_of_principal_components\", "
				+ "\"vals\".\"estimate_direction_set_orientation_approximation\", "
				+ "\"vals\".\"congruence_analysis\","
				+ "\"vals\".\"export_covariance_matrix\" ",
				
				// store threshold for averaging
				// check for old GNSS type 6 == 50, 7 == 60, 8 == 70
				"MERGE INTO \"AverageThreshold\" USING ( "
				+ "SELECT "
				+ "CASEWHEN(\"type\" > 10, \"type\" / 10 + 1, \"type\") AS \"type\", "
				+ "CASEWHEN(\"type\" IN (" + ObservationType.DIRECTION.getId() + ", " + ObservationType.ZENITH_ANGLE.getId() + "), \"threshold\" * PI() / 200, \"threshold\") AS \"threshold\" "
				+ "FROM \"PUBLIC\".\"AverageThreshold\" "
				+ ") AS \"vals\" (\"type\", \"value\") "
				+ "ON \"AverageThreshold\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"AverageThreshold\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value\" ",
				
				// store test statistics definition...
				"MERGE INTO \"TestStatisticDefinition\" USING ( "
				+ "SELECT \"id\", \"test_adjustment\", \"alpha\", \"beta\", \"global_alpha\" FROM \"PUBLIC\".\"LeastSquareSetting\" WHERE \"id\" = 1 LIMIT 1 "
				+ ") AS \"vals\" (\"id\", \"type\", \"probability_value\",\"power_of_test\",\"familywise_error_rate\") "
				+ "ON \"TestStatisticDefinition\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"TestStatisticDefinition\".\"type\"                  = \"vals\".\"type\", "
				+ "\"TestStatisticDefinition\".\"probability_value\"     = \"vals\".\"probability_value\", "
				+ "\"TestStatisticDefinition\".\"power_of_test\"         = \"vals\".\"power_of_test\", "
				+ "\"TestStatisticDefinition\".\"familywise_error_rate\" = \"vals\".\"familywise_error_rate\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"probability_value\", "
				+ "\"vals\".\"power_of_test\", "
				+ "\"vals\".\"familywise_error_rate\" ",
				
				// store estimated test statistics i.e. quantiles...
				"MERGE INTO \"TestStatistic\" USING ( "
				+ "SELECT \"f1\",\"f2\",\"alpha\",\"beta\",\"quantile\",\"ncp\",\"p\" FROM \"PUBLIC\".\"TestStatistic\" "
				+ ") AS \"vals\" (\"d1\",\"d2\",\"probability_value\",\"power_of_test\",\"quantile\",\"non_centrality_parameter\",\"p_value\") "
				+ "ON  \"TestStatistic\".\"d1\"                = \"vals\".\"d1\" "
				+ "AND \"TestStatistic\".\"d2\"                = \"vals\".\"d2\" "
				+ "AND \"TestStatistic\".\"probability_value\" = \"vals\".\"probability_value\" "
				+ "AND \"TestStatistic\".\"power_of_test\"     = \"vals\".\"power_of_test\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"TestStatistic\".\"quantile\"                 = \"vals\".\"quantile\", "
				+ "\"TestStatistic\".\"non_centrality_parameter\" = \"vals\".\"non_centrality_parameter\", "
				+ "\"TestStatistic\".\"p_value\"                  = \"vals\".\"p_value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"d1\", "
				+ "\"vals\".\"d2\", "
				+ "\"vals\".\"probability_value\", "
				+ "\"vals\".\"power_of_test\", "
				+ "\"vals\".\"quantile\", "
				+ "\"vals\".\"non_centrality_parameter\", "
				+ "\"vals\".\"p_value\" ",
				
				// store projection
				"MERGE INTO \"ProjectionDefinition\" USING ( "
				+ "SELECT \"id\", \"type\", \"reference_height\" FROM \"PUBLIC\".\"Projection\" WHERE \"id\" = 1 LIMIT 1 "
				+ ") AS \"vals\" (\"id\", \"type\", \"reference_height\") "
				+ "ON \"ProjectionDefinition\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ProjectionDefinition\".\"type\"             = \"vals\".\"type\", "
				+ "\"ProjectionDefinition\".\"reference_height\" = \"vals\".\"reference_height\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"reference_height\" ",
				
				// Principle component analysis
				"INSERT INTO \"PrincipalComponent\" (\"index\",\"value\",\"ratio\") "
				+ "SELECT \"index\",\"value\",\"ratio\" FROM \"PUBLIC\".\"PrincipalComponentAnalysis\" ",
				
				// rank defect of NES
				"MERGE INTO \"RankDefect\" USING ( "
				+ "SELECT \"id\",\"user_defined\",\"ty\",\"tx\",\"tz\",\"ry\",\"rx\",\"rz\",\"sy\",\"sx\",\"sz\",\"my\",\"mx\",\"mz\",\"mxy\",\"mxyz\" FROM \"PUBLIC\".\"RankDefect\" WHERE \"id\" = 1 LIMIT 1 "
				+ ") AS \"vals\" (\"id\",\"user_defined\",\"ty\",\"tx\",\"tz\",\"ry\",\"rx\",\"rz\",\"sy\",\"sx\",\"sz\",\"my\",\"mx\",\"mz\",\"mxy\",\"mxyz\") "
				+ "ON \"RankDefect\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"RankDefect\".\"user_defined\" = \"vals\".\"user_defined\", "
				+ "\"RankDefect\".\"ty\"           = \"vals\".\"ty\", "
				+ "\"RankDefect\".\"tx\"           = \"vals\".\"tx\", "
				+ "\"RankDefect\".\"tz\"           = \"vals\".\"tz\", "
				+ "\"RankDefect\".\"ry\"           = \"vals\".\"ry\", "
				+ "\"RankDefect\".\"rx\"           = \"vals\".\"rx\", "
				+ "\"RankDefect\".\"rz\"           = \"vals\".\"rz\", "
				+ "\"RankDefect\".\"sy\"           = \"vals\".\"sy\", "
				+ "\"RankDefect\".\"sx\"           = \"vals\".\"sx\", "
				+ "\"RankDefect\".\"sz\"           = \"vals\".\"sz\", "
				+ "\"RankDefect\".\"my\"           = \"vals\".\"my\", "
				+ "\"RankDefect\".\"mx\"           = \"vals\".\"mx\", "
				+ "\"RankDefect\".\"mz\"           = \"vals\".\"mz\", "
				+ "\"RankDefect\".\"mxy\"          = \"vals\".\"mxy\", "
				+ "\"RankDefect\".\"mxyz\"         = \"vals\".\"mxyz\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"user_defined\", "
				+ "\"vals\".\"ty\", "
				+ "\"vals\".\"tx\", "
				+ "\"vals\".\"tz\", "
				+ "\"vals\".\"ry\", "
				+ "\"vals\".\"rx\", "
				+ "\"vals\".\"rz\", "
				+ "\"vals\".\"sy\", "
				+ "\"vals\".\"sx\", "
				+ "\"vals\".\"sz\", "
				+ "\"vals\".\"my\", "
				+ "\"vals\".\"mx\", "
				+ "\"vals\".\"mz\", "
				+ "\"vals\".\"mxy\", "
				+ "\"vals\".\"mxyz\" ",

				// store varicance components 
				"INSERT INTO \"VarianceComponent\"(\"type\",\"redundancy\",\"omega\",\"sigma2apost\",\"number_of_observations\") "
				+ "SELECT "
				+ "(CASE \"id\" "
				+ "WHEN   -1 THEN " + VarianceComponentType.GLOBAL.getId() + " "

				+ "WHEN    1 THEN " + VarianceComponentType.LEVELING_COMPONENT.getId() + " "
				+ "WHEN    2 THEN " + VarianceComponentType.DIRECTION_COMPONENT.getId() + " "
				+ "WHEN    3 THEN " + VarianceComponentType.HORIZONTAL_DISTANCE_COMPONENT.getId() + " "
				+ "WHEN    4 THEN " + VarianceComponentType.SLOPE_DISTANCE_COMPONENT.getId() + " "
				+ "WHEN    5 THEN " + VarianceComponentType.ZENITH_ANGLE_COMPONENT.getId() + " "
				+ "WHEN   50 THEN " + VarianceComponentType.GNSS1D_COMPONENT.getId() + " "
				+ "WHEN   60 THEN " + VarianceComponentType.GNSS2D_COMPONENT.getId() + " "
				+ "WHEN   70 THEN " + VarianceComponentType.GNSS3D_COMPONENT.getId() + " "

				+ "WHEN  100 THEN " + VarianceComponentType.LEVELING_COMPONENT.getId() + " "
				+ "WHEN  101 THEN " + VarianceComponentType.LEVELING_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN  102 THEN " + VarianceComponentType.LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN  103 THEN " + VarianceComponentType.LEVELING_DISTANCE_DEPENDENT_COMPONENT.getId() + " "

				+ "WHEN  200 THEN " + VarianceComponentType.DIRECTION_COMPONENT.getId() + " "
				+ "WHEN  201 THEN " + VarianceComponentType.DIRECTION_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN  202 THEN " + VarianceComponentType.DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN  203 THEN " + VarianceComponentType.DIRECTION_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN  300 THEN " + VarianceComponentType.HORIZONTAL_DISTANCE_COMPONENT.getId() + " "
				+ "WHEN  301 THEN " + VarianceComponentType.HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN  302 THEN " + VarianceComponentType.HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN  303 THEN " + VarianceComponentType.HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN  400 THEN " + VarianceComponentType.SLOPE_DISTANCE_COMPONENT.getId() + " "
				+ "WHEN  401 THEN " + VarianceComponentType.SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN  402 THEN " + VarianceComponentType.SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN  403 THEN " + VarianceComponentType.SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN  500 THEN " + VarianceComponentType.ZENITH_ANGLE_COMPONENT.getId() + " "
				+ "WHEN  501 THEN " + VarianceComponentType.ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN  502 THEN " + VarianceComponentType.ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN  503 THEN " + VarianceComponentType.ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT.getId() + " "	
				
				+ "WHEN 5000 THEN " + VarianceComponentType.GNSS1D_COMPONENT.getId() + " "
				+ "WHEN 5001 THEN " + VarianceComponentType.GNSS1D_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN 5002 THEN " + VarianceComponentType.GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN 5003 THEN " + VarianceComponentType.GNSS1D_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN 6000 THEN " + VarianceComponentType.GNSS2D_COMPONENT.getId() + " "
				+ "WHEN 6001 THEN " + VarianceComponentType.GNSS2D_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN 6002 THEN " + VarianceComponentType.GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN 6003 THEN " + VarianceComponentType.GNSS2D_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN 7000 THEN " + VarianceComponentType.GNSS3D_COMPONENT.getId() + " "
				+ "WHEN 7001 THEN " + VarianceComponentType.GNSS3D_ZERO_POINT_OFFSET_COMPONENT.getId() + " "
				+ "WHEN 7002 THEN " + VarianceComponentType.GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				+ "WHEN 7003 THEN " + VarianceComponentType.GNSS3D_DISTANCE_DEPENDENT_COMPONENT.getId() + " "
				
				+ "WHEN 100001 THEN " + VarianceComponentType.STOCHASTIC_POINT_1D_COMPONENT.getId() + " "
				+ "WHEN 100002 THEN " + VarianceComponentType.STOCHASTIC_POINT_2D_COMPONENT.getId() + " "
				+ "WHEN 100003 THEN " + VarianceComponentType.STOCHASTIC_POINT_3D_COMPONENT.getId() + " "
				+ "WHEN 101112 THEN " + VarianceComponentType.STOCHASTIC_POINT_DEFLECTION_COMPONENT.getId() + " "
				
				+ "ELSE -999999 - \"id\" " // Unknown/unsupported component id; create dummy id
				+ "END CASE) AS \"id\", "
				+ "\"redundancy\",\"omega\",\"sigma2apost\",\"number_of_observations\" FROM \"PUBLIC\".\"VarianceEstimation\" ",
		};
		
		for (String sql : querys) {
			stmt = projectDataBase.getPreparedStatement(sql);
			stmt.execute();
		}
	}
	
	static void updateOADB3x(DataBase projectDataBase) throws SQLException {
		double dbVersion = DATABASE_VERSION_3x;
		
		PreparedStatement stmt = projectDataBase.getPreparedStatement("SET SCHEMA \"PUBLIC\"");
		stmt.execute();
		
		stmt = projectDataBase.getPreparedStatement("SELECT \"database_version\" FROM \"GeneralSetting\" WHERE \"id\" = 1");
		ResultSet result = stmt.executeQuery();
		
		if (result.next()) {
			dbVersion  = result.getDouble("database_version");	
			if (dbVersion < DATABASE_VERSION_3x) {
				
				Map<Double, String> dbUpdates = new LinkedHashMap<Double, String>();

				dbUpdates.put(1.001, "ALTER TABLE \"ObservationAposteriori\" ADD \"grzw\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.01,  "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"lambda\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.011, "ALTER TABLE \"ObservationGroup\" ADD \"reference_epoch\" BOOLEAN DEFAULT TRUE\r\n"); 
				dbUpdates.put(1.012, "ALTER TABLE \"LeastSquareSetting\" ADD \"deformation_analysis\" BOOLEAN DEFAULT FALSE\r\n"); 
				dbUpdates.put(1.013, "CREATE TABLE \"DeformationAnalysisTieGroup\" (\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY, \"name\" VARCHAR(255) NOT NULL, \"enable\" BOOLEAN NOT NULL)\r\n"); 
				dbUpdates.put(1.014, "CREATE TABLE \"DeformationAnalysisTieAposteriori\" (\"id\" INTEGER NOT NULL PRIMARY KEY, \"magnitude\" DOUBLE NOT NULL, \"delta_y\" DOUBLE NOT NULL, \"delta_x\" DOUBLE NOT NULL, \"delta_z\" DOUBLE NOT NULL, \"sigma_magnitude\" DOUBLE NOT NULL, \"sigma_y\" DOUBLE NOT NULL, \"sigma_x\" DOUBLE NOT NULL, \"sigma_z\" DOUBLE NOT NULL, \"a_con\" DOUBLE NOT NULL, \"b_con\" DOUBLE NOT NULL, \"c_con\" DOUBLE NOT NULL, \"alpha_con\" DOUBLE NOT NULL, \"beta_con\" DOUBLE NOT NULL, \"gamma_con\" DOUBLE NOT NULL, \"t_prio\" DOUBLE NOT NULL, \"t_post\" DOUBLE NOT NULL, \"significant\" BOOLEAN NOT NULL)\r\n"); 
				dbUpdates.put(1.015, "CREATE TABLE \"DeformationAnalysisTieApriori\" (\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY, \"group_id\" INTEGER NOT NULL, \"startpoint_id\" VARCHAR(255) NOT NULL, \"endpoint_id\" VARCHAR(255) NOT NULL, \"enable\" BOOLEAN NOT NULL)\r\n"); 
				dbUpdates.put(1.016, "CREATE UNIQUE INDEX \"Pkt_UNIQUE_1\" ON \"DeformationAnalysisTieApriori\" (\"startpoint_id\")\r\n"); 
				dbUpdates.put(1.02,  "CREATE UNIQUE INDEX \"Pkt_UNIQUE_2\" ON \"DeformationAnalysisTieApriori\" (\"endpoint_id\")\r\n");
				
				dbUpdates.put(1.021, "ALTER TABLE \"PointAposteriori\" ADD \"a_helmert\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.022, "ALTER TABLE \"PointAposteriori\" ADD \"b_helmert\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.03,  "ALTER TABLE \"PointAposteriori\" ADD \"alpha_helmert\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.031, "ALTER TABLE \"LeastSquareSetting\" ADD \"neighbourhood_adjustment_scheme\" INTEGER DEFAULT 0\r\n");
				dbUpdates.put(1.032, "CREATE TABLE \"NeighbourhoodAdjustmentScheme\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"a\" DOUBLE,\"b\" DOUBLE)\r\n");
				dbUpdates.put(1.033, "INSERT INTO \"NeighbourhoodAdjustmentScheme\" (\"id\", \"a\", \"b\") VALUES (0, NULL, NULL)\r\n");
				dbUpdates.put(1.034, "INSERT INTO \"NeighbourhoodAdjustmentScheme\" (\"id\", \"a\", \"b\") VALUES (1,  2.0, 0.0)\r\n");
				dbUpdates.put(1.035, "INSERT INTO \"NeighbourhoodAdjustmentScheme\" (\"id\", \"a\", \"b\") VALUES (2,  0.5, 0.0)\r\n");
				dbUpdates.put(1.04,  "INSERT INTO \"NeighbourhoodAdjustmentScheme\" (\"id\", \"a\", \"b\") VALUES (3, 10.0, 4.0)\r\n");
				
				dbUpdates.put(1.041, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"a_con_2d\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.042, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"b_con_2d\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.05,  "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"alpha_con_2d\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.06,  "ALTER TABLE \"GeneralSetting\" ADD \"export_covar\" BOOLEAN DEFAULT FALSE\r\n");			

				dbUpdates.put(1.061, "CREATE TABLE \"Projection\" (\"id\" INTEGER NOT NULL PRIMARY KEY,\"type\" INTEGER NOT NULL,\"reference_height\" DOUBLE NOT NULL,\"x0\" DOUBLE NOT NULL,\"y0\" DOUBLE NOT NULL,\"z0\" DOUBLE NOT NULL,\"h0\" DOUBLE NOT NULL,\"user_defined_mass_centre\" BOOLEAN NOT NULL)\r\n");
				dbUpdates.put(1.062, "INSERT INTO \"Projection\" (\"id\", \"type\", \"reference_height\", \"x0\", \"y0\", \"z0\", \"h0\", \"user_defined_mass_centre\") VALUES (1, 0, 0, 0, 0, 0, 0, false)\r\n");
				dbUpdates.put(1.07,  "UPDATE \"AdditionalParameterApriori\" SET \"value\" = \"value\" -1 WHERE \"type\" = 4\r\n");
				
				dbUpdates.put(1.08,  "ALTER TABLE \"LeastSquareSetting\" ADD \"automated_variance_estimation\" BOOLEAN DEFAULT FALSE\r\n");	
				
				dbUpdates.put(1.09,  "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"linear_proof\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.10,  "ALTER TABLE \"PointAposteriori\" ADD \"covar_index\" INTEGER DEFAULT -1\r\n");
				
				dbUpdates.put(1.11,  "ALTER TABLE \"VarianceEstimation\" ADD \"number_of_observations\" INTEGER DEFAULT 0\r\n");

				dbUpdates.put(1.1101, "ALTER TABLE \"PointAposteriori\" ADD \"ep_x\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1102, "ALTER TABLE \"PointAposteriori\" ADD \"ep_y\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1103, "ALTER TABLE \"PointAposteriori\" ADD \"ep_z\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1104, "ALTER TABLE \"NeighbourhoodAdjustmentScheme\" ALTER COLUMN \"a\" SET NULL\r\n");
				dbUpdates.put(1.1105, "ALTER TABLE \"NeighbourhoodAdjustmentScheme\" ALTER COLUMN \"b\" SET NULL\r\n");
				dbUpdates.put(1.1106, "UPDATE \"NeighbourhoodAdjustmentScheme\" SET \"a\" = NULL, \"b\" = NULL WHERE \"id\" = 0\r\n");
				dbUpdates.put(1.1107, "ALTER TABLE \"GeneralSetting\" ADD \"table_row_scheme\" INTEGER DEFAULT 0\r\n");
				dbUpdates.put(1.1108, "CREATE TABLE \"TableRowScheme\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"lower\" DOUBLE,\"upper\" DOUBLE)\r\n");
				dbUpdates.put(1.1109, "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (0, NULL, NULL)\r\n");
				dbUpdates.put(1.1110, "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (1, NULL, NULL)\r\n");
				dbUpdates.put(1.1111, "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (2, 0.3, 0.6)\r\n");
				dbUpdates.put(1.1112, "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (3, 0.005, 0.02)\r\n");
				dbUpdates.put(1.12,   "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (4, 0.001, 0.005)\r\n");
				
				dbUpdates.put(1.13,   "INSERT INTO \"NeighbourhoodAdjustmentScheme\" (\"id\", \"a\", \"b\") VALUES (4,  NULL, NULL)\r\n");
				
				dbUpdates.put(1.1301, "CREATE TABLE \"GNSSObservationApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"startpoint_id\" VARCHAR(255) NOT NULL,\"endpoint_id\" VARCHAR(255) NOT NULL,\"x0\" DOUBLE NOT NULL,\"y0\" DOUBLE NOT NULL,\"z0\" DOUBLE NOT NULL,\"sigma_x0\" DOUBLE NOT NULL,\"sigma_y0\" DOUBLE NOT NULL,\"sigma_z0\" DOUBLE NOT NULL,\"enable\" BOOLEAN NOT NULL)\r\n");
				dbUpdates.put(1.1302, "CREATE TABLE \"GNSSObservationAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"x\" DOUBLE NOT NULL,\"y\" DOUBLE NOT NULL,\"z\" DOUBLE NOT NULL,\"sigma_x0\" DOUBLE NOT NULL,\"sigma_y0\" DOUBLE NOT NULL,\"sigma_z0\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"redundancy_x\" DOUBLE NOT NULL,\"redundancy_y\" DOUBLE NOT NULL,\"redundancy_z\" DOUBLE NOT NULL,\"nabla_x\" DOUBLE NOT NULL,\"nabla_y\" DOUBLE NOT NULL,\"nabla_z\" DOUBLE NOT NULL,\"ep_x\" DOUBLE NOT NULL,\"ep_y\" DOUBLE NOT NULL,\"ep_z\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"outlier\" BOOLEAN NOT NULL)\r\n");
				dbUpdates.put(1.1303, "ALTER TABLE \"ObservationGroup\" ADD \"sigma0_a\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1304, "ALTER TABLE \"ObservationGroup\" ADD \"sigma0_b\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1305, "ALTER TABLE \"ObservationGroup\" ADD \"sigma0_c\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1306, "UPDATE \"ObservationGroup\" SET \"sigma0_a\" = \"sigma0_const\"\r\n");

				dbUpdates.put(1.1307, "UPDATE \"ObservationGroup\" SET \"sigma0_c\" = \"sigma0_vari\" WHERE \"type\" != 1\r\n");
				dbUpdates.put(1.1308, "UPDATE \"ObservationGroup\" SET \"sigma0_b\" = \"sigma0_vari\" WHERE \"type\"  = 1\r\n");
				dbUpdates.put(1.1309, "ALTER TABLE \"ObservationGroup\" DROP \"sigma0_const\"\r\n");
				dbUpdates.put(1.1310, "ALTER TABLE \"ObservationGroup\" DROP \"sigma0_vari\"\r\n");
				dbUpdates.put(1.14,   "ALTER TABLE \"VarianceEstimation\" ADD \"k_group\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.1401, "ALTER TABLE \"PointAposteriori\" ADD \"grzw_x\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1402, "ALTER TABLE \"PointAposteriori\" ADD \"grzw_y\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1403, "ALTER TABLE \"PointAposteriori\" ADD \"grzw_z\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.1405, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"grzw_x\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1406, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"grzw_y\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1407, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"grzw_z\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.1408, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_1d_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1409, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_2d_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1410, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_3d_prio\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.1411, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_1d_post\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1412, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_2d_post\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1413, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_3d_post\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.15, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"alpha_global\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.16, "INSERT INTO \"TableRowScheme\" (\"id\", \"lower\", \"upper\") VALUES (5, 0.1, 5.0)\r\n");
				
				dbUpdates.put(1.161, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"k_global_lower\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.162, "ALTER TABLE \"GlobalAdjustmentResult\" ADD \"k_global_upper\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.163, "ALTER TABLE \"VarianceEstimation\" ADD \"k_lower\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.17,  "ALTER TABLE \"VarianceEstimation\" ADD \"k_upper\" DOUBLE DEFAULT 0.0\r\n");
				
				dbUpdates.put(1.1710, "CREATE TABLE \"TestStatistic\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"f1\" DOUBLE NOT NULL,\"f2\" DOUBLE NOT NULL,\"alpha\" DOUBLE NOT NULL,\"beta\" DOUBLE NOT NULL,\"quantile\" DOUBLE NOT NULL,\"ncp\" DOUBLE NOT NULL)\r\n");
				dbUpdates.put(1.1711, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (1, 1, -9999999, (SELECT \"alpha_1d_prio\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_1d_prio\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				dbUpdates.put(1.1712, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (2, 1, (SELECT \"degree_of_freedom\"-1 AS \"f2\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"alpha_1d_post\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_1d_post\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				
				dbUpdates.put(1.1713, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (3, 2, -9999999, (SELECT \"alpha_2d_prio\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_2d_prio\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				dbUpdates.put(1.1714, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (4, 2, (SELECT \"degree_of_freedom\"-2 AS \"f2\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"alpha_2d_post\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_2d_post\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				
				dbUpdates.put(1.1715, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (5, 3, -9999999, (SELECT \"alpha_3d_prio\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_3d_prio\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				dbUpdates.put(1.1716, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (6, 3, (SELECT \"degree_of_freedom\"-3 AS \"f2\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"alpha_3d_post\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_3d_post\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				
				dbUpdates.put(1.1717, "INSERT INTO \"TestStatistic\" (\"id\", \"f1\", \"f2\", \"alpha\", \"quantile\", \"ncp\", \"beta\") VALUES (7, (SELECT \"degree_of_freedom\" AS \"f1\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), -9999999, (SELECT \"alpha_global\" AS \"alpha\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"k_global\" AS \"quantile\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1),(SELECT \"lambda\" AS \"ncp\" FROM \"GlobalAdjustmentResult\" WHERE \"id\" = 1), (SELECT \"beta\" FROM \"LeastSquareSetting\" WHERE \"id\" = 1) )\r\n");
				
				dbUpdates.put(1.1720, "ALTER TABLE \"VarianceEstimation\" DROP \"k_group\"\r\n");
				dbUpdates.put(1.1721, "ALTER TABLE \"VarianceEstimation\" DROP \"k_lower\"\r\n");
				dbUpdates.put(1.1722, "ALTER TABLE \"VarianceEstimation\" DROP \"k_upper\"\r\n");
				
				dbUpdates.put(1.1730, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_1d_prio\"\r\n");
				dbUpdates.put(1.1731, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_2d_prio\"\r\n");
				dbUpdates.put(1.1732, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_3d_prio\"\r\n");
				
				dbUpdates.put(1.1733, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_1d_post\"\r\n");
				dbUpdates.put(1.1734, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_2d_post\"\r\n");
				dbUpdates.put(1.1735, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_3d_post\"\r\n");
				
				dbUpdates.put(1.1736, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"alpha_global\"\r\n");
				dbUpdates.put(1.1737, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_global\"\r\n");
				dbUpdates.put(1.1738, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_global_lower\"\r\n");
				dbUpdates.put(1.1739, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_global_upper\"\r\n");
				dbUpdates.put(1.1740, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"lambda\"\r\n");
				
				dbUpdates.put(1.1741, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_1d_prio\"\r\n");
				dbUpdates.put(1.1742, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_2d_prio\"\r\n");
				dbUpdates.put(1.1743, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_3d_prio\"\r\n");
				
				dbUpdates.put(1.1744, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_1d_post\"\r\n");
				dbUpdates.put(1.1745, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_2d_post\"\r\n");
				dbUpdates.put(1.1746, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"k_3d_post\"\r\n");
				
				dbUpdates.put(1.1747, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"sigma2apost\"\r\n");
				dbUpdates.put(1.1748, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"degree_of_freedom\"\r\n");
				dbUpdates.put(1.1749, "ALTER TABLE \"GlobalAdjustmentResult\" DROP \"omega\"\r\n");
				
				dbUpdates.put(1.1760, "ALTER TABLE \"ObservationAposteriori\" ADD \"p_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1761, "ALTER TABLE \"ObservationAposteriori\" ADD \"p_post\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1762, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"p_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1763, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"p_post\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1764, "ALTER TABLE \"PointAposteriori\" ADD \"p_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1765, "ALTER TABLE \"PointAposteriori\" ADD \"p_post\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1766, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"p_prio\" DOUBLE DEFAULT 0.0\r\n");
				dbUpdates.put(1.1767, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"p_post\" DOUBLE DEFAULT 0.0\r\n");

				dbUpdates.put(1.1770, "ALTER TABLE \"LeastSquareSetting\" ADD \"global_alpha\" BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.1800, "ALTER TABLE \"LeastSquareSetting\" ADD \"test_adjustment\" INTEGER DEFAULT 2\r\n");
				
				dbUpdates.put(1.181, "ALTER TABLE \"ObservationAposteriori\" ADD \"efsp\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.182, "ALTER TABLE \"PointAposteriori\" ADD \"efsp\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.190, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"efsp\" DOUBLE DEFAULT 0\r\n");
				
				dbUpdates.put(1.200, "ALTER TABLE \"TestStatistic\" ADD \"p\" DOUBLE DEFAULT 0\r\n");
				
				dbUpdates.put(1.201, "CREATE TABLE \"AverageThreshold\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"type\" INTEGER NOT NULL,\"threshold\" DOUBLE NOT NULL)\r\n");
				dbUpdates.put(1.202, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (1,  0.05)\r\n");
				dbUpdates.put(1.203, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (50, 0.20)\r\n");
				dbUpdates.put(1.204, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (2,  0.05)\r\n");
				dbUpdates.put(1.205, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (3,  0.10)\r\n");
				dbUpdates.put(1.206, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (60, 0.20)\r\n");
				dbUpdates.put(1.207, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (4,  0.10)\r\n");
				dbUpdates.put(1.208, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (5,  0.05)\r\n");
				dbUpdates.put(1.210, "INSERT INTO \"AverageThreshold\" (\"type\", \"threshold\") VALUES (70, 0.20)\r\n");
					
				dbUpdates.put(1.211, "ALTER TABLE \"DeformationAnalysisTieGroup\" ADD \"dimension\" TINYINT DEFAULT 3\r\n");
				dbUpdates.put(1.212, "DROP INDEX \"Pkt_UNIQUE_1\" IF EXISTS\r\n");
				dbUpdates.put(1.213, "DROP INDEX \"Pkt_UNIQUE_2\" IF EXISTS\r\n");
				dbUpdates.put(1.214, "CREATE UNIQUE INDEX \"TieGroup_UNIQUE\" ON \"DeformationAnalysisTieApriori\"(\"group_id\",\"startpoint_id\",\"endpoint_id\")\r\n");
				dbUpdates.put(1.215, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"grzw_y\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.216, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"grzw_x\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.217, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"grzw_z\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.220, "UPDATE \"DeformationAnalysisTieGroup\" AS \"DefoGroup\" SET \"dimension\" = (SELECT MIN(CASE WHEN \"SG\".\"dimension\" > \"EG\".\"dimension\" THEN \"EG\".\"dimension\" ELSE \"SG\".\"dimension\" END) AS \"dim\" FROM \"DeformationAnalysisTieGroup\" JOIN \"DeformationAnalysisTieApriori\" ON \"DeformationAnalysisTieGroup\".\"id\" = \"DeformationAnalysisTieApriori\".\"group_id\" JOIN \"PointApriori\" AS \"SP\" ON \"DeformationAnalysisTieApriori\".\"startpoint_id\" = \"SP\".\"point_id\" JOIN \"PointApriori\" AS \"EP\" ON \"DeformationAnalysisTieApriori\".\"endpoint_id\" = \"EP\".\"point_id\" JOIN \"PointGroup\" AS \"SG\" ON \"SP\".\"group_id\" = \"SG\".\"id\" JOIN \"PointGroup\" AS \"EG\" ON \"EP\".\"group_id\" = \"EG\".\"id\" WHERE  \"DeformationAnalysisTieGroup\".\"id\" = \"DefoGroup\".\"id\")\r\n");
				
				dbUpdates.put(1.221, "ALTER TABLE \"AdditionalParameterAposteriori\" ADD \"p_prio\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.222, "ALTER TABLE \"AdditionalParameterAposteriori\" ADD \"p_post\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.223, "ALTER TABLE \"AdditionalParameterAposteriori\" ADD \"con\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.224, "ALTER TABLE \"AdditionalParameterAposteriori\" ADD \"nabla\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.230, "ALTER TABLE \"AdditionalParameterAposteriori\" ADD \"grzw\" DOUBLE DEFAULT 0\r\n");
			
				dbUpdates.put(1.23100, "CREATE TABLE \"StrainParameterRestriction\"(\"group_id\" INTEGER NOT NULL,\"type\" INTEGER NOT NULL,\"fixed\" BOOLEAN NOT NULL, PRIMARY KEY(\"group_id\",\"type\"))\r\n");
				dbUpdates.put(1.23200, "CREATE TABLE \"StrainParameterAposteriori\"(\"group_id\" INTEGER NOT NULL,\"type\" INTEGER NOT NULL,\"x\" DOUBLE NOT NULL,\"y\" DOUBLE NOT NULL,\"z\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"conf_a\" DOUBLE NOT NULL,\"conf_b\" DOUBLE NOT NULL,\"conf_c\" DOUBLE NOT NULL,\"nabla_x\" DOUBLE NOT NULL,\"nabla_y\" DOUBLE NOT NULL,\"nabla_z\" DOUBLE NOT NULL,\"grzw_x\" DOUBLE NOT NULL,\"grzw_y\" DOUBLE NOT NULL,\"grzw_z\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, PRIMARY KEY(\"group_id\",\"type\"))\r\n");
				dbUpdates.put(1.23300, "CREATE TABLE \"StrainObservationAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"epsilon_x\" DOUBLE NOT NULL,\"epsilon_y\" DOUBLE NOT NULL,\"epsilon_z\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"redundancy_x\" DOUBLE NOT NULL,\"redundancy_y\" DOUBLE NOT NULL,\"redundancy_z\" DOUBLE NOT NULL,\"conf_a\" DOUBLE NOT NULL,\"conf_b\" DOUBLE NOT NULL,\"conf_c\" DOUBLE NOT NULL,\"nabla_x\" DOUBLE NOT NULL,\"nabla_y\" DOUBLE NOT NULL,\"nabla_z\" DOUBLE NOT NULL,\"grzw_x\" DOUBLE NOT NULL,\"grzw_y\" DOUBLE NOT NULL,\"grzw_z\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"outlier\" BOOLEAN NOT NULL)\r\n");
				
				dbUpdates.put(1.23301, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  3, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 2\r\n");
				dbUpdates.put(1.23302, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  9, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 2\r\n");
				
				dbUpdates.put(1.23303, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  1, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23304, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  2, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23305, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  7, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23306, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  8, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23307, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  6, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23308, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 12, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				dbUpdates.put(1.23309, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 78, FALSE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" <> 1\r\n");
				
				dbUpdates.put(1.23310, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  0, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.23311, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  4, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.23312, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\",  5, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.23313, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 10, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.23314, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 11, TRUE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.23315, "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 79, FALSE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");
				dbUpdates.put(1.24,    "INSERT INTO \"StrainParameterRestriction\" (\"group_id\", \"type\", \"fixed\") SELECT \"id\", 89, FALSE FROM \"DeformationAnalysisTieGroup\" WHERE \"dimension\" = 3\r\n");

				dbUpdates.put(1.2411, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" DROP \"sigma_magnitude\"\r\n");
				dbUpdates.put(1.2412, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"nabla_x\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.2413, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"nabla_y\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.2414, "ALTER TABLE \"DeformationAnalysisTieAposteriori\" ADD \"nabla_z\" DOUBLE DEFAULT 0\r\n");
				
				dbUpdates.put(1.2421, "UPDATE \"DeformationAnalysisTieAposteriori\" SET \"nabla_x\" = \"delta_x\" \r\n");
				dbUpdates.put(1.2422, "UPDATE \"DeformationAnalysisTieAposteriori\" SET \"nabla_y\" = \"delta_y\" \r\n");
				dbUpdates.put(1.2423, "UPDATE \"DeformationAnalysisTieAposteriori\" SET \"nabla_z\" = \"delta_z\" \r\n");
				
				dbUpdates.put(1.2431, "DROP TABLE \"StrainObservationAposteriori\"\r\n");
				dbUpdates.put(1.2432, "DROP TABLE \"StrainParameterAposteriori\"\r\n");
				
				dbUpdates.put(1.25,   "CREATE TABLE \"StrainParameterAposteriori\"(\"group_id\" INTEGER NOT NULL,\"type\" INTEGER NOT NULL,\"value\" DOUBLE NOT NULL,\"sigma\" DOUBLE NOT NULL,\"con\" DOUBLE NOT NULL,\"nabla\" DOUBLE NOT NULL,\"grzw\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, PRIMARY KEY(\"group_id\",\"type\"))\r\n");
				
				dbUpdates.put(1.251,  "ALTER TABLE \"LeastSquareSetting\" ADD \"extended_variance_estimation\" BOOLEAN DEFAULT TRUE\r\n");
				dbUpdates.put(1.26,   "ALTER TABLE \"LeastSquareSetting\" DROP \"automated_variance_estimation\"\r\n");
				
				dbUpdates.put(1.261,  "ALTER TABLE \"PointAposteriori\" ADD \"fpc_y\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.262,  "ALTER TABLE \"PointAposteriori\" ADD \"fpc_x\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.263,  "ALTER TABLE \"PointAposteriori\" ADD \"fpc_z\" DOUBLE DEFAULT 0\r\n");
				dbUpdates.put(1.27,   "CREATE TABLE \"PrincipalComponentAnalysis\"(\"index\" INTEGER NOT NULL PRIMARY KEY,\"value\" DOUBLE NOT NULL,\"ratio\" DOUBLE NOT NULL)\r\n");
				
				dbUpdates.put(1.271,  "CREATE TABLE \"DeflectionApriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"dx0\" DOUBLE NOT NULL,\"dy0\" DOUBLE NOT NULL,\"sigma_dx0\" DOUBLE NOT NULL,\"sigma_dy0\" DOUBLE NOT NULL)");
				dbUpdates.put(1.272,  "CREATE TABLE \"DeflectionAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"dy\" DOUBLE NOT NULL,\"dx\" DOUBLE NOT NULL,\"sigma_dy\" DOUBLE NOT NULL,\"sigma_dx\" DOUBLE NOT NULL,\"a_con\" DOUBLE NOT NULL,\"b_con\" DOUBLE NOT NULL,\"redundancy_dy\" DOUBLE NOT NULL,\"redundancy_dx\" DOUBLE NOT NULL,\"nabla_dy\" DOUBLE NOT NULL,\"nabla_dx\" DOUBLE NOT NULL,\"grzw_dy\" DOUBLE NOT NULL,\"grzw_dx\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"covar_index\" INTEGER NOT NULL,\"outlier\" BOOLEAN NOT NULL,\"sigma_dy0\" DOUBLE NOT NULL,\"sigma_dx0\" DOUBLE NOT NULL)");
				dbUpdates.put(1.273,  "INSERT INTO \"DeflectionApriori\" (\"id\", \"dx0\", \"dy0\", \"sigma_dx0\", \"sigma_dy0\") SELECT \"id\", 0, 0, 0, 0 FROM \"PointApriori\"");

				dbUpdates.put(1.2741,  "ALTER TABLE \"PointGroup\" ADD \"consider_deflection\" BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2742,  "ALTER TABLE \"PointGroup\" ADD \"sigma_dy0\" DOUBLE DEFAULT 0.0003\r\n");
				dbUpdates.put(1.2743,  "ALTER TABLE \"PointGroup\" ADD \"sigma_dx0\" DOUBLE DEFAULT 0.0003\r\n");

				dbUpdates.put(1.2751,  "ALTER TABLE \"Projection\" DROP \"x0\" \r\n");
				dbUpdates.put(1.2752,  "ALTER TABLE \"Projection\" DROP \"y0\" \r\n");
				dbUpdates.put(1.2753,  "ALTER TABLE \"Projection\" DROP \"z0\" \r\n");
				dbUpdates.put(1.2754,  "ALTER TABLE \"Projection\" DROP \"h0\" \r\n");
				dbUpdates.put(1.2755,  "ALTER TABLE \"Projection\" DROP \"user_defined_mass_centre\" \r\n");
				dbUpdates.put(1.28,    "UPDATE \"Projection\" SET \"type\" = 0 WHERE \"type\" = 1 \r\n");
				
				dbUpdates.put(1.2811,  "ALTER TABLE \"RankDefect\" ADD \"user_defined\" BOOLEAN DEFAULT FALSE\r\n");
				
				dbUpdates.put(1.2812,  "ALTER TABLE \"RankDefect\" ADD \"mx\"   BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2813,  "ALTER TABLE \"RankDefect\" ADD \"my\"   BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2814,  "ALTER TABLE \"RankDefect\" ADD \"mz\"   BOOLEAN DEFAULT FALSE\r\n");
				
				dbUpdates.put(1.2815,  "ALTER TABLE \"RankDefect\" ADD \"mxy\"  BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2816,  "ALTER TABLE \"RankDefect\" ADD \"mxyz\" BOOLEAN DEFAULT FALSE\r\n");
				
				dbUpdates.put(1.2817,  "ALTER TABLE \"RankDefect\" ADD \"sx\"   BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2818,  "ALTER TABLE \"RankDefect\" ADD \"sy\"   BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.2819,  "ALTER TABLE \"RankDefect\" ADD \"sz\"   BOOLEAN DEFAULT FALSE\r\n");

				dbUpdates.put(1.2821,  "UPDATE \"RankDefect\" SET \"mz\"   = \"m1d\"\r\n");
				dbUpdates.put(1.2822,  "UPDATE \"RankDefect\" SET \"mxy\"  = \"m2d\"\r\n");
				dbUpdates.put(1.2823,  "UPDATE \"RankDefect\" SET \"mxyz\" = \"m3d\"\r\n");
				
				dbUpdates.put(1.2831,  "ALTER TABLE \"RankDefect\" DROP \"m1d\"\r\n");
				dbUpdates.put(1.2832,  "ALTER TABLE \"RankDefect\" DROP \"m2d\"\r\n");
				dbUpdates.put(1.29,    "ALTER TABLE \"RankDefect\" DROP \"m3d\"\r\n");
				
				
				String sqlUpdateVersion = "UPDATE \"GeneralSetting\" SET \"database_version\" = ? WHERE \"id\" = 1";
				for ( Map.Entry<Double, String> sql : dbUpdates.entrySet() ) {
					if (sql.getKey() > dbVersion) {
						stmt = projectDataBase.getPreparedStatement(sql.getValue());
						stmt.execute();

						// Speichere die Version des DB-Updates
						stmt = projectDataBase.getPreparedStatement(sqlUpdateVersion);
						stmt.setDouble(1, sql.getKey());
						stmt.execute();
					}
				}
				
				stmt = projectDataBase.getPreparedStatement(sqlUpdateVersion);
				stmt.setDouble(1, DATABASE_VERSION_3x);
				stmt.execute();
				
				// Korrektur der +INF-Values in TestStatistic (Platzhalter -9999999 -> +INF)
				if (dbVersion < 1.1717) {
					String sqlMaxDouble2Inf = "UPDATE \"PUBLIC\".\"TestStatistic\" SET \"f2\" = ? WHERE \"f2\" = -9999999\r\n";
					stmt = projectDataBase.getPreparedStatement(sqlMaxDouble2Inf);
					stmt.setDouble(1, Double.POSITIVE_INFINITY);
					stmt.execute();
				}
			}
		}
	}

}
