package com.javath.mapping;

// Generated Sep 10, 2013 10:46:29 AM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class StreamingOrder.
 * @see com.javath.mapping.StreamingOrder
 * @author Hibernate Tools
 */
public class StreamingOrderHome {

	private static final Log log = LogFactory.getLog(StreamingOrderHome.class);

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

	public void persist(StreamingOrder transientInstance) {
		log.debug("persisting StreamingOrder instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StreamingOrder instance) {
		log.debug("attaching dirty StreamingOrder instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StreamingOrder instance) {
		log.debug("attaching clean StreamingOrder instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StreamingOrder persistentInstance) {
		log.debug("deleting StreamingOrder instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StreamingOrder merge(StreamingOrder detachedInstance) {
		log.debug("merging StreamingOrder instance");
		try {
			StreamingOrder result = (StreamingOrder) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StreamingOrder findById(com.javath.mapping.StreamingOrderId id) {
		log.debug("getting StreamingOrder instance with id: " + id);
		try {
			StreamingOrder instance = (StreamingOrder) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.StreamingOrder", id);
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

	public List<StreamingOrder> findByExample(StreamingOrder instance) {
		log.debug("finding StreamingOrder instance by example");
		try {
			List<StreamingOrder> results = (List<StreamingOrder>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.StreamingOrder")
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
