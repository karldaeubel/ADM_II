import java.util.Arrays;


public class Matrix implements MatrixInterface {

	private FracBigInt[][] matrix;
	
	public Matrix( FracBigInt[][] matrix ){
		if ( matrix==null ){ 
			throw new NullPointerException("leere Matrix"); 
		}
		int m = matrix.length;
		int n = matrix[0].length;
		this.matrix = new FracBigInt[m][n];
		for ( int i=0 ; i<m ; i++ ){
			for ( int j=0 ; j<n ; j++ ){
				this.matrix[i][j] = matrix[i][j];
			}
		}
	}
	
	
	@Override
	public MatrixInterface of(int i1, int i2, int j1, int j2) {
		if ( (i1>i2) || (j1>j2) || (i1<0) || (j1<0) || (i2>=this.matrix.length) || (j2>=this.matrix[0].length) ){
			throw new IllegalArgumentException("falsche Grenzen");
		}
		int m = i2-i1+1;
		int n = j2-j1+1;
		FracBigInt[][] result = new FracBigInt[m][n];
		for ( int i=i1 ; i<=i2 ; i++ ){
			for ( int j=j1 ; j<=j2 ; j++ ){
				result[i-i1][j-j1] = matrix[i][j];
			}
		}
		return new Matrix(result);
	}

	@Override
	public MatrixInterface multiply(MatrixInterface matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatrixInterface multiply(FracBigInt factor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(int i1, int i2, int j1, int j2, MatrixInterface matrix) {
		// TODO Auto-generated method stub

	}

	@Override
	public FracBigInt min() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] argmin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gaussStep(MatrixInterface x, int row) {
		// TODO Auto-generated method stub

	}


	@Override
	public FracBigInt get(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void set(int i, int j, FracBigInt value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getM() {
		return this.matrix.length;
	}


	@Override
	public int getN() {
		return this.matrix[0].length;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < this.matrix.length; i++) {
			b.append(Arrays.toString(matrix[i]) + "\n");
		}
		return b.toString();
	}
}
