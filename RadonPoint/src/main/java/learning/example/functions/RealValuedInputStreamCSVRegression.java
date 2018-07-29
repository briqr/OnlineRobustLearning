package learning.example.functions;

import learning.example.Parameters;
import learning.example.functions.Hypothesis;
import learning.example.functions.InputStream;

public class RealValuedInputStreamCSVRegression extends InputStream {

	private int numFeatures = Parameters.DATA_LENGTH;
	private double scale = 2100.0;

	
	
	public RealValuedInputStreamCSVRegression (String filename, double scale) {
		super(filename);
		this.scale = scale;
	}
	
	@Override
	public double[] nextExample() {
		
		double[] currentSample = new double[numFeatures + 1];
		String row;
		try {
			if ((row = br.readLine()) != null) {
				String[] features = row.split(",");
				int i = 0;
				boolean isFirst = true;
				for (String feature : features) {
					if(isFirst) {
						currentSample[numFeatures] = Double.parseDouble(feature)/scale;
						isFirst = false;
					}
					else {
						currentSample[i++] = (Double.parseDouble(feature))/scale;
					}
				}
			}
			else {
				return null;
			}
	}
		catch (Exception e) {
			e.printStackTrace();
		}
	return currentSample;
	}

}
