/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
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
