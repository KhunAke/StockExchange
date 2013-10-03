package com.javath.stock.bualuang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.http.client.fluent.Form;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.Configuration;
import com.javath.ObjectException;
import com.javath.stock.settrade.DataProvider;
import com.javath.stock.settrade.FlashStreaming;
import com.javath.util.Browser;
import com.javath.util.html.FormFilter;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.InputFilter;
import com.javath.util.html.ParamFilter;

public class BrokerStreaming extends FlashStreaming {
	
	private static String defaultUserName;
	
	private String username;
	private String password;
	
	static {
		try {
			Properties properties = new Properties();

			String configuration = path.etc + file.separator
					+ "bualuang.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.bualuang",
							configuration));
			properties.load(propsFile);
			defaultUserName = properties.getProperty("default");
			int numberOfAccount = Integer.valueOf(properties.getProperty("number_of_account","0"));
			for (int index = 0; index < numberOfAccount; index++) {
				String username = properties.getProperty("username." + (index+1));
				String password = Configuration.decrypt(properties.getProperty("password." + (index+1)));
				String pin = Configuration.decrypt(properties.getProperty("pin." + (index+1)));
				BrokerStreaming broker = new BrokerStreaming(username, password, pin);
				putBroker(username, broker);
			}
			propsFile.close();
			
		} catch (FileNotFoundException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	public static BrokerStreaming getInstance() {
		return (BrokerStreaming) getBroker(defaultUserName, 
				BrokerStreaming.class.getCanonicalName());
	}
	
	public BrokerStreaming(String username, String password, String pin) {
		this.username = username;
		this.password = password;
		this.pin = pin;
		this.browser = new Browser(getHttpContext());
		login_request[login_request.length-1] += username;
	}
	
	private String[] login_request = {
		"http://www.bualuang.co.th/th/index.php",
		"https://ent.bualuang.co.th/auth/ws_login.php",
		"https://wwwa1.settrade.com/sso/SSOUMEquityRedirect.jsp",
		"https://we06.settrade.com/LoginRepOnRole.jsp",
		"https://we06.settrade.com/LoginBySystem.jsp",
		"/brokerpage/001/StaticPage/home_content/en/home_content.html?txtAccountNo="
	};
	
	@Override
	protected HttpContext login(Browser browser) {
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
				logger.severe(message("State-%d/%d HTTP Response %s \"%s\"", 
						state, login_request.length - 1, browser.getStatusCode(), browser.getReasonPhrase()));
				return null;
			}
				
			formFilter.setNode(parser.parse()).filter();
			//formFilter.print();
			if ((state + 1) < login_request.length) {
				logger.info(message("State-%d/%d Success.", state, login_request.length - 1));
				if (state == 0)
					form = buildForm(formFilter.action(login_request[state + 1]));
				else
					form = formFilter.actionForm(login_request[state + 1]);
				if (form == null) {
					logger.severe(message("State-%d/%d FORM not found.", state, login_request.length - 1));
					return null;
				}
						
			} else if ((state + 1) == login_request.length) {
				logger.info(message("State-%d/%d Authentication Success.",state, login_request.length - 1));
			}
		}
		loadFlashVars();
		refreshStatus();
		return browser.getContext();
	}
	
	@Override
	protected void loadFlashVars() {
		Form form = null;
		HtmlParser parser = new HtmlParser(null);
		FormFilter formFilter = new FormFilter(null);
		// Cookie Received : __txtUserRef, __txtBrokerId
		browser.get("https://we06.settrade.com/mylib.jsp?txtBrokerId=001");
		logger.finest(message("__txtUserRef=%s", browser.getCookie("__txtUserRef")));
		logger.finest(message("__txtBrokerId=%s", browser.getCookie("__txtBrokerId")));
		if  (browser.getCookie("__txtUserRef").equals("0")) {
			throw new ObjectException("Cookie name \"__txtUserRef\" not found.");
		}
		// Step 1 
		//   https://we06.settrade.com/multimarket/redirect_page.jsp 
		parser.setInputStream(
				browser.get(String.format("https://we06.settrade.com/multimarket/redirect_page.jsp?" +
						"txtPage=streaming4&" +
						"brokerid=%s&" +
						"userref=%s&" +
						"resolution=1360&$%d", 
						browser.getCookie("__txtBrokerId"),
						browser.getCookie("__txtUserRef"),
						new Date().getTime() )));
		formFilter.setNode(parser.parse()).filter();
		// search -> action URL
		//   https://*.settrade.com/C00_DefaultRedirectRealtime.jsp
		String action = null;
		NamedNodeMap attributes; 
		attributes = formFilter.action(Pattern.compile("/C00_DefaultRedirectRealtime.jsp$"))
				.getAttributes();
		for (int index = 0; index < attributes.getLength(); index++) {
			if (attributes.item(index).getNodeName().equals("action"))
				action = attributes.item(index).getNodeValue();
		}
		form = formFilter.actionForm(action);
		// Step 2 
		//   https://*.settrade.com/C00_DefaultRedirectRealtime.jsp
		parser.setInputStream(browser.post(action, form));
		formFilter.setNode(parser.parse()).filter();
		// Step 3 Redirect
		//   https://*/realtime/streaming4/flash/Streaming4Screen.jsp
		String streamingPage = "/realtime/streaming4/flash/Streaming4Screen.jsp";
		form = formFilter.actionForm(streamingPage);
		parser.setInputStream(
				browser.post(streamingPage, form));
		ParamFilter paramFilter = new ParamFilter(parser.parse());
		paramFilter.filter();
		// 
		attributes = paramFilter.name("FlashVars").getAttributes();
		for (int index = 0; index < attributes.getLength(); index++) {
			if (attributes.item(index).getNodeName().equals("value"))
				setFlashVars(attributes.item(index).getNodeValue().split("[&]"));
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
			if (name.equals("l_name"))
				form.add(name, username);
			else if (name.equals("passwd"))
				form.add(name, password);
			else if (name.equals("imageField")) {
				Random random = new Random();
				form.add("imageField.x", String.valueOf(random.nextInt(56)));
				form.add("imageField.y", String.valueOf(random.nextInt(13)));
			} else
				form.add(name, value);
			
		}
		
		return form;
	}

	@Override
	public double getCommissionRate() {
		return 0.1689;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
