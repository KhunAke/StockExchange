package com.javath.util.html;

import java.util.List;

import org.apache.http.client.fluent.Form;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FormFilter extends Filter {
	
	public FormFilter(Node node) {
		super(node);
	}

	@Override
	protected boolean condition(Node node) {
		return node.getNodeName().equals("FORM");
	}
	
	public Node action(String url) {
		List<Node> forms = this.nodes;
		for (int index = 0; index < forms.size(); index++) {
			Node htmlForm = forms.get(index);
			NamedNodeMap attributes = htmlForm.getAttributes();
			for (int item = 0; item < attributes.getLength(); item++) {
				Node attribute = attributes.item(item);
				if (attribute.getNodeName().equals("action") &&
						attribute.getNodeValue().equals(url)) {
					return htmlForm;
				}
			}
		}
		return null;
	}
	
	public Form actionForm(String url) {
		Node node = action(url);
		if (node == null)
			return null;
		Form form = Form.form();
		InputFilter inputFilter = new InputFilter(node);
		List<Node> inputs = inputFilter.filter();
		for (int index = 0; index < inputs.size(); index++) {
			Node input = inputs.get(index);
			NamedNodeMap attributes = input.getAttributes();
			String name = null;
			String value = null;
			for (int item = 0; item < attributes.getLength(); item++) {
				Node attribute = attributes.item(item);
				if (attribute.getNodeName().equals("name"))
					name = attribute.getNodeValue();
				else if (attribute.getNodeName().equals("value")) 
					value = attribute.getNodeValue();
			}
			form.add(name, value);
		}
		return form;
	}
	
}
