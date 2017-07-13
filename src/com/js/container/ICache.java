package com.js.container;

import java.util.Set;

public interface ICache<K, V> {
	public static abstract class Entry<K, V> {
		protected K mKey;
		protected V mValue;
		protected int mSize;
		
		protected Entry(K key, V value, int size) {
			mKey = key;
			mValue = value;
			mSize = size;
		}
		
		public K getKey() {
			return mKey;
		}
		
		public V getValue() {
			return mValue;
		}
		
		public int getSize() {
			return mSize;
		}
	}
	
	public int count();
	public void clear();

	public void setMaxSize(int max);
	public int getMaxSize();
	public int getCurrentSize();

	public boolean contains(K key);
	public void put(K key, V value, int size);
	public V get(K key);
	public boolean remove(K key);

	public Set<Entry<K, V>> entrys();
	public Set<K> keys();
	public Set<V> values();
}
