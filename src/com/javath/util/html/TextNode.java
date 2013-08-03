package com.javath.util.html;

import java.util.ArrayList;
import java.util.Stack;


import org.w3c.dom.Node;

import com.javath.Object;

public class TextNode extends Object {
	
	private char leftQuote;
	private char rightQuote;
	private char delimiter;

	private String[][] stringArray;	
	private Stack<String[]> stack = new Stack<String[]>();
	
	public TextNode() {
		this.setQuote("{}");
		this.setDelimiter('|');
	}
	
	public TextNode(Node node) {
		this();
		this.convert(node);
	}
	
	public void setQuote(String quote) {
		leftQuote = quote.charAt(0);
		rightQuote = quote.charAt(1);
	}
	
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
	
	public synchronized void convert(Node node) {
		stack.clear();
		stack.push(this.convert(0, node));
		this.reOrder();
	}
	
	private String[] convert(int depth, Node node) {
		Node child = node.getFirstChild();
		if (node.getNodeName().equals("#text")) {
			String text = node.getNodeValue();
			if (text == null)
				return new String[] {String.valueOf(depth) ,""};
			else
				return new String[] {String.valueOf(depth) , text.trim()};
		}
		if (node.getNodeName().equals("BR"))
			return new String[] {String.valueOf(depth) ,String.format("%s%s%s",leftQuote, "BR", rightQuote)};
		
		ArrayList<String> array = new ArrayList<String>();
		array.add(String.valueOf(depth));
		if (child == null)
			array.add("");
		while (child != null) { 
			String[]  childArray = convert(depth + 1, child);
			//printStringArray(childArray);
			if (childArray.length == 2) {
				array.add(childArray[1]);
			} else {
				//printStringArray(childArray);
				stack.push(childArray);
				array.add(String.format("%s%d%s",leftQuote, depth + 1, rightQuote));
			}
			child = child.getNextSibling();
		}
		String[] string = castArrayList(array);
		return string;
	}
	
	private String[] castArrayList(ArrayList<String> array) {
		String[] string = new String[array.size()];
		for (int index = 0; index < string.length; index++)
			string[index] = array.get(index);
		return string;
	}
	
	public void printStringArray(String[] object) {
		if (object.length == 0)
			System.out.print(leftQuote);
		else
			System.out.print(leftQuote + object[0].toString());
		for (int index = 1; index < object.length - 1; index++)
			System.out.print(delimiter + object[index].toString());
		if (object.length <= 1)
			System.out.println(rightQuote);
		else
			System.out.println(delimiter + object[object.length - 1].toString() + rightQuote);
	}
	
	public void printStringArray(int row) {
		printStringArray(getStringArray(row));
	}
	
	public void print() {
		for (int index = 0; index < stringArray.length; index++)
			printStringArray(index);
	}
	
	private void reOrder() {
		stringArray = new String[stack.size()][];
		int current = stack.size() - 1;
		for (int index = current; index > -1; index--)
			stringArray[index] = stack.pop();
		while (current > 0) {
			boolean loop = true;
			int source = current;
			while (loop) {
				int target = source - 1;
				if (target == -1)
					break;
				if (Integer.valueOf(stringArray[source][0]) < 
						Integer.valueOf(stringArray[target][0]))  {
					String[] temp = stringArray[target];
					stringArray[target] = stringArray[source];
					stringArray[source] = temp;
					source -= 1;
				} else
					loop = false;
			}
			if (Integer.valueOf(stringArray[current][0]) >= 
					Integer.valueOf(stringArray[current - 1][0]))
				current -= 1;
		}
		//System.out.println("Array");
		//for (int index = 0; index < stringArray.length; index++)
		//	printStringArray(stringArray[index]);
	}
	
	public int length() {
		return stringArray.length;
	}
	
	public String[] getStringArray(int row) {
		return stringArray[row];
	}
	
	public String getString(int row, int column) {
		return stringArray[row][column];
	}
	
}
