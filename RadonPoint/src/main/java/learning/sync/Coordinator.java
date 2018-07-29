package learning.sync;


import java.util.Collection;


/**
 * implements a static synchronisation protocol
 * 
 * @author Rania
 *
 */
public class Coordinator {

	protected ICoordSyncMethod sync;


	public Coordinator(ICoordSyncMethod sync) {
		this.sync = sync;
	}



	/**
	 * calculate the global averaged model of the given local models
	 * @param balancingSetWeights
	 * @return
	 */
	public RealValuedVector calcGSV(Collection<RealValuedVector> balancingSetWeights) {
		return sync.syncWeights(balancingSetWeights);
	}
	
	public void setAveragingFunction(ICoordSyncMethod sync) {
		this.sync = sync;
	}
	
}
