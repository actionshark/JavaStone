package com.js.thread;

public class ThreadHandler {
	public static enum Status {
		Running, Finished, Cancelled,
	}
	
	protected Status mStatus = Status.Running; 
	
	protected boolean mTryCancel = false;
	
	protected Runnable mRunnable;
	
	protected ThreadHandler() {
	}
	
	public synchronized Status getStatus() {
		return mStatus;
	}
	
	public synchronized void cancel() {
		mTryCancel = true;
	}
}
