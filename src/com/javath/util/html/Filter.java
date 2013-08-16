package com.javath.util.html;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.Object;

public abstract class Filter extends Object {
	
	private Node node; 
	protected List<Node> nodes;

	public Filter(Node node) {
		this.setNode(node);
	}
	
	public Filter setNode(Node node) {
		this.node = node;
		nodes = null;
		return this;
	}
	
	public List<Node> filter() {
		return filter(-1);
	}
	
	public synchronized List<Node> filter(int depth) {
		if (nodes == null) { 
			nodes = new LinkedList<Node>();
			synchronized(nodes) {
				this.scan(node, depth);
				node = null;
			}
		}
		return nodes;
	}
	
	protected abstract boolean condition(Node node);
	
	private void scan(Node node, int depth) {
		
		if (condition(node))
			nodes.add(node);
		
		Node child = node.getFirstChild();
        while (child != null) {
        	if (depth != 0)
        		scan(child, depth - 1);
            child = child.getNextSibling();
        }
	}
	
	public void print() {
		List<Node> nodes = this.nodes;
		for (int index = 0; index < nodes.size(); index++) {
			Node node = nodes.get(index);
			NamedNodeMap attributes = node.getAttributes();
			StringBuffer string = new StringBuffer();
			try {
				string.append(node.getNodeName());
				string.append(": ");
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);
					string.append(attribute.getNodeName());
					string.append("=");
					string.append(attribute.getNodeValue());
					string.append(", ");
				}
				if (string.charAt(string.length() - 2) == ',')
					string.delete(string.length() - 2, string.length());
			} catch (NullPointerException e) {} //bypass
			System.out.println((string.toString() + " : " + node.getNodeValue()).trim());
		}
	}
	
}
