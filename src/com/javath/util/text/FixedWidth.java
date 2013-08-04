package com.javath.util.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FixedWidth implements Fields {
	
	private BufferedReader reader = null;
	private int BufferSize = 4096;
	
	private class FieldRange {
		private String name = "";
		private int begin = 0;
		private int end = 0;
		
		public FieldRange(String name, int begin, int end) {
			this.name = name;
			this.begin = begin;
			this.end = end;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getBegin() {
			return begin;
		}
		
		public int getEnd() {
			return end;
		}
	}
	
	private Map<Integer,FieldRange> Fieldwidth = new HashMap<Integer,FieldRange>();
	private String current = null;
	
	public void setFieldwidth(int id, int begin, int end) {
		setFieldwidth(id, "", begin, end);
	}
	
	public void setFieldwidth(int id, String name, int begin, int end) {
		FieldRange fieldrange = new FieldRange(name, begin, end);
		Fieldwidth.put(id, fieldrange);
	}
	
	public void setFieldName(int fieldwidth, String name) {
		FieldRange fieldrange = Fieldwidth.get(fieldwidth);
		fieldrange.setName(name);
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
	
		
	public boolean hasNextRow() throws IOException {
		return reader.ready();
	}

	public void nextRow() throws IOException {
		current = reader.readLine();
	}

	public int getID(String name) {
		Iterator<Integer> keys = Fieldwidth.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = (Integer) keys.next();
			FieldRange fieldRange = Fieldwidth.get(key);
			if (fieldRange.getName().equals(name))
				return key;
		}
		return -1;
	}

	public String getName(int id) {
		FieldRange fieldrange = Fieldwidth.get(id);
		return fieldrange.getName();
	}

	public String getValue(int id) {
		FieldRange fieldrange = Fieldwidth.get(id);
		int begin = fieldrange.getBegin();
		int end = fieldrange.getEnd();
		try {
			return current.substring(begin, end).trim();
		} catch (StringIndexOutOfBoundsException e) {
			return current.substring(begin).trim();
		}
	}

}
