package org.jasig.portal.persondir.dao.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
    
//    @Column(name = "LST_PSWD_CGH_DT", nullable = false)
    @Transient
    private Date lastPasswordChange;

    @OneToMany(targetEntity = LocalAccountPersonAttributeImpl.class, mappedBy = "person", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private Collection<LocalAccountPersonAttributeImpl> attributes = new ArrayList<LocalAccountPersonAttributeImpl>(0);

    public LocalAccountPersonImpl() {
        this.id = -1;
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
        Map<String, List<Object>> attributeMap  = getAttributes();
        List<Object> values = attributeMap.get(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributeValues(java.lang.String)
     */
    public List<Object> getAttributeValues(String name) {
        Map<String, List<Object>> attributeMap  = getAttributes();
        return attributeMap.get(name);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributes()
     */
    public Map<String, List<Object>> getAttributes() {
        final Map<String, List<Object>> attributeMap = new HashMap<String, List<Object>>();
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            List<Object> values = new ArrayList<Object>();
            values.addAll(attribute.getValues());
            attributeMap.put(attribute.getName(), values);
        }
        
        List<Object> firstNames = attributeMap.get("given");
        List<Object> lastNames = attributeMap.get("sn");
        String displayName = "";
        if (firstNames != null && firstNames.size() > 0) {
            displayName = displayName.concat((String) firstNames.get(0) + " ");
        }
        if (lastNames != null && lastNames.size() > 0) {
            displayName = displayName.concat((String) lastNames.get(0));
        }
        attributeMap.put("displayName", Collections.<Object>singletonList(displayName));
        
        return attributeMap;
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
        
        attributes.add(new LocalAccountPersonAttributeImpl(this, name, values));
    }

    public void setAttribute(String name, String value) {
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                attribute.setValues(Collections.singletonList(value));
                return;
            }
        }
        
        attributes.add(new LocalAccountPersonAttributeImpl(this, name, Collections.singletonList(value)));
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.persondir.jpa.ILocalAccountPersonAttribute#setAttributes(java.util.Map)
     */
    public void setAttributes(Map<String, List<String>> attributes) {
        List<LocalAccountPersonAttributeImpl> a = new ArrayList<LocalAccountPersonAttributeImpl>();
        for (Map.Entry<String, List<String>> attribute : attributes.entrySet()) {
            a.add(new LocalAccountPersonAttributeImpl(this, attribute.getKey(), attribute.getValue()));
        }
        this.attributes = a;
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
