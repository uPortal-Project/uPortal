/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PersonFactory;

/**
 * This is a reference IPerson implementation.
 * @author Adam Rybicki, arybicki@unicon.net
 * @version $Revision$
 */
public class PersonImpl implements IPerson {
    private static final long serialVersionUID = 1L;

    protected Map<String, List<Object>> userAttributes = null;
    protected String m_FullName = null;
    protected int m_ID = -1;
    protected ISecurityContext m_securityContext = null;
    protected EntityIdentifier m_eid = new EntityIdentifier(null, IPerson.class);
    protected boolean entityIdentifierSet = false;

    public ISecurityContext getSecurityContext() {
        return m_securityContext;
    }

    public void setSecurityContext(ISecurityContext securityContext) {
        m_securityContext = securityContext;
    }

    /**
     * Returns an attribute for a key.  For objects represented as strings,
     * a <code>java.lang.String</code> will be returned.  Binary values will
     * be represented as byte arrays.
     * @param key the attribute name.
     * @return value the attribute value identified by the key.
     */
    public Object getAttribute(String key) {
        if (this.userAttributes == null) {
            return null;
        }

        final List<Object> values = this.userAttributes.get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }

        return null;
    }

    /**
     * Returns multiple attributes for a key.  If only one
     * value exists, it will be returned in an array of size one.
     * @param key the attribute name
     * @return the array of attribute values identified by the key
     */
    public Object[] getAttributeValues(String key) {
        if (this.userAttributes == null) {
            return null;
        }

        final List<Object> values = this.userAttributes.get(key);
        if (values != null) {
            return values.toArray();
        }

        return null;
    }

    /**
     * Returns a <code>java.util.Enumeration</code> of all the attribute values.
     * @return <code>java.util.Enumeration</code> of the attributes.
     */
    public Enumeration<List<Object>> getAttributes() {
        if (this.userAttributes == null) {
            return null;
        }
        
        final Collection<List<Object>> values = this.userAttributes.values();
        return Collections.enumeration(values);
    }

    /**
     * Returns an enumeration of all of the attribute names associated with the user
     * @return enumeration of all of the attribute names associated with the user
     */
    public Enumeration<String> getAttributeNames() {
        if (this.userAttributes == null) {
            return null;
        }
        
        final Set<String> names = this.userAttributes.keySet();
        return Collections.enumeration(names);
    }

    /**
     * Sets the specified attribute to a value.
     *
     * Reference impementation checks for the setting of the username attribute
     * and updates the EntityIdentifier accordingly
     *
     * @param key Attribute's name
     * @param value Attribute's value
     */
    public void setAttribute(String key, Object value) {
        if (value == null) {
            this.setAttribute(key, (List<Object>)null);
        }
        else {
            this.setAttribute(key, Collections.singletonList(value));
        }
    }
    
    public void setAttribute(String key, List<Object> value) {
        if (this.userAttributes == null) {
            this.userAttributes = new HashMap<String, List<Object>>();
        }

        if (value != null) {
            this.userAttributes.put(key, value);
        }
        else {
            this.userAttributes.remove(key);
        }
        
        if (!this.entityIdentifierSet && key.equals(IPerson.USERNAME)) {
            final Object userName = value != null && value.size() > 0 ? value.get(0) : null;
            this.m_eid = new EntityIdentifier(String.valueOf(userName), IPerson.class);
        }
    }

    /**
     * Sets the specified attributes. Uses {@link #setAttribute(String, Object)}
     * to set each.
     *
     * @see org.jasig.portal.security.IPerson#setAttributes(java.util.Map)
     */
    public void setAttributes(final Map<String, List<Object>> attrs) {
        for (final Entry<String, List<Object>> attrEntry : attrs.entrySet()) {
            final String key = attrEntry.getKey();
            final List<Object> value = attrEntry.getValue();
            this.setAttribute(key, value);
        }
    }

    /**
     * Returns the user's ID that was used for authentication.
     * Does not correlate to any eduPerson attribute but is the key
     * for user data in uPortal reference rdbms.
     * @return User's ID.
     */
    public int getID() {
        return m_ID;
    }

    /**
     * Sets the user's ID.
     * @param sID User's ID as supplied for authentication
     * Does not correlate to any eduPerson attribute but is the key
     * for user data in uPortal reference rdbms.
     */
    public void setID(int sID) {
        m_ID = sID;
    }

    /**
     * Returns the user's name that was established during authentication.
     * Correlates to cn (common name) in the eduPerson 1.0 specification.
     * @return User's name.
     */
    public String getFullName() {
        return m_FullName;
    }

    /**
     * Sets the user's full name.
     * @param sFullName User's name as established during authentication
     * Correlates to cn (common name) in the eduPerson 1.0 specification.
     */
    public void setFullName(String sFullName) {
        m_FullName = sFullName;
    }

    /**
     * Determines whether or not this person is a "guest" user.
     * <p>
     * This person is a "guest" if both of the following are true:
     * <ol>
     *   <li>This person has not successfully authenticated with the portal.</li>
     *   <li>This person's user name matches the value of the property
     *       <code>org.jasig.portal.security.PersonImpl.guest_user_name</code>
     *       in <code>portal.properties</code>.</li>
     * </ol>
     * @return <code>true</code> if person is a guest, otherwise <code>false</code>
     */
    public boolean isGuest() {
        boolean isGuest = false;
        String userName = (String) getAttribute(IPerson.USERNAME);
        if (userName.equals(PersonFactory.GUEST_USERNAME) && !m_securityContext.isAuthenticated()) {
            isGuest = true;
        }
        return isGuest;
    }

    /**
     * Returns an EntityIdentifier for this person.  The key contains the value
     * of the eudPerson username attribute, or null
     *
     * @return EntityIdentifier with attribute 'username' as key.
     */
    public EntityIdentifier getEntityIdentifier() {
        return m_eid;
    }

    /**
     * One time set of the entity identifier
     * @param ei
     */
    public void setEntityIdentifier(final EntityIdentifier ei) {
        m_eid = ei;
        entityIdentifierSet = true;
    }

    /* (non-Javadoc)
    * @see java.security.Principal#getName()
    */
    public String getName() {
        return (String) getAttribute(IPerson.USERNAME);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PersonImpl.class.getName());
        sb.append(" fullName=[");
        sb.append(this.m_FullName);
        sb.append("]");
        sb.append(" id=[");
        sb.append(this.m_ID);
        sb.append("]");
        sb.append(" securityContext=[");
        sb.append(this.m_securityContext);
        sb.append("]");
        sb.append(" attributes=[");
        sb.append(this.userAttributes);
        sb.append("]");
        sb.append(" isGuest:");
        sb.append(this.isGuest());
        return sb.toString();
    }
}
