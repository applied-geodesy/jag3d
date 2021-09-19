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

package org.applied_geodesy.jag3d.ui.table.row;

import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class VerticalDeflectionRow extends GroupRow {
	private ObjectProperty<String> name = new SimpleObjectProperty<String>();
	
	/**  Parameter **/	
	private ObjectProperty<Double> xApriori = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> yApriori = new SimpleObjectProperty<Double>(0.0);

	private ObjectProperty<Double> sigmaXapriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYapriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> xAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> yAposteriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> sigmaXaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaposteriori = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> maximumTolerableBiasX = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasX");
	private ObjectProperty<Double> maximumTolerableBiasY = new SimpleObjectProperty<Double>(this, "maximumTolerableBiasY");
	
	private ObjectProperty<Double> residualX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> residualY = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> testStatisticApriori     = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> testStatisticAposteriori = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> pValueApriori            = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> pValueAposteriori        = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceA = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceC = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> omega = new SimpleObjectProperty<Double>();
	
	private BooleanProperty significant = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty enable      = new SimpleBooleanProperty(Boolean.TRUE);
	
	public BooleanProperty enableProperty() {
		return this.enable;
	}

	public Boolean isEnable() {
		return this.enableProperty().get();
	}

	public void setEnable(final Boolean enable) {
		this.enableProperty().set(enable);
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}

	public final ObjectProperty<Double> xAprioriProperty() {
		return this.xApriori;
	}

	public final Double getXApriori() {
		return this.xAprioriProperty().get();
	}
	
	public final void setXApriori(final Double xApriori) {
		this.xAprioriProperty().set(xApriori);
	}

	public final ObjectProperty<Double> yAprioriProperty() {
		return this.yApriori;
	}

	public final Double getYApriori() {
		return this.yAprioriProperty().get();
	}

	public final void setYApriori(final Double yApriori) {
		this.yAprioriProperty().set(yApriori);
	}

	public final ObjectProperty<Double> sigmaXaprioriProperty() {
		return this.sigmaXapriori;
	}

	public final Double getSigmaXapriori() {
		return this.sigmaXaprioriProperty().get();
	}

	public final void setSigmaXapriori(final Double sigmaXapriori) {
		this.sigmaXaprioriProperty().set(sigmaXapriori);
	}

	public final ObjectProperty<Double> sigmaYaprioriProperty() {
		return this.sigmaYapriori;
	}

	public final Double getSigmaYapriori() {
		return this.sigmaYaprioriProperty().get();
	}

	public final void setSigmaYapriori(final Double sigmaYapriori) {
		this.sigmaYaprioriProperty().set(sigmaYapriori);
	}

	public final ObjectProperty<Double> xAposterioriProperty() {
		return this.xAposteriori;
	}
	
	public final Double getXAposteriori() {
		return this.xAposterioriProperty().get();
	}
	
	public final void setXAposteriori(final Double xAposteriori) {
		this.xAposterioriProperty().set(xAposteriori);
	}
	
	public final ObjectProperty<Double> yAposterioriProperty() {
		return this.yAposteriori;
	}
	
	public final Double getYAposteriori() {
		return this.yAposterioriProperty().get();
	}
	
	public final void setYAposteriori(final Double yAposteriori) {
		this.yAposterioriProperty().set(yAposteriori);
	}
	
	public final ObjectProperty<Double> sigmaXaposterioriProperty() {
		return this.sigmaXaposteriori;
	}
	
	public final Double getSigmaXaposteriori() {
		return this.sigmaXaposterioriProperty().get();
	}
	
	public final void setSigmaXaposteriori(final Double sigmaXaposteriori) {
		this.sigmaXaposterioriProperty().set(sigmaXaposteriori);
	}
	
	public final ObjectProperty<Double> sigmaYaposterioriProperty() {
		return this.sigmaYaposteriori;
	}
	
	public final Double getSigmaYaposteriori() {
		return this.sigmaYaposterioriProperty().get();
	}
	
	public final void setSigmaYaposteriori(final Double sigmaYaposteriori) {
		this.sigmaYaposterioriProperty().set(sigmaYaposteriori);
	}
	
	public final ObjectProperty<Double> minimalDetectableBiasXProperty() {
		return this.minimalDetectableBiasX;
	}
	
	public final Double getMinimalDetectableBiasX() {
		return this.minimalDetectableBiasXProperty().get();
	}
	
	public final void setMinimalDetectableBiasX(final Double minimalDetectableBiasX) {
		this.minimalDetectableBiasXProperty().set(minimalDetectableBiasX);
	}
	
	public final ObjectProperty<Double> minimalDetectableBiasYProperty() {
		return this.minimalDetectableBiasY;
	}
	
	public final Double getMinimalDetectableBiasY() {
		return this.minimalDetectableBiasYProperty().get();
	}
	
	public ObjectProperty<Double> maximumTolerableBiasXProperty() {
		return this.maximumTolerableBiasX;
	}

	public Double getMaximumTolerableBiasX() {
		return this.maximumTolerableBiasXProperty().get();
	}

	public void setMaximumTolerableBiasX(final Double maximumTolerableBiasX) {
		this.maximumTolerableBiasXProperty().set(maximumTolerableBiasX);
	}
	
	public ObjectProperty<Double> maximumTolerableBiasYProperty() {
		return this.maximumTolerableBiasY;
	}

	public Double getMaximumTolerableBiasY() {
		return this.maximumTolerableBiasYProperty().get();
	}

	public void setMaximumTolerableBiasY(final Double maximumTolerableBiasY) {
		this.maximumTolerableBiasYProperty().set(maximumTolerableBiasY);
	}
	
	public final void setMinimalDetectableBiasY(final Double minimalDetectableBiasY) {
		this.minimalDetectableBiasYProperty().set(minimalDetectableBiasY);
	}
	
	public final ObjectProperty<Double> residualXProperty() {
		return this.residualX;
	}

	public final Double getResidualX() {
		return this.residualXProperty().get();
	}

	public final void setResidualX(final Double residualX) {
		this.residualXProperty().set(residualX);
	}

	public final ObjectProperty<Double> residualYProperty() {
		return this.residualY;
	}
	
	public final Double getResidualY() {
		return this.residualYProperty().get();
	}

	public final void setResidualY(final Double residualY) {
		this.residualYProperty().set(residualY);
	}
	
	public final ObjectProperty<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public final Double getRedundancyX() {
		return this.redundancyXProperty().get();
	}
	
	public final void setRedundancyX(final Double redundancyX) {
		this.redundancyXProperty().set(redundancyX);
	}
	
	public final ObjectProperty<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public final Double getRedundancyY() {
		return this.redundancyYProperty().get();
	}
	
	public final void setRedundancyY(final Double redundancyY) {
		this.redundancyYProperty().set(redundancyY);
	}
	
	public final ObjectProperty<Double> grossErrorXProperty() {
		return this.grossErrorX;
	}
	
	public final Double getGrossErrorX() {
		return this.grossErrorXProperty().get();
	}
	
	public final void setGrossErrorX(final Double grossErrorX) {
		this.grossErrorXProperty().set(grossErrorX);
	}
	
	public final ObjectProperty<Double> grossErrorYProperty() {
		return this.grossErrorY;
	}
	
	public final Double getGrossErrorY() {
		return this.grossErrorYProperty().get();
	}
	
	public final void setGrossErrorY(final Double grossErrorY) {
		this.grossErrorYProperty().set(grossErrorY);
	}
	
	public final ObjectProperty<Double> testStatisticAprioriProperty() {
		return this.testStatisticApriori;
	}
	
	public final Double getTestStatisticApriori() {
		return this.testStatisticAprioriProperty().get();
	}
	
	public final void setTestStatisticApriori(final Double testStatisticApriori) {
		this.testStatisticAprioriProperty().set(testStatisticApriori);
	}
	
	public final ObjectProperty<Double> testStatisticAposterioriProperty() {
		return this.testStatisticAposteriori;
	}
	
	public final Double getTestStatisticAposteriori() {
		return this.testStatisticAposterioriProperty().get();
	}
	
	public final void setTestStatisticAposteriori(final Double testStatisticAposteriori) {
		this.testStatisticAposterioriProperty().set(testStatisticAposteriori);
	}
	
	public final ObjectProperty<Double> pValueAprioriProperty() {
		return this.pValueApriori;
	}
	
	public final Double getPValueApriori() {
		return this.pValueAprioriProperty().get();
	}
	
	public final void setPValueApriori(final Double pValueApriori) {
		this.pValueAprioriProperty().set(pValueApriori);
	}
	
	public final ObjectProperty<Double> pValueAposterioriProperty() {
		return this.pValueAposteriori;
	}
	
	public final Double getPValueAposteriori() {
		return this.pValueAposterioriProperty().get();
	}
	
	public final void setPValueAposteriori(final Double pValueAposteriori) {
		this.pValueAposterioriProperty().set(pValueAposteriori);
	}
	
	public final ObjectProperty<Double> confidenceAProperty() {
		return this.confidenceA;
	}
	
	public final Double getConfidenceA() {
		return this.confidenceAProperty().get();
	}
	
	public final void setConfidenceA(final Double confidenceA) {
		this.confidenceAProperty().set(confidenceA);
	}
	
	public final ObjectProperty<Double> confidenceCProperty() {
		return this.confidenceC;
	}
	
	public final Double getConfidenceC() {
		return this.confidenceCProperty().get();
	}
	
	public final void setConfidenceC(final Double confidenceC) {
		this.confidenceCProperty().set(confidenceC);
	}
	
	public final ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public final Double getOmega() {
		return this.omegaProperty().get();
	}
	
	public final void setOmega(final Double omega) {
		this.omegaProperty().set(omega);
	}
	
	public final BooleanProperty significantProperty() {
		return this.significant;
	}

	public final boolean isSignificant() {
		return this.significantProperty().get();
	}

	public final void setSignificant(final boolean significant) {
		this.significantProperty().set(significant);
	}
	
	
	public static VerticalDeflectionRow cloneRowApriori(VerticalDeflectionRow row) {
		VerticalDeflectionRow clone = new VerticalDeflectionRow();
		
		clone.setId(-1);
		clone.setGroupId(row.getGroupId());
		clone.setEnable(row.isEnable());
		
		clone.setName(row.getName());
		
		clone.setXApriori(row.getXApriori());
		clone.setYApriori(row.getYApriori());
		
		clone.setSigmaXapriori(row.getSigmaXapriori());
		clone.setSigmaYapriori(row.getSigmaYapriori());

		return clone;
	}
	
	public static VerticalDeflectionRow scan(String str) {
		FormatterOptions options = FormatterOptions.getInstance();

		try {
			String data[] = str.trim().split("[\\s;]+");
			
			if (data.length < 3)
				return null;
			
			VerticalDeflectionRow row = new VerticalDeflectionRow();
			
			// Name of point
			String name = data[0];
			double x = 0.0, y = 0.0;
			double sigmaY = 0.0, sigmaX = 0.0;
			
			row.setName(name);
			
			// Y 		
			y = options.convertAngleToModel(Double.parseDouble(data[1].replace(',', '.')));

			// X 		
			x = options.convertAngleToModel(Double.parseDouble(data[2].replace(',', '.')));

			// sigma X (or sigmaX/Y)
			if (data.length < 4) {
				row.setYApriori(y);
				row.setXApriori(x);
				return row;
			}
			
			sigmaY = sigmaX = options.convertAngleToModel(Double.parseDouble(data[3].replace(',', '.')));

			// sigma Y
			if (data.length < 5) {
				row.setYApriori(y);
				row.setXApriori(x);
				row.setSigmaYapriori(sigmaY);
				row.setSigmaXapriori(sigmaX);
				return row;
			}
			
			sigmaX = options.convertAngleToModel(Double.parseDouble(data[4].replace(',', '.')));
			
			row.setYApriori(y);
			row.setXApriori(x);
			row.setSigmaYapriori(sigmaY);
			row.setSigmaXapriori(sigmaX);
			
			return row;
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}
}
