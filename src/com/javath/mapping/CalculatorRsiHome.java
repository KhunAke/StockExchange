package com.javath.mapping;

// Generated Sep 30, 2013 2:02:00 PM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class CalculatorRsi.
 * @see com.javath.mapping.CalculatorRsi
 * @author Hibernate Tools
 */
public class CalculatorRsiHome {

	private static final Log log = LogFactory.getLog(CalculatorRsiHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(CalculatorRsi transientInstance) {
		log.debug("persisting CalculatorRsi instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(CalculatorRsi instance) {
		log.debug("attaching dirty CalculatorRsi instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(CalculatorRsi instance) {
		log.debug("attaching clean CalculatorRsi instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(CalculatorRsi persistentInstance) {
		log.debug("deleting CalculatorRsi instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public CalculatorRsi merge(CalculatorRsi detachedInstance) {
		log.debug("merging CalculatorRsi instance");
		try {
			CalculatorRsi result = (CalculatorRsi) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public CalculatorRsi findById(com.javath.mapping.CalculatorRsiId id) {
		log.debug("getting CalculatorRsi instance with id: " + id);
		try {
			CalculatorRsi instance = (CalculatorRsi) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.CalculatorRsi", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List<CalculatorRsi> findByExample(CalculatorRsi instance) {
		log.debug("finding CalculatorRsi instance by example");
		try {
			List<CalculatorRsi> results = (List<CalculatorRsi>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.CalculatorRsi")
					.add(create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
