package dwas.main;

import java.util.*;
import static dwas.main.Embedder.*;

public class QUBOEmbedder {
	public static double chainStrength=3;
	public static Map<int[], Double> embedQubo(float[][] matrix,String solver,String token,Map<Integer,List<Integer>> ch){
		Map<Tupel,Double> ht=new HashMap<Tupel,Double>();
		ch.clear();
		List<int[]> embedderEdges=new ArrayList<int[]>();
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix.length;j++)
				if(i!=j) if(matrix[i][j]!=0) 
					embedderEdges.add(new int[] {i,j});
		int[][] eedges=embedderEdges.toArray(new int[embedderEdges.size()][2]);
		//for(int[] edge:eedges) System.out.println(edge[0]+" "+edge[1]);
		int[][] edges=new QUBOProblem(0,new HashMap<int[],Double>(),token).getedges(solver);
		Map<Integer,List<Integer>> embedding=run_embedding(edges,eedges);
		ch.putAll(embedding);
		//implementing quadratic biases
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix.length;j++)
				if(matrix[i][j]!=0) if(i!=j) outerIteration:{
					List<Integer> embeddedI=embedding.get(i);
					List<Integer> embeddedJ=embedding.get(j);
					for(int bit:embeddedI) {
						for(int connection:internalNodes[bit])
							if(embeddedJ.contains(connection)) {
								increment(ht,new Tupel(bit,connection),matrix[i][j]);
								System.out.println(i+" "+j+" : "+bit+" "+connection);
								break outerIteration;
							}
					}
				}
				
		//implementing linear biases
		for(int i=0;i<matrix.length;i++) {
			List<Integer> embeddedI=embedding.get(i);
			double divisor=embeddedI.size();
			for(int bit:embeddedI) increment(ht, new Tupel(bit,bit), matrix[i][i]/divisor);
		}
		
		//implementing chains
		for(int i=0;i<matrix.length;i++) {
			List<Integer> embeddedI=embedding.get(i);
			for(int j:embeddedI) for(int k:internalNodes[j]) if(embeddedI.contains(k)){
				increment(ht, new Tupel(j,k), -chainStrength);
				increment(ht, new Tupel(j,j), chainStrength/2);
				increment(ht, new Tupel(k,k), chainStrength/2);
				//System.out.println(j+" "+k);
			}
		}
		Map<int[],Double> ret=new HashMap<int[],Double>();
		for(Tupel t:ht.keySet()) {
			ret.put(t.getVal(), ht.get(t));
		}
		return ret;
	}
	public static void increment(Map<Tupel,Double> ht,Tupel key,double val) {
		if(ht.containsKey(key)) ht.replace(key, ht.get(key)+val);
		else ht.put(key, val);
	}
}
