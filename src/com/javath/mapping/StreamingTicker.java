package com.javath.mapping;

// Generated Sep 30, 2013 2:01:59 PM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * StreamingTicker generated by hbm2java
 */
public class StreamingTicker implements java.io.Serializable {

	private StreamingTickerId id;
	private Short type;
	private Short n;
	private Date time;
	private String side;
	private Double price;
	private Double close;
	private Double change;
	private Double changePercent;
	private String a;
	private String b;
	private Integer volume;
	private String symbol;

	public StreamingTicker() {
	}

	public StreamingTicker(StreamingTickerId id) {
		this.id = id;
	}

	public StreamingTicker(StreamingTickerId id, Short type, Short n,
			Date time, String side, Double price, Double close, Double change,
			Double changePercent, String a, String b, Integer volume,
			String symbol) {
		this.id = id;
		this.type = type;
		this.n = n;
		this.time = time;
		this.side = side;
		this.price = price;
		this.close = close;
		this.change = change;
		this.changePercent = changePercent;
		this.a = a;
		this.b = b;
		this.volume = volume;
		this.symbol = symbol;
	}

	public StreamingTickerId getId() {
		return this.id;
	}

	public void setId(StreamingTickerId id) {
		this.id = id;
	}

	public Short getType() {
		return this.type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public Short getN() {
		return this.n;
	}

	public void setN(Short n) {
		this.n = n;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getSide() {
		return this.side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public Double getPrice() {
		return this.price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getClose() {
		return this.close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getChange() {
		return this.change;
	}

	public void setChange(Double change) {
		this.change = change;
	}

	public Double getChangePercent() {
		return this.changePercent;
	}

	public void setChangePercent(Double changePercent) {
		this.changePercent = changePercent;
	}

	public String getA() {
		return this.a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getB() {
		return this.b;
	}

	public void setB(String b) {
		this.b = b;
	}

	public Integer getVolume() {
		return this.volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}
