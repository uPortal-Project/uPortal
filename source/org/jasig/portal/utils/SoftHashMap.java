/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;



/**
 * A HashMap implementation that uses soft references, 
 * leaving memory management up to the gc.
 * @author Peter Kharchenko (thanks to Dr. Kabutz on whose article the code is based)
 * @version $Revision$
 */
public class SoftHashMap extends AbstractMap {

    private final HashMap map=new HashMap();
    private final LinkedList fifo=new LinkedList();
    private final ReferenceQueue removeQueue=new ReferenceQueue();

    private int minSize;
    private int maxSize;
    
    /**
     * Construct a SoftHashMap
     * @param minSize minimum number of objects to keep (approximate)
     */
    public SoftHashMap(int minSize) {
	this.minSize=minSize;
    }

    public SoftHashMap() {
	this(10);
    }

    public Object put(Object key,Object value) {
	cleanMap();
	KeyReferencePair pair=new KeyReferencePair(value,key,removeQueue);
	// place the object into fifo
	addToFIFO(value);
	return map.put(key,pair);
    }

    public Object get(Object key) {
	SoftReference soft_ref=(SoftReference) map.get(key);
	if(soft_ref!=null) {
	    Object obj=soft_ref.get();
	    if(obj==null) {
		// object has been consumed by gc
		map.remove(key);
	    } else {
		// place the object into fifo
		addToFIFO(obj);
	    }
	    return obj;
	}
	return null;
    }

    public Object remove(Object key) {
	cleanMap();
	SoftReference soft_ref=(SoftReference) map.remove(key);
	if(soft_ref!=null) {
	    return soft_ref.get();
	} else {
	    return null;
	}
    }

    public int size() {
	cleanMap();
	return map.size();
    }

    public void clear() {
	synchronized(fifo) {
	    fifo.clear();
	}
	map.clear();
    }

    public Set entrySet() {
	throw new UnsupportedOperationException();
    }

    /**
     * An extension of a SoftReference that contains a key
     * by which it was mapped.
     */
    private final static class KeyReferencePair extends SoftReference {
	private final Object key; 
	public KeyReferencePair (Object value, Object key, ReferenceQueue queue) {
	    super(value, queue);
	    this.key = key;
	}
    }


    private void addToFIFO(Object o) {
	synchronized(fifo) {
	    fifo.addFirst(o);
	    if(fifo.size()>minSize) {
		fifo.removeLast();
	    }	
	}
    }
    
    private void cleanMap() {
	KeyReferencePair pair;
	while((pair= (KeyReferencePair)removeQueue.poll())!=null) {
	    map.remove(pair.key);
	}
    }
}
