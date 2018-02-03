package org.applied_geodesy.jag3d.ui.dnd;

import java.io.Serializable;

public abstract class RowDnD implements Serializable {
	private static final long serialVersionUID = 7702551810226354087L;
	private int id = -1;
	private boolean enable = true;
	
	public int getId() {
		return id;
	}
	void setId(int id) {
		this.id = id;
	}
	public boolean isEnable() {
		return enable;
	}
	void setEnable(boolean enable) {
		this.enable = enable;
	}
}
