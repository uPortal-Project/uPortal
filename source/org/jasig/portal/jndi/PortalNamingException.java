/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.jndi;

import javax.naming.NamingException;

/**
 * PortalNamingException
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class PortalNamingException extends NamingException
{
  public PortalNamingException(String explanation)
  {
    super(explanation);
  }
}