/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This exception would inform uPortal that a particular
 * resource required to complete channel operation is
 * missing.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class ResourceMissingException extends PortalException {

    // URI of the missing resource
    private String str_resourceURI=null;
    private String str_description=null;

    public ResourceMissingException(String resourceURI, String resourceDescription, String msg) {
        super(msg);
        str_resourceURI=resourceURI;
        str_description=resourceDescription;
    }

    public ResourceMissingException(String resourceURI, String resourceDescription, String msg, boolean refresh, boolean reinstantiate) {
        super(msg,refresh,reinstantiate);
        str_resourceURI=resourceURI;
        str_description=resourceDescription;
    }

    public ResourceMissingException() {
        super();
    }

    public String getResourceURI() {
        return str_resourceURI;
    }

    public String getResourceDescription() {
        return str_description;
    }

}
