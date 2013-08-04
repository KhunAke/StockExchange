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
 * Home object for domain model class SetCompany.
 * @see com.javath.mapping.SetCompany
 * @author Hibernate Tools
 */
public class SetCompanyHome {

	private static final Log log = LogFactory.getLog(SetCompanyHome.class);

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

	public void persist(SetCompany transientInstance) {
		log.debug("persisting SetCompany instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SetCompany instance) {
		log.debug("attaching dirty SetCompany instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SetCompany instance) {
		log.debug("attaching clean SetCompany instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SetCompany persistentInstance) {
		log.debug("deleting SetCompany instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SetCompany merge(SetCompany detachedInstance) {
		log.debug("merging SetCompany instance");
		try {
			SetCompany result = (SetCompany) sessionFactory.getCurrentSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SetCompany findById(java.lang.String id) {
		log.debug("getting SetCompany instance with id: " + id);
		try {
			SetCompany instance = (SetCompany) sessionFactory
					.getCurrentSession().get("com.javath.mapping.SetCompany",
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

	public List<SetCompany> findByExample(SetCompany instance) {
		log.debug("finding SetCompany instance by example");
		try {
			List<SetCompany> results = (List<SetCompany>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SetCompany")
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
