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

package org.applied_geodesy.jag3d.ui.io.reader;

import java.io.BufferedInputStream;
import java.util.Map;
import java.util.Properties;

import org.applied_geodesy.adjustment.network.ObservationType;

public class DefaultImportOption {
	private final static Properties PROPERTIES = new Properties();
	private static Map<ObservationType, Boolean> DEFAULT_SEPARATE_STATION = Map.of(
			ObservationType.LEVELING,            Boolean.FALSE,
			ObservationType.DIRECTION,           Boolean.TRUE,
			ObservationType.HORIZONTAL_DISTANCE, Boolean.FALSE,
			ObservationType.SLOPE_DISTANCE,      Boolean.FALSE,
			ObservationType.ZENITH_ANGLE,        Boolean.FALSE,
			ObservationType.GNSS1D,              Boolean.FALSE,
			ObservationType.GNSS2D,              Boolean.FALSE,
			ObservationType.GNSS3D,              Boolean.FALSE
	);
	
	static {
		BufferedInputStream bis = null;
		final String path = "properties/import.default";
		try {
			if (DefaultImportOption.class.getClassLoader().getResourceAsStream(path) != null) {
				bis = new BufferedInputStream(DefaultImportOption.class.getClassLoader().getResourceAsStream(path));
				PROPERTIES.load(bis);
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
	
	public static boolean isGroupSeparation(ObservationType observationType) {
		Boolean value = null;
		switch(observationType) {
		case LEVELING:
			try { value = PROPERTIES.getProperty("LEVELING") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("LEVELING")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case DIRECTION:
			try { value = PROPERTIES.getProperty("DIRECTION") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("DIRECTION")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case HORIZONTAL_DISTANCE:
			try { value = PROPERTIES.getProperty("HORIZONTAL_DISTANCE") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("HORIZONTAL_DISTANCE")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case SLOPE_DISTANCE:
			try { value = PROPERTIES.getProperty("SLOPE_DISTANCE") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("SLOPE_DISTANCE")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case ZENITH_ANGLE:
			try { value = PROPERTIES.getProperty("ZENITH_ANGLE") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("ZENITH_ANGLE")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case GNSS1D:
			try { value = PROPERTIES.getProperty("GNSS2D") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("GNSS1D")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case GNSS2D:
			try { value = PROPERTIES.getProperty("GNSS2D") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("GNSS2D")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		case GNSS3D:
			try { value = PROPERTIES.getProperty("GNSS3D") == null ? null : Boolean.parseBoolean(PROPERTIES.getProperty("GNSS3D")); } catch (Exception e) {}
			return value == null ? DEFAULT_SEPARATE_STATION.get(observationType) : value;
		}
		return Boolean.FALSE;
	}
}
