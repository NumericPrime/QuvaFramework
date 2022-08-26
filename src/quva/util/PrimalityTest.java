package quva.util;

import quva.core.QUBOMatrix;

public class PrimalityTest extends QUBOMatrix implements QuvaTemplate {
	private static final long serialVersionUID = 9057676132756827312L;
	public int n=21;
	public PrimalityTest(int n) {
		super(200);
		init(10,false);

		//Calculating the bits needed to represent each number
		int l1=binaryDigits(n)-2;
		int l2=(int)((l1+1)/2);

		//registers p and q/ p=2^l1*p_0+2^(l1-1)p_1+2^(l1-2)p_2+...+2p_(l1-1)
		register("p",l1,(int)Math.pow(2,l1),false);
		register("q",l2,(int)Math.pow(2,l2),false);
		//multiplies p*q
		registerMultiplyCarries("pq","p","q");

		//adds the equation 0=n-(p+1)(q+1)=n-pq-p-q-1
		linearEquation(n+" -1*pq  -1*p  -1*q  -1");

		//Optimization 
		for(int i=find("pq").length-1;i>=0;i--) if(findWeight("pq")[i]>n) remove(find("pq")[i],false);
		for(int i=find("p").length-1;i>=0;i--) for(int j=find("q").length-1;j>=0;j--) if(findWeight("p")[i]*findWeight("q")[j]>n) add(4,find("p")[i],find("q")[j]); 

	}
	@Override
	public QUBOMatrix run() {
		return this;
	}

}
