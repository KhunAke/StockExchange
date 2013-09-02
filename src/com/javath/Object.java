package com.javath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.javath.File;
import com.javath.OS;
import com.javath.Path;
import com.javath.User;

public class Object {
	
	private static final String INIT = "com.javath.Object";
	
	protected static final File file = File.getInstance();
	protected static final OS os = OS.getInstance();
	protected static final Path path = Path.getInstance();
	protected static final User user = User.getInstance();
	
	protected Logger logger;
	protected String ClassID;

	private String classname;
	private String packagename;
	
	static {
		
	}

	public Object() {
		logger = Logger.getAnonymousLogger();
		ClassID = "[@" + String.valueOf(hashCode()) + "]";
		try {
			StackTraceElement[] stack = new Throwable().getStackTrace();
			int index = 0;
			//for (index = stack.length - 1; index > 0; index--) {
			String supperclass = INIT;
			for (index = 0; index < stack.length; index++) {
				classname = stack[index].getClassName();
				if (!classname.equals(INIT))
					if (stack[index].getMethodName().equals("<init>")) {
						Class<?> clazz = Class.forName(classname);
						if (clazz.getSuperclass().getCanonicalName().equals(supperclass))
							supperclass = classname;
						else
							break;
						//if (!Modifier.isAbstract(clazz.getModifiers()))
						//	break;
					}
			}
			//classname = stack[index].getClassName();
			classname = supperclass;
			packagename = Class.forName(classname).getPackage().getName();
			logger = Logger.getLogger(classname);
		} catch (ClassNotFoundException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		}
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if (obj != null) {
			return this.hashCode() == obj.hashCode();
		} else
			return false;
	}

	public final String getClassName() {
		return classname;
	}

	public final String getPackageName() {
		return packagename;
	}
	
	public final String getClassID() {
		return ClassID;
	}

	protected final java.lang.Object getContext(String name) {
		try {
			return new InitialContext().lookup(name);
		} catch (NamingException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	protected final String message(Throwable throwable) {
		System.err.print(String.format(Locale.US, "%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS %2$s %3$s ", 
				new Date(), classname, ClassID)); 
		throwable.printStackTrace(System.err);
		return String.format(ClassID + " %s", throwable);
	}
	
	protected final String message(String format, java.lang.Object... value) {
		return String.format(Locale.US, ClassID + " " + format, value);
	}
	
	protected final ObjectException newObjectException(int code, java.lang.Object... value) {
		return new ObjectException(this, code, value);
	}
	
	protected final void newThread(java.lang.Object object, String methodName, java.lang.Object... arguments) {
		String name = String.format("%s.%s(Object...)"
				, object.getClass().getCanonicalName(), methodName);
		newThread(name,object,methodName, arguments);
	}
	
	protected final void newThread(String name, java.lang.Object object, String methodName, java.lang.Object... arguments) {
	
		Thread thread = new Thread(new Runnable() {
			private java.lang.Object object;
			private Method method;
			//private String methodName;
			private java.lang.Object[] arguments;
			
			public Runnable setExecute(java.lang.Object object, String methodName, java.lang.Object[] arguments) {
				try {
					this.object = object;
					this.arguments = arguments;
					if (arguments.length == 0)
						this.method = object.getClass().getMethod(methodName);
					else
						this.method = object.getClass().getMethod(methodName, java.lang.Object[].class);
				} catch (NoSuchMethodException e) {
					logger.severe(message(e));
					throw new ObjectException(e);
				} catch (SecurityException e) {
					logger.severe(message(e));
					throw new ObjectException(e);
				}
				return this;
			}
			
			public void run() {
				try {
					if (arguments.length == 0)
						method.invoke(object);
					else
						method.invoke(object, (java.lang.Object) arguments);
				} catch (IllegalAccessException e) {
					logger.severe(message(e));
					throw new ObjectException(e);
				} catch (IllegalArgumentException e) {
					logger.severe(message(e));
					throw new ObjectException(e);
				} catch (InvocationTargetException e) {
					logger.severe(message(e));
					throw new ObjectException(e);
				}
			}
			
		}.setExecute(object, methodName, arguments), name);
		thread.start();
	}
	
	protected final void newThread(Runnable runnable) {
		String name = String.format("%s.run()", 
				runnable.getClass().getCanonicalName());
		newThread(runnable,name);
	}
	
	protected final void newThread(String name, Runnable runnable) {
		Thread thread = new Thread(runnable, name);
		thread.start();
	}
	
}