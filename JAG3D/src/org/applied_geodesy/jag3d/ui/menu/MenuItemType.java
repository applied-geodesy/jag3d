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

package org.applied_geodesy.jag3d.ui.menu;

enum MenuItemType {
	NEW, 
	OPEN,
	COPY,
	CLOSE,
	EXIT,
	
	PREFERENCES,
	TEST_STATISTIC,
	HORIZONTAL_PROJECTION,
	CONGRUENT_POINT,
	RANK_DEFECT,
	AVERAGE,
	APROXIMATE_VALUES,
	LEAST_SQUARES,
	
	ABOUT,
	CHECK_UPDATES,
	
	REPORT,
	
	RECENTLY_USED,
	
	IMPORT_FLAT_LEVELING, 
	IMPORT_FLAT_DIRECTION, 
	IMPORT_FLAT_HORIZONTAL_DISTANCE, 
	IMPORT_FLAT_SLOPE_DISTANCE, 
	IMPORT_FLAT_ZENITH_ANGLE, 
	IMPORT_FLAT_GNSS1D, 
	IMPORT_FLAT_GNSS2D, 
	IMPORT_FLAT_GNSS3D,
	
	IMPORT_FLAT_REFERENCE_POINT_1D,
	IMPORT_FLAT_REFERENCE_POINT_2D,
	IMPORT_FLAT_REFERENCE_POINT_3D,
	
	IMPORT_FLAT_STOCHASTIC_POINT_1D,
	IMPORT_FLAT_STOCHASTIC_POINT_2D,
	IMPORT_FLAT_STOCHASTIC_POINT_3D,
	
	IMPORT_FLAT_DATUM_POINT_1D,
	IMPORT_FLAT_DATUM_POINT_2D,
	IMPORT_FLAT_DATUM_POINT_3D,
	
	IMPORT_FLAT_NEW_POINT_1D,
	IMPORT_FLAT_NEW_POINT_2D,
	IMPORT_FLAT_NEW_POINT_3D,
	
	IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_1D,
	IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_2D,
	IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_3D,
	
	IMPORT_BEO,
	IMPORT_Z,
	IMPORT_DL100,
	
	IMPORT_M5,
	
	IMPORT_GSI1D,
	IMPORT_GSI2D,
	IMPORT_GSI2DH,
	IMPORT_GSI3D,
	
	IMPORT_LAND_XML2D,
	IMPORT_LAND_XML3D,
	
	IMPORT_JOB_XML2D,
	IMPORT_JOB_XML2DH,
	IMPORT_JOB_XML3D,
	
	IMPORT_COLUMN_BASED_FILES,
	
	HIGHLIGHT_TABLE_ROWS,
		
	MODULE_GEOTRA,
	MODULE_FORMFITTINGTOOLBOX,
	MODULE_COORDTRANS,

}
