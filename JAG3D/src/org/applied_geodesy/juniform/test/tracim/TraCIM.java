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

package org.applied_geodesy.juniform.test.tracim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.surface.CircularConeFeature;
import org.applied_geodesy.adjustment.geometry.surface.CircularCylinderFeature;
import org.applied_geodesy.adjustment.geometry.surface.PlaneFeature;
import org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature;
import org.applied_geodesy.adjustment.geometry.surface.SpatialLineFeature;
import org.applied_geodesy.adjustment.geometry.surface.SphereFeature;
import org.applied_geodesy.util.XMLUtilities;
import org.applied_geodesy.version.coordtrans.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Certificate of PTB may not be in list of trusted services defined by Java and must be added
// C:\Program Files\Java\jdk\bin\keytool.exe -import -alias tracim -keystore  "C:\Program Files\Java\jdk\lib\security\cacerts" -file tracimlx.ptb.de.crt
// Password: changeit
public class TraCIM {
	private final String orderId;
	private final String baseURI  = "https://tracim.ptb.de/tracim/api";
	private final String customer = "Steinbeis Transfer Centre Applied Geodesy";
	private final String vendor   = "Dr.-Ing. Michael Lösler";
	private final String software = "Java·Applied·Geodesy·3D";
	private final String version  = String.valueOf(Version.get()).substring(0, 4);
	private final String revision = String.valueOf(Version.get()).substring(4);
	private String processId;
	
	public TraCIM(String orderId) {
		this.orderId = orderId;
	}
	
	public Document getTestData() throws IOException, ParserConfigurationException, SAXException {
		String xmlString = ""; 
		if (READ_DATA_FROM_LOCAL_FILE) {
			xmlString = this.getFileContent(BASE_PATH + "/gauss_data_sets.xml");
		}
		else {
			final String address = this.baseURI + "/order/" + this.orderId + "/test";
			xmlString = this.sendRequest(address, null);

			if (STORE_COMPLETE_TRANSACTION) {
				String txtFile = BASE_PATH + "/gauss_data_sets.xml";
				this.toFile(new File(txtFile), xmlString);
			}
		}
		return this.convertStringToXMLDocument(xmlString);
	}
	
	public void saveReport(File file, String xmlResult) throws ParserConfigurationException, SAXException, IOException {
		String xmlString  = this.submitResult(xmlResult);
		if (STORE_COMPLETE_TRANSACTION) {
			String txtFile = BASE_PATH + "/gauss_test_report.xml";
			this.toFile(new File(txtFile), xmlString);
		}
		
		Document document = this.convertStringToXMLDocument(xmlString);
		
		String xpathPattern = "//tracim/validation"; 
		Node validationElement = (Node)XMLUtilities.xpathSearch(document, xpathPattern, null, XPathConstants.NODE);

//		Boolean passed   = (Boolean)XMLUtilities.xpathSearch(validationElement, "./passed", null, XPathConstants.BOOLEAN);
//		String report    = (String)XMLUtilities.xpathSearch(validationElement, "./report", null, XPathConstants.STRING);
		String reportPDF = (String)XMLUtilities.xpathSearch(validationElement, "./reportPDF", null, XPathConstants.STRING);
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			byte[] decoder = Base64.getDecoder().decode(reportPDF);

			fos.write(decoder);
			fos.flush();
		}
		finally {
			if (fos != null)
				fos.close();
		}
	}
	
	public String getResultAsXMLString(Document document) throws IOException {
		
		String xpathPattern = "//tracim//process/key";
		this.processId = (String)XMLUtilities.xpathSearch(document, xpathPattern, null, XPathConstants.STRING);
		StringBuffer xmlStringBuffer = new StringBuffer();
		
		// Quick and Dirty: create a pseudo XML file containing adjustment results
		// real XML is not required because PTB just requests an XML string and not a file
		xmlStringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		xmlStringBuffer.append("<gaussResultPackage xmlns:tracim=\"http://tracim.ptb.de/tracim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://tracim.ptb.de/gauss/test\">\r\n");
		xmlStringBuffer.append("<processKey>").append(this.processId).append("</processKey>\r\n");
		xmlStringBuffer.append("<customer>").append(this.customer).append("</customer>\r\n");
		xmlStringBuffer.append("<softwareVendor>").append(this.vendor).append("</softwareVendor>\r\n");
		xmlStringBuffer.append("<softwareName>").append(this.software).append("</softwareName>\r\n");
		xmlStringBuffer.append("<softwareVersion>").append(this.version).append("</softwareVersion>\r\n");
		xmlStringBuffer.append("<softwareRev>").append(this.revision).append("</softwareRev>\r\n");
		xmlStringBuffer.append("<resultPackage>\r\n").append(this.performTests(document)).append("</resultPackage>\r\n");
		
		// Default MPE - lower values are not accepted by PTB
		xmlStringBuffer.append("<mpe_size>0.0001</mpe_size>\r\n");
		xmlStringBuffer.append("<mpe_angle>0.0000001</mpe_angle>\r\n");
		xmlStringBuffer.append("<mpe_position>0.0001</mpe_position>\r\n");
		xmlStringBuffer.append("<mpe_orientation>0.0000001</mpe_orientation>\r\n");

		xmlStringBuffer.append("</gaussResultPackage>\r\n");
		
		String xmlString = xmlStringBuffer.toString();
		if (STORE_COMPLETE_TRANSACTION) {
			String txtFile = BASE_PATH + "/gauss_adjustment_results.xml";
			this.toFile(new File(txtFile), xmlString);
		}
		
		return xmlString;
	}
	
	private Document convertStringToXMLDocument(String xmlString) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
		return doc;
	}
	
	private String submitResult(String xmlResult) throws IOException  {
		final String address  = this.baseURI + "/test/" + this.processId;
		String response = this.sendRequest(address, xmlResult);
		return response;
	}
	
	private String getFileContent(String fileName) throws IOException {
		StringBuffer stringBuffer = new StringBuffer();
		BufferedReader reader = null; 
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = reader.readLine()) != null) {
	        	stringBuffer.append(str);
	        }
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuffer.toString();
	}
	
	private String sendRequest(String address, String data) throws IOException {
		String response = null;
		BufferedReader in = null;
		try {
			data = data == null ? "" : data;
			URL url = new URL(address);
			StringBuilder postData = new StringBuilder();
			postData.append(URLEncoder.encode(String.valueOf(data), "UTF-8"));
			byte[] postDataBytes = data.getBytes("UTF-8");

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "application/xml");
			conn.setRequestProperty("accept", "application/xml");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setInstanceFollowRedirects( true );
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.getOutputStream().write(postDataBytes);
////		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), Charset.forName("UTF-8")));
////		writer.write(URLEncoder.encode(data, "UTF-8"));
////		writer.flush();
////		writer.close();
			
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpsURLConnection.HTTP_OK) {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
				StringBuilder sb = new StringBuilder();
				for (int c; (c = in.read()) >= 0;)
					sb.append((char)c);
				response = sb.toString();
			}
			else {
				Map<Integer, String> errors = Map.of(
						403, "No more tests left for this test process.\r\nTest process already closed.\r\nTest Data already used.",
						404, "Unknown order number.",
						422, "Unknown request type. Consult specific message.\r\nProcess key does not match!\r\nCustomer element is blank!\r\nSoftware vendor element is blank!\r\nSoftware name element is blank!\r\nSoftware version element is blank!\r\nMPE-elements not present. You are allowed to set MPE’s to zero but the elements have to be present!\r\nCheck your MPEs! If you set one MPE you have to set it all!\r\nCheck your MPEs! Negative MPE’s are not allowed.",
						500, "Internal Server error."
						);

				
				throw new IOException("Error, server response code is " + responseCode + "\r\n" + errors.get(responseCode));
			}
		}
		finally {
			if (in != null)
				in.close();
		}
		return response;
	}
	
	private String performTests(Document document) throws IOException {
		StringBuffer xmlResults = new StringBuffer();
		String xpathPattern = "//tracim//testElement";
		NodeList testElementList = (NodeList)XMLUtilities.xpathSearch(document, xpathPattern, null, XPathConstants.NODESET);
		
		for (int i=0; i<testElementList.getLength(); i++) {
			Node testElement = testElementList.item(i);
			
			String xpath = "./basicID"; 
			String bId = (String)XMLUtilities.xpathSearch(testElement, xpath, null, XPathConstants.STRING);
			
			xpath = "./computationObject"; 
			String object = (String)XMLUtilities.xpathSearch(testElement, xpath, null, XPathConstants.STRING);
			
			xpath = "./pointCloud/vectors"; 
			NodeList vectorsList = (NodeList)XMLUtilities.xpathSearch(testElement, xpath, null, XPathConstants.NODESET);
			
			int cnt = 0;
			List<FeaturePoint> points = new ArrayList<FeaturePoint>(vectorsList.getLength());
			
			for (int j=0; j<vectorsList.getLength(); j++) {
				Node vector = vectorsList.item(j);
				Number x = (Number)XMLUtilities.xpathSearch(vector, "./x", null, XPathConstants.NUMBER);
				Number y = (Number)XMLUtilities.xpathSearch(vector, "./y", null, XPathConstants.NUMBER);
				Number z = (Number)XMLUtilities.xpathSearch(vector, "./z", null, XPathConstants.NUMBER);

				FeaturePoint point = new FeaturePoint(String.valueOf(++cnt), x.doubleValue(), y.doubleValue(), z.doubleValue());
				if (point != null)
					points.add(point);
			}
			
			if (STORE_COMPLETE_TRANSACTION) {
				String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".txt";
				this.toFile(new File(txtFile), points);
			}

			xmlResults.append("<results>\r\n");
			xmlResults.append("<basicID>").append(bId).append("</basicID>\r\n");
			xmlResults.append("<computationObject>").append(object).append("</computationObject>\r\n");
			xmlResults.append("<refParameter>\r\n");
			
			double x0, y0, z0, nx, ny, nz, d, r, alpha;
			TraCIMTest traCIMTest = new TraCIMTest();
			Feature feature = null;
			Map<ParameterType, UnknownParameter> unknownParameters = null;
			
			switch(object) {
			case "LINE_3D":
				feature = traCIMTest.adjust(new SpatialLineFeature(), points);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());
				
				x0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
				y0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
				z0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();
				
				nx = unknownParameters.get(ParameterType.VECTOR_X).getValue();
				ny = unknownParameters.get(ParameterType.VECTOR_Y).getValue();
				nz = unknownParameters.get(ParameterType.VECTOR_Z).getValue();
								
				this.addPosition(xmlResults, x0, y0, z0);
				this.addNormalVector(xmlResults, nx, ny, nz);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, nx, ny, nz});
				}

				break;
				
			case "PLANE":
				feature = traCIMTest.adjust(new PlaneFeature(), points);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());

				nx = unknownParameters.get(ParameterType.VECTOR_X).getValue();
				ny = unknownParameters.get(ParameterType.VECTOR_Y).getValue();
				nz = unknownParameters.get(ParameterType.VECTOR_Z).getValue();
				
				d  = unknownParameters.get(ParameterType.LENGTH).getValue();
				
				if (nx > ny && nx > nz) {
					x0 = d / nx;
					y0 = 0;
					z0 = 0;
				}
				else if (ny > nx && ny > nz) {
					x0 = 0;
					y0 = d / ny;
					z0 = 0;
				}
				else {
					x0 = 0;
					y0 = 0;
					z0 = d / nz;
				}
				
				this.addPosition(xmlResults, x0, y0, z0);
				this.addNormalVector(xmlResults, nx, ny, nz);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, nx, ny, nz});
				}
				
				break;
				
			case "CIRCLE":
				feature = traCIMTest.adjust(new SpatialCircleFeature(), points);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());
				
				x0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
				y0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
				z0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();
				
				nx = unknownParameters.get(ParameterType.VECTOR_X).getValue();
				ny = unknownParameters.get(ParameterType.VECTOR_Y).getValue();
				nz = unknownParameters.get(ParameterType.VECTOR_Z).getValue();

				r  = unknownParameters.get(ParameterType.RADIUS).getValue();
				
				this.addPosition(xmlResults, x0, y0, z0);
				this.addNormalVector(xmlResults, nx, ny, nz);
				this.addRadius(xmlResults, r);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, nx, ny, nz, r});
				}
				
				break;
				
			case "CYLINDER":
				feature = traCIMTest.adjust(new CircularCylinderFeature(), points);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());
				
				x0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
				y0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
				z0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();
				
				nx = unknownParameters.get(ParameterType.VECTOR_X).getValue();
				ny = unknownParameters.get(ParameterType.VECTOR_Y).getValue();
				nz = unknownParameters.get(ParameterType.VECTOR_Z).getValue();

				r  = unknownParameters.get(ParameterType.RADIUS).getValue();
				
				this.addPosition(xmlResults, x0, y0, z0);
				this.addNormalVector(xmlResults, nx, ny, nz);
				this.addRadius(xmlResults, r);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, nx, ny, nz, r});
				}
				
				break;
				
			case "CONE":
				feature = traCIMTest.adjust(new CircularConeFeature(), points, 0.0001);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());
				
				x0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
				y0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
				z0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();
				
				nx = unknownParameters.get(ParameterType.ROTATION_COMPONENT_R31).getValue();
				ny = unknownParameters.get(ParameterType.ROTATION_COMPONENT_R32).getValue();
				nz = unknownParameters.get(ParameterType.ROTATION_COMPONENT_R33).getValue();
				
				alpha = unknownParameters.get(ParameterType.ANGLE).getValue();
				r  = 0; // r = 0, if vector [x0/y0/z0] marks the apex of the cone

				// PTB specified the orientation of the normal vector in the direction of decreasing radius
				// even if the apex position is given, i.e., r == 0 and the orientation is ambiguous 
				Point point = Feature.deriveCenterOfMass(points);
				double mx = point.getX0() - x0;
				double my = point.getY0() - y0;
				double mz = point.getZ0() - z0;
				double len = Math.sqrt(mx*mx + my*my + mz*mz);
				mx /= len;
				my /= len;
				mz /= len;
				
				double phi = Math.acos(nx*mx + ny*my + nz*mz);
				
				if (phi < alpha) {
					nx = -nx;
					ny = -ny;
					nz = -nz;
				}
				
				alpha = 2.0 * alpha * Constant.RHO_RAD2DEG; // full angle at the apex in degree
				
				this.addPosition(xmlResults, x0, y0, z0);
				this.addNormalVector(xmlResults, nx, ny, nz);
				this.addRadius(xmlResults, r);
				this.addAngle(xmlResults, alpha);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, nx, ny, nz, alpha, r});
				}
				
				break;
				
			case "SPHERE":
				feature = traCIMTest.adjust(new SphereFeature(), points);
				if (feature == null) {
					System.err.println("Error, least-squares failed for " + object + ", " + bId);
					continue;
				}
				unknownParameters = convertParameterListToMap(feature.getUnknownParameters());
				
				x0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_X).getValue();
				y0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Y).getValue();
				z0 = unknownParameters.get(ParameterType.ORIGIN_COORDINATE_Z).getValue();

				r  = unknownParameters.get(ParameterType.RADIUS).getValue();
				
				this.addPosition(xmlResults, x0, y0, z0);
				this.addRadius(xmlResults, r);
				
				if (STORE_COMPLETE_TRANSACTION) {
					String txtFile = BASE_PATH + "/" + bId + "_" + object + "_" + this.orderId + ".sol";
					this.toFile(new File(txtFile), new double[]{x0, y0, z0, r});
				}
				
				break;
			}
			
			xmlResults.append("</refParameter>\r\n");
			xmlResults.append("</results>\r\n");
		}
		
		return xmlResults.toString();
	}
	
	private Map<ParameterType, UnknownParameter> convertParameterListToMap(List<UnknownParameter> unknownParameters) {
		Map<ParameterType, UnknownParameter> unknownParameterMap = new HashMap<ParameterType, UnknownParameter>(unknownParameters.size());
		for (UnknownParameter unknownParameter : unknownParameters) {
			if (unknownParameter.isVisible())
				unknownParameterMap.put(unknownParameter.getParameterType(), unknownParameter);
		}
		return unknownParameterMap;
	}
	
	private void addPosition(StringBuffer sb, double x, double y, double z) {
		sb.append("<positionX>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, x)).append("</positionX>\r\n");
		sb.append("<positionY>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, y)).append("</positionY>\r\n");
		sb.append("<positionZ>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, z)).append("</positionZ>\r\n");
	}
	
	private void addNormalVector(StringBuffer sb, double nx, double ny, double nz) {
		sb.append("<orientationX>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, nx)).append("</orientationX>\r\n");
		sb.append("<orientationY>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, ny)).append("</orientationY>\r\n");
		sb.append("<orientationZ>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, nz)).append("</orientationZ>\r\n");
	}
	
	private void addRadius(StringBuffer sb, double radius) {
		sb.append("<radius>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, radius)).append("</radius>\r\n");
	}
	
	private void addAngle(StringBuffer sb, double alpha) {
		sb.append("<angle>").append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, alpha)).append("</angle>\r\n");
	}
	
	private void toFile(File file, double[] array) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (double d : array)
			sb.append(String.format(Locale.ENGLISH, NUMBER_TEMPLATE, d)).append("\r\n");
		this.toFile(file, sb.toString());
	}
	
	private void toFile(File file, String string) throws IOException {
		PrintWriter pw = null;
    	try {
    		pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    		pw.println(string);
    	} 
    	finally {
    		if (pw != null) {
    			pw.close();
    		}
    	}
	}
	
	private void toFile(File file, List<FeaturePoint> points) throws IOException {
		PrintWriter pw = null;
    	try {
    		pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    		String format = "%25s\t%35.15f\t%35.15f\t%35.15f%n"; //Id, X, Y, Z
    		for ( FeaturePoint point : points ) {
    			pw.printf(Locale.ENGLISH, format, point.getName(), point.getX(), point.getY(), point.getZ());
    		}
    	} 
    	finally {
    		if (pw != null) {
    			pw.close();
    		}
    	}
	}
	
	public final static boolean READ_DATA_FROM_LOCAL_FILE = false;
	public final static String NUMBER_TEMPLATE = "%+.20f";
	public final static String BASE_PATH = "./tracim";
	public final static boolean STORE_COMPLETE_TRANSACTION = true;
	
	public static void main(String[] args) {
		System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");
		
		final String processKey = "";
		try {
			TraCIM traCIM = new TraCIM(processKey);
			Document document = traCIM.getTestData();
			String xmlResult = traCIM.getResultAsXMLString(document);
			System.out.println(xmlResult);
			
			traCIM.saveReport(new File(BASE_PATH + "/gauss_test_report.pdf"), xmlResult);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
