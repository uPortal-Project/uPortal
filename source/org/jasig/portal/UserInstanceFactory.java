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


package  org.jasig.portal;

import  javax.servlet.http.HttpSession;
import  javax.servlet.http.HttpServletRequest;
import  org.jasig.portal.security.IPerson;
import java.util.Hashtable;

/**
 * A UserInstance factory. 
 * Factory differentiates between the normal and guest users.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */


public class UserInstanceFactory {
    private static final int[] guestIds={1};
    private static Hashtable glbs=new Hashtable();

    public static UserInstance getUserInstance(HttpServletRequest req) {
	HttpSession session=req.getSession(false);
	boolean guest=false;
	// determine user Id
	IPerson person = (IPerson)session.getAttribute("up_person");
	// this is ugly ! This shouldn't be the job of this class !
	if (person == null) {
	    person = new org.jasig.portal.security.provider.PersonImpl();
	    person.setID(UserInstance.guestUserId);
	    person.setFullName("Guest");
	    session.setAttribute("up_person",person);
	    guest=true;
	} else {
	    // determine if this is a guest
	    // once again ugly ! Need to have .isGuest() on the IPerons, or something like this
	    for(int i=0;i<guestIds.length;i++) {
		if(person.getID()==guestIds[i]) {
		    guest=true; break;
		}
	    }
	}
	return new UserInstance(person);

	/*
          if(guest) {
          GuestUserInstance glb=(GuestUserInstance) glbs.get(new Integer(person.getID()));
          if(glb==null) {
          glb=new GuestUserInstance(person);
          glbs.put(new Integer(person.getID()),glb);
          }
          glb.registerSession(req);
          return null;
          } else {
          return new UserInstance(person);
          }*/
    }
}
