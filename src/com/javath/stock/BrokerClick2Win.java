package com.javath.stock;

import java.util.List;



import org.apache.http.client.fluent.Form;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.stock.settrade.FlashStreaming;
import com.javath.util.Browser;
import com.javath.util.html.FormFilter;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.InputFilter;

public class BrokerClick2Win extends FlashStreaming {
	
	private String username;
	private String password;
	
	//protected String accountNo;
	//protected String pin;
	//protected String url_synctime; 
	//protected String url_seos; 
	//protected String url_dataprovider;
	//protected String url_dataproviderbinary;
	
	//protected Browser browser;
	
	
	protected void url_init() {
		url_synctime = "https://click2win.settrade.com/realtime/streaming4/synctime.jsp";
		url_seos = "https://click2win.settrade.com/daytradeflex/streamingSeos.jsp";
		url_dataprovider = "https://pushctw1.settrade.com/realtime/streaming4/Streaming4DataProvider.jsp";
		url_dataproviderbinary = "https://pushctw1.settrade.com/realtime/streaming4/Streaming4DataProviderBinary.jsp";
	}
	
	public BrokerClick2Win(String username, String password) {
		this.url_init();
		this.username = username;
		this.password = password;
		this.browser = new Browser();
	}
	
	private static String[] login_request = {
			"http://click2win.settrade.com/SETClick2WIN/index.jsp",
			"https://click2win.settrade.com/LoginRepOnRole.jsp",
			"https://click2win.settrade.com/LoginBySystem.jsp",
			"/SETClick2WIN/SelectUserLeague.jsp",
			"https://click2win.settrade.com/SETClick2WIN/Welcome.jsp"
		};
	
	public void login() {
		Form form = null;
		HtmlParser parser = new HtmlParser(null);
		FormFilter formFilter = new FormFilter(null);
		for (int state = 0; state < login_request.length; state++) {
			if (form == null)
				parser.setInputStream(
						browser.get(login_request[state]));
			else
				parser.setInputStream(
						browser.post(login_request[state], form));
			if (browser.getStatusCode() != 200) {
				if (browser.getStatusCode() == 302) {
					logger.info(message("State-%d/%d %s.", state, login_request.length - 1, browser.getReasonPhrase()));
					continue;
				}
				logger.severe(message("State-%d/%d HTTP Response %s \"%s\"", 
						state, login_request.length - 1, browser.getStatusCode(), browser.getReasonPhrase()));
				return;
			}
			formFilter.setNode(parser.parse()).filter();
			if ((state + 1) < login_request.length) {
				logger.info(message("State-%d/%d Success.", state, login_request.length - 1));
				if (state == 0)
					form = buildForm(formFilter.action(login_request[state + 1]));
				else
					form = formFilter.actionForm(login_request[state + 1]);
				if (form == null) {
					logger.severe(message("State-%d/%d FORM not found.", state, login_request.length - 1));
					return;
				}		
			} else if ((state + 1) == login_request.length) {
				logger.info(message("State-%d/%d Authentication Success.", state, login_request.length - 1));
			}
		}
	}
	
	private Form buildForm(Node node) {
		//HtmlParser.print(node);
		if (node == null)
			return null;
		Form form = Form.form();
		InputFilter inputFilter = new InputFilter(node);
		List<Node> inputs = inputFilter.filter();
		//inputFilter.print();
		
		for (int index = 0; index < inputs.size(); index++) {
			Node input = inputs.get(index);
			NamedNodeMap attributes = input.getAttributes();
			String name = null;
			String value = null;
			for (int item = 0; item < attributes.getLength(); item++) {
				Node attribute = attributes.item(item);
				if (attribute.getNodeName().equals("name"))
					name = attribute.getNodeValue();
				else if (attribute.getNodeName().equals("value")) 
					value = attribute.getNodeValue();
			}
			try {
				if (name.equals("txtLogin"))
					form.add(name, this.username);
				else if (name.equals("txtPassword")) {
					form.add(name, this.password);
				//} else if (name.equals("imageField")) {
				//	Random random = new Random();
				//	form.add("imageField.x", String.valueOf(random.nextInt(56)));
				//	form.add("imageField.y", String.valueOf(random.nextInt(13)));
				} else
					form.add(name, value);
			} catch (java.lang.NullPointerException e) {
				// variable name is null
				//logger.warning(message(HtmlParser.node(input)));
			}
		}
		
		return form;
	}
	
	public void buy(String symbol, double price, long volume) {
		placeOrder(symbol, "B", price, volume);
	}
	
	public void sell(String symbol, double price, long volume) {
		placeOrder(symbol, "S", price, volume);
	}
	
	public void cancel(String symbol,  String orderNo) {
		cancelOrder(symbol, orderNo);
	}
		
}
