/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ldap.ILdapServer;
import org.jasig.portal.ldap.LdapServerImpl;
import org.jasig.portal.ldap.LdapServices;
import org.jasig.portal.rdbm.RDBMServicesDataSource;
import org.jasig.portal.services.persondir.support.AbstractDefaultQueryPersonAttributeDao;
import org.jasig.portal.services.persondir.support.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.JdbcPersonAttributeDaoImpl;
import org.jasig.portal.services.persondir.support.LdapPersonAttributeDaoImpl;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Adapts from a {@link PersonDirInfo} to a {@link IPersonAttributeDao}.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirInfoAdaptor implements IPersonAttributeDao {
    private static final Log LOG = LogFactory.getLog(PersonDirInfoAdaptor.class);
    
    //Needed since our Jdbc Dao needs an attribute to map queries with 
    private static final String QUERY_ATTRIBUTE = "uid";
    private static final List QUERY_ATTRIBUTE_LIST = Collections.singletonList(QUERY_ATTRIBUTE);
    
    /**
     * Delegate IPersonAttributeDao which we construct from the PersonDirInfo
     * provided at construction.
     */
    private IPersonAttributeDao delegate;
    
    /**
     * Instantiate this class implementing the IPersonAttributeDao defined
     * by the given PersonDirInfo. Throws IllegalArgumentException if the
     * given info doesn't define a valid IPersonAttributeDao (and this class
     * succeeds in detecting the problem).
     * 
     * @param info PersonDirInfo defining the attribute source we implement
     * @throws IllegalArgumentException
     */
    public PersonDirInfoAdaptor(final PersonDirInfo info) {
        if (LOG.isTraceEnabled())
            LOG.trace("entering PersonDirInfoAdaptor(" + info + ")");
        
        if (info == null)
            throw new IllegalArgumentException("info cannot be null.");
        
        final String validityMessage = info.validate();
        if (validityMessage != null) {
            throw new IllegalArgumentException("The PersonDirInfo to be adapted has illegal state: " + validityMessage);
        }
        
        if (info.isJdbc()) {
            this.delegate = this.jdbcDao(info);
        }
        else if (info.isLdap()) {
            this.delegate = this.ldapDao(info);
        }
        else {
            throw new IllegalArgumentException("Provided PersonDirInfo is not JDBC or LDAP, unable to adapat:" + info);
        }
        
        if (this.delegate == null)
            throw new IllegalStateException("There was an unknown problem creating the IPersonAttributeDao delegate.");
        
        if (this.delegate instanceof AbstractDefaultQueryPersonAttributeDao) {
            ((AbstractDefaultQueryPersonAttributeDao)this.delegate).setDefaultAttributeName(QUERY_ATTRIBUTE);
        }
        
        if (LOG.isTraceEnabled())
            LOG.trace("constructed " + this);
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getDefaultAttributeName()
     */
    public String getDefaultAttributeName() {
        return QUERY_ATTRIBUTE;
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.delegate.getPossibleUserAttributeNames();
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(Map seed) {
        return this.delegate.getUserAttributes(seed);
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public Map getUserAttributes(String uid) {
        return this.delegate.getUserAttributes(uid);
    }
    
    /* 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PersonDirInfoAdaptor: delegate=[" + this.delegate + "]");
        return sb.toString();
    }
    
    /**
     * Obtain a {@link JdbcPersonAttributeDaoImpl} for the given {@link PersonDirInfo}.
     * 
     * @param info The {@link PersonDirInfo} to use as a basis for the DAO
     * @return A fully configured {@link JdbcPersonAttributeDaoImpl}
     */
    private IPersonAttributeDao jdbcDao(final PersonDirInfo info) {
        final String sql = info.getUidquery();
        
        // determine where to get our DataSource
        DataSource source = null;
        
        final String dsRefName = info.getResRefName();
        if (dsRefName != null && dsRefName.length() > 0) {
            // get a DataSource from RDBMServices
            source = new RDBMServicesDataSource(dsRefName);
        }
        else {
            // construct a DataSource adhoc
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(info.getDriver());
            ds.setUsername(info.getLogonid());
            ds.setPassword(info.getLogonpassword());
            ds.setUrl(info.getUrl());
            
            source = ds;
        }
        
        final JdbcPersonAttributeDaoImpl jdbcImpl = new JdbcPersonAttributeDaoImpl(source, QUERY_ATTRIBUTE_LIST, sql);
        
        // Map from JDBC column names to Sets of Strings representing uPortal
        // attribute names.
        final Map jdbcToPortalAttribs = new HashMap();
        
        final String[] columnNames = info.getAttributenames();
        final String[] portalAttribNames = info.getAttributealiases();
        
        for (int i = 0; i < columnNames.length; i++) {
            final String columnName = columnNames[i];
            
            if (columnName != null && columnName.length() > 0){
                Set attributeNames = (Set)jdbcToPortalAttribs.get(columnName);
                
                if (attributeNames == null)
                    attributeNames = new HashSet();
                
                attributeNames.add(portalAttribNames[i]);
                jdbcToPortalAttribs.put(columnName, attributeNames);
            }
        }
        
        jdbcImpl.setColumnsToAttributes(jdbcToPortalAttribs);
        
        return jdbcImpl;
    }

    /**
     * Obtain a {@link LdapPersonAttributeDaoImpl} for the given {@link PersonDirInfo}.
     * 
     * @param info The {@link PersonDirInfo} to use as a basis for the DAO
     * @return A fully configured {@link LdapPersonAttributeDaoImpl}
     */
    private IPersonAttributeDao ldapDao(final PersonDirInfo info) {
        final LdapPersonAttributeDaoImpl ldapImpl = new LdapPersonAttributeDaoImpl();
           
        ILdapServer ldapServer = null;
        
        final String ldapRefName = info.getLdapRefName();
        if (ldapRefName != null) {
            ldapServer = LdapServices.getLdapServer(ldapRefName);
            
            if (ldapServer == null)
                throw new IllegalArgumentException("LdapServices does not have an LDAP server configured with name [" + ldapRefName + "]");
        }
        else {
            // instantiate an LDAP server ad-hoc.
       
            // set the "usercontext" attribute of the PersonDirInfo as the baseDN
            // of the LdapServerImpl we're instantiating because when 
            ldapServer = new LdapServerImpl(info.getUrl(), info.getUrl(), info.getUsercontext(), null, info.getLogonid(), info.getLogonpassword(), null);
        }
       
        ldapImpl.setLdapServer(ldapServer);
        ldapImpl.setTimeLimit(info.getLdaptimelimit());
        ldapImpl.setQuery(info.getUidquery());
        ldapImpl.setQueryAttributes(QUERY_ATTRIBUTE_LIST);
       
       
     
        // Map from LDAP attribute names to Sets of Strings representing uPortal
        // attribute names.
        final Map ldapToPortalAttribs = new HashMap();
       
        final String[] ldapAttribNames = info.getAttributenames();
        final String[] portalAttribNames = info.getAttributealiases();
        for (int i = 0; i < ldapAttribNames.length; i++) {
            final String ldapAttribName = ldapAttribNames[i];
           
            Set attributeNames = (Set)ldapToPortalAttribs.get(ldapAttribName);
            if (attributeNames == null)
                attributeNames = new HashSet();
            
            attributeNames.add(portalAttribNames[i]);
            ldapToPortalAttribs.put(ldapAttribName, attributeNames);
        }
       
        ldapImpl.setLdapAttributesToPortalAttributes(ldapToPortalAttribs);
       
        return ldapImpl;
    }
}

