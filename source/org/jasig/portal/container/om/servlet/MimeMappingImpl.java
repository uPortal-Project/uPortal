/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

/**
 * Data structure for each <servlet-mapping> element
 * in the portlet web.xml file. Used in ServletDefinitionImpl.
 * Pluto's ServletMappingImpl had an id field, which didn't 
 * appear to be used anywhere.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class MimeMappingImpl implements Serializable {

    private String extension;
    private String mimeType;
    
    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
