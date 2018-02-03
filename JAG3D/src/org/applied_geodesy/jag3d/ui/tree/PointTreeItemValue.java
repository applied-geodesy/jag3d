package org.applied_geodesy.jag3d.ui.tree;

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PointTreeItemValue extends TreeItemValue {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	
	PointTreeItemValue(TreeItemType type, String name) throws IllegalArgumentException {
		this(-1, type, name);
	}
	
	PointTreeItemValue(int groupId, TreeItemType type, String name) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE);
	}
	
	PointTreeItemValue(int groupId, TreeItemType type, String name, boolean enable) throws IllegalArgumentException {
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
	}
	
	public final PointType getPointType() {
		switch (this.getItemType()) {
		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
			return PointType.REFERENCE_POINT;
			
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
			return PointType.STOCHASTIC_POINT;
			
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
			return PointType.DATUM_POINT;
			
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_LEAF:
			return PointType.NEW_POINT;

		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to a PointType " + this.getItemType());
		}
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
		case REFERENCE_POINT_1D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case DATUM_POINT_1D_LEAF:
		case NEW_POINT_1D_LEAF:
			return 1;

		case REFERENCE_POINT_2D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:	
		case DATUM_POINT_2D_LEAF:
		case NEW_POINT_2D_LEAF:
			return 2;

		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			return 3;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to a PointType " + this.getItemType());
		}
	}


	@Override
	public TabType[] getTabTypes() {
		TreeItemType type = this.getItemType();
		List<TabType> tabTyps = new ArrayList<TabType>(5);

		// A-priori Values
		tabTyps.add(TabType.RAW_DATA);
		
		// Properties 
		switch (type) {
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:	
		case STOCHASTIC_POINT_3D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			tabTyps.add(TabType.PROPERTIES);
			break;
		default:
			break;
		}
		
		// Results Point
		tabTyps.add(TabType.RESULT_DATA);
		
		// Result Deflections
		switch (type) {
		//case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_DEFLECTION);
			break;
		default:
			break;
		}
		
		// Congruence points
		switch (type) {
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_CONGRUENCE_ANALYSIS_POINT);
			break;
		default:
			break;
		}
		
		// Congruence deflection
		switch (type) {
		case DATUM_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_CONGRUENCE_ANALYSIS_DEFLECTION);
			break;
		default:
			break;
		}
		
		return tabTyps.toArray(new TabType[tabTyps.size()]);
	}
	
	public static double getDefaultUncertainty(PointGroupUncertaintyType uncertaintyType) {
		switch(uncertaintyType) {
		case CONSTANT_X:
			return DefaultUncertainty.getUncertaintyX();
		case CONSTANT_Y:
			return DefaultUncertainty.getUncertaintyY();
		case CONSTANT_Z:
			return DefaultUncertainty.getUncertaintyZ();
		case DEFLECTION_X:
			return DefaultUncertainty.getUncertaintyDeflectionX();
		case DEFLECTION_Y:
			return DefaultUncertainty.getUncertaintyDeflectionY();
		default:
			return 0;
		}
	}
}
