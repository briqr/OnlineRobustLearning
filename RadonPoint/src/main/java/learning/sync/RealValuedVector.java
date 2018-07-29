package learning.sync;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class RealValuedVector implements Comparable<RealValuedVector>, Iterable<Entry> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1674807216027066652L;
	private double[] vector;
	public static double epsilon = 0.000001;
	
	public RealValuedVector(int dim) {
		this.vector = new double[dim];
	}

	
	public RealValuedVector(Double[] initVec) {
		this.vector = new double[initVec.length];
		for (int i = 0; i < initVec.length; i++) {
			this.vector[i] = initVec[i];
		}
	}
	
	public RealValuedVector(double[] initVec) {
		this.vector = new double[initVec.length];
		for (int i = 0; i < initVec.length; i++) {
			this.vector[i] = initVec[i];
		}
	}
	
	public RealValuedVector(Boolean[] initVec) {
		this.vector = new double[initVec.length];
		for (int i = 0; i < initVec.length; i++) {
			this.vector[i] = initVec[i] ? 1 : 0;
		}
	}


	
	public void add(RealValuedVector other) {
		for (int i = 0; i < this.getDimension(); i++) {
			this.vector[i] += other.get(i);
		}
	}

	
	public void subtract(RealValuedVector other) {
		for (int i = 0; i < this.getDimension(); i++) {
			this.vector[i] -= other.get(i);
		}
	}

	
	
	public void skalarMultiply(double d) {
		for (int i = 0; i < this.getDimension(); i++) {
			this.vector[i] *= d;
		}
	}

	
	/**
	 * calculates the Euclidean distance.
	 */
	public double distance(RealValuedVector other) {
		double dist = 0.0;
		for (int i = 0; i < this.getDimension(); i++) {
			double diff = this.vector[i] - other.get(i);
			dist += diff*diff;
		}
		return Math.sqrt(dist);
	}
	
	public double distance(double[] other) {
		return Math.sqrt(distanceSquare(other));
	}
	
	public double distanceSquare(double[] other) {
		double distSquare = 0.0;
		for (int i = 0; i < this.getDimension(); i++) {
			double diff = this.vector[i] - other[i];
			distSquare += diff*diff;
		}
		return distSquare;
	}
	
	/**
	 * calculates the dot product
	 */
	public double innerProduct(RealValuedVector other) {
		double value = 0.0;
		for (int i = 0; i < this.getDimension(); i++) {
			value+= this.vector[i] * other.get(i);
		}
		return value;
	}
	
	public double innerProduct(double[] other) {
		double value = 0.0;
		for (int i = 0; i < this.getDimension(); i++) {
			value+= this.vector[i] * other[i];
		}
		return value;
	}
	
	

	
	public RealValuedVector copy() {
		RealValuedVector v = new RealValuedVector(this.getDimension());
		for (int i = 0; i < this.getDimension(); i++) {
			v.set(i, this.vector[i]);
		}
		return v;
	}

	
	public Double get(Integer idx) {
		return this.vector[idx];
	}

	
	public void set(Integer idx, Double val) {		
		this.vector[idx] = val;
	}


	
	public void initializeToZeroVector() {
		for (int i = 0; i < this.getDimension(); i++) {
			this.vector[i] = 0;
		}
	}

	
	public void initialise(RealValuedVector v) {
		vector = new double[v.getDimension()];
		for (int i = 0; i < this.getDimension(); i++) {
			this.vector[i] = v.get(i);
		}
	}

	
	public int getDimension() {
		return vector.length;
	}
	
	public String toString() {
		return Arrays.toString(vector);
	}


	public Iterable<Integer> getKeys() {
		return new AbstractList<Integer>(){
			@Override
			public Integer get(int i) {
				return i;
			}

			@Override
			public int size() {
				return vector.length;
			}
		};
	}

	public boolean containsIndex(Integer index) {
		if(index < vector.length)
			return true;
		return false;
	}
	
	// the truncation operator will take care of unifying equal vectors or vectors that are not very far off.
	@Override
	public boolean equals(Object other) {
		if(other instanceof RealValuedVector) {
			RealValuedVector otherRvv = (RealValuedVector) other;
			return otherRvv.equals(vector);
		}
		return false;
	}

	public boolean equals(double[] otherVector) {
		return Arrays.equals(vector, otherVector); 
	}
	
	
	public int hashCode() {
		return Arrays.hashCode(vector);
	}



	//@Override
	public int compareTo(RealValuedVector o) {
		return o.equals(this) ? 0 : 1;
	}


	//@Override
	public Iterator<Entry> iterator() {
	
		return null;
	}


	
	public RealValuedVector average(Collection<RealValuedVector> balancingSetWeights) {
		RealValuedVector averagedModel = new RealValuedVector(vector.length);
		averagedModel.initializeToZeroVector();
		averagedModel.add(balancingSetWeights);
		averagedModel.skalarMultiply(1.0/balancingSetWeights.size());
		return averagedModel;
	}
	
	public void add(Collection<RealValuedVector> vlist) {
		for (RealValuedVector currentVec : vlist) {
			this.add(currentVec);
		}
	}
	

	public double[] getValue() {
		return vector;
	}

	

}
