package com.javath.mapping;

// Generated Sep 10, 2013 10:46:29 AM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * SetCompany generated by hbm2java
 */
public class SetCompany implements java.io.Serializable {

	private String symbol;
	private String name;
	private String market;
	private String industry;
	private String sector;
	private Date update;

	public SetCompany() {
	}

	public SetCompany(String symbol) {
		this.symbol = symbol;
	}

	public SetCompany(String symbol, String name, String market,
			String industry, String sector, Date update) {
		this.symbol = symbol;
		this.name = name;
		this.market = market;
		this.industry = industry;
		this.sector = sector;
		this.update = update;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMarket() {
		return this.market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getIndustry() {
		return this.industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getSector() {
		return this.sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public Date getUpdate() {
		return this.update;
	}

	public void setUpdate(Date update) {
		this.update = update;
	}

}
