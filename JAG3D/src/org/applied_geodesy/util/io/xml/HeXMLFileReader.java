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
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
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

public class HeXMLFileReader extends SourceFileReader implements ErrorHandler {

	private class InstrumentSetup {
		private String instrPointName;
		private double ih;
		public InstrumentSetup(String instrPointName, double ih) {
			this.instrPointName = instrPointName;
			this.ih = ih;
		}
		public double getInstrumentHeight() {
			return this.ih;
		}
		public String getSetupPointName() {
			return this.instrPointName;
		}
		@Override
		public String toString() {
			return "InstrumentSetup [instrPointName=" + instrPointName + ", ih=" + ih + "]";
		}
	}
	
	private LengthUnit  lengthUnit  = LengthUnit.METER;
	private AngularUnit angularUnit = AngularUnit.RADIAN;
	private Map<String, InstrumentSetup> setups = new LinkedHashMap<String, InstrumentSetup>();
	private boolean isValidDocument = true;
	private final DimensionType dim;

	private List<GNSSObservationRow> gnss2D = null;
	private List<GNSSObservationRow> gnss3D = null;
	
	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions          = null;
	
	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles   = null;
	
	private List<PointRow> points2d = null;
	private List<PointRow> points3d = null;
	
	private Set<String> pointNames  = null;
	private Set<String> point3DName = null;
	private Set<String> reservedNames = null;
	
	private TreeItem<TreeItemValue> lastTreeItem = null;
	
	public HeXMLFileReader(DimensionType dim) {
		this.dim = dim == DimensionType.PLAN ? DimensionType.PLAN : DimensionType.SPATIAL;
		this.reset();
	}
	
	public HeXMLFileReader(Path p, DimensionType dim) {
		super(p);
		this.dim = dim == DimensionType.PLAN ? DimensionType.PLAN : DimensionType.SPATIAL;
		this.reset();
	}

	public HeXMLFileReader(String xmlFileName, DimensionType dim)  {
		this(new File(xmlFileName), dim);
	}

	public HeXMLFileReader(File xmlFile, DimensionType dim)  {
		this(xmlFile.toPath(), dim);
	}
		
	// "decimal dd.mm.ss": "45.3025123" representing 45 degrees 30 minutes and 25.123 seconds
	private double convertToRadian(double d) {
		switch (this.angularUnit) {
			case GRADIAN:
				return d * Constant.RHO_GRAD2RAD;
			case DEGREE:
				return d * Constant.RHO_DEG2RAD;
			case DDMMSSss:
				int dd = (int)d;
				d = 100.0 * (d - dd);
				int mm = (int)d;
				double ss = 100.0 * (d - mm);
				return ((double)dd + (double)mm/60.0 + ss/3600.0)*Constant.RHO_DEG2RAD;
			default: // RAD
				return d;
		}
	}

	private double convertToMeter(double d) {
		switch (this.lengthUnit) {
			case MILLIMETER:
				return 0.001*d;
			case CENTIMETER:
				return 0.01*d;
			case KILOMETER:
				return 1000.0*d;
			case FOOT:
				return 12.0*0.0254*d;
			case INCH:
				return 0.0254*d;
			case US_SURVEY_FOOT:
				return 1200.0/3937.0*d;
			case MILE:
				return 1609.344*d;
			default: // METER
				return d;
		}
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
		if (this.setups == null)
			this.setups = new LinkedHashMap<String, InstrumentSetup>();

		if (this.points2d == null)
			this.points2d = new ArrayList<PointRow>();
		if (this.points3d == null)
			this.points3d = new ArrayList<PointRow>();
		
		if (this.horizontalDistances == null)
			this.horizontalDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.directions == null)
			this.directions = new ArrayList<TerrestrialObservationRow>();
		if (this.slopeDistances == null)
			this.slopeDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.zenithAngles == null)
			this.zenithAngles = new ArrayList<TerrestrialObservationRow>();
		
		if (this.gnss2D == null)
			this.gnss2D = new ArrayList<GNSSObservationRow>();
		if (this.gnss3D == null)
			this.gnss3D = new ArrayList<GNSSObservationRow>();
		
		if (this.reservedNames == null)
			this.reservedNames = new HashSet<String>();

		if (this.pointNames == null)
			this.pointNames = new HashSet<String>();
		
		if (this.point3DName == null)
			this.point3DName = new HashSet<String>();
		
		this.setups.clear();

		this.reservedNames.clear();
		this.pointNames.clear();
		this.point3DName.clear();
		
		this.points2d.clear();
		this.points3d.clear();

		this.horizontalDistances.clear();
		this.directions.clear();

		this.slopeDistances.clear();
		this.zenithAngles.clear();
		
		this.gnss2D.clear();
		this.gnss3D.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.lastTreeItem = null;
		this.reservedNames = SQLManager.getInstance().getFullPointNameSet();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
        DocumentBuilder builder;
        
		try {
			builder = factory.newDocumentBuilder();
			File xmlFile = this.getPath().toFile();
			Document document = builder.parse(xmlFile);
			
			HeXMLNamespaceContext namespaceContext = new HeXMLNamespaceContext(document);
					
			// Bestimme Einheiten
			String xpathPattern = "//landxml:LandXML/landxml:Units/*[1]";
			Node units = (Node)XMLUtilities.xpathSearch(document, xpathPattern, namespaceContext, XPathConstants.NODE);
			if (units != null) {				
				NamedNodeMap attr = units.getAttributes();
				// ENUM { millimeter, centimeter, meter, kilometer, foot, USSurveyFoot, inch, mile }
				String lengthUnit = attr.getNamedItem("linearUnit") == null ? "meter" : attr.getNamedItem("linearUnit").getNodeValue();
				if (lengthUnit.equalsIgnoreCase("millimeter"))
					this.lengthUnit = LengthUnit.MILLIMETER;
				else if (lengthUnit.equalsIgnoreCase("centimeter"))
					this.lengthUnit = LengthUnit.CENTIMETER;
				else if (lengthUnit.equalsIgnoreCase("kilometer"))
					this.lengthUnit = LengthUnit.KILOMETER;
				else if (lengthUnit.equalsIgnoreCase("foot"))
					this.lengthUnit = LengthUnit.FOOT;
				else if (lengthUnit.equalsIgnoreCase("USSurveyFoot"))
					this.lengthUnit = LengthUnit.US_SURVEY_FOOT;
				else if (lengthUnit.equalsIgnoreCase("inch"))
					this.lengthUnit = LengthUnit.INCH;
				else if (lengthUnit.equalsIgnoreCase("mile"))
					this.lengthUnit = LengthUnit.MILE;
				else //if (lengthUnit.equalsIgnoreCase("meter"))
					this.lengthUnit = LengthUnit.METER;

				// ENUM { radians, grads, decimal degrees, decimal dd.mm.ss }
				String angularUnit = attr.getNamedItem("angularUnit") == null ? "radians" : attr.getNamedItem("angularUnit").getNodeValue();
				if (angularUnit.equalsIgnoreCase("grads"))
					this.angularUnit = AngularUnit.GRADIAN;
				else if (angularUnit.equalsIgnoreCase("decimal degrees"))
					this.angularUnit = AngularUnit.DEGREE;
				else if (angularUnit.equalsIgnoreCase("decimal dd.mm.ss"))
					this.angularUnit = AngularUnit.DDMMSSss;
				else // if (angularUnit.equalsIgnoreCase("radians"))
					this.angularUnit = AngularUnit.RADIAN;
			}

			// Ermittle alle Punkte (Koordinaten) im Projekt und die SetupIDs
			xpathPattern = "//landxml:LandXML/landxml:CgPoints/landxml:CgPoint[@name] | "
					+ "//landxml:LandXML/landxml:Survey//landxml:Backsight/landxml:BacksightPoint[@name] | "
					+ "//landxml:LandXML/landxml:Survey//landxml:TargetPoint[@name] |"
					+ "//landxml:LandXML/landxml:Survey//landxml:InstrumentSetup[@stationName]/landxml:InstrumentPoint";

			NodeList nodeList = (NodeList)XMLUtilities.xpathSearch(document, xpathPattern, namespaceContext, XPathConstants.NODESET);
			for (int i=0; i<nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.hasChildNodes() && node.getFirstChild().getNodeType() == Node.TEXT_NODE && !node.getFirstChild().getNodeValue().trim().isEmpty()) {
					NamedNodeMap attr = node.getAttributes();
					String pointName = "";
					String code = null;
					String setupId = "";
					double ih = 0.0;
					PointRow point = null;
					boolean isDeleted = false;
					if (node.getNodeName().equalsIgnoreCase("CgPoint")) {
						pointName = attr.getNamedItem("oID") == null ? attr.getNamedItem("name").getNodeValue() : attr.getNamedItem("oID").getNodeValue();
						code = attr.getNamedItem("code") == null ? null : attr.getNamedItem("code").getNodeValue();
						// boolean isReferencePoint = attr.getNamedItem("role") != null && attr.getNamedItem("role").getNodeValue().equalsIgnoreCase("control point");
					}
					else if (node.getNodeName().equalsIgnoreCase("InstrumentPoint")) {
						NamedNodeMap parentNodeAttr = node.getParentNode().getAttributes();
						pointName = parentNodeAttr.getNamedItem("stationName").getNodeValue();
						Node status = parentNodeAttr.getNamedItem("status");
						isDeleted = status != null && status.getNodeValue() != null && status.getNodeValue().equalsIgnoreCase("deleted");
						// ermittle die Setup-ID
						setupId = parentNodeAttr.getNamedItem("id") == null ? "" : parentNodeAttr.getNamedItem("id").getNodeValue();
						
						//try {ih = attr.getNamedItem("instrumentHeight") == null ? 0.0 : this.convertToMeter(Double.parseDouble(attr.getNamedItem("instrumentHeight").getNodeValue()));}catch (NumberFormatException e) {ih = 0.0;}
						try {ih = parentNodeAttr.getNamedItem("instrumentHeight") == null ? 0.0 : this.convertToMeter(Double.parseDouble(parentNodeAttr.getNamedItem("instrumentHeight").getNodeValue()));}catch (NumberFormatException e) {ih = 0.0;}
					}
					else {
						pointName = attr.getNamedItem("name").getNodeValue();
					}
					
					if (!isDeleted && !pointName.isEmpty() && !this.pointNames.contains(pointName)) {
						this.pointNames.add(pointName);
						String pointContent[] = node.getFirstChild().getNodeValue().trim().split("\\s+");
						double xyz[] = new double[pointContent.length];
						try {
							for (int p=0; p<pointContent.length; p++)
								xyz[p] = this.convertToMeter(Double.parseDouble(pointContent[p]));
						} 
						catch (NumberFormatException e) {
							xyz = null;
						}

						if (xyz != null && pointContent.length > 1) {
							point = new PointRow();
							point.setName(pointName);
							point.setCode(code);
							double x = 0, y = 0, z = 0;
							if (xyz.length != 1) {
								x = xyz[0];
								y = xyz[1];
								
								point.setXApriori(x);
								point.setYApriori(y);
							}
							if (xyz.length != 2) {
								z = xyz[xyz.length-1];
								point.setZApriori(z);
							}
							
							if (xyz.length == 2 || this.dim == DimensionType.PLAN) {
								if (!this.reservedNames.contains(pointName))
									this.points2d.add(point);
							}
							else {
								if (!this.reservedNames.contains(pointName))
									this.points3d.add(point);
								this.point3DName.add(pointName);
							}
						}
					}
					
					if (!setupId.isEmpty() && !pointName.isEmpty() && this.pointNames.contains(pointName))
						this.setups.put(setupId, new InstrumentSetup(pointName, ih));
				}
			}
			// ermittle Beobachtungen zwischen den Punkten
			xpathPattern = "//landxml:LandXML/landxml:Survey//landxml:RawObservation[@setupID=\"%s\"]";
			for (String setupId : this.setups.keySet()) {
				InstrumentSetup setup = this.setups.get(setupId);
				nodeList = (NodeList)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPattern, setupId), namespaceContext, XPathConstants.NODESET);
				for (int i=0; i<nodeList.getLength(); i++) {
					Node node = nodeList.item(i);				
					double th = 0.0;
					Double dir = null, zenith = null, dist2d = null, dist3d = null;
					Boolean isDeleted = false;
					
					NamedNodeMap attr = node.getAttributes();
					try {isDeleted = attr.getNamedItem("status") == null ? false : attr.getNamedItem("status").getNodeValue().equalsIgnoreCase("deleted");} catch (Exception e) {isDeleted = false;}
					if (isDeleted)
						continue;
					
					try {th     = attr.getNamedItem("targetHeight")  == null ? 0.0  : this.convertToMeter(Double.parseDouble(attr.getNamedItem("targetHeight").getNodeValue())); } catch (NumberFormatException e) {th     = 0.0;}
					try {dir    = attr.getNamedItem("horizAngle")    == null ? null : this.convertToRadian(Double.parseDouble(attr.getNamedItem("horizAngle").getNodeValue()));  } catch (NumberFormatException e) {dir    = null;}
					try {zenith = attr.getNamedItem("zenithAngle")   == null ? null : this.convertToRadian(Double.parseDouble(attr.getNamedItem("zenithAngle").getNodeValue())); } catch (NumberFormatException e) {zenith = null;}
					try {dist2d = attr.getNamedItem("horizDistance") == null ? null : this.convertToMeter(Double.parseDouble(attr.getNamedItem("horizDistance").getNodeValue()));} catch (NumberFormatException e) {dist2d = null;}
					try {dist3d = attr.getNamedItem("slopeDistance") == null ? null : this.convertToMeter(Double.parseDouble(attr.getNamedItem("slopeDistance").getNodeValue()));} catch (NumberFormatException e) {dist3d = null;}
					
					boolean isFaceI = true;
					if (attr.getNamedItem("directFace") != null && !attr.getNamedItem("directFace").getNodeValue().equalsIgnoreCase("TRUE") || zenith != null && !Double.isNaN(zenith) && !Double.isInfinite(zenith) && zenith > Math.PI) {
						isFaceI = false;
					}
					
					// Reduziere auf Lage I
					if (!isFaceI) {
						if (dir != null)
							dir = MathExtension.MOD(dir + Math.PI, 2.0*Math.PI);
						if (this.dim == DimensionType.SPATIAL && zenith != null && zenith > Math.PI)
							zenith = MathExtension.MOD(2.0*Math.PI - zenith, 2.0*Math.PI);
					}

					String xpath = "./landxml:TargetPoint/@name";
					String endPointName = (String)XMLUtilities.xpathSearch(node, xpath, namespaceContext, XPathConstants.STRING);
					
					int targetPointDim = this.point3DName.contains(endPointName) ? 3 : this.pointNames.contains(endPointName) ? 2 : this.dim == DimensionType.PLAN ? 2 : 3;
					int startPointDim  = this.point3DName.contains(setup.getSetupPointName()) ? 3 : this.pointNames.contains(setup.getSetupPointName()) ? 2 : this.dim == DimensionType.PLAN ? 2 : 3;
					int obsDim = Math.min(targetPointDim, startPointDim);
					
//					System.out.println("DIM " +dim+"   "+obsDim);
//					System.out.println("Polar " +dir+"  "+zenit+"  "+dist2d+"  "+dist3d);
					double distanceForUncertaintyModel = 0;
					if (this.dim == DimensionType.SPATIAL && obsDim == 3) {
						// Bestimme Korrekturparameter fuer 3D-Strecke
						if (dist3d != null) {
							xpath = "./landxml:Feature[@code=\"observationInfo\"]/landxml:Property[@label=\"TPSCorrectionRef\"]/@value";
							String tpsCorr = (String)XMLUtilities.xpathSearch(node, xpath, namespaceContext, XPathConstants.STRING);
							
							xpath = "./landxml:TargetPoint/@pntRef";
							tpsCorr = tpsCorr == null || tpsCorr.isEmpty() ? (String)XMLUtilities.xpathSearch(node, xpath, namespaceContext, XPathConstants.STRING) : tpsCorr;
													
							// HeXML
							xpath = "1.0 + //landxml:LandXML/hexml:HexagonLandXML/hexml:Survey/hexml:TPSCorrection[@uniqueID = ./../hexml:InstrumentSetup[@uniqueID=\"%s\"]/hexml:RawObservation[@targetPntRef=\"%s\"]/@tpsCorrectionRef ]/@atmosphericPPM * 0.000001";
							Double scale = (Double)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpath, setupId, tpsCorr), namespaceContext, XPathConstants.NUMBER);
						
							xpath = "//landxml:LandXML/hexml:HexagonLandXML/hexml:Survey/hexml:InstrumentSetup[@uniqueID=\"%s\"]/hexml:RawObservation[@targetPntRef=\"%s\"]/@reflectorConstant";
							Double add = (Double)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpath, setupId, tpsCorr), namespaceContext, XPathConstants.NUMBER);

							// LandXML
							xpath = "1.0 + //landxml:LandXML/landxml:Survey//landxml:Corrections/landxml:Feature[@code=\"TPSCorrection\"]/landxml:Property[@label=\"oID\"][@value = \"%s\"]/../landxml:Property[@label=\"atmosphericPPM\"]/@value * 0.000001";
							scale = scale == null || Double.isNaN(scale) ? (Double)XMLUtilities.xpathSearch(node, String.format(Locale.ENGLISH, xpath, tpsCorr), namespaceContext, XPathConstants.NUMBER) : scale;
							
							xpath = "./landxml:Feature[@code=\"observationInfo\"]/landxml:Property[@label=\"reflectorConstant\"]/@value";
							add = add == null || Double.isNaN(add) ? (Double)XMLUtilities.xpathSearch(node, xpath, namespaceContext, XPathConstants.NUMBER) : add;
	
							// Validiere Korrekturwerte
							scale = scale == null || Double.isNaN(scale) ? 1.0 : scale;
							add = add == null || Double.isNaN(add)       ? 0.0 : add;
							add = this.convertToMeter(add);
							
							// Koorigiere Schraegstrecke
							dist3d = (dist3d - add) * scale + add;
							distanceForUncertaintyModel = dist3d;
							
							TerrestrialObservationRow slopeDistances = new TerrestrialObservationRow();
							slopeDistances.setInstrumentHeight(setup.getInstrumentHeight());
							slopeDistances.setStartPointName(setup.getSetupPointName());
							slopeDistances.setReflectorHeight(th);
							slopeDistances.setEndPointName(endPointName);
							slopeDistances.setValueApriori(dist3d);
							slopeDistances.setDistanceApriori(distanceForUncertaintyModel);
							if (dist3d > 0)
								this.slopeDistances.add(slopeDistances);
						}
						if (zenith != null) {
							TerrestrialObservationRow zenithAngles = new TerrestrialObservationRow();
							zenithAngles.setInstrumentHeight(setup.getInstrumentHeight());
							zenithAngles.setStartPointName(setup.getSetupPointName());
							zenithAngles.setReflectorHeight(th);
							zenithAngles.setEndPointName(endPointName);
							zenithAngles.setValueApriori(zenith);
							if (distanceForUncertaintyModel > 0)
								zenithAngles.setDistanceApriori(distanceForUncertaintyModel);
							this.zenithAngles.add(zenithAngles);
						}
					}
					else if (dist2d != null){
						distanceForUncertaintyModel = dist2d;
						TerrestrialObservationRow horizontalDistances = new TerrestrialObservationRow();
						horizontalDistances.setInstrumentHeight(setup.getInstrumentHeight());
						horizontalDistances.setStartPointName(setup.getSetupPointName());
						horizontalDistances.setReflectorHeight(th);
						horizontalDistances.setEndPointName(endPointName);
						horizontalDistances.setValueApriori(dist2d);
						horizontalDistances.setDistanceApriori(distanceForUncertaintyModel);
						if (dist2d > 0)
							this.horizontalDistances.add(horizontalDistances);
					}
					
					if (dir != null) {
						TerrestrialObservationRow directions = new TerrestrialObservationRow();
						directions.setInstrumentHeight(setup.getInstrumentHeight());
						directions.setStartPointName(setup.getSetupPointName());
						directions.setReflectorHeight(th);
						directions.setEndPointName(endPointName);
						directions.setValueApriori(dir);
						if (distanceForUncertaintyModel > 0)
							directions.setDistanceApriori(distanceForUncertaintyModel);
						this.directions.add(directions);
					}
				}
				// Speichere Richtungen, da diese Satzweise zu halten sind
				this.saveDirectionGroup();
			}

			// GNSS-Vector
			xpathPattern = "//landxml:LandXML/landxml:Survey//landxml:GPSVector";
			nodeList = (NodeList)XMLUtilities.xpathSearch(document, xpathPattern, namespaceContext, XPathConstants.NODESET);
			for (int i=0; i<nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NamedNodeMap attr = node.getAttributes();

				String startPointName  = attr.getNamedItem("setupID_A") == null ? null : attr.getNamedItem("setupID_A").getNodeValue();
				String endPointName = attr.getNamedItem("setupID_B") == null ? null : attr.getNamedItem("setupID_B").getNodeValue();
				
				if (startPointName == null || endPointName == null)
					continue;
				
				xpathPattern = "//landxml:LandXML/landxml:Survey//landxml:GPSSetup[@id=\"%s\"]//landxml:TargetPoint[1]";
				Node startNode  = (Node)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPattern, startPointName),  namespaceContext, XPathConstants.NODE);
				Node targetNode = (Node)XMLUtilities.xpathSearch(document, String.format(Locale.ENGLISH, xpathPattern, endPointName), namespaceContext, XPathConstants.NODE);
				
				if (startNode == null || targetNode == null)
					continue;
				
				// Bestimme Punktnummern der Basislinien
				startPointName  = startNode.getAttributes().getNamedItem("name") == null  ? null : startNode.getAttributes().getNamedItem("name").getNodeValue();
				endPointName = targetNode.getAttributes().getNamedItem("name") == null ? null : targetNode.getAttributes().getNamedItem("name").getNodeValue();
					
				if (startPointName == null || endPointName == null)
					continue;
				
				// Bestimme Koordinaten *in Gebrauchslage* und berechne dx, dy, dz - nutze *nicht* WGS84-Werte, da diese ggf. Undulationen nicht beruecksichtigen
				String startPointContent[]  = startNode.getFirstChild().getNodeValue().trim().split("\\s+");
				String targetPointContent[] = targetNode.getFirstChild().getNodeValue().trim().split("\\s+");
				
				int vecdim = Math.min(Math.min(startPointContent.length, targetPointContent.length), this.dim == DimensionType.PLAN ? 2 : 3);

				double sxyz[] = new double[vecdim];
				double txyz[] = new double[vecdim];
				try {
					for (int p=0; p<vecdim; p++) {
						sxyz[p] = this.convertToMeter(Double.parseDouble(startPointContent[p]));
						txyz[p] = this.convertToMeter(Double.parseDouble(targetPointContent[p]));
					}
				} 
				catch (NumberFormatException e) {
					sxyz = null;
					txyz = null;
				}
				
				// erzeuge Vektor
				if (sxyz != null && txyz != null) {
					GNSSObservationRow gnss = new GNSSObservationRow();
					gnss.setStartPointName(startPointName);
					gnss.setEndPointName(endPointName);
					if (vecdim != 1) {
						gnss.setXApriori( this.convertToMeter(txyz[0] - sxyz[0]) );
						gnss.setYApriori( this.convertToMeter(txyz[1] - sxyz[1]) );
					}
					if (vecdim != 2) 
						gnss.setZApriori( this.convertToMeter(txyz[vecdim-1] - sxyz[vecdim-1]) );
					
					if (vecdim == 2)
						this.gnss2D.add(gnss);
					else
						this.gnss3D.add(gnss);
				}				
			}

			String itemName = this.createItemName(null, null);

			// Speichere Punkte
			if (!this.points2d.isEmpty()) 
				this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_2D_LEAF, this.points2d);

			if (this.dim == DimensionType.SPATIAL && !this.points3d.isEmpty()) 
				this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_3D_LEAF, this.points3d);

			// Speichere Beobachtungen
			if (!this.directions.isEmpty())
				this.saveDirectionGroup();

			if (!this.horizontalDistances.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.HORIZONTAL_DISTANCE_LEAF, this.horizontalDistances);

			if (this.dim == DimensionType.SPATIAL && !this.slopeDistances.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.SLOPE_DISTANCE_LEAF, this.slopeDistances);

			if (this.dim == DimensionType.SPATIAL && !this.zenithAngles.isEmpty()) 
				this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.ZENITH_ANGLE_LEAF, this.zenithAngles);

			// Import von (relativen) GNSS-Messungen
			if (!this.gnss2D.isEmpty())
				this.lastTreeItem = this.saveGNSSObservations(itemName, TreeItemType.GNSS_2D_LEAF, this.gnss2D);

			if (this.dim == DimensionType.SPATIAL && !this.gnss3D.isEmpty())
				this.lastTreeItem = this.saveGNSSObservations(itemName, TreeItemType.GNSS_3D_LEAF, this.gnss3D);

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
				new ExtensionFilter(i18n.getString("HeXMLReader.extension.hexml", "LandXML/HeXML"), "*.hexml"),
				new ExtensionFilter(i18n.getString("HeXMLReader.extension.xml", "Extensible Markup Language"), "*.xml")
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
	
	private TreeItem<TreeItemValue> saveGNSSObservations(String itemName, TreeItemType treeItemType, List<GNSSObservationRow> gnssObservations) throws SQLException {
		if (gnssObservations == null || gnssObservations.isEmpty())
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
			for (GNSSObservationRow row : gnssObservations) {
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
