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

package org.applied_geodesy.adjustment.geometry;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.NormalEquationSystem;
import org.applied_geodesy.adjustment.UnscentedTransformationParameter;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.adjustment.statistic.BaardaMethodTestStatistic;
import org.applied_geodesy.adjustment.statistic.SidakTestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.adjustment.statistic.UnadjustedTestStatitic;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.NotConvergedException.Reason;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.UpperTriangPackMatrix;
import no.uib.cipr.matrix.Vector;

public class FeatureAdjustment {
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	private List<EventListener> listenerList = new ArrayList<EventListener>();

	private Feature feature;
	private List<UnknownParameter> parameters = new ArrayList<UnknownParameter>();
	private List<Restriction> restrictions    = new ArrayList<Restriction>();
	private List<GeometricPrimitive> geometricPrimitives = new ArrayList<GeometricPrimitive>();
	
	private List<FeaturePoint> points = new ArrayList<FeaturePoint>();
	
	private EstimationStateType currentEstimationStatus = EstimationStateType.BUSY;
	private EstimationType estimationType = EstimationType.L2NORM;
	
	private TestStatisticDefinition testStatisticDefinition = new TestStatisticDefinition(TestStatisticType.BAARDA_METHOD, DefaultValue.getProbabilityValue(), DefaultValue.getPowerOfTest(), false);
	private TestStatisticParameters testStatisticParameters = null;
	
	private static double SQRT_EPS = Math.sqrt(Constant.EPS);
	private int maximalNumberOfIterations = DefaultValue.getMaximalNumberOfIterations(),
			iterationStep                 = 0,
			numberOfModelEquations        = 0,
			numberOfUnknownParameters     = 0,
			maximumNumberOfGeometricPrimitivesPerPoint = 0;
	
	private boolean interrupt             = false,
			calculateStochasticParameters = false,
			adjustModelParametersOnly     = false,
			preconditioning      = true;

	private double maxAbsDx     = 0.0,
			maxAbsRestriction   = 0.0,
			lastValidmaxAbsDx   = 0.0,
			dampingValue        = 0.0,
			adaptedDampingValue = 0.0,
			alphaUT             = UnscentedTransformationParameter.getAlpha(),
            betaUT              = UnscentedTransformationParameter.getBeta(),
            weightZero          = UnscentedTransformationParameter.getWeightZero();
	
	private VarianceComponent varianceComponentOfUnitWeight = new VarianceComponent();
	private UpperSymmPackMatrix Qxx = null;

	
	public void init() {
		int nog = this.geometricPrimitives.size();

		if (nog > 0) {
			List<FeaturePoint> nonUniquePoints = new ArrayList<FeaturePoint>();
			for (GeometricPrimitive geometry : this.geometricPrimitives)
				nonUniquePoints.addAll(geometry.getFeaturePoints());
			
			// create a unique set of points
			LinkedHashSet<FeaturePoint> uniquePoints = new LinkedHashSet<FeaturePoint>(nonUniquePoints);
			nonUniquePoints.clear();
			
			// reset points
			for (FeaturePoint featurePoint : uniquePoints)
				featurePoint.reset();

			// filter enabled points
			FilteredList<FeaturePoint> enabledUniquePoints = new FilteredList<FeaturePoint>(FXCollections.<FeaturePoint>observableArrayList(uniquePoints));
			enabledUniquePoints.setPredicate(
					new Predicate<FeaturePoint>(){
						public boolean test(FeaturePoint featurePoint){
							return featurePoint.isEnable();
						}
					}
			);
			uniquePoints.clear();
			
			// Unique point list
			this.points = new ArrayList<FeaturePoint>(enabledUniquePoints);

			// number of equations rows in Jacobian A, B
			this.numberOfModelEquations = 0;
			for (FeaturePoint featurePoint : this.points)  {
				int numberOfGeometries = featurePoint.getNumberOfGeomtries();
				this.maximumNumberOfGeometricPrimitivesPerPoint = Math.max(this.maximumNumberOfGeometricPrimitivesPerPoint, numberOfGeometries);
				this.numberOfModelEquations += numberOfGeometries;
				featurePoint.getTestStatistic().setVarianceComponent(this.varianceComponentOfUnitWeight);
			}
			
//			// set column indices to unknown parameters in normal equation
//			// Please note: the following condition holds this.numberOfUnknownParameters <= this.parameters.size()
//			// because some parameters may be held fixed or will be estimated during post-processing
//			this.numberOfUnknownParameters = 0;
//			int column = 0;
//			for (UnknownParameter unknownParameter : this.parameters) {
//				if (unknownParameter.getProcessingType() == ProcessingType.ADJUSTMENT) { //  && unknownParameter.getColumn() < 0
//					unknownParameter.setColumn(column++);
//					this.numberOfUnknownParameters++;
//				}
//				else {
//					unknownParameter.setColumn(-1);
//				}
//			}
//
//			// set row indices to restrictions, i.e., row/column of Jacobian R in normal equation (behind unknown parameters)
//			for (Restriction restriction : this.restrictions)
//				restriction.setRow(column++);
//			
//			List<Restriction> calculations = this.feature.getPostProcessingCalculations();
//			for (Restriction restriction : calculations)
//				restriction.setRow(-1);
			
			this.testStatisticParameters = this.getTestStatisticParameters(this.testStatisticDefinition);
		}
	}
	
	public Feature getFeature() {
		return this.feature;
	}

	public void setFeature(Feature feature) {
		if (this.feature != null)
			this.fireFeatureChanged(this.feature, FeatureEventType.FEATURE_REMOVED);
				
		this.reset();
		this.feature = feature;
				
		if (this.feature != null) {
			this.parameters          = this.feature.getUnknownParameters();
			this.restrictions        = this.feature.getRestrictions();
			this.geometricPrimitives = this.feature.getGeometricPrimitives();
			this.fireFeatureChanged(this.feature, FeatureEventType.FEATURE_ADDED);
		}
	}
	
	private void reset() {
		this.varianceComponentOfUnitWeight.setVariance0(1.0);
		this.varianceComponentOfUnitWeight.setOmega(0.0);
		this.varianceComponentOfUnitWeight.setRedundancy(0.0);
		this.geometricPrimitives.clear();
		this.parameters.clear();
		this.restrictions.clear();
		this.points.clear();
		this.Qxx = null;
	}
	
	private void prepareIterationProcess(Point centerOfMass) {
		// set warm start solution x <-- x0
		this.feature.applyInitialGuess();
		// reset center of mass
		this.feature.getCenterOfMass().setX0(0);
		this.feature.getCenterOfMass().setY0(0);
		this.feature.getCenterOfMass().setZ0(0);

		// set center of mass
		if (this.feature.isEstimateCenterOfMass()) 
			this.feature.setCenterOfMass(centerOfMass);

		// set column indices to unknown parameters in normal equation
		// Please note: the following condition holds this.numberOfUnknownParameters <= this.parameters.size()
		// because some parameters may be held fixed or will be estimated during post-processing
		this.numberOfUnknownParameters = 0;
		int parColumn = 0;
		for (UnknownParameter unknownParameter : this.parameters) {
			if (unknownParameter.getProcessingType() == ProcessingType.ADJUSTMENT) { //  && unknownParameter.getColumn() < 0
				unknownParameter.setColumn(parColumn++);
				this.numberOfUnknownParameters++;
			}
			else {
				unknownParameter.setColumn(-1);
			}
		}

		// set row indices to restrictions, i.e., row/column of Jacobian R in normal equation (behind unknown parameters)
		for (Restriction restriction : this.restrictions)
			restriction.setRow(parColumn++);

		List<Restriction> calculations = this.feature.getPostProcessingCalculations();
		for (Restriction restriction : calculations)
			restriction.setRow(-1);

		// reset feature points
		for (FeaturePoint featurePoint : this.points)
			featurePoint.reset();
	}
	
	public EstimationStateType estimateModel() throws NotConvergedException, MatrixSingularException, OutOfMemoryError {
		boolean applyUnscentedTransformation = this.estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION;
		
		this.currentEstimationStatus = EstimationStateType.BUSY;
		this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
		
		try {
			this.Qxx = null;
			int dim    = this.feature.getFeatureType() == FeatureType.CURVE ? 2 : 3;
			int numObs = this.points.size() * dim;
			int numberOfEstimationSteps = applyUnscentedTransformation ? numObs + 2 : 1;

			double alpha2  = this.alphaUT * this.alphaUT;
			double weight0 = this.weightZero;
			double weighti = (1.0 - weight0) / (double)(numberOfEstimationSteps - 1.0);

			double SigmaUT[][] = null;
			Vector xUT = null, vUT = null;
			Matrix solutionVectors = null;
			
			Point centerOfMass = Feature.deriveCenterOfMass(this.feature.getFeaturePoints());
			
			if (applyUnscentedTransformation) {
				int numUnfixedParams = 0;
				for (UnknownParameter unknownParameter : this.parameters)
					numUnfixedParams += unknownParameter.getProcessingType() != ProcessingType.FIXED ? 1 : 0;
					
				xUT = new DenseVector(numUnfixedParams); // this.numberOfUnknownParameters
				vUT = new DenseVector(numObs);
				solutionVectors = new DenseMatrix(numUnfixedParams, numberOfEstimationSteps); // this.numberOfUnknownParameters, numberOfEstimationSteps
				
				SigmaUT = new double[numObs][dim];
				if (weight0 < 0 || weight0 >= 1)
					throw new IllegalArgumentException("Error, zero-weight is out of range. If SUT is applied, valid values are 0 <= w0 < 1! " + weight0);

				weight0 = weight0 / alpha2 + (1.0 - 1.0 / alpha2);
				weighti = weighti / alpha2;
			}
			
			for (int estimationStep = 0; estimationStep < numberOfEstimationSteps; estimationStep++) {
				this.prepareIterationProcess(new Point(centerOfMass));

				this.adaptedDampingValue = this.dampingValue;
				int runs = this.maximalNumberOfIterations-1;
				boolean isEstimated = false, estimateCompleteModel = false, isFirstIteration = true;

				if (this.maximalNumberOfIterations == 0) {
					estimateCompleteModel = isEstimated = true;
					isFirstIteration = false;
					this.adaptedDampingValue = 0;
				}

				this.maxAbsDx = 0.0;
				this.maxAbsRestriction = 0.0;
				this.lastValidmaxAbsDx = 0.0;
				runs = this.maximalNumberOfIterations-1;
				isEstimated = false;
				estimateCompleteModel = false;

				double sigma2apriori = this.getEstimateVarianceOfUnitWeightApriori();
				this.varianceComponentOfUnitWeight.setVariance0(1.0);
				this.varianceComponentOfUnitWeight.setOmega(0.0);
				this.varianceComponentOfUnitWeight.setRedundancy(this.numberOfModelEquations - this.numberOfUnknownParameters + this.restrictions.size());
				this.varianceComponentOfUnitWeight.setVariance0( sigma2apriori < SQRT_EPS ? SQRT_EPS : sigma2apriori );
				
				if (applyUnscentedTransformation) {
					this.currentEstimationStatus = EstimationStateType.UNSCENTED_TRANSFORMATION_STEP;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), numberOfEstimationSteps, estimationStep+1);
					this.prepareSphericalSimplexUnscentedTransformationObservation(estimationStep, SigmaUT, weighti);
				}

				do {
					this.maxAbsDx = 0.0;
					this.maxAbsRestriction = 0.0;
					this.iterationStep = this.maximalNumberOfIterations-runs;
					this.currentEstimationStatus = EstimationStateType.ITERATE;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), this.maximalNumberOfIterations, this.iterationStep);
					this.feature.prepareIteration();

					// create the normal system of equations including restrictions
					NormalEquationSystem neq = this.createNormalEquation();

					if (this.interrupt || neq == null) {
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						this.interrupt = false;
						return this.currentEstimationStatus;
					}

					// apply pre-conditioning to archive a stable normal equation
					if (this.preconditioning)
						this.applyPrecondition(neq);

					DenseVector n = neq.getVector();
					UpperSymmPackMatrix N = neq.getMatrix();

					if (!isFirstIteration) 
						estimateCompleteModel = isEstimated;

					try {
						if ((estimateCompleteModel && estimationStep == (numberOfEstimationSteps - 1)) || this.estimationType == EstimationType.L1NORM) {
							this.calculateStochasticParameters = (this.estimationType != EstimationType.L1NORM && estimateCompleteModel);

							if (this.estimationType != EstimationType.L1NORM) {
								this.currentEstimationStatus = EstimationStateType.INVERT_NORMAL_EQUATION_MATRIX;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
							}

							// in-place estimation normal system N * x = n: N <-- Qxx, n <-- dx 
							MathExtension.solve(N, n, !applyUnscentedTransformation);
							if (!applyUnscentedTransformation) {
								if (this.preconditioning)
									this.applyPrecondition(neq.getPreconditioner(), N, n);	
								
								// extract part of unknown parameters to Qxx
								if (this.numberOfUnknownParameters != N.numColumns()) {
									this.Qxx = new UpperSymmPackMatrix(this.numberOfUnknownParameters);
									for (int r = 0; r < this.parameters.size(); r++) {
										UnknownParameter parameterRow = this.parameters.get(r);
										int row = parameterRow.getColumn();
										if (row < 0)
											continue;
										for (int c = r; c < this.parameters.size(); c++) {
											UnknownParameter parameterCol = this.parameters.get(c);
											int column = parameterCol.getColumn();
											if (column < 0)
												continue;
											this.Qxx.set(row, column, N.get(row, column));
										}
									}
								}
								else {
									this.Qxx = N;
								}
							}
							else {
								if (this.preconditioning)
									this.applyPrecondition(neq.getPreconditioner(), null, n);
							}

							if (this.calculateStochasticParameters) {
								this.currentEstimationStatus = EstimationStateType.ESTIAMTE_STOCHASTIC_PARAMETERS;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
							}
						}
						else {
							// in-place estimates of N * x = n, vector n is replaced by the solution vector x
							MathExtension.solve(N, n, false);
							if (this.preconditioning)
								this.applyPrecondition(neq.getPreconditioner(), null, n);
						}

						N = null;
						// n == [dx k]' (in-place estimation)
						this.updateModel(n, estimateCompleteModel);

						n = null;
						
						if (applyUnscentedTransformation) {
							if (estimateCompleteModel) {
								this.addUnscentedTransformationSolution(xUT, vUT, solutionVectors, estimationStep, estimationStep < (numberOfEstimationSteps - 1) ? weighti : weight0);
							}
							
							// Letzter Durchlauf der UT
							// Bestimme Parameterupdate dx und Kovarianzmatrix Qxx
							if (estimateCompleteModel && estimationStep > 0 && estimationStep == (numberOfEstimationSteps - 1)) {
								this.estimateUnscentedTransformationParameterUpdateAndDispersion(xUT, solutionVectors, vUT, weighti, weight0 + (1.0 - alpha2 + this.betaUT));
							}
						}
					}
					catch (MatrixSingularException | MatrixNotSPDException | IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException e) {
						if (applyUnscentedTransformation && SigmaUT != null) 
							this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
						e.printStackTrace();
						this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						throw new MatrixSingularException("Error, normal equation matrix is singular!\r\n" + e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "");
					}
					catch (Exception e) {
						if (applyUnscentedTransformation && SigmaUT != null) 
							this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
						e.printStackTrace();
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						return this.currentEstimationStatus;
					}

					if (this.interrupt) {
						if (applyUnscentedTransformation && SigmaUT != null) 
							this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
						this.currentEstimationStatus = EstimationStateType.INTERRUPT;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						this.interrupt = false;
						return this.currentEstimationStatus;
					}

					if (Double.isInfinite(this.maxAbsDx) || Double.isNaN(this.maxAbsDx)) {
						if (applyUnscentedTransformation && SigmaUT != null) 
							this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
						this.currentEstimationStatus = EstimationStateType.NO_CONVERGENCE;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						throw new NotConvergedException(Reason.Breakdown, "Error, iteration process breaks down!");
					}
					else if (!isFirstIteration && this.maxAbsDx <= SQRT_EPS && this.maxAbsRestriction <= SQRT_EPS && runs > 0 && this.adaptedDampingValue == 0) {
						isEstimated = true;
						this.currentEstimationStatus = EstimationStateType.CONVERGENCE;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, Math.max(this.maxAbsDx, this.maxAbsRestriction));
					}
					else if (runs-- <= 1) {
						if (estimateCompleteModel) {
							if (this.estimationType == EstimationType.L1NORM) {
								if (applyUnscentedTransformation && SigmaUT != null) 
									this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
								this.currentEstimationStatus = EstimationStateType.ROBUST_ESTIMATION_FAILD;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
								throw new NotConvergedException(Reason.Iterations, "Error, euqation system does not converge! Last iterate max|dx| = " + this.maxAbsDx + " (" + SQRT_EPS + ").");
							}
							else {
								if (applyUnscentedTransformation && SigmaUT != null) 
									this.prepareSphericalSimplexUnscentedTransformationObservation(-1, SigmaUT, 0);
								this.currentEstimationStatus = EstimationStateType.NO_CONVERGENCE;
								this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxAbsDx);
								throw new NotConvergedException(Reason.Iterations, "Error, euqation system does not converge! Last iterate max|dx| = " + this.maxAbsDx + " (" + SQRT_EPS + ").");
							}
						}
						isEstimated = true;
					}
					else {
						this.currentEstimationStatus = EstimationStateType.CONVERGENCE;
						this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxAbsDx);
					}
					isFirstIteration = false;

					if (isEstimated || this.adaptedDampingValue <= SQRT_EPS || runs < this.maximalNumberOfIterations * 0.1 + 1)
						this.adaptedDampingValue = 0.0;
				}
				while (!estimateCompleteModel);
				
				// System.out.println(estimationStep + ". max(|dx|) = " + this.maxAbsDx + "; omega = " + this.varianceComponentOfUnitWeight.getOmega() + "; itr = " + this.iterationStep);
			}
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			this.currentEstimationStatus = EstimationStateType.OUT_OF_MEMORY;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			throw new OutOfMemoryError();
		}
		finally {
			// Reset the center of mass
			this.feature.getCenterOfMass().setX0(0);
			this.feature.getCenterOfMass().setY0(0);
			this.feature.getCenterOfMass().setZ0(0);
		}

		if (this.currentEstimationStatus.getId() == EstimationStateType.BUSY.getId() || this.calculateStochasticParameters) {
			this.currentEstimationStatus = EstimationStateType.ERROR_FREE_ESTIMATION;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxAbsDx);
		}

//		System.out.println("max(|dx|) = " + this.maxAbsDx + "; omega = " + this.varianceComponentOfUnitWeight.getOmega() + "; itr = " + this.iterationStep);
//		for (UnknownParameter unknownParameter : this.parameters)
//			System.out.println(unknownParameter);
		return this.currentEstimationStatus;
	}
	
	private double getEstimateVarianceOfUnitWeightApriori() {
		double vari = 0;
		int cnt = 0;
		for (FeaturePoint point : this.points) {
			if (this.interrupt)
				return 1.0;

			int dim = point.getDimension();
			
			Matrix D = point.getDispersionApriori();
			for (int rowD = 0; rowD < dim; rowD++) {
				double var = D.get(rowD, rowD);
				vari += var;
				cnt++;
			}
		}
		return vari / cnt;
	}
	
	private void reverseCenterOfMass() {
		for (GeometricPrimitive geometricPrimitive : this.geometricPrimitives) 
			geometricPrimitive.reverseCenterOfMass(this.Qxx);
		
		// Reset the center of mass
		this.feature.getCenterOfMass().setX0(0);
		this.feature.getCenterOfMass().setY0(0);
		this.feature.getCenterOfMass().setZ0(0);
	}

	private void postProcessing() {
		List<Restriction> calculations = this.feature.getPostProcessingCalculations();
		int numberOfUnknownParameters = this.numberOfUnknownParameters; // parameters to be estimated during adjustment 
		for (Restriction restriction : calculations) {
			UnknownParameter parameter = restriction.getRegressand();
			if (parameter.getProcessingType() != ProcessingType.POSTPROCESSING)
				continue;
			
			if (this.Qxx != null) {
				int rows    = this.Qxx.numRows();
				int columns = this.Qxx.numColumns();

				// column of parameter
				int column = -1;

				if (parameter.getColumn() < 0) {
					column = columns;
					columns++;
				}
				else {
					column = parameter.getColumn();
				}

				restriction.setRow(column);
				Matrix JrT = new DenseMatrix(rows, columns);
				for (int i = 0; i < rows; i++) {
					// skip column of parameter to avoid doubling, because 
					// transposedJacobianElements() _adds_ elements (instead of sets)
					if (i != column) 
						JrT.set(i, i, 1.0);
				}

				restriction.transposedJacobianElements(JrT);
				Matrix DpJrT = new DenseMatrix(rows, columns);
				this.Qxx.mult(JrT, DpJrT);
				this.Qxx = new UpperSymmPackMatrix(columns);
				JrT.transAmult(DpJrT, this.Qxx);
				parameter.setColumn(column);
			}
			else {
				if (parameter.getColumn() < 0) {
					parameter.setColumn(numberOfUnknownParameters++);
				}
			}
			
			double estimate = restriction.getMisclosure();
			parameter.setValue(estimate);
		}
	}
	
	private NormalEquationSystem createNormalEquation() {
		int nou = this.numberOfUnknownParameters;
		int nor = this.restrictions.size();

		UpperSymmPackMatrix N = new UpperSymmPackMatrix(nou + nor);
		UpperSymmBandMatrix V = this.preconditioning ? new UpperSymmBandMatrix(nou + nor, 0) : null;
		DenseVector n = new DenseVector(nou + nor);

		for (FeaturePoint point : this.points) {
			if (this.interrupt)
				return null;

			int nog = point.getNumberOfGeomtries();
			int dim = point.getDimension();
			
			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(nog, nou);
			Matrix Jv = new DenseMatrix(nog, dim);
			Vector misclosures = new DenseVector(nog);
			// Create a vector of the residuals
			Vector residuals = new DenseVector(dim);
			if (dim != 1) {
				residuals.set(0, point.getResidualX());
				residuals.set(1, point.getResidualY());
			}
			if (dim != 2)
				residuals.set(dim - 1, point.getResidualZ());
			
			int geoIdx = 0;
			for (GeometricPrimitive geometricPrimitive : point) {
				geometricPrimitive.jacobianElements(point, Jx, Jv, geoIdx);
				misclosures.set(geoIdx, geometricPrimitive.getMisclosure(point));
				geoIdx++;
			}

			// w = -B*v + w;
			Jv.multAdd(-1.0, residuals, misclosures);
			residuals = null;

			Matrix W = this.getWeightedMatrixOfMisclosures(point, Jv);
			Jv = null;

			// P * A
			Matrix WJx = new DenseMatrix(nog, nou);
			W.mult(Jx, WJx);
			W = null;

			// AT P A und AT P w
			for (int rowJxT = 0; rowJxT < this.parameters.size(); rowJxT++) {
				if (this.interrupt)
					return null;
				
				int rowN = this.parameters.get(rowJxT).getColumn();
				if (rowN < 0)
					continue;
				for (int colWJx = rowJxT; colWJx < this.parameters.size(); colWJx++) {
					int colN = this.parameters.get(colWJx).getColumn();
					if (colN < 0)
						continue;
					
					double mat = 0;
					double vec = 0;
					for (int colJxT = 0; colJxT < nog; colJxT++) {
						mat += Jx.get(colJxT, colN) * WJx.get(colJxT, rowN);
						if (colWJx == rowJxT)
							vec += -WJx.get(colJxT, rowN) * misclosures.get(colJxT);
					}
					N.add(rowN, colN, mat);
					if (colWJx == rowJxT)
						n.add(rowN, vec);
				}
			}
		}

		for (Restriction restriction : this.restrictions) {
			if (this.interrupt)
				return null;
			
			// set parameter restrictions behind the model equations
			restriction.transposedJacobianElements(N);
			double misclosure = restriction.getMisclosure();
			this.maxAbsRestriction = Math.max(Math.abs(misclosure), this.maxAbsRestriction);
			n.set(restriction.getRow(), -misclosure);
		}
		
		if (this.adaptedDampingValue > 0) {
			for (UnknownParameter unknownParameter : this.parameters) {
				int column = unknownParameter.getColumn();
				if (column < 0)
					continue;
				N.add(column, column, this.adaptedDampingValue * N.get(column, column));
			}
		}
		
		if (this.preconditioning) {
			// Vorkonditionierer == Wurzel der Hauptdiagonale von AT*P*A
			for (int column = 0; column < N.numColumns(); column++) {
				if (this.interrupt)
					return null;
				
				double value = N.get(column, column);
				V.set(column, column, value > Constant.EPS ? 1.0 / Math.sqrt(value) : 1.0);
				//V.set(column, column, 1.0);
			}
		}
		if (this.estimationType == EstimationType.SIMULATION)
			n.zero();

		return new NormalEquationSystem(N, n, V);
	}
	
	private void applyPrecondition(NormalEquationSystem neq) {
		this.applyPrecondition(neq.getPreconditioner(), neq.getMatrix(), neq.getVector());
	}

	private void applyPrecondition(UpperSymmBandMatrix V, UpperSymmPackMatrix M, Vector m) {
		if (V == null)
			return;
		
		for (int row = 0; row < V.numRows(); row++) {
			if (m != null)
				m.set(row, V.get(row, row) * m.get(row));
			if (M != null) {
				for (int column = row; column < V.numColumns(); column++) {
					M.set(row, column, V.get(column, column) * M.get(row, column) * V.get(row, row));
				}
			}
		}
	}
	
	/**
	 * Perform model update, i.e., updates residuals and parameters
	 * if estimateCompleteModel == true stochastic parameters will be derived
	 * @param dxk
	 * @param estimateCompleteModel
	 * @throws NotConvergedException 
	 * @throws IllegalArgumentException 
	 * @throws MatrixSingularException 
	 */
	private void updateModel(Vector dxk, boolean estimateCompleteModel) throws MatrixSingularException, IllegalArgumentException, NotConvergedException {
		// extract model parameters from dxk (ignoring Lagrange k) 
		Vector dx = (dxk.size() == this.numberOfUnknownParameters) ? dxk : Matrices.getSubVector(dxk, Matrices.index(0, this.numberOfUnknownParameters));

		if (this.adaptedDampingValue > 0) {
			// reduce the step length
			if (this.adaptedDampingValue != 0) {
				double alpha = 0.25 * Math.pow(this.adaptedDampingValue, -0.05);
				alpha = Math.min(alpha, 0.75);
				dxk.scale(alpha);
			}
			
			double prevOmega = this.varianceComponentOfUnitWeight.getOmega();
			double curOmega = this.getOmega(dx);
			prevOmega = prevOmega <= 0 ? Double.MAX_VALUE : prevOmega;
			// Pruefe aktuelle Loesung - wenn keine Konvergenz, dann verwerfen.
			boolean lmaConverge = prevOmega >= curOmega;
			this.varianceComponentOfUnitWeight.setOmega(curOmega);
			
			if (lmaConverge) {
				this.adaptedDampingValue *= 0.2;
			}
			else {
				this.adaptedDampingValue *= 5.0;
				
				// Um unendlich zu vermeiden
				if (this.adaptedDampingValue > 1.0/SQRT_EPS) {
					this.adaptedDampingValue = 1.0/SQRT_EPS;
					
					// force an update within the next iteration 
					this.varianceComponentOfUnitWeight.setOmega(0.0);
				}

				// Aktuelle Lösung ist schlechter als die letzte --> abbrechen
				this.maxAbsDx = this.lastValidmaxAbsDx;
				dxk.zero();
				return;
			}
		}
				
		if (this.interrupt)
			return;
		
		// estimate and update residuals before updating the model parameters --> estimated omega
		double omega = this.updateResiduals(dx, !this.adjustModelParametersOnly && estimateCompleteModel && this.Qxx != null && this.adaptedDampingValue == 0);
		this.varianceComponentOfUnitWeight.setOmega(omega);
		
		// updating model parameters --> estimated maxAbsDx
		this.maxAbsDx = this.updateUnknownParameters(dx);
		this.lastValidmaxAbsDx = this.maxAbsDx;
		if (this.interrupt)
			return;

		if (estimateCompleteModel) {
			// remove reduction to center of mass
			this.reverseCenterOfMass();
			// apply post processing of geometric templates
			this.postProcessing();
			// add uncertainties
			double varianceOfUnitWeight = this.varianceComponentOfUnitWeight.isApplyAposterioriVarianceOfUnitWeight() ? this.varianceComponentOfUnitWeight.getVariance() : this.varianceComponentOfUnitWeight.getVariance0();
			
			// global test statistic
			TestStatisticParameterSet globalTestStatistic = this.testStatisticParameters.getTestStatisticParameter(this.varianceComponentOfUnitWeight.getRedundancy(), Double.POSITIVE_INFINITY, Boolean.TRUE);
			double quantil = Math.max(globalTestStatistic.getQuantile(), 1.0 + Math.sqrt(Constant.EPS));
			boolean significant = this.varianceComponentOfUnitWeight.getVariance() / this.varianceComponentOfUnitWeight.getVariance0() > quantil; 
			this.varianceComponentOfUnitWeight.setSignificant(significant);
			
			if (this.Qxx != null) {
				for (UnknownParameter unknownParameter : this.parameters) {
					int column = unknownParameter.getColumn();
					unknownParameter.setUncertainty( column >= 0 ? Math.sqrt(Math.abs(varianceOfUnitWeight * this.Qxx.get(column, column))) : 0.0 );
				}
			}
		}
	}
	
	/**
	 * Perform update of the model parameters
	 * X = X0 + dx
	 * @param dx
	 * @return max(|dx|)
	 */
	private double updateUnknownParameters(Vector dx) {
		double maxAbsDx = 0;
		for (UnknownParameter unknownParameter : this.parameters) {
			if (this.interrupt)
				return 0;
			
			int column = unknownParameter.getColumn();
			if (column < 0)
				continue;
			
			double value  = unknownParameter.getValue();
			double dvalue = dx.get(column);
			maxAbsDx = Math.max(Math.abs(dvalue), maxAbsDx);
			unknownParameter.setValue(value + dvalue);
		}
		return maxAbsDx;
	}
	
	/**
	 * Estimates the residuals of the misclosures ve = A*dx+w, where w = -B*v + w.
	 * and returns omega = ve'*Pe*ve
	 * 
	 * 
	 * @param dx
	 * @return omega = ve'Pee
	 * @throws MatrixSingularException
	 * @throws IllegalArgumentException
	 * @throws NotConvergedException
	 */
	private double getOmega(Vector dx) throws MatrixSingularException, IllegalArgumentException, NotConvergedException {
		double omega = 0;

		int nou = this.numberOfUnknownParameters;
		for (FeaturePoint point : this.points) {
			if (this.interrupt)
				return 0;
			
			int nog = point.getNumberOfGeomtries();
			int dim = point.getDimension();
			
			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(nog, nou);
			Matrix Jv = new DenseMatrix(nog, dim);
			Vector misclosures = new DenseVector(nog);
			// Create a vector of the residuals
			Vector residuals = new DenseVector(dim);
			if (dim != 1) {
				residuals.set(0, point.getResidualX());
				residuals.set(1, point.getResidualY());
			}
			if (dim != 2)
				residuals.set(dim - 1, point.getResidualZ());
			
			int geoIdx = 0;
			for (GeometricPrimitive geometricPrimitive : point) {
				geometricPrimitive.jacobianElements(point, Jx, Jv, geoIdx);
				misclosures.set(geoIdx, geometricPrimitive.getMisclosure(point));
				geoIdx++;
			}
			
			// w = -B*v + w;
			// ve = A*dx+w;
			// v = -Qll*B'*Pww*ve;
			Jv.multAdd(-1.0, residuals, misclosures);

			// multAdd() --> y = alpha*A*x + y
			// to save space, the residuals of the misclosures are NOW stored in misclosures vector
			Jx.multAdd(dx, misclosures);
			
			UpperSymmPackMatrix Ww = this.getWeightedMatrixOfMisclosures(point, Jv);
			Vector Wv = new DenseVector(nog);
			Ww.mult(misclosures, Wv);
			
			omega += misclosures.dot(Wv);
		}
		return omega;
	}

	/**
	 * Estimates the residuals of the misclosures ve = A*dx+w, where w = -B*v + w.
	 * Transforms ve to the residuals v of the observations, i.e.,
	 * v = -Qll*B'*Pww*ve and update observations 
	 * 
	 * 
	 * @param dx
	 * @param estimateStochasticParameters
	 * @return omega = v'Pv
	 * @throws MatrixSingularException
	 * @throws IllegalArgumentException
	 * @throws NotConvergedException
	 */
	private double updateResiduals(Vector dx, boolean estimateStochasticParameters) throws MatrixSingularException, IllegalArgumentException, NotConvergedException {
		double omega = 0;

		int nou = this.numberOfUnknownParameters;
		for (FeaturePoint point : this.points) {
			if (this.interrupt)
				return 0;
			
			int nog = point.getNumberOfGeomtries();
			int dim = point.getDimension();
			
			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(nog, nou);
			Matrix Jv = new DenseMatrix(nog, dim);
			Vector misclosures = new DenseVector(nog);
			// Create a vector of the residuals
			Vector residuals = new DenseVector(dim);
			if (dim != 1) {
				residuals.set(0, point.getResidualX());
				residuals.set(1, point.getResidualY());
			}
			if (dim != 2)
				residuals.set(dim - 1, point.getResidualZ());
			
			int geoIdx = 0;
			for (GeometricPrimitive geometricPrimitive : point) {
				geometricPrimitive.jacobianElements(point, Jx, Jv, geoIdx);
				misclosures.set(geoIdx, geometricPrimitive.getMisclosure(point));
				geoIdx++;
			}
//			for (int geoIdx = 0; geoIdx < nog; geoIdx++) {
//				GeometricPrimitive geometricPrimitive = point.getGeometricPrimitive(geoIdx);
//				geometricPrimitive.jacobianElements(point, Jx, Jv, geoIdx);
//				misclosures.set(geoIdx, geometricPrimitive.getMisclosure(point));
//			}
			
			// w = -B*v + w;
			// ve = A*dx+w;
			// v = -Qll*B'*Pww*ve;
			Jv.multAdd(-1.0, residuals, misclosures);

			// multAdd() --> y = alpha*A*x + y
			// to save space, the residuals of the misclosures are NOW stored in misclosures vector
			Jx.multAdd(dx, misclosures);
			if (!estimateStochasticParameters)
				Jx = null;
			
			UpperSymmPackMatrix Ww = this.getWeightedMatrixOfMisclosures(point, Jv);
			Vector Wv = new DenseVector(nog);
			Ww.mult(misclosures, Wv);

			if (!estimateStochasticParameters)
				Ww = null;
			
			omega += misclosures.dot(Wv);
			
			Vector JvTWv = new DenseVector(dim);
			Jv.transMult(Wv, JvTWv);
			Wv = null;
			if (!estimateStochasticParameters)
				Jv = null;
			
			Matrix D = point.getDispersionApriori();
			D.mult(-1.0/this.varianceComponentOfUnitWeight.getVariance0(), JvTWv, residuals); 
			
			if (dim != 1) {
				point.setResidualX(residuals.get(0));
				point.setResidualY(residuals.get(1));
			}
			if (dim != 2)
				point.setResidualZ(residuals.get(2));
		
			// residuals are estimated - needed for deriving nabla
			if (estimateStochasticParameters) {
				int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();
				// test statistic depends on number of equation (i.e. number of geometries but not on the dimension of the point)
				TestStatisticParameterSet testStatisticParametersAprio = this.testStatisticParameters.getTestStatisticParameter(nog, Double.POSITIVE_INFINITY);
				TestStatisticParameterSet testStatisticParametersApost = this.testStatisticParameters.getTestStatisticParameter(nog, dof-nog);
				
				point.setFisherQuantileApriori(testStatisticParametersAprio.getQuantile());
				point.setFisherQuantileAposteriori(testStatisticParametersApost.getQuantile());
				
				this.addStochasticParameters(point, Jx, Jv, Ww);
			}
		}
		return omega;
	}
	
	private UpperSymmPackMatrix getDispersionOfMisclosures(FeaturePoint point, Matrix Jv) {
		int dim = point.getDimension();
		int nog = point.getNumberOfGeomtries();
		Matrix D = point.getDispersionApriori();
		UpperSymmPackMatrix Dw = new UpperSymmPackMatrix(nog); 
		
		Matrix JvD = new DenseMatrix(nog, dim);
		Jv.mult(1.0/this.varianceComponentOfUnitWeight.getVariance0(), D, JvD);
		JvD.transBmult(Jv, Dw);

		return Dw;
	}

	private UpperSymmPackMatrix inv(UpperSymmPackMatrix D, boolean inplace) throws MatrixSingularException, IllegalArgumentException {
		UpperSymmPackMatrix W = inplace ? D : new UpperSymmPackMatrix(D, true);

		if (D.numColumns() == 1) {
			double variance = W.get(0, 0);
			if (variance <= 0)
				throw new MatrixSingularException("Error, dispersion matrix is singular!");
			W.set(0, 0, 1.0 / variance);
		}
		else
			MathExtension.inv(W);

		return W;
	}
	
	private UpperSymmPackMatrix getWeightedMatrixOfMisclosures(FeaturePoint point, Matrix Jv) throws MatrixSingularException, IllegalArgumentException {
		UpperSymmPackMatrix D = this.getDispersionOfMisclosures(point, Jv);
		return this.inv(D, true);
	}
	
	private void addStochasticParameters(FeaturePoint point, Matrix Jx, Matrix Jv, UpperSymmPackMatrix Ww) throws NotConvergedException, MatrixSingularException, IllegalArgumentException {
		int nou = this.numberOfUnknownParameters;
		int nog = point.getNumberOfGeomtries();
		int dim = point.getDimension();
		
		// estimate Qll = A*Qxx*AT
		Matrix QxxJxT = new DenseMatrix(nou,nog);
		this.Qxx.transBmult(Jx, QxxJxT);
		UpperSymmPackMatrix JxQxxJxT = new UpperSymmPackMatrix(nog);
		Jx.mult(QxxJxT, JxQxxJxT);
		QxxJxT = null;
		
//		// estimates Qvv = Qww - Qll;   Qkk = W - W * Qll * W
//		// !!! overwrite Dw by Qvv !!!
//		for (MatrixEntry qvv : Dw) {
//			Dw.set(qvv.row(), qvv.column(), qvv.get() - JxQxxJxT.get(qvv.row(), qvv.column()));
//		}
//
//		// estimates test statistic of the point
//		// estimates Pnn = W * Qvv * W
//		Matrix QvvW = new DenseMatrix(nog,nog);
//		Dw.mult(Ww, QvvW);
//		UpperSymmPackMatrix WQvvW = new UpperSymmPackMatrix(nog);
//		Ww.mult(QvvW, WQvvW);
//
//		// estimate gross error
//		Matrix Qnn = MathExtension.pinv(WQvvW, -1.0);
//		Vector nablaw = new DenseVector(nog);
//		Qnn.mult(Wvw, nablaw);
//		Qnn = null;
//		double T = Wvw.dot(nablaw);

		// derive a-posteriori uncertainties of the point
		// estimates Qkk = W - W * Qll * W
		Matrix JxQxxJxTW = new DenseMatrix(nog, nog);
		JxQxxJxT.mult(Ww, JxQxxJxTW);
		JxQxxJxT = null;

		UpperSymmPackMatrix WJxQxxJxTW = new UpperSymmPackMatrix(nog);
		Ww.mult(JxQxxJxTW, WJxQxxJxTW);
		JxQxxJxTW = null;

		for (int row = 0; row < nog; row++) {
			for (int column = row; column < nog; column++) {
				WJxQxxJxTW.set(row, column, Ww.get(row, column) - WJxQxxJxTW.get(row, column));
			}
		}
		Ww = null;

		// estimate dispersion of residuals Qvv = Qll*B'*Qkk*B*Qll
		Matrix Qll = point.getDispersionApriori();

		Matrix JvQll = new DenseMatrix(nog, dim); 
		Jv.mult(1.0/this.varianceComponentOfUnitWeight.getVariance0(), Qll, JvQll);

		Matrix WJxQxxJxTWJvQll = new DenseMatrix(nog, dim);
		WJxQxxJxTW.mult(JvQll, WJxQxxJxTWJvQll);

		Matrix QllJvTWJxQxxJxTWJvQll = new UpperSymmPackMatrix(dim);
		JvQll.transAmult(WJxQxxJxTWJvQll, QllJvTWJxQxxJxTWJvQll);
		WJxQxxJxTWJvQll = null;

		// estimates redundancies R = P * Qvv
		Matrix P = point.getInvertedDispersion(false);
		P.scale(this.varianceComponentOfUnitWeight.getVariance0());
		Matrix R = new DenseMatrix(dim,dim);
		P.mult(QllJvTWJxQxxJxTWJvQll, R);
		
		double redundancy = 0;
		for (int row = 0; row < dim; row++) {
			double r = R.get(row, row);
			if (r > 0) {
				if (row == 0 && dim != 1)
					point.setRedundancyX(r);
				
				else if (row == 1 && dim != 1)
					point.setRedundancyY(r);
				
				else if (dim != 2)
					point.setRedundancyZ(r);
				
				redundancy += r;
			}
		}

		// overwrite dispersion matrix by estimated one
		// Qll.add(-1.0, QllJvTWJxQxxJxTWJvQll);
		for (int row = 0; row < dim; row++) {
			if (row == 0 && dim != 1)
				point.setCofactorX(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));
			
			else if (row == 1 && dim != 1)
				point.setCofactorY(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));
			
			else if (dim != 2)
				point.setCofactorZ(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));

		}
		QllJvTWJxQxxJxTWJvQll = null;

		// estimates the gross error if it is an controlled observation, i.e., r >> 0
		if (redundancy > Math.sqrt(Constant.EPS)) {
			// estimates Qnabla = P * Qvv * P = R * Qvv
			Matrix PQvvP = new UpperSymmPackMatrix(dim);
			R.mult(P, PQvvP);

			Vector residuals = new DenseVector(dim);
			if (dim != 1) {
				residuals.set(0, point.getResidualX());
				residuals.set(1, point.getResidualY());
			}
			if (dim != 2)
				residuals.set(dim - 1, point.getResidualZ());
			
			Vector weightedResiduals = new DenseVector(dim);
			P.mult(residuals,  weightedResiduals);

			Vector grossErrors = new DenseVector(dim);
			PQvvP = MathExtension.pinv(PQvvP, nog);
			PQvvP.mult(-1.0, weightedResiduals, grossErrors);
			if (dim != 1) {
				point.setGrossErrorX(grossErrors.get(0));
				point.setGrossErrorY(grossErrors.get(1));
			}
			if (dim != 2)
				point.setGrossErrorZ(grossErrors.get(2));

			// change sign, i.e. residual vs. error
			double T = -weightedResiduals.dot(grossErrors);
			point.getTestStatistic().setFisherTestNumerator(T);
			point.getTestStatistic().setDegreeOfFreedom(nog);
		}
	}

	public TestStatisticParameters getTestStatisticParameters() {
		return this.testStatisticParameters;
	}
	
	public TestStatisticDefinition getTestStatisticDefinition() {
		return this.testStatisticDefinition;
	}
	
	public UpperSymmPackMatrix getCorrelationMatrix() {
		if (this.Qxx == null)
			return null;
		
		int size = this.Qxx.numColumns();
		UpperSymmPackMatrix corr = new UpperSymmPackMatrix(size);
		
		for (int r = 0; r < size; r++) {
			UnknownParameter parameterR = this.parameters.get(r);
			int row = parameterR.getColumn();
			if (row < 0)
				continue;
			
			corr.set(row, row, 1.0);
			
			double varR = Math.abs(this.Qxx.get(row, row));
			if (varR < SQRT_EPS)
				continue;
			
			for (int c = r + 1; c < size; c++) {
				UnknownParameter parameterC = this.parameters.get(c);
				int column = parameterC.getColumn();
				if (column < 0)
					continue;
				
				double varC = this.Qxx.get(column, column);
				if (varC < SQRT_EPS)
					continue;
				
				corr.set(row, column, this.Qxx.get(row, column) / Math.sqrt(varR) / Math.sqrt(varC));
			}
		}
		return corr;
	}

	TestStatisticParameters getTestStatisticParameters(TestStatisticDefinition testStatisticDefinition) {
		double alpha = testStatisticDefinition.getProbabilityValue();
		double beta  = testStatisticDefinition.getPowerOfTest();
		int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();
		int numberOfHypotesis = this.points.size() + (dof > 0 ? 1 : 0); // add one for global test //TODO add further hypotesis tests
		int dim = this.maximumNumberOfGeometricPrimitivesPerPoint; // Reference number is equal to the point that lies in most geometries 
				
		TestStatistic testStatistic;
		switch (testStatisticDefinition.getTestStatisticType()) {
		case SIDAK:
			// alle Hypothesen + Test der Varianzkomponenten + Test der Festpunkte
			testStatistic = new SidakTestStatistic(numberOfHypotesis, alpha, beta, testStatisticDefinition.isFamilywiseErrorRate());
			break;
		case BAARDA_METHOD:
			testStatistic = new BaardaMethodTestStatistic(testStatisticDefinition.isFamilywiseErrorRate() ? dof : dim, alpha, beta);
			break;
		case NONE:
			testStatistic = new UnadjustedTestStatitic(alpha, beta);
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, unknown test statistic method " + testStatisticDefinition.getTestStatisticType());
		}
		return new TestStatisticParameters(testStatistic);
	}
	
	private void prepareSphericalSimplexUnscentedTransformationObservation(int estimationStep, double[][] SigmaUT, double weight) throws IllegalArgumentException, MatrixNotSPDException {
		int dim = this.getFeature().getFeatureType() == FeatureType.CURVE ? 2 : 3;
		int noo = this.points.size() * dim;

		int row = 0;
		for (FeaturePoint point : this.points) {
			// Cholesky factor
			UpperTriangPackMatrix G = new UpperTriangPackMatrix(point.getDispersionApriori(), true);
			MathExtension.chol(G);
			Vector sigmaUT = new DenseVector(SigmaUT[row], false);
			Vector delta = new DenseVector(dim);
			G.transMult(sigmaUT, delta);

			// Reverse modification of last UT step
			if (dim != 1) {
				point.setX0( point.getX0() - delta.get(0));
				point.setY0( point.getY0() - delta.get(1));
			}
			if (dim != 2) {
				point.setZ0( point.getZ0() - delta.get(dim-1));
			}

			// derive new Sigma points, if estimationStep >= 0
			sigmaUT.zero();
			
			if (dim != 1) {
				if (estimationStep >= 0 && estimationStep < noo + 2) {
					if (row == estimationStep - 1)
						sigmaUT.set(0, (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
					else if (row > estimationStep - 2)
						sigmaUT.set(0, -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
				}
				row++;
				if (estimationStep >= 0 && estimationStep < noo + 2) {
					if (row == estimationStep - 1)
						sigmaUT.set(1, (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
					else if (row > estimationStep - 2)
						sigmaUT.set(1, -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
				}
				row++;
			}
			if (dim != 2) {
				if (estimationStep >= 0 && estimationStep < noo + 2) {
					if (row == estimationStep - 1)
						sigmaUT.set(dim-1, (1.0 + row) / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
					else if (row > estimationStep - 2)
						sigmaUT.set(dim-1, -1.0 / Math.sqrt( ((1.0 + row) * (2.0 + row)) * weight ));
				}
				row++;
			}
			if (estimationStep >= 0) {
				delta = new DenseVector(dim);
				G.transMult(sigmaUT, delta);
				// Modify points
				if (dim != 1) {
					point.setX0( point.getX0() + delta.get(0));
					point.setY0( point.getY0() + delta.get(1));
				}
				if (dim != 2) {
					point.setZ0( point.getZ0() + delta.get(dim-1));
				}
			}
		}
	}
	
	private void addUnscentedTransformationSolution(Vector xUT, Vector vUT, Matrix solutionVectors, int solutionNumber, double weight) {
		for (UnknownParameter unknownParameter : this.parameters) {
			if (this.interrupt)
				return;

			int col = unknownParameter.getColumn();
			if (col < 0)
				continue;
	
			double value = unknownParameter.getValue();
			xUT.set(col, xUT.get(col) + weight * value);
			solutionVectors.set(col, solutionNumber, value);
		}

		if (vUT != null) {
			int row = 0;
			for (FeaturePoint point : this.points) {
				int dim = point.getDimension();
				
				if (dim != 1) {
					double vx = point.getResidualX();
					double vy = point.getResidualY();
					
					vUT.set(row, vUT.get(row++) + weight * vx);
					vUT.set(row, vUT.get(row++) + weight * vy);
				}
				
				if (dim != 2) {
					double vz = point.getResidualZ();
					
					vUT.set(row, vUT.get(row++) + weight * vz);
				}
			}
		}
	}
	
	private void estimateUnscentedTransformationParameterUpdateAndDispersion(Vector xUT, Matrix solutionVectors, Vector vUT, double weightN, double weightC) {
		int numberOfEstimationSteps = solutionVectors.numColumns();
		
		if (this.Qxx == null)
			this.Qxx = new UpperSymmPackMatrix(xUT.size());
		this.Qxx.zero();
		
		for (int r = 0; r < this.parameters.size(); r++) {
			if (this.interrupt)
				return;

			UnknownParameter unknownParameterRow = this.parameters.get(r);
			int row = unknownParameterRow.getColumn();
			if (row < 0)
				continue;

			unknownParameterRow.setValue(xUT.get(row));

			for (int c = r; c < this.parameters.size(); c++) {
				if (this.interrupt)
					return;

				UnknownParameter unknownParameterCol = this.parameters.get(c);
				int col = unknownParameterCol.getColumn();
				if (col < 0)
					continue;

				// Laufindex des jeweiligen Spalten-/Zeilenvektors
				for (int estimationStep = 0; estimationStep < numberOfEstimationSteps; estimationStep++) {
					double weight = estimationStep == (numberOfEstimationSteps - 1) ? weightC : weightN;

					double valueCol = solutionVectors.get(col, estimationStep) - xUT.get(col);
					double valueRow = solutionVectors.get(row, estimationStep) - xUT.get(row);

					this.Qxx.set(row, col, this.Qxx.get(row, col) + valueRow * weight * valueCol);
				}
			}
		}

		solutionVectors = null;
		xUT = null;
		
		int row = 0;
		for (FeaturePoint point : this.points) {
			if (this.interrupt)
				return;

			int dim = point.getDimension();

			if (dim != 1) {
				double vx = vUT.get(row++);
				double vy = vUT.get(row++);
				
				point.setResidualX(vx);
				point.setResidualY(vy);
			}
			if (dim != 2) {
				double vz = vUT.get(row++);
				point.setResidualZ(vz);
			}
		}
		
		// add uncertainties
		double varianceOfUnitWeight = this.varianceComponentOfUnitWeight.isApplyAposterioriVarianceOfUnitWeight() ? this.varianceComponentOfUnitWeight.getVariance() : this.varianceComponentOfUnitWeight.getVariance0();
		varianceOfUnitWeight = varianceOfUnitWeight / this.varianceComponentOfUnitWeight.getVariance0();

		// global test statistic
		TestStatisticParameterSet globalTestStatistic = this.testStatisticParameters.getTestStatisticParameter(this.varianceComponentOfUnitWeight.getRedundancy(), Double.POSITIVE_INFINITY, Boolean.TRUE);
		double quantil = Math.max(globalTestStatistic.getQuantile(), 1.0 + Math.sqrt(Constant.EPS));
		boolean significant = this.varianceComponentOfUnitWeight.getVariance() / this.varianceComponentOfUnitWeight.getVariance0() > quantil; 
		this.varianceComponentOfUnitWeight.setSignificant(significant);
		for (UnknownParameter unknownParameter : this.parameters) {
			int column = unknownParameter.getColumn();
			unknownParameter.setUncertainty( column >= 0 ? Math.sqrt(Math.abs(varianceOfUnitWeight * this.Qxx.get(column, column))) : 0.0 );
		}
	}
	
	public VarianceComponent getVarianceComponentOfUnitWeight() {
		return this.varianceComponentOfUnitWeight;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.change.removePropertyChangeListener(listener);
	}
	
	private void fireFeatureChanged(Feature feature, FeatureEventType eventType) {
		FeatureEvent evt = new FeatureEvent(feature, eventType);
		Object listeners[] = this.listenerList.toArray();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] instanceof FeatureChangeListener)
				((FeatureChangeListener)listeners[i]).featureChanged(evt);
		}
	}
	
	public void setLevenbergMarquardtDampingValue(double lambda) {
		this.dampingValue = Math.abs(lambda);
	}
	
	public double getLevenbergMarquardtDampingValue() {
		return this.dampingValue;
	}
	
	public int getMaximalNumberOfIterations() {
		return this.maximalNumberOfIterations;
	}

	public void setMaximalNumberOfIterations(int maximalNumberOfIterations) {
		this.maximalNumberOfIterations = maximalNumberOfIterations;
	}
	
	public boolean isAdjustModelParametersOnly() {
		return this.adjustModelParametersOnly;
	}
	
	public void setAdjustModelParametersOnly(boolean adjustModelParametersOnly) {
		this.adjustModelParametersOnly = adjustModelParametersOnly;
	}
	
	public boolean isPreconditioning() {
		return this.preconditioning;
	}
	
	public void setPreconditioning(boolean preconditioning) {
		this.preconditioning = preconditioning;
	}
	
	public void addFeatureChangeListener(FeatureChangeListener l) {
		this.listenerList.add(l);
	}
	
	public void removeFeatureChangeListener(FeatureChangeListener l) {
		this.listenerList.remove(l);
	}
	
	public void interrupt() {
		this.interrupt = true;
	}
	
	public EstimationType getEstimationType() {
		return this.estimationType;
	}
	
	public void setEstimationType(EstimationType estimationType) throws IllegalArgumentException {
		if (estimationType == EstimationType.L2NORM || estimationType == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION)
			this.estimationType = estimationType;
		else
			throw new IllegalArgumentException("Error, unsupported estimation type " + estimationType + "!");
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
	

//	public static void main(String[] args) throws Exception {
//		FeatureAdjustment adjustment = new FeatureAdjustment();
//
//		FeaturePoint point1 = new FeaturePoint("1", 2.986, 2.496, 2.928);
//		FeaturePoint point2 = new FeaturePoint("2", 4.370, 5.276, 3.079);
//		FeaturePoint point3 = new FeaturePoint("3", 4.845, 6.347, 5.817);
//		FeaturePoint point4 = new FeaturePoint("4", 2.375, 1.485, 6.589);
//		FeaturePoint point5 = new FeaturePoint("5", 2.448, 1.462, 4.205);
//		FeaturePoint point6 = new FeaturePoint("6", 3.049, 2.764, 8.050);
//		FeaturePoint point7 = new FeaturePoint("7", 4.012, 4.706, 8.082);
//		FeaturePoint point8 = new FeaturePoint("8", 4.567, 5.054, 7.141);
//
//		point1.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  16.0839803365062,  4.65534839899422, -4.38460084854308},
//					{  4.65534839899422,  26.3272479942061,  2.47834290278709},
//					{ -4.38460084854308,  2.47834290278709,  29.0815646040579}
//				}))
//				);
//
//		point2.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  17.8964457912516,  3.09111321599046,  5.64126184131749},
//					{  3.09111321599046,  19.0624298799118,  8.08205153582906},
//					{  5.64126184131749,  8.08205153582906,  26.1468430557483}
//				}))
//				);
//
//		point3.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  29.0047447515151, 0.403158357030017,  11.1766961352954},
//					{ 0.403158357030017,  22.9345053072238, -7.29211633546859},
//					{  11.1766961352954, -7.29211633546859,  21.2201800630207},
//				}))
//				);
//
//		point4.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{   14.007661244179,  1.79628360816322,  4.13632502272246},
//					{  1.79628360816322,  31.9556797927839, -2.25880229809073},
//					{  4.13632502272246, -2.25880229809073,  27.3871830020619}
//				}))
//				);
//
//		point5.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  24.2029609740167, -4.29135894085001, -4.78113396533508},
//					{ -4.29135894085001,  21.3674323634287, -1.42807065830297},
//					{ -4.78113396533508, -1.42807065830297,  17.6619349346275}
//				}))
//				);
//
//		point6.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  20.7448446052198,  7.74193819492723, -3.21311943337311},
//					{  7.74193819492723,  42.3093042517494,  -5.4060692623952},
//					{ -3.21311943337311,  -5.4060692623952,   17.565600995021}
//				}))
//				);
//
//		point7.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{   47.221895847014, 0.692081451296938,  3.57664252908793},
//					{ 0.692081451296938,  15.1198359736348, -3.95299745877936},
//					{  3.57664252908793, -3.95299745877936,  29.0659166670697}
//				}))
//				);
//
//		point8.setDispersionApriori(
//				new UpperSymmPackMatrix(new DenseMatrix(new double[][] {
//					{  29.3255840578623,  -7.3780624024934, -7.22152909351128},
//					{  -7.3780624024934,  13.6447146847114, 0.978337662987412},
//					{ -7.22152909351128, 0.978337662987412,  24.3329973857069}
//				}))
//				);
//
//		java.util.Set<FeaturePoint> points = new LinkedHashSet<FeaturePoint>();
//		points.add(point1);
//		points.add(point2);
//		points.add(point3);
//		points.add(point4);
//		points.add(point5);
//		points.add(point6);
//		points.add(point7);
//		points.add(point8);
//
//		org.applied_geodesy.adjustment.geometry.point.Point centerOfMass = Feature.deriveCenterOfMass(points);
//
//		/** Circle fit **/
//		Feature feature = new org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature();
//
//		/** Start adjustment **/
//		for (GeometricPrimitive geometricPrimitive : feature)
//			geometricPrimitive.getFeaturePoints().addAll(points);
//
//
//		org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction featurePointPlaneRestriction = 
//				new org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction(
//						false, 
//						((org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature)feature).getPlane(), 
//						point5
//		);
//		
//		org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction featurePointCircleRestriction = 
//				new org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction(
//						false, 
//						((org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature)feature).getSphere(), 
//						point5
//		);
//		
//		adjustment.setFeature(feature);
//		feature.deriveInitialGuess();
//		feature.applyInitialGuess();
//		feature.setCenterOfMass(centerOfMass);
//		
//		feature.getRestrictions().addAll(featurePointPlaneRestriction, featurePointCircleRestriction);
//
//		adjustment.init();
//		adjustment.estimateModel();
//		
//		System.out.println(point5.getX0()+"  "+point5.getX());
//		System.out.println(point5.getY0()+"  "+point5.getY());
//		System.out.println(point5.getZ0()+"  "+point5.getZ());
//	}
}
