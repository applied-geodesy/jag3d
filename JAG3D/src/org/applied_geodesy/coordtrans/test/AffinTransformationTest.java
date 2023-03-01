package org.applied_geodesy.coordtrans.test;

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.transformation.AffinTransformation;
import org.applied_geodesy.adjustment.transformation.ParameterRestrictionType;
import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.parameter.ProcessingType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;

import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public class AffinTransformationTest {

	
	public static void main(String[] args) {
		System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");
		
		List<HomologousFramePositionPair> homologousPointPairs = new ArrayList<HomologousFramePositionPair>();
		
		homologousPointPairs.add(new HomologousFramePositionPair("1", 4157222.543, 664789.307, 4774952.099,   4157870.237, 664818.678, 4775416.524));
		homologousPointPairs.add(new HomologousFramePositionPair("2", 4149043.336, 688836.443, 4778632.188,   4149691.049, 688865.785, 4779096.588));
		homologousPointPairs.add(new HomologousFramePositionPair("3", 4172803.511, 690340.078, 4758129.701,   4173451.354, 690369.375, 4758594.075));
		homologousPointPairs.add(new HomologousFramePositionPair("4", 4177148.376, 642997.635, 4760764.800,   4177796.064, 643026.700, 4761228.899));
		homologousPointPairs.add(new HomologousFramePositionPair("5", 4137012.190, 671808.029, 4791128.215,   4137659.549, 671837.337, 4791592.531));
		homologousPointPairs.add(new HomologousFramePositionPair("6", 4146292.729, 666952.887, 4783859.856,   4146940.228, 666982.151, 4784324.099));
		homologousPointPairs.add(new HomologousFramePositionPair("7", 4138759.902, 702670.738, 4785552.196,   4139407.506, 702700.227, 4786016.645));

//		homologousPointPairs.add(new HomologousFramePositionPair("1", 585.435,  755.475, 102.520, 929.580, 422.800, -0.210));
//		homologousPointPairs.add(new HomologousFramePositionPair("2", 553.175,  988.105, 104.190, 575.360, 480.900,  2.370));
//		homologousPointPairs.add(new HomologousFramePositionPair("3", 424.045,  785.635, 106.125, 812.370, 200.820, -0.240));
//		homologousPointPairs.add(new HomologousFramePositionPair("4", 394.950, 1061.700, 106.070, 396.280, 283.240,  0.410));
		
//		homologousPointPairs.add(new HomologousFramePositionPair("1", 1094.883,  820.085, 109.821, 10037.81, 5262.09, 772.04));
//		homologousPointPairs.add(new HomologousFramePositionPair("2",  503.891, 1598.698, 117.685, 10956.68, 5128.17, 783.00));
//		homologousPointPairs.add(new HomologousFramePositionPair("3", 2349.343,  207.658, 151.387,  8780.08, 4840.29, 782.62));
//		homologousPointPairs.add(new HomologousFramePositionPair("4", 1395.320, 1348.853, 215.261, 10185.80, 4700.21, 851.32));
		
		try {
			TransformationAdjustment adjustment = new TransformationAdjustment();
			adjustment.setMaximalNumberOfIterations(500);
			AffinTransformation transformation = new AffinTransformation();
			
			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_X));
			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_Y));
			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SHEAR_Z));
			
//			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_X));
//			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Y));
//			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.FIXED_SCALE_Z));

			
			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_XY));
			transformation.getRestrictions().add(transformation.getSupportedParameterRestrictions().get(ParameterRestrictionType.IDENTICAL_SCALE_XZ));
			
			transformation.getTransformationEquations().getHomologousFramePositionPairs().addAll(homologousPointPairs);
			transformation.deriveInitialGuess();
			for (UnknownParameter unknownParameter : transformation.getUnknownParameters()) {
				if (unknownParameter.getProcessingType() == ProcessingType.ADJUSTMENT)
					System.out.println(unknownParameter.getValue0());
			}

			adjustment.setTransformation(transformation);
			adjustment.init();
			EstimationStateType type = adjustment.estimateModel();
			
			for (UnknownParameter unknownParameter : transformation.getUnknownParameters()) {
				System.out.println(unknownParameter.getParameterType()+" \t "+unknownParameter.getValue()+" \t "+unknownParameter.getUncertainty());
			}
			
			for (HomologousFramePositionPair homologousPointPair : homologousPointPairs) {
				homologousPointPair.getSourceSystemPosition().reset();
				homologousPointPair.getTargetSystemPosition().reset();
				homologousPointPair.getTargetSystemPosition().setX0(0);
				homologousPointPair.getTargetSystemPosition().setY0(0);
				homologousPointPair.getTargetSystemPosition().setZ0(0);
				transformation.getTransformationEquations().transform(homologousPointPair, adjustment.getDispersionMatrix());
				
				System.out.print(homologousPointPair.getName()+"  ");
				System.out.print(homologousPointPair.getTargetSystemPosition().getX0()+"  ");
				System.out.print(homologousPointPair.getTargetSystemPosition().getY0()+"  ");
				System.out.print(homologousPointPair.getTargetSystemPosition().getZ0()+"  ");
				
				System.out.print(homologousPointPair.getTargetSystemPosition().getUncertaintyX()+"  ");
				System.out.print(homologousPointPair.getTargetSystemPosition().getUncertaintyY()+"  ");
				System.out.println(homologousPointPair.getTargetSystemPosition().getUncertaintyZ()+"  ");
			}

		} catch (MatrixSingularException | IllegalArgumentException | UnsupportedOperationException | NotConvergedException e) {
			e.printStackTrace();

		}
		
		
		
	}
}
