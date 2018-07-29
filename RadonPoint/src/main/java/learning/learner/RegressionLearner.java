package learning.learner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import learning.example.Parameters;
import learning.example.Parameters.SyncType;
import learning.example.functions.FlickrRadonDriver;
import learning.example.functions.FlickrReaderSingleLabel;
import learning.sync.RealValuedVector;

import smile.classification.NeuralNetworkRegression;
import smile.classification.NeuralNetworkRegression.ActivationFunction;
import smile.classification.NeuralNetworkRegression.ErrorFunction;


/**
 * the learner node
 * @author Rania
 *
 */


public class RegressionLearner {

	
	
	private NeuralNetworkRegression nnClassifier; 
	
	public static Map<SyncType, Integer> instancesCount = new HashMap<SyncType, Integer>();
	public static Map<SyncType, Integer> correctPredCount = new HashMap<SyncType, Integer>();
	public static Map<SyncType, Integer> correctPositivePredCount = new HashMap<SyncType, Integer>();
	public static Map<SyncType, Integer> falsePositivePredCount = new HashMap<SyncType, Integer>();
	public static Map<SyncType, Integer> falseNegatives = new HashMap<SyncType, Integer>();
	
	public static Map<SyncType, Double> losses = new HashMap<SyncType, Double>();
	
	private SyncType type;

	public RegressionLearner(SyncType type) {
		int[] numUnits = new int[2];
		numUnits[0] = Parameters.DATA_LENGTH;
		numUnits[1] = 1;
		//nnClassifier = new NeuralNetwork(ErrorFunction.LEAST_MEAN_SQUARES, ActivationFunction.LINEAR, numUnits);
		nnClassifier = new NeuralNetworkRegression(ErrorFunction.LEAST_MEAN_SQUARES, ActivationFunction.LINEAR, numUnits);
		this.type = type;

	}
	
	
	public void resetStatistics() {
		instancesCount.put(type, 0);
		correctPredCount.put(type, 0);
		correctPositivePredCount.put(type, 0);
		falsePositivePredCount.put(type, 0);
		falseNegatives.put(type, 0);
		losses.put(type, 0.0);
	}
	


	
	public static double printAccuracy(SyncType type) {
		System.out.println("accuracy of " + type);
		//double accuracy = (double)correctPredCount.getOrDefault(type, 0)/instancesCount.getOrDefault(type, 0);
		//System.out.println("correct predictions: " + correctPredCount.get(type) + " / " + instancesCount.get(type) + ": " +  (double)correctPredCount.get(type)/instancesCount.get(type));
		//System.out.println("precision: " + (double)correctPositivePredCount.getOrDefault(type, 0)/(correctPositivePredCount.getOrDefault(type, 0)+falsePositivePredCount.getOrDefault(type, 0)) + ", recall: " + (double)correctPositivePredCount.getOrDefault(type, 0)/(correctPositivePredCount.getOrDefault(type, 0)+falseNegatives.getOrDefault(type, 0)));
		System.out.println("loss: " + losses.get(type));
		return losses.get(type);
	}

	

	public void processInput(double[] sample, double y) {
		instancesCount.put(type, instancesCount.getOrDefault(type, 0)+1);
		double pred = nnClassifier.predictRegression(sample);
		//System.out.println(type + ":, " + pred +", " +y);
		if(pred == y) {
			correctPredCount.put(type, correctPredCount.getOrDefault(type, 0)+1);
			if(pred>0.5)
				correctPositivePredCount.put(type, correctPositivePredCount.getOrDefault(type, 0)+1);
		}
		else {
			if(pred>0.5)
				falsePositivePredCount.put(type, falsePositivePredCount.getOrDefault(type, 0)+1);
			else
				falseNegatives.put(type, falseNegatives.getOrDefault(type, 0)+1);
		}
		if(FlickrReaderSingleLabel.positiveLabelsUnfished &&FlickrRadonDriver.totalNumSamples<FlickrRadonDriver.trainingsize) {
			double loss = nnClassifier.learn(sample, y);
			losses.put(type, losses.getOrDefault(type, 0.0)+loss);
		}
	}




	public void setLocalVariable(RealValuedVector averagedModel) {
		double[][] weight = new double[1][];
		//weight[0] = new double[averagedModel.getDimension()];
		weight[0] = Arrays.copyOf(averagedModel.getValue(), averagedModel.getValue().length);
		nnClassifier.setWeight(weight);
		
	}




	public RealValuedVector getLocalVariable() {
		// TODO Auto-generated method stub
		return new RealValuedVector(nnClassifier.getWeight(1)[0]);
	}


	public void processInput(double[][] samples, int[] y) {
		nnClassifier.learn(samples, y);
		
	}

	
}