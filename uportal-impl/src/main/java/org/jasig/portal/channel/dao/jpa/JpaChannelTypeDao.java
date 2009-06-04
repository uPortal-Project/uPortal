package org.jasig.portal.channel.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channel.dao.IChannelTypeDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA/Hibernate implementation of IChannelTypeDao.  This DAO handles 
 * channel types and is not yet integrated with the channel definition persistence
 * code.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public class JpaChannelTypeDao implements IChannelTypeDao {

    private EntityManager entityManager;
    
    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    
    // Public API methods
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#deleteChannelType(org.jasig.portal.channel.IChannelType)
     */
    @Transactional
	public void deleteChannelType(IChannelType type) {
        Validate.notNull(type, "definition can not be null");
        
        final IChannelType persistentChanneltype;
        if (this.entityManager.contains(type)) {
            persistentChanneltype = type;
        }
        else {
            persistentChanneltype = this.entityManager.merge(type);
        }
    	this.entityManager.remove(persistentChanneltype);
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#createChannelType(java.lang.String, java.lang.String, java.lang.String)
     */
    @Transactional
    public IChannelType createChannelType(String name, String clazz, String cpdUri) {
        Validate.notEmpty(name, "name can not be null");
        Validate.notEmpty(clazz, "clazz can not be null");
        Validate.notEmpty(cpdUri, "cpdUri can not be null");
        
        final ChannelTypeImpl channelType = new ChannelTypeImpl(name, clazz, cpdUri);
        
        this.entityManager.persist(channelType);
        
        return channelType;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(int)
     */
	public IChannelType getChannelType(int id) {
		return this.entityManager.find(ChannelTypeImpl.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(java.lang.String)
	 */
    @SuppressWarnings("unchecked")
	public IChannelType getChannelType(String name) {
        final Query query = this.entityManager.createQuery("from ChannelTypeImpl type where type.name = :name");
        query.setParameter("name", name);
        query.setHint("org.hibernate.cacheable", true);
        query.setMaxResults(1);
        
        final List<IChannelType> channelTypes = query.getResultList();
        IChannelType type = (IChannelType) DataAccessUtils.uniqueResult(channelTypes);
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelTypes()
	 */
    @SuppressWarnings("unchecked")
	public List<IChannelType> getChannelTypes() {
        final Query query = this.entityManager.createQuery("from ChannelTypeImpl channel");
        query.setHint("org.hibernate.cacheable", true);
        final List<IChannelType> channelTypes = query.getResultList();
		return channelTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#saveChannelType(org.jasig.portal.channel.IChannelType)
	 */
	@Transactional
	public IChannelType updateChannelType(IChannelType type) {
        Validate.notNull(type, "type can not be null");
        
        this.entityManager.persist(type);
        
        return type;
	}

}
