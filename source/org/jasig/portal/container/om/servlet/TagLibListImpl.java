/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class TagLibListImpl implements Serializable {

    private Map tagLibs = null; // TagLib Uri --> TagLibImpl
    
    public TagLibListImpl() {
        tagLibs = new HashMap();
    }
    
    public Iterator iterator() {
        return tagLibs.values().iterator();
    }

    public TagLibImpl get(String tagLibUri) {
        return (TagLibImpl)tagLibs.get(tagLibUri);
    }

    public TagLibImpl remove(String name) {
        return (TagLibImpl)tagLibs.remove(name);
    }

    public void remove(TagLibImpl tagLib) {
        tagLibs.remove(tagLib.getTaglibUri());
    }
        
    public void add(TagLibImpl tagLib) {
        tagLibs.put(tagLib.getTaglibUri(), tagLib);
    }
    
    public int size() {
        return tagLibs.size();
    }

}
