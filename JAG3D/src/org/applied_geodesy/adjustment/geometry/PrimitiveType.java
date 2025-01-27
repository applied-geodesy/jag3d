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

package org.applied_geodesy.adjustment.geometry;

import java.util.ArrayList;
import java.util.List;

public enum PrimitiveType {
	LINE(FeatureType.CURVE),
	CIRCLE(FeatureType.CURVE),
	ELLIPSE(FeatureType.CURVE),
	QUADRATIC_CURVE(FeatureType.CURVE),
	
	PLANE(FeatureType.SURFACE),
	SPHERE(FeatureType.SURFACE),
	ELLIPSOID(FeatureType.SURFACE),
	CYLINDER(FeatureType.SURFACE),
	CONE(FeatureType.SURFACE),
	PARABOLOID(FeatureType.SURFACE),
	QUADRATIC_SURFACE(FeatureType.SURFACE),
	TORUS(FeatureType.SURFACE),
	;
	
	private final FeatureType featureType;
	private PrimitiveType(FeatureType featureType) {
		this.featureType = featureType;
	}
	
	public FeatureType getFeatureType() {
		return this.featureType;
	}
	
	public static PrimitiveType[] values(FeatureType featureType) {
		List<PrimitiveType> primitiveTypeList = new ArrayList<PrimitiveType>();
		for (PrimitiveType primitiveType : values()) {
			if (primitiveType.getFeatureType() == featureType)
				primitiveTypeList.add(primitiveType);
		}
		
		PrimitiveType[] primitiveTypeArray = new PrimitiveType[primitiveTypeList.size()];
        return primitiveTypeList.toArray(primitiveTypeArray);
	}
}
