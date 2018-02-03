package org.applied_geodesy.jag3d.ui.dnd;

public abstract class ObservationRowDnD extends RowDnD {
	private static final long serialVersionUID = 4855377431348113363L;
	private String startPointName; 
	private String endPointName;
	
	public String getStartPointName() {
		return startPointName;
	}
	void setStartPointName(String startPointName) {
		this.startPointName = startPointName;
	}
	public String getEndPointName() {
		return endPointName;
	}
	void setEndPointName(String endPointName) {
		this.endPointName = endPointName;
	}
}
