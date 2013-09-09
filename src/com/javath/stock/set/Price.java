package com.javath.stock.set;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.javath.Configuration;
import com.javath.Object;

public class Price extends Object implements Comparable<Price>{

	private static int[][] TickSize = null;

	private static final int LessThen = 0;
	private static final int GreaterEqual = 1;
	private static final int STEP = 2;

	private int value = 0;

	static {
		try {

			Properties properties = new Properties();
			String configuration = path.etc + file.separator
					+ "price.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.set.price",
							configuration));
			properties.load(propsFile);
			propsFile.close();

			int range = Integer.parseInt(properties.getProperty(
					"ticksize.range", "0"));
			if (range > 0) {
				TickSize = new int[range][3];
				for (int index = 0; index < range; index++) {
					TickSize[index][GreaterEqual] = Integer.parseInt(properties
							.getProperty("ticksize.ge." + index, "0"));
					TickSize[index][LessThen] = Integer.parseInt(properties
							.getProperty("ticksize.lt." + index,
									String.valueOf(Integer.MAX_VALUE)));
					TickSize[index][STEP] = Integer.parseInt(properties
							.getProperty("ticksize.step." + index, "-1"));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	public Price(double value) {
		this.setValue(value);
	}

	public void setValue(double value) {
		this.value = (int) (value * 100);
	}

	public double getValue() {
		return (double) this.value / 100;
	}

	private double next(int value, int range, int step) {
		int space = (TickSize[range][LessThen] - value) / TickSize[range][STEP];
		if (step <= space)
			return (double) (value + (TickSize[range][STEP] * step)) / 100;
		return next(TickSize[range][LessThen], range + 1, step - space);
	}

	public double next(int step) {
		if (step < 0)
			return this.previous(step * (-1));
		else if (step == 0)
			return this.getValue();
		return this.next(value, getRange(value), step);
	}

	private double previous(int value, int range, int step) {
		if (range == -1)
			return -1;
		int space = (value - TickSize[range][GreaterEqual])
				/ TickSize[range][STEP];
		if (step <= space)
			return (double) (value - (TickSize[range][STEP] * step)) / 100;
		return previous(TickSize[range][GreaterEqual], range - 1, step - space);
	}

	public double previous(int step) {
		if (step < 0)
			return this.next(step * (-1));
		else if (step == 0)
			return this.getValue();
		return this.previous(value, getRange(value), step);
	}

	private static int getRange(int value) {
		for (int index = 0; index < TickSize.length; index++) {
			if ((value < TickSize[index][LessThen])
					&& (value >= TickSize[index][GreaterEqual]))
				return index;
		}
		return -1;
	}

	@Override
	public int compareTo(Price obj) {
		int value = Integer.valueOf(String.format("%.2f",obj.getValue()).replace(".", ""));
		if (this.value == value)
			return 0;
		else {
			int thisRange = getRange(this.value);
			int objRange = getRange(value);
			if (thisRange == objRange)
				return (this.value - value) / TickSize[thisRange][STEP];
			else if (thisRange < objRange) {
				return (this.value - TickSize[thisRange][LessThen]) / TickSize[thisRange][STEP] +
						new Price((double) TickSize[thisRange][LessThen] / 100).compareTo(obj);
			} else if (thisRange > objRange) {
				return -(value - TickSize[objRange][LessThen]) / TickSize[objRange][STEP] -
				new Price((double) TickSize[objRange][LessThen] / 100).compareTo(this);
			}
			throw new RuntimeException();
		}
	}

	@Override
	public String toString() {
		int range = getRange(value);
		if (range > 5)
			return String.format("%.0f",getValue());
		else
			return String.format("%.2f",getValue());
	}
	
	

}
