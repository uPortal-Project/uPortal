package org.jasig.portal.fragment.subscribe.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.security.IPerson;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for retrieving information about fragments (pre-formatted tabs) to 
 * which a user has subscribed.
 * 
 * @author Mary Hunt
 * @version $Revision$ $Date$
 */
@Repository
public class JpaUserFragmentSubscriptionDaoImpl implements IUserFragmentSubscriptionDao {
	
	private static final String FIND_USER_FRAGMENT_INFO_BY_PERSON = 
	        "from UserFragmentSubscriptionImpl subscription " +
	        "where subscription.userId = :userId";

	private static final String FIND_USER_FRAGMENT_INFO_BY_PERSON_AND_FRAGMENTOWNER = 
        "from UserFragmentSubscriptionImpl subscription " +
        "where subscription.userId = :userId and subscription.fragmentOwner = :fragmentOwner";
	
    private EntityManager entityManager;
    
    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
	public IUserFragmentSubscription createUserFragmentInfo(IPerson person,
			IPerson fragmentOwner) {
		IUserFragmentSubscription userFragmentInfo = new UserFragmentSubscriptionImpl(person, fragmentOwner);
        this.entityManager.persist(userFragmentInfo);

		return userFragmentInfo;
	}

    @Transactional
    public void deleteUserFragmentInfo(
			IUserFragmentSubscription userFragmentInfo) {
		Validate.notNull(userFragmentInfo, "user fragment info can not be null");
		userFragmentInfo.setInactive();
		this.entityManager.merge(userFragmentInfo);

	}
	
	@SuppressWarnings("unchecked")
	public List<IUserFragmentSubscription> getUserFragmentInfo(IPerson person) {
       final Query query = this.entityManager.createQuery(FIND_USER_FRAGMENT_INFO_BY_PERSON);
       query.setParameter("userId", person.getID());
       query.setHint("org.hibernate.cacheable", true);

        
       final List<IUserFragmentSubscription> userFragmentInfos = query.getResultList();
       return userFragmentInfos;

	}

	@SuppressWarnings("unchecked")
	public IUserFragmentSubscription getUserFragmentInfo(IPerson person,
			IPerson fragmentOwner) {
       final Query query = this.entityManager.createQuery(FIND_USER_FRAGMENT_INFO_BY_PERSON_AND_FRAGMENTOWNER);
       query.setParameter("userId", person.getID());
       query.setParameter("fragmentOwner", fragmentOwner.getUserName());
       query.setHint("org.hibernate.cacheable", true);

        
       final List<IUserFragmentSubscription> userFragmentInfos = query.getResultList();
       final UserFragmentSubscriptionImpl userFragmentInfo = (UserFragmentSubscriptionImpl)DataAccessUtils.uniqueResult(userFragmentInfos);
        
       return userFragmentInfo;

	}
	
    public IUserFragmentSubscription getUserFragmentInfo(long userFragmentInfoId) {
        
        final UserFragmentSubscriptionImpl userFragmentInfo = this.entityManager.find(UserFragmentSubscriptionImpl.class, userFragmentInfoId);
        
        return userFragmentInfo;
    }

    @Transactional
	public void updateUserFragmentInfo(
			IUserFragmentSubscription userFragmentInfo) {
		Validate.notNull(userFragmentInfo, "user fragment info can not be null");
		this.entityManager.merge(userFragmentInfo);

	}

}
