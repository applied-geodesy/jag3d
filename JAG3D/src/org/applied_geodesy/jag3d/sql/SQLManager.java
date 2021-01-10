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
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.UnscentedTransformationParameter;
import org.applied_geodesy.adjustment.network.DefaultAverageThreshold;
import org.applied_geodesy.adjustment.network.DefectType;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.RankDefect;
import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.adjustment.network.approximation.sql.SQLApproximationManager;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.adjustment.network.observation.reduction.ProjectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.Reduction;
import org.applied_geodesy.adjustment.network.observation.reduction.ReductionTaskType;
import org.applied_geodesy.adjustment.network.sql.SQLAdjustmentManager;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.jag3d.ui.dialog.LeastSquaresSettingDialog.LeastSquaresSettings;
import org.applied_geodesy.jag3d.ui.graphic.UIGraphicPaneBuilder;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.SQLGraphicManager;
import org.applied_geodesy.jag3d.ui.io.ImportOption;
import org.applied_geodesy.jag3d.ui.io.report.FTLReport;
import org.applied_geodesy.jag3d.ui.metadata.MetaData;
import org.applied_geodesy.jag3d.ui.metadata.UIMetaDataPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UICongruenceAnalysisPropertiesPane;
import org.applied_geodesy.jag3d.ui.propertiespane.UICongruenceAnalysisPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UIObservationPropertiesPane;
import org.applied_geodesy.jag3d.ui.propertiespane.UIObservationPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UIPointPropertiesPane;
import org.applied_geodesy.jag3d.ui.propertiespane.UIPointPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UIVerticalDeflectionPropertiesPane;
import org.applied_geodesy.jag3d.ui.propertiespane.UIVerticalDeflectionPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.table.UIAdditionalParameterTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UICongruenceAnalysisTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIGNSSObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIPointTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIPrincipalComponentTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITerrestrialObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITestStatisticTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIVarianceComponentTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIVerticalDeflectionTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.AdditionalParameterRow;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.PrincipalComponentRow;
import org.applied_geodesy.jag3d.ui.table.row.Row;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.TestStatisticRow;
import org.applied_geodesy.jag3d.ui.table.row.VarianceComponentRow;
import org.applied_geodesy.jag3d.ui.table.row.VerticalDeflectionRow;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.DefaultTableRowHighlightValue;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.FormatterOptions.FormatterOption;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.util.sql.DataBase;
import org.applied_geodesy.util.unit.AngleUnit;
import org.applied_geodesy.util.unit.LengthUnit;
import org.applied_geodesy.util.unit.ScaleUnit;
import org.applied_geodesy.util.unit.Unit;
import org.applied_geodesy.util.unit.UnitType;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;
import org.applied_geodesy.version.jag3d.Version;
import org.applied_geodesy.version.VersionType;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

public class SQLManager {
	private I18N i18n = I18N.getInstance();
	private final static String TABLE_STORAGE_TYPE = "CACHED"; //"CACHED"; 
	private DataBase dataBase;
	private HostServices hostServices;
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	private static SQLManager SQL_MANAGER = new SQLManager();

	private SQLManager() {}

	public static SQLManager getInstance() {
		return SQL_MANAGER;
	}

	public static SQLManager openExistingProject(DataBase db) throws ClassNotFoundException, SQLException, DatabaseVersionMismatchException {
		SQL_MANAGER.setDataBase(db);
		SQL_MANAGER.prepareDataBase(Boolean.FALSE);
		SQL_MANAGER.loadExistingDataBase();
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
		UITreeBuilder.getInstance().getTree().getSelectionModel().selectFirst();
		
		return SQL_MANAGER;
	}

	public static SQLManager createNewProject(DataBase db) throws ClassNotFoundException, SQLException, DatabaseVersionMismatchException {
		SQL_MANAGER.setDataBase(db);
		SQL_MANAGER.prepareDataBase(Boolean.TRUE);
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
		UITreeBuilder.getInstance().getTree().getSelectionModel().selectFirst();
		
		return SQL_MANAGER;
	}
	
	public static void setHostServices(HostServices hostServices) {
		SQL_MANAGER.hostServices = hostServices;
	}
	
	public static void closeProject() {
		SQL_MANAGER.closeDataBase();
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
		UITreeBuilder.getInstance().removeAllItems();
		UITreeBuilder.getInstance().getTree().getSelectionModel().selectFirst();
	}

	public FTLReport getFTLReport() {
		if (this.hasDatabase() && this.dataBase.isOpen()) {
			FTLReport ftlReport = new FTLReport(this.dataBase, this.hostServices);
			return ftlReport;
		}
		return null;
	}	
	
	public SQLAdjustmentManager getAdjustmentManager() {
		if (this.hasDatabase() && this.dataBase.isOpen()) {
			return new SQLAdjustmentManager(this.dataBase);
		}
		return null;
	}
	
	public SQLGraphicManager getSQLGraphicManager() {
		if (this.hasDatabase() && this.dataBase.isOpen()) {
			return new SQLGraphicManager(this.dataBase);
		}
		return null;
	}
	
	public SQLApproximationManager getApproximationManager() {
		if (this.hasDatabase() && this.dataBase.isOpen()) {
			return new SQLApproximationManager(this.dataBase);
		}
		return null;
	}

	private void setDataBase(DataBase db) throws ClassNotFoundException, SQLException {
		this.closeDataBase();
		this.fireDatabaseStateChanged(ProjectDatabaseStateType.OPENING);
		this.dataBase = db;
		this.dataBase.open();
		this.fireDatabaseStateChanged(this.hasDatabase() ? ProjectDatabaseStateType.OPENED : ProjectDatabaseStateType.CLOSED);
	}

	private void loadExistingDataBase() throws SQLException {
		this.loadTableRowHighlight();
		this.loadImportPreferences();
		
		this.loadPointGroups();
		this.loadObservationGroups();
		this.loadGNSSObservationGroups();
		this.loadCongruenceAnalysisGroups();
		this.loadVerticalDeflectioGroups();
	}

	private void prepareDataBase(boolean newProject) throws SQLException, DatabaseVersionMismatchException {
		UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
		UITreeBuilder.getInstance().removeAllItems();
		
		UIGraphicPaneBuilder.getInstance().getLayerManager().clearAllLayers();
		boolean transferOADB3FxT0FX = false;
		String[] initSqls = new String[0];
		if (!newProject && this.isOADBVersionFX()) { //&& false // use false to force update
			initSqls = new String[] {
					"SET SCHEMA \"OpenAdjustment\";",
			};
		}
		else {
			// Update old DB to the last state of 3.x
			if (!newProject && this.isOADBVersion3x()) {
				Optional<ButtonType> result = OptionDialog.showConfirmationDialog(
						i18n.getString("SQLManager.database.version.3x.title", "JAG3D v3.x project detected"), 
						i18n.getString("SQLManager.database.version.3x.header", "Do you want to update the selected database?"), 
						i18n.getString("SQLManager.database.version.3x.message", "The selected database contains an existing project, created by an earlier version 3.x of JAG3D. JAG3D can try to update the existing project to the current version. Please create a backup of the database bevor running the update.")
				);
				if (result.get() == ButtonType.OK) {
					SQLManager3x.updateOADB3x(this.dataBase);
					transferOADB3FxT0FX = true;
				}
				else {
					this.closeDataBase(); // Stop openning process
					// return
				}
			}

			initSqls = new String[] {
					"SET FILES NIO SIZE 8192;",
					"DROP SCHEMA \"OpenAdjustment\" IF EXISTS CASCADE;",
					"CREATE SCHEMA \"OpenAdjustment\";",

					"SET INITIAL SCHEMA \"OpenAdjustment\";",
					"SET SCHEMA \"OpenAdjustment\";",

					"CREATE " + TABLE_STORAGE_TYPE + " TABLE \"Version\"(\"type\" SMALLINT NOT NULL PRIMARY KEY, \"version\" DOUBLE NOT NULL);",
					"INSERT INTO \"Version\" (\"type\", \"version\") VALUES (" + VersionType.ADJUSTMENT_CORE.getId() + ", 0), (" + VersionType.DATABASE.getId() + ", 0), (" + VersionType.USER_INTERFACE.getId() + ", 0);"
			};
		}

		for (String sql : initSqls) {
			PreparedStatement statment = this.dataBase.getPreparedStatement(sql);
			statment.execute();
		}

		if (transferOADB3FxT0FX && !newProject && this.isOADBVersion3x())
			SQLManager3x.transferOADB3xToFX(this.dataBase);

		this.updateDatabase();
		this.initFormatterOptions();
	}

	private void updateDatabase() throws SQLException, DatabaseVersionMismatchException {
		PreparedStatement stmt = null;
		final String sqlSelectVersion = "SELECT \"version\" FROM \"Version\" WHERE \"type\" = ?";
		final String sqlUpdateVersion = "UPDATE \"Version\" SET \"version\" = ? WHERE \"type\" = ?";

		stmt = this.dataBase.getPreparedStatement(sqlSelectVersion);
		stmt.setInt(1, VersionType.DATABASE.getId());
		ResultSet rs = stmt.executeQuery();

		double databaseVersion = -1;
		if (rs.next()) {
			databaseVersion = rs.getDouble("version");
			if (rs.wasNull()) {
				throw new SQLException(this.getClass().getSimpleName() + " : Error, could not detect database version. Database update failed!");
			}

			if (databaseVersion > Version.get(VersionType.DATABASE)) {
				//this.closeDataBase();
				throw new DatabaseVersionMismatchException("Error, database version of the stored project is greater than accepted database version of the application: " + databaseVersion + " > " +  Version.get(VersionType.DATABASE));
			}
			
			Map<Double, String> querys = SQLManager.dataBase();
			for ( Map.Entry<Double, String> query : querys.entrySet() ) {
				double subDBVersion = query.getKey();
				String sql          = query.getValue();
				if (subDBVersion > databaseVersion && subDBVersion <= Version.get(VersionType.DATABASE)) {
					stmt = this.dataBase.getPreparedStatement(sql);
					stmt.execute();

					// Speichere die Version des DB-Updates
					stmt = this.dataBase.getPreparedStatement(sqlUpdateVersion);
					stmt.setDouble(1, subDBVersion);
					stmt.setInt(2, VersionType.DATABASE.getId());
					stmt.execute();
				}
			}

			stmt = this.dataBase.getPreparedStatement(sqlUpdateVersion);
			stmt.setDouble(1, Version.get(VersionType.DATABASE));
			stmt.setInt(2, VersionType.DATABASE.getId());
			stmt.execute();
			
			stmt.setDouble(1, Version.get(VersionType.USER_INTERFACE));
			stmt.setInt(2, VersionType.USER_INTERFACE.getId());
			stmt.execute();

		}
	}

	private boolean isOADBVersionFX() throws SQLException {
		String sql = "SELECT TRUE AS \"exists\" "
				+ "FROM \"INFORMATION_SCHEMA\".\"COLUMNS\" "
				+ "WHERE \"TABLE_SCHEMA\" = 'OpenAdjustment' AND "
				+ "\"TABLE_NAME\"  = 'Version' AND "
				+ "\"COLUMN_NAME\" = 'type'";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			boolean exists = rs.getBoolean("exists");
			return !rs.wasNull() && exists;
		}
		return false;
	}

	private boolean isOADBVersion3x() throws SQLException {
		String sql = "SELECT TRUE AS \"exists\" "
				+ "FROM \"INFORMATION_SCHEMA\".\"COLUMNS\" "
				+ "WHERE \"TABLE_SCHEMA\" = 'PUBLIC' AND "
				+ "\"TABLE_NAME\"  = 'GeneralSetting' AND "
				+ "\"COLUMN_NAME\" = 'database_version'";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			boolean exists = rs.getBoolean("exists");
			return !rs.wasNull() && exists;
		}
		return false;
	}

	static Map<Double, String> dataBase() {
		Map<Double, String> sqls = new LinkedHashMap<Double, String>();

		sqls.put(20180106.0101, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ObservationGroup\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"name\" VARCHAR(256) NOT NULL,\"type\" SMALLINT NOT NULL,\"enable\" BOOLEAN NOT NULL, \"reference_epoch\" BOOLEAN DEFAULT TRUE NOT NULL);\r\n");
		sqls.put(20180106.0102, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ObservationGroupUncertainty\"(\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"value\" DOUBLE NOT NULL, PRIMARY KEY(\"group_id\",\"type\"), CONSTRAINT \"ObservationGroupUncertaintyOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"ObservationGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20180106.0103, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ObservationApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"start_point_name\" VARCHAR(256) NOT NULL,\"end_point_name\" VARCHAR(256) NOT NULL,\"instrument_height\" DOUBLE DEFAULT 0 NOT NULL,\"reflector_height\" DOUBLE DEFAULT 0 NOT NULL,\"value_0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_0\" DOUBLE DEFAULT 0 NOT NULL,\"distance_0\" DOUBLE DEFAULT 0 NOT NULL,\"enable\" BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT \"ObservationOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"ObservationGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20180106.0104, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ObservationAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"value\" DOUBLE NOT NULL,\"sigma_0\" DOUBLE NOT NULL,\"sigma\" DOUBLE NOT NULL,\"redundancy\" DOUBLE NOT NULL,\"gross_error\" DOUBLE NOT NULL,\"minimal_detectable_bias\" DOUBLE NOT NULL,\"influence_on_position\" DOUBLE NOT NULL,\"influence_on_network_distortion\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, CONSTRAINT \"ObservationOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"ObservationApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		sqls.put(20180106.0105, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"GNSSObservationApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"start_point_name\" VARCHAR(256) NOT NULL,\"end_point_name\" VARCHAR(256) NOT NULL,\"y0\" DOUBLE DEFAULT 0 NOT NULL,\"x0\" DOUBLE DEFAULT 0 NOT NULL,\"z0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_y0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_x0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_z0\" DOUBLE DEFAULT 0 NOT NULL,\"enable\" BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT \"GNSSObservationOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"ObservationGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20180106.0106, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"GNSSObservationAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"y\" DOUBLE NOT NULL,\"x\" DOUBLE NOT NULL,\"z\" DOUBLE NOT NULL,\"sigma_y0\" DOUBLE NOT NULL,\"sigma_x0\" DOUBLE NOT NULL,\"sigma_z0\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"redundancy_y\" DOUBLE NOT NULL,\"redundancy_x\" DOUBLE NOT NULL,\"redundancy_z\" DOUBLE NOT NULL,\"gross_error_y\" DOUBLE NOT NULL,\"gross_error_x\" DOUBLE NOT NULL,\"gross_error_z\" DOUBLE NOT NULL,\"minimal_detectable_bias_y\" DOUBLE NOT NULL,\"minimal_detectable_bias_x\" DOUBLE NOT NULL,\"minimal_detectable_bias_z\" DOUBLE NOT NULL,\"influence_on_position_y\" DOUBLE NOT NULL,\"influence_on_position_x\" DOUBLE NOT NULL,\"influence_on_position_z\" DOUBLE NOT NULL,\"influence_on_network_distortion\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, CONSTRAINT \"GNSSObservationOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"GNSSObservationApriori\"(\"id\") ON DELETE CASCADE);\r\n");
		
		sqls.put(20180106.0107, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"AdditionalParameterApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"value_0\" DOUBLE DEFAULT 0 NOT NULL,\"enable\" BOOLEAN NOT NULL, CONSTRAINT \"AdditionalParameterOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"ObservationGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20180106.0108, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"AdditionalParameterAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"value\" DOUBLE NOT NULL,\"sigma\" DOUBLE NOT NULL,\"confidence\" DOUBLE NOT NULL,\"gross_error\" DOUBLE NOT NULL,\"minimal_detectable_bias\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, CONSTRAINT \"AdditionalParameterOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"AdditionalParameterApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		
		sqls.put(20180106.0201, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PointGroup\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"name\" VARCHAR(256) NOT NULL,\"type\" SMALLINT NOT NULL,\"dimension\" TINYINT NOT NULL,\"enable\" BOOLEAN NOT NULL, \"consider_deflection\" BOOLEAN DEFAULT FALSE NOT NULL);\r\n");
		sqls.put(20180106.0202, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PointGroupUncertainty\"(\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"value\" DOUBLE NOT NULL, PRIMARY KEY(\"group_id\",\"type\"), CONSTRAINT \"PointGroupUncertaintyOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"PointGroup\"(\"id\") ON DELETE CASCADE);\r\n");

		sqls.put(20180106.0204, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PointApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"name\" VARCHAR(256) NOT NULL,\"code\" VARCHAR(256) NOT NULL,\"y0\" DOUBLE DEFAULT 0 NOT NULL,\"x0\" DOUBLE DEFAULT 0 NOT NULL,\"z0\" DOUBLE DEFAULT 0 NOT NULL,\"dy0\" DOUBLE DEFAULT 0 NOT NULL,\"dx0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_y0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_x0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_z0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_dy0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_dx0\" DOUBLE DEFAULT 0 NOT NULL,\"enable\" BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT \"PointOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"PointGroup\"(\"id\") ON DELETE CASCADE, CONSTRAINT \"UniquePointName\" UNIQUE (\"name\"));\r\n");
		sqls.put(20180106.0205, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PointAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"y\" DOUBLE NOT NULL,\"x\" DOUBLE NOT NULL,\"z\" DOUBLE NOT NULL,\"sigma_y0\" DOUBLE NOT NULL,\"sigma_x0\" DOUBLE NOT NULL,\"sigma_z0\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"confidence_major_axis\" DOUBLE NOT NULL,\"confidence_middle_axis\" DOUBLE NOT NULL,\"confidence_minor_axis\" DOUBLE NOT NULL,\"confidence_alpha\" DOUBLE NOT NULL,\"confidence_beta\" DOUBLE NOT NULL,\"confidence_gamma\" DOUBLE NOT NULL,\"helmert_major_axis\" DOUBLE NOT NULL,\"helmert_minor_axis\" DOUBLE NOT NULL,\"helmert_alpha\" DOUBLE NOT NULL,\"redundancy_y\" DOUBLE NOT NULL,\"redundancy_x\" DOUBLE NOT NULL,\"redundancy_z\" DOUBLE NOT NULL,\"gross_error_y\" DOUBLE NOT NULL,\"gross_error_x\" DOUBLE NOT NULL,\"gross_error_z\" DOUBLE NOT NULL,\"minimal_detectable_bias_y\" DOUBLE NOT NULL,\"minimal_detectable_bias_x\" DOUBLE NOT NULL,\"minimal_detectable_bias_z\" DOUBLE NOT NULL,\"influence_on_position_y\" DOUBLE NOT NULL,\"influence_on_position_x\" DOUBLE NOT NULL,\"influence_on_position_z\" DOUBLE NOT NULL,\"influence_on_network_distortion\" DOUBLE NOT NULL,\"first_principal_component_y\" DOUBLE NOT NULL,\"first_principal_component_x\" DOUBLE NOT NULL,\"first_principal_component_z\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL,\"covar_index\" INTEGER NOT NULL, CONSTRAINT \"PointOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"PointApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		sqls.put(20180106.0206, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"DeflectionAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"dy\" DOUBLE NOT NULL,\"dx\" DOUBLE NOT NULL,\"sigma_dy0\" DOUBLE NOT NULL,\"sigma_dx0\" DOUBLE NOT NULL,\"sigma_dy\" DOUBLE NOT NULL,\"sigma_dx\" DOUBLE NOT NULL,\"confidence_major_axis\" DOUBLE NOT NULL,\"confidence_minor_axis\" DOUBLE NOT NULL,\"redundancy_dy\" DOUBLE NOT NULL,\"redundancy_dx\" DOUBLE NOT NULL,\"gross_error_dy\" DOUBLE NOT NULL,\"gross_error_dx\" DOUBLE NOT NULL,\"minimal_detectable_bias_dy\" DOUBLE NOT NULL,\"minimal_detectable_bias_dx\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL,\"covar_index\" INTEGER NOT NULL, CONSTRAINT \"DeflectionOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"PointApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		
		sqls.put(20180106.0301, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"CongruenceAnalysisGroup\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"name\" VARCHAR(256) NOT NULL,\"dimension\" TINYINT NOT NULL,\"enable\" BOOLEAN NOT NULL);\r\n");
		sqls.put(20180106.0302, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"CongruenceAnalysisPointPairApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"start_point_name\" VARCHAR(256) NOT NULL,\"end_point_name\" VARCHAR(256) NOT NULL,\"enable\" BOOLEAN NOT NULL, CONSTRAINT \"CongruenceAnalysisPointPairAprioriOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"CongruenceAnalysisGroup\"(\"id\") ON DELETE CASCADE, CONSTRAINT \"CongruenceAnalysisUniquePointNamesPerGroup\" UNIQUE (\"group_id\",\"start_point_name\",\"end_point_name\"));\r\n");
		sqls.put(20180106.0303, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"CongruenceAnalysisPointPairAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"y\" DOUBLE NOT NULL,\"x\" DOUBLE NOT NULL,\"z\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"sigma_z\" DOUBLE NOT NULL,\"confidence_major_axis\" DOUBLE NOT NULL,\"confidence_middle_axis\" DOUBLE NOT NULL,\"confidence_minor_axis\" DOUBLE NOT NULL,\"confidence_alpha\" DOUBLE NOT NULL,\"confidence_beta\" DOUBLE NOT NULL,\"confidence_gamma\" DOUBLE NOT NULL,\"confidence_major_axis_2d\" DOUBLE NOT NULL,\"confidence_minor_axis_2d\" DOUBLE NOT NULL,\"confidence_alpha_2d\" DOUBLE NOT NULL,\"gross_error_y\" DOUBLE NOT NULL,\"gross_error_x\" DOUBLE NOT NULL,\"gross_error_z\" DOUBLE NOT NULL,\"minimal_detectable_bias_y\" DOUBLE NOT NULL,\"minimal_detectable_bias_x\" DOUBLE NOT NULL,\"minimal_detectable_bias_z\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL, CONSTRAINT \"CongruenceAnalysisPointPairOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"CongruenceAnalysisPointPairApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		sqls.put(20180106.0304, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"CongruenceAnalysisStrainParameterRestriction\"(\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"enable\" BOOLEAN NOT NULL,PRIMARY KEY(\"group_id\",\"type\"), CONSTRAINT \"CongruenceAnalysisStrainParameterRestrictionOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"CongruenceAnalysisGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20180106.0305, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"CongruenceAnalysisStrainParameterAposteriori\"(\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"value\" DOUBLE NOT NULL,\"sigma\" DOUBLE NOT NULL,\"confidence\" DOUBLE NOT NULL,\"gross_error\" DOUBLE NOT NULL,\"minimal_detectable_bias\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL,PRIMARY KEY(\"group_id\",\"type\"), CONSTRAINT \"CongruenceAnalysisStrainParameterAposterioriOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"CongruenceAnalysisGroup\"(\"id\") ON DELETE CASCADE);\r\n");
		
		sqls.put(20180106.0401, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"VarianceComponent\"(\"type\" SMALLINT NOT NULL PRIMARY KEY,\"redundancy\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"sigma2apost\" DOUBLE NOT NULL,\"number_of_observations\" INTEGER NOT NULL);\r\n");
		sqls.put(20180106.0402, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PrincipalComponent\"(\"index\" INTEGER NOT NULL PRIMARY KEY,\"value\" DOUBLE NOT NULL,\"ratio\" DOUBLE NOT NULL);\r\n");
		sqls.put(20180106.0403, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"RankDefect\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"user_defined\" BOOLEAN DEFAULT FALSE NOT NULL,\"ty\" BOOLEAN DEFAULT FALSE NOT NULL,\"tx\" BOOLEAN DEFAULT FALSE NOT NULL,\"tz\" BOOLEAN DEFAULT FALSE NOT NULL,\"ry\" BOOLEAN DEFAULT FALSE NOT NULL,\"rx\" BOOLEAN DEFAULT FALSE NOT NULL,\"rz\" BOOLEAN DEFAULT FALSE NOT NULL,\"sy\" BOOLEAN DEFAULT FALSE NOT NULL,\"sx\" BOOLEAN DEFAULT FALSE NOT NULL,\"sz\" BOOLEAN DEFAULT FALSE NOT NULL,\"my\" BOOLEAN DEFAULT FALSE NOT NULL,\"mx\" BOOLEAN DEFAULT FALSE NOT NULL,\"mz\" BOOLEAN DEFAULT FALSE NOT NULL,\"mxy\" BOOLEAN DEFAULT FALSE NOT NULL,\"mxyz\" BOOLEAN DEFAULT FALSE NOT NULL);\r\n");
		sqls.put(20180106.0404, "INSERT INTO \"RankDefect\" (\"id\") VALUES (1);\r\n");
		
		sqls.put(20180106.0501, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"TestStatistic\"(\"d1\" DOUBLE NOT NULL,\"d2\" DOUBLE NOT NULL,\"probability_value\" DOUBLE NOT NULL,\"power_of_test\" DOUBLE NOT NULL,\"quantile\" DOUBLE NOT NULL,\"non_centrality_parameter\" DOUBLE NOT NULL,\"p_value\" DOUBLE NOT NULL,PRIMARY KEY(\"d1\",\"d2\",\"probability_value\",\"power_of_test\"));\r\n");
		sqls.put(20180106.0502, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"TestStatisticDefinition\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"type\" SMALLINT DEFAULT " + TestStatisticType.BAARDA_METHOD.getId() + " NOT NULL, \"probability_value\" DOUBLE DEFAULT " + DefaultValue.getProbabilityValue() + " NOT NULL,\"power_of_test\" DOUBLE DEFAULT " + DefaultValue.getPowerOfTest() + " NOT NULL,\"familywise_error_rate\" BOOLEAN DEFAULT FALSE NOT NULL);\r\n");
		sqls.put(20180106.0503, "INSERT INTO \"TestStatisticDefinition\" (\"id\") VALUES (1);\r\n");

		sqls.put(20180106.0504, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ProjectionDefinition\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"type\" SMALLINT DEFAULT " + ProjectionType.NONE.getId() + " NOT NULL,\"reference_height\" DOUBLE DEFAULT 0 NOT NULL);\r\n");
		sqls.put(20180106.0505, "INSERT INTO \"ProjectionDefinition\" (\"id\") VALUES (1);\r\n");
		
		sqls.put(20180106.0506, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"AdjustmentDefinition\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"type\" SMALLINT DEFAULT " + EstimationType.L2NORM.getId() + " NOT NULL, \"number_of_iterations\" INTEGER DEFAULT 50 NOT NULL, \"robust_estimation_limit\" DOUBLE DEFAULT " + DefaultValue.getRobustEstimationLimit() + " NOT NULL, \"number_of_principal_components\" INTEGER DEFAULT 1 NOT NULL, \"estimate_direction_set_orientation_approximation\" BOOLEAN DEFAULT TRUE NOT NULL, \"congruence_analysis\" BOOLEAN DEFAULT FALSE NOT NULL, \"export_covariance_matrix\" BOOLEAN DEFAULT FALSE NOT NULL);\r\n");
		sqls.put(20180106.0507, "INSERT INTO \"AdjustmentDefinition\" (\"id\") VALUES (1);\r\n");
		
		sqls.put(20180106.0508, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ProjectMetadata\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"name\" VARCHAR(256) DEFAULT '' NOT NULL, \"operator\" VARCHAR(256) DEFAULT '' NOT NULL, \"description\" VARCHAR(10000) DEFAULT '' NOT NULL, \"date\" TIMESTAMP(6) DEFAULT NOW() NOT NULL, \"customer_id\" VARCHAR(256) DEFAULT '' NOT NULL, \"project_id\" VARCHAR(256) DEFAULT '' NOT NULL);\r\n");
		sqls.put(20180106.0509, "INSERT INTO \"ProjectMetadata\" (\"id\", \"operator\") VALUES (1, '" + (System.getProperty("user.name") == null ? "" : System.getProperty("user.name").replaceAll("'", "")) + "');\r\n");
		
		sqls.put(20180106.0510, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"AverageThreshold\"(\"type\" SMALLINT NOT NULL PRIMARY KEY,\"value\" DOUBLE);\r\n");
		sqls.put(20180106.0511, "INSERT INTO \"AverageThreshold\" (\"type\", \"value\") VALUES (" + ObservationType.LEVELING.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.LEVELING) + "'), (" + ObservationType.DIRECTION.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.DIRECTION) + "'), (" + ObservationType.HORIZONTAL_DISTANCE.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.HORIZONTAL_DISTANCE) + "'), (" + ObservationType.SLOPE_DISTANCE.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.SLOPE_DISTANCE) + "'), (" + ObservationType.ZENITH_ANGLE.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.ZENITH_ANGLE) + "'), (" + ObservationType.GNSS1D.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.GNSS1D) + "'), (" + ObservationType.GNSS2D.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.GNSS2D) + "'), (" + ObservationType.GNSS3D.getId() + ", '" + DefaultAverageThreshold.getThreshold(ObservationType.GNSS3D) + "');\r\n");

		// Formatters
		sqls.put(20180106.1001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"FormatterOption\" (\"type\" SMALLINT NOT NULL PRIMARY KEY, \"unit\" SMALLINT NULL, \"digits\" TINYINT NOT NULL);\r\n");
			
		// Generate indices
		sqls.put(20180106.5001, "CREATE INDEX \"IndexPointAprioriGroupId\" ON \"PointApriori\"(\"group_id\");\r\n");
		sqls.put(20180106.5002, "CREATE INDEX \"IndexPointAprioriName\" ON \"PointApriori\"(\"name\");\r\n");
		sqls.put(20180106.5003, "CREATE INDEX \"IndexPointGroupUncertaintyGroupId\" ON \"PointGroupUncertainty\"(\"group_id\");\r\n");
		
		sqls.put(20180106.5011, "CREATE INDEX \"IndexObservationGroupUncertaintyGroupId\" ON \"ObservationGroupUncertainty\"(\"group_id\");\r\n");
		sqls.put(20180106.5012, "CREATE INDEX \"IndexObservationGroupId\" ON \"ObservationApriori\"(\"group_id\");\r\n");
		sqls.put(20180106.5013, "CREATE INDEX \"IndexGNSSObservationGroupId\" ON \"GNSSObservationApriori\"(\"group_id\");\r\n");
		sqls.put(20180106.5014, "CREATE INDEX \"IndexObservationAprioriStartPointName\" ON \"ObservationApriori\"(\"start_point_name\");\r\n");
		sqls.put(20180106.5015, "CREATE INDEX \"IndexObservationAprioriEndPointName\" ON \"ObservationApriori\"(\"end_point_name\");\r\n");
		sqls.put(20180106.5016, "CREATE INDEX \"IndexGNSSObservationAprioriStartPointName\" ON \"GNSSObservationApriori\"(\"start_point_name\");\r\n");
		sqls.put(20180106.5017, "CREATE INDEX \"IndexGNSSObservationAprioriEndPointName\" ON \"GNSSObservationApriori\"(\"end_point_name\");\r\n");
		
		sqls.put(20180106.5021, "CREATE INDEX \"IndexAdditionalParameterAprioriGroupId\" ON \"AdditionalParameterApriori\"(\"group_id\");\r\n");
		sqls.put(20180107.0000, "CREATE INDEX \"IndexCongruenceAnalysisPointPairAprioriGroupId\" ON \"CongruenceAnalysisPointPairApriori\"(\"group_id\");\r\n");
		
		// Layer properties
		sqls.put(20180219.0601, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"Layer\" (\"type\" SMALLINT NOT NULL PRIMARY KEY, \"red\" DOUBLE DEFAULT 0.5 NOT NULL, \"green\" DOUBLE DEFAULT 0.5 NOT NULL, \"blue\" DOUBLE DEFAULT 0.5 NOT NULL, \"symbol_size\" DOUBLE DEFAULT " + SymbolBuilder.DEFAULT_SIZE + " NOT NULL, \"line_width\" DOUBLE DEFAULT 1.0 NOT NULL, \"order\" INTEGER DEFAULT 0 NOT NULL, \"visible\" BOOLEAN DEFAULT TRUE NOT NULL);\r\n");
		sqls.put(20180219.0602, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"LayerExtent\" (\"id\" INTEGER NOT NULL PRIMARY KEY, \"min_x\" DOUBLE NOT NULL, \"min_y\" DOUBLE NOT NULL, \"max_x\" DOUBLE NOT NULL, \"max_y\" DOUBLE NOT NULL);\r\n");
		sqls.put(20180219.0603, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"LayerEllipseScale\" (\"id\" INTEGER NOT NULL PRIMARY KEY, \"value\" DOUBLE NOT NULL);\r\n");
		sqls.put(20180219.0604, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"LayerFont\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"family\" VARCHAR(256) DEFAULT 'SYSTEM' NOT NULL, \"size\" DOUBLE DEFAULT 12.0 NOT NULL, \"red\" DOUBLE DEFAULT 0 NOT NULL, \"green\" DOUBLE DEFAULT 0 NOT NULL, \"blue\" DOUBLE DEFAULT 0 NOT NULL, CONSTRAINT \"LayerFontOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		sqls.put(20180219.0605, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"PointLayerProperty\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"type\" SMALLINT NOT NULL, \"point_1d_visible\" BOOLEAN DEFAULT FALSE NOT NULL, \"point_2d_visible\" BOOLEAN DEFAULT TRUE NOT NULL, \"point_3d_visible\" BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT \"LayerSymbolOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		sqls.put(20180219.0606, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ObservationLayerProperty\" (\"layer\" SMALLINT NOT NULL, \"observation_type\" SMALLINT NOT NULL, \"red\" DOUBLE DEFAULT 0 NOT NULL, \"green\" DOUBLE DEFAULT 0 NOT NULL, \"blue\" DOUBLE DEFAULT 0 NOT NULL, \"visible\" BOOLEAN DEFAULT TRUE NOT NULL, PRIMARY KEY(\"layer\",\"observation_type\"), CONSTRAINT \"LayerObservationColorOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		sqls.put(20180219.0607, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ArrowLayerProperty\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"type\" SMALLINT NOT NULL, CONSTRAINT \"ArrowLayerPropertyOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		sqls.put(20180219.0608, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ConfidenceLayerProperty\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"red\" DOUBLE DEFAULT 0.5 NOT NULL, \"green\" DOUBLE DEFAULT 0.5 NOT NULL, \"blue\" DOUBLE DEFAULT 0.5 NOT NULL, CONSTRAINT \"ConfidenceLayerPropertyOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		
		sqls.put(20180311.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"HighlightLayerProperty\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"red\" DOUBLE DEFAULT 0.5 NOT NULL, \"green\" DOUBLE DEFAULT 0.5 NOT NULL, \"blue\" DOUBLE DEFAULT 0.5 NOT NULL, \"line_width\" DOUBLE DEFAULT 2.5 NOT NULL, CONSTRAINT \"HighlightLayerPropertyOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		
		// add residual columns to aposteriori tables
		sqls.put(20180410.0001, "ALTER TABLE \"ObservationAposteriori\" ADD \"residual\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		
		sqls.put(20180410.0011, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"residual_x\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		sqls.put(20180410.0012, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"residual_y\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		sqls.put(20180410.0013, "ALTER TABLE \"GNSSObservationAposteriori\" ADD \"residual_z\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		
		sqls.put(20180410.0021, "ALTER TABLE \"PointAposteriori\" ADD \"residual_x\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		sqls.put(20180410.0022, "ALTER TABLE \"PointAposteriori\" ADD \"residual_y\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		sqls.put(20180410.0023, "ALTER TABLE \"PointAposteriori\" ADD \"residual_z\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		
		sqls.put(20180410.0031, "ALTER TABLE \"DeflectionAposteriori\" ADD \"residual_dx\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		sqls.put(20180410.0032, "ALTER TABLE \"DeflectionAposteriori\" ADD \"residual_dy\" DOUBLE DEFAULT 0 NOT NULL\r\n");
		
		// add table row highlighting
		sqls.put(20180411.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"TableRowHighlightRange\" (\"type\" SMALLINT NOT NULL PRIMARY KEY, \"left_boundary\" DOUBLE NOT NULL, \"right_boundary\" DOUBLE NOT NULL);\r\n");
		sqls.put(20180411.0002, "INSERT INTO \"TableRowHighlightRange\" (\"type\", \"left_boundary\", \"right_boundary\") VALUES (" + TableRowHighlightType.REDUNDANCY.getId() + ", '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.REDUNDANCY)[0] + "', '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.REDUNDANCY)[1] + "'), (" + TableRowHighlightType.P_PRIO_VALUE.getId() + ", '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.P_PRIO_VALUE)[0] + "', '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.P_PRIO_VALUE)[1] + "'), (" + TableRowHighlightType.INFLUENCE_ON_POSITION.getId() + ", '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.INFLUENCE_ON_POSITION)[0] + "', '" + DefaultTableRowHighlightValue.getRange(TableRowHighlightType.INFLUENCE_ON_POSITION)[1] + "');\r\n");

		sqls.put(20180411.0011, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"TableRowHighlightProperty\" (\"type\" SMALLINT NOT NULL PRIMARY KEY, \"red\" DOUBLE DEFAULT 0.5 NOT NULL, \"green\" DOUBLE DEFAULT 0.5 NOT NULL, \"blue\" DOUBLE DEFAULT 0.5 NOT NULL);\r\n");
		sqls.put(20180411.0012, "INSERT INTO \"TableRowHighlightProperty\" (\"type\", \"red\", \"green\", \"blue\") VALUES (" + TableRowHighlightRangeType.EXCELLENT.getId() + ", '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.EXCELLENT).getRed() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.EXCELLENT).getGreen() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.EXCELLENT).getBlue() + "'), (" + TableRowHighlightRangeType.SATISFACTORY.getId() + ", '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.SATISFACTORY).getRed() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.SATISFACTORY).getGreen() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.SATISFACTORY).getBlue() + "'), (" + TableRowHighlightRangeType.INADEQUATE.getId() + ", '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.INADEQUATE).getRed() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.INADEQUATE).getGreen() + "', '" + DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.INADEQUATE).getBlue() + "');\r\n");

		sqls.put(20180411.0021, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"TableRowHighlightScheme\" (\"id\" INTEGER NOT NULL PRIMARY KEY, \"type\" SMALLINT DEFAULT " + TableRowHighlightType.NONE.getId() + " NOT NULL);\r\n");
		sqls.put(20180411.0022, "INSERT INTO \"TableRowHighlightScheme\" (\"id\") VALUES (1);\r\n");

		// add variance of unit weight
		sqls.put(20180430.0001, "ALTER TABLE \"AdjustmentDefinition\" ADD \"apply_variance_of_unit_weight\" BOOLEAN DEFAULT TRUE NOT NULL\r\n");
		
		// add order id of items
		sqls.put(20190225.0001, "ALTER TABLE \"ObservationGroup\" ADD \"order\" INTEGER DEFAULT 0 NOT NULL\r\n");
		sqls.put(20190225.0002, "UPDATE \"ObservationGroup\" SET \"order\" = \"id\";\r\n");
		
		sqls.put(20190225.0011, "ALTER TABLE \"PointGroup\" ADD \"order\" INTEGER DEFAULT 0 NOT NULL\r\n");
		sqls.put(20190225.0012, "UPDATE \"PointGroup\" SET \"order\" = \"id\";\r\n");
		
		sqls.put(20190225.0021, "ALTER TABLE \"CongruenceAnalysisGroup\" ADD \"order\" INTEGER DEFAULT 0 NOT NULL\r\n");
		sqls.put(20190225.0022, "UPDATE \"CongruenceAnalysisGroup\" SET \"order\" = \"id\";\r\n");
		
		// add legend layer
		sqls.put(20190309.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"LegendLayerProperty\" (\"layer\" SMALLINT NOT NULL PRIMARY KEY, \"type\" SMALLINT NOT NULL, CONSTRAINT \"LegendLayerPropertyOnLayerDelete\" FOREIGN KEY(\"layer\") REFERENCES \"Layer\"(\"type\") ON DELETE CASCADE);\r\n");
		
		// change and add projection/reduction tables
		sqls.put(20200119.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ReductionDefinition\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"projection_type\" SMALLINT DEFAULT " + ProjectionType.NONE.getId() + " NOT NULL, \"reference_height\" DOUBLE DEFAULT 0 NOT NULL, \"earth_radius\" DOUBLE DEFAULT " + Constant.EARTH_RADIUS + " NOT NULL);\r\n");
		sqls.put(20200119.0002, "INSERT INTO \"ReductionDefinition\" (\"id\", \"projection_type\", \"reference_height\", \"earth_radius\") (SELECT \"id\", CASEWHEN(\"type\" IN (4, 24, 34, 234), " + ProjectionType.GAUSS_KRUEGER.getId() + ", CASEWHEN(\"type\" IN (5, 25, 35, 235), " + ProjectionType.UTM.getId() + ", " + ProjectionType.NONE.getId() + ")) AS \"projection_type\", \"reference_height\", " + Constant.EARTH_RADIUS + " AS \"earth_radius\" FROM \"ProjectionDefinition\" WHERE \"id\" = 1 LIMIT 1);\r\n");
		
		sqls.put(20200119.0011, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ReductionTask\"(\"reduction_id\" INTEGER NOT NULL, \"type\" SMALLINT NOT NULL, PRIMARY KEY(\"reduction_id\", \"type\"), CONSTRAINT \"ReductionDefinitionOnGroupDelete\" FOREIGN KEY(\"reduction_id\") REFERENCES \"ReductionDefinition\"(\"id\") ON DELETE CASCADE);\r\n");
		sqls.put(20200119.0012, "INSERT INTO \"ReductionTask\" (\"reduction_id\", \"type\") SELECT \"id\", CASEWHEN(\"type\" IN (4, 24, 34, 234, 5, 25, 35, 235), " + ReductionTaskType.DISTANCE.getId()  + ", -1) FROM \"ProjectionDefinition\" WHERE \"id\" = 1 LIMIT 1;\r\n");
		sqls.put(20200119.0013, "INSERT INTO \"ReductionTask\" (\"reduction_id\", \"type\") SELECT \"id\", CASEWHEN(\"type\" IN (23, 34, 234, 35, 235),           " + ReductionTaskType.HEIGHT.getId()    + ", -2) FROM \"ProjectionDefinition\" WHERE \"id\" = 1 LIMIT 1;\r\n");
		sqls.put(20200119.0014, "INSERT INTO \"ReductionTask\" (\"reduction_id\", \"type\") SELECT \"id\", CASEWHEN(\"type\" IN (23, 24, 234, 25, 235),           " + ReductionTaskType.DIRECTION.getId() + ", -3) FROM \"ProjectionDefinition\" WHERE \"id\" = 1 LIMIT 1;\r\n");
		sqls.put(20200119.0015, "DELETE FROM \"ReductionTask\" WHERE \"type\" < 0;\r\n");
		
		sqls.put(20200119.0020, "DROP TABLE \"ProjectionDefinition\"\r\n");
		
		sqls.put(20200124.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"UnscentedTransformation\"(\"id\" INTEGER NOT NULL PRIMARY KEY, \"scaling\" DOUBLE DEFAULT " + UnscentedTransformationParameter.getAlpha() + " NOT NULL, \"damping\" DOUBLE DEFAULT " + UnscentedTransformationParameter.getBeta() + " NOT NULL, \"weight_zero\" DOUBLE DEFAULT " + UnscentedTransformationParameter.getWeightZero() + " NOT NULL);\r\n");
		sqls.put(20200124.0002, "INSERT INTO \"UnscentedTransformation\" (\"id\") VALUES (1);\r\n");
		
		sqls.put(20200327.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"ImportSeparation\" (\"id\" INTEGER NOT NULL PRIMARY KEY, \"separate\" BOOLEAN DEFAULT FALSE NOT NULL);\r\n");
		sqls.put(20200327.0002, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.LEVELING.getId() + ", "            + ImportOption.getInstance().isGroupSeparation(ObservationType.LEVELING)+ ");\r\n");
		sqls.put(20200327.0003, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.DIRECTION.getId() + ", "           + ImportOption.getInstance().isGroupSeparation(ObservationType.DIRECTION)+ ");\r\n");
		sqls.put(20200327.0004, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.HORIZONTAL_DISTANCE.getId() + ", " + ImportOption.getInstance().isGroupSeparation(ObservationType.HORIZONTAL_DISTANCE)+ ");\r\n");
		sqls.put(20200327.0005, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.SLOPE_DISTANCE.getId() + ", "      + ImportOption.getInstance().isGroupSeparation(ObservationType.SLOPE_DISTANCE)+ ");\r\n");
		sqls.put(20200327.0006, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.ZENITH_ANGLE.getId() + ", "        + ImportOption.getInstance().isGroupSeparation(ObservationType.ZENITH_ANGLE)+ ");\r\n");
		sqls.put(20200327.0007, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.GNSS1D.getId() + ", "              + ImportOption.getInstance().isGroupSeparation(ObservationType.GNSS1D)+ ");\r\n");
		sqls.put(20200327.0008, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.GNSS2D.getId() + ", "              + ImportOption.getInstance().isGroupSeparation(ObservationType.GNSS2D)+ ");\r\n");
		sqls.put(20200327.0009, "INSERT INTO \"ImportSeparation\" (\"id\", \"separate\") VALUES (" + ObservationType.GNSS3D.getId() + ", "              + ImportOption.getInstance().isGroupSeparation(ObservationType.GNSS3D)+ ");\r\n");

		// deflection tables
		sqls.put(20201220.0001, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"VerticalDeflectionGroup\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"name\" VARCHAR(256) NOT NULL,\"type\" SMALLINT NOT NULL,\"enable\" BOOLEAN NOT NULL,\"order\" INTEGER DEFAULT 0 NOT NULL);\r\n");
		sqls.put(20201220.0002, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"VerticalDeflectionGroupUncertainty\"(\"group_id\" INTEGER NOT NULL,\"type\" SMALLINT NOT NULL,\"value\" DOUBLE NOT NULL, PRIMARY KEY(\"group_id\",\"type\"), CONSTRAINT \"VerticalDeflectionUncertaintyOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"VerticalDeflectionGroup\"(\"id\") ON DELETE CASCADE);\r\n");

		sqls.put(20201220.0003, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"VerticalDeflectionApriori\"(\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"name\" VARCHAR(256) NOT NULL,\"y0\" DOUBLE DEFAULT 0 NOT NULL,\"x0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_y0\" DOUBLE DEFAULT 0 NOT NULL,\"sigma_x0\" DOUBLE DEFAULT 0 NOT NULL,\"enable\" BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT \"VerticalDeflectionOnGroupDelete\" FOREIGN KEY(\"group_id\") REFERENCES \"VerticalDeflectionGroup\"(\"id\") ON DELETE CASCADE, CONSTRAINT \"UniqueVerticalDeflectionName\" UNIQUE (\"name\"));\r\n");
		sqls.put(20201220.0004, "CREATE " + TABLE_STORAGE_TYPE + " TABLE \"VerticalDeflectionAposteriori\"(\"id\" INTEGER NOT NULL PRIMARY KEY,\"y\" DOUBLE NOT NULL,\"x\" DOUBLE NOT NULL,\"sigma_y0\" DOUBLE NOT NULL,\"sigma_x0\" DOUBLE NOT NULL,\"sigma_y\" DOUBLE NOT NULL,\"sigma_x\" DOUBLE NOT NULL,\"residual_y\" DOUBLE DEFAULT 0 NOT NULL,\"residual_x\" DOUBLE DEFAULT 0 NOT NULL,\"confidence_major_axis\" DOUBLE NOT NULL,\"confidence_minor_axis\" DOUBLE NOT NULL,\"redundancy_y\" DOUBLE NOT NULL,\"redundancy_x\" DOUBLE NOT NULL,\"gross_error_y\" DOUBLE NOT NULL,\"gross_error_x\" DOUBLE NOT NULL,\"minimal_detectable_bias_y\" DOUBLE NOT NULL,\"minimal_detectable_bias_x\" DOUBLE NOT NULL,\"omega\" DOUBLE NOT NULL,\"p_prio\" DOUBLE NOT NULL,\"p_post\" DOUBLE NOT NULL,\"t_prio\" DOUBLE NOT NULL,\"t_post\" DOUBLE NOT NULL,\"significant\" BOOLEAN NOT NULL,\"covar_index\" INTEGER NOT NULL, CONSTRAINT \"VerticalDeflectionOnDelete\" FOREIGN KEY(\"id\") REFERENCES \"VerticalDeflectionApriori\"(\"id\") ON DELETE CASCADE);\r\n");

		// transfer deflection values to new tables
		sqls.put(20201220.0010, "INSERT INTO \"VerticalDeflectionGroup\" (\"id\", \"name\", \"type\", \"enable\", \"order\") SELECT \"id\", \"name\", " + VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION.getId()  + " AS \"type\", \"enable\", \"order\" FROM \"PointGroup\" WHERE \"type\" = " + PointType.REFERENCE_POINT.getId()  + " and \"dimension\" = 3 and \"consider_deflection\" = TRUE\r\n");
		sqls.put(20201220.0011, "INSERT INTO \"VerticalDeflectionGroup\" (\"id\", \"name\", \"type\", \"enable\", \"order\") SELECT \"id\", \"name\", " + VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION.getId() + " AS \"type\", \"enable\", \"order\" FROM \"PointGroup\" WHERE \"type\" = " + PointType.STOCHASTIC_POINT.getId() + " and \"dimension\" = 3 and \"consider_deflection\" = TRUE\r\n");
		sqls.put(20201220.0012, "INSERT INTO \"VerticalDeflectionGroup\" (\"id\", \"name\", \"type\", \"enable\", \"order\") SELECT \"id\", \"name\", " + VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION.getId()        + " AS \"type\", \"enable\", \"order\" FROM \"PointGroup\" WHERE \"type\" IN (" + PointType.NEW_POINT.getId() + "," + PointType.DATUM_POINT.getId() + ") and \"dimension\" = 3 and \"consider_deflection\" = TRUE\r\n");
		
		sqls.put(20201220.0013, "INSERT INTO \"VerticalDeflectionGroupUncertainty\" (\"group_id\", \"type\", \"value\") SELECT \"group_id\", " + VerticalDeflectionGroupUncertaintyType.DEFLECTION_X.getId() + " AS \"type\", \"value\" FROM \"PointGroupUncertainty\" JOIN \"PointGroup\" ON \"PointGroupUncertainty\".\"group_id\" = \"PointGroup\".\"id\" AND \"dimension\" = 3 AND \"consider_deflection\" = TRUE WHERE \"PointGroupUncertainty\".\"type\" = " + VerticalDeflectionGroupUncertaintyType.DEFLECTION_X.getId() + "\r\n");
		sqls.put(20201220.0014, "INSERT INTO \"VerticalDeflectionGroupUncertainty\" (\"group_id\", \"type\", \"value\") SELECT \"group_id\", " + VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y.getId() + " AS \"type\", \"value\" FROM \"PointGroupUncertainty\" JOIN \"PointGroup\" ON \"PointGroupUncertainty\".\"group_id\" = \"PointGroup\".\"id\" AND \"dimension\" = 3 AND \"consider_deflection\" = TRUE WHERE \"PointGroupUncertainty\".\"type\" = " + VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y.getId() + "\r\n");
		
		sqls.put(20201220.0015, "INSERT INTO \"VerticalDeflectionApriori\" (\"name\", \"group_id\", \"y0\", \"x0\", \"sigma_y0\", \"sigma_x0\", \"enable\")  SELECT \"name\", \"group_id\", \"dy0\", \"dx0\", \"sigma_dy0\", \"sigma_dx0\", \"enable\" FROM \"PointApriori\" JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" WHERE \"dimension\" = 3 AND \"consider_deflection\" = TRUE\r\n");	

		// remove old tables and entries
		sqls.put(20201220.0020, "DELETE FROM \"PointGroupUncertainty\" WHERE \"type\" IN (" + VerticalDeflectionGroupUncertaintyType.DEFLECTION_X.getId() + ", " + VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y.getId() + ")\r\n");
		sqls.put(20200119.0021, "DROP TABLE \"DeflectionAposteriori\"\r\n");
		sqls.put(20201220.0022, "ALTER TABLE \"PointApriori\" DROP COLUMN \"dy0\"\r\n");
		sqls.put(20201220.0023, "ALTER TABLE \"PointApriori\" DROP COLUMN \"dx0\"\r\n");
		sqls.put(20201220.0024, "ALTER TABLE \"PointApriori\" DROP COLUMN \"sigma_dy0\"\r\n");
		sqls.put(20201220.0025, "ALTER TABLE \"PointApriori\" DROP COLUMN \"sigma_dx0\"\r\n");
		sqls.put(20201220.0026, "ALTER TABLE \"PointGroup\" DROP COLUMN \"consider_deflection\"\r\n");
		
		
		return sqls;
	}

	public boolean hasDatabase() {
		return this.dataBase != null;
	}

	public void closeDataBase() {
		if (this.dataBase != null && this.dataBase.isOpen()) {
			this.fireDatabaseStateChanged(ProjectDatabaseStateType.CLOSING);
			this.dataBase.close();
			this.dataBase = null;
			this.fireDatabaseStateChanged(ProjectDatabaseStateType.CLOSED);
		}
	}
	
	private void initFormatterOptions() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "SELECT \"unit\", \"digits\" FROM \"FormatterOption\" WHERE \"type\" = ?";
		
		Map<CellValueType, FormatterOption> options = FormatterOptions.getInstance().getFormatterOptions();
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		
		for (FormatterOption option : options.values()) {
			CellValueType type = option.getType();
			int idx = 1;
			stmt.setInt(idx++, type.getId());
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Unit unit = null;
				int digits = rs.getInt("digits");
				UnitType unitType = UnitType.getEnumByValue(rs.getInt("unit"));
				if (!rs.wasNull() && unitType != null) {
					unit = LengthUnit.getUnit(unitType);
					if (unit == null)
						unit = AngleUnit.getUnit(unitType);
					if (unit == null)
						unit = ScaleUnit.getUnit(unitType);
				}

				if (digits >= 0)
					option.setFractionDigits(digits);
				if (unit != null)
					option.setUnit(unit);
			}
			else {
				this.saveFormatterOption(option);
			}
		}
	}
	
	private void loadMetaData(MetaData metaData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;

		String sql = "SELECT \"name\", \"operator\", \"description\", \"date\", \"customer_id\", \"project_id\" "
				+ "FROM \"ProjectMetadata\" "
				+ "WHERE \"id\" = 1 LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			metaData.setName(rs.getString("name"));
			metaData.setOperator(rs.getString("operator"));
			metaData.setCustomerId(rs.getString("customer_id"));
			metaData.setProjectId(rs.getString("project_id"));
			metaData.setDescription(rs.getString("description"));
			metaData.setDate(rs.getTimestamp("date").toLocalDateTime().toLocalDate());
		}
	}
	
	private void loadObservationGroups() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		String sql = "SELECT \"id\", \"name\", \"enable\" "
				+ "FROM \"ObservationGroup\" "
				+ "WHERE \"type\" = ? "
				+ "ORDER BY \"order\" ASC, \"id\" ASC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		TreeItemType[] types = new TreeItemType[] {
				TreeItemType.LEVELING_DIRECTORY,
				TreeItemType.DIRECTION_DIRECTORY,
				TreeItemType.HORIZONTAL_DISTANCE_DIRECTORY,
				TreeItemType.SLOPE_DISTANCE_DIRECTORY,
				TreeItemType.ZENITH_ANGLE_DIRECTORY,
		};

		for (TreeItemType parentType : types) {
			ObservationType obsType = TreeItemType.getObservationTypeByTreeItemType(parentType);
			if (obsType == null) {
				System.err.println(this.getClass().getSimpleName() + " : Error, cannot convert tree item type to observation type " + parentType);
				continue;
			}

			stmt.setInt(1, obsType.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id         = rs.getInt("id");
				boolean enable = rs.getBoolean("enable");
				String name    = rs.getString("name");

				treeBuilder.addItem(parentType, id, name, enable, false);
			}
		}
	}

	private void loadGNSSObservationGroups() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		String sql = "SELECT \"id\", \"name\", \"enable\" "
				+ "FROM \"ObservationGroup\" "
				+ "WHERE \"type\" = ? "
				+ "ORDER BY \"order\" ASC, \"id\" ASC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		TreeItemType[] types = new TreeItemType[] {						
				TreeItemType.GNSS_1D_DIRECTORY,
				TreeItemType.GNSS_2D_DIRECTORY,
				TreeItemType.GNSS_3D_DIRECTORY
		};

		for (TreeItemType parentType : types) {
			ObservationType obsType = TreeItemType.getObservationTypeByTreeItemType(parentType);
			if (obsType == null) {
				System.err.println(this.getClass().getSimpleName() + " : Error, cannot convert tree item type to observation type " + parentType);
				continue;
			}

			stmt.setInt(1, obsType.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id         = rs.getInt("id");
				boolean enable = rs.getBoolean("enable");
				String name    = rs.getString("name");

				treeBuilder.addItem(parentType, id, name, enable, false);
			}
		}
	}

	private void loadPointGroups() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		String sql = "SELECT \"id\", \"name\", \"enable\" "
				+ "FROM \"PointGroup\" "
				+ "WHERE \"type\" = ? AND \"dimension\" = ? "
				+ "ORDER BY \"order\" ASC, \"id\" ASC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		TreeItemType[] types = new TreeItemType[] {
				TreeItemType.REFERENCE_POINT_1D_DIRECTORY,
				TreeItemType.REFERENCE_POINT_2D_DIRECTORY,
				TreeItemType.REFERENCE_POINT_3D_DIRECTORY,

				TreeItemType.STOCHASTIC_POINT_1D_DIRECTORY,
				TreeItemType.STOCHASTIC_POINT_2D_DIRECTORY,
				TreeItemType.STOCHASTIC_POINT_3D_DIRECTORY,

				TreeItemType.DATUM_POINT_1D_DIRECTORY,
				TreeItemType.DATUM_POINT_2D_DIRECTORY,
				TreeItemType.DATUM_POINT_3D_DIRECTORY,

				TreeItemType.NEW_POINT_1D_DIRECTORY,
				TreeItemType.NEW_POINT_2D_DIRECTORY,
				TreeItemType.NEW_POINT_3D_DIRECTORY,
		};

		for (TreeItemType parentType : types) {
			int dimension = -1;
			switch(parentType) {
			case REFERENCE_POINT_1D_DIRECTORY:
			case STOCHASTIC_POINT_1D_DIRECTORY:
			case DATUM_POINT_1D_DIRECTORY:
			case NEW_POINT_1D_DIRECTORY:
				dimension = 1;
				break;
			case REFERENCE_POINT_2D_DIRECTORY:
			case STOCHASTIC_POINT_2D_DIRECTORY:
			case DATUM_POINT_2D_DIRECTORY:
			case NEW_POINT_2D_DIRECTORY:
				dimension = 2;
				break;
			case REFERENCE_POINT_3D_DIRECTORY:
			case STOCHASTIC_POINT_3D_DIRECTORY:
			case DATUM_POINT_3D_DIRECTORY:
			case NEW_POINT_3D_DIRECTORY:
				dimension = 3;
				break;
			default:
				dimension = -1;
				break;
			}


			PointType pointType = TreeItemType.getPointTypeByTreeItemType(parentType);
			if (pointType == null) {
				System.err.println(this.getClass().getSimpleName() + " : Error, cannot convert tree item type to point type " + parentType);
				continue;
			}

			stmt.setInt(1, pointType.getId());
			stmt.setInt(2, dimension);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id         = rs.getInt("id");
				boolean enable = rs.getBoolean("enable");
				String name    = rs.getString("name");

				treeBuilder.addItem(parentType, id, name, enable, false);
			}
		}
	}
	
	private void loadVerticalDeflectioGroups() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		String sql = "SELECT \"id\", \"name\", \"enable\" "
				+ "FROM \"VerticalDeflectionGroup\" "
				+ "WHERE \"type\" = ? "
				+ "ORDER BY \"order\" ASC, \"id\" ASC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		TreeItemType[] types = new TreeItemType[] {
				TreeItemType.REFERENCE_VERTICAL_DEFLECTION_DIRECTORY,
				TreeItemType.STOCHASTIC_VERTICAL_DEFLECTION_DIRECTORY,
				TreeItemType.UNKNOWN_VERTICAL_DEFLECTION_DIRECTORY,
		};

		for (TreeItemType parentType : types) {
			VerticalDeflectionType verticalDeflectionType = TreeItemType.getVerticalDeflectionTypeByTreeItemType(parentType);
			if (verticalDeflectionType == null) {
				System.err.println(this.getClass().getSimpleName() + " : Error, cannot convert tree item type to vertical deflection type " + parentType);
				continue;
			}

			stmt.setInt(1, verticalDeflectionType.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id         = rs.getInt("id");
				boolean enable = rs.getBoolean("enable");
				String name    = rs.getString("name");

				treeBuilder.addItem(parentType, id, name, enable, false);
			}
		}
	}

	private void loadCongruenceAnalysisGroups() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();

		String sql = "SELECT \"id\", \"name\", \"enable\" "
				+ "FROM \"CongruenceAnalysisGroup\" "
				+ "WHERE \"dimension\" = ? "
				+ "ORDER BY \"order\" ASC, \"id\" ASC";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		TreeItemType[] types = new TreeItemType[] {
				TreeItemType.CONGRUENCE_ANALYSIS_1D_DIRECTORY,
				TreeItemType.CONGRUENCE_ANALYSIS_2D_DIRECTORY,
				TreeItemType.CONGRUENCE_ANALYSIS_3D_DIRECTORY
		};

		for (TreeItemType parentType : types) {
			int dimension = -1;
			switch(parentType) {
			case CONGRUENCE_ANALYSIS_1D_DIRECTORY:
				dimension = 1;
				break;
			case CONGRUENCE_ANALYSIS_2D_DIRECTORY:
				dimension = 2;
				break;
			case CONGRUENCE_ANALYSIS_3D_DIRECTORY:
				dimension = 3;
				break;
			default:
				dimension = -1;
				break;
			}

			stmt.setInt(1, dimension);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id         = rs.getInt("id");
				boolean enable = rs.getBoolean("enable");
				String name    = rs.getString("name");

				treeBuilder.addItem(parentType, id, name, enable, false);
			}
		}
	}

	public void loadData(TreeItemValue itemValue, TreeItemValue... selectedTreeItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		TreeItemType treeItemType = itemValue.getItemType();

		switch(treeItemType) {
		case ROOT:
			MetaData metaData = UIMetaDataPaneBuilder.getInstance().getMetaData();
			this.loadMetaData(metaData);
			this.loadTestStatistics();
			this.loadVarianceComponents();
			this.loadPrincipalComponents();
			break;
		case CONGRUENCE_ANALYSIS_1D_LEAF:
		case CONGRUENCE_ANALYSIS_2D_LEAF:
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			if (itemValue instanceof CongruenceAnalysisTreeItemValue) {
				CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue = (CongruenceAnalysisTreeItemValue)itemValue;
				CongruenceAnalysisTreeItemValue[] selectedCongruenceAnalysisItemValuesArray = null;
				Set<CongruenceAnalysisTreeItemValue> selectedCongruenceAnalysisItemValues = new LinkedHashSet<CongruenceAnalysisTreeItemValue>();
				selectedCongruenceAnalysisItemValues.add(congruenceAnalysisTreeItemValue);

				if (selectedTreeItemValues != null) {
					for (TreeItemValue selectedItem : selectedTreeItemValues) {
						if (selectedItem instanceof CongruenceAnalysisTreeItemValue)
							selectedCongruenceAnalysisItemValues.add((CongruenceAnalysisTreeItemValue)selectedItem);
					}
				}
				selectedCongruenceAnalysisItemValuesArray = selectedCongruenceAnalysisItemValues.toArray(new CongruenceAnalysisTreeItemValue[selectedCongruenceAnalysisItemValues.size()]);

				//UICongruenceAnalysisPropertiesPaneBuilder.getInstance().getCongruenceAnalysisPropertiesPane(congruenceAnalysisTreeItemValue.getItemType()).reset();

				this.loadCongruenceAnalysisPointPair(congruenceAnalysisTreeItemValue, selectedCongruenceAnalysisItemValuesArray);
				this.loadStrainParameterRestrictions(congruenceAnalysisTreeItemValue, selectedCongruenceAnalysisItemValuesArray);
				this.loadStrainParameters(congruenceAnalysisTreeItemValue, selectedCongruenceAnalysisItemValuesArray);
			}

			break;

		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_LEAF:

			if (itemValue instanceof PointTreeItemValue) {
				PointTreeItemValue pointItemValue = (PointTreeItemValue)itemValue;
				PointTreeItemValue[] selectedPointItemValuesArray = null;
				Set<PointTreeItemValue> selectedPointItemValues = new LinkedHashSet<PointTreeItemValue>();
				selectedPointItemValues.add(pointItemValue);

				if (selectedTreeItemValues != null) {
					for (TreeItemValue selectedItem : selectedTreeItemValues) {
						if (selectedItem instanceof PointTreeItemValue)
							selectedPointItemValues.add((PointTreeItemValue)selectedItem);
					}
				}
				selectedPointItemValuesArray = selectedPointItemValues.toArray(new PointTreeItemValue[selectedPointItemValues.size()]);
				this.loadPoints(pointItemValue, selectedPointItemValuesArray);

				//UIPointPropertiesPaneBuilder.getInstance().getPointPropertiesPane(pointItemValue.getItemType()).reset();

				switch(treeItemType) {
				case REFERENCE_POINT_3D_LEAF:
				case STOCHASTIC_POINT_1D_LEAF:
				case STOCHASTIC_POINT_2D_LEAF:
				case STOCHASTIC_POINT_3D_LEAF:
				case DATUM_POINT_3D_LEAF:
				case NEW_POINT_3D_LEAF:

					this.loadUncertainties(pointItemValue, selectedPointItemValuesArray);

					break;
				default:
					break;
				}
			}

			break;

		case LEVELING_LEAF:
		case DIRECTION_LEAF:
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
		case ZENITH_ANGLE_LEAF:
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:

			if (itemValue instanceof ObservationTreeItemValue) {
				ObservationTreeItemValue observationItemValue = (ObservationTreeItemValue)itemValue;
				ObservationTreeItemValue[] selectedObservationItemValuesArray = null;
				Set<ObservationTreeItemValue> selectedObservationItemValues = new LinkedHashSet<ObservationTreeItemValue>();
				selectedObservationItemValues.add(observationItemValue);

				if (selectedTreeItemValues != null) {
					for (TreeItemValue selectedItem : selectedTreeItemValues) {
						if (selectedItem instanceof ObservationTreeItemValue)
							selectedObservationItemValues.add((ObservationTreeItemValue)selectedItem);
					}
				}
				selectedObservationItemValuesArray = selectedObservationItemValues.toArray(new ObservationTreeItemValue[selectedObservationItemValues.size()]);

				switch(treeItemType) {

				case LEVELING_LEAF:
				case DIRECTION_LEAF:
				case HORIZONTAL_DISTANCE_LEAF:
				case SLOPE_DISTANCE_LEAF:
				case ZENITH_ANGLE_LEAF:
					this.loadObservations(observationItemValue, selectedObservationItemValuesArray);
					break;
				case GNSS_1D_LEAF:
				case GNSS_2D_LEAF:
				case GNSS_3D_LEAF:
					this.loadGNSSObservations(observationItemValue, selectedObservationItemValuesArray);
					break;
				default:
					break;
				}

				//UIObservationPropertiesPaneBuilder.getInstance().getObservationPropertiesPane(observationItemValue.getItemType()).reset();

				this.loadUncertainties(observationItemValue, selectedObservationItemValuesArray);
				this.loadEpoch(observationItemValue, selectedObservationItemValuesArray);
				this.loadAdditionalParameters(observationItemValue, selectedObservationItemValuesArray);
			}

			break;
			
		case REFERENCE_VERTICAL_DEFLECTION_LEAF:
		case STOCHASTIC_VERTICAL_DEFLECTION_LEAF:
		case UNKNOWN_VERTICAL_DEFLECTION_LEAF:
			if (itemValue instanceof VerticalDeflectionTreeItemValue) {
				VerticalDeflectionTreeItemValue verticalDeflectionTreeItemValue = (VerticalDeflectionTreeItemValue)itemValue;
				VerticalDeflectionTreeItemValue[] selectedVerticalDeflectionItemValuesArray = null;
				Set<VerticalDeflectionTreeItemValue> selectedVerticalDeflectionTreeItemValues = new LinkedHashSet<VerticalDeflectionTreeItemValue>();
				selectedVerticalDeflectionTreeItemValues.add(verticalDeflectionTreeItemValue);

				if (selectedTreeItemValues != null) {
					for (TreeItemValue selectedItem : selectedTreeItemValues) {
						if (selectedItem instanceof VerticalDeflectionTreeItemValue)
							selectedVerticalDeflectionTreeItemValues.add((VerticalDeflectionTreeItemValue)selectedItem);
					}
				}
				selectedVerticalDeflectionItemValuesArray = selectedVerticalDeflectionTreeItemValues.toArray(new VerticalDeflectionTreeItemValue[selectedVerticalDeflectionTreeItemValues.size()]);
				
				this.loadVerticalDeflections(verticalDeflectionTreeItemValue, selectedVerticalDeflectionItemValuesArray);
				this.loadUncertainties(verticalDeflectionTreeItemValue, selectedVerticalDeflectionItemValuesArray);
			}
		break;

		default:
			break;

		}
	}

	private void loadVarianceComponents() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIVarianceComponentTableBuilder tableBuilder = UIVarianceComponentTableBuilder.getInstance();
		TableView<VarianceComponentRow> table = tableBuilder.getTable();
		List<VarianceComponentRow> tableModel = FXCollections.observableArrayList();
		
		String sql = "SELECT "
				+ "\"type\",\"redundancy\",\"omega\",\"sigma2apost\",\"number_of_observations\", \"quantile\", \"sigma2apost\" > \"quantile\" AS \"significant\" "
				+ "FROM \"VarianceComponent\" "
				+ "JOIN \"TestStatistic\" ON \"VarianceComponent\".\"redundancy\" = \"TestStatistic\".\"d1\" "
				+ "WHERE \"redundancy\" > 0 "
				+ "AND \"d2\" + 1 = \"d2\" " // Workaround to select Infinity-Values
				+ "ORDER BY \"type\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			VarianceComponentType varianceComponentType = VarianceComponentType.getEnumByValue(rs.getInt("type"));

			if (varianceComponentType == null)
				continue;
			
			int numberOfObservations = rs.getInt("number_of_observations");
			double redundancy        = rs.getDouble("redundancy");
			
			double omega       = rs.getDouble("omega");
			double sigma2apost = rs.getDouble("sigma2apost");

			boolean significant = rs.getBoolean("significant");
			
			VarianceComponentRow row = new VarianceComponentRow();

			row.setId(varianceComponentType.getId());
			row.setVarianceComponentType(varianceComponentType);
			row.setNumberOfObservations(numberOfObservations);
			row.setRedundancy(redundancy);
			row.setOmega(omega);
			row.setSigma2aposteriori(sigma2apost);
			row.setSignificant(significant);
			
			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}
	
	private void loadPrincipalComponents() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIPrincipalComponentTableBuilder tableBuilder = UIPrincipalComponentTableBuilder.getInstance();		
		TableView<PrincipalComponentRow> table = tableBuilder.getTable();
		List<PrincipalComponentRow> tableModel = FXCollections.observableArrayList();

		String sql = "SELECT "
				+ "\"index\", SQRT(ABS(\"value\")) AS \"value\", \"ratio\" "
				+ "FROM \"PrincipalComponent\" "
				+ "ORDER BY \"index\" DESC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		int id = 1;
		while (rs.next()) {
			int index    = rs.getInt("index");
			double value = rs.getDouble("value");
			double ratio = rs.getDouble("ratio");

			PrincipalComponentRow row = new PrincipalComponentRow();
			
			row.setId(id++);
			row.setIndex(index);
			row.setValue(value);
			row.setRatio(ratio);
			
			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}
	
	private void loadTestStatistics() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UITestStatisticTableBuilder tableBuilder = UITestStatisticTableBuilder.getInstance();		
		TableView<TestStatisticRow> table = tableBuilder.getTable();
		List<TestStatisticRow> tableModel = FXCollections.observableArrayList();
		
		String sql = "SELECT "
				+ "ABS(\"d1\") AS \"d1\", ABS(\"d2\") AS \"d2\","
				+ "\"probability_value\",\"power_of_test\",\"quantile\","
				+ "\"non_centrality_parameter\",\"p_value\" "
				+ "FROM \"TestStatistic\" "
				+ "ORDER BY ABS(\"d1\") ASC, ABS(\"d2\") DESC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		int id = 1;
		while (rs.next()) {
			double numeratorDegreeOfFreedom   = rs.getDouble("d1");
			double denominatorDegreeOfFreedom = rs.getDouble("d2");
			
			double probabilityValue = rs.getDouble("probability_value");
			double powerOfTest      = rs.getDouble("power_of_test");
			
			double quantile = rs.getDouble("quantile");
			double pValue   = rs.getDouble("p_value");
			
			double noncentralityParameter = rs.getDouble("non_centrality_parameter");
			
			
			TestStatisticRow row = new TestStatisticRow();
			
			row.setId(id++);
			row.setNumeratorDegreeOfFreedom(numeratorDegreeOfFreedom);
			row.setDenominatorDegreeOfFreedom(denominatorDegreeOfFreedom);
			row.setProbabilityValue(probabilityValue);
			row.setPowerOfTest(powerOfTest);
			row.setQuantile(quantile);
			row.setPValue(pValue);
			row.setNoncentralityParameter(noncentralityParameter);
			
			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}
	
	private void loadAdditionalParameters(ObservationTreeItemValue observationItemValue, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIObservationPropertiesPaneBuilder propertiesPaneBuilder = UIObservationPropertiesPaneBuilder.getInstance();
		UIObservationPropertiesPane propertiesPane = propertiesPaneBuilder.getObservationPropertiesPane(observationItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedObservationItemValues);

		ParameterType[] parameterTypes = ObservationTreeItemValue.getParameterTypes(observationItemValue.getItemType());
		StringBuilder inTypeArrayValues = new StringBuilder("?");
		for (int i=1; i<parameterTypes.length; i++) 
			inTypeArrayValues.append(",?");

		StringBuilder inGroupArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedObservationItemValues.length; i++)
			inGroupArrayValues.append(",?");

		String sql = "SELECT \"AdditionalParameterApriori\".\"id\" AS \"id\", \"enable\", \"group_id\", \"type\", " +
				"\"value_0\", \"value\", \"sigma\", \"t_prio\", \"t_post\", \"p_prio\", \"p_post\", \"confidence\", \"gross_error\", \"minimal_detectable_bias\", \"significant\" " +
				"FROM \"AdditionalParameterApriori\" " +
				"LEFT JOIN \"AdditionalParameterAposteriori\" ON \"AdditionalParameterApriori\".\"id\" = \"AdditionalParameterAposteriori\".\"id\" " +
				"WHERE \"group_id\" IN (" + inGroupArrayValues + ") AND \"type\" IN (" + inTypeArrayValues + ") ORDER BY \"group_id\" ASC, \"type\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedObservationItemValues.length; i++)
			stmt.setInt(idx++, selectedObservationItemValues[i].getGroupId());

		for (int i=0; i<parameterTypes.length; i++) 
			stmt.setInt(idx++, parameterTypes[i].getId());

		UIAdditionalParameterTableBuilder tableBuilder = UIAdditionalParameterTableBuilder.getInstance();
		TableView<AdditionalParameterRow> table = tableBuilder.getTable();
		List<AdditionalParameterRow> tableModel = FXCollections.observableArrayList();

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ParameterType paramType = null;
			int type = rs.getInt("type");
			if (rs.wasNull() || (paramType = ParameterType.getEnumByValue(type)) == null)
				continue;

			int paramId    = rs.getInt("id");
			int groupId    = rs.getInt("group_id");
			double value0  = rs.getDouble("value_0");
			boolean enable = rs.getBoolean("enable");

			// Settings/Properties of selected Item
			if (observationItemValue.getGroupId() == groupId)
				propertiesPane.setAdditionalParameter(paramType, value0, enable);

			// Result of all selected Items
			if (enable) {
				AdditionalParameterRow row = new AdditionalParameterRow();
				row.setId(paramId);
				row.setParameterType(paramType);
				double value = rs.getDouble("value");
				if (!rs.wasNull()) 
					row.setValueAposteriori(value);
				else
					continue;

				value = rs.getDouble("sigma");
				if (!rs.wasNull())
					row.setSigmaAposteriori(value);
				else
					continue;

				value = rs.getDouble("confidence");
				if (!rs.wasNull())
					row.setConfidence(value);
				else
					continue;

				value = rs.getDouble("gross_error");
				if (!rs.wasNull())
					row.setGrossError(value);
				else
					continue;

				value = rs.getDouble("minimal_detectable_bias");
				if (!rs.wasNull())
					row.setMinimalDetectableBias(value);
				else
					continue;

				value = rs.getDouble("p_prio");
				if (!rs.wasNull())
					row.setPValueApriori(value);
				else
					continue;

				value = rs.getDouble("p_post");
				if (!rs.wasNull())
					row.setPValueAposteriori(value);
				else
					continue;

				value = rs.getDouble("t_prio");
				if (!rs.wasNull())
					row.setTestStatisticApriori(value);
				else
					continue;

				value = rs.getDouble("t_post");
				if (!rs.wasNull())
					row.setTestStatisticAposteriori(value);
				else
					continue;

				boolean significant = rs.getBoolean("significant");
				row.setSignificant(!rs.wasNull() && significant == Boolean.TRUE);

				tableModel.add(row);
			}
		}
		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadStrainParameterRestrictions(CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue, CongruenceAnalysisTreeItemValue... selectedCongruenceAnalysisItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UICongruenceAnalysisPropertiesPaneBuilder propertiesPaneBuilder = UICongruenceAnalysisPropertiesPaneBuilder.getInstance();
		UICongruenceAnalysisPropertiesPane propertiesPane = propertiesPaneBuilder.getCongruenceAnalysisPropertiesPane(congruenceAnalysisItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedCongruenceAnalysisItemValues);
		
		RestrictionType[] restrictionTypes = CongruenceAnalysisTreeItemValue.getRestrictionTypes(congruenceAnalysisItemValue.getItemType());
		StringBuilder inTypeArrayValues = new StringBuilder("?");
		for (int i=1; i<restrictionTypes.length; i++) 
			inTypeArrayValues.append(",?");

		String sql = "SELECT "
				+ "\"type\", \"enable\" "
				+ "FROM \"CongruenceAnalysisStrainParameterRestriction\" "
				+ "WHERE \"group_id\" = ? AND \"type\" IN (" + inTypeArrayValues + ")";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setInt(idx++, congruenceAnalysisItemValue.getGroupId());

		for (int i=0; i<restrictionTypes.length; i++) 
			stmt.setInt(idx++, restrictionTypes[i].getId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			RestrictionType restrictionType = null;
			int type       = rs.getInt("type");
			boolean enable = rs.getBoolean("enable");
			if (rs.wasNull() || (restrictionType = RestrictionType.getEnumByValue(type)) == null)
				continue;

			propertiesPane.setStrainParameter(restrictionType, enable);
		}

	}

	private void loadStrainParameters(CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue, CongruenceAnalysisTreeItemValue... selectedCongruenceAnalysisItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UICongruenceAnalysisPropertiesPaneBuilder propertiesPaneBuilder = UICongruenceAnalysisPropertiesPaneBuilder.getInstance();
		UICongruenceAnalysisPropertiesPane propertiesPane = propertiesPaneBuilder.getCongruenceAnalysisPropertiesPane(congruenceAnalysisItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedCongruenceAnalysisItemValues);

		ParameterType[] parameterTypes = CongruenceAnalysisTreeItemValue.getParameterTypes(congruenceAnalysisItemValue.getItemType());
		StringBuilder inTypeArrayValues = new StringBuilder("?");
		for (int i=1; i<parameterTypes.length; i++) 
			inTypeArrayValues.append(",?");

		StringBuilder inGroupArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedCongruenceAnalysisItemValues.length; i++)
			inGroupArrayValues.append(",?");

		String sql = "SELECT "
				+ "\"type\",\"value\",\"sigma\","
				+ "\"confidence\",\"gross_error\",\"minimal_detectable_bias\","
				+ "\"p_prio\",\"p_post\",\"t_prio\",\"t_post\",\"significant\" "
				+ "FROM \"CongruenceAnalysisStrainParameterAposteriori\" "
				+ "WHERE \"group_id\" IN (" + inGroupArrayValues + ") AND \"type\" IN (" + inTypeArrayValues + ") ORDER BY \"group_id\" ASC, \"type\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedCongruenceAnalysisItemValues.length; i++)
			stmt.setInt(idx++, selectedCongruenceAnalysisItemValues[i].getGroupId());

		for (int i=0; i<parameterTypes.length; i++) 
			stmt.setInt(idx++, parameterTypes[i].getId());

		UIAdditionalParameterTableBuilder tableBuilder = UIAdditionalParameterTableBuilder.getInstance();
		TableView<AdditionalParameterRow> table = tableBuilder.getTable();
		List<AdditionalParameterRow> tableModel = FXCollections.observableArrayList();

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ParameterType paramType = null;
			int type = rs.getInt("type");
			if (rs.wasNull() || (paramType = ParameterType.getEnumByValue(type)) == null)
				continue;

			// Result of all selected Items
			AdditionalParameterRow row = new AdditionalParameterRow();

			row.setId(-1); // Strain parameter hasn't an id
			row.setParameterType(paramType);

			double value = rs.getDouble("value");
			if (!rs.wasNull())
				row.setValueAposteriori(value);

			value = rs.getDouble("sigma");
			if (!rs.wasNull())
				row.setSigmaAposteriori(value > 0 ? value : 0.0);

			value = rs.getDouble("confidence");
			if (!rs.wasNull())
				row.setConfidence(value);

			value = rs.getDouble("gross_error");
			if (!rs.wasNull())
				row.setGrossError(value);

			value = rs.getDouble("minimal_detectable_bias");
			if (!rs.wasNull())
				row.setMinimalDetectableBias(value);

			value = rs.getDouble("p_prio");
			if (!rs.wasNull())
				row.setPValueApriori(value);

			value = rs.getDouble("p_post");
			if (!rs.wasNull())
				row.setPValueAposteriori(value);

			value = rs.getDouble("t_prio");
			if (!rs.wasNull())
				row.setTestStatisticApriori(value);

			value = rs.getDouble("t_post");
			if (!rs.wasNull())
				row.setTestStatisticAposteriori(value);

			boolean significant = rs.getBoolean("significant");
			row.setSignificant(!rs.wasNull() && significant == Boolean.TRUE);
			tableModel.add(row);
		}
		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadEpoch(ObservationTreeItemValue observationItemValue, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIObservationPropertiesPaneBuilder propertiesPaneBuilder = UIObservationPropertiesPaneBuilder.getInstance();
		UIObservationPropertiesPane propertiesPane = propertiesPaneBuilder.getObservationPropertiesPane(observationItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedObservationItemValues);

		boolean referenceEpoch = true;
		String sql = "SELECT \"reference_epoch\" "
				+ "FROM \"ObservationGroup\" "
				+ "WHERE \"id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, observationItemValue.getGroupId());

		ResultSet rs = stmt.executeQuery();
		if (rs.next())
			referenceEpoch = rs.getBoolean("reference_epoch");

		propertiesPane.setReferenceEpoch(referenceEpoch);
	}
	
	private void loadUncertainties(VerticalDeflectionTreeItemValue verticalDeflectionItemValue, VerticalDeflectionTreeItemValue... selectedVerticalDeflectionItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIVerticalDeflectionPropertiesPaneBuilder propertiesPaneBuilder = UIVerticalDeflectionPropertiesPaneBuilder.getInstance();
		UIVerticalDeflectionPropertiesPane propertiesPane = propertiesPaneBuilder.getVerticalDeflectionPropertiesPane(verticalDeflectionItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedVerticalDeflectionItemValues);

		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"VerticalDeflectionGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, verticalDeflectionItemValue.getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			VerticalDeflectionGroupUncertaintyType type = VerticalDeflectionGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (type != null) {
				double value = rs.getDouble("value");
				propertiesPane.setUncertainty(type, value);
			}
		}
	}
	
	private void loadVerticalDeflections(VerticalDeflectionTreeItemValue verticalDeflectionItemValue, VerticalDeflectionTreeItemValue... selectedVerticalDeflectionItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedVerticalDeflectionItemValues.length; i++)
			inArrayValues.append(",?");

		UIVerticalDeflectionTableBuilder tableBuilder = UIVerticalDeflectionTableBuilder.getInstance();
		TableView<VerticalDeflectionRow> table = tableBuilder.getTable(verticalDeflectionItemValue);
		List<VerticalDeflectionRow> tableModel = FXCollections.observableArrayList();

		String sql = "SELECT " + 
				// Part: point
				"\"VerticalDeflectionApriori\".\"id\", \"name\", \"enable\", \"group_id\", \"type\", " + 
				"\"x0\", \"y0\", " +
				"\"VerticalDeflectionApriori\".\"sigma_y0\", \"VerticalDeflectionApriori\".\"sigma_x0\", " + 
				"\"y\", \"x\",  \"sigma_y\", \"sigma_x\", " + 
				"\"confidence_major_axis\", \"confidence_minor_axis\", " + 
				"\"residual_y\", \"residual_x\", " +
				"\"redundancy_y\", \"redundancy_x\", " +
				"\"gross_error_y\", \"gross_error_x\", \"minimal_detectable_bias_y\", \"minimal_detectable_bias_x\", " + 
				"\"omega\", \"significant\", " + 
				"\"t_prio\", \"t_post\", \"p_prio\", \"p_post\" " + 
				"FROM \"VerticalDeflectionApriori\" " + 
				"JOIN \"VerticalDeflectionGroup\" ON \"VerticalDeflectionApriori\".\"group_id\" = \"VerticalDeflectionGroup\".\"id\" " + 
				"LEFT JOIN \"VerticalDeflectionAposteriori\" ON \"VerticalDeflectionApriori\".\"id\" = \"VerticalDeflectionAposteriori\".\"id\" " + 

				"WHERE \"VerticalDeflectionGroup\".\"type\" = ? " +
				"AND \"VerticalDeflectionGroup\".\"id\" IN (" + inArrayValues + ") " + 

				"ORDER BY \"VerticalDeflectionGroup\".\"order\" ASC, \"VerticalDeflectionGroup\".\"id\" ASC, \"VerticalDeflectionApriori\".\"id\" ASC";


		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		
		stmt.setInt(1, verticalDeflectionItemValue.getVerticalDeflectionType().getId());

		for (int i=0; i<selectedVerticalDeflectionItemValues.length; i++)
			stmt.setInt(i+2, selectedVerticalDeflectionItemValues[i].getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			VerticalDeflectionRow row = new VerticalDeflectionRow();
			// POINT

			// a-priori-Values
			row.setId(rs.getInt("id"));
			row.setGroupId(rs.getInt("group_id"));
			row.setName(rs.getString("name"));
			row.setEnable(rs.getBoolean("enable"));

			double value;
			value = rs.getDouble("x0");
			row.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			row.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			row.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			row.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);


			// a-posteriori
			value = rs.getDouble("x");
			row.setXAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y");
			row.setYAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x");
			row.setSigmaXaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_y");
			row.setSigmaYaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			// Confidence
			value = rs.getDouble("confidence_major_axis");
			row.setConfidenceA(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_minor_axis");
			row.setConfidenceC(rs.wasNull() ? null : Math.abs(value));
			
			// Residual
			value = rs.getDouble("residual_x");
			row.setResidualX(rs.wasNull() ? null : value);

			value = rs.getDouble("residual_y");
			row.setResidualY(rs.wasNull() ? null : value);

			// Redundancy
			value = rs.getDouble("redundancy_x");
			row.setRedundancyX(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("redundancy_y");
			row.setRedundancyY(rs.wasNull() ? null : Math.abs(value));

			// Gross error
			value = rs.getDouble("gross_error_x");
			row.setGrossErrorX(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_y");
			row.setGrossErrorY(rs.wasNull() ? null : value);

			// MDB
			value = rs.getDouble("minimal_detectable_bias_x");
			row.setMinimalDetectableBiasX(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_y");
			row.setMinimalDetectableBiasY(rs.wasNull() ? null : value);


			// Statistics
			value = rs.getDouble("omega");
			row.setOmega(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("p_prio");
			row.setPValueApriori(rs.wasNull() ? null : value);

			value = rs.getDouble("p_post");
			row.setPValueAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("t_prio");
			row.setTestStatisticApriori(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("t_post");
			row.setTestStatisticAposteriori(rs.wasNull() ? null : Math.abs(value));

			boolean significant = rs.getBoolean("significant");
			row.setSignificant(!rs.wasNull() && significant == Boolean.TRUE);

			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadUncertainties(ObservationTreeItemValue observationItemValue, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIObservationPropertiesPaneBuilder propertiesPaneBuilder = UIObservationPropertiesPaneBuilder.getInstance();
		UIObservationPropertiesPane propertiesPane = propertiesPaneBuilder.getObservationPropertiesPane(observationItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedObservationItemValues);

		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"ObservationGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, observationItemValue.getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ObservationGroupUncertaintyType type = ObservationGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (type != null) {
				double value = rs.getDouble("value");
				propertiesPane.setUncertainty(type, value);
			}
		}
	}

	private void loadObservations(ObservationTreeItemValue observationItemValue, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedObservationItemValues.length; i++)
			inArrayValues.append(",?");

		UITerrestrialObservationTableBuilder tableBuilder = UITerrestrialObservationTableBuilder.getInstance();
		TableView<TerrestrialObservationRow> table = tableBuilder.getTable(observationItemValue);
		List<TerrestrialObservationRow> tableModel = FXCollections.observableArrayList();
		String sql = "SELECT " + 
				"\"ObservationApriori\".\"id\", \"group_id\", \"start_point_name\", \"end_point_name\", \"instrument_height\", \"reflector_height\", \"value_0\", \"distance_0\", \"ObservationApriori\".\"sigma_0\" AS \"sigma_0\", \"enable\", " + 
				"\"ObservationAposteriori\".\"value\", \"sigma\", \"residual\", \"redundancy\", \"gross_error\", \"influence_on_position\", \"influence_on_network_distortion\", \"minimal_detectable_bias\", \"omega\", \"t_prio\", \"t_post\", \"p_prio\", \"p_post\", \"significant\" " + 
				"FROM \"ObservationApriori\" " + 
				"INNER JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " + 
				"LEFT JOIN \"ObservationAposteriori\" ON \"ObservationApriori\".\"id\" = \"ObservationAposteriori\".\"id\" " + 
				"WHERE \"ObservationGroup\".\"type\" = ? " +
				"AND \"ObservationGroup\".\"id\" IN (" + inArrayValues + ") " + 
				"ORDER BY \"ObservationGroup\".\"order\" ASC, \"ObservationGroup\".\"id\" ASC, \"ObservationApriori\".\"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, observationItemValue.getObservationType().getId());
		for (int i=0; i<selectedObservationItemValues.length; i++)
			stmt.setInt(i+2, selectedObservationItemValues[i].getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			TerrestrialObservationRow row = new TerrestrialObservationRow();

			// Apriori-Values
			row.setId(rs.getInt("id"));
			row.setGroupId(rs.getInt("group_id"));
			row.setStartPointName(rs.getString("start_point_name"));
			row.setEndPointName(rs.getString("end_point_name"));
			row.setEnable(rs.getBoolean("enable"));
			row.setValueApriori(rs.getDouble("value_0"));

			double value = rs.getDouble("instrument_height");
			if (!rs.wasNull())
				row.setInstrumentHeight(value);

			value = rs.getDouble("reflector_height");
			if (!rs.wasNull())
				row.setReflectorHeight(value);

			value = rs.getDouble("distance_0");
			if (!rs.wasNull())
				row.setDistanceApriori(value > 0 ? value : null);

			value = rs.getDouble("sigma_0");
			if (!rs.wasNull())
				row.setSigmaApriori(value > 0 ? value : null);

			// Aposterior-Values
			value = rs.getDouble("value");
			if (!rs.wasNull())
				row.setValueAposteriori(value);

			value = rs.getDouble("sigma");
			if (!rs.wasNull())
				row.setSigmaAposteriori(value > 0 ? value : 0.0);
			
			value = rs.getDouble("residual");
			if (!rs.wasNull())
				row.setResidual(value);

			value = rs.getDouble("redundancy");
			if (!rs.wasNull())
				row.setRedundancy(Math.abs(value));

			value = rs.getDouble("gross_error");
			if (!rs.wasNull())
				row.setGrossError(value);

			value = rs.getDouble("influence_on_position");
			if (!rs.wasNull())
				row.setInfluenceOnPointPosition(value);

			value = rs.getDouble("influence_on_network_distortion");
			if (!rs.wasNull())
				row.setInfluenceOnNetworkDistortion(value);

			value = rs.getDouble("minimal_detectable_bias");
			if (!rs.wasNull())
				row.setMinimalDetectableBias(value);

			value = rs.getDouble("omega");
			if (!rs.wasNull())
				row.setOmega(value);

			value = rs.getDouble("p_prio");
			if (!rs.wasNull())
				row.setPValueApriori(value);

			value = rs.getDouble("p_post");
			if (!rs.wasNull())
				row.setPValueAposteriori(value);

			value = rs.getDouble("t_prio");
			if (!rs.wasNull())
				row.setTestStatisticApriori(value);

			value = rs.getDouble("t_post");
			if (!rs.wasNull())
				row.setTestStatisticAposteriori(value);

			boolean significant = rs.getBoolean("significant");
			if (!rs.wasNull())
				row.setSignificant(significant);

			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadGNSSObservations(ObservationTreeItemValue observationGNSSItemValue, ObservationTreeItemValue... selectedGNSSObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i = 1; i < selectedGNSSObservationItemValues.length; i++)
			inArrayValues.append(",?");

		UIGNSSObservationTableBuilder tableBuilder = UIGNSSObservationTableBuilder.getInstance();
		TableView<GNSSObservationRow> table = tableBuilder.getTable(observationGNSSItemValue);
		List<GNSSObservationRow> tableModel = FXCollections.observableArrayList();
		String sql = "SELECT " + 
				"\"GNSSObservationApriori\".\"id\", \"group_id\", \"start_point_name\", \"end_point_name\", \"y0\", \"x0\", \"z0\", \"GNSSObservationApriori\".\"sigma_y0\" AS \"sigma_y0\", \"GNSSObservationApriori\".\"sigma_x0\" AS \"sigma_x0\", \"GNSSObservationApriori\".\"sigma_z0\" AS \"sigma_z0\", \"enable\", " + 
				"\"y\", \"x\", \"z\",  \"sigma_y\", \"sigma_x\", \"sigma_z\", " + 

				"\"residual_y\", \"residual_x\", \"residual_z\", " +
				"\"redundancy_y\", \"redundancy_x\", \"redundancy_z\", " +
				"\"gross_error_y\", \"gross_error_x\", \"gross_error_z\", " +
				"\"minimal_detectable_bias_y\", \"minimal_detectable_bias_x\", \"minimal_detectable_bias_z\", " +
				"\"influence_on_position_y\", \"influence_on_position_x\", \"influence_on_position_z\", \"influence_on_network_distortion\", " + 
				"\"omega\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"significant\" " + 

				"FROM \"GNSSObservationApriori\" " + 
				"INNER JOIN \"ObservationGroup\" ON \"GNSSObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " + 
				"LEFT JOIN \"GNSSObservationAposteriori\" ON \"GNSSObservationApriori\".\"id\" = \"GNSSObservationAposteriori\".\"id\" " + 
				"WHERE \"ObservationGroup\".\"type\" = ? " +
				"AND \"ObservationGroup\".\"id\" IN (" + inArrayValues + ") " + 
				"ORDER BY \"ObservationGroup\".\"order\" ASC, \"ObservationGroup\".\"id\" ASC, \"GNSSObservationApriori\".\"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, observationGNSSItemValue.getObservationType().getId());
		for (int i = 0; i < selectedGNSSObservationItemValues.length; i++)
			stmt.setInt(i+2, selectedGNSSObservationItemValues[i].getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			GNSSObservationRow row = new GNSSObservationRow();
			// Apriori-Values
			row.setId(rs.getInt("id"));
			row.setGroupId(rs.getInt("group_id"));
			row.setStartPointName(rs.getString("start_point_name"));
			row.setEndPointName(rs.getString("end_point_name"));
			row.setEnable(rs.getBoolean("enable"));
			double value;
			value = rs.getDouble("x0");
			row.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			row.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z0");
			row.setZApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			row.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			row.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_z0");
			row.setSigmaZapriori(rs.wasNull() || value <= 0 ? null : value);


			// Aposteriori
			value = rs.getDouble("x");
			row.setXAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("y");
			row.setYAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("z");
			row.setZAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("sigma_x");
			row.setSigmaXaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_y");
			row.setSigmaYaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_z");
			row.setSigmaZaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);
			
			// Residuals
			value = rs.getDouble("residual_x");
			row.setResidualX(rs.wasNull() ? null : value);

			value = rs.getDouble("residual_y");
			row.setResidualY(rs.wasNull() ? null : value);

			value = rs.getDouble("residual_z");
			row.setResidualZ(rs.wasNull() ? null : value);

			// Redundancy
			value = rs.getDouble("redundancy_x");
			row.setRedundancyX(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("redundancy_y");
			row.setRedundancyY(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("redundancy_z");
			row.setRedundancyZ(rs.wasNull() ? null : Math.abs(value));

			// Gross error
			value = rs.getDouble("gross_error_x");
			row.setGrossErrorX(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_y");
			row.setGrossErrorY(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_z");
			row.setGrossErrorZ(rs.wasNull() ? null : value);

			// MDB
			value = rs.getDouble("minimal_detectable_bias_x");
			row.setMinimalDetectableBiasX(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_y");
			row.setMinimalDetectableBiasY(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_z");
			row.setMinimalDetectableBiasZ(rs.wasNull() ? null : value);

			// EP + EFSPmax
			value = rs.getDouble("influence_on_position_x");
			row.setInfluenceOnPointPositionX(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_position_y");
			row.setInfluenceOnPointPositionY(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_position_z");
			row.setInfluenceOnPointPositionZ(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_network_distortion");
			row.setInfluenceOnNetworkDistortion(rs.wasNull() ? null : value);

			// Statistics
			value = rs.getDouble("omega");
			row.setOmega(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("p_prio");
			row.setPValueApriori(rs.wasNull() ? null : value);

			value = rs.getDouble("p_post");
			row.setPValueAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("t_prio");
			row.setTestStatisticApriori(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("t_post");
			row.setTestStatisticAposteriori(rs.wasNull() ? null : Math.abs(value));

			boolean significantPoint = rs.getBoolean("significant");
			row.setSignificant(!rs.wasNull() && significantPoint == Boolean.TRUE);

			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadUncertainties(PointTreeItemValue pointItemValue, PointTreeItemValue... selectedPointItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		UIPointPropertiesPaneBuilder propertiesPaneBuilder = UIPointPropertiesPaneBuilder.getInstance();
		UIPointPropertiesPane propertiesPane = propertiesPaneBuilder.getPointPropertiesPane(pointItemValue.getItemType());
		propertiesPane.setTreeItemValue(selectedPointItemValues);

		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"PointGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, pointItemValue.getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			PointGroupUncertaintyType type = PointGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (type != null) {
				double value = rs.getDouble("value");
				propertiesPane.setUncertainty(type, value);
			}
		}
	}

	private void loadPoints(PointTreeItemValue pointItemValue, PointTreeItemValue... selectedPointItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedPointItemValues.length; i++)
			inArrayValues.append(",?");

		UIPointTableBuilder tableBuilder = UIPointTableBuilder.getInstance();
		TableView<PointRow> table = tableBuilder.getTable(pointItemValue);
		List<PointRow> tableModel = FXCollections.observableArrayList();

		String sql = "SELECT " + 
				// Part: point
				"\"PointApriori\".\"id\", \"name\", \"code\", \"enable\"," + 
				"\"x0\", \"y0\", \"z0\", " +
				"\"PointApriori\".\"sigma_y0\", \"PointApriori\".\"sigma_x0\", \"PointApriori\".\"sigma_z0\", " + 
				"\"y\", \"x\", \"z\",  \"sigma_y\", \"sigma_x\", \"sigma_z\", " + 
				"\"PointAposteriori\".\"confidence_major_axis\", \"PointAposteriori\".\"confidence_middle_axis\", \"PointAposteriori\".\"confidence_minor_axis\", \"confidence_alpha\", \"confidence_beta\", \"confidence_gamma\", " + 
				"\"residual_y\", \"residual_x\", \"residual_z\", " + 
				"\"redundancy_y\", \"redundancy_x\", \"redundancy_z\", " + 
				"\"gross_error_y\", \"gross_error_x\", \"gross_error_z\", \"influence_on_position_y\", \"influence_on_position_x\", \"influence_on_position_z\", " + 
				"\"influence_on_network_distortion\", \"minimal_detectable_bias_y\", \"minimal_detectable_bias_x\", \"minimal_detectable_bias_z\", \"first_principal_component_y\", \"first_principal_component_x\", \"first_principal_component_z\", " + 
				"\"omega\", \"significant\", " + 
				"\"t_prio\", \"t_post\", \"p_prio\", \"p_post\", " + 
				// Part: group
				"\"group_id\", \"type\", \"dimension\" " + 
				// Part: deflection
				"FROM \"PointApriori\" " + 
				"JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" " + 
				"LEFT JOIN \"PointAposteriori\" ON \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" " + 

				"WHERE \"PointGroup\".\"type\" = ? AND \"PointGroup\".\"dimension\" = ? " +
				"AND \"PointGroup\".\"id\" IN (" + inArrayValues + ") " + 

				"ORDER BY \"PointGroup\".\"order\" ASC, \"PointGroup\".\"id\" ASC, \"PointApriori\".\"id\" ASC";


		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		stmt.setInt(1, pointItemValue.getPointType().getId());
		stmt.setInt(2, pointItemValue.getDimension());

		for (int i=0; i<selectedPointItemValues.length; i++)
			stmt.setInt(i+3, selectedPointItemValues[i].getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			PointRow row = new PointRow();
			// POINT

			// Apriori-Values
			row.setId(rs.getInt("id"));
			row.setGroupId(rs.getInt("group_id"));
			row.setName(rs.getString("name"));
			row.setCode(rs.getString("code"));
			row.setEnable(rs.getBoolean("enable"));

			double value;
			value = rs.getDouble("x0");
			row.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			row.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z0");
			row.setZApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			row.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			row.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_z0");
			row.setSigmaZapriori(rs.wasNull() || value <= 0 ? null : value);


			// Aposteriori
			value = rs.getDouble("x");
			row.setXAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y");
			row.setYAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z");
			row.setZAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x");
			row.setSigmaXaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_y");
			row.setSigmaYaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_z");
			row.setSigmaZaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			// Confidence
			value = rs.getDouble("confidence_major_axis");
			row.setConfidenceA(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_middle_axis");
			row.setConfidenceB(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_minor_axis");
			row.setConfidenceC(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_alpha");
			row.setConfidenceAlpha(rs.wasNull() ? null : value);

			value = rs.getDouble("confidence_beta");
			row.setConfidenceBeta(rs.wasNull() ? null : value);

			value = rs.getDouble("confidence_gamma");
			row.setConfidenceGamma(rs.wasNull() ? null : value);
			
			// Residual
			value = rs.getDouble("residual_x");
			row.setResidualX(rs.wasNull() ? null : value);

			value = rs.getDouble("residual_y");
			row.setResidualY(rs.wasNull() ? null : value);

			value = rs.getDouble("residual_z");
			row.setResidualZ(rs.wasNull() ? null : value);

			// Redundancy
			value = rs.getDouble("redundancy_x");
			row.setRedundancyX(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("redundancy_y");
			row.setRedundancyY(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("redundancy_z");
			row.setRedundancyZ(rs.wasNull() ? null : Math.abs(value));

			// Gross error
			value = rs.getDouble("gross_error_x");
			row.setGrossErrorX(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_y");
			row.setGrossErrorY(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_z");
			row.setGrossErrorZ(rs.wasNull() ? null : value);

			// MDB
			value = rs.getDouble("minimal_detectable_bias_x");
			row.setMinimalDetectableBiasX(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_y");
			row.setMinimalDetectableBiasY(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_z");
			row.setMinimalDetectableBiasZ(rs.wasNull() ? null : value);

			// PCA
			value = rs.getDouble("first_principal_component_x");
			row.setFirstPrincipalComponentX(rs.wasNull() ? null : value);

			value = rs.getDouble("first_principal_component_y");
			row.setFirstPrincipalComponentY(rs.wasNull() ? null : value);

			value = rs.getDouble("first_principal_component_z");
			row.setFirstPrincipalComponentZ(rs.wasNull() ? null : value);

			// EP + EFSPmax
			value = rs.getDouble("influence_on_position_x");
			row.setInfluenceOnPointPositionX(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_position_y");
			row.setInfluenceOnPointPositionY(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_position_z");
			row.setInfluenceOnPointPositionZ(rs.wasNull() ? null : value);

			value = rs.getDouble("influence_on_network_distortion");
			row.setInfluenceOnNetworkDistortion(rs.wasNull() ? null : value);

			// Statistics
			value = rs.getDouble("omega");
			row.setOmega(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("p_prio");
			row.setPValueApriori(rs.wasNull() ? null : value);

			value = rs.getDouble("p_post");
			row.setPValueAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("t_prio");
			row.setTestStatisticApriori(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("t_post");
			row.setTestStatisticAposteriori(rs.wasNull() ? null : Math.abs(value));

			boolean significantPoint = rs.getBoolean("significant");
			row.setSignificant(!rs.wasNull() && significantPoint == Boolean.TRUE);

			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	private void loadCongruenceAnalysisPointPair(CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue, CongruenceAnalysisTreeItemValue... selectedCongruenceAnalysisItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedCongruenceAnalysisItemValues.length; i++)
			inArrayValues.append(",?");

		UICongruenceAnalysisTableBuilder tableBuilder = UICongruenceAnalysisTableBuilder.getInstance();
		TableView<CongruenceAnalysisRow> table = tableBuilder.getTable(congruenceAnalysisItemValue);
		List<CongruenceAnalysisRow> tableModel = FXCollections.observableArrayList();

		String sql = "SELECT "
				// a-priori
				+ "\"CongruenceAnalysisPointPairApriori\".\"id\", \"start_point_name\", \"end_point_name\", \"CongruenceAnalysisPointPairApriori\".\"enable\", "
				// a-posteriori
				+ "\"y\", \"x\", \"z\", \"sigma_y\", \"sigma_x\", \"sigma_z\", \"confidence_major_axis\", \"confidence_middle_axis\", \"confidence_minor_axis\", "
				+ "\"confidence_alpha\", \"confidence_beta\", \"confidence_gamma\", \"gross_error_y\", \"gross_error_x\", \"gross_error_z\", \"minimal_detectable_bias_y\", "
				+ "\"minimal_detectable_bias_x\", \"minimal_detectable_bias_z\", \"p_prio\", \"p_post\", \"t_prio\", \"t_post\", \"significant\", "
				// group
				+ "\"group_id\", \"dimension\" FROM \"CongruenceAnalysisPointPairApriori\" "
				+ "JOIN \"CongruenceAnalysisGroup\" ON \"CongruenceAnalysisPointPairApriori\".\"group_id\" = \"CongruenceAnalysisGroup\".\"id\" "
				+ "LEFT JOIN \"CongruenceAnalysisPointPairAposteriori\" ON \"CongruenceAnalysisPointPairApriori\".\"id\" = \"CongruenceAnalysisPointPairAposteriori\".\"id\" "
				+ "WHERE \"CongruenceAnalysisGroup\".\"dimension\" = ? "
				+ "AND \"CongruenceAnalysisGroup\".\"id\" IN (" + inArrayValues + ") "
				+ "ORDER BY \"CongruenceAnalysisGroup\".\"order\" ASC, \"CongruenceAnalysisGroup\".\"id\" ASC, \"CongruenceAnalysisPointPairApriori\".\"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		int idx = 1;
		stmt.setInt(idx++, congruenceAnalysisItemValue.getDimension());

		for (int i=0; i<selectedCongruenceAnalysisItemValues.length; i++)
			stmt.setInt(idx++, selectedCongruenceAnalysisItemValues[i].getGroupId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			CongruenceAnalysisRow row = new CongruenceAnalysisRow();

			// Apriori-Values
			row.setId(rs.getInt("id"));
			row.setGroupId(rs.getInt("group_id"));
			row.setNameInReferenceEpoch(rs.getString("start_point_name"));
			row.setNameInControlEpoch(rs.getString("end_point_name"));
			row.setEnable(rs.getBoolean("enable"));

			double value;
			// Aposteriori
			value = rs.getDouble("x");
			row.setXAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y");
			row.setYAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z");
			row.setZAposteriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x");
			row.setSigmaXaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_y");
			row.setSigmaYaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			value = rs.getDouble("sigma_z");
			row.setSigmaZaposteriori(rs.wasNull() ? null : value > 0 ? value : 0.0);

			// Confidence
			value = rs.getDouble("confidence_major_axis");
			row.setConfidenceA(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_middle_axis");
			row.setConfidenceB(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_minor_axis");
			row.setConfidenceC(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("confidence_alpha");
			row.setConfidenceAlpha(rs.wasNull() ? null : value);

			value = rs.getDouble("confidence_beta");
			row.setConfidenceBeta(rs.wasNull() ? null : value);

			value = rs.getDouble("confidence_gamma");
			row.setConfidenceGamma(rs.wasNull() ? null : value);

			// Gross error
			value = rs.getDouble("gross_error_x");
			row.setGrossErrorX(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_y");
			row.setGrossErrorY(rs.wasNull() ? null : value);

			value = rs.getDouble("gross_error_z");
			row.setGrossErrorZ(rs.wasNull() ? null : value);

			// MDB
			value = rs.getDouble("minimal_detectable_bias_x");
			row.setMinimalDetectableBiasX(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_y");
			row.setMinimalDetectableBiasY(rs.wasNull() ? null : value);

			value = rs.getDouble("minimal_detectable_bias_z");
			row.setMinimalDetectableBiasZ(rs.wasNull() ? null : value);

			// Statistics
			value = rs.getDouble("p_prio");
			row.setPValueApriori(rs.wasNull() ? null : value);

			value = rs.getDouble("p_post");
			row.setPValueAposteriori(rs.wasNull() ? null : value);

			value = rs.getDouble("t_prio");
			row.setTestStatisticApriori(rs.wasNull() ? null : Math.abs(value));

			value = rs.getDouble("t_post");
			row.setTestStatisticAposteriori(rs.wasNull() ? null : Math.abs(value));

			boolean significantPoint = rs.getBoolean("significant");
			row.setSignificant(!rs.wasNull() && significantPoint == Boolean.TRUE);

			tableModel.add(row);
		}

		if (!tableModel.isEmpty())
			table.getItems().setAll(tableModel);
		else
			table.getItems().setAll(tableBuilder.getEmptyRow());
		table.sort();
	}

	public void remove(Row rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = null;
		if (rowData instanceof TerrestrialObservationRow)
			sql = "DELETE FROM \"ObservationApriori\" WHERE \"id\" = ? LIMIT 1";
		else if (rowData instanceof GNSSObservationRow)
			sql = "DELETE FROM \"GNSSObservationApriori\" WHERE \"id\" = ? LIMIT 1";
		else if (rowData instanceof PointRow)
			sql = "DELETE FROM \"PointApriori\" WHERE \"id\" = ? LIMIT 1";
		else if (rowData instanceof CongruenceAnalysisRow)
			sql = "DELETE FROM \"CongruenceAnalysisPointPairApriori\" WHERE \"id\" = ? LIMIT 1";
		else if (rowData instanceof VerticalDeflectionRow)
			sql = "DELETE FROM \"VerticalDeflectionApriori\" WHERE \"id\" = ? LIMIT 1";

		if (sql != null) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			stmt.setInt(1, rowData.getId());
			stmt.execute();
		}
	}

	public void save(MetaData metaData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;

		String sql = "MERGE INTO \"ProjectMetadata\" USING (VALUES "
				+ "(CAST(? AS INT), ?, ?, ?, ?, ?, ?) "
				+ ") AS \"vals\" (\"id\", \"name\", \"operator\", \"description\", \"date\", \"customer_id\", \"project_id\") ON \"ProjectMetadata\".\"id\" = \"vals\".\"id\" AND \"ProjectMetadata\".\"id\" = 1 "
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
				+ "\"vals\".\"project_id\" ";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		stmt.setInt(idx++,     1); // default ID
		stmt.setString(idx++,  metaData.getName());
		stmt.setString(idx++,  metaData.getOperator());
		stmt.setString(idx++,  metaData.getDescription());
		stmt.setTimestamp(idx++, Timestamp.valueOf(metaData.getDate().atStartOfDay()));
		
		stmt.setString(idx++,  metaData.getCustomerId());
		stmt.setString(idx++,  metaData.getProjectId());

		stmt.execute();
	}	

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveItem(TerrestrialObservationRow rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || rowData == null || 
				rowData.getStartPointName() == null || rowData.getEndPointName() == null || 
				rowData.getStartPointName().equals(rowData.getEndPointName()) ||
				rowData.getValueApriori() == null ||
				rowData.getStartPointName().trim().isEmpty() || rowData.getEndPointName().trim().isEmpty())
			return;
		
		String sql = "MERGE INTO \"ObservationApriori\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), ?, ?, CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"group_id\", \"start_point_name\", \"end_point_name\", \"instrument_height\", \"reflector_height\", \"value_0\", \"sigma_0\", \"distance_0\", \"enable\") ON \"ObservationApriori\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ObservationApriori\".\"group_id\"           = \"vals\".\"group_id\", "
				+ "\"ObservationApriori\".\"start_point_name\"   = \"vals\".\"start_point_name\", "
				+ "\"ObservationApriori\".\"end_point_name\"     = \"vals\".\"end_point_name\", "
				+ "\"ObservationApriori\".\"instrument_height\"  = \"vals\".\"instrument_height\", "
				+ "\"ObservationApriori\".\"reflector_height\"   = \"vals\".\"reflector_height\", "
				+ "\"ObservationApriori\".\"value_0\"            = \"vals\".\"value_0\", "
				+ "\"ObservationApriori\".\"sigma_0\"            = \"vals\".\"sigma_0\", "
				+ "\"ObservationApriori\".\"distance_0\"         = \"vals\".\"distance_0\", "
				+ "\"ObservationApriori\".\"enable\"             = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"start_point_name\", "
				+ "\"vals\".\"end_point_name\","
				+ "\"vals\".\"instrument_height\", "
				+ "\"vals\".\"reflector_height\", "
				+ "\"vals\".\"value_0\", "
				+ "\"vals\".\"sigma_0\", "
				+ "\"vals\".\"distance_0\", "
				+ "\"vals\".\"enable\"";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (rowData.getId() < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, rowData.getId());

		stmt.setInt(idx++,     rowData.getGroupId());
		stmt.setString(idx++,  rowData.getStartPointName());
		stmt.setString(idx++,  rowData.getEndPointName());

		stmt.setDouble(idx++,  rowData.getInstrumentHeight());
		stmt.setDouble(idx++,  rowData.getReflectorHeight());

		stmt.setDouble(idx++,  rowData.getValueApriori());

		stmt.setDouble(idx++,  rowData.getSigmaApriori() == null || rowData.getSigmaApriori() < 0 ? 0 : rowData.getSigmaApriori());
		stmt.setDouble(idx++,  rowData.getDistanceApriori() == null || rowData.getDistanceApriori() < 0 ? 0 : rowData.getDistanceApriori());

		stmt.setBoolean(idx++, rowData.isEnable());

		stmt.execute();

		if (rowData.getId() < 0) {
			int id = this.dataBase.getLastInsertId();
			rowData.setId(id);
		}
	}
	
	public void saveItem(VerticalDeflectionRow rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() ||
				rowData.getName() == null || rowData.getName().trim().isEmpty() ||
				(rowData.getYApriori() == null && rowData.getXApriori() == null))
			return;
		
		String sql = "MERGE INTO \"VerticalDeflectionApriori\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), ?, CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"group_id\", \"name\", \"y0\", \"x0\", \"sigma_y0\", \"sigma_x0\", \"enable\") ON \"VerticalDeflectionApriori\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"VerticalDeflectionApriori\".\"group_id\"  = \"vals\".\"group_id\", "
				+ "\"VerticalDeflectionApriori\".\"name\"      = \"vals\".\"name\", "
				+ "\"VerticalDeflectionApriori\".\"y0\"        = \"vals\".\"y0\", "
				+ "\"VerticalDeflectionApriori\".\"x0\"        = \"vals\".\"x0\", "
				+ "\"VerticalDeflectionApriori\".\"sigma_y0\"  = \"vals\".\"sigma_y0\", "
				+ "\"VerticalDeflectionApriori\".\"sigma_x0\"  = \"vals\".\"sigma_x0\", "
				+ "\"VerticalDeflectionApriori\".\"enable\"    = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"y0\", "
				+ "\"vals\".\"x0\", "
				+ "\"vals\".\"sigma_y0\", "
				+ "\"vals\".\"sigma_x0\", "
				+ "\"vals\".\"enable\" ";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (rowData.getId() < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, rowData.getId());
		stmt.setInt(idx++,     rowData.getGroupId());

		stmt.setString(idx++,  rowData.getName());

		stmt.setDouble(idx++,  rowData.getYApriori() == null ? 0.0 : rowData.getYApriori());
		stmt.setDouble(idx++,  rowData.getXApriori() == null ? 0.0 : rowData.getXApriori());

		stmt.setDouble(idx++,  rowData.getSigmaYapriori() == null ? 0.0 : rowData.getSigmaYapriori());
		stmt.setDouble(idx++,  rowData.getSigmaXapriori() == null ? 0.0 : rowData.getSigmaXapriori());

		stmt.setBoolean(idx++, rowData.isEnable());

		stmt.execute();

		if (rowData.getId() < 0) {
			int id = this.dataBase.getLastInsertId();
			rowData.setId(id);
		}
	}

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveItem(GNSSObservationRow rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || rowData == null || 
				rowData.getStartPointName() == null || rowData.getEndPointName() == null || 
				rowData.getStartPointName().equals(rowData.getEndPointName()) ||
				(rowData.getYApriori() == null && rowData.getXApriori() == null && rowData.getZApriori() == null) ||
				rowData.getStartPointName().trim().isEmpty() || rowData.getEndPointName().trim().isEmpty())
			return;
		
		String sql = "MERGE INTO \"GNSSObservationApriori\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), ?, ?, CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"group_id\", \"start_point_name\", \"end_point_name\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\", \"enable\") ON \"GNSSObservationApriori\".\"id\" = \"vals\".\"id\" " 
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"GNSSObservationApriori\".\"group_id\"         = \"vals\".\"group_id\", "
				+ "\"GNSSObservationApriori\".\"start_point_name\" = \"vals\".\"start_point_name\", "
				+ "\"GNSSObservationApriori\".\"end_point_name\"   = \"vals\".\"end_point_name\", "
				+ "\"GNSSObservationApriori\".\"y0\"               = \"vals\".\"y0\", "
				+ "\"GNSSObservationApriori\".\"x0\"               = \"vals\".\"x0\", "
				+ "\"GNSSObservationApriori\".\"z0\"               = \"vals\".\"z0\", "
				+ "\"GNSSObservationApriori\".\"sigma_y0\"         = \"vals\".\"sigma_y0\", "
				+ "\"GNSSObservationApriori\".\"sigma_x0\"         = \"vals\".\"sigma_x0\", "
				+ "\"GNSSObservationApriori\".\"sigma_z0\"         = \"vals\".\"sigma_z0\", "
				+ "\"GNSSObservationApriori\".\"enable\"           = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"start_point_name\", "
				+ "\"vals\".\"end_point_name\","
				+ "\"vals\".\"y0\", "
				+ "\"vals\".\"x0\", "
				+ "\"vals\".\"z0\", "
				+ "\"vals\".\"sigma_y0\", "
				+ "\"vals\".\"sigma_x0\", "
				+ "\"vals\".\"sigma_z0\", "
				+ "\"vals\".\"enable\"";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (rowData.getId() < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, rowData.getId());

		stmt.setInt(idx++,     rowData.getGroupId());
		stmt.setString(idx++,  rowData.getStartPointName());
		stmt.setString(idx++,  rowData.getEndPointName());

		stmt.setDouble(idx++,  rowData.getYApriori() == null ? 0.0 : rowData.getYApriori());
		stmt.setDouble(idx++,  rowData.getXApriori() == null ? 0.0 : rowData.getXApriori());
		stmt.setDouble(idx++,  rowData.getZApriori() == null ? 0.0 : rowData.getZApriori());

		stmt.setDouble(idx++,  rowData.getSigmaYapriori() == null || rowData.getSigmaYapriori() < 0 ? 0 : rowData.getSigmaYapriori());
		stmt.setDouble(idx++,  rowData.getSigmaXapriori() == null || rowData.getSigmaXapriori() < 0 ? 0 : rowData.getSigmaXapriori());
		stmt.setDouble(idx++,  rowData.getSigmaZapriori() == null || rowData.getSigmaZapriori() < 0 ? 0 : rowData.getSigmaZapriori());

		stmt.setBoolean(idx++, rowData.isEnable());

		stmt.execute();
		
		if (rowData.getId() < 0) {
			int id = this.dataBase.getLastInsertId();
			rowData.setId(id);
		}
	}

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement 
	public void saveItem(PointRow rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() ||
				rowData.getName() == null || rowData.getName().trim().isEmpty() ||
				(rowData.getYApriori() == null && rowData.getXApriori() == null && rowData.getZApriori() == null))
			return;
		
		String sql = "MERGE INTO \"PointApriori\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), ?, ?, CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"group_id\", \"name\", \"code\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\", \"enable\") ON \"PointApriori\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"PointApriori\".\"group_id\"  = \"vals\".\"group_id\", "
				+ "\"PointApriori\".\"name\"      = \"vals\".\"name\", "
				+ "\"PointApriori\".\"code\"      = \"vals\".\"code\", "
				+ "\"PointApriori\".\"y0\"        = \"vals\".\"y0\", "
				+ "\"PointApriori\".\"x0\"        = \"vals\".\"x0\", "
				+ "\"PointApriori\".\"z0\"        = \"vals\".\"z0\", "
				+ "\"PointApriori\".\"sigma_y0\"  = \"vals\".\"sigma_y0\", "
				+ "\"PointApriori\".\"sigma_x0\"  = \"vals\".\"sigma_x0\", "
				+ "\"PointApriori\".\"sigma_z0\"  = \"vals\".\"sigma_z0\", "
				+ "\"PointApriori\".\"enable\"    = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"code\","
				+ "\"vals\".\"y0\", "
				+ "\"vals\".\"x0\", "
				+ "\"vals\".\"z0\", "
				+ "\"vals\".\"sigma_y0\", "
				+ "\"vals\".\"sigma_x0\", "
				+ "\"vals\".\"sigma_z0\", "
				+ "\"vals\".\"enable\" ";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (rowData.getId() < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, rowData.getId());
		stmt.setInt(idx++,     rowData.getGroupId());

		stmt.setString(idx++,  rowData.getName());
		stmt.setString(idx++,  rowData.getCode() == null || rowData.getCode().trim().isEmpty() ? "" : rowData.getCode());

		stmt.setDouble(idx++,  rowData.getYApriori() == null ? 0.0 : rowData.getYApriori());
		stmt.setDouble(idx++,  rowData.getXApriori() == null ? 0.0 : rowData.getXApriori());
		stmt.setDouble(idx++,  rowData.getZApriori() == null ? 0.0 : rowData.getZApriori());

		stmt.setDouble(idx++,  rowData.getSigmaYapriori() == null ? 0.0 : rowData.getSigmaYapriori());
		stmt.setDouble(idx++,  rowData.getSigmaXapriori() == null ? 0.0 : rowData.getSigmaXapriori());
		stmt.setDouble(idx++,  rowData.getSigmaZapriori() == null ? 0.0 : rowData.getSigmaZapriori());

		stmt.setBoolean(idx++, rowData.isEnable());

		stmt.execute();

		if (rowData.getId() < 0) {
			int id = this.dataBase.getLastInsertId();
			rowData.setId(id);
		}
	}

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveItem(CongruenceAnalysisRow rowData) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() ||
				rowData.getNameInControlEpoch() == null || rowData.getNameInReferenceEpoch() == null ||
				rowData.getNameInControlEpoch().trim().isEmpty() || rowData.getNameInReferenceEpoch().trim().isEmpty())
			return;
		
		String sql = "MERGE INTO \"CongruenceAnalysisPointPairApriori\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), ?, ?, CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"group_id\", \"start_point_name\", \"end_point_name\", \"enable\") ON \"CongruenceAnalysisPointPairApriori\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"CongruenceAnalysisPointPairApriori\".\"group_id\"          = \"vals\".\"group_id\", "
				+ "\"CongruenceAnalysisPointPairApriori\".\"start_point_name\"  = \"vals\".\"start_point_name\", "
				+ "\"CongruenceAnalysisPointPairApriori\".\"end_point_name\"    = \"vals\".\"end_point_name\", "
				+ "\"CongruenceAnalysisPointPairApriori\".\"enable\"            = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"start_point_name\", "
				+ "\"vals\".\"end_point_name\", "
				+ "\"vals\".\"enable\" ";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (rowData.getId() < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, rowData.getId());
		stmt.setInt(idx++,     rowData.getGroupId());

		stmt.setString(idx++,  rowData.getNameInReferenceEpoch());
		stmt.setString(idx++,  rowData.getNameInControlEpoch());

		stmt.setBoolean(idx++, rowData.isEnable());

		stmt.execute();

		if (rowData.getId() < 0) {
			int id = this.dataBase.getLastInsertId();
			rowData.setId(id);
		}
	}	

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveGroup(PointTreeItemValue pointTreeItemValue) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"PointGroup\" USING (VALUES "
				+ "(CAST(? AS INT), ?, CAST(? AS INT), CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"id\", \"name\", \"type\", \"dimension\", \"enable\", \"order\") ON \"PointGroup\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"PointGroup\".\"name\"                = \"vals\".\"name\", "
				+ "\"PointGroup\".\"type\"                = \"vals\".\"type\", "
				+ "\"PointGroup\".\"dimension\"           = \"vals\".\"dimension\", "
				+ "\"PointGroup\".\"enable\"              = \"vals\".\"enable\", "
				+ "\"PointGroup\".\"order\"               = \"vals\".\"order\" "
				+ "WHEN NOT MATCHED THEN INSERT "
				+ "(\"id\", \"name\", \"type\", \"dimension\", \"enable\", \"order\") "
				+ "VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"dimension\", "
				+ "\"vals\".\"enable\", "
				+ "\"vals\".\"order\" ";
		
		int groupId        = pointTreeItemValue.getGroupId();
		String name        = pointTreeItemValue.getName().trim();
		int dimension      = pointTreeItemValue.getDimension();
		PointType type     = pointTreeItemValue.getPointType();
		boolean enable     = pointTreeItemValue.isEnable();
		int orderId        = pointTreeItemValue.getOrderId();
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (groupId < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, groupId);

		stmt.setString(idx++,  name);
		stmt.setInt(idx++,     type.getId());
		stmt.setInt(idx++,     dimension);
		stmt.setBoolean(idx++, enable);
		stmt.setInt(idx++,     orderId);
		//stmt.setBoolean(idx++, false); // consider_deflection: not used for insert/update

		stmt.execute();

		// insert new group
		if (groupId < 0) {
			int id = this.dataBase.getLastInsertId();
			pointTreeItemValue.setGroupId(id);

			PointGroupUncertaintyType uncertaintyTypes[] = PointGroupUncertaintyType.values();
			for (PointGroupUncertaintyType uncertaintyType : uncertaintyTypes) {
				this.saveUncertainty(
						uncertaintyType, 
						PointTreeItemValue.getDefaultUncertainty(uncertaintyType), 
						pointTreeItemValue
						);
			}
		}
	}

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveGroup(ObservationTreeItemValue observationTreeItemValue) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"ObservationGroup\" USING (VALUES "
				+ "(CAST(? AS INT), ?, CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"id\", \"name\", \"type\", \"enable\", \"order\") ON \"ObservationGroup\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ObservationGroup\".\"name\"           = \"vals\".\"name\", "
				+ "\"ObservationGroup\".\"type\"           = \"vals\".\"type\", "
				+ "\"ObservationGroup\".\"enable\"         = \"vals\".\"enable\", "
				+ "\"ObservationGroup\".\"order\"          = \"vals\".\"order\" "
				+ "WHEN NOT MATCHED THEN INSERT "
				+ "(\"id\", \"name\", \"type\", \"enable\", \"order\", \"reference_epoch\") "
				+ "VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"enable\", "
				+ "\"vals\".\"order\", "
				+ "DEFAULT"; // reference_epoch type is set to DEFAULT

		int groupId           = observationTreeItemValue.getGroupId();
		String name           = observationTreeItemValue.getName().trim();
		ObservationType type  = observationTreeItemValue.getObservationType();
		boolean enable        = observationTreeItemValue.isEnable();
		int orderId           = observationTreeItemValue.getOrderId();

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (groupId < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, groupId);

		stmt.setString(idx++,  name);
		stmt.setInt(idx++,     type.getId());
		stmt.setBoolean(idx++, enable);
		stmt.setInt(idx++,     orderId);
		
		//stmt.setBoolean(idx++, true); // reference epoch; not used for update/insert

		stmt.execute();

		// insert new group
		if (groupId < 0) {
			groupId = this.dataBase.getLastInsertId();
			observationTreeItemValue.setGroupId(groupId);

			ParameterType parameters[] = ObservationTreeItemValue.getParameterTypes(observationTreeItemValue.getItemType());
			for (ParameterType parameterType : parameters) {
				this.saveAdditionalParameter(
						parameterType, 
						parameterType == ParameterType.ORIENTATION ? true : false, 
								parameterType == ParameterType.SCALE ? 1.0 : 0.0, 
										observationTreeItemValue
						);
			}

			ObservationGroupUncertaintyType uncertaintyTypes[] = ObservationGroupUncertaintyType.values();
			for (ObservationGroupUncertaintyType uncertaintyType : uncertaintyTypes) {
				this.saveUncertainty(
						uncertaintyType, 
						ObservationTreeItemValue.getDefaultUncertainty(observationTreeItemValue.getItemType(), uncertaintyType), 
						observationTreeItemValue
						);
			}
		}
	}

	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveGroup(VerticalDeflectionTreeItemValue verticalDeflectionTreeItemValue) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"VerticalDeflectionGroup\" USING (VALUES "
				+ "(CAST(? AS INT), ?, CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"id\", \"name\", \"type\", \"enable\", \"order\") ON \"VerticalDeflectionGroup\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"VerticalDeflectionGroup\".\"name\"   = \"vals\".\"name\", "
				+ "\"VerticalDeflectionGroup\".\"type\"   = \"vals\".\"type\", "
				+ "\"VerticalDeflectionGroup\".\"enable\" = \"vals\".\"enable\", "
				+ "\"VerticalDeflectionGroup\".\"order\"  = \"vals\".\"order\" "
				+ "WHEN NOT MATCHED THEN INSERT "
				+ "(\"id\", \"name\", \"type\", \"enable\", \"order\") "
				+ "VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"enable\", "
				+ "\"vals\".\"order\"";
		
		int groupId                 = verticalDeflectionTreeItemValue.getGroupId();
		String name                 = verticalDeflectionTreeItemValue.getName().trim();
		VerticalDeflectionType type = verticalDeflectionTreeItemValue.getVerticalDeflectionType();
		boolean enable              = verticalDeflectionTreeItemValue.isEnable();
		int orderId                 = verticalDeflectionTreeItemValue.getOrderId();
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (groupId < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, groupId);

		stmt.setString(idx++,  name);
		stmt.setInt(idx++,     type.getId());
		stmt.setBoolean(idx++, enable);
		stmt.setInt(idx++,     orderId);

		stmt.execute();

		// insert new group
		if (groupId < 0) {
			int id = this.dataBase.getLastInsertId();
			verticalDeflectionTreeItemValue.setGroupId(id);

			VerticalDeflectionGroupUncertaintyType uncertaintyTypes[] = VerticalDeflectionGroupUncertaintyType.values();
			for (VerticalDeflectionGroupUncertaintyType uncertaintyType : uncertaintyTypes) {
				this.saveUncertainty(
						uncertaintyType, 
						VerticalDeflectionTreeItemValue.getDefaultUncertainty(uncertaintyType), 
						verticalDeflectionTreeItemValue
						);
			}
		}
	}
	
	// http://hsqldb.org/doc/2.0/guide/dataaccess-chapt.html#dac_merge_statement
	public void saveGroup(CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"CongruenceAnalysisGroup\" USING (VALUES "
				+ "(CAST(? AS INT), ?, CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"id\", \"name\", \"dimension\", \"enable\", \"order\") ON \"CongruenceAnalysisGroup\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"CongruenceAnalysisGroup\".\"name\"   = \"vals\".\"name\", "
				+ "\"CongruenceAnalysisGroup\".\"enable\" = \"vals\".\"enable\", "
				+ "\"CongruenceAnalysisGroup\".\"order\"  = \"vals\".\"order\" "
				+ "WHEN NOT MATCHED THEN INSERT "
				+ "(\"id\", \"name\", \"dimension\", \"enable\", \"order\") "
				+ "VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"name\", "
				+ "\"vals\".\"dimension\", "
				+ "\"vals\".\"enable\", "
				+ "\"vals\".\"order\"";

		int groupId    = congruenceAnalysisTreeItemValue.getGroupId();
		String name    = congruenceAnalysisTreeItemValue.getName().trim();
		int dimension  = congruenceAnalysisTreeItemValue.getDimension();
		boolean enable = congruenceAnalysisTreeItemValue.isEnable();
		int orderId    = congruenceAnalysisTreeItemValue.getOrderId();
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		if (groupId < 0)
			stmt.setNull(idx++, Types.INTEGER);
		// Update existing item
		else
			stmt.setInt(idx++, groupId);

		stmt.setString(idx++,  name);
		stmt.setInt(idx++,  dimension);
		stmt.setBoolean(idx++, enable);
		stmt.setInt(idx++,  orderId);
		
		stmt.execute();

		// insert new group
		if (groupId < 0) {
			int id = this.dataBase.getLastInsertId();
			congruenceAnalysisTreeItemValue.setGroupId(id);

			RestrictionType restrictions[] = CongruenceAnalysisTreeItemValue.getRestrictionTypes(congruenceAnalysisTreeItemValue.getItemType());
			for (RestrictionType restrictionType : restrictions) {
				switch(restrictionType) {
				case IDENT_SCALES_XY:
				case IDENT_SCALES_XZ:
				case IDENT_SCALES_YZ:
					this.saveStrainParameter(restrictionType, true, congruenceAnalysisTreeItemValue);
					break;
				default:
					this.saveStrainParameter(restrictionType, false, congruenceAnalysisTreeItemValue);
					break;
				}
			}
		}
	}

	public void removeGroup(TreeItemValue treeItemValue) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;

		String sql = null;
		int id = -1;
		if (treeItemValue instanceof PointTreeItemValue) {
			sql = "DELETE FROM \"PointGroup\" WHERE \"id\" = ? LIMIT 1";
			id = ((PointTreeItemValue)treeItemValue).getGroupId();
		}
		else if (treeItemValue instanceof ObservationTreeItemValue) {
			sql = "DELETE FROM \"ObservationGroup\" WHERE \"id\" = ? LIMIT 1";
			id = ((ObservationTreeItemValue)treeItemValue).getGroupId();
		}
		else if (treeItemValue instanceof CongruenceAnalysisTreeItemValue) {
			sql = "DELETE FROM \"CongruenceAnalysisGroup\" WHERE \"id\" = ? LIMIT 1";
			id = ((CongruenceAnalysisTreeItemValue)treeItemValue).getGroupId();
		}
		else if (treeItemValue instanceof VerticalDeflectionTreeItemValue) {
			sql = "DELETE FROM \"VerticalDeflectionGroup\" WHERE \"id\" = ? LIMIT 1";
			id = ((VerticalDeflectionTreeItemValue)treeItemValue).getGroupId();
		}
		else {
			System.err.println(this.getClass().getSimpleName() + " : Error, cannot remove item, because type is unknwon " + treeItemValue.getItemType());
			return;
		}

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, id);

		stmt.execute();
	}

	public void saveEpoch(boolean referenceEpoch, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder inArrayValues = new StringBuilder("?");
		for (int i=1; i<selectedObservationItemValues.length; i++)
			inArrayValues.append(",?");

		String sql = "UPDATE \"ObservationGroup\" SET \"reference_epoch\" = ? WHERE \"id\" IN (" + inArrayValues + ")";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setBoolean(idx++, referenceEpoch);
		for (int i=0; i<selectedObservationItemValues.length; i++) {
			int groupId = selectedObservationItemValues[i].getGroupId();
			stmt.setInt(idx++, groupId);
		}

		stmt.execute();
	}

	public void saveUncertainty(PointGroupUncertaintyType uncertaintyType, double value, PointTreeItemValue... selectedPointItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder values = new StringBuilder("(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");
		for (int i=1; i<selectedPointItemValues.length; i++)
			values.append(",(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");

		String sql = "MERGE INTO \"PointGroupUncertainty\" USING (VALUES "
				+ values
				+ ") AS \"vals\" (\"group_id\", \"type\",\"value\") ON \"PointGroupUncertainty\".\"group_id\" = \"vals\".\"group_id\" AND \"PointGroupUncertainty\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"PointGroupUncertainty\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value\" ";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedPointItemValues.length; i++) {
			int groupId = selectedPointItemValues[i].getGroupId();
			stmt.setInt(idx++, groupId);
			stmt.setInt(idx++, uncertaintyType.getId());
			stmt.setDouble(idx++, value);
		}

		stmt.execute();
	}
	
	public void saveUncertainty(VerticalDeflectionGroupUncertaintyType uncertaintyType, double value, VerticalDeflectionTreeItemValue... selectedVerticalDeflectionItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder values = new StringBuilder("(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");
		for (int i=1; i<selectedVerticalDeflectionItemValues.length; i++)
			values.append(",(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");

		String sql = "MERGE INTO \"VerticalDeflectionGroupUncertainty\" USING (VALUES "
				+ values
				+ ") AS \"vals\" (\"group_id\", \"type\",\"value\") ON \"VerticalDeflectionGroupUncertainty\".\"group_id\" = \"vals\".\"group_id\" AND \"VerticalDeflectionGroupUncertainty\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"VerticalDeflectionGroupUncertainty\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value\" ";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedVerticalDeflectionItemValues.length; i++) {
			int groupId = selectedVerticalDeflectionItemValues[i].getGroupId();
			stmt.setInt(idx++, groupId);
			stmt.setInt(idx++, uncertaintyType.getId());
			stmt.setDouble(idx++, value);
		}

		stmt.execute();
	}

	public void saveUncertainty(ObservationGroupUncertaintyType uncertaintyType, double value, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder values = new StringBuilder("(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");
		for (int i=1; i<selectedObservationItemValues.length; i++)
			values.append(",(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE))");

		String sql = "MERGE INTO \"ObservationGroupUncertainty\" USING (VALUES "
				+ values
				+ ") AS \"vals\" (\"group_id\", \"type\",\"value\") ON \"ObservationGroupUncertainty\".\"group_id\" = \"vals\".\"group_id\" AND \"ObservationGroupUncertainty\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ObservationGroupUncertainty\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value\" ";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedObservationItemValues.length; i++) {
			int groupId = selectedObservationItemValues[i].getGroupId();
			if (groupId < 0)
				continue;
			stmt.setInt(idx++, groupId);
			stmt.setInt(idx++, uncertaintyType.getId());
			stmt.setDouble(idx++, value);
		}

		stmt.execute();
	}

	public void saveStrainParameter(RestrictionType parameterType, boolean enable, CongruenceAnalysisTreeItemValue... selectedCongruenceAnalysisItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder values = new StringBuilder("(CAST(? AS INT), CAST(? AS INT), CAST(? AS BOOLEAN))");
		for (int i=1; i<selectedCongruenceAnalysisItemValues.length; i++)
			values.append(",(CAST(? AS INT), CAST(? AS INT), CAST(? AS BOOLEAN))");

		String sql = "MERGE INTO \"CongruenceAnalysisStrainParameterRestriction\" USING (VALUES "
				+ values
				+ ") AS \"vals\" (\"group_id\",\"type\",\"enable\") ON \"CongruenceAnalysisStrainParameterRestriction\".\"group_id\" = \"vals\".\"group_id\" AND \"CongruenceAnalysisStrainParameterRestriction\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"CongruenceAnalysisStrainParameterRestriction\".\"enable\" = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"enable\" ";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedCongruenceAnalysisItemValues.length; i++) {
			int groupId = selectedCongruenceAnalysisItemValues[i].getGroupId();
			if (groupId < 0)
				continue;
			stmt.setInt(idx++, groupId);
			stmt.setInt(idx++, parameterType.getId());
			stmt.setBoolean(idx++, enable);
		}

		stmt.execute();
	}

	public void saveAdditionalParameter(ParameterType parameterType, boolean enable, double value, ObservationTreeItemValue... selectedObservationItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		StringBuilder values = new StringBuilder("(CAST(? AS INT), CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS BOOLEAN))");
		for (int i=1; i<selectedObservationItemValues.length; i++)
			values.append(",(CAST(? AS INT), CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS BOOLEAN))");

		String sql = "MERGE INTO \"AdditionalParameterApriori\" USING (VALUES "
				+ values
				+ ") AS \"vals\" (\"id\",\"group_id\",\"type\",\"value_0\",\"enable\") ON \"AdditionalParameterApriori\".\"group_id\" = \"vals\".\"group_id\" AND \"AdditionalParameterApriori\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"AdditionalParameterApriori\".\"value_0\" = \"vals\".\"value_0\", "
				+ "\"AdditionalParameterApriori\".\"enable\" = \"vals\".\"enable\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"group_id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value_0\", "
				+ "\"vals\".\"enable\" ";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		for (int i=0; i<selectedObservationItemValues.length; i++) {
			int groupId = selectedObservationItemValues[i].getGroupId();
			if (groupId < 0)
				continue;
			stmt.setNull(idx++, Types.INTEGER); // ID, is not used for insert/update
			stmt.setInt(idx++, groupId);
			stmt.setInt(idx++, parameterType.getId());
			stmt.setDouble(idx++, value);
			stmt.setBoolean(idx++, enable);
		}

		stmt.execute();
	}
	
	public String getNextValidVerticalDeflectionName(String name) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || name == null || name.trim().isEmpty())
			return null;


		String regexp = "^\\Q"+name+"\\E\\s+\\((\\d+)\\)$";
		String sql = "SELECT MAX(CAST(REGEXP_REPLACE(\"name\", ?, '$1') AS INTEGER)) AS \"cnt\" FROM \"VerticalDeflectionApriori\" WHERE REGEXP_MATCHES(\"name\", ?)";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setString(1, regexp);
		stmt.setString(2, regexp);

		int cnt = 0;
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt("cnt");
			if (rs.wasNull())
				cnt = 0;
		}
		return String.format(Locale.ENGLISH, "%s (%d)", name, cnt + 1);				
	}

	public String getNextValidPointName(String name) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || name == null || name.trim().isEmpty())
			return null;

		String regexp = "^\\Q"+name+"\\E\\s+\\((\\d+)\\)$";
		String sql = "SELECT MAX(CAST(REGEXP_REPLACE(\"name\", ?, '$1') AS INTEGER)) AS \"cnt\" FROM \"PointApriori\" WHERE REGEXP_MATCHES(\"name\", ?)";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setString(1, regexp);
		stmt.setString(2, regexp);

		int cnt = 0;
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt("cnt");
			if (rs.wasNull())
				cnt = 0;
		}
		return String.format(Locale.ENGLISH, "%s (%d)", name, cnt + 1);				
	}

	public String[] getNextValidPointNexusNames(int groupId, String firstPointName, String secondPointName) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || 
				groupId < 0 || 
				firstPointName == null || firstPointName.trim().isEmpty() || 
				secondPointName == null || secondPointName.trim().isEmpty())
			return new String[] {firstPointName, secondPointName};

		String regexp = "^\\Q"+secondPointName+"\\E\\s+\\((\\d+)\\)$";
		String sql = "SELECT MAX(CAST(REGEXP_REPLACE(\"end_point_name\", ?, '$1') AS INTEGER)) AS \"cnt\" FROM \"CongruenceAnalysisPointPairApriori\" WHERE \"group_id\" = ? AND \"start_point_name\" = ? AND REGEXP_MATCHES(\"end_point_name\", ?)";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setString(1, regexp);
		stmt.setInt(2, groupId);
		stmt.setString(3, firstPointName);
		stmt.setString(4, regexp);

		int cnt = 0;
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt("cnt");
			if (rs.wasNull())
				cnt = 0;
		}
		return new String[] {firstPointName, String.format(Locale.ENGLISH, "%s (%d)", secondPointName, cnt + 1)};
	}
	
	/** Dialogs **/
	
	public void checkNumberOfObersvationsPerUnknownParameter() throws SQLException, PointTypeMismatchException, UnderDeterminedPointException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sqlCountPointGroups  = "SELECT COUNT(\"PointApriori\".\"id\") AS \"number_of_points\", \"PointGroup\".\"type\" FROM \"PointApriori\" JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" WHERE \"PointApriori\".\"enable\" = TRUE AND \"PointGroup\".\"enable\" = TRUE AND \"PointGroup\".\"type\" IN (?,?,?) GROUP BY \"PointGroup\".\"type\" ORDER BY  \"PointGroup\".\"type\" ASC";
		String sqlCountObservations = "SELECT \"UnionTable\".\"name\", SUM(\"UnionTable\".\"number_of_observations\") AS \"number_of_observations\", \"UnionTable\".\"dimension\" FROM ( " +

										// Alle Punkte abfragen, die bestimmt werden muessen
										"SELECT \"name\", 0 AS \"number_of_observations\", \"dimension\" FROM \"PointApriori\" " +
										"JOIN \"PointGroup\" ON \"PointGroup\".\"id\" = \"PointApriori\".\"group_id\" " +
										"WHERE \"PointGroup\".\"enable\" = TRUE AND \"PointApriori\".\"enable\" = TRUE " +
										"AND \"PointGroup\".\"type\" IN (?, ?) " +

										"UNION ALL " +

										// Alle Standpunkte abfragen und terr. Beobachtungen zaehlen
										"SELECT \"start_point_name\" AS \"name\", COUNT(\"start_point_name\") AS \"number_of_observations\", \"StartPointGroup\".\"dimension\" FROM \"ObservationApriori\" " +
										"JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " +

										"JOIN \"PointApriori\" AS \"StartPointApriori\" ON \"ObservationApriori\".\"start_point_name\" = \"StartPointApriori\".\"name\" " +
										"JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"ObservationApriori\".\"end_point_name\" = \"EndPointApriori\".\"name\" " +

										"JOIN \"PointGroup\" AS \"StartPointGroup\" ON \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" " +
										"JOIN \"PointGroup\" AS \"EndPointGroup\" ON \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" " +

										"WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE " +
										"AND \"StartPointGroup\".\"type\" IN (?, ?) " +

										"GROUP BY \"start_point_name\", \"StartPointGroup\".\"dimension\" " +

										"UNION ALL " +

										// Alle Zielpunkte abfragen und terr. Beobachtungen zaehlen
										"SELECT \"end_point_name\" AS \"name\", COUNT(\"end_point_name\") AS \"number_of_observations\", \"EndPointGroup\".\"dimension\" FROM \"ObservationApriori\" " +
										"JOIN \"ObservationGroup\" ON \"ObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " +

										"JOIN \"PointApriori\" AS \"StartPointApriori\" ON \"ObservationApriori\".\"start_point_name\" = \"StartPointApriori\".\"name\" " +
										"JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"ObservationApriori\".\"end_point_name\" = \"EndPointApriori\".\"name\" " +

										"JOIN \"PointGroup\" AS \"StartPointGroup\" ON \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" " +
										"JOIN \"PointGroup\" AS \"EndPointGroup\" ON \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" " +

										"WHERE \"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE " +
										"AND \"EndPointGroup\".\"type\" IN (?, ?) " +

										"GROUP BY \"end_point_name\", \"EndPointGroup\".\"dimension\" " +

										"UNION ALL " +

										// Alle Standpunkte abfragen und GNSS-Beobachtungen zaehlen
										"SELECT \"start_point_name\" AS \"name\", CAST(CASE WHEN \"ObservationGroup\".\"type\" = ? THEN 3*COUNT(\"start_point_name\") WHEN \"ObservationGroup\".\"type\" = ? THEN 2*COUNT(\"start_point_name\") ELSE COUNT(\"start_point_name\") END AS INTEGER) AS \"number_of_observations\", \"StartPointGroup\".\"dimension\" FROM \"GNSSObservationApriori\" " +
										"JOIN \"ObservationGroup\" ON \"GNSSObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " +

										"JOIN \"PointApriori\" AS \"StartPointApriori\" ON \"GNSSObservationApriori\".\"start_point_name\" = \"StartPointApriori\".\"name\" " +
										"JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"GNSSObservationApriori\".\"end_point_name\" = \"EndPointApriori\".\"name\" " +

										"JOIN \"PointGroup\" AS \"StartPointGroup\" ON \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" " +
										"JOIN \"PointGroup\" AS \"EndPointGroup\" ON \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" " +

										"WHERE \"GNSSObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE " +
										"AND \"StartPointGroup\".\"type\" IN (?, ?) " +

										"GROUP BY \"start_point_name\", \"StartPointGroup\".\"dimension\", \"ObservationGroup\".\"type\" " +

										"UNION ALL " +

										// Alle Zielpunkte abfragen und GNSS-Beobachtungen zaehlen										
										"SELECT \"end_point_name\" AS \"name\", CAST(CASE WHEN \"ObservationGroup\".\"type\" = ? THEN 3*COUNT(\"end_point_name\") WHEN \"ObservationGroup\".\"type\" = ? THEN 2*COUNT(\"end_point_name\") ELSE COUNT(\"end_point_name\") END AS INTEGER) AS \"number_of_observations\", \"EndPointGroup\".\"dimension\" FROM \"GNSSObservationApriori\" " +
										"JOIN \"ObservationGroup\" ON \"GNSSObservationApriori\".\"group_id\" = \"ObservationGroup\".\"id\" " +

										"JOIN \"PointApriori\" AS \"StartPointApriori\" ON \"GNSSObservationApriori\".\"start_point_name\" = \"StartPointApriori\".\"name\" " +
										"JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"GNSSObservationApriori\".\"end_point_name\" = \"EndPointApriori\".\"name\" " +

										"JOIN \"PointGroup\" AS \"StartPointGroup\" ON \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" " +
										"JOIN \"PointGroup\" AS \"EndPointGroup\" ON \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" " +

										"WHERE \"GNSSObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE " +
										"AND \"EndPointGroup\".\"type\" IN (?, ?) " +

										"GROUP BY \"end_point_name\", \"EndPointGroup\".\"dimension\", \"ObservationGroup\".\"type\" " +

										") AS \"UnionTable\" " +

										"GROUP BY \"UnionTable\".\"name\", \"UnionTable\".\"dimension\" HAVING SUM(\"UnionTable\".\"number_of_observations\") < \"UnionTable\".\"dimension\" ORDER BY \"number_of_observations\", \"UnionTable\".\"name\" ASC LIMIT 1";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sqlCountPointGroups);
		stmt.setInt(1, PointType.DATUM_POINT.getId());
		stmt.setInt(2, PointType.REFERENCE_POINT.getId());
		stmt.setInt(3, PointType.STOCHASTIC_POINT.getId());

		ResultSet rs = stmt.executeQuery();
		int numberOfDatumPoints = 0, numberOfReferencePoints = 0;

		// Gibt es neben Datumspunkten auch Fest/Anschlusspunkte?
		while (rs.next()) {
			int cnt  = rs.getInt("number_of_points");
			PointType type = PointType.getEnumByValue(rs.getInt("type"));
			if (type == PointType.DATUM_POINT)
				numberOfDatumPoints += cnt;
			else
				numberOfReferencePoints += cnt;
		}

		if (numberOfReferencePoints > 0 && numberOfDatumPoints > 0) {
			throw new PointTypeMismatchException("Error, the project contains reference points as well as datum points.");
		}

		// Gibt es Punkte, die weniger Beobachtungen haben als deren 
		// Dimension erfordert? Pruefe nur Neu- und Datumspunkte.

		stmt = this.dataBase.getPreparedStatement(sqlCountObservations);
		int idx = 1;
		// Alle Punkte abfragen, die bestimmt werden muessen
		stmt.setInt(idx++, PointType.DATUM_POINT.getId());
		stmt.setInt(idx++, PointType.NEW_POINT.getId());

		// Alle Standpunkte abfragen und terr. Beobachtungen zaehlen
		stmt.setInt(idx++, PointType.DATUM_POINT.getId());
		stmt.setInt(idx++, PointType.NEW_POINT.getId());

		// Alle Zielpunkte abfragen und terr. Beobachtungen zaehlen
		stmt.setInt(idx++, PointType.DATUM_POINT.getId());
		stmt.setInt(idx++, PointType.NEW_POINT.getId());

		// Alle Standpunkte abfragen und GNSS-Beobachtungen zaehlen
		stmt.setInt(idx++, ObservationType.GNSS3D.getId());
		stmt.setInt(idx++, ObservationType.GNSS2D.getId());
		stmt.setInt(idx++, PointType.DATUM_POINT.getId());
		stmt.setInt(idx++, PointType.NEW_POINT.getId());

		// Alle Zielpunkte abfragen und GNSS-Beobachtungen zaehlen
		stmt.setInt(idx++, ObservationType.GNSS3D.getId());
		stmt.setInt(idx++, ObservationType.GNSS2D.getId());
		stmt.setInt(idx++, PointType.DATUM_POINT.getId());
		stmt.setInt(idx++, PointType.NEW_POINT.getId());

		rs = stmt.executeQuery();

		if (rs.next()) {
			String pointName = rs.getString("name");
			int dimension    = rs.getInt("dimension");
			int numberOfObservations = rs.getInt("number_of_observations");

			throw new UnderDeterminedPointException("Error, the point " + pointName + " of dimension " + dimension + " has not enough observations to be estimable.", pointName, dimension, numberOfObservations);
		}
	}
	
	public List<TerrestrialObservationRow> getCongruentPoints(double snapDistance, boolean include1D, boolean include2D, boolean include3D) throws SQLException {
		List<TerrestrialObservationRow> rows = new ArrayList<TerrestrialObservationRow>();

		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return rows;
		
		if (!include1D && !include2D && !include3D)
			return rows;

		String sql = "SELECT "
				+ "\"point_prio_1\".\"name\" AS \"point_name_1\", \"point_prio_2\".\"name\" AS \"point_name_2\", "
				+ "SQRT( POWER(\"point_post_1\".\"x\" - \"point_post_2\".\"x\",2) + POWER(\"point_post_1\".\"y\" - \"point_post_2\".\"y\",2) + POWER(\"point_post_1\".\"z\" - \"point_post_2\".\"z\", 2) ) AS \"slope_distance\" "
				+ "FROM \"PointApriori\" AS \"point_prio_1\", "
				+ "\"PointApriori\" AS \"point_prio_2\" "
				+ "JOIN \"PointAposteriori\" AS \"point_post_1\" ON "
				+ "\"point_prio_1\".\"id\" = \"point_post_1\".\"id\" AND \"point_prio_1\".\"enable\" = TRUE "
				+ "JOIN \"PointAposteriori\" AS \"point_post_2\" ON "
				+ "\"point_prio_2\".\"id\" = \"point_post_2\".\"id\" AND \"point_prio_2\".\"enable\" = TRUE "
				+ "JOIN \"PointGroup\" AS \"group_1\" ON "
				+ "\"point_prio_1\".\"group_id\" = \"group_1\".\"id\" AND \"group_1\".\"enable\" = TRUE "
				+ "JOIN \"PointGroup\" AS \"group_2\" ON "
				+ "\"point_prio_2\".\"group_id\" = \"group_2\".\"id\" AND \"group_2\".\"enable\" = TRUE AND \"group_2\".\"dimension\" = \"group_1\".\"dimension\" AND \"group_1\".\"dimension\" IN (?,?,?)"
				+ "WHERE \"point_prio_1\".\"id\" < \"point_prio_2\".\"id\" AND "
				+ "SQRT( POWER(\"point_post_1\".\"x\" - \"point_post_2\".\"x\", 2) + POWER(\"point_post_1\".\"y\" - \"point_post_2\".\"y\", 2) + POWER(\"point_post_1\".\"z\" - \"point_post_2\".\"z\", 2) ) < ? "
				+ "ORDER BY \"slope_distance\" ASC LIMIT 15";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, include1D ? 1 : -1);
		stmt.setInt(idx++, include2D ? 2 : -2);
		stmt.setInt(idx++, include3D ? 3 : -3);
		stmt.setDouble(idx++, snapDistance);
		
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			String pointNameA = rs.getString("point_name_1");
			String pointNameB = rs.getString("point_name_2");
			double distance   = rs.getDouble("slope_distance");

			TerrestrialObservationRow row = new TerrestrialObservationRow();
			row.setStartPointName(pointNameA);
			row.setEndPointName(pointNameB);
			row.setDistanceApriori(distance);
			row.setValueApriori(distance);
			row.setValueAposteriori(distance);

			rows.add(row);	
		}

		return rows;
	}
	
	public void saveFormatterOption(FormatterOption option) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"FormatterOption\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"type\",\"unit\",\"digits\") ON \"FormatterOption\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"FormatterOption\".\"unit\"   = \"vals\".\"unit\", "
				+ "\"FormatterOption\".\"digits\" = \"vals\".\"digits\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"unit\", "
				+ "\"vals\".\"digits\" ";
		
		CellValueType type = option.getType();
		Unit unit   = option.getUnit();
		int digits  = option.getFractionDigits();
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, type.getId());
		if (unit == null)
			stmt.setNull(idx++, Types.SMALLINT);
		else
			stmt.setInt(idx++, unit.getType().getId());
		stmt.setInt(idx++, digits);
		stmt.execute();
	}
	
	public void save(TestStatisticDefinition testStatistic) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"TestStatisticDefinition\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"type\",\"probability_value\",\"power_of_test\",\"familywise_error_rate\") ON \"TestStatisticDefinition\".\"id\" = \"vals\".\"id\" AND \"TestStatisticDefinition\".\"id\" = 1 "
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
				+ "\"vals\".\"familywise_error_rate\" ";
		
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, 1);
		
		if (testStatistic.getTestStatisticType() == null)
			stmt.setNull(idx++, Types.SMALLINT);
		else
			stmt.setInt(idx++, testStatistic.getTestStatisticType().getId());
		
		stmt.setDouble(idx++, testStatistic.getProbabilityValue());
		stmt.setDouble(idx++, testStatistic.getPowerOfTest());
		
		stmt.setBoolean(idx++, testStatistic.isFamilywiseErrorRate());
		stmt.execute();
	}

	public TestStatisticDefinition getTestStatisticDefinition() throws SQLException {
		if (this.hasDatabase() && this.dataBase.isOpen()) {

			String sql = "SELECT "
					+ "\"type\",\"probability_value\",\"power_of_test\",\"familywise_error_rate\" "
					+ "FROM \"TestStatisticDefinition\" "
					+ "WHERE \"id\" = 1 LIMIT 1";

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				TestStatisticType testStatisticType = TestStatisticType.getEnumByValue(rs.getInt("type"));
				if (testStatisticType != null) {
					double probabilityValue = rs.getDouble("probability_value");
					double powerOfTest      = rs.getDouble("power_of_test");
					boolean familywiseErrorRate = rs.getBoolean("familywise_error_rate");

					return new TestStatisticDefinition(testStatisticType, probabilityValue, powerOfTest, familywiseErrorRate);
				}
			}
		}
		return new TestStatisticDefinition();
	}
	
	public void save(Reduction reduction) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"ReductionDefinition\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"id\", \"projection_type\", \"reference_height\", \"earth_radius\") ON \"ReductionDefinition\".\"id\" = \"vals\".\"id\" AND \"ReductionDefinition\".\"id\" = 1 "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ReductionDefinition\".\"projection_type\"  = \"vals\".\"projection_type\", "
				+ "\"ReductionDefinition\".\"reference_height\" = \"vals\".\"reference_height\", "
				+ "\"ReductionDefinition\".\"earth_radius\"     = \"vals\".\"earth_radius\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"projection_type\", "
				+ "\"vals\".\"reference_height\", "
				+ "\"vals\".\"earth_radius\" ";
		
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, 1);
		
		stmt.setInt(idx++, reduction.getProjectionType().getId());
		stmt.setDouble(idx++, reduction.getReferenceHeight());
		stmt.setDouble(idx++, reduction.getEarthRadius());
		stmt.execute();

		this.clearReductionTasks();
		this.saveReductionTasks(reduction);
	}
	
	private void clearReductionTasks() throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;

		PreparedStatement stmt = this.dataBase.getPreparedStatement("DELETE FROM \"ReductionTask\" WHERE \"reduction_id\" = 1");
		stmt.execute();
	}
	
	private void saveReductionTasks(Reduction reduction) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;

		boolean hasBatch = false;

		String sql = "INSERT INTO \"ReductionTask\" "
				+ "(\"reduction_id\", \"type\") "
				+ "VALUES (?, ?) ";
		
		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

			for (ReductionTaskType type : ReductionTaskType.values()) {
				if (!reduction.applyReductionTask(type))
					continue;

				int idx = 1;
				
				stmt.setInt(idx++, 1);
				stmt.setInt(idx++, type.getId());

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
	
	public double getAverageThreshold(ObservationType type) throws SQLException {
		double value = 0;
		if (this.hasDatabase() && this.dataBase.isOpen()) {

			String sql = "SELECT \"value\" "
					+ "FROM \"AverageThreshold\" "
					+ "WHERE \"type\" = ? LIMIT 1";

			int idx = 1;
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			stmt.setInt(idx++, type.getId());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) 
				value = rs.getDouble("value");
		}
		return value > 0 ? value : DefaultAverageThreshold.getThreshold(type); 
	}
	
	public void save(ObservationType type, double threshold) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"AverageThreshold\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"type\",\"value\") "
				+ "ON \"AverageThreshold\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"AverageThreshold\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"value\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, type.getId());
		stmt.setDouble(idx++, threshold > 0 ? threshold : DefaultAverageThreshold.getThreshold(type));
		stmt.execute();
	}
	
	public Reduction getReductionDefinition() throws SQLException {
		Reduction reductions = new Reduction();

		if (this.hasDatabase() && this.dataBase.isOpen()) {
			String sql = "SELECT "
					+ "\"projection_type\", \"reference_height\", \"earth_radius\", \"type\" AS \"task_type\" "
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
				double referenceHeight        = rs.getDouble("reference_height");
				double earthRadius            = rs.getDouble("earth_radius");

				reductions.setProjectionType(projectionType);
				reductions.setReferenceHeight(referenceHeight);
				reductions.setEarthRadius(earthRadius);
	
				if (hasTaskType) {
					ReductionTaskType taskType = ReductionTaskType.getEnumByValue(taskTypeId);
					reductions.addReductionTaskType(taskType);	
				}
			}
		}
		return reductions;
	}
		
	public void save(boolean userDefined, RankDefect rankDefect) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		String sql = "MERGE INTO \"RankDefect\" USING (VALUES "
				+ "(CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\",\"user_defined\",\"ty\",\"tx\",\"tz\",\"ry\",\"rx\",\"rz\",\"sy\",\"sx\",\"sz\",\"my\",\"mx\",\"mz\",\"mxy\",\"mxyz\") "
				+ "ON \"RankDefect\".\"id\" = \"vals\".\"id\" AND \"RankDefect\".\"id\" = 1 "
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
				+ "\"vals\".\"mxyz\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, 1);
		stmt.setBoolean(idx++, userDefined);
		
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
	
	public RankDefect getRankDefectDefinition() throws SQLException {
		RankDefect rankDefect = new RankDefect();

		if (this.hasDatabase() && this.dataBase.isOpen()) {

			String sql = "SELECT "
					+ "\"user_defined\",\"ty\",\"tx\",\"tz\",\"ry\",\"rx\",\"rz\",\"sy\",\"sx\",\"sz\",\"my\",\"mx\",\"mz\",\"mxy\",\"mxyz\" "
					+ "FROM \"RankDefect\" "
					+ "WHERE \"id\" = 1 LIMIT 1";

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				if (rs.getBoolean("user_defined")) {
					if (rs.getBoolean("ty"))
						rankDefect.setTranslationYDefectType(DefectType.FREE);

					if (rs.getBoolean("tx"))
						rankDefect.setTranslationXDefectType(DefectType.FREE);

					if (rs.getBoolean("tz"))
						rankDefect.setTranslationZDefectType(DefectType.FREE);

					if (rs.getBoolean("ry"))
						rankDefect.setRotationYDefectType(DefectType.FREE);

					if (rs.getBoolean("rx") )
						rankDefect.setRotationXDefectType(DefectType.FREE);

					if (rs.getBoolean("rz"))
						rankDefect.setRotationZDefectType(DefectType.FREE);

					if (rs.getBoolean("sy"))
						rankDefect.setShearYDefectType(DefectType.FREE);

					if (rs.getBoolean("sx"))
						rankDefect.setShearXDefectType(DefectType.FREE);

					if (rs.getBoolean("sz"))
						rankDefect.setShearZDefectType(DefectType.FREE);

					if (rs.getBoolean("my"))
						rankDefect.setScaleYDefectType(DefectType.FREE);

					if (rs.getBoolean("mx"))
						rankDefect.setScaleXDefectType(DefectType.FREE);

					if (rs.getBoolean("mz"))
						rankDefect.setScaleZDefectType(DefectType.FREE);

					if (rs.getBoolean("mxy"))
						rankDefect.setScaleXYDefectType(DefectType.FREE);

					if (rs.getBoolean("mxyz"))
						rankDefect.setScaleXYZDefectType(DefectType.FREE);
				}
			}
		}
		return rankDefect;
	}
	
	public void load(LeastSquaresSettings settings) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
	
		String sql = "SELECT "
				+ "\"type\", \"number_of_iterations\", \"robust_estimation_limit\", "
				+ "\"number_of_principal_components\", \"apply_variance_of_unit_weight\", "
				+ "\"estimate_direction_set_orientation_approximation\", "
				+ "\"congruence_analysis\", \"export_covariance_matrix\", "
				+ "\"scaling\", \"damping\", \"weight_zero\" "
				+ "FROM \"AdjustmentDefinition\" "
				+ "JOIN \"UnscentedTransformation\" "
				+ "ON \"AdjustmentDefinition\".\"id\" = \"UnscentedTransformation\".\"id\" "
				+ "WHERE \"id\" = 1 LIMIT 1";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			EstimationType type = EstimationType.getEnumByValue(rs.getInt("type"));
			if (type != null) {
				settings.setEstimationType(type);
				settings.setIteration(rs.getInt("number_of_iterations"));
				settings.setRobustEstimationLimit(rs.getDouble("robust_estimation_limit"));
				settings.setPrincipalComponents(rs.getInt("number_of_principal_components"));
				settings.setApplyVarianceOfUnitWeight(rs.getBoolean("apply_variance_of_unit_weight"));
				settings.setOrientation(rs.getBoolean("estimate_direction_set_orientation_approximation"));
				settings.setCongruenceAnalysis(rs.getBoolean("congruence_analysis"));
				settings.setExportCovarianceMatrix(rs.getBoolean("export_covariance_matrix"));
				
				settings.setScalingParameterAlphaUT(rs.getDouble("scaling"));
				settings.setDampingParameterBetaUT(rs.getDouble("damping"));
				settings.setWeightZero(rs.getDouble("weight_zero"));
			}
		}
	}
	
	public void save(LeastSquaresSettings settings) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
	
		String sql = "MERGE INTO \"AdjustmentDefinition\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"type\", \"number_of_iterations\", \"robust_estimation_limit\", \"number_of_principal_components\", \"apply_variance_of_unit_weight\", \"estimate_direction_set_orientation_approximation\", \"congruence_analysis\", \"export_covariance_matrix\") ON \"AdjustmentDefinition\".\"id\" = \"vals\".\"id\" AND \"AdjustmentDefinition\".\"id\" = 1 "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"AdjustmentDefinition\".\"type\"                            = \"vals\".\"type\", "
				+ "\"AdjustmentDefinition\".\"number_of_iterations\"            = \"vals\".\"number_of_iterations\", "
				+ "\"AdjustmentDefinition\".\"robust_estimation_limit\"         = \"vals\".\"robust_estimation_limit\", "
				+ "\"AdjustmentDefinition\".\"number_of_principal_components\"  = \"vals\".\"number_of_principal_components\", "
				+ "\"AdjustmentDefinition\".\"apply_variance_of_unit_weight\"   = \"vals\".\"apply_variance_of_unit_weight\", "
				+ "\"AdjustmentDefinition\".\"estimate_direction_set_orientation_approximation\" = \"vals\".\"estimate_direction_set_orientation_approximation\", "
				+ "\"AdjustmentDefinition\".\"congruence_analysis\"      = \"vals\".\"congruence_analysis\", "
				+ "\"AdjustmentDefinition\".\"export_covariance_matrix\" = \"vals\".\"export_covariance_matrix\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"number_of_iterations\", "
				+ "\"vals\".\"robust_estimation_limit\", "
				+ "\"vals\".\"number_of_principal_components\", "
				+ "\"vals\".\"apply_variance_of_unit_weight\", "
				+ "\"vals\".\"estimate_direction_set_orientation_approximation\", "
				+ "\"vals\".\"congruence_analysis\", "
				+ "\"vals\".\"export_covariance_matrix\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		stmt.setInt(idx++,      1); // default ID
		stmt.setInt(idx++,      settings.getEstimationType().getId());
		stmt.setInt(idx++,      settings.getIteration());
		stmt.setDouble(idx++,   settings.getRobustEstimationLimit());
		stmt.setInt(idx++,      settings.getPrincipalComponents());
		
		stmt.setBoolean(idx++,  settings.isApplyVarianceOfUnitWeight());
		stmt.setBoolean(idx++,  settings.isOrientation());
		stmt.setBoolean(idx++,  settings.isCongruenceAnalysis());
		stmt.setBoolean(idx++,  settings.isExportCovarianceMatrix());

		stmt.execute();
		

		sql = "MERGE INTO \"UnscentedTransformation\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"id\", \"scaling\", \"damping\", \"weight_zero\") ON \"UnscentedTransformation\".\"id\" = \"vals\".\"id\" AND \"UnscentedTransformation\".\"id\" = 1 "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"UnscentedTransformation\".\"scaling\"     = \"vals\".\"scaling\", "
				+ "\"UnscentedTransformation\".\"damping\"     = \"vals\".\"damping\", "
				+ "\"UnscentedTransformation\".\"weight_zero\" = \"vals\".\"weight_zero\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"scaling\", "
				+ "\"vals\".\"damping\", "
				+ "\"vals\".\"weight_zero\" ";
		
		
		idx = 1;
		stmt = this.dataBase.getPreparedStatement(sql);
		// Insert new item
		stmt.setInt(idx++,      1); // default ID
		stmt.setDouble(idx++,   settings.getScalingParameterAlphaUT());
		stmt.setDouble(idx++,   settings.getDampingParameterBetaUT());
		stmt.setDouble(idx++,   settings.getWeightZero());

		stmt.execute();
	}
		
	public void searchAndReplacePointNames(String searchRegex, String replaceRegex, boolean applyToWholeProject, TreeItemValue itemValue, TreeItemValue... selectedTreeItemValues) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return;
		
		if (applyToWholeProject) {
			UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
			TreeItemType[] itemTypes = TreeItemType.values();
			for (TreeItemType itemType : itemTypes) {
				if (TreeItemType.isPointTypeDirectory(itemType) ||
						TreeItemType.isObservationTypeDirectory(itemType) ||
						TreeItemType.isGNSSObservationTypeDirectory(itemType) ||
						TreeItemType.isVerticalDeflectionTypeDirectory(itemType) ||
						TreeItemType.isCongruenceAnalysisTypeDirectory(itemType)) {

					TreeItem<TreeItemValue> parent = treeBuilder.getDirectoryItemByType(itemType);

					if (parent != null && !parent.getChildren().isEmpty()) {
						List<TreeItem<TreeItemValue>> items = parent.getChildren();
						TreeItemValue[] itemValues = new TreeItemValue[items.size()];
						for (int i = 0; i < itemValues.length; i++) 
							itemValues[i] = items.get(i).getValue();
						
						this.searchAndReplacePointNames(searchRegex, replaceRegex, false, itemValues[0], itemValues);
					}
				}
			}
			
			List<TreeItem<TreeItemValue>> selectedItems = new ArrayList<TreeItem<TreeItemValue>>(treeBuilder.getTree().getSelectionModel().getSelectedItems());
			if (selectedItems != null && !selectedItems.isEmpty()) {
				treeBuilder.getTree().getSelectionModel().clearSelection();
				for (TreeItem<TreeItemValue> selectedItem : selectedItems)
					treeBuilder.getTree().getSelectionModel().select(selectedItem);
			}
		}
		else {
			TreeItemType treeItemType = itemValue.getItemType();

			switch(treeItemType) {
			case REFERENCE_POINT_1D_LEAF:
			case REFERENCE_POINT_2D_LEAF:
			case REFERENCE_POINT_3D_LEAF:
			case STOCHASTIC_POINT_1D_LEAF:
			case STOCHASTIC_POINT_2D_LEAF:
			case STOCHASTIC_POINT_3D_LEAF:
			case DATUM_POINT_1D_LEAF:
			case DATUM_POINT_2D_LEAF:
			case DATUM_POINT_3D_LEAF:
			case NEW_POINT_1D_LEAF:
			case NEW_POINT_2D_LEAF:
			case NEW_POINT_3D_LEAF:
				if (itemValue instanceof PointTreeItemValue) {
					PointTreeItemValue pointItemValue = (PointTreeItemValue)itemValue;
					PointTreeItemValue[] selectedPointItemValuesArray = null;
					Set<PointTreeItemValue> selectedPointItemValues = new LinkedHashSet<PointTreeItemValue>();
					selectedPointItemValues.add(pointItemValue);

					if (selectedTreeItemValues != null) {
						for (TreeItemValue selectedItem : selectedTreeItemValues) {
							if (selectedItem instanceof PointTreeItemValue)
								selectedPointItemValues.add((PointTreeItemValue)selectedItem);
						}
					}
					selectedPointItemValuesArray = selectedPointItemValues.toArray(new PointTreeItemValue[selectedPointItemValues.size()]);
					this.searchAndReplacePointNames(searchRegex, replaceRegex, selectedPointItemValuesArray);
					this.loadPoints(pointItemValue, selectedPointItemValuesArray);
				}
				break;

			case CONGRUENCE_ANALYSIS_1D_LEAF:
			case CONGRUENCE_ANALYSIS_2D_LEAF:
			case CONGRUENCE_ANALYSIS_3D_LEAF:
				if (itemValue instanceof CongruenceAnalysisTreeItemValue) {
					CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue = (CongruenceAnalysisTreeItemValue)itemValue;
					CongruenceAnalysisTreeItemValue[] selectedCongruenceAnalysisItemValuesArray = null;
					Set<CongruenceAnalysisTreeItemValue> selectedCongruenceAnalysisItemValues = new LinkedHashSet<CongruenceAnalysisTreeItemValue>();
					selectedCongruenceAnalysisItemValues.add(congruenceAnalysisTreeItemValue);

					if (selectedTreeItemValues != null) {
						for (TreeItemValue selectedItem : selectedTreeItemValues) {
							if (selectedItem instanceof CongruenceAnalysisTreeItemValue)
								selectedCongruenceAnalysisItemValues.add((CongruenceAnalysisTreeItemValue)selectedItem);
						}
					}
					selectedCongruenceAnalysisItemValuesArray = selectedCongruenceAnalysisItemValues.toArray(new CongruenceAnalysisTreeItemValue[selectedCongruenceAnalysisItemValues.size()]);
					this.searchAndReplacePointNames(searchRegex, replaceRegex, selectedCongruenceAnalysisItemValuesArray);
					this.loadCongruenceAnalysisPointPair(congruenceAnalysisTreeItemValue, selectedCongruenceAnalysisItemValuesArray);
				}
				break;

			case LEVELING_LEAF:
			case DIRECTION_LEAF:
			case HORIZONTAL_DISTANCE_LEAF:
			case SLOPE_DISTANCE_LEAF:
			case ZENITH_ANGLE_LEAF:
			case GNSS_1D_LEAF:
			case GNSS_2D_LEAF:
			case GNSS_3D_LEAF:
				if (itemValue instanceof ObservationTreeItemValue) {
					ObservationTreeItemValue observationItemValue = (ObservationTreeItemValue)itemValue;
					ObservationTreeItemValue[] selectedObservationItemValuesArray = null;
					Set<ObservationTreeItemValue> selectedObservationItemValues = new LinkedHashSet<ObservationTreeItemValue>();
					selectedObservationItemValues.add(observationItemValue);

					if (selectedTreeItemValues != null) {
						for (TreeItemValue selectedItem : selectedTreeItemValues) {
							if (selectedItem instanceof ObservationTreeItemValue)
								selectedObservationItemValues.add((ObservationTreeItemValue)selectedItem);
						}
					}
					selectedObservationItemValuesArray = selectedObservationItemValues.toArray(new ObservationTreeItemValue[selectedObservationItemValues.size()]);
					this.searchAndReplacePointNames(searchRegex, replaceRegex, selectedObservationItemValuesArray);
					if (TreeItemType.isObservationTypeLeaf(treeItemType))
						this.loadObservations(observationItemValue, selectedObservationItemValuesArray);
					else if (TreeItemType.isGNSSObservationTypeLeaf(treeItemType))
						this.loadGNSSObservations(observationItemValue, selectedObservationItemValuesArray);
				}
				break;
				
			case REFERENCE_VERTICAL_DEFLECTION_LEAF:
			case STOCHASTIC_VERTICAL_DEFLECTION_LEAF:
			case UNKNOWN_VERTICAL_DEFLECTION_LEAF:
				if (itemValue instanceof VerticalDeflectionTreeItemValue) {
					VerticalDeflectionTreeItemValue verticalDeflectionItemValue = (VerticalDeflectionTreeItemValue)itemValue;
					VerticalDeflectionTreeItemValue[] selectedVerticalDeflectionItemValuesArray = null;
					Set<VerticalDeflectionTreeItemValue> selectedVerticalDeflectionItemValues = new LinkedHashSet<VerticalDeflectionTreeItemValue>();
					selectedVerticalDeflectionItemValues.add(verticalDeflectionItemValue);

					if (selectedTreeItemValues != null) {
						for (TreeItemValue selectedItem : selectedTreeItemValues) {
							if (selectedItem instanceof VerticalDeflectionTreeItemValue)
								selectedVerticalDeflectionItemValues.add((VerticalDeflectionTreeItemValue)selectedItem);
						}
					}
					selectedVerticalDeflectionItemValuesArray = selectedVerticalDeflectionItemValues.toArray(new VerticalDeflectionTreeItemValue[selectedVerticalDeflectionItemValues.size()]);
					this.searchAndReplacePointNames(searchRegex, replaceRegex, selectedVerticalDeflectionItemValuesArray);
					this.loadVerticalDeflections(verticalDeflectionItemValue, selectedVerticalDeflectionItemValuesArray);
				}
				break;

			default:
				break;
			}
		}
	}

	private void searchAndReplacePointNames(String searchRegex, String replaceRegex, VerticalDeflectionTreeItemValue... selectedTreeItemValues) throws SQLException {
		String sql = "SELECT \"name\" FROM \"VerticalDeflectionApriori\" WHERE REGEXP_MATCHES(\"name\", ?) AND \"group_id\" = ?";
		for (VerticalDeflectionTreeItemValue verticalDeflectionTreeItemValue : selectedTreeItemValues) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			int idx = 1;
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, verticalDeflectionTreeItemValue.getGroupId());
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String oldName = rs.getString("name");
				String newName = oldName.replaceFirst(searchRegex, replaceRegex);
				newName = newName.substring(0, Math.min(newName.length(), 255));
				
				if (this.isNameCollisions(verticalDeflectionTreeItemValue, newName))
					continue;
				
				this.replacePointName(verticalDeflectionTreeItemValue, oldName, newName);
			}
		}
	}
	
	private void searchAndReplacePointNames(String searchRegex, String replaceRegex, PointTreeItemValue... selectedTreeItemValues) throws SQLException {
		String sql = "SELECT \"name\" FROM \"PointApriori\" WHERE REGEXP_MATCHES(\"name\", ?) AND \"group_id\" = ?";
		for (PointTreeItemValue pointTreeItemValue : selectedTreeItemValues) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			int idx = 1;
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, pointTreeItemValue.getGroupId());
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String oldName = rs.getString("name");
				String newName = oldName.replaceFirst(searchRegex, replaceRegex);
				newName = newName.substring(0, Math.min(newName.length(), 255));
				
				if (this.isNameCollisions(pointTreeItemValue, newName))
					continue;
				
				this.replacePointName(pointTreeItemValue, oldName, newName);
			}
		}
	}
	
	private void searchAndReplacePointNames(String searchRegex, String replaceRegex, ObservationTreeItemValue... selectedTreeItemValues) throws SQLException {
		String sql = "SELECT "
				+ "DISTINCT \"end_point_name\" AS \"name\" FROM \"%s\" WHERE REGEXP_MATCHES(\"end_point_name\", ?) AND \"group_id\" = ? "
				+ "UNION ALL "
				+ "SELECT "
				+ "DISTINCT \"start_point_name\" AS \"name\" FROM \"%s\" WHERE REGEXP_MATCHES(\"start_point_name\", ?) AND \"group_id\" = ?";
		
		for (ObservationTreeItemValue observationTreeItemValue : selectedTreeItemValues) {
			String tableName = TreeItemType.isObservationTypeLeaf(observationTreeItemValue.getItemType()) ? "ObservationApriori" : "GNSSObservationApriori";
			PreparedStatement stmt = this.dataBase.getPreparedStatement(String.format(sql, tableName, tableName));
			int idx = 1;
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, observationTreeItemValue.getGroupId());
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, observationTreeItemValue.getGroupId());
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String oldName = rs.getString("name");
				String newName = oldName.replaceFirst(searchRegex, replaceRegex);
				newName = newName.substring(0, Math.min(newName.length(), 255));
				
				if (this.isNameCollisions(observationTreeItemValue, oldName, newName))
					continue;
				
				this.replacePointName(observationTreeItemValue, oldName, newName);
			}
		}
	}
	
	private void searchAndReplacePointNames(String searchRegex, String replaceRegex, CongruenceAnalysisTreeItemValue... selectedTreeItemValues) throws SQLException {
		String sql = "SELECT "
				+ "DISTINCT \"end_point_name\" AS \"name\" FROM \"CongruenceAnalysisPointPairApriori\" WHERE REGEXP_MATCHES(\"end_point_name\", ?) AND \"group_id\" = ? "
				+ "UNION ALL "
				+ "SELECT "
				+ "DISTINCT \"start_point_name\" AS \"name\" FROM \"CongruenceAnalysisPointPairApriori\" WHERE REGEXP_MATCHES(\"start_point_name\", ?) AND \"group_id\" = ?";
		
		for (CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue : selectedTreeItemValues) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			int idx = 1;
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, congruenceAnalysisTreeItemValue.getGroupId());
			stmt.setString(idx++, searchRegex);
			stmt.setInt(idx++, congruenceAnalysisTreeItemValue.getGroupId());
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String oldName = rs.getString("name");
				String newName = oldName.replaceFirst(searchRegex, replaceRegex);
				newName = newName.substring(0, Math.min(newName.length(), 255));

				if (this.isNameCollisions(congruenceAnalysisTreeItemValue, oldName, newName))
					continue;
				
				this.replacePointName(congruenceAnalysisTreeItemValue, oldName, newName);
			}
		}
	}
	
	private void replacePointName(VerticalDeflectionTreeItemValue verticalDeflectionTreeItemValue, String oldName, String newName) throws SQLException {
		String sql = "UPDATE \"VerticalDeflectionApriori\" SET \"name\" = ? WHERE \"name\" = ? AND \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setString(idx++, newName);
		stmt.setString(idx++, oldName);
		stmt.setInt(idx++, verticalDeflectionTreeItemValue.getGroupId());
		stmt.execute();
	}
	
	private void replacePointName(PointTreeItemValue pointTreeItemValue, String oldName, String newName) throws SQLException {
		String sql = "UPDATE \"PointApriori\" SET \"name\" = ? WHERE \"name\" = ? AND \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setString(idx++, newName);
		stmt.setString(idx++, oldName);
		stmt.setInt(idx++, pointTreeItemValue.getGroupId());
		stmt.execute();
	}
	
	private void replacePointName(ObservationTreeItemValue observationTreeItemValue, String oldName, String newName) throws SQLException {
		String sqls[] = new String[] {
				"UPDATE \"%s\" SET \"start_point_name\" = ? WHERE \"start_point_name\" = ? AND \"group_id\" = ?",
				"UPDATE \"%s\" SET \"end_point_name\"   = ? WHERE \"end_point_name\"   = ? AND \"group_id\" = ?"
		};
		String tableName = TreeItemType.isObservationTypeLeaf(observationTreeItemValue.getItemType()) ? "ObservationApriori" : "GNSSObservationApriori";
		for (String sql : sqls) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(String.format(sql, tableName));
			int idx = 1;
			stmt.setString(idx++, newName);
			stmt.setString(idx++, oldName);
			stmt.setInt(idx++, observationTreeItemValue.getGroupId());
			stmt.execute();
		}
	}
	
	private void replacePointName(CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue, String oldName, String newName) throws SQLException {
		String sqls[] = new String[] {
				"UPDATE \"CongruenceAnalysisPointPairApriori\" SET \"start_point_name\" = ? WHERE \"start_point_name\" = ? AND \"group_id\" = ?",
				"UPDATE \"CongruenceAnalysisPointPairApriori\" SET \"end_point_name\"   = ? WHERE \"end_point_name\"   = ? AND \"group_id\" = ?"
		};
		for (String sql : sqls) {
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			int idx = 1;
			stmt.setString(idx++, newName);
			stmt.setString(idx++, oldName);
			stmt.setInt(idx++, congruenceAnalysisTreeItemValue.getGroupId());
			stmt.execute();
		}
	}
	
	private boolean isNameCollisions(VerticalDeflectionTreeItemValue verticalDeflectionTreeItemValue, String proposedName) throws SQLException {
		String sql = "SELECT COUNT(\"id\") > 0 AS \"collisions\" FROM \"VerticalDeflectionApriori\" WHERE \"name\" = ? AND \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setString(idx++, proposedName);
		stmt.setInt(idx++, verticalDeflectionTreeItemValue.getGroupId());
		ResultSet rs = stmt.executeQuery();
		
		boolean collisions = false;
		if (rs.next()) 
			collisions = rs.getBoolean("collisions");
		return collisions;
	}
	
	private boolean isNameCollisions(PointTreeItemValue pointTreeItemValue, String proposedName) throws SQLException {
		String sql = "SELECT COUNT(\"id\") > 0 AS \"collisions\" FROM \"PointApriori\" WHERE \"name\" = ? AND \"group_id\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setString(idx++, proposedName);
		stmt.setInt(idx++, pointTreeItemValue.getGroupId());
		ResultSet rs = stmt.executeQuery();
		
		boolean collisions = false;
		if (rs.next()) 
			collisions = rs.getBoolean("collisions");
		return collisions;
	}
	
	private boolean isNameCollisions(ObservationTreeItemValue observationTreeItemValue, String oldName, String newName) throws SQLException {
		String sql = "SELECT COUNT(\"id\") > 0 AS \"collisions\" FROM \"%s\" "
				+ "WHERE ((\"start_point_name\" = ? AND \"end_point_name\" = ?) OR (\"start_point_name\" = ? AND \"end_point_name\" = ?)) "
				+ "AND \"group_id\" = ?";
		String tableName = TreeItemType.isObservationTypeLeaf(observationTreeItemValue.getItemType()) ? "ObservationApriori" : "GNSSObservationApriori";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(String.format(sql, tableName));
		int idx = 1;
		stmt.setString(idx++, oldName);
		stmt.setString(idx++, newName);
		stmt.setString(idx++, newName);
		stmt.setString(idx++, oldName);
		stmt.setInt(idx++, observationTreeItemValue.getGroupId());
		ResultSet rs = stmt.executeQuery();
		
		boolean collisions = false;
		if (rs.next()) 
			collisions = rs.getBoolean("collisions");
		return collisions;
	}
	
	private boolean isNameCollisions(CongruenceAnalysisTreeItemValue congruenceAnalysisTreeItemValue, String oldName, String newName) throws SQLException {
		String sql = "SELECT COUNT(\"id\") > 0 AS \"collisions\" FROM "
				+ "\"CongruenceAnalysisPointPairApriori\""
				+ "WHERE ((\"start_point_name\" = ? AND \"end_point_name\" = ?) OR (\"start_point_name\" = ? AND \"end_point_name\" = ?)) "
				+ "AND \"group_id\" = ?";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		int idx = 1;
		stmt.setString(idx++, oldName);
		stmt.setString(idx++, newName);
		stmt.setString(idx++, newName);
		stmt.setString(idx++, oldName);
		stmt.setInt(idx++, congruenceAnalysisTreeItemValue.getGroupId());
		ResultSet rs = stmt.executeQuery();
		
		boolean collisions = false;
		if (rs.next()) 
			collisions = rs.getBoolean("collisions");
		return collisions;
	}
	
	public Set<String> getFullPointNameSet() throws SQLException {
		Set<String> names = new HashSet<String>();
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return null;

		String sql = "SELECT "
				+ "\"name\" "
				+ "FROM \"PointApriori\" "
				+ "ORDER BY \"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String name = rs.getString("name");
			names.add(name);
		}

		return names;
	}
	
	public Set<String> getFullVerticalDeflectionNameSet() throws SQLException {
		Set<String> names = new HashSet<String>();
		if (!this.hasDatabase() || !this.dataBase.isOpen())
			return null;

		String sql = "SELECT "
				+ "\"name\" "
				+ "FROM \"VerticalDeflectionApriori\" "
				+ "ORDER BY \"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String name = rs.getString("name");
			names.add(name);
		}

		return names;
	}

	public void loadTableRowHighlight() throws SQLException {
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		
		this.loadTableRowHighlightProperties(tableRowHighlight);
		this.loadTableRowHighlightType(tableRowHighlight);
		this.loadTableRowHighlightRange(tableRowHighlight);
	}
	
	private void loadTableRowHighlightProperties(TableRowHighlight tableRowHighlight) throws SQLException {
		String sql = "SELECT "
				+ "\"red\", \"green\", \"blue\" "
				+ "FROM \"TableRowHighlightProperty\" "
				+ "WHERE \"type\" = ? LIMIT 1";
		
		for (TableRowHighlightRangeType tableRowHighlightRangeType : TableRowHighlightRangeType.values()) {
			int idx = 1;
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			stmt.setInt(idx++, tableRowHighlightRangeType.getId());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				double opacity = 1.0;

				double red = rs.getDouble("red");
				red = Math.min(Math.max(0, red), 1);

				double green = rs.getDouble("green");
				green = Math.min(Math.max(0, green), 1);

				double blue = rs.getDouble("blue");
				blue = Math.min(Math.max(0, blue), 1);
				
				tableRowHighlight.setColor(tableRowHighlightRangeType, new Color(red, green, blue, opacity));
			}
		}
	}
	
	private void loadTableRowHighlightRange(TableRowHighlight tableRowHighlight) throws SQLException {
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType();
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
			tableRowHighlight.setRange(leftBoundary, rightBoundary);
		}
	}
	
	private void loadTableRowHighlightType(TableRowHighlight tableRowHighlight) throws SQLException {
		String sql = "SELECT "
				+ "\"type\" "
				+ "FROM \"TableRowHighlightScheme\" "
				+ "WHERE \"id\" = 1 LIMIT 1";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			TableRowHighlightType tableRowHighlightType = TableRowHighlightType.getEnumByValue(rs.getInt("type"));
			if (tableRowHighlightType != null)
				tableRowHighlight.setTableRowHighlightType(tableRowHighlightType);
		}
	}

	public void saveTableRowHighlight() throws SQLException {
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType();
		
		// save color properties
		this.save(TableRowHighlightRangeType.EXCELLENT,    tableRowHighlight.getColor(TableRowHighlightRangeType.EXCELLENT));
		this.save(TableRowHighlightRangeType.SATISFACTORY, tableRowHighlight.getColor(TableRowHighlightRangeType.SATISFACTORY));
		this.save(TableRowHighlightRangeType.INADEQUATE,   tableRowHighlight.getColor(TableRowHighlightRangeType.INADEQUATE));

		// save range and display options
		switch(tableRowHighlightType) {
		case INFLUENCE_ON_POSITION:
		case REDUNDANCY:
		case P_PRIO_VALUE:
			this.save(tableRowHighlightType, tableRowHighlight.getLeftBoundary(), tableRowHighlight.getRightBoundary());
			// no break, to enter default-case 

		default:
			this.save(tableRowHighlightType);
			break;
		}
	}
	
	private void save(TableRowHighlightRangeType tableRowHighlightRangeType, Color color) throws SQLException {
		String sql = "MERGE INTO \"TableRowHighlightProperty\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"type\", \"red\", \"green\", \"blue\") ON \"TableRowHighlightProperty\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"TableRowHighlightProperty\".\"red\"   = \"vals\".\"red\", "
				+ "\"TableRowHighlightProperty\".\"green\" = \"vals\".\"green\", "
				+ "\"TableRowHighlightProperty\".\"blue\"  = \"vals\".\"blue\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"red\", "
				+ "\"vals\".\"green\", "
				+ "\"vals\".\"blue\"";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, tableRowHighlightRangeType.getId());
		stmt.setDouble(idx++, color.getRed());
		stmt.setDouble(idx++, color.getGreen());
		stmt.setDouble(idx++, color.getBlue());
		stmt.execute();
	}
	
	private void save(TableRowHighlightType tableRowHighlightType, double leftBoundary, double rightBoundary) throws SQLException {
		String sql = "MERGE INTO \"TableRowHighlightRange\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"type\", \"left_boundary\", \"right_boundary\") ON \"TableRowHighlightRange\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"TableRowHighlightRange\".\"left_boundary\"  = \"vals\".\"left_boundary\", "
				+ "\"TableRowHighlightRange\".\"right_boundary\" = \"vals\".\"right_boundary\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"left_boundary\", "
				+ "\"vals\".\"right_boundary\"";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, tableRowHighlightType.getId());
		stmt.setDouble(idx++, leftBoundary);
		stmt.setDouble(idx++, rightBoundary);
		stmt.execute();
	}
	
	private void save(TableRowHighlightType tableRowHighlightType) throws SQLException {
		String sql = "MERGE INTO \"TableRowHighlightScheme\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"id\", \"type\") ON \"TableRowHighlightScheme\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"TableRowHighlightScheme\".\"type\" = \"vals\".\"type\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"type\"";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, 1); // default id
		stmt.setInt(idx++, tableRowHighlightType.getId());
		stmt.execute();
	}
	
	public void loadImportPreferences() throws SQLException {
		ImportOption importOption = ImportOption.getInstance();
		String sql = "SELECT "
				+ "\"id\", \"separate\" "
				+ "FROM \"ImportSeparation\"";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt("id");
			ObservationType observationType = ObservationType.getEnumByValue(id);
			if (observationType == null)
				continue;
			
			boolean separate = rs.getBoolean("separate");
			importOption.setGroupSeparation(observationType, separate);
		}
	}
	
	public void saveImportPreferences() throws SQLException {
		String sql = "MERGE INTO \"ImportSeparation\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"id\", \"separate\") ON \"ImportSeparation\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ImportSeparation\".\"separate\" = \"vals\".\"separate\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"separate\"";
		
		ImportOption importOption = ImportOption.getInstance();
		boolean hasBatch = false;
		
		try {
			this.dataBase.setAutoCommit(false);
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			
			for (ObservationType observationType : ObservationType.values()) {
				int idx = 1;
				stmt.setInt(idx++, observationType.getId());
				stmt.setBoolean(idx++, importOption.isGroupSeparation(observationType));
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

	public void executeStatement(String sql) throws SQLException {
		if (!this.hasDatabase() || !this.dataBase.isOpen() || sql == null || sql.isBlank() || sql.isEmpty())
			return;
		
		try {
			this.dataBase.setAutoCommit(false);
			Statement stmt = this.dataBase.getStatement();
			stmt.execute(sql);
		}
		finally {
			this.dataBase.setAutoCommit(true);
		}
	}
	
	public DataBase getDataBase() {
		return this.dataBase;
	}
	
	private void fireDatabaseStateChanged(ProjectDatabaseStateType stateType) {
		ProjectDatabaseStateEvent evt = new ProjectDatabaseStateEvent(this, stateType);
		Object listeners[] = this.listenerList.toArray();
		for (int i=0; i<listeners.length; i++) {
			if (listeners[i] instanceof ProjectDatabaseStateChangeListener) {
				((ProjectDatabaseStateChangeListener)listeners[i]).projectDatabaseStateChanged(evt);
			}
		}
	}
		
	public void addProjectDatabaseStateChangeListener(ProjectDatabaseStateChangeListener l) {
		this.listenerList.add(l);
	}
	
	public void removeProjectDatabaseStateChangeListener(ProjectDatabaseStateChangeListener l) {
		this.listenerList.remove(l);
	}
}
