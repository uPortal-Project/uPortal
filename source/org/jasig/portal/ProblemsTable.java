/*
 * Copyright © 2003-2004 The JA-SIG Collaborative.  All rights reserved.
 * See notice at end of file.
 */

package org.jasig.portal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * A cache of recently reported PortalExceptions.
 * @author Howard Gilbert
 * @author andrew.petro@yale.edu
 * @version $Revision$
 */
public class ProblemsTable {

    /** TreeMap from ErrorID Categories to TreeMaps.
     *   The enclosed TreeMaps map from Specifics (ErrorID subcategories)
     *  to CountID objects.  The CountID objects cache the PortalExceptions
     *  that were in the Specific.
     */
	public static TreeMap registeredIds = new TreeMap();
    
    /**
     * List of recently modified CountID instances.
     */
	public static LinkedList recentIds = new LinkedList();
    
    /**
     * List of recently reported PortalExceptions, regardless of category.
     */
    private static LinkedList recentPortalExceptions = new LinkedList();
    
    /**
     * The name of the PropertiesManager property the value of which should be the 
     * number of recent PortalExceptions you would like stored for each specific subcategory of ErrorID.
     */
    public static final String MAX_RECENT_ERRORS_PER_SPECIFIC_PROPERTY = "org.jasig.portal.ProblemsTable.maxRecentErrorsPerSpecific";
    
    /**
     * The default number of recent PortalExceptions that will be stored for each specific subcategory of ErrorID
     * in the case where the relevant property is not set.
     */
	private static final int DEFAULT_MAX_RECENT_PER_SPECIFIC = 10;
    
    /**
     * The number of recent PortalExceptions that will be stored for each specific subcategory of ErrorID.
     */
    private static final int maxRecent = PropertiesManager.getPropertyAsInt(MAX_RECENT_ERRORS_PER_SPECIFIC_PROPERTY, DEFAULT_MAX_RECENT_PER_SPECIFIC);
    
    /**
     * The name of the propertiesManager property the value of which should be the number of recent
     * PortalExceptions you would like stored in the overall FIFO cache, regardless of ErrorID.
     */
    public static final String OVERALL_RECENT_ERRORS_PROPERTY = "org.jasig.portal.ProblemsTable.recentErrorsOverall";
    
    /**
     * The default number of recent PortalExceptions that will be stored in the overall FIFO queue
     * regardless of ErrorID, which will be used in the case where the relevant property is not set.
     */
    private static final int DEFAULT_OVERALL_RECENT_ERRORS_COUNT = 40;
    
    /**
     * The number of recent PortalExceptions that will be stored in the overall FIFO queue
     * regardless of ErrorID.
     */
    private static final int overallErrorsCount = PropertiesManager.getPropertyAsInt(OVERALL_RECENT_ERRORS_PROPERTY, DEFAULT_OVERALL_RECENT_ERRORS_COUNT);

	/**
	 * Add ErrorID to TreeMaps
	 * 
	 * @param id ErrorID (ignored if duplicate)
	 */
	public synchronized static void register(ErrorID id) {
		if (id == null)
			return;
		String category = id.getCategory();
		String specific = id.getSpecific();
		TreeMap minor = (TreeMap) registeredIds.get(category);
		if (minor == null) {
			minor = new TreeMap();
			registeredIds.put(category, minor);
		}
		if (!minor.containsKey(specific)) {
			minor.put(specific, new CountID(id));
		}
	}

	/**
	 * Store a PortalException in the tables.
	 * 
	 * @param pe PortalException to be tabulated
	 */
	public synchronized static void store(PortalException pe) {
		if (pe == null)
			return; // bad argument
        if (recentPortalExceptions.contains(pe))
            return; // already recorded
		ErrorID id = pe.getErrorID();
		if (id == null)
			return; // no ErrorID (Msg only PortalException)
		String category = id.getCategory();
		String specific = id.getSpecific();
		TreeMap minor = (TreeMap) registeredIds.get(category);
		if (minor == null)
			return; // ErrorID not registered
		CountID countid = (CountID) minor.get(specific);
		if (countid == null)
			return; // ErrorID not registered

		countid.count++;
		countid.lastPortalException = pe;

		recentIds.remove(countid);         
		recentIds.addFirst(countid);
		if (recentIds.size()>maxRecent)
			recentIds.removeLast();
        
        // store this PortalException in the overall FIFO queue of recent PortalExceptions.
        ProblemsTable.recentPortalExceptions.addFirst(pe);
        if (ProblemsTable.recentPortalExceptions.size() > ProblemsTable.overallErrorsCount)
            ProblemsTable.recentPortalExceptions.removeLast();
	}
    
    /**
     * Get an unmodifiable shallow copy of the list of recent PortalExceptions.
     * @return an unmodifiable shallow copy of the list of recent PortalExceptions.
     */
    public synchronized static List getRecentPortalExceptions(){
        return Collections.unmodifiableList((List) ProblemsTable.recentPortalExceptions.clone());
    }

}

/**
 * ErrorID tabulation class
 * 
 * The TreeMaps yield an instance of this class
 */
class CountID {
	ErrorID errorID = null;
	int count = 0;
	PortalException lastPortalException = null;

	CountID(ErrorID id) {
		errorID = id;
	}
}

/*
 * Copyright © 2003-2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */