package dwas.main;
import java.util.*;

public class Embedder {
	public static DijikstraImplementation dimpl;
	public static Map<Integer,List<Integer>> run_embedding(int[][] edges,int[][] eedges){
		
		if(quva.core.QuvaExecutionSettings.useNative) return run_native_embedding(edges,eedges);
		System.err.println("WARNING: Using the non-native implementation! This version is highly unstable.");
		int maxnode=0;
		int maxemnode=0;
		for(int[] edge:edges) for(int n:edge) if(maxnode<n) maxnode=n;
		for(int[] edge:eedges) for(int n:edge) if(maxemnode<n) maxemnode=n;
		//System.out.println(maxnode+1);
		//System.out.println(maxemnode+1);
		bake(maxnode+1,maxemnode+1,edges,eedges);
		dimpl=new DijikstraImplementation(maxnode+1,internalNodes);
		return embed(maxnode+1,maxemnode+1,edges,eedges);
	}
	public static Map<Integer,List<Integer>> run_native_embedding(int[][] edges,int[][] eedges){
		int maxnode=0;
		int maxemnode=0;
		for(int[] edge:edges) for(int n:edge) if(maxnode<n) maxnode=n;
		for(int[] edge:eedges) for(int n:edge) if(maxemnode<n) maxemnode=n;
		//System.out.println(maxnode+1);
		//System.out.println(maxemnode+1);
		bake(maxnode+1,maxemnode+1,edges,eedges);
		int[][] ined=inverted(edges);
		int[][] ineed=inverted(eedges);
		return MinorMinerImplementation.createEmbedding(maxnode+1, maxemnode+1, ined[0], ined[1], ineed[0], ineed[1]);
		//dimpl=new DijikstraImplementation(maxnode+1,internalNodes);
		//return embed(maxnode+1,maxemnode+1,edges,eedges);
	}
	public static int[][] inverted(int[][] edges){
		int[][] ret=new int[2][edges.length];
		for(int i=0;i<edges.length;i++) {
			ret[0][i]=edges[i][0];
			ret[1][i]=edges[i][1];
		}
		return ret;
	}
	public static Map<Integer,List<Integer>> embed(int nodes, int enodes, int[][] edges, int[][] eedges) {
		  long diam=/*calcDiam(nodes, edges)*/100;
		  int stage=1;
		  Map<Integer, List<Integer>> st=new HashMap<Integer, List<Integer>>();

		  int olds=Integer.MAX_VALUE;
		  int oldv=Integer.MAX_VALUE;
		  for (int i=0; i<enodes; i++) {
		    st.put(i, new LinkedList<Integer>());
		  }
		  //System.out.println(st);
		  boolean improved=false;

		  List<Integer> shuffled=new ArrayList<Integer>();
		  for (int i=0; i<enodes; i++) shuffled.add(i);
		  Collections.shuffle(shuffled);

		  do {
		    int[] allBits=new int[nodes];
		    for (int i=0; i<enodes; i++) {
		      findAllBits(allBits, st);
		      long[] weights=new long[nodes];
		      for (int j=0; j<nodes; j++) {
		        	weights[j]=(long) Math.pow(diam, allBits[j]);
		        	//System.out.println(weights);
		      }
		      st.replace(shuffled.get(i), findMinVertexM(nodes, weights, st, internalEmNodes[shuffled.get(i)], edges));
		    }

		    stage++;
		    improved=false;
		    if (stage>=2) {
		      findAllBits(allBits, st);
		      int sz=0;
		      for(int bit: allBits) if(bit>0) sz++;
		      int s=0;
		      if (oldv>(s=sz)) {
		        improved=true;
		      }
		      oldv=s;
		      s=0;
		      for (int val : allBits) if (s<val) s=val;
		      if (olds>s) {
		        improved=true;
		      }
		      olds=s;
		      //System.out.println(oldv+" "+olds);
		    }
		    //System.out.println(st);
		  } while (stage<=2||improved);
		  return st;
		}
	public static List<Integer> findJoined(int[][] edges, int node) {
		List<Integer> ret=new LinkedList<Integer>();
	  for (int[] edge : edges) {
	    if (edge[0]==node) ret.add(edge[1]);
	    if (edge[1]==node) ret.add(edge[0]);
	  }
	  return ret;
	}

	public static List<Integer> findMinVertexM(int nodes, long[] w, Map<Integer, List<Integer>> data, int[] joined, int[][] edges) {
	  List<Integer> ret=new LinkedList<Integer>();
	  Set<Integer> bits=new HashSet<Integer>();
	  for (int v : joined) for (int bit : data.get(v)) bits.add(bit);
	  if (bits.isEmpty()) {
	    ret.add((int)(Math.random()*(double)nodes));
	    return ret;
	  }
	  /*System.out.println("Connections");
	  for(int v:joined) System.out.print(v+" ");
	  System.out.println("\n");*/
	  //long[] weightsCopy=Arrays.copyOf(w, nodes);
	  //int c2;
	  //List<Integer> bestVariant=null;
	  //int bestScore=Integer.MAX_VALUE;
	  List<List<Integer>> collectedOj=new LinkedList<List<Integer>>();
	  List<Integer> collect=new LinkedList<Integer>();
	  for (int j : joined) {
	    List<Integer> oj=data.get(j);
	    if (oj.isEmpty()) continue;
	    collectedOj.add(oj);
	  }
      List<int[]> collection2=new LinkedList<int[]>();
      int mappedScore[]=new int[nodes];
      for(List<Integer> o:collectedOj) {
          int path[]=new int[nodes];
          collection2.add(path);
          int sc[]=new int[nodes];
    	  int src=o.get(0);
    	  dimpl.dijkstra(src, w, collectedOj, path, sc);
    	  //System.out.println();
    	  //System.out.println("\nIteration: "+src);
    	  //for(int i=0;i<nodes;i++) System.out.print(sc[i]+" ");
    	  //for(int i=0;i<nodes;i++) if(mappedScore[i]==0&&sc[i]==0) System.out.println("Error at "+i);
    	  for(int i=0;i<nodes;i++) if(sc[i]!=Integer.MAX_VALUE&&mappedScore[i]!=Integer.MAX_VALUE) mappedScore[i]+=sc[i];
    	  else mappedScore[i]=Integer.MAX_VALUE;
      }
      int g=0;
      int minDist=Integer.MAX_VALUE;
      //System.out.println(collectedOj);
	  for(List<Integer> o:collectedOj) for(int n:o) {
		  mappedScore[n]+=(int) w[n];
		  if(w[n]>Integer.MAX_VALUE&&mappedScore[n]==Integer.MAX_VALUE) mappedScore[n]=Integer.MAX_VALUE;
		  //System.out.println(n+" "+w[n]+" "+mappedScore[n]);
		  }
	  //System.out.println();
	  //for(int i=0;i<nodes;i++) System.out.print(mappedScore[i]+" ");
	  for(int i=0;i<nodes;i++) { 
		  //System.out.print(mappedScore[i]+" ");
		  if(internalNodes[i].length>0) if(minDist>mappedScore[i]) {
			  //System.out.println(i+" "+mappedScore[i]);
			  g=i;
			  minDist=mappedScore[i];
		  }
	  }
	  //System.out.println(g);
	  boolean lock[]=new boolean[nodes];
	  for(List<Integer> o:collectedOj) for(int n:o) lock[n]=true;
	  for(int[] collected:collection2) {
		  int read=g;
		  while(!lock[read]) {
			  collect.add(read);
			  read=collected[read];
		  }
	  }
	  collect.add(g);
	  collect=new LinkedList<Integer>(new HashSet<Integer>(collect));
	  //System.out.println("iterationDone ");
	  /*
	  int min=Integer.MAX_VALUE;
	  int bg=0;
	  int ref=0;
	  for (int g=0;g<c2.length;g++) if (min>(ref=c2[g])) {
	    bg=g;
	    min=ref;
	  }*/
	return collect;
	}
	public static void findAllBits(int[] allBits, Map<Integer, List<Integer>> st) {
	  for(int i=0;i<allBits.length;i++) allBits[i]=0;
	  for (List<Integer> vals : st.values()) for (int bit : vals) {
		  allBits[bit]++;
	  }
	}
	public static int[][] internalNodes;
	public static int[][] internalEmNodes;
	public static void bake(int nodes,int enodes,int[][] edges,int[][] eedges) {
		List<Integer>[] internalN=new LinkedList[nodes];
		for(int i=0;i<nodes;i++) internalN[i]=new LinkedList<Integer>();
		for(int[] edge:edges) {
			internalN[edge[0]].add(edge[1]);
			internalN[edge[1]].add(edge[0]);
			//System.out.println(internalN[edge[0]]+" "+edge[1]);
			//System.out.println(internalN[edge[1]]+" "+edge[0]);
		}
		internalNodes=new int[nodes][];
		for(int i=0;i<nodes;i++) internalNodes[i]=internalN[i].stream().mapToInt(x->x).toArray();
		
		internalN=new LinkedList[enodes];
		for(int i=0;i<enodes;i++) internalN[i]=new LinkedList<Integer>();
		for(int[] edge:eedges) {
			internalN[edge[0]].add(edge[1]);
			internalN[edge[1]].add(edge[0]);
		}
		internalEmNodes=new int[enodes][];
		for(int i=0;i<enodes;i++) internalEmNodes[i]=internalN[i].stream().mapToInt(x->x).toArray();
	}
}
	/*public static int findpath2(int stc, int nodes, int edges[][], long[] w, List<Integer> oj, List<Integer> collect) {
      System.out.println("Begin: "+System.currentTimeMillis());
	  int distN[]=new int[nodes];
	  for (int i=0; i<distN.length; i++) {
	    distN[i]=Integer.MAX_VALUE;
	  }
	  int iterationlimit=500;
	  distN[stc]=0;
	  boolean scorched[]=new boolean[nodes];
	  boolean burning[]=new boolean[nodes];
	  for (int i=0; i<scorched.length; i++) scorched[i]=false;
	  for (int i=0; i<burning.length; i++) burning[i]=false;
	  boolean allc=true;
	  burning[stc]=true;
	  int[] previous=new int[nodes];
	  previous[stc]=stc;
	  int buffer=0;
	  int scorchedc=0;
	  boolean[] included=new boolean[nodes];
	  for(int i=0;i<included.length;i++) included[i]=true;
	  for(int excluded:oj) included[excluded]=false;
	  
	  //for(int i:oj) scorched[i]=true;
	  do {
	    for (int i=0; i<nodes; i++) if (!scorched[i]&&burning[i]) {
	      for (int j : internalNodes[i]) if (!scorched[j]) 
	        if (distN[j]>(buffer=distN[i]+
	          (included[j]?(int)(long)w[j]:0)
	          )) {
	          burning[j]=true;
	          System.out.println("burn "+j);
	          previous[j]=i;
	          distN[j]=buffer;
	        }
	      scorched[i]=true;
	      System.out.println(++scorchedc);
	    }
	    allc=false;
	    for (int i=0;i<scorched.length&&!allc;i++)allc=(!scorched[i]&&burning[i]);
	  } while (!allc);
      System.out.println("Postproc start: "+System.currentTimeMillis());
	  int min=Integer.MAX_VALUE;
	  int ind=0;
	  for (int n : oj) if (min>distN[n]) {
	    ind=n;
	    min=distN[n];
	  }
	  included[stc]=false;
	  while(ind!=stc) {
		  if(included[previous[ind]]) collect.add(ind=previous[ind]);
		  else ind=previous[ind];
	      System.out.println("collect "+ind);
	  }
      System.out.println("return "+System.currentTimeMillis());
	  return distN[ind];
	}*/
	/*public static java.util.List<Integer> forbidden=new java.util.LinkedList<Integer>();
	public static int dijkstra(int stc, int nodes, int edges[][]) {
	  int distN[]=new int[nodes];
	  for (int i=0; i<distN.length; i++) distN[i]=Integer.MAX_VALUE;
	  boolean check;
	  java.util.Stack<Integer> newfound=new java.util.Stack<Integer>();
	  newfound.push(stc);
	  distN[stc]=0;
	  do {
	    while (!newfound.isEmpty()) {
	      int currnode=newfound.pop();
	      for (int[] nd : edges) {
	        if (nd[0]==currnode) {
	          if (distN[nd[1]]==Integer.MAX_VALUE) newfound.push(nd[1]);
	          distN[nd[1]]=Math.min(distN[nd[1]], distN[currnode]+1);
	        }
	        if (nd[1]==currnode) {
	          if (distN[nd[0]]==Integer.MAX_VALUE) newfound.push(nd[0]);
	          distN[nd[0]]=Math.min(distN[nd[0]], distN[currnode]+1);
	        }
	      }
	    }
	    check=true;
	    for (int i=0; i<distN.length; i++) if (distN[i]==Integer.MAX_VALUE) check=false;
	  } while (!check);
	  int maxd=0;
	  int nodemax=stc;
	  for (int i=0; i<nodes; i++) if (maxd<distN[i]) {
	    maxd=distN[i];
	    nodemax=i;
	  }
	  forbidden.add(nodemax);
	  return maxd;
	}
			public static int calcDiam(int nodes, int[][] edges) {
			  java.util.Stack<Integer> vals=new java.util.Stack<Integer>();
			  for (int i=0; i<nodes; i++) 
			  {
			    if (forbidden.contains(i)) continue;
			    vals.push(dijkstra(i, nodes, edges));
			  }
			  int max=vals.pop();
			  while (!vals.isEmpty())  if (max<vals.peek()) max=vals.pop(); 
			  else vals.pop();
			  return max;
			}
}*/
/*
	public static List<Integer> findMinVertexM(int nodes, long[] w, Map<Integer, List<Integer>> data, int[] joined, int[][] edges) {
	  List<Integer> ret=new LinkedList<Integer>();
	  Set<Integer> bits=new HashSet<Integer>();
	  for (int v : joined) for (int bit : data.get(v)) bits.add(bit);
	  if (bits.isEmpty()) {
	    ret.add((int)(Math.random()*(double)nodes));
	    return ret;
	  }
	  long[] weightsCopy=Arrays.copyOf(w, nodes);
	  int c2;
	  List<Integer> bestVariant=null;
	  int bestScore=Integer.MAX_VALUE;
	  mainLoops: for (int g=0; g<nodes; g++) { 
		if(internalNodes[g].length==0) {
			continue;
		}
	    c2=0;
	    List<List<Integer>> collectedOj=new LinkedList<List<Integer>>();
	    List<Integer> collect=new LinkedList<Integer>();
	    for (int j : joined) {
	      List<Integer> oj=data.get(j);
	      if (oj.isEmpty()) continue;
	      if (oj.contains(g)) {
	    	  c2+=(int)w[g];
	        continue;
	      }
	      collectedOj.add(oj);
	      }/*
	      List<int[]> collection2=new LinkedList<int[]>();
	      int newCol[]=new int[nodes];
	      collection2.add(newCol);
	      c2+=dimpl.dijkstra(g, w, collectedOj, collect,newCol);
	      for(List<Integer> oj:collectedOj) for(int k:oj) w[k]=weightsCopy[k];

	    if(bestScore>c2) {
		    collect.add(g);
	    	bestVariant=collect;
	    	bestScore=c2;
	    }
	    if(c2==1) {
	    	bestVariant=collect;
	    	bestScore=1;
	    	break mainLoops;
	    	}
	    *//*
	  }
      List<int[]> collection2=new LinkedList<int[]>();
      int newCol[]=new int[nodes];
      collection2.add(newCol);
	  //System.out.println("iterationDone ");
	  /*
	  int min=Integer.MAX_VALUE;
	  int bg=0;
	  int ref=0;
	  for (int g=0;g<c2.length;g++) if (min>(ref=c2[g])) {
	    bg=g;
	    min=ref;
	  }*//*
	return bestVariant;
	}*/
