package org.applied_geodesy.jag3d.ui.tree;

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CongruenceAnalysisTreeItemValue extends TreeItemValue {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	
	CongruenceAnalysisTreeItemValue(TreeItemType type, String name) throws IllegalArgumentException {
		this(-1, type, name);
	}
	
	CongruenceAnalysisTreeItemValue(int groupId, TreeItemType type, String name) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE);
	}
	
	CongruenceAnalysisTreeItemValue(int groupId, TreeItemType type, String name, boolean enable) throws IllegalArgumentException {
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
	}
	
	public IntegerProperty groupIdProperty() {
		return this.groupId;
	}
	
	public int getGroupId() {
		return this.groupIdProperty().get();
	}
	
	public void setGroupId(final int groupId) {
		this.groupIdProperty().set(groupId);
	}
	
	public final int getDimension() {
		switch (this.getItemType()) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return 1;

		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return 2;

		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return 3;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to an object point nexus (congruence analysis) " + this.getItemType());
		}
	}
	
	public static RestrictionType[] getRestrictionTypes(TreeItemType itemType) {
		switch (itemType) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Z,
					
					RestrictionType.FIXED_SCALE_Z
			};
		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Y,
					RestrictionType.FIXED_TRANSLATION_X,
					
					RestrictionType.FIXED_ROTATION_Z,
					
					RestrictionType.FIXED_SCALE_Y,
					RestrictionType.FIXED_SCALE_X,
					
					RestrictionType.FIXED_SHEAR_Z
			};
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Y,
					RestrictionType.FIXED_TRANSLATION_X,
					RestrictionType.FIXED_TRANSLATION_Z,
					
					RestrictionType.FIXED_ROTATION_Y,
					RestrictionType.FIXED_ROTATION_X,
					RestrictionType.FIXED_ROTATION_Z,
					
					RestrictionType.FIXED_SCALE_Y,
					RestrictionType.FIXED_SCALE_X,
					RestrictionType.FIXED_SCALE_Z,
					
					RestrictionType.FIXED_SHEAR_Y,
					RestrictionType.FIXED_SHEAR_X,
					RestrictionType.FIXED_SHEAR_Z
			};
		default:
			throw new IllegalArgumentException(ObservationTreeItemValue.class.getSimpleName() + " : Error, TreeItemType does not refer to a congruence analysis type " + itemType);
		}
	}
	
	public static ParameterType[] getParameterTypes(TreeItemType itemType) {
		switch (itemType) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Z,
					ParameterType.STRAIN_SCALE_Z,
			};
		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Y,
					ParameterType.STRAIN_TRANSLATION_X,
					ParameterType.STRAIN_ROTATION_Z,
					ParameterType.STRAIN_SCALE_Y,
					ParameterType.STRAIN_SCALE_X,
					ParameterType.STRAIN_SHEAR_Z,
			};
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Y,
					ParameterType.STRAIN_TRANSLATION_X,
					ParameterType.STRAIN_TRANSLATION_Z,
					ParameterType.STRAIN_ROTATION_Y,
					ParameterType.STRAIN_ROTATION_X,
					ParameterType.STRAIN_ROTATION_Z,
					ParameterType.STRAIN_SCALE_Y,
					ParameterType.STRAIN_SCALE_X,
					ParameterType.STRAIN_SHEAR_Y,
					ParameterType.STRAIN_SHEAR_X,
					ParameterType.STRAIN_SHEAR_Z,
			};
		default:
			throw new IllegalArgumentException(ObservationTreeItemValue.class.getSimpleName() + " : Error, TreeItemType does not refer to a congruence analysis type " + itemType);
		}
	}

	@Override
	public TabType[] getTabTypes() {
		return new TabType[] {
				TabType.RAW_DATA,
				TabType.PROPERTIES,
				TabType.RESULT_CONGRUENCE_ANALYSIS_POINT,
				TabType.ADDITIONAL_PARAMETER
		};
	}
}