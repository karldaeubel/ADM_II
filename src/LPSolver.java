import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;

public class LPSolver {
	
	private LPReader lp;
	
	private Matrix A;
	
	private Matrix Carry;
	
	private int noOfArti;
	
	private boolean debug;
	private boolean output;
	
	public static void main(String[] args) {
		System.out.println("|---------------Willkommen---------------|\n" +
				   "|----------------------------------------|\n" +
				   "|----------------------------------------|\n" +
				   "|--------------LP Solver von-------------|\n" +
				   "|----------------------------------------|\n" +
				   "|-------------Marcel und Karl------------|\n" +
				   "|----------------------------------------|\n\n");
		
		if(args.length == 0 || args[0].toLowerCase().contains("-h")) {
			System.out.println("Eingabe:\n\njava LPSolver [Optionen] file\n\nOptionen:\n" +
					"\n  '-d'     Debugmodus (Ausgabe von aktuellem ZFW etc.)" +
					"\n  '-x'     Ausgabe der Variablen" +
					"\n  '-r'     Erweiterte ausgabe des LPReaders" +
					"\n\nBeispiel:\n\n  java LPSolver -d -x /home/karl/Desktop/kb2.lp\n");
			/*LPSolver lp = new LPSolver("/home/karl/Desktop/kb2.lp",false);
			lp.solve();
			*/
		}else {
			boolean out = false;
			boolean deb = false;
			boolean rd = false;
			for(int i = 0; i < args.length; i++) {
				if(args[i].toLowerCase().equals("-d") || args[i].toLowerCase().equals("debug")) {
					deb = true;
				}else if(args[i].toLowerCase().equals("-x") || args[i].toLowerCase().equals("var")) {
					out = true;
				}else if(args[i].toLowerCase().equals("-r") || args[i].toLowerCase().equals("read")) {
					rd = true;
				}else {
					System.out.println("Datei: " + args[i] + "\n");
					LPSolver s = new LPSolver(args[i], deb, out, rd);
					s.solve();
				}
			}
		}
	}
	
	public LPSolver() {
		System.out.println("\nBitte geben Sie eine Datei an!\n\nBei hilfe 'java LPSolver -h' eingeben");
	}
	
	public LPSolver(String file) {
		this (file, false, false, false);
	}
	
	public LPSolver(String file, boolean boo, boolean var, boolean rd) {
		System.out.println("|---------Start the Program!---------|");
		debug = boo;
		output = var;
		lp = new LPReader(file, rd);
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
			if(output) {System.out.println("\nDas LP hat folgede optimale Lösung:\n");}
			FracBigInt sum = new FracBigInt("0");
			for(int i = 0; i < x.getN(); i++) {
				if(output) {System.out.println(lp.variableName(i) + " = " + x.get(0, i).toDouble());}
				sum = sum.add(x.get(0, i).multiply(new FracBigInt(lp.objectiveVector()[i])));
			}
			System.out.println("\nZFW: " + sum.toDouble());
		}
		System.out.println("\nDas Lösen des Problems hat " + (stop - start)/1000000. + " ms gedauert!\n");
	}

	private Matrix PhaseI() {
		System.out.println("|----------PhaseI gestartet----------|");
		int m = lp.noOfConstraints();
		int noOfGE = 0;
		int noOfLE = 0;
		int noOfUb = 0;
		int[] r = new int[m];
		LinkedList<Integer> ub = new LinkedList<Integer>();

		double[] au = new double[lp.noOfConstraints()];
		double sumw = 0;
		for(int i = 0; i < lp.noOfVariables(); i++) {
			if(Double.isInfinite(lp.lbound[i])) {
				noOfUb++;
				ub.add(i +1);
			}
		}
		for(int i = 0; i < m; i++) {
			sumw = 0;
			for(int j = 0; j < lp.noOfVariables(); j++) {
				if(!Double.isInfinite(lp.lbound[j])) {
					sumw += lp.constraint[i][j]*lp.lbound[j];
				}
			}
			au[i] = sumw;
		}
		for(int i = 0; i < m; i++) {
			lp.rhs[i] = lp.rhs[i] - au[i];
		}
		for(int i = 0; i < lp.noOfVariables(); i++) {
			if(!Double.isInfinite(lp.lbound[i])) {
				lp.ubound[i] = lp.ubound[i] - lp.lbound[i];
			}
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
			}
		}
		
		noOfArti = m -noOfLE;
		int n = m + lp.noOfVariables() + noOfGE + noOfUb;
		int[] B = new int[m];
		NonBasis[] non = new NonBasis[lp.noOfVariables() + noOfGE + noOfUb];
		int countNon = 0;
		
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
			non[countNon++] = new NonBasis(j, NonBasis.L);
			//L.add(new Integer(j));
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}

		count = 0;
		sum = new FracBigInt("0");
		for(int j = noOfUb + noOfArti +1 + lp.noOfVariables(); j < n +1; j++) {
			for(int i = 1 + count; i < m +1; i++) {
				if( r[i -1] != 0) {
					zero[i][j] = new FracBigInt(r[i -1]);
					count = i;
					if(r[i -1] == 1) {
						B[i -1] = j;
					}else {
						non[countNon++] = new NonBasis(j, NonBasis.L);
						//L.add(new Integer(j));
						sum = sum.substract(zero[i][j]);
					}
					break;
				}
			}
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}
		
		A = new Matrix(zero);
		count = 0;
		for(int i = 0; i < ub.size(); i++) {
			non[countNon++] = new NonBasis(lp.noOfVariables() +noOfArti +i +1, NonBasis.L);
			A.set(0, m, noOfArti +1 +lp.noOfVariables() +i, noOfArti +1 +lp.noOfVariables() +i, A.get(0, m, ub.get(i) + noOfArti, ub.get(i) + noOfArti).multiply(new FracBigInt("-1")));
		}
		
		Carry.set(0, m, 0, 0, A.of(0, m, 0, 0));
		for(int i = 0; i < m; i++) {
			Carry.set(0, m, i +1, i +1, A.of(0, m, B[i], B[i]));
		}
		//System.out.println(A);
		//LinkedList<Integer> U = new LinkedList<Integer>();
		
		long start = System.nanoTime();
		Matrix x = PhaseII(B,non);
		long stop = System.nanoTime();
		if(debug) {System.out.println("Zeit: " + (stop -start));}
		if(Carry.get(0, 0).compareTo(FracBigInt.ZERO) != 0) {
			System.out.println("keine zulässige Lösung!");
			return null;
		}

		Matrix At = null;
		for(int i = 0; i < B.length; i++) {
			if(B[i] <= noOfArti) {
				At = (Matrix) Carry.of(i +1, i +1, 1, m).multiply(A.of(1, m, 0, A.getN() -1));
				int t = -1;
				
				for(int j = noOfArti +1; j < At.getN(); j++) {
					if(At.get(0, j).compareTo(FracBigInt.ZERO) != 0) {
						for(int l = 0; l < non.length; l++) {
							if(non[l].index == j && non[l].LorU == NonBasis.L) {
								gauss((Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, j, j)),i +1);
								non[l].index = B[i];
								non[l].LorU = NonBasis.L;
								B[i] = j;
								
								t = j;
								break;
							}
						}
						if(t != -1) {break;}
					}
				}
				if(t == -1) {
					System.out.println("Redundant!?!");
					double[][] constr = new double[lp.noOfConstraints() -1][lp.noOfVariables()];
					double[] rightH = new double[lp.noOfConstraints() -1];
					int[] sen = new int[lp.noOfConstraints() -1];
					count = 0;
					for(int j = 0; j < lp.noOfConstraints(); j++) {
						if(j != i) {
							rightH[count] = lp.rhs[j];
							sen[count] = lp.sense[j];
							for(int k = 0; k < lp.noOfVariables(); k++) {
								constr[count][k] = lp.constraint[j][k];
							}
							count++;
						}
					}
					lp.rhs = rightH;
					lp.sense = sen;
					lp.constraint = constr;
					return PhaseI();
				}
			}
		}
		
		
		if(noOfArti != 0) {
			for(int i = 0; i < m; i++) {
				B[i] = B[i] -noOfArti;
			}
		}

		NonBasis[] NON = new NonBasis[lp.noOfVariables() + noOfGE + noOfLE + noOfUb - lp.noOfConstraints()];
		countNon = 0;
		for(int i = 0; i < non.length; i++) {
			if(non[i].index -noOfArti > 0) {
				NON[countNon++] = new NonBasis(non[i].index - noOfArti, non[i].LorU);
			}
		}

		Matrix b_ = (Matrix) A.of(0, m, 0, 0);
		A = (Matrix) A.of(0, m, noOfArti, A.getN() -1);
		A.set(0, m, 0, 0, b_);
		noOfArti = 0;
		for(int i = 0; i < lp.noOfVariables() + noOfUb + noOfGE + noOfLE +1; i++) {
			A.set(0, i, new FracBigInt("0"));
			if(lp.objectiveSense() == LPReader.SENSE_MAX) {
				if(i <= lp.noOfVariables() && i > 0) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[i -1]*-1));
				}else if(i > lp.noOfVariables() && i <= lp.noOfVariables() + noOfUb) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[ub.get(i -lp.noOfVariables() -1)]));
				}
			}else if(lp.objectiveSense() == LPReader.SENSE_MIN) {
				if(i <= lp.noOfVariables() && i > 0) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[i -1]));
				}else if(i > lp.noOfVariables() && i <= lp.noOfVariables() + noOfUb) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[ub.get(i -lp.noOfVariables() -1) -1] *-1));
				}
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
			}else if(B[i] > lp.noOfVariables() && B[i] <= lp.noOfVariables() + noOfUb) {
				if(lp.objectiveSense() == LPReader.SENSE_MAX) {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[ub.get(B[i] -lp.noOfVariables() -1)]));
				}else {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[ub.get(B[i] -lp.noOfVariables() -1)])).multiply(new FracBigInt("-1"));
				}
			}
		}
		
		Matrix C = (Matrix)(new Matrix(c)).multiply(new FracBigInt("-1"));
		Matrix pi = (Matrix) C.multiply(Carry.of(1, m, 1, m));
		Carry.set(0, 0, 1, m, pi);
		Carry.set(0, 0, 0, 0, C.multiply(Carry.of(1, m, 0, 0)));
		
		System.out.println("|----------PhaseI beendet------------|");
		
		x = PhaseII(B,NON);

		if(x == null) {return null;}
		FracBigInt[][] s = new FracBigInt[1][lp.noOfVariables()];
		for(int i = 0; i < NON.length; i++) {
			if(NON[i].index <= lp.noOfVariables()) {
				if(NON[i].LorU == NonBasis.L) {
					s[0][NON[i].index -1] = new FracBigInt(lp.lbound[NON[i].index -1]);
				}else {
					s[0][NON[i].index -1] = new FracBigInt(lp.ubound[NON[i].index -1]).add(new FracBigInt(lp.lbound[NON[i].index -1]));
				}
			}
		}

		for(int i = 0; i < m; i++) {
			if(B[i] <= lp.noOfVariables()) {
				if(!Double.isInfinite(lp.lbound[B[i] -1])) {
					s[0][B[i] -1] = x.get(i,0).add(new FracBigInt(lp.lbound[B[i] -1]));
				}else {
					s[0][B[i] -1] = x.get(i, 0);
				}
			}else if(B[i] > lp.noOfVariables() && B[i] <= lp.noOfVariables() +noOfUb) {
				s[0][ub.get(NON[i].index -lp.noOfVariables() -1)] =  s[0][ub.get(NON[i].index -lp.noOfVariables() -1)].substract(x.get(i, 0));
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

	public Matrix PhaseII(int[] B, NonBasis[] non) {
		System.out.println("|----------PhaseII gestartet---------|");
		int m = lp.noOfConstraints();
		
		int count = 0;
		LinkedList<Integer> index = new LinkedList<Integer>();
		LinkedList<FracBigInt> MIN = new LinkedList<FracBigInt>();
		LinkedList<Integer> LUindex = new LinkedList<Integer>();
		int rb = (A.getN() -m)/3;
		if(rb == 0) {rb = 1;}
		FracBigInt c_j = new FracBigInt("0");
		while(true) {
			if(MIN.size() == 0) {
				//System.out.println("neue berechnung!!!!");
				c_j = FracBigInt.ZERO;
				for(int j = 0; j < non.length; j++) {
					c_j = A.get(0,non[j].index).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, non[j].index, non[j].index)).get(0,0));
					
					if((non[j].LorU == NonBasis.L && c_j.compareTo(FracBigInt.ZERO) < 0) || (non[j].LorU == NonBasis.U && c_j.compareTo(FracBigInt.ZERO) > 0)) {

						Matrix y = new Matrix(new FracBigInt[m +1][1]);
						y.set(0, 0, c_j);
						y.set(1, m, 0, 0, Carry.of(1, m, 1, m).multiply(A.of(1, m, non[j].index, non[j].index)));
						FracBigInt mo = stepI(y, B, j);
						if(mo.compareTo(FracBigInt.ZERO) != 0) {
							c_j = mo.multiply(c_j);
						}
						if(c_j.compareTo(FracBigInt.ZERO) < 0) {
							c_j = c_j.multiply(new FracBigInt("-1"));
						}
					
						if(MIN.size() < rb) {
							if(MIN.size() == 0) {
								MIN.add(c_j);
								index.add(non[j].index);
								LUindex.add(j);
							}else if(MIN.getLast().compareTo(c_j) >= 0) {
								MIN.add(c_j);
								index.add(non[j].index);
								LUindex.add(j);
							}else {
								for(int i = 0; i < MIN.size(); i++) {
									if(c_j.compareTo(MIN.get(i)) > 0) {
										MIN.add(i,c_j);
										index.add(i,non[j].index);
										LUindex.add(i,j);
										break;
									}
								}
							}
						}else {
							for(int i = 0; i < MIN.size(); i++) {
								if(c_j.compareTo(MIN.get(i)) > 0) {
									MIN.set(i, c_j);
									index.set(i, non[j].index);
									LUindex.set(i, j);
									break;
								}
							}
						}
					}
				}
			}
			
			if(MIN.size() == 0) {
				System.out.println("|----------PhaseII beendet-----------| Count: " + count);
				return (Matrix) Carry.of(1, m, 0, 0);
			}
			
			c_j = A.get(0,index.getFirst()).add(Carry.of(0, 0, 1, m).multiply(A.of(1, m, index.getFirst(), index.getFirst())).get(0,0));
			if((non[LUindex.getFirst()].LorU  == NonBasis.L && c_j.compareTo(FracBigInt.ZERO) < 0) || (non[LUindex.getFirst()].LorU  == NonBasis.U && c_j.compareTo(FracBigInt.ZERO) > 0)) {
				
				Matrix y = new Matrix(new FracBigInt[m +1][1]);
				y.set(0, 0, c_j);
				y.set(1, m, 0, 0, (Matrix) Carry.of(1, m, 1, m).multiply(A.of(1, m, index.getFirst(), index.getFirst())));
				
				int r = step(y,B, index.getFirst());
				
				if(r == -1) {System.out.println("Unbeschrenkt!");return null;}
			
				if(r < m +1) {
					int temp = B[r -1];
					B[r -1] = non[LUindex.getFirst()].index;
					//B[r -1] = L.remove((int) LUindex.getFirst());
				
					non[LUindex.getFirst()].index = temp;
				
					if(y.get(r, 0).compareTo(FracBigInt.ZERO) < 0) {
						non[LUindex.getFirst()].LorU *= -1;
					}
				}else {
					non[LUindex.getFirst()].LorU *= -1;
				}
				count++;
				if(debug) {System.out.println("z: " + Carry.get(0, 0).multiply(new FracBigInt("-1")));}
			}
			MIN.removeFirst();
			index.removeFirst();
			LUindex.removeFirst();
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

	