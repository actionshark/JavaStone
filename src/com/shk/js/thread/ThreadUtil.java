package com.shk.js.thread;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;

public class ThreadUtil {
	private static ThreadUtil sInstance = new ThreadUtil(new DefaultExecutor());

	public static void setInstance(IExecutor executor) {
		sInstance = new ThreadUtil(executor);
	}

	public static ThreadUtil getInstance() {
		return sInstance;
	}

	public static boolean sleep(long millis) {
		try {
			if (millis > 0) {
				Thread.sleep(millis);
			}

			return true;
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}

		return false;
	}

	public static void exitApp() {
		System.exit(0);
	}

	/////////////////////////////////////////////////////////////////////////

	private final IExecutor mExecutor;

	private ThreadUtil(IExecutor executor) {
		mExecutor = executor;
	}

	public ThreadHandler run(Runnable runnable) {
		return run(runnable, 0);
	}

	public ThreadHandler run(final Runnable runnable, long firstDelay) {
		final ThreadHandler handler = new ThreadHandler();
		handler.mStatus = ThreadHandler.Status.Running;

		handler.mRunnable = new Runnable() {
			@Override
			public void run() {
				synchronized (handler) {
					if (handler.mTryCancel) {
						handler.mStatus = ThreadHandler.Status.Cancelled;
						return;
					}
				}

				callRunnable(runnable);

				synchronized (handler) {
					handler.mStatus = ThreadHandler.Status.Finished;
				}
			}
		};

		mExecutor.run(handler.mRunnable, firstDelay);

		return handler;
	}

	public ThreadHandler run(Runnable runnable, long firstDelay, long repeatDelay) {
		return run(runnable, firstDelay, repeatDelay, -1, false);
	}

	public ThreadHandler run(final Runnable runnable, final long firstDelay, final long repeatDelay,
			final int repeatTimes, final boolean fixedInterval) {

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
			Logger.print(Level.E, e);
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
