package learning.sync;

import java.io.Serializable;
import java.util.Collection;

/**
 * the mean update model is suitable for combining linear models that have been learned in parallel on 
 * independent training set
 * @author Rania	
 *
 */
public class MeanAverageSync implements ICoordSyncMethod, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 688848442751015268L;


	public RealValuedVector syncWeights(Collection<RealValuedVector> balancingSetWeights) {
		//System.out.println("mean Synchronization round");
		long starttime = System.nanoTime();
		RealValuedVector singleModel = balancingSetWeights.iterator().next();
		RealValuedVector averagedModel = singleModel.average(balancingSetWeights);
		Long diff = (System.nanoTime() - starttime);
		
		return averagedModel;
	}
}