package com.javath.stock.settrade;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Node;

import com.javath.Object;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardHome;
import com.javath.mapping.SettradeBoardId;
import com.javath.util.Browser;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Board extends Object implements Runnable, CustomHandler {
	private Browser browser = new Browser();
	private HtmlParser parser;
	//private CustomFilter filter;
	
	private Date date;
	
	public Board(Date date) {
		this.date = date;
		//filter = new CustomFilter(null);
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
	
	public void run() {
		try {
			parser = HtmlParser.poll();
			this.getWebPage();
		} catch (Exception e) {
			logger.severe(message(e));
		} finally {
			HtmlParser.offer(parser);
			parser = null;
		}
	}
	
	public void getWebPage() {
		parser.setInputStream(
				browser.get("http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=AOM"));
		logger.info(message("{%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS} Cache in \"%2$s\"", this.date, browser.getFileContent()));
		Index set = Index.getInstance();
		if (!this.date.equals(set.getDateTime())) {
			logger.warning(message("Server delayed because request of \"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS\" but received of \"%2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\"",
					this.date,  set.getDateTime()));
		}
		
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		//filter.setNode(parser.parse());
		List<Node> nodes = filter.filter(3);
		TextNode textNode = null;
		try {
			textNode =new TextNode(nodes.get(0));
		} catch (java.lang.IndexOutOfBoundsException e) {
			logger.severe(message("Node not found"));
			return;
		}
		
		for (int index = 1; index < textNode.length(); index++) {
			String[] stringArray = textNode.getStringArray(index);
			if (stringArray[0].equals("2") && (stringArray[2].length() > 0)) {
				//textNode.printStringArray(stringArray);
				String[] symbolArray = textNode.getStringArray(index + 1);
				//textNode.printStringArray(symbolArray);
				this.store(symbolArray, stringArray);
			}
		}
		//textNode.print();
	}
	
	private void store(String[] symbol,String[] data) {
		SettradeBoardHome home = new SettradeBoardHome();
		SettradeBoardId id = new SettradeBoardId();
		SettradeBoard board = new SettradeBoard(id);
		
		id.setSymbol(symbol[1]);
		id.setDate(this.getDateTime());
		//id.setTime(this.getTime());
		try {
			board.setOpen(Index.castFloat(data[4]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setHigh(Index.castFloat(data[6]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setLow(Index.castFloat(data[8]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setLast(Index.castFloat(data[10]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setBid(Index.castFloat(data[16]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setOffer(Index.castFloat(data[18]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setVolume(Index.castLong(data[20]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setValue(Index.castDouble(data[22]));
		} catch (java.lang.NumberFormatException e) {}
		
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
		//	.openSession();
			.getCurrentSession();
		session.beginTransaction();
		home.persist(board);
		try {
			session.getTransaction().commit();
		} catch (org.hibernate.JDBCException e) {
			logger.severe(message("{%1$s, %2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS} SQL Error %3$d, SQLState %4$s: %5$s", 
					id.getSymbol() , id.getDate(), e.getErrorCode(), e.getSQLState(), e.getMessage()));
			session.getTransaction().rollback();
		} 
		/**
		 * 
		 */
		//Quote quote = Quote.getInstance(id.getSymbol());
		//quote.change(board);
	}
	
	private Date getDateTime() {
		//String data =  String.format(Locale.US,"%1$tY-%1$tm-%1$td", date);
		return this.date;
	}
	
}
