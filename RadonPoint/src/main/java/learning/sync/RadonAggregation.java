package learning.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;

import learning.example.Parameters;
	
	/**
	 * using the iterated Radon point algorithm for averaging the models across all
	 * the learners. 
	 * 
	 * @author Rania
	 *
	 * @param <Index>
	 * @param <Value>
	 */
	public class  RadonAggregation implements ICoordSyncMethod, Serializable {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1436768541164539157L;
		private static final double epsilon = 0.001;
		private static final double max_rel_epsilon = 0.0001;
		private int numSync = 0;
		private int treeHeight = 5;
		private static Random random = new Random();
		public static int numberSingular = 0;
		
		
		@SuppressWarnings("unused")
		//@Override
		public RealValuedVector syncWeights(Collection<RealValuedVector> balancingSetWeights) {
			
			if (Parameters.numNodes < Parameters.DATA_LENGTH + 3) {
				
				return null;
			} else if (Parameters.numNodes == Parameters.DATA_LENGTH + 3) {
				double[] radonVector = getRadonPoint1(balancingSetWeights);
				if(radonVector ==null) {
					return null;// indicating that each model should use their own current model
				}
				RealValuedVector averagedModel = new RealValuedVector(radonVector);
			
				numSync++;
				
				return (RealValuedVector) averagedModel;
			}
			else if (Parameters.numNodes != Parameters.DATA_LENGTH + 3) {
				Collections.shuffle((List<?>) balancingSetWeights);
				int intermediateSize = Parameters.DATA_LENGTH + 3;
				int i = 0;
				treeHeight = (int) (Math.log(Parameters.numNodes) / Math.log(intermediateSize));
				ArrayList<RealValuedVector> prevIntermediateRadons = new ArrayList<RealValuedVector>(balancingSetWeights);
				ArrayList<RealValuedVector> intermediateRadons = new ArrayList<RealValuedVector>();
				while (i < treeHeight) {
					int numIters = prevIntermediateRadons.size() / intermediateSize;
					int k = 0;
					intermediateRadons = new ArrayList<RealValuedVector>();
					while (k < numIters) {
						int endIndex = intermediateSize * k + intermediateSize;
						if (endIndex > prevIntermediateRadons.size())
							endIndex = prevIntermediateRadons.size();
						List<RealValuedVector> subList = ((List<RealValuedVector>) prevIntermediateRadons)
								.subList(intermediateSize * k, endIndex);
						double[] radonVector = getRadonPoint1(subList);
						if(radonVector ==null) {
							//return (RealValuedVector) ((List)balancingSetWeights).get(balancingSetWeights.size()-1);
							return null;
						}
						RealValuedVector averagedModel = new RealValuedVector(radonVector);
						intermediateRadons.add((RealValuedVector) averagedModel);
						++k;
					}
					prevIntermediateRadons = new ArrayList<RealValuedVector>(intermediateRadons);
					Collections.shuffle((List<?>) prevIntermediateRadons);
					++i;
				}
				int rndindex = random.nextInt(intermediateRadons.size());
				return intermediateRadons.get(rndindex);
			}
			
			return null;
		}
	
	


		private double[] getRadonPoint1(Collection<RealValuedVector> balancingSetWeights) {
			// the number of nodes (balanicingset) has to be equal to the weights
			// vector plus 2.
			Iterator<RealValuedVector> it = balancingSetWeights.iterator();
			int size = Parameters.DATA_LENGTH + 3;//vec.getValue().length + 2;
			double[][] solutions = new double[size][size];
			int i = 0;
			while (it.hasNext()) {
				double[] solAsCol = ((RealValuedVector) it.next()).getValue();
				System.arraycopy(solAsCol, 0, solutions[i], 0, solAsCol.length);
				solutions[i][size - 2] = 1;
				solutions[i][size - 1] = 0;
				i++;
			}
			solutions[0][size - 1] = 1;
	
			double constantsCols[] = new double[size];
			constantsCols[size - 1] = 1;
	
	
			RealMatrix S = new Array2DRowRealMatrix(solutions, false);
			S = S.transpose();
			DecompositionSolver solver = new LUDecomposition(S).getSolver();
	
			RealVector constants = new ArrayRealVector(constantsCols, false);
			RealVector solution = null;
			try  {
			solution = solver.solve(constants);
			}
			catch (Exception e){
				System.err.println("singular matrix, returning svd solution");
				numberSingular++;
				solver = new SingularValueDecomposition(S).getSolver();
				solution = solver.solve(constants);
				
			}
			Vector<Double> positiveRadonSolution = new Vector<Double>();
			Vector<Double> negativeRadonSolution = new Vector<Double>();
			double positiveSum = 0;
			double negativeSum = 0;
			Vector<Integer> positiveIndices = new Vector<Integer>();
			for (i = 0; i < solution.getDimension(); i++) {
				double currentCoeff = solution.getEntry(i);
				if (currentCoeff > 0) {
					positiveRadonSolution.add(currentCoeff);
					positiveSum += currentCoeff;
					positiveIndices.add(i);
				} else if (currentCoeff < 0) {
					negativeRadonSolution.add(currentCoeff);
					negativeSum += currentCoeff;
				}
			}
			if (!areDoubleEqual(positiveSum, -negativeSum)) {
				System.err.println("Something unexpected happened: " + positiveSum + ", " + negativeSum);

			}
			double[] radonVector = new double[size - 2];
			for (i = 0; i < size - 2; i++) {
				radonVector[i] = 0.0;
				for (int k = 0; k < positiveIndices.size(); k++) {
					radonVector[i] += positiveRadonSolution.get(k) * solutions[positiveIndices.get(k)][i];
				}
				radonVector[i] /= positiveSum;
			}
		
			return radonVector;
		}
	
	
	
	
	
	
		
		


		private static boolean areDoubleEqual(double x, double y) {
			if (x == y)
				return true;
			if (Math.abs(x - y) < epsilon)
				return true;
			double relErorr;
			if (Math.abs(y) < Math.abs(x)) {
				relErorr = Math.abs((x - y) / y);
			} else {
				relErorr = Math.abs((x - y) / x);
			}
			if (relErorr < max_rel_epsilon)
				return true;
			return false;
		}
		
		
		
		
	}

