package com.javath.stock.settrade;

import java.util.Calendar;
import java.util.Locale;

import org.w3c.dom.Node;

import com.javath.Object;
import com.javath.ObjectException;
import com.javath.util.Browser;
import com.javath.util.TodoAdapter;
import com.javath.util.Trigger;
import com.javath.util.html.CustomHandler;
import com.javath.util.html.HtmlParser;

public class Market extends Object implements Runnable, CustomHandler {

	private Browser browser = new Browser();
	private HtmlParser parser = HtmlParser.poll();
	
	private static Market instance = new Market();
	
	private Market() {}
	
	public static Market getInstance() {
		return instance;
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

	@Override
	public void run() {
		//getWebPage();
		newThread(this, "threadWebPage");
		nextTask();
	}
	
	public void getWebPage() {
		logger.warning("Not implemented");
	}
	
	private void nextTask() {
		long time = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 5);
		time = calendar.getTimeInMillis();
		Trigger trigger = Trigger.getInstance();
		trigger.addTodo(new TodoAdapter(time, this, 
				String.format(Locale.US, "%1$s(%2$tY-%2$tm-%2$tdT%2$tH:%2$tM:%2$tS)", 
				this.getClass().getSimpleName(), time)));
	}
	
	@Override
	protected void finalize() throws Throwable {
		HtmlParser.offer(parser);
		super.finalize();
	}

}
