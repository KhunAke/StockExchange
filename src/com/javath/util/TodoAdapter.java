package com.javath.util;

import java.util.Calendar;

public class TodoAdapter extends Thread implements Todo {
	
	protected long schedule;
	
	public TodoAdapter(long schedule, Runnable runnable, String name) {
		super(runnable, name);
		setSchedule(schedule);
	}
	
	public TodoAdapter(Runnable runnable, String name) {
		this(Calendar.getInstance().getTimeInMillis(), runnable, name);
	}
	
	public TodoAdapter(long schedule, Runnable runnable) {
		//super(runnable);
		//setSchedule(schedule);
		this(schedule, runnable, String.format("%s.run()", 
				runnable.getClass().getCanonicalName()));
	}
	
	public TodoAdapter(Runnable runnable) {
		this(Calendar.getInstance().getTimeInMillis(), runnable);
	}
	
	public void setSchedule(long schedule) {
		this.schedule = schedule;
	}
	
	public long getSchedule() {
		return schedule;
	}
	
}
