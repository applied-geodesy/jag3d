package org.applied_geodesy.adjustment.network.congruence.strain;

public enum RestrictionType {
	UNIT_QUATERNION(0),

	FIXED_TRANSLATION_X(1),
	FIXED_TRANSLATION_Y(2),
	FIXED_TRANSLATION_Z(3),

	FIXED_ROTATION_X(4),
	FIXED_ROTATION_Y(5),
	FIXED_ROTATION_Z(6),

	FIXED_SCALE_X(7),
	FIXED_SCALE_Y(8),
	FIXED_SCALE_Z(9),

	FIXED_SHEAR_X(10),
	FIXED_SHEAR_Y(11),
	FIXED_SHEAR_Z(12),

	IDENT_SCALES_XY(78),
	IDENT_SCALES_XZ(79),
	IDENT_SCALES_YZ(89);

	private final int id;
	private RestrictionType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}

	public static RestrictionType getEnumByValue(int id) {
		RestrictionType[] restrictions = RestrictionType.values();
		for (RestrictionType restriction : restrictions) {
			if (restriction.getId() == id)
				return restriction;
		}
		return null;
	}
}
