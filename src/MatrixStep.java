
public class MatrixStep extends Thread {
	
	private Matrix matrix;
	private int[] B;
	private Matrix x;
	private int row;
	private Matrix rowr;
	private FracBigInt xrs;
	
	public MatrixStep(Matrix matrix, int[] B , Matrix x , int row, Matrix rowr, FracBigInt xrs){
		this.B = B;
		this.matrix = matrix;
		this.x = x;
		this.row = row;
		this.rowr = rowr;
		this.xrs = xrs;
	}
	
	
	public void run(){
		matrix.set(row,(Matrix) matrix.get(row).add(rowr.multiply((new FracBigInt("-1")).multiply(xrs.multiply(x.get(row,0))))));
	}
}
