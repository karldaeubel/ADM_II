import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;


public class LPSolver {
	
	private LPReader lp;
	
	private Matrix A;
	
	private Matrix Carry;
	
	private int noOfArti;
	
	public LPSolver(String file) {
		System.out.println("----------Start the Program!----------");
		lp = new LPReader(file, false);
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
		Matrix x = PhaseI();
		if(x == null) {
			System.out.println("Das Ausgangsproblem ist laut Phase I nicht lösbar...");
		}else {
			System.out.println("Das LP hat folgede optimale Lösung:\n");
			FracBigInt sum = new FracBigInt("0");
			for(int i = 0; i < x.getN(); i++) {
				System.out.println("x_" + (i +1) + " = " + x.get(0, i));
				sum = sum.add(x.get(0, i).multiply(new FracBigInt(lp.objectiveVector()[i])));
			}
			System.out.println("\nZFW: " + sum);
		}
	}

	private Matrix PhaseI() {
		System.out.println("|----------PhaseI gestartet----------|");
		int m = lp.noOfConstraints();
		int noOfGE = 0;
		int noOfLE = 0;
		int noOfEq = 0;
		int[] r = new int[m];
		for(int i = 0; i < m;i++) {
			if(lp.rhs[i] < 0) {
				lp.rhs[i] = lp.rhs[i]*-1;
				lp.sense[i] *= -1;
				for(int j = 0; j < lp.noOfVariables(); j++) {
					lp.constraint[i][j] = lp.constraint[i][j]*-1;
				}
			}
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
		noOfArti = lp.noOfConstraints();
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
				}else if( r[i] != 0 && r1 == 0) {
					a[i][j] = new FracBigInt(r[i]);
					r1 = 1;
					r[i] = 0;
					sum = sum.substract(a[i][j]);
				}
			}
			r1 = 0;
			A.set(0,j + m +1,sum);
			sum = new FracBigInt("0");
		}
		
		A.set(1, m, 1, m, new Matrix(unit));
		A.set(1, m, 0, 0, new Matrix(b));
		A.set(1, m, 1 +m, n, new Matrix(a));
		
		//System.out.println(A);
		int[] B = new int[m];
		LinkedList<Integer> L = new LinkedList<Integer>();
		LinkedList<Integer> U = new LinkedList<Integer>();
		
		for(int i = 0; i < m; i++) {
			B[i] = i +1;
		}
		for(int i = 0; i < n -m; i++) {
			L.add(new Integer(m +i +1));
		}
		
		Carry = (Matrix) A.of(0, m, 0, m);

		Matrix x = PhaseII(B,L,U);
		if(Carry.get(0, 0).compareTo(FracBigInt.ZERO) != 0) {
			System.out.println("keine zulässige Lösung!");
			return null;
		}
		Matrix At = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, 0, A.getN() -1));
		for(int i = 0; i < B.length; i++) {
			if(B[i] <= m) {
				int t = -1;
				for(int j = m +1; j < A.getN() -1; j++) {
					if(At.get(i, j).compareTo(FracBigInt.ZERO) != 0) {
						gauss(At,i,j);
						t = j;
						break;
					}
				}
				if(t == -1) {
					System.out.println("Redundant!?!");
					//TODO zeile in at entfernen da redundant
				}
				for(int j = 0; j < L.size(); j++) {
					if(L.get(j) == t) {
						B[i] = t;
						L.remove(j);
						break;
					}
				}
				for(int j = 0; j < U.size(); j++) {
					if(U.get(j) == t) {
						B[i] = t;
						U.remove(j);
						break;
					}
				}
			}
		}
		
		noOfArti = 0;
		for(int i = 0; i < m; i++) {
			B[i] = B[i] -m;
		}
		
		for(int i = 0; i < L.size(); i++) {
			if(L.get(i) -m <= 0) {
				L.remove(i);
				i--;
			}else {
				L.set(i, L.get(i) -m);
			}
		}
		for(int i = 0; i < U.size(); i++) {
			if(U.get(i) -m <= 0) {
				U.remove(i);
				i--;
			}else {
				U.set(i, U.get(i) -m);
			}
		}

		Matrix b_ = (Matrix) A.of(0, m, 0, 0);
		Matrix A_ = (Matrix) A.of(0, m, m +1, A.getN() -1);
		A = new Matrix(new FracBigInt[m +1][b_.getN() + A_.getN()]);
		A.set(0, m, 0, 0, b_);
		A.set(0, m, 1, A.getN() -1, A_);
		for(int i = 0; i < lp.noOfVariables() + noOfGE + noOfLE +1; i++) {
			A.set(0, i, new FracBigInt("0"));
			if(lp.objectiveSense() == LPReader.SENSE_MAX && i <= lp.noOfVariables() && i > 0) {
				A.set(0, i, new FracBigInt(lp.objectiveVector()[i -1]*-1));
			}else if(lp.objectiveSense() == LPReader.SENSE_MIN && i <= lp.noOfVariables() && i > 0) {
				A.set(0, i, new FracBigInt(lp.objectiveVector()[i -1]));
			}
		}
		FracBigInt[][] c = new FracBigInt[1][m];
		for(int i = 0; i < m; i++) {
			c[0][i] = new FracBigInt("0");
			if(B[i] <= lp.noOfVariables()) {
				if(lp.objectiveSense() == LPReader.SENSE_MAX) {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[B[i] -1])).multiply(new FracBigInt("-1"));
				}else {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[B[i] -1]));
				}
			}
		}
		Matrix C = (Matrix)(new Matrix(c)).multiply(new FracBigInt("-1"));
		Matrix pi = (Matrix) C.multiply(Carry.of(1, m, 1, m));
		Carry.set(0, 0, 1, m, pi);
		Carry.set(0, 0, 0, 0, C.multiply(Carry.of(1, m, 0, 0)).multiply(new FracBigInt("-1")));
		
		System.out.println("|----------PhaseI beendet------------|");
		
		x = PhaseII(B,L,U);
		if(x == null) {return null;}
		FracBigInt[][] s = new FracBigInt[1][lp.noOfVariables()];
		for(int i = 0; i < L.size(); i++) {
			if(L.get(i) <= lp.noOfVariables()) {
				s[0][L.get(i) -1] = new FracBigInt("0");
			}
		}
		for(int i = 0; i < U.size(); i++) {
			if(U.get(i) <= lp.noOfVariables()) {
				s[0][U.get(i) -1] = new FracBigInt(lp.ubound[U.get(i) -1]);
			}
		}
		for(int i = 0; i < m; i++) {
			if(B[i] <= lp.noOfVariables()) {
				s[0][B[i] -1] = x.get(i,0);
			}
		}
		return new Matrix(s);
	}
	
	private void gauss(Matrix at, int r, int j) {
		FracBigInt xrs = at.get(r,j).invert();
		Matrix rowr = (Matrix) at.of(r, r, 0, at.getN()-1);
		for ( int i=0 ; i<at.getM() ; i++ ){
			if ( i==r ){
				at.set(i,i,0,at.getN()-1,at.of(i,i,0,at.getN()-1).multiply(xrs));
			}
			else{
				at.set(i,i,0,at.getN()-1,at.of(i,i,0,at.getN()-1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(at.get(i,j))))));
			}
		}
	}

	public Matrix PhaseII(int[] B, LinkedList<Integer> L, LinkedList<Integer> U) {
		System.out.println("|----------PhaseII gestartet---------|");
		int m = lp.noOfConstraints();
		

		while(true) {
			//Calculate the reduced cost c_j, j in N, and save the index k
			int k = -1;
			int l = -1;
			int u = -1;
			int v = -1;
			FracBigInt c_j = new FracBigInt("0");
			for(int j = 0; j < L.size(); j++) {
				c_j = A.get(0,L.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, L.get(j), L.get(j))).get(0,0));
				if(c_j.compareTo(FracBigInt.ZERO) == -1) {
					k = L.get(j);
					u = j;
					break;
				}
			}
			for(int j = 0; j < U.size(); j++) {
				c_j = A.get(0,U.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, U.get(j), U.get(j))).get(0,0));
				if(c_j.compareTo(FracBigInt.ZERO) == 1) {
					l = U.get(j);
					v = j;
					break;
				}
			}
						
			if(k == -1 && l == -1) {
				System.out.println("|----------PhaseII beendet-----------|");
				return (Matrix) Carry.of(1, m, 0, 0);
			}
			if(l == -1 || (k != -1 && l != -1 && L.get(u) < U.get(v))) {
				Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, k, k));
				Matrix y = new Matrix(new FracBigInt[m +1][1]);
				y.set(0, 0, A.get(0,k).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, k, k)).get(0,0)));
				y.set(1, m, 0, 0, x);
		//TODO korrekte implementierung in derMatrix klasse...
		//int r = Carry.gaussStep(y);
				int r = step(y,B, k);
				
				if(r == -1) {System.out.println("Unbeschrenkt!");return null;}
				if(r < m +1) {
					int temp = B[r -1];
					B[r -1] = L.remove(u);
					
					if(x.get(r -1, 0).compareTo(FracBigInt.ZERO) > 0) {
						L.add(u,temp);
					}else {
						U.add(temp);
					}
					
				}else {
					U.add(L.remove(u));
				}
			} else if(k == -1 || (k != -1 && l != -1 && L.get(u) > U.get(v))){
				Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, l, l));
				Matrix y = new Matrix(new FracBigInt[m +1][1]);
				y.set(0, 0, A.get(0,l).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, l, l)).get(0,0)));
				y.set(1, m, 0, 0, x);
		//TODO korrekte implementierung in derMatrix klasse...
		//int r = Carry.gaussStep(y);
				int r = step(y, B, l);
				if(r == -1) {System.out.println("Unbeschrenkt!");return null;}
				if(r < m +1) {
					int temp = B[r -1];
					B[r -1] = U.remove(v);
					if(x.get(r -1, 0).compareTo(FracBigInt.ZERO) > 0) {
						U.add(v,temp);
					}else {
						L.add(temp);
					}
					
				}else {
					L.add(U.remove(v));
				}
			}
			/*FracBigInt[][] p = new FracBigInt[m +1][m +1];
			for(int i = 0; i < m +1; i++) {
				for(int j = 0; j < m +1; j++) {
					p[i][j] = new FracBigInt("0");
					if(j == r && i == 0) {
						p[i][j] = c_j.divide(x.get(r -1, 0)).multiply(new FracBigInt("-1"));
					}else if(j == r && i != r) {
						p[i][j] = x.get(i -1, 0).divide(x.get(r -1, 0)).multiply(new FracBigInt("-1"));
					}else if(j == r && i == r) {
						p[i][j] = FracBigInt.ONE.divide(x.get(r -1,0));
					}else if(i == j) {
						p[i][j] = new FracBigInt("1");
					}
				}
			}
			Matrix P = new Matrix(p);
			Carry = (Matrix) P.multiply(Carry);
			*/
			System.out.println("z: " + Carry.get(0, 0).multiply(new FracBigInt("-1")));
		}
	}
	
	private int argmin(Matrix b, Matrix x) {
		int result = -1;
		FracBigInt min = new FracBigInt("1000000");
		for(int i = 0; i < x.getM(); i++) {
			if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
				if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
					min = b.get(i, 0).divide(x.get(i, 0));
					result = i +1;
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			LPSolver lp = new LPSolver("/home/karl/Desktop/afiro.lp");
			lp.solve();
		}else {
			for(int i = 0; i < args.length; i++) {
				System.out.println("Datei: " + args[i]);
				LPSolver s = new LPSolver(args[i]);
				s.solve();
			}
		}
	}
	
	public int step(Matrix x, int[] B, int k) {
		int result = -1;
		Matrix b = (Matrix) Carry.of(1, lp.noOfConstraints(), 0, 0);
		FracBigInt min = new FracBigInt("10000000");
		if(x.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(b.get(i -1, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i -1, 0).divide(x.get(i, 0));
						result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i -1, 0))).divide(x.get(i,0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i -1, 0))).divide(x.get(i,0));
							result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti -1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti -1])) > 0) {
						for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k -noOfArti -1]))));
						}
						return lp.noOfConstraints() +1;
					}

				}
			}
			if(result > 0) {
				FracBigInt xrs = x.get(result,0).invert();
				Matrix rowr = (Matrix) Carry.of(result, result, 0, Carry.getN() -1);
				for ( int i=0 ; i < Carry.getM() ; i++ ){
					if (i == result){
						Carry.set(i,i,0,Carry.getN() -1,Carry.of(i,i,0,Carry.getN()-1).multiply(xrs));
					}
					else{
						Carry.set(i,i,0, Carry.getN() -1, Carry.of(i,i,0,Carry.getN() -1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
					}
				}
			}
		}else {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(b.get(i -1, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i -1, 0).divide(x.get(i, 0));
						result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i -1, 0))).divide(x.get(i,0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i -1, 0))).divide(x.get(i,0));
							result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti -1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti -1])) > 0) {
						for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k -noOfArti -1])).multiply(new FracBigInt("-1"))));
						}
						return lp.noOfConstraints() +1;
					}

				}
			}
			if(result > 0) {
				FracBigInt xrs = x.get(result,0).invert();
				Matrix rowr = (Matrix) Carry.of(result, result, 0, Carry.getN() -1);
				for ( int i=0 ; i < Carry.getM() ; i++ ){
					if (i == result){
						Carry.set(i,i,0,Carry.getN() -1,Carry.of(i,i,0,Carry.getN()-1).multiply(xrs));
					}
					else{
						Carry.set(i,i,0, Carry.getN() -1, Carry.of(i,i,0,Carry.getN() -1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
					}
				}
			}
		}
		
		return result;
	}
}

	