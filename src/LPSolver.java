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
		lp = new LPReader("/home/karl/Desktop/afiro.lp", false);
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
		A = new Matrix(new FracBigInt[0][0]);
	}
	
	public void solve() {
		FracBigInt[] feasibleX = PhaseI();
		if(feasibleX == null) {
			System.out.println("Das Ausgangsproblem ist laut Phase I nicht lösbar...");
		}else {
			FracBigInt[] c = new FracBigInt[lp.noOfVariables()];
			for(int i = 0; i < c.length; i++) {
				c[i] = new FracBigInt(lp.objectiveVector()[i]);
			}
			FracBigInt[] optSolution = PhaseII();
			System.out.println(Arrays.toString(optSolution));
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
		A.set(0, 0, sum);
		sum = new FracBigInt("0");
		for(int j = 0; j < lp.noOfVariables() + noOfGE + noOfLE; j++) {
			for(int i = 0; i < m;i++) {
				if(j < lp.noOfVariables()) {
					a[i][j] = new FracBigInt(lp.constraint[i][j]);
					sum = sum.substract(a[i][j]);
				}else if( r[i -2] != 0 && r1 == 0) {
					a[i][j] = new FracBigInt(r[i -2]);
					r1 = 1;
					r[i -2] = 0;
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
		/*for(int i = 0; i < m; i++) {
			if(x[i] != FracBigInt.ZERO) {return null;}
		}*/
		return x;
	}
	
	public Matrix PhaseII(int[] B, int[] N) {
		int m = lp.noOfConstraints();
		
		Carry = (Matrix) A.of(0, m +1, 0, m +1);

		while(true) {
			//Calculate the reduced cost c_j, j in N, and save the index k
			int k = -1;
			for(int j = 0; j < N.length; j++) {
				//TODO korrekte implementierung dieses ausdrucks...
				FracBigInt c_j = A.get(0,N[j]).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, N[j], N[j])).get(0,0));
				if(c_j.compareTo(FracBigInt.ZERO) == -1) {
					k = j;
					break;
				}
			}
			if(k == -1) {
				return (Matrix) A.of(1, m, 0, 0);
			}
			Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, k, k));
			
		}
		
		return null;
		
	}
}
