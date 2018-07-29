package learning.example.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import learning.example.Parameters;
import smile.projection.RandomProjection;

public class FlickrReaderSingleLabelNoProjection extends InputStream {

	protected int numFeatures = Parameters.DATA_LENGTH;
	private double scale = 10.0;
	private BufferedReader targetBR;
	private String labelFile = "/media/datasets/flickr/alexnet_features/target_data_photo_label_alexnet_0.txt"; // "/media/datasets/flickr/alexnet_features/target_data_photo_label_vgg19.txt";
																												// //
																												// "/media/datasets/flickr/alexnet_features/target_data_photo_label.txt";
	// //"/media/datasets/flickr/target_data/target_data_photo_label10.txt";
	private static String singleFeatureFile = "/media/datasets/flickr/CNN_features/feature_per_label_travel.txt";
	private Random random = new Random();
	private int numSamples = 0;
	private int numPosSamples = 0;
	
	private RandomProjection randomProjection;
	private int totalNumFeatures = 4096;
	private Set<String> targetLabels = new HashSet<String>();
	private BufferedReader positiveBR;

	// private String filename =
	// "/media/datasets/flickr/alexnet_features/per_feature_sample"
	public FlickrReaderSingleLabelNoProjection(String filename) {
		super(filename);
		try {
			File inFile = new File(labelFile);
			targetBR = new BufferedReader(new FileReader(inFile));
			positiveBR = new BufferedReader(new FileReader(singleFeatureFile));
			String labelRow = null;
			while ((labelRow = targetBR.readLine()) != null) {

				if (labelRow == null || labelRow.isEmpty()) {
					continue;
				}
				String[] labelStr = labelRow.split(" ");
				if (labelStr.length < 2)
					continue;
				if (labelStr[1].toLowerCase().contains(Parameters.targetAttribute))
					targetLabels.add(labelStr[0]);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		randomProjection = new RandomProjection(totalNumFeatures, Parameters.DATA_LENGTH, true);

		/*
		 * for (int i =0; i< 4096; i++) { int rnd = list.get(i);
		 * randomFeatures.add(rnd); }
		 */

	}

	public FlickrReaderSingleLabelNoProjection(String filename, double scale) {

		this(filename);
		this.scale = scale;

		Parameters.batchSize = Parameters.numNodes * Parameters.batchMult;

	}

	@Override
	public double[] nextExample() {
		// if(numSamples %100000==0)
		// System.out.println(numSamples + ", positive samples" + numPosSamples);
		double[] currentSample = new double[totalNumFeatures+1]; // no projection new double[totalNumFeatures+1];
		double[] projectedSample = null;
		String row;
		double target = 0.0;
		scale = 100;
		String[] features = null;
		int offset = 2;
		try {
			while (true) {
				double prob = random.nextDouble();
				if (prob >= 0.5) {
					target = 1.0;
					if ((row = positiveBR.readLine()) == null) {
						System.out.println("total num samples, positive samples" + numSamples + ", " + numPosSamples);
						return null;
					}
					if(row.trim().length()==0) {
						System.out.println("empty");
						continue;
					}
					++numPosSamples;
					offset = 2;
					features = row.split("(\\s)+");
					positiveBR.readLine();
					break;
				} else {
					if ((row = br.readLine()) == null)
						continue;
					features = row.split("(\\s)+");
					if (targetLabels.contains(features[0])) {
						continue;
					}
					target = 0.0;
					offset = 2;
					break;
				}
			}

			// currentSample[currentSample.length-1] = target;
			// System.out.println("correct" + labelsStr[0] + ":" + features[0]);
			for (int i = 0; i < totalNumFeatures; i++) {
				scale = 100;
				currentSample[i] = (Double.parseDouble(features[i + offset])) / scale;
				if (Math.abs(currentSample[i]) > 1) {
				// 	System.out.println("current feature larger than 1" + currentSample[i]);
				}
				currentSample[totalNumFeatures] = target; //if not project//
				//if (currentSample[i] < 0)
				//	currentSample[i] = 0;
				++i;
			}
			// projection
			//projectedSample = randomProjection.project(currentSample); // currentSample; //
			scale = 100;
			/*for (int i = 0; i < projectedSample.length; i++) {
				projectedSample[i] = projectedSample[i] / scale;
				if (projectedSample[i] > 1)
					System.out.println("projected " + projectedSample[i]);
			}*/
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		//double[] projectedSampleTarget = new double[projectedSample.length + 1];
		//System.arraycopy(projectedSample, 0, projectedSampleTarget, 0, projectedSample.length);

		//projectedSampleTarget[Parameters.DATA_LENGTH] = target;
		++numSamples;
		if (numSamples % 10000 == 0)
			System.out.println("total num samples, positive samples" + numSamples + ", " + numPosSamples);
		return currentSample;// currentSample;//projectedSampleTarget;
	}

}
