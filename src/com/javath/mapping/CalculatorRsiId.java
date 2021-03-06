package com.javath.mapping;

// Generated Sep 30, 2013 2:01:59 PM by Hibernate Tools 4.0.0

/**
 * CalculatorRsiId generated by hbm2java
 */
public class CalculatorRsiId implements java.io.Serializable {

	private String symbol;
	private short parameter;

	public CalculatorRsiId() {
	}

	public CalculatorRsiId(String symbol, short parameter) {
		this.symbol = symbol;
		this.parameter = parameter;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public short getParameter() {
		return this.parameter;
	}

	public void setParameter(short parameter) {
		this.parameter = parameter;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof CalculatorRsiId))
			return false;
		CalculatorRsiId castOther = (CalculatorRsiId) other;

		return ((this.getSymbol() == castOther.getSymbol()) || (this
				.getSymbol() != null && castOther.getSymbol() != null && this
				.getSymbol().equals(castOther.getSymbol())))
				&& (this.getParameter() == castOther.getParameter());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getSymbol() == null ? 0 : this.getSymbol().hashCode());
		result = 37 * result + this.getParameter();
		return result;
	}

}
