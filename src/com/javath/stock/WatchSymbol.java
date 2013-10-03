package com.javath.stock;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.javath.Object;
import com.javath.stock.set.Symbol;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;

public class WatchSymbol extends Object implements Runnable {
	
	private static WatchSymbol instance = new WatchSymbol();
	
	private WatchSymbol() {}
	
	public WatchSymbol getInstance() {
		return instance;
	}

	@Override
	public void run() {
		Date date = new Date();
		Set<String> symbols = Symbol.getSymbols().keySet();
		for (Iterator<String> iterator = symbols.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			Symbol symbol = Symbol.get(name);
			if (symbol.change())
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
