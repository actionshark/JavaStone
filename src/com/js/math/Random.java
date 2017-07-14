package com.js.math;

public class Random extends java.util.Random {
	private static final long serialVersionUID = 1L;
	
	// TODO: may exceed
	
	public boolean nextBoolean(float prob) {
		return nextFloat() < prob;
	}
	
	public int nextInt(int min, int max) {
		long dis = max - min;
		long add = nextLong() % dis;
		if (add < 0) {
			add += dis;
		}
		
		long ret = min + add;
		
		return (int) ret;
	}
	
	public long nextLong(long min, long max) {
		long dis = max - min;
		long add = nextLong() % dis;
		if (add < 0) {
			add += dis;
		}
		
		return min + add;
	}
	
	public float nextFloat(float min, float max) {
		return min + nextFloat() * (max - min);
	}
	
	public double nextDouble(double min, double max) {
		return min + nextDouble() * (max - min);
	}
}
