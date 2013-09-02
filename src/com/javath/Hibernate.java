package com.javath;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class Hibernate {

	private static final SessionFactory sessionFactory;

	static {

		//String confHibernate = path.etc + file.separator + "hibernate.cfg.xml";
		//String fileHibernate = confHibernate;
		//String fileHibernate = Configuration.getProperty(
		//		"configuration.hibernate", confHibernate);

		Configuration configuration = new Configuration();
		com.javath.Path path = com.javath.Path.getInstance();
		com.javath.File file = com.javath.File.getInstance();
		String confHibernate = path.etc + file.separator + "hibernate.cfg.xml";
		String fileHibernate = com.javath.Configuration.getProperty(
				"configuration.hibernate", confHibernate);
		java.io.File fileConfig = new java.io.File(fileHibernate);
		configuration.configure(fileConfig);
		//configuration.configure(fileHibernate);
		Properties properties = configuration.getProperties();
		properties.put("hibernate.current_session_context_class", "thread");
		// Hibernate 4.0.1
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
				.applySettings(properties).buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		// Hibernate 3.6.10 deprecated
		//sessionFactory = configuration.buildSessionFactory();

	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}