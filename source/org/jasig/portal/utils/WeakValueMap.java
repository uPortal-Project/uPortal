/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */


package org.jasig.portal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WeakValueMap is a hashmap that maintains its values via weak references (without preventing them from being garbage collected)
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class WeakValueMap extends HashMap {
    /**
     * Logger for this class
     */
    private static final Log logger = LogFactory.getLog(WeakValueMap.class);

    protected ReferenceQueue queue=new ReferenceQueue();


    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        cleanRefQueue();
        for (Iterator iter = super.values().iterator(); iter.hasNext();) {
            Object trueValue = ((WeakReference) iter.next()).get();
            if(value==trueValue) {
                return true;
            }
        }
        return false;
    }
    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        Map temp=new HashMap();
        for (Iterator iter = super.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object value=((WeakReference) entry.getValue()).get();
            if(value!=null) {
                temp.put(entry.getKey(),value);
            }
        }
        return temp.entrySet();
    }
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        cleanRefQueue();
        WeakReference ref=(WeakReference) super.get(key);
        if(ref!=null) {
            return ref.get();
        }
        return null;
    }
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        cleanRefQueue();
        return super.put(key,new KeyValueReference(key,value,queue));
    }
    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map m) {
        for (Iterator iter = m.keySet().iterator(); iter.hasNext();) {
            Object key = (Object) iter.next();
            put(key,m.get(key));
        }
    }
    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        cleanRefQueue();
        List values=new ArrayList(super.size());
        for (Iterator iter = super.values().iterator(); iter.hasNext();) {
            Object value = ((WeakReference) iter.next()).get();
            if(value!=null) {
                values.add(value);
            }
        }
        return values;
    }
    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
       cleanRefQueue();
       return super.isEmpty();
    }
    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        cleanRefQueue();
        return super.keySet();
    }
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        cleanRefQueue();
        return super.remove(key);
    }
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        cleanRefQueue();
        return super.size();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        WeakValueMap wvm=new WeakValueMap();
        for (Iterator iter = entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            wvm.put(entry.getKey(),entry.getValue());
        }
        return wvm;
    }
    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        cleanRefQueue();
        return super.containsKey(key);
    }
    
    protected void cleanRefQueue() {
        KeyValueReference r;
        while ((r = (KeyValueReference) queue.poll()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cleanRefQueue() - removing phantom reference : KeyValueReference.key = " + r.getKey());
            }
            super.remove(r.getKey());

        }
    }
    
    protected class KeyValueReference extends WeakReference {
        Object key;
        public KeyValueReference(Object key, Object value, ReferenceQueue queue) {
            super(value,queue);
            this.key=key;
        }
        public Object getKey() { return this.key; }
        public Object getValue() { return super.get(); }
    }
}
