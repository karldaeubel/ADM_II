import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;


public class LPSolver {
	
	private LPReader lp;
	
	private Matrix A;
	
	private Matrix Carry;
	
	public LPSolver() {
		System.out.println("----------Start the Program----------");
		lp = new LPReader("/home/karl/Desktop/test.lp", false);
		try {	
				long start = System.nanoTime();
				lp.readLP();
				long stop = System.nanoTime();
				System.out.println("Reading Time: " + (stop - start)/1000000. + " ms");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void solve() {
		Matrix feasibleX = PhaseI();
		if(feasibleX == null) {
			System.out.println("Das Ausgangsproblem ist laut Phase I nicht lösbar...");
		}else {/*
			FracBigInt[] c = new FracBigInt[lp.noOfVariables()];
			for(int i = 0; i < c.length; i++) {
				c[i] = new FracBigInt(lp.objectiveVector()[i]);
			}
			Matrix optSolution = PhaseII();
			System.out.println(Arrays.toString(optSolution));
			*/
		}
	}

	private Matrix PhaseI() {
		int m = lp.noOfConstraints();
		int noOfGE = 0;
		int noOfLE = 0;
		int noOfEq = 0;
		int[] r = new int[m];
		for(int i = 0; i < m;i++) {
			if(lp.sense[i] == LPReader.SENSE_GEQ) {
				noOfGE++;
				r[i] = -1;
			} else if (lp.sense[i] == LPReader.SENSE_LEQ) {
				noOfLE++;
				r[i] = 1;
			}else if(lp.sense[i] == LPReader.SENSE_EQ) {
				noOfEq++;
			}
		}
		int n = m + lp.noOfVariables() + noOfGE + noOfLE;
		
		A = new Matrix(new FracBigInt[m +1][n +1]);
		int r1 = 0;
		FracBigInt sum = new FracBigInt("0");
		FracBigInt[][] unit = new FracBigInt[m][m];
		FracBigInt[][] b = new FracBigInt[m][1];
		FracBigInt[][] a = new FracBigInt[m][lp.noOfVariables() + noOfGE + noOfLE];
		for(int i = 0; i < m; i++) {
			for(int j = 0; j < m; j++) {
				if(i == j) {
					unit[i][j] = new FracBigInt("1"); 
				}else {
					unit[i][j] = new FracBigInt("0"); 
				}
			}
			A.set(0,i +1, new FracBigInt("0"));
			b[i][0] = new FracBigInt(lp.rhs[i]);
			sum = sum.substract(b[i][0]);
		}
		
		/*System.out.println(Arrays.deepToString(unit));
		Matrix I = new Matrix(unit);
		System.out.println(I);
		System.out.println(I.of(0, 1, 0, 1));
		*/
		A.set(0, 0, sum);
		sum = new FracBigInt("0");
		for(int j = 0; j < lp.noOfVariables() + noOfGE + noOfLE; j++) {
			for(int i = 0; i < m;i++) {
				a[i][j] = new FracBigInt("0");
				if(j < lp.noOfVariables()) {
					a[i][j] = new FracBigInt(lp.constraint[i][j]);
					sum = sum.substract(a[i][j]);
				}else if( r[j -2] != 0 && r1 == 0) {
					a[i][j] = new FracBigInt(r[j -2]);
					r1 = 1;
					r[j -2] = 0;
					sum = sum.substract(a[i][j]);
				}
			}
			r1 = 0;
			A.set(0,j + m +1,sum);
			sum = new FracBigInt("0");
		}
		
		A.set(1, m, 1, m, new Matrix(unit));
		A.set(0, 1, 1, m, new Matrix(b));
		A.set(1, m, 1 +m, n +1, new Matrix(a));
		
		System.out.println(A);
		int[] B = new int[m];
		int[] N = new int[n -m];
		for(int i = 0; i < m; i++) {
			B[i] = i;
		}
		for(int i = 0; i < n -m; i++) {
			N[i] = m +i;
		}
		
		
		Matrix x = PhaseII(B,N);
		if(Carry.get(0, 0).compareTo(FracBigInt.ZERO) == 1) {
			System.out.println("keine zulässige Lösung!");
			return null;
		}
		for(int i = 0; i < B.length; i++) {
			if(B[i] < m) {
				//TODO was passiert bei küstlichen var in der Basis??
				System.out.println("Hilfe... :D");
			}
		}
		
		
		for(int i = 0; i < m; i++) {
			B[i] = B[i] -m;
		}
		for(int i = 0 ; i < N.length; i++) {
			N[i] = N[i] -m;
		}
		Matrix b_ = (Matrix) A.of(0, m, 0, 0);
		Matrix A_ = (Matrix) A.of(0, m, m +1, A.getN());
		A = new Matrix(new FracBigInt[m +1][b_.getN() + A_.getM()]);
		A.set(0, m, 0, 0, b_);
		A.set(0, m, 1, A.getN(), A_);
		for(int i = 0; i < lp.noOfVariables() + noOfGE + noOfLE +1; i++) {
			A.set(0, i, new FracBigInt("0"));
			if(lp.objectiveSense() == LPReader.SENSE_MAX && i < lp.noOfVariables() && i > 0) {
				A.set(0, i, new FracBigInt(lp.objectiveVector()[i]*-1));
			}else if(lp.objectiveSense() == LPReader.SENSE_MIN && i < lp.noOfVariables() && i > 0) {
				A.set(0, i, new FracBigInt(lp.objectiveVector()[i]));
			}
		}
		FracBigInt[][] c = new FracBigInt[1][m];
		for(int i = 0; i < m; i++) {
			c[0][i] = new FracBigInt(lp.objectiveVector()[B[i]]);
		}
		Matrix C = (Matrix)(new Matrix(c)).multiply(new FracBigInt("-1"));
		Matrix pi = (Matrix) C.multiply(Carry.of(1, m, 1, m));
		Carry.set(0, 0, 1, m, pi);
		Carry.set(0, 0, 0, 0, C.multiply(Carry.of(1, m, 0, 0)));

		return PhaseII(B,N);
	}
	
	public Matrix PhaseII(int[] B, int[] N) {
		int m = lp.noOfConstraints();
		
		Carry = (Matrix) A.of(0, m, 0, m);

		while(true) {
			//Calculate the reduced cost c_j, j in N, and save the index k
			int k = -1;
			FracBigInt c_j = new FracBigInt("0");
			for(int j = 0; j < N.length; j++) {
				//TODO korrekte implementierung dieses ausdrucks...
				c_j = A.get(0,N[j]).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, N[j], N[j])).get(0,0));
				if(c_j.compareTo(FracBigInt.ZERO) == -1) {
					k = j;
					break;
				}
			}
			if(k == -1) {
				return (Matrix) A.of(1, m, 0, 0);
			}
			Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, k, k));
			int r = argmin((Matrix) Carry.of(1, m, 0, 0), x);
			if(r == -1) {System.out.println("Unbeschrenkt!");return null;}
			int temp = B[r];
			B[r] = N[k];
			N[k] = temp;
			FracBigInt[][] p = new FracBigInt[m +1][m +1];
			for(int i = 0; i < m +1; i++) {
				for(int j = 0; j < m +1; j++) {
					p[i][j] = new FracBigInt("0");
					if(j == r +1 && i == 0) {
						p[i][j] = c_j.divide(x.get(r, 0));
					}else if(j == r +1 && i != r +1) {
						p[i][j] = x.get(i -1, 0).divide(x.get(r, 0));
					}else if(j == r +1 && i == r +1) {
						p[i][j] = FracBigInt.ONE.divide(x.get(r,0));
					}else if(i == j) {
						p[i][j] = new FracBigInt("1");
					}
				}
			}
			Matrix P = new Matrix(p);
			Carry = (Matrix) Carry.multiply(P);
		}
	}
	
	private int argmin(Matrix b, Matrix x) {
		int result = -1;
		FracBigInt min = new FracBigInt("1000000");
		for(int i = 0; i < x.getN(); i++) {
			if(!x.get(i, 0).equals(FracBigInt.ZERO)) {
				if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
					min = b.get(i, 0).divide(x.get(i, 0));
					result = i;
				}
			}
		}
		return result;
	}
}