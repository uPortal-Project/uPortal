/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalUrlInfo {
    /**
     * @return <code>true</code> if the tag value has validated as expected.
     */
    public boolean isValidRequest();
    
    /**
     * Returns a tag identifier.
     *
     * @return a <code>String</code> tag value,  <code>null</code> if no tag was specified.
     */
    public String getTagId();

    /**
     * Determine method name
     *
     * @return a <code>String</code> method name,  <code>null</code> if no method was specified.
     */
    public String getMethod();

    /**
     * Determine Id specified by the method element.
     *
     * @return a <code>String</code> method node Id value, <code>null</code> if no method was specified.
     */
    public String getMethodNodeId();

    /**
     * Determine Id specified by the "target" element.
     *
     * @return a <code>String</code> target Id value, <code>null</code> if no target was specified.
     */
    public String getTargetNodeId();

    /**
     * Returns a "cleaned-up" version of the uP file with all known
     * fields such as tag, method, and target, removed. This can be used by...
     *
     * @return a <code>String</code> value, <code>null</code> if none were encountered.
     */
    public String getUPFileExtras();
}
