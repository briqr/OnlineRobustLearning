package learning.example;


/**
 * some learning constants
 * @author Rania
 *
 */
public class Parameters {
	/**
	 * the number of learner/worker nodes
	 */
		
	/**
	 * the number of instances to be generated, a negative value indicates an infinite number of samples
	 */
	
	public enum SyncType {radon, geometric_median, mean, nosync};
	
	public static int instancesNum = 10000000;
	

	// the number of steps needed to send an update to the coordinator
	public static int batchSize = 48400; // 200*numNodes;
	
	public static int batchMult = 200; // the actual batch size
	public static int height = 1; // the height of the Iterated Radon point algorithm

	//public static int required_height = 2; // the height of the Iterated Radon point algorithm
	/**
	 * The number of features in a data sample	
	 */
	public static int DATA_LENGTH =  90; // the projection new number of attributes

	public static int numNodes = 93;


	public static String singleFeatureFile = "/media/datasets/flickr/CNN_features/feature_per_label_church.txt";;
	public static final String targetAttribute = "church";
	
	public static final double decisionThreshold = 0.0;

	

}
