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

package org.applied_geodesy.jag3d.ui.table.rowhighlight;

import java.io.BufferedInputStream;
import java.util.Properties;

import javafx.scene.paint.Color;

public class DefaultTableRowHighlightValue {
	private static final double[] REDUNDANCY            = new double[] {0.3, 0.7};
	private static final double[] P_PRIO_VALUE          = new double[] {1.0, 5.0};
	private static final double[] INFLUENCE_ON_POSITION = new double[] {0.001, 0.005};
	
	private static final Color EXCELLENT_COLOR    = Color.web("#bcee68");
	private static final Color SATISFACTORY_COLOR = Color.web("#ffec8b");
	private static final Color INADEQUATE_COLOR   = Color.web("#ff3030");
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/tablerowhighlight.default";
		try {
			if (DefaultTableRowHighlightValue.class.getResource(path) != null) {
				bis = new BufferedInputStream(DefaultTableRowHighlightValue.class.getResourceAsStream(path));
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

	private DefaultTableRowHighlightValue() {}
	
	public static Color getExcellentColor() {
		try {
			return Color.web(PROPERTIES.getProperty("EXCELLENT_COLOR", "#bcee68"));
		} catch (Exception e) {
			return EXCELLENT_COLOR;
		}
	}
	
	public static Color getSatisfactoryColor() {
		try {
			return Color.web(PROPERTIES.getProperty("SATISFACTORY_COLOR", "#ffec8b"));
		} catch (Exception e) {
			return SATISFACTORY_COLOR;
		}
	}
	
	public static Color getInadequateColor() {
		try {
			return Color.web(PROPERTIES.getProperty("INADEQUATE_COLOR", "#ff3030"));
		} catch (Exception e) {
			return INADEQUATE_COLOR;
		}
	}

	public static double[] getRangeOfRedundancy() {
		double leftBoundary  = -1;
		double rightBoundary = -1;
		try { 
			leftBoundary  = Double.parseDouble(PROPERTIES.getProperty("REDUNDANCY_LEFT_BOUNDARY"));
			rightBoundary = Double.parseDouble(PROPERTIES.getProperty("REDUNDANCY_RIGHT_BOUNDARY"));
		} catch (Exception e) {}
		
		return leftBoundary >= 0 && rightBoundary >= 0 && leftBoundary <= 1 && rightBoundary <= 1 && leftBoundary < rightBoundary ? new double[] {leftBoundary, rightBoundary} : new double[] {REDUNDANCY[0], REDUNDANCY[1]};
	}
	
	public static double[] getRangeOfPprioValue() {
		double leftBoundary  = -1;
		double rightBoundary = -1;
		try { 
			leftBoundary  = Double.parseDouble(PROPERTIES.getProperty("P_PRIO_VALUE_LEFT_BOUNDARY"));
			rightBoundary = Double.parseDouble(PROPERTIES.getProperty("P_PRIO_VALUE_RIGHT_BOUNDARY"));
		} catch (Exception e) {}
		
		return leftBoundary > 0 && rightBoundary > 0 && leftBoundary < 100 && rightBoundary < 100 && leftBoundary < rightBoundary ? new double[] {leftBoundary, rightBoundary} : new double[] {P_PRIO_VALUE[0], P_PRIO_VALUE[1]};
	}
	
	public static double[] getRangeOfInfluenceOnPosition() {
		double leftBoundary  = -1;
		double rightBoundary = -1;
		try { 
			leftBoundary  = Double.parseDouble(PROPERTIES.getProperty("INFLUENCE_ON_POSITION_LEFT_BOUNDARY"));
			rightBoundary = Double.parseDouble(PROPERTIES.getProperty("INFLUENCE_ON_POSITION_RIGHT_BOUNDARY"));
		} catch (Exception e) {}
		
		return leftBoundary >= 0 && rightBoundary >= 0 && leftBoundary <= rightBoundary ? new double[] {leftBoundary, rightBoundary} : new double[] {INFLUENCE_ON_POSITION[0], INFLUENCE_ON_POSITION[1]};
	}
	
	public static double[] getRange(TableRowHighlightType type) {
		switch(type) {
		case INFLUENCE_ON_POSITION:
			return getRangeOfInfluenceOnPosition();
			
		case P_PRIO_VALUE:
			return getRangeOfPprioValue();

		case REDUNDANCY:
			return getRangeOfRedundancy();

		case NONE:
		case TEST_STATISTIC:
			return null;
		}
		
		return null;
	}
	
	public static Color getColor(TableRowHighlightRangeType type) {
		switch(type) {
		case EXCELLENT:
			return getExcellentColor();
			
		case SATISFACTORY:
			return getSatisfactoryColor();

		case INADEQUATE:
			return getInadequateColor();

		case NONE:
			return Color.TRANSPARENT;
		}
		
		return Color.TRANSPARENT;
	}
}
