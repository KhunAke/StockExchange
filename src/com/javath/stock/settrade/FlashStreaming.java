package com.javath.stock.settrade;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.fluent.Form;
import org.apache.http.protocol.HttpContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.OS;
import com.javath.ObjectException;
import com.javath.mapping.StreamingBidsOffers;
import com.javath.mapping.StreamingOrder;
import com.javath.mapping.StreamingOrderHome;
import com.javath.mapping.StreamingOrderId;
import com.javath.mapping.StreamingTicker;
import com.javath.stock.Broker;
import com.javath.stock.set.Symbol;
import com.javath.util.Browser;
import com.javath.util.Lock;
import com.javath.util.Storage;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;

public abstract class FlashStreaming extends Broker  implements Runnable{
	
	public final static int ERROR_ARGUMENTS_MISMATCH = 1;
	
	private final Lock login_process = new Lock();
	
	//protected String accountNo = getAccountNo();
	protected String pin = "000000";
	
	protected double credit;
	protected double cash;
	protected double line;
	
	// service = S4MarketSummary
	private String mode = "Pull";
	// service = S4InstrumentInfo
	private String initiatedFlag = "1";
	private String newInstInfo = "";
	private String oldInstInfo = "";
	// service = S4InstrumentTicker
	private String sequenceId = ""; //
	private String newInstTicker = "";
	private String newMarket = "";
	private String oldInstTicker = "";
	private String oldMarket = "";
	// service = S4MarketTicker
	private String optionSequenceId2 = "-1"; //
	private String sequenceId2 = "-1"; //
	private String newMarket2 = "A";
	private String newInstTicker2 = "_all";
	private String newSum2 = "N";
	private String oldMarket2 = "";
	private String oldInstTicker2 = "";
	private String oldSum2 = "";
	
	private Map<String,String> flashVars;
	
	protected void setFlashVars(String[] flashVars) {
		this.flashVars = new HashMap<String,String>();
		for (int index = 0; index < flashVars.length; index++) {
			String[] vars = flashVars[index].split("[=]");
			if (vars.length == 1)
				this.flashVars.put(vars[0],"");
			else
				this.flashVars.put(vars[0],vars[1]);
		}
	}
	
	private void processLogin() {
		if (lockLoginProcess(true))
			synchronized (httpContext) {
				setHttpContext(login(browser));
				lockLoginProcess(false);
			}
		else
			browser.setContext(getHttpContext());
	}
	
	protected String getFlashVars(String name) {
		try {
			return flashVars.get(name);
		} catch (NullPointerException e) {
			processLogin();
			return getFlashVars(name);
		}
	}
	
	protected String getAccountNo() {
		DataProvider data = new DataProvider(); 
		data.read(getFlashVars("fvAccountInfoList"));
		try {
			return data.get(0, 0, 2);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	protected String getAccountType() {
		DataProvider data = new DataProvider(); 
		data.read(getFlashVars("fvAccountInfoList"));
		try {
			return data.get(2, 0, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	protected void printFlashVars() {
		for (Iterator<String> iterator = flashVars.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			System.out.printf("%s = %s%n",key,flashVars.get(key));
		}
	}
	
	protected abstract void loadFlashVars();
	
	private Browser browser_for_runnable;
	
	public boolean lockLoginProcess(boolean lock) {
		synchronized (login_process) {
			if (lock) {
				if (login_process.isValue())
					return false;
				else { // lock : false -> true
					login_process.setValue(lock);
					return true;
				}
			} else {
				if (login_process.isValue()) { // lock : true -> false
					login_process.setValue(lock);
					return true;
				} else
					return false;
			}
		}
	}

	public FlashStreaming() {
		browser_for_runnable = new Browser();
		setHttpContext(browser_for_runnable.getContext());
		browser_for_runnable.setTimeOut(5000);
	}
	
	public long synctime() {
		long time = new Date().getTime();
		String url_synctime = String.format("https://%s%s", getFlashVars("fvPrimaryHost"), getFlashVars("fvSyncTimeServlet"));
		browser.get(String.format("%s?%d", url_synctime, time));
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		if (browser.getStatusCode() != 200) {
			logger.severe(message("HTTP Status %s \"%s\"",browser.getStatusCode(), browser.getReasonPhrase()));
			return 0;
		}
		DataProvider dataProvider = new DataProvider().read(browser.getInputStream());
		long result = Long.valueOf(dataProvider.get(0,0,2)) - time ;
		logger.finest(message(
				"Adjust time Settrade server offset %.3f sec",result/1000.0d));
		return result;
	}
	
	protected DataProvider seos(Form form) {
		// browser httpContext
		browser.post(String.format("%s/daytradeflex/streamingSeos.jsp", getFlashVars("fvITPHost")), form);
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		if (browser.getStatusCode() != 200) {
			logger.severe(message("HTTP Status %s \"%s\"",browser.getStatusCode(), browser.getReasonPhrase()));
			return null;
		}
		DataProvider dataProvider = null;
		try {
			dataProvider = new DataProvider().read(browser.getInputStream());
		} catch (ObjectException e) {
			if (e.getMessage().equals("Unauthorized Access.")) {
				logger.warning(message("Unauthorized Access"));
				processLogin();
				dataProvider = seos(form);
			} else
				throw e;
		}
		return dataProvider;	
	}
	
	protected DataProvider seos(String service) {
		Form form = Form.form();
		form.add("Service", service);
		form.add("txtAccountNo", getAccountNo());
		form.add("NewMode", "Pull");
		form.add("txtAccountType", getAccountType());
		return seos(form);
	}
	
	// Key is orderNo
	private Map<Long,StreamingOrder> orders = new HashMap<Long,StreamingOrder>();
	
	public long[] orderStatus() {
		//        0|1|	   2|   3|	 4|	   5|	  6|	  7|	  8|	    9|	  10|
		// Order No| |Symbol|Time|Side|Price|Volume|Matched|Balance|Cancelled|Status|
		// 11|12|13|14|	             15|16|17|      18|          19|       20|
		//   |  |  |  |Date(dd-mm-yyyy)|  |  |Order No|Order Broker|Condition|
		// status = Queuing(SX), Cancelled(CX), Pending(S), Open(O)
		DataProvider data = seos("OrderStatus");
		int service = data.getOrderOfServiceName("OrderStatus");
		int results = data.getNumberOfResults(service);
		ArrayList<Long> arrayOrderNo = new ArrayList<Long>();
		for (int result = 0; result < results; result++) {
			String[] string = data.getResult(service, result);
			//
			System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
					string[0],string[1],string[2],string[3],string[4],string[5],string[6],string[7],string[8],string[9],string[10],string[11],string[12],string[13],string[14],string[15],string[16],string[17],string[18],string[19],string[20]);
			//
			long orderNo = Long.valueOf(string[0]);
			StreamingOrder order = getOrder(orderNo);
			if (order == null) {
				StreamingOrderId id = new StreamingOrderId(string[2], castTime(string[3]));
				order = new StreamingOrder(id);
				order.setOrderNo(orderNo);
				order.setSide(string[4]);
				order.setPrice(Double.valueOf(string[5]));
				order.setVolume(Integer.valueOf(string[6]));
				arrayOrderNo.add(orderNo);
			}
			order.setMatched(Integer.valueOf(string[7]));
			order.setBalance(Integer.valueOf(string[8]));
			order.setCancelled(Integer.valueOf(string[9]));
			order.setStatus(string[10]);
			try {
				order.setOrderNo(Long.valueOf(string[19]));
			} catch (NumberFormatException e) {}
			synchronized (orders) {
				orders.put(orderNo, order);
			}
		}
		long[] newOrderNo = new long[arrayOrderNo.size()];
		for (int index = 0; index < newOrderNo.length; index++) {
			newOrderNo[index] = arrayOrderNo.get(index);
		}
		return newOrderNo;
	}
	
	public StreamingOrder getOrder(long orderNo) {
		synchronized (orders) {
			return orders.get(orderNo);
		}
	}
	
	public Set<Long> getSetOfOrderNo() {
		synchronized (orders) {
			return orders.keySet();
		}
	}
	
	public Date castTime(String time) {
		Date date = new Date();
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = dateFormat.parse(String.format("%1$tY-%1$tm-%1$td %2$s", date, time));
		} catch (ParseException e) {
			logger.severe(message(e));
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			date = calendar.getTime();
		}
		//return calendar.getTime();
		return date;
	}
	
	private void storeOrder(String[] data) {
		// orderNo||symbol|time|side|price|volume|match|balance|cancelled|status|
		StreamingOrderHome home = new StreamingOrderHome();
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
		//	.openSession();
			.getCurrentSession();
		session.beginTransaction();
		StreamingOrderId id =  new StreamingOrderId();
		StreamingOrder order = new StreamingOrder();
		
		id.setSymbol(data[2]);
		id.setDate(castTime(data[3]));
		
		order.setId(id);
		order.setOrderNo(Long.valueOf(data[0]));
		order.setSide(data[4]);
		order.setPrice(Double.valueOf(data[5]));
		order.setVolume(Integer.valueOf(data[6]));
		order.setMatched(Integer.valueOf(data[7]));
		order.setBalance(Integer.valueOf(data[8]));
		order.setCancelled(Integer.valueOf(data[9]));
		order.setStatus(data[10]);
		
		home.attachDirty(order);
		
		try {
			session.getTransaction().commit();
		} catch (org.hibernate.JDBCException e) {
			logger.severe(message("SQL Error %d, SQLState %s: %s", 
					e.getErrorCode(), e.getSQLState(), e.getMessage()));
			session.getTransaction().rollback();
		}
	}
	
	// Key is [ credit|cash|line ]
	private final Map<String, Long> budgets = new HashMap<String, Long>();
	
	public void accountInfo() {
		// credit|  ee|line|null|0.00|null
		// credit|cash|line|null|0.00|null
		DataProvider data = seos("AccountInfo");
		int service = data.getOrderOfServiceName("AccountInfo");
		int results = data.getNumberOfResults(service);
		if (results != 1)
			logger.warning(message("Account Information have %d recode.", results));
		//for (int result = 0; result < results; result++) {
			String[] string = data.getResult(service, 0);
			//logger.info(message("Credit=%s, Cash=%s, Line=%s", string[0], string[1], string[2]));
			budgets.put("credit", Long.valueOf(string[0].replace(".", "")));
			budgets.put("cash", Long.valueOf(string[1].replace(".", "")));
			budgets.put("line", Long.valueOf(string[2].replace(".", "")));
		//}
	}
	
	// Key is symbol
	private Map<String, Long> stocks = new HashMap<String, Long>();
	
	public void portfolio() {
		//      0|1|           2|           3|           4|             5|              6|
		// Symbol| |Market Price|Amount Price|Market Value|Unrealized P/L|%Unrealized P/L|
		//            7|8|              *9|           10|          11|12|13|14|15 
		// Realized P/L| |Available Volume|Actual Volume|Average Cost|  |  |  |
		DataProvider data = seos("Portfolio");
		int service = data.getOrderOfServiceName("Portfolio");
		int results = data.getNumberOfResults(service);
		for (int result = 0; result < results; result++) {
			String[] string = data.getResult(service, result);
			//
			//System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
			//		string[0],string[1],string[2],string[3],string[4],string[5],string[6],string[7],string[8],string[9],string[10],string[11],string[12],string[13],string[14],string[15]);
			synchronized (stocks) {
				stocks.put(string[0], Long.valueOf(string[9]));
			}
		}
	}
	
	public long checkStock(String symbol) {
		synchronized (stocks) {
			return stocks.get(symbol);
		}
	}
	
	protected DataProvider placeOrder(String symbol, String order, double price, long volue) {
		Form form = Form.form();
		//FormEntity formEntity = new FormEntity(null);
		form.add("txtTerminalType", "streaming");
		form.add("type", "place");
		form.add("positionType", "");
		form.add("txtClientType", "");
		form.add("txtNvdr", "");
		form.add("txtBorS", order);
		form.add("txtPublishVol", "");
		form.add("txtSymbol", symbol);
		form.add("txtAccountNo", getAccountNo()); // AccountNo
		form.add("txtQty", String.valueOf(volue));
		form.add("Service", "PlaceOrder");
		form.add("txtPrice", String.valueOf(price));
		form.add("txtCondition", "DAY");
		form.add("txtPIN_new", pin); // Pin
		form.add("confirmedWarn", "");
		form.add("txtOrderNo", "");
		form.add("txtPriceType", "limit");
		return seos(form);
	}
	
	protected DataProvider cancelOrder(String symbol, String orderNo) {
		Form form = Form.form();
		form.add("txtCancelSymbol", symbol);
		form.add("type", "cancel");
		form.add("Service", "PlaceOrder");
		form.add("txtTerminalType", "streaming");
		form.add("positionType", "");
		form.add("txtBorS", "");
		form.add("txtNewATOATC", "");
		form.add("extOrderNo", null);
		form.add("txtClientType", "");
		form.add("txtAccountNo", getAccountNo()); // AccountNo
		form.add("txtPrice", "");
		form.add("txtNvdr", "");
		form.add("txtPIN_new", pin); // Pin
		form.add("txtQty", "");
		form.add("txtSymbol", "");
		form.add("txtOrderNo", orderNo);
		return seos(form);
	}
	
	
	protected void setting(String name, String value) {
		try {
			// optionName=TICKER_EQUITY_FILTER
			name = URLEncoder.encode(name,Charset.defaultCharset().name());
			// optionValue=<SUM:N|Y>|<SET|TFEX>|Foreign#Warrant|SET50#Options
			value = URLEncoder.encode(name,Charset.defaultCharset().name());
			String  request = String.format("https://%s%s?service=S4Setting&optionName=%s&optionValue=%s",
					getFlashVars("fvPrimaryHost"), getFlashVars("fvDataStrServlet"), name, value);
			browser.get(request);
			logger.info(message("Cache in \"%s\"", browser.getFileContent()));
			if (browser.getStatusCode() != 200) {
				logger.severe(message("HTTP Status %s \"%s\"",browser.getStatusCode(), browser.getReasonPhrase()));
				return;
			}
		} catch (UnsupportedEncodingException e) {
			logger.severe(message(e));
		}
		
	}
	
	protected DataProvider dataProvider(Form form) {
		browser.post(String.format("https://%s%s", getFlashVars("fvPrimaryHost"), getFlashVars("fvDataStrServlet")), 
				form);
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		if (browser.getStatusCode() != 200) {
			logger.severe(message("HTTP Status %s \"%s\"",browser.getStatusCode(), browser.getReasonPhrase()));
			return null;
		}
		DataProvider dataProvider = new DataProvider().read(browser.getInputStream());
		return dataProvider;
	}
	
	protected DataProviderBinary dataProviderBinary(Browser browser, Form form) {
		if (!browser.getContext().equals(getHttpContext()))
			browser.setContext(getHttpContext());
		browser.post(String.format("https://%s%s", getFlashVars("fvPrimaryHost"), getFlashVars("fvDataBinServlet")), 
				form);
		logger.fine(message("Cache in \"%s\"", browser.getFileContent()));
		if (browser.getStatusCode() == 200) {
			try {
				DataProviderBinary dataBinary = new DataProviderBinary();
				dataBinary.read(browser.getInputStream());
				return dataBinary;
			} catch (ObjectException e) {
				if (e.getMessage().equals("Unauthorised Access")) {
					logger.warning(message("Unauthorised Access"));
					processLogin();
					return dataProviderBinary(browser, form);
				} else
					throw e;
			}
			//storeMarketTicker(dataBinary.getMarketTicker());
			//storeBidOffer(dataBinary.getBidOffer());
		} else {
			logger.severe(message("HTTP Response %s \"%s\"", browser.getStatusCode(), browser.getReasonPhrase()));
			return null;
		}	
	}
	
	protected DataProviderBinary dataProviderBinary(Browser browser, String service) {
		Form form = Form.form();
		logger.finest(message("DataProviderBinary service=\"%s\"",service));
		form.add("service", service);
		form.add("mode", mode);
		boolean updateSequenceId = false;
		String[] services = service.split(",");
		for (int index = 0; index < services.length; index++) {
			Streaming4 streaming4 = Streaming4.getService(services[index]);
			switch (streaming4) {
			case MarketSummary:
				break;
			case InstrumentTicker:
				form.add("newMarket", newMarket);
				form.add("newInstTicker", newInstTicker);
				form.add("sequenceId", sequenceId);
				form.add("oldMarket", oldMarket);
				form.add("oldInstTicker", oldInstTicker);
				oldMarket = "";
				oldInstTicker = "";
				break;
			case InstrumentInfo:
				form.add("initiatedFlag", initiatedFlag);
				form.add("newInstInfo", newInstInfo);
				form.add("oldInstInfo", oldInstInfo);
				oldInstInfo = "";
				break;
			case MarketTicker:
				logger.finest(message("sequenceId2=\"%s\", optionSequenceId2=\"%s\"",sequenceId2,optionSequenceId2));
				if (newMarket2.equals("A") || newMarket2.equals("D"))
					form.add("optionSequenceId2", optionSequenceId2);
				if (newMarket2.equals("A") || newMarket2.equals("E"))
					form.add("sequenceId2", sequenceId2);
				form.add("newMarket2", newMarket2);
				form.add("newInstTicker2", newInstTicker2);
				form.add("newSum2", newSum2);
				form.add("oldMarket2", oldMarket2);
				form.add("oldInstTicker2", oldInstTicker2);
				form.add("oldSum2", oldSum2);
				oldMarket2 = "";
				oldInstTicker2 = "";
				oldSum2 = "";
				updateSequenceId = true;
				break;
			default:
				logger.warning(message("Unknow service \"%s\"",services[index]));
				break;
			}
		}
		DataProviderBinary dataBinary = dataProviderBinary(browser, form);
		if (updateSequenceId) {
			if (dataBinary.getSequenceId() != -1)
				sequenceId2 = String.valueOf(dataBinary.getSequenceId());
			if (dataBinary.getOptionSequenceId() != -1)
				optionSequenceId2 = String.valueOf(dataBinary.getOptionSequenceId());
		}
		return dataBinary;
	}
	
	protected void setNewMarket(String newMarket) {
		if (!this.newMarket.equals(newMarket)) {
			if (this.oldMarket.equals(""))
				this.oldMarket = this.newMarket;
			this.newMarket = newMarket;
		}
	}
	
	protected void setNewMarket2(String newMarket2) {
		if (!this.newMarket2.equals(newMarket2)) {
			if (this.oldMarket2.equals(""))
				this.oldMarket2 = this.newMarket2;
			this.newMarket2 = newMarket2;
		}
	}
	
	protected void setNewInstTicker(String newInstTicker) {
		if (!this.newInstTicker.equals(newInstTicker)) {
			if (this.oldInstTicker.equals(""))
				this.oldInstTicker = this.newInstTicker;
			this.newInstTicker = newInstTicker;
		}
	}
	
	protected void setNewSum2(String newSum2) {
		if (!this.newSum2.equals(newSum2)) {
			if (this.oldSum2.equals(""))
				this.oldSum2 = this.newSum2;
			this.newSum2 = newSum2;
		}
	}
	
	protected void setNewInstInfo(String newInstInfo) {
		if (!this.newInstInfo.equals(newInstInfo)) {
			if (this.oldInstInfo.equals(""))
				this.oldInstInfo = this.newInstInfo;
			this.newInstInfo = newInstInfo;
		}
	}
	
	private void createStoreMarketTicker(String[] tickers) {
		String comment = String.format("%s \"%s\"", task, browser_for_runnable.getFileContent());
		newThread(String.format(Locale.US, "StoreMarketTicker(%1$s)", Trigger.datetime(new Date())),
				this, "threadStoreMarketTicker", comment, tickers);
	}
	
	public void threadStoreMarketTicker(java.lang.Object... arguments) {
		if ((arguments[0] instanceof String) &&
			(arguments[1] instanceof String[])) {
			storeMarketTicker((String) arguments[0],  (String[]) arguments[1]);
		} else
			throw newObjectException(ERROR_ARGUMENTS_MISMATCH,"Arguments mismatch");
	}
	
	private void storeMarketTicker(String comment, String[] tickers) {
		if (tickers == null)
			return;
		Date current = new Date();
		@SuppressWarnings("static-access")
		String filename = String.format(Locale.US, "%1$s%2$sticker.%3$s.txt", 
				path.var, file.separator, file.date(current));
		Storage storage  = Storage.getInstance(filename);
		try {
			OutputStreamWriter output = new OutputStreamWriter(storage.append());
			output.write(String.format("#-- %s%n", comment));
			
			String date = OS.date(current);
			for (int index = 0; index < tickers.length; index++) {
				String[] tokens = tickers[index].split(",");
				//String date;
				String type = tokens[0];
				String market = tokens[1];
				String N = tokens[2];
				String time = String.format("%s %s", date, tokens[3]);
				String side = tokens[4];
				String price = tokens[5];
				String close = tokens[6];
				String change = tokens[7];
				String change_percent = tokens[8];
				String sequence = tokens[9];
				String a = tokens[10];
				String b = tokens[11];
				String volume = tokens[12];
				String symbol = tokens[13];
				output.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", 
						date,type,market,N,time,side,price,close,change,change_percent,sequence,a,b,volume,symbol));
				StreamingTicker ticker = Symbol.createTicker(
						date,type,market,N,time,side,price,close,change,change_percent,sequence,a,b,volume,symbol);
				Symbol.putTicker(ticker);
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
		
	}
		
	private class BidOffer {
		public String bid_volume = "";
		public String offer_volume = "";
	}
	
	private void storeBidOffer(String comment, String[] bid_offer, Map<String,String> symbol_date) {
		Date current = new Date();
		@SuppressWarnings("static-access")
		String filename = String.format(Locale.US, "%1$s%2$sbid.offer.%3$s.txt", 
				path.var, file.separator, file.date(current));
		Storage storage  = Storage.getInstance(filename);

		try {
			
			OutputStreamWriter output = new OutputStreamWriter(storage.append());
			output.write(String.format("#-- %s%n", comment));
			
			for (int index = 0; index < bid_offer.length; index++) {
				// bid_offer = symbol,
				//		bid_1,bid_vol_1,bid_2,bid_vol_2,bid_3,bid_vol_3,bid_4,bid_vol_4,bid_5,bid_vol_5,
				//		offer_1,offer_vol_1,offer_2,offer_vol_2,offer_3,offer_vol_3,offer_4,offer_vol_4,offer_5,offer_vol_5
				Map<String,BidOffer> prices = new HashMap<String,BidOffer>();
				String[] token = bid_offer[index].split(",");
				String symbol = token[0];
				String datetime = String.format(Locale.US, "%1$s %2$s", OS.date(current), symbol_date.get(symbol));
				// Bid Order
				for (int order = 0; order < 5; order++) {
					String price = token[1 + (order * 2)];
					String bid_volume = token[2 + (order * 2)];
					if (Float.valueOf(price) != 0.00) {
						BidOffer bid = new BidOffer();
						bid.bid_volume = bid_volume;
						prices.put(price, bid);
					}
				}
				// Offer Order
				for (int order = 0; order < 5; order++) {
					String price = token[11 + (order * 2)];
					String offer_volume = token[12 + (order * 2)];
					if (Float.valueOf(price) != 0.00) {
						BidOffer offer = prices.get(price);
						if (offer == null)
							offer = new BidOffer();
						offer.offer_volume = offer_volume;
						prices.put(price, offer);
					}
				}
				
				prices.keySet().iterator();
				for (Iterator<String> keys = prices.keySet().iterator(); keys.hasNext();) {
					String price = keys.next();
					BidOffer volume = prices.get(price);
					output.write(String.format("%s,%s,%s,%s,%s%n", 
							symbol,datetime,price,volume.bid_volume,volume.offer_volume));
					StreamingBidsOffers bids_offers = Symbol.createBidOffer(symbol,datetime,price,volume.bid_volume,volume.offer_volume);
					Symbol.setBidOffer(bids_offers);
				}	
			}
			
			output.flush();
			//output.close();
			storage.release();
		} catch (FileNotFoundException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			logger.severe(message(comment));
			logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
		
	protected String runService = String.format("%s,%s", Streaming4.MarketSummary, Streaming4.MarketTicker);
	protected String runInstInfo = "";
	protected String runInstTicker = "";
	protected String runMarket = "";
	protected String runMarket2 = "E";
	protected String runInstTicker2 = "_all";
	protected String runSum2 = "Y";
	
	private String task = String.format("Trigger \"%s\" cache in", this.getClass().getSimpleName());;
	
	public void setupRun(String service) {
		this.runService = service;
	}
	
	private void createInstrumentInfo(Map<String, String> symbol_date) {
		newThread(String.format(Locale.US, "InstrumentInfo(%1$s)", Trigger.datetime(new Date())),
				this, "threadInstrumentInfo", task, symbol_date);
	}
	
	@SuppressWarnings("unchecked")
	public void threadInstrumentInfo(java.lang.Object... arguments) {
		if ((arguments[0] instanceof String) &&
			(arguments[1] instanceof Map)) {
			getInstrumentInfo((String) arguments[0], (Map<String,String>) arguments[1]);
		} else
			throw newObjectException(ERROR_ARGUMENTS_MISMATCH,"Arguments mismatch");
	}
	
	private void getInstrumentInfo(String task, Map<String,String> symbol_date) {
		// Assign Begin "symbol list"
		String symbolList = "";
		Iterator<String> symbols = symbol_date.keySet().iterator();
		while (symbols.hasNext()) {
			symbolList += (String) symbols.next() + ",";
		}
		try {
			symbolList = symbolList.substring(0, symbolList.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			symbolList = "";
		}
		// Assign End "symbol list"  
		if (!symbolList.equals("")) {
			Browser browser = new Browser(getHttpContext());
			setNewInstInfo(symbolList);
			DataProviderBinary dataBinary = 
					dataProviderBinary(browser, Streaming4.InstrumentInfo.toString());
			String[] bids_offers = dataBinary.getBidOffer();
			//symbol_date = checkSymbolDate(symbol_date);
			String comment = String.format("%s \"%s\"", task, browser.getFileContent());
			storeBidOffer(comment, bids_offers, symbol_date);
		}
	}
		
	public void run() {
		browser_for_runnable.setContext(getHttpContext());
		setNewInstInfo(runInstInfo);
		setNewInstTicker(runInstTicker);
		setNewMarket(runMarket);
		setNewMarket2(runMarket2);
		setNewSum2(runSum2);
		DataProviderBinary dataBinary = dataProviderBinary(browser_for_runnable, runService);
		// new Thread
		createStoreMarketTicker(dataBinary.getMarketTicker());
		createInstrumentInfo(dataBinary.getSymbolDate());
		//newThread(String.format(Locale.US, "InstrumentInfo(%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS)", new Date()),
		//		this, "threadInstrumentInfo", dataBinary.getSymbolDate());
		nextTask(5);
	}
	
	protected void nextTask(int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, second);
		long time = calendar.getTimeInMillis();
		
		Trigger trigger = Trigger.getInstance();
		//trigger.start();
		String next = String.format(Locale.US, "%1$s \"%2$s\"", this.getClass().getSimpleName(), Trigger.datetime(time));
		logger.info(message("Next %1$s", next));
		task = String.format("%1$s cache in", next);
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$s)", 
				this.getClass().getSimpleName(), Trigger.datetime(time)),5));
	}
	
	public void buy(int source, String symbol, double price) {
		long volume = 100;
		logger.info(message("%s buy %s,%.2f,%d", getName(), symbol, price, volume));
		//buy(symbol,price,100);
	}
	
	public long[] buy(String symbol, double price, long volume) {
		long cost = Math.round(price * volume * (100 + getCommissionRate()));
		long budget = 0;
		synchronized (budgets) {
			try {
				budget = budgets.get("line");
			} catch (NullPointerException e) {
				processLogin();
				budget = budgets.get("line");
			}
		}
		if (budget >= cost) {
			placeOrder(symbol, "B", price, volume);
			return refreshStatus();
		}
		return null;
	}
	
	public void sell(int source, String symbol, double price) {
		long volume = 100;
		logger.info(message("%s sell %s,%.2f,%d", getName(), symbol, price, volume));
		//sell(symbol,price,100);
	}
	
	public long[] sell(String symbol, double price, long volume) {
		long stock = 0;
		synchronized (stocks) {
			try {
				stock = stocks.get(symbol);
			} catch (NullPointerException e) {
				processLogin();
				stock = stocks.get(symbol);
			}
		}
		if (stock >= volume) {
			placeOrder(symbol, "S", price, volume);
			return refreshStatus();
		}
		return null;
	}
	
	public boolean cancel(String symbol,  String orderNo) {
		cancelOrder(symbol, orderNo);
		refreshStatus();
		return false;
	}
	
	protected long[] refreshStatus() {
		long[] result = orderStatus();
		accountInfo();
		portfolio();
		return result;
	}
}
