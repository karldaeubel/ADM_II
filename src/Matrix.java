import java.util.Arrays;
import java.util.Random;


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
		for ( int i = 0 ; i < this.m ; i++){
			this.matrix[i] = matrix[i].clone();
		}
	}
	
	/**
	 * constructs a random matrix
	 * @param r
	 * @param m
	 * @param n
	 */
	public Matrix(int r, int m, int n){
		matrix = new FracBigInt[m][n];
		this.m = m;
		this.n = n;
		Random rand = new Random();
		for ( int i = 0; i<m ; i++ ){
			for ( int j = 0 ; j<n ; j++ ){
				matrix[i][j] = new FracBigInt(rand.nextInt(10));
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
			System.arraycopy(matrix[i], j1, result[i-i1], 0, n);
		}
		return new Matrix(result);
	}
	
	/**
	 * bringt nur was bei gro�en Matrizen
	 * @param i1
	 * @param i2
	 * @param j1
	 * @param j2
	 * @return
	 */
	public Matrix get(int i1, int i2, int j1, int j2){
		if ( (i1>i2) || (j1>j2) || (i1<0) || (j1<0) || (i2>=this.matrix.length) || (j2>=this.matrix[0].length) ){
			throw new IllegalArgumentException("falsche Grenzen");
		}
		int m = i2-i1+1;
		int n = j2-j1+1;
		FracBigInt[][] result = new FracBigInt[m][n];
		for ( int i=i1 ; i<=i2 ; i=i+4 ){
			MatrixBuild mb = new MatrixBuild(this.matrix[i],result,i-i1,j1,j2);
			mb.start();
			MatrixBuild mb2 = null;
			MatrixBuild mb3 = null;
			MatrixBuild mb4 = null;
			if ( i+1 <= i2 ){
				mb2 = new MatrixBuild(this.matrix[i+1],result,i+1-i1,j1,j2);
				mb2.start();
				if ( i+2 <= i2 ){
					mb3 = new MatrixBuild(this.matrix[i+2],result,i+2-i1,j1,j2);
					mb3.start();
					if ( i+3 <= i2 ){
						mb4 = new MatrixBuild(this.matrix[i+3],result,i+3-i1,j1,j2);
						mb4.start();
					}
				}
			}
			try {
				mb.join();
				if ( mb2 != null ){
					mb2.join();
					if ( mb3 != null ){
						mb3.join();
						if ( mb4 != null ){
							mb4.join();
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Matrix(result);
	}

	@Override
	public MatrixInterface altMultiply(MatrixInterface matrix) {
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
				if ( min == null ){
					if ( (this.matrix[i][j] != null)){
						min = this.matrix[i][j];
					}
				}
				else{
					if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0){
						min=this.matrix[i][j];
					}
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
		if ( min != null ){
			result[0] = 0;
			result[1] = 0;
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( min == null ){
					if ( (this.matrix[i][j] != null)){
						min = this.matrix[i][j];
						result[0] = i;
						result[1] = j;
					}
				}
				else{
					if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0){
						min=this.matrix[i][j];
						result[0] = i;
						result[1] = j;
					}
				}
			}
		}
		return result;
	}

	public int[] argmin2() {
		int result[] = new int[2];
		result[0]=-1;
		result[1]=-1;
		FracBigInt min = this.matrix[0][0];
		if ( min != null ){
			result[0] = 0;
			result[1] = 0;
		}
		for ( int i = 0 ; i<this.m ; i++ ){
			for ( int j = 0 ; j<this.n ; j++ ){
				if ( min == null ){
					if ( (this.matrix[i][j] != null)){
						min = this.matrix[i][j];
						result[0] = i;
						result[1] = j;
					}
				}
				else{
					if( this.matrix[i][j] != null && this.matrix[i][j].compareTo(min) < 0){
						min=this.matrix[i][j];
						result[0] = i;
						result[1] = j;
					}
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
		//TODO eventuell entfernen
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
		//TODO eventuell entfernen
		
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
	
	/**
	 * A method to generate vector consisting of all theta_i
	 * @param ubound
	 * @param b
	 * @param x
	 * @return theta the theta vector
	 */
	private static Matrix generateTheta( int[] B, double[] ubound, MatrixInterface b , MatrixInterface x){
		int cf = (int) Math.signum(x.get(0,0).toDouble());			//to correct the terms if x_s is in L
		Matrix theta = new Matrix(b.getM(),1);
		for ( int i=0 ; i<b.getM(); i++ ){
			if ( x.get(i+1, 0).toDouble() != 0){
				if ( x.get(i+1, 0).toDouble()*cf < 0 ){
					theta.set(i, 0, (new FracBigInt(-cf)).multiply(b.get(i,0).divide(x.get(i+1, 0))));
				}
				else{
					if ( B[i]<ubound.length){
						theta.set(i, 0, ((new FracBigInt(ubound[B[i]]).substract(b.get(i, 0))).divide((new FracBigInt(cf)).multiply(x.get(i+1,0)))));
					}
					else{
						theta.set(i, 0, new FracBigInt(Double.POSITIVE_INFINITY));
					}
				}
			}
		}
		return theta;
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
			throw new IllegalArgumentException("Matrix hat falsche Gr��e");
		}
		Matrix result = new Matrix(this.m,this.n);
		for ( int i=0 ; i<this.m ; i++ ){
			for ( int j=0 ; j<this.n ; j++ ){
				result.matrix[i][j] = this.matrix[i][j].add(matrix.get(i, j));
			}
		}
		return result;
	}
	
	public void remove(int i, int j){
		Matrix ul = null;
		Matrix ur = null;
		Matrix ll = null;
		Matrix lr = null;
		
		if ( i > 0 && j > 0 ){
			ul = (Matrix) this.of(0, i-1, 0, j-1);
		}
		if ( i > 0 && j+1 < this.n){
			ur = (Matrix) this.of(0, i-1, j+1, this.n-1);
		}
		if ( i+1 < this.m && j > 0 ){
			ll = (Matrix) this.of(i+1, this.m-1, 0, j-1);
		}
		if ( i+1 < this.m && j+1 < this.n ){
			lr = (Matrix) this.of(i+1, this.m-1, j+1, this.n-1);
		}
		
		this.matrix = new FracBigInt[this.m-1][this.n-1];
		this.m--;
		this.n--;
		if ( ul != null ){
			this.set(0, i-1, 0, j-1, ul);
		}
		if ( ur != null ){
			this.set(0, i-1, j, this.n-1, ur);
		}
		if ( ll != null ){
			this.set(i, this.m-1 , 0, j-1, ll);
		}
		if ( lr != null ){
			this.set(i, this.m-1, j, this.n-1, lr);
		}
	}
	
	/**
	 * computes one pivot step
	 * @param x the column of A multiplied with B^-1
	 * @param ubound the vector of upper bounds
	 * @return r the position of the basis vector that has changed (begins with 0) or -1 if no basis vector has changed
	 */
	public int step(int[] B, Matrix x, double[] ubound, int noOfArt){
		double[] uboundnew = new double[ubound.length+noOfArt];
		System.arraycopy(ubound,0,uboundnew,noOfArt,ubound.length);
		for ( int i = 0; i<noOfArt ; i++ ){
			uboundnew[i] = Double.POSITIVE_INFINITY;
		}
		ubound = uboundnew;
		Matrix b = (Matrix) this.of(1,this.m-1,0,0);
		Matrix theta = Matrix.generateTheta(B,ubound, b, x);
		int r = theta.argmin()[0];
		if ( theta.get(r, 0).toDouble() > ubound[r] ){
			return -1;
		}
		r++;
		FracBigInt xrs = x.get(r,0).invert();
		Matrix rowr = (Matrix) this.of(r, r, 0, this.n-1);
		for ( int i=0 ; i<this.m ; i++ ){
			if ( i==r ){
				this.set(i,(Matrix) this.of(i,i,0,this.n-1).multiply(xrs));
				if ( x.get(0, 0).toDouble() > 0 ){
					this.set(i, 0, (new FracBigInt(ubound[r-1]).substract(theta.get(r-1,0))));
				}
				else{
					this.set(i, 0, theta.get(r-1, 0));
				}
			}
			else{
				this.set(i,(Matrix) this.of(i,i,0,this.n-1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
			}
		}
		return r-1;
	}
	
	/**
	 * computes one pivot step, uses multiple cores
	 * @param x the column of A multiplied with B^-1
	 * @param ubound the vector of upper bounds
	 * @return r the position of the basis vector that has changed (begins with 0) or -1 if no basis vector has changed
	 */
	public int stepAlt(int[] B, Matrix x, double[] ubound, int noOfArt){
		double[] uboundnew = new double[ubound.length+noOfArt];
		System.arraycopy(ubound,0,uboundnew,noOfArt,ubound.length);
		for ( int i = 0; i<noOfArt ; i++ ){
			uboundnew[i] = Double.POSITIVE_INFINITY;
		}
		ubound = uboundnew;
		Matrix b = (Matrix) this.of(1,this.m-1,0,0);
		Matrix theta = Matrix.generateTheta(B,ubound, b, x);
		int r = theta.argmin()[0];
		if ( theta.get(r, 0).toDouble() > ubound[r] ){
			return -1;
		}
		r++;
		FracBigInt xrs = x.get(r,0).invert();
		Matrix rowr = (Matrix) this.of(r, r, 0, this.n-1);
		for ( int i=0 ; i<this.m ; i=i+4 ){
			MatrixStep ms = null;
			MatrixStep ms2 = null;
			MatrixStep ms3 = null;
			MatrixStep ms4 = null;
			if ( i==r ){
				this.set(i,i,0,this.n-1,this.of(i,i,0,this.n-1).multiply(xrs));
				if ( x.get(0, 0).toDouble() > 0 ){
					this.set(i, 0, (new FracBigInt(ubound[r-1]).substract(theta.get(r-1,0))));
				}
				else{
					this.set(i, 0, theta.get(r-1, 0));
				}
			}
			else{
				ms = new MatrixStep(this, B , x , i, rowr, xrs);
				ms.start();
			}
			if ( i+1 < this.m ){	
				if ( i+1==r ){
					this.set(i+1,i+1,0,this.n-1,this.of(i+1,i+1,0,this.n-1).multiply(xrs));
					if ( x.get(0, 0).toDouble() > 0 ){
						this.set(i+1, 0, (new FracBigInt(ubound[r-1]).substract(theta.get(r-1,0))));
					}
					else{
						this.set(i+1, 0, theta.get(r-1, 0));
					}
				}
				else{
					ms2 = new MatrixStep(this, B , x , i+1, rowr, xrs);
					ms2.start();
				}
				if ( i+2 < this.m ){
					if ( i+2==r ){
						this.set(i+2,i+2,0,this.n-1,this.of(i+2,i+2,0,this.n-1).multiply(xrs));
						if ( x.get(0, 0).toDouble() > 0 ){
							this.set(i+2, 0, (new FracBigInt(ubound[r-1]).substract(theta.get(r-1,0))));
						}
						else{
							this.set(i+2, 0, theta.get(r-1, 0));
						}
					}
					else{
						ms3 = new MatrixStep(this, B , x , i+2, rowr, xrs);
						ms3.start();
					}
					if ( i+3 < this.m ){
						if ( i+3==r ){
							this.set(i+3,i+3,0,this.n-1,this.of(i+3,i+3,0,this.n-1).multiply(xrs));
							if ( x.get(0, 0).toDouble() > 0 ){
								this.set(i+3, 0, (new FracBigInt(ubound[r-1]).substract(theta.get(r-1,0))));
							}
							else{
								this.set(i+3, 0, theta.get(r-1, 0));
							}
						}
						else{
							ms4 = new MatrixStep(this, B , x , i+3, rowr, xrs);
							ms4.start();
						}
					}
				}
			}
			
			
			try {
				if( ms != null ){
					ms.join();
				}
				if( ms2 != null ){
					ms2.join();
				}
				if( ms3 != null ){
					ms3.join();
				}
				if( ms4 != null ){
					ms4.join();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		}
		return r-1;
	}
	
	public double[][] toDouble(){
		double[][] result = new double[this.m][this.n];
		for ( int i = 0 ; i < this.m ; i++){
			for( int j = 0; j < this.n ; j++){
				result[i][j] = this.get(i, j).toDouble();
			}
		}
		return result;
	}
	
	public Matrix multiply(Matrix A){
		Matrix result = new Matrix(this.m , A.getN());
		for ( int i = 0 ; i < this.m ; i=i+10){
			MatrixMult mult1 = new MatrixMult(A,i,(Matrix) this.get(i),result);
			mult1.start();
			MatrixMult mult2 = null;
			MatrixMult mult3 = null;
			MatrixMult mult4 = null;
			MatrixMult mult5 = null;
			MatrixMult mult6 = null;
			MatrixMult mult7 = null;
			MatrixMult mult8 = null;
			MatrixMult mult9 = null;
			MatrixMult mult10 = null;
			if ( i+1 < this.m ){
				mult2 = new MatrixMult(A,i+1,(Matrix) this.get(i+1),result);
				mult2.start();
				if ( i+2 < this.m ){
					mult3 = new MatrixMult(A,i+2,(Matrix) this.get(i+2),result);
					mult3.start();
					if ( i+3 < this.m ){
						mult4 = new MatrixMult(A,i+3,(Matrix) this.get(i+3),result);
						mult4.start();
						if ( i+4 < this.m ){
							mult5 = new MatrixMult(A,i+4,(Matrix) this.get(i+4),result);
							mult5.start();
							if ( i+5 < this.m ){
								mult6 = new MatrixMult(A,i+5,(Matrix) this.get(i+5),result);
								mult6.start();
								if ( i+6 < this.m ){
									mult7 = new MatrixMult(A,i+6,(Matrix) this.get(i+6),result);
									mult7.start();
									if ( i+7 < this.m ){
										mult8 = new MatrixMult(A,i+7,(Matrix) this.get(i+7),result);
										mult8.start();
										if ( i+8 < this.m ){
											mult9 = new MatrixMult(A,i+8,(Matrix) this.get(i+8),result);
											mult9.start();
											if ( i+9 < this.m ){
												mult10 = new MatrixMult(A,i+9,(Matrix) this.get(i+9),result);
												mult10.start();
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			try {
				mult1.join();
				if ( mult2 != null ){
					mult2.join();
					if ( mult3 != null ){
						mult3.join();
						if ( mult4 != null ){
							mult4.join();
							if ( mult5 != null ){
								mult5.join();
								if ( mult6 != null ){
									mult6.join();
									if ( mult7 != null ){
										mult7.join();
										if ( mult8 != null ){
											mult8.join();
											if ( mult9 != null ){
												mult9.join();
												if ( mult10 != null ){
													mult10.join();
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * returns the row i
	 * @param i
	 * @return
	 */
	public Matrix get(int i){
		FracBigInt[][] result = new FracBigInt[1][this.n];
		result[0] = this.matrix[i].clone();
		return new Matrix(result);
	}
	
	/**
	 * sets row i
	 * @param i
	 */
	public void set(int i , Matrix row){
		if ( row.getN() != this.n){
			throw new IllegalArgumentException("ungleiche L�nge");
		}
		this.matrix[i] = row.matrix[0].clone();
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
		System.out.println(testmatrix.get(0,1,0,0));
		System.out.println(testmatrix.min());
		System.out.println(Arrays.toString(testmatrix.argmin()));
		System.out.println(testmatrix.multiplyPW(testmatrix2));
		System.out.println(testmatrix.dividePW(testmatrix2));
		System.out.println(new Matrix(2,2));
		testmatrix2.set(1,1,new FracBigInt("-2","1"));
		System.out.println(testmatrix.dividePW(testmatrix2));
		System.out.println(testmatrix.dividePW(testmatrix2).min());
		System.out.println(testmatrix.add(testmatrix2));
		testmatrix.remove(1, 0);
		System.out.println(testmatrix);
		*/
		
		/*
		long time1;
		long time2;
		Matrix test = new Matrix(1,100,100);
		System.out.println(test);
		time1=System.nanoTime();
		System.out.println(test.of(0, 99, 0, 1));
		time2 = System.nanoTime();
		System.out.println("normal: "+(time2-time1));
		time1=System.nanoTime();
		System.out.println(test.get(0, 99, 0, 1));
		time2 = System.nanoTime();
		System.out.println("thread: "+(time2-time1));
		*/
		
		/*
		FracBigInt[][] carryarray = {{FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ZERO},{new FracBigInt("10"),FracBigInt.ONE,FracBigInt.ZERO,FracBigInt.ZERO},{new FracBigInt("8"),FracBigInt.ZERO,FracBigInt.ONE,FracBigInt.ZERO},{new FracBigInt("24"),FracBigInt.ZERO,FracBigInt.ZERO,FracBigInt.ONE}};
		
		Matrix carry = new Matrix(carryarray);
		FracBigInt[][] xarray = {{new FracBigInt("-5")},{new FracBigInt("1")},{new FracBigInt("2")},{new FracBigInt("4")}};
		Matrix x = new Matrix(xarray);
		double[] bounds = {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
		int[] B = {2,3,4};
		long time1;
		long time2;
		
		System.out.println(carry);
		System.out.println(x);
		time1=System.nanoTime();
		System.out.println(carry.stepAlt(B,x,bounds));
		time2=System.nanoTime();
		System.out.println(carry);
		System.out.println("zeit_alt: " + (time2-time1));
		
		
		carry = new Matrix(carryarray);
		System.out.println(carry);
		time1=System.nanoTime();
		System.out.println(carry.stepAlt(B,x,bounds));
		time2=System.nanoTime();
		System.out.println(carry);
		System.out.println("zeit_neu: " + (time2-time1));
		*/
		
		
		long time1;
		long time2;
		Matrix test1 = new Matrix(1,10,10);
		Matrix test2 = new Matrix(1,10,1);
		Matrix test3 = null;
		System.out.println(test1);
		System.out.println(test2);
		time1=System.nanoTime();
		System.out.println(test1.altMultiply(test2));
		//test3 = (Matrix) test1.multiply(test2);
		time2 = System.nanoTime();
		System.out.println("normal: "+(time2-time1));
		time1 = System.nanoTime();
		System.out.println(test1.multiply(test2));
		//test3 = test1.altMultiply(test2);
		time2 = System.nanoTime();
		System.out.println("thread: "+(time2-time1));
		
	}

	
}
