/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ldap.ILdapServer;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * LDAP implementation of {@link IPersonAttributeDao}. This is code copied
 * from uPortal 2.4 {@link org.jasig.portal.services.PersonDirectory} and
 * made to implement this DAO interface. Dependent upon JNDI.
 * 
 * In the case of multi valued attributes, now stores a
 * {@link java.util.ArrayList} rather than a {@link java.util.Vector}.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class LdapPersonAttributeDaoImpl extends AbstractDefaultQueryPersonAttributeDao {
    private static final Log LOG = LogFactory.getLog(LdapPersonAttributeDaoImpl.class);
    
    /**
     * Time limit, in milliseconds, for LDAP query.  
     * Zero means wait indefinitely.
     */
    private int timeLimit = 0;

    /**
     * The query we should execute.
     */
    private String query;
    
    /**
     * Map from LDAP attribute names to uPortal attribute names.
     */
    private Map attributeMappings = Collections.EMPTY_MAP;
    
    /**
     * {@link Set} of attributes that may be provided for a user.
     */
    private Set userAttributes = Collections.EMPTY_SET;
    
    /**
     * List of attributes to use in the query.
     */
    private List queryAttributes = Collections.EMPTY_LIST;

    /**
     * The ldap server to use to make the queries against.
     */
    private ILdapServer ldapServer;
    

    /**
     * Returned {@link Map} will have values of String or String[] or byte[]
     * 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        //Checks to make sure the argument & state is valid
        if (seed == null)
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        
        if (this.ldapServer == null)
            throw new IllegalStateException("ILdapServer is null");

        if (this.query == null)
            throw new IllegalStateException("query is null");

        //Ensure the data needed to run the query is avalable
        if (!((queryAttributes != null && seed.keySet().containsAll(queryAttributes)) || (queryAttributes == null && seed.containsKey(this.getDefaultAttributeName())))) {
            return null;
        }

        //Connect to the LDAP server
        DirContext context = null;
        try {
            context = this.ldapServer.getConnection();
            
            if (context == null) {
                throw new DataAccessResourceFailureException("No LDAP Connection could be obtained. Aborting ldap person attribute lookup.");
            }
           
            // Search for the userid in the usercontext subtree of the directory
            // Use the uidquery substituting username for {0}, {1}, ...
            final SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setTimeLimit(this.timeLimit);

            //Can't just to a toArray here since the order of the keys in the Map
            //may not match the order of the keys in the List and it is important to
            //the query.
            final Object[] args = new Object[this.queryAttributes.size()];
            for (int index = 0; index < args.length; index++) {
                final String attrName = (String)this.queryAttributes.get(index);
                args[index] = seed.get(attrName);
            }
            
            //Search the LDAP
            final NamingEnumeration userlist = context.search(this.ldapServer.getBaseDN(), this.query, args, sc);
            try {
                final Map rowResults = new HashMap();
                
                if (userlist.hasMoreElements()) {
                    final SearchResult result = (SearchResult)userlist.next();
                    
                    //Only allow one result for the query, do the check here to
                    //save on attribute processing time.
                    if (userlist.hasMoreElements()) {
                        throw new IncorrectResultSizeDataAccessException("More than one result for ldap person attribute search.", 1, -1);
                    }

                    final Attributes ldapAttributes = result.getAttributes();
                    
                    //Iterate through the attributes
                    for (final Iterator ldapAttrIter = this.attributeMappings.keySet().iterator(); ldapAttrIter.hasNext();) {
                        final String ldapAttributeName = (String)ldapAttrIter.next();
                        
                        final Attribute attribute = ldapAttributes.get(ldapAttributeName);
                        
                        //The attribute exists
                        if (attribute != null) {
                            for (final NamingEnumeration attrValueEnum = attribute.getAll(); attrValueEnum.hasMore();) {
                                Object attributeValue = attrValueEnum.next();
                                
                                //Convert everything except byte[] to String
                                //TODO should we be doing this conversion?
                                if (!(attributeValue instanceof byte[])) {
                                    attributeValue = attributeValue.toString();
                                }
                                
                                //See if the ldap attribute is mapped
                                Set attributeNames = (Set)attributeMappings.get(ldapAttributeName);
                                
                                //No mapping was found, just use the ldap attribute name
                                if (attributeNames == null)
                                    attributeNames = Collections.singleton(ldapAttributeName);
                                
                                //Run through the mapped attribute names
                                for (final Iterator attrNameItr = attributeNames.iterator(); attrNameItr.hasNext();){
                                    final String attributeName = (String)attrNameItr.next();
                                    
                                    MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
                                }
                            }
                        }
                    }
                }
                
                return rowResults;
            }
            finally {
                try {
                    userlist.close();
                }
                catch (final NamingException ne) {
                    LOG.warn("Error closing ldap person attribute search results.", ne);
                }
            }
        } 
        catch (final Throwable t) {
            throw new DataAccessResourceFailureException("LDAP person attribute lookup failure.", t);
        } 
        finally {
            this.ldapServer.releaseConnection(context);
        }
    }
    
    /* 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.userAttributes;
    }
    
    /**
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map getLdapAttributesToPortalAttributes() {
        return this.attributeMappings;
    }
    
    /**
     * Set the {@link Map} to use for mapping from a ldap attribute name to a 
     * portal attribute name or {@link Set} of portal attribute names. Ldap
     * attribute names that are specified but have null mappings will use the
     * ldap attribute name for the portal attribute name.
     * Ldap attribute names that are not specified as keys in this {@link Map}
     * will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param ldapAttributesToPortalAttributesArg {@link Map} from ldap attribute names to portal attribute names.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setLdapAttributesToPortalAttributes(final Map ldapAttributesToPortalAttributesArg) {
        this.attributeMappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(ldapAttributesToPortalAttributesArg);
        final Collection userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.attributeMappings.values()); 
        
        this.userAttributes = Collections.unmodifiableSet(new HashSet(userAttributeCol));

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
     * @return Returns the query.
     */
    public String getQuery() {
        return this.query;
    }
    
    /**
     * @param uidQuery The query to set.
     */
    public void setQuery(String uidQuery) {
        this.query = uidQuery;
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
    /**
     * @return Returns the queryAttributes.
     */
    public List getQueryAttributes() {
        return this.queryAttributes;
    }
    /**
     * @param queryAttributes The queryAttributes to set.
     */
    public void setQueryAttributes(List queryAttributes) {
        this.queryAttributes = Collections.unmodifiableList(new LinkedList(queryAttributes));;
    }
}
