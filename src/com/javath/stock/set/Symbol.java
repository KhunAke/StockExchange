package com.javath.stock.set;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.javath.OS;
import com.javath.Object;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardId;
import com.javath.mapping.StreamingBidsOffers;
import com.javath.mapping.StreamingBidsOffersId;
import com.javath.mapping.StreamingTicker;
import com.javath.mapping.StreamingTickerId;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;

public class Symbol extends Object implements Runnable {
	
	private static final Map<String,Symbol> symbols = new HashMap<String,Symbol>();
	private static long changeDateTime = 0;
	
	public static final Map<String,Symbol> getSymbols() {
		return symbols;
	}
	
	public static final long getChangeDateTime() {
		return changeDateTime;
	}
	
	public final String name;
	public final Price price = new Price(0.0);
	private final Map<Float,Long> lastMapVolume = new HashMap<Float,Long>();
	private final Map<Float,Long> MapVolume = new TreeMap<Float,Long>();
	
	private SettradeBoard lastBoard;
	private final ConcurrentLinkedQueue<SettradeBoard> boards = new ConcurrentLinkedQueue<SettradeBoard>();
	private StreamingTicker lastTicker;
	private final ConcurrentLinkedQueue<StreamingTicker> tickers = new ConcurrentLinkedQueue<StreamingTicker>();
	private final Map<Float,StreamingBidsOffers> bids_offers = new TreeMap<Float,StreamingBidsOffers>();
	//private final Map<Float,StreamingBidsOffers> bids_offers = new HashMap<Float,StreamingBidsOffers>();
	//private LinkedList<Ticker> tickers = new LinkedList<Ticker>();
	
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
	
	public static void putBoard(SettradeBoard board) {
		SettradeBoardId id = board.getId();
		Symbol symbol = Symbol.get(id.getSymbol());
		symbol.offerBoard(board);
	}
	
	public static SettradeBoard createBoard(String symbol, String datetime,
			String open, String high, String low, String last,
			String bid, String bid_volume, String offer, String offer_volume,
			String volume, String value) {
		SettradeBoardId id = new SettradeBoardId();
		id.setSymbol(symbol);
		id.setDate(OS.date(datetime));
		SettradeBoard board = new SettradeBoard(id);
		try {
			board.setOpen(Float.valueOf(open));
		} catch (NumberFormatException e) {}
		try {
			board.setHigh(Float.valueOf(high));
		} catch (NumberFormatException e) {}
		try {
			board.setLow(Float.valueOf(low));
		} catch (NumberFormatException e) {}
		try {
			board.setLast(Float.valueOf(last));
		} catch (NumberFormatException e) {}
		try {
			board.setBid(Float.valueOf(bid));
		} catch (NumberFormatException e) {}
		try {
			board.setBidVolume(Long.valueOf(bid_volume));
		} catch (NumberFormatException e) {}
		try {
			board.setOffer(Float.valueOf(offer));
		} catch (NumberFormatException e) {}
		try {
			board.setOfferVolume(Long.valueOf(offer_volume));
		} catch (NumberFormatException e) {}
		try {
			board.setVolume(Long.valueOf(volume));
		} catch (NumberFormatException e) {}
		try {
			board.setValue(Double.valueOf(value));
		} catch (NumberFormatException e) {}
		return board;
	}
	
	private void offerBoard(SettradeBoard board) {
		boards.offer(board);
	}
	
	private SettradeBoard pollBoard() {
		return boards.poll();
	}
	
	public static void putTicker(StreamingTicker ticker) {
		//StreamingTickerId id = ticker.getId();
		Symbol symbol = Symbol.get(ticker.getSymbol());
		symbol.offerTicker(ticker);
	}
	
	public static StreamingTicker createTicker(
			String date, String type, String market, String N,
			String time, String side, String price, String close,
			String change, String change_percent, String sequence,
			String a, String b, String volume, String symbol) {
		StreamingTickerId id = new StreamingTickerId();
		id.setDate(OS.date(date));
		id.setMarket(Short.valueOf(market));
		id.setSequence(Integer.valueOf(sequence));
		StreamingTicker ticker = new StreamingTicker(id);
		try {
			ticker.setType(Short.valueOf(type));
		} catch (NumberFormatException e) {}
		//ticker.setMarket();
		try {
			ticker.setN(Short.valueOf(N));
		} catch (NumberFormatException e) {}
		ticker.setTime(OS.datetime(time));
		try {
			ticker.setSide(side);
		} catch (NumberFormatException e) {}
		try {
			ticker.setPrice(Float.valueOf(price));
		} catch (NumberFormatException e) {}
		try {
			ticker.setClose(Float.valueOf(close));
		} catch (NumberFormatException e) {}
		try {
			ticker.setChange(Float.valueOf(change));
		} catch (NumberFormatException e) {}
		try {
			ticker.setChangePercent(Float.valueOf(change_percent));
		} catch (NumberFormatException e) {}
		//ticker.setSequence();
		ticker.setA(a);
		ticker.setB(b);
		try {
			ticker.setVolume(Integer.valueOf(volume));
		} catch (NumberFormatException e) {}
		ticker.setSymbol(symbol);
		return ticker;
	}
	
	public void offerTicker(StreamingTicker ticker) {
		tickers.offer(ticker);
	}
	
	public StreamingTicker pollTicker() {
		return tickers.poll();
	}
	
	public static void setBidOffer(StreamingBidsOffers bid_offer) {
		StreamingBidsOffersId id = bid_offer.getId();
		Symbol symbol = Symbol.get(id.getSymbol());
		symbol.putBidOffer(bid_offer);
	}
	
	public void putBidOffer(StreamingBidsOffers bid_offer) {
		StreamingBidsOffersId id = bid_offer.getId();
		bids_offers.put(id.getPrice(), bid_offer);
	}
	
	public StreamingBidsOffers[] getBidsOffers() {
		bids_offers.size();
		StreamingBidsOffers[] result = new StreamingBidsOffers[bids_offers.size()];
		int index = 0;
		//List<Float> keys = new LinkedList<Float>(bids_offers.keySet());
        //Collections.sort(keys);
		for (Iterator<Float> iterator = bids_offers.keySet().iterator(); iterator.hasNext();) {
			Float key = iterator.next();
			result[index] = bids_offers.get(key);
			index += 1;
		}
		return result;
	}
	
	public static StreamingBidsOffers createBidOffer(String symbol,String datetime,String price,String bid_volume,String offer_volume) {
		StreamingBidsOffersId id = new StreamingBidsOffersId();
		id.setSymbol(symbol);
		id.setDate(OS.datetime(datetime));
		id.setPrice(Float.valueOf(price));
		StreamingBidsOffers bid_offer = new StreamingBidsOffers(id);
		try {
			bid_offer.setBidVolume(Long.valueOf(bid_volume));
		} catch (NumberFormatException e) {}
		try {
			bid_offer.setOfferVolume(Long.valueOf(offer_volume));
		} catch (NumberFormatException e) {}
		return bid_offer;
	}
	
	public static void resetAll() {
		for (Iterator<String> iterator = symbols.keySet().iterator(); iterator.hasNext();) {
			String name = iterator.next();
			Symbol symbol = symbols.get(name);
			symbol.reset();
		}
	}
	
	public void reset() {
		MapVolume.clear();
		bids_offers.clear();
	}
	
	public boolean change() {
		if (!boards.isEmpty())
			return true;
		if (!tickers.isEmpty())
			return true;
		return false;
	}

	@Override
	public void run() {
		while (!boards.isEmpty()) {
			SettradeBoard board = pollBoard();
			double price = 0.0;
			double oldValue = 0.0;
			double newValue = 0.0;
			long oldVolume = 0;
			long newVolume = 0;
			try {
				price = board.getLast();
			} catch (NullPointerException e) {
				lastBoard = board;
				continue;
			}
			try {
				oldValue = lastBoard.getValue();
			} catch (NullPointerException e) {}
			try {
				oldVolume = lastBoard.getVolume();
			} catch (NullPointerException e) {}
			try {
				newValue = board.getValue();
			} catch (NullPointerException e) {}
			try {
				newVolume = board.getVolume();
			} catch (NullPointerException e) {}
			newValue = newValue - oldValue;
			newVolume = newVolume - oldVolume;
			if (newVolume > 0)
				if (compare(price * newVolume / 1000, newValue))
					System.out.printf("  %s, %f, %d, %f%n",
							name, price, newVolume, newValue);
				else
					System.out.printf("# %s, %f, %d, %f%n",
							name, price, newVolume, newValue);
			lastBoard = board;
		}
		while (!tickers.isEmpty()) {
			lastTicker = pollTicker();
		}
	}
	
	public boolean compare(double a, double b) {
		return String.format("%.2f", a).equals(String.format("%.2f", b));
	}

}
