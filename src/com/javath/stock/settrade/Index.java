package com.javath.stock.settrade;

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

public class Index extends Object implements Runnable, CustomHandler {

	private Browser browser = new Browser();
	private HtmlParser parser;
	//private CustomFilter filter;
	
	private static Index instance = new Index();
	
	private Date date;
	private String status;
	
	private Index() {
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
		this.status = "";
		//CustomFilter filter = new CustomFilter(null);
	}
	
	public static Index getInstance() {
		//if (instance == null)
		//	instance = new Index();
		return instance;
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
		Index.getInstance().run();
	}
	
	public synchronized void run() {
		try {
			parser = HtmlParser.poll();
			this.getWebPage();
		} catch (Exception e) {
			logger.severe(message(e));
		} finally {
			HtmlParser.offer(parser);
			parser = null;
			//this.nextTask(this.date);
		}
	}
	
	public void getWebPage() {
		parser.setInputStream(
				browser.get("http://www.settrade.com/C13_MarketSummary.jsp?detail=SET"));
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		/** Debug 
		try {
			parser.setInputStream(
					new FileInputStream("var/C13_MarketSummary.SET.2013-07-26.Intermission.jsp"));
		} catch (FileNotFoundException e) {
			logger.severe(message(e));
		}
		*/
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		//filter.setNode(parser.parse());
		List<Node> nodes = filter.filter(6);
		TextNode textNode = null;
		
		try {
			textNode = new TextNode(nodes.get(0));
		} catch (java.lang.IndexOutOfBoundsException e) {
			logger.severe(message("Node not found."));
			return;
		}
		//
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
		
		String status = textNode.getString(8, 4);
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
			
		//System.out.println(String.format("%s, %s, %s", 
		//		this.getDate(), this.getTime(), this.getStatus()));
		//
		for (int index = 1; index < textNode.length(); index++) {
			String[] stringArray = textNode.getStringArray(index);
			if (stringArray[0].equals("2") && (stringArray[2].length() > 0)) {
				//textNode.printStringArray(stringArray);
				/** store **/
				this.store(stringArray);
			}
		}
		//textNode.print();
	}
	
	public Date getDateTime() {
		return this.date;
	}
	
	public String getStatus() {
			return this.status;
	}
	
	private void store(String[] data) {
		SettradeIndexHome home = new SettradeIndexHome();
		SettradeIndexId id = new SettradeIndexId();
		SettradeIndex index = new SettradeIndex(id);
		
		id.setSymbol(data[2].substring(0, data[2].indexOf(" Index")));
		id.setDate(this.getDateTime());
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
		/**
		 * time = H * 3600 + M * 60 + S
		 * 00:00 - 09:30 
		 * 09:30 - T1    Pre-Open(I)
		 *    T1 - 12:30 Open(I)
		 * 12:30 - 14:00 Intermission
		 * 14:00 - T2    Pre-Open(II)
		 *    T2 - 16:30 Open(II)
		 * 16:30 - T3    Pre-close
		 *    T3 - 17:00 OffHour
		 * 17:00 - 23:59 Closed
		 */
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
		}
		calendar.setTime(date);
		// switch (String) support compiler compliance settings to 1.7
		/*
		switch (this.getStatus()) {
		case "":
			calendar.set(Calendar.HOUR_OF_DAY, 9);
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case "Pre-Open(I)":
		case "Open(I)":
		case "Pre-Open(II)":
		case "Open(II)":
		case "Pre-close":
		case "OffHour":
			if ((current - time) < 15000) // 15 (s) * 1000 (ms) 
				calendar.add(Calendar.SECOND, 16);
			else {
				calendar.setTime(new Date());
				calendar.add(Calendar.SECOND, 5);
			}
			break;
		case "Intermission":
			calendar.set(Calendar.HOUR_OF_DAY, 14);
			calendar.set(Calendar.MINUTE, 00);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case "Closed":
			// Trading quotation will be officially updated at around 18:30
			// 9:30 - 18:30
			 if ((current > 9000000) && (current < 41400000) ) {
				calendar.setTime(new Date());
				calendar.set(Calendar.HOUR_OF_DAY, 18);
				calendar.set(Calendar.MINUTE, 30);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			} else if (current <= 9000000) {
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 30);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			} else {
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
			break;
		default:
			logger.warning(message("Unknow Status at %1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS is \"%2$s\"", 
					date, this.getStatus()));
			if ((current - time) < 15000) // 15 (s) * 1000 (ms)
				calendar.add(Calendar.SECOND, 16);
		}
		 */
		time = calendar.getTimeInMillis();
		calendar.setTime(new Date());
		current = calendar.getTimeInMillis();
		if (time < current) {
			calendar.add(Calendar.SECOND, 5);
			if ((status.equals("")) || (status.equals("Intermission"))) {
				time = calendar.getTimeInMillis();
			} else {
				nextTask(calendar.getTime());
				return;
			}
		}
		logger.fine(message("Last Update \"%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS\" -> Next Task \"%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS\"", this.date,  time));

		Trigger trigger = Trigger.getInstance();
		//trigger.start();
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS)", 
				this.getClass().getSimpleName(), time)));
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


