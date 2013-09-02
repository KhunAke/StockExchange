package com.javath.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import com.javath.Configuration;
import com.javath.Object;
import com.javath.ObjectException;

public class Trigger extends Object {
	
	private static Trigger trigger;
	private static Preferences preferences;
	
	private final static Map<String,Long> watchdog = new HashMap<String, Long>();
	private static Queue<Todo> doingList = new LinkedList<Todo>();
	private static Queue<Todo> todoList = new LinkedList<Todo>();
	private static int pulserate = 60000;
	private static long stepdown = Long.MAX_VALUE;
	private static long downtime = 0;
	
	private Timer timer;
	
	static {
		try {
			
			Properties properties = new Properties();
			String configuration = path.etc + 
					file.separator + "trigger.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.trigger", configuration));
			properties.load(propsFile);
	        propsFile.close();
	        
	        pulserate = Integer.parseInt(properties.getProperty("PulseRate", "60000"));
	        downtime = Integer.parseInt(properties.getProperty("downtime", "300000"));
	        int size = Integer.parseInt(properties.getProperty("Todo.size", "0"));
	        for (int index = 0; index < size; index++) {
	        	String classname = properties.getProperty("Todo." + index);
	        	watchdog.put(classname, new Long(0));
	        	/**
				@SuppressWarnings("unchecked")
				Class<Runnable> classRunnable = (Class<Runnable>) Class.forName(classname);
	        	Runnable runnable = null;
				try {
					runnable = classRunnable.newInstance();
				} catch (InstantiationException e) {
					runnable = invoke_getInstance(classRunnable);
				} catch (IllegalAccessException e) {
					runnable = invoke_getInstance(classRunnable);
				}
				todoList.add(new TodoAdapter(runnable,String.format("%s", runnable.getClass().getCanonicalName()), downtime/pulserate));
	        	//todoList.add(new TodoAdapter(runnable,String.format("Todo-%d", index), downtime/pulserate));
	        	*/
			}
	        wakeUp(1);
	        
		} catch (FileNotFoundException e) { 
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} //catch (ClassNotFoundException e) {
			//logger.severe(message(e));
		//	throw new ObjectException(e);
		//}
    }
	
	private static Runnable invoke_getInstance(Class<Runnable> classRunnable) {
		try {
			Method method = classRunnable.getDeclaredMethod("getInstance");
			return (Runnable) method.invoke(null);
		} catch (SecurityException ex) {
			//logger.severe(message(ex));
			throw new ObjectException(ex);
		} catch (NoSuchMethodException ex) {
			//logger.severe(message(ex));
			throw new ObjectException(ex);
		} catch (IllegalArgumentException ex) {
			//logger.severe(message(ex));
			throw new ObjectException(ex);
		} catch (IllegalAccessException ex) {
			//logger.severe(message(ex));
			throw new ObjectException(ex);
		} catch (InvocationTargetException ex) {
			//logger.severe(message(ex));
			throw new ObjectException(ex);
		}
	}
	
	private Trigger() {
		preferences = Configuration.getPreferenceNode(this.getClassName().replace('.', '/'));
		boolean running = preferences.getBoolean("running", false);
		if (running == true)
			todoList.clear();
		this.timer = new Timer();
		timer.schedule( new TimerTask() {
		//*	
			private Trigger trigger;
			
			public TimerTask setTrigger(Trigger trigger) {
				this.trigger = trigger;
				return this;
			}
			
	  		public void run(){
	  			long datetime = Calendar.getInstance().getTimeInMillis();
	  			trigger.checkSchedule(datetime);
	  		}
	  	//*	
		}.setTrigger(this), 1, pulserate);
	}
	
	public static Trigger getInstance() {
		if (trigger == null)
			trigger = new Trigger();
		return trigger;
	}
	
	public void start() {
		boolean running = preferences.getBoolean("running", false);
		if (running == false)
			preferences.putBoolean("running", true);
		else {
			throw new RuntimeException("Another application is running");
		}
	}
	
	public void stop() {
		preferences.putBoolean("running", false);
	}
	
	public static void exit() {
		Trigger trigger = Trigger.getInstance();
		trigger.stop();
	}
	
	protected boolean isThreadDefault(Thread thread) {
		String name = thread.getName();
		if ( name.equals("Reference Handler") ||
			 name.equals("Attach Listener") ||
			 name.equals("Finalizer") ||
			 name.equals("Timer-0") ||
			 name.equals("DestroyJavaVM") ||
			 name.equals("Signal Dispatcher"))
			return true;
		else
			return false;
	}
	
	protected void checkSchedule(long datetime) {
		boolean running = preferences.getBoolean("running", false);
		if (running == false) {
			if (stepdown != Long.MAX_VALUE) {
				if ((stepdown + downtime) < datetime) {
					Map<Thread, StackTraceElement[]> mapThread = Thread.getAllStackTraces();
					for (Iterator<Thread> threads = mapThread.keySet().iterator(); 
							threads.hasNext();) {
						Thread thread = threads.next();
						//String thread_name = thread.getName();
						if (! isThreadDefault(thread) ) {
							logger.warning(message("TERMINATED thread(%d) name=\"%s\", status=\"%s\""
									,thread.getId(), thread.getName(), thread.getState()));
						}
					}
					System.exit(0xFF);
				} else {
					boolean process = false;
					Map<Thread, StackTraceElement[]> mapThread = Thread.getAllStackTraces();
					for (Iterator<Thread> threads = mapThread.keySet().iterator(); 
							threads.hasNext();) {
						Thread thread = threads.next();
						//String thread_name = thread.getName();
						if (! isThreadDefault(thread) ) {
							logger.info(message("thread(%d) name=\"%s\", status=\"%s\"",
									thread.getId(), thread.getName(), thread.getState()));
							process = true;
						}
					}
					if (!process)
						System.exit(0);
				}
			} else {
				stepdown = Calendar.getInstance().getTimeInMillis();
			}
			return;
		}
		//if (stepdown < datetime)
		//	System.exit(0);
		synchronized(doingList) {
			Queue<Todo> queue = new LinkedList<Todo>();
			while (doingList.size() != 0) {
				Todo todo = doingList.poll();
				updateWatchdog(todo.getRunnableNameClass(), datetime);
				if (todo.getState() != State.TERMINATED) {
					queue.add(todo);
					if ((todo.getDeathTime() != 0) && (todo.getDeathTime() <= datetime)) {
							todo.stop();
						logger.warning(message("Thread ID=%d, Name=%s : Life limit %d times",
									todo.getId(), todo.getName(), todo.getLifeTimes()));
					}
				} else {
					logger.fine(message("Triger(%d).TERMINATED Thread ID=%d, Name=%s, Queue(%d,%d)", datetime/pulserate,
							todo.getId(), todo.getName(), todoList.size(), doingList.size()));
				}
				
			}
			doingList = queue;
		}
		//if (running) {
			synchronized(todoList) {
				Queue<Todo> queue = new LinkedList<Todo>();
				while (todoList.size() != 0) {
					Todo todo = todoList.poll();
					updateWatchdog(todo.getRunnableNameClass(), datetime);
					if (todo.getSchedule() <= datetime) {
						if (todo.getLifeTimes() != 0)
							todo.setDeathTime(datetime + (todo.getLifeTimes() * pulserate)); 
						todo.start();
						logger.fine(message("Triger(%d).NEW Thread ID=%d, Name=%s, Queue(%d,%d)", datetime/pulserate,
								todo.getId(), todo.getName(), todoList.size(), doingList.size() + 1));
						doingList.add(todo);
					} else
						queue.add(todo);
				}
				todoList = queue;
			}
		//} else if (stepdown != Long.MAX_VALUE) {
		//	if ((stepdown + downtime) < datetime)
		//		System.exit(0);
			//else if (doingList.size() == 0)
			//	timer.cancel();
		//} else {
		//	stepdown = Calendar.getInstance().getTimeInMillis();
		//}
		wakeUp(datetime);	
	}
	
	public boolean addTodo(Todo todo) {
		return todoList.add(todo);
	}
	
	public boolean removeTodo(Todo todo) {
		return todoList.remove(todo);
	}
	
	public static void main(String[] args) {
		Trigger trigger = Trigger.getInstance();
		trigger.start();
	}
	
	public static String datetime(Date date) {
		return String.format(Locale.US, "%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", date);
	}
	
	public static Date datetime(String date) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); 
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
	}
	
	private void updateWatchdog(String classname, long datetime) {
		Long state = watchdog.get(classname);
		if (state != null) { 
			state = datetime;
			watchdog.put(classname, state);
		}
	}
	
	private static void wakeUp(long datetime) {
		for (Iterator<String> iterator = watchdog.keySet().iterator(); iterator.hasNext();) {
			String classname = iterator.next();
			Long status = watchdog.get(classname);
			if (status != null) {
				if (status.longValue() != datetime) {
					System.out.printf("%s Trigger wakes up \"%s\"%n", datetime(new Date()), classname);
					try {
						@SuppressWarnings("unchecked")
						Class<Runnable> classRunnable = (Class<Runnable>) Class.forName(classname);
						Runnable runnable = null;
						try {
							runnable = classRunnable.newInstance();
						} catch (InstantiationException e) {
							runnable = invoke_getInstance(classRunnable);
						} catch (IllegalAccessException e) {
							runnable = invoke_getInstance(classRunnable);
						}
						todoList.add(new TodoAdapter(runnable,String.format("%s", runnable.getClass().getCanonicalName()), downtime/pulserate));
					} catch (ClassNotFoundException e) {
						//logger.severe(message(e));
						throw new ObjectException(e);
					}
				}
			}
		}
	}
	
}
