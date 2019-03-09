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

import org.applied_geodesy.jag3d.ui.table.row.PointRow;

public class PointRowDnD extends TableRowDnD {
	private static final long serialVersionUID = 7564914007654701928L;
	private String name = null;
	private String code = null;
	
	private double xApriori = 0.0;
	private double yApriori = 0.0;
	private double zApriori = 0.0;
	
	private double sigmaXapriori = -1.0;
	private double sigmaYapriori = -1.0;
	private double sigmaZapriori = -1.0;

	public static PointRowDnD fromPointRow(PointRow row) {
		if (row.getXApriori() == null || row.getYApriori() == null || row.getZApriori() == null || 
				row.getName() == null || row.getName().trim().isEmpty())
			return null;
		
		PointRowDnD rowDnD = new PointRowDnD();
		
		rowDnD.setId(row.getId());
		rowDnD.setGroupId(row.getGroupId());
		rowDnD.setEnable(row.isEnable());
		
		rowDnD.name = row.getName();
		rowDnD.code = row.getCode();

		rowDnD.xApriori      = row.getXApriori();
		rowDnD.yApriori      = row.getYApriori();
		rowDnD.zApriori      = row.getZApriori();

		rowDnD.sigmaXapriori = row.getSigmaXapriori() == null || row.getSigmaXapriori() < 0 ? -1 : row.getSigmaXapriori();
		rowDnD.sigmaYapriori = row.getSigmaYapriori() == null || row.getSigmaYapriori() < 0 ? -1 : row.getSigmaYapriori();
		rowDnD.sigmaZapriori = row.getSigmaZapriori() == null || row.getSigmaZapriori() < 0 ? -1 : row.getSigmaZapriori();
		
		return rowDnD;
	}
	
	public PointRow toPointRow() {
		PointRow row = new PointRow();
		
		row.setId(this.getId());
		row.setGroupId(this.getGroupId());
		row.setEnable(this.isEnable());
		
		row.setName(this.name);
		row.setCode(this.code);
		
		row.setXApriori(this.xApriori);
		row.setYApriori(this.yApriori);
		row.setZApriori(this.zApriori);
		
		row.setSigmaXapriori(this.sigmaXapriori < 0 ? null : this.sigmaXapriori);
		row.setSigmaYapriori(this.sigmaYapriori < 0 ? null : this.sigmaYapriori);
		row.setSigmaZapriori(this.sigmaZapriori < 0 ? null : this.sigmaZapriori);
		
		return row;
	}
}

