import java.math.BigInteger;
import java.util.Arrays;

public class FracBigInt implements Comparable<FracBigInt> {

	private BigInteger num;
	private BigInteger denom;
	public static final FracBigInt ONE = new FracBigInt("1","1");
	public static final FracBigInt ZERO = new FracBigInt();

	public FracBigInt(String n) {
		num = new BigInteger(n);
		denom = BigInteger.ONE;
	}

	public FracBigInt() {
		num = BigInteger.ZERO;
		denom = BigInteger.ONE;
	}

	public FracBigInt(String n, String d) {
		if (n.equals("0")) {
			num = BigInteger.ZERO;
			denom = BigInteger.ONE;
		} else {
			num = new BigInteger(n);
			denom = new BigInteger(d);
			this.simplify();
		}
	}

	public FracBigInt(double x){
		String s = Double.toString(x);
		String[] split = s.split("\\.");
		//System.out.println(Arrays.toString(split)+" "+s);
		num = new BigInteger(split[0]+split[1]);
		denom = new BigInteger(Long.toString((Math.round(Math.pow(10,split[1].length())))));
		this.simplify();
		
	}
	
	public FracBigInt add(FracBigInt f) {
		FracBigInt result = new FracBigInt((this.num.multiply(f.denom)).add(f.num.multiply(this.denom)).toString(), this.denom.multiply(f.denom).toString());
		result.simplify();
		return result;
	}

	public FracBigInt multiply(FracBigInt f) {
		FracBigInt result = new FracBigInt(this.num.multiply(f.num).toString(),this.denom.multiply(f.denom).toString());
		result.simplify();
		return result;
	}

	public FracBigInt divide(FracBigInt f) {
		FracBigInt result = new FracBigInt(this.num.multiply(f.denom).toString(), this.denom.multiply(f.num).toString());
		result.simplify();
		return result;
	}

	public FracBigInt substract(FracBigInt f) {
		FracBigInt result = new FracBigInt((this.num.multiply(f.denom)).subtract(f.num.multiply(this.denom)).toString(), this.denom.multiply(f.denom).toString());
		result.simplify();
		return result;
	}

	private void simplify() {
		if ((this.num.equals(BigInteger.ZERO) && !this.denom.equals(BigInteger.ZERO))|| (this.num.equals(BigInteger.ZERO) && this.denom.equals(BigInteger.ZERO))) {
			this.denom = BigInteger.ONE;
		} else {
			if (!this.denom.equals(BigInteger.ZERO)) {
				try {
					BigInteger gcd = num.gcd(denom);
					num = num.divide(gcd);
					denom = denom.divide(gcd);
					num = num
							.multiply(BigInteger.valueOf((long) denom.signum()));
					denom = denom.multiply(BigInteger.valueOf((long) denom
							.signum()));
				} catch (ArithmeticException ae) {
				}
			}
		}
	}

	public String toString() {
		String str;
		if (!denom.equals(BigInteger.ONE))
			str = num.toString() + "/" + denom.toString();
		else
			str = "" + num.toString();
		return str;
	}

	public boolean equals(FracBigInt f) {
		return (this.num.equals(f.num) && this.denom.equals(f.denom));
	}

	public FracBigInt clone() {
		return new FracBigInt(this.num.toString(), this.denom.toString());
	}

	public BigInteger getNumerator() {
		return num;
	}

	public BigInteger getDenominator() {
		return denom;
	}

	public double toDouble(){
		return ((double) num.intValue())/((double) denom.intValue());
	}
	
	public void invert(){
		BigInteger temp = new BigInteger(num.toString());
		num = new BigInteger(denom.toString());
		denom = new BigInteger(temp.toString());
	}
	
	public static void main(String[] args) {
		FracBigInt test = new FracBigInt(2.232);
		System.out.println(test.toString());
		System.out.println(Double.toString(test.toDouble()));
		/*
		FracBigInt test = new FracBigInt(args[0], args[1]);
		FracBigInt test2 = new FracBigInt(args[2], args[3]);
		System.out.println(test.toString() + " " + test2.toString());
		System.out.println((test.add(test2)).toString());
		System.out.println((test.multiply(test2)).toString());
		System.out.println((test.substract(test2)).toString());
		*/
	}

	@Override
	public int compareTo(FracBigInt o) {
		return this.num.multiply(o.denom).compareTo(this.denom.multiply(o.num)); 
	}

}
