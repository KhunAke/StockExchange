package com.javath.util.html;

import org.w3c.dom.Node;

public class TextFilter extends Filter {

	public TextFilter(Node node) {
		super(node);
	}

	@Override
	protected boolean condition(Node node) {
		return node.getNodeName().equals("#text");
	}
	
	public void print() {
		for (int index = 0; index < nodes.size(); index++) {
			Node node = nodes.get(index);
			System.out.println("\"" + node.getNodeValue().trim() + "\"");
			/*
			NamedNodeMap attributes = nodes.get(index).getAttributes();
			StringBuffer string = new StringBuffer();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				string.append(attribute.getNodeName());
				string.append("=");
				string.append(attribute.getNodeValue());
				string.append(", ");
			}
			System.out.println(string.substring(0, string.length() - 2));
			*/
		}
	}

}
