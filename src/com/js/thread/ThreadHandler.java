package com.js.thread;

public class ThreadHandler {
	public static enum Status {
		Running, Finished, Cancelled,
	}
	
	Status mStatus = Status.Running; 
	
	boolean mTryCancel = false;
	
	Runnable mRunnable;
	
	public ThreadHandler() {
	}
	
	public synchronized void setStatus(Status status) {
		mStatus = status;
	}
	
	public synchronized Status getStatus() {
		return mStatus;
	}
	
	public synchronized void cancel() {
		mTryCancel = true;
	}
}
