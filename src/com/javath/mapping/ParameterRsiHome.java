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
 * Home object for domain model class ParameterRsi.
 * @see com.javath.mapping.ParameterRsi
 * @author Hibernate Tools
 */
public class ParameterRsiHome {

	private static final Log log = LogFactory.getLog(ParameterRsiHome.class);

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

	public void persist(ParameterRsi transientInstance) {
		log.debug("persisting ParameterRsi instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(ParameterRsi instance) {
		log.debug("attaching dirty ParameterRsi instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ParameterRsi instance) {
		log.debug("attaching clean ParameterRsi instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(ParameterRsi persistentInstance) {
		log.debug("deleting ParameterRsi instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ParameterRsi merge(ParameterRsi detachedInstance) {
		log.debug("merging ParameterRsi instance");
		try {
			ParameterRsi result = (ParameterRsi) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public ParameterRsi findById(short id) {
		log.debug("getting ParameterRsi instance with id: " + id);
		try {
			ParameterRsi instance = (ParameterRsi) sessionFactory
					.getCurrentSession().get("com.javath.mapping.ParameterRsi",
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

	public List<ParameterRsi> findByExample(ParameterRsi instance) {
		log.debug("finding ParameterRsi instance by example");
		try {
			List<ParameterRsi> results = (List<ParameterRsi>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.ParameterRsi")
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
