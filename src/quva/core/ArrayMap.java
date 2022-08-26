package quva.core;

import java.util.*;

/**This is a map recognizes arrays with the same content to be the same*/
public class ArrayMap<K, V> implements Map<K[], V> {
  /**Here a array is saved to have it as reference for the toArray method*/
  public K[] buffer2=null;
  /**Here the data is saved*/
  public Map<List<K>, V> mainMap=new HashMap<List<K>, V>();
  /**{@inheritDoc}*/
  @Override
    public int size() {
    return mainMap.size();
  }

  /**{@inheritDoc}*/
  @Override
    public boolean isEmpty() {
    return mainMap.isEmpty();
  }

@SuppressWarnings("unchecked")
@Override
/**{@inheritDoc}*/
    public boolean containsKey(Object obkey) {
	if (obkey.getClass().isArray()) {
      System.out.println("array detected");
      return mainMap.containsKey(Arrays.asList((K[]) obkey));
    }
    return false;
  }

	/**{@inheritDoc}*/
  @Override
    public boolean containsValue(Object value) {
    return mainMap.containsValue(value);
  }

  /**{@inheritDoc}*/
  @Override
    public V get(Object obkey) {
    if (obkey.getClass().isArray()) {
      return mainMap.get(Arrays.asList(obkey));
    }
    return null;
  }

  /**{@inheritDoc}*/
  @Override
    public V remove(Object obkey) {
    if (obkey.getClass().isArray()) {
      return mainMap.remove(Arrays.asList(obkey));
    }
    return null;
  }


  /**{@inheritDoc}*/
  @Override
    public void clear() {
    mainMap.clear();
  }

  /**{@inheritDoc}*/
  @Override
    public Set<K[]> keySet() {
    Map<K[], V> buffer=new HashMap<K[], V>();
    for (List<K> lst : mainMap.keySet()) buffer.put(lst.toArray(buffer2.clone()), null);
    return buffer.keySet();
  }

    /**{@inheritDoc}*/
  @Override
    public Collection<V> values() {
    return mainMap.values();
  }
  /**{@inheritDoc}*/
  @Override
    public V put(K[] objkey, V value) {
    if (buffer2==null) buffer2=objkey.clone();
    return mainMap.put(Arrays.asList(objkey), value);
  }

  /**{@inheritDoc}*/
  @Override
    public void putAll(Map<? extends K[], ? extends V> m) {
    for (K[] k:m.keySet()) put(k, m.get(k));
  }

  /**{@inheritDoc}*/
  @Override
    public Set<Entry<K[], V>> entrySet() {
    Map<K[], V> buffer=new HashMap<K[], V>();
    for (List<K> lst:mainMap.keySet()) buffer.put(lst.toArray(buffer2.clone()), null);
    return buffer.entrySet();
  }
}
