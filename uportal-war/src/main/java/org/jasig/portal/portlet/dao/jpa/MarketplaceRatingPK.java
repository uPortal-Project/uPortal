package org.jasig.portal.portlet.dao.jpa;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.jasig.portal.persondir.dao.jpa.LocalAccountPersonImpl;

@SecondaryTables({
		@SecondaryTable(name="UP_PORTLET_DEF"),
		@SecondaryTable(name="UP_PERSON_DIR")
})
@Embeddable
public class MarketplaceRatingPK implements Serializable{

	private static final long serialVersionUID = -1294203685313115404L;

	@ManyToOne
	@Cascade({CascadeType.PERSIST})
	@JoinColumn(name="USER_ID", referencedColumnName="USER_DIR_ID", unique=false, nullable=true, insertable=true, updatable=true)
	protected LocalAccountPersonImpl user;

	@ManyToOne
	@Cascade({CascadeType.PERSIST})
	@JoinColumn(name="PORTLET_ID", referencedColumnName="PORTLET_DEF_ID", unique=false, nullable=true, insertable=true, updatable=true)
	protected PortletDefinitionImpl portletDefinition;

	/**
	 * Empty constructor is needed for Serializable
	 */
	public MarketplaceRatingPK(){
	};

    public MarketplaceRatingPK(LocalAccountPersonImpl user, PortletDefinitionImpl portletDefinition) {
        this.user = user;
        this.portletDefinition = portletDefinition;
    }

    public LocalAccountPersonImpl getUser() {
		return user;
	}

	public void setUser(LocalAccountPersonImpl userID) {
		this.user = userID;
	}

	public PortletDefinitionImpl getPortletDefinition() {
		return portletDefinition;
	}

	public void setPortletDefinition(PortletDefinitionImpl portletID) {
		this.portletDefinition =  portletID;
	}

    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof MarketplaceRatingPK)){
            return false;
        }else if(obj == this){
            return true;
    	}
        MarketplaceRatingPK tempRating = (MarketplaceRatingPK)obj;
        return new EqualsBuilder().
            append(user, tempRating.user).
            append(portletDefinition, tempRating.portletDefinition).
            isEquals();
    }

	@Override
	public int hashCode(){
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            append(user).
	            append(portletDefinition).
	            toHashCode();
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("User: ");
		builder.append(this.user);
		builder.append("\n");
		builder.append("Portlet: ");
		builder.append(this.portletDefinition);
		return builder.toString();
	}
}
