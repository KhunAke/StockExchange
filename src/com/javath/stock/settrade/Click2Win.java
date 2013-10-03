package com.javath.stock.settrade;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.http.client.fluent.Form;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.Configuration;
import com.javath.ObjectException;
import com.javath.util.Browser;
import com.javath.util.html.FormFilter;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.InputFilter;
import com.javath.util.html.ParamFilter;

public class Click2Win extends FlashStreaming {
	
	private static String defaultUserName;
	
	private String username;
	private String password;
	
	//protected String accountNo;
	//protected String pin;
	//protected String url_synctime; 
	//protected String url_seos; 
	//protected String url_dataprovider;
	//protected String url_dataproviderbinary;
	
	//protected Browser browser;
	
	static {
		try {
			Properties properties = new Properties();

			String configuration = path.etc + file.separator
					+ "click2win.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.broker.settrade",
							configuration));
			properties.load(propsFile);
			defaultUserName = properties.getProperty("default");
			int numberOfAccount = Integer.valueOf(properties.getProperty("number_of_account","0"));
			for (int index = 0; index < numberOfAccount; index++) {
				String username = properties.getProperty("username." + (index+1));
				String password = Configuration.decrypt(properties.getProperty("password." + (index+1)));
				Click2Win broker = new Click2Win(username, password);
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
	
	public static Click2Win getInstance() {
		return (Click2Win) getBroker(defaultUserName, 
				Click2Win.class.getCanonicalName());
	}
	
	public Click2Win(String username, String password) {
		this.username = username;
		this.password = password;
		browser = new Browser(getHttpContext());
	}
	
	private static String[] login_request = {
			"http://click2win.settrade.com/SETClick2WIN/index.jsp",
			"https://click2win.settrade.com/LoginRepOnRole.jsp",
			"https://click2win.settrade.com/LoginBySystem.jsp",
			"/SETClick2WIN/SelectUserLeague.jsp",
			"https://click2win.settrade.com/SETClick2WIN/Welcome.jsp"
		};
	
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
				if (browser.getStatusCode() == 302) {
					logger.info(message("State-%d/%d %s.", state, login_request.length - 1, browser.getReasonPhrase()));
					continue;
				}
				logger.severe(message("State-%d/%d HTTP Response %s \"%s\"", 
						state, login_request.length - 1, browser.getStatusCode(), browser.getReasonPhrase()));
				return browser.getContext();
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
					return browser.getContext();
				}		
			} else if ((state + 1) == login_request.length) {
				logger.info(message("State-%d/%d Authentication Success.", state, login_request.length - 1));
			}
		}
		loadFlashVars();
		refreshStatus();
		return browser.getContext();	
	}
	
	@Override
	protected void loadFlashVars() {
		HtmlParser parser = new HtmlParser(null);
		//https://click2win.settrade.com/realtime/streaming4/flash/Streaming4Screen.jsp
		String streamingPage = "https://click2win.settrade.com/realtime/streaming4/flash/Streaming4Screen.jsp";
		parser.setInputStream(browser.get(streamingPage));
		if (browser.getStatusCode() != 200) {
			throw new ObjectException(browser.getReasonPhrase());
		}
		ParamFilter paramFilter = new ParamFilter(parser.parse());
		paramFilter.filter();
		// 
		NamedNodeMap attributes = paramFilter.name("FlashVars").getAttributes();
		for (int index = 0; index < attributes.getLength(); index++) {
			if (attributes.item(index).getNodeName().equals("value"))
				setFlashVars(attributes.item(index).getNodeValue().split("[&]"));
		}
	}
	
	private Form buildForm(Node node) {
		//HtmlParser.print(node);
		if (node == null)
			return Form.form();
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

	@Override
	public double getCommissionRate() {
		return 0.1605;
	}

	@Override
	public String getName() {
		if (name == null)
			name = String.format("%s@%s", username, this.getClass().getCanonicalName());
		return name;
	}

}
