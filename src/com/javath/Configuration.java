package com.javath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;


public class Configuration {
	
	private static Properties properties = new Properties();
	private static Preferences preferences;
	private static String application;
	
	static {
		try {
			Properties system = System.getProperties();
			File file = File.getInstance();
			String configuration = system.getProperty("user.dir") + 
					file.separator + "etc" + 
					file.separator + "configuration.properties";
			FileInputStream propsFile = new FileInputStream(system.getProperty("configuration", configuration));
			properties.load(propsFile);
	        propsFile.close();
	        
	        application = properties.getProperty("Application");
	        if (application == null) {
	        	throw new RuntimeException("Cannot property name \"Application\" in configuration file.");
	        }
	        
	        if (properties.getProperty("Preferences","user").equalsIgnoreCase("system"))
	        	preferences = Preferences.systemRoot().node(application);
	        else
	        	preferences = Preferences.userRoot().node(application);
	        
		} catch (FileNotFoundException e) { 
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
    }
	
	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static Preferences getPreferenceNode(String node) {
		return preferences.node(node);
	}
	
}
