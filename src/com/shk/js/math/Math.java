package com.shk.js.math;

public class Math {
	public static int max(int... params) {
		int value = Integer.MIN_VALUE;

		for (int p : params) {
			if (value < p) {
				value = p;
			}
		}

		return value;
	}

	public static long max(long... params) {
		long value = Long.MIN_VALUE;

		for (long p : params) {
			if (value < p) {
				value = p;
			}
		}

		return value;
	}

	public static float max(float... params) {
		float value = -Float.MAX_VALUE;

		for (float p : params) {
			if (value < p) {
				value = p;
			}
		}

		return value;
	}

	public static double max(double... params) {
		double value = -Double.MAX_VALUE;

		for (double p : params) {
			if (value < p) {
				value = p;
			}
		}

		return value;
	}

	public static int min(int... params) {
		int value = Integer.MAX_VALUE;

		for (int p : params) {
			if (value > p) {
				value = p;
			}
		}

		return value;
	}

	public static long min(long... params) {
		long value = Long.MAX_VALUE;

		for (long p : params) {
			if (value > p) {
				value = p;
			}
		}

		return value;
	}

	public static float min(float... params) {
		float value = Float.MAX_VALUE;

		for (float p : params) {
			if (value > p) {
				value = p;
			}
		}

		return value;
	}

	public static double min(double... params) {
		double value = Double.MAX_VALUE;

		for (double p : params) {
			if (value > p) {
				value = p;
			}
		}

		return value;
	}

	public static int sum(int... params) {
		int sum = 0;

		for (int p : params) {
			sum += p;
		}

		return sum;
	}

	public static long sum(long... params) {
		long sum = 0;

		for (long p : params) {
			sum += p;
		}

		return sum;
	}

	public static float sum(float... params) {
		float sum = 0;

		for (float p : params) {
			sum += p;
		}

		return sum;
	}

	public static double sum(double... params) {
		double sum = 0;

		for (double p : params) {
			sum += p;
		}

		return sum;
	}
	
	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}
	
	public static long clamp(long value, long min, long max) {
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}
	
	public static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}
	
	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}
}
