/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * ProblemsTable.
 * @author Howard Gilbert
 * @version $Revision$
 */
public class ProblemsTable {

	public static TreeMap registeredIds = new TreeMap();
    
	public static LinkedList recentIds = new LinkedList();
    
	private static final int maxRecent = 10;

	/**
	 * Add ErrorID to TreeMaps
	 * 
	 * @param id ErrorID (ignored if duplicate)
	 */
	public static void register(ErrorID id) {
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
	public static void store(PortalException pe) {
		if (pe == null)
			return; // bad argument
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
