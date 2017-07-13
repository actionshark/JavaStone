package com.js.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.js.log.Level;
import com.js.log.Logger;

public class ThreadUtil {
	public static final String TAG = ThreadUtil.class.getSimpleName();
	
	private static ExecutorService sNewService;
	
	static {
		sNewService = Executors.newCachedThreadPool();
	}
	
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return false;
	}
	
	public static ThreadHandler run(Runnable runnable) {
		return run(runnable, 0);
	}
	

	public static ThreadHandler run(Runnable runnable, long firstDelay) {
		return run(runnable, firstDelay, 0);
	}

	public static ThreadHandler run(Runnable runnable, long firstDelay, long repeatDelay) {
		return run(runnable, firstDelay, repeatDelay, -1, true);
	}
	
	public static ThreadHandler run(final Runnable runnable, final long firstDelay,
			final long repeatDelay, final int repeatTimes, final boolean fixedInterval) {
		
		final ThreadHandler handler = new ThreadHandler();
		handler.setStatus(ThreadHandler.Status.Running);
		
		sNewService.submit(new Runnable() {
			@Override
			public void run() {
				sleep(firstDelay);
				
				do {
					synchronized (handler) {
						if (handler.mTryCancel) {
							handler.setStatus(ThreadHandler.Status.Cancelled);
							return;
						}
					}
					
					long startTime = System.currentTimeMillis();
					
					callRunnable(runnable);
					
					if(countTimes(repeatTimes) == false) {
						handler.setStatus(ThreadHandler.Status.Finished);
						return;
					}
					
					if (fixedInterval) {
						sleep(repeatDelay);
					} else {
						long delay = repeatDelay - (System.currentTimeMillis() - startTime);
						if (delay > 0) {
							delay = 0;
						}
						
						sleep(delay);
					}
				} while (true);
			}
		});
		
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
