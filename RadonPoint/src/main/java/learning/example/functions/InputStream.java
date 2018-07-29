package learning.example.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import learning.example.Parameters;

/**
 * An example data generator spout for the learning algorithm, the learning model is classification
 * @author Rania
 *
 */
public abstract class InputStream {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4549999045442462256L;
	// the number of tuples that we are generating

	protected final Random random = new Random();
	protected String delim = ","; 
	protected int instancesCount = 0 ;
	protected BufferedReader br;
	public InputStream(){
		
	}
	public InputStream (String filename) {


		File inFile = new File(filename);
        try {
            	br = new BufferedReader(new FileReader(inFile));
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		String row = null;
		try {
			row = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		delim = row.contains(";") ? ";" : ","; 
		Parameters.DATA_LENGTH = row.split(delim).length-1;
		Parameters.numNodes = (int) Math.pow(Parameters.DATA_LENGTH+3, Parameters.height);
		Parameters.batchSize = Parameters.numNodes*Parameters.batchMult;
	}
	

	
	public abstract double [] nextExample() ;
}