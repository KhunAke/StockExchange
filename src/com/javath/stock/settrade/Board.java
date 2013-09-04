package com.javath.stock.settrade;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Node;

import com.javath.OS;
import com.javath.Object;
import com.javath.ObjectException;
import com.javath.stock.set.Symbol;
import com.javath.util.Browser;
import com.javath.util.Storage;
import com.javath.util.Trigger;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Board extends Object implements Runnable, CustomHandler {
	
	private Browser browser;
	private String task;
	private Date date;
	private MarketStatus status;
	
	public Board(String task, Date date, MarketStatus status) {
		this.browser = new Browser();
		this.task = task;
		this.date = date;
		this.status = status;
	}
	
	private Date getDateTime() {
		return this.date;
	}
	
	private MarketStatus getStatus() {
		return this.status;
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
	
	public static void createBoard(String task, Date date, MarketStatus status) {
		Board board = new Board(task, date, status);
		board.newThread(String.format(Locale.US, "Board(%1$s)", Trigger.datetime(date)), 
				board, "run");
	}

	@Override
	public void run() {
		try {
			getWebPage();
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
			logger.fine(message("{%1$s} Cache in \"%2$s\"", Trigger.datetime(this.date), browser.getFileContent()));
			Market set = Market.getInstance();
			if (!this.date.equals(set.getDateTime())) {
				logger.warning(message("Server delayed because request of \"%1$s\" but received of \"%2$s\"",
						OS.datetime(this.date),  OS.datetime(set.getDateTime())));
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
			
			String comment = String.format("%s \"%s\"", 
					task != null ? task : "Trigger cache in", 
					browser.getFileContent());
			
			store(comment, getDateTime(), getStatus(), textNode);
			/**
			for (int index = 1; index < textNode.length(); index++) {
				String[] stringArray = textNode.getStringArray(index);
				if (stringArray[0].equals("2") && (stringArray[2].length() > 0)) {
					//textNode.printStringArray(stringArray);
					String[] symbolArray = textNode.getStringArray(index + 1);
					//textNode.printStringArray(symbolArray);
					store(symbolArray, stringArray);
				}
			}
			*/
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
	
	private void store(String comment, Date date, MarketStatus status, TextNode textNode) {
		@SuppressWarnings("static-access")
		String filename = String.format(Locale.US, "%1$s%2$sboard.%3$s.%4$s.txt", 
				path.var, file.separator, file.date(date), status);
		String datetime = OS.datetime(date);
		Storage storage  = Storage.getInstance(filename);
		OutputStream outputStream = null;
		try {
			outputStream = storage.append();
			OutputStreamWriter output = new OutputStreamWriter(outputStream);
			output.write(String.format("#-- %s%n", comment));
			
			for (int index = 1; index < textNode.length(); index++) {
				String[] data = textNode.getStringArray(index);
				if (data[0].equals("2") && (data[2].length() > 0)) {
					String[] symbolArray = textNode.getStringArray(index + 1);
					String symbol = symbolArray[1];
					//String datetime;
					String open = Market.replace(data[4]);
					String high = Market.replace(data[6]);
					String low = Market.replace(data[8]);
					String last = Market.replace(data[10]);
					String bid = Market.replace(data[16]);
					String offer = Market.replace(data[18]);
					String volume = Market.replace(data[20]);
					String value = Market.replace(data[22]);
					output.write(String.format("%s,%s,%s,%s,%s,%s,%s,,%s,,%s,%s%n", 
							symbol,datetime,open,high,low,last,bid,offer,volume,value));
					Symbol.update(symbol, datetime, open, high, low, last, bid, offer, volume, value);
				}
			}
			
			output.flush();
			//output.close();
			storage.release();
		} catch (FileNotFoundException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		}
		storage.release();
	}
	
}
