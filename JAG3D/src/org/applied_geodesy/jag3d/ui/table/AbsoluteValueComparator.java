package org.applied_geodesy.jag3d.ui.table;

import java.util.Comparator;

public class AbsoluteValueComparator implements Comparator<Double> {

	@Override
	public int compare(Double val1, Double val2) {
		if (val1 == null && val2 == null)
			return 0;
		else if (val1 == null)
			return -1;
		else if (val2 == null)
			return +1;
		else if (Math.abs(val1.doubleValue()) < Math.abs(val2.doubleValue()))
			return -1;
		else if(Math.abs(val1.doubleValue()) > Math.abs(val2.doubleValue()))
			return +1;
		else
			return  0;
	}
}
