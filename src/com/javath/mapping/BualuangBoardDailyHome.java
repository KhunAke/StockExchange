package com.javath.mapping;

// Generated Aug 7, 2013 9:38:50 AM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class BualuangBoardDaily.
 * @see com.javath.mapping.BualuangBoardDaily
 * @author Hibernate Tools
 */
public class BualuangBoardDailyHome {

	private static final Log log = LogFactory
			.getLog(BualuangBoardDailyHome.class);

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

	public void persist(BualuangBoardDaily transientInstance) {
		log.debug("persisting BualuangBoardDaily instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(BualuangBoardDaily instance) {
		log.debug("attaching dirty BualuangBoardDaily instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(BualuangBoardDaily instance) {
		log.debug("attaching clean BualuangBoardDaily instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(BualuangBoardDaily persistentInstance) {
		log.debug("deleting BualuangBoardDaily instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public BualuangBoardDaily merge(BualuangBoardDaily detachedInstance) {
		log.debug("merging BualuangBoardDaily instance");
		try {
			BualuangBoardDaily result = (BualuangBoardDaily) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public BualuangBoardDaily findById(
			com.javath.mapping.BualuangBoardDailyId id) {
		log.debug("getting BualuangBoardDaily instance with id: " + id);
		try {
			BualuangBoardDaily instance = (BualuangBoardDaily) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.BualuangBoardDaily", id);
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

	public List<BualuangBoardDaily> findByExample(BualuangBoardDaily instance) {
		log.debug("finding BualuangBoardDaily instance by example");
		try {
			List<BualuangBoardDaily> results = (List<BualuangBoardDaily>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.BualuangBoardDaily")
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
