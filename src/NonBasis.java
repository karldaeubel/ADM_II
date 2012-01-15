/**
 * a class to represent all non basis variables including whether they are in the upper or lower bound set
 * @author marcel, karl
 *
 */
public class NonBasis {
	/**
	 * represent the lower bound set
	 */
	public static final int L = -1;
	/**
	 * represent the upper bound set
	 */
	public static final int U = 1;
	/**
	 * the index in the A matrix
	 */
	public int index;
	/**
	 * is this non basis variable in the upper or lower bound set?
	 */
	public int LorU;
	/**
	 * a default constructor for a non basis variable with index = -1 and it is set to non of the lower or upper sets 
	 */
	public NonBasis() {
		index = -1;
		LorU = 0;
	}
	/**
	 * a constructor to initialize a non basis variable
	 * @param idx the current index of the variable in A
	 * @param lu tells whether it is in the upper or lower bound set, have to be one of the static variables L or U
	 */
	public NonBasis(int idx, int lu) {
		index = idx;
		LorU = lu;
	}
	
	@Override
	public String toString() {
		return (index + ":" + LorU);
	}
}
