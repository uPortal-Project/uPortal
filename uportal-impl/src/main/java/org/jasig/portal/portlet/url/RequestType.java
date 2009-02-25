/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

/**
 * Defines types of requests to Porlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public enum RequestType {
    /**
     * The request is to execute a portlet render request
     */
    RENDER,

    /**
     * The request is to execute a portlet action request
     */
    ACTION;
}
