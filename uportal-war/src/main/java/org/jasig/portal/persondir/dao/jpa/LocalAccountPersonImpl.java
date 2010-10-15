package org.jasig.portal.persondir.dao.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.persondir.ILocalAccountPerson;

@Entity
@Table(name = "UP_PERSON_DIR")
@GenericGenerator(name = "UP_PERSON_DIR_GEN", strategy = "native", parameters = {
        @Parameter(name = "sequence", value = "UP_PERSON_DIR_SEQ"),
        @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
        @Parameter(name = "column", value = "NEXT_UP_PERSON_DIR_HI") })
public class LocalAccountPersonImpl implements Serializable, ILocalAccountPerson {
    
    @Id
    @GeneratedValue(generator = "UP_PERSON_DIR_GEN")
    @Column(name = "USER_DIR_ID")
    private final long id;
    
    @Column(name = "USER_NAME", length = 35, nullable = false)
    private String name;
    
    @Column(name = "ENCRPTD_PSWD", length = 256)
    private String password;
    
    @Column(name = "LST_PSWD_CGH_DT")
    private Date lastPasswordChange;

    @OneToMany(targetEntity = LocalAccountPersonAttributeImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DIR_ID", nullable = false)
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private Collection<LocalAccountPersonAttributeImpl> attributes = new ArrayList<LocalAccountPersonAttributeImpl>(0);

    public LocalAccountPersonImpl() {
        this.id = -1;
    }
    
    public long getId() {
        return id;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getLastPasswordChange()
     */
    public Date getLastPasswordChange() {
        return lastPasswordChange;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setLastPasswordChange(java.util.Date)
     */
    public void setLastPasswordChange(Date lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(String name) {
        final List<Object> values = this.getAttributeValues(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributeValues(java.lang.String)
     */
    public List<Object> getAttributeValues(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                return this.getObjectValues(attribute);
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributes()
     */
    public Map<String, List<Object>> getAttributes() {
        final Map<String, List<Object>> attributeMap = new LinkedHashMap<String, List<Object>>();
        
        for (final LocalAccountPersonAttributeImpl attribute : attributes) {
            final List<Object> objValues = this.getObjectValues(attribute);
            attributeMap.put(attribute.getName(), objValues);
        }
        
        final Object firstNames = getAttributeValue("given");
        final Object lastNames = getAttributeValue("sn");
        final StringBuilder displayName = new StringBuilder();
        if (firstNames != null) {
            displayName.append(firstNames).append(" ");
        }
        if (lastNames != null) {
            displayName.append(lastNames);
        }
        attributeMap.put("displayName", Collections.<Object>singletonList(displayName.toString()));
        
        return Collections.unmodifiableMap(attributeMap);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setAttribute(java.lang.String, java.util.List)
     */
    public void setAttribute(String name, List<String> values) {
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                attribute.setValues(values);
                return;
            }
        }
        
        attributes.add(new LocalAccountPersonAttributeImpl(name, values));
    }

    public void setAttribute(String name, String value) {
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                attribute.setValues(Collections.singletonList(value));
                return;
            }
        }
        
        attributes.add(new LocalAccountPersonAttributeImpl(name, Collections.singletonList(value)));
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setAttributes(java.util.Map)
     */
    public void setAttributes(Map<String, List<String>> attributes) {
        //Tries to modify as many of the existing attributes in place to reduce DB churn in hibernate
        
        //Make a local copy so we don't edit the original reference
        attributes = new LinkedHashMap<String, List<String>>(attributes);
        
        for (final Iterator<LocalAccountPersonAttributeImpl> attributesItr = this.attributes.iterator(); attributesItr.hasNext(); ) {
            final LocalAccountPersonAttributeImpl attribute = attributesItr.next();
            
            //Remove the new values for the attribute from the input map
            final String name = attribute.getName();
            final List<String> newValues = attributes.remove(name);

            //If no new values remove the attribute
            if (newValues == null) {
                attributesItr.remove();
            }
            //Otherwise update the existing values
            else {
                attribute.setValues(new ArrayList<String>(newValues));
            }
        }
        
        //Add any remaining new attributes to the list
        for (final Map.Entry<String, List<String>> attribute : attributes.entrySet()) {
            final String name = attribute.getKey();
            final List<String> values = attribute.getValue();
            this.attributes.add(new LocalAccountPersonAttributeImpl(name, values));
        }
    }
    
    protected List<Object> getObjectValues(LocalAccountPersonAttributeImpl attribute) {
        final List<String> values = attribute.getValues();
        final List<Object> objValues = new ArrayList<Object>(values.size());
        objValues.addAll(values);
        return Collections.unmodifiableList(objValues);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((lastPasswordChange == null) ? 0 : lastPasswordChange.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocalAccountPersonImpl)) {
            return false;
        }
        LocalAccountPersonImpl other = (LocalAccountPersonImpl) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        }
        else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (lastPasswordChange == null) {
            if (other.lastPasswordChange != null) {
                return false;
            }
        }
        else if (!lastPasswordChange.equals(other.lastPasswordChange)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        }
        else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", this.id)
        .append("name", this.name)
        .append("attributes", this.attributes)
        .toString();
    }

}
