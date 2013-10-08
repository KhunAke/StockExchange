package com.javath.util.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.javath.Object;

public class Delimited extends Object implements Fields {
	
	private BufferedReader reader = null;
	private int BufferSize = 4096;
	private String delimiter = "\t";
	
	private Map<Integer,String> FieldName = new HashMap<Integer,String>();
	
	private String current;
	private String[] fields;

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public void setFieldName(int id, String name) {
		FieldName.put(id, name);
	}
	
	public void setInputStream(InputStream inputstream) {
		setInputStream(inputstream, Charset.defaultCharset());
	}
	
	public void setInputStream(InputStream inputstream, Charset charset) {
		this.reader = new BufferedReader(
				new InputStreamReader(inputstream, charset), BufferSize);
	}
	
	public void setBufferSize(int bufferSize) {
		this.BufferSize = bufferSize;
	}

	@Override
	public boolean hasNextRow() throws IOException {
		return reader.ready();
	}

	@Override
	public void nextRow() throws IOException {
		current = reader.readLine();
		fields = current.split(delimiter,-1);
	}
	
	public String getCurrent() {
		return current;
	}

	@Override
	public int getID(String name) {
		Iterator<Integer> keys = FieldName.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = (Integer) keys.next();
			String fieldname = FieldName.get(key);
			if (fieldname.equals(name))
				return key;
		}
		return -1;
	}

	@Override
	public String getName(int id) {
		String fieldname = FieldName.get(id);
		if (fieldname == null)
			return String.valueOf(id);
		else 
			return fieldname;
	}

	@Override
	public String getValue(int id) {
		return fields[id];
	}
	
	public String[] getFields() {
		return fields;
	}

	public int getFieldLength() {
		return fields.length;
	}
	
}
