package com.javath.stock.settrade;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Node;

import com.javath.Object;
import com.javath.ObjectException;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardHome;
import com.javath.mapping.SettradeBoardId;
import com.javath.stock.set.Price;
import com.javath.util.Browser;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Quote extends Object implements Runnable, CustomHandler {
	
	private static Map<String,Quote> mapQuote = new HashMap<String,Quote>();
	
	//private Browser browser = new Browser();
	//private HtmlParser parser = new HtmlParser(null);
	//private CustomFilter filter = new CustomFilter(null);
	
	private String symbol;
	
	/* Statistic
	private Date StatisticDate;
	private float PriceEarning;
	private float EarningPerShare;
	private float PricePerBookValue;
	private float MarketCapitalization;
	private float DividendYield;
	private float DividendPerShare;
	private float ListedShare;
	*/
	
	public static Quote getInstance(String symbol) {
		Quote quote = mapQuote.get(symbol);
		if (quote == null) {
			quote = new Quote(symbol);
			mapQuote.put(symbol, quote);
		}
		return quote;
	}
	
	private Quote(String symbol) {
		this.symbol = symbol.toUpperCase();
	}
	
	public boolean condition(Node node) {
		try {
			if (node.getNodeName().equals("DIV"))
				return HtmlParser.attribute(node, "class").equals("divDetailBox");
		} catch (NullPointerException e) {
			return false;
		}
		return false;
	}
	
	public static void main(String[] args) {
		Quote.getInstance("TC").run();
	}
	
	
	public void run() {
		int again = 0;
		while (true) {
			try {
				getWebPage(again);
				break;
			} catch (Exception e) {
				try {
					again += 1; 
					Thread.sleep(5000 + (again * 500));
				} catch (InterruptedException ie) {}
			}
		}
	}
	
	public void getWebPage(int again) {
		// TODO Auto-generated method stub
		Browser browser = new Browser();
		HtmlParser parser = HtmlParser.poll();
		
		try {
			String txtSymbol = URLEncoder.encode(symbol,Charset.defaultCharset().name());
			parser.setInputStream(
				browser.get(String.format("http://www.settrade.com/C04_01_stock_quote_p1.jsp?txtSymbol=%s&selectPage=1",
						txtSymbol)));
			//logger.info(message("http://www.settrade.com/C04_01_stock_quote_p1.jsp?txtSymbol=%s&selectPage=1", txtSymbol));
			logger.info(message("{%s.%d} Cache in \"%s\"", symbol, again, browser.getFileContent()));
		} catch (UnsupportedEncodingException e) {
			logger.severe(message(e));
		}
		
		//browser.get("http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=AOM"));
		//FileInputStream is = new FileInputStream("var/C13_MarketSummary.jsp");
		//parser.setInputStream(is);
		CustomFilter filter = new CustomFilter(null);
		filter.setHandler(this);
		filter.setNode(parser.parse());
		List<Node> nodes = filter.filter(6);
		//print(nodes.get(1));
		TextNode textNode = null;
		try {
			textNode = new TextNode(nodes.get(1));
		} catch (IndexOutOfBoundsException e) {
			logger.warning(message("Symbol \"%s\" not found.", symbol));
			throw new ObjectException("Symbol \"%s\" not found.", symbol);
		}
		
		if (!symbol.equalsIgnoreCase(textNode.getString(7, 2))) {
			logger.warning(message("Symbol \"%s\" not match in Thread \"%s\".", 
					symbol, Thread.currentThread().getName()));
			throw new ObjectException("Symbol \"%s\" not match in Thread \"%s\".", 
					symbol, Thread.currentThread().getName());
		}
		//textNode.print();
		//
		SettradeBoardId id = new SettradeBoardId();
		SettradeBoard board = new SettradeBoard(id);
		
		id.setSymbol(symbol);
		try {
			id.setDate(Index.castDate("ข้อมูลล่าสุด dd/MM/yyyy HH:mm:ss",textNode.getString(2, 4)));
		} catch (ParseException e) {
			logger.warning(message(e));
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			id.setDate(calendar.getTime());
		}
		try {
			board.setLast(Index.castFloat(textNode.getString(7,8)));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setOpen(Index.castFloat(textNode.getString(25,4)));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setHigh(Index.castFloat(textNode.getString(30,4)));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setLow(Index.castFloat(textNode.getString(35,4)));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setVolume(Index.castLong(textNode.getString(22,4)));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setValue(Index.castDouble(textNode.getString(27,4)));
		} catch (java.lang.NumberFormatException e) {}
		
		boolean loopVolume = true;
		int index = 43;
		while (loopVolume) {
			if  (textNode.getString(index, 2).equals("ปริมาณเสนอซื้อ")) {
				index += 1;
				try {
					board.setBid(Index.castFloat(textNode.getString(index,4)));
				} catch (java.lang.NumberFormatException e) {}
				try {
					board.setBidVolume(Index.castLong(textNode.getString(index,2)));
				} catch (java.lang.NumberFormatException e) {}
				try {
					board.setOffer(Index.castFloat(textNode.getString(index,6)));
				} catch (java.lang.NumberFormatException e) {}
				try {
					board.setOfferVolume(Index.castLong(textNode.getString(index,8)));
				} catch (java.lang.NumberFormatException e) {}
				break;
			} else
				index += 1;
		}
		
		this.store(board);

		HtmlParser.offer(parser);
	}
	
	private void store(SettradeBoard board) {
		SettradeBoardHome home = new SettradeBoardHome();
		
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
		//	.openSession();
				.getCurrentSession();
		session.beginTransaction();
		
		home.attachDirty(board);
		
		try {
			session.getTransaction().commit();
		} catch (org.hibernate.JDBCException e) {
			logger.severe(message("SQL Error %d, SQLState %s: %s", 
					e.getErrorCode(), e.getSQLState(), e.getMessage()));
			session.getTransaction().rollback();
		} 
	}
	
	public void change(SettradeBoard board) {
		try {
			Price price = new Price(board.getLast());
			if (price.previous(1) == board.getLow()) {
				//System.out.printf("%s [UP] %s, %s\n",
				//		board.getId().getSymbol(), price.getValue(), board.getLow());
				//if (board.getLast().equals(board.getBid()))	
					new Thread(this, board.getId().getSymbol()).start();
			//} else if (price.next(1) == board.getHigh()) {
			//	System.out.printf("%s [DOWN] %s, %s\n",
			//			board.getId().getSymbol(), price.getValue(), board.getHigh());
			//	new Thread(this, board.getId().getSymbol()).start();
			} 
		} catch (java.lang.NullPointerException e) {}
	}
}
