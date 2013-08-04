package com.javath.mapping;

// Generated Aug 4, 2013 5:35:48 PM by Hibernate Tools 4.0.0

/**
 * BualuangBoardHistory generated by hbm2java
 */
public class BualuangBoardHistory implements java.io.Serializable {

	private BualuangBoardHistoryId id;
	private Float open;
	private Float high;
	private Float low;
	private Float close;
	private Long volume;
	private Double value;

	public BualuangBoardHistory() {
	}

	public BualuangBoardHistory(BualuangBoardHistoryId id) {
		this.id = id;
	}

	public BualuangBoardHistory(BualuangBoardHistoryId id, Float open,
			Float high, Float low, Float close, Long volume, Double value) {
		this.id = id;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.value = value;
	}

	public BualuangBoardHistoryId getId() {
		return this.id;
	}

	public void setId(BualuangBoardHistoryId id) {
		this.id = id;
	}

	public Float getOpen() {
		return this.open;
	}

	public void setOpen(Float open) {
		this.open = open;
	}

	public Float getHigh() {
		return this.high;
	}

	public void setHigh(Float high) {
		this.high = high;
	}

	public Float getLow() {
		return this.low;
	}

	public void setLow(Float low) {
		this.low = low;
	}

	public Float getClose() {
		return this.close;
	}

	public void setClose(Float close) {
		this.close = close;
	}

	public Long getVolume() {
		return this.volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Double getValue() {
		return this.value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
