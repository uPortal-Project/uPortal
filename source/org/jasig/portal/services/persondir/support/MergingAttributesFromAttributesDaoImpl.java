/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jasig.portal.services.persondir.support.merger.AttributeMerger;
import org.jasig.portal.services.persondir.support.merger.ReplacingAttributeAdder;


/**
 * A standard implementation of the PersonAttributeDao which delegates to
 * an ordered List of subservient PersonAttributeDaos.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MergingAttributesFromAttributesDaoImpl implements AttributesFromAttributesDao {

    /**
     * A List of AttributeFromAttributeDaos which we will consider in order to accumulate
     * our attributes.
     */
    private List personAttributeDaos;
    
    /**
     * The strategy we will apply to merge each considered DAO's resulting map
     * into our accumulated and returned attribute map.
     */
    private AttributeMerger merger = new ReplacingAttributeAdder();

    /**
     * True if we should catch, log, and ignore Throwables propogated by
     * individual DAOs.
     */
    private boolean recoverExceptions;
    
    /**
     * Set the List of AttributeFromAttributeDaos which this implementation will
     * consult, in order.
     * @param daos
     */
    public void setAttributeFromAttributeDaos(List daos){
        this.personAttributeDaos = daos;
    }
    
    /**
     * Get the List of AttributeFromAttributeDaos which this implementation considers,
     * in order.
     * @return
     */
    public List getPersonAttributeDaos(){
        return this.personAttributeDaos;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.PersonAttributeDao#attributesForUser(java.util.Map)
     */
    public Map attributesFromAttributes(Map attributes) {
        
        /*
         * This implementation considers each of the personAttributeDaos.
         * We locally cache and eventually return a Map called 
         * "accumulatedAttributes".
         * 
         * For each DAO in our List, we pass a copy of 
         * accumulatedAttributes to its attributesGivenAttributes() method.  We then
         * pass accumulatedAttributes and the Map returned by the DAO we just
         * called into the mergeAttributes() method of our AttributeMerger.  We
         * assign the accumulatedAttributes reference to point to the Map
         * returned by mergeAttributes.
         * 
         * Finally, we return acccumulatedAttributes.
         */
        Map accumulatedAttributes = new HashMap();
        for (Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();){
            // consider each DAO in turn
            AttributesFromAttributesDao aDao = (AttributesFromAttributesDao) iter.next();
            
            /*
             * Create a copy of our accumulated attributes, which we will pass
             * to the AttributesFromAttributesDao we are currently considering.
             * Note that this doesn't protect us from the DAO changing mutable
             * objects referenced as values of attributes in this Map.
             */
            Map attributesSeed = new HashMap();
            attributesSeed.putAll(accumulatedAttributes);
            
            // obtain attributes from the currently considered DAO
            Map toConsider = aDao.attributesFromAttributes(attributesSeed);
            
            // apply the merge strategy to the accumulated and currently being
            // considered attributes.
            accumulatedAttributes = 
                this.merger.mergeAttributes(accumulatedAttributes, toConsider);
        }
        
        return accumulatedAttributes;
    }
    
    
    
    /**
     * @return Returns the merger.
     */
    public AttributeMerger getMerger() {
        return this.merger;
    }
    /**
     * @param merger The merger to set.
     */
    public void setMerger(AttributeMerger merger) {
        this.merger = merger;
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