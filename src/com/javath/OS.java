package com.javath;

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
}
