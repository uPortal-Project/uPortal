/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ldap.ILdapServer;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * LDAP implementation of PersonAttributeDao. This is code copied from uPortal
 * 2.4 PersonDirectory and made to implement this DAO interface. Dependent upon
 * JNDI.
 * 
 * In the case of multi valued attributes, now stores a List rather than a Vector.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class LdapPersonAttributeDaoImpl implements PersonAttributeDao {

    private Log log = LogFactory.getLog(getClass());

    /**
     * Time limit, in milliseconds, for LDAP query.  
     * Zero means wait indefinitely.
     */
    private int timeLimit;

    /**
     * The query we should execute.
     */
    private String uidQuery;
    
    /**
     * Map from LDAP attribute names to uPortal attribute names.
     */
    private Map ldapAttributesToPortalAttributes;

    private ILdapServer ldapServer;
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.yale.its.portal.services.persondir.support.PersonAttributeDao#attributesForUser(java.lang.String)
     */
    public Map attributesForUser(String uid) {
        
        if (uid == null)
            throw new IllegalArgumentException("Cannot get attributes for null user.");
        
        DirContext context = null;
        NamingEnumeration userlist = null;

        try {
            context = this.ldapServer.getConnection();
            
            if (context == null) {
                throw new DataAccessResourceFailureException("No LDAP Connection could be obtained. Aborting ldap person attribute lookup.");
            }
           
            // Search for the userid in the usercontext subtree of the directory
            // Use the uidquery substituting username for {0}

            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setTimeLimit(this.timeLimit);
            Object[] args = new Object[] { uid };
            userlist = context
                    .search(this.ldapServer.getBaseDN(), this.uidQuery, args, sc);

            // If one object matched, extract attributes from the attribute list
            Map attribs = new HashMap();

            if (userlist.hasMoreElements()) {
                SearchResult result = (SearchResult) userlist.next();
                Attributes ldapattribs = result.getAttributes();
                for (Iterator ldapAttribsIter = 
                    this.ldapAttributesToPortalAttributes.keySet().iterator(); 
                    ldapAttribsIter.hasNext();
                    ) {
                    String ldapAttribName = (String) ldapAttribsIter.next();
                    
                    Set portalAttribNames = (Set) 
                        this.ldapAttributesToPortalAttributes.get(ldapAttribName);
                    Attribute tattrib = null;
                    if (ldapAttribName != null)
                        tattrib = ldapattribs.get(ldapAttribName);
                    if (tattrib != null) {
                        // determine if this attribute is a String or a binary
                        // (byte array)
                        if (tattrib.size() == 1) {
                            Object att = tattrib.get();
                            if (! (att instanceof byte[])) {
                                att = att.toString();
                            }
                            for (Iterator iter = portalAttribNames.iterator(); iter.hasNext(); ) {
                                String portalAttribName = (String) iter.next();
                                attribs.put(portalAttribName, att);
                            }
                        } else {
                            // multivalued
                            List values = new ArrayList();
                            for (NamingEnumeration ne = tattrib.getAll(); ne
                                    .hasMoreElements();) {
                                Object value = ne.nextElement();
                                if (value instanceof byte[]) {
                                    values.add(value);
                                } else {
                                    values.add(value.toString());
                                }
                            }
                            for (Iterator iter = portalAttribNames.iterator(); iter.hasNext(); ) {
                                String portalAttribName = (String) iter.next();
                                attribs.put(portalAttribName, values);
                            }
                        }
                    }
                }
            }

            return attribs;

        } catch (Throwable t) {
            throw new DataAccessResourceFailureException("LDAP failure.", t);
        } finally {
            try {
                if (userlist != null)
                    userlist.close();
            } catch (Exception e) {
                this.log.error("Exception closing user list.", e);
            }
            
            this.ldapServer.releaseConnection(context);
        }

    }

    /**
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map getLdapAttributesToPortalAttributes() {
        return this.ldapAttributesToPortalAttributes;
    }
    
    /**
     * Set the Map from LDAP attribute names to Sets of Strings that are the names
     * of the one or more uPortal attributes. LDAP attribute names that do not appear
     * as keys in this map will be mapped to attribute names of the same value as
     * the column name. As a convenience, the Set received as an argument by
     * this method may map to Strings representing the name of the single
     * uPortal attribute to be set from the column name rather than to Sets containing
     * that one String.  This method translates to the Map expected by internal
     * consumers of this property.  
     * 
     * @param ldapAttributesToPortalAttributesArg  Map from non-null Strings
     * representing LDAP attribute names to non-null Strings representing uPortal 
     * attribute names or Sets of such Strings.
     */
    public void setLdapAttributesToPortalAttributes(
            Map ldapAttributesToPortalAttributesArg) {
        /*
         * This implementation validates and makes a defensive copy of the
         * columnsToAttributes map argument.
         */
        Map internalLdapToAttributes = new HashMap();
        if (ldapAttributesToPortalAttributesArg == null)
            throw new IllegalArgumentException("Cannot set the mapping from " +
                    "ldap attribute names to attributes to be null.");
        for (Iterator iter = ldapAttributesToPortalAttributesArg.keySet().iterator(); iter.hasNext();){
            
            // validate the key
            Object key = iter.next();
            if (key == null)
                throw new IllegalArgumentException("The map from ldap attribute names to" +
                        " uPortal attributes must not have any null keys.");
            if (! (key instanceof String))
                throw new IllegalArgumentException("The map from ldap attribute names to" +
                        " uPortal attributes must not have any non-String keys.  The key ["
                        + key + " is of type [" + key.getClass().getName() + "] which is not a String.");
            
            // validate the value
            Object value = ldapAttributesToPortalAttributesArg.get(key);
            if (value == null)
                throw new IllegalArgumentException("Null must not appear as the" +
                        "value of any key-value pair in the ldapAttributesToPortalAttributes map.  " +
                        "However, null was the value for the key [" + key + "]");
            if (value instanceof String) {
                // translate to a Set containing the String
                value = Collections.singleton(value);
            } else if (value instanceof Set) {
                Set valueAsSet = (Set) value;
                if (valueAsSet.isEmpty()) {
                    throw new IllegalArgumentException("ldapAttributesToPortalAttributes mapping illegal: " +
                            "Key [" + key + "] maps to empty set." +
                            " Keys must map to either a non-null String or a " +
                            "non-empty Set of non-null Strings.");
                }
            } else {
                throw new IllegalArgumentException("ldapAttributesToPortalAttributes mapping illegal: " +
                        "Key [" + key + "] maps to an object that is neither a String nor  Set:" +
                                " it is of type [" + value.getClass().getName() + 
                                "], with String representation " + value);
            }
            
            internalLdapToAttributes.put(key, value);
        }
        
        this.ldapAttributesToPortalAttributes = internalLdapToAttributes;
    }
    
    /**
     * @return Returns the timeLimit.
     */
    public int getTimeLimit() {
        return this.timeLimit;
    }
    
    /**
     * @param timeLimit The timeLimit to set.
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    /**
     * @return Returns the uidQuery.
     */
    public String getUidQuery() {
        return this.uidQuery;
    }
    
    /**
     * @param uidQuery The uidQuery to set.
     */
    public void setUidQuery(String uidQuery) {
        this.uidQuery = uidQuery;
    }
   
    /**
     * @return Returns the ldapServer.
     */
    public ILdapServer getLdapServer() {
        return this.ldapServer;
    }
    /**
     * @param ldapServer The ldapServer to set.
     */
    public void setLdapServer(ILdapServer ldapServer) {
        this.ldapServer = ldapServer;
    }
}
