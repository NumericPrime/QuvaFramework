package quva.construct;

import java.util.*;

import quva.core.QuvaDebug;

/**Here the assigned qubtis for the combinations of qubits are saved. It works essentially like a HashMap&lt;boolean[],Integer&gt; with the difference that it recognizes arrays with the same contents as key.*/
public class QuvaConstructRegister extends HashMap<List<Boolean>,Integer> implements QuvaConstruct{

	/**{@inheritDoc}*/
	public QuvaConstructRegister() {
		super();
	}
	/**Works like the put function in a Map
	 * @param key key to be used
	 * @param val value to be used
	 * @see java.util.HashMap#put(Object, Object)*/
	public void put(boolean[] key,int val) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		//System.out.println(created);
		QuvaDebug.logprnt("ConstructRegister.put","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
		put(created,val);
	}
	/**Works like the get function in a Map
	 * @return the value associated with key
	 * @param key key to be used
	 * @see java.util.HashMap#get(Object)*/
	public int get(boolean[] key) {
		List<Boolean> created=new LinkedList<Boolean>();
		for(boolean bval:key) created.add(bval);
		QuvaDebug.logprnt("ConstructRegister.get","Used get on:","\n"+getClass().getSimpleName(),hashCode()+"",created);
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
			QuvaDebug.logprnt("ConstructRegister.getKeys","Used getKeys on:","\n"+getClass().getSimpleName(),hashCode()+"");
			@SuppressWarnings("unchecked")
			List<Boolean> boolList=(List<Boolean>) setList[i];
			ret[i]=new boolean[boolList.size()];
			//System.out.println(boolList.size());
			Boolean[] nonPrimArray=boolList.toArray(new Boolean[boolList.size()]);
			for(int j=0;j<boolList.size();j++) {
				ret[i][j]=nonPrimArray[j];
				QuvaDebug.logprnt("ConstructRegister.getKeys",ret[i][j]+" ");
				//System.out.print(ret[i][j]+" ");
				
			}
		}
		return ret;
	}
	/**Converts a array of Booleans in a array of the primitives
	 * @return array of boolean[]
	 * @param arr Boolean[] array to be converted*/
	public static boolean[] convert(Boolean[] arr) {
		boolean[] ret=new boolean[arr.length];
		for(int i=0;i<ret.length;i++) ret[i]=arr[i];
		return ret;
	}
	/**Converts a list of Booleans in a array of the primitives
	 * @return array of boolean[]
	 * @param arr List&lt;Boolean&gt; array to be converted*/
	public static boolean[] convert(List<Boolean> arr) {
		boolean[] ret=new boolean[arr.size()];
		for(int i=0;i<ret.length;i++) ret[i]=arr.get(i);
		return ret;
	}
	/**This implementation returns the object itself
	 * @see quva.construct.QuvaConstruct#getRegistry()*/
	@Override
	public QuvaConstructRegister getRegistry() {
		return this;
	}
}
