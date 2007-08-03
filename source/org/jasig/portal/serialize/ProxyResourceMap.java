/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.serialize;

import java.util.HashMap;
import java.util.Map;

/**
 * This class extends <code>HashMap</code> and allows the generation
 * of sequential resource ids.  These ids are to be mapped to proxied resources.
 * This class is not synchronized and is designed to be stored and accessed
 * in the http session.
 */
public class ProxyResourceMap<K,V> extends HashMap<K,V> {
    
    private static final long serialVersionUID = 1L;
    
    private int nextResourceId = 0;

    public ProxyResourceMap() {
        super();
    }

    public ProxyResourceMap(int arg0, float arg1) {
        super(arg0, arg1);
    }

    public ProxyResourceMap(int arg0) {
        super(arg0);
    }

    public ProxyResourceMap(Map arg0) {
        super(arg0);
    }
    
    public int getNextResourceId() {
        return nextResourceId++;
    }

    @Override
    public void clear() {
        super.clear();
        nextResourceId = 0;
    }

}
