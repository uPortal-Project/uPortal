/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.services.persondir.support.merger.AttributeMerger;
import org.jasig.portal.services.persondir.support.merger.ReplacingAttributeAdder;

/**
 * A PersonAttributeDao implementation which iterates over children 
 * PersonAttributeDaos and merges their reported attributes in a configurable way.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MergingPersonAttributeDaoImpl 
    implements PersonAttributeDao {

    private static final Log log = 
        LogFactory.getLog(MergingPersonAttributeDaoImpl.class);
    
    /**
     * A List of child PersonAttributeDao instances which we will poll in order.
     */
    private List personAttributeDaos;
    
    /**
     * Strategy for merging together the results from successive PersonAttributeDaos.
     */
    private AttributeMerger merger = new ReplacingAttributeAdder();
    
    /**
     * True if we should catch, log, and ignore Throwables propogated by
     * individual DAOs.
     */
    private boolean recoverExceptions = true;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#attributesForUser(java.lang.String)
     */
    public Map attributesForUser(String uid) {
        
        if (uid == null)
            throw new IllegalArgumentException("Cannot get attributes for null user.");
        
        Map attributes = new HashMap();
        
        for (Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            PersonAttributeDao currentlyConsidering = 
                (PersonAttributeDao) iter.next();
            
            Map marginalAttributes = new HashMap();
            try {
                marginalAttributes = currentlyConsidering.attributesForUser(uid);
            } catch (RuntimeException rte) {
                log.warn("Exception thrown by DAO: " + currentlyConsidering, rte);
                if (!this.recoverExceptions)
                    throw rte;
            }

            attributes = this.merger.mergeAttributes(attributes, marginalAttributes);
        }
        
        return attributes;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#getAttributeNames()
     */
    public Set getAttributeNames() {
        final Set attrNames = new HashSet();
        
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final PersonAttributeDao currentDao = (PersonAttributeDao) iter.next();
            final Set currentDaoAttrNames = currentDao.getAttributeNames();
            
            if (currentDaoAttrNames != null)
                attrNames.addAll(currentDaoAttrNames);
        }
        
        return Collections.unmodifiableSet(attrNames);
    }

    /**
     * Get the strategy whereby we accumulate attributes.
     * @return Returns the merger.
     */
    public AttributeMerger getMerger() {
        return this.merger;
    }
    
    /**
     * Set the strategy whereby we accumulate attributes from the results of 
     * polling our delegates.
     * @param merger The merger to set.
     */
    public void setMerger(AttributeMerger merger) {
        this.merger = merger;
    }
    
    
    /**
     * Get the List of delegates which we will poll for attributes.
     * @return Returns the personAttributeDaos.
     */
    public List getPersonAttributeDaos() {
        return this.personAttributeDaos;
    }
    
    /**
     * Set the List of delegates which we will poll for attributes.
     * @param personAttributeDaos The personAttributeDaos to set.
     */
    public void setPersonAttributeDaos(List personAttributeDaos) {
        this.personAttributeDaos = personAttributeDaos;
    }
    
    /**
     * True if this class will catch exceptions thrown by its delegate DAOs
     * and fail to propogate them.  False if this class will stop on failure.
     * @return Returns the recoverExceptions.
     */
    public boolean isRecoverExceptions() {
        return this.recoverExceptions;
    }
    
    /**
     * Set to true if you would like this class to swallow RuntimeExceptions
     * thrown by its delegates.  This allows it to recover if a particular attribute
     * source fails, still considering previous and subsequent sources.
     * Set to false if you would like this class to fail hard upon any Throwable
     * thrown by its children.  This is desirable in cases where your Portal will not
     * function without attributes from all of its sources.
     * @param recoverExceptions The recoverExceptions to set.
     */
    public void setRecoverExceptions(boolean recoverExceptions) {
        this.recoverExceptions = recoverExceptions;
    }
}