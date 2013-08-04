package com.javath.util.text;

import java.io.IOException;

public interface Fields {
	public boolean hasNextRow() throws IOException;
	public void nextRow() throws IOException;
	public int getID(String name);
	public String getName(int id);
	public String getValue(int id);
}
