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

package org.applied_geodesy.juniform.ui.menu;

enum MenuItemType {
	
	LINE,
	CIRCLE,
	ELLIPSE,
	QUADRATIC_CURVE,
	
	PLANE,
	SPHERE,
	ELLIPSOID,
	CIRCULAR_CYLINDER,
	CYLINDER,
	CIRCULAR_CONE,
	CONE,
	CIRCULAR_PARABOLOID,
	PARABOLOID,
	SPATIAL_CIRCLE,
	SPATIAL_ELLIPSE,
	SPATIAL_LINE,
	QUADRATIC_SURFACE,
	
	MODIFIABLE_CURVE,
	MODIFIABLE_SURFACE,
	
	IMPORT_CURVE_POINTS,
	IMPORT_SURFACE_POINTS,
		
	FEATURE_PROPERTIES,
	PARAMETER_PROPERTIES,
	RESTRICTION_PROPERTIES,
	POSTPROCESSING_PROPERTIES,
	
	TEST_STATISTIC,
	LEAST_SQUARES,
	PREFERENCES,
	
	QUANTILES,
	VARIANCE_COMPONENT_ESTIMATION,
	
	EXPORT_MATLAB,
	REPORT,
	ABOUT,
	EXIT;
}
