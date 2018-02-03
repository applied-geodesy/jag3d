package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.table.row.PointRow;

public class PointRowDnD extends RowDnD{
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

