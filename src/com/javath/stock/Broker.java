package com.javath.stock;

import java.util.HashMap;
import java.util.Map;

import com.javath.Object;

public abstract class Broker extends Object {
	
	public static final double CommissionRate = 0.1689;
	// Key is username@classname
	private static Map<String,Broker> brokers = new HashMap<String,Broker>();
	
	public Broker getBroker(String username, String classname) {
		return brokers.get(String.format("%s@%s", username, classname));
	}
	
	public abstract void login();
	public abstract void portfolio();
	public abstract void buy(String symbol, double price, long volume);
	public abstract void sell(String symbol, double price, long volume); 
	public abstract void cancel(String symbol,  String orderNo);
	
}
