package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;

public class TerrestrialObservationRowDnD extends ObservationRowDnD {
	private static final long serialVersionUID = 6880080455876005180L;

	private double valueApriori;
	private double distanceApriori  = -1.0;
	
	private double instrumentHeight =  0.0;
	private double reflectorHeight  =  0.0;
	
	private double sigmaApriori     = -1.0;

	private TerrestrialObservationRowDnD() {}
	
	public static TerrestrialObservationRowDnD fromTerrestrialObservationRow(TerrestrialObservationRow row) {
		if (row.getValueApriori() == null || row.getStartPointName() == null || row.getEndPointName() == null ||
				row.getStartPointName().trim().isEmpty() || row.getEndPointName().trim().isEmpty() || 
				row.getStartPointName().equals(row.getEndPointName()))
			return null;
		
		TerrestrialObservationRowDnD rowDnD = new TerrestrialObservationRowDnD();
		
		rowDnD.setId(row.getId());
		rowDnD.setEnable(row.isEnable());
		
		rowDnD.setStartPointName(row.getStartPointName());
		rowDnD.setEndPointName(row.getEndPointName());
		
		rowDnD.instrumentHeight = row.getInstrumentHeight() == null ? 0 : row.getInstrumentHeight();
		rowDnD.reflectorHeight  = row.getReflectorHeight() == null ? 0 : row.getReflectorHeight();
		
		rowDnD.valueApriori    = row.getValueApriori();
		rowDnD.distanceApriori = row.getDistanceApriori() == null || row.getDistanceApriori() < 0 ? -1 : row.getDistanceApriori();
		rowDnD.sigmaApriori    = row.getSigmaApriori() == null || row.getSigmaApriori() < 0 ? -1 : row.getSigmaApriori();
		
		return rowDnD;
	}
	
	public TerrestrialObservationRow toTerrestrialObservationRow() {
		TerrestrialObservationRow row = new TerrestrialObservationRow();
		
		row.setId(this.getId());
		row.setEnable(this.isEnable());
		
		row.setStartPointName(this.getStartPointName());
		row.setEndPointName(this.getEndPointName());
		
		row.setInstrumentHeight(this.instrumentHeight);
		row.setReflectorHeight(this.reflectorHeight);
		
		row.setValueApriori(this.valueApriori);
		row.setDistanceApriori(this.distanceApriori < 0 ? null : this.distanceApriori);
		row.setSigmaApriori(this.sigmaApriori < 0 ? null : this.sigmaApriori);
		
		return row;
	}
}
