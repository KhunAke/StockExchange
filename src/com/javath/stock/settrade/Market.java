package com.javath.stock.settrade;

import java.io.IOException;
import java.io.InputStream;
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

import com.javath.Object;
import com.javath.ObjectException;
import com.javath.mapping.SettradeIndex;
import com.javath.mapping.SettradeIndexHome;
import com.javath.mapping.SettradeIndexId;
import com.javath.util.Browser;
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
	}
	
	public static Market getInstance() {
		return instance;
	}
	
	public Date getDate() {
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
			file.delete(browser.getFileContent());
		} catch (Exception e){
			logger.severe(message(e));
		} catch (ThreadDeath e) {
			logger.severe(message(e));
		} finally {
			nextTask(this.date);
		}
	}
	
	public synchronized void getWebPage() {
		InputStream inputStream = null;
		try {
			inputStream = browser.get("http://www.settrade.com/C13_MarketSummary.jsp?detail=SET");
			HtmlParser parser = new HtmlParser(inputStream);
			logger.info(message("Cache in \"%s\"", browser.getFileContent()));
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
				logger.info(message("Status at %1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS is \"%2$s\"", date, status));
				this.status = status;
			}
			
			if (date.equals(this.date)) {
				return;
			} else {
				this.date = date;
				/** Thread of Board **/
				//new Thread(new Board(date), 
				//		String.format(Locale.US, "Board(%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS)", date))
				//	.start();
			}
			
			for (int index = 1; index < textNode.length(); index++) {
				String[] stringArray = textNode.getStringArray(index);
				if (stringArray[0].equals("2") && (stringArray[2].length() > 0)) {
					//textNode.printStringArray(stringArray);
					/** Thread of store  **/
					newThread(String.format(Locale.US, "%1s(%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS)", 
							stringArray[2].substring(0, stringArray[2].indexOf(" Index")), this.date), 
							this, "threadStore", (java.lang.Object) stringArray);
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
	
	public void threadStore(java.lang.Object... arguments) {
		if (arguments[0] instanceof String[]) {
			store((String[]) arguments[0]);
		} else
			throw newObjectException(ERROR_ARGUMENTS_MISMATCH,"Arguments mismatch");
	}
	
	private void store(String[] data) {
		SettradeIndexHome home = new SettradeIndexHome();
		SettradeIndexId id = new SettradeIndexId();
		SettradeIndex index = new SettradeIndex(id);
		
		id.setSymbol(data[2].substring(0, data[2].indexOf(" Index")));
		id.setDate(this.getDate());
		//id.setTime(this.getTime());
		try {
			index.setLast(castFloat(data[4]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			index.setHigh(castFloat(data[10]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			index.setLow(castFloat(data[12]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			index.setVolume(castLong(data[14]));
		} catch (java.lang.NumberFormatException e) {}
		try {
			index.setValue(castDouble(data[16]));
		} catch (java.lang.NumberFormatException e) {}
		
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
		//	.openSession();
			  .getCurrentSession();
		session.beginTransaction();
		home.persist(index);
		try {
			session.getTransaction().commit();
		} catch (org.hibernate.JDBCException e) {
			logger.severe(message("{%1$s, %2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS} SQL Error %3$d, SQLState %4$s: %5$s", 
					id.getSymbol(), id.getDate(), e.getErrorCode(), e.getSQLState(), e.getMessage()));
			session.getTransaction().rollback();
		} 
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
		case Unknow:
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
			logger.warning(message("Unknow Status at %1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS is \"%2$s\"", 
					date, this.getStatus()));
			if ((current - time) < 15000) // 15 (s) * 1000 (ms)
				calendar.add(Calendar.SECOND, 16);
			time = calendar.getTimeInMillis();
			break;
		}
		
		calendar.setTime(new Date());
		current = calendar.getTimeInMillis();
		if (time < current) {
			calendar.add(Calendar.SECOND, 5);
			if ((status.equals(MarketStatus.Unknow)) || (status.equals(MarketStatus.Intermission))) {
				time = calendar.getTimeInMillis();
			} else {
				nextTask(calendar.getTime());
				return;
			}
		}
		
		
		Trigger trigger = Trigger.getInstance();
		logger.fine(message("Last Update \"%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS\" -> Next Task \"%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS\"", this.date,  time));	
		//trigger.start();
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS)", 
				this.getClass().getSimpleName(), time, 15)));
	}
	
	public static Date castDate(String format, String data) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
		return dateFormat.parse(data);
	}
	
	public static Float castFloat(String data) {
		return Float.valueOf(data.replace(",", ""));
	}
	
	public static Double castDouble(String data) {
		return Double.valueOf(data.replace(",", ""));
	}
	
	public static Long castLong(String data) {
		return Long.valueOf(data.replace(",", ""));
	}

}
