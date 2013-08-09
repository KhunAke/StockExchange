package com.javath;

import java.util.Properties;

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

import com.javath.util.Trigger;

public class Service implements Daemon {

	@Override
	public void destroy() {
		System.out.println("done.");
	}

	@Override
	public void init(DaemonContext dc) throws DaemonInitException, Exception {
		System.out.println("initializing ...");
		initVM_argument();
	}

	@Override
	public void start() throws Exception {
		System.out.println("starting ...");
		Trigger trigger = Trigger.getInstance();
		trigger.start();
	}

	@Override
	public void stop() throws Exception {
		Trigger trigger = Trigger.getInstance();
		trigger.stop();
		System.out.println("stopping ...");
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
	
	private static void initVM_argument() {
		Properties system = System.getProperties();
		String vm_argument = null;
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
