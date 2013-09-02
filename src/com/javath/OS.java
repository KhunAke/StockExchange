package com.javath;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class OS {
	
	private final static OS os;
	
	public final String name;
	public final String version;
	public final String patch;
	public final String arch;
	public final String unicode;
	public final String endian;
	public final String desktop;
	public final String model;
	
	static {
		os = new OS();
	}
	
	private OS() {
		Properties properties= System.getProperties();
		
		name = properties.getProperty("os.name");
		version = properties.getProperty("os.version");
		patch = properties.getProperty("sun.os.patch.level");
		arch = properties.getProperty("os.arch");
		unicode = properties.getProperty("sun.io.unicode.encoding");
		endian = properties.getProperty("sun.cpu.endian");
		desktop = properties.getProperty("sun.desktop");
		model = properties.getProperty("sun.arch.data.model");
	}
	
	public static OS getInstance() {
		return os;
	}
	
	public static String datetime() {
		return datetime(new Date());
	}
	
	public static String datetime(Date date) {
		return String.format(Locale.US, "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", date);
	}
	
	public static Date datetime(String date) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
	}
	
	public static String date() {
		return date(new Date());
	}
	
	public static String date(Date date) {
		return String.format(Locale.US, "%1$tY-%1$tm-%1$td", date);
	}
	
	public static Date date(String date) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US); 
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
	}
	
	public static String time() {
		return time(new Date());
	}
	
	public static String time(Date date) {
		return String.format(Locale.US, "%1$tH:%1$tM:%1$tS", date);
	}
	
	public static Date time(String date) {
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US); 
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
	}
}
