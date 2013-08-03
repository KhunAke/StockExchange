package com.javath.util.html;

import org.w3c.dom.Node;

public class LinkFilter extends Filter {

	public LinkFilter(Node node) {
		super(node);
	}

	@Override
	protected boolean condition(Node node) {
		return node.getNodeName().equals("A");
	}

}
