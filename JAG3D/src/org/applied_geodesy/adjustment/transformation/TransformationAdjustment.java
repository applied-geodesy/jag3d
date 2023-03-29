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

package org.applied_geodesy.adjustment.transformation;

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
import org.applied_geodesy.adjustment.statistic.BaardaMethodTestStatistic;
import org.applied_geodesy.adjustment.statistic.SidakTestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatistic;
import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameters;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.adjustment.statistic.UnadjustedTestStatitic;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.equation.TransformationEquations;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.EstimatedFramePosition;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePosition;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.Position;
import org.applied_geodesy.adjustment.transformation.point.SimplePositionPair;
import org.applied_geodesy.adjustment.transformation.restriction.Restriction;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
import no.uib.cipr.matrix.NotConvergedException.Reason;

public class TransformationAdjustment {
	private final PropertyChangeSupport change = new PropertyChangeSupport(this);
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
	private Transformation transformation;
	private List<HomologousFramePositionPair> homologousPointPairs = new ArrayList<HomologousFramePositionPair>();
	
	private List<UnknownParameter> parameters = new ArrayList<UnknownParameter>();
	private List<Restriction> restrictions    = new ArrayList<Restriction>();
	private TransformationEquations transformationEquations;
	
	private TestStatisticDefinition testStatisticDefinition = new TestStatisticDefinition(TestStatisticType.BAARDA_METHOD, DefaultValue.getProbabilityValue(), DefaultValue.getPowerOfTest(), false);
	private TestStatisticParameters testStatisticParameters = null;
	
	private EstimationStateType currentEstimationStatus = EstimationStateType.BUSY;
	private EstimationType estimationType = EstimationType.L2NORM;
	
	private boolean interrupt             = false,
			calculateStochasticParameters = false,
			adjustModelParametersOnly     = false,
			preconditioning               = true;
	
	private int maximalNumberOfIterations = DefaultValue.getMaximalNumberOfIterations(),
			iterationStep                 = 0,
			numberOfModelEquations        = 0,
			numberOfUnknownParameters     = 0;
	
	private double maxAbsDx     = 0.0,
			maxAbsRestriction   = 0.0,
			lastValidmaxAbsDx   = 0.0,
			dampingValue        = 0.0,
			adaptedDampingValue = 0.0;
	
	private static double SQRT_EPS = Math.sqrt(Constant.EPS);
	
	private VarianceComponent varianceComponentOfUnitWeight = new VarianceComponent();
	private UpperSymmPackMatrix Qxx = null;
	
	
	
	public void init() {
		int dim = this.transformationEquations.getTransformationType().getDimension();

		List<HomologousFramePositionPair> nonUniqueHomologousFramePositionPairs = new ArrayList<HomologousFramePositionPair>(this.transformationEquations.getHomologousFramePositionPairs());

		// create a unique set of points
		LinkedHashSet<HomologousFramePositionPair> uniqueHomologousFramePositionPairs = new LinkedHashSet<HomologousFramePositionPair>(nonUniqueHomologousFramePositionPairs);
		nonUniqueHomologousFramePositionPairs.clear();

		// reset points
		for (HomologousFramePositionPair homologousPointPair : uniqueHomologousFramePositionPairs) {
			homologousPointPair.reset();
			homologousPointPair.getTestStatistic().setVarianceComponent(this.varianceComponentOfUnitWeight);
		}
		
		for (FramePositionPair framePositionPair : this.transformation.getFramePositionPairs()) {
			framePositionPair.reset();
			EstimatedFramePosition targetPosition = framePositionPair.getTargetSystemPosition();
			// reset residuals and prior estimated values
			targetPosition.setX0(0);
			targetPosition.setY0(0);
			targetPosition.setZ0(0);
			targetPosition.setVarianceComponent(this.varianceComponentOfUnitWeight);
		}

		// filter enabled points
		FilteredList<HomologousFramePositionPair> enabledUniqueHomologousFramePositionPairs = new FilteredList<HomologousFramePositionPair>(FXCollections.<HomologousFramePositionPair>observableArrayList(uniqueHomologousFramePositionPairs));
		enabledUniqueHomologousFramePositionPairs.setPredicate(
				new Predicate<HomologousFramePositionPair>(){
					public boolean test(HomologousFramePositionPair homologousPointPair){
						return homologousPointPair.isEnable();
					}
				}
		);
		uniqueHomologousFramePositionPairs.clear();

		// Unique point list
		this.homologousPointPairs = new ArrayList<HomologousFramePositionPair>(enabledUniqueHomologousFramePositionPairs);

		// number of equations rows in Jacobian A, B
		this.numberOfModelEquations = dim * this.homologousPointPairs.size();
		
		for (UnknownParameter unknownParameter : this.parameters) {
			unknownParameter.getTestStatistic().setVarianceComponent(this.varianceComponentOfUnitWeight);
		}

		this.testStatisticParameters = this.getTestStatisticParameters(this.testStatisticDefinition);

	}
	
	public Transformation getTransformation() {
		return this.transformation;
	}

	public void setTransformation(Transformation transformation) {
		if (this.transformation != null) {
//			this.transformation.getFramePositionPairs().clear();
//			this.transformation.getHomologousFramePositionPairs().clear();
			this.fireTransformationChanged(this.transformation, TransformationEventType.TRANSFORMATION_MODEL_REMOVED);
		}
				
		this.reset();
		this.transformation = transformation;
				
		if (this.transformation != null) {
			this.parameters              = this.transformation.getUnknownParameters();
			this.restrictions            = this.transformation.getRestrictions();
			this.transformationEquations = this.transformation.getTransformationEquations();
			this.fireTransformationChanged(this.transformation, TransformationEventType.TRANSFORMATION_MODEL_ADDED);
		}
	}
	
	private void reset() {
		this.varianceComponentOfUnitWeight.setVariance0(1.0);
		this.varianceComponentOfUnitWeight.setOmega(0.0);
		this.varianceComponentOfUnitWeight.setRedundancy(0.0);
		this.transformationEquations = null;
		this.parameters.clear();
		this.restrictions.clear();
		this.homologousPointPairs.clear();
		this.Qxx = null;
	}
	
	private void prepareIterationProcess(SimplePositionPair centerOfMasses) {
		// set warm start solution x <-- x0
		this.transformation.applyInitialGuess();
		
		// reset center of masses
		this.transformationEquations.getCenterOfMasses().getSourceSystemPosition().setX(0);
		this.transformationEquations.getCenterOfMasses().getSourceSystemPosition().setY(0);
		this.transformationEquations.getCenterOfMasses().getSourceSystemPosition().setZ(0);
		
		this.transformationEquations.getCenterOfMasses().getTargetSystemPosition().setX(0);
		this.transformationEquations.getCenterOfMasses().getTargetSystemPosition().setY(0);
		this.transformationEquations.getCenterOfMasses().getTargetSystemPosition().setZ(0);

		// set center of masses
		if (this.transformation.isEstimateCenterOfMasses()) 
			this.transformationEquations.setCenterOfMasses(centerOfMasses);

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

		List<Restriction> calculations = this.transformation.getPostProcessingCalculations();
		for (Restriction restriction : calculations)
			restriction.setRow(-1);

		// reset points
		for (HomologousFramePositionPair homologousPointPair : this.homologousPointPairs) {
			homologousPointPair.reset();
		}
	}

	public EstimationStateType estimateModel() throws NotConvergedException, MatrixSingularException, OutOfMemoryError {
		
		try {
			SimplePositionPair centerOfMasses = Transformation.deriveCenterOfMasses(this.transformation.getHomologousFramePositionPairs(), this.transformation.getRestrictions(), this.transformation.getSupportedParameterRestrictions());
			this.prepareIterationProcess(centerOfMasses);
			
			int nou = this.numberOfUnknownParameters;
			int nor = this.restrictions.size();
			int noe = this.numberOfModelEquations;
			
			if (noe < (nou-nor))
				throw new MatrixSingularException("Error, the number of equations is less than the number of parameters to be estimated, " + noe + " < " + (nou - nor) + "! The system of equations is underestimated. Please add further equations." );
			
			this.adaptedDampingValue = this.dampingValue;
			int runs = this.maximalNumberOfIterations - 1;
			boolean isEstimated = false, estimateCompleteModel = false, isFirstIteration = true;
			
			this.maxAbsDx          = 0.0;
			this.maxAbsRestriction = 0.0;
			this.lastValidmaxAbsDx = 0.0;
			
			if (this.maximalNumberOfIterations == 0) {
				estimateCompleteModel = isEstimated = true;
				isFirstIteration = false;
				this.adaptedDampingValue = 0;
			}

			double sigma2apriori = this.getEstimateVarianceOfUnitWeightApriori();
			this.varianceComponentOfUnitWeight.setVariance0(1.0);
			this.varianceComponentOfUnitWeight.setOmega(0.0);
			this.varianceComponentOfUnitWeight.setRedundancy(this.numberOfModelEquations - this.numberOfUnknownParameters + this.restrictions.size());
			this.varianceComponentOfUnitWeight.setVariance0( sigma2apriori < SQRT_EPS ? SQRT_EPS : sigma2apriori );
			
			do {
				this.maxAbsDx = 0.0;
				this.maxAbsRestriction = 0.0;
				this.iterationStep = this.maximalNumberOfIterations-runs;
				this.currentEstimationStatus = EstimationStateType.ITERATE;
				this.change.firePropertyChange(this.currentEstimationStatus.name(), this.maximalNumberOfIterations, this.iterationStep);

				// create the normal system of equations including restrictions
				NormalEquationSystem neq = this.createNormalEquation();

				if (this.interrupt || neq == null) {
					this.currentEstimationStatus = EstimationStateType.INTERRUPT;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
					this.interrupt = false;
					return this.currentEstimationStatus;
				}

				// apply pre-conditioning to achieve a stable normal equation
				if (this.preconditioning)
					this.applyPrecondition(neq);

				DenseVector n = neq.getVector();
				UpperSymmPackMatrix N = neq.getMatrix();

				if (!isFirstIteration) 
					estimateCompleteModel = isEstimated;

				try {
					if (estimateCompleteModel || this.estimationType == EstimationType.L1NORM) {
						this.calculateStochasticParameters = (this.estimationType != EstimationType.L1NORM && estimateCompleteModel);

						if (this.estimationType != EstimationType.L1NORM) {
							this.currentEstimationStatus = EstimationStateType.INVERT_NORMAL_EQUATION_MATRIX;
							this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
						}

						// in-place estimation normal system N * x = n: N <-- Qxx, n <-- dx 
						MathExtension.solve(N, n, true);

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
									double qxx = N.get(row, column);
									if (!Double.isFinite(qxx)) {
										this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
										this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
										throw new MatrixSingularException("Error, normal equation matrix is singular!");
									}
									this.Qxx.set(row, column, qxx);
								}
							}
						}
						else {
							this.Qxx = N;
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

				}
				catch (MatrixSingularException | MatrixNotSPDException | IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException e) {
					e.printStackTrace();
					this.currentEstimationStatus = EstimationStateType.SINGULAR_MATRIX;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
					throw new MatrixSingularException("Error, normal equation matrix is singular!\r\n" + e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "");
				}
				catch (Exception e) {
					e.printStackTrace();
					this.currentEstimationStatus = EstimationStateType.INTERRUPT;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
					return this.currentEstimationStatus;
				}

				if (this.interrupt) {
					this.currentEstimationStatus = EstimationStateType.INTERRUPT;
					this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
					this.interrupt = false;
					return this.currentEstimationStatus;
				}

				if (Double.isInfinite(this.maxAbsDx) || Double.isNaN(this.maxAbsDx)) {
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
							this.currentEstimationStatus = EstimationStateType.ROBUST_ESTIMATION_FAILED;
							this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
							throw new NotConvergedException(Reason.Iterations, "Error, euqation system does not converge! Last iterate max|dx| = " + this.maxAbsDx + " (" + SQRT_EPS + ").");
						}
						else {
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

				if (isEstimated || this.adaptedDampingValue <= SQRT_EPS || runs < this.maximalNumberOfIterations * 0.1 + 1)
					this.adaptedDampingValue = 0.0;
				
				isFirstIteration = false;
			}
			while (!estimateCompleteModel);
			
			
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			this.currentEstimationStatus = EstimationStateType.OUT_OF_MEMORY;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), false, true);
			throw new OutOfMemoryError();
		}
		finally {
			// Reset the center of mass
			Position centerOfMaasSrc = this.transformationEquations.getCenterOfMasses().getSourceSystemPosition();
			Position centerOfMaasTrg = this.transformationEquations.getCenterOfMasses().getTargetSystemPosition();
			
			centerOfMaasSrc.setX(0);
			centerOfMaasSrc.setY(0);
			centerOfMaasSrc.setZ(0);
			
			centerOfMaasTrg.setX(0);
			centerOfMaasTrg.setY(0);
			centerOfMaasTrg.setZ(0);
		}

		if (this.currentEstimationStatus.getId() == EstimationStateType.BUSY.getId() || this.calculateStochasticParameters) {
			this.currentEstimationStatus = EstimationStateType.ERROR_FREE_ESTIMATION;
			this.change.firePropertyChange(this.currentEstimationStatus.name(), SQRT_EPS, this.maxAbsDx);
		}

		return this.currentEstimationStatus;
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
			// Check if the current solution converts - if not, reject
			boolean lmaConverge = prevOmega >= curOmega;
			this.varianceComponentOfUnitWeight.setOmega(curOmega);
			
			if (lmaConverge) {
				this.adaptedDampingValue *= 0.2;
			}
			else {
				this.adaptedDampingValue *= 5.0;
				
				// To avoid infinity
				if (this.adaptedDampingValue > 1.0/SQRT_EPS) {
					this.adaptedDampingValue = 1.0/SQRT_EPS;
					
					// force an update within the next iteration 
					this.varianceComponentOfUnitWeight.setOmega(0.0);
				}

				// Current solution is NOT an improvement --> cancel procedure
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
				int dim = 1;
				int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();

				TestStatisticParameterSet testStatisticParametersAprio = this.testStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
				TestStatisticParameterSet testStatisticParametersApost = this.testStatisticParameters.getTestStatisticParameter(dim, dof-dim);

				for (UnknownParameter unknownParameter : this.parameters) {
					ParameterType parameterType = unknownParameter.getParameterType();
					boolean isFixedParameter = this.transformation.isFixedParameter(unknownParameter);
					int column = unknownParameter.getColumn();
					double expValue    = unknownParameter.getExpectedValue();
					double value       = !isFixedParameter && column >= 0 ? unknownParameter.getValue() : expValue;
					double cofactor    = !isFixedParameter && column >= 0 ? Math.max(0, this.Qxx.get(column, column)) : 0;
					double dVal        = !isFixedParameter ? value - expValue : 0;
					
					if (!isFixedParameter && 
							(parameterType == ParameterType.EULER_ANGLE_X || parameterType == ParameterType.EULER_ANGLE_Y || parameterType == ParameterType.EULER_ANGLE_Z) && 
							Math.abs( 2.0*Math.PI - dVal ) < Math.abs(dVal))
						dVal = 2.0*Math.PI - dVal;
					
					if (!isFixedParameter && 
							(parameterType == ParameterType.SHEAR_X || parameterType == ParameterType.SHEAR_Y || parameterType == ParameterType.SHEAR_Z) && 
							Math.abs( Math.PI - dVal ) < Math.abs(dVal))
						dVal = Math.PI - dVal;
					
					double uncertainty = !isFixedParameter && column >= 0 ? Math.sqrt(Math.abs(varianceOfUnitWeight * cofactor)) : 0.0;
					double T           = !isFixedParameter && uncertainty > SQRT_EPS & Math.abs(dVal) > SQRT_EPS ? dVal * dVal / cofactor : 0;

					unknownParameter.setValue(value);
					unknownParameter.setUncertainty(uncertainty );
					unknownParameter.getTestStatistic().setFisherTestNumerator(T);
					unknownParameter.getTestStatistic().setDegreeOfFreedom(dim);

					unknownParameter.setFisherQuantileApriori(testStatisticParametersAprio.getQuantile());
					unknownParameter.setFisherQuantileAposteriori(testStatisticParametersApost.getQuantile());
				}
				
				if (!this.adjustModelParametersOnly)
					this.transformFramePositionPairs();
			}
		}
	}
	
	private void transformFramePositionPairs() {
		this.transformation.transformFramePositionPairs(this.Qxx);
//		ObservableUniqueList<FramePositionPair> framePositionPairs = this.transformation.getFramePositionPairs();
//		
//		Map<String, HomologousFramePositionPair> homologousFramePositionPairs = new HashMap<String, HomologousFramePositionPair>(this.homologousPointPairs.size());
//		for (HomologousFramePositionPair homologousFramePositionPair : this.homologousPointPairs)
//			homologousFramePositionPairs.put(homologousFramePositionPair.getName(), homologousFramePositionPair);
//		
//		for (FramePositionPair framePositionPair : framePositionPairs) {
//			if (!framePositionPair.isEnable())
//				continue;
//
//			this.transformationEquations.transform(framePositionPair, this.Qxx);
//			
//			if (homologousFramePositionPairs.containsKey(framePositionPair.getName())) {
//				HomologousFramePositionPair homologousFramePositionPair = homologousFramePositionPairs.get(framePositionPair.getName());
//				HomologousFramePosition targetPosition0 = homologousFramePositionPair.getTargetSystemPosition();
//				EstimatedFramePosition transformedTargetPosition = framePositionPair.getTargetSystemPosition();
//				
//				double vx = transformedTargetPosition.getX0() - targetPosition0.getX0();
//				double vy = transformedTargetPosition.getY0() - targetPosition0.getY0();
//				double vz = transformedTargetPosition.getZ0() - targetPosition0.getZ0();
//				
//				transformedTargetPosition.setX0(targetPosition0.getX0());
//				transformedTargetPosition.setY0(targetPosition0.getY0());
//				transformedTargetPosition.setZ0(targetPosition0.getZ0());
//				
//				transformedTargetPosition.setResidualX(vx);
//				transformedTargetPosition.setResidualY(vy);
//				transformedTargetPosition.setResidualZ(vz);
//			}
//		}
	}
	
	private double getEstimateVarianceOfUnitWeightApriori() {
		int dim = this.transformationEquations.getTransformationType().getDimension();
		double vari = 0;
		int cnt = 0;
		for (HomologousFramePositionPair homologousPointPair : this.homologousPointPairs) {
			if (this.interrupt)
				return 1.0;

			for (HomologousFramePosition point : homologousPointPair) {
				Matrix D = point.getDispersionApriori();
				for (int rowD = 0; rowD < dim; rowD++) {
					double var = D.get(rowD, rowD);
					vari += var;
					cnt++;
				}
			}
		}
		return vari / cnt;
	}
	
	private void reverseCenterOfMass() {
		this.transformationEquations.reverseCenterOfMasses(this.Qxx);
		
		// Reset the center of mass
		Position centerOfMaasSrc = this.transformationEquations.getCenterOfMasses().getSourceSystemPosition();
		Position centerOfMaasTrg = this.transformationEquations.getCenterOfMasses().getTargetSystemPosition();
		
		centerOfMaasSrc.setX(0);
		centerOfMaasSrc.setY(0);
		centerOfMaasSrc.setZ(0);
		
		centerOfMaasTrg.setX(0);
		centerOfMaasTrg.setY(0);
		centerOfMaasTrg.setZ(0);
	}

	private void postProcessing() {
		List<Restriction> calculations = this.transformation.getPostProcessingCalculations();
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
		for (HomologousFramePositionPair homologousPointPair : this.homologousPointPairs) {
			if (this.interrupt)
				return 0;

			int dim = this.transformationEquations.getTransformationType().getDimension();

			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(dim, nou);
			Matrix JvSrc = new DenseMatrix(dim, dim);
			Matrix JvTrg = new DenseMatrix(dim, dim);
			Vector misclosures = new DenseVector(dim);
			this.transformationEquations.normalEquationElements(homologousPointPair, Jx, JvSrc, JvTrg, misclosures);

			boolean isSourcePoint = true;
			for (HomologousFramePosition point : homologousPointPair) {
				Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
				isSourcePoint = false;

				// Create a vector of the residuals
				Vector residuals = new DenseVector(dim);

				if (dim != 1) {
					residuals.set(0, point.getResidualX());
					residuals.set(1, point.getResidualY());
				}
				if (dim != 2) {
					residuals.set(dim - 1, point.getResidualZ());
				}

				// w = -B*v + w;
				// ve = A*dx+w;
				// v = -Qll*B'*Pww*ve;
				Jv.multAdd(-1.0, residuals, misclosures);
			}
			// multAdd() --> y = alpha*A*x + y
			// to save space, the residuals of the misclosures are NOW stored in misclosures vector
			Jx.multAdd(dx, misclosures);

			UpperSymmPackMatrix Ww = this.getWeightedMatrixOfMisclosures(homologousPointPair, JvSrc, JvTrg);
			Vector Wv = new DenseVector(dim);
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
		for (HomologousFramePositionPair homologousPointPair : this.homologousPointPairs) {
			if (this.interrupt)
				return 0;
			
			int dim = this.transformationEquations.getTransformationType().getDimension();
			
			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(dim, nou);
			Matrix JvSrc = new DenseMatrix(dim, dim);
			Matrix JvTrg = new DenseMatrix(dim, dim);
			Vector misclosures = new DenseVector(dim);
			
			this.transformationEquations.normalEquationElements(homologousPointPair, Jx, JvSrc, JvTrg, misclosures);
			
			// w = -B*v + w;
			// ve = A*dx+w;
			// v = -Qll*B'*Pww*ve;
			boolean isSourcePoint = true;
			for (HomologousFramePosition point : homologousPointPair) {
				Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
				isSourcePoint = false;
				
				Vector residuals = new DenseVector(dim);
				
				if (dim != 1) {
					residuals.set(0, point.getResidualX());
					residuals.set(1, point.getResidualY());
				}
				if (dim != 2) {
					residuals.set(dim - 1, point.getResidualZ());
				}
				
				// w = -B*v + w;
				Jv.multAdd(-1.0, residuals, misclosures);
			}
			
			UpperSymmPackMatrix Ww = null;
			UpperSymmPackMatrix Dw = null;
			
			// ve = A*dx+w;
			// multAdd() --> y = alpha*A*x + y
			// to save space, the residuals of the misclosures are NOW stored in misclosures vector
			Jx.multAdd(dx, misclosures);
			if (!estimateStochasticParameters) {
				Jx = null;
				Ww = this.getWeightedMatrixOfMisclosures(homologousPointPair, JvSrc, JvTrg);
			}
			else {
				Dw = this.getDispersionOfMisclosures(homologousPointPair, JvSrc, JvTrg);
				Ww = this.inv(Dw, false);
			}
			
			Vector Wv = new DenseVector(dim);
			Ww.mult(misclosures, Wv);

			if (!estimateStochasticParameters)
				Ww = null;
			
			omega += misclosures.dot(Wv);
			
			isSourcePoint = true;
			for (HomologousFramePosition point : homologousPointPair) {
				Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
				isSourcePoint = false;
				
				Vector residuals = new DenseVector(dim);
				Vector JvTWv = new DenseVector(dim);
				Jv.transMult(Wv, JvTWv);
				// v = -Qll*B'*Pww*ve;
				Matrix D = point.getDispersionApriori();
				D.mult(-1.0/this.varianceComponentOfUnitWeight.getVariance0(), JvTWv, residuals); 
				
				if (dim != 1) {
					point.setResidualX(residuals.get(0));
					point.setResidualY(residuals.get(1));
				}
				if (dim != 2) {
					point.setResidualZ(residuals.get(dim - 1));
				}
			}
			
			if (!estimateStochasticParameters) {
				Wv = null;
				JvSrc = null;
				JvTrg = null;
			}

			// residuals are estimated - needed for deriving nabla
			if (estimateStochasticParameters) {
				int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();
				// test statistic depends on number of equation (i.e. dimension of transformation)
				TestStatisticParameterSet testStatisticParametersAprio = this.testStatisticParameters.getTestStatisticParameter(dim, Double.POSITIVE_INFINITY);
				TestStatisticParameterSet testStatisticParametersApost = this.testStatisticParameters.getTestStatisticParameter(dim, dof-dim);
				double noncentralityParameter = Math.sqrt(Math.abs(testStatisticParametersAprio.getNoncentralityParameter()));
				
				homologousPointPair.setFisherQuantileApriori(testStatisticParametersAprio.getQuantile());
				homologousPointPair.setFisherQuantileAposteriori(testStatisticParametersApost.getQuantile());
				
				this.addStochasticParameters(homologousPointPair, Jx, JvSrc, JvTrg, Dw, Ww, Wv, noncentralityParameter);
			}
		}
		return omega;
	}
	
	private void addStochasticParameters(HomologousFramePositionPair homologousPointPair, Matrix Jx, Matrix JvSrc, Matrix JvTrg, UpperSymmPackMatrix Dw, UpperSymmPackMatrix Ww, Vector weightedResidualsOfMisclosures, double nonCentralityParameter) throws NotConvergedException, MatrixSingularException, IllegalArgumentException {
		int nou = this.numberOfUnknownParameters;
		int dim = this.transformationEquations.getTransformationType().getDimension();
		int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();

		// estimate Qll = A*Qxx*AT
		Matrix QxxJxT = new DenseMatrix(nou, dim);
		this.Qxx.transBmult(Jx, QxxJxT);
		UpperSymmPackMatrix JxQxxJxT = new UpperSymmPackMatrix(dim);
		Jx.mult(QxxJxT, JxQxxJxT);
		QxxJxT = null;

		// derive a-posteriori uncertainties of the point
		// estimates Qkk = W - W * Qll * W
		Matrix JxQxxJxTW = new DenseMatrix(dim, dim);
		JxQxxJxT.mult(Ww, JxQxxJxTW);
		//JxQxxJxT = null;

		UpperSymmPackMatrix WJxQxxJxTW = new UpperSymmPackMatrix(dim);
		Ww.mult(JxQxxJxTW, WJxQxxJxTW);
		JxQxxJxTW = null;

		for (int row = 0; row < dim; row++) {
			for (int column = row; column < dim; column++) {
				WJxQxxJxTW.set(row, column, Ww.get(row, column) - WJxQxxJxTW.get(row, column));
			}
		}
		
		boolean isSourcePoint = true;
		double redundancy = 0;
		for (HomologousFramePosition point : homologousPointPair) {
			Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
			isSourcePoint = false;
			
			// estimate dispersion of residuals Qvv = Qll*B'*Qkk*B*Qll
			Matrix Qll = point.getDispersionApriori();
			
			Matrix JvQll = new DenseMatrix(dim, dim); 
			Jv.mult(1.0/this.varianceComponentOfUnitWeight.getVariance0(), Qll, JvQll);

			Matrix WJxQxxJxTWJvSrcQll = new DenseMatrix(dim, dim);
			WJxQxxJxTW.mult(JvQll, WJxQxxJxTWJvSrcQll);
			
			Matrix WJxQxxJxTWJvQll = new DenseMatrix(dim, dim);
			WJxQxxJxTW.mult(JvQll, WJxQxxJxTWJvQll);
			
			Matrix QllJvTWJxQxxJxTWJvQll = new UpperSymmPackMatrix(dim);
			JvQll.transAmult(WJxQxxJxTWJvQll, QllJvTWJxQxxJxTWJvQll);

			// estimates redundancies R = P * Qvv
			Matrix P = point.getInvertedDispersion(false);
			Matrix R = new DenseMatrix(dim,dim);
			
			if (dof > 0) {
				P.mult(this.varianceComponentOfUnitWeight.getVariance0(), QllJvTWJxQxxJxTWJvQll, R);
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
			}
			
			for (int row = 0; row < dim; row++) {
				if (row == 0 && dim != 1)
					point.setCofactorX(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));
				
				else if (row == 1 && dim != 1)
					point.setCofactorY(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));
				
				else if (dim != 2)
					point.setCofactorZ(Qll.get(row, row) / this.varianceComponentOfUnitWeight.getVariance0() - QllJvTWJxQxxJxTWJvQll.get(row, row));

			}
			QllJvTWJxQxxJxTWJvQll = null;
			
		}
		WJxQxxJxTW = null;


		// estimates the gross error of misclosures, if r >> 0
		if (dof > 0 && redundancy > Math.sqrt(Constant.EPS)) {
			// derive Qvv = Qll - A*Qxx*AT of misclosures and overwritte Dw <-- Qvv
			Dw.add(-1.0, JxQxxJxT);
			JxQxxJxT = null;
			
			// estimate Qvv * P
			Matrix QvvWw = new DenseMatrix(dim,dim);
			Dw.mult(Ww, QvvWw);

			// estimates Qnabla = P * Qvv * P = R * Qvv
			UpperSymmPackMatrix WwQvvWw = new UpperSymmPackMatrix(dim);
			QvvWw.mult(this.varianceComponentOfUnitWeight.getVariance0(), Ww, WwQvvWw);
			QvvWw = null;
			
			Vector grossErrorsOfMisclosures = new DenseVector(dim);
			Matrix invWwQvvWw = MathExtension.pinv(WwQvvWw, -1.0);
			invWwQvvWw.mult(-1.0, weightedResidualsOfMisclosures, grossErrorsOfMisclosures);

			if (dim != 1) {
				homologousPointPair.setGrossErrorX(grossErrorsOfMisclosures.get(0));
				homologousPointPair.setGrossErrorY(grossErrorsOfMisclosures.get(1));
			}
			if (dim != 2)
				homologousPointPair.setGrossErrorZ(grossErrorsOfMisclosures.get(dim - 1));

			// estimate maximum tolerable/minimal detectable bias, using same orientation as gross error vector
			// nabla0 * Pnn * nabla0 == 1  or  nabla0 * Pnn * nabla0 == ncp
			Vector minimalDetectableBias = new DenseVector(grossErrorsOfMisclosures, true);
			Vector weightedMinimalDetectableBias = new DenseVector(dim);
			WwQvvWw.mult(1.0/this.varianceComponentOfUnitWeight.getVariance0(), minimalDetectableBias, weightedMinimalDetectableBias);

			double nQn0 = minimalDetectableBias.dot(weightedMinimalDetectableBias);
			for (int j = 0; j < dim; j++)
				if (nQn0 > 0)
					minimalDetectableBias.set(j, minimalDetectableBias.get(j) / Math.sqrt(nQn0));

			if (dim != 1) {
				homologousPointPair.setMinimalDetectableBiasX(nonCentralityParameter * minimalDetectableBias.get(0));
				homologousPointPair.setMinimalDetectableBiasY(nonCentralityParameter * minimalDetectableBias.get(1));
				homologousPointPair.setMaximumTolerableBiasX(minimalDetectableBias.get(0));
				homologousPointPair.setMaximumTolerableBiasY(minimalDetectableBias.get(1));
			}
			if (dim != 2) {
				homologousPointPair.setMinimalDetectableBiasZ(nonCentralityParameter * minimalDetectableBias.get(dim - 1));
				homologousPointPair.setMaximumTolerableBiasZ(minimalDetectableBias.get(dim - 1));
			}

			// change sign, i.e. residual vs. error
			double T = -weightedResidualsOfMisclosures.dot(grossErrorsOfMisclosures);
			homologousPointPair.getTestStatistic().setFisherTestNumerator(T);
			homologousPointPair.getTestStatistic().setDegreeOfFreedom(dim);
		}
	}
	
	private NormalEquationSystem createNormalEquation() {
		int nou = this.numberOfUnknownParameters;
		int nor = this.restrictions.size();

		UpperSymmPackMatrix N = new UpperSymmPackMatrix(nou + nor);
		
		UpperSymmBandMatrix V = this.preconditioning ? new UpperSymmBandMatrix(nou + nor, 0) : null;
		DenseVector n = new DenseVector(nou + nor);

		int dim = this.transformationEquations.getTransformationType().getDimension();
		for (HomologousFramePositionPair homologousPointPair : this.homologousPointPairs) {
			if (this.interrupt)
				return null;
			
			// Derive Jacobians A, B and vector of misclosures
			Matrix Jx = new DenseMatrix(dim, nou);
			Matrix JvSrc = new DenseMatrix(dim, dim);
			Matrix JvTrg = new DenseMatrix(dim, dim);
			Vector misclosures = new DenseVector(dim);
			this.transformationEquations.normalEquationElements(homologousPointPair, Jx, JvSrc, JvTrg, misclosures);

			boolean isSourcePoint = true;
			for (HomologousFramePosition point : homologousPointPair) {
				if (this.interrupt)
					return null;
				
				Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
				isSourcePoint = false;
				
				// Create a vector of the residuals
				Vector residuals = new DenseVector(dim);
				if (dim != 1) {
					residuals.set(0, point.getResidualX());
					residuals.set(1, point.getResidualY());
				}
				if (dim != 2) {
					residuals.set(dim - 1, point.getResidualZ());
				}
				
				// w = -B*v + w;
				Jv.multAdd(-1.0, residuals, misclosures);
			}
			
			Matrix W = this.getWeightedMatrixOfMisclosures(homologousPointPair, JvSrc, JvTrg);
			JvSrc = null;
			JvTrg = null;

			// P * A
			Matrix WJx = new DenseMatrix(dim, nou);
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
					for (int colJxT = 0; colJxT < dim; colJxT++) {
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
			// Pre-conditioning == Just the square root of the main diagonal of AT*P*A
			for (int column = 0; column < N.numColumns(); column++) {
				if (this.interrupt)
					return null;
				
				double value = N.get(column, column);
				V.set(column, column, value > Constant.EPS ? 1.0 / Math.sqrt(value) : 1.0);
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

	private UpperSymmPackMatrix getDispersionOfMisclosures(HomologousFramePositionPair homologousPositionPair, Matrix JvSrc, Matrix JvTrg) {
		int dim = this.transformationEquations.getTransformationType().getDimension();
		UpperSymmPackMatrix Dw = new UpperSymmPackMatrix(dim); 
		
		boolean isSourcePoint = true;
		for (HomologousFramePosition point : homologousPositionPair) {
			if (this.interrupt)
				return null;
			
			Matrix Jv = isSourcePoint ? JvSrc : JvTrg;
			isSourcePoint = false;
			
			Matrix D = point.getDispersionApriori();
			Matrix JvD = new DenseMatrix(dim, dim);
			
			Jv.mult(1.0/this.varianceComponentOfUnitWeight.getVariance0(), D, JvD);
			JvD.transBmultAdd(Jv, Dw);
		}
		
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
	
	private UpperSymmPackMatrix getWeightedMatrixOfMisclosures(HomologousFramePositionPair homologousPointPair, Matrix JvSrc, Matrix JvTrg) throws MatrixSingularException, IllegalArgumentException {
		UpperSymmPackMatrix D = this.getDispersionOfMisclosures(homologousPointPair, JvSrc, JvTrg);
		return this.inv(D, true);
	}
	
	public UpperSymmPackMatrix getDispersionMatrix() {
		return this.Qxx;
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

	public TestStatisticParameters getTestStatisticParameters() {
		return this.testStatisticParameters;
	}
	
	public TestStatisticDefinition getTestStatisticDefinition() {
		return this.testStatisticDefinition;
	}
	
	TestStatisticParameters getTestStatisticParameters(TestStatisticDefinition testStatisticDefinition) {
		double alpha = testStatisticDefinition.getProbabilityValue();
		double beta  = testStatisticDefinition.getPowerOfTest();
		int dof = (int)this.varianceComponentOfUnitWeight.getRedundancy();
		int dim = this.transformationEquations.getTransformationType().getDimension();
		int numberOfHypotesis = this.homologousPointPairs.size() + (dof > 0 ? 1 : 0); // add one for global test //TODO add further hypotesis tests
				
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
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.change.removePropertyChangeListener(listener);
	}
	
	public void addTransformationChangeListener(TransformationChangeListener l) {
		this.listenerList.add(l);
	}
	
	public void removeTransformationChangeListener(TransformationChangeListener l) {
		this.listenerList.remove(l);
	}
	
	private void fireTransformationChanged(Transformation transformation, TransformationEventType eventType) {
		TransformationEvent evt = new TransformationEvent(transformation, eventType);
		Object listeners[] = this.listenerList.toArray();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] instanceof TransformationChangeListener)
				((TransformationChangeListener)listeners[i]).transformationChanged(evt);
		}
	}
	
	public VarianceComponent getVarianceComponentOfUnitWeight() {
		return this.varianceComponentOfUnitWeight;
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
	
	public void interrupt() {
		this.interrupt = true;
	}
	
	public EstimationType getEstimationType() {
		return this.estimationType;
	}
	
	public void setEstimationType(EstimationType estimationType) throws IllegalArgumentException {
		if (estimationType == EstimationType.L2NORM)
			this.estimationType = estimationType;
		else
			throw new IllegalArgumentException("Error, unsupported estimation type " + estimationType + "!");
	}
}
