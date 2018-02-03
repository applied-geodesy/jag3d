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
import org.applied_geodesy.jag3d.ui.graphic.layer.ArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.util.sql.DataBase;

public class SQLGraphicManager {
	private final DataBase dataBase;

	public SQLGraphicManager(DataBase dataBase) {
		if (dataBase == null || !dataBase.isOpen())
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, database must be open! " + dataBase);
		this.dataBase = dataBase;
	}
	
	public void load(LayerManager layerManager) throws SQLException {
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
			}
		}
		
		layerTypes = new LayerType[] {
				LayerType.OBSERVATION_APOSTERIORI,
				LayerType.OBSERVATION_APRIORI,
				LayerType.ARROW
		};
		
		for (LayerType layerType : layerTypes) {
			switch(layerType) {
			case ARROW:
				ArrowLayer displacementLayer = (ArrowLayer)layerManager.getLayer(layerType);
				this.loadCongruenceAnalysisNexus(displacementLayer, completeAposterioriPointMap);
				break;
				
			case OBSERVATION_APOSTERIORI:
				ObservationLayer observationAposterioriLayer = (ObservationLayer) layerManager.getLayer(layerType);
				this.loadObservations(observationAposterioriLayer, completeAposterioriPointMap);
				break;
				
			case OBSERVATION_APRIORI:
				ObservationLayer observationAprioriLayer = (ObservationLayer) layerManager.getLayer(layerType);
				this.loadObservations(observationAprioriLayer, completeAprioriPointMap);
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
					"\"x0\", \"y0\", " +
					"\"y\", \"x\", " + 
					"\"helmert_major_axis\", \"helmert_minor_axis\", " +
					"0.5 * PI() + \"helmert_alpha\" AS \"helmert_alpha\", " + // sitch over to geodetic system north == x etc.
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
					boolean significant = rs.getBoolean("significant");

					GraphicPoint graphicPoint = new GraphicPoint(name, dimension, y, x, majorAxis, minorAxis, angle, significant);
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

		boolean selectAprioriValues = layerType == LayerType.OBSERVATION_APOSTERIORI;

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
//					double xs0 = rs.getDouble("xs0");
//					double ys0 = rs.getDouble("ys0");
//
//					double xe0 = rs.getDouble("xe0");
//					double ye0 = rs.getDouble("ye0");
//
//					GraphicPoint startPoint = new GraphicPoint(startPointName, ys0, xs0);
//					GraphicPoint endPoint   = new GraphicPoint(endPointName, ye0, xe0);
					
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
//					double xs = rs.getDouble("xs");
//					if (rs.wasNull())
//						continue;
//
//					double ys = rs.getDouble("ys");
//					if (rs.wasNull())
//						continue;
//
//					double xe = rs.getDouble("xe");
//					if (rs.wasNull())
//						continue;
//
//					double ye = rs.getDouble("ye");
//					if (rs.wasNull())
//						continue;
//
//					GraphicPoint startPoint = new GraphicPoint(startPointName, ys, xs);
//					GraphicPoint endPoint   = new GraphicPoint(endPointName, ye, xe);
					
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
	
	private void loadCongruenceAnalysisNexus(ArrowLayer arrowLayer, Map<String, GraphicPoint> completePointMap) throws SQLException {
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
				+ "AND \"StartPointGroup\".\"enable\" = TRUE AND \"EndPointGroup\".\"enable\" = TRUE";
		
		
		PreparedStatement stmt = this.dataBase.getPreparedStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			String startPointName = rs.getString("start_point_name");
			String endPointName   = rs.getString("end_point_name");
			// int dimension         = rs.getInt("dimension");
			
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
			
			RelativeConfidence relativeConfidence = new RelativeConfidence(startPoint, endPoint, majorAxis, minorAxis, angle, significant);
			relativeConfidence.setSignificant(significant);
			
			relativeConfidences.put(key, relativeConfidence);
		}
		
		arrowLayer.setRelativeConfidences(new ArrayList<RelativeConfidence>(relativeConfidences.values()));
	}
}
