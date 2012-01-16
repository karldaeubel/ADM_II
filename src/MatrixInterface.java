
public interface MatrixInterface {
		
	/**
	 * returns the submatrix between the rows i1 and i2 and the columns j1 and j2
	 * @param i1 the first row
	 * @param i2 the second row
	 * @param j1 the first column
	 * @param j2 the second column
	 * @return the submatrix between the rows i1 and i2 and the columns j1 and j2
	 */
	public MatrixInterface of(int i1, int i2, int j1, int j2);
	
	/**
	 * computes the product of this matrix and matrix
	 * @param matrix
	 * @return the matrix pruduct if this and matrix 
	 */
	public MatrixInterface altMultiply(MatrixInterface matrix);
	
	/**
	 * returns the product of this matrix and a factor
	 * @param factor
	 * @return product of this and a factor
	 */
	public MatrixInterface multiply(FracBigInt factor);
	
	/**
	 * sets the entries of this matrix between the rows i1 and i2 and the columns j1 and j2 to the entries of matrix
	  * @param i1 the first row
	 * @param i2 the second row
	 * @param j1 the first column
	 * @param j2 the second column
	 * @param matrix
	 */
	public void set(int i1, int i2, int j1, int j2, MatrixInterface matrix);
	
	/**
	 * sets the entry of the field (i,j) of this matrix to value
	 * @param i the row
	 * @param j the column
	 * @param value
	 */
	public void set(int i, int j, FracBigInt value);
	
	/**
	 * returns the entry of the field (i,j) of this matrix
	 * @param i the row
	 * @param j the column
	 * @return the entry of the field (i,j)
	 */
	public FracBigInt get(int i, int j);
	
	/**
	 * returns a string representation of this matrix
	 * @return a string representation of this matrix
	 */
	public String toString();
	
	/**
	 * returns the number of rows of this matrix
	 * @return the number of rows
	 */
	public int getM();
	
	/**
	 * returns the number of columns of this matrix
	 * @return the number of columns
	 */
	public int getN();
	
	/**
	 * adds this matrix with matrix
	 * @param matrix
	 * @return the sum of this matrix and matrix
	 */
	public MatrixInterface add( MatrixInterface matrix );
}