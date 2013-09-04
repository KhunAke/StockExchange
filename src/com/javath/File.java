package com.javath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;

public class File {
	
	private final static File file;
	private final static long TIME = 86400000; // Define multiple of 24h, 60m, 60s, 1000ms
	
	public final String line;
	public final String encoding;
	public final String separator;
	
	static {
		file = new File();
	}
	
	private File() {
		Properties properties = System.getProperties();
		
		line = properties.getProperty("line.separator");
		encoding = properties.getProperty("file.encoding");
		separator = properties.getProperty("file.separator");
	}
	
	public static File getInstance() {
		return file;
	}
	
	private OutputStream write(java.io.File file, boolean append) throws FileNotFoundException {
		return new FileOutputStream(file, append);
	}
	
	public OutputStream append(String filename) throws FileNotFoundException {
		java.io.File fileOutput = new java.io.File(filename);
		try {
			return write(fileOutput, true);
		} catch (FileNotFoundException e) {
			java.io.File pathParent = new java.io.File(fileOutput.getParent());
			pathParent.mkdirs();
			return write(fileOutput, true);
		}
	}
	
	public OutputStream overwrite(String filename) throws FileNotFoundException {
		java.io.File fileOutput = new java.io.File(filename);
		try {
			return write(fileOutput, false);
		} catch (FileNotFoundException e) {
			java.io.File pathParent = new java.io.File(fileOutput.getParent());
			pathParent.mkdirs();
			return write(fileOutput, false);
		}
	}
	
	public synchronized String unique(String reference) throws FileNotFoundException {
		java.io.File fileOutput = new java.io.File(reference);
		String name = fileOutput.getName();
		if (fileOutput.exists()) {
			int dot = name.lastIndexOf('.');
			if (dot == -1)
				dot = name.length();
			//Calendar calendar = Calendar.getInstance();
			name = name.substring(0, dot) + "." +
					//String.format(Locale.US ,"%1$tY-%1$tm-%1$td.%2$07x", calendar.getTime(), calendar.getTimeInMillis() % TIME) +
					datetime() +
					name.substring(dot);
		}
		return fileOutput.getParent() + file.separator + name;
	}
	
	public InputStream read(String filename) throws FileNotFoundException {
		return new FileInputStream(new java.io.File(filename));
	}
	
	public boolean delete(String filename) {
		java.io.File file = new java.io.File(filename);
		return file.delete();
	}
	
	public static String datetime() {
		Calendar calendar = Calendar.getInstance();
		return String.format(Locale.US ,"%1$tY%1$tm%1$td.%2$07x", calendar.getTime(), calendar.getTimeInMillis() % TIME);
	}
	
	public static String date(Date date) {
		return String.format(Locale.US, "%1$tY%1$tm%1$td", date);
	}
	
	public static Date date(String date) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US); 
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
	}
	
	public static String time(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return String.format("%07x", calendar.getTimeInMillis() % TIME);
	}
	
	public static Date time(String date) {
		long time = Long.decode("#" + date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.getTime();
	}
	
}
