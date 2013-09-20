package com.javath.stock.indicators;

import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Parameter;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.Object;
import com.javath.ObjectException;
import com.javath.Service;
import com.javath.mapping.CalculatorRsi;
import com.javath.mapping.IndicatorRsi;
import com.javath.mapping.ParameterRsi;
import com.javath.mapping.ParameterRsiHome;

/**
 * RSI = 100-(100/(1+RS));
 * RS  = Average Gain / Average Loss;
 */
public class RelativeStrengthIndex extends Object {
	
	private ParameterRsi parameter;
	private CalculatorRsi calculator;
	
	public static ParameterRsi getParameter(int id) {
		Session session = Service.getSession();
		session.beginTransaction();
		ParameterRsiHome home = new ParameterRsiHome();
		ParameterRsi parameter = home.findById((short) id);
		session.getTransaction().commit();
		return parameter;
	}
	
	public static List<ParameterRsi> getParameter(int period, String table, String column) {
		Session session = Service.getSession();
		session.beginTransaction();
		ParameterRsiHome home = new ParameterRsiHome();
		ParameterRsi parameter = new ParameterRsi();
		//parameter.setId(id);
		parameter.setPeriod(period);
		parameter.setTableName(table);
		parameter.setColumnName(column);
		List<ParameterRsi> list = home.findByExample(parameter);
		session.getTransaction().commit();
		return list;
	}
	
	public RelativeStrengthIndex(String symbol) {
		this(symbol, new Date());
	}
	
	public RelativeStrengthIndex(String symbol, Date date) {
		Session session = ((SessionFactory) getContext("SessionFactory"))
				.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery(
				"select board.id.date, board.close " + 
				"from BualuangBoardDaily as board " +
				"where board.id.symbol = '" + symbol + "' " +
				"order by board.id.date desc");
		//query.setMaxResults(period);
		List<?> results = query.list();
		for (int index = results.size() - 1; index > -1; index--) {
			//price.offer((Double) ((java.lang.Object[]) results.get(index))[1]);
			System.out.println( ((java.lang.Object[]) results.get(index))[1] );
		}
		session.getTransaction().commit();
	}
	
}
