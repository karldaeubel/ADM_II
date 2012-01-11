
public interface MatrixInterface {
		
	public MatrixInterface of(int i1, int i2, int j1, int j2);
	
	public MatrixInterface altMultiply(MatrixInterface matrix);
	
	public MatrixInterface multiply(FracBigInt factor);
	
	public void set(int i1, int i2, int j1, int j2, MatrixInterface matrix);
	
	public FracBigInt min();
	
	public int[] argmin();
	
	public int gaussStep(MatrixInterface x);
	
	public void set(int i, int j, FracBigInt value);
	
	public FracBigInt get(int i, int j);
	
	public String toString();
	
	public int getM();
	
	public int getN();
	
	public MatrixInterface add( MatrixInterface matrix );
}