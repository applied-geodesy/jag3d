package org.applied_geodesy.jag3d.ui.graphic.sql;

public class PointPairKey {
	private String startPointName;
	private String endPointName;
	public PointPairKey(String startPointName, String endPointName) {
		boolean interChange = startPointName.compareTo(endPointName) <= 0;
		this.startPointName = interChange ? startPointName : endPointName;
		this.endPointName   = interChange ? endPointName : startPointName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endPointName == null) ? 0 : endPointName.hashCode());
		result = prime * result + ((startPointName == null) ? 0 : startPointName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointPairKey other = (PointPairKey) obj;
		if (this.endPointName != null && other.endPointName != null && this.endPointName.equals(other.endPointName) &&
				this.startPointName != null && other.startPointName != null && this.startPointName.equals(other.startPointName))
			return true;
		return false;
	}
}
