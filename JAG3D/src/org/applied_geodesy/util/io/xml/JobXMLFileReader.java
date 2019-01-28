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

package org.applied_geodesy.util.io.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.XMLUtilities;
import org.applied_geodesy.util.io.DimensionType;
import org.applied_geodesy.util.io.SourceFileReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

public class JobXMLFileReader extends SourceFileReader implements ErrorHandler {
	private class StationRecord {
		private String stationName;
		private double ih = 0, ppm = 0, refraction = 0;
		private boolean applyEarthCurveCorrection = false;
		public StationRecord(String stationName, double ih, double ppm, double refraction, boolean applyEarthCurveCorrection) {
			this.stationName = stationName;
			this.ih = ih;
			this.ppm = ppm;
			this.refraction = refraction;
			this.applyEarthCurveCorrection = applyEarthCurveCorrection;
		}
		public double getInstrumentHeight() {
			return this.ih;
		}
		public String getStationName() {
			return this.stationName;
		}
		public double getAtmospherePPMValue() {
			return this.ppm;
		}
		public double getAtmosphereRefractionValue() {
			return this.refraction;
		}
		public boolean applyEarthCurveCorrection() {
			return this.applyEarthCurveCorrection;
		}
		@Override
		public String toString() {
			return "StationRecord [stationName=" + stationName + ", ih=" + ih + "]";
		}
	}
	
	private Map<String, StationRecord> stations = new LinkedHashMap<String, StationRecord>();
	private boolean isValidDocument = true;
	private final DimensionType dim;
	
	private List<TerrestrialObservationRow> leveling = null;
	
	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions = null;
	
	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles = null;
	
	private List<PointRow> points2d = null;
	private List<PointRow> points3d = null;
	
	private Set<String> pointNames = null;
	
	private TreeItem<TreeItemValue> lastTreeItem = null;
	
	public JobXMLFileReader(DimensionType dim) {
		this.dim = dim == DimensionType.PLAN ? DimensionType.PLAN : dim == DimensionType.PLAN_AND_HEIGHT ? DimensionType.PLAN_AND_HEIGHT : DimensionType.SPATIAL;
		this.reset();
	}
	
	public JobXMLFileReader(Path p, DimensionType dim) {
		super(p);
		this.dim = dim == DimensionType.PLAN ? DimensionType.PLAN : dim == DimensionType.PLAN_AND_HEIGHT ? DimensionType.PLAN_AND_HEIGHT : DimensionType.SPATIAL;
		this.reset();
	}
	
	public JobXMLFileReader(String xmlFileName, DimensionType dim)  {
		this(new File(xmlFileName), dim);
	}

	public JobXMLFileReader(File xmlFile, DimensionType dim)  {
		this(xmlFile.toPath(), dim);
	}
	
	public boolean isValidDocument() {
		return this.isValidDocument;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		e.getMessage();
		this.isValidDocument = false;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		e.getMessage();
		this.isValidDocument = false;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		e.getMessage();	
	}

	@Override
	public void parse(String line) {}

	@Override
	public void reset() {

		if (this.points2d == null)
			this.points2d = new ArrayList<PointRow>();
		if (this.points3d == null)
			this.points3d = new ArrayList<PointRow>();
		
		if (this.leveling == null)
			this.leveling = new ArrayList<TerrestrialObservationRow>();
		if (this.horizontalDistances == null)
			this.horizontalDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.directions == null)
			this.directions = new ArrayList<TerrestrialObservationRow>();
		if (this.slopeDistances == null)
			this.slopeDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.zenithAngles == null)
			this.zenithAngles = new ArrayList<TerrestrialObservationRow>();

		if (this.pointNames == null)
			this.pointNames = new HashSet<String>();
		if (this.stations == null)
			this.stations = new LinkedHashMap<String, StationRecord>();
		
		this.stations.clear();

		this.pointNames.clear();		
		this.points2d.clear();
		this.points3d.clear();

		this.leveling.clear();
		
		this.horizontalDistances.clear();
		this.directions.clear();

		this.slopeDistances.clear();
		this.zenithAngles.clear();

	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.lastTreeItem = null;
		this.pointNames.addAll(SQLManager.getInstance().getFullPointNameSet());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
        DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			File xmlFile = this.getPath().toFile();
			Document document = builder.parse(xmlFile);
			
			JobXMLNamespaceContext namespaceContextJobXML = new JobXMLNamespaceContext(document);

			String xpathPattern = "//JOBFile/FieldBook/PointRecord/Grid | "
					+ "//JOBFile/FieldBook/PointRecord/ComputedGrid | "
					+ "//JOBFile/Reductions/Point/Grid";
			
			NodeList nodeList = (NodeList)XMLUtilities.xpathSearch(document, xpathPattern, namespaceContextJobXML, XPathConstants.NODESET);
			for (int i=0; i<nodeList.getLength(); i++) {
				Node pointNode = nodeList.item(i);
				
				String pointName = (String)XMLUtilities.xpathSearch(pointNode, "../Name",    namespaceContextJobXML, XPathConstants.STRING);
				String pointCode = (String)XMLUtilities.xpathSearch(pointNode, "../Code",    namespaceContextJobXML, XPathConstants.STRING);
				String deleted   = (String)XMLUtilities.xpathSearch(pointNode, "../Deleted", namespaceContextJobXML, XPathConstants.STRING);
				boolean isDeleted = deleted != null && Boolean.parseBoolean(deleted);
				
				if (isDeleted)
					continue;
				
				Double x0 = (Double)XMLUtilities.xpathSearch(pointNode, "North",     namespaceContextJobXML, XPathConstants.NUMBER);
				Double y0 = (Double)XMLUtilities.xpathSearch(pointNode, "East",      namespaceContextJobXML, XPathConstants.NUMBER);
				Double z0 = (Double)XMLUtilities.xpathSearch(pointNode, "Elevation", namespaceContextJobXML, XPathConstants.NUMBER);

				if (pointName != null && !pointName.trim().isEmpty() && !this.pointNames.contains(pointName)) {
					x0 = x0 == null || Double.isNaN(x0) ||  Double.isInfinite(x0) ? 0.0 : x0;
					y0 = y0 == null || Double.isNaN(y0) ||  Double.isInfinite(y0) ? 0.0 : y0;
					z0 = z0 == null || Double.isNaN(z0) ||  Double.isInfinite(z0) ? 0.0 : z0;
					
					this.pointNames.add(pointName);
					
					PointRow point = new PointRow();
					point.setName(pointName);
					if (pointCode != null)
						point.setCode(pointCode);
					
					if (this.dim != DimensionType.HEIGHT) {
						point.setXApriori(x0);
						point.setYApriori(y0);
					}
					if (this.dim != DimensionType.PLAN)
						point.setZApriori(z0);
					
					if (this.dim == DimensionType.PLAN)
						this.points2d.add(point);
					else 
						this.points3d.add(point);
				}
			}

			// Bestimme Stationen
			xpathPattern = "//JOBFile/FieldBook/StationRecord";
			nodeList = (NodeList)XMLUtilities.xpathSearch(document, xpathPattern, namespaceContextJobXML, XPathConstants.NODESET);
			for (int i=0; i<nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node != null && node.hasChildNodes()) {
					NamedNodeMap attr = node.getAttributes();
					String stationId = attr.getNamedItem("ID") == null ? null : attr.getNamedItem("ID").getNodeValue();
					String stationName   = (String)XMLUtilities.xpathSearch(node, "StationName",      namespaceContextJobXML, XPathConstants.STRING);
					Double stationHeight = (Double)XMLUtilities.xpathSearch(node, "TheodoliteHeight", namespaceContextJobXML, XPathConstants.NUMBER);
					String atmosphereId  = (String)XMLUtilities.xpathSearch(node, "AtmosphereID",     namespaceContextJobXML, XPathConstants.STRING);
					stationHeight = stationHeight == null || Double.isNaN(stationHeight) ||  Double.isInfinite(stationHeight) ? 0.0 : stationHeight;

					if (stationId != null && !stationId.trim().isEmpty() && stationName != null && !stationName.trim().isEmpty()) {
						xpathPattern = "//JOBFile/FieldBook/AtmosphereRecord[@ID=\"%s\"]";
						Node atmNode = (Node)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPattern, atmosphereId), namespaceContextJobXML, XPathConstants.NODE);
						
						Double ppm = 0.0, refraction = 0.0;
						boolean applyEarthCurveCorr = false;
						if (atmNode != null && atmNode.hasChildNodes()) {
							String applyPPM        = (String)XMLUtilities.xpathSearch(atmNode, "ApplyPPMToRawDistances",    namespaceContextJobXML, XPathConstants.STRING);
							String applyRefraction = (String)XMLUtilities.xpathSearch(atmNode, "ApplyRefractionCorrection", namespaceContextJobXML, XPathConstants.STRING);
							
							String applyEarthCurve = (String)XMLUtilities.xpathSearch(atmNode, "ApplyEarthCurvatureCorrection", namespaceContextJobXML, XPathConstants.STRING);
							ppm = (Double)XMLUtilities.xpathSearch(atmNode, "PPM", namespaceContextJobXML, XPathConstants.NUMBER);
							refraction = (Double)XMLUtilities.xpathSearch(atmNode, "RefractionCoefficient", namespaceContextJobXML, XPathConstants.NUMBER);
							
							boolean applyPPMCorr = applyPPM != null && Boolean.parseBoolean(applyPPM);
							boolean applyRefractionCorr = applyRefraction != null && Boolean.parseBoolean(applyRefraction);
							applyEarthCurveCorr = applyEarthCurve != null && Boolean.parseBoolean(applyEarthCurve);
							
							ppm = !applyPPMCorr || ppm == null || Double.isNaN(ppm) || Double.isInfinite(ppm) ? 0.0 : ppm;
							refraction = !applyRefractionCorr || refraction == null || Double.isNaN(refraction) || Double.isInfinite(refraction) ? 0.0 : refraction;
						}
						this.stations.put(stationId, new StationRecord(stationName, stationHeight, ppm, refraction, applyEarthCurveCorr));
						if (!this.pointNames.contains(stationName)) {
							this.pointNames.add(stationName);
							PointRow point = new PointRow();
							point.setName(stationName);
							
							if (this.dim != DimensionType.HEIGHT) {
								point.setXApriori(0.0);
								point.setYApriori(0.0);
							}
							if (this.dim != DimensionType.PLAN)
								point.setZApriori(0.0);
							
							if (this.dim == DimensionType.PLAN)
								this.points2d.add(point);
							else 
								this.points3d.add(point);
						}
					}
				}
			}
			
			xpathPattern = "//JOBFile/FieldBook/PointRecord[StationID=\"%s\"]";
			for (String stationId : this.stations.keySet()) {
				StationRecord station = this.stations.get(stationId);
				double ppm              = station.getAtmospherePPMValue();
				double refraction       = station.getAtmosphereRefractionValue();
				boolean applyEarthCurve = station.applyEarthCurveCorrection();
				final double R          = Constant.EARTH_RADIUS; 
				nodeList = (NodeList)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPattern, stationId), namespaceContextJobXML, XPathConstants.NODESET);
				for (int i=0; i<nodeList.getLength(); i++) {
					Node stationNode  = nodeList.item(i);
					String targetID   = (String)XMLUtilities.xpathSearch(stationNode, "TargetID", namespaceContextJobXML, XPathConstants.STRING);
					String targetName = (String)XMLUtilities.xpathSearch(stationNode, "Name",     namespaceContextJobXML, XPathConstants.STRING);
					String deleted    = (String)XMLUtilities.xpathSearch(stationNode, "Deleted",  namespaceContextJobXML, XPathConstants.STRING);
					boolean isDeleted = deleted != null && Boolean.parseBoolean(deleted);
					
					// Keine Punktnummer fuer den Zielpunkt vorhanden oder Messung als geloescht markiert
					if (isDeleted || targetName == null || targetName.trim().isEmpty())
						continue;
					
					Double direction   = (Double)XMLUtilities.xpathSearch(stationNode, "Circle/HorizontalCircle", namespaceContextJobXML, XPathConstants.NUMBER);
					Double zenithAngle = (Double)XMLUtilities.xpathSearch(stationNode, "Circle/VerticalCircle",   namespaceContextJobXML, XPathConstants.NUMBER);
					Double distance3d  = (Double)XMLUtilities.xpathSearch(stationNode, "Circle/EDMDistance",      namespaceContextJobXML, XPathConstants.NUMBER);
					String faceType    = (String)XMLUtilities.xpathSearch(stationNode, "Circle/Face",             namespaceContextJobXML, XPathConstants.STRING);
					
					boolean isFaceI    = true;
					//Valid values Face1, Face2 *AND* FaceNull
					if (faceType != null && !faceType.equalsIgnoreCase("Face1") || zenithAngle != null && !Double.isNaN(zenithAngle) && !Double.isInfinite(zenithAngle) && zenithAngle > 180.0) {
						isFaceI = false;
					}

					// Reduziere auf Lage I und Umrechnung in RAD
					if (direction != null && !Double.isNaN(direction) && !Double.isInfinite(direction)) {
						direction = direction * Constant.RHO_DEG2RAD;
						
						if (!isFaceI)
							direction = MathExtension.MOD(direction + Math.PI, 2.0*Math.PI);
					}
					
					if (zenithAngle != null && !Double.isNaN(zenithAngle) && !Double.isInfinite(zenithAngle)) {
						zenithAngle = zenithAngle * Constant.RHO_DEG2RAD;
						
						if (!isFaceI)
							zenithAngle = MathExtension.MOD(2.0*Math.PI - zenithAngle, 2.0*Math.PI);
					}

					Double prismConstant = 0.0;
					Double targetHeight  = 0.0;
					
					if (targetID != null && !targetID.trim().isEmpty()) {
						String xpathPatternTargetRecord = "//JOBFile/FieldBook/TargetRecord[@ID=\"%s\"]";	
						Node targetNode = (Node)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPatternTargetRecord, targetID), namespaceContextJobXML, XPathConstants.NODE);

						prismConstant = (Double)XMLUtilities.xpathSearch(targetNode, "PrismConstant", namespaceContextJobXML, XPathConstants.NUMBER);
						targetHeight  = (Double)XMLUtilities.xpathSearch(targetNode, "TargetHeight",  namespaceContextJobXML, XPathConstants.NUMBER);
						
						prismConstant = prismConstant == null || Double.isNaN(prismConstant) ||  Double.isInfinite(prismConstant) ? 0.0 : prismConstant;
						targetHeight  = targetHeight == null  || Double.isNaN(targetHeight)  ||  Double.isInfinite(targetHeight)  ? 0.0 : targetHeight;
					}

					double distanceForUncertaintyModel = 0;
					if (this.dim == DimensionType.SPATIAL) {
						if (distance3d != null && !Double.isNaN(distance3d) && !Double.isInfinite(distance3d) && distance3d > 0) {
							TerrestrialObservationRow distanceRow = new TerrestrialObservationRow();
							distanceRow.setInstrumentHeight(station.getInstrumentHeight());
							distanceRow.setStartPointName(station.getStationName());
							distanceRow.setReflectorHeight(targetHeight);
							distanceRow.setEndPointName(targetName);
							distanceRow.setValueApriori( (distance3d * (1.0 + ppm*1E-6)) + prismConstant );
							distanceForUncertaintyModel = (distance3d * (1.0 + ppm*1E-6)) + prismConstant;
							distanceRow.setDistanceApriori(distanceForUncertaintyModel);
							this.slopeDistances.add(distanceRow);
						}
						
						if (zenithAngle != null && !Double.isNaN(zenithAngle) && !Double.isInfinite(zenithAngle)) {
							TerrestrialObservationRow zenithAnglesRow = new TerrestrialObservationRow();
							zenithAnglesRow.setInstrumentHeight(station.getInstrumentHeight());
							zenithAnglesRow.setStartPointName(station.getStationName());
							zenithAnglesRow.setReflectorHeight(targetHeight);
							zenithAnglesRow.setEndPointName(targetName);
							zenithAnglesRow.setValueApriori(zenithAngle);
							if (distanceForUncertaintyModel > 0)
								zenithAnglesRow.setDistanceApriori(distanceForUncertaintyModel);
							this.zenithAngles.add(zenithAnglesRow);
						}
					}

					else if (this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) {
						if ((zenithAngle != null && !Double.isNaN(zenithAngle) && !Double.isInfinite(zenithAngle)) &&
								(distance3d != null && !Double.isNaN(distance3d) && !Double.isInfinite(distance3d) && distance3d > 0)) {
							double dist3d = (distance3d * (1.0 + ppm*1E-6)) + prismConstant;
							double zenith = zenithAngle;
							
//							// Reduziere die II. Lage auf die I. Lage
//							if (zenith > Math.PI)
//								zenith = 2.0*Math.PI - zenith;
							
							// Baumann 1993, S.  99 - Strecke 2D 
							// Baumann 1993, S. 137 - Hoehenunterschied
							double kDist2DRefra =  refraction*dist3d*dist3d / 2.0 / R * Math.cos(zenith);
							double kDeltaHRefra = -refraction*dist3d*dist3d / 2.0 / R * Math.sin(zenith);
							double kDist2DEarth = 0.0;
							double kDeltaHEarth = 0.0;

							if (applyEarthCurve) {
								kDist2DEarth = -dist3d*dist3d / R * Math.cos(zenith);
								kDeltaHEarth =  dist3d*dist3d / 2.0 / R * Math.sin(zenith);
							}

							double distance2d = dist3d * Math.sin(zenith) + kDist2DRefra + kDist2DEarth;
							double leveling = dist3d * Math.cos(zenith)   + kDeltaHRefra + kDeltaHEarth;

							distanceForUncertaintyModel = distance2d;
							
							TerrestrialObservationRow distanceRow = new TerrestrialObservationRow();
							distanceRow.setInstrumentHeight(station.getInstrumentHeight());
							distanceRow.setStartPointName(station.getStationName());
							distanceRow.setReflectorHeight(targetHeight);
							distanceRow.setEndPointName(targetName);
							distanceRow.setValueApriori(distance2d);
							distanceRow.setDistanceApriori(distanceForUncertaintyModel);
							this.horizontalDistances.add(distanceRow);
							
							if (this.dim == DimensionType.PLAN_AND_HEIGHT) {
								TerrestrialObservationRow levelingRow = new TerrestrialObservationRow();
								levelingRow.setInstrumentHeight(station.getInstrumentHeight());
								levelingRow.setStartPointName(station.getStationName());
								levelingRow.setReflectorHeight(targetHeight);
								levelingRow.setEndPointName(targetName);
								levelingRow.setValueApriori(leveling);
								if (distanceForUncertaintyModel > 0)
									levelingRow.setDistanceApriori(distanceForUncertaintyModel);
								this.leveling.add(levelingRow);
							}
						}
					}
					
					if (direction != null && !Double.isNaN(direction) && !Double.isInfinite(direction)) {
						TerrestrialObservationRow directionsRow = new TerrestrialObservationRow();
						directionsRow.setInstrumentHeight(station.getInstrumentHeight());
						directionsRow.setStartPointName(station.getStationName());
						directionsRow.setReflectorHeight(targetHeight);
						directionsRow.setEndPointName(targetName);
						directionsRow.setValueApriori(direction);
						if (distanceForUncertaintyModel > 0)
							directionsRow.setDistanceApriori(distanceForUncertaintyModel);
						this.directions.add(directionsRow);
					}
				}
				// Speichere Richtungen, da diese Satzweise zu halten sind
				this.saveDirectionGroup();
			}
			
			String itemName = this.createItemName(null, null);

			// Speichere Punkte
			if (this.dim == DimensionType.PLAN && !this.points2d.isEmpty()) 
				this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_2D_LEAF, this.points2d);

			if ((this.dim == DimensionType.PLAN_AND_HEIGHT || this.dim == DimensionType.SPATIAL) && !this.points3d.isEmpty()) 
				this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_3D_LEAF, this.points3d);

			// Speichere Beobachtungen
			if ((this.dim == DimensionType.HEIGHT || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.leveling.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.LEVELING_LEAF, this.leveling);
			
			if (!this.directions.isEmpty())
				this.saveDirectionGroup();

			if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.horizontalDistances.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.HORIZONTAL_DISTANCE_LEAF, this.horizontalDistances);

			if (this.dim == DimensionType.SPATIAL && !this.slopeDistances.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.SLOPE_DISTANCE_LEAF, this.slopeDistances);

			if (this.dim == DimensionType.SPATIAL && !this.zenithAngles.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.ZENITH_ANGLE_LEAF, this.zenithAngles);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			this.isValidDocument = false;
		} catch (SAXException e) {
			this.isValidDocument = false;
			e.printStackTrace();
		} catch (IOException e) {
			this.isValidDocument = false;
			e.printStackTrace();
		}
		
		this.reset();
		
		return this.lastTreeItem;
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(i18n.getString("JobXMLFileReader.extension.jxl", "JobXML"), "*.jxl"),
				new ExtensionFilter(i18n.getString("JobXMLFileReader.extension.xml", "Extensible Markup Language"), "*.xml")
		};
	}
	
	private void saveDirectionGroup() throws SQLException {
		if (!this.directions.isEmpty()) {
			String itemName = this.createItemName(null, " (" + this.directions.get(0).getStartPointName() + ")"); 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.DIRECTION_LEAF, this.directions);
		}
		this.directions.clear();
	}

	private TreeItem<TreeItemValue> saveTerrestrialObservations(String itemName, TreeItemType treeItemType, List<TerrestrialObservationRow> observations) throws SQLException {
		if (observations == null || observations.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
		try {
			SQLManager.getInstance().saveGroup((ObservationTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((ObservationTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (TerrestrialObservationRow row : observations) {
				//SQLManager.getInstance().saveItem(groupId, row);
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private TreeItem<TreeItemValue> savePoints(String itemName, TreeItemType treeItemType, List<PointRow> points) throws SQLException {
		if (points == null || points.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
}
