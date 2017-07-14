package com.js.thread;

public interface IExecutor {
	public void run(Runnable runnable, long delay);
}
