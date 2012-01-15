/**
 * a class for an element of the list in which are all elements with feasible reduced costs
 * @author marcel, karl
 *
 */
public class BestColElement implements Comparable<BestColElement>{
	/**
	 * the current index of the variable in A
	 */
	public int colIndex;
	/**
	 * the current index in the array of all non basis variables
	 */
	public int nonBasisIndex;
	/**
	 * the reduced costs of this column multiplied by theta
	 */
	public FracBigInt redcost;
	/**
	 * a default constructor
	 */
	public BestColElement() {
		colIndex = -1;
		nonBasisIndex = -1;
		redcost = FracBigInt.ZERO;
	}
	/**
	 * a constructor initialize an element of the best reduced cost list
	 * @param ci the current column index in A
	 * @param noni the current index in the non basis array
	 * @param cost the current reduced costs multiplied by theta
	 */
	public BestColElement(int ci, int noni, FracBigInt cost) {
		colIndex = ci;
		nonBasisIndex = noni;
		redcost = cost.clone();
	}
	
	@Override
	public String toString() {
		return colIndex + " " + redcost;
	}

	@Override
	public int compareTo(BestColElement temp) {
		if(redcost.compareTo(temp.redcost) == 1) {
			return 1;
		}else if(redcost.compareTo(temp.redcost) == -1) {
			return -1;
		}else {
			return 0;
		}
	}	
}
