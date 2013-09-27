package com.javath.util.html;

import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ParamFilter extends Filter {

	public ParamFilter(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean condition(Node node) {
		return node.getNodeName().equals("PARAM");
	}
	
	public Node name(String name) {
		List<Node> forms = this.nodes;
		for (int index = 0; index < forms.size(); index++) {
			Node htmlParam = forms.get(index);
			NamedNodeMap attributes = htmlParam.getAttributes();
			for (int item = 0; item < attributes.getLength(); item++) {
				Node attribute = attributes.item(item);
				if (attribute.getNodeName().equals("name") &&
						attribute.getNodeValue().equals(name)) {
					return htmlParam;
				}
			}
		}
		return null;
	}
	
}
