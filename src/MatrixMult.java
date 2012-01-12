
public class MatrixMult extends Thread {
	Matrix matrix;
	int rowInd;
	Matrix row;
	Matrix result;
	
	public MatrixMult(Matrix matrix, int row, Matrix col, Matrix result){
		this.matrix = matrix;
		this.rowInd = row;
		this.row = col;
		this.result = result;
	}
	
	
	public void run(){
		for ( int j = 0; j<matrix.getN() ; j++){
			result.set(rowInd, j, this.row.altMultiply(this.matrix.of(0,this.matrix.getM()-1,j,j)).get(0, 0));
		}
	}
}
