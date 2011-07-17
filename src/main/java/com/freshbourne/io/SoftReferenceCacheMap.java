/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SoftReferenceCacheMap<K, V> implements Map<K, V> {
	
	private Map<K, Reference<V>> refs = new HashMap<K, Reference<V>>();
	
	
	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		refs.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		if(refs.containsKey(key)){
			if(refs.get(key).get() == null){
				refs.remove(key);
			}
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		V val;
		for(K key : refs.keySet()){
			if((val = refs.get(key).get()) == null){
				refs.remove(key);
			}
			
			if(val.equals(value))
				return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException("entrySet is not supported in this CacheMap");
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		return refs.containsKey(key) ? refs.get(key).get() : null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		for(K key : refs.keySet()){
			if(refs.get(key).get() == null){
				refs.remove(key);
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet() {
		removeNullRefs();
		return refs.keySet();
	}
	
	private void removeNullRefs(){
		for(K key : refs.keySet()){
			if(refs.get(key).get() == null)
				refs.remove(key);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		Reference<V> result = refs.put(key, new SoftReference<V>(value));
		return result == null ? null : result.get();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		Reference<V> result = refs.remove(key);
		return result == null ? null : result.get();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		removeNullRefs();
		return refs.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values() {
		Collection<V> result = new ArrayList<V>();
		V value;
		for(K key : refs.keySet()){
			if((value = refs.get(key).get()) == null)
				refs.remove(key);
			else
				result.add(value);
		}
		return result;
	}

}
