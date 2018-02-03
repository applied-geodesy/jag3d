package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;

public class GNSSObservationRowDnD extends ObservationRowDnD {
	private static final long serialVersionUID = 7009256737601618896L;
	private double xApriori = 0.0;
	private double yApriori = 0.0;
	private double zApriori = 0.0;
	
	private double sigmaXapriori = -1.0;
	private double sigmaYapriori = -1.0;
	private double sigmaZapriori = -1.0;
	
	public static GNSSObservationRowDnD fromGNSSObservationRow(GNSSObservationRow row) {
		if (row.getXApriori() == null || row.getYApriori() == null || row.getZApriori() == null || 
				row.getStartPointName() == null || row.getEndPointName() == null ||
				row.getStartPointName().trim().isEmpty() || row.getEndPointName().trim().isEmpty() || 
				row.getStartPointName().equals(row.getEndPointName()))
			return null;
		
		GNSSObservationRowDnD rowDnD = new GNSSObservationRowDnD();
		
		rowDnD.setId(row.getId());
		rowDnD.setEnable(row.isEnable());
		
		rowDnD.setStartPointName(row.getStartPointName());
		rowDnD.setEndPointName(row.getEndPointName());

		rowDnD.xApriori      = row.getXApriori();
		rowDnD.yApriori      = row.getYApriori();
		rowDnD.zApriori      = row.getZApriori();

		rowDnD.sigmaXapriori = row.getSigmaXapriori() == null || row.getSigmaXapriori() < 0 ? -1 : row.getSigmaXapriori();
		rowDnD.sigmaYapriori = row.getSigmaYapriori() == null || row.getSigmaYapriori() < 0 ? -1 : row.getSigmaYapriori();
		rowDnD.sigmaZapriori = row.getSigmaZapriori() == null || row.getSigmaZapriori() < 0 ? -1 : row.getSigmaZapriori();
		
		return rowDnD;
	}
	
	public GNSSObservationRow toGNSSObservationRow() {
		GNSSObservationRow row = new GNSSObservationRow();
		
		row.setId(this.getId());
		row.setEnable(this.isEnable());
		
		row.setStartPointName(this.getStartPointName());
		row.setEndPointName(this.getEndPointName());
		
		row.setXApriori(this.xApriori);
		row.setYApriori(this.yApriori);
		row.setZApriori(this.zApriori);
		
		row.setSigmaXapriori(this.sigmaXapriori < 0 ? null : this.sigmaXapriori);
		row.setSigmaYapriori(this.sigmaYapriori < 0 ? null : this.sigmaYapriori);
		row.setSigmaZapriori(this.sigmaZapriori < 0 ? null : this.sigmaZapriori);
		
		return row;
	}
}
