package com.javath.mapping;
// Generated Sep 17, 2013 2:05:15 PM by Hibernate Tools 4.0.0


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class StreamingTicker.
 * @see com.javath.mapping.StreamingTicker
 * @author Hibernate Tools
 */
public class StreamingTickerHome {

    private static final Log log = LogFactory.getLog(StreamingTickerHome.class);

    private final SessionFactory sessionFactory = getSessionFactory();
    
    protected SessionFactory getSessionFactory() {
        try {
            return (SessionFactory) new InitialContext().lookup("SessionFactory");
        }
        catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }
    
    public void persist(StreamingTicker transientInstance) {
        log.debug("persisting StreamingTicker instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(StreamingTicker instance) {
        log.debug("attaching dirty StreamingTicker instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(StreamingTicker instance) {
        log.debug("attaching clean StreamingTicker instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(StreamingTicker persistentInstance) {
        log.debug("deleting StreamingTicker instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public StreamingTicker merge(StreamingTicker detachedInstance) {
        log.debug("merging StreamingTicker instance");
        try {
            StreamingTicker result = (StreamingTicker) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public StreamingTicker findById( com.javath.mapping.StreamingTickerId id) {
        log.debug("getting StreamingTicker instance with id: " + id);
        try {
            StreamingTicker instance = (StreamingTicker) sessionFactory.getCurrentSession()
                    .get("com.javath.mapping.StreamingTicker", id);
            if (instance==null) {
                log.debug("get successful, no instance found");
            }
            else {
                log.debug("get successful, instance found");
            }
            return instance;
        }
        catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    public List<StreamingTicker> findByExample(StreamingTicker instance) {
        log.debug("finding StreamingTicker instance by example");
        try {
            List<StreamingTicker> results = (List<StreamingTicker>) sessionFactory.getCurrentSession()
                    .createCriteria("com.javath.mapping.StreamingTicker")
                    .add( create(instance) )
            .list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        }
    } 
}

