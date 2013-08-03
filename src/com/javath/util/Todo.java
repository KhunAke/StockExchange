package com.javath.util;

import java.lang.Thread.State;

public interface Todo extends Runnable {
	public long getSchedule();
	public void start();
	public long getId();
	public String getName();
	public State getState();
}
