/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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