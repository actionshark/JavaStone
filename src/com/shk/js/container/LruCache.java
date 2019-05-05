package com.shk.js.container;

import java.util.HashSet;
import java.util.Set;

public class LruCache<K, V> implements ICache<K, V> {
	public static class Entry<K, V> extends ICache.Entry<K, V> {
		private Entry<K, V> prev;
		private Entry<K, V> next;
		
		protected Entry(K key, V value, int size) {
			super(key, value, size);
		}
	}
	
	private final Entry<K, V> mHeader;
	private int mCount = 0;
	
	private int mCurSize = 0;
	private int mMaxSize = 1024 * 1024 * 16;
	
	public LruCache() {
		mHeader = new Entry<K, V>(null, null, 0);
		mHeader.prev = mHeader.next = mHeader;
	}
	
	@Override
	public int count() {
		return mCount;
	}

	@Override
	public void setMaxSize(int max) {
		mMaxSize = max;
		checkSize();
	}

	@Override
	public int getMaxSize() {
		return mMaxSize;
	}

	@Override
	public int getCurrentSize() {
		return mCurSize;
	}
	
	public void checkSize() {
		for (Entry<K, V> node = mHeader.prev; node != mHeader && mMaxSize < mCurSize; node = node.prev) {
			removeEntry(node);
		}
	}
	
	private boolean keyEquals(K a, K b) {
		if (a == null) {
			if (b == null) {
				return true;
			} else {
				return b.equals(a);
			}
		} else {
			return a.equals(b);
		}
	}
	
	private void putEntry(Entry<K, V> node) {
		node.prev = mHeader;
		node.next = mHeader.next;
		mHeader.next.prev = node;
		mHeader.next = node;
		
		mCount++;
		mCurSize += node.getSize();
	}
	
	private void removeEntry(Entry<K, V> node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
		
		mCount--;
		mCurSize -= node.getSize();
	}

	@Override
	public boolean contains(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.getKey(), key)) {
				removeEntry(node);
				putEntry(node);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void put(K key, V value, int size) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.getKey(), key)) {
				removeEntry(node);
				break;
			}
		}
		
		Entry<K, V> node = new Entry<K, V>(key, value, size);
		
		putEntry(node);
		checkSize();
	}

	@Override
	public V get(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.getKey(), key)) {
				removeEntry(node);
				putEntry(node);
				return node.getValue();
			}
		}
		
		return null;
	}

	@Override
	public boolean remove(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.getKey(), key)) {
				removeEntry(node);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void clear() {
		mHeader.prev = mHeader.next = mHeader;
		mCount = 0;
		mCurSize = 0;
	}

	@Override
	public Set<ICache.Entry<K, V>> entrys() {
		Set<ICache.Entry<K, V>> set = new HashSet<ICache.Entry<K, V>>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node);
		}
		
		return set;
	}

	@Override
	public Set<K> keys() {
		Set<K> set = new HashSet<K>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node.getKey());
		}
		
		return set;
	}

	@Override
	public Set<V> values() {
		Set<V> set = new HashSet<V>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node.getValue());
		}
		
		return set;
	}
}
