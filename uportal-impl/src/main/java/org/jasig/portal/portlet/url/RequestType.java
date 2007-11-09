/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
