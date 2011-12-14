
public interface MatrixInterface {
		
	public MatrixInterface of(int i1, int i2, int j1, int j2);
	
	public MatrixInterface getL();
	
	public MatrixInterface getR();
	
	public void generateLR();
	
	public MatrixInterface Multiply(MatrixInterface matrix);
}