/**
 *  Copyright (c) 2000 The JA-SIG Collaborative. All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer. 2. Redistributions in
 *  binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or other
 *  materials provided with the distribution. 3. Redistributions of any form
 *  whatsoever must retain the following acknowledgment: "This product includes
 *  software developed by the JA-SIG Collaborative (http://www.jasig.org/)."
 *  THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 *  EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 *  This is a base class for all the Portal beans to extend. The base class
 *  functionality contains all of the reusable code.
 *
 *@author     Ken Weiner
 *@created    June 7, 2001
 */
public class GenericPortalBean {
    /**
     *  Description of the Field
     */
    public boolean DEBUG = false;
    private static String sPortalBaseDir = null;


    /**
     *  Just a simple debug method that prints messages to System.out
     *
     *@param  message  Description of Parameter
     */
    public void debug(String message) {
        if (DEBUG) {
            System.out.println("DEBUG: " + message);
        }
    }


    /**
     *  Set the top level directory for the portal. This makes it possible to
     *  use relative paths in the application for loading properties files, etc.
     *
     *@param  sPathToPortal
     */
    public static void setPortalBaseDir(String sPathToPortal) {
        sPortalBaseDir = sPathToPortal;
    }


    /**
     *  Get the top level directory for the portal. This makes it possible to
     *  use relative paths in the application for loading properties files, etc.
     *
     *@return    The PortalBaseDir value
     */
    public static String getPortalBaseDir() {
        return sPortalBaseDir;
    }


    /**
     *  This method is used to initialize the portal base directory for the
     *  system. It should be called before anything else in all the entry points
     *  to the system (currently the jsp pages). It will try to get the
     *  parameter "sPortalBaseDir" from the ServletContext.getInitParameter()
     *  method. If that returns null, then it will try to deduce what the base
     *  dir is using the following algorithm: 1) Get the real path for the
     *  virtual path "/". Basically, find docroot. 2) Look for the "properties"
     *  directory. 3) If "properties" isn't there, then move up one directory
     *  and look again. 4) If it doesn't find it then complain. As long as you
     *  follow the standard way of installing the portal, you can probably not
     *  set this parameter. If you put the portal base directory in a different
     *  location from the properties directory, then you'll have to specify it
     *  with a init parameter. This was done so that the portal would run with
     *  minimal configuration, and so that it would run on app servers that
     *  don't support passing init params to JSP pages.
     *
     *@param  application  -- the application context from your JSP page.
     */
    public static void initialize(javax.servlet.ServletContext application) {
        if (sPortalBaseDir == null) {

            String sPathToPortal = application.getInitParameter("sPortalBaseDir");
            if (sPathToPortal == null) {
                // either the web server is stupid (like WebLogic) and they
                // can't set InitParameters for JSPs, or they just didn't do it
                sPathToPortal = application.getRealPath("/");
                // now we have to search for the "properties" directory
                File testFile = new File(sPathToPortal, "properties");
                if (!testFile.exists()) {
                    testFile = new File(testFile.getParentFile().getParentFile(), "properties");
                    if (testFile.exists()) {
                        // found it on the second try, remove the properties directory
                        sPathToPortal = testFile.getParent() + File.separator;
                        System.err.println("portal base directory is : " + sPathToPortal);
                    }
                }
                else {
                    // found it on the first try, remove the properties directory
                    sPathToPortal = testFile.getParent() + File.separator;
                }
            }

            System.err.println("sPathTOPortal is : " + sPathToPortal);
            setPortalBaseDir(sPathToPortal);
        }
    }
}


