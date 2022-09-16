package quva.construct;

import java.util.*;

import quva.core.QuvaDebug;

/**Here the assigned weights for the combinations of qubits are saved. It is used to implement arbitrary functions. It works essentially like a HashMap&lt;boolean[],Float&gt; with the difference that it recognizes arrays with the same contents as key.*/
public class QuvaConstructWeight extends HashMap<List<Boolean>,Float>{
	/**{@inheritDoc}*/
	public QuvaConstructWeight() {
		super();
	}

	/**Works like the put function in a Map
	 * @param key key to be used
	 * @param val value to be used
	 * @see java.util.HashMap#put(Object, Object)*/
	public void put(boolean[] key,float val) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		//System.out.println(created);
		QuvaDebug.logprnt("QuvaConstructWeight.put","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
		put(created,val);
	}
	/**Works like the get function in a Map
	 * @return the value associated with key
	 * @param key key to be used
	 * @see java.util.HashMap#get(Object)*/
	public float get(boolean[] key) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		QuvaDebug.logprnt("QuvaConstructWeight.get","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
		return get(created);
	}
	/**Works like the containsKey function in a Map
	 * @return true when the key is recognized, false if not
	 * @param key key to be used
	 * @see java.util.HashMap#containsKey(Object)*/
	public boolean containsKey(boolean[] key) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		QuvaDebug.logprnt("QuvaConstructWeight.get","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
		return containsKey(created);
	}
	/**Works like the remove function in a Map
	 * @param key key to be used
	 * @see java.util.HashMap#remove(Object)*/
	public void remove(boolean[] key) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		QuvaDebug.logprnt("QuvaConstructWeight.get","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
		remove(created);
	}
	/**Works like the keySet function in a Map it just returns a boolean[][] instead a Set&lt;List&lt;Boolean&gt;&gt; containing the same data.
	 * @see java.util.HashMap#keySet()
	 * @return all keys*/
	public boolean[][] getKeys() {
		Set<List<Boolean>> currkeySet=keySet();
		Object[] setList=	currkeySet.toArray();
		boolean[][] ret=new boolean[currkeySet.size()][1];
		for(int i=0;i<setList.length;i++) {
			QuvaDebug.logprnt("QuvaConstructWeight.getKeys","Used getKeys on:","\n"+getClass().getSimpleName(),hashCode()+"");
			@SuppressWarnings("unchecked")
			List<Boolean> boolList=(List<Boolean>) setList[i];
			ret[i]=new boolean[boolList.size()];
			//System.out.println(boolList.size());
			Boolean[] nonPrimArray=boolList.toArray(new Boolean[boolList.size()]);
			for(int j=0;j<boolList.size();j++) {
				ret[i][j]=nonPrimArray[j];
				QuvaDebug.logprnt("QuvaConstructWeight.getKeys",ret[i][j]+" ");
				//System.out.print(ret[i][j]+" ");
				
			}
		}
		return ret;
	}
}
