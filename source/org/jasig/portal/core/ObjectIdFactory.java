/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.core;


/**
 * The object Ids factory.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class ObjectIdFactory {
	
	protected static class ObjectIdImpl implements IObjectId {

	    protected String id;
	    
	    public ObjectIdImpl(String id) {
	        this.id = id;
	    }

	    public ObjectIdImpl(int id) {
	        this(Integer.toString(id));
	    }
	    
	    public ObjectIdImpl(long id) {
	        this(Long.toString(id));
	    }

	    public boolean equals(Object obj) {
	        return ( (obj instanceof IObjectId) && id.equals(obj.toString()) );
	    }
	    
	    public String toString() {
	        return id;
	    }
	    
	    public int toInt() {
	        return Integer.parseInt(id);
	    }
	    
	    public long toLong() {
	        return Long.parseLong(id);
	    }

	}
    
    public static IObjectId createId(String id) {
        return new ObjectIdImpl(id);
    }
    
    public static IObjectId createId(int id) {
        return new ObjectIdImpl(id);
    }
    
    public static IObjectId createId(long id) {
        return new ObjectIdImpl(id);
    }
}
