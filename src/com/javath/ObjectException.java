package com.javath;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ObjectException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private static Map<Integer, String> reasons = new HashMap<Integer, String>();
	private static Map<String, Integer> classOffset = new HashMap<String, Integer>();
	
	static {
		InputStream input = ObjectException.class.getResourceAsStream("/ObjectException.properties");
		Properties properties = new Properties();
		try {
			properties.load(input);
			int numberOfClass = Integer.valueOf(properties.getProperty("number_of_class","0"));
			for (int index = 0; index < numberOfClass; index++) {
				String classname = properties.getProperty("class.name." + (index + 1), null);
				int offset = Integer.valueOf(properties.getProperty("class.offset." + (index + 1), "0"));
				if (classname != null)
					classOffset.put(classname, offset);
			}
			int maxOfErrorcode = Integer.valueOf(properties.getProperty("max_of_class","0"));
			for (int index = 0; index < (maxOfErrorcode + 1); index++) {
				String reason = properties.getProperty(String.valueOf(index), null);
				if (reason != null)
					reasons.put(index, reason);
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private String classname;
	private int code;
	
	public String getSourceClassname() {
		return classname;
	}
	
	public int getCode() {
		return code;
	}
	
	private static String searchReason(java.lang.Object obj, int code, java.lang.Object... value) {
		Integer offset = classOffset.get(obj.getClass().getName());
		String reason = null;
		if (offset != null)
			reason = reasons.get(offset + (code -1));
		if (reason == null) {
			String string = ""; 
			for (int index = 0; index < value.length; index++) {
				string += "%s,";
			}
			try {
				return string.substring(0, string.length() - 1);
			} catch (StringIndexOutOfBoundsException e) {
				// Not value
				return "";
			}
		}else
			return reason;
	}
	
	public ObjectException(java.lang.Object obj, int code, java.lang.Object... value) {
		this(searchReason(obj, code, value), value);
		this.classname = obj.getClass().getName();
		this.code = code;
	}
	
	public ObjectException(String message) {
		super(message);
	}
	
	public ObjectException(String format, java.lang.Object... value) {
		this(String.format(format, value));
	}

}
