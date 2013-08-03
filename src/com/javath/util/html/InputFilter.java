package com.javath.util.html;

import org.w3c.dom.Node;

public class InputFilter extends Filter {

	public InputFilter(Node node) {
		super(node);
	}

	@Override
	protected boolean condition(Node node) {
		// TODO Auto-generated method stub
		return node.getNodeName().equals("INPUT") || node.getNodeName().equals("SELECT");
	}

}
