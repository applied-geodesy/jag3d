package org.applied_geodesy.adjustment.network.observation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.parameter.RotationZ;
import org.applied_geodesy.adjustment.network.parameter.Scale;
import org.applied_geodesy.adjustment.point.Point;

import no.uib.cipr.matrix.Matrix;

public abstract class GNSSBaseline extends Observation {
	//private List<Observation> baselineComponents = new ArrayList<Observation>(3);
	
	private Map<ComponentType, GNSSBaseline> baselineComponents = new LinkedHashMap<ComponentType, GNSSBaseline>(3);
	
	
	private Scale scale = new Scale();
	private RotationZ rz = new RotationZ();
	private Matrix subR;
	
	public GNSSBaseline(int id, Point startPoint, Point endPoint, double startPointHeight, double endPointHeight, double observation, double sigma) {
		super(id, startPoint, endPoint, startPointHeight, endPointHeight, observation, sigma, Math.abs(observation));
		this.baselineComponents.put(this.getComponent(), this);
	}

	/**
	 * Fuegt die uebrigen Komponenten der Basislinie hinzu
	 * @param baselineComp
	 */
	public void addAssociatedBaselineComponent(GNSSBaseline baseline) {
		if (this.getId() == baseline.getId() && !this.baselineComponents.containsKey(baseline.getComponent()) && !this.baselineComponents.containsValue(baseline))
			this.baselineComponents.put(baseline.getComponent(), baseline);
	}
	
	/**
	 * Setzt bzw. ueberschreibt den Ma&szig;stab
	 * @param newScale Ma&szlig;stab
	 */
	public void setScale(Scale newScale) {
		this.scale = newScale;
		this.scale.setObservation( this );
	}
	
	/**
	 * Liefert Ma&szig;stab
	 * @return scale Ma&szlig;stab
	 */
	public Scale getScale() {
		return this.scale;
	}
	
	@Override
	public int getColInJacobiMatrixFromScale() {
		return this.scale.getColInJacobiMatrix();
	}
	
	/**
	 * Setzt bzw. ueberschreibt die Drehung
	 * @param r Drehung
	 */
	public void setRotationZ(RotationZ r) {
		this.rz = r;
		this.rz.setObservation( this );
	}
	
	/**
	 * Liefert den Drehwinkel
	 * @param r Winkel
	 */
	public RotationZ getRotationZ() {
		return this.rz;
	}
	
	@Override
	public int getColInJacobiMatrixFromRotationZ() {
		return this.rz.getColInJacobiMatrix();
	}
	
	/**
	 * Speichert die Matrix <code>R = Q<sub>vv</sub>P</code>, die zur Bestimmung der Testgroessen notwendig ist. 
	 * Uebergibt eine Referenz auf alle zugeordneten Beobachtungen (Basislinienteile).
	 * 
	 * @param R
	 */
	public void setBaselineRedundancyMatrix(Matrix R) {
		for (GNSSBaseline baseline : this.baselineComponents.values())
			baseline.setRedundancyMatrix(R);	
	}
	
	protected void setRedundancyMatrix(Matrix R) {
		this.subR = R;
	}
	
	/**
	 * Liefert die Matrix <code>R = Q<sub>vv</sub>P</code>, die zur Bestimmung der Testgroessen notwendig ist. 
	 * 
	 * @return R
	 */
	public Matrix getBaselineRedundancyMatrix() {
		return this.subR;
	}
	
	/**
	 * Liefert die uebrigen Komponenten der Basislinie
	 * @return components
	 */
	public List<Observation> getBaselineComponents() {
		return new ArrayList<Observation>(this.baselineComponents.values());
	}
	
	/**
	 * Liefert die zurm Typ gehoerende Komponente oder null, wenn diese nicht existiert
	 * @return component
	 */
	public GNSSBaseline getBaselineComponent(ComponentType type) {
		return this.baselineComponents.get(type);
	}
	
	/**
	 * Liefert die Dimension der Basislinie
	 * @return dim
	 */
	public abstract int getDimension();
	
	/**
	 * Gibt Auskunft ueber den Anteil (X,Y,Z) der Basislinie
	 * @return omp
	 */
	public abstract ComponentType getComponent();

	@Override
	public double diffDeflectionXs() {
		return 0;
	}
	
	@Override
	public double diffDeflectionYs() {
		return 0;
	}
	
	@Override
	public double diffDeflectionXe() {
		return 0;
	}
	
	@Override
	public double diffDeflectionYe() {
		return 0;
	}
}
