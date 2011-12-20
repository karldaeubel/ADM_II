import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;


public class LPSolver {
	
	private LPReader lp;
	
	private FracBigInt[][] Tableau;
	
	private FracBigInt[][] Carry;
	
	public LPSolver() {
		System.out.println("----------Start the Programm----------");
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
		Tableau = new FracBigInt[0][0];
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
			FracBigInt[] optSolution = PhaseII(c , feasibleX);
			System.out.println(Arrays.toString(optSolution));
		}
	}

	private FracBigInt[] PhaseI() {
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
		System.out.println(noOfEq + "  " + noOfGE + "  " + noOfLE + "  " + lp.noOfConstraints() + "  " + lp.noOfVariables());
		Tableau = new FracBigInt[m +2][n +1];
		int r1 = 0;
		FracBigInt sum = new FracBigInt("0");
		for(int j = 0; j < n +1; j++) {
			for(int i = 0; i <  m +2; i++) {
				Tableau[i][j] = new FracBigInt("0");
				if(i == 0 && j >= m +1) {
					Tableau[i][j] = new FracBigInt("1");
				}else if(i == 1 && j > 0 && j < m +1) {
					Tableau[i][j] = new FracBigInt("1");
				}else if(i > 1 && j == 0) {
					Tableau[i][j] = new FracBigInt(lp.rhs[i -2]);
					sum = sum.substract(Tableau[i][j]);
				}else if(i > 1 && j > 0 && j < m +1 && i == j +1) {
					Tableau[i][j] = new FracBigInt("1");
					sum = sum.substract(Tableau[i][j]);
				}else if(i > 1 && j >= m +1 && j < m +1 +lp.noOfVariables()) {
					Tableau[i][j] = new FracBigInt(lp.constraint[i -2][j - m -1]);
					sum = sum.substract(Tableau[i][j]);
				}else if(i > 1 && j >= m +1 +lp.noOfVariables() && r[i -2] != 0 && r1 == 0) {
					Tableau[i][j] = new FracBigInt(r[i -2]);
					r1 = 1;
					r[i -2] = 0;
					sum = sum.substract(Tableau[i][j]);
				}
			}
			r1 = 0;
			Tableau[1][j] = Tableau[1][j].add(sum);
			sum = new FracBigInt("0");
		}
		System.out.println(Arrays.deepToString(Tableau));

		
		FracBigInt[] c = new FracBigInt[n];
		FracBigInt[] feasible = new FracBigInt[n];
		for(int i = 0; i < n; i++) {
			if(i < m) {
				c[i] = new FracBigInt("1");
				feasible[i] = new FracBigInt(lp.rhs[i]);
			}else {
				c[i] = new FracBigInt("0");
				feasible[i] = new FracBigInt("0");
			}
		}
		System.out.println(Arrays.toString(feasible) + "  " + Arrays.toString(c));
		FracBigInt[] x = PhaseII(c, feasible);
		for(int i = 0; i < m; i++) {
			if(x[i] != FracBigInt.ZERO) {return null;}
		}
		return x;
	}
	
	public FracBigInt[] PhaseII(FracBigInt[] c, FracBigInt[] x) {
		int m = lp.noOfConstraints();
		int n = Tableau.length -1;
		Carry = new FracBigInt[m +1][m +1];
		boolean[] B = new boolean[n];
		for(int i = 0; i < m; i++) {
			B[i] = true;
		}
		for(int i = 0; i < m; i++) {
			for(int j = 0; j < m; j++) {
				Carry[i][j] = Tableau[i][j].clone();
			}
		}
		FracBigInt cN = FracBigInt.ZERO;
		FracBigInt result = new FracBigInt("0");
		int k = 1;
		
		while(k < n +1) {
			if(B[k] == false) {
				for(int i = 0; i < m; i++) {
					result = result.add(Carry[0][i +1].add(Tableau[i +2][k]));
				}
				cN = Tableau[1][k].add(result);
			}else {
				k++;
			}
			if(cN.equals(FracBigInt.ZERO)) {
				k--;
				break;
			}
		}
		if(k == n +1) {return x;}
		
		
		return null;
		
	}
}
