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

package org.applied_geodesy.jag3d.ui.tree;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.PointType;

public enum TreeItemType {
	ROOT,
	
	UNSPECIFIC,
	
	/** Points **/
	REFERENCE_POINT_1D_DIRECTORY,
	STOCHASTIC_POINT_1D_DIRECTORY,
	DATUM_POINT_1D_DIRECTORY,
	NEW_POINT_1D_DIRECTORY,
	
	REFERENCE_POINT_2D_DIRECTORY,
	STOCHASTIC_POINT_2D_DIRECTORY,
	DATUM_POINT_2D_DIRECTORY,
	NEW_POINT_2D_DIRECTORY,
	
	REFERENCE_POINT_3D_DIRECTORY,
	STOCHASTIC_POINT_3D_DIRECTORY,
	DATUM_POINT_3D_DIRECTORY,
	NEW_POINT_3D_DIRECTORY,
	
	REFERENCE_POINT_1D_LEAF,
	STOCHASTIC_POINT_1D_LEAF,
	DATUM_POINT_1D_LEAF,
	NEW_POINT_1D_LEAF,
	
	REFERENCE_POINT_2D_LEAF,
	STOCHASTIC_POINT_2D_LEAF,
	DATUM_POINT_2D_LEAF,
	NEW_POINT_2D_LEAF,
	
	REFERENCE_POINT_3D_LEAF,
	STOCHASTIC_POINT_3D_LEAF,
	DATUM_POINT_3D_LEAF,
	NEW_POINT_3D_LEAF,

	/** Terr. Observation **/
	LEVELING_DIRECTORY,
	DIRECTION_DIRECTORY,
	HORIZONTAL_DISTANCE_DIRECTORY,
	SLOPE_DISTANCE_DIRECTORY,
	ZENITH_ANGLE_DIRECTORY,
		
	LEVELING_LEAF,
	DIRECTION_LEAF,
	HORIZONTAL_DISTANCE_LEAF,
	SLOPE_DISTANCE_LEAF,
	ZENITH_ANGLE_LEAF,

	/** GNSS **/
	GNSS_1D_DIRECTORY,
	GNSS_2D_DIRECTORY,
	GNSS_3D_DIRECTORY,
	
	GNSS_1D_LEAF,
	GNSS_2D_LEAF,
	GNSS_3D_LEAF,

	/** Deformation analysis **/
	CONGRUENCE_ANALYSIS_1D_DIRECTORY,
	CONGRUENCE_ANALYSIS_2D_DIRECTORY,
	CONGRUENCE_ANALYSIS_3D_DIRECTORY,
	
	CONGRUENCE_ANALYSIS_1D_LEAF,
	CONGRUENCE_ANALYSIS_2D_LEAF,
	CONGRUENCE_ANALYSIS_3D_LEAF,
	;
	
	public static TreeItemType getLeafByDirectoryType(TreeItemType type) {
		switch (type) {
		case REFERENCE_POINT_1D_DIRECTORY:
			return TreeItemType.REFERENCE_POINT_1D_LEAF;
		case REFERENCE_POINT_2D_DIRECTORY:
			return TreeItemType.REFERENCE_POINT_2D_LEAF;
		case REFERENCE_POINT_3D_DIRECTORY:
			return TreeItemType.REFERENCE_POINT_3D_LEAF;
			
		case STOCHASTIC_POINT_1D_DIRECTORY:
			return TreeItemType.STOCHASTIC_POINT_1D_LEAF;
		case STOCHASTIC_POINT_2D_DIRECTORY:
			return TreeItemType.STOCHASTIC_POINT_2D_LEAF;
		case STOCHASTIC_POINT_3D_DIRECTORY:
			return TreeItemType.STOCHASTIC_POINT_3D_LEAF;
			
		case DATUM_POINT_1D_DIRECTORY:
			return TreeItemType.DATUM_POINT_1D_LEAF;
		case DATUM_POINT_2D_DIRECTORY:
			return TreeItemType.DATUM_POINT_2D_LEAF;
		case DATUM_POINT_3D_DIRECTORY:
			return TreeItemType.DATUM_POINT_3D_LEAF;
			
		case NEW_POINT_1D_DIRECTORY:
			return TreeItemType.NEW_POINT_1D_LEAF;
		case NEW_POINT_2D_DIRECTORY:
			return TreeItemType.NEW_POINT_2D_LEAF;
		case NEW_POINT_3D_DIRECTORY:
			return TreeItemType.NEW_POINT_3D_LEAF;

		case LEVELING_DIRECTORY:
			return TreeItemType.LEVELING_LEAF;
		case DIRECTION_DIRECTORY:
			return TreeItemType.DIRECTION_LEAF;
		case HORIZONTAL_DISTANCE_DIRECTORY:
			return TreeItemType.HORIZONTAL_DISTANCE_LEAF;
		case SLOPE_DISTANCE_DIRECTORY:
			return TreeItemType.SLOPE_DISTANCE_LEAF;
		case ZENITH_ANGLE_DIRECTORY:
			return TreeItemType.ZENITH_ANGLE_LEAF;
			
		case GNSS_1D_DIRECTORY:
			return TreeItemType.GNSS_1D_LEAF;
		case GNSS_2D_DIRECTORY:
			return TreeItemType.GNSS_2D_LEAF;
		case GNSS_3D_DIRECTORY:
			return TreeItemType.GNSS_3D_LEAF;
			
		case CONGRUENCE_ANALYSIS_1D_DIRECTORY:
			return TreeItemType.CONGRUENCE_ANALYSIS_1D_LEAF;
		case CONGRUENCE_ANALYSIS_2D_DIRECTORY:
			return TreeItemType.CONGRUENCE_ANALYSIS_2D_LEAF;
		case CONGRUENCE_ANALYSIS_3D_DIRECTORY:
			return TreeItemType.CONGRUENCE_ANALYSIS_3D_LEAF;
			
		default:
			return null;
		}
	}
	
	public static TreeItemType getDirectoryByLeafType(TreeItemType type) {
		switch (type) {
		case REFERENCE_POINT_1D_LEAF:
			return TreeItemType.REFERENCE_POINT_1D_DIRECTORY;
		case REFERENCE_POINT_2D_LEAF:
			return TreeItemType.REFERENCE_POINT_2D_DIRECTORY;
		case REFERENCE_POINT_3D_LEAF:
			return TreeItemType.REFERENCE_POINT_3D_DIRECTORY;
			
		case STOCHASTIC_POINT_1D_LEAF:
			return TreeItemType.STOCHASTIC_POINT_1D_DIRECTORY;
		case STOCHASTIC_POINT_2D_LEAF:
			return TreeItemType.STOCHASTIC_POINT_2D_DIRECTORY;
		case STOCHASTIC_POINT_3D_LEAF:
			return TreeItemType.STOCHASTIC_POINT_3D_DIRECTORY;
			
		case DATUM_POINT_1D_LEAF:
			return TreeItemType.DATUM_POINT_1D_DIRECTORY;
		case DATUM_POINT_2D_LEAF:
			return TreeItemType.DATUM_POINT_2D_DIRECTORY;
		case DATUM_POINT_3D_LEAF:
			return TreeItemType.DATUM_POINT_3D_DIRECTORY;
			
		case NEW_POINT_1D_LEAF:
			return TreeItemType.NEW_POINT_1D_DIRECTORY;
		case NEW_POINT_2D_LEAF:
			return TreeItemType.NEW_POINT_2D_DIRECTORY;
		case NEW_POINT_3D_LEAF:
			return TreeItemType.NEW_POINT_3D_DIRECTORY;

		case LEVELING_LEAF:
			return TreeItemType.LEVELING_DIRECTORY;
		case DIRECTION_LEAF:
			return TreeItemType.DIRECTION_DIRECTORY;
		case HORIZONTAL_DISTANCE_LEAF:
			return TreeItemType.HORIZONTAL_DISTANCE_DIRECTORY;
		case SLOPE_DISTANCE_LEAF:
			return TreeItemType.SLOPE_DISTANCE_DIRECTORY;
		case ZENITH_ANGLE_LEAF:
			return TreeItemType.ZENITH_ANGLE_DIRECTORY;
			
		case GNSS_1D_LEAF:
			return TreeItemType.GNSS_1D_DIRECTORY;
		case GNSS_2D_LEAF:
			return TreeItemType.GNSS_2D_DIRECTORY;
		case GNSS_3D_LEAF:
			return TreeItemType.GNSS_3D_DIRECTORY;
			
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return TreeItemType.CONGRUENCE_ANALYSIS_1D_DIRECTORY;
		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return TreeItemType.CONGRUENCE_ANALYSIS_2D_DIRECTORY;
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return TreeItemType.CONGRUENCE_ANALYSIS_3D_DIRECTORY;
			
		default:
			return null;
		}
	}
	
	public static PointType getPointTypeByTreeItemType(TreeItemType type) {
		switch (type) {
		case DATUM_POINT_1D_DIRECTORY:
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_DIRECTORY:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_DIRECTORY:
		case DATUM_POINT_3D_LEAF:
			return PointType.DATUM_POINT;

		case NEW_POINT_1D_DIRECTORY:
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_DIRECTORY:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_DIRECTORY:
		case NEW_POINT_3D_LEAF:
			return PointType.NEW_POINT;
			
		case REFERENCE_POINT_1D_DIRECTORY:
		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_DIRECTORY:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_DIRECTORY:
		case REFERENCE_POINT_3D_LEAF:
			return PointType.REFERENCE_POINT;
			
		case STOCHASTIC_POINT_1D_DIRECTORY:
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_DIRECTORY:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_DIRECTORY:
		case STOCHASTIC_POINT_3D_LEAF:
			return PointType.STOCHASTIC_POINT;
			
		default:
			return null;		
		}
	}
	
	public static ObservationType getObservationTypeByTreeItemType(TreeItemType type) {
		switch (type) {

		case LEVELING_DIRECTORY:
		case LEVELING_LEAF:
			return ObservationType.LEVELING;
			
		case DIRECTION_DIRECTORY:
		case DIRECTION_LEAF:
			return ObservationType.DIRECTION;
			
		case HORIZONTAL_DISTANCE_DIRECTORY:
		case HORIZONTAL_DISTANCE_LEAF:
			return ObservationType.HORIZONTAL_DISTANCE;
			
		case SLOPE_DISTANCE_DIRECTORY:
		case SLOPE_DISTANCE_LEAF:
			return ObservationType.SLOPE_DISTANCE;
			
		case ZENITH_ANGLE_DIRECTORY:
		case ZENITH_ANGLE_LEAF:
			return ObservationType.ZENITH_ANGLE;
			
		case GNSS_1D_DIRECTORY:
		case GNSS_1D_LEAF:
			return ObservationType.GNSS1D;
			
		case GNSS_2D_DIRECTORY:
		case GNSS_2D_LEAF:
			return ObservationType.GNSS2D;
					
		case GNSS_3D_DIRECTORY:
		case GNSS_3D_LEAF:
			return ObservationType.GNSS3D;
			
		default:
			return null;
		}
	}
	
	public static TreeItemType getTreeItemTypeByObservationType(ObservationType type) {
		switch (type) {
		case LEVELING:
			return TreeItemType.LEVELING_LEAF;
		case DIRECTION:
			return TreeItemType.DIRECTION_LEAF;
		case HORIZONTAL_DISTANCE:
			return TreeItemType.HORIZONTAL_DISTANCE_LEAF;
		case SLOPE_DISTANCE:
			return TreeItemType.SLOPE_DISTANCE_LEAF;
		case ZENITH_ANGLE:
			return TreeItemType.ZENITH_ANGLE_LEAF;
		case GNSS1D:
			return TreeItemType.GNSS_1D_LEAF;
		case GNSS2D:
			return TreeItemType.GNSS_2D_LEAF;
		case GNSS3D:
			return TreeItemType.GNSS_3D_LEAF;
		default:
			return null;
		}
	}
	
	public static TreeItemType getTreeItemTypeByPointType(PointType type, int dimension) {
		switch (type) {
		case REFERENCE_POINT:
			if (dimension == 1)
				return TreeItemType.REFERENCE_POINT_1D_LEAF;
			else if (dimension == 2)
				return TreeItemType.REFERENCE_POINT_2D_LEAF;
			else if (dimension == 3)
				return TreeItemType.REFERENCE_POINT_3D_LEAF;
		case STOCHASTIC_POINT:
			if (dimension == 1)
				return TreeItemType.STOCHASTIC_POINT_1D_LEAF;
			else if (dimension == 2)
				return TreeItemType.STOCHASTIC_POINT_2D_LEAF;
			else if (dimension == 3)
				return TreeItemType.STOCHASTIC_POINT_3D_LEAF;
		case DATUM_POINT:
			if (dimension == 1)
				return TreeItemType.DATUM_POINT_1D_LEAF;
			else if (dimension == 2)
				return TreeItemType.DATUM_POINT_2D_LEAF;
			else if (dimension == 3)
				return TreeItemType.DATUM_POINT_3D_LEAF;
		case NEW_POINT:
			if (dimension == 1)
				return TreeItemType.NEW_POINT_1D_LEAF;
			else if (dimension == 2)
				return TreeItemType.NEW_POINT_2D_LEAF;
			else if (dimension == 3)
				return TreeItemType.NEW_POINT_3D_LEAF;
		default:
			return null;
		}
	}
	
	public static boolean isPointTypeLeaf(TreeItemType type) {
		switch (type) {
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_LEAF:
		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
			return true;
		default:
			return false;		
		}
	}
	
	public static boolean isPointTypeDirectory(TreeItemType type) {
		TreeItemType child = getLeafByDirectoryType(type);
		return child != null && isPointTypeLeaf(child);
	}
	
	public static boolean isObservationTypeLeaf(TreeItemType type) {
		switch (type) {
		case LEVELING_LEAF:
		case DIRECTION_LEAF:
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
		case ZENITH_ANGLE_LEAF:
			return true;
			
		default:
			return false;
		}
	}
	
	public static boolean isObservationTypeDirectory(TreeItemType type) {
		TreeItemType child = getLeafByDirectoryType(type);
		return child != null && isObservationTypeLeaf(child);
	}
	
	public static boolean isGNSSObservationTypeLeaf(TreeItemType type) {
		switch (type) {
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:
			return true;
			
		default:
			return false;
		}
	}
	
	public static boolean isGNSSObservationTypeDirectory(TreeItemType type) {
		TreeItemType child = getLeafByDirectoryType(type);
		return child != null && isGNSSObservationTypeLeaf(child);
	}
	
	
	public static boolean isCongruenceAnalysisTypeLeaf(TreeItemType type) {
		switch (type) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
		case CONGRUENCE_ANALYSIS_2D_LEAF:
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return true;
		default:
			return false;		
		}
	}
	
	public static boolean isCongruenceAnalysisTypeDirectory(TreeItemType type) {
		TreeItemType child = getLeafByDirectoryType(type);
		return child != null && isCongruenceAnalysisTypeLeaf(child);
	}
}
