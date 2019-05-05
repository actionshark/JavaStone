package com.shk.js.log;

public enum Level {
	V(1, "V"),
	D(2, "D"),
	I(3, "I"),
	W(4, "W"),
	E(5, "E"),
	F(6, "F");
	
	private final int mId;
	private final String mText;
	
	private Level(int id, String text) {
		mId = id;
		mText = text;
	}
	
	public int getId() {
		return mId;
	}
	
	public String getText() {
		return mText;
	}
}
