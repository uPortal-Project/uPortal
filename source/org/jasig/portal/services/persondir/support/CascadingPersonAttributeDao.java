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
import org.jasig.portal.services.persondir.support.merger.IAttributeMerger;
import org.jasig.portal.services.persondir.support.merger.ReplacingAttributeAdder;


/**
 * This {@link org.jasig.portal.services.persondir.support.IPersonAttributeDao}
 * implementation iterates through an ordered {@link java.util.List} of
 * {@link org.jasig.portal.services.persondir.support.IPersonAttributeDao} impls,
 * taking the resulting Map from each and passing it on as the query seed for
 * the next dao. This may cause problems for multi-valued attributes since the
 * current dao implementations do not understand values of type {@link java.util.List}.
 * <br>
 * The default merger is {@link ReplacingAttributeAdder}
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class CascadingPersonAttributeDao extends AbstractDefaultQueryPersonAttributeDao {
    private static final Log LOG = LogFactory.getLog(CascadingPersonAttributeDao.class);
    
    /**
     * A List of child IPersonAttributeDao instances which we will poll in order.
     */
    private List personAttributeDaos;
    
    /**
     * Strategy for merging together the results from successive PersonAttributeDaos.
     */
    private IAttributeMerger attrMerger = new ReplacingAttributeAdder();
    
    /**
     * True if we should catch, log, and ignore Throwables propogated by
     * individual DAOs.
     */
    private boolean recoverExceptions = true;
    
    /**
     * Iterates through the configured {@link List} of {@link IPersonAttributeDao}
     * instances. The results from each DAO are merged into the result {@link Map}
     * by the configured {@link IAttributeMerger}. 
     * 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        //Ensure the arguements and state are valid
        if (seed == null)
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        
        if (this.personAttributeDaos == null)
            throw new IllegalStateException("No IPersonAttributeDaos have been specified.");
        
        
        Map resultAttributes = new HashMap(seed);
        
        //Iterate through the configured IPersonAttributeDaos, querying each.
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final IPersonAttributeDao currentlyConsidering = (IPersonAttributeDao)iter.next();
            
            Map currentAttributes = new HashMap();
            try {
                currentAttributes = currentlyConsidering.getUserAttributes(resultAttributes);
            }
            catch (final RuntimeException rte) {
                final String msg = "Exception thrown by DAO: " + currentlyConsidering;

                if (this.recoverExceptions) {
                    LOG.warn("Recovering From " + msg, rte);
                }
                else {
                    LOG.error(msg, rte);
                    throw rte;
                }
            }

            if (currentAttributes != null) {
                //Perform the appropriate attribute attrMerger
                resultAttributes = this.attrMerger.mergeAttributes(resultAttributes, currentAttributes);
            }
        }
        
        return resultAttributes;
    }
    
    /**
     * This implementation is not always correct.
     * It handles the basic case where the Set of attributes returned by this
     * implementation is the union of the attributes declared by all of the
     * underlying implementations to be merged.  Of course, an IAttributeMerger
     * might provide for a merging policy such that the attributes resulting from
     * invoking this IPersonAttributeDao implementation are not the union
     * of the attributes declared by the underlying PersonAttributeDaos.
     * 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        final Set attrNames = new HashSet();
        
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final IPersonAttributeDao currentDao = (IPersonAttributeDao)iter.next();
            
            Set currentDaoAttrNames = null;
            try {
                currentDaoAttrNames = currentDao.getPossibleUserAttributeNames();
            }
            catch (final RuntimeException rte) {
                final String msg = "Exception thrown by DAO: " + currentDao;

                if (this.recoverExceptions) {
                    LOG.warn(msg, rte);
                }
                else {
                    LOG.error(msg, rte);
                    throw rte;
                }
            }
            
            if (currentDaoAttrNames != null)
                attrNames.addAll(currentDaoAttrNames);
        }
        
        return Collections.unmodifiableSet(attrNames);
    }
    
    /**
     * Get the strategy whereby we accumulate attributes.
     * 
     * @return Returns the attrMerger.
     */
    public IAttributeMerger getMerger() {
        return this.attrMerger;
    }
    
    /**
     * Set the strategy whereby we accumulate attributes from the results of 
     * polling our delegates.
     * 
     * @param merger The attrMerger to set.
     * @throws IllegalArgumentException If merger is <code>null</code>.
     */
    public void setMerger(final IAttributeMerger merger) {
        if (merger == null)
            throw new IllegalArgumentException("The merger cannot be null");
            
        this.attrMerger = merger;
    }
    
    
    /**
     * Get the {@link List} of delegates which we will poll for attributes.
     * 
     * @return Returns the personAttributeDaos.
     */
    public List getPersonAttributeDaos() {
        return this.personAttributeDaos;
    }
    
    /**
     * Set the {@link List} of delegates which we will poll for attributes.
     * 
     * @param daos The personAttributeDaos to set.
     * @throws IllegalArgumentException If daos is <code>null</code>.
     */
    public void setPersonAttributeDaos(final List daos) {
        if (daos == null)
            throw new IllegalArgumentException("The dao list cannot be null");
            
        this.personAttributeDaos = Collections.unmodifiableList(daos);
    }
    
    /**
     * True if this class will catch exceptions thrown by its delegate DAOs
     * and fail to propogate them.  False if this class will stop on failure.
     * 
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
     * 
     * @param recover The recoverExceptions to set.
     */
    public void setRecoverExceptions(boolean recover) {
        this.recoverExceptions = recover;
    }
}