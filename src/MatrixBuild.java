
public class MatrixBuild extends Thread {

	private FracBigInt[] row;
	private FracBigInt[][] matrix;
	private int rowind;
	private int j1;
	private int j2;
	
	public MatrixBuild( FracBigInt[] row, FracBigInt[][] matrix, int rowind, int j1, int j2){
		this.row = row;
		this.matrix=matrix;
		this.rowind = rowind;
		this.j1 = j1;
		this.j2 = j2;
	}
	
	public void run(){
		System.arraycopy(row,j1,matrix[rowind],0,j2-j1+1);
	}
	
}
