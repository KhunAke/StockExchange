package com.javath;

import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.PosixParser;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.util.Trigger;

public class Service implements Daemon {
	
	public static Session getSession() {
		try {
			return ((SessionFactory) new InitialContext().lookup("SessionFactory"))
					.getCurrentSession();
		} catch (NamingException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}

	@Override
	public void destroy() {
		System.out.println(time() + "Done.");
	}

	@Override
	public void init(DaemonContext dc) throws DaemonInitException, Exception {
		System.out.println(time() + "Initializing ...");
		initVM_argument();
	}

	@Override
	public void start() throws Exception {
		System.out.println(time() + "Starting ...");
		Trigger trigger = Trigger.getInstance();
		trigger.start();
	}

	@Override
	public void stop() throws Exception {
		Trigger trigger = Trigger.getInstance();
		trigger.stop();
		System.out.println(time() + "Stopping ...");
	}
	
	private String time() {
		return String.format(Locale.US ,"%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS ", new Date());
	}
	
	public static void main(String[] args) {
		initVM_argument();
		Options options = initOptions();
		if (args.length == 0) {
			try {
				Trigger trigger = Trigger.getInstance();
				trigger.start();
			} catch (java.lang.RuntimeException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
			return;
		}
		CommandLineParser parser = new GnuParser();
		//CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			if (cmd.hasOption("stop")) {
				Trigger trigger = Trigger.getInstance();
				trigger.stop();
				return;
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			Service.usage("Service",options);
		}

	}
	
	public static void initVM_argument() {
		Properties system = System.getProperties();
		String vm_argument = null; // variable temporary 
		vm_argument = System.getProperty("java.naming.factory.initial");
		if (vm_argument == null)
			system.setProperty("java.naming.factory.initial", "com.javath.ContextFactory");
		vm_argument = System.getProperty("java.util.logging.config.file");
		if (vm_argument == null)
			system.setProperty("java.util.logging.config.file","etc/logging.properties");
	}
	
	private static Options initOptions() {
		// create Options object
		Options options = new Options();
		
		// add option
		options.addOption("stop", false, "stop the service");
		
		return options;
	}
	
	public static void usage(String command, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( command, options , true);
	}

}
