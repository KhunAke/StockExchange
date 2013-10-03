package com.javath.stock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.protocol.HttpContext;

import com.javath.Object;
import com.javath.util.Browser;

public abstract class Broker extends Object {
	
	// Key is username@classname
	private static Map<String,Broker> brokers = new HashMap<String,Broker>();
	
	public static Broker getBroker(String username, String classname) {
		return brokers.get(String.format("%s@%s", username, classname));
	}
	
	protected static void putBroker(String username, Broker broker) {
		brokers.put(String.format("%s@%s", username, broker.getClass().getCanonicalName()), 
				broker);
	}
	
	protected Browser browser;
	protected HttpContext httpContext;
	protected String name;
	
	protected abstract HttpContext login(Browser browser);
	public abstract double getCommissionRate();
	public abstract String getName();
	public abstract void portfolio();
	public abstract void buy(int source, String symbol, double price);
	public abstract long[] buy(String symbol, double price, long volume);
	public abstract void sell(int source, String symbol, double price);
	public abstract long[] sell(String symbol, double price, long volume); 
	public abstract boolean cancel(String symbol, String orderNo);

	public HttpContext getHttpContext() {
		synchronized (httpContext) {
			return httpContext;
		}
	}

	public void setHttpContext(HttpContext httpContext) {
		synchronized (httpContext) {
			this.httpContext = httpContext;
		}
	}
	
	public static void signalBuy(int source, String symbol, double price) {
		Set<String> keys =   brokers.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			brokers.get(key).buy(source, symbol, price);
		}
	}
	
	public static void signalSell(int source, String symbol, double price) {
		Set<String> keys =   brokers.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			brokers.get(key).sell(source, symbol, price);
		}
	}
}
