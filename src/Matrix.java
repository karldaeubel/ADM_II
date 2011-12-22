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
		Matrix result = new Matrix(this.m,this.n);
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				result.matrix[i][j] = this.matrix[i][j].multiply(factor);
			}
		}
		return result;
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
				if ( this.matrix[i][j] != null ){
					min = this.matrix[i][j];
					break;
				}
			}
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0 ){
					min=this.matrix[i][j];
				}
			}
		}
		return min.clone();
	}

	@Override
	public int[] argmin() {
		int result[] = new int[2];
		result[0]=-1;
		result[1]=-1;
		FracBigInt min = this.matrix[0][0];
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( (this.matrix[i][j] != null)){
					min = this.matrix[i][j];
					result[0] = i;
					result[1] = j;
					break;
				}
			}
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0){
					min=this.matrix[i][j];
					result[0] = i;
					result[1] = j;
				}
			}
		}
		return result;
	}

	/**
	 * needs a Carry-Matrix
	 * computes one Gauss-Step for the vector x_s=x
	 * @param x the vector in the tableau with negative reduced cost coefficient multiplied with B^-1
	 * @return r the position of the basis vector that has changed
	 */
	@Override
	public int gaussStep(MatrixInterface x) {
		Matrix b = (Matrix) this.of(1,this.m-1,0,0);
		int r = (b.dividePW(x.of(1, x.getM()-1, 0, 0))).argmin()[0];
		r++;
		FracBigInt xrs = x.get(r,0).invert();
		Matrix rowr = (Matrix) this.of(r, r, 0, this.n-1);
		for ( int i=0 ; i<this.m ; i++ ){
			if ( i==r ){
				this.set(i,i,0,this.n-1,this.of(i,i,0,this.n-1).multiply(xrs));
			}
			else{
				this.set(i,i,0,this.n-1,this.of(i,i,0,this.n-1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
			}
		}
		return r;
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
	
	private Matrix dividePW(MatrixInterface matrix){
		if ( this.m != matrix.getM() || this.n != matrix.getN() ){
			throw new IllegalArgumentException("Matrizen passen nicht");
		}
		
		Matrix result = new Matrix(this.m,this.n);
		
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( matrix.get(i,j).compareTo(FracBigInt.ZERO)>0 ){
					result.matrix[i][j] = this.matrix[i][j].multiply(matrix.get(i,j).invert());
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
	
	@Override
	public MatrixInterface add(MatrixInterface matrix) {
		if ( this.m != matrix.getM() || this.n != matrix.getN() ){
			throw new IllegalArgumentException("Matrix hat falsche Grš§e");
		}
		Matrix result = new Matrix(this.m,this.n);
		for ( int i=0 ; i<this.m ; i++ ){
			for ( int j=0 ; j<this.n ; j++ ){
				result.matrix[i][j] = this.matrix[i][j].add(matrix.get(i, j));
			}
		}
		return result;
	}
	
	public static void main(String[] args){
		/*
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
		System.out.println(testmatrix.add(testmatrix2));
		*/
		FracBigInt[][] carryarray = {{FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ZERO},{new FracBigInt("10"),FracBigInt.ONE,FracBigInt.ZERO,FracBigInt.ZERO},{new FracBigInt("8"),FracBigInt.ZERO,FracBigInt.ONE,FracBigInt.ZERO},{new FracBigInt("24"),FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ONE}};
		Matrix carry = new Matrix(carryarray);
		FracBigInt[][] xarray = {{new FracBigInt("-5")},{new FracBigInt("1")},{new FracBigInt("2")},{new FracBigInt("4")}};
		Matrix x = new Matrix(xarray);
		System.out.println(carry);
		System.out.println(x);
		System.out.println(carry.gaussStep(x));
		System.out.println(carry);
		
		
	}

	
}
