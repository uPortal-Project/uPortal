package org.jasig.portal.channel.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
    	this.entityManager.remove(type);
	}

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(int)
     */
	public IChannelType getChannelType(int id) {
		return this.entityManager.find(ChannelTypeImpl.class, new Long(id));
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(java.lang.String)
	 */
	public IChannelType getChannelType(String name) {
        final Query query = this.entityManager.createQuery("from ChannelTypeImpl type where type.name = :name");
        query.setParameter("name", name);
        query.setMaxResults(1);
        
        final List<IChannelType> channelTypes = query.getResultList();
        IChannelType type = (IChannelType) DataAccessUtils.uniqueResult(channelTypes);
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelTypes()
	 */
	public List<IChannelType> getChannelTypes() {
        final Query query = this.entityManager.createQuery("from ChannelTypeImpl channel");
        final List<IChannelType> channelTypes = query.getResultList();
		return channelTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#saveChannelType(org.jasig.portal.channel.IChannelType)
	 */
	@Transactional
	public IChannelType saveChannelType(IChannelType type) {
		return this.entityManager.merge(type);
	}

}
