package learning.sync;

import java.util.Collection;

/**
 * the coordinator weight update model, for example, mean/median/Radon
 * @author Rania
 *
 */
public interface ICoordSyncMethod{

	/**
	 * synchronise the local weights values to a new value based on the weights received from the coordinator
	 * @param existingWeights: the existing weights
	 * @param allWeights: the new weights received from the nodes in current round
	 */
	

	public abstract RealValuedVector syncWeights(Collection<RealValuedVector> balancingSetWeights);
}