/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.channels.groupsmanager;

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */

import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.utils.*;

/**
 * CGroupsManagerServant is an IServant subclass of CGroupsManager
 * This will allow other channels to delegate to CGroupsManager at runtime
 *
 * Master channels should instantiate this channel with the following
 * staticData parameter preset:
 *
 * prmOwners = IPermissible[] owners
 *
 * where owners is an array with a single element being an instance of the
 * master's representative IPermissible class.
 *
 * see org.jasig.portal.IPermissible for more information
 *
 */

 public class CGroupsManagerServant extends CGroupsManager
      implements IServant {

   /**
    * put your documentation comment here
    */
   public CGroupsManagerServant () {
   }

   /**
    * True when the servant is ready to pass control back to the master channel.
    * @return
    */
   public boolean isFinished () {
      boolean isFinished = false;
      if (staticData.containsKey("groupManagerFinished") && staticData.getParameter("groupManagerFinished").equals("true")) {
         isFinished = true;
      }
      return  isFinished;
   }

   /**
    * Sets the staticData.
    * @param sd
    */
   public void setStaticData (ChannelStaticData sd) {
      super.setStaticData(sd);
      sd.setParameter("grpServantMode", "true");
   }

   /**
    * Returns an array of objects representing the result set.
    * Note that these are IGroupMembers !!
    * @return
    */
   public Object[] getResults () {
      Object[] results = null;
      if (staticData.containsKey("princResults")) {
         results = (Object[])staticData.get("princResults");
      }
      Utility.logMessage("DEBUG", "CGroupsManagerservant.getResults()");
      return  results;
   }
}



