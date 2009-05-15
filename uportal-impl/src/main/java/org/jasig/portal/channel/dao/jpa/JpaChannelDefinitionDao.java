package org.jasig.portal.channel.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.dao.IChannelDefinitionDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA/Hibernate implementation of IChannelDefinitionDao.  This DAO handles 
 * channel definitions, their parameters, and their localization strings.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
@Repository
public class JpaChannelDefinitionDao implements IChannelDefinitionDao {

    private static final String FIND_CHANNEL_DEF_BY_FNAME = 
        "from ChannelDefinitionImpl channel where channel.fname = :fname";

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
     * @see org.jasig.portal.channel.dao.IChannelDefinitionDao#deleteChannelDefinition(org.jasig.portal.channel.IChannelDefinition)
     */
    @Transactional
	public void deleteChannelDefinition(IChannelDefinition definition) {
    	entityManager.remove(definition);
	}

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelDefinitionDao#getChannelDefinitions()
     */
    @SuppressWarnings("unchecked")
	public List<IChannelDefinition> getChannelDefinitions() {
        final Query query = this.entityManager.createQuery("from ChannelDefinitionImpl channel");
        final List<IChannelDefinition> channelDefinitions = query.getResultList();
		return channelDefinitions;
	}

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelDefinitionDao#getChannelDefinition(int)
     */
	public IChannelDefinition getChannelDefinition(int id) {
		return entityManager.find(ChannelDefinitionImpl.class, new Long(id));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelDefinitionDao#getChannelDefinition(java.lang.String)
	 */
    @SuppressWarnings("unchecked")
	public IChannelDefinition getChannelDefinition(String fname) {
        final Query query = this.entityManager.createQuery(FIND_CHANNEL_DEF_BY_FNAME);
        query.setParameter("fname", fname);
        query.setMaxResults(1);
        
        final List<IChannelDefinition> channelDefinitions = query.getResultList();
        IChannelDefinition definition = (IChannelDefinition) DataAccessUtils.uniqueResult(channelDefinitions);
		return definition;
	}

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelDefinitionDao#saveChannelDefinition(org.jasig.portal.channel.IChannelDefinition)
     */
    @Transactional
	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition) {
    	return this.entityManager.merge(definition);
	}

}
