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

import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PointRow extends GroupRow {
	private ObjectProperty<String> name = new SimpleObjectProperty<String>();
	private ObjectProperty<String> code = new SimpleObjectProperty<String>("0"); 
	
	private ObjectProperty<Double> xApriori = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> yApriori = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> zApriori = new SimpleObjectProperty<Double>(0.0);
	
	private ObjectProperty<Double> sigmaXapriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYapriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaZapriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> xAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> yAposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> zAposteriori = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> sigmaXaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaposteriori = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaZaposteriori = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> residualX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> residualY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> residualZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> omega                    = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> testStatisticApriori     = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> testStatisticAposteriori = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> pValueApriori            = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> pValueAposteriori        = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceA = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceB = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceC = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceAlpha = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceBeta = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceGamma = new SimpleObjectProperty<Double>();
	
	private BooleanProperty significant = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty enable      = new SimpleBooleanProperty(Boolean.TRUE);
	
	private ObjectProperty<Double> firstPrincipalComponentX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> firstPrincipalComponentY = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> firstPrincipalComponentZ = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> influenceOnPointPositionX = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> influenceOnPointPositionY = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> influenceOnPointPositionZ = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> influenceOnNetworkDistortion = new SimpleObjectProperty<Double>();
	
	/** Deflection Parameter **/	
	private ObjectProperty<Double> xAprioriDeflection = new SimpleObjectProperty<Double>(0.0);
	private ObjectProperty<Double> yAprioriDeflection = new SimpleObjectProperty<Double>(0.0);

	private ObjectProperty<Double> sigmaXaprioriDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaprioriDeflection = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> xAposterioriDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> yAposterioriDeflection = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> sigmaXaposterioriDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> sigmaYaposterioriDeflection = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> minimalDetectableBiasXDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> minimalDetectableBiasYDeflection = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> residualXDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> residualYDeflection = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> redundancyXDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> redundancyYDeflection = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> grossErrorXDeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> grossErrorYDeflection = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> testStatisticAprioriDeflection     = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> testStatisticAposterioriDeflection = new SimpleObjectProperty<Double>(); 
	
	private ObjectProperty<Double> pValueAprioriDeflection            = new SimpleObjectProperty<Double>(); 
	private ObjectProperty<Double> pValueAposterioriDeflection        = new SimpleObjectProperty<Double>();
	
	private ObjectProperty<Double> confidenceADeflection = new SimpleObjectProperty<Double>();
	private ObjectProperty<Double> confidenceBDeflection = new SimpleObjectProperty<Double>();

	private ObjectProperty<Double> omegaDeflection = new SimpleObjectProperty<Double>();
	private BooleanProperty significantDeflection  = new SimpleBooleanProperty(Boolean.FALSE);
	
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
	
	public ObjectProperty<String> codeProperty() {
		return this.code;
	}
	
	public String getCode() {
		return this.codeProperty().get();
	}
	
	public void setCode(final String code) {
		this.codeProperty().set(code);
	}
	
	public ObjectProperty<Double> xAprioriProperty() {
		return this.xApriori;
	}
	
	public Double getXApriori() {
		return this.xAprioriProperty().get();
	}
	
	public void setXApriori(final Double xApriori) {
		this.xAprioriProperty().set(xApriori);
	}
	
	public ObjectProperty<Double> yAprioriProperty() {
		return this.yApriori;
	}
	
	public Double getYApriori() {
		return this.yAprioriProperty().get();
	}
	
	public void setYApriori(final Double yApriori) {
		this.yAprioriProperty().set(yApriori);
	}
	
	public ObjectProperty<Double> zAprioriProperty() {
		return this.zApriori;
	}
	
	public Double getZApriori() {
		return this.zAprioriProperty().get();
	}
	
	public void setZApriori(final Double zApriori) {
		this.zAprioriProperty().set(zApriori);
	}
	
	public ObjectProperty<Double> sigmaXaprioriProperty() {
		return this.sigmaXapriori;
	}
	
	public Double getSigmaXapriori() {
		return this.sigmaXaprioriProperty().get();
	}

	public void setSigmaXapriori(final Double sigmaXapriori) {
		this.sigmaXaprioriProperty().set(sigmaXapriori);
	}
	
	public ObjectProperty<Double> sigmaYaprioriProperty() {
		return this.sigmaYapriori;
	}
	
	public Double getSigmaYapriori() {
		return this.sigmaYaprioriProperty().get();
	}

	public void setSigmaYapriori(final Double sigmaYapriori) {
		this.sigmaYaprioriProperty().set(sigmaYapriori);
	}
	
	public ObjectProperty<Double> sigmaZaprioriProperty() {
		return this.sigmaZapriori;
	}
	
	public Double getSigmaZapriori() {
		return this.sigmaZaprioriProperty().get();
	}
	
	public void setSigmaZapriori(final Double sigmaZapriori) {
		this.sigmaZaprioriProperty().set(sigmaZapriori);
	}
	
	public ObjectProperty<Double> xAposterioriProperty() {
		return this.xAposteriori;
	}
	
	public Double getXAposteriori() {
		return this.xAposterioriProperty().get();
	}
	
	public void setXAposteriori(final Double xAposteriori) {
		this.xAposterioriProperty().set(xAposteriori);
	}
	
	public ObjectProperty<Double> yAposterioriProperty() {
		return this.yAposteriori;
	}
	
	public Double getYAposteriori() {
		return this.yAposterioriProperty().get();
	}
	
	public void setYAposteriori(final Double yAposteriori) {
		this.yAposterioriProperty().set(yAposteriori);
	}
	
	public ObjectProperty<Double> zAposterioriProperty() {
		return this.zAposteriori;
	}
	
	public Double getZAposteriori() {
		return this.zAposterioriProperty().get();
	}
	
	public void setZAposteriori(final Double zAposteriori) {
		this.zAposterioriProperty().set(zAposteriori);
	}
	
	public ObjectProperty<Double> sigmaXaposterioriProperty() {
		return this.sigmaXaposteriori;
	}
	
	public Double getSigmaXaposteriori() {
		return this.sigmaXaposterioriProperty().get();
	}
	
	public void setSigmaXaposteriori(final Double sigmaXaposteriori) {
		this.sigmaXaposterioriProperty().set(sigmaXaposteriori);
	}
	
	public ObjectProperty<Double> sigmaYaposterioriProperty() {
		return this.sigmaYaposteriori;
	}
	
	public Double getSigmaYaposteriori() {
		return this.sigmaYaposterioriProperty().get();
	}
	
	public void setSigmaYaposteriori(final Double sigmaYaposteriori) {
		this.sigmaYaposterioriProperty().set(sigmaYaposteriori);
	}
	
	public ObjectProperty<Double> sigmaZaposterioriProperty() {
		return this.sigmaZaposteriori;
	}
	
	public Double getSigmaZaposteriori() {
		return this.sigmaZaposterioriProperty().get();
	}
	
	public void setSigmaZaposteriori(final Double sigmaZaposteriori) {
		this.sigmaZaposterioriProperty().set(sigmaZaposteriori);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasXProperty() {
		return this.minimalDetectableBiasX;
	}
	
	public Double getMinimalDetectableBiasX() {
		return this.minimalDetectableBiasXProperty().get();
	}
	
	public void setMinimalDetectableBiasX(final Double minimalDetectableBiasX) {
		this.minimalDetectableBiasXProperty().set(minimalDetectableBiasX);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasYProperty() {
		return this.minimalDetectableBiasY;
	}
	
	public Double getMinimalDetectableBiasY() {
		return this.minimalDetectableBiasYProperty().get();
	}
	
	public void setMinimalDetectableBiasY(final Double minimalDetectableBiasY) {
		this.minimalDetectableBiasYProperty().set(minimalDetectableBiasY);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasZProperty() {
		return this.minimalDetectableBiasZ;
	}
	
	public Double getMinimalDetectableBiasZ() {
		return this.minimalDetectableBiasZProperty().get();
	}
	
	public void setMinimalDetectableBiasZ(final Double minimalDetectableBiasZ) {
		this.minimalDetectableBiasZProperty().set(minimalDetectableBiasZ);
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
	
	public final ObjectProperty<Double> residualZProperty() {
		return this.residualZ;
	}
	
	public final Double getResidualZ() {
		return this.residualZProperty().get();
	}
	
	public final void setResidualZ(final Double residualZ) {
		this.residualZProperty().set(residualZ);
	}
	
	public ObjectProperty<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public Double getRedundancyX() {
		return this.redundancyXProperty().get();
	}
	
	public void setRedundancyX(final Double redundancyX) {
		this.redundancyXProperty().set(redundancyX);
	}
	
	public ObjectProperty<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public Double getRedundancyY() {
		return this.redundancyYProperty().get();
	}
	
	public void setRedundancyY(final Double redundancyY) {
		this.redundancyYProperty().set(redundancyY);
	}
	
	public ObjectProperty<Double> redundancyZProperty() {
		return this.redundancyZ;
	}
	
	public Double getRedundancyZ() {
		return this.redundancyZProperty().get();
	}
	
	public void setRedundancyZ(final Double redundancyZ) {
		this.redundancyZProperty().set(redundancyZ);
	}
	
	public ObjectProperty<Double> grossErrorXProperty() {
		return this.grossErrorX;
	}
	
	public Double getGrossErrorX() {
		return this.grossErrorXProperty().get();
	}
	
	public void setGrossErrorX(final Double nablaX) {
		this.grossErrorXProperty().set(nablaX);
	}
	
	public ObjectProperty<Double> grossErrorYProperty() {
		return this.grossErrorY;
	}
	
	public Double getGrossErrorY() {
		return this.grossErrorYProperty().get();
	}
	
	public void setGrossErrorY(final Double nablaY) {
		this.grossErrorYProperty().set(nablaY);
	}
	
	public ObjectProperty<Double> grossErrorZProperty() {
		return this.grossErrorZ;
	}
	
	public Double getGrossErrorZ() {
		return this.grossErrorZProperty().get();
	}
	
	public void setGrossErrorZ(final Double nablaZ) {
		this.grossErrorZProperty().set(nablaZ);
	}
	
	public ObjectProperty<Double> omegaProperty() {
		return this.omega;
	}
	
	public Double getOmega() {
		return this.omegaProperty().get();
	}
	
	public void setOmega(final Double omega) {
		this.omegaProperty().set(omega);
	}
	
	public ObjectProperty<Double> testStatisticAprioriProperty() {
		return this.testStatisticApriori;
	}
	
	public Double getTestStatisticApriori() {
		return this.testStatisticAprioriProperty().get();
	}
	
	public void setTestStatisticApriori(final Double testStatisticApriori) {
		this.testStatisticAprioriProperty().set(testStatisticApriori);
	}
	
	public ObjectProperty<Double> testStatisticAposterioriProperty() {
		return this.testStatisticAposteriori;
	}
	
	public Double getTestStatisticAposteriori() {
		return this.testStatisticAposterioriProperty().get();
	}
	
	public void setTestStatisticAposteriori(final Double testStatisticAposteriori) {
		this.testStatisticAposterioriProperty().set(testStatisticAposteriori);
	}
	
	public ObjectProperty<Double> pValueAprioriProperty() {
		return this.pValueApriori;
	}
	
	public Double getPValueApriori() {
		return this.pValueAprioriProperty().get();
	}
	
	public void setPValueApriori(final Double pValueApriori) {
		this.pValueAprioriProperty().set(pValueApriori);
	}
	
	public ObjectProperty<Double> pValueAposterioriProperty() {
		return this.pValueAposteriori;
	}
	
	public Double getPValueAposteriori() {
		return this.pValueAposterioriProperty().get();
	}
	
	public void setPValueAposteriori(final Double pValueAposteriori) {
		this.pValueAposterioriProperty().set(pValueAposteriori);
	}
	
	public ObjectProperty<Double> confidenceAProperty() {
		return this.confidenceA;
	}
	
	public Double getConfidenceA() {
		return this.confidenceAProperty().get();
	}
	
	public void setConfidenceA(final Double confidenceA) {
		this.confidenceAProperty().set(confidenceA);
	}
	
	public ObjectProperty<Double> confidenceBProperty() {
		return this.confidenceB;
	}
	
	public Double getConfidenceB() {
		return this.confidenceBProperty().get();
	}
	
	public void setConfidenceB(final Double confidenceB) {
		this.confidenceBProperty().set(confidenceB);
	}
	
	public ObjectProperty<Double> confidenceCProperty() {
		return this.confidenceC;
	}
	
	public Double getConfidenceC() {
		return this.confidenceCProperty().get();
	}
	
	public void setConfidenceC(final Double confidenceC) {
		this.confidenceCProperty().set(confidenceC);
	}
	
	public ObjectProperty<Double> confidenceAlphaProperty() {
		return this.confidenceAlpha;
	}
	
	public Double getConfidenceAlpha() {
		return this.confidenceAlphaProperty().get();
	}
	
	public void setConfidenceAlpha(final Double confidenceAlpha) {
		this.confidenceAlphaProperty().set(confidenceAlpha);
	}
	
	public ObjectProperty<Double> confidenceBetaProperty() {
		return this.confidenceBeta;
	}
	
	public Double getConfidenceBeta() {
		return this.confidenceBetaProperty().get();
	}
	
	public void setConfidenceBeta(final Double confidenceBeta) {
		this.confidenceBetaProperty().set(confidenceBeta);
	}
	
	public ObjectProperty<Double> confidenceGammaProperty() {
		return this.confidenceGamma;
	}
	
	public Double getConfidenceGamma() {
		return this.confidenceGammaProperty().get();
	}
	
	public void setConfidenceGamma(final Double confidenceGamma) {
		this.confidenceGammaProperty().set(confidenceGamma);
	}
	
	public ObjectProperty<Double> firstPrincipalComponentXProperty() {
		return this.firstPrincipalComponentX;
	}
	
	public Double getFirstPrincipalComponentX() {
		return this.firstPrincipalComponentXProperty().get();
	}
	
	public void setFirstPrincipalComponentX(final Double firstPrincipalComponentX) {
		this.firstPrincipalComponentXProperty().set(firstPrincipalComponentX);
	}

	public ObjectProperty<Double> firstPrincipalComponentYProperty() {
		return this.firstPrincipalComponentY;
	}
	
	public Double getFirstPrincipalComponentY() {
		return this.firstPrincipalComponentYProperty().get();
	}
	
	public void setFirstPrincipalComponentY(final Double firstPrincipalComponentY) {
		this.firstPrincipalComponentYProperty().set(firstPrincipalComponentY);
	}
	
	public ObjectProperty<Double> firstPrincipalComponentZProperty() {
		return this.firstPrincipalComponentZ;
	}
	
	public Double getFirstPrincipalComponentZ() {
		return this.firstPrincipalComponentZProperty().get();
	}
	
	public void setFirstPrincipalComponentZ(final Double firstPrincipalComponentZ) {
		this.firstPrincipalComponentZProperty().set(firstPrincipalComponentZ);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionXProperty() {
		return this.influenceOnPointPositionX;
	}
	
	public Double getInfluenceOnPointPositionX() {
		return this.influenceOnPointPositionXProperty().get();
	}
	
	public void setInfluenceOnPointPositionX(final Double influenceOnPointPositionX) {
		this.influenceOnPointPositionXProperty().set(influenceOnPointPositionX);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionYProperty() {
		return this.influenceOnPointPositionY;
	}
	
	public Double getInfluenceOnPointPositionY() {
		return this.influenceOnPointPositionYProperty().get();
	}
	
	public void setInfluenceOnPointPositionY(final Double influenceOnPointPositionY) {
		this.influenceOnPointPositionYProperty().set(influenceOnPointPositionY);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionZProperty() {
		return this.influenceOnPointPositionZ;
	}
	
	public Double getInfluenceOnPointPositionZ() {
		return this.influenceOnPointPositionZProperty().get();
	}
	
	public void setInfluenceOnPointPositionZ(final Double influenceOnPointPositionZ) {
		this.influenceOnPointPositionZProperty().set(influenceOnPointPositionZ);
	}
	
	public ObjectProperty<Double> influenceOnNetworkDistortionProperty() {
		return this.influenceOnNetworkDistortion;
	}
	
	public Double getInfluenceOnNetworkDistortion() {
		return this.influenceOnNetworkDistortionProperty().get();
	}
	
	public void setInfluenceOnNetworkDistortion(final Double influenceOnNetworkDistortion) {
		this.influenceOnNetworkDistortionProperty().set(influenceOnNetworkDistortion);
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
	
	/** Deflections **/
	public final ObjectProperty<Double> xAprioriDeflectionProperty() {
		return this.xAprioriDeflection;
	}

	public final Double getXAprioriDeflection() {
		return this.xAprioriDeflectionProperty().get();
	}
	
	public final void setXAprioriDeflection(final Double xAprioriDeflection) {
		this.xAprioriDeflectionProperty().set(xAprioriDeflection);
	}

	public final ObjectProperty<Double> yAprioriDeflectionProperty() {
		return this.yAprioriDeflection;
	}

	public final Double getYAprioriDeflection() {
		return this.yAprioriDeflectionProperty().get();
	}

	public final void setYAprioriDeflection(final Double yAprioriDeflection) {
		this.yAprioriDeflectionProperty().set(yAprioriDeflection);
	}

	public final ObjectProperty<Double> sigmaXaprioriDeflectionProperty() {
		return this.sigmaXaprioriDeflection;
	}

	public final Double getSigmaXaprioriDeflection() {
		return this.sigmaXaprioriDeflectionProperty().get();
	}

	public final void setSigmaXaprioriDeflection(final Double sigmaXaprioriDeflection) {
		this.sigmaXaprioriDeflectionProperty().set(sigmaXaprioriDeflection);
	}

	public final ObjectProperty<Double> sigmaYaprioriDeflectionProperty() {
		return this.sigmaYaprioriDeflection;
	}

	public final Double getSigmaYaprioriDeflection() {
		return this.sigmaYaprioriDeflectionProperty().get();
	}

	public final void setSigmaYaprioriDeflection(final Double sigmaYaprioriDeflection) {
		this.sigmaYaprioriDeflectionProperty().set(sigmaYaprioriDeflection);
	}

	public final ObjectProperty<Double> xAposterioriDeflectionProperty() {
		return this.xAposterioriDeflection;
	}
	
	public final Double getXAposterioriDeflection() {
		return this.xAposterioriDeflectionProperty().get();
	}
	
	public final void setXAposterioriDeflection(final Double xAposterioriDeflection) {
		this.xAposterioriDeflectionProperty().set(xAposterioriDeflection);
	}
	
	public final ObjectProperty<Double> yAposterioriDeflectionProperty() {
		return this.yAposterioriDeflection;
	}
	
	public final Double getYAposterioriDeflection() {
		return this.yAposterioriDeflectionProperty().get();
	}
	
	public final void setYAposterioriDeflection(final Double yAposterioriDeflection) {
		this.yAposterioriDeflectionProperty().set(yAposterioriDeflection);
	}
	
	public final ObjectProperty<Double> sigmaXaposterioriDeflectionProperty() {
		return this.sigmaXaposterioriDeflection;
	}
	
	public final Double getSigmaXaposterioriDeflection() {
		return this.sigmaXaposterioriDeflectionProperty().get();
	}
	
	public final void setSigmaXaposterioriDeflection(final Double sigmaXaposterioriDeflection) {
		this.sigmaXaposterioriDeflectionProperty().set(sigmaXaposterioriDeflection);
	}
	
	public final ObjectProperty<Double> sigmaYaposterioriDeflectionProperty() {
		return this.sigmaYaposterioriDeflection;
	}
	
	public final Double getSigmaYaposterioriDeflection() {
		return this.sigmaYaposterioriDeflectionProperty().get();
	}
	
	public final void setSigmaYaposterioriDeflection(final Double sigmaYaposterioriDeflection) {
		this.sigmaYaposterioriDeflectionProperty().set(sigmaYaposterioriDeflection);
	}
	
	public final ObjectProperty<Double> minimalDetectableBiasXDeflectionProperty() {
		return this.minimalDetectableBiasXDeflection;
	}
	
	public final Double getMinimalDetectableBiasXDeflection() {
		return this.minimalDetectableBiasXDeflectionProperty().get();
	}
	
	public final void setMinimalDetectableBiasXDeflection(final Double minimalDetectableBiasXDeflection) {
		this.minimalDetectableBiasXDeflectionProperty().set(minimalDetectableBiasXDeflection);
	}
	
	public final ObjectProperty<Double> minimalDetectableBiasYDeflectionProperty() {
		return this.minimalDetectableBiasYDeflection;
	}
	
	public final Double getMinimalDetectableBiasYDeflection() {
		return this.minimalDetectableBiasYDeflectionProperty().get();
	}
	
	public final void setMinimalDetectableBiasYDeflection(final Double minimalDetectableBiasYDeflection) {
		this.minimalDetectableBiasYDeflectionProperty().set(minimalDetectableBiasYDeflection);
	}
	
	public final ObjectProperty<Double> residualXDeflectionProperty() {
		return this.residualXDeflection;
	}

	public final Double getResidualXDeflection() {
		return this.residualXDeflectionProperty().get();
	}

	public final void setResidualXDeflection(final Double residualXDeflection) {
		this.residualXDeflectionProperty().set(residualXDeflection);
	}

	public final ObjectProperty<Double> residualYDeflectionProperty() {
		return this.residualYDeflection;
	}
	
	public final Double getResidualYDeflection() {
		return this.residualYDeflectionProperty().get();
	}

	public final void setResidualYDeflection(final Double residualYDeflection) {
		this.residualYDeflectionProperty().set(residualYDeflection);
	}
	
	public final ObjectProperty<Double> redundancyXDeflectionProperty() {
		return this.redundancyXDeflection;
	}
	
	public final Double getRedundancyXDeflection() {
		return this.redundancyXDeflectionProperty().get();
	}
	
	public final void setRedundancyXDeflection(final Double redundancyXDeflection) {
		this.redundancyXDeflectionProperty().set(redundancyXDeflection);
	}
	
	public final ObjectProperty<Double> redundancyYDeflectionProperty() {
		return this.redundancyYDeflection;
	}
	
	public final Double getRedundancyYDeflection() {
		return this.redundancyYDeflectionProperty().get();
	}
	
	public final void setRedundancyYDeflection(final Double redundancyYDeflection) {
		this.redundancyYDeflectionProperty().set(redundancyYDeflection);
	}
	
	public final ObjectProperty<Double> grossErrorXDeflectionProperty() {
		return this.grossErrorXDeflection;
	}
	
	public final Double getGrossErrorXDeflection() {
		return this.grossErrorXDeflectionProperty().get();
	}
	
	public final void setGrossErrorXDeflection(final Double grossErrorXDeflection) {
		this.grossErrorXDeflectionProperty().set(grossErrorXDeflection);
	}
	
	public final ObjectProperty<Double> grossErrorYDeflectionProperty() {
		return this.grossErrorYDeflection;
	}
	
	public final Double getGrossErrorYDeflection() {
		return this.grossErrorYDeflectionProperty().get();
	}
	
	public final void setGrossErrorYDeflection(final Double grossErrorYDeflection) {
		this.grossErrorYDeflectionProperty().set(grossErrorYDeflection);
	}
	
	public final ObjectProperty<Double> testStatisticAprioriDeflectionProperty() {
		return this.testStatisticAprioriDeflection;
	}
	
	public final Double getTestStatisticAprioriDeflection() {
		return this.testStatisticAprioriDeflectionProperty().get();
	}
	
	public final void setTestStatisticAprioriDeflection(final Double testStatisticAprioriDeflection) {
		this.testStatisticAprioriDeflectionProperty().set(testStatisticAprioriDeflection);
	}
	
	public final ObjectProperty<Double> testStatisticAposterioriDeflectionProperty() {
		return this.testStatisticAposterioriDeflection;
	}
	
	public final Double getTestStatisticAposterioriDeflection() {
		return this.testStatisticAposterioriDeflectionProperty().get();
	}
	
	public final void setTestStatisticAposterioriDeflection(final Double testStatisticAposterioriDeflection) {
		this.testStatisticAposterioriDeflectionProperty().set(testStatisticAposterioriDeflection);
	}
	
	public final ObjectProperty<Double> pValueAprioriDeflectionProperty() {
		return this.pValueAprioriDeflection;
	}
	
	public final Double getPValueAprioriDeflection() {
		return this.pValueAprioriDeflectionProperty().get();
	}
	
	public final void setPValueAprioriDeflection(final Double pValueAprioriDeflection) {
		this.pValueAprioriDeflectionProperty().set(pValueAprioriDeflection);
	}
	
	public final ObjectProperty<Double> pValueAposterioriDeflectionProperty() {
		return this.pValueAposterioriDeflection;
	}
	
	public final Double getPValueAposterioriDeflection() {
		return this.pValueAposterioriDeflectionProperty().get();
	}
	
	public final void setPValueAposterioriDeflection(final Double pValueAposterioriDeflection) {
		this.pValueAposterioriDeflectionProperty().set(pValueAposterioriDeflection);
	}
	
	public final ObjectProperty<Double> confidenceADeflectionProperty() {
		return this.confidenceADeflection;
	}
	
	public final Double getConfidenceADeflection() {
		return this.confidenceADeflectionProperty().get();
	}
	
	public final void setConfidenceADeflection(final Double confidenceADeflection) {
		this.confidenceADeflectionProperty().set(confidenceADeflection);
	}
	
	public final ObjectProperty<Double> confidenceBDeflectionProperty() {
		return this.confidenceBDeflection;
	}
	
	public final Double getConfidenceBDeflection() {
		return this.confidenceBDeflectionProperty().get();
	}
	
	public final void setConfidenceBDeflection(final Double confidenceBDeflection) {
		this.confidenceBDeflectionProperty().set(confidenceBDeflection);
	}
	
	public final ObjectProperty<Double> omegaDeflectionProperty() {
		return this.omegaDeflection;
	}
	
	public final Double getOmegaDeflection() {
		return this.omegaDeflectionProperty().get();
	}
	
	public final void setOmegaDeflection(final Double omegaDeflection) {
		this.omegaDeflectionProperty().set(omegaDeflection);
	}
	
	public final BooleanProperty significantDeflectionProperty() {
		return this.significantDeflection;
	}
	
	public final boolean isSignificantDeflection() {
		return this.significantDeflectionProperty().get();
	}
	
	public final void setSignificantDeflection(final boolean significantDeflection) {
		this.significantDeflectionProperty().set(significantDeflection);
	}
	
	public static PointRow cloneRowApriori(PointRow row) {
		PointRow clone = new PointRow();
		
		clone.setId(-1);
		clone.setGroupId(row.getGroupId());
		clone.setEnable(row.isEnable());
		
		clone.setName(row.getName());
		clone.setCode(row.getCode());
		
		clone.setXApriori(row.getXApriori());
		clone.setYApriori(row.getYApriori());
		clone.setZApriori(row.getZApriori());
		
		clone.setSigmaXapriori(row.getSigmaXapriori());
		clone.setSigmaYapriori(row.getSigmaYapriori());
		clone.setSigmaZapriori(row.getSigmaZapriori());
		
		clone.setXAprioriDeflection(row.getXAprioriDeflection());
		clone.setYAprioriDeflection(row.getYAprioriDeflection());
		
		clone.setSigmaXaprioriDeflection(row.getSigmaXaprioriDeflection());
		clone.setSigmaYaprioriDeflection(row.getSigmaYaprioriDeflection());

		return clone;
	}
	
	private static PointRow scan1D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String name; 
			double x = 0.0, y = 0.0, z = 0.0;
			double sigmaZ = 0.0;

			PointRow row = new PointRow();
			
			// Name of point
			if (!scanner.hasNext())
				return null;
			name = scanner.next();
			row.setName(name);
			
			// Y or Z 		
			if (!scanner.hasNextDouble())
				return null;
			y = z = options.convertLengthToModel(scanner.nextDouble());

			// X or Sigma
			if (!scanner.hasNextDouble()) {
				row.setZApriori(z);	
				return row;
			}
			
			x = sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			// Z
			if (!scanner.hasNextDouble()) {
				row.setZApriori(z);	
				row.setSigmaZapriori(sigmaZ);	
				return row;
			}
			
			z = options.convertLengthToModel(scanner.nextDouble());

			// Sigma
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);	
				row.setXApriori(x);	
				row.setZApriori(z);	
				return row;
			}
			
			sigmaZ = options.convertLengthToModel(scanner.nextDouble());
			
			row.setYApriori(y);	
			row.setXApriori(x);	
			row.setZApriori(z);	
			row.setSigmaZapriori(sigmaZ);
			
			return row;
		}
		finally {
			scanner.close();
		}
	}
	
	private static PointRow scan2D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String name; 
			double y = 0.0, x = 0.0;
			double sigmaY = 0.0, sigmaX = 0.0;

			PointRow row = new PointRow();
			// Name of point
			if (!scanner.hasNext())
				return null;
			name = scanner.next();
			row.setName(name);
			
			// Y 		
			if (!scanner.hasNextDouble())
				return null;
			y = options.convertLengthToModel(scanner.nextDouble());

			// X 		
			if (!scanner.hasNextDouble())
				return null;
			x = options.convertLengthToModel(scanner.nextDouble());

			// sigma X (or sigmaX/Y)
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);
				row.setXApriori(x);
				return row;
			}
			
			sigmaY = sigmaX = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);
				row.setXApriori(x);
				row.setSigmaYapriori(sigmaY);
				row.setSigmaXapriori(sigmaX);
				return row;
			}
			
			sigmaX = options.convertLengthToModel(scanner.nextDouble());
			
			row.setYApriori(y);
			row.setXApriori(x);
			row.setSigmaYapriori(sigmaY);
			row.setSigmaXapriori(sigmaX);
			
			return row;
		}
		finally {
			scanner.close();
		}
	}
	
	private static PointRow scan3D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String name; 
			double y = 0.0, x = 0.0, z = 0.0;
			double sigmaY = 0.0, sigmaX = 0.0, sigmaZ = 0.0;

			PointRow row = new PointRow();
			// Name of point
			if (!scanner.hasNext())
				return null;
			name = scanner.next();
			row.setName(name);
			
			// Y 		
			if (!scanner.hasNextDouble())
				return null;
			y = options.convertLengthToModel(scanner.nextDouble());

			// X 		
			if (!scanner.hasNextDouble())
				return null;
			x = options.convertLengthToModel(scanner.nextDouble());

			// Z 		
			if (!scanner.hasNextDouble())
				return null;
			z = options.convertLengthToModel(scanner.nextDouble());

			// sigma X (or sigma x/y/z)
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);
				row.setXApriori(x);
				row.setZApriori(z);
				return row;
			}

			sigmaY = sigmaX = sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y (or sigma Z)
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);
				row.setXApriori(x);
				row.setZApriori(z);
				
				row.setSigmaYapriori(sigmaY);
				row.setSigmaXapriori(sigmaX);
				row.setSigmaZapriori(sigmaZ);
				
				return row;
			}

			sigmaX = sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			// sigma Z
			if (!scanner.hasNextDouble()) {
				row.setYApriori(y);
				row.setXApriori(x);
				row.setZApriori(z);
				
				row.setSigmaYapriori(sigmaY);
				row.setSigmaXapriori(sigmaY);
				row.setSigmaZapriori(sigmaZ);
				
				return row;
			}

			sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			row.setYApriori(y);
			row.setXApriori(x);
			row.setZApriori(z);
			
			row.setSigmaYapriori(sigmaY);
			row.setSigmaXapriori(sigmaX);
			row.setSigmaZapriori(sigmaZ);
			
			return row;
		}
		finally {
			scanner.close();
		}
	}
	
	public static PointRow scan(String str, int dimension) {
		if (dimension == 1)
			return scan1D(str);
		else if (dimension == 2)
			return scan2D(str);
		else if (dimension == 3)
			return scan3D(str);
		return null;
	}
}
