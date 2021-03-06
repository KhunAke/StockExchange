package com.javath.util;

import java.lang.Thread.State;

public interface Todo extends Runnable {
	public long getSchedule();
	public long getLifeTimes();
	public void setDeathTime(long time);
	public long getDeathTime();
	public String getRunnableNameClass();
	public void start();
	public void stop();
	public long getId();
	public String getName();
	public State getState();
}
