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

package org.applied_geodesy.adjustment.network.congruence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations1D;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations2D;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations3D;

public class CongruenceAnalysisGroup {
	private final int id; 
	private final int dimension; 
	private Map<String, CongruenceAnalysisPointPair> commonPointPairMap = new LinkedHashMap<String, CongruenceAnalysisPointPair>();
	private Map<String, CongruenceAnalysisPointPair> strainAnalysablePointPairMap = new LinkedHashMap<String, CongruenceAnalysisPointPair>();
	private List<CongruenceAnalysisPointPair> commonPointPairList = new ArrayList<CongruenceAnalysisPointPair>();
	private List<CongruenceAnalysisPointPair> strainAnalysablePointPairList = new ArrayList<CongruenceAnalysisPointPair>();
	private StrainAnalysisEquations strainAnalysisEquations;

	public CongruenceAnalysisGroup(int id, int dimension) throws IllegalArgumentException {
		this.id = id;
		this.dimension = dimension;
		if (dimension == 1)
			this.strainAnalysisEquations = new StrainAnalysisEquations1D();
		else if (dimension == 2)
			this.strainAnalysisEquations = new StrainAnalysisEquations2D();
		else if (dimension == 3)
			this.strainAnalysisEquations = new StrainAnalysisEquations3D();
		else
			throw new IllegalArgumentException("Error, invalid vector dimension. Dimesnion mus be between 1 and 3: " + dimension);
	}

	public void setStrainRestrictions(RestrictionType restriction) {
		this.strainAnalysisEquations.addRestriction(restriction);
	}

	public boolean isRestricted(RestrictionType restriction) {
		return this.strainAnalysisEquations.isRestricted(restriction);
	}

	public final int getId() {
		return this.id;
	}

	public boolean add(CongruenceAnalysisPointPair nexus, boolean analysablePointPair) {
		int dimesnion = nexus.getDimension();
		String startPointId = nexus.getStartPoint().getName();
		String endPointId   = nexus.getEndPoint().getName();
		
		if (this.dimension != dimesnion || this.commonPointPairMap.containsKey( startPointId ) || this.commonPointPairMap.containsKey( endPointId ) ||
				this.strainAnalysablePointPairMap.containsKey( startPointId ) || this.strainAnalysablePointPairMap.containsKey( endPointId ))
			return false;
						
		if (analysablePointPair) {
			this.strainAnalysablePointPairList.add( nexus );
			this.strainAnalysablePointPairMap.put( startPointId, nexus );
			this.strainAnalysablePointPairMap.put( endPointId, nexus );
		}
		else {
			this.commonPointPairMap.put( startPointId, nexus );
			this.commonPointPairMap.put( endPointId, nexus );
			this.commonPointPairList.add( nexus );
		}
		
		return true;
	}

	public CongruenceAnalysisPointPair get(int index, boolean analysablePointPair) {
		return analysablePointPair ? this.strainAnalysablePointPairList.get( index ) : this.commonPointPairList.get( index );
	}

	public int totalSize() {
		return this.strainAnalysablePointPairList.size() + this.commonPointPairList.size();
	}

	public int size(boolean analysablePointPair) {
		return analysablePointPair ? this.strainAnalysablePointPairList.size() : this.commonPointPairList.size();
	}

	public int getDimension() {
		return this.dimension;
	}
	
	public StrainAnalysisEquations getStrainAnalysisEquations() {
		return this.strainAnalysisEquations;
	}
	
	public boolean isEmpty() {
		return this.totalSize() == 0;
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.id + " point pairs in group: " + this.totalSize());
	}	
}