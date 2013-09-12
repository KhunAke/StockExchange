package com.javath.stock;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.javath.Object;
import com.javath.stock.set.Symbol;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;

public class WatchSET extends Object implements Runnable {
	
	private final static Map<String,Symbol> symbols = Symbol.getSymbols(); 
	
	private static WatchSET instance = new WatchSET();
	
	public WatchSET getInstance() {
		return instance;
	}
	
	@Override
	public void run() {
		//Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		for (Iterator<String> iterator = symbols.keySet().iterator(); iterator.hasNext();) {
			String name = iterator.next();
			Symbol symbol = symbols.get(name);
			newThread(String.format("%s(%s)",name,Trigger.datetime(date)), symbol);
		}
		nextTask();
	}
	
	private void nextTask() {
		Trigger trigger = Trigger.getInstance();
		//trigger.start();
		trigger.addTodo(new TodoAdapter(0, this, 
				String.format(Locale.US, "%1$s", 
				this.getClass().getSimpleName()),5));
	}
	
}
