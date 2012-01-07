
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
		for ( int j = j1; j <= j2 ; j++){
			this.matrix[rowind][j-j1] = this.row[j];
		}
	}
	
}
