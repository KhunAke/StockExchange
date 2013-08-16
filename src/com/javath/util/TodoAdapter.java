package com.javath.util;

import java.util.Calendar;

public class TodoAdapter extends Thread implements Todo {
	
	protected long schedule = 0;
	protected long lifeTimes = 0;
	protected long deathTime = 0;
	
	public TodoAdapter(long schedule, Runnable runnable, String name, long lifeTimes) {
		super(runnable, name);
		setSchedule(schedule);
		setLifeTimes(lifeTimes);
	}
	
	public TodoAdapter(long schedule, Runnable runnable, String name) {
		this(schedule, runnable, name, 0);
	}
	
	public TodoAdapter(Runnable runnable, String name, long lifeTimes) {
		this(Calendar.getInstance().getTimeInMillis(), runnable, name, lifeTimes);
	}
	
	public TodoAdapter(Runnable runnable, String name) {
		this(runnable, name, 0);
	}
	
	public TodoAdapter(long schedule, Runnable runnable, long lifeTimes) {
		this(schedule, runnable, String.format("%s.run()", 
				runnable.getClass().getCanonicalName()), lifeTimes);
	}
	
	public TodoAdapter(long schedule, Runnable runnable) {
		this(schedule, runnable, 0);
	}
	
	public TodoAdapter(Runnable runnable, long lifeTimes) {
		this(Calendar.getInstance().getTimeInMillis(), runnable, lifeTimes);
	}
	
	public TodoAdapter(Runnable runnable) {
		this(runnable, 0);
	}
	
	public void setSchedule(long schedule) {
		this.schedule = schedule;
	}
	
	@Override
	public long getSchedule() {
		return schedule;
	}

	@Override
	public long getLifeTimes() {
		return lifeTimes;
	}
	
	public void setLifeTimes(long times) {
		this.lifeTimes = times;
	}

	@Override
	public long getDeathTime() {
		return deathTime;
	}
	
	@Override
	public void setDeathTime(long time) {
		this.deathTime = time;
	}
	
}
