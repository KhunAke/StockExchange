package com.javath.stock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.http.client.fluent.Form;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.Configuration;
import com.javath.ObjectException;
import com.javath.util.Browser;
import com.javath.util.html.FormFilter;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.InputFilter;


public class BrokerSettrade extends Broker{
	
	private String username;
	private String password;
	private Browser browser;
	
	private HtmlParser parser = new HtmlParser(null);
	private FormFilter formFilter = new FormFilter(null);
	
	private static String[] login_state = {
			"https://www.settrade.com/template/loginForm.jsp",
			//"https://www.settrade.com/LoginRepOnRole.jsp?txtLoginPage=login.jsp",
			"https://backup.settrade.com/LoginRepOnRole.jsp?txtLoginPage=login.jsp",
			"http://portal.settrade.com/LoginBySystem.jsp",
			"/C19_Member_FirstPage.jsp?txtBrokerId=IPO"
		};
	
	static {
		try {
			Properties properties = new Properties();

			String configuration = path.etc + file.separator
					+ "settrade.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.broker.settrade",
							configuration));
			String username = properties.getProperty("username");
			String password = Configuration.decrypt(properties.getProperty("password"));
			properties.load(propsFile);
			propsFile.close();
			
			
		} catch (FileNotFoundException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}

	
	
	public BrokerSettrade(String username, String password) {
		this.username = username;
		this.password = password;
		this.browser = new Browser();
		//this.login();
	}
	
	private Form buildForm(Node node) {
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
			if (name.equals("txtLogin"))
				form.add(name, username);
			else if (name.equals("txtPassword"))
				form.add(name, password);
			else if (name.equals("txtBrokerId"))
				form.add(name, "IPO");
			else if (name.equals("imageFieldGo")) {
				Random random = new Random();
				form.add("imageFieldGo.x", String.valueOf(random.nextInt(32)));
				form.add("imageFieldGo.y", String.valueOf(random.nextInt(16)));
			} else
				form.add(name, value);
			
		}
		
		return form;
	}
	
	public void login() {
		Form form = null;
		for (int state = 0; state < login_state.length; state++) {
			if (form == null)
				parser.setInputStream(
						browser.get(login_state[state]));
			else
				if (state == 1)
					parser.setInputStream(
							browser.post("https://www.settrade.com/LoginRepOnRole.jsp?txtLoginPage=login.jsp", form));
				else
					parser.setInputStream(
							browser.post(login_state[state], form));
			if (browser.getStatusCode() != 200) {
				logger.severe(message("State-%d HTTP Response %d %s", 
						state, browser.getStatusCode(), browser.getReasonPhrase()));
				return;
			}
				
			formFilter.setNode(parser.parse()).filter();
			if ((state + 1) < login_state.length) {
				logger.info(message("State-%d Success.",state));
				if (state == 0)
					form = buildForm(formFilter.action(login_state[state + 1]));
				else
					form = formFilter.actionForm(login_state[state + 1]);
				if (form == null) {
					logger.severe(message("State-%d FORM not found.", state));
					return;
				}
						
			} else if ((state + 1) == login_state.length) {
				logger.info(message("State-%d Authentication Success.",state));
				//System.out.println(browser.getReasonPhrase());
				browser.printContent();
			}
		}
	}
	
	public void portfolio() {
		String request = "http://portal.settrade.com/portfolio/stock_start.jsp?txtBrokerId=IPO&t=" + 
				Calendar.getInstance().getTimeInMillis();
		browser.get(request);
		if (browser.getStatusCode() == 200) {
			//FormFilter forms = new FormFilter(browser.getInputStream());
			//forms.print();
			return;
		}
		throw new ObjectException(String.format("Response %d %s", browser.getStatusCode(), browser.getReasonPhrase()));
	}
	
	public void buy(String symbol, double price, long volume) {
		String request = "http://www.settrade.com/portfolio/actions/stock_sendOrderRep.jsp";
		Form form = Form.form();
		form.add("BS", "B");
		form.add("symbol", symbol);
		form.add("volume", String.valueOf(volume));
		form.add("price", String.valueOf(price));
		form.add("chkComm", "on");
		form.add("comm", String.valueOf(CommissionRate) );
		browser.post(request, form);
		if (browser.getStatusCode() == 200)
			logger.info(message("HTTP Response %d %s", browser.getStatusCode(), browser.getReasonPhrase()));
		else {
			String message = String.format("HTTP Response %d %s", browser.getStatusCode(), browser.getReasonPhrase());
			logger.warning(message);
			throw new ObjectException(message);
		}
	}
	
	public void sell(String symbol, double price, long volume) {
		String request = "http://www.settrade.com/portfolio/actions/stock_sendOrderRep.jsp";
		Form form = Form.form();
		form.add("BS", "S");
		form.add("symbol", symbol);
		form.add("volume", String.valueOf(volume));
		form.add("price", String.valueOf(price));
		form.add("chkComm", "on");
		form.add("comm", "0.1689");
		browser.post(request, form);
		if (browser.getStatusCode() == 200)
			logger.info(String.format("HTTP Response %d %s", browser.getStatusCode(), browser.getReasonPhrase()));
		else {
			String message = String.format("HTTP Response %d %s", browser.getStatusCode(), browser.getReasonPhrase());
			logger.warning(message);
			throw new ObjectException(message);
		}
	}

	@Override
	public void cancel(String symbol, String orderNo) {
		// TODO Auto-generated method stub
	}
}
