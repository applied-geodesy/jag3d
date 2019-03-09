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

package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;

public class CongruenceAnalysisRowDnD extends TableRowDnD {
	private static final long serialVersionUID = 6317004442775639322L;
	private String nameInReferenceEpoch = null;
	private String nameInControlEpoch = null;

	public static CongruenceAnalysisRowDnD fromCongruenceAnalysisRow(CongruenceAnalysisRow row) {
		if (row.getNameInReferenceEpoch() == null || row.getNameInControlEpoch() == null ||
				row.getNameInReferenceEpoch().trim().isEmpty() || row.getNameInControlEpoch().trim().isEmpty() || 
				row.getNameInReferenceEpoch().equals(row.getNameInControlEpoch()))
			return null;
		
		CongruenceAnalysisRowDnD rowDnD = new CongruenceAnalysisRowDnD();
		
		rowDnD.setId(row.getId());
		rowDnD.setGroupId(row.getGroupId());
		rowDnD.setEnable(row.isEnable());
		
		rowDnD.nameInReferenceEpoch = row.getNameInReferenceEpoch();
		rowDnD.nameInControlEpoch   = row.getNameInControlEpoch();

		return rowDnD;
	}
	
	public CongruenceAnalysisRow toCongruenceAnalysisRow() {
		CongruenceAnalysisRow row = new CongruenceAnalysisRow();
		
		row.setId(this.getId());
		row.setGroupId(this.getGroupId());
		row.setEnable(this.isEnable());
		
		row.setNameInReferenceEpoch(this.nameInReferenceEpoch);
		row.setNameInControlEpoch(this.nameInControlEpoch);
		
		return row;
	}
}