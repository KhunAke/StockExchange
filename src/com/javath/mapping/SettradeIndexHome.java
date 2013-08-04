package com.javath.mapping;

// Generated Aug 4, 2013 5:35:49 PM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SettradeIndex.
 * @see com.javath.mapping.SettradeIndex
 * @author Hibernate Tools
 */
public class SettradeIndexHome {

	private static final Log log = LogFactory.getLog(SettradeIndexHome.class);

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

	public void persist(SettradeIndex transientInstance) {
		log.debug("persisting SettradeIndex instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SettradeIndex instance) {
		log.debug("attaching dirty SettradeIndex instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SettradeIndex instance) {
		log.debug("attaching clean SettradeIndex instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SettradeIndex persistentInstance) {
		log.debug("deleting SettradeIndex instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SettradeIndex merge(SettradeIndex detachedInstance) {
		log.debug("merging SettradeIndex instance");
		try {
			SettradeIndex result = (SettradeIndex) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SettradeIndex findById(com.javath.mapping.SettradeIndexId id) {
		log.debug("getting SettradeIndex instance with id: " + id);
		try {
			SettradeIndex instance = (SettradeIndex) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.SettradeIndex", id);
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

	public List<SettradeIndex> findByExample(SettradeIndex instance) {
		log.debug("finding SettradeIndex instance by example");
		try {
			List<SettradeIndex> results = (List<SettradeIndex>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SettradeIndex")
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
