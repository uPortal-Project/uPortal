/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.layout.node.IUserLayoutChannelDescription;

/**
 * An Aggregated Layout channel description.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0 $Revision$ $Date$
 */
public interface IALChannelDescription extends IUserLayoutChannelDescription, IALNodeDescription {}
