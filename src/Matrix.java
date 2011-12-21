import java.util.Arrays;


public class Matrix implements MatrixInterface {

	private FracBigInt[][] matrix;
	private int m;
	private int n;
	
	public Matrix(int m, int n){
		this.m = m;
		this.n = n;
		matrix = new FracBigInt[m][n];
	}
	
	public Matrix( FracBigInt[][] matrix ){
		if ( matrix==null ){ 
			throw new NullPointerException("leere Matrix"); 
		}
		this.m = matrix.length;
		this.n = matrix[0].length;
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
		if ( this.n != matrix.getM() ){
			throw new IllegalArgumentException("Matrizen haben inkompatible Groessen");
		}
		
		Matrix result = new Matrix(this.m , matrix.getN());
		
		for ( int i = 0 ; i<result.m ; i++ ){
			for ( int j = 0 ; j<result.n ; j++ ){
				FracBigInt sum = new FracBigInt();
				for ( int k=0 ; k<this.n ; k++ ){
					sum = sum.add(this.matrix[i][k].multiply(matrix.get(k,j)));
				}
				result.matrix[i][j] = sum.clone();
			}
		}
		
		
		return result;
	}

	@Override
	public MatrixInterface multiply(FracBigInt factor) {
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				this.matrix[i][j] = this.matrix[i][j].multiply(factor);
			}
		}
		return this.clone();
	}

	@Override
	public void set(int i1, int i2, int j1, int j2, MatrixInterface matrix) {
		if ( (i1>i2) || (j1>j2) || (i1<0) || (j1<0) || (i2>=this.matrix.length) || (j2>=this.matrix[0].length) ){
			throw new IllegalArgumentException("falsche Grenzen");
		}
		int m = i2-i1+1;
		int n = j2-j1+1;
		for ( int i=i1 ; i<=i2 ; i++ ){
			for ( int j=j1 ; j<=j2 ; j++ ){
				this.matrix[i][j] = matrix.get(i-i1,j-j1).clone();
			}
		}
	}

	@Override
	public FracBigInt min() {
		FracBigInt min = this.matrix[0][0];
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( this.matrix[i][j] != null && this.matrix[i][j].compareTo(FracBigInt.ZERO)>0){
					min = this.matrix[i][j];
					break;
				}
			}
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0 && matrix[i][j].compareTo(FracBigInt.ZERO)>0){
					min=this.matrix[i][j];
				}
			}
		}
		return min.clone();
	}

	@Override
	public int[] argmin() {
		int result[] = new int[2];
		FracBigInt min = this.matrix[0][0];
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( (this.matrix[i][j] != null) && (this.matrix[i][j].compareTo(FracBigInt.ZERO)>0)){
					min = this.matrix[i][j];
					break;
				}
			}
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0 && this.matrix[i][j].compareTo(FracBigInt.ZERO)>0){
					min=this.matrix[i][j];
					result[0] = i;
					result[1] = j;
				}
			}
		}
		return result;
	}

	@Override
	public void gaussStep(MatrixInterface x) {
		Matrix b = (Matrix) this.of(1,this.m,0,0);
		//TODO fertig implementieren

	}

	private Matrix multiplyPW(Matrix matrix){
		if ( this.m != matrix.getM() || this.n != matrix.getN() ){
			throw new IllegalArgumentException("Matrizen passen nicht");
		}
		
		Matrix result = new Matrix(this.m,this.n);
		
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				result.matrix[i][j] = this.matrix[i][j].multiply(matrix.get(i, j)).clone();
			}
		}
		return result;
	}
	
	private Matrix dividePW(Matrix matrix){
		if ( this.m != matrix.getM() || this.n != matrix.getN() ){
			throw new IllegalArgumentException("Matrizen passen nicht");
		}
		
		Matrix result = new Matrix(this.m,this.n);
		
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( matrix.get(i,j).compareTo(FracBigInt.ZERO)>0 ){
					result.matrix[i][j] = this.matrix[i][j].multiply(matrix.get(i,j).invert()).clone();
				}
			}
		}
		return result;
	}

	@Override
	public FracBigInt get(int i, int j) {
		return this.matrix[i][j].clone();
	}


	@Override
	public void set(int i, int j, FracBigInt value) {
		this.matrix[i][j] = value.clone();
	}


	@Override
	public int getM() {
		return this.m;
	}


	@Override
	public int getN() {
		return this.n;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < this.matrix.length; i++) {
			b.append(Arrays.toString(matrix[i]) + "\n");
		}
		return b.toString();
	}
	
	public Matrix clone(){
		return new Matrix(this.matrix);
	}
	
	
	public static void main(String[] args){
		FracBigInt[][] test = {{new FracBigInt("1"),new FracBigInt("2")},{new FracBigInt("3"),new FracBigInt("4")}};
		Matrix testmatrix = new Matrix(test);
		FracBigInt[][] test2 = {{new FracBigInt("1"),new FracBigInt("2")},{new FracBigInt("1"),new FracBigInt("2")}};
		Matrix testmatrix2 = new Matrix(test2);
		System.out.println(testmatrix);
		System.out.println(testmatrix2);
		System.out.println(testmatrix.multiply(testmatrix2));
		System.out.println(testmatrix.of(0,1,0,0));
		System.out.println(testmatrix.min());
		System.out.println(Arrays.toString(testmatrix.argmin()));
		System.out.println(testmatrix.multiplyPW(testmatrix2));
		System.out.println(testmatrix.dividePW(testmatrix2));
		System.out.println(new Matrix(2,2));
		testmatrix2.set(1,1,new FracBigInt("-2","1"));
		System.out.println(testmatrix.dividePW(testmatrix2));
		System.out.println(testmatrix.dividePW(testmatrix2).min());
	}
}
