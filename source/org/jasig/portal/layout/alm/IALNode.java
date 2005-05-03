/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.layout.node.ILayoutNode;



/**
 * IALNode interface.
 * <p>
 * Company: Instructional Media &amp; Magic
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public interface IALNode extends ILayoutNode {

   
     public String getFragmentId();
     
     public String getFragmentNodeId();
    

  }
