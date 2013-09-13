package com.javath.stock.bualuang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.Configuration;
import com.javath.OS;
import com.javath.Object;
import com.javath.ObjectException;
import com.javath.mapping.BualuangBoardDaily;
import com.javath.mapping.BualuangBoardDailyHome;
import com.javath.mapping.BualuangBoardDailyId;
import com.javath.util.Browser;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;
import com.javath.util.text.FixedWidth;

public class BoardDaily extends Object implements Runnable {
	
	private static FixedWidth textField = new FixedWidth();
	private static String URL;
	private static Locale date_locale;
	private static SimpleDateFormat format;
	private static Date launch;
	private static Date expire;
	private Date date = null;
	
	private static BualuangBoardDailyHome home = new BualuangBoardDailyHome();
	
	static {
		try {
			Properties properties = new Properties();

			String configuration = path.etc + file.separator
					+ "bualuang.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.bualuang",
							configuration));
			properties.load(propsFile);
			propsFile.close();

			int length = Integer.valueOf(properties.getProperty("field.length",
					"0"));
			int begin = 0;
			int end = Integer.valueOf(properties.getProperty(
					"field.position." + 1, "0"));
			for (int index = 1; index < length; index++) {
				String name = properties.getProperty("field.name." + index, "");
				begin = end;
				end = Integer.valueOf(properties.getProperty("field.position."
						+ (index + 1), "0"));
				textField.setFieldwidth(index, name, begin, end);
			}
			textField.setFieldwidth(0, "", 0, end);
			textField.setFieldwidth(length, "OutOfRange", end, -1);

			URL = properties.getProperty("board", "http://localhost");
			date_locale = new Locale(properties.getProperty("date.locale",
					"US_us"));
			format = new SimpleDateFormat(properties.getProperty("date.format", "yyyy-MM-dd"), 
					new Locale("US_us"));
			launch = OS.date(properties.getProperty("launch",
					"2000-01-01"));	
			expire = OS.date(properties.getProperty("expire",
					"2999-12-30"));			

		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private String getURL() {
		return String.format(date_locale, URL, date);
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	private Calendar increaseDate(Date date) {
		Calendar calendar = Calendar.getInstance(new Locale("US_us"));
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);
		int DayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (DayOfWeek == 1)
			calendar.add(Calendar.DATE, 1);
		else if (DayOfWeek == 7)
			calendar.add(Calendar.DATE, 2);
		this.setDate(calendar.getTime());
		return calendar;
	}

	public BoardDaily() {
		Session session = ((SessionFactory) getContext("SessionFactory"))
				.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("select max(board.id.date) from BualuangBoardDaily as board");
		Date date = (Date) query.uniqueResult();
		session.getTransaction().commit();
		if (date == null)
			date = launch;
		setDate(date);
	}
	
	private void getWebPage() {
		try {
			Browser browser = new Browser();
			InputStream inputstream = browser.get(getURL());
			textField.setInputStream(inputstream);
			System.out.println(this.getURL());
			System.out.println(browser.getFileContent());
			//System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s\n",
			//		textField.getName(1), textField.getName(2),
			//		textField.getName(3), textField.getName(4),
			//		textField.getName(5), textField.getName(6),
			//		textField.getName(7), textField.getName(8));
			
			if (browser.getStatusCode() == 200) {
				while (textField.hasNextRow()) {
					textField.nextRow();
				//	System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s\n",
				//			textField.getValue(textField.getID("Symbol")),
				//			textField.getValue(textField.getID("Date")),
				//			textField.getValue(textField.getID("Open")),
				//			textField.getValue(textField.getID("High")),
				//			textField.getValue(textField.getID("Low")),
				//			textField.getValue(textField.getID("Close")),
				//			textField.getValue(textField.getID("Volume")),
				//			textField.getValue(textField.getID("Value")));
					//SimpleDateFormat format = new SimpleDateFormat("yyMMdd", new Locale("US_us"));

					BualuangBoardDailyId id = new BualuangBoardDailyId(textField.getValue(textField.getID("Symbol")),
							format.parse(textField.getValue(textField.getID("Date"))));
					BualuangBoardDaily board = null;
					try {
						board = new BualuangBoardDaily(id, 
							Double.valueOf(textField.getValue(textField.getID("Open"))),
							Double.valueOf(textField.getValue(textField.getID("High"))),
							Double.valueOf(textField.getValue(textField.getID("Low"))),
							Double.valueOf(textField.getValue(textField.getID("Close"))), 
							Long.valueOf(textField.getValue(textField.getID("Volume"))),
							Double.valueOf(textField.getValue(textField.getID("Value"))));
					} catch (NumberFormatException e) {
						logger.warning(message("Skip \"%s\" %s",textField.getValue(textField.getID("Symbol")),e.getMessage()));
						continue;
					}
					// Skip for not volume
					if (board.getVolume() == 0)
						continue;
					Session session = ((SessionFactory) getContext("SessionFactory"))
							.getCurrentSession();
					try {
						session.beginTransaction();
						home.persist(board);
						session.getTransaction().commit();
					} catch (Exception e) {
						session.getTransaction().rollback();
						logger.fine(message("%s - %s", this.getURL(), e.getMessage()));
					}
				}
			} else
				logger.warning(message("%s - Status %d %s", this.getURL(), browser.getStatusCode(), browser.getReasonPhrase()));
			
		} catch (ClientProtocolException e) {
			logger.severe(message(e));
		} catch (IOException e) {
			logger.severe(message(e));
		} catch (ParseException e) {
			logger.severe(message(e));
		} finally {
		}
	}
	
	private void nextTask() {
		Calendar calendar = increaseDate(date);
		calendar.set(Calendar.HOUR, 18);
		calendar.set(Calendar.MINUTE, 30);
		long time = calendar.getTimeInMillis();
		
		Trigger trigger = Trigger.getInstance();
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$s)", 
				this.getClass().getSimpleName(), Trigger.datetime(time))));
	}
	
	@Override
	public void run() {
		try {
		if (expire.equals(date)) {
			Trigger trigger = Trigger.getInstance();
			trigger.stop();
		} else {
			getWebPage();
			nextTask();
		}
		} catch (Exception e ) {
			Trigger trigger = Trigger.getInstance();
			trigger.stop();
			throw new ObjectException(e);
		}
	}

}
