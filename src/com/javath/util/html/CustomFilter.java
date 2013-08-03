package com.javath.util.html;

import org.w3c.dom.Node;

public class CustomFilter extends Filter {
	
	private CustomHandler handler;

	public CustomFilter(Node node) {
		super(node);
	}
	
	public CustomFilter setHandler(CustomHandler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	protected boolean condition(Node node) {
		return handler.condition(node);
	}

}
