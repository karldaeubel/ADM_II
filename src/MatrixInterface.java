
public interface MatrixInterface {
		
	public MatrixInterface of(int i1, int i2, int j1, int j2);
	
	public MatrixInterface multiply(MatrixInterface matrix);
	
	public MatrixInterface multiply(FracBigInt factor);
	
	public void set(int i1, int i2, int j1, int j2, MatrixInterface matrix);
	
	public FracBigInt min();
	
	public int[] argmin();
	
	public void gaussStep(MatrixInterface x, int row);
	
}