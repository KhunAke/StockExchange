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
 * Home object for domain model class SettradeBoard.
 * @see com.javath.mapping.SettradeBoard
 * @author Hibernate Tools
 */
public class SettradeBoardHome {

	private static final Log log = LogFactory.getLog(SettradeBoardHome.class);

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

	public void persist(SettradeBoard transientInstance) {
		log.debug("persisting SettradeBoard instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SettradeBoard instance) {
		log.debug("attaching dirty SettradeBoard instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SettradeBoard instance) {
		log.debug("attaching clean SettradeBoard instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SettradeBoard persistentInstance) {
		log.debug("deleting SettradeBoard instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SettradeBoard merge(SettradeBoard detachedInstance) {
		log.debug("merging SettradeBoard instance");
		try {
			SettradeBoard result = (SettradeBoard) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SettradeBoard findById(com.javath.mapping.SettradeBoardId id) {
		log.debug("getting SettradeBoard instance with id: " + id);
		try {
			SettradeBoard instance = (SettradeBoard) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.SettradeBoard", id);
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

	public List<SettradeBoard> findByExample(SettradeBoard instance) {
		log.debug("finding SettradeBoard instance by example");
		try {
			List<SettradeBoard> results = (List<SettradeBoard>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SettradeBoard")
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
