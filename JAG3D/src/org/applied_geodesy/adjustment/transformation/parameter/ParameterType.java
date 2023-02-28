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

package org.applied_geodesy.adjustment.transformation.parameter;

public enum ParameterType {
	SHIFT_X,
	SHIFT_Y,
	SHIFT_Z,

	QUATERNION_Q0,
	QUATERNION_Q1,
	QUATERNION_Q2,
	QUATERNION_Q3,
	
	SCALE_X,
	SCALE_Y,
	SCALE_Z,
	
	SHEAR_X,
	SHEAR_Y,
	SHEAR_Z,
	
	EULER_ANGLE_X,
	EULER_ANGLE_Y,
	EULER_ANGLE_Z,
	
	SCALE_SHEAR_COMPONENT_S11,
	SCALE_SHEAR_COMPONENT_S12,
	SCALE_SHEAR_COMPONENT_S13,
	
	SCALE_SHEAR_COMPONENT_S22,
	SCALE_SHEAR_COMPONENT_S23,
	
	SCALE_SHEAR_COMPONENT_S33,
	
	LENGTH,
	VECTOR_LENGTH,
	
	CONSTANT,
	;
}
