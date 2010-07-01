package org.jasig.portal.fragment.subscribe.dao.jpa;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.security.IPerson;

/**
 * UserFragmentSubscriptionImpl is the default JPA-based implementation of 
 * {@link IUserFragmentSubscription}. 
 * 
 * @author Mary Hunt
 * @version $Revision$ $Date$
 */

@Entity
@Table(name = "UP_USER_FRAGMENT_SUBSCRIPTION",
	       uniqueConstraints = {
		      @UniqueConstraint(columnNames = {"USER_ID","FRAGMENT_OWNER"})
			}
		)
@org.hibernate.annotations.Table(
        appliesTo = "UP_USER_FRAGMENT_SUBSCRIPTION", 
        indexes = {
            @Index(name = "IDX_USER_FRAG__USER", columnNames = "USER_ID")
        }
    )
@GenericGenerator(
        name = "UP_USER_FRAGMENT_SUBSCRIPTION_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_USER_FRAGMENT_SUBSCRIPTION_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_USER_FRAGMENT_SUBSCRIPTION_HI")
        }
    )
    

public class UserFragmentSubscriptionImpl implements IUserFragmentSubscription {
	
   //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
   @Id
   @GeneratedValue(generator = "UP_USER_FRAGMENT_SUBSCRIPTION_GEN")
   @Column(name = "USER_FRAGMENT_SUBSCRIPTION_ID")
   private final long userFragmentInfoId;
   
   @Column(name = "USER_ID", updatable = false, nullable = false)
   private final int userId;
   
   @Column(name = "FRAGMENT_OWNER",updatable = false, nullable = false)
   private final String fragmentOwner;
   
   @Column(name = "ACTIVE",updatable = true, nullable = false)
   private boolean active;
   
   @Column(name = "CREATED_BY", updatable = false, nullable = false)
   private String createdBy;

   @Column(name = "CREATION_DATE", updatable = false, nullable = false)
   @Temporal(TemporalType.TIMESTAMP)
   private Calendar creationDate;
   
   @Column(name = "LAST_UPDATED_DATE", updatable = true, nullable = true)
   @Temporal(TemporalType.TIMESTAMP)
   private Calendar lastUpdatedDate; 
   
   @Transient
   private EntityIdentifier m_eid = new EntityIdentifier(null, IUserFragmentSubscription.class);
  
   /**
    * Used by the ORM layer to create instances of the object.
    */
   @SuppressWarnings("unused")
   private UserFragmentSubscriptionImpl() {
       this.userFragmentInfoId = -1;
       this.active = false;
       this.userId = -1;
       this.fragmentOwner = "";
       this.createdBy = "";

   }
   
   public UserFragmentSubscriptionImpl(IPerson person, IPerson fragmentOwner) {
       this.userFragmentInfoId = -1;
       this.active = true;
       this.fragmentOwner = fragmentOwner.getUserName();
       this.m_eid = new EntityIdentifier(String.valueOf(this.fragmentOwner), IUserFragmentSubscription.class);
       this.userId = person.getID();
       this.createdBy = person.getUserName();
   }


	public int getUserId() {
		return userId;
	}


	public String getFragmentOwner() {
		return fragmentOwner;
	}


	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCreatedBy() {
		return createdBy;
	}


	public Calendar getCreationDate() {
		return creationDate;
	}
    
	public Calendar getLastUpdatedDate() {
		return lastUpdatedDate;
	}
	
	public long getId() {
		return userFragmentInfoId;
	}
	
    @PreUpdate
    @SuppressWarnings("unused")
	private void setLastUpdatedDate() {
		this.lastUpdatedDate =  new GregorianCalendar();
	}

	   
	@PrePersist
    @SuppressWarnings("unused")
	private void setCreationDate() {
		this.creationDate =  new GregorianCalendar();
	}
	
	public void setInactive() {
		this.active = false;
	}
	
    /**
     * Returns an EntityIdentifier for this fragment owner.  The key contains the value
     * of the eudPerson fragment owner attribute, or null
     *
     * @return EntityIdentifier with attribute 'fragment owner' as key.
     */
    public EntityIdentifier getEntityIdentifier() {
        return m_eid;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fragmentOwner == null) ? 0 : fragmentOwner.hashCode());
		result = prime * result
				+ (int) (userFragmentInfoId ^ (userFragmentInfoId >>> 32));
		result = prime * result + userId;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserFragmentSubscriptionImpl))
			return false;
		UserFragmentSubscriptionImpl other = (UserFragmentSubscriptionImpl) obj;
		if (fragmentOwner == null) {
			if (other.fragmentOwner != null)
				return false;
		} else if (!fragmentOwner.equals(other.fragmentOwner))
			return false;
		if (userFragmentInfoId != other.userFragmentInfoId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
	   

       

}
