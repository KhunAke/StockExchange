package com.javath;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class User {
	
	private final static User user;
	
	public final String name;
	public final String home;
	public final String hostname;
	public final String language;
	public final String country;
	public final String timezone;
	
	static {
		user = new User();
	}
	
	private User() {
		Properties properties = System.getProperties();
		
		name = properties.getProperty("user.name");
		home = properties.getProperty("user.home");
		String localhost;
		try {
			localhost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			localhost = "";
		}
		hostname = localhost;
		language = properties.getProperty("user.language");
		country = properties.getProperty("user.country");
		timezone = properties.getProperty("user.timezone");
	}
	
	public static User getInstance() {
		return user;
	}
}
