package com.javath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Random;
import java.util.prefs.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Base64;

import com.javath.util.AES;

public class Configuration {
	
	private static final Properties properties = new Properties();
	private static final Preferences preferences;
	private static final String application;
	private static final AES aes;
	
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
	        
	        String shared_key = preferences.get("SharedKey", "");
	        if (shared_key.equals("")) {
	        	shared_key = randomMD5();
	        	preferences.put("SharedKey", shared_key);
	        }
	        
	        aes = new AES(md5(shared_key));
	        
		} catch (FileNotFoundException e) { 
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
    }
	
	public static String md5(String message) {
		try {
			byte[] bytesOfMessage = message.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfMessage);
			return Base64.encodeBase64String(digest);
		} catch (UnsupportedEncodingException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (NoSuchAlgorithmException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	public static String randomMD5() {
		char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 22; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		return sb.toString() + "==";
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
	
	public static String encrypt(String message) {
		return aes.encrypt(message);
	}
	
	public static String decrypt(String message) {
		return aes.decrypt(message);
	}
	
	public static String getApplicationName() {
		return application;
	}
	
	public static void main(String[] args) {
		Options options = initOptions();
		if (args.length == 0) {
			Service.usage("Configuration",options);
			return;
		}
		//CommandLineParser parser = new GnuParser();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			if (cmd.hasOption("encrypt")) {
				String message = cmd.getOptionValue("encrypt");
				System.out.println(Configuration.encrypt(message));
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			Service.usage("Configuration",options);
		}
	}
	
	@SuppressWarnings("static-access")
	private static Options initOptions() {
		// create Options object
		Options options = new Options();
		
		// add option
		options.addOption( OptionBuilder.withLongOpt( "encrypt" )
                .withDescription( "use encryption message" )
                .hasArg()
                .isRequired()
                .withArgName("message")
                .create("e") );
		
		return options;
	}
	
}
