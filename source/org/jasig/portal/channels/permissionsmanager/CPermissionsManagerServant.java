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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels.permissionsmanager;

import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.utils.*;


/**
 * CPermissionsManagerServant is an IServant subclass of CPermissionsManager
 * This will allow other channels to delegate to CPermissionsManager at runtime
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
 * @author Alex Vigdor
 * @version $Revision$
 */
public class CPermissionsManagerServant extends CPermissionsManager
        implements IServant {
    private IPermission[] results;

    /** Creates new CPermissionsManagerServant */
    public CPermissionsManagerServant () {
    }

    /**
     * put your documentation comment here
     * @param xslt
     */
    protected void transform (XSLT xslt) {
        if (!isFinished()) {
            try {
                xslt.setStylesheetParameter("isAdminUser", "true");
                xslt.transform();
            } catch (Exception e) {
                LogService.instance().log(LogService.DEBUG, e);
            }
        }
    }

    /**
     * put your documentation comment here
     * @return
     */
    public boolean isFinished () {
        boolean isFinished = false;
        if (staticData.containsKey("prmFinished") && staticData.getParameter("prmFinished").equals("true")) {
            isFinished = true;
        }
        return  isFinished;
    }

    /**
     * put your documentation comment here
     * Currently returns and empy array
     * @return
     */
    public Object[] getResults () {
        return  results;
    }

    public void setStaticData (org.jasig.portal.ChannelStaticData sD) {
        super.setStaticData(sD);
        isauthorized = true;
    }
}



