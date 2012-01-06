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
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		} catch (ParseException e1) {
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		} catch (IOException e1) {
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		}
	}
	
	public void solve() {
		long start = System.nanoTime();
		Matrix x = PhaseI();
		long stop = System.nanoTime();
		if(x == null) {
			System.out.println("Das Ausgangsproblem ist laut Phase I nicht lösbar...");
		}else {
			System.out.println("Das LP hat folgede optimale Lösung:\n");
			FracBigInt sum = new FracBigInt("0");
			for(int i = 0; i < x.getN(); i++) {
				System.out.println("x_" + (i +1) + " = " + x.get(0, i).toDouble());
				sum = sum.add(x.get(0, i).multiply(new FracBigInt(lp.objectiveVector()[i])));
			}
			System.out.println("\nZFW: " + sum.toDouble());
		}
		System.out.println("Das Lösen des Problems hat " + (stop - start)/1000000. + " ms gedauert!");
	}

	private Matrix PhaseI() {
		System.out.println("|----------PhaseI gestartet----------|");
		int m = lp.noOfConstraints();
		int noOfGE = 0;
		int noOfLE = 0;
		int noOfEq = 0;
		int[] r = new int[m];

		double[] au = new double[lp.noOfConstraints()];
		double sumw = 0;
		for(int i = 0; i < m; i++) {
			sumw = 0;
			for(int j = 0; j < lp.noOfVariables(); j++) {
				sumw += lp.constraint[i][j]*lp.lbound[j];
			}
			au[i] = sumw;
		}
		for(int i = 0; i < m; i++) {
			lp.rhs[i] = lp.rhs[i] - au[i];
		}
		for(int i = 0; i < lp.noOfVariables(); i++) {
			lp.ubound[i] = lp.ubound[i] - lp.lbound[i];
		}
		
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
		
		noOfArti = m -noOfLE;
		int n = m + lp.noOfVariables() + noOfGE;
		int[] B = new int[m];
		LinkedList<Integer> L = new LinkedList<Integer>();
		
		Carry = new Matrix(m +1, m +1);
		
		FracBigInt[][] zero = new FracBigInt[m +1][n +1];
		for(int i = 0; i < m +1; i++) {
			for(int j = 0; j < n +1; j++) {
				zero[i][j] = new FracBigInt("0");
			}
		}
		
		FracBigInt sum = FracBigInt.ZERO;
		int count = 0;
		for(int i = 0; i < m; i++) {
			zero[i +1][0] = new FracBigInt(lp.rhs[i]);
			if(lp.sense[i] != LPReader.SENSE_LEQ) {
				B[i] = count +1;
				zero[i +1][count +1] = FracBigInt.ONE;
				count++;
				sum = sum.substract(zero[i +1][0]);
			}
		}
		zero[0][0] = sum;
		sum = new FracBigInt("0");
		for(int j = noOfArti +1; j < noOfArti +1 +lp.noOfVariables(); j++) {
			for(int i = 1; i < m +1; i++) {
				zero[i][j] = new FracBigInt(lp.constraint[i -1][j -noOfArti -1]);
				if(lp.sense[i -1] != LPReader.SENSE_LEQ) {
					sum = sum.substract(zero[i][j]);
				}
			}
			L.add(new Integer(j));
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}

		count = 0;
		sum = new FracBigInt("0");
		for(int j = noOfArti +1 + lp.noOfVariables(); j < n +1; j++) {
			for(int i = 1 + count; i < m +1; i++) {
				if( r[i -1] != 0) {
					zero[i][j] = new FracBigInt(r[i -1]);
					count = i;
					if(r[i -1] == 1) {
						B[i -1] = j;
					}else {
						L.add(new Integer(j));
						sum = sum.substract(zero[i][j]);
					}
					break;
				}
			}
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}
		
		A = new Matrix(zero);
		
		Carry.set(0, m, 0, 0, A.of(0, m, 0, 0));
		for(int i = 0; i < m; i++) {
			Carry.set(0, m, i +1, i +1, A.of(0, m, B[i], B[i]));
		}
		//System.out.println(A);
		LinkedList<Integer> U = new LinkedList<Integer>();
		
		long start = System.nanoTime();
		Matrix x = PhaseII(B,L,U);
		long stop = System.nanoTime();
		System.out.println("Zeit: " + (stop -start));
		if(Carry.get(0, 0).compareTo(FracBigInt.ZERO) != 0) {
			System.out.println("keine zulässige Lösung!");
			return null;
		}
		Matrix At = null;
		for(int i = 0; i < B.length; i++) {
			if(B[i] <= noOfArti) {
				System.out.println("test");
				At = (Matrix) Carry.of(i +1, i +1, 1, m).multiply(A.of(1, m, 0, A.getN() -1));
				int t = -1;
				for(int j = noOfArti +1; j < At.getN(); j++) {
					if(At.get(0, j).compareTo(FracBigInt.ZERO) != 0) {
						gauss((Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, j, j)),i +1);
						t = j;
						break;
					}
				}
				if(t == -1) {
					System.out.println("Redundant!?!");
					
					//TODO zeile in at entfernen da redundant
				}
				boolean mh = false;
				for(int j = 0; j < L.size(); j++) {
					if(L.get(j) == t) {
						mh = true;
						B[i] = t;
						L.remove(j);
						break;
					}
				}
				if(!mh) {
				for(int j = 0; j < U.size(); j++) {
					if(U.get(j) == t) {
						B[i] = t;
						U.remove(j);
						break;
					}
				}
				}
			}
		}
		
		if(noOfArti != 0) {
		for(int i = 0; i < m; i++) {
			B[i] = B[i] -noOfArti;
		}
		
		for(int i = 0; i < L.size(); i++) {
			if(L.get(i) -noOfArti <= 0) {
				L.remove(i);
				i--;
			}else {
				L.set(i, L.get(i) -noOfArti);
			}
		}
		for(int i = 0; i < U.size(); i++) {
			if(U.get(i) -noOfArti <= 0) {
				U.remove(i);
				i--;
			}else {
				U.set(i, U.get(i) -noOfArti);
			}
		}
		}
		
		Matrix b_ = (Matrix) A.of(0, m, 0, 0);
		A = (Matrix) A.of(0, m, noOfArti, A.getN() -1);
		A.set(0, m, 0, 0, b_);
		noOfArti = 0;
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
		Carry.set(0, 0, 0, 0, C.multiply(Carry.of(1, m, 0, 0)));
		
		System.out.println("|----------PhaseI beendet------------|");
		
		x = PhaseII(B,L,U);
		if(x == null) {return null;}
		FracBigInt[][] s = new FracBigInt[1][lp.noOfVariables()];
		for(int i = 0; i < L.size(); i++) {
			if(L.get(i) <= lp.noOfVariables()) {
				s[0][L.get(i) -1] = new FracBigInt(lp.lbound[L.get(i) -1]);
			}
		}
		for(int i = 0; i < U.size(); i++) {
			if(U.get(i) <= lp.noOfVariables()) {
				s[0][U.get(i) -1] = new FracBigInt(lp.ubound[U.get(i) -1]).add(new FracBigInt(lp.lbound[U.get(i) -1]));
			}
		}
		for(int i = 0; i < m; i++) {
			if(B[i] <= lp.noOfVariables()) {
				s[0][B[i] -1] = x.get(i,0).add(new FracBigInt(lp.lbound[B[i] -1]));
			}
		}
		return new Matrix(s);
	}
	
	private void gauss(Matrix at, int r) {
		FracBigInt xrs = at.get(r -1,0).invert();
		Matrix rowr = (Matrix) Carry.of(r, r, 0, Carry.getN()-1);

		for ( int i=1 ; i<Carry.getM() ; i++ ){
			if ( i==r ){
				Carry.set(i,i,0,Carry.getN()-1,Carry.of(i,i,0,Carry.getN()-1).multiply(xrs));
			}
			else{
				Carry.set(i,i,0,Carry.getN()-1,Carry.of(i,i,0,Carry.getN()-1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(at.get(i -1, 0))))));
			}
		}
	}

	public Matrix PhaseII(int[] B, LinkedList<Integer> L, LinkedList<Integer> U) {
		System.out.println("|----------PhaseII gestartet---------|");
		int m = lp.noOfConstraints();
		
		int count = 0;
		LinkedList<Integer> index = new LinkedList<Integer>();
		LinkedList<FracBigInt> MIN = new LinkedList<FracBigInt>();
		LinkedList<Integer> LUindex = new LinkedList<Integer>();
		LinkedList<Integer> LORU = new LinkedList<Integer>();
		int rb = (A.getN() -m)/2;
		if(rb == 0) {rb = 1;}
		FracBigInt c_j = new FracBigInt("0");
		while(true) {
			if(MIN.size() == 0) {
				System.out.println("neue berechnung!!!!");
				c_j = FracBigInt.ZERO;
				for(int j = 0; j < L.size(); j++) {
					c_j = A.get(0,L.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, L.get(j), L.get(j))).get(0,0));
					
					//TODO entfernen?
					if(c_j.compareTo(FracBigInt.ZERO) < 0) {
						
					Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, L.get(j), L.get(j) ));
					Matrix y = new Matrix(new FracBigInt[m +1][1]);
					y.set(0, 0, A.get(0,L.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, L.get(j), L.get(j))).get(0,0)));
					y.set(1, m, 0, 0, x);
					FracBigInt mo = stepI(y, B, j);
					c_j = mo.multiply(c_j);
					if(c_j.compareTo(FracBigInt.ZERO) < 0) {
						c_j = c_j.multiply(new FracBigInt("-1"));
					}
					
					if(MIN.size() < rb) {
						if(MIN.size() == 0) {
							MIN.add(c_j);
							index.add(L.get(j));
							LUindex.add(j);
							LORU.add(0);
						}
						for(int i = 0; i < MIN.size(); i++) {
							if(c_j.compareTo(MIN.get(i)) > 0) {
								MIN.add(i,c_j);
								index.add(i,L.get(j));
								LUindex.add(i,j);
								LORU.add(i,0);
								break;
							}
						}
					}else {
						for(int i = 0; i < MIN.size(); i++) {
							if(c_j.compareTo(MIN.get(i)) > 0) {
								MIN.remove(i);
								index.remove(i);
								LUindex.remove(i);
								LORU.remove(i);
								
								MIN.add(i,c_j);
								index.add(i,L.get(j));
								LUindex.add(i,j);
								LORU.add(i,0);
								break;
							}
						}
					}
					}
				}
				//min_L = min_L.multiply(new FracBigInt("-1"));
				for(int j = 0; j < U.size(); j++) {
					c_j = A.get(0,U.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, U.get(j), U.get(j))).get(0,0));
					
					if(c_j.compareTo(FracBigInt.ZERO) > 0) {
						
						Matrix x = (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, U.get(j), U.get(j)));
						Matrix y = new Matrix(new FracBigInt[m +1][1]);
						y.set(0, 0, A.get(0,U.get(j)).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, U.get(j), U.get(j))).get(0,0)));
						y.set(1, m, 0, 0, x);
						FracBigInt mo = stepI(y, B, j);
						c_j = mo.multiply(c_j);
						if(c_j.compareTo(FracBigInt.ZERO) < 0) {
							c_j = c_j.multiply(new FracBigInt("-1"));
						}
					
					if(MIN.size() < rb) {
						if(MIN.size() == 0) {
							MIN.add(c_j);
							index.add(L.get(j));
							LUindex.add(j);
							LORU.add(1);
						}
						for(int i = 0; i < MIN.size(); i++) {
							if(c_j.compareTo(MIN.get(i)) > 0) {
								MIN.add(i,c_j);
								index.add(i,L.get(j));
								LUindex.add(i,j);
								LORU.add(i,1);
								break;
							}
						}
					}else {
						for(int i = 0; i < MIN.size(); i++) {
							if(c_j.compareTo(MIN.get(i)) > 0) {
								MIN.remove(i);
								index.remove(i);
								LUindex.remove(i);
								LORU.remove(i);
								
								MIN.add(i,c_j);
								index.add(i,L.get(j));
								LUindex.add(i,j);
								LORU.add(1);
								break;
							}
						}
					}
				}
			}
			}

			count++;
			//Calculate the reduced cost c_j, j in N, and save the index k
			if(MIN.size() == 0) {
				System.out.println("|----------PhaseII beendet-----------| Count: " + count);
				return (Matrix) Carry.of(1, m, 0, 0);
			}
					
			/*if(k == -1 && l == -1) {
				System.out.println("|----------PhaseII beendet-----------| Count: " + count);
				return (Matrix) Carry.of(1, m, 0, 0);
			}
			*/
			
			c_j = A.get(0,index.getFirst()).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, index.getFirst(), index.getFirst())).get(0,0));
			if((LORU.getFirst() == 0 && c_j.compareTo(FracBigInt.ZERO) < 0) || (LORU.getFirst() == 1 && c_j.compareTo(FracBigInt.ZERO) > 0)) {
			Matrix y = new Matrix(new FracBigInt[m +1][1]);
			y.set(0, 0, A.get(0,index.getFirst()).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, index.getFirst(), index.getFirst())).get(0,0)));
			y.set(1, m, 0, 0, (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, index.getFirst(), index.getFirst())));

			int r = step(y,B, index.getFirst());
				
			if(r == -1) {System.out.println("Unbeschrenkt!");return null;}
			if(y.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
				if(r < m +1) {
					int temp = B[r -1];
					B[r -1] = L.remove((int) LUindex.getFirst());
					
					if(y.get(r, 0).compareTo(FracBigInt.ZERO) > 0) {
						L.add(LUindex.getFirst(),temp);
					}else {
						U.add(temp);
					}
					
				}else {
					U.add(L.remove((int)LUindex.getFirst()));
				}
			}else {
				if(r < m +1) {
					int temp = B[r -1];
					B[r -1] = U.remove((int)LUindex.getFirst());
					
					if(y.get(r, 0).compareTo(FracBigInt.ZERO) > 0) {
						U.add(LUindex.getFirst(),temp);
					}else {
						L.add(temp);
					}
					
				}else {
					L.add(U.remove((int)LUindex.getFirst()));
				}
			}
			}
			MIN.removeFirst();
			index.removeFirst();
			LUindex.removeFirst();
			LORU.removeFirst();
			System.out.println("z: " + Carry.get(0, 0).multiply(new FracBigInt("-1")));
			if(Carry.get(0, 0).multiply(new FracBigInt("-1")).compareTo(new FracBigInt(6561.71213907)) == 0) {
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			LPSolver lp = new LPSolver("/home/karl/Desktop/boeing2.lp");
			lp.solve();
		}else {
			for(int i = 0; i < args.length; i++) {
				System.out.println("Datei: " + args[i]);
				LPSolver s = new LPSolver(args[i]);
				s.solve();
			}
		}
	}
	
	public FracBigInt stepI(Matrix x, int[] B, int k) {
		Matrix b = (Matrix) Carry.of(0, lp.noOfConstraints(), 0, 0);
		FracBigInt min = new FracBigInt("10000000");
		if(x.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0));
						//result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0).multiply(new FracBigInt("-1")));
							//result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti -1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti -1])) > 0) {
						return new FracBigInt(lp.ubound[k - noOfArti -1]);
					}
						/*for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k -noOfArti -1]))));
						}
						return lp.noOfConstraints() +1;
						*/
				}

			}
			return min;
		}else {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1")));
						//result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0));
							//result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti -1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti -1])) > 0) {
						return new FracBigInt(lp.ubound[k - noOfArti -1]);
						/*for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k -noOfArti -1])).multiply(new FracBigInt("-1"))));
						}
						return lp.noOfConstraints() +1;
						*/
					}

				}
			}
		}
		return min;
	}
	
	public int step(Matrix x, int[] B, int k) {
		int result = -1;
		Matrix b = (Matrix) Carry.of(0, lp.noOfConstraints(), 0, 0);
		FracBigInt min = new FracBigInt("10000000");
		if(x.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0));
						result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0).multiply(new FracBigInt("-1")));
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
				Matrix rowr = (Matrix) Carry.of(result, result, 1, Carry.getN() -1);
				for ( int i=0 ; i < Carry.getM() ; i++ ){
					if (i == result){
						Carry.set(i, 0, min);
						Carry.set(i,i,1,Carry.getN() -1,Carry.of(i,i,1,Carry.getN()-1).multiply(xrs));
					}
					else{
						Carry.set(i, 0, Carry.get(i, 0).substract(min.multiply(x.get(i, 0))));
						Carry.set(i,i,1, Carry.getN() -1, Carry.of(i,i,1,Carry.getN() -1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
					}
				}
			}
		}else {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1")));
						result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(B[i -1] > noOfArti && B[i -1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i -1] -noOfArti -1])) {
						if(((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i -1] -noOfArti -1])).substract(b.get(i, 0))).divide(x.get(i,0));
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
				Matrix rowr = (Matrix) Carry.of(result, result, 1, Carry.getN() -1);
				for ( int i=0 ; i < Carry.getM() ; i++ ){
					if (i == result){
						System.out.println(lp.ubound[k -1 - noOfArti]);
						Carry.set(i, 0, new FracBigInt(lp.ubound[k -1 - noOfArti]).substract(min));
						Carry.set(i,i,1,Carry.getN() -1,Carry.of(i,i,1,Carry.getN()-1).multiply(xrs));
					}
					else{
						Carry.set(i, 0, Carry.get(i, 0).add(min.multiply(x.get(i, 0))));
						Carry.set(i,i,1, Carry.getN() -1, Carry.of(i,i,1,Carry.getN() -1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i,0))))));
					}
				}
			}
		}
		
		return result;
	}
}

	