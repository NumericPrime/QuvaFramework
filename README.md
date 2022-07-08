# QuvaAPI
An Java-based API to use Quantumannealers

This API allows quantumannealing using Java. This API is made for people new to quantumannealing. It adds many features beyond basic manipulation of the hamilton matrix such as basic mathematical operations and variables. 
Quva can also let a simulation of the quantumannealer run and solve the QUBO-Problem as well as send the hamiltonmatrix to a real quantumannealer. Quva also allows reading the results and postprocessing them.
<details>
<summary>Examples</summary>
<details><summary>n-Queens Problem</summary>

```java

import quva.core.*;

public class QuvaMain extends QUBOMatrix{
	public QuvaMain() {
		//inits an empty matrix
		super(100);
		int n=6;

		//Each group of n (0 - n-1;n - 2n-1;...;n*n-n  - n*n-1) only has one qubit with the value 1
		for(int i=0;i<n;i++) limit(1,range(0+i*n,n-1 +i*n));
		//Each group that can be written like (i+0,i+n,i+2n,...,i+n*n-n) has exactly one qubit with the value one
		for(int i=0;i<n;i++) limit(1,range(0+i,n*n-n +i,n));

		//These conditions look if the queens are on the same diagonal. If so a punishment of one is added
		/*Same as 
		for(int i=0;i<n*n;i++) for(int j=0;j<n*n;j++) if(((i/n)-(i%n))==((j/n)-(j%n))&&i!=j) add(1,i,j);*/
		 applyRule(range(0,n*n-1),range(0,n*n-1),(i,j)-> ((i/n)-(i%n))==((j/n)-(j%n))&&i!=j, (i,j) ->1 );
		/*Same as 
		for(int i=0;i<n*n;i++) for(int j=0;j<n*n;j++) if(((i/n)+(i%n))==((j/n)+(j%n))&&i!=j) add(1,i,j);*/
		 applyRule(range(0,n*n-1),range(0,n*n-1),(i,j)-> ((i/n)+(i%n))==((j/n)+(j%n))&&i!=j, (i,j) ->1);

		//Runs the hamilton matrix
		int[] results=execute(SIMULATE);

		//prints the results
		for(int i=0;i<results.length;i++) {
		System.out.print(" "+results[i]);
		if(i%n==n-1)System.out.println("");
		}
	}
}
```

</details>
<details><summary>Traveling Salesman</summary>
This is the code required to solve the traveling Salesman problem:

```java

import quva.core.*;

public class QuvaMain extends QUBOMatrix{
	public QuvaMain() {
   		 //creates empty matrix with 100 qubits
		super(100);
    		//sets up the prorisation system for conditions (conditions with highter prority are weighted by the factor 10
    		// and the baseline is set at prority 0 rather than 1)
		init(10,false);
    		//The distances between the cities
		float[][] distances= {{0,3,4},{3,0,5},{4,5,0}};
		int n=distances.length;
   		 //Sets the priority of the conditions to follow to one (The higher the number the higher the priority)
		layer(1);
    		//groups the qubits in groups of n to make referencing them easier (the weights are added to make it easier to read the results)
		for(int i=0;i<n;i++) register("step"+i,n,fromIntArray(range(1,n)));
    		//Limits the merchant to visit only one city at a time
		for(int i=0;i<n;i++) limit(1,find("step"+i));
    		//Limits the merchant to visit a city only once
		for(int i=0;i<n;i++) limit(1,range(i,n*n-n+i,n));
   		 //Sets the prority to the baseline (wich was set to 0)
		layer(0);
   		 //Adds the travel costs
		for(int i=0;i<n;i++) pattern(distances,find("step"+i),find("step"+(i+1)%n));
		//prints the matrix
    		//System.out.println(this);
   		 //Simulates the hamilton-matrix
		int[] results=execute(SIMULATE);
   		 //reads the results
		System.out.println("The merchant visits the cities in the order: ");
		for(int i=0;i<n;i++) System.out.print((int)readVar(results,"step"+i)+" ");
	}
	float[] fromIntArray(int[] arr) {
		float[] ret=new float[arr.length];
		for(int i=0;i<arr.length;i++) ret[i]=arr[i];
		return ret;
	}
}
```
</details><details><summary>Solving equations</summary>
This is an example on how to solve the equation x^2+2x-1=9 -> x^2+2x-8=0

```java

import quva.core.*;

public class QuvaMain extends QUBOMatrix{
	public QuvaMain() {
		//inits the matrix
		super(100);
		init(10,false);
		//adds conditions with a priority of 1 (the higher the number the higher the priority)
		layer(1);
		//registers the variable x=-4q_0+2q_1+1q_2
		register("x",3,4,true);
		//registers xx as the product of x with itself
		registerMultiplyCarries("xx","x","x");
		layer(0);
		//adds the equation as a linear equation
		linearEquation("-8+2*x+1*xx");
		//prints out the hamilton matrix
		System.out.println(this);
		//executes the QUBO-Problem
		int ret[]=execute(SIMULATE);
		//reads x
		System.out.println("x="+readVar(ret,"x"));
	}
}
```
</details><details><summary>Primality Test</summary>
This program checks if a number is prime and if not it returns a p and q with n=p*q

```java

import quva.core.*;

public class QuvaMain extends QUBOMatrix{
	public QuvaMain() {
		super(100);
		init(10,false);
		int n=21;

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

		int[] returnvalues=execute(SIMULATE);
		println("");
		int p=(int)(readVar(returnvalues,"p")+1);
		int q=(int)(readVar(returnvalues,"q")+1);

		if(p*q==n) println("no prime number ("+n+"="+p+"*"+q+")");
		if(p*q!=n) println(n+" is a prime number");
	}
}
```
</details>
</details>

# Installation

<ol>
	<li>Download the latest release</li>
	<li>Unzip it</li>
	<li>In pythonSctiptsRoot/QuboFromAPI replace Insert Token Here with your API token to get access to the DWave solvers</li>
	<li>Add Quva.jar to your build path and make sure pythonScriptsRoot is in the same folder of Quva.jar</li>
</ol>

# Requirements

<ul>
	<li>Java 12 (or higher)</li>
<li>Python 3 (on Windows 64-bit)</li>
<li>Python libraries:</li>
<li>	ocean-sdk</li>
<li>	dwave-qbsolv</li>
</ul>
