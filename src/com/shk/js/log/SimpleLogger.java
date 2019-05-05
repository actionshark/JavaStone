package com.shk.js.log;

public class SimpleLogger extends Logger {
	@Override
	protected void onPrint(Level level, String content) throws Exception {
		System.out.println(String.format("%s %s", level.getText(), content));
	}
}
