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

package org.applied_geodesy.util.io.properties;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.applied_geodesy.adjustment.DefaultUncertainty;

public class HTTPPropertiesLoader {

	static {
		BufferedInputStream bis = null;
		final String path = "/properties/proxy.default";

		try {
			if (DefaultUncertainty.class.getResource(path) != null) {
				Properties properties = new Properties();
				bis = new BufferedInputStream(DefaultUncertainty.class.getResourceAsStream(path));
				properties.load(bis);

				String host = properties.getProperty("HOST", null);
				if (host != null && !host.trim().isEmpty()) {
					String protocol = properties.getProperty("PROTOCOL", "HTTP").toLowerCase();
					String port     = properties.getProperty("PORT", "80");
					String username = properties.getProperty("USERNAME", "");
					String password = properties.getProperty("PASSWORD", "");

					Properties systemSettings = System.getProperties();
					systemSettings.put(protocol + ".proxyHost", host.trim());
					systemSettings.put(protocol + ".proxyPort", port.trim());
					systemSettings.put(protocol + ".proxyUser", username.trim());
					systemSettings.put(protocol + ".proxyPassword", password.trim());
				}
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stellt eine Verbindung zum Server her und prueft auf UPDATES. 
	 * Hierbei wird eine PHP-Datei gelesen bspw.:
	 * 
	 * <?php
	 * 	define("VERSION", 2.0);
	 * 	define("BUILD",   20091120);
	 * 	define("URI",     "http://sourceforge.net/projects/javagraticule3d/files/latest");
	 * 	
	 * 	header('X-Powered-By: JAG3D');
	 * 	header('Cache-Control: no-cache, no-store, max-age=0, must-revalidate');
	 * 	header('Pragma: no-cache');
	 * 
	 * 	if (isset($_POST['checkupdate']) && $_POST['checkupdate'] == "jag3d") {
	 * 		header('Content-Type: text/plain; charset=utf-8');
	 * 		echo "VERSION: ".VERSION."\r\n";
	 * 		echo "BUILD: ".BUILD."\r\n";
	 * 		echo "URI: ".URI."\r\n";
	 * 	} 
	 * 	else {
	 * 		header('Content-Type: text/html; charset=utf-8');
	 * 		header('HTTP/1.1 404 Not Found');
	 * 		print('<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
	 * 			<html><head>
	 * 			<title>404 Found</title>
	 * 			</head><body>
	 * 			<h1>Not Found</h1>
	 * 			<p>The requested URL was not found on this server.</p>
	 * 			<hr>
	 * 			<address>'.$_SERVER['SERVER_SOFTWARE'].' Server at '.$_SERVER['SERVER_NAME'].' Port '.$_SERVER['SERVER_PORT'].'</address>
	 * 			</body></html>');
	 * 		exit;
	 * 	}
	 * ?>
	 * @throws IOException 
	 * 
	 */
	public static Properties getProperties(String address, URLParameter... params) throws IOException {
		Properties properties = new Properties();
		BufferedReader bufferedReader = null;
		OutputStreamWriter outputStreamWriter = null;
		try {
			// https://stackoverflow.com/questions/1626549/authenticated-http-proxy-with-java
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					if (getRequestorType() == RequestorType.PROXY) {
						String protocol = getRequestingProtocol().toLowerCase();
						String host     = System.getProperty(protocol + ".proxyHost", "");
						String port     = System.getProperty(protocol + ".proxyPort", "80");
						String username = System.getProperty(protocol + ".proxyUser", "");
						String password = System.getProperty(protocol + ".proxyPassword", "");

						int portNumber = 80;
						try {portNumber = Integer.parseInt(port);} catch(Exception e) {e.printStackTrace();}

						if (getRequestingHost().equalsIgnoreCase(host) && portNumber == getRequestingPort()) {
							return new PasswordAuthentication(username, password.toCharArray());  
						}
					}
					return null;
				}  
			});

			String data = "";
			for (URLParameter param : params)
				data += URLEncoder.encode(param.getKey().trim(), "UTF-8") + "=" + URLEncoder.encode(param.getValue().trim(), "UTF-8");

			URL url = new URL( address );
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
			outputStreamWriter.write(data);
			outputStreamWriter.flush();

			bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			properties.load(bufferedReader);

		}
		finally { 
			if ( outputStreamWriter != null )
				try { outputStreamWriter.close(); } catch ( Exception e ) { }

			if ( bufferedReader != null ) 
				try { bufferedReader.close(); } catch ( Exception e ) { } 
		} 
		return properties;
	}


	public static void main(String[] args) throws IOException {
		String address = "https://software.applied-geodesy.org/update.php";

		URLParameter param = new URLParameter("checkupdate", "jag3d");

		Properties s = HTTPPropertiesLoader.getProperties(address, param);
		System.out.println(s);
	}
}
