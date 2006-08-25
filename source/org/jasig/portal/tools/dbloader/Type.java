/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.dbloader;

/**
 * Utility class used during loading of the database.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class Type
{
      String genericType; // "generic" is a Java reserved word
      String local;

      public String getGeneric() { return genericType; }
      public String getLocal() { return local; }

      public void setGeneric(String genericType) { this.genericType = genericType; }
      public void setLocal(String local) { this.local = local; }
}
