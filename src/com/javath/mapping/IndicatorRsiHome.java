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
 * Home object for domain model class IndicatorRsi.
 * @see com.javath.mapping.IndicatorRsi
 * @author Hibernate Tools
 */
public class IndicatorRsiHome {

	private static final Log log = LogFactory.getLog(IndicatorRsiHome.class);

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

	public void persist(IndicatorRsi transientInstance) {
		log.debug("persisting IndicatorRsi instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(IndicatorRsi instance) {
		log.debug("attaching dirty IndicatorRsi instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(IndicatorRsi instance) {
		log.debug("attaching clean IndicatorRsi instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(IndicatorRsi persistentInstance) {
		log.debug("deleting IndicatorRsi instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public IndicatorRsi merge(IndicatorRsi detachedInstance) {
		log.debug("merging IndicatorRsi instance");
		try {
			IndicatorRsi result = (IndicatorRsi) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public IndicatorRsi findById(com.javath.mapping.IndicatorRsiId id) {
		log.debug("getting IndicatorRsi instance with id: " + id);
		try {
			IndicatorRsi instance = (IndicatorRsi) sessionFactory
					.getCurrentSession().get("com.javath.mapping.IndicatorRsi",
							id);
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

	public List<IndicatorRsi> findByExample(IndicatorRsi instance) {
		log.debug("finding IndicatorRsi instance by example");
		try {
			List<IndicatorRsi> results = (List<IndicatorRsi>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.IndicatorRsi")
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
