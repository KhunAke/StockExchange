package com.javath.stock.settrade;

import java.util.Calendar;
import java.util.Date;

public enum MarketStatus {
	/**
	 * time = H * 3600 + M * 60 + S
	 * 00:00 - 09:30 
	 * 09:30 - T1    Pre-Open(I)
	 *    T1 - 12:30 Open(I)
	 * 12:30 - 14:00 Intermission
	 * 14:00 - T2    Pre-Open(II)
	 *    T2 - 16:30 Open(II)
	 * 16:30 - T3    Pre-close
	 *    T3 - 17:00 OffHour
	 * 17:00 - 23:59 Closed
	 */
	Empty ("00:00:00:0", "09:30:00:0"),
	PreOpen_I ("09:30:00:0", "T1"),
	Open_I ("T1", "12:30:00:0"),
	Intermission ("12:30:00:0", "14:00:00:0"),
	PreOpen_II ("14:00:00:0", "T2"),
	Open_II ("T2", "16:30:00:0"),
	PreClose ("16:30:00:0", "T3"),
	OffHour ("T3", "17:00:00:0"),
	Closed ("17:00:00:00", "23:59:59:999"),
	Unknow ("00:00:00:0", "23:59:59:999");
	
	private String begin;
	private String end;
	
	private MarketStatus(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }
	
	public long getBegin(Date date) { 
		if (begin.charAt(0) == 'T')
			throw new RuntimeException(
					String.format("Market Status at {%s}", begin));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String[] times = begin.split(":");
		calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(times[0]));
		calendar.set(Calendar.MINUTE, Integer.valueOf(times[1]));
		calendar.set(Calendar.SECOND, Integer.valueOf(times[2]));
		calendar.set(Calendar.MILLISECOND, Integer.valueOf(times[3]));
		return calendar.getTimeInMillis();
	}
	
	public long getEnd(Date date) {
		if (end.charAt(0) == 'T')
			throw new RuntimeException(
					String.format("Market Status at {%s}", end));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String[] times = end.split(":");
		calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(times[0]));
		calendar.set(Calendar.MINUTE, Integer.valueOf(times[1]));
		calendar.set(Calendar.SECOND, Integer.valueOf(times[2]));
		calendar.set(Calendar.MILLISECOND, Integer.valueOf(times[3]));
		return calendar.getTimeInMillis();
	}
	
	public static MarketStatus getStatus(String status) {
		if (status.equals("Pre-Open(I)"))
			return PreOpen_I;
		else if (status.equals("Open(I)"))
			return Open_I;
		else if (status.equals("Intermission"))
			return Intermission;
		else if (status.equals("Pre-Open(II)"))
			return PreOpen_II;
		else if (status.equals("Open(II)"))
			return Open_II;
		else if (status.equals("Pre-close"))
			return PreClose;
		else if (status.equals("OffHour"))
			return OffHour;
		else if (status.equals("Closed"))
			return Closed;
		else if (status.equals("")) 
			return Empty;
		else 
			return Unknow;
	}
	
}
