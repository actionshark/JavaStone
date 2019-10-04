package com.shk.js.container;

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

	int count();

	void clear();

	void setMaxSize(int max);

	int getMaxSize();

	int getCurrentSize();

	boolean contains(K key);

	void put(K key, V value, int size);

	V get(K key);

	boolean remove(K key);

	Set<Entry<K, V>> entrys();

	Set<K> keys();

	Set<V> values();
}
