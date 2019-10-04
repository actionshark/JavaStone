package com.shk.js.thread;

public interface IExecutor {
	void run(Runnable runnable, long delay);
}
