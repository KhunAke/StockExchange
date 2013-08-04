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
import com.javath.Object;
import com.javath.mapping.BualuangBoardHistory;
import com.javath.mapping.BualuangBoardHistoryHome;
import com.javath.mapping.BualuangBoardHistoryId;
import com.javath.util.Browser;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;
import com.javath.util.text.FixedWidth;

public class Daily extends Object implements Runnable {

	private static FixedWidth textField = new FixedWidth();
	private static String URL;
	private static Locale date_locale;
	private Date date = null;
	
	private static BualuangBoardHistoryHome home = new BualuangBoardHistoryHome();

	static {
		try {
			Properties properties = new Properties();

			String configuration = path.etc + file.separator
					+ "set.daily.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.set.daily",
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

			URL = properties.getProperty("URL", "http://localhost");
			date_locale = new Locale(properties.getProperty("date.locale",
					"US_us"));

		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public Daily() {
		
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
				.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("select max(stock.id.date) from StockBualuang as stock");
		Date date = (Date) query.uniqueResult();
		session.getTransaction().commit();
		
		if (date == null) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", new Locale("US_us"));
				date = format.parse("2013-05-06");
			} catch (ParseException e) {
				logger.severe(message(e));
			}
		}
		
		this.setDate(date);
	}

	public Daily(Date date) {
		setDate(date);
	}

	public String getURL() {
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

	@Override
	protected java.lang.Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void run() {

		try {
			Browser browser = new Browser();
			InputStream inputstream = browser.get(this.getURL());
			textField.setInputStream(inputstream);
			System.out.println(this.getURL());
			//System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s\n",
			//		textField.getName(1), textField.getName(2),
			//		textField.getName(3), textField.getName(4),
			//		textField.getName(5), textField.getName(6),
			//		textField.getName(7), textField.getName(8));
			
			if (browser.getStatusCode() == 200) {
				Session session = ((SessionFactory) this.getContext("SessionFactory"))
						.getCurrentSession();
				session.beginTransaction();
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
					
					SimpleDateFormat format = new SimpleDateFormat("yyMMdd", new Locale("US_us"));

					BualuangBoardHistoryId id = new BualuangBoardHistoryId(textField.getValue(textField.getID("Symbol")),
							format.parse(textField.getValue(textField.getID("Date"))));
					BualuangBoardHistory board = new BualuangBoardHistory(id, 
							Float.valueOf(textField.getValue(textField.getID("Open"))),
							Float.valueOf(textField.getValue(textField.getID("High"))),
							Float.valueOf(textField.getValue(textField.getID("Low"))),
							Float.valueOf(textField.getValue(textField.getID("Close"))), 
							Long.valueOf(textField.getValue(textField.getID("Volume"))),
							Double.valueOf(textField.getValue(textField.getID("Value"))));
						home.persist(board);
				}
				try {
					session.getTransaction().commit();
				} catch (Exception e) {
					session.getTransaction().rollback();
					logger.severe(message("%s - %s", this.getURL(), e.getMessage()));
				}
			} else
				logger.warning(message("%s - Status %d %s", this.getURL(), browser.getStatusCode(), browser.getReasonPhrase()));
			
			Calendar calendar = this.increaseDate(date);
			calendar.set(Calendar.HOUR, 18);
			calendar.set(Calendar.MINUTE, 30);
			
			Trigger trigger = Trigger.getInstance();
			trigger.addTodo(new TodoAdapter(calendar.getTimeInMillis(), this, 
					String.format(Locale.US, "Daily.%1$tY-%1$tm-%1$td", date)));
			
		} catch (ClientProtocolException e) {
			logger.severe(message(e));
		} catch (IOException e) {
			logger.severe(message(e));
		} catch (ParseException e) {
			logger.severe(message(e));
		} finally {
		}

	}

}
