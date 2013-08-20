package com.javath.stock.settrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Node;

import com.javath.Object;
import com.javath.ObjectException;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardHome;
import com.javath.mapping.SettradeBoardId;
import com.javath.util.Browser;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Board extends Object implements Runnable, CustomHandler {
	
	private Browser browser;
	private Date date;
	
	public Board(Date date) {
		this.browser = new Browser();
		this.date = date;
	}
	
	private Date getDateTime() {
		return this.date;
	}
	
	@Override
	public boolean condition(Node node) {
		try {
			if (node.getNodeName().equals("DIV"))
				return HtmlParser.attribute(node, "class").equals("divDetailBox");
		} catch (NullPointerException e) {
			return false;
		}
		return false;
	}

	@Override
	public void run() {
		try {
			getWebPage();
			file.delete(browser.getFileContent());
		} catch (Exception e){
			logger.severe(message(e));
		} catch (ThreadDeath e) {
			logger.severe(message(e));
		} finally {
		}
	}
	
	private void getWebPage() {
		InputStream inputStream = null;
		try {
			inputStream = browser.get("http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=AOM");
			HtmlParser parser = new HtmlParser(inputStream);		
			logger.info(message("{%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS} Cache in \"%2$s\"", this.date, browser.getFileContent()));
			Market set = Market.getInstance();
			if (!this.date.equals(set.getDate())) {
				logger.warning(message("Server delayed because request of \"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS\" but received of \"%2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\"",
						this.date,  set.getDate()));
			}
			CustomFilter filter = new CustomFilter(parser.parse());
			filter.setHandler(this);
			List<Node> nodes = filter.filter(3);
			TextNode textNode = null;
			
			try {
				textNode =new TextNode(nodes.get(0));
			} catch (java.lang.IndexOutOfBoundsException e) {
				logger.severe(message(e));
				throw new ObjectException(e);
			}
			
			for (int index = 1; index < textNode.length(); index++) {
				String[] stringArray = textNode.getStringArray(index);
				if (stringArray[0].equals("2") && (stringArray[2].length() > 0)) {
					//textNode.printStringArray(stringArray);
					String[] symbolArray = textNode.getStringArray(index + 1);
					//textNode.printStringArray(symbolArray);
					store(symbolArray, stringArray);
				}
			}
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				logger.severe(message(e));
				throw new ObjectException(e);
			}
		}
	}
	
	private void store(String[] symbol,String[] data) {
		SettradeBoardHome home = new SettradeBoardHome();
		SettradeBoardId id = new SettradeBoardId();
		SettradeBoard board = new SettradeBoard(id);
		
		id.setSymbol(symbol[1]);
		id.setDate(this.getDateTime());
		//id.setTime(this.getTime());
		try {
			board.setOpen(Market.castFloat(data[4]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setHigh(Market.castFloat(data[6]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setLow(Market.castFloat(data[8]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setLast(Market.castFloat(data[10]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setBid(Market.castFloat(data[16]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setOffer(Market.castFloat(data[18]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setVolume(Market.castLong(data[20]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			board.setValue(Market.castDouble(data[22]));
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
	}
}
