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

package org.applied_geodesy.adjustment.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.ConfidenceRegion;
import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.NormalEquationSystem;
import org.applied_geodesy.adjustment.UnscentedTransformationParameter;
import org.applied_geodesy.adjustment.network.congruence.CongruenceAnalysisGroup;
import org.applied_geodesy.adjustment.network.congruence.CongruenceAnalysisPointPair;
import org.applied_geodesy.adjustment.network.congruence.strain.CoordinateComponent;
import org.applied_geodesy.adjustment.network.congruence.strain.Equation;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.adjustment.network.congruence.strain.StrainAnalysisEquations;
import org.applied_geodesy.adjustment.network.congruence.strain.parameter.StrainParameter;
import org.applied_geodesy.adjustment.network.observation.ComponentType;
import org.applied_geodesy.adjustment.network.observation.DeltaZ;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline1D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaseline3D;
import org.applied_geodesy.adjustment.network.observation.HorizontalDistance;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.SlopeDistance;
import org.applied_geodesy.adjustment.network.observation.ZenithAngle;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.network.parameter.AdditionalUnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameters;
import org.applied_geodesy.adjustment.network.parameter.VerticalDeflection;
import org.applied_geodesy.adjustment.network.parameter.VerticalDeflectionX;
import org.applied_geodesy.adjustment.network.parameter.VerticalDeflectionY;
import org.applied_geodesy.adjustment.network.point.Point;
import org.applied_geodesy.adjustment.network.point.Point3D;
import org.applied_geodesy.adjustment.statistic.BaardaMethodTestStatistic;
import org.applied_geodesy.adjustment.statistic.BinomialTestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.SidakTestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.adjustment.statistic.UnadjustedTestStatitic;
import org.applied_geodesy.jag3d.ui.io.writer.AdjustmentResultWritable;
import org.applied_geodesy.jag3d.ui.io.writer.NetworkAdjustmentResultWriter;
import org.applied_geodesy.transformation.datum.SphericalDeflectionModel;

import javafx.util.Pair;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

public class NetworkAdjustment implements Runnable {
	private Map<Observation, Double> adaptedObservationUncertainties = new LinkedHashMap<Observation, Double>();
	private Map<Point, Double[]> adaptedPointUncertainties = new LinkedHashMap<Point, Double[]>();
	private Map<VerticalDeflection, Double> adaptedVerticalDeflectionUncertainties = new LinkedHashMap<VerticalDeflection, Double>();
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	private boolean calculateStochasticParameters = false;
	private static double SQRT_EPS = Math.sqrt(Constant.EPS);
	private EstimationType estimationType = EstimationType.L2NORM;
	private UpperSymmPackMatrix Qxx = null;
	private SphericalDeflectionModel sphericalDeflectionModel = null;
	
	private int maximalNumberOfIterations        = DefaultValue.getMaximumNumberOfIterations(),
				iterationStep                    = 0,
				numberOfStochasticPointRows      = 0,
				numberOfStochasticDeflectionRows = 0,
				numberOfFixPointRows             = 0,
				numberOfUnknownParameters        = 0,
	            numberOfObservations             = 0,
	            numberOfHypotesis                = 0,
	            numberOfPrincipalComponents      = 0;
	
	private boolean interrupt          = false,
					freeNetwork	 	   = false,
					congruenceAnalysis = false,
					proofOfDatumDefectDetection = false,
					applyAposterioriVarianceOfUnitWeight = true;

	private double maxDx            = Double.MIN_VALUE,
	               degreeOfFreedom  = 0.0,
	               omega            = 0.0,
	               traceCxxPoints   = 0.0,
	               finalLinearisationError = 0.0,
	               robustEstimationLimit   = DefaultValue.getRobustEstimationLimit(),
	               alphaUT    = UnscentedTransformationParameter.getAlpha(),
	               betaUT     = UnscentedTransformationParameter.getBeta(),
	               weightZero = UnscentedTransformationParameter.getWeightZero();

	private EstimationStateType currentEstimationStatus = EstimationStateType.BUSY;
	private double currentMaxAbsDx = maxDx;
	
	private TestStatisticDefinition testStatisticDefinition = new TestStatisticDefinition();
	private TestStatisticDefinition confidenceRegionDefinition = new TestStatisticDefinition(TestStatisticType.NONE, 1.0 - DefaultValue.getConfidenceLevel());
	private TestStatisticParameters significanceTestStatisticParameters = null;
	private BinomialTestStatisticParameters binomialTestStatisticParameters = null;
	private TestStatisticParameters confidenceRegionParameters = null;
	
	private Map<String,Point> allPoints = new LinkedHashMap<String,Point>();
	private List<Point> datumPoints = new ArrayList<Point>();
	private List<Point> stochasticPoints = new ArrayList<Point>();
	
	private List<Point> pointsWithUnknownDeflection = new ArrayList<Point>();
	private List<Point> pointsWithStochasticDeflection = new ArrayList<Point>();
	private List<Point> pointsWithReferenceDeflection = new ArrayList<Point>();
	
	private List<Point> referencePoints = new ArrayList<Point>();
	private PrincipalComponent principalComponents[] = new PrincipalComponent[0];
	private List<CongruenceAnalysisGroup> congruenceAnalysisGroup = new ArrayList<CongruenceAnalysisGroup>();
	private Map<VarianceComponentType, VarianceComponent> varianceComponents = new LinkedHashMap<VarianceComponentType, VarianceComponent>();
	private Map<Integer,Matrix> ATQxxBP_GNSS_EP    = new LinkedHashMap<Integer,Matrix>();
	private Map<Integer,Matrix> PAzTQzzAzP_GNSS_EF = new LinkedHashMap<Integer,Matrix>();
	private UnknownParameters unknownParameters = new UnknownParameters();
	private ObservationGroup projectObservations = new ObservationGroup(-1, Double.NaN, Double.NaN, Double.NaN, Epoch.REFERENCE);
	private RankDefect rankDefect = new RankDefect();
	
	//private String coVarExportPathAndFileName = null;
	private AdjustmentResultWritable adjustmentResultWriter = null;
	
	/**
	 * Berechnung von Vor-Faktoren zur Bestimmung von EP und EF*SP. Fuer terrestrische Beobachtung wird der Vor-Faktor 
	 * temp. in EP bzw. EFSP gespeichert und spaeter mit Nabla verrechnet. Fuer GNSS ergibt sich eine Matrix, die mittels
	 * Nabla zu einem Vektor bzw. Skalar wird. Daher erfolgt die Speicherung in Maps.
	 * 
	 * Zusaetzlich werden die Normalgleichung und der Absolutgliedvektor in-situ ueberschrieben, 
	 * sodass N == Qxx (wenn invert == true) und n == dx am Ende ist.
	 * 
	 * @param N NEG-Matrix
	 * @param n neg-Vektor
	 */
	private void estimateFactorsForOutherAccracy(UpperSymmPackMatrix N, DenseVector n) {
		// Indexzuordnung Submatrix vs. Gesamtmatrix
		Map<Integer, Integer> idxAddParamGlobal2LocalInQxx = new LinkedHashMap<Integer, Integer>();
		Map<Integer, Integer> idxPointGlobal2LocalInQxx = new LinkedHashMap<Integer, Integer>();
		List<Integer> idxAddParamLocal2GlobalInQxx = new ArrayList<Integer>();
		List<Integer> idxPointLocal2GlobalInQxx = new ArrayList<Integer>();
		
		for (int i=0; i<this.unknownParameters.size(); i++) {
			UnknownParameter param = this.unknownParameters.get(i);
			if (param.getColInJacobiMatrix() < 0) 
				continue;
			
			if (param.getParameterType() == ParameterType.POINT1D) {
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix(), idxPointLocal2GlobalInQxx.size());
				
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix());
			}
			else if (param.getParameterType() == ParameterType.POINT2D) {
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix(),     idxPointLocal2GlobalInQxx.size());
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix() + 1, idxPointLocal2GlobalInQxx.size() + 1);
				
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix());
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix() + 1);
			}
			else if (param.getParameterType() == ParameterType.POINT3D) {
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix(),     idxPointLocal2GlobalInQxx.size());
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix() + 1, idxPointLocal2GlobalInQxx.size() + 1);
				idxPointGlobal2LocalInQxx.put(param.getColInJacobiMatrix() + 2, idxPointLocal2GlobalInQxx.size() + 2);
				
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix());
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix() + 1);
				idxPointLocal2GlobalInQxx.add(param.getColInJacobiMatrix() + 2);
			}
			else if (param instanceof AdditionalUnknownParameter){
				idxAddParamGlobal2LocalInQxx.put(param.getColInJacobiMatrix(), idxAddParamLocal2GlobalInQxx.size());
				
				idxAddParamLocal2GlobalInQxx.add(param.getColInJacobiMatrix());
			}
		}

		UpperSymmPackMatrix Qzz = new UpperSymmPackMatrix(idxAddParamLocal2GlobalInQxx.size());
		for (int k=0; k<idxAddParamLocal2GlobalInQxx.size(); k++) {
			int rowM = idxAddParamLocal2GlobalInQxx.get(k);
			for (int j=k; j<idxAddParamLocal2GlobalInQxx.size(); j++) {
				int colM = idxAddParamLocal2GlobalInQxx.get(j);
				Qzz.set(k, j, N.get(rowM, colM));
			}
		}
		
		Matrix QzzNzx = new DenseMatrix(idxAddParamLocal2GlobalInQxx.size(), idxPointLocal2GlobalInQxx.size());
		try {
			MathExtension.inv(Qzz);
			for (int k=0; k<idxAddParamLocal2GlobalInQxx.size(); k++) {
				for (int j=0; j<idxPointLocal2GlobalInQxx.size(); j++) {
					int colM = idxPointLocal2GlobalInQxx.get(j);
					for (int i=0; i<idxAddParamLocal2GlobalInQxx.size(); i++) {
						int idx = idxAddParamLocal2GlobalInQxx.get(i);
						double qzznzx = Qzz.get(k, i) * N.get(idx, colM);
						QzzNzx.add(k, j, qzznzx);
					}
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		// In-Situ Invertierung der NGL: N <-- Qxx, n <-- dx 
		MathExtension.solve(N, n, true);
		this.Qxx = N;
	
		Set<Integer> gnssObsIds = new LinkedHashSet<Integer>();
		for (int i=0; i<this.projectObservations.size(); i++) {
			Observation obs = this.projectObservations.get(i);
			boolean isGNSS = obs.getObservationType() == ObservationType.GNSS1D || obs.getObservationType() == ObservationType.GNSS2D || obs.getObservationType() == ObservationType.GNSS3D;
			if (isGNSS && gnssObsIds.contains(obs.getId()))
				continue;

			if (isGNSS)
				gnssObsIds.add(obs.getId());

			List<Observation> observations = null;
			if (isGNSS)
				observations = ((GNSSBaseline)obs).getBaselineComponents();
			else {
				observations = new ArrayList<Observation>(1);
				observations.add(obs);
			}

			int numOfObs = observations.size();
			Matrix aRedu = new DenseMatrix(numOfObs, idxPointLocal2GlobalInQxx.size());
			Matrix aQxx  = new DenseMatrix(numOfObs, idxPointLocal2GlobalInQxx.size());
			Matrix aAdd  = new DenseMatrix(numOfObs, idxAddParamLocal2GlobalInQxx.size());
			double weights[] = new double[numOfObs];
			
			for (int d=0; d<numOfObs; d++) {
				Observation observation = observations.get(d);
				
				// Gewicht der Beobachtung
				weights[d] = 1.0/observation.getStdApriori()/observation.getStdApriori();
				
				// Bestimme Zeile aus dem Produkt: A'*Qxx
				for (int column=0; column < QzzNzx.numColumns(); column++)
					aQxx.set(d, column, this.getAQxxElement(observation, idxPointLocal2GlobalInQxx.get(column), true));

				// X-Lotabweichung des Standpunktes
				int col = observation.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffVerticalDeflectionXs());
				}
				// Y-Lotabweichung des Standpunktes
				col = observation.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffVerticalDeflectionYs());
				}
				// X-Lotabweichung des Zielpunktes
				col = observation.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffVerticalDeflectionXe());
				}
				// Y-Lotabweichung des Zielpunktes
				col = observation.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffVerticalDeflectionYe());
				}
				
				// Orientierung					
				col = observation.getColInJacobiMatrixFromOrientation();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffOri());
				}
				
				// Massstab
				col = observation.getColInJacobiMatrixFromScale();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffScale());
				}
				
				// Additionskonstante
				col = observation.getColInJacobiMatrixFromAdd();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffAdd());
				}
				
				// Refraktion
				col = observation.getColInJacobiMatrixFromRefCoeff();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffRefCoeff());
				}
				
				// Rotation X
				col = observation.getColInJacobiMatrixFromRotationX();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffRotX());
				}
				
				// Rotation Y
				col = observation.getColInJacobiMatrixFromRotationY();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffRotY());
				}
				
				// Rotation Z
				col = observation.getColInJacobiMatrixFromRotationZ();
				if (col >= 0 && idxAddParamGlobal2LocalInQxx.containsKey(col)) {
					col = idxAddParamGlobal2LocalInQxx.get(col);
					aAdd.set(d, col, observation.diffRotZ());
				}
			}
			
			// Bestimme um Zusatzunbekannte reduzierten Anteil der Designmatrix A
			aAdd.mult(QzzNzx, aRedu);
			aRedu = aRedu.scale(-1.0);
			
			for (int d=0; d<numOfObs; d++) {
				Observation observation = observations.get(d);
				
				int col = observation.getStartPoint().getColInJacobiMatrix();
				int dim = observation.getStartPoint().getDimension();
				
				// Startpunkt
				if (col >= 0 && idxPointGlobal2LocalInQxx.containsKey(col)) {
					col = idxPointGlobal2LocalInQxx.get(col);
					if (dim != 1) {
						aRedu.add(d, col++, observation.diffXs());
						aRedu.add(d, col++, observation.diffYs());
					}
					if (dim != 2) {
						aRedu.add(d, col++, observation.diffZs());
					}
				}

				col = observation.getEndPoint().getColInJacobiMatrix();
				dim = observation.getEndPoint().getDimension();

				// Zielpunkt
				if (col >= 0 && idxPointGlobal2LocalInQxx.containsKey(col)) {
					col = idxPointGlobal2LocalInQxx.get(col);
					if (dim != 1) {
						aRedu.add(d, col++, observation.diffXe());
						aRedu.add(d, col++, observation.diffYe());
					}
					if (dim != 2) {
						aRedu.add(d, col++, observation.diffZe());
					}
				}
			}

			Matrix AzTQzz = new DenseMatrix(aAdd.numRows(), Qzz.numColumns());
			aAdd.mult(Qzz, AzTQzz);
			Matrix PAzTQzzAzP = new UpperSymmPackMatrix(aAdd.numRows());
			AzTQzz.transBmult(aAdd, PAzTQzzAzP);
			AzTQzz = null;

			Matrix ATQxxBP = new DenseMatrix(numOfObs,numOfObs);
			aQxx.transBmult(aRedu, ATQxxBP);
			
			for (int r=0; r<numOfObs; r++) {
				for (int c=0; c<numOfObs; c++) {
					ATQxxBP.set(r,c,  weights[r] * ATQxxBP.get(r,c));
					PAzTQzzAzP.set(r,c, weights[c] * weights[r] * PAzTQzzAzP.get(r,c));
				}
			}
			
			if (isGNSS) {			
				this.ATQxxBP_GNSS_EP.put(obs.getId(), ATQxxBP);
				this.PAzTQzzAzP_GNSS_EF.put(obs.getId(), PAzTQzzAzP);
			}
			else {
				// Geschaetzte Modellstoerung ist noch nicht bestimmt
				// Sodass hier zunachst nur der Faktor A'QxxArPi
				// zwischengespeichert wird
				obs.setInfluenceOnPointPosition(ATQxxBP.get(0, 0));
				obs.setInfluenceOnNetworkDistortion(PAzTQzzAzP.get(0, 0));
			}
		}
	}

	/**
	 * Liefert ein Element des Matrizenprodukts aqxx = AQ<sub>xx</sub>(i,j)
	 * @param observation
	 * @param column
	 * @return aqxx
	 */
	private double getAQxxElement(Observation observation, int column) {
		return this.getAQxxElement(observation, column, false);
	}
	/**
	 * Liefert ein Element des Matrizenprodukts aqxx = AQ<sub>xx</sub>(i,j) wahlweise auch ohne Zusatzparameter
	 * @param observation
	 * @param column
	 * @param withoutAdditionalParameters
	 * @return aqxx
	 */
	private double getAQxxElement(Observation observation, int column, boolean withoutAdditionalParameters) {
		if (column < 0)
			return 0.0;
		
		double aqxx = 0.0;
		
		int row = observation.getStartPoint().getColInJacobiMatrix();
		int dim = observation.getStartPoint().getDimension();
		
		// Startpunkt
		if (row >= 0) {
			if (dim != 1) {
				aqxx += observation.diffXs()*this.Qxx.get(row++, column);
				aqxx += observation.diffYs()*this.Qxx.get(row++, column);
			}
			if (dim != 2) {
				aqxx += observation.diffZs()*this.Qxx.get(row, column);
			}
		}
		
		row = observation.getEndPoint().getColInJacobiMatrix();
		dim = observation.getEndPoint().getDimension();
		
		// Zielpunkt
		if (row >= 0) {
			if (dim != 1) {
				aqxx += observation.diffXe()*this.Qxx.get(row++, column);
				aqxx += observation.diffYe()*this.Qxx.get(row++, column);
			}
			if (dim != 2) {
				aqxx += observation.diffZe()*this.Qxx.get(row, column);
			}
		}
		
		if (!withoutAdditionalParameters) {
			// Zusatzparameter
			// X-Lotabweichung des Standpunktes
			row = observation.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix();
			if (row >= 0)
				aqxx += observation.diffVerticalDeflectionXs()*this.Qxx.get(row, column);
			// Y-Lotabweichung des Standpunktes
			row = observation.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix();
			if (row >= 0)
				aqxx += observation.diffVerticalDeflectionYs()*this.Qxx.get(row, column);
			// X-Lotabweichung des Zielpunktes
			row = observation.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix();
			if (row >= 0)
				aqxx += observation.diffVerticalDeflectionXe()*this.Qxx.get(row, column);
			// Y-Lotabweichung des Zielpunktes
			row = observation.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix();
			if (row >= 0)
				aqxx += observation.diffVerticalDeflectionYe()*this.Qxx.get(row, column);
			// Orientierung
			row = observation.getColInJacobiMatrixFromOrientation();
			if (row >= 0)
				aqxx += observation.diffOri()*this.Qxx.get(row, column);
			// Maßstab
			row = observation.getColInJacobiMatrixFromScale();
			if (row >= 0)
				aqxx += observation.diffScale()*this.Qxx.get(row, column);
			// Additionskonstante
			row = observation.getColInJacobiMatrixFromAdd();
			if (row >= 0)
				aqxx += observation.diffAdd()*this.Qxx.get(row, column);
			// Refraktion
			row = observation.getColInJacobiMatrixFromRefCoeff();
			if (row >= 0)
				aqxx += observation.diffRefCoeff()*this.Qxx.get(row, column);
			// Rotation X
			row = observation.getColInJacobiMatrixFromRotationX();
			if (row >= 0)
				aqxx += observation.diffRotX()*this.Qxx.get(row, column);
			// Rotation Y
			row = observation.getColInJacobiMatrixFromRotationY();
			if (row >= 0)
				aqxx += observation.diffRotY()*this.Qxx.get(row, column);
			// Rotation Z
			row = observation.getColInJacobiMatrixFromRotationZ();
			if (row >= 0)
				aqxx += observation.diffRotZ()*this.Qxx.get(row, column);		
		}
		return aqxx;
	}
	
	/**
	 * Liefert ein Element der Kofaktormatrix der Beobachtungen (a-post)
	 * qll = Q<sub>ll</sub>(i,j) = AQ<sub>xx</sub>A<sup>T</sup>(i,j)
	 * @param observationOne
	 * @param observationTwo
	 * @return qll
	 */
	private double getQllElement(Observation observationOne, Observation observationTwo) {

		double qll = 0;
		
		int col = observationTwo.getStartPoint().getColInJacobiMatrix();
		int dim = observationTwo.getStartPoint().getDimension();
		
		// Startpunkt
		if (col >= 0) {
			if (dim != 1) {
				qll += observationTwo.diffXs()*this.getAQxxElement(observationOne, col++);
				qll += observationTwo.diffYs()*this.getAQxxElement(observationOne, col++);
			}
			if (dim != 2) {
				qll += observationTwo.diffZs()*this.getAQxxElement(observationOne, col);
			}
		}
		
		col = observationTwo.getEndPoint().getColInJacobiMatrix();
		dim = observationTwo.getEndPoint().getDimension();
		
		// Endpunkt
		if (col >= 0) {
			if (dim != 1) {
				qll += observationTwo.diffXe()*this.getAQxxElement(observationOne, col++);
				qll += observationTwo.diffYe()*this.getAQxxElement(observationOne, col++);
			}
			if (dim != 2) {
				qll += observationTwo.diffZe()*this.getAQxxElement(observationOne, col);
			}
		}
		
		// Zusatzparameter
		// X-Lotabweichung des Standpunktes
		col = observationTwo.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix();
		if (col >= 0)
			qll += observationTwo.diffVerticalDeflectionXs()*this.getAQxxElement(observationOne, col);
		// Y-Lotabweichung des Standpunktes
		col = observationTwo.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix();
		if (col >= 0)
			qll += observationTwo.diffVerticalDeflectionYs()*this.getAQxxElement(observationOne, col);
		// X-Lotabweichung des Zielpunktes
		col = observationTwo.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix();
		if (col >= 0)
			qll += observationTwo.diffVerticalDeflectionXe()*this.getAQxxElement(observationOne, col);
		// Y-Lotabweichung des Zielpunktes
		col = observationTwo.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix();
		if (col >= 0)
			qll += observationTwo.diffVerticalDeflectionYe()*this.getAQxxElement(observationOne, col);
		// Orientierung
		col = observationTwo.getColInJacobiMatrixFromOrientation();
		if (col >= 0)
			qll += observationTwo.diffOri()*this.getAQxxElement(observationOne, col);
		// Maßstab
		col = observationTwo.getColInJacobiMatrixFromScale();
		if (col >= 0)
			qll += observationTwo.diffScale()*this.getAQxxElement(observationOne, col);
		// Additionskonstante
		col = observationTwo.getColInJacobiMatrixFromAdd();
		if (col >= 0)
			qll += observationTwo.diffAdd()*this.getAQxxElement(observationOne, col);
		// Refraktion
		col = observationTwo.getColInJacobiMatrixFromRefCoeff();
		if (col >= 0)
			qll += observationTwo.diffRefCoeff()*this.getAQxxElement(observationOne, col);
		// Rotation X
		col = observationTwo.getColInJacobiMatrixFromRotationX();
		if (col >= 0)
			qll += observationTwo.diffRotX()*this.getAQxxElement(observationOne, col);
		// Rotation Y
		col = observationTwo.getColInJacobiMatrixFromRotationY();
		if (col >= 0)
			qll += observationTwo.diffRotY()*this.getAQxxElement(observationOne, col);
		// Rotation Z
		col = observationTwo.getColInJacobiMatrixFromRotationZ();
		if (col >= 0)
			qll += observationTwo.diffRotZ()*this.getAQxxElement(observationOne, col);

		return qll;
	}
	
	public void addSubRedundanceAndCofactor2Deflection(Point deflectionPoint, boolean restoreUncertainties) {
		
		VerticalDeflection deflectionX = deflectionPoint.getVerticalDeflectionX();
		VerticalDeflection deflectionY = deflectionPoint.getVerticalDeflectionY();
		
		int colX = deflectionX.getColInJacobiMatrix();
		int colY = deflectionY.getColInJacobiMatrix();
		int cols[] = new int[] {colX, colY};
		
		int dim = cols.length; // Anz der Lotparameter
		
		double rDiag[] = new double[dim];
		double sumDiagR = 0;

		//Matrix Qvv = new UpperSymmDenseMatrix(dim);
		//Matrix R   = new DenseMatrix(dim,dim);
		Matrix Qll_P = new DenseMatrix(dim,dim);
		Matrix PR    = new UpperSymmPackMatrix(dim);
		Matrix Qnn   = new DenseMatrix(dim,dim);
		Vector Pv    = new DenseVector(dim);
		Vector Pv0   = new DenseVector(dim);
		Vector nabla = new DenseVector(dim);
		Matrix subPsubPQvvP = new UpperSymmPackMatrix(dim);

		double qll[] = new double[dim];
		double qll0[]= new double[dim];
		
		Double u[] = new Double[dim];
		if (this.estimationType == EstimationType.L1NORM && restoreUncertainties) {
			if (this.adaptedVerticalDeflectionUncertainties.containsKey(deflectionX))
				u[0] = this.adaptedVerticalDeflectionUncertainties.get(deflectionX);
			if (this.adaptedVerticalDeflectionUncertainties.containsKey(deflectionY))
				u[1] = this.adaptedVerticalDeflectionUncertainties.get(deflectionY);
		}
		
		int diag = 0;
		double vx = (deflectionX.getValue() - deflectionX.getValue0());
		double vy = (deflectionY.getValue() - deflectionY.getValue0());

		qll[diag]    = deflectionX.getStdApriori()*deflectionX.getStdApriori();
		qll0[diag]   = u[diag] == null ? qll[diag] : u[diag];
		deflectionX.setOmega(vx*vx/qll0[diag]);
		Pv0.set(diag, vx/qll0[diag]);
		Pv.set(diag, vx/qll[diag++]);

		qll[diag]    = deflectionY.getStdApriori()*deflectionY.getStdApriori();
		qll0[diag]   = u[diag] == null ? qll[diag] : u[diag];
		deflectionY.setOmega(vy*vy/qll0[diag]);
		Pv0.set(diag, vy/qll0[diag]);
		Pv.set(diag, vy/qll[diag++]);

		for (int i=0; i<dim; i++) {
			for (int j=i; j<dim; j++) {
				double qxx = this.Qxx.get(cols[i], cols[j]);
				if (i==j) {
					double qvv = qll[i] - qxx;

					//Qll(a-post) kann aus num. gruenden mal groesser als Qll(aprio) werden
					//In diesem Fall wird die Diagonale Qvv negativ, was nicht zulassig ist
					//Liegen Qll(a-post) und Qll(aprio) sehr dich beisammen, wird Qvv sehr klein
					//Dies kann zu einer Verzerrung von R führen. 
					qvv = Math.abs(qvv) < Constant.EPS || qvv < 0 ? 0.0:qvv;						

					//Qvv.set(i,i, qll[i] - qxx);
					//double rr = 1.0/qll[i]*Qvv.get(i,i);
					double rr = 1.0/qll[i]*qvv;
					rDiag[i] = rr;
					sumDiagR += rr;
					//R.set(i,i, rr);
					Qll_P.set(i,i,  qxx/qll[i]);
					PR.set(i,i, 1.0/qll[i]*rr);
				}
				else {
					double qvv = -qxx;

					//Qll(a-post) kann aus num. gruenden mal groesser als Qll(aprio) werden
					//In diesem Fall wird die Diagonale Qvv negativ, was nicht zulassig ist
					//Liegen Qll(a-post) und Qll(aprio) sehr dich beisammen, wird Qvv sehr klein
					//Dies kann zu einer Verzerrung von R führen. 
					qvv = Math.abs(qvv)<Constant.EPS?0.0:qvv;

					//Qvv.set(i, j, -qxx);
					//double rrIJ = 1.0/qll[i]*Qvv.get(i,j);
					double rrIJ = 1.0/qll[i]*qvv;
					//R.set(j,i, rrIJ);
					Qll_P.set(j,i, qxx/qll[i]);
					PR.set(i,j, 1.0/qll[j]*rrIJ);
					//double rrJI = 1.0/qll[j]*Qvv.get(i,j);
					//double rrJI = 1.0/qll[j]*qvv;
					//R.set(i,j, rrJI);
					Qll_P.set(i,j, qxx/qll[j]);
					//RP.set(j,i, 1.0/qll[i]*rrJI);
				}
			}
		}
		subPsubPQvvP = new UpperSymmPackMatrix(PR);
		subPsubPQvvP.scale(-1.0);
		for (int d=0; d<dim; d++) {
			subPsubPQvvP.add(d, d, 1.0/qll[d]);
		}			

		deflectionX.setRedundancy(rDiag[0]);
		deflectionY.setRedundancy(rDiag[1]);
		
		this.degreeOfFreedom += sumDiagR;
		this.omega += deflectionX.getOmega() + deflectionY.getOmega();
		
		VarianceComponentType vcType = VarianceComponentType.STOCHASTIC_DEFLECTION_COMPONENT;
		if (vcType != null && this.varianceComponents.containsKey(vcType)) {
			VarianceComponent vc = this.varianceComponents.get(vcType);
			vc.setOmega(vc.getOmega() + deflectionX.getOmega() + deflectionY.getOmega());
			vc.setRedundancy(vc.getRedundancy() + sumDiagR);
			vc.setNumberOfObservations(vc.getNumberOfObservations() + 2);
			if (sumDiagR > 0) {
				vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + (vx < 0 ? 1 : 0) + (vy < 0 ? 1 : 0));
				vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + 2);
			}
		}

		if (sumDiagR > SQRT_EPS) {
			boolean isCalculated = false;
			ConfidenceRegion confidenceRegion = null;
			try {
				Qnn = MathExtension.pinv(PR, -1);
				confidenceRegion = new ConfidenceRegion(Qnn);
				isCalculated = true;
			} 
			catch (NotConvergedException nce) {
				isCalculated = false;
				nce.printStackTrace();
			}
			
			if (isCalculated) {
			    if (this.estimationType == EstimationType.SIMULATION) {
			    	// Nichtzentralitaetsparameter ist noch nicht bestimmt, 
					// sodass GRZW ein vorlaeufiger Wert ist, 
				    // der nabla*Pnn*nabla == 1 erfuellt.
			    	Vector nabla0 = new DenseVector(dim);
					for (int j=0; j<dim; j++)
						nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));
					
					Vector subPsubPQvvPNabla0 = new DenseVector(dim);
					subPsubPQvvP.mult(nabla0, subPsubPQvvPNabla0);
	
					//Qll_P.mult(nabla0, ep);
					
					//deflectionX.setMinimalDetectableBias(nabla0.get(0));
					//deflectionY.setMinimalDetectableBias(nabla0.get(1));
					deflectionX.setMaximumTolerableBias(nabla0.get(0));
					deflectionY.setMaximumTolerableBias(nabla0.get(1));
			    }
			    else {
			    	Qnn.mult(Pv, nabla);
			    	double normNabla = nabla.norm(Vector.Norm.Two);
			    	double nablaCoVarNable = Math.abs(nabla.dot(Pv0));
			    	((VerticalDeflectionX)deflectionX).setNablaCoVarNabla( nablaCoVarNable );

			    	nabla = nabla.scale(-1.0);
			    	deflectionX.setGrossError(nabla.get(0));
			    	deflectionY.setGrossError(nabla.get(1));

			    	//Qll_P.mult(nabla, ep);

			    	Vector subPsubPQvvPNabla = new DenseVector(dim);
			    	subPsubPQvvP.mult(nabla, subPsubPQvvPNabla);

			    	// Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
			    	Vector nabla0 = new DenseVector(nabla, true);
			    	Vector PQvvPnabla0 = new DenseVector(nabla0);
			    	PR.mult(nabla0, PQvvPnabla0);
			    	double nQn0 = nabla0.dot(PQvvPnabla0);
			    	if (normNabla < SQRT_EPS || nQn0 <= 0) {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));
			    	}
			    	else {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, nabla0.get(j)/Math.sqrt(nQn0));
			    	}
					//deflectionX.setMinimalDetectableBias(nabla0.get(0));
					//deflectionY.setMinimalDetectableBias(nabla0.get(1));
					deflectionX.setMaximumTolerableBias(nabla0.get(0));
					deflectionY.setMaximumTolerableBias(nabla0.get(1));
			    }
			}
		}
		if (restoreUncertainties && sumDiagR > SQRT_EPS)
			this.numberOfHypotesis++;
	}
	
	/**
	 * Berechnet die Elemente der Kofaktormatrix der Punktesbeobachtung
	 * nach der Ausgleichung aus <code>Q_ll(i,i) = A*Qxx*A'</code>, fuer i von 1 bis point.getDimension()
	 * 
	 * Berechnet die Elemente der Kofaktormatrix der Verbesserungen fuer einen Punkt
	 * <code>Q<sub>vv</sub>(i,i) = P<sup>-1</sup> - Q<sub>ll</sub></code>
	 * 
	 * Berechnet die Elemente der Redundanzmatrix der Punktbeobachtung
	 * nach der Ausgleichung aus <code>R(i,i) = P * Q<sub>vv</sub></code>
	 * 
	 * Ergaenzt die Punktbeobachtung durch ihre Kofaktoren qll und
	 * ihren Redundanzanteil r, dieser wird gleichzeitig 
	 * zur Gesamtredundanz benutzt. Ferner wird &Omega; der Gesamtausgleichung
	 * berechnet und das fuer den statistischen Test benoetigte Produkt &nabla;Q<sub>&nabla;&nabla;</sub>&nabla;,
	 * welches ebenfalls der Punktbeobachtungen uebergeben wird.
	 * 
	 * 
	 * <strong>HINWEIS</strong> !!!Das doppelte Aufrufen der Methode fuert somit zu
	 * falschen Ergebnissen beim Redundanzanteil und &Omega;. Die a-priori 
	 * Standardabweichung wird durch den Kofaktor ueberschrieben.!!!  
	 * 
	 * @param point
	 * @param restoreUncertainties
	 */
	public void addSubRedundanceAndCofactor2Point(Point point, boolean restoreUncertainties) {
		int dim = point.getDimension();
		int col = point.getColInJacobiMatrix();
		int negativeSignPoint = 0;
		double rDiag[] = new double[dim];	
		double sumDiagR = 0;
		double omegaPoint = 0.0;
		//Matrix Qvv = new UpperSymmDenseMatrix(dim);
		//Matrix R   = new DenseMatrix(dim,dim);
		Matrix Qll_P = new DenseMatrix(dim,dim);
		Matrix PR    = new UpperSymmPackMatrix(dim);
		Matrix Qnn   = new DenseMatrix(dim,dim);
		Vector Pv    = new DenseVector(dim);
		Vector Pv0   = new DenseVector(dim);
		Vector nabla = new DenseVector(dim);
		Vector ep    = new DenseVector(dim);
		Matrix subPsubPQvvP = new UpperSymmPackMatrix(dim);

		double qll[] = new double[dim];
		double qll0[]= new double[dim];
		Double u[] = this.estimationType == EstimationType.L1NORM && restoreUncertainties && this.adaptedPointUncertainties.containsKey(point) ? this.adaptedPointUncertainties.get(point) : new Double[dim];
		int diag = 0;
		if (dim != 1) {
			double vx = (point.getX() - point.getX0());
			double vy = (point.getY() - point.getY0());

			qll[diag]  = point.getStdXApriori()*point.getStdXApriori();
			qll0[diag] = u[diag] == null ? qll[diag] : u[diag];
			omegaPoint += (vx*vx/qll0[diag]);
			Pv0.set(diag, vx/qll0[diag]);
			Pv.set(diag, vx/qll[diag++]);

			qll[diag] = point.getStdYApriori()*point.getStdYApriori();
			qll0[diag] = u[diag] == null ? qll[diag] : u[diag];
			omegaPoint += (vy*vy/qll0[diag]);
			Pv0.set(diag, vy/qll0[diag]);
			Pv.set(diag, vy/qll[diag++]);
			
			negativeSignPoint += vx < 0 ? 1 : 0;
			negativeSignPoint += vy < 0 ? 1 : 0;
		}
		if (dim != 2) {
			double vz = (point.getZ() - point.getZ0());

			qll[diag] = point.getStdZApriori()*point.getStdZApriori();
			qll0[diag] = u[diag] == null ? qll[diag] : u[diag];
			omegaPoint += (vz*vz/qll0[diag]);
			Pv0.set(diag, vz/qll0[diag]);
			Pv.set(diag, vz/qll[diag++]);
			
			negativeSignPoint += vz < 0 ? 1 : 0;
		}

		for (int i=0; i<dim; i++) {
			for (int j=i; j<dim; j++) {
				double qxx = this.Qxx.get(col+i, col+j);
				if (i==j) {
					double qvv = qll[i] - qxx;

					//Qll(a-post) kann aus num. gruenden mal groesser als Qll(aprio) werden
					//In diesem Fall wird die Diagonale Qvv negativ, was nicht zulassig ist
					//Liegen Qll(a-post) und Qll(aprio) sehr dich beisammen, wird Qvv sehr klein
					//Dies kann zu einer Verzerrung von R führen. 
					qvv = Math.abs(qvv) < Constant.EPS || qvv < 0 ? 0.0:qvv;						

					//Qvv.set(i,i, qll[i] - qxx);
					//double rr = 1.0/qll[i]*Qvv.get(i,i);
					double rr = 1.0/qll[i]*qvv;
					rDiag[i] = rr;
					sumDiagR += rr;
					//R.set(i,i, rr);
					Qll_P.set(i,i,  qxx/qll[i]);
					PR.set(i,i, 1.0/qll[i]*rr);
				}
				else {
					double qvv = -qxx;

					//Qll(a-post) kann aus num. gruenden mal groesser als Qll(aprio) werden
					//In diesem Fall wird die Diagonale Qvv negativ, was nicht zulassig ist
					//Liegen Qll(a-post) und Qll(aprio) sehr dich beisammen, wird Qvv sehr klein
					//Dies kann zu einer Verzerrung von R führen. 
					qvv = Math.abs(qvv)<Constant.EPS?0.0:qvv;

					//Qvv.set(i, j, -qxx);
					//double rrIJ = 1.0/qll[i]*Qvv.get(i,j);
					double rrIJ = 1.0/qll[i]*qvv;
					//R.set(j,i, rrIJ);
					Qll_P.set(j,i, qxx/qll[i]);
					PR.set(i,j, 1.0/qll[j]*rrIJ);
					//double rrJI = 1.0/qll[j]*Qvv.get(i,j);
					//double rrJI = 1.0/qll[j]*qvv;
					//R.set(i,j, rrJI);
					Qll_P.set(i,j, qxx/qll[j]);
					//RP.set(j,i, 1.0/qll[i]*rrJI);
				}
			}
		}
		subPsubPQvvP = new UpperSymmPackMatrix(PR);
		subPsubPQvvP.scale(-1.0);
		for (int d=0; d<dim; d++) {
			subPsubPQvvP.add(d, d, 1.0/qll[d]);
		}			

		point.setRedundancy( rDiag );
		this.degreeOfFreedom += sumDiagR;
		point.setOmega(omegaPoint);
		this.omega += omegaPoint;
		
		VarianceComponentType vcType = VarianceComponentType.getComponentTypeByPointDimension(dim);
		if (vcType != null && this.varianceComponents.containsKey(vcType)) {
			VarianceComponent vc = this.varianceComponents.get(vcType);
			vc.setOmega(vc.getOmega() + omegaPoint);
			vc.setRedundancy(vc.getRedundancy() + sumDiagR);
			vc.setNumberOfObservations(vc.getNumberOfObservations() + point.getDimension());
			if (sumDiagR > 0) {
				vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + negativeSignPoint);
				vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + point.getDimension());
			}
		}

		if (sumDiagR > SQRT_EPS) {
			boolean isCalculated = false;
			ConfidenceRegion confidenceRegion = null;
			try {
				Qnn = MathExtension.pinv(PR, -1);
				confidenceRegion = new ConfidenceRegion(Qnn);
				isCalculated = true;
			} 
			catch (NotConvergedException nce) {
				isCalculated = false;
				nce.printStackTrace();
			}
			
			if (isCalculated) {
			    if (this.estimationType == EstimationType.SIMULATION) {
			    	// Nichtzentralitaetsparameter ist noch nicht bestimmt, 
					// sodass GRZW ein vorlaeufiger Wert ist, 
				    // der nabla*Pnn*nabla == 1 erfuellt.
			    	Vector nabla0 = new DenseVector(dim);
					for (int j=0; j<dim; j++)
						nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));
					
					Vector subPsubPQvvPNabla0 = new DenseVector(dim);
					subPsubPQvvP.mult(nabla0, subPsubPQvvPNabla0);
					double efsp = Math.sqrt(Math.abs(subPsubPQvvPNabla0.dot(nabla0)));		
//					Vector PQvvPnabla0 = new DenseVector(nabla0);
//					BTPQvvPB.mult(nabla0, PQvvPnabla0);
//					double nQn0 = nabla0.dot(PQvvPnabla0);
//					for (int j=0; j<dim; j++)
//						if (nQn0 > 0)
//							nabla0.set(j, nabla0.get(j)/Math.sqrt(nQn0));
					Qll_P.mult(nabla0, ep);
					//point.setMinimalDetectableBiases( Matrices.getArray(nabla0) );
					point.setMaximumTolerableBiases( Matrices.getArray(nabla0) );
					point.setInfluencesOnPointPosition( Matrices.getArray(ep) );
					point.setInfluenceOnNetworkDistortion(efsp);
			    }
			    else {
			    	Qnn.mult(Pv, nabla);
			    	point.setNablaCoVarNabla( Math.abs(nabla.dot(Pv0)) );
			    	nabla = nabla.scale(-1.0);
			    	point.setGrossErrors( Matrices.getArray(nabla) );
			    	Qll_P.mult(nabla, ep);
			    	point.setInfluencesOnPointPosition( Matrices.getArray(ep) );

			    	Vector subPsubPQvvPNabla = new DenseVector(dim);
			    	subPsubPQvvP.mult(nabla, subPsubPQvvPNabla);
			    	double efsp = Math.sqrt(Math.abs(subPsubPQvvPNabla.dot(nabla)));	

			    	// Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
			    	Vector nabla0 = new DenseVector(nabla, true);
			    	Vector PQvvPnabla0 = new DenseVector(nabla0);
			    	PR.mult(nabla0, PQvvPnabla0);
			    	double nQn0 = nabla0.dot(PQvvPnabla0);
			    	double normNabla = nabla.norm(Vector.Norm.Two);
			    	if (normNabla < SQRT_EPS || nQn0 <= 0) {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));
			    	}
			    	else {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, nabla0.get(j)/Math.sqrt(nQn0));
			    	}
			    	//point.setMinimalDetectableBiases(Matrices.getArray(nabla0));
					point.setMaximumTolerableBiases( Matrices.getArray(nabla0) );
					point.setInfluenceOnNetworkDistortion(efsp);
			    }
			}
		}
		if (restoreUncertainties && sumDiagR > SQRT_EPS)
			this.numberOfHypotesis++;
	}
	
	/**
	 * Berechnet die Elemente der Kofaktormatrix der Beobachtung
	 * nach der Ausgleichung aus <code>Q_ll(i,i) = A*Qxx*A'</code>, fuer i = 1
	 * 
	 * Berechnet die Elemente der Kofaktormatrix der Verbesserungen fuer eine Beobachtung
	 * <code>Q<sub>vv</sub>(i,i) = P<sup>-1</sup> - Q<sub>ll</sub></code>
	 * 
	 * Berechnet die Elemente der Redundanzmatrix der Beobachtung
	 * nach der Ausgleichung aus <code>R(i,i) = P * Q<sub>vv</sub></code>
	 * 
	 * Ergaenzt die Beobachtung durch den Kofaktor qll und 
	 * ihren Redundanzanteil r, dieser wird gleichzeitig 
	 * zur Gesamtredundanz benutzt. Ferner wird &Omega; der Gesamtausgleichung
	 * berechnet und das fuer den statistischen Test benoetigte Produkt &nabla;Q<sub>&nabla;&nabla;</sub>&nabla;,
	 * welches ebenfalls der Beobachtungen uebergeben wird.
	 * 
	 * <strong>HINWEIS</strong> !!!Das doppelte Aufrufen der Methode fuert somit zu
	 * falschen ergebnissen beim Redundanzanteil und &Omega;. Die a-priori Standardabweichung
	 * wird durch den Kofaktor ueberschrieben.!!!  
	 */
	public void addSubRedundanceAndCofactor(Observation observation) {
		boolean isGNSS = observation.getObservationType() == ObservationType.GNSS1D || observation.getObservationType() == ObservationType.GNSS2D || observation.getObservationType() == ObservationType.GNSS3D;
		List<Observation> observations = null;
		if (isGNSS)
			observations = ((GNSSBaseline)observation).getBaselineComponents();
		else {
			observations = new ArrayList<Observation>(1);
			observations.add(observation);
		}
		
		int dim = observations.size();
		Matrix aRows = new DenseMatrix(dim, this.numberOfUnknownParameters);
		
		// Punkte der Beobachtung
		Point startPoint = observation.getStartPoint();
		Point endPoint   = observation.getEndPoint();
		
		// Hole Indizes in Jacobi-Matrix und speichere in Liste
		Set<Integer> colums = new LinkedHashSet<Integer>(15);
		
		for (int row=0; row<dim; row++) {
			Observation obs = observations.get(row);
			int colASp = startPoint.getColInJacobiMatrix();
			int colAEp = endPoint.getColInJacobiMatrix();
			
			if (colASp >= 0) {
				if (startPoint.getDimension() != 1) {
					colums.add(colASp);
					aRows.set(row, colASp++, obs.diffXs());
					colums.add(colASp);
					aRows.set(row, colASp++, obs.diffYs());
				}
			
				if (startPoint.getDimension() != 2) {
					colums.add(colASp);
					aRows.set(row, colASp, obs.diffZs());
				}
			}
			if (colAEp >= 0) {
				if (endPoint.getDimension() != 1) {
					colums.add(colAEp);
					aRows.set(row, colAEp++, obs.diffXe());
					colums.add(colAEp);
					aRows.set(row, colAEp++, obs.diffYe());
				}
			
				if (endPoint.getDimension() != 2) {
					colums.add(colAEp);
					aRows.set(row, colAEp, obs.diffZe());
				}
			}			
			// mgl. Zusatzparameter
			if (obs.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix() >= 0) {
				colums.add(obs.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix());
				aRows.set(row, obs.getStartPoint().getVerticalDeflectionX().getColInJacobiMatrix(), obs.diffVerticalDeflectionXs());
			}
			if (obs.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix() >= 0) {
				colums.add(obs.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix());
				aRows.set(row, obs.getStartPoint().getVerticalDeflectionY().getColInJacobiMatrix(), obs.diffVerticalDeflectionYs());
			}
			if (obs.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix() >= 0) {
				colums.add(obs.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix());
				aRows.set(row, obs.getEndPoint().getVerticalDeflectionX().getColInJacobiMatrix(), obs.diffVerticalDeflectionXe());
			}
			if (obs.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix() >= 0) {
				colums.add(obs.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix());
				aRows.set(row, obs.getEndPoint().getVerticalDeflectionY().getColInJacobiMatrix(), obs.diffVerticalDeflectionYe());
			}
			
			if (obs.getColInJacobiMatrixFromOrientation() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromOrientation());
				aRows.set(row, obs.getColInJacobiMatrixFromOrientation(), obs.diffOri());
			}
				
			if (obs.getColInJacobiMatrixFromAdd() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromAdd());
				aRows.set(row, obs.getColInJacobiMatrixFromAdd(), obs.diffAdd());
			}
				
			if (obs.getColInJacobiMatrixFromScale() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromScale());
				aRows.set(row, obs.getColInJacobiMatrixFromScale(), obs.diffScale());
			}
				
			if (obs.getColInJacobiMatrixFromRefCoeff() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromRefCoeff());
				aRows.set(row, obs.getColInJacobiMatrixFromRefCoeff(), obs.diffRefCoeff());
			}
			
			if (obs.getColInJacobiMatrixFromRotationX() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromRotationX());
				aRows.set(row, obs.getColInJacobiMatrixFromRotationX(), obs.diffRotX());
			}
			
			if (obs.getColInJacobiMatrixFromRotationY() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromRotationY());
				aRows.set(row, obs.getColInJacobiMatrixFromRotationY(), obs.diffRotY());
			}
			
			if (obs.getColInJacobiMatrixFromRotationZ() >= 0) {
				colums.add(obs.getColInJacobiMatrixFromRotationZ());
				aRows.set(row, obs.getColInJacobiMatrixFromRotationZ(), obs.diffRotZ());
			}
		}
		
		Matrix subR = new DenseMatrix(dim,dim);
		
		// Bestimme Qvv
		
		// Berechne A*Qxx und Q*Qxx*A'
		for (int dc=0; dc<dim; dc++) {
			for (Integer col : colums) {
//			for (int c=0; c<colums.size(); c++) {
//				int col = colums.get(c);
				double aqxx = 0.0;
				for (Integer row : colums) {
//				for (int r=0; r<colums.size(); r++) {
//					int row = colums.get(r);
					aqxx += aRows.get(dc, row) * this.Qxx.get(row, col);
				}
				
				for (int dr=0; dr<dim; dr++) {
					subR.set(dr, dc, subR.get(dr, dc) + aqxx * aRows.get(dr, col));
				}
			}
		}
		// subRP == Q_ll; Drehe Vorzeichen, da Q_vv = Qll - Q_ll --> Mit Qll als Diagonalmatrix entspricht Q_vv = -Q_ll fuer i != j
		subR = subR.scale(-1.0);
		
		// Berechne R = Qvv*P
		double rr = 0;
		for (int i=0; i<dim; i++) {
			Observation obs = observations.get(i);
			double qll  = obs.getStdApriori()*obs.getStdApriori();
			double q_ll = Math.abs(subR.get(i,i)); // Negativ, da Vorzeichen gedreht
			obs.setStd(Math.sqrt(q_ll));
			double qvv = qll - q_ll;
			//Qll(a-post) kann aus num. gruenden mal groesser als Qll(aprio) werden
			//In diesem Fall wird die Diagonale Qvv negativ, was nicht zulassig ist
			//Liegen Qll(a-post) und Qll(aprio) sehr dich beisammen, wird Qvv sehr klein
			//dies kann zu einer Verzerrung von R führen.
			qvv = Math.abs(qvv)<Constant.EPS || qvv < 0 ? 0.0:qvv;
			subR.set(i,i, qvv);
			
			for (int j=0; j<dim; j++) {
				// R
				double r = 1.0/qll * subR.get(j,i);
				subR.set(j,i, r);
				if (i==j) {
					obs.setRedundancy(r);
					rr += r;
				}
 
				// P*R
				//subRP.set(i,j, 1.0/qll * subRP.get(i,j));
			}
		}
		
//		// Berechne RP = PQvvP
//		for (int i=0; i<dim; i++) {
//			Observation obs = observations.get(i);
//			double qll = obs.getStdApriori()*obs.getStdApriori();
//				
//			for (int j=0; j<dim; j++) {
//				subRP.set(i,j, 1.0/qll * subRP.get(i,j));
//			}
//		}
		
		if (isGNSS) 
			((GNSSBaseline)observation).setBaselineRedundancyMatrix(subR);
		
		if (rr > SQRT_EPS)
			this.numberOfHypotesis++;

		this.degreeOfFreedom += rr;
	}
	
	private void estimateRobustWeights() {
		double maxNV2 = Double.MIN_VALUE;
		Observation maxNVObs = null;
		Point maxNVPoint = null;
		VerticalDeflection maxNVVerticalDeflection = null;
		int maxNVPointComp = -1;
		int maxNVVerticalDeflectionComp = -1;
		int counter = 0;
		// Bestimme Beobachtung mit groesster NV
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			double u   = observation.getStdApriori();
			double qll = u*u;
			double r   = observation.getRedundancy();
			double v   = this.estimationType == EstimationType.SIMULATION ? 0.0 : observation.getObservationalError();

			if (qll > 0.0 && r > SQRT_EPS) {
				double nv2 = v*v/qll/r;
				if (nv2 > maxNV2) {
					maxNV2   = nv2;
					maxNVObs = observation;
				}	
			}

			// Pruefe, ob Grenzwert fuer robustes Intervall angepasst werden sollte
			if (Math.abs(v) >= 500.0 * u)
				counter++;
		}

		// Bestimme stochastischen Punkt mit groesster NV
		for (Point point : this.pointsWithStochasticDeflection) {
			VerticalDeflection deflectionX = point.getVerticalDeflectionX();
			VerticalDeflection deflectionY = point.getVerticalDeflectionY();
			
			double vx = deflectionX.getValue0() - deflectionX.getValue();
			double vy = deflectionY.getValue0() - deflectionY.getValue();
			
			double rx = deflectionX.getRedundancy();
			double ry = deflectionY.getRedundancy();
			
			double ux = deflectionX.getStdApriori();
			double uy = deflectionY.getStdApriori();
			
			double qx = ux*ux;
			double qy = uy*uy;
			
			if (qx > 0 && rx >= SQRT_EPS) {
				double nv2 = vx*vx/qx/rx;
				if (nv2 > maxNV2) {
					maxNV2 = nv2;
					maxNVVerticalDeflection = deflectionX;
					maxNVVerticalDeflectionComp = 1;
				}	
			}
			
			if (qy > 0 && ry >= SQRT_EPS) {
				double nv2 = vy*vy/qy/ry;
				if (nv2 > maxNV2) {
					maxNV2 = nv2;
					maxNVVerticalDeflection = deflectionY;
					maxNVVerticalDeflectionComp = 2;
				}	
			}
			
			if (Math.abs(vx) >= 500.0 * ux)
				counter++;
			
			if (Math.abs(vy) > 500.0 * uy)
				counter++;
			
		}
		for (Point point : this.stochasticPoints) {
			int dim = point.getDimension();
			for (int d=0; d<dim; d++) {
				double v = 0, qll = 0, r = 0, u = 0;
				if (dim != 1) {
					if (d == 0) {
						v   = point.getX0() - point.getX();
						u   = point.getStdXApriori();
						qll = u*u;
						r   = point.getRedundancyX();
					}
					else if (d == 1) {
						v = point.getY0() - point.getY();
						u   = point.getStdYApriori();
						qll = u*u;
						r   = point.getRedundancyY();
					}
				}
				if (dim != 2 && d == dim-1) {
					v = point.getZ0() - point.getZ();
					u   = point.getStdZApriori();
					qll = u*u;
					r   = point.getRedundancyZ();
				}
				
				if (qll > 0.0 && r >= SQRT_EPS) {
					double nv2 = v*v/qll/r;
					if (nv2 > maxNV2) {
						maxNV2     = nv2;
						maxNVPoint = point;
						maxNVPointComp = d;
						
						maxNVVerticalDeflection = null;
						maxNVVerticalDeflectionComp = -1;
					}	
				}
				
				if (Math.abs(v) >= 500.0 * u)
					counter++;
			}
		}
		boolean adapteRobustBoundary = (double)counter/(double)(this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows) > 0.35;
		double c = adapteRobustBoundary ? this.robustEstimationLimit + (Math.sqrt(maxNV2) - this.robustEstimationLimit) * 0.9 : this.robustEstimationLimit;
		
		// Wenn maxNVPoint == null und maxNVDeflection == null, dann wurde eine Beobachtung mit max(NV) gefunden
		if (maxNVObs != null && maxNVPoint == null && maxNVVerticalDeflection == null) {
			double u = maxNVObs.getStdApriori();
			double r = maxNVObs.getRedundancy();
			double k = c*u*Math.sqrt(r);
			double v = this.estimationType == EstimationType.SIMULATION ? 0.0 : Math.abs(maxNVObs.getObservationalError());

			if (v >= k && k > SQRT_EPS) {
				maxNVObs.setStdApriori(u * Math.sqrt(v/k));
				if (!this.adaptedObservationUncertainties.containsKey(maxNVObs))
					this.adaptedObservationUncertainties.put(maxNVObs, u);
			}
		}
		// Wenn maxNVVerticalDeflectionComp != -1, dann wurde eine Lotabweichungskomponente mit max(NV) gefunden
		else if (maxNVVerticalDeflection != null && maxNVVerticalDeflectionComp >= 0) {
			// X-Wert
			double u = maxNVVerticalDeflection.getStdApriori();
			double r = maxNVVerticalDeflection.getRedundancy();
			double k = c*u*Math.sqrt(r);
			double v = this.estimationType == EstimationType.SIMULATION ? 0.0 : Math.abs(maxNVVerticalDeflection.getValue0() - maxNVVerticalDeflection.getValue());
			if (v >= k && k > SQRT_EPS) {
				maxNVVerticalDeflection.setStdApriori(u * Math.sqrt(v/k));
				if (!this.adaptedVerticalDeflectionUncertainties.containsKey(maxNVVerticalDeflection))
					this.adaptedVerticalDeflectionUncertainties.put(maxNVVerticalDeflection, u);
			}
		}
		// Wenn maxNVPointComp != -1, dann wurde eine Koordinatenkomponente mit max(NV) gefunden
		else if (maxNVPoint != null && maxNVPointComp >= 0) {
			int dim = maxNVPoint.getDimension();
			Double u[] = new Double[dim];
			
			for (int d=0; d<dim; d++) {
				double v = 0, k = 0, r = 0;
				if (d == dim-1 && dim != 2) {
					u[d] = maxNVPoint.getStdZApriori();
					if (maxNVPointComp == d) {
						r = maxNVPoint.getRedundancyZ();
						k = c*u[d]*Math.sqrt(r);
						v = this.estimationType == EstimationType.SIMULATION ? 0.0 : Math.abs(maxNVPoint.getZ0() - maxNVPoint.getZ());
						if (v >= k && k > SQRT_EPS) {
							maxNVPoint.setStdZApriori(u[d] * Math.sqrt(v/k));
							if (!this.adaptedPointUncertainties.containsKey(maxNVPoint))
								this.adaptedPointUncertainties.put(maxNVPoint, u);
						}
					}
				}
				else if (d == 0 && dim != 1) {
					u[d] = maxNVPoint.getStdXApriori();
					if (maxNVPointComp == d) {
						r = maxNVPoint.getRedundancyX();
						k = c*u[d]*Math.sqrt(r);
						v = this.estimationType == EstimationType.SIMULATION ? 0.0 : Math.abs(maxNVPoint.getX0() - maxNVPoint.getX());
						if (v >= k && k > SQRT_EPS) {
							maxNVPoint.setStdXApriori(u[d] * Math.sqrt(v/k));
							if (!this.adaptedPointUncertainties.containsKey(maxNVPoint))
								this.adaptedPointUncertainties.put(maxNVPoint, u);
						}
					}
				}
				else if (d == 1 && dim != 1) {
					u[d] = maxNVPoint.getStdYApriori();
					if (maxNVPointComp == d) {
						r = maxNVPoint.getRedundancyY();
						k = c*u[d]*Math.sqrt(r);
						v = this.estimationType == EstimationType.SIMULATION ? 0.0 : Math.abs(maxNVPoint.getY0() - maxNVPoint.getY());
						if (v >= k && k > SQRT_EPS) {
							maxNVPoint.setStdYApriori(u[d] * Math.sqrt(v/k));
							if (!this.adaptedPointUncertainties.containsKey(maxNVPoint))
								this.adaptedPointUncertainties.put(maxNVPoint, u);
						}
					}
				}
			}
		}
	}
				
	/**
	 * erzeugt die Normalgleichungsmatrix N = A<sup>T</sup>PA <em>direkt</em>, d.h. ohne
	 * das explizite Aufstellen von A und P
	 * return NEQ
	 */
	public NormalEquationSystem createNormalEquation() {
		int numberOfStrainEquations  = 0; // Anzahl der zusaetzlichen Bedingungsgleichungen zur bestimmung der Strain-Parameter 

		if (this.freeNetwork && this.congruenceAnalysis) {
			for (CongruenceAnalysisGroup tieGroup : this.congruenceAnalysisGroup) {
				StrainAnalysisEquations strainAnalysisEquations = tieGroup.getStrainAnalysisEquations();
				int nou = strainAnalysisEquations.numberOfParameters();
				int nor = strainAnalysisEquations.numberOfRestrictions();
				int not = tieGroup.size(true);
				int dim = tieGroup.getDimension();
				
				if (strainAnalysisEquations.hasUnconstraintParameters() && not * dim + nor >= nou) {
					numberOfStrainEquations  += not * dim + nor;
				}
			}
		}
		
		UpperSymmPackMatrix N = new UpperSymmPackMatrix( this.numberOfUnknownParameters + this.rankDefect.getDefect() + numberOfStrainEquations);
		DenseVector n = new DenseVector( N.numRows() );
		
		if (this.estimationType == EstimationType.L1NORM) {
			this.estimateRobustWeights();
		}
		
		for (int u=0; u<this.unknownParameters.size(); u++) {
			if (this.interrupt)
				return null;
			
			UnknownParameter unknownParameterAT = this.unknownParameters.get(u);
			ObservationGroup observationGroupAT = unknownParameterAT.getObservations();
			int dimAT = 1;
			switch (unknownParameterAT.getParameterType()) {
			case POINT2D:
				dimAT = 2;
				break;
			case POINT3D:
				dimAT = 3;
				break;
			default:
				dimAT = 1;
				break;
			}
			for (int i=0; i<dimAT; i++) {
				int colAT = unknownParameterAT.getColInJacobiMatrix() + i;
				Vector aTp = new SparseVector(this.numberOfObservations, observationGroupAT.size());
				for (int j=0; j<observationGroupAT.size(); j++) {
					Observation observationAT = observationGroupAT.get(j);
					int rowAT = observationAT.getRowInJacobiMatrix();
					double at = 0.0;

					// Berechnet AT*P
					if (unknownParameterAT.getParameterType() == ParameterType.POINT1D) {
						Point p = (Point)unknownParameterAT;
						if (p.equals(observationAT.getStartPoint())) 
							at = observationAT.diffZs();
						else if (p.equals(observationAT.getEndPoint())) 
							at = observationAT.diffZe();
					}
					else if (unknownParameterAT.getParameterType() == ParameterType.POINT2D) {
						Point p = (Point)unknownParameterAT;
						if (p.equals(observationAT.getStartPoint())) {
							if (i==0)
								at = observationAT.diffXs();
							else if (i==1)
								at = observationAT.diffYs();
						}
						else if (p.equals(observationAT.getEndPoint())) {
							if (i==0)
								at = observationAT.diffXe();
							else if (i==1)
								at = observationAT.diffYe();
						}
					}
					else if (unknownParameterAT.getParameterType() == ParameterType.POINT3D) {
						Point p = (Point)unknownParameterAT;
						if (p.equals(observationAT.getStartPoint())) {
							if (i==0)
								at = observationAT.diffXs();
							else if (i==1)
								at = observationAT.diffYs();
							else if (i==2)
								at = observationAT.diffZs();
						}
						else if (p.equals(observationAT.getEndPoint())) {
							if (i==0)
								at = observationAT.diffXe();
							else if (i==1)
								at = observationAT.diffYe();
							else if (i==2)
								at = observationAT.diffZe();
						}
					}

					else if (unknownParameterAT.getParameterType() == ParameterType.VERTICAL_DEFLECTION_X) {
						VerticalDeflectionX deflection = (VerticalDeflectionX)unknownParameterAT;
						Point p = deflection.getPoint();
						if (p.equals(observationAT.getStartPoint()))
							at = observationAT.diffVerticalDeflectionXs();
						else if (p.equals(observationAT.getEndPoint()))
							at = observationAT.diffVerticalDeflectionXe();
					}
					else if (unknownParameterAT.getParameterType() == ParameterType.VERTICAL_DEFLECTION_Y) {
						VerticalDeflectionY deflection = (VerticalDeflectionY)unknownParameterAT;
						Point p = deflection.getPoint();
						if (p.equals(observationAT.getStartPoint()))
							at = observationAT.diffVerticalDeflectionYs();
						else if (p.equals(observationAT.getEndPoint()))
							at = observationAT.diffVerticalDeflectionYe();
					}
					else if (unknownParameterAT.getParameterType() == ParameterType.ORIENTATION)
						at = observationAT.diffOri();
					else if (unknownParameterAT.getParameterType() == ParameterType.ZERO_POINT_OFFSET)
						at = observationAT.diffAdd();
					else if (unknownParameterAT.getParameterType() == ParameterType.SCALE) 
						at = observationAT.diffScale();
					else if (unknownParameterAT.getParameterType() == ParameterType.REFRACTION_INDEX)
						at = observationAT.diffRefCoeff();
					else if (unknownParameterAT.getParameterType() == ParameterType.ROTATION_X)
						at = observationAT.diffRotX();
					else if (unknownParameterAT.getParameterType() == ParameterType.ROTATION_Y)
						at = observationAT.diffRotY();
					else if (unknownParameterAT.getParameterType() == ParameterType.ROTATION_Z)
						at = observationAT.diffRotZ();

					// Zeile aT*p bestimmen
					double atp = at / (observationAT.getStdApriori() * observationAT.getStdApriori());
					aTp.set(rowAT, atp);
					// Absolutgliedvektor bestimmen
					n.add(colAT, atp * observationAT.getObservationalError());
					// Hauptdiagonalelement aT*p*a
					N.add(colAT, colAT, atp * at);
				}

				for (int uu=u; uu<this.unknownParameters.size(); uu++) {
					UnknownParameter unknownParameterA = this.unknownParameters.get(uu);
					ObservationGroup observationGroupA = unknownParameterA.getObservations();

					int dimA = 1;
					switch (unknownParameterA.getParameterType()) {
					case POINT2D:
						dimA = 2;
						break;
					case POINT3D:
						dimA = 3;
						break;
					default:
						dimA = 1;
						break;
					}
					for (int ii=uu==u?i+1:0; ii<dimA; ii++) {
						int colA = unknownParameterA.getColInJacobiMatrix() + ii;

						for (int jj=0; jj<observationGroupA.size(); jj++) {
							Observation observationA = observationGroupA.get(jj);
							int rowA = observationA.getRowInJacobiMatrix();
							// skip zero multiplications
							if (aTp.get(rowA) == 0)
								continue;
							
							double a = 0.0;
							// Berechnte Normalgleichung N=AT*P*A
							if (unknownParameterA.getParameterType() == ParameterType.POINT1D) {
								Point p = (Point)unknownParameterA;
								
								if (p.equals(observationA.getStartPoint())) 
									a = observationA.diffZs();
								else if (p.equals(observationA.getEndPoint())) 
									a = observationA.diffZe();
							}
							else if (unknownParameterA.getParameterType() == ParameterType.POINT2D) {
								Point p = (Point)unknownParameterA;
								if (p.equals(observationA.getStartPoint())) {
									if (ii==0)
										a = observationA.diffXs();
									else if (ii==1)
										a = observationA.diffYs();

								}
								else if (p.equals(observationA.getEndPoint())) {
									if (ii==0)
										a = observationA.diffXe();
									else if (ii==1)
										a = observationA.diffYe();
								}
							}
							else if (unknownParameterA.getParameterType() == ParameterType.POINT3D) {
								Point p = (Point)unknownParameterA;
								if (p.equals(observationA.getStartPoint())) {
									if (ii==0)
										a = observationA.diffXs();
									else if (ii==1)
										a = observationA.diffYs();
									else if (ii==2)
										a = observationA.diffZs();
								}
								else if (p.equals(observationA.getEndPoint())) {
									if (ii==0)
										a = observationA.diffXe();
									else if (ii==1)
										a = observationA.diffYe();
									else if (ii==2)
										a = observationA.diffZe();
								}
							}

							else if (unknownParameterA.getParameterType() == ParameterType.VERTICAL_DEFLECTION_X) {
								VerticalDeflectionX deflection = (VerticalDeflectionX)unknownParameterA;
								Point p = deflection.getPoint();
								if (p.equals(observationA.getStartPoint()))
									a = observationA.diffVerticalDeflectionXs();
								else if (p.equals(observationA.getEndPoint()))
									a = observationA.diffVerticalDeflectionXe();
							}
							else if (unknownParameterA.getParameterType() == ParameterType.VERTICAL_DEFLECTION_Y) {
								VerticalDeflectionY deflection = (VerticalDeflectionY)unknownParameterA;
								Point p = deflection.getPoint();
								if (p.equals(observationA.getStartPoint()))
									a = observationA.diffVerticalDeflectionYs();
								else if (p.equals(observationA.getEndPoint()))
									a = observationA.diffVerticalDeflectionYe();
							}
							else if (unknownParameterA.getParameterType() == ParameterType.ORIENTATION)
								a = observationA.diffOri();
							else if (unknownParameterA.getParameterType() == ParameterType.ZERO_POINT_OFFSET)
								a = observationA.diffAdd();
							else if (unknownParameterA.getParameterType() == ParameterType.SCALE)
								a = observationA.diffScale();
							else if (unknownParameterA.getParameterType() == ParameterType.REFRACTION_INDEX)
								a = observationA.diffRefCoeff();
							else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_X)
								a = observationA.diffRotX();
							else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_Y)
								a = observationA.diffRotY();
							else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_Z)
								a = observationA.diffRotZ();
							// Berechnung von N = ATP*A 
							N.add(colAT, colA, aTp.get(rowA)*a);
						}
					}
				}
			}
		}

		// Fuege stochastische Lotabweichungen hinzu
		if (this.pointsWithStochasticDeflection != null && !this.pointsWithStochasticDeflection.isEmpty()) {
			for (Point point : this.pointsWithStochasticDeflection) {		
				VerticalDeflection deflectionX = point.getVerticalDeflectionX();
				int col = deflectionX.getColInJacobiMatrix();
				double qll = deflectionX.getStdApriori() * deflectionX.getStdApriori();
				n.add(col, (deflectionX.getValue0()-deflectionX.getValue())/qll);
				N.add(col, col, 1.0/qll);
				
				VerticalDeflection deflectionY = point.getVerticalDeflectionY();
				col = deflectionY.getColInJacobiMatrix();
				qll = deflectionY.getStdApriori() * deflectionY.getStdApriori();
				n.add(col, (deflectionY.getValue0()-deflectionY.getValue())/qll);
				N.add(col, col, 1.0/qll);
			}
		}
		
		// Fuege stochastische Anschlusspunkte hinzu
		if (this.stochasticPoints != null && !this.stochasticPoints.isEmpty()) {
			for (Point point : this.stochasticPoints) {
				int col = point.getColInJacobiMatrix();
				if (point.getDimension() != 1) {
					double qll = point.getStdXApriori()*point.getStdXApriori();
					n.add(col, (point.getX0()-point.getX())/qll);
					N.add(col, col++, 1.0/qll);

					qll = point.getStdYApriori()*point.getStdYApriori();
					n.add(col, (point.getY0()-point.getY())/qll);
					N.add(col, col++, 1.0/qll);
				}
				if (point.getDimension() != 2) {
					double qll = point.getStdZApriori()*point.getStdZApriori();
					n.add(col, (point.getZ0()-point.getZ())/qll);
					N.add(col, col, 1.0/qll);
				}
			}
		}

		if (this.freeNetwork && this.datumPoints != null && !this.datumPoints.isEmpty()) {			
			// Schwerpunkt bestimmen
			double x0=0, y0=0, z0=0;
			int nx=0, ny=0, nz=0;
			for (int i=0; i<this.datumPoints.size(); i++) {
				Point p = this.datumPoints.get(i);
				int dim = p.getDimension();
				if (dim != 1) {
					x0 += p.getX();
					y0 += p.getY();
					nx++;
					ny++;
				}
				if (dim != 2) {
					z0 += p.getZ();
					nz++;
				}
			}
			x0 = nx > 0 ? x0 / nx : 0.0;
			y0 = ny > 0 ? y0 / ny : 0.0;
			z0 = nz > 0 ? z0 / nz : 0.0;
			// Schwerpunkt aller Datumspunkte 1D+2D+3D
			Point centerPoint3D = new Point3D("c3D", x0, y0, z0);
			int row = N.numRows() - this.rankDefect.getDefect() - numberOfStrainEquations;
			// Positionen in der BedingungsMatrix
			int defectRow = 0;
			int tx   = this.rankDefect.estimateTranslationX()?defectRow++ : -1;
			int ty   = this.rankDefect.estimateTranslationY()?defectRow++ : -1;
			int tz   = this.rankDefect.estimateTranslationZ()?defectRow++ : -1;
			int rx   = this.rankDefect.estimateRotationX()?defectRow++    : -1;
			int ry   = this.rankDefect.estimateRotationY()?defectRow++    : -1;
			int rz   = this.rankDefect.estimateRotationZ()?defectRow++    : -1;
			int sx   = this.rankDefect.estimateShearX()?defectRow++       : -1;
			int sy   = this.rankDefect.estimateShearY()?defectRow++       : -1;
			int sz   = this.rankDefect.estimateShearZ()?defectRow++       : -1;
			int mx   = this.rankDefect.estimateScaleX()?defectRow++       : -1;
			int my   = this.rankDefect.estimateScaleY()?defectRow++       : -1;
			int mz   = this.rankDefect.estimateScaleZ()?defectRow++       : -1;
			int mxy  = this.rankDefect.estimateScaleXY()?defectRow++      : -1;
			int mxyz = this.rankDefect.estimateScaleXYZ()?defectRow++     : -1;

			if (mxyz >= 0) {
				mx  = mxyz;
				my  = mxyz;
				mz  = mxyz;
				mxy = mxyz;
			}
			else if (mxy >= 0) {
				mx  = mxy;
				my  = mxy;
			}

			// Summe der quadrierten Wert einer Spalte in der Bedingunsmatrix
			double normColumn[] = new double[this.rankDefect.getDefect()];
			
			for (int i=0; i<this.datumPoints.size(); i++) {
				Point point = this.datumPoints.get(i);
				int dim = point.getDimension();
				int col = point.getColInJacobiMatrix();
				
				// Translation und Maßstab
				// 1D und 3D (Hoehe)
				if (dim != 2) {
					if (tz >= 0) {
						N.set(col+dim-1, row+tz, 1.0);
						normColumn[tz] += 1.0; 
					}
					if (mz >= 0) {
						double z = point.getZ() - centerPoint3D.getZ();
						N.set(col+dim-1, row+mz, z );	
						normColumn[mz] += z*z;
					}
				}
				
				// 2D und 3D (Lage)
				if (dim != 1) {
					if (tx >= 0) {
						N.set(col+0, row+tx, 1.0);
						normColumn[tx] += 1.0;
					}
					if (ty >= 0) {
						N.set(col+1, row+ty, 1.0);
						normColumn[ty] += 1.0;
					}
				}

				// Rotation bei 1D-GNSS
				if (dim == 1) {
					double x = point.getX() - centerPoint3D.getX();
					double y = point.getY() - centerPoint3D.getY();

					if (rx >= 0) {
						N.set(col, row+rx, -y );
						normColumn[rx] += y*y;
					}
					if (ry >= 0) {
						N.set(col, row+ry,  x );
						normColumn[ry] += x*x;
					}
				}
				
				// Rotation & Maßstab
				if (dim > 1) {
					double x = point.getX() - centerPoint3D.getX();
					double y = point.getY() - centerPoint3D.getY();

					if (rz >= 0) {
						N.set(col+0, row+rz,  y );
						N.set(col+1, row+rz, -x );	
						normColumn[rz] += x*x + y*y;
					}
					
					if (sz >= 0) {
						N.set(col+0, row+sz,  y );
						N.set(col+1, row+sz,  x );	
						normColumn[sz] += x*x + y*y;
					}
					
					if (mx >= 0) {
						N.set(col+0, row+mx, x );
						normColumn[mx] += x*x;
					}
					
					if (my >= 0) {
						N.set(col+1, row+my, y );
						normColumn[my] += y*y;
					}
					
					if (dim == 3) {
						double z = point.getZ() - centerPoint3D.getZ();
						if (rx >= 0) {
							N.set(col+1, row+rx,  z );
							N.set(col+2, row+rx, -y );
							normColumn[rx] += z*z + y*y;
						}
						
						if (ry >= 0) {
							N.set(col+0, row+ry, -z );
							N.set(col+2, row+ry,  x );
							normColumn[ry] += z*z + x*x;
						}
						
						if (sx >= 0) {
							N.set(col+1, row+sx,  z );
							N.set(col+2, row+sx,  y );
							normColumn[sx] += z*z + y*y;
						}
						
						if (sy >= 0) {
							N.set(col+0, row+sy,  z );
							N.set(col+2, row+sy,  x );
							normColumn[sy] += z*z + x*x;
						}
					}
				}
			}	
								
			// Normieren der Spalten
			for (int i=0; i<this.datumPoints.size(); i++) {
				Point point = this.datumPoints.get(i);
				int dim = point.getDimension();
				int col = point.getColInJacobiMatrix();
				
				// Translation und Maßstab
				// 1D und 3D (Hoehe)
				if (dim != 2) {
					if (tz >= 0) {
						N.set(col+dim-1, row+tz, N.get(col+dim-1, row+tz)/Math.sqrt(normColumn[tz]));
					}
					if (mz >= 0) {
						N.set(col+dim-1, row+mz, N.get(col+dim-1, row+mz)/Math.sqrt(normColumn[mz]));	
					}
				}
				
				// 2D und 3D (Lage)
				if (dim != 1) {
					if (tx >= 0) {
						N.set(col+0, row+tx, N.get(col+0, row+tx)/Math.sqrt(normColumn[tx]));
					}
					if (ty >= 0) {
						N.set(col+1, row+ty, N.get(col+1, row+ty)/Math.sqrt(normColumn[ty]));
					}
				}

				// Rotation & Maßstab
				if (dim > 1) {
					if (rz >= 0) {
						N.set(col+0, row+rz, N.get(col+0, row+rz)/Math.sqrt(normColumn[rz]) );
						N.set(col+1, row+rz, N.get(col+1, row+rz)/Math.sqrt(normColumn[rz]) );	
					}
					
					if (sz >= 0) {
						N.set(col+0, row+sz, N.get(col+0, row+sz)/Math.sqrt(normColumn[sz]) );
						N.set(col+1, row+sz, N.get(col+1, row+sz)/Math.sqrt(normColumn[sz]) );	
					}
					
					if (mx >= 0) {
						N.set(col+0, row+mx, N.get(col+0, row+mx)/Math.sqrt(normColumn[mx]) );
					}
					
					if (my >= 0) {
						N.set(col+1, row+my, N.get(col+1, row+my)/Math.sqrt(normColumn[my]) );
					}
					
					if (dim == 3) {
						if (rx >= 0) {
							N.set(col+1, row+rx, N.get(col+1, row+rx)/Math.sqrt(normColumn[rx]) );
							N.set(col+2, row+rx, N.get(col+2, row+rx)/Math.sqrt(normColumn[rx]) );
						}
						
						if (ry >= 0) {
							N.set(col+0, row+ry, N.get(col+0, row+ry)/Math.sqrt(normColumn[ry]) );
							N.set(col+2, row+ry, N.get(col+2, row+ry)/Math.sqrt(normColumn[ry]) );
						}
						
						if (sx >= 0) {
							N.set(col+1, row+sx, N.get(col+1, row+sx)/Math.sqrt(normColumn[sx]) );
							N.set(col+2, row+sx, N.get(col+2, row+sx)/Math.sqrt(normColumn[sx]) );
						}
						
						if (sy >= 0) {
							N.set(col+0, row+sy, N.get(col+0, row+sy)/Math.sqrt(normColumn[sy]) );
							N.set(col+2, row+sy, N.get(col+2, row+sy)/Math.sqrt(normColumn[sy]) );
						}
					}
				}	
			}
	
			if (this.congruenceAnalysis && numberOfStrainEquations > 0) {
				row = N.numRows() - numberOfStrainEquations;

				for (CongruenceAnalysisGroup tieGroup : this.congruenceAnalysisGroup) {
					StrainAnalysisEquations strainAnalysisEquations = tieGroup.getStrainAnalysisEquations();
					int nou = strainAnalysisEquations.numberOfParameters();
					int nor = strainAnalysisEquations.numberOfRestrictions();
					int not = tieGroup.size(true);
					int dim = tieGroup.getDimension();
					Equation equations[] = StrainAnalysisEquations.getEquations(dim);
					CoordinateComponent components[] = StrainAnalysisEquations.getCoordinateComponents(dim);
					
					if (strainAnalysisEquations.hasUnconstraintParameters() && not * dim + nor >= nou) {
					
						// Ableitungen nach den Restriktionen zum fix. von Parametern
						for (int resIdx = 0; resIdx < nor; resIdx++) {
							RestrictionType restriction = strainAnalysisEquations.getRestriction(resIdx);
							for (int parIdx=0; parIdx < nou; parIdx++) {
								StrainParameter parameter = strainAnalysisEquations.get( parIdx );
								int colInA = parameter.getColInJacobiMatrix();

								N.set(colInA, row + resIdx, strainAnalysisEquations.diff(parameter, restriction));
								n.set(row + resIdx, -strainAnalysisEquations.getContradiction(restriction));
							}
						}
						row += nor;
						
						for (int i=0; i<not; i++) {
							CongruenceAnalysisPointPair tie = tieGroup.get(i, true);
							Point p0 = tie.getStartPoint();
							Point p1 = tie.getEndPoint();

							for (int eqIdx = 0; eqIdx < equations.length; eqIdx++) {
								Equation equation = equations[eqIdx];
								
								// Ableitungen nach den Zusatzparametern
								for (int parIdx=0; parIdx < nou; parIdx++) {
									StrainParameter parameter = strainAnalysisEquations.get( parIdx );
									int colInA = parameter.getColInJacobiMatrix();
									
									N.set(colInA, row, strainAnalysisEquations.diff(parameter, p0, equation));
									n.set(row, -strainAnalysisEquations.getContradiction(p0, p1, equation));
								}
																
								// Ableitungen nach den Punkten
								for (int coordIdx = 0; coordIdx < components.length; coordIdx++) {
									CoordinateComponent component = components[coordIdx];
									Point p;
									
									if (component == CoordinateComponent.X1 || component == CoordinateComponent.Y1 || component == CoordinateComponent.Z1)
										p = p0;
									else
										p = p1;
									
									int colInA = p.getColInJacobiMatrix();
									
									if (component == CoordinateComponent.Y1 || component == CoordinateComponent.Y2)
										colInA += 1;
									
									if (component == CoordinateComponent.Z1 || component == CoordinateComponent.Z2)
										colInA += p.getDimension() - 1;

									N.set(colInA, row, strainAnalysisEquations.diff(p0, p1, component, equation));
								}
								row++;
							}
						}
					}
				}
			}
			
		}
//		// Unnoetig, da Pseudobeobachtung und berechneter Wert identisch sind, d.h., 
		// observation.setValueApriori(observation.getValueAposteriori()); in SQLAdjustmentManager  
		if (this.estimationType == EstimationType.SIMULATION)
			n.zero();
		
		return new NormalEquationSystem(N, n);
	}
	
	/**
	 * Liefert die Kofaktormatrix der Ausgleichung.
	 * !!! Auchtung, die Matrix wird in verschiedenen Schritten überschrieben und enthaelt am Ende der
	 * Berechnung nicht mehr Qxx. Die Methode sollte nur von AdjustmentResultWritable Klassen aufgerufen 
	 * werrden !!!
	 * @return dispersion
	 */
	public Matrix getCofactorMatrix() {
		return this.Qxx;
	}
	
	/**
	 * Zeigt an, ob Iteration an der naechst moeglichen Stelle abzubrechen ist
	 */
	public boolean isInterrupted() {
		return this.interrupt;
	}
	
	/**
	 * Bricht Iteration an der naechst moeglichen Stelle ab
	 */
	public void interrupt() {
		this.interrupt = true;
	}
	
	/**
	 * Setz die Varianzkomponenten der einzelnen Gruppen auf <em>Null</em> zurueck
	 */
	private void resetVarianceComponents() {
		Map<VarianceComponentType, VarianceComponent> varianceComponents = new LinkedHashMap<VarianceComponentType, VarianceComponent>();
		for (VarianceComponent varianceEstimation : this.varianceComponents.values())
			varianceComponents.put(varianceEstimation.getVarianceComponentType(), new VarianceComponent(varianceEstimation.getVarianceComponentType()));
		this.varianceComponents = varianceComponents;
	}
	
	/**
	 * Berechnet das Ausgleichungsmodell iterativ 
	 * und gibt im Erfolgsfall true zurueck
	 * 
	 * @return estimateStatus
	 */
	public EstimationStateType estimateModel() {
		boolean applyUnscentedTransformation = this.estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION || this.estimationType == EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION;
		this.maxDx = Double.MIN_VALUE;
		this.currentMaxAbsDx = this.maxDx;
		this.numberOfHypotesis = 0;
		this.calculateStochasticParameters = false;
		this.currentEstimationStatus = EstimationStateType.BUSY;
    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);

		int runs = this.maximalNumberOfIterations-1;
		boolean isEstimated = false, estimateCompleteModel = false, isConverge = true;
				
		if (this.maximalNumberOfIterations == 0) {
			estimateCompleteModel = isEstimated = true;
		}
		if (this.estimationType == EstimationType.SIMULATION) {
			estimateCompleteModel = isEstimated = true;
		}
		
		// Fuege Lotabweichungen als Unbekannte zum Modell hinzu
		this.addVerticalDeflectionToModel();
		//Fuehre stochastische Anschlusspunkte und stochastische Lotparameter ins Modell ein
		this.addStochasticPointsAndStochasticDeflectionToModel();

		// ermittle Rank-Defekt anhand der Beobachtungen
		this.freeNetwork = this.freeNetwork || (this.referencePoints == null || this.referencePoints.isEmpty()) && (this.stochasticPoints == null || this.stochasticPoints.isEmpty());

		if (this.freeNetwork) {
			if (this.datumPoints == null || this.datumPoints.isEmpty()) {
				this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
				this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
				return this.currentEstimationStatus;
			}
			this.detectRankDefect();
		}
	
		// Fuehre die Strain-Parameter als mgl. Zusatzparameter mit ins Modell ein
		if (this.freeNetwork && this.congruenceAnalysis)
			this.addStrainParametersToModel();

		// Setze VC auf 1 fuer alle Typen
		this.resetVarianceComponents();
		
		// Sortiere die unbekannten Parameter so, dass die Zusatzparameter am Ende stehen
		this.unknownParameters.resortParameters();

		try {
			double lastStepSignum = 0.0;
			int numObs = this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows;
			int numberOfEstimationSteps = this.estimationType == EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION ? 2 * numObs + 1 : 
				this.estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION ? numObs + 2 : 1;

			double alpha2  = this.alphaUT * this.alphaUT;
			double weight0 = this.weightZero;
			double weighti = (1.0 - weight0) / (double)(numberOfEstimationSteps - 1.0);
	
			Vector SigmaUT = null, xUT = null, vUT = null;
			Matrix solutionVectors = null;

			if (applyUnscentedTransformation) {
				xUT = new DenseVector(this.numberOfUnknownParameters);
				vUT = new DenseVector(numObs);
				solutionVectors = new DenseMatrix(this.numberOfUnknownParameters, numberOfEstimationSteps);
				
				if (this.estimationType == EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION) {
					SigmaUT = new DenseVector(1);
					SigmaUT.set(0, Math.sqrt( 0.5 / weighti ));
				}
				else if (this.estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION) {
					SigmaUT = new DenseVector(numObs);
					if (weight0 < 0 || weight0 >= 1)
						throw new IllegalArgumentException("Error, zero-weight is out of range. If SUT is applied, valid values are 0 <= w0 < 1! " + weight0);
				}

				weight0 = weight0 / alpha2 + (1.0 - 1.0 / alpha2);
				weighti = weighti / alpha2;
			}
			
			for (int estimationStep = 0; estimationStep < numberOfEstimationSteps; estimationStep++) {
				// Reset aller Iterationseinstellungen
				this.maxDx = Double.MIN_VALUE;
				this.currentMaxAbsDx = this.maxDx;
				runs = this.maximalNumberOfIterations - 1;
				isEstimated = false;
				estimateCompleteModel = false;
				isConverge = true;
				
				if (applyUnscentedTransformation) {
					this.currentEstimationStatus = EstimationStateType.UNSCENTED_TRANSFORMATION_STEP;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), numberOfEstimationSteps, estimationStep+1);
					
					// Reset der unbekannten Datumsparameter
					if (numberOfEstimationSteps > 1 && estimationStep != (numberOfEstimationSteps - 1))
						this.resetDatumPoints();
					
					if (this.estimationType == EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION) {
						double signum = estimationStep == (numberOfEstimationSteps - 1) ? 0.0 : estimationStep < numObs ? +1.0 : -1.0;
						int currentObsIdx = estimationStep % numObs;

						// Entferne letzte UT-Modifizierung
						if (estimationStep > 0) {
							int lastObsIdx = currentObsIdx - 1;
							lastObsIdx = lastObsIdx < 0 ? numObs - 1 : lastObsIdx;
							this.prepareModifiedUnscentedTransformationObservation(lastObsIdx, -lastStepSignum * SigmaUT.get(0));
						}

						// UT-Modifizierung des aktuellen Schritts
						if (estimationStep < numberOfEstimationSteps - 1) {
							this.prepareModifiedUnscentedTransformationObservation(currentObsIdx, signum * SigmaUT.get(0));
						}
						lastStepSignum = signum;	
					}
					else if (this.estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION) {
						this.prepareSphericalSimplexUnscentedTransformationObservation(estimationStep, SigmaUT, weighti);
					}
				}
				
				do {
					this.maxDx = Double.MIN_VALUE;
					this.numberOfHypotesis = 0;
					this.iterationStep = this.maximalNumberOfIterations-runs;
					this.currentEstimationStatus = EstimationStateType.ITERATE;

					this.change.firePropertyChange(this.currentEstimationStatus.name(), this.maximalNumberOfIterations, this.iterationStep);

					// erzeuge Normalgleichung
					this.applySphericalVerticalDeflections();
					NormalEquationSystem neq = this.createNormalEquation();
					this.resetVarianceComponents();

					if (this.interrupt || neq == null) {
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						this.interrupt = false;
						return this.currentEstimationStatus;
					}
					DenseVector n = neq.getVector();
					UpperSymmPackMatrix N = neq.getMatrix();
					Vector dx = n;

					estimateCompleteModel = isEstimated;				
					try {
						if ( (estimateCompleteModel && estimationStep == (numberOfEstimationSteps - 1)) || this.estimationType == EstimationType.L1NORM) {
							this.calculateStochasticParameters = (this.estimationType != EstimationType.L1NORM && estimateCompleteModel);
							// Bestimme die Parameter der ausseren Genauigkeit und
							// ueberschreibe die Normalgleichung und den Absolutgliedvektor
							// in-situ, sodass N == Qxx und n == dx am Ende ist.
							// Wenn UT gewaehlt, wird Qxx seperat aus den Einzelloesungen bestimmt
							if (this.estimationType != EstimationType.L1NORM) {
								this.currentEstimationStatus = EstimationStateType.INVERT_NORMAL_EQUATION_MATRIX;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
							}
							this.estimateFactorsForOutherAccracy(N, n);

							if (this.calculateStochasticParameters) {
								this.currentEstimationStatus = EstimationStateType.ESTIAMTE_STOCHASTIC_PARAMETERS;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
							}
						}
						else
							// Loese Nx=n und ueberschreibe n durch die Loesung x
							MathExtension.solve(N, n, false);
						
						n = null;
						N = null;
					}
					catch (MatrixSingularException | MatrixNotSPDException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
						this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						return this.currentEstimationStatus;
					}
					catch (Exception e) {
						e.printStackTrace();
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						return this.currentEstimationStatus;
					}
					

					if (applyUnscentedTransformation) {
						if (estimateCompleteModel) {
							this.addUnscentedTransformationSolution(dx, xUT, vUT, solutionVectors, estimationStep, estimationStep < (numberOfEstimationSteps - 1) ? weighti : weight0);
						}
						
						// Letzter Durchlauf der UT
						// Bestimme Parameterupdate dx und Kovarianzmatrix Qxx
						if (estimateCompleteModel && estimationStep > 0 && estimationStep == (numberOfEstimationSteps - 1)) {
							this.estimateUnscentedTransformationParameterUpdateAndCovarianceMatrix(dx, xUT, solutionVectors, weighti, weight0 + (1.0 - alpha2 + this.betaUT));
						}
					}

					this.updateModel(dx, vUT, estimateCompleteModel && estimationStep == (numberOfEstimationSteps - 1));
					dx = null;
					vUT = null;

					if (this.interrupt) {
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						this.interrupt = false;
						return this.currentEstimationStatus;
					}

					if (Double.isInfinite(this.maxDx) || Double.isNaN(this.maxDx)) {
						this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						return this.currentEstimationStatus;
					}
					else if (this.maxDx <= SQRT_EPS && runs > 0) {
						isEstimated = true;
						this.currentEstimationStatus = EstimationStateType.CONVERGENCE;
						if (!applyUnscentedTransformation)
							this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
					}
					else if (runs-- <= 1) {
						if (estimateCompleteModel) {
							if (this.estimationType == EstimationType.L1NORM) {
								this.currentEstimationStatus = EstimationStateType.ROBUST_ESTIMATION_FAILED;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
								return this.currentEstimationStatus;
							}
							else {
								this.currentEstimationStatus = EstimationStateType.NO_CONVERGENCE;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
								isConverge = false;
							}
						}
						isEstimated = true;
					}
					else {
						this.currentEstimationStatus = EstimationStateType.CONVERGENCE;
						if (!applyUnscentedTransformation)
							this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
					}

					// Sollten nur stochastische Punkte enthalten sein, ist maxDx MIN_VALUE
					if (this.maxDx > Double.MIN_VALUE)
						this.currentMaxAbsDx = this.maxDx;
					else
						this.maxDx = this.currentMaxAbsDx;
				}
				while (!estimateCompleteModel);
			}
			
			// Exportiere CoVar (sofern aktiviert), da diese danach ueberschrieben wird
			try {
				this.exportAdjustmentResults();
			} catch (NullPointerException | IllegalArgumentException | IOException e) {
				e.printStackTrace();
				this.currentEstimationStatus = EstimationStateType.EXPORT_ADJUSTMENT_RESULTS_FAILED;
				this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
				return this.currentEstimationStatus;
			}
			
			// Fuehre Hauptkomponentenanalyse durch; Qxx wird hierbei zu NULL gesetzt
			if (this.numberOfPrincipalComponents > 0)
				this.estimatePrincipalComponentAnalysis(this.numberOfPrincipalComponents);
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			this.currentEstimationStatus = EstimationStateType.OUT_OF_MEMORY;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			return this.currentEstimationStatus;
		}

		if (!isConverge) {
			this.currentEstimationStatus = EstimationStateType.NO_CONVERGENCE;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
		}
		else if (this.currentEstimationStatus.getId() == EstimationStateType.BUSY.getId() || this.calculateStochasticParameters) {
			this.currentEstimationStatus = EstimationStateType.ERROR_FREE_ESTIMATION;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxDx);
		}

		return this.currentEstimationStatus;
	}
	
	/**
	 * Liefert den maximalen (absoluten) Zuzschlag der Iteration 
	 * @return max(|DX|)
	 */
	public double getMaxAbsDx() {
		return this.currentMaxAbsDx;
	}
	
	/**
	 * Modifiziert die Beobachtung observation(index) fuer die
	 * Unscented Transformation (bzw. macht die Modifikation rueckgaengig)
	 * @param index
	 * @param signum
	 * @param scale
	 */
	private void prepareSphericalSimplexUnscentedTransformationObservation(int estimationStep, Vector SigmaUT, double weight) {
		int noo = SigmaUT.size();
		
		for (int idx=0; idx < this.numberOfObservations; idx++) {
			Observation observation = this.projectObservations.get(idx);
			int row      = observation.getRowInJacobiMatrix();
			double std   = observation.getStdApriori();
			double value = observation.getValueApriori();
			double sigmaUT = SigmaUT.get(row);
			// entferne letzte Modifikation
			value = value - std * sigmaUT;
			sigmaUT = 0;
			
			if (estimationStep < noo + 2 && row >= 0) {
//				if (row == 0) {
//					if (estimationStep == 0)
//						sigmaUT = -1.0/Math.sqrt(2.0 * weight);
//					else if (estimationStep == 1)
//						sigmaUT = +1.0/Math.sqrt(2.0 * weight);
//				}
//				if (row >= 0) {
				if (row == estimationStep - 1)
					sigmaUT = (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
				else if (row > estimationStep - 2)
					sigmaUT = -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
//				}
			}
			// modifiziere Beobachtung
			value = value + std * sigmaUT;
			observation.setValueApriori(value);
			SigmaUT.set(row, sigmaUT);
		}

		for (Point point : this.pointsWithStochasticDeflection) {
			VerticalDeflection deflectionX = point.getVerticalDeflectionX();
			VerticalDeflection deflectionY = point.getVerticalDeflectionY();

			int rowX = deflectionX.getRowInJacobiMatrix();
			int rowY = deflectionY.getRowInJacobiMatrix();
			
			double stdX   = deflectionX.getStdApriori();
			double stdY   = deflectionY.getStdApriori();
			
			double valueX = deflectionX.getValue0();
			double valueY = deflectionY.getValue0();
			
			double sigmaUTX = SigmaUT.get(rowX);
			double sigmaUTY = SigmaUT.get(rowY);
			
			// entferne letzte Modifikation in X/Y
			valueX = valueX - stdX * sigmaUTX;
			valueY = valueY - stdY * sigmaUTY;
			
			sigmaUTX = 0;
			sigmaUTY = 0;
			
			if (estimationStep < noo + 2 && rowX >= 0 && rowY >= 0) {
				if (rowX == estimationStep - 1)
					sigmaUTX = (1.0 + rowX) / Math.sqrt( ((1.0 + rowX) * (2.0 + rowX)) * weight );
				else if (rowX > estimationStep - 2)
					sigmaUTX = -1.0 / Math.sqrt( ((1.0 + rowX) * (2.0 + rowX)) * weight );
				
				if (rowY == estimationStep - 1)
					sigmaUTY = (1.0 + rowY) / Math.sqrt( ((1.0 + rowY) * (2.0 + rowY)) * weight );
				else if (rowY > estimationStep - 2)
					sigmaUTY = -1.0 / Math.sqrt( ((1.0 + rowY) * (2.0 + rowY)) * weight );
			}

			// modifiziere X/Y
			valueX = valueX + stdX * sigmaUTX;
			valueY = valueY + stdY * sigmaUTY;
			
			deflectionX.setValue0(valueX);
			deflectionY.setValue0(valueY);
			
			SigmaUT.set(rowX, sigmaUTX);
			SigmaUT.set(rowY, sigmaUTY);
		}
		
		for (Point point : this.stochasticPoints) {
			int row = point.getRowInJacobiMatrix();
			int dim = point.getDimension();
			
			if (row >= 0) {
				if (dim != 1) {
					double stdX    = point.getStdXApriori();
					double valueX  = point.getX0();
					double sigmaUTX = SigmaUT.get(row);
					// entferne letzte Modifikation in X
					valueX = valueX - stdX * sigmaUTX;
					sigmaUTX = 0;

					if (estimationStep < noo + 2) {
						if (row == estimationStep - 1)
							sigmaUTX = (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
						else if (row > estimationStep - 2)
							sigmaUTX = -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
					}
					// modifiziere X
					valueX = valueX + stdX * sigmaUTX;
					point.setX0(valueX);
					SigmaUT.set(row, sigmaUTX);
					row++;

					double stdY    = point.getStdYApriori();
					double valueY  = point.getY0();
					double sigmaUTY = SigmaUT.get(row);
					// entferne letzte Modifikation in Y
					valueY = valueY - stdY * sigmaUTY;
					sigmaUTY = 0;

					if (estimationStep < noo + 2) {
						if (row == estimationStep - 1)
							sigmaUTY = (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
						else if (row > estimationStep - 2)
							sigmaUTY = -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
					}
					// modifiziere Y
					valueY = valueY + stdY * sigmaUTY;
					point.setY0(valueY);
					SigmaUT.set(row, sigmaUTY);
					row++;
				}
				if (dim != 2) {
					double stdZ    = point.getStdZApriori();
					double valueZ  = point.getZ0();
					double sigmaUTZ = SigmaUT.get(row);
					// entferne letzte Modifikation in Z
					valueZ = valueZ - stdZ * sigmaUTZ;
					sigmaUTZ = 0;

					if (estimationStep < noo + 2) {
						if (row == estimationStep - 1)
							sigmaUTZ = (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
						else if (row > estimationStep - 2)
							sigmaUTZ = -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight );
					}
					// modifiziere Z
					valueZ = valueZ + stdZ * sigmaUTZ;
					point.setZ0(valueZ);
					SigmaUT.set(row, sigmaUTZ);
				}
			}
		}
	}
	
	/**
	 * Modifiziert die Beobachtung observation(index) fuer die
	 * Unscented Transformation (bzw. macht die Modifikation rueckgaengig)
	 * @param index
	 * @param signum
	 * @param scale
	 */
	private void prepareModifiedUnscentedTransformationObservation(int index, double scale) {
		if (index < this.numberOfObservations) {
			Observation observation = this.projectObservations.get(index);

			double std   = observation.getStdApriori();
			double value = observation.getValueApriori();

			value = value + scale * std;
			observation.setValueApriori(value);
		}
		else if (index < this.numberOfObservations + this.numberOfStochasticDeflectionRows) {
			int deflectionIdx  = index - this.numberOfObservations;
			int deflectionType = deflectionIdx % 2; // 0 == x oder 1 == y
			deflectionIdx = deflectionIdx / 2;

			Point point = this.pointsWithStochasticDeflection.get(deflectionIdx);
			
			if (deflectionType == 0) {
				VerticalDeflection deflectionX = point.getVerticalDeflectionX();

				double std   = deflectionX.getStdApriori();
				double value = deflectionX.getValue0();

				value = value + scale * std;
				deflectionX.setValue0(value);
			}
			else {
				VerticalDeflection deflectionY = point.getVerticalDeflectionY();

				double std   = deflectionY.getStdApriori();
				double value = deflectionY.getValue0();

				value = value + scale * std;
				deflectionY.setValue0(value);
			}
		}
		else if (index < this.numberOfObservations + this.numberOfStochasticDeflectionRows + this.numberOfStochasticPointRows) {
			int pointIdx = index - this.numberOfObservations - this.numberOfStochasticDeflectionRows;
			for (int i = 0, j = 0; i < this.stochasticPoints.size(); i++) {
				Point point = this.stochasticPoints.get(i);
				int dim = point.getDimension();
				
				if (pointIdx >= j + dim) {
					j += dim;
					continue;
				}

				for (int d = 0; d < dim; d++) {
					if (pointIdx == j + d) {
						if (dim != 1) {
							if (d == 0) {
								double std   = point.getStdXApriori();
								double value = point.getX0();

								value = value + scale * std;
								point.setX0(value);
							}
							else if (d == 1) {
								double std   = point.getStdYApriori();
								double value = point.getY0();

								value = value + scale * std;
								point.setY0(value);
							}
						}
						if (dim != 2 && d == dim-1) {
							double std   = point.getStdZApriori();
							double value = point.getZ0();

							value = value + scale * std;
							point.setZ0(value);
						}
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Ueberschreibt die Schaetzung mit den Naeherungswerten der Datumspunkte.
	 * Notwendig fuer die jeweilige Iteration mittels UnscentedTransformation
	 */
	private void resetDatumPoints() {
		for (Point p : this.datumPoints) {
			p.resetCoordinates();
		}
	}
	
	private void estimateUnscentedTransformationParameterUpdateAndCovarianceMatrix(Vector dx, Vector xUT, Matrix solutionVectors, double weightN, double weightC) {
		int numberOfEstimationSteps = solutionVectors.numColumns();
		this.Qxx.zero();
		
		for (int c = 0; c < this.unknownParameters.size(); c++) {
			if (this.interrupt)
				return;

			UnknownParameter unknownParameterCol = this.unknownParameters.get(c);
			int col = unknownParameterCol.getColInJacobiMatrix();
	
			int dimCol = 1;
			switch (unknownParameterCol.getParameterType()) {
			case POINT2D:
				dimCol = 2;
				break;
			case POINT3D:
				dimCol = 3;
				break;
			default:
				dimCol = 1;
				break;
			}
			
			if (unknownParameterCol instanceof Point) {
				Point point = (Point)unknownParameterCol;
				dimCol = point.getDimension();
				
				// Erzeuge aus der finalen UT-Loesung den Zuschlagsvektor dx = x - x0 -  Koordinaten
				if (dimCol != 1) {
					dx.set(col+0, xUT.get(col+0) - point.getX());
					dx.set(col+1, xUT.get(col+1) - point.getY());
				}
				if (dimCol != 2)
					dx.set(col+dimCol-1, xUT.get(col+dimCol-1) - point.getZ());
			}
			// Erzeuge aus der finalen UT-Loesung den Zuschlagsvektor - Zusazuparameter
			else if (unknownParameterCol instanceof VerticalDeflection){
				VerticalDeflection deflectionParameter = (VerticalDeflection) unknownParameterCol;
				dx.set(col, xUT.get(col) - deflectionParameter.getValue());
			}
			else if (unknownParameterCol instanceof AdditionalUnknownParameter){
				AdditionalUnknownParameter additionalUnknownParameter = (AdditionalUnknownParameter) unknownParameterCol;
				dx.set(col, xUT.get(col) - additionalUnknownParameter.getValue());
			}
			else if (unknownParameterCol instanceof StrainParameter){
				StrainParameter strainParameter = (StrainParameter) unknownParameterCol;
				dx.set(col, xUT.get(col) - strainParameter.getValue());
			}
			
			for (int dCol = 0; dCol < dimCol; dCol++) {
				for (int r = c; r < this.unknownParameters.size(); r++) {
					if (this.interrupt)
						return;

					UnknownParameter unknownParameterRow = this.unknownParameters.get(r);
					int row = unknownParameterRow.getColInJacobiMatrix();

					int dimRow = 1;
					switch (unknownParameterRow.getParameterType()) {
					case POINT2D:
						dimRow = 2;
						break;
					case POINT3D:
						dimRow = 3;
						break;
					default:
						dimRow = 1;
						break;
					}

					if (unknownParameterRow instanceof Point) {
						Point point = (Point)unknownParameterRow;
						dimRow = point.getDimension();
					}

					for (int dRow = 0; dRow < dimRow; dRow++) {
						// Laufindex des jeweiligen Spalten-/Zeilenvektors
						for (int estimationStep = 0; estimationStep < numberOfEstimationSteps; estimationStep++) {
							double weight = estimationStep == (numberOfEstimationSteps - 1) ? weightC : weightN;

							double valueCol = solutionVectors.get(col + dCol, estimationStep) - xUT.get(col + dCol);
							double valueRow = solutionVectors.get(row + dRow, estimationStep) - xUT.get(row + dRow);

							this.Qxx.set(col + dCol, row + dRow, this.Qxx.get(col + dCol, row + dRow) + valueRow * weight * valueCol);
						}
					}
				}
			}
		}
		solutionVectors = null;
		xUT = null;
	}
	
	/**
	 * Fuegt die einzelen UT-Loesungen (transformierte SIGMA-Punkte) der Matrix solutionVectors hinzu
	 * Akkumuliert die Loesungen, sodass final das Ergebnis in dxUT steht. 
	 * !!! HINWEIS: Der Vektor dxUT enthaelt den vollstaendigen Loesungsvektor !!!
	 * !!!          und NICHT den Zuschlagsvektor.                             !!!
	 * @param dX   Zuschag der letzten Iteration (theoretich ein Nullvektor)
	 * @param dxUT UT-Loesung
	 * @param solutionVectors Matrix mit allen UT-Loesungen
	 * @param solutionNumber Aktuelle UT-Loesungsnummer 
	 * @param weight UT-Gewicht
	 */
	private void addUnscentedTransformationSolution(Vector dX, Vector xUT, Vector vUT, Matrix solutionVectors, int solutionNumber, double weight) {
		for (int i=0; i<this.unknownParameters.size(); i++) {
			if (this.interrupt)
				return;
			
			UnknownParameter unknownParameter = this.unknownParameters.get(i);
			int col = unknownParameter.getColInJacobiMatrix();
			if (unknownParameter instanceof Point) {
				Point point = (Point)unknownParameter;
				int dim = point.getDimension();
				if (dim != 1) {
					double xValue = point.getX() + dX.get(col);
					xUT.set(col, xUT.get(col) + weight * xValue);
					solutionVectors.set(col, solutionNumber, xValue);
					col++;
					
					double yValue = point.getY() + dX.get(col);
					xUT.set(col, xUT.get(col) + weight * yValue);
					solutionVectors.set(col, solutionNumber, yValue);
					col++;
				}
				if (dim != 2) {
					double zValue = point.getZ() + dX.get(col);
					xUT.set(col, xUT.get(col) + weight * zValue);
					solutionVectors.set(col, solutionNumber, zValue);
					col++;
				}
			}
			else if (unknownParameter instanceof VerticalDeflection){
				VerticalDeflection deflectionParameter = (VerticalDeflection) unknownParameter;
				double value = deflectionParameter.getValue() + dX.get(col);
				xUT.set(col, xUT.get(col) + weight * value);
				solutionVectors.set(col, solutionNumber, value);
			}
			else if (unknownParameter instanceof AdditionalUnknownParameter){
				AdditionalUnknownParameter additionalUnknownParameter = (AdditionalUnknownParameter) unknownParameter;
				double value = additionalUnknownParameter.getValue() + dX.get(col);
				xUT.set(col, xUT.get(col) + weight * value);
				solutionVectors.set(col, solutionNumber, value);
			}
			else if (unknownParameter instanceof StrainParameter){
				StrainParameter strainParameter = (StrainParameter) unknownParameter;
				double value = strainParameter.getValue() + dX.get(col);
				xUT.set(col, xUT.get(col) + weight * value);
				solutionVectors.set(col, solutionNumber, value);
			}
		}

		if (vUT != null)
			vUT.add(weight, this.getObservationalErrors());
	}
	
	/**
	 * Aktualisiert die unbekannten Groessen nach der Iteration
	 * @param X Updatevektor
	 * @param updateCompleteModel 
	 */
	private void updateModel(Vector dX, Vector vUT, boolean updateCompleteModel) {
		// Bestimme Qll(a-post) fuer alle Beobachtungen, 
		// da Designamatrix hier noch verfuegbar
		this.omega = 0.0;
		this.degreeOfFreedom = 0;
		Vector vVec = null;
		if (updateCompleteModel || this.estimationType == EstimationType.L1NORM) {			
			this.addSubRedundanceAndCofactor2Observations();
			
			if (updateCompleteModel) {
				for (int i=0; i<this.numberOfObservations; i++) {
					Observation observation = this.projectObservations.get(i);
					if (this.adaptedObservationUncertainties.containsKey(observation))
						observation.setStdApriori(this.adaptedObservationUncertainties.get(observation));
				}
			}

			// Bestimme Verbesserungen der AGL (bevor Parameterupdate erfolgt)
			if (updateCompleteModel && this.estimationType != EstimationType.SIMULATION) {
				vVec = this.getCorrectionVector(dX);
			}
		}
	
		for (int i=0; i<this.unknownParameters.size(); i++) {
			if (this.interrupt)
				return;
			
			UnknownParameter unknownParameter = this.unknownParameters.get(i);
			int col = unknownParameter.getColInJacobiMatrix();
			if (unknownParameter instanceof Point) {
				Point point = (Point)unknownParameter;
				double tmpMaxDx = this.maxDx;
				int dim = point.getDimension();
				// Updaten der stochastischen Punkte unterbinden in den Iterationen
//				if (!updateCompleteModel && this.stochasticPoints.contains(point))
//					continue;

				if (dim != 1) {
					this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
					point.setX( point.getX() + dX.get(col++) );
					this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
					point.setY( point.getY() + dX.get(col++) );
				}
				if (dim != 2) {
					this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
					point.setZ( point.getZ() + dX.get(col) );
				}
				
				if (updateCompleteModel && this.stochasticPoints.contains(point))
					this.maxDx = tmpMaxDx;
				
				// Hinzufuegen der stochastischen Groessen
				if ((updateCompleteModel || this.estimationType == EstimationType.L1NORM) && point.getRowInJacobiMatrix() >= 0){
					this.addSubRedundanceAndCofactor2Point(point, updateCompleteModel);
				}
			}
			else if (unknownParameter instanceof AdditionalUnknownParameter){
				this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
				AdditionalUnknownParameter additionalUnknownParameter = (AdditionalUnknownParameter) unknownParameter;
				additionalUnknownParameter.setValue( additionalUnknownParameter.getValue() + dX.get(col) );
			}
			else if (unknownParameter instanceof StrainParameter){
				this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
				StrainParameter strainParameter = (StrainParameter) unknownParameter;
				strainParameter.setValue( strainParameter.getValue() + dX.get(col) );
			}
			else if (unknownParameter instanceof VerticalDeflection){
				// Hinzufuegen der stochastischen Groessen
				if (unknownParameter instanceof VerticalDeflectionX) {
					if ((updateCompleteModel || this.estimationType == EstimationType.L1NORM) && ((VerticalDeflectionX)unknownParameter).getRowInJacobiMatrix() >= 0) {
						Point point = ((VerticalDeflectionX)unknownParameter).getPoint();
						this.addSubRedundanceAndCofactor2Deflection(point, updateCompleteModel);
					}
				}
				
				this.maxDx = Math.max(Math.abs(dX.get(col)), this.maxDx);
				VerticalDeflection deflectionParameter = (VerticalDeflection) unknownParameter;
				deflectionParameter.setValue( deflectionParameter.getValue() + dX.get(col) );
			}
		}
		
		if (updateCompleteModel || this.estimationType == EstimationType.L1NORM) {
			int totalNumberOfNegativeResiduals     = 0;
			int totalNumberOfEffectiveObservations = 0;
			for (int i=0; i<this.numberOfObservations; i++) {
				if (this.interrupt)
					return;
				// in addSubRedundanceAndCofactor hinzugefuegt
				Observation observation = this.projectObservations.get(i); 
				double qll = observation.getStdApriori()*observation.getStdApriori();
				double r   = observation.getRedundancy();
				double v   = this.estimationType == EstimationType.SIMULATION ? 0.0 : (vUT != null ? vUT.get(observation.getRowInJacobiMatrix()) : observation.getObservationalError());
				double vv  = v*v;
				double omegaObs = vv/qll;
				observation.setOmega(omegaObs);
				this.omega += omegaObs;
				// Modell muss noch nicht vollstaendig geloest werden, sondern nur der Teil fuer robuste Schaetzung
				if (!updateCompleteModel)
					return;
				
				// Bestimme Varianzkomponenten der Beobachtungen
				ObservationType observationType = observation.getObservationType();
				VarianceComponentType vcType = VarianceComponentType.getVarianceComponentTypeByObservationType(observationType);
				if (vcType != null && this.varianceComponents.containsKey(vcType)) {
					VarianceComponent vc = this.varianceComponents.get(vcType);
					vc.setOmega(vc.getOmega() + omegaObs);
					vc.setRedundancy(vc.getRedundancy() + r);
					vc.setNumberOfObservations(vc.getNumberOfObservations() + 1);
					if (r > 0) {
						
						vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + (v < 0 ? 1 : 0));
						vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + 1);
						
						totalNumberOfNegativeResiduals    += v < 0 ? 1 : 0;
						totalNumberOfEffectiveObservations++;
					}
				}
				
				// Bestimme die erweitere Varianzkomponenten der Beobachtungen
				if (observation.useGroupUncertainty()) {
					ObservationGroup group = observation.getObservationGroup();

					double qllA = group.getStdA(observation);
					double qllB = group.getStdB(observation);
					double qllC = group.getStdC(observation);

					qllA *= qllA;
					qllB *= qllB;
					qllC *= qllC;

					if ( Math.abs(Math.sqrt(qllA + qllB + qllC) - observation.getStdApriori()) > SQRT_EPS) {
						System.err.println(this.getClass().getSimpleName() + " Erweitere VKS; Beobachtungsdifferenz im stoch. Modell zu gross: "+observation+"    "+Math.abs(Math.sqrt(qllA + qllB + qllC) - observation.getStdApriori()));
						continue;
					}

					// Bestimme Omega vTPv
					double omegaVKS = omegaObs / qll; // vv / qll / qll
					double omegaA   = omegaVKS * qllA;
					double omegaB   = omegaVKS * qllB;
					double omegaC   = omegaVKS * qllC;

					// Bestimme Teilredundanz
					double redundancyVKS = r / qll;
					double redundancyA   = redundancyVKS * qllA;
					double redundancyB   = redundancyVKS * qllB;
					double redundancyC   = redundancyVKS * qllC;
				
					vcType = VarianceComponentType.getZeroPointOffsetVarianceComponentTypeByObservationType(observationType);
					if (vcType != null) {
						if (!this.varianceComponents.containsKey(vcType))
							this.varianceComponents.put(vcType, new VarianceComponent(vcType));
						VarianceComponent vc = this.varianceComponents.get(vcType);
						
						vc.setOmega(vc.getOmega() + omegaA);
						vc.setRedundancy(vc.getRedundancy() + redundancyA);
						vc.setNumberOfObservations(vc.getNumberOfObservations() + 1);
						if (r > 0) {
							vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + (v < 0 ? 1 : 0));
							vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + 1);
						}
					}
					
					vcType = VarianceComponentType.getSquareRootDistanceDependentVarianceComponentTypeByObservationType(observationType);
					if (vcType != null) {
						if (!this.varianceComponents.containsKey(vcType))
							this.varianceComponents.put(vcType, new VarianceComponent(vcType));
						VarianceComponent vc = this.varianceComponents.get(vcType);
						
						vc.setOmega(vc.getOmega() + omegaB);
						vc.setRedundancy(vc.getRedundancy() + redundancyB);
						vc.setNumberOfObservations(vc.getNumberOfObservations() + 1);
						if (r > 0) {
							vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + (v < 0 ? 1 : 0));
							vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + 1);
						}
					}
					
					vcType = VarianceComponentType.getDistanceDependentVarianceComponentTypeByObservationType(observationType);
					if (vcType != null) {
						if (!this.varianceComponents.containsKey(vcType))
							this.varianceComponents.put(vcType, new VarianceComponent(vcType));
						VarianceComponent vc = this.varianceComponents.get(vcType);
						
						vc.setOmega(vc.getOmega() + omegaC);
						vc.setRedundancy(vc.getRedundancy() + redundancyC);
						vc.setNumberOfObservations(vc.getNumberOfObservations() + 1);
						if (r > 0) {
							vc.setNumberOfNegativeResiduals(vc.getNumberOfNegativeResiduals() + (v < 0 ? 1 : 0));
							vc.setNumberOfEffectiveObservations(vc.getNumberOfEffectiveObservations() + 1);
						}
					}
				}
				if (updateCompleteModel && this.estimationType != EstimationType.SIMULATION) {
					double observationLinearisationProof = vVec.get(observation.getRowInJacobiMatrix()) + v;
					this.finalLinearisationError = Math.max(this.finalLinearisationError, Math.abs(observationLinearisationProof));
				}
			}

			// Es liegt nun die Gesamtredundanz und Omega vor, sodass Testgroessen
			// bestimmt werden koennen. 
			int dof = this.degreeOfFreedom();
			double varianceOfUnitWeight = this.getVarianceFactorAposteriori();
			boolean applyEmpiricalVarianceOfUnitWeight = this.applyAposterioriVarianceOfUnitWeight && this.estimationType != EstimationType.SIMULATION && dof > 0 && varianceOfUnitWeight > SQRT_EPS;
			
			if (this.estimationType != EstimationType.SIMULATION && this.testStatisticDefinition.getTestStatisticType() == TestStatisticType.SIDAK) {
				this.numberOfHypotesis += this.referencePoints.size();
				this.numberOfHypotesis += this.pointsWithReferenceDeflection.size();
				
				for (VarianceComponent varianceEstimation : this.varianceComponents.values()) {
					double r = Math.round(varianceEstimation.getRedundancy() * 1.0E5) / 1.0E5;
					if (r > 0) 
						this.numberOfHypotesis++;
				}
			}

			// Bestimme maximalen Helmert'schen Punktfehler sigmaPointMax zur Ableitung von EF*SP
			double sigma2PointMax = 0.0;
			for (Point point : this.allPoints.values()) {
				int col = point.getColInJacobiMatrix();
				if (col < 0)
					continue;
				int dim = point.getDimension();
				double sigma2PointMaxi = 0.0;
				for (int d=0; d<dim; d++)
					sigma2PointMaxi += varianceOfUnitWeight * this.Qxx.get(col + d, col + d);
				sigma2PointMax = Math.max(sigma2PointMaxi, sigma2PointMax);
			}

			// Fuege globales Modell zu VCE hinzu
			VarianceComponent vc = new VarianceComponent(VarianceComponentType.GLOBAL);
			vc.setRedundancy(dof);
			vc.setNumberOfObservations(this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows);
			vc.setNumberOfNegativeResiduals(totalNumberOfNegativeResiduals);
			vc.setNumberOfEffectiveObservations(totalNumberOfEffectiveObservations);
			vc.setOmega(this.omega);
			this.varianceComponents.put(vc.getVarianceComponentType(), vc);

			// ermittle kritische Werte zur Bewertung der VCE
			this.significanceTestStatisticParameters = this.getSignificanceTestStatisticParameters();
			this.binomialTestStatisticParameters     = this.getBinomialTestStatisticParameters();
			this.confidenceRegionParameters          = this.getConfidenceRegionParameters();

			for (VarianceComponent varianceEstimation : this.varianceComponents.values()) {
				if (varianceEstimation.getRedundancy() > 0) {
					this.significanceTestStatisticParameters.getTestStatisticParameter(varianceEstimation.getRedundancy(), Double.POSITIVE_INFINITY, varianceEstimation.getVarianceComponentType() == VarianceComponentType.GLOBAL);
					this.binomialTestStatisticParameters.getTestStatisticParameter(varianceEstimation.getNumberOfEffectiveObservations(), 0.5);
				}
			}

			List<Point> points = null;
			if (this.congruenceAnalysis && this.freeNetwork) {
				// Pruefung der Stabilpunkte
				points = this.datumPoints;
			}
			else {
				// Pruefung der Festpunkte
				points = this.referencePoints;
			}

			for (int i=0; i<points.size(); i++) {
				if (this.interrupt)
					return;
				Point point = points.get(i);
				ObservationGroup observations = point.getObservations();
				int dim = point.getDimension();
				if (!this.freeNetwork) {
					if (dim != 1) {
						point.setStdX(-1.0);
						point.setStdY(-1.0);
					}
					if (dim != 2)
						point.setStdZ(-1.0);
				}
				
				Matrix BTPQvvPB = new DenseMatrix(dim,dim);
				Vector BTPv     = new DenseVector(dim);

				// Flag zur Pruefung, ob Datumspunkt in Referenzepoche ueberhaut Beobachtungen besitzt.
				// Wenn Datumspunkt nur in Folgeepoche vorhanden, kann kein Stabilpunkttest durchgefuehrt werden.
				boolean hasObservationInReferenceEpoch = !(this.congruenceAnalysis && this.freeNetwork);
				for (int k=0; k<observations.size(); k++) {
					Observation observationB = observations.get(k);
					if (this.congruenceAnalysis && this.freeNetwork && observationB.getObservationGroup().getEpoch() == Epoch.REFERENCE) {
						hasObservationInReferenceEpoch = true;
						continue;
					}
					double qB = observationB.getStdApriori()*observationB.getStdApriori();
					double vB = this.estimationType == EstimationType.SIMULATION ? 0.0 : -observationB.getObservationalError();
					double b  = 0.0;
					vB = Math.abs(vB) < SQRT_EPS ? 0.0 : vB;

					for (int c=0; c<dim; c++) {
						if (observationB.getStartPoint().equals(point)) {
							if (c==0 && dim !=1)
								b = observationB.diffXs();
							else if (c==1)
								b = observationB.diffYs();
							else if (c==2 || dim == 1)
								b = observationB.diffZs();
						}
						else if (observationB.getEndPoint().equals(point)) {
							if (c==0 && dim !=1)
								b = observationB.diffXe();
							else if (c==1)
								b = observationB.diffYe();
							else if (c==2 || dim == 1)
								b = observationB.diffZe();
						}
						BTPv.set(c, BTPv.get(c) + b*vB/qB);

						for (int j=0; j<observations.size(); j++) {
							Observation observationBT = observations.get(j);
							if (this.congruenceAnalysis && this.freeNetwork && observationBT.getObservationGroup().getEpoch() == Epoch.REFERENCE)
								continue;
							double qll = this.getQllElement(observationBT, observationB);
							double qBT = observationBT.getStdApriori()*observationBT.getStdApriori();
							// P*Qvv*P
							// P*(Qll - Q_ll)*P
							// (P*Qll - P*Q_ll)*P
							// (I - P*Q_ll)*P
							// (P - P*Q_ll*P)
							
							// Numerische Null wird auf Hauptdiagonale zu Null gesetzt, um Summation von "Fragmenten" zu unterbinden
							double pqvvp = k==j ? Math.max(1.0/qBT - qll/qBT/qB, 0.0) : -qll/qBT/qB; 
							
							for (int r=0; r<dim; r++) {
								double bT = 0.0;
								if (observationBT.getStartPoint().equals(point)) {
									if (r==0 && dim !=1)
										bT = observationBT.diffXs();
									else if (r==1)
										bT = observationBT.diffYs();
									else if (r==2 || dim == 1)
										bT = observationBT.diffZs();
								}
								else if (observationBT.getEndPoint().equals(point)) {
									if (r==0 && dim !=1)
										bT = observationBT.diffXe();
									else if (r==1)
										bT = observationBT.diffYe();
									else if (r==2 || dim == 1)
										bT = observationBT.diffZe();
								}								
								BTPQvvPB.set(r,c, BTPQvvPB.get(r,c) + bT*pqvvp*b);
							}
						}
					}					
				}
				Matrix Qnn = new DenseMatrix(dim, dim);
				Vector nabla = new DenseVector(dim);
				boolean isCalculated = false;
				ConfidenceRegion confidenceRegion = null;

				if (hasObservationInReferenceEpoch && observations.size() >= dim) {
					try {
						Qnn = MathExtension.pinv(BTPQvvPB, -1);
						confidenceRegion = new ConfidenceRegion(Qnn);
						isCalculated = true;
					} 
					catch (NotConvergedException nce) {
						nce.printStackTrace();
						isCalculated = false;
					}
				}

			    if (!isCalculated)
			    	continue;
			    
			    
			    if (this.estimationType == EstimationType.SIMULATION) {
			    	Vector nabla0 = new DenseVector(dim);
			    	// Nichtzentralitaetsparameter ist noch nicht bestimmt, 
					// sodass GRZW ein vorlaeufiger Wert ist, 
				    // der nabla*Pnn*nabla == 1 erfuellt.
					for (int j=0; j<dim; j++)
						nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));

					//point.setMinimalDetectableBiases(Matrices.getArray(nabla0));
					point.setMaximumTolerableBiases(Matrices.getArray(nabla0));
			    }
			    else {
			    	Qnn.mult(BTPv, nabla);
			    	double normNabla = nabla.norm(Vector.Norm.Two);
			    	point.setNablaCoVarNabla( Math.abs(nabla.dot(BTPv)) );
			    	point.setGrossErrors( Matrices.getArray(nabla.scale(-1.0)) );

			    	// Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
			    	Vector nabla0 = new DenseVector(nabla, true);
			    	Vector PQvvPnabla0 = new DenseVector(nabla0);
			    	BTPQvvPB.mult(nabla0, PQvvPnabla0);
			    	double nQn0 = nabla0.dot(PQvvPnabla0);
			    	if (normNabla < SQRT_EPS || nQn0 <= 0) {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j));
			    	}
			    	else {
			    		for (int j=0; j<dim; j++)
			    			nabla0.set(j, nabla0.get(j)/Math.sqrt(nQn0));
			    	}
			    	//point.setMinimalDetectableBiases(Matrices.getArray(nabla0));
			    	point.setMaximumTolerableBiases(Matrices.getArray(nabla0));
			    }
			}
			
			for (int i=0; i<this.pointsWithReferenceDeflection.size(); i++) {
				if (this.interrupt)
					return;
				Point point = this.pointsWithReferenceDeflection.get(i);
				boolean isStation = false;
				
				VerticalDeflectionX deflectionX = point.getVerticalDeflectionX();
				VerticalDeflectionY deflectionY = point.getVerticalDeflectionY();

				ObservationGroup observations = point.getObservations();
				int dim = 2;
				
				Matrix BTPQvvPB = new DenseMatrix(dim,dim);
				Vector BTPv     = new DenseVector(dim);

				for (int k=0; k<observations.size(); k++) {
					Observation observationB = observations.get(k);

					double qB = observationB.getStdApriori()*observationB.getStdApriori();
					double vB = this.estimationType == EstimationType.SIMULATION ? 0.0 : -observationB.getObservationalError();
					double b  = 0.0;
					vB = Math.abs(vB) < SQRT_EPS ? 0.0 : vB;

					for (int c=0; c<dim; c++) {
						if (observationB.getStartPoint().equals(point)) {
							if (c==0)
								b = observationB.diffVerticalDeflectionXs();
							else if (c==1)
								b = observationB.diffVerticalDeflectionYs();
							isStation = true;
						}
						else if (observationB.getEndPoint().equals(point)) {
							if (c==0)
								b = observationB.diffVerticalDeflectionXe();
							else if (c==1)
								b = observationB.diffVerticalDeflectionYe();
						}
						BTPv.set(c, BTPv.get(c) + b*vB/qB);

						for (int j=0; j<observations.size(); j++) {
							Observation observationBT = observations.get(j);

							double qll = this.getQllElement(observationBT, observationB);
							double qBT = observationBT.getStdApriori()*observationBT.getStdApriori();
							// P*Qvv*P
							// P*(Qll - Q_ll)*P
							// (P*Qll - P*Q_ll)*P
							// (I - P*Q_ll)*P
							// (P - P*Q_ll*P)
							
							// Numerische Null wird auf Hauptdiagonale zu Null gesetzt, um Summation von "Fragmenten" zu unterbinden
							double pqvvp = k==j ? Math.max(1.0/qBT - qll/qBT/qB, 0.0) : -qll/qBT/qB; 
							
							for (int r=0; r<dim; r++) {
								double bT = 0.0;
								if (observationBT.getStartPoint().equals(point)) {
									if (r==0)
										bT = observationBT.diffVerticalDeflectionXs();
									else if (r==1)
										bT = observationBT.diffVerticalDeflectionYs();
								}
								else if (observationBT.getEndPoint().equals(point)) {
									if (r==0)
										bT = observationBT.diffVerticalDeflectionXe();
									else if (r==1)
										bT = observationBT.diffVerticalDeflectionYe();
								}								
								BTPQvvPB.set(r,c, BTPQvvPB.get(r,c) + bT*pqvvp*b);
							}
						}
					}					
				}
				Matrix Qnn = new DenseMatrix(dim, dim);
				Vector nabla = new DenseVector(dim);
				boolean isCalculated = false;
				ConfidenceRegion confidenceRegion = null;

				if (isStation && observations.size() >= dim) {
					try {
						Qnn = MathExtension.pinv(BTPQvvPB, -1);
						confidenceRegion = new ConfidenceRegion(Qnn);
						isCalculated = true;
					} 
					catch (NotConvergedException nce) {
						nce.printStackTrace();
						isCalculated = false;
					}
				}

			    if (!isCalculated) {
			    	continue;
			    }
		    
			    if (this.estimationType == EstimationType.SIMULATION) {
			    	// Nichtzentralitaetsparameter ist noch nicht bestimmt, 
					// sodass GRZW ein vorlaeufiger Wert ist, 
				    // der nabla*Pnn*nabla == 1 erfuellt.
					//deflectionX.setMinimalDetectableBias(confidenceRegion.getMinimalDetectableBias(0));
					//deflectionY.setMinimalDetectableBias(confidenceRegion.getMinimalDetectableBias(1));
					deflectionX.setMaximumTolerableBias(confidenceRegion.getMinimalDetectableBias(0));
					deflectionY.setMaximumTolerableBias(confidenceRegion.getMinimalDetectableBias(1));
			    }
			    else {
			    	Qnn.mult(BTPv, nabla);
				    deflectionX.setNablaCoVarNabla( Math.abs(nabla.dot(BTPv)) );
					nabla = nabla.scale(-1.0);
					deflectionX.setGrossError(nabla.get(0));
					deflectionY.setGrossError(nabla.get(1));
				    
				    // Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
				    Vector nabla0 = new DenseVector(nabla, true);
				    Vector PQvvPnabla0 = new DenseVector(nabla0);
					BTPQvvPB.mult(nabla0, PQvvPnabla0);
					double nQn0 = nabla0.dot(PQvvPnabla0);
					if (nQn0 > 0) {
						//deflectionX.setMinimalDetectableBias(nabla0.get(0)/Math.sqrt(nQn0));
						//deflectionY.setMinimalDetectableBias(nabla0.get(1)/Math.sqrt(nQn0));
						deflectionX.setMaximumTolerableBias(nabla0.get(0)/Math.sqrt(nQn0));
						deflectionY.setMaximumTolerableBias(nabla0.get(1)/Math.sqrt(nQn0));
					}
			    }
			}

			for (int i=0; i<this.referencePoints.size(); i++) {
				if (this.interrupt)
					return;
				Point point = this.referencePoints.get(i);
				int dim = point.getDimension();
				point.calcStochasticParameters(varianceOfUnitWeight, dof, applyEmpiricalVarianceOfUnitWeight);

				TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
				TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;
				double sqrtLambda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));
				double kPrio = tsPrio.getQuantile();
				double kPost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
				
				double mdb[] = new double[dim];
				if (dim != 1) {
					mdb[0] = point.getMaximumTolerableBiasX()*sqrtLambda;
					mdb[1] = point.getMaximumTolerableBiasY()*sqrtLambda;
				}
				if (dim != 2) {
					mdb[dim-1] = point.getMaximumTolerableBiasZ()*sqrtLambda;
				}
				
				point.setMinimalDetectableBiases(mdb);
				// Bei Simulation ist Nabla == GRZW, da keine Fehler bestimmbar sind
				if (this.estimationType == EstimationType.SIMULATION)
					point.setGrossErrors(mdb);
				else {
					double tPrio = point.getTprio();
					double tPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? point.getTpost() : 0.0;
					double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
					double pPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
					point.setProbabilityValues(pPrio, pPost);
					point.setSignificant(tPrio > kPrio || tPost > kPost);
				}
			}
			
			for (int i=0; i<this.pointsWithReferenceDeflection.size(); i++) {
				if (this.interrupt)
					return;
				Point point = this.pointsWithReferenceDeflection.get(i);
				
				VerticalDeflectionX deflectionX = point.getVerticalDeflectionX();
				VerticalDeflectionY deflectionY = point.getVerticalDeflectionY();
				
				int dim = 2;
				deflectionX.calcStochasticParameters(varianceOfUnitWeight, dof, applyEmpiricalVarianceOfUnitWeight);

				TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
				TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;
				
				double lamda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));
				double kPrio = tsPrio.getQuantile();
				double kPost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
				
				deflectionX.setMinimalDetectableBias(lamda * deflectionX.getMaximumTolerableBias());
				deflectionY.setMinimalDetectableBias(lamda * deflectionY.getMaximumTolerableBias());
				
				// Bei Simulation ist Nabla == GRZW, da keine Fehler bestimmbar sind
				if (this.estimationType == EstimationType.SIMULATION) {
					deflectionX.setGrossError(deflectionX.getMinimalDetectableBias());
					deflectionY.setGrossError(deflectionY.getMinimalDetectableBias());
				}
				else {
					double tPrio = deflectionX.getTprio();
					double tPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? deflectionX.getTpost() : 0.0;
					double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
					double pPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
					// deflectionX stellt stoch. Parameter fuer beide Lotparameter bereit
					deflectionX.setPprio(pPrio);
					deflectionX.setPpost(pPost);
					deflectionX.setSignificant(tPrio > kPrio || tPost > kPost);
				}
			}

			this.addStochasticParameters2Observations(varianceOfUnitWeight, dof, sigma2PointMax);
			this.traceCxxPoints = 0;
			
			for (int i=0; i<this.unknownParameters.size(); i++) {
				if (this.interrupt)
					return;
				UnknownParameter unknownParameter = this.unknownParameters.get(i);
				int col = unknownParameter.getColInJacobiMatrix();
				if (unknownParameter instanceof Point) {
					Point point = (Point)unknownParameter;
					int dim = point.getDimension();
					int subCol = 0;
					// Hole Submatrix aus Qxx zur Bestimmung der Konfidenzbereiche
					Matrix subQxx = new UpperSymmPackMatrix( dim );
					for (int r=0; r<dim; r++) {
						for (int c=r; c<dim; c++) {
							double qxx = this.Qxx.get(col+r, col+c);
							subQxx.set(r, c, qxx);
							if (c == r)
								this.traceCxxPoints += varianceOfUnitWeight * qxx;
						}
					}

					try {
						ConfidenceRegion confidenceRegion = new ConfidenceRegion(this.confidenceRegionParameters, subQxx, varianceOfUnitWeight, applyEmpiricalVarianceOfUnitWeight ? dof : Double.POSITIVE_INFINITY);
						if (confidenceRegion != null)
							point.setConfidenceRegion(confidenceRegion);
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (point.getRowInJacobiMatrix() >= 0 || (this.congruenceAnalysis && this.freeNetwork)) {
						point.calcStochasticParameters(varianceOfUnitWeight, dof, applyEmpiricalVarianceOfUnitWeight);
									
						TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
						TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;
						double lamda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));
						double Kprio = tsPrio.getQuantile();
						double Kpost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
	
						double mdb[] = new double[dim];
						double ep[]   = new double[dim];
						if (dim != 1) {
							mdb[0] = point.getMaximumTolerableBiasX()*lamda;
							mdb[1] = point.getMaximumTolerableBiasY()*lamda;
							
							ep[0] = point.getInfluenceOnPointPositionX()*lamda;
							ep[1] = point.getInfluenceOnPointPositionY()*lamda;
						}
						if (dim != 2) {
							mdb[dim-1] = point.getMaximumTolerableBiasZ()*lamda;
							ep[dim-1]  = point.getInfluenceOnPointPositionZ()*lamda;
						}
						
						point.setMinimalDetectableBiases(mdb);
						// Bei Simulation ist Nabla == GRZW, da keine Fehler bestimmbar sind
						// EP und EFSP wird dann aus GRZW bestimmt
						if (this.estimationType == EstimationType.SIMULATION) {
							point.setInfluenceOnNetworkDistortion(point.getInfluenceOnNetworkDistortion() * lamda * Math.sqrt(sigma2PointMax));
							point.setGrossErrors(mdb);
							point.setInfluencesOnPointPosition(ep);
						}
						else {
							point.setInfluenceOnNetworkDistortion(point.getInfluenceOnNetworkDistortion() * Math.sqrt(sigma2PointMax));
							double tPrio = point.getTprio();
							double tPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? point.getTpost() : 0.0;
							double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
							double pPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
							point.setProbabilityValues(pPrio, pPost);
							point.setSignificant(tPrio > Kprio || tPost > Kpost || this.adaptedPointUncertainties.containsKey(point));
						}	
					}
					
					if (dim != 1) {
						double stdX = Math.sqrt(Math.abs(varianceOfUnitWeight * subQxx.get(subCol, subCol++)));
						double stdY = Math.sqrt(Math.abs(varianceOfUnitWeight * subQxx.get(subCol, subCol++)));

						point.setStdX(stdX);
						point.setStdY(stdY);
					}
					if (dim != 2) {
						double stdZ = Math.sqrt(Math.abs(varianceOfUnitWeight * subQxx.get(subCol, subCol++)));
						point.setStdZ(stdZ);
					}
				}
				else if (unknownParameter instanceof VerticalDeflection) { 
					// VerticalDeflectionY is taken from the VerticalDeflectionX Component
					// --> No need to modify again
					// Stochastic deflection checked for outliers
					if (unknownParameter instanceof VerticalDeflectionX) {
						VerticalDeflectionX deflectionX = (VerticalDeflectionX)unknownParameter;
						VerticalDeflectionY deflectionY = deflectionX.getPoint().getVerticalDeflectionY();

						int cols[] = new int[] {deflectionX.getColInJacobiMatrix(), deflectionY.getColInJacobiMatrix()};
						// Hole Submatrix aus Qxx zur Bestimmung der Konfidenzbereiche
						Matrix subQxx = new UpperSymmPackMatrix( 2 );
						for (int r=0; r<cols.length; r++) {
							for (int c=r; c<cols.length; c++) {
								double qxx = this.Qxx.get(cols[r], cols[c]);
								subQxx.set(r, c, qxx);
							}
						}

						try {
							ConfidenceRegion confidence = new ConfidenceRegion(this.confidenceRegionParameters, subQxx, varianceOfUnitWeight, applyEmpiricalVarianceOfUnitWeight ? dof : Double.POSITIVE_INFINITY);
							if (confidence != null) {
								deflectionX.setConfidence(confidence.getConfidenceRegionAxis(0));
								deflectionY.setConfidence(confidence.getConfidenceRegionAxis(1));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						deflectionX.setStd(Math.sqrt(Math.abs(varianceOfUnitWeight * subQxx.get(0, 0))));
						deflectionY.setStd(Math.sqrt(Math.abs(varianceOfUnitWeight * subQxx.get(1, 1))));

						if ((deflectionX.getRowInJacobiMatrix() >= 0 && deflectionY.getRowInJacobiMatrix() >= 0) || (this.congruenceAnalysis && this.freeNetwork)) { 
							int dim = 2;
							deflectionX.calcStochasticParameters(varianceOfUnitWeight, dof, applyEmpiricalVarianceOfUnitWeight);

							TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
							TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;

							double lamda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));
							double Kprio = tsPrio.getQuantile();
							double Kpost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;

							deflectionX.setMinimalDetectableBias( deflectionX.getMaximumTolerableBias() * lamda );
							deflectionY.setMinimalDetectableBias( deflectionY.getMaximumTolerableBias() * lamda );

							// Bei Simulation ist Nabla == GRZW, da keine Fehler bestimmbar sind
							if (this.estimationType == EstimationType.SIMULATION) {
								deflectionX.setGrossError(deflectionX.getMinimalDetectableBias());
								deflectionY.setGrossError(deflectionY.getMinimalDetectableBias());
							}
							else {
								double tPrio = deflectionX.getTprio();
								double tPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? deflectionX.getTpost() : 0.0;
								double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
								double pPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
								deflectionX.setPprio(pPrio);
								deflectionX.setPpost(pPost);
								deflectionX.setSignificant(tPrio > Kprio || tPost > Kpost || this.adaptedVerticalDeflectionUncertainties.containsKey(deflectionX) || this.adaptedVerticalDeflectionUncertainties.containsKey(deflectionY));
							}
						}
					}
				}
				else if (unknownParameter instanceof AdditionalUnknownParameter){
					AdditionalUnknownParameter additionalUnknownParameter = (AdditionalUnknownParameter) unknownParameter;
					
					double qxxPrio = Math.abs(this.Qxx.get(col, col));
					double qxxPost = varianceOfUnitWeight * qxxPrio;
					
					TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(1, Double.POSITIVE_INFINITY);
					TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(1, dof) : null;
					double lambda = tsPrio.getNoncentralityParameter();
					double kPrio  = tsPrio.getQuantile();
					double kPost  = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
					
					TestStatisticParameterSet confLevelPrio = this.confidenceRegionParameters.getTestStatisticParameter(1, Double.POSITIVE_INFINITY);
					TestStatisticParameterSet confLevelPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? this.confidenceRegionParameters.getTestStatisticParameter(1, dof) : null;

					double value = additionalUnknownParameter.getValue();
					if (additionalUnknownParameter.getParameterType() == ParameterType.ORIENTATION ||
							additionalUnknownParameter.getParameterType() == ParameterType.ROTATION_X ||
							additionalUnknownParameter.getParameterType() == ParameterType.ROTATION_Y ||
							additionalUnknownParameter.getParameterType() == ParameterType.ROTATION_Z) {
						value = MathExtension.MOD(value, 2.0*Math.PI);
						if (Math.abs(2.0*Math.PI - value) < Math.abs(value))
							value = value - 2.0*Math.PI;
					}

					double nabla = value - additionalUnknownParameter.getExpectationValue();
					double tPrio = qxxPrio < Constant.EPS ? Double.POSITIVE_INFINITY : nabla*nabla/qxxPrio;
					double tPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? (qxxPost < Constant.EPS ? Double.POSITIVE_INFINITY : nabla*nabla/qxxPost) : 0.0;
					double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, 1);
					double pPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, 1, dof) : 0.0;
					double nabla0 = Math.signum(nabla) * Math.sqrt(Math.abs(lambda * qxxPrio));

					additionalUnknownParameter.setTprio(tPrio);
					additionalUnknownParameter.setTpost(tPost);
					additionalUnknownParameter.setPprio(pPrio);
					additionalUnknownParameter.setPpost(pPost);
					additionalUnknownParameter.setStd(Math.sqrt(qxxPost));
					additionalUnknownParameter.setGrossError(nabla);
					additionalUnknownParameter.setSignificant(tPrio > kPrio || tPost > kPost);
					additionalUnknownParameter.setMinimalDetectableBias(nabla0);
					additionalUnknownParameter.setConfidence(Math.sqrt(qxxPost * (applyEmpiricalVarianceOfUnitWeight && dof > 0 ? confLevelPost.getQuantile() : confLevelPrio.getQuantile())));
				}
				// Strain-Parameter und Lotabweichungen werden gesondert bearbeitet, da teilw. Hilfsparameter bestimmt werden, die keine Interpretation zulassen
				else if (unknownParameter instanceof StrainParameter){}
				else {
					System.err.println(this.getClass() + " Fehler, unbekannter Parametertyp! " + unknownParameter);
				}
			}

			// DeformationsAnalyse
			if (this.congruenceAnalysisGroup != null && !this.congruenceAnalysisGroup.isEmpty()) {
				for ( CongruenceAnalysisGroup tieGroup : this.congruenceAnalysisGroup) {
					int dim = tieGroup.getDimension();
					StrainAnalysisEquations strainAnalysisEquations = tieGroup.getStrainAnalysisEquations();
					
					// Auswertung der einzelnen Deformationsvektoren (Punkt-zu-Punkt Verschiebungen)
					for (boolean flag : new boolean[] {false, true}) {
						TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
						TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof) : null;
						double sqrtLambda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));
						double kPrio = tsPrio.getQuantile();
						double kPost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
						
						for ( int tieIdx=0; tieIdx < tieGroup.size(flag); tieIdx++ ) {
							CongruenceAnalysisPointPair tie = tieGroup.get(tieIdx, flag);
							if (this.interrupt)
								return;
							Point p0 = tie.getStartPoint();
							Point p1 = tie.getEndPoint();

							int colP0  = p0.getColInJacobiMatrix();
							int colP1  = p1.getColInJacobiMatrix();
							Matrix tieQxx = new UpperSymmPackMatrix(dim);
							
							// Sind beides Festpunkte, brich ab
							if (colP0 < 0 && colP1 < 0)
								continue;

							// Ist nur P0 ein Festpunkt, nutze die Matrix von P1
							if (colP0 < 0) {
								// wenn nur die Z-Komponente zu pruefen ist,
								// passe CoVar-Index bei 3D-Punkten an
								if (dim == 1 && p1.getDimension() == 3)
									colP1 += 2;

								for (int r=0; r<dim; r++) {
									for (int c=r; c<dim; c++) {
										double q22 = this.Qxx.get(colP1+r, colP1+c);
										tieQxx.set(r, c, q22);
									}
								}
							}
							// Ist nur P1 ein Festpunkt, nutze die Matrix von P0
							else if (colP1 < 0) {
								// wenn nur die Z-Komponente zu pruefen ist,
								// passe CoVar-Index bei 3D-Punkten an
								if (dim == 1 && p0.getDimension() == 3)
									colP0 += 2;

								for (int r=0; r<dim; r++) {
									for (int c=r; c<dim; c++) {
										double q11 = this.Qxx.get(colP0+r, colP0+c);
										tieQxx.set(r, c, q11);
									}
								}
							}
							// Nutze die Matrix von P0 und P1
							else {
								// wenn nur die Z-Komponente zu pruefen ist,
								// passe CoVar-Index bei 3D-Punkten an
								if (dim == 1 && p0.getDimension() == 3)
									colP0 += 2;
								if (dim == 1 && p1.getDimension() == 3)
									colP1 += 2;

								for (int r=0; r<dim; r++) {
									for (int c=r; c<dim; c++) {
										double q11 = this.Qxx.get(colP0+r, colP0+c);
										double q22 = this.Qxx.get(colP1+r, colP1+c);
										double q12 = this.Qxx.get(colP0+r, colP1+c);
										double q21 = this.Qxx.get(colP1+r, colP0+c);
										tieQxx.set(r, c, (q11-q12-q21+q22));
									}
								}
							}
							
							double nabla[] = new double[dim];
							double mdb[]   = new double[dim];
							double normNabla = 0;
							if (dim != 1) {
								nabla[0] = p0.getX() - p1.getX();
								nabla[1] = p0.getY() - p1.getY();
							}
							if (dim != 2) {
								nabla[dim-1] = p0.getZ() - p1.getZ();
							}
							
							for (int r=0; r<dim; r++)
								normNabla += nabla[r] * nabla[r];
							normNabla = Math.sqrt(normNabla);
							
							Matrix tiePxx = null;
							try {
								tiePxx = MathExtension.pinv(tieQxx, -1);
							}
							catch (NotConvergedException nce) {
								nce.printStackTrace();
							}
							
							if (tiePxx != null && normNabla > SQRT_EPS) {
								double tPost = 0.0;
								double tPrio = 0.0;
								for (int r=0; r<dim; r++) {
									double tmp = 0;
									for (int c=0; c<dim; c++) {
										tmp += tiePxx.get(r, c) * nabla[c];
									}
									tPrio += tmp * nabla[r];
								}
								tPrio /= dim;
								tPost = applyEmpiricalVarianceOfUnitWeight && varianceOfUnitWeight > SQRT_EPS ? tPrio / varianceOfUnitWeight : 0.0;

								Vector nabla0 = new DenseVector(nabla, true);
								Vector PxxNabla0 = new DenseVector(nabla0, true);
								tiePxx.mult(nabla0, PxxNabla0);
								double nPn0 = nabla0.dot(PxxNabla0);
								if (nPn0 > 0) {
									for (int j=0; j<dim; j++)
										mdb[j] = sqrtLambda * nabla0.get(j)/Math.sqrt(nPn0);
								}
								tie.setTeststatisticValues(tPrio, tPost);
							}
							
							double sigma[] = new double[dim];
							
							for (int i=0; i<dim; i++)
								sigma[i] = Math.sqrt(Math.abs(varianceOfUnitWeight * tieQxx.get(i, i)));

							try {
								ConfidenceRegion confidenceRegion = new ConfidenceRegion(this.confidenceRegionParameters, tieQxx, varianceOfUnitWeight, applyEmpiricalVarianceOfUnitWeight ? dof : Double.POSITIVE_INFINITY);
								if (confidenceRegion != null) {
									tie.setConfidenceRegion(confidenceRegion);
									if (normNabla < SQRT_EPS) { // this.estimationType == EstimationType.SIMULATION) {
										for (int i=0; i<dim; i++) {
											mdb[i] = sqrtLambda*confidenceRegion.getMinimalDetectableBias(i);
											if (this.estimationType != EstimationType.SIMULATION) 
												nabla[i] = mdb[i];
										}
									}
								}
								confidenceRegion = null;
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							tie.setMinimalDetectableBiases(mdb);
							tie.setGrossErrors(nabla);
							tie.setSigma(sigma);

							double tPrio = tie.getTprio();
							double tPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? tie.getTpost() : 0.0;
							double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
							double pPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof) : 0.0;

							tie.setProbabilityValues(pPrio, pPost);
							tie.setSignificant(tPrio > kPrio || tPost > kPost);
						}
					}
					
					// Bestimmung der Modellstoerungen bei Strain-Analyse der einzelnen Vektoren; ueberschreibt GRZW, NABLA, Tprio, Tpost
					// Bestimmung der signifikanten Strain-Parameter im Modell
					int nou = strainAnalysisEquations.numberOfParameters();
					int nor = strainAnalysisEquations.numberOfRestrictions();
					int not = tieGroup.size(true);
					if (this.congruenceAnalysis && this.freeNetwork && strainAnalysisEquations.hasUnconstraintParameters() && not * dim + nor >= nou) {
						// Teststatistik fuer Strain-Parameter
						TestStatisticParameterSet tsPrioParam = this.significanceTestStatisticParameters.getTestStatisticParameter(1, Double.POSITIVE_INFINITY);
						TestStatisticParameterSet tsPostParam = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(1, dof) : null;
						// Konfidenzbereich der Strain-Parameter
						TestStatisticParameterSet confLevelPrio = this.confidenceRegionParameters.getTestStatisticParameter(1, Double.POSITIVE_INFINITY);
						TestStatisticParameterSet confLevelPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? this.confidenceRegionParameters.getTestStatisticParameter(1, dof) : null;
						
						double sqrtLambdaParam = Math.sqrt(Math.abs(tsPrioParam.getNoncentralityParameter()));
						double kPrioParam = tsPrioParam.getQuantile();
						double kPostParam = tsPostParam != null ? tsPostParam.getQuantile() : Double.POSITIVE_INFINITY;
						
						// Teststatistik fuer Verschiebungsvektoren
						TestStatisticParameterSet tsPrioTie = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
						TestStatisticParameterSet tsPostTie = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;
						double sqrtLambdaTie = Math.sqrt(Math.abs(tsPrioTie.getNoncentralityParameter()));
						double kPrioTie = tsPrioTie.getQuantile();
						double kPostTie = tsPostTie != null ? tsPostTie.getQuantile() : Double.POSITIVE_INFINITY;
						
						Matrix subQxx = new UpperSymmPackMatrix(strainAnalysisEquations.numberOfParameters());
						for (int r=0; r<subQxx.numRows(); r++) {
							int row = strainAnalysisEquations.get(r).getColInJacobiMatrix();
							for (int c=r; c<subQxx.numRows(); c++) {
								int col = strainAnalysisEquations.get(c).getColInJacobiMatrix();
								subQxx.set(r,c, this.Qxx.get(row, col));
							}
						}
						strainAnalysisEquations.expandParameters(varianceOfUnitWeight, subQxx, applyEmpiricalVarianceOfUnitWeight);
						for (int i=0; i<strainAnalysisEquations.numberOfParameters(); i++) {
							StrainParameter parameter = strainAnalysisEquations.get(i);
							parameter.setMinimalDetectableBias(parameter.getMinimalDetectableBias() * sqrtLambdaParam);
							double tPrio = parameter.getTprio();
							double tPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? parameter.getTpost() : 0.0;
							double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, 1);
							double pPost = applyEmpiricalVarianceOfUnitWeight && dof > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, 1, dof) : 0.0;
							parameter.setConfidence(parameter.getStd() * (applyEmpiricalVarianceOfUnitWeight && dof > 0 ? Math.sqrt( confLevelPost.getQuantile() ) : Math.sqrt( confLevelPrio.getQuantile() )));
							parameter.setPprio(pPrio);
							parameter.setPpost(pPost);
							parameter.setSignificant(tPrio > kPrioParam || tPost > kPostParam);
						}
						
						for ( int tieIdx=0; tieIdx < tieGroup.size(true); tieIdx++ ) {
							CongruenceAnalysisPointPair tie = tieGroup.get(tieIdx, true);
							if (this.interrupt)
								return;
							
							Point point = tie.getEndPoint();

							// Sind Festpunkte dabei, brich ab
							if (tie.getStartPoint().getColInJacobiMatrix() < 0 || tie.getEndPoint().getColInJacobiMatrix() < 0)
								continue;

							ObservationGroup observations = point.getObservations();
							
							Matrix BTPQvvPB = new DenseMatrix(dim,dim);
							Vector BTPv     = new DenseVector(dim);

							for (int k=0; k<observations.size(); k++) {
								Observation observationB = observations.get(k);
								//if (observationB.getObservationGroup().isReferenceEpoch())
								//	continue;
								double qB = observationB.getStdApriori()*observationB.getStdApriori();
								double vB = this.estimationType == EstimationType.SIMULATION ? 0.0 : -observationB.getObservationalError();
								double b  = 0.0;
								vB = Math.abs(vB) < SQRT_EPS ? 0.0 : vB;
								
								for (int c=0; c<dim; c++) {
									if (observationB.getStartPoint().equals(point)) {
										if (c==0 && dim !=1)
											b = observationB.diffXs();
										else if (c==1)
											b = observationB.diffYs();
										else if (c==2 || dim == 1)
											b = observationB.diffZs();
									}
									else if (observationB.getEndPoint().equals(point)) {
										if (c==0 && dim !=1)
											b = observationB.diffXe();
										else if (c==1)
											b = observationB.diffYe();
										else if (c==2 || dim == 1)
											b = observationB.diffZe();
									}
									BTPv.set(c, BTPv.get(c) + b*vB/qB);

									for (int j=0; j<observations.size(); j++) {
										Observation observationBT = observations.get(j);
										//if (observationBT.getObservationGroup().isReferenceEpoch())
										//	continue;
										double qll = this.getQllElement(observationBT, observationB);
										double qBT = observationBT.getStdApriori()*observationBT.getStdApriori();
										// P*Qvv*P
										// P*(Qll - Q_ll)*P
										// (P*Qll - P*Q_ll)*P
										// (I - P*Q_ll)*P
										// (P - P*Q_ll*P)
										
										// Numerische Null wird auf Hauptdiagonale zu Null gesetzt, um Summation von "Fragmenten" zu unterbinden
										double pqvvp = k==j ? Math.max(1.0/qBT - qll/qBT/qB, 0.0) : -qll/qBT/qB; 
										
										for (int r=0; r<dim; r++) {
											double bT = 0.0;
											if (observationBT.getStartPoint().equals(point)) {
												if (r==0 && dim !=1)
													bT = observationBT.diffXs();
												else if (r==1)
													bT = observationBT.diffYs();
												else if (r==2 || dim == 1)
													bT = observationBT.diffZs();
											}
											else if (observationBT.getEndPoint().equals(point)) {
												if (r==0 && dim !=1)
													bT = observationBT.diffXe();
												else if (r==1)
													bT = observationBT.diffYe();
												else if (r==2 || dim == 1)
													bT = observationBT.diffZe();
											}								
											BTPQvvPB.set(r,c, BTPQvvPB.get(r,c) + bT*pqvvp*b);
										}
									}
								}					
							}
							Matrix Qnn = new DenseMatrix(dim, dim);
							Vector nabla = new DenseVector(dim);
							boolean isCalculated = false;
							ConfidenceRegion confidenceRegion = null;
							
							try {
								if (not * dim + nor > nou)
									Qnn = MathExtension.pinv(BTPQvvPB, -1);
								confidenceRegion = new ConfidenceRegion(Qnn);
								isCalculated = true;
							} 
							catch (NotConvergedException nce) {
								//nce.printStackTrace();
								isCalculated = false;
							}

						    if (!isCalculated) {
						    	continue;
						    }
						    
						    if (this.estimationType == EstimationType.SIMULATION) {
						    	Vector nabla0 = new DenseVector(dim);
								for (int j=0; j<dim; j++)
									nabla0.set(j, sqrtLambdaTie * confidenceRegion.getMinimalDetectableBias(j));
								tie.setMinimalDetectableBiases(Matrices.getArray(nabla0));
						    }
						    else {
						    	Qnn.mult(BTPv, nabla);
						    	
						    	double NablaQnnNabla = BTPv.dot(nabla);
						    	double sigma2apostTie = (dof - dim) > 0 ? (this.omega - NablaQnnNabla) / (dof - dim) : 0;
						    	double tPrio = NablaQnnNabla / dim;
						    	double tPost = applyEmpiricalVarianceOfUnitWeight && (dof - dim) > 0 && sigma2apostTie > SQRT_EPS ? tPrio / sigma2apostTie : 0;
						    			
						    	nabla = nabla.scale(-1.0);
							    // Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
						    	// und skaliere mit Nicht-Zentralitaetsparameter
							    Vector nabla0 = new DenseVector(nabla, true);
							    Vector PQvvPnabla0 = new DenseVector(nabla0);
								BTPQvvPB.mult(nabla0, PQvvPnabla0);
								double nQn0 = nabla0.dot(PQvvPnabla0);
								if (nQn0 > 0) {
									for (int j=0; j<dim; j++)
										nabla0.set(j, sqrtLambdaTie * nabla0.get(j)/Math.sqrt(nQn0));
								}
								tie.setMinimalDetectableBiases(Matrices.getArray(nabla0));
								tie.setGrossErrors(Matrices.getArray(nabla));

								double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
								double pPost = applyEmpiricalVarianceOfUnitWeight && dof-dim > 0 ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
								
								tie.setTeststatisticValues(tPrio, tPost);
								tie.setProbabilityValues(pPrio, pPost);
								tie.setSignificant(tPrio > kPrioTie || tPost > kPostTie);
						    } 
						}
					}
				}
			}
			else {
				this.congruenceAnalysisGroup = new ArrayList<CongruenceAnalysisGroup>(0);
			}
		}
	}
	
	private void addSubRedundanceAndCofactor2Observations() {
		Set<Integer> gnssObsIds = new LinkedHashSet<Integer>();
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			boolean isGNSS = observation.getObservationType() == ObservationType.GNSS1D || observation.getObservationType() == ObservationType.GNSS2D || observation.getObservationType() == ObservationType.GNSS3D;
			
			if (isGNSS && gnssObsIds.contains(observation.getId()))
				continue;
			
			if (isGNSS)
				gnssObsIds.add(observation.getId());

			this.addSubRedundanceAndCofactor( observation );
		}
	}
	
	private void addStochasticParameters2Observations(double sigma2apost, int dof, double sigma2PointMax) {
		Set<Integer> gnssObsIds = new LinkedHashSet<Integer>();
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			boolean isCalculated = false;
			double nPn  = 0.0; // Nabla*inv(Qnn)*Nabla
			boolean isGNSS = observation.getObservationType() == ObservationType.GNSS1D || observation.getObservationType() == ObservationType.GNSS2D || observation.getObservationType() == ObservationType.GNSS3D;
			
			if (isGNSS && gnssObsIds.contains(observation.getId()))
				continue;
			
			if (isGNSS) {
				gnssObsIds.add(observation.getId());
				GNSSBaseline gnss = (GNSSBaseline)observation;
				int dim = gnss.getDimension();
	
				TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
				double sqrtLambda = Math.sqrt(Math.abs(tsPrio.getNoncentralityParameter()));

				List<Observation> baseline = gnss.getBaselineComponents();
				
				double traceR     = 0;
				Matrix subPQvvP   = gnss.getBaselineRedundancyMatrix();
				Matrix ATQxxBP    = ATQxxBP_GNSS_EP.remove(gnss.getId());
				Matrix PAzTQzzAzP = PAzTQzzAzP_GNSS_EF.remove(gnss.getId());
				
				if (subPQvvP == null)
					subPQvvP = new DenseMatrix(dim,dim);
				if (ATQxxBP == null)
					ATQxxBP = new DenseMatrix(dim,dim);
				if (PAzTQzzAzP == null)
					PAzTQzzAzP = new DenseMatrix(dim,dim);

				// Berechne P - PQvvP mit PR = PQvvP
				Matrix subPsubPQvvP = new DenseMatrix(subPQvvP);
				for (int k=0; k<dim; k++) {
					Observation obs = baseline.get(k);
					double qll = obs.getStdApriori()*obs.getStdApriori();
					traceR += Math.abs(subPQvvP.get(k,k));
					for (int j=0; j<dim; j++) {
						double pqvvp = 1.0/qll * subPQvvP.get(k,j);
						subPQvvP.set(k,j, pqvvp);
						subPsubPQvvP.set(k,j, (k == j) ? Math.max(1.0/qll - pqvvp, 0) : -pqvvp);
					}
				}

				Matrix Qnn = new DenseMatrix(dim,dim);
				ConfidenceRegion confidenceRegion = null;

				try {
					Qnn = MathExtension.pinv(subPQvvP, -1);
					confidenceRegion = new ConfidenceRegion(Qnn);
					isCalculated = true;
				} 
				catch (NotConvergedException nce) {
					isCalculated = false;
					nce.printStackTrace();
				}
				if (isCalculated && traceR > SQRT_EPS) {
					if (this.estimationType == EstimationType.SIMULATION) {
				    	// Nichtzentralitaetsparameter ist noch nicht bestimmt, 
						// sodass GRZW ein vorlaeufiger Wert ist, 
					    // der nabla*Pnn*nabla == 1 erfuellt.
						Vector nabla0 = new DenseVector(dim);
						Vector ep     = new DenseVector(gnss.getDimension());
						for (int j=0; j<dim; j++)
							nabla0.set(j, confidenceRegion.getMinimalDetectableBias(j)*sqrtLambda);

						ATQxxBP.mult(nabla0, ep);
						
						Vector Mnabla0 = new DenseVector(dim); 
						PAzTQzzAzP.mult(nabla0, Mnabla0);
						double uz2 = nabla0.dot(Mnabla0);
						subPsubPQvvP.mult(nabla0, Mnabla0);
						double du2 = nabla0.dot(Mnabla0);

						for (int j=0; j<dim; j++) {
							double nablaJ = nabla0.get(j);
							baseline.get(j).setMinimalDetectableBias(nablaJ);
							baseline.get(j).setMaximumTolerableBias(confidenceRegion.getMinimalDetectableBias(j));
							baseline.get(j).setGrossError(nablaJ);
							baseline.get(j).setInfluenceOnPointPosition(ep.get(j));
							baseline.get(j).setInfluenceOnNetworkDistortion( Math.sqrt(sigma2PointMax * Math.abs(du2 - uz2)) );
						}
				    }
				    else {
				    	Vector subPv = new DenseVector(dim);
						for (int j=0; j<dim; j++)
							subPv.set(j, baseline.get(j).getObservationalError() / baseline.get(j).getStdApriori() / baseline.get(j).getStdApriori());

				    	Vector nabla = new DenseVector(gnss.getDimension());
						Vector ep    = new DenseVector(gnss.getDimension());
						Qnn.mult(subPv, nabla);
						nPn = subPv.dot(nabla);
						nabla = nabla.scale(-1.0);
						
						ATQxxBP.mult(nabla, ep);
						Vector Mnabla = new DenseVector(dim); 
						PAzTQzzAzP.mult(nabla, Mnabla);
						double uz2 = nabla.dot(Mnabla);
						subPsubPQvvP.mult(nabla, Mnabla);
						double du2 = nabla.dot(Mnabla);
		
						Vector nabla0 = new DenseVector(nabla, true);
						// Bestimme Nabla auf der Grenzwertellipse mit nabla0*Pnn*nabla0 == 1
					    Vector PQvvPnabla0 = new DenseVector(nabla0);
					    subPQvvP.mult(nabla0, PQvvPnabla0);
						double nQn0 = nabla0.dot(PQvvPnabla0);
//						for (int j=0; j<dim; j++)
//							if (nQn0 > 0)
//								nabla0.set(j, nabla0.get(j)/Math.sqrt(nQn0)*lamda);

						for (int j=0; j<dim; j++) {
							double mtb = 0;
							if (nQn0 > 0)
								mtb = nabla0.get(j)/Math.sqrt(nQn0);
							
//							baseline.get(j).setMinimalDetectableBias( nabla0.get(j) );
							baseline.get(j).setMinimalDetectableBias( mtb * sqrtLambda );
							baseline.get(j).setMaximumTolerableBias( mtb );
							baseline.get(j).setGrossError( nabla.get(j) );	
							baseline.get(j).setInfluenceOnPointPosition( ep.get(j) );
							baseline.get(j).setInfluenceOnNetworkDistortion( Math.sqrt(sigma2PointMax * Math.abs(du2 - uz2)) );
						}
				    }
				}
				
				for (int j=0; j<dim; j++)
					this.addStochasticParameters2Observation(baseline.get(j), sigma2apost, dof, nPn);
			}
			else {
				TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(1, Double.POSITIVE_INFINITY);
				double lamda = Math.abs(tsPrio.getNoncentralityParameter());
				double qll = observation.getStdApriori()*observation.getStdApriori();
				double r   = observation.getRedundancy();
				double v   = this.estimationType == EstimationType.SIMULATION ? 0.0 : observation.getObservationalError();
				double pvv  = v*v/qll;
				double mdb = r > SQRT_EPS ? Math.sqrt(Math.abs(lamda*qll/r)) : 0.0;
				double mtb = r > SQRT_EPS ? Math.sqrt(Math.abs(qll/r)) : 0.0;
				observation.setMinimalDetectableBias(mdb);
				observation.setMaximumTolerableBias(mtb);
				
				// Distance fuer Richtungen/Zenitwinkel zur Bestimmung von EP in [m]
				double dist = 1;
				if (observation.getObservationType() == ObservationType.DIRECTION || observation.getObservationType() == ObservationType.ZENITH_ANGLE) {
					if (observation.getStartPoint().getDimension() == 3 && observation.getEndPoint().getDimension() == 3)
						dist = observation.getCalculatedDistance3D();
					else
						dist = observation.getCalculatedDistance2D();
				}
				
				if (r > SQRT_EPS) {
					if (this.estimationType != EstimationType.SIMULATION) {
						nPn = pvv/r;
						double nabla = v/r;
						double uz2 = nabla * observation.getInfluenceOnNetworkDistortion() * nabla;
						double du2 = nabla * (1 - r)/qll * nabla;
						observation.setGrossError( nabla );
						observation.setInfluenceOnPointPosition( observation.getInfluenceOnPointPosition() * nabla * dist);
						observation.setInfluenceOnNetworkDistortion( Math.sqrt(sigma2PointMax * Math.abs(du2 - uz2)));
						observation.setMinimalDetectableBias( Math.signum(v)*mdb );
						observation.setMaximumTolerableBias( Math.signum(v)*mtb);
					}
					else { // if (this.estimationType == EstimationType.SIMULATION) {
						double uz2 = mdb * observation.getInfluenceOnNetworkDistortion() * mdb;
						double du2 = mdb * (1 - r)/qll * mdb;
						
						observation.setGrossError( mdb );
						observation.setInfluenceOnPointPosition( observation.getInfluenceOnPointPosition() * mdb * dist);
						observation.setInfluenceOnNetworkDistortion( Math.sqrt(sigma2PointMax * Math.abs(du2 - uz2)));
					} 	
				}
				else { // Redundanz zu gering
					observation.setInfluenceOnPointPosition(0.0);
					observation.setInfluenceOnNetworkDistortion(0.0);
				}
				
				this.addStochasticParameters2Observation(observation, sigma2apost, dof, nPn);
			}
		}
	}
	
	/**
	 * Berechnet die Testgroessen T<sub>prio</sub> und T<sub>post</sub>
	 * und die Standardabweichung a-posteriori fuer eine Beobachtung
	 * 
	 * @param obs Beobachtung
	 * @param sigma2apost Varianzfaktor a-posteriori
	 * @param dof Freiheitsgrad der Gesamtausgleichung
	 * @param nPn Produkt aus (&nabla;Q<sub>&nabla;&nabla;</sub>&nabla;)<sup>-1</sup>
	 */
	private void addStochasticParameters2Observation(Observation obs, double sigma2apost, int dof, double nPn) {
		// Berechnung der entgueltigen Standardabweichungen
		double sigma = obs.getStd();
		if (sigma > 0)
			obs.setStd( Math.sqrt(sigma2apost*sigma*sigma) );
		
		if (this.estimationType != EstimationType.SIMULATION) {
			// Bestimmung der Testgroessen
			int dim = obs.getObservationType() == ObservationType.GNSS1D || obs.getObservationType() == ObservationType.GNSS2D || obs.getObservationType() == ObservationType.GNSS3D ? ((GNSSBaseline)obs).getDimension() : 1;
			double omega = sigma2apost*(double)dof;
			double sigma2apostObs = (dof-dim) > 0 && omega > nPn ? (omega-nPn)/(dof-dim) : 0.0;
			sigma2apostObs = sigma2apostObs > NetworkAdjustment.SQRT_EPS ? sigma2apostObs : 0.0;
			boolean applyEmpiricalVarianceOfUnitWeight = this.applyAposterioriVarianceOfUnitWeight && dof-dim > 0 && sigma2apostObs > NetworkAdjustment.SQRT_EPS;
			
			double tPrio = nPn/dim;
			double tPost = applyEmpiricalVarianceOfUnitWeight ? tPrio/sigma2apostObs : 0.0;
			
			double pPrio = TestStatistic.getLogarithmicProbabilityValue(tPrio, dim);
			double pPost = applyEmpiricalVarianceOfUnitWeight ? TestStatistic.getLogarithmicProbabilityValue(tPost, dim, dof-dim) : 0.0;
			obs.setTestAndProbabilityValues(tPrio, tPost, pPrio, pPost);

			TestStatisticParameterSet tsPrio = this.significanceTestStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
			TestStatisticParameterSet tsPost = applyEmpiricalVarianceOfUnitWeight ? this.significanceTestStatisticParameters.getTestStatisticParameter(dim, dof-dim) : null;
			double kPrio = tsPrio.getQuantile();
			double kPost = tsPost != null ? tsPost.getQuantile() : Double.POSITIVE_INFINITY;
			
			obs.setSignificant(obs.getTprio() > kPrio || obs.getTpost() > kPost || this.adaptedObservationUncertainties.containsKey(obs));
		}
	}
	
	/**
	 * Fuegt die unbekannten Lotabweichungen zum Modell
	 * hinzu.
	 */
	private void addVerticalDeflectionToModel() {
		for (Point point : this.pointsWithUnknownDeflection) {
			this.addUnknownParameter(point.getVerticalDeflectionX());
			this.addUnknownParameter(point.getVerticalDeflectionY());
		}
		
		for (Point point : this.pointsWithStochasticDeflection) {
			this.addUnknownParameter(point.getVerticalDeflectionX());
			this.addUnknownParameter(point.getVerticalDeflectionY());
		}
	}
	
	/**
	 * Fuegt die stochastischen Punkte und Lotabweichungen 
	 * als Pseudo-Beobachtungen ein, dieser Schritt muss 
	 * am Ende erfolgen, damit die Punkte in der N-Matrix 
	 * zusammenhaengend stehen. 
	 */
	private void addStochasticPointsAndStochasticDeflectionToModel() {
		int row = this.numberOfObservations;
		
		for (Point point : this.pointsWithStochasticDeflection) {
			point.getVerticalDeflectionX().setRowInJacobiMatrix(row++);
			point.getVerticalDeflectionY().setRowInJacobiMatrix(row++);

			this.numberOfStochasticDeflectionRows += 2;
		}
		
		for (Point point : this.stochasticPoints) {
			point.setRowInJacobiMatrix(row);
			row += point.getDimension();
			this.addUnknownParameter( point );
		}
		
		if (this.numberOfStochasticDeflectionRows > 0) {
			VarianceComponentType vcType = VarianceComponentType.STOCHASTIC_DEFLECTION_COMPONENT;
			if (vcType != null && !this.varianceComponents.containsKey(vcType))
				this.varianceComponents.put(vcType, new VarianceComponent(vcType));
		}
	}
	
	/**
	 * Fuehre die Strain-Parameter als Unbekannte ein,
	 * diese werden ueber zusaetzliche Bedingungsgleichungen
	 * im Zuge der Deformationsanalyse geschaetzt.  
	 */
	private void addStrainParametersToModel() {
		for (CongruenceAnalysisGroup tieGroup : this.congruenceAnalysisGroup) {
			StrainAnalysisEquations strainAnalysisEquations = tieGroup.getStrainAnalysisEquations();
			int nou = strainAnalysisEquations.numberOfParameters();
			int nor = strainAnalysisEquations.numberOfRestrictions();
			int not = tieGroup.size(true);
			int dim = tieGroup.getDimension();
			if (strainAnalysisEquations.hasUnconstraintParameters() && not * dim + nor >= nou) {
				for (int i=0; i<strainAnalysisEquations.numberOfParameters(); i++) {
					if (strainAnalysisEquations.get(i).getColInJacobiMatrix() < 0) {
						this.addUnknownParameter(strainAnalysisEquations.get(i));
					}
				}
			}
		}
	}
	
	/**
	 * Fuegt einen Datumspunkt dem Projekt hinzu
	 * @param point
	 * @param verticalDeflectionType
	 * @return isAdded
	 */
	public boolean addDatumPoint(Point point, VerticalDeflectionType verticalDeflectionType) {
		if ((this.stochasticPoints == null || this.stochasticPoints.isEmpty()) &&
				(this.referencePoints == null || this.referencePoints.isEmpty()) && this.addNewPoint(point, verticalDeflectionType)) {
			this.datumPoints.add(point);
			this.freeNetwork = true;
			return true;
		}
		return false;
	}

	/**
	 * Fuegt einen varianzfreien Punkt dem Projekt hinzu
	 * @param point
	 * @param verticalDeflectionType
	 * @return isAdded
	 */
	public boolean addReferencePoint(Point point, VerticalDeflectionType verticalDeflectionType) {
		if (this.freeNetwork || this.allPoints.containsKey(point.getName())) {
			System.err.println(this.getClass() + " Fehler, ein Punkt mit der " +
					"ID " + point.getName() + " existiert bereits!");
			return false;
		}

		point.setColInDesignmatrixOfModelErros(this.numberOfFixPointRows);
		this.numberOfFixPointRows += point.getDimension();
		this.referencePoints.add(point);
		this.addObservations( point );
		this.allPoints.put(point.getName(), point);
		
		// Deflection werden nachtraeglich hinzugefuegt via addUnknownParameter()
		if (verticalDeflectionType != null) {
			if (verticalDeflectionType == VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION)
				this.pointsWithReferenceDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION)
				this.pointsWithStochasticDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION)
				this.pointsWithUnknownDeflection.add(point);
		}

		return true;
	}	
	
	/**
	 * Fuegt einen stochastischen Punkt dem Projekt hinzu
	 * @param point
	 * @param verticalDeflectionType
	 * @return isAdd
	 */
	public boolean addStochasticPoint(Point point, VerticalDeflectionType verticalDeflectionType) {
		if (this.freeNetwork || this.allPoints.containsKey(point.getName())) {
			System.err.println(this.getClass() + " Fehler, ein Punkt mit der " +
					"ID " + point.getName() + " existiert bereits!");
			return false;
		}
			
		this.stochasticPoints.add(point);
		this.allPoints.put(point.getName(), point);
		this.numberOfStochasticPointRows += point.getDimension();
		
		// Deflection werden nachtraeglich hinzugefuegt via addUnknownParameter()
		if (verticalDeflectionType != null) {
			if (verticalDeflectionType == VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION)
				this.pointsWithReferenceDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION)
				this.pointsWithStochasticDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION)
				this.pointsWithUnknownDeflection.add(point);
		}
		
		VarianceComponentType vcType = VarianceComponentType.getComponentTypeByPointDimension(point.getDimension());
		if (vcType != null && !this.varianceComponents.containsKey(vcType))
			this.varianceComponents.put(vcType, new VarianceComponent(vcType));	
		return true;
	}
	
	/**
	 * Fuegt einen Neupunkt hinzu.
	 * @param point
	 * @param verticalDeflectionType
	 * @return isAdd
	 */
	public boolean addNewPoint(Point point, VerticalDeflectionType verticalDeflectionType) {
		int dim = point.getDimension();
		
		if (verticalDeflectionType != null && verticalDeflectionType == VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION)
			dim += 2;
		
		if (this.allPoints.containsKey(point.getName())) {
			System.err.println(this.getClass() + "\nFehler, ein Punkt mit der " +
					"ID " + point.getName() + " existiert bereits!");
			return false;
		}
		else if (dim > point.getObservations().size()) {
			System.err.println(this.getClass() + "\nFehler, Punkt "+point.getName()+" besitzt nicht genuegend " +
					"Beobachtungen um bestimmt zu werden! Punkt wird ignoriert. Dim = "+ dim +", Obs = " + point.getObservations().size() );
			return false;
		}
		this.addUnknownParameter( point );
		this.allPoints.put(point.getName(), point);
		
		// Deflection werden nachtraeglich hinzugefuegt via addUnknownParameter()
		if (verticalDeflectionType != null) {
			if (verticalDeflectionType == VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION)
				this.pointsWithReferenceDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION)
				this.pointsWithStochasticDeflection.add(point);
			else if (verticalDeflectionType == VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION)
				this.pointsWithUnknownDeflection.add(point);
		}
		
		return true;
	}
	
	public boolean addAdditionalUnknownParameter(AdditionalUnknownParameter additionalUnknownParameter) {
		if (this.unknownParameters.contains(additionalUnknownParameter)) {
			System.err.println(this.getClass() + "\nFehler, der Parameter wurde bereits " +
					"hinzugefuegt!");
			return false;
		}
		// Verhindern, dass Gruppenparameter von leere Gruppen in die 
		// AGL einbezogen werden (bspw. leerer Richtungssatz --> eine Orientierung)
		else if (additionalUnknownParameter.getObservations().size() == 0){
			System.err.println(this.getClass() + "\nFehler, Parameter hat keine " +
					"Verknuepfung zu Beobachtungen und kann somit\nnicht bestimmt werden! " +
					"Parameter wird ignoriert.\n"+additionalUnknownParameter);
			return false;
		}
		this.addUnknownParameter(additionalUnknownParameter);
		return true;
	}
	
	/**
	 * Fuegt einen unbekannten Parameter dem Modell hinzu
	 * @param unknownParameter
	 */
	private void addUnknownParameter(UnknownParameter unknownParameter) {
		this.addObservations( unknownParameter );
		this.unknownParameters.add( unknownParameter );
		this.numberOfUnknownParameters = this.unknownParameters.columnsInJacobi();		
	}
	
	/**
	 * Fuegt die Beobachtungen, die die Punkte und Zusatzparameter haben,
	 * dem Modell hinzu.
	 * @param unknownParameter
	 */
	private void addObservations(UnknownParameter unknownParameter) {
		for (int i=0; i<unknownParameter.getObservations().size(); i++) {
			Observation observation = unknownParameter.getObservations().get(i);
			if (observation.getRowInJacobiMatrix()<0) {
				observation.setRowInJacobiMatrix(this.numberOfObservations++);
				this.projectObservations.add(observation);
				VarianceComponentType vcType = VarianceComponentType.getVarianceComponentTypeByObservationType(observation.getObservationType());
				if (vcType != null && !this.varianceComponents.containsKey(vcType))
					this.varianceComponents.put(vcType, new VarianceComponent(vcType));				
			}
		}
	}
	
	/**
	 * Fuegt eine Vektorgruppe zur Deo-Analyse dem Projekt hinzu
	 * @param congruenceAnalysisGroup
	 * @return isAdded
	 */
	public boolean addCongruenceAnalysisGroup(CongruenceAnalysisGroup congruenceAnalysisGroup) {
		if (!this.congruenceAnalysisGroup.contains(congruenceAnalysisGroup) && congruenceAnalysisGroup.totalSize() > 0) {
			this.congruenceAnalysisGroup.add(congruenceAnalysisGroup);
			return true;
		}
		return false;
	}
	
	/**
	 * Liefert alle Unbekannten im Modell
	 * @return unknow
	 */
	public UnknownParameters getUnknownParameters() {
		return this.unknownParameters;
	}
	
	/**
	 * Bestimmt den theoretischen Defekt der Normalgleichung basierend auf den terrestrischen Beobachtungen
	 * Wahlweise wird eine Defekt-Analyse durchgefuehrt. 
	 * @return RankDefect
	 * @throws IllegalArgumentException
	 * @throws NotConvergedException
	 */
	public RankDefect detectRankDefect() {
		if (!this.freeNetwork || this.rankDefect.isUserDefinedRankDefect())
			return this.rankDefect;
	
		this.rankDefect.reset();
		
		boolean is3DNet = false;
		boolean is2DNet = false;
		boolean is1DNet = false;
		
		int eigenValueDefectCounter = -1;
		
		for (int i=0; i<this.datumPoints.size(); i++) {
			Point point = this.datumPoints.get(i);
			if (point.getDimension() != 2)
				this.rankDefect.setTranslationZ(DefectType.FREE);
			if (point.getDimension() != 1) {
				this.rankDefect.setTranslationX(DefectType.FREE);
				this.rankDefect.setTranslationY(DefectType.FREE);
			}
			if (!is3DNet && point.getDimension() == 3) {
				is3DNet = true;
				is2DNet = false;
				is1DNet = false;
			}
			else if (!is3DNet && !is2DNet && point.getDimension() == 2)
				is2DNet = true;
			else if (!is3DNet && !is1DNet && point.getDimension() == 1)
				is1DNet = true;
		}
		
		if (this.proofOfDatumDefectDetection) {
			try {
				int maxDefect = 0;
				if (is1DNet)
					maxDefect = 2;
				else if (is2DNet)
					maxDefect = 4;
				else
					maxDefect = 7;
				maxDefect = Math.min(maxDefect, this.numberOfUnknownParameters); 
				// Der Index ist Eins-Index-basierend, d.h., der kleinste Eigenwert hat den Index Eins und der groesste ist am Index n!
				NormalEquationSystem neq = this.createNormalEquation();
				UpperSymmPackMatrix N = neq.getMatrix();

				Matrix eig[] = MathExtension.eig(N, this.numberOfUnknownParameters, 1, maxDefect, false);

				double values[] = new double[maxDefect];
				double threshold = 0;
				for (int i = 0; i < maxDefect; i++) {
					values[i] = Math.abs(eig[0].get(i, i));
					if (is1DNet && i < 1)
						threshold += values[i];
					else if (is2DNet && i < 2)
						threshold += values[i] / 2.0;
					else if (is3DNet && i < 3)
						threshold += values[i] / 3.0;
				}
				threshold = 10 * (threshold + SQRT_EPS);

				eigenValueDefectCounter = 0;
				for (int i = 0; i < maxDefect; i++)
					eigenValueDefectCounter += Math.abs(eig[0].get(i, i)) < threshold ? 1 : 0;
			} 
			catch (IllegalArgumentException | NotConvergedException e) {
				eigenValueDefectCounter = -1;
				e.printStackTrace();
			}
		}
		
		// Setzte alle mgl. Defekte, die von den Beobachtungen 
		// anschließend festgesetzt werden koennen. Translation
		// ist bereits festgesetzt in Schleife drueber	
		if (is1DNet) {
			this.rankDefect.setScaleZ(DefectType.FREE);
			this.rankDefect.setRotationX(DefectType.FREE);
			this.rankDefect.setRotationY(DefectType.FREE);
		}
		if (is2DNet){
			this.rankDefect.setScaleXY(DefectType.FREE);
			this.rankDefect.setRotationZ(DefectType.FREE);
		}
		if (is3DNet) {
			this.rankDefect.setScaleXYZ(DefectType.FREE);
			this.rankDefect.setRotationX(DefectType.FREE);
			this.rankDefect.setRotationY(DefectType.FREE);
			this.rankDefect.setRotationZ(DefectType.FREE);
		}

		int deltaH2PointsWithKnownDeflections       = 0;
		int zenithangleStationsWithKnownDeflections = 0;
		
		Set<Pair<String,String>> zenithangleStationNamesWithKnownDeflections = new HashSet<Pair<String,String>>();
		
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			ObservationGroup observationGroup = observation.getObservationGroup();
			int numberOfAddParams = observationGroup.numberOfAdditionalUnknownParameter();	
			int groupSize = observationGroup.size();
			
			// Beobachtung (bzw. Gruppe) kann nicht zur Datumsdefinition genutzt werden
			if (groupSize <= numberOfAddParams)
				continue;
			
			if (observation instanceof DeltaZ) {
				// muss dim == 1 oder 3 sein, darf keine Lotabweichungen haben oder aber besitzt Lotabweichungen die gleichzeitig aber auch Beobachtungen sind
				if ((observation.getStartPoint().getDimension() != 2 && observation.getEndPoint().getDimension() != 2) && 
						(!observation.getStartPoint().hasUnknownDeflectionParameters() || 
								!observation.getEndPoint().hasUnknownDeflectionParameters() ||
								observation.getStartPoint().hasUnknownDeflectionParameters() && observation.getStartPoint().hasObservedDeflectionParameters() ||
								observation.getEndPoint().hasUnknownDeflectionParameters() && observation.getEndPoint().hasObservedDeflectionParameters() )) {

					if (!observation.getStartPoint().hasUnknownDeflectionParameters())
						deltaH2PointsWithKnownDeflections++;
					if (!observation.getEndPoint().hasUnknownDeflectionParameters())
						deltaH2PointsWithKnownDeflections++;
				}
				if (!is3DNet) {
					if (!((DeltaZ)observation).getScale().isEnable())
						this.rankDefect.setScaleZ(DefectType.FIXED);
					else if (this.rankDefect.getScaleZ() != DefectType.FIXED)
						this.rankDefect.setScaleZ(DefectType.FREE);

//					this.rankDefect.setRotationX(DefectType.FIXED);
//					this.rankDefect.setRotationY(DefectType.FIXED);
				}
				else {
					if (!((DeltaZ)observation).getScale().isEnable())
						this.rankDefect.setScaleXYZ(DefectType.FIXED);
					else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
						this.rankDefect.setScaleXYZ(DefectType.FREE);
				}
			}
			else if (observation instanceof Direction) { 
				if (!((Direction)observation).getOrientation().isEnable())
					this.rankDefect.setRotationZ(DefectType.FIXED);
				else if (this.rankDefect.getRotationZ() != DefectType.FIXED)
					this.rankDefect.setRotationZ(DefectType.FREE);
			}
			else if (observation instanceof HorizontalDistance) {
				if (this.rankDefect.getRotationZ() != DefectType.FIXED)
					this.rankDefect.setRotationZ(DefectType.FREE);
				
				if (!is3DNet) {
					if (!((HorizontalDistance)observation).getScale().isEnable())
						this.rankDefect.setScaleXY(DefectType.FIXED);
					else if (this.rankDefect.getScaleXY() != DefectType.FIXED)
						this.rankDefect.setScaleXY(DefectType.FREE);
				}
				else {
					if (!((HorizontalDistance)observation).getScale().isEnable())
						this.rankDefect.setScaleXYZ(DefectType.FIXED);
					else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
						this.rankDefect.setScaleXYZ(DefectType.FREE);
				}
			}
			else if (observation instanceof SlopeDistance) {
				if (this.rankDefect.getRotationX() != DefectType.FIXED)
					this.rankDefect.setRotationX(DefectType.FREE);
				if (this.rankDefect.getRotationY() != DefectType.FIXED)
					this.rankDefect.setRotationY(DefectType.FREE);
				if (this.rankDefect.getRotationZ() != DefectType.FIXED)
					this.rankDefect.setRotationZ(DefectType.FREE);
				
				if (!((SlopeDistance)observation).getScale().isEnable())
					this.rankDefect.setScaleXYZ(DefectType.FIXED);
				else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
					this.rankDefect.setScaleXYZ(DefectType.FREE);
				
			}
			else if (observation instanceof ZenithAngle) {
				if (this.rankDefect.getRotationZ() != DefectType.FIXED)
					this.rankDefect.setRotationZ(DefectType.FREE);
				
				// muss dim == 3 sein, darf keine Lotabweichungen haben oder aber besitzt Lotabweichungen die gleichzeitig auch Beobachtungen sind
//				zenithangleStationsWithKnownDeflections += (observation.getStartPoint().getDimension() == 3 && 
//						(!observation.getStartPoint().hasUnknownDeflectionParameters() || 
//								observation.getStartPoint().hasUnknownDeflectionParameters() && observation.getStartPoint().hasObservedDeflectionParameters())) ? 1 : 0;
				Pair<String, String> stationAndTargetNames = new Pair<String, String>(observation.getStartPoint().getName(), observation.getEndPoint().getName());
				if (observation.getStartPoint().getDimension() == 3 && !zenithangleStationNamesWithKnownDeflections.contains(stationAndTargetNames) && 
						(!observation.getStartPoint().hasUnknownDeflectionParameters() || 
								observation.getStartPoint().hasUnknownDeflectionParameters() && observation.getStartPoint().hasObservedDeflectionParameters())) {
					zenithangleStationsWithKnownDeflections++;
					zenithangleStationNamesWithKnownDeflections.add(stationAndTargetNames);
				}
			}
			else if (observation instanceof GNSSBaseline1D) {
				if (((GNSSBaseline1D)observation).getDimension() >= 1) {
					double gnssZ = ((GNSSBaseline1D)observation).getBaselineComponent(ComponentType.Z).getValueApriori();
					
					// ermittle die Anzahl an Basislinienkomponenten, die der Start- bzw. Zielpunkt hat (eine Basislinie == eine Komponenten)
					ObservationGroup startPointObservations = observation.getStartPoint().getObservations();
					ObservationGroup endPointObservations   = observation.getEndPoint().getObservations();
					int gnssCompCntStart = 0;
					for (int j=0; j<startPointObservations.size(); j++) {
						if (startPointObservations.get(j) instanceof GNSSBaseline1D && ((GNSSBaseline1D)observation).getDimension() >= 1 && ++gnssCompCntStart > 1) 
							break;
					}
					int gnssCompCntEnd = 0;
					for (int j=0; j<endPointObservations.size(); j++) {
						if (endPointObservations.get(j) instanceof GNSSBaseline1D && ((GNSSBaseline1D)observation).getDimension() >= 1 && ++gnssCompCntEnd > 1) 
							break;
					}
					
					if (gnssZ != 0) {
						if (!is3DNet) {
							if (!((GNSSBaseline1D)observation).getScale().isEnable())
								this.rankDefect.setScaleZ(DefectType.FIXED);
							else if (this.rankDefect.getScaleZ() != DefectType.FIXED)
								this.rankDefect.setScaleZ(DefectType.FREE);
						}
						else {
							if (!((GNSSBaseline1D)observation).getScale().isEnable())
								this.rankDefect.setScaleXYZ(DefectType.FIXED);
							else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
								this.rankDefect.setScaleXYZ(DefectType.FREE);
						}
					}
					
					if (gnssCompCntStart > 1 || gnssCompCntEnd > 1) {
						if (!((GNSSBaseline1D)observation).getRotationX().isEnable())
							this.rankDefect.setRotationX(DefectType.FIXED);
						else if (this.rankDefect.getRotationX() != DefectType.FIXED)
							this.rankDefect.setRotationX(DefectType.FREE);

						if (!((GNSSBaseline1D)observation).getRotationY().isEnable())
							this.rankDefect.setRotationY(DefectType.FIXED);
						else if (this.rankDefect.getRotationY() != DefectType.FIXED)
							this.rankDefect.setRotationY(DefectType.FREE);
					}
				}
			}
			else if (observation instanceof GNSSBaseline2D) {
				if (((GNSSBaseline2D)observation).getDimension() >= 2) { // Ist vollstaendige Baseline X und Y vorhanden
					double gnssX = ((GNSSBaseline2D)observation).getBaselineComponent(ComponentType.X).getValueApriori();
					double gnssY = ((GNSSBaseline2D)observation).getBaselineComponent(ComponentType.Y).getValueApriori();
					if (gnssX != 0 || gnssY != 0) {
						// ermittle die Anzahl an Basislinienkomponenten, die der Start- bzw. Zielpunkt hat (eine Basislinie == zwei Komponenten)
						ObservationGroup startPointObservations = observation.getStartPoint().getObservations();
						ObservationGroup endPointObservations   = observation.getEndPoint().getObservations();
						int gnssCompCntStart = 0;
						for (int j=0; j<startPointObservations.size(); j++) {
							if (startPointObservations.get(j) instanceof GNSSBaseline2D && ((GNSSBaseline2D)observation).getDimension() >= 2 && ++gnssCompCntStart > 2) 
								break;
						}
						int gnssCompCntEnd = 0;
						for (int j=0; j<endPointObservations.size(); j++) {
							if (endPointObservations.get(j) instanceof GNSSBaseline2D && ((GNSSBaseline2D)observation).getDimension() >= 2 && ++gnssCompCntEnd > 2) 
								break;
						}
						
						if (!is3DNet) {
							if (!((GNSSBaseline2D)observation).getScale().isEnable())
								this.rankDefect.setScaleXY(DefectType.FIXED);
							else if (this.rankDefect.getScaleXY() != DefectType.FIXED)
								this.rankDefect.setScaleXY(DefectType.FREE);
						}
						else {
							if (!((GNSSBaseline2D)observation).getScale().isEnable())
								this.rankDefect.setScaleXYZ(DefectType.FIXED);
							else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
								this.rankDefect.setScaleXYZ(DefectType.FREE);
						}
						
						if (gnssCompCntStart > 2 || gnssCompCntEnd > 2) {
							if (!((GNSSBaseline2D)observation).getRotationZ().isEnable())
								this.rankDefect.setRotationZ(DefectType.FIXED);
							else if (this.rankDefect.getRotationZ() != DefectType.FIXED)
								this.rankDefect.setRotationZ(DefectType.FREE);
						}
					}
				}
			}
			else if (observation instanceof GNSSBaseline3D) {
				if (((GNSSBaseline3D)observation).getDimension() >= 3) { // Ist vollstaendige Baseline X, Y und Z vorhanden
					double gnssX = ((GNSSBaseline3D)observation).getBaselineComponent(ComponentType.X).getValueApriori();
					double gnssY = ((GNSSBaseline3D)observation).getBaselineComponent(ComponentType.Y).getValueApriori();
					double gnssZ = ((GNSSBaseline3D)observation).getBaselineComponent(ComponentType.Z).getValueApriori();				
					if (gnssX != 0 || gnssY != 0 || gnssZ != 0) {
						// ermittle die Anzahl an Basislinienkomponenten, die der Start- bzw. Zielpunkt hat (eine Basislinie == drei Komponenten)
						ObservationGroup startPointObservations = observation.getStartPoint().getObservations();
						ObservationGroup endPointObservations   = observation.getEndPoint().getObservations();
						int gnssCompCntStart = 0;
						for (int j=0; j<startPointObservations.size(); j++) {
							if (startPointObservations.get(j) instanceof GNSSBaseline3D && ((GNSSBaseline3D)observation).getDimension() >= 3 && ++gnssCompCntStart > 3) 
								break;
						}
						int gnssCompCntEnd = 0;
						for (int j=0; j<endPointObservations.size(); j++) {
							if (endPointObservations.get(j) instanceof GNSSBaseline3D && ((GNSSBaseline3D)observation).getDimension() >= 3 && ++gnssCompCntEnd > 3) 
								break;
						}

						if (!((GNSSBaseline3D)observation).getScale().isEnable())
							this.rankDefect.setScaleXYZ(DefectType.FIXED);
						else if (this.rankDefect.getScaleXYZ() != DefectType.FIXED)
							this.rankDefect.setScaleXYZ(DefectType.FREE);
						
						if (gnssCompCntStart > 3 || gnssCompCntEnd > 3) {
							if (gnssY != 0 || gnssZ != 0) {
								if (!((GNSSBaseline3D)observation).getRotationX().isEnable())
									this.rankDefect.setRotationX(DefectType.FIXED);
								else if (this.rankDefect.getRotationX() != DefectType.FIXED)
									this.rankDefect.setRotationX(DefectType.FREE);
							}

							if (gnssX != 0 || gnssZ != 0) {
								if (!((GNSSBaseline3D)observation).getRotationY().isEnable())
									this.rankDefect.setRotationY(DefectType.FIXED);
								else if (this.rankDefect.getRotationY() != DefectType.FIXED)
									this.rankDefect.setRotationY(DefectType.FREE);
							}

							if (gnssX != 0 || gnssY != 0) {
								if (!((GNSSBaseline3D)observation).getRotationZ().isEnable())
									this.rankDefect.setRotationZ(DefectType.FIXED);
								else if (this.rankDefect.getRotationZ() != DefectType.FIXED)
									this.rankDefect.setRotationZ(DefectType.FREE);
							}
						}
					}
				}
			}
		}

		if (deltaH2PointsWithKnownDeflections > 1) {
			this.rankDefect.setRotationX(DefectType.FIXED);
			this.rankDefect.setRotationY(DefectType.FIXED);	
		}
		
		if (zenithangleStationsWithKnownDeflections > 1) {
			this.rankDefect.setRotationX(DefectType.FIXED);
			this.rankDefect.setRotationY(DefectType.FIXED);
		}
		
		if (!is1DNet && deltaH2PointsWithKnownDeflections == 1) {
			if (this.rankDefect.getRotationX() == DefectType.FREE)
				this.rankDefect.setRotationX(DefectType.FIXED);
			else if (this.rankDefect.getRotationY() == DefectType.FREE)
				this.rankDefect.setRotationY(DefectType.FIXED);
		}
		
		if (zenithangleStationsWithKnownDeflections == 1) {
			if (this.rankDefect.getRotationX() == DefectType.FREE)
				this.rankDefect.setRotationX(DefectType.FIXED);
			else if (this.rankDefect.getRotationY() == DefectType.FREE)
				this.rankDefect.setRotationY(DefectType.FIXED);
		}

		// In einem reinen Richtungs- und/oder Zenitwinkelnetz gibts 
		// keine Strecken, die den Massstab festhalten. Er muss demnach 
		// als Bedingung in die freie Ausgleichung eingefuehrt werden
		if (is2DNet && this.rankDefect.getScaleXY() == DefectType.NOT_SET) 
			this.rankDefect.setScaleXY(DefectType.FREE);
		if (is3DNet && this.rankDefect.getScaleXYZ() == DefectType.NOT_SET)
			this.rankDefect.setScaleXYZ(DefectType.FREE);

		if (this.proofOfDatumDefectDetection && eigenValueDefectCounter != -1 && eigenValueDefectCounter != this.rankDefect.getDefect())
			System.err.println("Error, the defect of the normal equations system is not "
					+ "equal to its theoretical value: " + eigenValueDefectCounter + " vs. " + this.rankDefect.getDefect());

		return this.rankDefect;
	}
	
	public RankDefect getRankDefect() {
		return this.rankDefect;
	}
	
	/**
	 * Liefert die Verbesserungen der AGL v = A*dX - dl
	 * @param dx
	 * @return v
	 */
	private Vector getCorrectionVector(Vector dx) {
		Vector v = new DenseVector(this.numberOfStochasticPointRows + this.numberOfObservations + this.numberOfStochasticDeflectionRows);

		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			ObservationGroup observationGroup = observation.getObservationGroup();
			Point startPoint = observation.getStartPoint();
			Point endPoint   = observation.getStartPoint();
			
			int row = observation.getRowInJacobiMatrix();
			
			int colInJacobiStart = startPoint.getColInJacobiMatrix();
			int colInJacobiEnd   = endPoint.getColInJacobiMatrix();
			
			int dimStart = startPoint.getDimension();
			int dimEnd   = endPoint.getDimension();
			
			double aDx = 0.0;
			// Bestimme A*dx fuer eine Beobachtung -> Ableitung nach den Punkte
			if (colInJacobiStart >= 0) {
				int dim = 0;
				if (dimStart != 1) {
					aDx += dx.get(colInJacobiStart+dim)*observation.diffXs();
					dim++;
					aDx += dx.get(colInJacobiStart+dim)*observation.diffYs();
					dim++;
				}
				if (dimStart != 2) {
					aDx += dx.get(colInJacobiStart+dim)*observation.diffZs();
				}
			}
			if (colInJacobiEnd >= 0) {
				int dim = 0;
				if (dimEnd != 1) {
					aDx += dx.get(colInJacobiEnd+dim)*observation.diffXe();
					dim++;
					aDx += dx.get(colInJacobiEnd+dim)*observation.diffYe();
					dim++;
				}
				if (dimEnd != 2) {
					aDx += dx.get(colInJacobiEnd+dim)*observation.diffZe();
				}
			}
			// Lotabweichung des Standpunktes
			if (startPoint.hasUnknownDeflectionParameters()) {
				VerticalDeflection deflX = startPoint.getVerticalDeflectionX();
				VerticalDeflection deflY = startPoint.getVerticalDeflectionY();
				
				aDx += dx.get(deflX.getColInJacobiMatrix())*observation.diffVerticalDeflectionXs();
				aDx += dx.get(deflY.getColInJacobiMatrix())*observation.diffVerticalDeflectionYs();
			}
			// Lotabweichung des Zielpunktes
			if (endPoint.hasUnknownDeflectionParameters()) {
				VerticalDeflection deflX = endPoint.getVerticalDeflectionX();
				VerticalDeflection deflY = endPoint.getVerticalDeflectionY();
				
				aDx += dx.get(deflX.getColInJacobiMatrix())*observation.diffVerticalDeflectionXe();
				aDx += dx.get(deflY.getColInJacobiMatrix())*observation.diffVerticalDeflectionYe();
			}
			// Zusatzparameter
			if (observationGroup.numberOfAdditionalUnknownParameter() > 0) {
				
				if (observation instanceof DeltaZ) {
					AdditionalUnknownParameter addPar = ((DeltaZ)observation).getScale();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffScale(); 
					}
				}
				else if (observation instanceof Direction) {
					AdditionalUnknownParameter addPar = ((Direction)observation).getOrientation();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffOri(); 
					}
				}
				else if (observation instanceof HorizontalDistance) {
					AdditionalUnknownParameter addPar = ((HorizontalDistance)observation).getScale();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffScale();
					}
					addPar = ((HorizontalDistance)observation).getZeroPointOffset();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffAdd();
					}
				}
				else if (observation instanceof SlopeDistance) {
					AdditionalUnknownParameter addPar = ((SlopeDistance)observation).getScale();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffScale();
					}
					addPar = ((SlopeDistance)observation).getZeroPointOffset();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffAdd();
					}
				}
				else if (observation instanceof ZenithAngle) {
					AdditionalUnknownParameter addPar = ((ZenithAngle)observation).getRefractionCoefficient();
					if (addPar.isEnable()) {
						aDx += dx.get(addPar.getColInJacobiMatrix())*observation.diffRefCoeff();
					}
				}
				
			}
			v.set(row, aDx - observation.getObservationalError());
		}
		
		for (Point point : this.pointsWithStochasticDeflection) {
			int col = point.getVerticalDeflectionX().getColInJacobiMatrix();
			int row = point.getVerticalDeflectionX().getRowInJacobiMatrix();
			v.set(row, dx.get(col));
			
			col = point.getVerticalDeflectionY().getColInJacobiMatrix();
			row = point.getVerticalDeflectionY().getRowInJacobiMatrix();
			v.set(row, dx.get(col));
		}
		
		for (Point point : this.stochasticPoints) {
			int row = point.getRowInJacobiMatrix();
			int col = point.getColInJacobiMatrix();
			int dim = point.getDimension();
			if (row >= 0) {
				for (int j=0; j<dim; j++)
					v.set(row + j, dx.get(col + j));
			}
		}
		
		return v;
	}
	
	/**
	 * Liefert die Parameter zur Bestimmung der Konfidenzbereiche
	 * @return confidenceRegionParameters
	 */
	public TestStatisticParameters getConfidenceRegionParameters() {
		if (this.confidenceRegionParameters != null)
			return this.confidenceRegionParameters;
		
		return this.confidenceRegionParameters = new TestStatisticParameters(this.getTestStatistic(this.confidenceRegionDefinition));
	}
	
	/**
	 * Liefert die Teststatitikparameter fuer den Signifikanztest (Modellstoerungen)
	 * @return significanceTestStatisticParameters
	 */
	public TestStatisticParameters getSignificanceTestStatisticParameters() {
		if (this.significanceTestStatisticParameters != null)
			return this.significanceTestStatisticParameters;
		
		return this.significanceTestStatisticParameters = new TestStatisticParameters(this.getTestStatistic(this.testStatisticDefinition));
	}
	
	/**
	 * Liefert die Teststatitikparameter fuer den Signifikanztest (Modellstoerungen)
	 * @return significanceTestStatisticParameters
	 */
	public BinomialTestStatisticParameters getBinomialTestStatisticParameters() {
		if (this.binomialTestStatisticParameters != null)
			return this.binomialTestStatisticParameters;
		
		return this.binomialTestStatisticParameters = new BinomialTestStatisticParameters(this.getTestStatistic(this.testStatisticDefinition));
	}
			
	/**
	 * Liefert die Parameter der Teststatistic
	 * @return testStatistic
	 */
	TestStatistic getTestStatistic(TestStatisticDefinition testStatisticDefinition) {
		double alpha = testStatisticDefinition.getProbabilityValue();
		double beta  = testStatisticDefinition.getPowerOfTest();
		int dof = this.degreeOfFreedom();
		
		TestStatistic testStatistic;
		switch (testStatisticDefinition.getTestStatisticType()) {
		case SIDAK:
			// alle Hypothesen + Test der Varianzkomponenten + Test der Festpunkte
			testStatistic = new SidakTestStatistic(this.numberOfHypotesis, alpha, beta, testStatisticDefinition.isFamilywiseErrorRate());
			break;
		case BAARDA_METHOD:
			testStatistic = new BaardaMethodTestStatistic(testStatisticDefinition.isFamilywiseErrorRate() ? dof : 1, alpha, beta);
			break;
		case NONE:
			testStatistic = new UnadjustedTestStatitic(alpha, beta);
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, unknown test statistic method " + testStatisticDefinition.getTestStatisticType());
		}
		return testStatistic;
	}
		
	/**
	 * Liefert den aktuellen Status
	 * @return state
	 */
	public EstimationStateType getEstimationStateType() {
		return this.currentEstimationStatus;
	}
	
	/**
	 * Liefert <code>true</code>, wenn das Modell berechenbar war
	 * @return isEstimateModel
	 */
	public boolean isEstimateModel() {
		return this.currentEstimationStatus == EstimationStateType.ERROR_FREE_ESTIMATION;
	}
	
	/**
	 * Liefert den aktuellen Iterationsschritt
	 * @return iterationStep
	 */
	public int getCurrentIterationStep() {
		return this.iterationStep<0?0:this.iterationStep;
	}
	
	/**
	 * Liefert den Freiheitsgrad nach der Ausgleichung aus der Redundanzmatrix
	 * <code>f = n-u+d = spur(R)
	 * @return f
	 */
	public int degreeOfFreedom() {
		return (int)Math.rint(this.degreeOfFreedom);
	}
	
	/**
	 * Liefert die Anzahl der Beobachtungen im Netz
	 * @return numberOfObservations
	 */
	public int getNumberOfObservations() {
		return this.numberOfObservations;
	}
	
	/**
	 * Liefert die Anzahl an Parametern im Modell
	 * @return numberOfUnknownParameters
	 */
	public int getNumberOfUnknownParameters() {
		return this.numberOfUnknownParameters;
	}
		
	/**
	 * Liefert die Quadratsumme der Verbesserungen &Omega; = vTPv
	 * @return &Omega;
	 */
	public double getOmega() {
		return this.omega;
	}

	@Override
	public void run() {
		this.estimateModel();
	}
	
	public boolean calculateStochasticParameters() {
		return this.calculateStochasticParameters;
	}
	
	/**
	 * Setzt die Definition fuer die Konfidenzbereiche der Parameter
	 */
	public void setConfidenceRegionDefinition(TestStatisticDefinition confidenceRegionDefinition) {
		this.confidenceRegionDefinition = confidenceRegionDefinition;		
	}
	
	/**
	 * Setzt die Teststatistikdefinition fuer Hypothesentests
	 */
	public void setTestStatisticDefinition(TestStatisticDefinition testStatisticDefinition) {
		this.testStatisticDefinition = testStatisticDefinition;		
	}
	
	/**
	 * Liefert den Grenzwert, mit dem der Konvergenzfortschritt bemessen wird
	 * @return EPS
	 */
	public double getConvergenceThreshold() {
		return SQRT_EPS;
	}
	
	/**
	 * Liefert die max. Anzahl an Iterationen, die durchgefuehrt werden 
	 * @return maxIter
	 */
	public int getMaximalNumberOfIterations() {
		return this.maximalNumberOfIterations;
	}
	
	/**
	 * Liefert das Schaetzverfahren
	 * @return estimationType
	 */
	public EstimationType getEstimationType() {
		return this.estimationType;
	}
	
	/**
	 * Legt die max. Anzahl an Iterationen fest
	 * @param newMaxIterations Iterationen
	 */
	public void setMaximalNumberOfIterations(int maximalNumberOfIterations) {
		if (maximalNumberOfIterations < 0 || maximalNumberOfIterations > DefaultValue.getMaximumNumberOfIterations() )
			this.maximalNumberOfIterations = DefaultValue.getMaximumNumberOfIterations();
		else
			this.maximalNumberOfIterations = maximalNumberOfIterations;
	}
	
	/**
	 * Legt das Schaetzverfahren fest
	 * @param estimationType
	 */
	public void setEstimationType(EstimationType estimationType) {
		this.estimationType = estimationType;
	}
	
	/**
	 * Beruecksichtigung des geschaetzten Varianzfaktors zur Skallierung der Kovarianzmatrix
	 * @param applyAposterioriVarianceOfUnitWeight
	 */
	public void setApplyAposterioriVarianceOfUnitWeight(boolean applyAposterioriVarianceOfUnitWeight) {
		this.applyAposterioriVarianceOfUnitWeight = applyAposterioriVarianceOfUnitWeight;
	}
	
	/**
	 * Deformationsanalyse durchfuehren
	 * @param congruenceAnalysis
	 */
	public void setCongruenceAnalysis(boolean congruenceAnalysis) {
		this.congruenceAnalysis = congruenceAnalysis;
	}
	
	/**
	 * Maximale Abweichung der AGL-Probe 
	 * @return finalLinearisationError
	 */
	public double getFinalLinearisationError() {
		return this.finalLinearisationError;
	}
	
	/**
	 * Legt die Intervallsgrenze fuer robuste Ausgleichung fest
	 * @param robustEstimationLimit
	 */
	public void setRobustEstimationLimit(double robustEstimationLimit) {
		this.robustEstimationLimit = robustEstimationLimit > 0  ?  robustEstimationLimit : DefaultValue.getRobustEstimationLimit();
	}
	
	/**
	 * Liefert den Skalierungsparameter alpha der UT,
	 * der den Abstand der Sigma-Punkte um den Mittelwert
	 * steuert. Ueblicherweise ist alpha gering, d.h., 1E-3
	 * Der Defaultwert ist 1, sodass keine Skalierung
	 * vorgenommen wird und die Standard-UT resultiert
	 * @return alphaUT
	 */
	public double getUnscentedTransformationScaling() {
		return this.alphaUT;
	}
	
	/**
	 * Liefert den Daempfungsparameter beta der UT,
	 * der a-priori Informatione bzgl. der Verteilung
	 * der Daten beruecksichtigt. Fuer Gauss-Verteilung
	 * ist beta = 2 optimal
	 * @return betaUT
	 */
	public double getUnscentedTransformationDamping() {
		return this.betaUT;
	}
	
	/**
	 * Liefert die Gewichtung des Sigma-Punktes X0 bzw. Y0 = f(X0)
	 * @return w0
	 */
	public double getUnscentedTransformationWeightZero() {
		return this.weightZero;
	}
	
	/**
	 * setzt den Skalierungsparameter alpha der UT,
	 * der den Abstand der Sigma-Punkte um den Mittelwert
	 * steuert. Ueblicherweise ist alpha gering, d.h., 1E-3
	 * Der Defaultwert ist 1, sodass keine Skalierung
	 * vorgenommen wird und die Standard-UT resultiert
	 * @param alpha
	 */
	public void setUnscentedTransformationScaling(double alpha) {
		if (alpha > 0)
			this.alphaUT = alpha;
	}
	
	/**
	 * Liefert den Daempfungsparameter beta der UT,
	 * der a-priori Informatione bzgl. der Verteilung
	 * der Daten beruecksichtigt. Fuer Gauss-Verteilung
	 * ist beta = 2 optimal
	 * @param beta
	 */
	public void setUnscentedTransformationDamping(double beta) {
		this.betaUT = beta;
	}
	
	/**
	 * Setzt die Gewichtung des Sigma-Punktes X0 bzw. Y0 = f(X0)
	 * @param w0
	 */
	public void setUnscentedTransformationWeightZero(double w0) {
		if (w0 < 1) 
			this.weightZero = w0;
	}
	
	/**
	 * Liefert die Jacobimatrix der Ausgleichung
	 * @return A
	 * @deprecated - Nur fuer DEBUG-Ausgabe
	 */
	Matrix getJacobiMatrix() {
		Matrix A = new DenseMatrix(this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows, this.numberOfUnknownParameters);
		for (int u=0; u<this.unknownParameters.size(); u++) {
			UnknownParameter unknownParameterA = this.unknownParameters.get(u);
			ObservationGroup observaionGroupA = unknownParameterA.getObservations();
			int colA = unknownParameterA.getColInJacobiMatrix();
			
			int dimA = 1;
			switch (unknownParameterA.getParameterType()) {
			case POINT2D:
				dimA = 2;
				break;
			case POINT3D:
				dimA = 3;
				break;
			default:
				dimA = 1;
				break;
			}

			for (int i=0; i<dimA; i++) {
				//colA += i;
				colA = unknownParameterA.getColInJacobiMatrix() + i;
			
				for (int j=0; j<observaionGroupA.size(); j++) {
					Observation observationA = observaionGroupA.get(j);
					double a = 0.0;
					int rowA = observationA.getRowInJacobiMatrix();

					if (unknownParameterA.getParameterType() == ParameterType.POINT1D) {
						Point p = (Point)unknownParameterA;
						if (p.equals(observationA.getStartPoint())) {
							a = observationA.diffZs();
						}
						else if (p.equals(observationA.getEndPoint())) {
							a = observationA.diffZe();
						}
					}
					else if (unknownParameterA.getParameterType() == ParameterType.POINT2D) {
						Point p = (Point)unknownParameterA;
						if (p.equals(observationA.getStartPoint())) {
							if (i==0)
								a = observationA.diffXs();
							else if (i==1)
								a = observationA.diffYs();
						}
						else if (p.equals(observationA.getEndPoint())) {
							if (i==0)
								a = observationA.diffXe();
							else if (i==1)
								a = observationA.diffYe();
						}
					}
					else if (unknownParameterA.getParameterType() == ParameterType.POINT3D) {
						Point p = (Point)unknownParameterA;
						if (p.equals(observationA.getStartPoint())) {
							if (i==0)
								a = observationA.diffXs();
							else if (i==1)
								a = observationA.diffYs();
							else if (i==2)
								a = observationA.diffZs();
						}
						else if (p.equals(observationA.getEndPoint())) {
							if (i==0)
								a = observationA.diffXe();
							else if (i==1)
								a = observationA.diffYe();
							else if (i==2)
								a = observationA.diffZe();
						}
					}
					else if (unknownParameterA.getParameterType() == ParameterType.VERTICAL_DEFLECTION_X) {
						VerticalDeflectionX deflection = (VerticalDeflectionX)unknownParameterA;
						Point p = deflection.getPoint();
						if (p.equals(observationA.getStartPoint()))
							a = observationA.diffVerticalDeflectionXs();
						else if (p.equals(observationA.getEndPoint()))
							a = observationA.diffVerticalDeflectionXe();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.VERTICAL_DEFLECTION_Y) {
						VerticalDeflectionY deflection = (VerticalDeflectionY)unknownParameterA;
						Point p = deflection.getPoint();
						if (p.equals(observationA.getStartPoint()))
							a = observationA.diffVerticalDeflectionYs();
						else if (p.equals(observationA.getEndPoint()))
							a = observationA.diffVerticalDeflectionYe();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.ORIENTATION) {
						a = observationA.diffOri();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.ZERO_POINT_OFFSET) {
						a = observationA.diffAdd();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.SCALE) {
						a = observationA.diffScale();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.REFRACTION_INDEX) {
						a = observationA.diffRefCoeff();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_X) {
						a = observationA.diffRotX();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_Y) {
						a = observationA.diffRotY();
					}
					else if (unknownParameterA.getParameterType() == ParameterType.ROTATION_Z) {
						a = observationA.diffRotZ();
					}
					A.set(rowA, colA, a);
				}
			}
		}
		
		// Stochastische Punkte hinzufuegen
		for (Point point : this.pointsWithStochasticDeflection) {
			VerticalDeflection deflectionX = point.getVerticalDeflectionX();
			VerticalDeflection deflectionY = point.getVerticalDeflectionY();
			
			A.set(deflectionX.getRowInJacobiMatrix(), deflectionX.getColInJacobiMatrix(), 1.0);
			A.set(deflectionY.getRowInJacobiMatrix(), deflectionY.getColInJacobiMatrix(), 1.0);
		}

		// Stochastische Punkte hinzufuegen
		for (Point point : this.stochasticPoints) {
			int col = point.getColInJacobiMatrix();
			int row = point.getRowInJacobiMatrix();

			if (point.getDimension() != 1) {
				A.set(row++, col++, 1.0);
				A.set(row++, col++, 1.0);
			}
			if (point.getDimension() != 2) {
				A.set(row, col, 1.0);
			}
		}
		return A;
	}
	
	/**
	 * Liefert die Hauptdiagonale der a-priori Gewichtsmatrix
	 * @return W
	 * @deprecated - Nur fuer DEBUG-Ausgabe
	 */
	Vector getWeightedMatrix() {
		Vector W = new DenseVector(this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows);
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			W.set(observation.getRowInJacobiMatrix(), 1.0/observation.getStdApriori()/observation.getStdApriori());
		}
		
		for (Point point : this.pointsWithStochasticDeflection) {
			VerticalDeflection deflectionX = point.getVerticalDeflectionX();
			VerticalDeflection deflectionY = point.getVerticalDeflectionY();

			W.set(deflectionX.getRowInJacobiMatrix(), 1.0/deflectionX.getStdApriori()/deflectionX.getStdApriori());
			W.set(deflectionY.getRowInJacobiMatrix(), 1.0/deflectionY.getStdApriori()/deflectionY.getStdApriori());
		}
		
		for (Point point : this.stochasticPoints) {
			int dim = point.getDimension();
			int row = point.getRowInJacobiMatrix();
			
			if (dim != 1) {
				W.set(row++, 1.0/point.getStdXApriori()/point.getStdXApriori());
				W.set(row++, 1.0/point.getStdYApriori()/point.getStdYApriori());
			}
			if (dim != 2) 
				W.set(row++, 1.0/point.getStdZApriori()/point.getStdZApriori());
		}
		return W;
	}
	
	/**
	 * Liefert die Verbesserungen nach der Ausgleichung IST - SOLL
	 * @return e
	 */
	private Vector getObservationalErrors() {
		Vector e = new DenseVector(this.numberOfObservations + this.numberOfStochasticPointRows + this.numberOfStochasticDeflectionRows);
		for (int i=0; i<this.numberOfObservations; i++) {
			Observation observation = this.projectObservations.get(i);
			e.set(observation.getRowInJacobiMatrix(), observation.getObservationalError());
		}
		
		for (Point point : this.pointsWithStochasticDeflection) {
			VerticalDeflection deflectionX = point.getVerticalDeflectionX();
			VerticalDeflection deflectionY = point.getVerticalDeflectionY();
			
			if (deflectionX.getRowInJacobiMatrix() < 0 || deflectionY.getRowInJacobiMatrix() < 0)
				continue;
			
			e.set(deflectionX.getRowInJacobiMatrix(), deflectionX.getValue0() - deflectionX.getValue());
			e.set(deflectionY.getRowInJacobiMatrix(), deflectionY.getValue0() - deflectionY.getValue());
		}

		for (Point point : this.stochasticPoints) {
			int dim = point.getDimension();
			int row = point.getRowInJacobiMatrix();
			
			if (dim != 1) {
				e.set(row++, point.getX0()-point.getX());
				e.set(row++, point.getY0()-point.getY());
			}
			if (dim != 2) 
				e.set(row++, point.getZ0()-point.getZ());
		}
		return e;
	}
	
	/**
	 * Liefert den Varianzfaktor nach der Ausgleichung
	 * Ist der Freiheitsgrad oder die Verbesserungsquadratsumme 
	 * gleich Null, so wird 1 (Varianzfaktor a-priori) 
	 * zurueckgegeben
	 * Bei einer Praeanalyse wird immer 1 ausgegeben
	 * @return sigma2apost
	 */
	public double getVarianceFactorAposteriori() {
		return this.degreeOfFreedom > 0 && this.omega > 0 && this.estimationType != EstimationType.SIMULATION && this.applyAposterioriVarianceOfUnitWeight ? Math.abs(this.omega/this.degreeOfFreedom) : 1.0;
	}
	
	/**
	 * Liefert die Varianzanteile der einzelnen Beobachtungstypen
	 * @return vks
	 */
	public Map<VarianceComponentType, VarianceComponent> getVarianceComponents() {
		return this.varianceComponents;
	}
	
	/**
	 * Liefert die mittlere Redundanz (Bedingungsdichte) im Netz r<sub>m</sub> = 1-u/(n+d)
	 * @return r<sub>m</sub>
	 */
	public double getMeanRedundancy() {
		int nd = this.degreeOfFreedom()+this.numberOfUnknownParameters; 
		return nd>0?1.0-(double)this.numberOfUnknownParameters/nd:0.0;
	}
	
	/**
	 * Liefert die trace(Qxx) fuer den Punkt-bezogenen Anteil aus Qxx
	 * @return trace(Qxx)
	 */
	public double getTraceOfCovarianceMatrixOfPoints() {
		return this.traceCxxPoints;
	}
	
	/**
	 * Setzt die Anzahl der zubestimmenden Hauptkomponenten
	 * @param numberOfPrincipalComponents
	 */
	public void setNumberOfPrincipalComponents(int numberOfPrincipalComponents) {
		this.numberOfPrincipalComponents = numberOfPrincipalComponents < 0 ? 0 : numberOfPrincipalComponents;
	}
	
	/**
	 * Liefert die ersten n. Hauptkomponente der Hauptkomponentenanalyse (maximale Eigenwerte von Cxx)
	 * @return fpc
	 */
	public PrincipalComponent[] getPrincipalComponents() {
		return this.principalComponents;
	}
	
	/**
	 * Fuehrt eine Hauptkomponentenanalyse durch und bestimmt die k Hauptkomponente. Hierbei wird Qxx ueberschrieben.
	 * Um Fehler zu vermeiden, wird Qxx = null am Ende gesetzt.
	 * @param numberOfComponents Anzahl der zu bestimmenden Komponenten
	 */
	private void estimatePrincipalComponentAnalysis(int numberOfComponents) {
		try {
			this.currentEstimationStatus = EstimationStateType.PRINCIPAL_COMPONENT_ANALYSIS;
	    	this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			double sigma2apost = this.getVarianceFactorAposteriori();
			int n = this.unknownParameters.columnsOfPoints();

			if (this.Qxx != null && n > 0 && numberOfComponents > 0) {				
				// Der Index ist Eins-Index-basierend, d.h., der kleinste Eigenwert hat den Index Eins und der groesste ist am Index n!
				Matrix evalEvec[] = MathExtension.eig(this.Qxx, n, Math.max(n - numberOfComponents + 1, 1), n, true);
				Matrix eval = (UpperSymmBandMatrix)evalEvec[0];
				Matrix evec = (DenseMatrix)evalEvec[1];
				// Anzahl der tatsaechlich bestimmten Komponenten
				numberOfComponents = eval.numColumns();
				double sqrtFPC = 0.0;
				this.principalComponents = new PrincipalComponent[numberOfComponents];
				for (int i = 0; i < numberOfComponents; i++) {
					PrincipalComponent principalComponent = new PrincipalComponent(n - i, Math.abs(sigma2apost * eval.get(numberOfComponents - 1 - i, numberOfComponents - 1 - i)));
					this.principalComponents[i] = principalComponent;
					if (i == 0)
						sqrtFPC = Math.sqrt(principalComponent.getValue());
				}

				for (int i=0; i<this.unknownParameters.size(); i++) {
					if (this.interrupt)
						return;

					UnknownParameter unknownParameter = this.unknownParameters.get(i);
					int row = unknownParameter.getColInJacobiMatrix();
					int col = numberOfComponents - 1;
					if (unknownParameter instanceof Point && row >= 0) {
						Point point = (Point)unknownParameter;
						int dim = point.getDimension();
						double principalComponents[] = new double[dim];
						if (dim != 1) {
							principalComponents[0] = sqrtFPC * evec.get(row++, col);
							principalComponents[1] = sqrtFPC * evec.get(row++, col);
						}
						if (dim != 2) {
							principalComponents[dim - 1] = sqrtFPC * evec.get(row++, col);
						}
						point.setFirstPrincipalComponents(principalComponents);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.Qxx = null;
		}
	}
	
	private void applySphericalVerticalDeflections() {
		if (this.sphericalDeflectionModel != null) {
			for (Point point : this.allPoints.values()) {
				this.sphericalDeflectionModel.setSphericalDeflections(point);
			}
		}
	}
	
	public void setSphericalDeflectionModel(SphericalDeflectionModel sphericalDeflectionModel) {
		this.sphericalDeflectionModel = sphericalDeflectionModel;
	}
	
	public boolean hasAdjustmentResultWriter() {
		return this.adjustmentResultWriter != null;
	}

	public void setAdjustmentResultWritable(AdjustmentResultWritable adjustmentResultWriter) {
		this.adjustmentResultWriter = adjustmentResultWriter;
	}
	
	private void exportAdjustmentResults() throws NullPointerException, IOException {
		if (this.adjustmentResultWriter == null)
			return;
		
		this.currentEstimationStatus = EstimationStateType.EXPORT_ADJUSTMENT_RESULTS;
		if (this.adjustmentResultWriter instanceof NetworkAdjustmentResultWriter)
			this.change.firePropertyChange(this.currentEstimationStatus.name(), null, ((NetworkAdjustmentResultWriter)this.adjustmentResultWriter).getExportPathAndFileBaseName());
		this.adjustmentResultWriter.export(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.change.removePropertyChangeListener(listener);
	}
	
	public void clearMatrices() {
		this.Qxx = null;
		this.ATQxxBP_GNSS_EP = null;
		this.PAzTQzzAzP_GNSS_EF = null;
	}
}
