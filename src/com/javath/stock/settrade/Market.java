package com.javath.stock.settrade;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Node;

import com.javath.File;
import com.javath.OS;
import com.javath.Object;
import com.javath.ObjectException;
import com.javath.util.Browser;
import com.javath.util.Storage;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Market extends Object implements Runnable, CustomHandler {
	
	public final static int ERROR_ARGUMENTS_MISMATCH = 1;
	
	private static Market instance = new Market();
	
	private Browser browser;
	private Date date;
	private MarketStatus status;
	private String task;
	
	private Market() {
		browser = new Browser();
		browser.setTimeOut(15000);
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
				.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("select max(index.id.date) from SettradeIndex as index");
		Date date = (Date) query.uniqueResult();
		session.getTransaction().commit();
		if (date == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(0);
			this.date = calendar.getTime();
		} else
			this.date = date;
		this.status = MarketStatus.Unknow;
		this.task = String.format("Trigger \"%s\" cache in", this.getClass().getSimpleName());
	}
	
	public static Market getInstance() {
		return instance;
	}
	
	public Date getDateTime() {
		return this.date;
	}
	
	public MarketStatus getStatus() {
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

	@Override
	public void run() {
		try {
			getWebPage();	
		} catch (Exception e){
			logger.severe(message(e));
		} catch (ThreadDeath e) {
			logger.severe(message(e));
		} finally {
			nextTask(getDateTime());
		}
	}
	
	private void getWebPage() {
		InputStream inputStream = null;
		try {
			inputStream = browser.get("http://www.settrade.com/C13_MarketSummary.jsp?detail=SET");
			HtmlParser parser = new HtmlParser(inputStream);
			logger.fine(message("Cache in \"%s\"", browser.getFileContent()));
			//task = task != null ? task : "Trigger \"Market\" cache in";
			String comment = String.format("%s \"%s\"", 
					task, browser.getFileContent());
			CustomFilter filter = new CustomFilter(parser.parse());
			
			//filter.setNode(parser.parse());
			filter.setHandler(this);
			List<Node> nodes = filter.filter(6);
			TextNode textNode = null;
			
			try {
				textNode = new TextNode(nodes.get(0));
			} catch (java.lang.IndexOutOfBoundsException e) {
				logger.severe(message(e));
				throw new ObjectException(e);
			}
			
			Date date;
			try {
				date = castDate("ข้อมูลล่าสุด dd/MM/yyyy HH:mm:ss", textNode.getString(0, 2));
			} catch (ParseException e) {
				logger.warning(message(e.getMessage()));
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				date = calendar.getTime();
			}
			
			MarketStatus status = MarketStatus.getStatus(textNode.getString(8, 4));
			if (!status.equals(this.status)) {
				logger.info(message("Status at %1$s is \"%2$s\"", Trigger.datetime(date), status));
				this.status = status;
			}
			
			if (date.equals(this.date)) {
				return;
			} else {
				this.date = date;
				/** Thread of Board **/
				//newThread(String.format(Locale.US, "Board(%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS)", date), 
				//		new Board(date, status), "run");
				Board.createBoard(task, getDateTime(), getStatus());
			}
			
			createStoreMarket(comment, textNode);
			
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
	
	private void createStoreMarket(String comment, TextNode textNode) {
		newThread(String.format(Locale.US, "StoreMarket(%1$s)", Trigger.datetime(getDateTime())), 
				this, "threadStoreMarket", comment, getDateTime(), getStatus(), textNode);
	}
	
	public void threadStoreMarket(java.lang.Object... arguments) {
		if ((arguments[0] instanceof String) && 
			(arguments[1] instanceof Date) &&
			(arguments[2] instanceof MarketStatus) &&
			(arguments[3] instanceof TextNode)) {
			storeMarket((String) arguments[0],
						(Date) arguments[1],
						(MarketStatus) arguments[2],
						(TextNode) arguments[3]);
		} else
			throw newObjectException(ERROR_ARGUMENTS_MISMATCH,"Arguments mismatch");
	}
	
	private void storeMarket(String comment, Date date, MarketStatus status, TextNode textNode) {
		@SuppressWarnings("static-access")
		String filename = String.format(Locale.US, "%1$s%2$smarket.%3$s.%4$s.txt", 
				path.var, file.separator, file.date(date), status);
		String datetime = OS.datetime(date);
		Storage storage  = Storage.getInstance(filename);
		try {
			OutputStreamWriter output = new OutputStreamWriter(storage.append());
			output.write(String.format("#-- %s%n", comment));
			
			for (int index = 1; index < textNode.length(); index++) {
				String[] data = textNode.getStringArray(index);
				if (data[0].equals("2") && (data[2].length() > 0)) {
					String symbol =data[2].substring(0, data[2].indexOf(" Index"));
					//String datetime;
					String last = replace(data[4]);
					String high = replace(data[10]);
					String low = replace(data[12]);
					String volume = replace(data[14]);
					String value = replace(data[16]);
					output.write(String.format("%s,%s,%s,%s,%s,%s,%s%n", 
							symbol,datetime,last,high,low,volume,value));
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
	
	private void nextTask(Date date) {
		Calendar calendar = Calendar.getInstance();
		long current = 0;
		long time= 0;
		try {
			calendar.setTime(castDate("HH:mm:ss", 
					String.format("%1$tH:%1$tM:%1$tS", new Date())));
			current = calendar.getTimeInMillis();
			calendar.setTime(castDate("HH:mm:ss", 
					String.format("%1$tH:%1$tM:%1$tS", date)));
			time = calendar.getTimeInMillis();
		} catch (ParseException e) {
			logger.severe(message(e));
			new ObjectException(e);
		}
		
		calendar.setTime(date);
		switch (this.getStatus()) {
		case Empty:
			time = MarketStatus.PreOpen_I.getBegin(new Date());
			break;
		case PreOpen_I:
		case Open_I:
		case PreOpen_II:
		case Open_II:
		case PreClose:
		case OffHour:
			if ((current - time) < 15000) // 15 (s) * 1000 (ms) 
				calendar.add(Calendar.SECOND, 16);
			else {
				calendar.setTime(new Date());
				calendar.add(Calendar.SECOND, 5);
			}
			time = calendar.getTimeInMillis();
			break;
		case Intermission:
			time = MarketStatus.PreOpen_II.getBegin(new Date());
			break;
		case Closed:
			// Trading quotation will be officially updated at around 18:30
			// 9:30 - 18:30
			if ((current > 9000000) && (current < 41400000) ) {
				calendar.setTime(new Date());
				calendar.set(Calendar.HOUR_OF_DAY, 18);
				calendar.set(Calendar.MINUTE, 30);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			} else if (current <= 9000000) { // next task at 09:30 of today
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 30);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			} else { // next task at 09:30 of tomorrow
				calendar.add(Calendar.DATE, 1);
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 30);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
					calendar.add(Calendar.DATE, 2);
				else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
					calendar.add(Calendar.DATE, 1);
			}
			time = calendar.getTimeInMillis();
			break;
		default:
			logger.warning(message("Unknow Status at %1$s", Trigger.datetime(date)));
			if ((current - time) < 15000) // 15 (s) * 1000 (ms)
				calendar.add(Calendar.SECOND, 16);
			time = calendar.getTimeInMillis();
			break;
		}
		
		calendar.setTime(new Date());
		current = calendar.getTimeInMillis();
		if (time < current) {
			calendar.add(Calendar.SECOND, 5);
			if ((status.equals(MarketStatus.Empty)) || (status.equals(MarketStatus.Intermission))) {
				time = calendar.getTimeInMillis();
			} else {
				nextTask(calendar.getTime());
				return;
			}
		}
		
		Trigger trigger = Trigger.getInstance();
		String market_datetime = String.format(Locale.US, "%1$s \"%2$s\"", 
				this.getClass().getSimpleName(), Trigger.datetime(time));
		logger.info(message("Last Update \"%1$s\" -> Next %2$s", Trigger.datetime(this.date),  market_datetime));
		task = String.format("%1$s cache in", market_datetime);
		//trigger.start();
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$s)", 
				this.getClass().getSimpleName(), Trigger.datetime(time), 15)));
	}
	
	public static Date castDate(String format, String data) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
		return dateFormat.parse(data);
	}

	public static String replace(String data) {
		String result = data.replace("-", "");
		return result.replace(",", "");
	}
}
