
public class NonBasis {
	
	public static final int L = -1;
	
	public static final int U = 1;
	
	public int index;
	
	public int LorU;
	
	public NonBasis() {
		index = -1;
		LorU = 0;
	}
	
	public NonBasis(int idx, int lu) {
		index = idx;
		LorU = lu;
	}
	
	public String toString() {
		return (index + ":" + LorU);
	}
}
