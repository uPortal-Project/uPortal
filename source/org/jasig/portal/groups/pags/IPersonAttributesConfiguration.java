/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags;

import java.util.Map;

/**
 * Interface for configuration providers for the Person Attributes Group Store.
 * Portal implementors may choose to override the default implementation of 
 * this type, <code>XMLPersonAttributesConfiguration</code>, in order to 
 * provide a list of group definitions for the PAGS to use.
 * 
 * @author Al wold
 * @version $Revision$
 */
public interface IPersonAttributesConfiguration {
   /**
    * Get the group definitions for the store.  Implementations
    * should initialize a Map of PersonAttributesGroupStore.GroupDefinition 
    * objects.
    * 
    * @return Map consisting of group definitions, keyed by group key
    */
   public Map getConfig();
}
