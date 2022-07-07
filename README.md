# QuvaAPI
An Java-based API to use Quantumannealers

This API allows quantumannealing using Java. This API is made for people new to quantumannealing. It adds many features beyond basic manipulation of the hamilton matrix such as basic mathematical operations and variables. 
Quva can also let a simulation of the quantumannealer run and solve the QUBO-Problem as well as send the hamiltonmatrix to a real quantumannealer. Quva also allows reading the results and postprocessing them.
<details>
<summary>Examples</summary>
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

</details>
</details>
