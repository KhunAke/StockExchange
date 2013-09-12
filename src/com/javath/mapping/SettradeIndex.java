package com.javath.mapping;

// Generated Sep 10, 2013 10:46:29 AM by Hibernate Tools 4.0.0

/**
 * SettradeIndex generated by hbm2java
 */
public class SettradeIndex implements java.io.Serializable {

	private SettradeIndexId id;
	private Double last;
	private Double high;
	private Double low;
	private Long volume;
	private Double value;

	public SettradeIndex() {
	}

	public SettradeIndex(SettradeIndexId id) {
		this.id = id;
	}

	public SettradeIndex(SettradeIndexId id, Double last, Double high,
			Double low, Long volume, Double value) {
		this.id = id;
		this.last = last;
		this.high = high;
		this.low = low;
		this.volume = volume;
		this.value = value;
	}

	public SettradeIndexId getId() {
		return this.id;
	}

	public void setId(SettradeIndexId id) {
		this.id = id;
	}

	public Double getLast() {
		return this.last;
	}

	public void setLast(Double last) {
		this.last = last;
	}

	public Double getHigh() {
		return this.high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return this.low;
	}

	public void setLow(Double low) {
		this.low = low;
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
