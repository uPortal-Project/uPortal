/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRequestInfo {
    public IPortletWindowId getTargetWindowId();
    public Map<String, List<String>> getPortletParameters();
    public Map<String, List<String>> getPublicPortletParameters();
    public WindowState getWindowState();
    public PortletMode getPortletMode();
    public IPortletRequestInfo getDelegatePortletRequestInfo();
}
