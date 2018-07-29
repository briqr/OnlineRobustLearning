package learning.example.functions;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import learning.example.Parameters;
import learning.example.Parameters.SyncType;
import learning.learner.ClassifierLearner;
import learning.sync.Coordinator;
import learning.sync.GeometricMedianSync;
import learning.sync.MeanAverageSync;
import learning.sync.RadonAggregation;
import learning.sync.RealValuedVector;

public class FlickrRadonDriverSusy {

	//public enum SyncType{geometric_median, radon, nosync, mean}
	private int nextNodeIndex = 0;
	 

	private int numSamples = 0;
	
	private String datafile = "/home/briq/Noise_Radon/SUSY.csv";
	
	private Map<SyncType, ArrayList<ClassifierLearner>> learners;

	private Map<SyncType, Coordinator> coords = new HashMap<SyncType, Coordinator>();

	//private int testSize = 50;

	private Map<Integer, Boolean> brokenNodesIndicator = new HashMap<Integer, Boolean>();
	//private final static int numOutliers = 700;
	
	private Random random = new Random();
	
	//private List<double[]> samples = new ArrayList<double[]>();
	private int numSyncRounds = 0;

	private double recoveryProb =0.8;
	private double failureProb = 0.01;
	private static final double featureErrorProb = 0.2;
	
	private double stdDeviation = 100.5;
	
	public static int totalNumSamples = 0;
	public static int trainingsize = 4000000;
	public static void main(String[] args) {
	
		FlickrRadonDriverSusy example = new FlickrRadonDriverSusy();
		example.learn();
				
	}

	public FlickrRadonDriverSusy() {
	}
	
	private void learn() {
	
		RealValuedInputStreamCSVRegression dataStream = null;
		dataStream = new RealValuedInputStreamCSVRegression(datafile, 1);
		initLearners();
		//List<Double> targets = new ArrayList<Double>();
		
		
		initCoord();
		for(int i = 0; i< Parameters.instancesNum; i++) {
			double[] sample = dataStream.nextExample();
			if (sample == null) {
				break;
			}
			
			int y = (int)sample[sample.length-1];
			double [] sampleNoL = new double[sample.length-1];
			System.arraycopy(sample, 0, sampleNoL, 0, sample.length-1);
			//if(i==samples.size()-testSize)
			//	break;
			//turnToOutlier1(sampleNoL);
			routeSample(sampleNoL, y, Parameters.batchSize, SyncType.radon);
			routeSample(sampleNoL, y, Parameters.batchSize, SyncType.geometric_median);
			routeSample(sampleNoL, y, Parameters.batchSize, SyncType.mean);
			boolean isSync = routeSample(sampleNoL, y, Parameters.batchSize, SyncType.nosync);
			totalNumSamples++;
			if(isSync) {
				numSamples = 0;
				numSyncRounds++;
				if(numSyncRounds==1) { //  just to have clean statistics free from differences of initializations. 
					learners.get(SyncType.radon).get(0).resetStatistics();
					learners.get(SyncType.geometric_median).get(0).resetStatistics();
					learners.get(SyncType.mean).get(0).resetStatistics();
					learners.get(SyncType.nosync).get(0).resetStatistics();
				}
			}
			
			else 
				++numSamples;
			++i;
			nextNodeIndex = (nextNodeIndex + 1) % learners.get(SyncType.mean).size();
			if(false) {//(numSamples == 5*Parameters.numNodes) {
				System.out.println("printing accuracies mid way");
				printAccuracies();
			}
			
		}
		System.out.println("end ********************************" + numSyncRounds + "**************");

		printAccuracies();
	}

	
	
	private void printAccuracies() {
		ClassifierLearner.printAccuracy(SyncType.radon);
		ClassifierLearner.printAccuracy(SyncType.geometric_median);
		ClassifierLearner.printAccuracy(SyncType.mean);
		ClassifierLearner.printAccuracy(SyncType.nosync);
	}

	private void initLearners() {
		
		learners = new HashMap<SyncType, ArrayList<ClassifierLearner>>();
		
		
		createClassifiers(SyncType.radon);
		createClassifiers(SyncType.geometric_median);
		createClassifiers(SyncType.mean);
		createClassifiers(SyncType.nosync);
		
		
	}


	

	private void createClassifiers(SyncType type) {
		
		ArrayList <ClassifierLearner> currentLearners = new ArrayList<ClassifierLearner>();
		for (int i = 0; i < Parameters.numNodes; i++) {
			ClassifierLearner learner = new ClassifierLearner(type);
			currentLearners.add(learner);
		}
		learners.put(type, currentLearners);
	}


	
	private void initCoord() {
		
		coords.put(SyncType.mean, new Coordinator(new MeanAverageSync()));
		coords.put(SyncType.radon, new Coordinator(new RadonAggregation()));
		coords.put(SyncType.geometric_median, new Coordinator(new GeometricMedianSync()));
		coords.put(SyncType.nosync, new Coordinator(new MeanAverageSync()));
				
	}

		



	private boolean routeSample(double[] sample, int y, int batchSize, SyncType type) {
		
		learners.get(type).get(nextNodeIndex).processInput(sample, y);
	
		if (numSamples == Parameters.batchSize) {
			if(type != SyncType.nosync &&  FlickrReaderSingleLabel.positiveLabelsUnfished && totalNumSamples<trainingsize)
				sendModelsToCoordinator(type);
			if(type==SyncType.radon)
				System.out.println("********************************" + numSyncRounds + "**************");
			ClassifierLearner.printAccuracy(type);
			
			return true;
		}
		 if(totalNumSamples==trainingsize)
				System.out.println("reached the training size, no more synchronizations");
		return false;

	}

	
	

	private void sendModelsToCoordinator(SyncType type) {
		Collection<RealValuedVector> balancingSetWeights = new ArrayList<RealValuedVector>();
		SyncType syncType = type;
		if(numSyncRounds==0) {
			syncType = SyncType.mean; // just so all models start from the an identical point to avoid differencces from random init
		}
		for (ClassifierLearner currentLeaner : learners.get(syncType)) {
			RealValuedVector currentModel = currentLeaner.getLocalVariable();
			balancingSetWeights.add(currentModel);
		}
		long startTime = System.nanoTime();	
		RealValuedVector  averagedModel = coords.get(type).calcGSV(balancingSetWeights);
		long endTime = System.nanoTime();
		long runtime = endTime - startTime;
		//runTimes.put(syncType, runTimes.getOrDefault(syncType, 0L)+runtime);
		
		if(averagedModel == null) {
			
			 return;
		}
		for (ClassifierLearner currentLeaner : learners.get(type)) {
			currentLeaner.setLocalVariable(averagedModel);
		}
		
	}
	
	
	private void turnToOutlier1(double[] sample) {
		double prob;
		boolean perturb = false;
		if(brokenNodesIndicator.getOrDefault(nextNodeIndex, false)) {//already broken
			prob = random.nextDouble();
			if(prob >= (1-recoveryProb)) // recover
				brokenNodesIndicator.put(nextNodeIndex, false);
			else
				perturb = true;
				
		}
		else {
			prob = random.nextDouble();
			if (prob>=(1-failureProb)) { // node is now broken
				brokenNodesIndicator.put(nextNodeIndex, true);
				perturb = true;
			}

		}
		if(perturb) {
			for (int i = 0; i < sample.length; i++) {
				prob = random.nextDouble();
				if (prob >= featureErrorProb ) {
					double magnitude = random.nextGaussian()*stdDeviation;
					sample[i] += magnitude ;
				}
			}
			//sample[sample.length-1] +=random.nextGaussian()*stdDeviation; 
			//++numberOfFlipped;
		}


	}

	
}
	
	
	
