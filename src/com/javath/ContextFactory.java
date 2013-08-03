package com.javath;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class ContextFactory implements InitialContextFactory, Context {
	
	private static Context context = new ContextFactory();
	
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {
		// TODO Auto-generated method of InitialContextFactory
		return context;
	}

	public java.lang.Object addToEnvironment(String propName,
			java.lang.Object propVal) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public void bind(Name name, java.lang.Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void bind(String name, java.lang.Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public Context createSubcontext(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public Context createSubcontext(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroySubcontext(Name name) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void destroySubcontext(String name) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public Hashtable<?, ?> getEnvironment() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNameInNamespace() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NameParser getNameParser(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NameParser getNameParser(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public java.lang.Object lookup(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public java.lang.Object lookup(String name) throws NamingException {
		// TODO Auto-generated method stub
		if (name.equals("SessionFactory"));
			return Hibernate.getSessionFactory();
	}

	public java.lang.Object lookupLink(Name name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public java.lang.Object lookupLink(String name) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public void rebind(Name name, java.lang.Object obj) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void rebind(String name, java.lang.Object obj)
			throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public java.lang.Object removeFromEnvironment(String propName)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void rename(String oldName, String newName) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void unbind(Name name) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	public void unbind(String name) throws NamingException {
		// TODO Auto-generated method stub
		
	}

	
}
