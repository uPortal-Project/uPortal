/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WelcomeFileListImpl implements Serializable {

    private List welcomeFiles = null; // Holds welcomeFile Strings
    
    public WelcomeFileListImpl() {
        welcomeFiles = new ArrayList();
    }
    
    public Iterator iterator() {
        return welcomeFiles.iterator();
    }

    public boolean remove(String welcomeFile) {
        return welcomeFiles.remove(welcomeFile);
    }
        
    public void add(String welcomeFile) {
        welcomeFiles.add(welcomeFile);
    }
    
    public int size() {
        return welcomeFiles.size();
    }

}
