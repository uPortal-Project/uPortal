/*
 * Created on Sep 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jasig.portal;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author gilbert
 *
 * 
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
