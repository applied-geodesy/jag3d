package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;

public class CongruenceAnalysisRowDnD extends RowDnD{
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
		rowDnD.setEnable(row.isEnable());
		
		rowDnD.nameInReferenceEpoch = row.getNameInReferenceEpoch();
		rowDnD.nameInControlEpoch   = row.getNameInControlEpoch();

		return rowDnD;
	}
	
	public CongruenceAnalysisRow toCongruenceAnalysisRow() {
		CongruenceAnalysisRow row = new CongruenceAnalysisRow();
		
		row.setId(this.getId());
		row.setEnable(this.isEnable());
		
		row.setNameInReferenceEpoch(this.nameInReferenceEpoch);
		row.setNameInControlEpoch(this.nameInControlEpoch);
		
		return row;
	}
}