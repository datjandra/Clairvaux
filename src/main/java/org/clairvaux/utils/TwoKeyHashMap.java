package org.clairvaux.utils;

import java.util.HashMap;

@SuppressWarnings("serial")
public class TwoKeyHashMap<K1, K2, V> extends HashMap<Pair<K1, K2>, V> {

	public V get(K1 first, K2 second) {
		return super.get(new Pair<K1, K2>(first, second));
	}
	
	public boolean containsKey(K1 first, K2 second) {
		return super.containsKey(new Pair<K1, K2>(first, second));
	}
	
	public void put(K1 first, K2 second, V value) {
		super.put(new Pair<K1, K2>(first, second), value);
	}
	
	public V remove(K1 first, K2 second, V value) {
		return super.remove(new Pair<K1, K2>(first, second));
	}
}
