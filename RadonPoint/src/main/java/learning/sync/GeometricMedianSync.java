package learning.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * the mean update model is suitable for combining linear models that have been learned in parallel on 
 * independent training set
 * This geometric median implementation has been adapted from https://github.com/j05u3/weiszfeld-implementation. 	
 *
 */
public class GeometricMedianSync  implements ICoordSyncMethod , Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 688848442751015268L;

	public static void main(String [] args) {
		
		//S = np.array ([[0.71, 5.4], [-0.6, 0.5], [-1.3, 1.3], [6.4, 5.2]]) 		
		RealValuedVector x1 = new RealValuedVector(new double[]{2.71, 5.4});
		RealValuedVector x2 = new RealValuedVector(new double[]{-0.6, 1.5});
		RealValuedVector x3 = new RealValuedVector(new double[]{-1.3, 1.3});
		RealValuedVector x4 = new RealValuedVector(new double[]{15.4, 15.2});
		Collection<RealValuedVector> x = new ArrayList<RealValuedVector>();
		x.add(x1);
		x.add(x2);
		x.add(x3);
		x.add(x4);
		GeometricMedianSync g = new GeometricMedianSync();
		RealValuedVector res = g.syncWeights(x);
		System.out.println("the geometric median is: " );
	}
	
	
	
	
	
	
	public Point process(Input input) {

        // filtering repeated points
        Map<Point, Double> map = new HashMap<Point, Double>();
        for (WeightedPoint wPoint : input.getPoints()) {
            Point point = wPoint.getPoint();
            if (map.containsKey(point)) {
                map.put(point, map.get(point) + wPoint.getWeight());
            } else {
                map.put(point, wPoint.getWeight());
            }
        }
        
        // anchor points
        List<WeightedPoint> aPoints = new ArrayList<WeightedPoint>(map.size());
        for (Map.Entry<Point, Double> entry : map.entrySet()) {
            WeightedPoint wPoint = new WeightedPoint();
            wPoint.setPoint(entry.getKey());
            wPoint.setWeight(entry.getValue());
            aPoints.add(wPoint);
        }

        int n = input.getDimension();
        int maxIterations = Integer.MAX_VALUE;
        if (input.getMaxIterations() != null) {
            maxIterations = input.getMaxIterations();
        }
        Double permissibleError = input.getPermissibleError();
        if (permissibleError == null) {
            permissibleError = Double.MIN_VALUE;
        }
        
        // choosing starting point
        Point startPoint = null;
        double mini = Double.POSITIVE_INFINITY;
        for (WeightedPoint wPoint : aPoints) {
            double eval = evaluateF(wPoint.getPoint(), aPoints);
            if (eval < mini) {
                mini = eval;
                startPoint = wPoint.getPoint();
            }
        }

        Point x = null;
        try {
            x = (Point)startPoint.clone();
        } catch (Exception ex) {
           // Logger.getLogger(WeiszfeldAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        	return null;
        }
        
        Point lastX;
        double error;
        int iterationCounter = 0;
        do {
            lastX = x;
            if (map.containsKey(x)) {
                Point rj = R(x, aPoints, n);
                double wj = map.get(x);
                if (rj.getNorm() > wj) {
                    x = operatorS(x, wj, rj, aPoints, n);
                }
            } else {
                x = operatorT(x, aPoints, n);
            }
            error = Point.substraction(x, lastX).getNorm();
            iterationCounter++;
        } while (error > permissibleError && iterationCounter < maxIterations);
        if(error>permissibleError) {
        	//System.err.println("number iterations exceeded " +maxIterations);
        }
        /* Stops whenever the error is less than or equal the permissibleError
        *  or reaches the maximum number of iterations.
        */

        //Output output = new Output();
        //output.setPoint(x);
        //output.setLastError(error);
       // output.setNumberOfIterations(iterationCounter);
        
        return x;
    }
    private Point operatorT(Point x, List<WeightedPoint> aPoints, int dimension) {
        Point result = new Point(dimension);

        double weightsSum = 0;
        for (WeightedPoint a: aPoints) {
            double w = a.getWeight();
            double curWeight = w/Point.substraction(x,a.getPoint()).getNorm();
            Point cur = Point.multiply(a.getPoint(), curWeight);

            weightsSum += curWeight;
            result.add(cur);
        }

        return result.multiply(1d/weightsSum);
    }
    
    private Point operatorS(Point aj, double wj, Point rj
            , List<WeightedPoint> aPoints, int dimension) {
        double rjNorm = rj.getNorm();        
        Point dj = new Point(dimension);
        dj.add(rj);
        dj.multiply(-1.0/rjNorm);
        
        // calculating tj (stepsize) taken from Vardi and Zhang
        double lj = operatorL(aj, aPoints);
        double tj = (rjNorm - wj)/lj;
        
        dj.multiply(tj);
        dj.add(aj);
        
        return dj;
    }
    
    private Point R(Point aj, List<WeightedPoint> aPoints, int dimension) {
        Point result = new Point(dimension);
        
        for (WeightedPoint ai: aPoints) {
            if (ai.getPoint().compareTo(aj) != 0) {
                double w = ai.getWeight();
                Point dif = Point.substraction(ai.getPoint(), aj);
                double factor = w/dif.getNorm();
                dif.multiply(factor);

                result.add(dif);
            }
        }
        
        return result;
    }

    private double operatorL(Point aj, List<WeightedPoint> aPoints) {
        double res = 0;
        for (WeightedPoint ai: aPoints) {
            if (aj.compareTo(ai.getPoint()) != 0) {
                Point dif = Point.substraction(aj, ai.getPoint());
                res += ai.getWeight()/dif.getNorm();
            }
        }
        return res;
    }
    
    /**
     * Evaluating the objective function in a given point x
     * @param x Point to evaluate the function.
     * @param aPoints List of weighted points.
     * @return 
     */
    private double evaluateF(Point x, List<WeightedPoint> aPoints) {
        double res = 0;
        for (WeightedPoint ai: aPoints) {
            res += ai.getWeight() * Point.substraction(ai.getPoint(), x).getNorm();
        }
        return res;
    }






    
    
	public RealValuedVector syncWeights(Collection<RealValuedVector> balancingSetWeights) {
		//System.out.println("Synchronization round");
		long starttime = System.nanoTime();
		 Iterator<RealValuedVector> it = balancingSetWeights.iterator();
		Input input = new Input();
       List<WeightedPoint> wPoints = new ArrayList<WeightedPoint>(balancingSetWeights.size());
       int dim = 1;
		while(it.hasNext()) {
			RealValuedVector current = (RealValuedVector) it.next();
			  WeightedPoint wPoint = new WeightedPoint();
             wPoint.setPoint(new Point(current.getValue()));
             dim = current.getDimension();
             wPoint.setWeight(1);
             wPoints.add(wPoint);
		}
		 input.setDimension(dim);
        input.setPoints(wPoints);
        input.setMaxIterations(1000);
        input.setPermissibleError(Math.exp(-2));
        Point geometricMedian = process(input);
        if(geometricMedian == null) {
       	 System.err.println("geometric median returning null");
       	 return null;
        }
		Long diff = (System.nanoTime() - starttime);
		//System.out.println("****************** average vector" + Arrays.toString(((RealValuedVector)averagedModel).getValue()));
		//System.out.println("Average Mean Sync time: " + diff );
		RealValuedVector averagedModel = new RealValuedVector(geometricMedian.getValues());

		return (RealValuedVector) averagedModel;
	}

    
}