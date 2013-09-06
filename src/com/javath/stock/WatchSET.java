package com.javath.stock;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import com.javath.Object;
import com.javath.stock.set.Symbol;

public class WatchSET extends Object implements Runnable {
	
	private final static Map<String,Symbol> symbols = Symbol.getSymbols(); 
	
	@Override
	public void run() {
		Calendar calendar = Calendar.getInstance();
		long current = calendar.getTimeInMillis();
		if (Symbol.getChangeDateTime() < current) {
			for (Iterator<String> iterator = symbols.keySet().iterator(); iterator.hasNext();) {
				String name = iterator.next();
				Symbol symbol = symbols.get(name);
				
			}
		}
	}
	
}
