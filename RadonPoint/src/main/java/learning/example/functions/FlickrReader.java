package learning.example.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import learning.example.Parameters;
import smile.projection.RandomProjection;

public class FlickrReader extends InputStream {

	protected int numFeatures = Parameters.DATA_LENGTH;
	private double scale = 10.0;
	private BufferedReader targetBR;
	// This is used to draw negative samples in addition to the all positive drawn samples from the single file
	private String labelFile = "/media/datasets/flickr/alexnet_features/target_data_photo_label_alexnet_0.txt"; //"/media/datasets/flickr/alexnet_features/target_data_photo_label_vgg19.txt"; // "/media/datasets/flickr/alexnet_features/target_data_photo_label.txt";
																									// //"/media/datasets/flickr/target_data/target_data_photo_label10.txt";
	private Random random = new Random();
	private int numSamples = 0;
	private int numPosSamples = 0;
	private int numNegSamples = 0;
	
	private RandomProjection randomProjection;
	private int totalNumFeatures = 4096;
	private Set<String> targetLabels = new HashSet<String>();

	//private String filename = "/media/datasets/flickr/alexnet_features/per_feature_sample" 
	public FlickrReader(String filename) {
		super(filename);
		try {
			File inFile = new File(labelFile);
			targetBR = new BufferedReader(new FileReader(inFile));

			String labelRow = null;
			while ((labelRow = targetBR.readLine()) != null) {

				if (labelRow == null || labelRow.isEmpty()) {
					continue;
				}
				String[] labelStr = labelRow.split(" ");
				if (labelStr.length < 2)
					continue;
				if(labelStr[1].toLowerCase().contains(Parameters.targetAttribute))
					targetLabels.add(labelStr[0]);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*List<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < 4096; i++) {
			list.add(new Integer(i + 2));
		}
		Collections.shuffle(list);

		
		  for (int i =0; i< Parameters.DATA_LENGTH; i++) { int rnd = list.get(i);
		  randomFeatures.add(rnd); }
		 */
		randomProjection = new RandomProjection(totalNumFeatures, Parameters.DATA_LENGTH, true);

		/*
		 * for (int i =0; i< 4096; i++) { int rnd = list.get(i);
		 * randomFeatures.add(rnd); }
		 */

	}

	public FlickrReader(String filename, double scale) {

		this(filename);
		this.scale = scale;
		// Parameters.DATA_LENGTH = 40;
		// Parameters.numNodes = (int) Math.pow(Parameters.DATA_LENGTH + 3,
		// Parameters.height);
		Parameters.batchSize = Parameters.numNodes * Parameters.batchMult;

		/*
		 * this.scale = scale; try { br = new BufferedReader( new InputStreamReader( new
		 * GZIPInputStream( new FileInputStream(filename)))); } catch(Exception e) {
		 * System.out.println(e.getMessage()); }
		 */
	}

	@Override
	public double[] nextExample() {
		//if(numSamples %100000==0)
			//System.out.println(numSamples + ", positive samples" + numPosSamples);
		double[] currentSample = new double[totalNumFeatures]; // no projection new double[totalNumFeatures+1]; 
		double [] projectedSample = null;
		String row;
		double target = 0.0;
		scale = 100;
		String[] features = null;
		try {
			while (true) {
				if ((row = br.readLine()) == null) {
						System.out.println("total num samples, positive samples" + numSamples + ", " + numPosSamples);
						return null;	
				}
				
					features = row.split("(\\s)+");
					
				
					//System.out.println("photo label found");
					if(targetLabels.contains(features[0])) {
						//System.out.println("found target features");
						//currentSample[Parameters.DATA_LENGTH] = 1.0;
						target = 1.0;
						++numPosSamples;
						break;
					} else {
						double prob = random.nextDouble();
						if (prob <0.02 && numNegSamples <numPosSamples+50 ) {//0.018) {//0.025) { // include this negative example
							//currentSample[Parameters.DATA_LENGTH] = 0.0;
							++numNegSamples;
							target = 0.0;
							break;
						} else
							continue;
					}
			}
				//currentSample[currentSample.length-1] = target;
				//System.out.println("correct" + labelsStr[0] + ":"  + features[0]);
				for (int i =0 ;i <totalNumFeatures; i++) {
					scale = 1000;
					currentSample[i] = (Double.parseDouble(features[i+2])) / scale;
					if(Math.abs(currentSample[i]) > 1) 
						 System.out.println("current feature larger than 1" + currentSample[i]);
					if(currentSample[i] < 0) 
						currentSample[i] = 0;
					++i;
				}
				// projection
					projectedSample = randomProjection.project(currentSample); // currentSample; //
					scale = 100;
					for (int i =0 ;i <projectedSample.length; i++) {
						projectedSample[i] = projectedSample[i] / scale;
						if(projectedSample[i]> 1)
							System.out.println("projected " + projectedSample[i]);
					}
		}	
		 
		catch (Exception e) {
			e.printStackTrace();
		}
		double [] projectedSampleTarget = new double[projectedSample.length+1];
		System.arraycopy(projectedSample, 0, projectedSampleTarget,0,  projectedSample.length);

		projectedSampleTarget[Parameters.DATA_LENGTH] = target;
		++numSamples;
		if(numSamples%10000==0)
			System.out.println("total num samples, positive samples" + numSamples+ ", " + numPosSamples);
		return projectedSampleTarget;//currentSample;//projectedSampleTarget;
	}

}
