/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import junit.framework.*;


public class RestrictionsTest extends TestCase {
  

    protected void setUp() throws Exception {
       
    }

    public void testRestrictionTypes() throws Exception {
    	
    	System.out.println ( RestrictionTypes.DEPTH_RESTRICTION.hashCode() );
    	System.out.println ( RestrictionTypes.PRIORITY_RESTRICTION.hashCode() );
    	System.out.println ( RestrictionTypes.HIDDEN_RESTRICTION.hashCode() );
    	System.out.println ( RestrictionTypes.UNREMOVABLE_RESTRICTION.hashCode() );
    	System.out.println ( RestrictionTypes.IMMUTABLE_RESTRICTION.hashCode() );
    	System.out.println ( RestrictionTypes.GROUP_RESTRICTION.hashCode() );
    	
    }
    
}