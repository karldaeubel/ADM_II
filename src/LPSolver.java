import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;

/**
 * A program to solve .lp files with the revised simplex algorithm and exact arithmetic
 * @author marcel, karl
 */
public class LPSolver {
	/**
	 * The LP to solve
	 */
	private LPReader lp;
	/**
	 * The original A matrix with slag variables and artificial variables
	 */
	private Matrix A;
	/**
	 * The Carry matrix to update
	 */
	private Matrix Carry;
	//some other parameters
	private int noOfArti;

	private boolean debug;
	private boolean output;
	private boolean core;
	
	/**
	 * The main program to call (for informations type java LPSolver -h)
	 * @param args some options and files for the input
	 */
	public static void main(String[] args) {
		System.out.println("|---------------Willkommen---------------|\n" + 
				"|----------------------------------------|\n" + 
				"|----------------------------------------|\n" + 
				"|--------------LP Solver von-------------|\n" + 
				"|----------------------------------------|\n" + 
				"|-------------Marcel und Karl------------|\n" + 
				"|----------------------------------------|\n\n");

		if(args.length == 0 || args[0].toLowerCase().contains("-h")) {//do you need help?
			System.out.println("Eingabe:\n\njava LPSolver [Optionen] file1 file2 ..." + 
					"\n\nOptionen:\n" + 
					"\n '-d' oder 'debug' Debugmodus (Ausgabe von aktuellem ZFW, red. Kosten, etc.)" + 
					"\n '-x' oder 'var' Ausgabe der Variablen einer opt. Lösung" + 
					"\n '-r' oder 'read' Erweiterte Ausgabe des LPReaders" + 
					"\n '-c' oder 'core' Optionale Benutzung von mehreren Kernen bei der Multiplikation" + 
					"\n '-a' oder 'all' Äquivalent zu -d -x -r -c auf true" + 
					"\n\n Alle Argumente (außer -a) sind wie Schalter. Zweimalige Eingabe bringt die Variable in den ursprünglichen Zustand" + 
					"\n\nBeispiele:\n" + "\n java LPSolver -d -x /home/karl/kb2.lp" + 
					"\n <=> java LPSolver -a -r -c /home/karl/kb2.lp" + 
					"\n <=> java LPSolver -x -d -d -d -r -r /home/karl/kb2.lp" + 
					"\n\n java LPSolver -d -x /home/karl/kb2.lp -d -x /home/karl/afiro.lp" + 
					"\n <=> java LPSolver -a -r -c /home/karl/kb2.lp -a -x -d -r -c /home/karl/afiro.lp");
		}else {//the general case
			boolean out = false;
			boolean deb = false;
			boolean rd = false;
			boolean co = false;
			for(int i = 0; i < args.length; i++) {
				if(args[i].toLowerCase().equals("-d") || args[i].toLowerCase().equals("debug")) {
					deb = !deb;
				}else if(args[i].toLowerCase().equals("-x") || args[i].toLowerCase().equals("var")) {
					out = !out;
				}else if(args[i].toLowerCase().equals("-r") || args[i].toLowerCase().equals("read")) {
					rd = !rd;
				}else if(args[i].toLowerCase().equals("-c") || args[i].toLowerCase().equals("core")) {
					co = !co;
				}else if(args[i].toLowerCase().equals("-a") || args[i].toLowerCase().equals("all")) {
					deb = true;
					out = true;
					rd = true;
					co = true;
				}else {
					System.out.println("Datei: " + args[i] + "\n");
					LPSolver s = new LPSolver(args[i], deb, out, rd, co);
					s.solve();
				}
			}
		}
	}
	/**
	 * The default constructor which only tells you how to use this programm
	 */
	public LPSolver() {
		System.out.println("\nBitte geben Sie eine Datei an!\n\nBei hilfe 'java LPSolver -h' eingeben");
	}
	/**
	 * the real default constructor which set all parameters false
	 * @param file the .lp file to solve
	 */
	public LPSolver(String file) {
		this(file, false, false, false, false);
	}
	/**
	 * a constructor to initialize the LPSolver
	 * @param file the .lp file to solve
	 * @param de the option to enable the debug mode
	 * @param var the option to print all variables of an optimal solution at the end
	 * @param rd the option to print some extra informations about the reading process
	 * @param co the option to use some multiple cores for the matrix multiplication
	 */
	public LPSolver(String file, boolean de, boolean var, boolean rd, boolean co) {
		System.out.println("|---------Start the Program!---------|");
		debug = de;
		output = var;
		core = co;
		lp = new LPReader(file, rd);
		try {
			long start = System.nanoTime();
			lp.readLP();
			long stop = System.nanoTime();
			System.out.println("Reading Time: " + (stop - start) / 1000000. + " ms");
		}catch (FileNotFoundException e1) {
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		}catch (ParseException e1) {
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		}catch (IOException e1) {
			System.out.println("\n" + e1.toString() + "\n\nBeende...");
			System.exit(1);
		}
	}
	/**
	 * the method to call for solving an initialized LP
	 */
	public void solve() {
		long start = System.nanoTime();
		Matrix x = PhaseI();
		long stop = System.nanoTime();
		if(x == null) {
			System.out.println("Das Ausgangsproblem ist laut Phase I nicht lösbar...");
		}else {
			if(output) {
				System.out.println("\nDas LP hat folgede optimale Lösung:\n");
			}
			FracBigInt sum = new FracBigInt("0");
			for(int i = 0; i < x.getN(); i++) {
				if(output) {
					System.out.println(lp.variableName(i) + " = " + x.get(0, i).toDouble());
				}
				sum = sum.add(x.get(0, i).multiply(new FracBigInt(lp.objectiveVector()[i])));
			}
			System.out.println("\nZFW: " + sum.toDouble());
		}
		System.out.println("\nDas Lösen des Problems hat " + (stop - start) / 1000000. + " ms gedauert!\n");
	}
	/**
	 * the phase I of the revised simplex algorithm
	 * @return the optimal solution for the problem
	 */
	private Matrix PhaseI() {
		System.out.println("|----------PhaseI gestartet----------|");
		int m = lp.noOfConstraints();
		int noOfGE = 0;
		int noOfLE = 0;
		int noOfUb = 0;
		int[] r = new int[m];
		LinkedList<Integer> ub = new LinkedList<Integer>();
		//fix the lower bound to 0
		double[] au = new double[lp.noOfConstraints()];
		double sumw = 0;
		for(int i = 0; i < lp.noOfVariables(); i++) {
			if(Double.isInfinite(lp.lbound[i])) {
				noOfUb++;
				ub.add(i + 1);
			}
		}
		for(int i = 0; i < m; i++) {
			sumw = 0;
			for(int j = 0; j < lp.noOfVariables(); j++) {
				if(!Double.isInfinite(lp.lbound[j])) {
					sumw += lp.constraint[i][j] * lp.lbound[j];
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
		//if the right hand side is negative multiply the common constraint with -1 and count some important parametrs
		for(int i = 0; i < m; i++) {
			if(lp.rhs[i] < 0) {
				lp.rhs[i] = lp.rhs[i] * -1;
				lp.sense[i] *= -1;
				for(int j = 0; j < lp.noOfVariables(); j++) {
					lp.constraint[i][j] = lp.constraint[i][j] * -1;
				}
			}
			if(lp.sense[i] == LPReader.SENSE_GEQ) {
				noOfGE++;
				r[i] = -1;
			}else if(lp.sense[i] == LPReader.SENSE_LEQ) {
				noOfLE++;
				r[i] = 1;
			}
		}
		//initialize needed structures for all basis variables and non basis variables
		noOfArti = m - noOfLE;
		int n = m + lp.noOfVariables() + noOfGE + noOfUb;
		int[] B = new int[m];
		NonBasis[] non = new NonBasis[lp.noOfVariables() + noOfGE + noOfUb];
		int countNon = 0;

		FracBigInt[][] zero = new FracBigInt[m + 1][n + 1];
		for(int i = 0; i < m + 1; i++) {
			for(int j = 0; j < n + 1; j++) {
				zero[i][j] = new FracBigInt("0");
			}
		}
		//put all needed artificial and slag variables to the basic A matrix
		FracBigInt sum = FracBigInt.ZERO;
		int count = 0;
		for(int i = 0; i < m; i++) {
			zero[i + 1][0] = new FracBigInt(lp.rhs[i]);
			if(lp.sense[i] != LPReader.SENSE_LEQ) {
				B[i] = count + 1;
				zero[i + 1][count + 1] = FracBigInt.ONE;
				count++;
				sum = sum.substract(zero[i + 1][0]);
			}
		}
		zero[0][0] = sum;
		sum = new FracBigInt("0");
		for(int j = noOfArti + 1; j < noOfArti + 1 + lp.noOfVariables(); j++) {
			for(int i = 1; i < m + 1; i++) {
				zero[i][j] = new FracBigInt(lp.constraint[i - 1][j - noOfArti - 1]);
				if(lp.sense[i - 1] != LPReader.SENSE_LEQ) {
					sum = sum.substract(zero[i][j]);
				}
			}
			non[countNon++] = new NonBasis(j, NonBasis.L);
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}

		count = 0;
		sum = new FracBigInt("0");
		for(int j = noOfUb + noOfArti + 1 + lp.noOfVariables(); j < n + 1; j++) {
			for(int i = 1 + count; i < m + 1; i++) {
				if(r[i - 1] != 0) {
					zero[i][j] = new FracBigInt(r[i - 1]);
					count = i;
					if(r[i - 1] == 1) {
						B[i - 1] = j;
					}else {
						non[countNon++] = new NonBasis(j, NonBasis.L);
						sum = sum.substract(zero[i][j]);
					}
					break;
				}
			}
			zero[0][j] = sum;
			sum = new FracBigInt("0");
		}
		//initialize the A matrix
		A = new Matrix(zero);
		count = 0;
		for(int i = 0; i < ub.size(); i++) {
			non[countNon++] = new NonBasis(lp.noOfVariables() + noOfArti + i + 1, NonBasis.L);
			A.set(0, m, noOfArti + 1 + lp.noOfVariables() + i, noOfArti + 1 + lp.noOfVariables() + i, A.get(0, m, ub.get(i) + noOfArti, ub.get(i) + noOfArti).multiply(new FracBigInt("-1")));
		}
		//initialize the Carry matrix
		Carry = new Matrix(m + 1, m + 1);

		Carry.set(0, m, 0, 0, A.of(0, m, 0, 0));
		for(int i = 0; i < m; i++) {
			Carry.set(0, m, i + 1, i + 1, A.of(0, m, B[i], B[i]));
		}
		//call the phase II to get a feasible solution for the problem
		long start = System.nanoTime();
		Matrix x = PhaseII(B, non);
		long stop = System.nanoTime();
		if(debug) {
			System.out.println("Zeit: " + (stop - start));
		}
		//if the reduced costs of the feasible solution(for the changed cost function) then the problem has no feasible solution
		if(Carry.get(0, 0).compareTo(FracBigInt.ZERO) != 0) {
			System.out.println("keine zulässige Lösung!");
			return null;
		}
		//if some artificial variables are already in the basis we have to change them for non artificial variables
		Matrix At = null;
		for(int i = 0; i < B.length; i++) {
			if(B[i] <= noOfArti) {
				if(core) {
					At = ((Matrix) Carry.of(i + 1, i + 1, 1, m)).multiply((Matrix) A.of(1, m, 0, A.getN() - 1));
				}else {
					At = (Matrix) Carry.of(i + 1, i + 1, 1, m).altMultiply(A.of(1, m, 0, A.getN() - 1));
				}
				int t = -1;

				for(int j = noOfArti + 1; j < At.getN(); j++) {
					if(At.get(0, j).compareTo(FracBigInt.ZERO) != 0) {
						for(int l = 0; l < non.length; l++) {
							if(non[l].index == j) {
								if(non[l].LorU == NonBasis.L) {
									if(core) {
										gauss(((Matrix) Carry.of(1, m, 1, m)).multiply((Matrix) A.of(1, m, j, j)), i + 1);
									}else {
										gauss((Matrix) Carry.of(1, m, 1, m).altMultiply(A.of(1, m, j, j)), i + 1);
									}
									non[l].index = B[i];
									non[l].LorU = NonBasis.L;
									B[i] = j;
								}else {
									Matrix temp;
									if(core) {
										temp = ((Matrix) Carry.of(1, m, 1, m)).multiply((Matrix) A.of(1, m, j, j));
									}else {
										temp = (Matrix) Carry.of(1, m, 1, m).altMultiply(A.of(1, m, j, j));
									}
									FracBigInt c_j = Carry.of(0, 0, 1, m).altMultiply(A.of(1, m, j, j)).get(0, 0);

									Carry.set(0, 0, Carry.get(0, 0).add(new FracBigInt(lp.ubound[j - noOfArti]).multiply(c_j)));
									for(int k = 1; k < Carry.getM(); k++) {
										Carry.set(k, 0, Carry.get(k, 0).add(new FracBigInt(lp.ubound[j - noOfArti]).multiply(temp.get(k - 1, 0))));
									}
									if(core) {
										gauss(((Matrix) Carry.of(1, m, 1, m)).multiply((Matrix) A.of(1, m, j, j)), i + 1);
									}else {
										gauss((Matrix) Carry.of(1, m, 1, m).altMultiply(A.of(1, m, j, j)), i + 1);
									}
									non[l].index = B[i];
									non[l].LorU = NonBasis.L;
									B[i] = j;
								}
								t = j;
								break;
							}
						}
						if(t != -1) {
							break;
						}
					}
				}

				if(t == -1) {
					System.out.println("Redundante Zeile im LP! Beginne von vorn mit geändertem LP");
					double[][] constr = new double[lp.noOfConstraints() - 1][lp.noOfVariables()];
					double[] rightH = new double[lp.noOfConstraints() - 1];
					int[] sen = new int[lp.noOfConstraints() - 1];
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
		//construct the original problem
		if(noOfArti != 0) {
			for(int i = 0; i < m; i++) {
				B[i] = B[i] - noOfArti;
			}
		}

		NonBasis[] NON = new NonBasis[lp.noOfVariables() + noOfGE + noOfLE + noOfUb - lp.noOfConstraints()];
		countNon = 0;
		for(int i = 0; i < non.length; i++) {
			if(non[i].index - noOfArti > 0) {
				NON[countNon++] = new NonBasis(non[i].index - noOfArti, non[i].LorU);
			}
		}
		//calculate the original cost function
		Matrix b_ = (Matrix) A.of(0, m, 0, 0);
		A = (Matrix) A.of(0, m, noOfArti, A.getN() - 1);
		A.set(0, m, 0, 0, b_);
		noOfArti = 0;
		for(int i = 0; i < lp.noOfVariables() + noOfUb + noOfGE + noOfLE + 1; i++) {
			A.set(0, i, new FracBigInt("0"));
			if(lp.objectiveSense() == LPReader.SENSE_MAX) {
				if(i <= lp.noOfVariables() && i > 0) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[i - 1] * -1));
				}else if(i > lp.noOfVariables() && i <= lp.noOfVariables() + noOfUb) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[ub.get(i - lp.noOfVariables() - 1)]));
				}
			}else if(lp.objectiveSense() == LPReader.SENSE_MIN) {
				if(i <= lp.noOfVariables() && i > 0) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[i - 1]));
				}else if(i > lp.noOfVariables() && i <= lp.noOfVariables() + noOfUb) {
					A.set(0, i, new FracBigInt(lp.objectiveVector()[ub.get(i - lp.noOfVariables() - 1) - 1] * -1));
				}
			}
		}
		//compute the updated reduced costs for the Carry matrix
		FracBigInt[][] c = new FracBigInt[1][m];
		for(int i = 0; i < m; i++) {
			c[0][i] = new FracBigInt("0");
			if(B[i] <= lp.noOfVariables()) {
				if(lp.objectiveSense() == LPReader.SENSE_MAX) {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[B[i] - 1])).multiply(new FracBigInt("-1"));
				}else {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[B[i] - 1]));
				}
			}else if(B[i] > lp.noOfVariables() && B[i] <= lp.noOfVariables() + noOfUb) {
				if(lp.objectiveSense() == LPReader.SENSE_MAX) {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[ub.get(B[i] - lp.noOfVariables() - 1)]));
				}else {
					c[0][i] = (new FracBigInt(lp.objectiveVector()[ub.get(B[i] - lp.noOfVariables() - 1)])).multiply(new FracBigInt("-1"));
				}
			}
		}

		Matrix C = (Matrix) (new Matrix(c)).multiply(new FracBigInt("-1"));
		Matrix pi;
		if(core) {
			pi = ((Matrix) C).multiply((Matrix) Carry.of(1, m, 1, m));
		}else {
			pi = (Matrix) C.altMultiply(Carry.of(1, m, 1, m));
		}
		Carry.set(0, 0, 1, m, pi);
		Carry.set(0, 0, 0, 0, C.altMultiply((Matrix) Carry.of(1, m, 0, 0)));

		System.out.println("|----------PhaseI beendet------------|");
		//start to solve the real problem
		x = PhaseII(B, NON);

		if(x == null) {
			return null;
		}
		//compute the solution by shifting with the lower bounds and adding the upper variables
		FracBigInt[][] s = new FracBigInt[1][lp.noOfVariables()];
		for(int i = 0; i < NON.length; i++) {
			if(NON[i].index <= lp.noOfVariables()) {
				if(NON[i].LorU == NonBasis.L) {
					s[0][NON[i].index - 1] = new FracBigInt(lp.lbound[NON[i].index - 1]);
				}else {
					s[0][NON[i].index - 1] = new FracBigInt(lp.ubound[NON[i].index - 1]).add(new FracBigInt(lp.lbound[NON[i].index - 1]));
				}
			}
		}

		for(int i = 0; i < m; i++) {
			if(B[i] <= lp.noOfVariables()) {
				if(!Double.isInfinite(lp.lbound[B[i] - 1])) {
					s[0][B[i] - 1] = x.get(i, 0).add(new FracBigInt(lp.lbound[B[i] - 1]));
				}else {
					s[0][B[i] - 1] = x.get(i, 0);
				}
			}else if(B[i] > lp.noOfVariables() && B[i] <= lp.noOfVariables() + noOfUb) {
				s[0][ub.get(NON[i].index - lp.noOfVariables() - 1)] = s[0][ub.get(NON[i].index - lp.noOfVariables() - 1)].substract(x.get(i, 0));
			}
		}
		return new Matrix(s);
	}
	/**
	 * do one gauss step on the carry matrix
	 * @param at the row to pivot at
	 * @param r the index of the row
	 */
	private void gauss(Matrix at, int r) {
		FracBigInt xrs = at.get(r - 1, 0).invert();
		Matrix rowr = (Matrix) Carry.of(r, r, 0, Carry.getN() - 1);

		for(int i = 1; i < Carry.getM(); i++) {
			if(i == r) {
				Carry.set(i, i, 0, Carry.getN() - 1, Carry.of(i, i, 0, Carry.getN() - 1).multiply(xrs));
			}else {
				Carry.set(i, i, 0, Carry.getN() - 1, Carry.of(i, i, 0, Carry.getN() - 1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(at.get(i - 1, 0))))));
			}
		}
	}
	/**
	 * the phasae II of the revised simplex algorithm
	 * @param B the actual basis variables
	 * @param non the actual non basis variables
	 * @return the optimal solution
	 */
	public Matrix PhaseII(int[] B, NonBasis[] non) {
		System.out.println("|----------PhaseII gestartet---------|");
		int m = lp.noOfConstraints();

		int count = 0;
		//a list to save all columns up to a common size rb which have feasible reduced costs 
		LinkedList<BestColElement> best = new LinkedList<BestColElement>();
		int rb = (A.getN() - m) / 3;
		if(rb == 0) {rb = 1;}
		FracBigInt c_j = new FracBigInt("0");
		while (true) {
			if(best.size() == 0) {//initialize the list only of it is empty
				c_j = FracBigInt.ZERO;
				for(int j = 0; j < non.length; j++) {
					c_j = A.get(0, non[j].index).add(Carry.of(0, 0, 1, m).altMultiply(A.of(1, m, non[j].index, non[j].index)).get(0, 0));

					if((non[j].LorU == NonBasis.L && c_j.compareTo(FracBigInt.ZERO) < 0) || (non[j].LorU == NonBasis.U && c_j.compareTo(FracBigInt.ZERO) > 0)) {

						Matrix y = new Matrix(new FracBigInt[m + 1][1]);
						y.set(0, 0, c_j);
						if(core) {
							y.set(1, m, 0, 0, ((Matrix) Carry.of(1, m, 1, m)).multiply((Matrix) A.of(1, m, non[j].index, non[j].index)));
						}else {
							y.set(1, m, 0, 0, (Matrix) Carry.of(1, m, 1, m).altMultiply(A.of(1, m, non[j].index, non[j].index)));
						}
						FracBigInt mo = stepI(y, B, j);
						if(mo.compareTo(FracBigInt.ZERO) != 0) {
							c_j = mo.multiply(c_j);
						}
						if(c_j.compareTo(FracBigInt.ZERO) < 0) {
							c_j = c_j.multiply(new FracBigInt("-1"));
						}

						if(best.size() < rb) {
							if(best.size() == 0) {
								best.add(new BestColElement(non[j].index, j, c_j));
							}else if(best.getLast().redcost.compareTo(c_j) >= 0) {
								best.add(new BestColElement(non[j].index, j, c_j));
							}else {
								for(int i = 0; i < best.size(); i++) {
									if(c_j.compareTo(best.get(i).redcost) > 0) {
										best.add(i, new BestColElement(non[j].index, j, c_j));
										break;
									}
								}
							}
						}else {
							for(int i = 0; i < best.size(); i++) {
								if(c_j.compareTo(best.get(i).redcost) > 0) {
									best.set(i, new BestColElement(non[j].index, j, c_j));
									break;
								}
							}
						}
					}
				}
			}
			//the there is no column with nice reduced costs we are done
			if(best.size() == 0) {
				System.out.println("|----------PhaseII beendet-----------| Count: " + count);
				return (Matrix) Carry.of(1, m, 0, 0);
			}
			//get the element of the list with the best reduced costs: |c_j*theta| and make a pivot step
			c_j = A.get(0, best.getFirst().colIndex).add(Carry.of(0, 0, 1, m).altMultiply(A.of(1, m, best.getFirst().colIndex, best.getFirst().colIndex)).get(0, 0));
			if((non[best.getFirst().nonBasisIndex].LorU == NonBasis.L && c_j.compareTo(FracBigInt.ZERO) < 0) || (non[best.getFirst().nonBasisIndex].LorU == NonBasis.U && c_j.compareTo(FracBigInt.ZERO) > 0)) {
				if(debug) {
					System.out.println("reduced cost: " + c_j + " L/U: " + non[best.getFirst().nonBasisIndex].LorU);
				}
				Matrix y = new Matrix(new FracBigInt[m + 1][1]);
				y.set(0, 0, c_j);
				if(core) {
					y.set(1, m, 0, 0, ((Matrix) Carry.of(1, m, 1, m)).multiply((Matrix) A.of(1, m, best.getFirst().colIndex, best.getFirst().colIndex)));
				}else {
					y.set(1, m, 0, 0, Carry.of(1, m, 1, m).altMultiply(A.of(1, m, best.getFirst().colIndex, best.getFirst().colIndex)));
				}
				//calculate the basis index for which basis variable have to leave
				int r = step(y,B, best.getFirst().colIndex);
				//if there is no common index the problem is unbounded
				if(r == -1) {
					System.out.println("Unbeschrenkt!");
					return null;
				}
				//otherwise update the basis and non basis variables
				if(r < m + 1) {
					if(debug) {
						System.out.println("old basis var: " + B[r - 1] + " new basis var: " + non[best.getFirst().nonBasisIndex].index);
					}
					int temp = B[r - 1];
					B[r - 1] = non[best.getFirst().nonBasisIndex].index;

					non[best.getFirst().nonBasisIndex].index = temp;

					if(y.get(r, 0).compareTo(FracBigInt.ZERO) < 0) {
						non[best.getFirst().nonBasisIndex].LorU *= -1;
					}
				}else {
					non[best.getFirst().nonBasisIndex].LorU *= -1;
				}
				count++;
				if(debug) {
					System.out.println("z: " + Carry.get(0, 0).multiply(new FracBigInt("-1")));
				}
			}
			//remove the current element from the list
			best.removeFirst();
		}
	}
	/**
	 * to calculate the theta in each column
	 * @param x the column
	 * @param B all basis variables
	 * @param k the index of the column
	 * @return the common theta for this column
	 */
	public FracBigInt stepI(Matrix x, int[] B, int k) {
		Matrix b = (Matrix) Carry.of(0, lp.noOfConstraints(), 0, 0);
		FracBigInt min = FracBigInt.POSINF;
		if(x.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0));
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(B[i - 1] > noOfArti && B[i - 1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i - 1] - noOfArti - 1])) {
						if(((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0).multiply(new FracBigInt("-1")));
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti - 1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti - 1])) > 0) {
						return new FracBigInt(lp.ubound[k - noOfArti - 1]);
					}
				}

			}
			return min;
		}else {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0).multiply(new FracBigInt("-1")));
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(B[i - 1] > noOfArti && B[i - 1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i - 1] - noOfArti - 1])) {
						if(((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0));
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti - 1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti - 1])) > 0) {
						return new FracBigInt(lp.ubound[k - noOfArti - 1]);
					}
				}
			}
		}
		return min;
	}
	/**
	 * a method to calculate the optimal row to make a pivot step and update the Carry matrix
	 * @param x the current column
	 * @param B the current basis
	 * @param k the index of the current column
	 * @return the basis variable for which the minimum is taken
	 */
	public int step(Matrix x, int[] B, int k) {
		int result = -1;
		Matrix b = (Matrix) Carry.of(0, lp.noOfConstraints(), 0, 0);
		FracBigInt min = FracBigInt.POSINF;
		if(x.get(0, 0).compareTo(FracBigInt.ZERO) < 0) {
			for(int i = 1; i < x.getM(); i++) {
				if(x.get(i, 0).compareTo(FracBigInt.ZERO) > 0) {
					if(b.get(i, 0).divide(x.get(i, 0)).compareTo(min) == -1) {
						min = b.get(i, 0).divide(x.get(i, 0));
						result = i;
					}
				}else if(x.get(i, 0).compareTo(FracBigInt.ZERO) < 0) {
					if(B[i - 1] > noOfArti && B[i - 1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i - 1] - noOfArti - 1])) {
						if(((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0).multiply(new FracBigInt("-1"))).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0).multiply(new FracBigInt("-1")));
							result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti - 1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti - 1])) > 0) {
						for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k - noOfArti - 1]))));
						}
						return lp.noOfConstraints() + 1;
					}

				}
			}
			if(result > 0) {
				FracBigInt xrs = x.get(result, 0).invert();
				Matrix rowr = (Matrix) Carry.of(result, result, 1, Carry.getN() - 1);
				for(int i = 0; i < Carry.getM(); i++) {
					if(i == result) {
						Carry.set(i, 0, min);
						Carry.set(i, i, 1, Carry.getN() - 1, Carry.of(i, i, 1, Carry.getN() - 1).multiply(xrs));
					}else {
						Carry.set(i, 0, Carry.get(i, 0).substract(min.multiply(x.get(i, 0))));
						Carry.set(i, i, 1, Carry.getN() - 1, Carry.of(i, i, 1, Carry.getN() - 1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i, 0))))));
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
					if(B[i - 1] > noOfArti && B[i - 1] <= noOfArti + lp.noOfVariables() && !Double.isInfinite(lp.ubound[B[i - 1] - noOfArti - 1])) {
						if(((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0)).compareTo(min) == -1) {
							min = ((new FracBigInt(lp.ubound[B[i - 1] - noOfArti - 1])).substract(b.get(i, 0))).divide(x.get(i, 0));
							result = i;
						}
					}
				}
			}
			if(k > noOfArti && k <= noOfArti + lp.noOfVariables()) {
				if(!Double.isInfinite(lp.ubound[k - noOfArti - 1])) {
					if(min.compareTo(new FracBigInt(lp.ubound[k - noOfArti - 1])) > 0) {
						for(int i = 0; i < Carry.getM(); i++) {
							Carry.set(i, 0, Carry.get(i, 0).substract(x.get(i, 0).multiply(new FracBigInt(lp.ubound[k - noOfArti - 1])).multiply(new FracBigInt("-1"))));
						}
						return lp.noOfConstraints() + 1;
					}

				}
			}
			if(result > 0) {
				FracBigInt xrs = x.get(result, 0).invert();
				Matrix rowr = (Matrix) Carry.of(result, result, 1, Carry.getN() - 1);
				for(int i = 0; i < Carry.getM(); i++) {
					if(i == result) {
						Carry.set(i, 0, new FracBigInt(lp.ubound[k - 1 - noOfArti]).substract(min));
						Carry.set(i, i, 1, Carry.getN() - 1, Carry.of(i, i, 1, Carry.getN() - 1).multiply(xrs));
					}else {
						Carry.set(i, 0, Carry.get(i, 0).add(min.multiply(x.get(i, 0))));
						Carry.set(i, i, 1, Carry.getN() - 1, Carry.of(i, i, 1, Carry.getN() - 1).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(i, 0))))));
					}
				}
			}
		}
		return result;
	}
}