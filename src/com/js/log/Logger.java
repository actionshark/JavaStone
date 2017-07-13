package com.js.log;

public abstract class Logger {
	private static Logger sInstance;
	
	public static synchronized void setInstance(Logger logger) {
		sInstance = logger;
	}
	
	public static synchronized Logger getInstance() {
		return sInstance;
	}
	
	//////////////////////////////////////////////////////////////
	
	protected String mDefaultTag = "DEBUG";
	
	protected Level mLevelLimit = Level.D;
	
	public synchronized void setDefaultTag(String tag) {
		mDefaultTag = tag;
	}
	
	public synchronized void setLevelLimit(Level limit) {
		mLevelLimit = limit;
	}
	
	protected abstract void onPrint(String tag, Level level, String content) throws Exception;
	
	protected synchronized void printInternal(String tag, Level level, String content) {
		if (level.getId() < mLevelLimit.getId()) {
			return;
		}
		
		if (tag == null) {
			tag = mDefaultTag;
		}
		
		StringBuilder sb = new StringBuilder();
		
		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 2) {
			StackTraceElement element = stack[2];
			
			sb.append(element.getFileName()).append('-').append(element.getLineNumber())
				.append(':').append(element.getMethodName()).append("()\n");
		}
		
		sb.append(content);
		
		try {
			onPrint(tag, level, sb.toString());
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public synchronized void print(String tag, Level level, Object... content) {
		StringBuilder sb = new StringBuilder();
		
		for (Object ct : content) {
			sb.append(ct).append(' ');
		}
		
		printInternal(tag, level, sb.toString());
	}
	
	public synchronized void printf(String tag, Level level, String format, Object... args) {
		printInternal(tag, level, String.format(format, args));
	}
}
