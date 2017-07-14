package com.js.thread;

import com.js.log.Level;
import com.js.log.Logger;

public class ThreadUtil {
	public static final String TAG = ThreadUtil.class.getSimpleName();
	
	private static ThreadUtil sMainInstance;
	private static ThreadUtil sViceInstance;
	
	public static synchronized void setMain(IExecutor executor) {
		sMainInstance = new ThreadUtil(executor);
	}
	
	public static synchronized void setVice(IExecutor executor) {
		sViceInstance = new ThreadUtil(executor);
	}
	
	public static synchronized ThreadUtil getMain() {
		if (sMainInstance == null) {
			sMainInstance = new ThreadUtil(new DefaultExecutor());
		}
		
		return sMainInstance;
	}
	
	public static synchronized ThreadUtil getVice() {
		if (sViceInstance == null) {
			sViceInstance = new ThreadUtil(new DefaultExecutor());
		}
		
		return sViceInstance;
	}
	
	public static boolean sleep(long millis) {
		try {
			if (millis > 0) {
				Thread.sleep(millis);
			}
			
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	private final IExecutor mExecutor;
	
	private ThreadUtil(IExecutor executor) {
		mExecutor = executor;
	}
	
	public ThreadHandler run(Runnable runnable) {
		return run(runnable, 0);
	}
	

	public ThreadHandler run(Runnable runnable, long firstDelay) {
		return run(runnable, firstDelay, 0, 1, false);
	}

	public ThreadHandler run(Runnable runnable, long firstDelay, long repeatDelay) {
		return run(runnable, firstDelay, repeatDelay, -1, false);
	}
	
	public ThreadHandler run(final Runnable runnable, final long firstDelay,
			final long repeatDelay, final int repeatTimes, final boolean fixedInterval) {
		
		final ThreadHandler handler = new ThreadHandler();
		synchronized (handler) {
			handler.mStatus = ThreadHandler.Status.Running;
		}
		
		handler.mRunnable = new Runnable() {
			@Override
			public void run() {
				synchronized (handler) {
					if (handler.mTryCancel) {
						handler.mStatus = ThreadHandler.Status.Cancelled;
						return;
					}
				}
				
				if (fixedInterval) {
					boolean goon = countTimes(repeatTimes);
					
					if (goon) {
						mExecutor.run(handler.mRunnable, repeatDelay);
					}
					
					callRunnable(runnable);
					
					if (!goon) {
						synchronized (handler) {
							handler.mStatus = ThreadHandler.Status.Finished;
						}
					}
				} else {
					callRunnable(runnable);
					
					if (countTimes(repeatTimes)) {
						mExecutor.run(handler.mRunnable, repeatDelay);
					} else {
						synchronized (handler) {
							handler.mStatus = ThreadHandler.Status.Finished;
						}
					}
				}
			}
		};
			
		mExecutor.run(handler.mRunnable, firstDelay);
		
		return handler;
	}
	
	private static void callRunnable(Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
	}
	
	private static boolean countTimes(int repeatTimes) {
		if (repeatTimes == 0 || repeatTimes == 1) {
			return false;
		} else {
			repeatTimes--;
			return true;
		}
	}
}
