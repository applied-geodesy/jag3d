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

package org.applied_geodesy.jag3d.ui.io.sql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.ObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.VerticalDeflectionRow;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.sql.DataBase;
import org.applied_geodesy.version.jag3d.Version;
import org.applied_geodesy.version.VersionType;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

public class OADBReader extends SourceFileReader<TreeItem<TreeItemValue>> {
	private Set<String> reservedPointNames = null;
	private Set<String> reservedVerticalDeflectionNames = null;
	private DataBase dataBase = null;
	
	public OADBReader() {
		this.reset();
	}
	
	public OADBReader(DataBase dataBase) {
		this.dataBase = dataBase;
		this.reset();
	}

	@Override
	public void reset() {
		if (this.dataBase != null)
			this.dataBase.close();
		
		if (this.reservedPointNames == null)
			this.reservedPointNames = new HashSet<String>();
		
		if (this.reservedVerticalDeflectionNames == null)
			this.reservedVerticalDeflectionNames = new HashSet<String>();
		
		this.reservedPointNames.clear();
		this.reservedVerticalDeflectionNames.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		TreeItem<TreeItemValue> lastTreeItem = null;
				
		this.reset();

		try {
			if (SQLManager.getInstance().getDataBase().getURI().equals(this.dataBase.getURI()))
				throw new IOException(this.getClass().getSimpleName() + " : Error, cannot re-import data to the same database!");
			
			this.dataBase.open();
			if (OADBReader.isCurrentOADBVersion(this.dataBase)) {
				this.reservedPointNames              = SQLManager.getInstance().getFullPointNameSet();
				this.reservedVerticalDeflectionNames = SQLManager.getInstance().getFullVerticalDeflectionNameSet();
				
				TreeItem<TreeItemValue> lastPointTreeItem              = this.transferPointGroups();
				TreeItem<TreeItemValue> lastObservationTreeItem        = this.transferObservationGroups();
				TreeItem<TreeItemValue> lastCongruenceAnalysisTreeItem = this.transferCongruenceAnalysisGroups();
				TreeItem<TreeItemValue> lastVerticalDeflectionTreeItem = this.transferVerticalDeflectionGroups();
				
				if (lastPointTreeItem != null)
					lastTreeItem = lastPointTreeItem;
				else if (lastObservationTreeItem != null)
					lastTreeItem = lastObservationTreeItem;
				else if (lastCongruenceAnalysisTreeItem != null)
					lastTreeItem = lastCongruenceAnalysisTreeItem;
				else if (lastVerticalDeflectionTreeItem != null)
					lastTreeItem = lastVerticalDeflectionTreeItem;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		finally {
			if (dataBase != null)
				dataBase.close();
		}
		
		// Clear all lists
		this.reset();
		return lastTreeItem;
	}

	@Override
	public void parse(String line) throws SQLException {
		throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database content cannot be parsed!");
	}
	
	private TreeItem<TreeItemValue> transferVerticalDeflectionGroups() throws SQLException {
		TreeItem<TreeItemValue> lastValidTreeItem = null;
		
		String sql = "SELECT " + 
				"\"id\", \"name\", \"type\", \"enable\" " + 
				"FROM \"VerticalDeflectionGroup\" " + 
				"ORDER BY \"order\" ASC, \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			VerticalDeflectionType verticalDeflectionType = VerticalDeflectionType.getEnumByValue(rs.getInt("type"));
			if (verticalDeflectionType == null)
				continue;
			
			int groupId       = rs.getInt("id");
			String groupName  = rs.getString("name");
			boolean enable    = rs.getBoolean("enable");
			
			TreeItemType treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(verticalDeflectionType);
			
			List<VerticalDeflectionRow> verticalDeflections = null;
			
			if (TreeItemType.isVerticalDeflectionTypeLeaf(treeItemType)) 
				verticalDeflections = this.getVerticalDeflections(groupId);
				
			if (verticalDeflections == null)
				continue;

			TreeItem<TreeItemValue> treeItem = this.saveVerticalDeflections(treeItemType, groupName, enable, verticalDeflections);
			if (treeItem != null) {
				this.transferUncertainties(groupId, (VerticalDeflectionTreeItemValue)treeItem.getValue());
				
				lastValidTreeItem = treeItem;
			}
		}
		return lastValidTreeItem;
	}
	
	private TreeItem<TreeItemValue> transferPointGroups() throws SQLException {
		TreeItem<TreeItemValue> lastValidTreeItem = null;
		
		String sql = "SELECT " + 
				"\"id\", \"name\", \"type\", \"enable\", \"dimension\" " + 
				"FROM \"PointGroup\" " + 
				"ORDER BY \"order\" ASC, \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			PointType pointType = PointType.getEnumByValue(rs.getInt("type"));
			if (pointType == null)
				continue;
			
			int groupId       = rs.getInt("id");
			int dimension     = rs.getInt("dimension");
			String groupName  = rs.getString("name");
			boolean enable    = rs.getBoolean("enable");
			
			TreeItemType treeItemType = TreeItemType.getTreeItemTypeByPointType(pointType, dimension);
			
			List<PointRow> points = null;
			
			if (TreeItemType.isPointTypeLeaf(treeItemType)) 
				points = this.getPoints(groupId);
				
			if (points == null)
				continue;
			
			TreeItem<TreeItemValue> treeItem = this.savePoints(treeItemType, groupName, enable, points);
			if (treeItem != null) {
				this.transferUncertainties(groupId, (PointTreeItemValue)treeItem.getValue());
	
				lastValidTreeItem = treeItem;
			}
		}
		return lastValidTreeItem;
	}

	private TreeItem<TreeItemValue> transferObservationGroups() throws SQLException {
		TreeItem<TreeItemValue> lastValidTreeItem = null;
		
		String sql = "SELECT " + 
				"\"id\", \"name\", \"type\", \"enable\" " + 
				"FROM \"ObservationGroup\" " + 
				"ORDER BY \"order\" ASC, \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ObservationType observationType = ObservationType.getEnumByValue(rs.getInt("type"));
			if (observationType == null)
				continue;
			
			int groupId       = rs.getInt("id");
			String groupName  = rs.getString("name");
			boolean enable    = rs.getBoolean("enable");
			
			TreeItemType treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
			
			List<ObservationRow> observations = null;
			
			if (TreeItemType.isObservationTypeLeaf(treeItemType)) 
				observations = this.getTerrestrialObservations(groupId);
			else if (TreeItemType.isGNSSObservationTypeLeaf(treeItemType))
				observations = this.getGNSSObservations(groupId);
				
			if (observations == null)
				continue;
			
			TreeItem<TreeItemValue> treeItem = this.saveObservations(treeItemType, groupName, enable, observations);
			if (treeItem != null) {
				this.transferUncertainties(groupId, (ObservationTreeItemValue)treeItem.getValue());
				this.transferEpoch(groupId, (ObservationTreeItemValue)treeItem.getValue());
				this.transferAdditionalParameters(groupId, (ObservationTreeItemValue)treeItem.getValue());

				lastValidTreeItem = treeItem;
			}
		}
		return lastValidTreeItem;
	}
	
	private TreeItem<TreeItemValue> transferCongruenceAnalysisGroups() throws SQLException {
		TreeItem<TreeItemValue> lastValidTreeItem = null;
		
		String sql = "SELECT " + 
				"\"id\", \"name\", \"enable\", \"dimension\" " + 
				"FROM \"CongruenceAnalysisGroup\" " + 
				"ORDER BY \"order\" ASC, \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int groupId       = rs.getInt("id");
			int dimension     = rs.getInt("dimension");
			String groupName  = rs.getString("name");
			boolean enable    = rs.getBoolean("enable");
			
			TreeItemType treeItemType = TreeItemType.getTreeItemTypeByCongruenceAnalysisDimension(dimension);
			
			List<CongruenceAnalysisRow> pairs = null;
			
			if (TreeItemType.isCongruenceAnalysisTypeLeaf(treeItemType)) 
				pairs = this.getCongruenceAnalysisPairs(groupId);
				
			if (pairs == null)
				continue;
			
			TreeItem<TreeItemValue> treeItem = this.saveCongruenceAnalysisPairs(treeItemType, groupName, enable, pairs);
			if (treeItem != null) {
				this.transferStrainParameters(groupId, (CongruenceAnalysisTreeItemValue)treeItem.getValue());
				
				lastValidTreeItem = treeItem;
			}
		}
		return lastValidTreeItem;
	}
	
	private List<PointRow> getPoints(int groupId) throws SQLException {
		List<PointRow> points = new ArrayList<PointRow>();
		
		String sql = "SELECT " + 
				"\"name\", \"code\", \"enable\"," + 
				"\"x0\", \"y0\", \"z0\", " +
				"\"sigma_y0\", \"sigma_x0\", \"sigma_z0\" " + 
				"FROM \"PointApriori\" " + 
				"WHERE \"group_id\" = ? " +
				"ORDER BY \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, groupId);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			PointRow point = new PointRow();

			String name = rs.getString("name");
			if (this.reservedPointNames.contains(name))
				continue;
			
			point.setName(name);
			point.setCode(rs.getString("code"));
			point.setEnable(rs.getBoolean("enable"));

			double value;
			value = rs.getDouble("x0");
			point.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			point.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z0");
			point.setZApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			point.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			point.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_z0");
			point.setSigmaZapriori(rs.wasNull() || value <= 0 ? null : value);

			points.add(point);
		}
		return points;
	}
	
	private List<VerticalDeflectionRow> getVerticalDeflections(int groupId) throws SQLException {
		List<VerticalDeflectionRow> verticalDeflections = new ArrayList<VerticalDeflectionRow>();
		
		String sql = "SELECT " + 
				"\"name\", \"enable\"," + 
				"\"x0\", \"y0\", " +
				"\"sigma_y0\", \"sigma_x0\" " + 
				"FROM \"VerticalDeflectionApriori\" " + 
				"WHERE \"group_id\" = ? " +
				"ORDER BY \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, groupId);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			VerticalDeflectionRow verticalDeflection = new VerticalDeflectionRow();

			String name = rs.getString("name");
			if (this.reservedVerticalDeflectionNames.contains(name))
				continue;
			
			verticalDeflection.setName(name);
			verticalDeflection.setEnable(rs.getBoolean("enable"));

			double value;
			value = rs.getDouble("x0");
			verticalDeflection.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			verticalDeflection.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			verticalDeflection.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			verticalDeflection.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);

			verticalDeflections.add(verticalDeflection);
		}
		return verticalDeflections;
	}
	
	private List<ObservationRow> getTerrestrialObservations(int groupId) throws SQLException {
		List<ObservationRow> observations = new ArrayList<ObservationRow>();
		
		String sql = "SELECT " + 
				"\"start_point_name\", \"end_point_name\", \"instrument_height\", \"reflector_height\", \"value_0\", \"distance_0\", \"sigma_0\" AS \"sigma_0\", \"enable\" " +
				"FROM \"ObservationApriori\" " + 
				"WHERE \"group_id\" = ? " +
				"ORDER BY \"id\" ASC";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, groupId);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			TerrestrialObservationRow observation = new TerrestrialObservationRow();

			observation.setStartPointName(rs.getString("start_point_name"));
			observation.setEndPointName(rs.getString("end_point_name"));
			observation.setEnable(rs.getBoolean("enable"));
			observation.setValueApriori(rs.getDouble("value_0"));
			
			double value = rs.getDouble("instrument_height");
			if (!rs.wasNull())
				observation.setInstrumentHeight(value);

			value = rs.getDouble("reflector_height");
			if (!rs.wasNull())
				observation.setReflectorHeight(value);

			value = rs.getDouble("distance_0");
			if (!rs.wasNull())
				observation.setDistanceApriori(value > 0 ? value : null);

			value = rs.getDouble("sigma_0");
			if (!rs.wasNull())
				observation.setSigmaApriori(value > 0 ? value : null);
			
			observations.add(observation);
		}
		return observations;
	}
	
	private List<CongruenceAnalysisRow> getCongruenceAnalysisPairs(int groupId) throws SQLException {
		List<CongruenceAnalysisRow> pairs = new ArrayList<CongruenceAnalysisRow>();
		
		String sql = "SELECT "
				+ "\"start_point_name\", \"end_point_name\", \"enable\" "
				+ "FROM \"CongruenceAnalysisPointPairApriori\" "
				+ "WHERE \"group_id\" = ? "
				+ "ORDER BY \"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, groupId);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			CongruenceAnalysisRow pair = new CongruenceAnalysisRow();

			pair.setNameInReferenceEpoch(rs.getString("start_point_name"));
			pair.setNameInControlEpoch(rs.getString("end_point_name"));
			pair.setEnable(rs.getBoolean("enable"));

			pairs.add(pair);
		}
		return pairs;
	}
	
	private List<ObservationRow> getGNSSObservations(int groupId) throws SQLException {
		List<ObservationRow> observations = new ArrayList<ObservationRow>();
		
		String sql = "SELECT " + 
				"\"start_point_name\", \"end_point_name\", \"y0\", \"x0\", \"z0\", \"sigma_y0\", \"sigma_x0\", \"sigma_z0\", \"enable\" " + 
				"FROM \"GNSSObservationApriori\" " + 
				"WHERE \"group_id\" = ? " +
				"ORDER BY \"id\" ASC";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, groupId);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			GNSSObservationRow observation = new GNSSObservationRow();
			
			// Apriori-Values
			observation.setStartPointName(rs.getString("start_point_name"));
			observation.setEndPointName(rs.getString("end_point_name"));
			observation.setEnable(rs.getBoolean("enable"));
			double value;
			value = rs.getDouble("x0");
			observation.setXApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("y0");
			observation.setYApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("z0");
			observation.setZApriori(rs.wasNull() ? 0 : value);

			value = rs.getDouble("sigma_x0");
			observation.setSigmaXapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_y0");
			observation.setSigmaYapriori(rs.wasNull() || value <= 0 ? null : value);

			value = rs.getDouble("sigma_z0");
			observation.setSigmaZapriori(rs.wasNull() || value <= 0 ? null : value);
			
			observations.add(observation);
		}
		return observations;
	}
	
	private void transferUncertainties(int srcGroupId, PointTreeItemValue destPointItemValue) throws SQLException {
		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"PointGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			PointGroupUncertaintyType uncertaintyType = PointGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (uncertaintyType == null)
				continue;
			
			double value = rs.getDouble("value");
			SQLManager.getInstance().saveUncertainty(uncertaintyType, value, destPointItemValue);
		}
	}
	
	private void transferUncertainties(int srcGroupId, VerticalDeflectionTreeItemValue destVerticalDeflectionItemValue) throws SQLException {
		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"VerticalDeflectionGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			VerticalDeflectionGroupUncertaintyType uncertaintyType = VerticalDeflectionGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (uncertaintyType == null)
				continue;
			
			double value = rs.getDouble("value");
			SQLManager.getInstance().saveUncertainty(uncertaintyType, value, destVerticalDeflectionItemValue);
		}
	}
	
	private void transferUncertainties(int srcGroupId, ObservationTreeItemValue destObservationItemValue) throws SQLException {
		String sql = "SELECT \"type\", \"value\" "
				+ "FROM \"ObservationGroupUncertainty\" "
				+ "WHERE \"group_id\" = ?";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ObservationGroupUncertaintyType uncertaintyType = ObservationGroupUncertaintyType.getEnumByValue(rs.getInt("type"));
			if (uncertaintyType == null)
				continue;

			double value = rs.getDouble("value");
			SQLManager.getInstance().saveUncertainty(uncertaintyType, value, destObservationItemValue);
		}
	}
	
	private void transferEpoch(int srcGroupId, ObservationTreeItemValue destObservationItemValue) throws SQLException {
		String sql = "SELECT \"reference_epoch\" "
				+ "FROM \"ObservationGroup\" "
				+ "WHERE \"id\" = ?";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			boolean referenceEpoch = rs.getBoolean("reference_epoch");
			SQLManager.getInstance().saveEpoch(referenceEpoch, destObservationItemValue);
		}
	}
	
	private void transferAdditionalParameters(int srcGroupId, ObservationTreeItemValue destObservationItemValue) throws SQLException {
		String sql = "SELECT \"type\", \"value_0\", \"enable\" " +
				"FROM \"AdditionalParameterApriori\" " +
				"WHERE \"group_id\" = ? ";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ParameterType parameterType = ParameterType.getEnumByValue(rs.getInt("type"));
			if (parameterType == null)
				continue;
			
			boolean enable = rs.getBoolean("enable");
			double value   = rs.getDouble("value_0");
			SQLManager.getInstance().saveAdditionalParameter(parameterType, enable, value, destObservationItemValue);
		}
	}
	
	private void transferStrainParameters(int srcGroupId, CongruenceAnalysisTreeItemValue destCongruenceAnalysisItemValue) throws SQLException {
		String sql = "SELECT \"type\", \"enable\" " +
				"FROM \"CongruenceAnalysisStrainParameterRestriction\" " +
				"WHERE \"group_id\" = ? ";
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(1, srcGroupId);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			RestrictionType restrictionType = RestrictionType.getEnumByValue(rs.getInt("type"));
			if (restrictionType == null)
				continue;
			
			boolean enable = rs.getBoolean("enable");
			SQLManager.getInstance().saveStrainParameter(restrictionType, enable, destCongruenceAnalysisItemValue);
		}
	}

	public static boolean isCurrentOADBVersion(DataBase dataBase) throws SQLException, ClassNotFoundException {
		boolean isOpen = dataBase.isOpen();
		
		try {
			if (!isOpen)
				dataBase.open();

			String sql = "SELECT TRUE AS \"exists\" "
					+ "FROM \"INFORMATION_SCHEMA\".\"COLUMNS\" "
					+ "WHERE \"TABLE_SCHEMA\" = 'OpenAdjustment' "
					+ "AND \"TABLE_NAME\" = 'Version' "
					+ "AND \"COLUMN_NAME\" = 'version'";

			PreparedStatement stmt = dataBase.getPreparedStatement(sql);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				boolean exists = rs.getBoolean("exists");

				if (!rs.wasNull() && exists) {
					sql = "SET SCHEMA \"OpenAdjustment\";";
					stmt = dataBase.getPreparedStatement(sql);
					stmt.execute();

					sql = "SELECT \"version\" FROM \"Version\" WHERE \"type\" = ?";
					stmt = dataBase.getPreparedStatement(sql);
					stmt.setInt(1, VersionType.DATABASE.getId());
					rs = stmt.executeQuery();

					if (rs.next()) {
						double databaseVersion = rs.getDouble("version");
						if (rs.wasNull()) 
							throw new SQLException("Error, could not detect database version!");

						return databaseVersion == Version.get(VersionType.DATABASE);
					}
				}
			}
		}
		finally {
			if (!isOpen)
				dataBase.close();
		}
		return false;
	}

	
	private TreeItem<TreeItemValue> saveVerticalDeflections(TreeItemType treeItemType, String itemName, boolean enable, List<VerticalDeflectionRow> verticalDeflections) throws SQLException {
		if (verticalDeflections == null || verticalDeflections.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, enable, false);
		try {
			SQLManager.getInstance().saveGroup((VerticalDeflectionTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((VerticalDeflectionTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (VerticalDeflectionRow row : verticalDeflections) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
				this.reservedVerticalDeflectionNames.add(row.getName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private TreeItem<TreeItemValue> savePoints(TreeItemType treeItemType, String itemName, boolean enable, List<PointRow> points) throws SQLException {
		if (points == null || points.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, enable, false);
		try {
			SQLManager.getInstance().saveGroup((PointTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((PointTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (PointRow row : points) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
				this.reservedPointNames.add(row.getName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private TreeItem<TreeItemValue> saveObservations(TreeItemType treeItemType, String groupName, boolean enable, List<ObservationRow> observations) throws SQLException {
		if (observations == null || observations.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, groupName, enable, false);

		try {
			SQLManager.getInstance().saveGroup((ObservationTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((ObservationTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (ObservationRow row : observations) {
				row.setGroupId(groupId);
				if (TreeItemType.isObservationTypeLeaf(treeItemType)) 
					SQLManager.getInstance().saveItem((TerrestrialObservationRow)row);
				else if (TreeItemType.isGNSSObservationTypeLeaf(treeItemType)) 
					SQLManager.getInstance().saveItem((GNSSObservationRow)row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private TreeItem<TreeItemValue> saveCongruenceAnalysisPairs(TreeItemType treeItemType, String groupName, boolean enable, List<CongruenceAnalysisRow> pairs) throws SQLException {
		if (pairs == null || pairs.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, groupName, enable, false);

		try {
			SQLManager.getInstance().saveGroup((CongruenceAnalysisTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((CongruenceAnalysisTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (CongruenceAnalysisRow row : pairs) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("OADBReader.extension.script", "HyperSQL"), "*.script")
		};
	}
}
