package learning.example.functions;

/**
 * Represents a hypothesis 
 * @author Rania
 *
 * @param <T>
 */
public abstract class Hypothesis <T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5509930849682610894L;
	
	public abstract T evaluateInstance(T[] currentSample);
	
	public abstract int getHypothesisLength();

	public Double evaluateInstance(Double[] currentSample) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
