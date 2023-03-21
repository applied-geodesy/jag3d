/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.adjustment.transformation.parameter;

import org.applied_geodesy.adjustment.transformation.TestStatistic;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

public class UnknownParameter extends Parameter {
	/** Value: -1 == not set, Integer.MIN_VALUE == fixed, else column in normal equation system **/
	private ObjectProperty<ProcessingType> processingType       = new SimpleObjectProperty<ProcessingType>(ProcessingType.ADJUSTMENT);
	private ReadOnlyObjectProperty<TestStatistic> testStatistic = new ReadOnlyObjectWrapper<TestStatistic>(this, "testStatistic", new TestStatistic());
	
	private ObjectProperty<Integer> column      = new SimpleObjectProperty<Integer>(this, "column", -1);
	private ObjectProperty<Double> value        = new SimpleObjectProperty<Double>(this, "value", 0.0);
	private ObjectProperty<Double> uncertainty  = new SimpleObjectProperty<Double>(this, "uncertainty", 0.0);
	
	private ReadOnlyObjectProperty<Double> expectedValue = null;

	private ReadOnlyObjectWrapper<Double> testStatisticApriori     = new ReadOnlyObjectWrapper<Double>(this, "testStatisticApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> testStatisticAposteriori = new ReadOnlyObjectWrapper<Double>(this, "testStatisticAposteriori", 0.0);
	
	private ReadOnlyObjectWrapper<Double> pValueApriori     = new ReadOnlyObjectWrapper<Double>(this, "pValueApriori", 0.0);
	private ReadOnlyObjectWrapper<Double> pValueAposteriori = new ReadOnlyObjectWrapper<Double>(this, "pValueAposteriori", 0.0);
	
	private ObjectBinding<Boolean> significant;

	private ObjectProperty<Double> fisherQuantileApriori     = new SimpleObjectProperty<Double>(this, "fisherQuantileApriori", Double.MAX_VALUE);
	private ObjectProperty<Double> fisherQuantileAposteriori = new SimpleObjectProperty<Double>(this, "fisherQuantileAposteriori", Double.MAX_VALUE);

	
	private ReadOnlyObjectProperty<Boolean> indispensable;
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable) {
		this(parameterType, indispensable, 0, Boolean.TRUE);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, boolean visible) {
		this(parameterType, indispensable, 0, visible);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value) {
		this(parameterType, indispensable, value, Boolean.TRUE);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, boolean visible) {
		this(parameterType, indispensable, value, value, visible, -1, ProcessingType.ADJUSTMENT);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, double expectedValue, boolean visible) {
		this(parameterType, indispensable, value, expectedValue, visible, -1, ProcessingType.ADJUSTMENT);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, boolean visible, ProcessingType processingType) {
		this(parameterType, indispensable, value, value, visible, -1, processingType);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, double expectedValue, boolean visible, ProcessingType processingType) {
		this(parameterType, indispensable, value, expectedValue, visible, -1, processingType);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, double expectedValue, boolean visible, int column, ProcessingType processingType) {
		super(parameterType);
		this.setValue0(value);
		this.setValue(value);
		this.setColumn(column);
		this.setProcessingType(processingType);
		this.setVisible(visible);
		
		this.expectedValue = new ReadOnlyObjectWrapper<Double>(this, "expectedValue",  expectedValue);
		this.indispensable = new ReadOnlyObjectWrapper<Boolean>(this, "indispensable", indispensable);
		
		this.testStatistic.get().setApplyBiasCorrection(false);
		this.testStatisticApriori.bind(this.testStatistic.get().testStatisticAprioriProperty());
        this.testStatisticAposteriori.bind(this.testStatistic.get().testStatisticAposterioriProperty());
        
        this.pValueApriori.bind(this.testStatistic.get().pValueAprioriProperty());
        this.pValueAposteriori.bind(this.testStatistic.get().pValueAposterioriProperty());
		
		this.significant = new ObjectBinding<Boolean>() {
        	{
                super.bind(fisherQuantileApriori, testStatisticApriori, fisherQuantileAposteriori, testStatisticAposteriori);
            }
        	
        	@Override
            protected Boolean computeValue() {
        		boolean significant = testStatisticApriori.get() > fisherQuantileApriori.get();

        		if (testStatistic.get().varianceComponentProperty().get().isApplyAposterioriVarianceOfUnitWeight())
        			return significant || testStatisticAposteriori.get() > fisherQuantileAposteriori.get();

        		return significant;
        	}
        };
	}
		
	public ReadOnlyObjectProperty<Boolean> indispensableProperty() {
		return this.indispensable;
	}
	
	public boolean isIndispensable() {
		return this.indispensable.get();
	}
	
	public void setColumn(int column) {
		this.column.set(column);
	}
	
	public int getColumn() {
		return this.column.get();
	}
	
	public ObjectProperty<Integer> columnProperty() {
		return this.column;
	}
	
	public ProcessingType getProcessingType() {
		return this.processingType.get();
	}
	
	public void setProcessingType(ProcessingType processingType) {
		this.processingType.set(processingType);
		if (processingType != ProcessingType.ADJUSTMENT)
			this.column.set(-1);
	}
	
	public ObjectProperty<ProcessingType> processingTypeProperty() {
		return this.processingType;
	}
	
	public void setValue(double value) {
		this.value.set(value);
	}
	
	public double getValue() {
		return this.value.get();
	}
	
	public ObjectProperty<Double> valueProperty() {
		return this.value;
	}
	
	public double getExpectedValue() {
		return this.expectedValue.get();
	}
	
	public ObservableValue<Double> expectedValueProperty() {
		return this.expectedValue;
	}
	
	public void setUncertainty(double uncertainty) {
		this.uncertainty.set(uncertainty);
	}
	
	public double getUncertainty() {
		return this.uncertainty.get();
	}
	
	public ObjectProperty<Double> uncertaintyProperty() {
		return this.uncertainty;
	}
	
	public ObjectBinding<Boolean> significantProperty() {
		return this.significant;
	}
	
	public boolean isSignificant() {
		return this.significant.get();
	}
	
	public TestStatistic getTestStatistic() {
		return this.testStatistic.get();
	}

	public ReadOnlyObjectProperty<TestStatistic> testStatisticProperty() {
		return this.testStatistic;
	}
	
	public void setFisherQuantileApriori(double fisherQuantileApriori) {
		this.fisherQuantileApriori.set(fisherQuantileApriori);
	}
	
	public double getFisherQuantileApriori() {
		return this.fisherQuantileApriori.get();
	}
	
	public ObjectProperty<Double> fisherQuantileAprioriProperty() {
		return this.fisherQuantileApriori;
	}
	
	public void setFisherQuantileAposteriori(double fisherQuantileAposteriori) {
		this.fisherQuantileAposteriori.set(fisherQuantileAposteriori);
	}
	
	public double getFisherQuantileAposteriori() {
		return this.fisherQuantileAposteriori.get();
	}
	
	public ObjectProperty<Double> fisherQuantileAposterioriProperty() {
		return this.fisherQuantileAposteriori;
	}

	@Override
	public String toString() {
		return "UnknownParameter [Name=" + getName() + ", ParameterType=" + getParameterType()
			+ ", Value0=" + getValue0() + ", Column=" + getColumn()
			+ ", ProcessingType="	+ getProcessingType() + ", Value=" + getValue() + "]";
	}	
}
