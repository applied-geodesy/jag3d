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

import java.util.Locale;
import java.util.Scanner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class CongruenceAnalysisRow extends GroupRow {
	private ObjectProperty<String> nameInReferenceEpoch = new SimpleObjectProperty<String>(); 
	private ObjectProperty<String> nameInControlEpoch   = new SimpleObjectProperty<String>();

	private ObjectProperty<Double> testStatisticApriori     = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> testStatisticAposteriori = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> pValueApriori            = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> pValueAposteriori        = new SimpleObjectProperty<Double>();
	
	private BooleanProperty significant = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty enable      = new SimpleBooleanProperty(Boolean.TRUE);
	
	private ObjectProperty<Double> xAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> yAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> zAposteriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> sigmaXaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaZaposteriori = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceA = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceB = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceC = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceAlpha = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceBeta = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceGamma = new SimpleObjectProperty<Double>();

	public final ObjectProperty<String> nameInReferenceEpochProperty() {
		return this.nameInReferenceEpoch;
	}

	public final String getNameInReferenceEpoch() {
		return this.nameInReferenceEpochProperty().get();
	}

	public final void setNameInReferenceEpoch(final String nameInReferenceEpoch) {
		this.nameInReferenceEpochProperty().set(nameInReferenceEpoch);
	}

	public final ObjectProperty<String> nameInControlEpochProperty() {
		return this.nameInControlEpoch;
	}

	public final String getNameInControlEpoch() {
		return this.nameInControlEpochProperty().get();
	}

	public final void setNameInControlEpoch(final String nameInControlEpoch) {
		this.nameInControlEpochProperty().set(nameInControlEpoch);
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

	public final BooleanProperty significantProperty() {
		return this.significant;
	}

	public final boolean isSignificant() {
		return this.significantProperty().get();
	}

	public final void setSignificant(final boolean significant) {
		this.significantProperty().set(significant);
	}

	public final BooleanProperty enableProperty() {
		return this.enable;
	}

	public final boolean isEnable() {
		return this.enableProperty().get();
	}

	public final void setEnable(final boolean enable) {
		this.enableProperty().set(enable);
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

	public final ObjectProperty<Double> zAposterioriProperty() {
		return this.zAposteriori;
	}

	public final Double getZAposteriori() {
		return this.zAposterioriProperty().get();
	}

	public final void setZAposteriori(final Double zAposteriori) {
		this.zAposterioriProperty().set(zAposteriori);
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

	public final ObjectProperty<Double> sigmaZaposterioriProperty() {
		return this.sigmaZaposteriori;
	}

	public final Double getSigmaZaposteriori() {
		return this.sigmaZaposterioriProperty().get();
	}

	public final void setSigmaZaposteriori(final Double sigmaZaposteriori) {
		this.sigmaZaposterioriProperty().set(sigmaZaposteriori);
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

	public final void setMinimalDetectableBiasY(final Double minimalDetectableBiasY) {
		this.minimalDetectableBiasYProperty().set(minimalDetectableBiasY);
	}

	public final ObjectProperty<Double> minimalDetectableBiasZProperty() {
		return this.minimalDetectableBiasZ;
	}

	public final Double getMinimalDetectableBiasZ() {
		return this.minimalDetectableBiasZProperty().get();
	}

	public final void setMinimalDetectableBiasZ(final Double minimalDetectableBiasZ) {
		this.minimalDetectableBiasZProperty().set(minimalDetectableBiasZ);
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

	public final ObjectProperty<Double> grossErrorZProperty() {
		return this.grossErrorZ;
	}

	public final Double getGrossErrorZ() {
		return this.grossErrorZProperty().get();
	}

	public final void setGrossErrorZ(final Double grossErrorZ) {
		this.grossErrorZProperty().set(grossErrorZ);
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

	public final ObjectProperty<Double> confidenceBProperty() {
		return this.confidenceB;
	}

	public final Double getConfidenceB() {
		return this.confidenceBProperty().get();
	}

	public final void setConfidenceB(final Double confidenceB) {
		this.confidenceBProperty().set(confidenceB);
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

	public final ObjectProperty<Double> confidenceAlphaProperty() {
		return this.confidenceAlpha;
	}

	public final Double getConfidenceAlpha() {
		return this.confidenceAlphaProperty().get();
	}

	public final void setConfidenceAlpha(final Double confidenceAlpha) {
		this.confidenceAlphaProperty().set(confidenceAlpha);
	}

	public final ObjectProperty<Double> confidenceBetaProperty() {
		return this.confidenceBeta;
	}

	public final Double getConfidenceBeta() {
		return this.confidenceBetaProperty().get();
	}

	public final void setConfidenceBeta(final Double confidenceBeta) {
		this.confidenceBetaProperty().set(confidenceBeta);
	}

	public final ObjectProperty<Double> confidenceGammaProperty() {
		return this.confidenceGamma;
	}

	public final Double getConfidenceGamma() {
		return this.confidenceGammaProperty().get();
	}

	public final void setConfidenceGamma(final Double confidenceGamma) {
		this.confidenceGammaProperty().set(confidenceGamma);
	}
	
	public static CongruenceAnalysisRow cloneRowApriori(CongruenceAnalysisRow row) {
		CongruenceAnalysisRow clone = new CongruenceAnalysisRow();
		
		clone.setId(-1);
		clone.setGroupId(row.getGroupId());
		clone.setEnable(row.isEnable());
		
		clone.setNameInReferenceEpoch(row.getNameInReferenceEpoch());
		clone.setNameInControlEpoch(row.getNameInControlEpoch());

		return clone;
	}
	
	public static CongruenceAnalysisRow scan(String str) {
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String startPointName = new String(), 
					endPointName  = new String(); 
			
			CongruenceAnalysisRow row = new CongruenceAnalysisRow();
			// reference epoch
			if (!scanner.hasNext())
				return null;
			startPointName = scanner.next();

			// control epoch
			if (!scanner.hasNext())
				return null;
			endPointName = scanner.next();	

			if (startPointName.equals(endPointName))
				return null;

			row.setNameInReferenceEpoch(startPointName);
			row.setNameInControlEpoch(endPointName);
			
			return row;
		}
		finally {
			scanner.close();
		}
	}
}