/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.jasig.portal.ldap.ILdapServer;
import org.jasig.portal.ldap.LdapServerImpl;
import org.jasig.portal.ldap.LdapServices;
import org.jasig.portal.rdbm.RDBMServicesDataSource;

import org.jasig.portal.services.persondir.support.PersonAttributeDao;
import org.jasig.portal.services.persondir.support.LdapPersonAttributeDaoImpl;
import org.jasig.portal.services.persondir.support.JdbcPersonAttributeDaoImpl;

/**
 * Adapts from a PersonDirInfo to a PersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirInfoAdaptor implements PersonAttributeDao {

    private Log log = LogFactory.getLog(getClass());
    
    /**
     * Delegate PersonAttributeDao which we construct from the PersonDirInfo
     * provided at construction.
     */
    private PersonAttributeDao delegate;
    
    /**
     * Instantiate this class implementing the PersonAttributeDao defined
     * by the given PersonDirInfo. Throws IllegalArgumentException if the
     * given info doesn't define a valid PersonAttributeDao (and this class
     * succeeds in detecting the problem).
     * @param info PersonDirInfo defining the attribute source we implement
     * @throws IllegalArgumentException
     */
    public PersonDirInfoAdaptor(PersonDirInfo info){
        if (this.log.isTraceEnabled())
            this.log.trace("entering PersonDirInfoAdaptor(" + info + ")");
        if (info == null)
            throw new IllegalArgumentException("Cannot adapt a null PersonDirInfo.");
        
        String validityMessage = info.validate();
        if (validityMessage != null) {
            throw new IllegalArgumentException("The PersonDirInfo to be adapted " +
                    "had illegal state: " + validityMessage);
        }
        
        if (info.isJdbc()) {
            this.delegate = jdbcDao(info);
        } else if (info.isLdap()) {
           this.delegate = ldapDao(info);
        } else {
            throw new IllegalArgumentException("Info received was neither a JDBC " +
                    "nor an LDAP.  I can't adapt it:" + info);
        }
        if (this.log.isTraceEnabled())
            this.log.trace("constructed " + this);
    }
    
    /**
     * Obtain a JDBCPersonAttributeDaoImpl for the given PersonDirInfo.
     * @param info - a JDBC persondirinfo
     * @return - a JDBCPersonAttributeDaoImpl
     */
    private PersonAttributeDao jdbcDao(PersonDirInfo info) {
        
        String sql = info.getUidquery();
        
        // determine where to get our DataSource
        DataSource source = null;
        String dsRef = info.getResRefName();
        if (dsRef != null && !"".equals(dsRef)){
            // get a DataSource from RDBMServices
            RDBMServicesDataSource ds = new RDBMServicesDataSource(dsRef);
            source = ds;
        } else {
            // construct a DataSource adhoc
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(info.getDriver());
            ds.setUsername(info.getLogonid());
            ds.setPassword(info.getLogonpassword());
            ds.setUrl(info.getUrl());
            source = ds;
        }
        
        JdbcPersonAttributeDaoImpl jdbcImpl = 
            new JdbcPersonAttributeDaoImpl(source, sql);
        
        /*
         * Map from JDBC column names to Sets of Strings representing uPortal
         * attribute names.
         */
        Map jdbcToPortalAttribs = new HashMap();
        
        String[] columnNames = info.getAttributenames();
        String[] portalAttribNames = info.getAttributealiases();
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            if (columnName != null && !"".equals(columnName)){
                Set attributeNames = (Set) jdbcToPortalAttribs.get(columnName);
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
     * Obtain a PersonAttributeDao LDAP implementation from suitable info.
     * @param info
     * @return
     */
    private PersonAttributeDao ldapDao(PersonDirInfo info) {
        // configure a new LDAP source from this object
           LdapPersonAttributeDaoImpl ldapImpl = new LdapPersonAttributeDaoImpl();
           
           ILdapServer ldapServer = null;
           String ldapRefName = info.getLdapRefName();
           
           if (ldapRefName != null) {
               ldapServer = LdapServices.getLdapServer(ldapRefName);
               if (ldapServer == null)
                   throw new IllegalArgumentException("LdapServices does not have " +
                        "an LDAP server configured with name [" + ldapRefName + "]");
           
           } else {
               // instantiate an LDAP server ad-hoc.
               
               // set the "usercontext" attribute of the PersonDirInfo as the baseDN
               // of the LdapServerImpl we're instantiating because when 
               
               ldapServer = new LdapServerImpl(info.getUrl(), 
                       info.getUrl(), info.getUsercontext(), null, 
                       info.getLogonid(), info.getLogonpassword(), null);
               
             
           }
           
           ldapImpl.setLdapServer(ldapServer);
           ldapImpl.setTimeLimit(info.getLdaptimelimit());
           ldapImpl.setUidQuery(info.getUidquery());
           
           
         
           /*
            * Map from LDAP attribute names to Sets of Strings representing uPortal
            * attribute names.
            */
           Map ldapToPortalAttribs = new HashMap();
           
           String[] ldapAttribNames = info.getAttributenames();
           String[] portalAttribNames = info.getAttributealiases();
           for (int i = 0; i < ldapAttribNames.length; i++) {
               String ldapAttribName = ldapAttribNames[i];
               Set attributeNames = (Set) ldapToPortalAttribs.get(ldapAttribName);
               if (attributeNames == null)
                   attributeNames = new HashSet();
               attributeNames.add(portalAttribNames[i]);
               ldapToPortalAttribs.put(ldapAttribName, attributeNames);
           }
           
           ldapImpl.setLdapAttributesToPortalAttributes(ldapToPortalAttribs);
           return ldapImpl;
    }

    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.support.PersonAttributeDao#attributesForUser(java.lang.String)
     */
    public Map attributesForUser(String uid) {
        return this.delegate.attributesForUser(uid);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PersonDirInfoAdaptor: delegate=[" + this.delegate + "]");
        return sb.toString();
    }

}

