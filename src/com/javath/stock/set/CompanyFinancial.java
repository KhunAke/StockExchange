package com.javath.stock.set;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Node;

import com.javath.Object;
import com.javath.util.Browser;
import com.javath.util.html.CustomFilter;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class CompanyFinancial extends Object implements Runnable, CustomHandler {
	
	private String symbol;
	
	//private CustomFilter filter = new CustomFilter(null);
	public CompanyFinancial(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public static void main(String[] args) {
		new CompanyFinancial("PTT").run();
	}
	
	@Override
	public void run() {
		this.getWebPage();
	}
	
	private void getWebPage() {
		Browser browser = new Browser();
		InputStream inputStream = browser.get("http://www.settrade.com/C04_03_stock_companyhighlight_p1.jsp?txtSymbol=PTT&selectPage=3");
		HtmlParser parser = new HtmlParser(inputStream);
	
		//	browser.get("http://www.set.or.th/set/companyhighlight.do?symbol=PTT&language=en&country=US"));
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter();
		//for (int index = 0; index < nodes.size(); index++) {
			TextNode textNode = new TextNode(nodes.get(1));
			//textNode.print();
		//}
		int step = 0;
		for (int index = 1; index < textNode.length(); index++) {
			String[] stringArray = textNode.getStringArray(index);
			if (step == 0) {
				if (stringArray[0].equals("1") && (stringArray.length == 21)) {
					//System.out.println("length = " + stringArray.length);
					step = 1;
					//textNode.printStringArray(stringArray);
				} 
			} else {
				if (stringArray[0].equals("1"))
					break;
				if ((step == 1) && stringArray[0].equals("2")) {
					for (int position = 0; position < stringArray.length; position++) {
						System.out.println(stringArray[position].trim());
					}
					step = 2;
				}
				if (stringArray[0].equals("3")) {
					
				}
				textNode.printStringArray(stringArray);
			}
		}
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
	
}
