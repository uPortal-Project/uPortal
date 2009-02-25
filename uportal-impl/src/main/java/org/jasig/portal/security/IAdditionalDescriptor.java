/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security;

import java.io.Serializable;

/**
 * <p>A marker interface that should be extended by security providers that
 * have some incidental additional information that should be associated with
 * a security context.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

public interface IAdditionalDescriptor extends Serializable {
}