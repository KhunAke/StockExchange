package com.javath.stock;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.client.fluent.Form;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.stock.settrade.FlashStreaming;
import com.javath.util.Browser;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.FormFilter;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.InputFilter;
import com.javath.util.html.TextNode;

public class BrokerBualuang extends FlashStreaming {
	
	private String username;
	private String password;
	//protected String accountNo;
	//protected String pin;
	//protected String url_synctime; 
	//protected String url_seos; 
	//protected String url_dataprovider;
	//protected String url_dataproviderbinary;
	
	private HtmlParser parser = new HtmlParser(null);
	private FormFilter formFilter = new FormFilter(null);
	
	private String[] login_state = {
		"http://www.bualuang.co.th/th/index.php",
		"https://ent.bualuang.co.th/auth/ws_login.php",
		"https://wwwa1.settrade.com/sso/SSOUMEquityRedirect.jsp",
		"https://we06.settrade.com/LoginRepOnRole.jsp",
		"https://we06.settrade.com/LoginBySystem.jsp",
		"/brokerpage/001/StaticPage/home_content/en/home_content.html?txtAccountNo="
	};
	
	protected void url_init() {
		url_synctime = "https://wmi2.settrade.com/realtime/streaming4/synctime.jsp";
		url_seos = "https://we06.settrade.com/daytradeflex/streamingSeos.jsp";
		url_dataprovider = "https://wwwc11.settrade.com/realtime/streaming4/Streaming4DataProvider.jsp";
		url_dataproviderbinary = "https://wwwc11.settrade.com/realtime/streaming4/Streaming4DataProviderBinary.jsp";
	}
	
	public BrokerBualuang(String username, String password) {
		this.url_init();
		this.username = username;
		this.password = password;
		this.browser = new Browser();
		// Modify URL with append "username" 
		login_state[login_state.length -1] += username;
	}
	
	public void login() {
		Form form = null;
		for (int state = 0; state < login_state.length; state++) {
			if (form == null)
				parser.setInputStream(
						browser.get(login_state[state]));
			else
				parser.setInputStream(
						browser.post(login_state[state], form));
			if (browser.getStatusCode() != 200) {
				logger.severe(message("State-%d HTTP Response %s \"%s\"", 
						state, browser.getStatusCode(), browser.getReasonPhrase()));
				return;
			}
				
			formFilter.setNode(parser.parse()).filter();
			//formFilter.print();
			if ((state + 1) < login_state.length) {
				logger.info(message("State-%d Success.", state));
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
				// Cookie Received : __txtUserRef, __txtBrokerId
				browser.get("https://we06.settrade.com/mylib.jsp?txtBrokerId=001");
				logger.finest(message("__txtUserRef=%s", browser.getCookie("__txtUserRef")));
				logger.finest(message("__txtBrokerId=%s", browser.getCookie("__txtBrokerId")));
				
				browser.get(String.format("https://we06.settrade.com/multimarket/redirect_page.jsp?" +
						"txtPage=streaming4&" +
						"brokerid=%s&" +
						"userref=%s&" +
						"resolution=1360&$%d", 
						browser.getCookie("__txtBrokerId"),
						browser.getCookie("__txtUserRef"),
						new Date().getTime() ));
				/**
					"https://wmc2.settrade.com/C00_DefaultRedirectRealtime.jsp"
						search -> input type='hidden' name='txtEquityAccountInfo' value='T|5221172|5221172~FIS~CASH_BALANCE~Y~N~ ~ ' 
					"https://wwwe13.settrade.com/realtime/streaming4/Streaming4RegisterOrderStatus.jsp"
						newAccountNo="5221172"
						newMkt="E"
						oldAccountNo=""	
						oldMkt=""	
				 */
				//browser.printContent();
				//System.out.println();
				//formFilter.print();
				//browser.printCookie();
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
	
	public void portfolioMovement() {
		browser.get("http://realtime.bualuang.co.th/myeasy/auth/auth1.php3?menu=port&page=port_movement");
		HtmlParser parser = new HtmlParser(browser.getInputStream());
		
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(new CustomHandler() {

			public boolean condition(Node node) {
				return node.getNodeName().equals("TABLE");
			}
			
		});
		List<Node> nodes = filter.filter(7);
		TextNode textNode =new TextNode(nodes.get(3));
		
		//TextNode textNode =new TextNode(parser.parse());
		
		textNode.print();
	}

	@Override
	public void buy(String symbol, double price, long volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sell(String symbol, double price, long volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel(String symbol, String orderNo) {
		// TODO Auto-generated method stub
		
	}
	
}
