package dwas.main;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


public class MinorMinerImplementation {
	static File execScriptsRoot=quva.core.ExecuteProgram.execScriptsRoot;
	public static void loadNative(String path) {
		quva.core.QuvaExecutionSettings.useNative=true;
		System.load(new File(execScriptsRoot,"native.dll").getAbsolutePath());
	}
	public native int[][] find_Embedding(int n1,int n2,int[] nodes1,int[] nodes2,int[] nodes3,int[] nodes4);
	public static Map<Integer,List<Integer>> createEmbedding(int n1,int n2,int[] nodes1,int[] nodes2,int[] nodes3,int[] nodes4){
		Map<Integer,List<Integer>> ret=new HashMap<Integer,List<Integer>>();
		//System.out.println("beginning embedding");
		int[][] res=(new MinorMinerImplementation()).find_Embedding(n2,n1,nodes3,nodes4,nodes1,nodes2);
		//System.out.println("embedding done");
		for(int i=0;i<res.length;i++) ret.put(i, Arrays.stream(res[i]).boxed().collect(Collectors.toList()));
		return ret;
	}
}
