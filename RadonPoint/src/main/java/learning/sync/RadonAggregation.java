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
		private static final double epsilon = 0.0001;
		private static final double max_rel_epsilon = 0.000001;
		private int numSync = 0;
		private int treeHeight = 5;
		private static Random random = new Random();
		public static int numberSingular = 0;
		
		/*public static void main(String [] args) {
			random = new SecureRandom();
			Collection <RealValuedVector>samples = new ArrayList<RealValuedVector>(4099);
			for (int i = 0; i< 4099; i++) {
				double [] s = new double[4096];
				for(int n=0; n<4096; n++) {
					s[n] = random.nextDouble()*random.nextDouble()*random.nextDouble()*100;
				}
				RealValuedVector r = new RealValuedVector(s);
				samples.add(r);
			}
			getRadonPoint1(samples);
			
		}*/
		@SuppressWarnings("unused")
		//@Override
		public RealValuedVector syncWeights(Collection<RealValuedVector> balancingSetWeights) {
			//System.out.println("radon Synchronization round");
			/*if(Parameters.required_height ==0) {
				int rndIndex = random.nextInt(balancingSetWeights.size());
				RealValuedVector [] arr = balancingSetWeights.toArray(new RealValuedVector[balancingSetWeights.size()]);
				return arr[rndIndex];
			}*/
			if (Parameters.numNodes < Parameters.DATA_LENGTH + 3) {
				//int rndindex = random.nextInt(Parameters.numNodes);
				//return ((List<RealValuedVector>) balancingSetWeights).get(rndindex);
				return null;
			} else if (Parameters.numNodes == Parameters.DATA_LENGTH + 3) {
				double[] radonVector = getRadonPoint1(balancingSetWeights);
				if(radonVector ==null) {
					//return (RealValuedVector) ((List)balancingSetWeights).get(balancingSetWeights.size()-1);
					return null;// indicating that each model should use their own current model
				}
				RealValuedVector averagedModel = new RealValuedVector(radonVector);
			
				//System.out.println("Radon Mean Sync time: " + diff);
				numSync++;
				//System.out.println("Num of radon synchronizations so far: " + numSync);
				//System.out.println("returning radon vector");
				return (RealValuedVector) averagedModel;
			}
			else if (Parameters.numNodes != Parameters.DATA_LENGTH + 3) {
				Collections.shuffle((List<?>) balancingSetWeights);
				int intermediateSize = Parameters.DATA_LENGTH + 3;
				int i = 0;
				treeHeight = (int) (Math.log(Parameters.numNodes) / Math.log(intermediateSize));
				//treeHeight = Parameters.required_height;
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
				//System.out.println("returning radon vector");
				return intermediateRadons.get(rndindex);
			}
			
			return null;
		}
	
		
		/*private Object getRadonRec(Collection<RealValuedVector> balancingSetWeights) {
			if(balancingSetWeights==null || balancingSetWeights.size()==0)
				return null;
			if(balancingSetWeights.size()==Parameters.DATA_LENGTH+3) {
				Collections.shuffle((List<?>) balancingSetWeights);
				return getRadonPoint1(balancingSetWeights);
			}
			int intermediateSize = Parameters.DATA_LENGTH + 3;
			//getRadonPoint1(Collection<RealValuedVector> balancingSetWeights) 
			
		}*/


		private double[] getRadonPoint1(Collection<RealValuedVector> balancingSetWeights) {
			// the number of nodes (balanicingset) has to be equal to the weights
			// vector plus 2.
			Iterator<RealValuedVector> it = balancingSetWeights.iterator();
			//RealValuedVector vec = (RealValuedVector) (balancingSetWeights.iterator().next());
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
//				System.err.println("singular matrix, returning null");
				System.err.println("singular matrix, returning svd solution");
				numberSingular++;
				//return new MeanAverageSync().syncWeights(balancingSetWeights).getValue();
				solver = new SingularValueDecomposition(S).getSolver();
				solution = solver.solve(constants);
				
			}
			//System.out.println("not singular matrix");
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
				// throw proper exception
			}
			double[] radonVector = new double[size - 2];
			for (i = 0; i < size - 2; i++) {
				radonVector[i] = 0.0;
				for (int k = 0; k < positiveIndices.size(); k++) {
					radonVector[i] += positiveRadonSolution.get(k) * solutions[positiveIndices.get(k)][i];
				}
				radonVector[i] /= positiveSum;
			}
			//System.out.println("****************** radon vector" + Arrays.toString(radonVector));

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

