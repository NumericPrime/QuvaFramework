package dwas.main;
import java.util.*;
public class DijikstraImplementation {
	int nodes;
	int[][] internalNodes;
	List<Node>[] internalNodesNd;
	Node nds[];
	public DijikstraImplementation(int nodes,int[][] internalNodes) {
		this.nodes=nodes;
		this.internalNodes=internalNodes;
		nds=new Node[nodes];
		for(int i=0;i<nds.length;i++) nds[i]=new Node(i);
		internalNodesNd=new ArrayList[nodes];
		for(int i=0;i<internalNodesNd.length;i++) {
			internalNodesNd[i]=new ArrayList<Node>();
			for(int j=0;j<internalNodes[i].length;j++) internalNodesNd[i].add(nds[internalNodes[i][j]]);
		}
	}
	public PriorityQueue<Node> pq=new PriorityQueue<Node>();
	public int dijkstra(int src,long[] wa,List<List<Integer>> oj,int[] precede,int[] distNode) {
		for(Node n:nds) n.dist=Integer.MAX_VALUE;
		//for(List<Node> n:internalNodesNd) System.out.println(n);
		//System.out.println("Neighbours of "+src);
		//for(Node n:internalNodesNd[src]) System.out.print(n.l+" ");
		//System.out.println();
		//int[] distNode=new int[nodes];
		boolean settled[]=new boolean[nodes];
		for(int i=0;i<distNode.length;i++) distNode[i]=Integer.MAX_VALUE;
		distNode[src]=0;
		precede[src]=src;
		//w=Arrays.copyOf(w, nodes);
		int identity[]=new int[nodes];
		for(int i=0;i<oj.size();i++) {
			for(int j:oj.get(i)) identity[j]+=i+1;
		}
  	  	//System.out.println("\nIdentities: "+src);
  	  	//System.out.println(oj);
  	  	//for(int i=0;i<nodes;i++) System.out.print(identity[i]+" ");
		long[] w=Arrays.copyOf(wa, nodes);
		for(List<Integer> l:oj) for(int n:l) w[n]=0;
		pq.add(nds[src]);
		nds[src].dist=0;
		int newDist=0;
		int u=0;
		while(!pq.isEmpty()) {
			u=pq.remove().l;
			if(settled[u]) continue;
			settled[u]=true;
			for(Node neighbour:internalNodesNd[u]) {
				//System.out.println("LookAtNode "+neighbour.l);
				int npos=neighbour.l;
				if(settled[npos]) continue;
				newDist=(int) (distNode[u]+(identity[npos]==0||identity[u]==identity[npos]?w[u]:100000));
				//System.out.println(u+"-"+neighbour+" Comparing "+newDist+" "+distNode[neighbour.l]);
				if(newDist<distNode[npos]&&newDist>=0) {
					precede[npos]=u;
					distNode[npos]=neighbour.dist=newDist;
				}
				//System.out.println(" "+u+"-"+neighbour+" Done "+newDist+" "+distNode[neighbour.l]);
				
				
				pq.add(neighbour);
			}
			//boolean breakof=false;
			//if(breakof=settled[firstOj]) for(int n:internalNodes[firstOj]) if(!settled[n]) breakof=false;
			
			/*if(breakof) {
				pq.clear();
				}*/
		}
		return 0;
		
	}
	boolean checkOneTrue(boolean[] arr) {
		boolean ret=false;
		lp: for(boolean val:arr) if(ret) break lp; else ret=val;
		return ret;
	}
	class Node implements Comparable<Node>{
		public int dist=Integer.MAX_VALUE;
		public int l;
		Node(int l){
			this.l=l;
		}
		@Override
		public int compareTo(Node o) {
			return dist>o.dist?1:-1;
		}
		@Override 
		public String toString() {
			return "Node("+l+","+dist+")";
		}
	}
}
