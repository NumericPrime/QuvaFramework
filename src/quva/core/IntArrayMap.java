package quva.core;

import java.util.*;
import java.util.stream.Collectors;

/**This map takes int[] as keys and recognizes equal content as equal keys. This means you can e.g get a value from the map when you use an int[] with the same content as the original key.*/
public class IntArrayMap<V> implements Map<int[], V> {
	Map<List<Integer>,V> main=new HashMap<List<Integer>,V>();
	/**{@inheritDoc}*/
	@Override
	public int size() {
		return main.size();
	}

	/**{@inheritDoc}*/
	@Override
	public boolean isEmpty() {
		return main.isEmpty();
	}

	/**{@inheritDoc}*/
	@Override
	public boolean containsKey(Object key) {
		List<Integer> cont=Arrays.stream((int[])key).boxed().collect(Collectors.toList());
		
		return main.containsKey(cont);
	}

	/**{@inheritDoc}*/
	@Override
	public boolean containsValue(Object value) {
		return main.containsValue(value);
	}

	/**{@inheritDoc}*/
	@Override
	public V get(Object key) {
		List<Integer> cont=Arrays.stream((int[])key).boxed().collect(Collectors.toList());
		return  main.get(cont);
	}

	/**{@inheritDoc}*/
	@Override
	public V put(int[] key, V value) {
		List<Integer> cont=Arrays.stream((int[])key).boxed().collect(Collectors.toList());
		
		return main.put(cont,value);
	}

	/**{@inheritDoc}*/
	@Override
	public V remove(Object key) {
		List<Integer> cont=Arrays.stream((int[])key).boxed().collect(Collectors.toList());
		return main.remove(cont);
	}

	/**{@inheritDoc}*/
	@Override
	public void putAll(Map<? extends int[], ? extends V> m) {
		for(int[] array:m.keySet()) put(array,m.get(array));
	}

	/**{@inheritDoc}*/
	@Override
	public void clear() {
		main.clear();
	}
	/**{@inheritDoc}*/
	@Override
	public Set<int[]> keySet() {
		Map<int[],V> buffer=new HashMap<int[],V>();
		for(List<Integer> lst:main.keySet()) buffer.put(convert(lst), main.get(lst));
		return buffer.keySet();
	}

	/**{@inheritDoc}*/
	@Override
	public Collection<V> values() {
		return main.values();
	}

	/**{@inheritDoc}*/
	@Override
	public Set<Entry<int[], V>> entrySet() {
		Map<int[],V> buffer=new HashMap<int[],V>();
		for(List<Integer> lst:main.keySet()) buffer.put(convert(lst), main.get(lst));
		
		return buffer.entrySet();
	}
	/**This method converts an {@code Integer} list into an int array.
	 * @param lst list to be processed
	 * @return the list as an array*/
	public int[] convert(List<Integer> lst) {
		return lst.stream().mapToInt(Integer::intValue).toArray();
	}
}
