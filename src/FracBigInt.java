import java.math.BigInteger;

public class FracBigInt{

    private BigInteger num;
    private BigInteger denom;
   

    public FracBigInt( String n ){
	num = new BigInteger(n);
	denom = BigInteger.ONE;
    }

    public FracBigInt(){
	num = BigInteger.ZERO;
	denom = BigInteger.ONE;
    }

    public FracBigInt( String n , String d ){
	if ( n.equals("0") ){
	    num = BigInteger.ZERO;
	    denom = BigInteger.ONE;
	} else {
	    num = new BigInteger(n);
	    denom = new BigInteger(d);
	    this.simplify();
	}
    }

    public FracBigInt add( FracBigInt f ){
	FracBigInt result = new FracBigInt( (this.num.multiply(f.denom)).add( f.num.multiply(this.denom) ).toString() , this.denom.multiply(f.denom).toString() );
	result.simplify();
	return result;
    }

    public FracBigInt multiply( FracBigInt f ){
	FracBigInt result = new FracBigInt( this.num.multiply(f.num).toString() , this.denom.multiply(f.denom).toString() );
	result.simplify();
	return result;
    }

    public FracBigInt divide( FracBigInt f ){
	FracBigInt result = new FracBigInt( this.num.multiply(f.denom).toString() , this.denom.multiply(f.num).toString() );
	result.simplify();
	return result;
    }

    public FracBigInt substract( FracBigInt f ){
	FracBigInt result = new FracBigInt( (this.num.multiply(f.denom)).subtract( f.num.multiply(this.denom)).toString() , this.denom.multiply(f.denom).toString() );
	result.simplify();
	return result;
    }

    private void simplify(){
	if ( (this.num.equals(BigInteger.ZERO) && !this.denom.equals(BigInteger.ZERO)) || (this.num.equals(BigInteger.ZERO) && this.denom.equals(BigInteger.ZERO))){
	    this.denom = BigInteger.ONE;
	}else{
	    if ( !this.denom.equals(BigInteger.ZERO) ){
		try{
		    BigInteger gcd = num.gcd( denom );	
		    num = num.divide(gcd);
		    denom = denom.divide(gcd);
		    num = num.multiply(BigInteger.valueOf((long)denom.signum()));
		    denom = denom.multiply(BigInteger.valueOf((long)denom.signum()));
		}catch( ArithmeticException ae ){
		}
	    }
	}
    }

     

    public String toString(){
	String str;
	if ( !denom.equals(BigInteger.ONE) )
	    str = num.toString() + "/" + denom.toString();
	else 
	    str = "" + num.toString();
	return str;
    }

    public boolean equals( FracBigInt f ){
	return ( this.num.equals( f.num )&& this.denom.equals( f.denom ) );
    }

    public FracBigInt clone(){
	return new FracBigInt( this.num.toString() , this.denom.toString() );
    }

    public BigInteger getNumerator(){
	return num;
    }

    public BigInteger getDenominator(){
	return denom;
    }
    
    public static void main( String[] args ){
	FracBigInt test = new FracBigInt ( args[0] , args[1] );
	FracBigInt test2 = new FracBigInt ( args[2] , args[3] );
	System.out.println( test.toString() + " " + test2.toString() );	
	System.out.println( (test.add(test2)).toString() );
	System.out.println( (test.multiply(test2)).toString() );
	System.out.println( (test.substract(test2)).toString() );
    }


}

