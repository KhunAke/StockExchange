package com.javath.stock.set;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.javath.OS;
import com.javath.Object;
import com.javath.stock.settrade.Market;

public class Symbol extends Object {
	
	private static final int TICKER_HISTORY = 5;
	
	private static Map<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	private boolean changeData = false;
	private int changePriceStep = 0;
	
	private String name;
	private Date datetime;
	private float open = 0.0f;
	private float high = 0.0f;
	private float low = 0.0f;
	private float last = 0.0f;
	private float bid = 0.0f;
	private float offer = 0.0f;
	private long volume = 0;
	private double value= 0.0;
	
	private class BidOffer {
		public float price = 0.0f;
		public int bid = 0;
		public int offer = 0;
	}
	
	private class Ticker {
		public String side;
		public float price;
		public int volume;
	}
	
	private Map<Float,BidOffer> bids_offers = new HashMap<Float,BidOffer>();
	private LinkedList<Ticker> tickers = new LinkedList<Ticker>();
	
	private Symbol(String name) {
		this.name = name;
	}
	
	public static Symbol get(String name) {
		Symbol symbol = symbols.get(name);
		if (symbol == null) {
			symbol = new Symbol(name);
			symbols.put(name, symbol);
		}
		return symbol;
	}
	
	public String getName() {
		return name;
	}
	
	public static void update(String name,String datetime,
			String open,String high,String low,String last,
			String bid,String offer,String volume,String value) {
		Symbol symbol = get(name);
		symbol.update(datetime, open, high, low, last, bid, offer, volume, value);
	}
	
	public void update(String datetime,
			String open,String high,String low,String last,
			String bid,String offer,String volume,String value) {
		this.datetime = OS.datetime(datetime);
		try {
			this.open = Float.valueOf(open);
		} catch (NumberFormatException e) {}
		try {
			this.high = Float.valueOf(high);
		} catch (NumberFormatException e) {}
		try {
			this.low = Float.valueOf(low);
		} catch (NumberFormatException e) {}
		try {
			float change = Float.valueOf(last);
			if (this.last != change) {
				changeData = true;
				if (this.last > change) 
					if (changePriceStep > 0) {
						logger.info(message("%1$s Reversed for %2$d step",name,changePriceStep));
						changePriceStep = -1;
					} else
						changePriceStep -= 1;
				else if (this.last < change)
					if (changePriceStep < 0) {
						logger.info(message("%1$s Reversed for %2$d step",name,changePriceStep));
						changePriceStep = 1;
					} else
						changePriceStep += 1;
				this.last = change;
			}
		} catch (NumberFormatException e) {}
		try {
			this.bid = Float.valueOf(bid);
		} catch (NumberFormatException e) {}
		try {
			this.offer = Float.valueOf(offer);
		} catch (NumberFormatException e) {}
		try {
			this.volume = Long.valueOf(volume);
		} catch (NumberFormatException e) {}
		try {
			this.value = Double.valueOf(value);
		} catch (NumberFormatException e) {}
	}
	
	public static void ticker(String name,String side,String price,String volume) {
		Symbol symbol = get(name);
		try {
			symbol.ticker(side, Float.valueOf(price), Integer.valueOf(volume));
		} catch (NumberFormatException e) {}
	}
	
	public void ticker(String side,float price,int volume) {
		Ticker last = tickers.getLast();
		if ((last.side == side) && (last.price == price))
			last.volume += volume;
		else {
			Ticker ticker = new Ticker();
			ticker.side = side;
			ticker.price = price;
			ticker.volume = volume;
			tickers.addLast(ticker);
			while (tickers.size() > TICKER_HISTORY)
				tickers.removeFirst();
		}
	}
	
	public void setBidOffer(String name,String price,String bid_volume,String offer_volume) {
		Symbol symbol = get(name);
		try {
			symbol.setLastBidOffer(Float.valueOf(price), Integer.valueOf(bid_volume), Integer.valueOf(offer_volume));
		} catch (NumberFormatException e) {}
	}
	
	public void setLastBidOffer(float price, int bidVolume, int offerVolume) {
		BidOffer bid_offer = bids_offers.get(Float.valueOf(price));
		if (bid_offer == null) {
			bid_offer = new BidOffer();
			bids_offers.put(Float.valueOf(price), bid_offer);
		}
		bid_offer.bid = bidVolume;
		bid_offer.offer = offerVolume;
	}
	
	public static void resetAll() {
		for (Iterator<String> iterator = symbols.keySet().iterator(); iterator.hasNext();) {
			String name = iterator.next();
			Symbol symbol = symbols.get(name);
			symbol.reset();
		}
	}
	
	public void reset() {
		bids_offers.clear();
		tickers.clear();
	}

}
