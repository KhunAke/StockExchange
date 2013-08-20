package com.javath.stock.set;

import java.text.ParseException;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.Object;
import com.javath.mapping.SetCompany;
import com.javath.mapping.SetCompanyHome;
import com.javath.stock.settrade.Market;
import com.javath.util.Browser;
import com.javath.util.html.HtmlParser;
import com.javath.util.html.TextNode;

public class Company extends Object implements Runnable {
	
	private Browser browser = new Browser();
	private HtmlParser parser = new HtmlParser(null);
	//private CustomFilter filter = new CustomFilter(null);
	private static Company instance = new Company();
	
	private Company() {};
	
	public static Company getInstance() {
		//if (instance == null)
		//	instance = new Company();
		return instance;
	} 
	
	public static void main(String[] args) {
		Company.getInstance().run();
	}
	
	public synchronized void run() {
		this.getWebPage();
	}
	
	public void getWebPage() {
		parser.setInputStream(
			browser.get("http://www.set.or.th/listedcompany/static/listedCompanies_en_US.xls"));
		logger.info(message("Cache in \"%s\"", browser.getFileContent()));
		TextNode textNode = new TextNode(parser.parse());
		//textNode.print();
		Date date = null;
		for (int index = 1; index < textNode.length(); index++) {
			String[] stringArray = textNode.getStringArray(index);
			if (stringArray[0].equals("3") && (stringArray.length > 6)) {
				//textNode.printStringArray(index);
				if (!stringArray[2].equals("Symbol")) { 
					SetCompany company = new SetCompany();
					company.setSymbol(stringArray[2]);
					company.setName(stringArray[4]);
					company.setMarket(stringArray[6]);
					company.setIndustry(stringArray[8]);
					company.setSector(stringArray[10]);
					company.setUpdate(date);
					this.store(company);
				}
			} else if (stringArray[0].equals("5")) {
				//textNode.printStringArray(index);
				try {
					date = Market.castDate("dd MMM yyyy", stringArray[1].substring(6));
				} catch (ParseException e) {
					logger.severe(message(e));
				}
			}
		}
		//System.out.println(String.format(Locale.US, "Date = %1$tY-%1$tm-%1$td", date));	
	}
	
	private void store(SetCompany company) {
		SetCompanyHome home = new SetCompanyHome();
		
		Session session = ((SessionFactory) this.getContext("SessionFactory"))
		//	.openSession();
				.getCurrentSession();
		session.beginTransaction();
		home.attachDirty(company);
		try {
			session.getTransaction().commit();
		} catch (org.hibernate.JDBCException e) {
			logger.severe(message("SQL Error %d, SQLState %s: %s", 
					e.getErrorCode(), e.getSQLState(), e.getMessage()));
			session.getTransaction().rollback();
		} 
	}
}
