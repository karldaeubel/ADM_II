
public class BestColElement {
	
	public int colIndex;
	
	public int nonBasisIndex;
	
	public FracBigInt redcost;
	
	public BestColElement() {
		colIndex = -1;
		nonBasisIndex = -1;
		redcost = FracBigInt.ZERO;
	}
	
	public BestColElement(int ci, int noni, FracBigInt cost) {
		colIndex = ci;
		nonBasisIndex = noni;
		redcost = cost.clone();
	}
	
	public String toString() {
		return colIndex + " " + redcost;
	}
}
