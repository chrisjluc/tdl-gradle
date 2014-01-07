package com.ac.tdl.model;

import java.util.Calendar;

public abstract class Model {

	public abstract void getModelFromDb();
	public abstract void setModelInDb();
	
	protected long getCurrentTime() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	protected int getIntFromBool(boolean isTrue) {
		if (isTrue) {
			return 1;
		}
		return 0;
	}

	protected boolean getBoolFromInt(int isTrue) throws Exception {
		if (isTrue == 1) {
			return true;
		} else if (isTrue == 0) {
			return false;
		}
		throw new Exception();
	}
}
