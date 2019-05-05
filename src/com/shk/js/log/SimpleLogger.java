package com.shk.js.log;

public class SimpleLogger extends Logger {
	@Override
	protected void onPrint(String tag, Level level, String content) throws Exception {
		System.out.println(String.format("%s %s %s", tag, level.getText(), content));
	}
}
