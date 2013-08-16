package com.javath.util.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.javath.Object;
import com.javath.ObjectException;

public class HtmlParser extends Object {

	private InputSource source;
	private DocumentFragment fragment;
	
	public HtmlParser(InputStream source) {
		this.setInputStream(source);
	}
	
	public HtmlParser(InputStream source, String charset) { 
        this.setInputStream(source, charset);
	}
	
	public HtmlParser setInputStream(InputStream source) {
		setInputStream(source, Charset.defaultCharset().toString());
		return this;
	}
	
	public HtmlParser setInputStream(InputStream source, String charset) {
		this.source = new InputSource(source);
        this.source.setEncoding(charset);
        this.fragment = null;
		return this;
	}
	
	public DocumentFragment parse() {
		try {
			DOMFragmentParser parser = new DOMFragmentParser();
			HTMLDocument document = new HTMLDocumentImpl();
			
			if (fragment == null) {
				fragment = document.createDocumentFragment();
				parser.parse(source, fragment);
				source = null;
			}
			//travel(null,fragment);
			return fragment;
		} catch (SAXException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	public static String attribute(Node node, String name) {
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if (attribute.getNodeName().equals(name))
				return attribute.getNodeValue();
		}
		return null;
	}
	
	public static String text(Node node) {
		StringBuffer string = new StringBuffer();
		if (node.getNodeName().equals("#text"))
			string.append(node.getNodeValue());
		Node child = node.getFirstChild();
		while (child != null) {
			string.append(text(child));
            child = child.getNextSibling();
        }
		return string.toString().trim();
	}
	
	public static void print(Node node) {
		print("", node);
	}
	
	private static void print(String space, Node node) {
		printNode(space, node);
		Node child = node.getFirstChild();
		while (child != null) {
			print(space + " ", child);
            child = child.getNextSibling();
        }
	}
	
	public static void printNode(String space, Node node) {
		StringBuffer string = new StringBuffer();
		NamedNodeMap attributes = node.getAttributes();
		try {
			string.append(node.getNodeName());
			string.append(": ");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				string.append(attribute.getNodeName());
				string.append("=\"");
				string.append(attribute.getNodeValue());
				string.append("\", ");
			}
			if (string.charAt(string.length() - 2) == ',')
				string.delete(string.length() - 2, string.length());
		} catch (NullPointerException e) {} //bypass 
		System.out.println(space + (string.toString() + " : " + node.getNodeValue()).trim());
	}
	
	public static String stringNode(Node node) {
		StringBuffer string = new StringBuffer();
		NamedNodeMap attributes = node.getAttributes();
		try {
			string.append(node.getNodeName());
			string.append(": ");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				string.append(attribute.getNodeName());
				string.append("=\"");
				string.append(attribute.getNodeValue());
				string.append("\", ");
			}
			if (string.charAt(string.length() - 2) == ',')
				string.delete(string.length() - 2, string.length());
		} catch (NullPointerException e) {} //bypass
		return (string.toString() + " : " + node.getNodeValue()).trim();
	}
	
}
