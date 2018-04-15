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

package org.applied_geodesy.jag3d.ui.graphic.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.graphic.layer.ArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ConfidenceLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.HighlightableLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointShiftArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.util.sql.DataBase;

import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class SQLGraphicManager {
	private final DataBase dataBase;

	public SQLGraphicManager(DataBase dataBase) {
		if (dataBase == null || !dataBase.isOpen())
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database must be open! " + dataBase);
		this.dataBase = dataBase;
	}
	
	public void initLayer(LayerManager layerManager) throws SQLException {
		String sqlExists = "SELECT TRUE AS \"exists\" FROM \"Layer\" WHERE \"type\" = ?";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sqlExists);
		LayerType layerTyps[] = LayerType.values();
		int order = 0;
		for (LayerType layerType : layerTyps) {
			Layer layer = layerManager.getLayer(layerType);
			if (layer == null)
				continue;
			
			int idx = 1;
			stmt.setInt(idx++, layerType.getId());
			ResultSet rs = stmt.executeQuery();
			boolean exists = rs.next() && rs.getBoolean("exists") == Boolean.TRUE;

			switch (layerType) {
			case DATUM_POINT_APOSTERIORI:
			case DATUM_POINT_APRIORI:
			case NEW_POINT_APOSTERIORI:
			case NEW_POINT_APRIORI:
			case REFERENCE_POINT_APOSTERIORI:
			case REFERENCE_POINT_APRIORI:			
			case STOCHASTIC_POINT_APOSTERIORI:
			case STOCHASTIC_POINT_APRIORI:
				if (exists)
					this.load((PointLayer)layer);
				else
					this.save((PointLayer)layer, order++);
				break;

			case OBSERVATION_APOSTERIORI:
			case OBSERVATION_APRIORI:
				if (exists)
					this.load((ObservationLayer)layer);
				else
					this.save((ObservationLayer)layer, order++);
				break;
				
			case POINT_SHIFT:
			case PRINCIPAL_COMPONENT_HORIZONTAL:
			case PRINCIPAL_COMPONENT_VERTICAL:
				if (exists)
					this.load((ArrowLayer)layer);
				else
					this.save((ArrowLayer)layer, order++);
				break;

			case ABSOLUTE_CONFIDENCE:
			case RELATIVE_CONFIDENCE:
				if (exists)
					this.load((ConfidenceLayer<?>)layer);
				else
					this.save((ConfidenceLayer<?>)layer, order++);
				break;
			}
		}
		
		List<LayerType> layerOrder = new ArrayList<LayerType>();
		String sqlOrder = "SELECT \"type\" FROM \"Layer\" ORDER BY \"order\" ASC";
		stmt = this.dataBase.getPreparedStatement(sqlOrder);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			LayerType type = LayerType.getEnumByValue(rs.getInt("type"));
			if (type != null)
				layerOrder.add(type);
		}
		
		List<Layer> reorderedLayerList = FXCollections.observableArrayList();
		for (LayerType layerType : layerOrder) {
			reorderedLayerList.add(layerManager.getLayer(layerType));
		}
		layerManager.reorderLayer(reorderedLayerList);
	}
	
	public void loadEllipseScale(LayerManager layerManager) throws SQLException {
		String sql = "SELECT \"value\" FROM \"LayerEllipseScale\" WHERE \"id\" = 1 LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		if (rs.next())
			layerManager.setEllipseScale(rs.getDouble("value"));
	}
	
	public boolean load(GraphicExtent graphicExtent) throws SQLException {
		String sql = "SELECT \"min_x\", \"min_y\", \"max_x\", \"max_y\" FROM \"LayerExtent\" WHERE \"id\" = 1 LIMIT 1";
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			double xmin = rs.getDouble("min_x");
			double ymin = rs.getDouble("min_y");
			double xmax = rs.getDouble("max_x");
			double ymax = rs.getDouble("max_y");
			
			graphicExtent.set(xmin, ymin, xmax, ymax);
			return true;
		}
		return false;
	}
	
	public void load(LayerManager layerManager) throws SQLException {
		this.initLayer(layerManager);
		
		Map<String, GraphicPoint> completeAprioriPointMap     = new HashMap<String, GraphicPoint>();
		Map<String, GraphicPoint> completeAposterioriPointMap = new HashMap<String, GraphicPoint>();
		
		LayerType layerTypes[] = new LayerType[] {
				LayerType.DATUM_POINT_APOSTERIORI,
				LayerType.NEW_POINT_APOSTERIORI,
				LayerType.REFERENCE_POINT_APOSTERIORI,
				LayerType.STOCHASTIC_POINT_APOSTERIORI,
				
				LayerType.DATUM_POINT_APRIORI,
				LayerType.NEW_POINT_APRIORI,
				LayerType.REFERENCE_POINT_APRIORI,
				LayerType.STOCHASTIC_POINT_APRIORI,
		};
		
		for (LayerType layerType : layerTypes) {
			switch(layerType) {
			case DATUM_POINT_APOSTERIORI:
			case NEW_POINT_APOSTERIORI:
			case REFERENCE_POINT_APOSTERIORI:
			case STOCHASTIC_POINT_APOSTERIORI:
				PointLayer pointAposterioriLayer = (PointLayer) layerManager.getLayer(layerType);
				completeAposterioriPointMap.putAll(this.loadPoints(pointAposterioriLayer));
				break;
				
			case DATUM_POINT_APRIORI:
			case NEW_POINT_APRIORI:
			case REFERENCE_POINT_APRIORI:
			case STOCHASTIC_POINT_APRIORI:
				PointLayer pointAprioriLayer = (PointLayer) layerManager.getLayer(layerType);
				completeAprioriPointMap.putAll(this.loadPoints(pointAprioriLayer));
				break;

			case POINT_SHIFT:
			case PRINCIPAL_COMPONENT_HORIZONTAL:
			case PRINCIPAL_COMPONENT_VERTICAL:
			case OBSERVATION_APOSTERIORI:
			case OBSERVATION_APRIORI:
			case ABSOLUTE_CONFIDENCE:
			case RELATIVE_CONFIDENCE:
				break;
			}
		}
		
		layerTypes = new LayerType[] {
				LayerType.OBSERVATION_APOSTERIORI,
				LayerType.OBSERVATION_APRIORI,
				LayerType.POINT_SHIFT
		};
		
		for (LayerType layerType : layerTypes) {
			switch(layerType) {
			case POINT_SHIFT:
				PointShiftArrowLayer pointShiftArrowLayer = (PointShiftArrowLayer)layerManager.getLayer(layerType);
				this.loadCongruenceAnalysisNexus(pointShiftArrowLayer, completeAposterioriPointMap);
				break;
								
			case OBSERVATION_APOSTERIORI:
				ObservationLayer observationAposterioriLayer = (ObservationLayer) layerManager.getLayer(layerType);
				this.loadObservations(observationAposterioriLayer, completeAposterioriPointMap);
				break;
				
			case OBSERVATION_APRIORI:
				ObservationLayer observationAprioriLayer = (ObservationLayer) layerManager.getLayer(layerType);
				this.loadObservations(observationAprioriLayer, completeAprioriPointMap);
				break;
				
			case ABSOLUTE_CONFIDENCE:
			case RELATIVE_CONFIDENCE:
			case DATUM_POINT_APOSTERIORI:
			case DATUM_POINT_APRIORI:
			case NEW_POINT_APOSTERIORI:
			case NEW_POINT_APRIORI:
			case REFERENCE_POINT_APOSTERIORI:
			case REFERENCE_POINT_APRIORI:
			case STOCHASTIC_POINT_APOSTERIORI:
			case STOCHASTIC_POINT_APRIORI:
			case PRINCIPAL_COMPONENT_HORIZONTAL:
			case PRINCIPAL_COMPONENT_VERTICAL:
				break;
			}
		}
	}

	private Map<String, GraphicPoint> loadPoints(PointLayer pointLayer) throws SQLException {
		LayerType layerType = pointLayer.getLayerType();
		List<GraphicPoint> pointList = new ArrayList<GraphicPoint>();
		Map<String, GraphicPoint> pointMap = new HashMap<String, GraphicPoint>();
		PointType type = null;
		boolean selectAprioriValues = true;
		switch(layerType) {
		case DATUM_POINT_APOSTERIORI:
			type = PointType.DATUM_POINT;
			selectAprioriValues = false;
			break;

		case DATUM_POINT_APRIORI:
			type = PointType.DATUM_POINT;
			break;

		case NEW_POINT_APOSTERIORI:
			type = PointType.NEW_POINT;
			selectAprioriValues = false;
			break;

		case NEW_POINT_APRIORI:
			type = PointType.NEW_POINT;
			break;

		case REFERENCE_POINT_APOSTERIORI:
			type = PointType.REFERENCE_POINT;
			selectAprioriValues = false;
			break;

		case REFERENCE_POINT_APRIORI:
			type = PointType.REFERENCE_POINT;
			break;

		case STOCHASTIC_POINT_APOSTERIORI:
			type = PointType.STOCHASTIC_POINT;
			selectAprioriValues = false;
			break;

		case STOCHASTIC_POINT_APRIORI:
			type = PointType.STOCHASTIC_POINT;
			break;
			
		default: // load only point layers
			break;
		}

		if (type != null) {

			String sql = "SELECT " + 
					// Part: point
					"\"name\", " + 
					"\"y0\", \"x0\", " +
					"\"y\",  \"x\", " + 
					"\"helmert_major_axis\", \"helmert_minor_axis\", " +
					"0.5 * PI() + \"helmert_alpha\" AS \"helmert_alpha\", " + // switch over to geodetic system north == x etc.
					"\"first_principal_component_y\", \"first_principal_component_x\", \"first_principal_component_z\", " +
					"\"significant\", " +
					"\"dimension\" " + 
					"FROM \"PointApriori\" " + 
					"JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" " + 
					"LEFT JOIN \"PointAposteriori\" ON \"PointApriori\".\"id\" = \"PointAposteriori\".\"id\" " + 
					"WHERE \"PointGroup\".\"type\" = ? AND \"PointApriori\".\"enable\" = TRUE AND \"PointGroup\".\"enable\" = TRUE " +
					"ORDER BY \"PointGroup\".\"id\" ASC, \"PointApriori\".\"id\" ASC";

			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			int idx = 1;
			stmt.setInt(idx++, type.getId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String name = rs.getString("name");
				int dimension = rs.getInt("dimension");

				if (selectAprioriValues) {
					double x0 = rs.getDouble("x0");
					double y0 = rs.getDouble("y0");

					GraphicPoint graphicPoint = new GraphicPoint(name, dimension, y0, x0);
					pointList.add(graphicPoint);
					pointMap.put(name, graphicPoint);
				}
				else {
					double x = rs.getDouble("x");
					if (rs.wasNull())
						continue;

					double y = rs.getDouble("y");
					if (rs.wasNull())
						continue;

					double majorAxis = rs.getDouble("helmert_major_axis");
					double minorAxis = rs.getDouble("helmert_minor_axis");
					double angle     = rs.getDouble("helmert_alpha");
					
					double principleComponentX = rs.getDouble("first_principal_component_x");
					double principleComponentY = rs.getDouble("first_principal_component_y");
					double principleComponentZ = rs.getDouble("first_principal_component_z");
					
					boolean significant = rs.getBoolean("significant");

					GraphicPoint graphicPoint = new GraphicPoint(name, dimension, y, x, majorAxis, minorAxis, angle, principleComponentY, principleComponentX, principleComponentZ, significant);
					pointList.add(graphicPoint);
					pointMap.put(name, graphicPoint);
				}
			}
		}
		pointLayer.setPoints(pointList);
		return pointMap;
	}

	private void loadObservations(ObservationLayer observationLayer, Map<String, GraphicPoint> completePointMap) throws SQLException {
		LayerType layerType = observationLayer.getLayerType();
		Map<PointPairKey, ObservableMeasurement> observationMap = new HashMap<PointPairKey, ObservableMeasurement>();

		boolean selectAprioriValues = layerType == LayerType.OBSERVATION_APRIORI;

		String sql = "SELECT "
				+ "\"start_point_name\", \"end_point_name\", \"type\", "
				+ "\"StartPointApriori\".\"x0\" AS \"xs0\", \"StartPointApriori\".\"y0\" AS \"ys0\", "
				+ "\"StartPointAposteriori\".\"x\" AS \"xs\", \"StartPointAposteriori\".\"y\" AS \"ys\", "
				+ "\"EndPointApriori\".\"x0\" AS \"xe0\", \"EndPointApriori\".\"y0\" AS \"ye0\", "
				+ "\"EndPointAposteriori\".\"x\" AS \"xe\", \"EndPointAposteriori\".\"y\" AS \"ye\", "
				+ "\"ObservationAposteriori\".\"significant\" "
				+ "FROM \"ObservationApriori\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationGroup\".\"id\" = \"ObservationApriori\".\"group_id\" "
				+ "LEFT JOIN \"ObservationAposteriori\" ON \"ObservationAposteriori\".\"id\" = \"ObservationApriori\".\"id\" "
				+ "JOIN \"PointApriori\" AS \"StartPointApriori\" ON  \"StartPointApriori\".\"name\" = \"start_point_name\" "
				+ "JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"EndPointApriori\".\"name\" = \"end_point_name\" "
				+ "JOIN \"PointGroup\" AS \"StartPointGroup\" ON  \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" "
				+ "JOIN \"PointGroup\" AS \"EndPointGroup\" ON  \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" "
				+ "LEFT JOIN \"PointAposteriori\" AS \"StartPointAposteriori\" ON \"StartPointAposteriori\".\"id\" = \"StartPointApriori\".\"id\" "
				+ "LEFT JOIN \"PointAposteriori\" AS \"EndPointAposteriori\" ON \"EndPointAposteriori\".\"id\" = \"EndPointApriori\".\"id\" "
				+ "WHERE "
				+ "\"ObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE "
				+ "AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE "
				+ "AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE "
				+ "UNION ALL "
				+ "SELECT "
				+ "\"start_point_name\", \"end_point_name\", \"type\", "
				+ "\"StartPointApriori\".\"x0\" AS \"xs0\", \"StartPointApriori\".\"y0\" AS \"ys0\", "
				+ "\"StartPointAposteriori\".\"x\" AS \"xs\", \"StartPointAposteriori\".\"y\" AS \"ys\", "
				+ "\"EndPointApriori\".\"x0\" AS \"xe0\", \"EndPointApriori\".\"y0\" AS \"ye0\", "
				+ "\"EndPointAposteriori\".\"x\" AS \"xe\", \"EndPointAposteriori\".\"y\" AS \"ye\", "
				+ "\"GNSSObservationAposteriori\".\"significant\" "
				+ "FROM \"GNSSObservationApriori\" "
				+ "JOIN \"ObservationGroup\" ON \"ObservationGroup\".\"id\" = \"GNSSObservationApriori\".\"group_id\" "
				+ "LEFT JOIN \"GNSSObservationAposteriori\" ON \"GNSSObservationAposteriori\".\"id\" = \"GNSSObservationApriori\".\"id\" "
				+ "JOIN \"PointApriori\" AS \"StartPointApriori\" ON  \"StartPointApriori\".\"name\" = \"start_point_name\" "
				+ "JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"EndPointApriori\".\"name\" = \"end_point_name\" "
				+ "JOIN \"PointGroup\" AS \"StartPointGroup\" ON  \"StartPointGroup\".\"id\" = \"StartPointApriori\".\"group_id\" "
				+ "JOIN \"PointGroup\" AS \"EndPointGroup\" ON  \"EndPointGroup\".\"id\" = \"EndPointApriori\".\"group_id\" "
				+ "LEFT JOIN \"PointAposteriori\" AS \"StartPointAposteriori\" ON \"StartPointAposteriori\".\"id\" = \"StartPointApriori\".\"id\" "
				+ "LEFT JOIN \"PointAposteriori\" AS \"EndPointAposteriori\" ON \"EndPointAposteriori\".\"id\" = \"EndPointApriori\".\"id\" "
				+ "WHERE "
				+ "\"GNSSObservationApriori\".\"enable\" = TRUE AND \"ObservationGroup\".\"enable\" = TRUE "
				+ "AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE "
				+ "AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE";

		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");
			
			if (!completePointMap.containsKey(startPointName) || !completePointMap.containsKey(endPointName))
				continue;

			PointPairKey key = new PointPairKey(startPointName, endPointName);
			ObservationType observationType = ObservationType.getEnumByValue(rs.getInt("type"));

			if (selectAprioriValues) {
				if (!observationMap.containsKey(key)) {
					GraphicPoint startPoint = completePointMap.get(startPointName);
					GraphicPoint endPoint   = completePointMap.get(endPointName);
					
					observationMap.put(key, new ObservableMeasurement(startPoint, endPoint));
				}

				ObservableMeasurement observableLink = observationMap.get(key);

				switch(observationType) {
				case LEVELING:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.LEVELING);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.LEVELING);
					break;

				case DIRECTION:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.DIRECTION);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.DIRECTION);
					break;

				case HORIZONTAL_DISTANCE:
				case SLOPE_DISTANCE:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.DISTANCE);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.DISTANCE);
					break;

				case ZENITH_ANGLE:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.ZENITH_ANGLE);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.ZENITH_ANGLE);
					break;

				case GNSS1D:
				case GNSS2D:
				case GNSS3D:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.GNSS);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.GNSS);
					break;
				}
			}
			else {
				boolean significant = rs.getBoolean("significant");
				if (rs.wasNull())
					continue;

				if (!observationMap.containsKey(key)) {
					GraphicPoint startPoint = completePointMap.get(startPointName);
					GraphicPoint endPoint   = completePointMap.get(endPointName);

					observationMap.put(key, new ObservableMeasurement(startPoint, endPoint));
				}

				ObservableMeasurement observableLink = observationMap.get(key);
				if (significant)
					observableLink.setSignificant(significant);
					
				switch(observationType) {
				case LEVELING:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.LEVELING);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.LEVELING);
					break;

				case DIRECTION:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.DIRECTION);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.DIRECTION);
					break;

				case HORIZONTAL_DISTANCE:
				case SLOPE_DISTANCE:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.DISTANCE);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.DISTANCE);
					break;

				case ZENITH_ANGLE:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.ZENITH_ANGLE);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.ZENITH_ANGLE);
					break;

				case GNSS1D:
				case GNSS2D:
				case GNSS3D:
					if (startPointName.equals(observableLink.getStartPoint().getName()))
						observableLink.addStartPointObservationType(ObservationSymbolProperties.ObservationType.GNSS);
					else
						observableLink.addEndPointObservationType(ObservationSymbolProperties.ObservationType.GNSS);
					break;
				}

			}
		}
		observationLayer.setObservableMeasurements(new ArrayList<ObservableMeasurement>(observationMap.values()));
	}
	
	private void loadCongruenceAnalysisNexus(PointShiftArrowLayer pointShiftArrowLayer, Map<String, GraphicPoint> completePointMap) throws SQLException {
		Map<PointPairKey, RelativeConfidence> relativeConfidences = new HashMap<PointPairKey, RelativeConfidence>();

		String sql = "SELECT "
				+ "\"start_point_name\", \"end_point_name\", "
				+ "\"CongruenceAnalysisGroup\".\"dimension\", "
				+ "\"StartPointAposteriori\".\"x\" AS \"xs\", "
				+ "\"StartPointAposteriori\".\"y\" AS \"ys\", "
				+ "\"EndPointAposteriori\".\"x\" AS \"xe\", "
				+ "\"EndPointAposteriori\".\"y\" AS \"ye\", "
				+ "\"CongruenceAnalysisPointPairAposteriori\".\"confidence_major_axis_2d\", "
				+ "\"CongruenceAnalysisPointPairAposteriori\".\"confidence_minor_axis_2d\", "
				+ "0.5 * PI() + \"CongruenceAnalysisPointPairAposteriori\".\"confidence_alpha_2d\" AS \"confidence_alpha_2d\", "
				+ "\"CongruenceAnalysisPointPairAposteriori\".\"significant\" "
				+ "FROM \"CongruenceAnalysisPointPairApriori\" "
				+ "JOIN \"CongruenceAnalysisPointPairAposteriori\" ON \"CongruenceAnalysisPointPairApriori\".\"id\" = \"CongruenceAnalysisPointPairAposteriori\".\"id\" "
				+ "JOIN \"CongruenceAnalysisGroup\" ON \"CongruenceAnalysisPointPairApriori\".\"group_id\" = \"CongruenceAnalysisGroup\".\"id\" "
				+ "JOIN \"PointApriori\" AS \"StartPointApriori\" ON \"CongruenceAnalysisPointPairApriori\".\"start_point_name\" = \"StartPointApriori\".\"name\" "
				+ "JOIN \"PointApriori\" AS \"EndPointApriori\" ON \"CongruenceAnalysisPointPairApriori\".\"end_point_name\" = \"EndPointApriori\".\"name\" "
				+ "JOIN \"PointAposteriori\" AS \"StartPointAposteriori\" ON \"StartPointApriori\".\"id\" = \"StartPointAposteriori\".\"id\" "
				+ "JOIN \"PointAposteriori\" AS \"EndPointAposteriori\" ON \"EndPointApriori\".\"id\" = \"EndPointAposteriori\".\"id\" "
				+ "JOIN \"PointGroup\" AS \"StartPointGroup\" ON \"StartPointApriori\".\"group_id\" = \"StartPointGroup\".\"id\" "
				+ "JOIN \"PointGroup\" AS \"EndPointGroup\" ON \"EndPointApriori\".\"group_id\" = \"EndPointGroup\".\"id\" "
				+ "WHERE "
				+ "\"CongruenceAnalysisPointPairApriori\".\"enable\" = TRUE AND \"CongruenceAnalysisGroup\".\"enable\" = TRUE "
				+ "AND \"StartPointApriori\".\"enable\" = TRUE AND \"EndPointApriori\".\"enable\" = TRUE "
				+ "AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE "
				+ "UNION ALL "
				+ "SELECT "
				+ "\"name\" AS \"start_point_name\", \"name\" AS \"end_point_name\", "
				+ "\"dimension\", "
				+ "\"x\" - 0.5 * \"gross_error_x\" AS \"xs\", "
				+ "\"y\" - 0.5 * \"gross_error_y\" AS \"ys\", "
				+ "\"x\" + 0.5 * \"gross_error_x\" AS \"xe\", "
				+ "\"y\" + 0.5 * \"gross_error_y\" AS \"ye\", "
				+ "0 AS \"confidence_major_axis_2d\", "
				+ "0 AS \"confidence_minor_axis_2d\", "
				+ "0 AS \"confidence_alpha_2d\", "
				+ "\"significant\" "
				+ "FROM \"PointApriori\" "
				+ "JOIN \"PointAposteriori\" ON \"PointApriori\".\"id\" =  \"PointAposteriori\".\"id\" "
				+ "JOIN \"PointGroup\" ON \"PointApriori\".\"group_id\" = \"PointGroup\".\"id\" "
				+ "WHERE "
				+ "\"PointApriori\".\"enable\" = TRUE AND \"PointGroup\".\"enable\" = TRUE "
				+ "AND \"PointAposteriori\".\"significant\" = TRUE";
		
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");
			int dimension         = rs.getInt("dimension");
			
			double majorAxis      = rs.getDouble("confidence_major_axis_2d");
			double minorAxis      = rs.getDouble("confidence_minor_axis_2d");
			double angle          = rs.getDouble("confidence_alpha_2d");
		
			if (!completePointMap.containsKey(startPointName) || !completePointMap.containsKey(endPointName))
				continue;
			
			boolean significant = rs.getBoolean("significant");
			if (rs.wasNull())
				continue;

			PointPairKey key = new PointPairKey(startPointName, endPointName);
		
			if (relativeConfidences.containsKey(key))
				continue;
			
			GraphicPoint startPoint = completePointMap.get(startPointName);
			GraphicPoint endPoint   = completePointMap.get(endPointName);
			
			// shift values of reference points
			if (startPointName.equals(endPointName)) {
				double xs = rs.getDouble("xs");
				double ys = rs.getDouble("ys");
				double xe = rs.getDouble("xe");
				double ye = rs.getDouble("ye");
				
				startPoint = new GraphicPoint(startPointName, dimension, ys, xs);
				endPoint   = new GraphicPoint(endPointName,   dimension, ye, xe);
				
				startPoint.visibleProperty().bind(completePointMap.get(startPointName).visibleProperty());
				endPoint.visibleProperty().bind(completePointMap.get(endPointName).visibleProperty());
			}
			
			RelativeConfidence relativeConfidence = new RelativeConfidence(startPoint, endPoint, majorAxis, minorAxis, angle, significant);
			relativeConfidences.put(key, relativeConfidence);
		}
		
		pointShiftArrowLayer.setRelativeConfidences(new ArrayList<RelativeConfidence>(relativeConfidences.values()));
	}

	public void saveEllipseScale(double scale) throws SQLException {
		String sql = "MERGE INTO \"LayerEllipseScale\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"id\", \"value\") ON \"LayerEllipseScale\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"LayerEllipseScale\".\"value\" = \"vals\".\"value\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"value\" ";
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++,    1); // default id
		stmt.setDouble(idx++, scale);
		stmt.execute();
	}
	
	public void save(GraphicExtent extent) throws SQLException {
		String sql = "MERGE INTO \"LayerExtent\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"id\", \"min_x\", \"min_y\", \"max_x\", \"max_y\") ON \"LayerExtent\".\"id\" = \"vals\".\"id\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"LayerExtent\".\"min_x\" = \"vals\".\"min_x\", "
				+ "\"LayerExtent\".\"min_y\" = \"vals\".\"min_y\", "
				+ "\"LayerExtent\".\"max_x\" = \"vals\".\"max_x\", "
				+ "\"LayerExtent\".\"max_y\" = \"vals\".\"max_y\"  "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"id\", "
				+ "\"vals\".\"min_x\", "
				+ "\"vals\".\"min_y\", "
				+ "\"vals\".\"max_x\", "
				+ "\"vals\".\"max_y\" ";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++,     1); // default id
		stmt.setDouble(idx++,  extent.getMinX()); 
		stmt.setDouble(idx++,  extent.getMinY());
		stmt.setDouble(idx++,  extent.getMaxX());
		stmt.setDouble(idx++,  extent.getMaxY());	
		stmt.execute();
	}

	public void save(PointLayer pointLayer, int order) throws SQLException {
		this.saveLayer(pointLayer);
		this.saveLayerOrder(pointLayer.getLayerType(), order);
		this.saveFont(pointLayer);
		this.saveSymbolAndPointVisibleProperies(pointLayer); 
		
		switch(pointLayer.getLayerType()) {
		case DATUM_POINT_APOSTERIORI:
		case REFERENCE_POINT_APOSTERIORI:
		case REFERENCE_POINT_APRIORI:
		case STOCHASTIC_POINT_APOSTERIORI:
			this.save(pointLayer);
			break;
		default:
			break;
		}
	}
	
	public void save(ObservationLayer observationLayer, int order) throws SQLException {
		this.saveLayer(observationLayer);
		this.saveLayerOrder(observationLayer.getLayerType(), order);
		this.saveObservationLayerColors(observationLayer);
		
		if (observationLayer.getLayerType() == LayerType.OBSERVATION_APOSTERIORI)
			this.save(observationLayer);
	}
	
	public void save(ArrowLayer arrowLayer, int order) throws SQLException {
		this.saveLayer(arrowLayer);
		this.saveLayerOrder(arrowLayer.getLayerType(), order);
		this.saveSymbol(arrowLayer);
	}
	
	public void save(ConfidenceLayer<?> confidenceLayer, int order) throws SQLException {
		this.saveLayer(confidenceLayer);
		this.saveLayerOrder(confidenceLayer.getLayerType(), order);
		this.saveStrokeColor(confidenceLayer);
	}
	
	private void save(HighlightableLayer layer) throws SQLException {
		String sql = "MERGE INTO \"HighlightLayerProperty\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"layer\", \"red\", \"green\", \"blue\", \"line_width\") ON \"HighlightLayerProperty\".\"layer\" = \"vals\".\"layer\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"HighlightLayerProperty\".\"red\"       = \"vals\".\"red\", "
				+ "\"HighlightLayerProperty\".\"green\"      = \"vals\".\"green\", "
				+ "\"HighlightLayerProperty\".\"blue\"       = \"vals\".\"blue\", "
				+ "\"HighlightLayerProperty\".\"line_width\" = \"vals\".\"line_width\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"layer\", "
				+ "\"vals\".\"red\", "
				+ "\"vals\".\"green\", "
				+ "\"vals\".\"blue\","
				+ "\"vals\".\"line_width\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++,     layer.getLayerType().getId());
		stmt.setDouble(idx++,  layer.getHighlightColor().getRed());
		stmt.setDouble(idx++,  layer.getHighlightColor().getGreen());
		stmt.setDouble(idx++,  layer.getHighlightColor().getBlue());
		stmt.setDouble(idx++,  layer.getHighlightLineWidth());
		stmt.execute();
	}
	
	private void saveLayerOrder(LayerType type, int order) throws SQLException {
		String sql = "UPDATE \"Layer\" SET \"order\" = ? WHERE \"type\" = ?";
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, order);
		stmt.setInt(idx++, type.getId());
		stmt.execute();
	}
	
	private void saveStrokeColor(ConfidenceLayer<?> confidenceLayer) throws SQLException {
		String sql = "MERGE INTO \"ConfidenceLayerProperty\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"layer\", \"red\", \"green\", \"blue\") ON \"ConfidenceLayerProperty\".\"layer\" = \"vals\".\"layer\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ConfidenceLayerProperty\".\"red\"   = \"vals\".\"red\", "
				+ "\"ConfidenceLayerProperty\".\"green\" = \"vals\".\"green\", "
				+ "\"ConfidenceLayerProperty\".\"blue\"  = \"vals\".\"blue\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"layer\", "
				+ "\"vals\".\"red\", "
				+ "\"vals\".\"green\", "
				+ "\"vals\".\"blue\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++,     confidenceLayer.getLayerType().getId());
		stmt.setDouble(idx++,  confidenceLayer.getStrokeColor().getRed());
		stmt.setDouble(idx++,  confidenceLayer.getStrokeColor().getGreen());
		stmt.setDouble(idx++,  confidenceLayer.getStrokeColor().getBlue());
		stmt.execute();
	}

	private void saveObservationLayerColors(ObservationLayer observationLayer) throws SQLException {
		boolean hasBatch = false;
		try {
			this.dataBase.setAutoCommit(false);
			String sql = "MERGE INTO \"ObservationLayerProperty\" USING (VALUES "
					+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS BOOLEAN)) "
					+ ") AS \"vals\" (\"layer\", \"observation_type\", \"red\", \"green\", \"blue\", \"visible\") "
					+ "ON \"ObservationLayerProperty\".\"layer\" = \"vals\".\"layer\" AND \"ObservationLayerProperty\".\"observation_type\" = \"vals\".\"observation_type\" "
					+ "WHEN MATCHED THEN UPDATE SET "
					+ "\"ObservationLayerProperty\".\"red\"     = \"vals\".\"red\", "
					+ "\"ObservationLayerProperty\".\"green\"   = \"vals\".\"green\", "
					+ "\"ObservationLayerProperty\".\"blue\"    = \"vals\".\"blue\", "
					+ "\"ObservationLayerProperty\".\"visible\" = \"vals\".\"visible\" "
					+ "WHEN NOT MATCHED THEN INSERT VALUES "
					+ "\"vals\".\"layer\", "
					+ "\"vals\".\"observation_type\", "
					+ "\"vals\".\"red\", "
					+ "\"vals\".\"green\", "
					+ "\"vals\".\"blue\", "
					+ "\"vals\".\"visible\" ";

			int idx = 1;
			PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
			stmt.setInt(idx++, observationLayer.getLayerType().getId());

			for (ObservationSymbolProperties.ObservationType observationType : ObservationSymbolProperties.ObservationType.values()) {
				idx = 2;
				ObservationSymbolProperties properties = observationLayer.getObservationSymbolProperties(observationType);
				if (properties == null)
					continue;
				stmt.setInt(idx++,     observationType.getId());
				stmt.setDouble(idx++,  properties.getColor().getRed());
				stmt.setDouble(idx++,  properties.getColor().getGreen());
				stmt.setDouble(idx++,  properties.getColor().getBlue());
				stmt.setBoolean(idx++, properties.isVisible());
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

	private void saveLayer(Layer layer) throws SQLException {
		String sql = "MERGE INTO \"Layer\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS INT), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"type\", \"red\", \"green\", \"blue\", \"symbol_size\", \"line_width\", \"order\", \"visible\") ON \"Layer\".\"type\" = \"vals\".\"type\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"Layer\".\"red\"         = \"vals\".\"red\", "
				+ "\"Layer\".\"green\"       = \"vals\".\"green\", "
				+ "\"Layer\".\"blue\"        = \"vals\".\"blue\", "
				+ "\"Layer\".\"symbol_size\" = \"vals\".\"symbol_size\", "
				+ "\"Layer\".\"line_width\"  = \"vals\".\"line_width\", "
				+ "\"Layer\".\"order\"       = \"vals\".\"order\", "
				+ "\"Layer\".\"visible\"     = \"vals\".\"visible\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"red\", "
				+ "\"vals\".\"green\", "
				+ "\"vals\".\"blue\", "
				+ "\"vals\".\"symbol_size\", "
				+ "\"vals\".\"line_width\", "
				+ "\"vals\".\"order\", "
				+ "\"vals\".\"visible\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++,     layer.getLayerType().getId());
		stmt.setDouble(idx++,  layer.getColor().getRed());
		stmt.setDouble(idx++,  layer.getColor().getGreen());
		stmt.setDouble(idx++,  layer.getColor().getBlue());
		stmt.setDouble(idx++,  layer.getSymbolSize());
		stmt.setDouble(idx++,  layer.getLineWidth());
		stmt.setInt(idx++,     -1);
		stmt.setBoolean(idx++, layer.isVisible());
		stmt.execute();
	}
	
	private void saveSymbol(ArrowLayer arrowLayer) throws SQLException {
		String sql = "MERGE INTO \"ArrowLayerProperty\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT)) "
				+ ") AS \"vals\" (\"layer\", \"type\") ON \"ArrowLayerProperty\".\"layer\" = \"vals\".\"layer\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"ArrowLayerProperty\".\"type\" = \"vals\".\"type\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"layer\", "
				+ "\"vals\".\"type\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, arrowLayer.getLayerType().getId());
		stmt.setInt(idx++, arrowLayer.getSymbolType().getId());
		stmt.execute();
	}
	
	private void saveSymbolAndPointVisibleProperies(PointLayer pointLayer) throws SQLException {
		String sql = "MERGE INTO \"PointLayerProperty\" USING (VALUES "
				+ "(CAST(? AS INT), CAST(? AS INT), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN)) "
				+ ") AS \"vals\" (\"layer\", \"type\", \"point_1d_visible\", \"point_2d_visible\", \"point_3d_visible\") ON \"PointLayerProperty\".\"layer\" = \"vals\".\"layer\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"PointLayerProperty\".\"type\" = \"vals\".\"type\", "
				+ "\"PointLayerProperty\".\"point_1d_visible\" = \"vals\".\"point_1d_visible\", "
				+ "\"PointLayerProperty\".\"point_2d_visible\" = \"vals\".\"point_2d_visible\", "
				+ "\"PointLayerProperty\".\"point_3d_visible\" = \"vals\".\"point_3d_visible\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"layer\", "
				+ "\"vals\".\"type\", "
				+ "\"vals\".\"point_1d_visible\", "
				+ "\"vals\".\"point_2d_visible\", "
				+ "\"vals\".\"point_3d_visible\" ";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, pointLayer.getLayerType().getId());
		stmt.setInt(idx++, pointLayer.getPointSymbolType().getId());
		stmt.setBoolean(idx++, pointLayer.isPoint1DVisible());
		stmt.setBoolean(idx++, pointLayer.isPoint2DVisible());
		stmt.setBoolean(idx++, pointLayer.isPoint3DVisible());
		stmt.execute();
	}
	
	private void saveFont(PointLayer pointLayer) throws SQLException {
		String sql = "MERGE INTO \"LayerFont\" USING (VALUES "
				+ "(CAST(? AS INT), ?, CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE), CAST(? AS DOUBLE)) "
				+ ") AS \"vals\" (\"layer\", \"family\", \"size\", \"red\", \"green\", \"blue\") ON \"LayerFont\".\"layer\" = \"vals\".\"layer\" "
				+ "WHEN MATCHED THEN UPDATE SET "
				+ "\"LayerFont\".\"family\" = \"vals\".\"family\", "
				+ "\"LayerFont\".\"size\"   = \"vals\".\"size\", "
				+ "\"LayerFont\".\"red\"    = \"vals\".\"red\", "
				+ "\"LayerFont\".\"green\"  = \"vals\".\"green\", "
				+ "\"LayerFont\".\"blue\"   = \"vals\".\"blue\" "
				+ "WHEN NOT MATCHED THEN INSERT VALUES "
				+ "\"vals\".\"layer\", "
				+ "\"vals\".\"family\", "
				+ "\"vals\".\"size\", "
				+ "\"vals\".\"red\", "
				+ "\"vals\".\"green\", "
				+ "\"vals\".\"blue\" ";
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, pointLayer.getLayerType().getId());
		stmt.setString(idx++, pointLayer.getFontFamily());
		stmt.setDouble(idx++, pointLayer.getFontSize());
		stmt.setDouble(idx++, pointLayer.getFontColor().getRed());
		stmt.setDouble(idx++, pointLayer.getFontColor().getGreen());
		stmt.setDouble(idx++, pointLayer.getFontColor().getBlue());
		stmt.execute();
	}
	
	private void load(ObservationLayer observationLayer) throws SQLException {
		this.loadLayer(observationLayer);
		this.loadObservationColors(observationLayer);
		
		if (observationLayer.getLayerType() == LayerType.OBSERVATION_APOSTERIORI)
			this.loadHighlightProperties(observationLayer);
	}

	private void load(PointLayer pointLayer) throws SQLException {
		this.loadLayer(pointLayer);
		this.loadFont(pointLayer);
		this.loadSymbolAndPointVisibleProperties(pointLayer);
		
		switch(pointLayer.getLayerType()) {
		case DATUM_POINT_APOSTERIORI:
		case REFERENCE_POINT_APOSTERIORI:
		case REFERENCE_POINT_APRIORI:
		case STOCHASTIC_POINT_APOSTERIORI:
			this.loadHighlightProperties(pointLayer);
			break;
		default:
			break;
		}
	}
	
	private void loadHighlightProperties(HighlightableLayer layer) throws SQLException {
		String sql = "SELECT "
				+ "\"red\", \"green\", \"blue\", \"line_width\" "
				+ "FROM \"HighlightLayerProperty\" "
				+ "WHERE \"layer\" = ? LIMIT 1";

		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, layer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			double opacity = 1.0;

			double red = rs.getDouble("red");
			red = Math.min(Math.max(0, red), 1);

			double green = rs.getDouble("green");
			green = Math.min(Math.max(0, green), 1);

			double blue = rs.getDouble("blue");
			blue = Math.min(Math.max(0, blue), 1);

			double lineWidth = rs.getDouble("line_width");
			
			layer.setHighlightColor(new Color(red, green, blue, opacity));
			layer.setHighlightLineWidth(lineWidth >= 0 ? lineWidth : 0);
		}
	}

	private void load(ArrowLayer arrowLayer) throws SQLException {
		this.loadLayer(arrowLayer);
		this.loadSymbol(arrowLayer);
	}
	
	private void load(ConfidenceLayer<?> confidenceLayer) throws SQLException {
		this.loadLayer(confidenceLayer);
		this.loadStrokeColor(confidenceLayer);
	}

	private void loadSymbolAndPointVisibleProperties(PointLayer pointLayer) throws SQLException {
		String sql = "SELECT "
				+ "\"type\", \"point_1d_visible\", \"point_2d_visible\", \"point_3d_visible\" "
				+ "FROM \"PointLayerProperty\" "
				+ "WHERE \"layer\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, pointLayer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			PointSymbolType pointSymbolType = PointSymbolType.getEnumByValue(rs.getInt("type"));
			if (pointSymbolType == null)
				return;

			pointLayer.setSymbolType(pointSymbolType);
			pointLayer.setPoint1DVisible(rs.getBoolean("point_1d_visible"));
			pointLayer.setPoint2DVisible(rs.getBoolean("point_2d_visible"));
			pointLayer.setPoint3DVisible(rs.getBoolean("point_3d_visible"));
		}
	}
	
	private void loadStrokeColor(ConfidenceLayer<?> confidenceLayer) throws SQLException {
		String sql = "SELECT "
				+ "\"red\", \"green\", \"blue\" "
				+ "FROM \"ConfidenceLayerProperty\" "
				+ "WHERE \"layer\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, confidenceLayer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			double opacity = 1.0;
			
			double red = rs.getDouble("red");
			red = Math.min(Math.max(0, red), 1);
			
			double green = rs.getDouble("green");
			green = Math.min(Math.max(0, green), 1);
			
			double blue = rs.getDouble("blue");
			blue = Math.min(Math.max(0, blue), 1);
			
			confidenceLayer.setStrokeColor(new Color(red, green, blue, opacity));
		}
	}

	private void loadSymbol(ArrowLayer arrowLayer) throws SQLException {
		String sql = "SELECT "
				+ "\"type\" "
				+ "FROM \"ArrowLayerProperty\" "
				+ "WHERE \"layer\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, arrowLayer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			ArrowSymbolType arrowSymbolType = ArrowSymbolType.getEnumByValue(rs.getInt("type"));
			if (arrowSymbolType == null)
				return;

			arrowLayer.setSymbolType(arrowSymbolType);
		}
	}
	
	private void loadFont(PointLayer layer) throws SQLException {
		String sql = " SELECT "
				+ "\"family\", \"size\", "
				+ "\"red\", \"green\", \"blue\" "
				+ "FROM \"LayerFont\" "
				+ "WHERE \"layer\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, layer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()) {
			String fontFamily = rs.getString("family");
			double fontSize   = rs.getDouble("size");
			
			double opacity = 1.0;
			
			double red = rs.getDouble("red");
			red = Math.min(Math.max(0, red), 1);
			
			double green = rs.getDouble("green");
			green = Math.min(Math.max(0, green), 1);
			
			double blue = rs.getDouble("blue");
			blue = Math.min(Math.max(0, blue), 1);
			
			layer.setFontColor(new Color(red, green, blue, opacity));
			layer.setFontFamily(fontFamily);
			layer.setFontSize(fontSize);
		}
	}
	
	private void loadLayer(Layer layer) throws SQLException {
		String sql = "SELECT "
				+ "\"red\", \"green\", \"blue\", \"symbol_size\", \"line_width\", \"visible\" "
				+ "FROM \"Layer\" "
				+ "WHERE \"type\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, layer.getLayerType().getId());
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			double opacity = 1.0;
			
			double red = rs.getDouble("red");
			red = Math.min(Math.max(0, red), 1);
			
			double green = rs.getDouble("green");
			green = Math.min(Math.max(0, green), 1);
			
			double blue = rs.getDouble("blue");
			blue = Math.min(Math.max(0, blue), 1);
			
			double symbolSize = rs.getDouble("symbol_size");
			symbolSize = Math.max(0, symbolSize);
			
			double lineWidth = rs.getDouble("line_width");
			lineWidth = Math.max(0, lineWidth);
			
			boolean visible = rs.getBoolean("visible");

			layer.setColor(new Color(red, green, blue, opacity));
			layer.setSymbolSize(symbolSize);
			layer.setLineWidth(lineWidth);
			layer.setVisible(visible);
		}
	}
	
	private void loadObservationColors(ObservationLayer observationLayer) throws SQLException {
		String sql = "SELECT "
				+ "\"observation_type\", "
				+ "\"red\", \"green\", \"blue\", \"visible\" "
				+ "FROM \"ObservationLayerProperty\" "
				+ "WHERE \"layer\" = ? AND \"observation_type\" = ? LIMIT 1";
		
		int idx = 1;
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		stmt.setInt(idx++, observationLayer.getLayerType().getId());
		
		for (ObservationSymbolProperties.ObservationType observationType : ObservationSymbolProperties.ObservationType.values()) {
			stmt.setInt(idx, observationType.getId());
			ResultSet rs = stmt.executeQuery();
			ObservationSymbolProperties properties = observationLayer.getObservationSymbolProperties(observationType);
			
			if (rs.next() && properties != null) {
				double opacity = 1.0;
				
				double red = rs.getDouble("red");
				red = Math.min(Math.max(0, red), 1);
				
				double green = rs.getDouble("green");
				green = Math.min(Math.max(0, green), 1);
				
				double blue = rs.getDouble("blue");
				blue = Math.min(Math.max(0, blue), 1);
				
				boolean visible = rs.getBoolean("visible");

				properties.setColor(new Color(red, green, blue, opacity));
				properties.setVisible(visible);
			}
		}
	}
}
