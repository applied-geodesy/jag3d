package org.applied_geodesy.adjustment.statistic;

public enum TestStatisticType {

	NONE(1),
	BAARDA_METHOD(2), 
	SIDAK(3); 
	
	private final int id;
	private TestStatisticType(int id) {
		this.id = id;
	}
		
	public int getId() {
		return this.id;
	}
	
	public static TestStatisticType getEnumByValue(int id) {
		for (TestStatisticType type : TestStatisticType.values()) 
			if (type.getId() == id)
				return type;
		return null;
	}
}