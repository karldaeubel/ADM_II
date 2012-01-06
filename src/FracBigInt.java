import java.math.BigInteger;
import java.util.Arrays;

public class FracBigInt implements Comparable<FracBigInt> {

	private BigInteger num;
	private BigInteger denom;
	public static final FracBigInt ONE = new FracBigInt("1","1");
	public static final FracBigInt ZERO = new FracBigInt();
	public static final FracBigInt POSINF = new FracBigInt("1","0");
	public static final FracBigInt NEGINF = new FracBigInt("-1","0");

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
		if (x == Double.POSITIVE_INFINITY){
			this.num = new BigInteger("1");
			this.denom = new BigInteger("0");
		}
		if (x == Double.NEGATIVE_INFINITY){
			this.num = new BigInteger("-1");
			this.denom = new BigInteger("0");
		}
		if ( !Double.isInfinite(x)){
			String s = Double.toString(x);
			String[] split = s.split("\\.");
			//System.out.println(Arrays.toString(split)+" "+s);
			num = new BigInteger(split[0]+split[1]);
			denom = new BigInteger(Long.toString((Math.round(Math.pow(10,split[1].length())))));
			this.simplify();
		}
		
	}
	
	public FracBigInt add(FracBigInt f) {
		if ( this.isInfinite() || f.isInfinite() ){
			if ( this.equals(FracBigInt.POSINF) && !f.equals(FracBigInt.NEGINF)){
				return FracBigInt.POSINF;
			}
			if ( !this.equals(FracBigInt.POSINF) && f.equals(FracBigInt.NEGINF)){
				return FracBigInt.NEGINF;
			}
			if ( this.equals(FracBigInt.NEGINF) && !f.equals(FracBigInt.POSINF)){
				return FracBigInt.NEGINF;
			}
			if ( !this.equals(FracBigInt.NEGINF) && f.equals(FracBigInt.POSINF)){
				return FracBigInt.POSINF;
			}
			if ( (this.equals(FracBigInt.POSINF) && f.equals(FracBigInt.NEGINF)) || (this.equals(FracBigInt.NEGINF) && f.equals(FracBigInt.POSINF))){
				throw new IllegalArgumentException("beide Argumente verschieden unendlich");
			}
		}
		FracBigInt result = new FracBigInt((this.num.multiply(f.denom)).add(f.num.multiply(this.denom)).toString(), this.denom.multiply(f.denom).toString());
		result.simplify();
		return result;
	}

	public FracBigInt multiply(FracBigInt f) {
		if ( this.isInfinite() || f.isInfinite() ){
			if ( this.equals(FracBigInt.POSINF) && f.compareTo(FracBigInt.ZERO)>0){
				return FracBigInt.POSINF;
			}
			if ( this.equals(FracBigInt.POSINF) && f.compareTo(FracBigInt.ZERO)<0){
				return FracBigInt.NEGINF;
			}
			if ( this.equals(FracBigInt.NEGINF) && f.compareTo(FracBigInt.ZERO)>0){
				return FracBigInt.NEGINF;
			}
			if ( this.equals(FracBigInt.NEGINF) && f.compareTo(FracBigInt.ZERO)<0){
				return FracBigInt.POSINF;
			}
			if ( f.equals(FracBigInt.POSINF) && this.compareTo(FracBigInt.ZERO)>0){
				return FracBigInt.POSINF;
			}
			if ( f.equals(FracBigInt.POSINF) && this.compareTo(FracBigInt.ZERO)<0){
				return FracBigInt.NEGINF;
			}
			if ( f.equals(FracBigInt.NEGINF) && this.compareTo(FracBigInt.ZERO)>0){
				return FracBigInt.NEGINF;
			}
			if ( f.equals(FracBigInt.NEGINF) && this.compareTo(FracBigInt.ZERO)<0){
				return FracBigInt.POSINF;
			}
		}
		FracBigInt result = new FracBigInt(this.num.multiply(f.num).toString(),this.denom.multiply(f.denom).toString());
		result.simplify();
		return result;
	}

	public FracBigInt divide(FracBigInt f) {
		if ( this.isInfinite() && !f.isInfinite()){
			return this.clone();
		}
		if ( !this.isInfinite() && f.isInfinite() ){
			return FracBigInt.ZERO;
		}
		if ( this.isInfinite() && f.isInfinite()){
			throw new IllegalArgumentException("Beide Argumnte sind unendlich");
		}
		FracBigInt result = new FracBigInt(this.num.multiply(f.denom).toString(), this.denom.multiply(f.num).toString());
		result.simplify();
		return result;
	}

	public FracBigInt substract(FracBigInt f) {
		if ( this.isInfinite() || f.isInfinite() ){
			if ( this.equals(FracBigInt.POSINF) && !f.equals(FracBigInt.NEGINF)){
				return FracBigInt.POSINF;
			}
			if ( !this.equals(FracBigInt.POSINF) && f.equals(FracBigInt.NEGINF)){
				return FracBigInt.POSINF;
			}
			if ( this.equals(FracBigInt.NEGINF) && !f.equals(FracBigInt.POSINF)){
				return FracBigInt.NEGINF;
			}
			if ( !this.equals(FracBigInt.NEGINF) && f.equals(FracBigInt.POSINF)){
				return FracBigInt.NEGINF;
			}
			if ( (this.equals(FracBigInt.POSINF) && f.equals(FracBigInt.NEGINF)) || (this.equals(FracBigInt.NEGINF) && f.equals(FracBigInt.POSINF))){
				throw new IllegalArgumentException("beide Argumente verschieden unendlich");
			}
		}
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
		if ( this == null ){
			return "null";
		}
		if ( this.denom.equals(BigInteger.ZERO) && this.num.equals(BigInteger.ONE)){
			return "inf";
		}
		if ( this.denom.equals(BigInteger.ZERO) && this.num.equals(new BigInteger("-1"))){
			return "-inf";
		}
		
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
		if (this.equals(FracBigInt.POSINF)){
			return Double.POSITIVE_INFINITY;
		}
		if (this.equals(FracBigInt.NEGINF)){
			return Double.NEGATIVE_INFINITY;
		}
		return (num.doubleValue())/(denom.doubleValue());
	}
	
	public FracBigInt invert(){
		if ( this.num.equals(BigInteger.ZERO) ){
			throw new IllegalArgumentException("0 kann nicht invertiert werden");
		}
		if ( this.isInfinite()){
			return FracBigInt.ZERO;
		}
		return new FracBigInt(this.denom.toString(),this.num.toString());
	}
	
	public boolean isInfinite(){
		return this.denom.equals(BigInteger.ZERO);
	}
	
	public static void main(String[] args) {
		FracBigInt test = new FracBigInt(2.232);
		System.out.println(test.toString());
		System.out.println(Double.toString(test.toDouble()));
		System.out.println(new FracBigInt("1","0"));
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
		if ( this.equals(o)){
			return 0;
		}
		if ( this.equals(FracBigInt.POSINF) ){
			return 1;
		}
		if ( this.equals(FracBigInt.NEGINF)){
			return -1;
		}
		return this.num.multiply(o.denom).compareTo(this.denom.multiply(o.num)); 
	}

}
