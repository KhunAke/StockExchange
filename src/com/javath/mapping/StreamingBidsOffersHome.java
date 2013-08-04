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
 * Home object for domain model class StreamingBidsOffers.
 * @see com.javath.mapping.StreamingBidsOffers
 * @author Hibernate Tools
 */
public class StreamingBidsOffersHome {

	private static final Log log = LogFactory
			.getLog(StreamingBidsOffersHome.class);

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

	public void persist(StreamingBidsOffers transientInstance) {
		log.debug("persisting StreamingBidsOffers instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StreamingBidsOffers instance) {
		log.debug("attaching dirty StreamingBidsOffers instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StreamingBidsOffers instance) {
		log.debug("attaching clean StreamingBidsOffers instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StreamingBidsOffers persistentInstance) {
		log.debug("deleting StreamingBidsOffers instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StreamingBidsOffers merge(StreamingBidsOffers detachedInstance) {
		log.debug("merging StreamingBidsOffers instance");
		try {
			StreamingBidsOffers result = (StreamingBidsOffers) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StreamingBidsOffers findById(
			com.javath.mapping.StreamingBidsOffersId id) {
		log.debug("getting StreamingBidsOffers instance with id: " + id);
		try {
			StreamingBidsOffers instance = (StreamingBidsOffers) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.StreamingBidsOffers", id);
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

	public List<StreamingBidsOffers> findByExample(StreamingBidsOffers instance) {
		log.debug("finding StreamingBidsOffers instance by example");
		try {
			List<StreamingBidsOffers> results = (List<StreamingBidsOffers>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.StreamingBidsOffers")
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
