package org.applied_geodesy.adjustment.network;

public enum ParameterType {

	ORIENTATION(1),
	SCALE(2),
	ZERO_POINT_OFFSET(3),	                        
	REFRACTION_INDEX(4),
	
	POINT1D(5),
	POINT2D(6),
	POINT3D(7),
	
	ROTATION_X(8),
	ROTATION_Y(9),
	ROTATION_Z(10),

	DEFLECTION_X(11),
	DEFLECTION_Y(12),

	STRAIN_A11(101),
	STRAIN_A12(102),
	STRAIN_A21(103),
	STRAIN_A22(104),

	STRAIN_Q0(111),
	STRAIN_Q1(112),
	STRAIN_Q2(113),
	STRAIN_Q3(114),

	STRAIN_S11(121),
	STRAIN_S12(122),
	STRAIN_S13(123),
	STRAIN_S22(124),
	STRAIN_S23(125),
	STRAIN_S33(126),

	STRAIN_TRANSLATION_X(131),
	STRAIN_TRANSLATION_Y(132),
	STRAIN_TRANSLATION_Z(133),

	STRAIN_SCALE_X(141),
	STRAIN_SCALE_Y(142),
	STRAIN_SCALE_Z(143),

	STRAIN_ROTATION_X(151),
	STRAIN_ROTATION_Y(152),
	STRAIN_ROTATION_Z(153),

	STRAIN_SHEAR_X(161),
	STRAIN_SHEAR_Y(162),
	STRAIN_SHEAR_Z(163);

	private int id;
	private ParameterType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ParameterType getEnumByValue(int value) {
		for(ParameterType element : ParameterType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}	
