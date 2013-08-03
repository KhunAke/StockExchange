package com.javath;

import java.util.Properties;

public class Path {
	
	private final static Path path;
	
	public final String home;
	public final String separator;
	public final String etc;
	public final String var;
	public final String log;
	public final String tmp;
	
	static {
		path = new Path();
	}
	
	private Path() {
		Properties properties = System.getProperties();
		File file = File.getInstance();
		
		//home = user.dir;
		home = Configuration.getProperty("home", properties.getProperty("user.dir"));
		separator = properties.getProperty("path.separator");
		//etc = home + file.separator + "etc";
		etc = Configuration.getProperty("etc", home + file.separator + "etc");
		//var = home + file.separator + "var";
		var = Configuration.getProperty("var", home + file.separator + "var");
		//log = var + file.separator + "log";
		log = Configuration.getProperty("log", var + file.separator + "log");
		//tmp = properties.getProperty("java.io.tmpdir");
		String tmpPath = Configuration.getProperty("tmp", properties.getProperty("java.io.tmpdir"));
		if (tmpPath.equals(properties.getProperty("java.io.tmpdir"))) {
			tmpPath = tmpPath + file.separator + Configuration.getProperty("Application","");
			java.io.File localFile = new java.io.File(tmpPath);
			if (!localFile.exists())
				localFile.mkdir();
		}
		tmp = tmpPath;
	}
	
	public static Path getInstance() {
		return path;
	}
}
