/**
 * $Author$
 * $Date$
 * $Id$
 * $Name$
 * $Revision$
 *
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
 */


package org.jasig.portal;

import java.lang.ref.*;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import java.lang.Byte;
import com.objectspace.xml.*;

import org.jasig.portal.layout.*;




/**
 * Class to control the caching of the users XML document.  It is configurable to 
 * either allow the garbage collector to collect the XML document if it needs 
 * to (using SoftReferences) or not.  It also handles getting the XML document 
 * from the database as needed.
 *
 * The session.properties file controls this class, specifically the 
 * session.memory.layoutxml_dynamic_loading option.
 *
 * @author $Author$
 */

public class LayoutXmlCache {

    private SoftReference layoutXmlRef = null;      // this is used to maintain the soft reference to the layout
    private boolean useSoftReference = true;       // determines if we should allow the GC to collect the layout or not
    private IXml layoutXmlHolder = null;            // this is used to keep the layout from being collected
    private String sUserName = null;                 // this is the user we are caching a layout for
    private String sLayoutCacheDir = null;   // stores the location of the cache directory
    private static RdbmServices sDbServices = null;   // must initialize at least one of these
    

    /** Constructor used to create the cache.  You must indicate
     * the username so the cache can get the layout when it needs to do so.
     *
     * @author $Author$
     */
    public LayoutXmlCache(String uName) {

	if(sDbServices == null) {
	    // need to load a RdbmServices object so it initializes
	    // this is only done once though
	    sDbServices = new RdbmServices();
	}

        // ask the SessionManager how to handle our layouts
        if("no".equals(SessionManager.getConfiguration("session.memory.layoutxml_dynamic_loading"))) {
            useSoftReference = false;
            Logger.log(Logger.DEBUG, "LayoutXmlCache: NOT dynamically loading the users' XML documents.");
        } else {
            Logger.log(Logger.DEBUG, "LayoutXmlCache: using dynamic loading of user XML documents.");
            useSoftReference = true;
        }


        sLayoutCacheDir = SessionManager.getConfiguration("session.memory.layoutxml_cache_directory");

        if(sLayoutCacheDir == null) {
            Logger.log(Logger.DEBUG, "LayoutXmlCache:  session.memory.layoutxml_cache_directory not set.  DISK CACHING DISABLED");
        } else {
            // check that the directory is good
            File cacheDir = new File(sLayoutCacheDir);
            if(!cacheDir.exists()) {
                Logger.log(Logger.DEBUG, "LayoutXmlCache: layout cache directory " + cacheDir + " does not exist! DISK CACHING DISABLED.");
                sLayoutCacheDir = null;
            } else if(!cacheDir.isDirectory()) {
                Logger.log(Logger.DEBUG, "LayoutXmlCache: layout cache path " + cacheDir + " is NOT a directory! DISK CACHING DISABLED.");
                sLayoutCacheDir = null;
            } else {
                Logger.log(Logger.DEBUG, "LayoutXmlCache: caching user layouts in " + cacheDir);
            }
        }


        // default to "guest" if null is given
        if(uName == null)
            sUserName = "guest";
        else
            sUserName = uName;
    }




    /** Sets the layout for the user to the given one.  This basically just writes the new layout to
     * the database, and then updates our internal representation to match the layoutXml parameter. 
     *
     * @author $Author$
     */
    public void setLayoutXml(String sPathToLayoutDtd, String sLayoutDtd, IXml layoutXml) {
        Connection con = null;

        // update it in the database first
        try {
            con = sDbServices.getConnection ();
            Statement stmt = con.createStatement();

            StringWriter sw = new StringWriter ();
            layoutXml.saveDocument(sw);
            String sLayoutXml = sw.toString();

            // Remove path to layout dtd before saving
            int iRemoveFrom = sLayoutXml.indexOf (sPathToLayoutDtd);
            int iRemoveTo = sLayoutXml.indexOf (sLayoutDtd);
            sLayoutXml = sLayoutXml.substring (0, iRemoveFrom) + sLayoutXml.substring (iRemoveTo);

            try {
                String sUpdate = "UPDATE PORTAL_USERS SET LAYOUT_XML='" +
                                 sLayoutXml + "' WHERE USER_NAME='" + sUserName + "'";
                int iUpdated = stmt.executeUpdate (sUpdate);
                Logger.log (Logger.DEBUG, "Saving layout xml for " + sUserName +
                            ". Updated " + iUpdated + " rows.");
                stmt.close ();
            } catch (SQLException e) {
                // oracle fails if you try to process a string literal of more than 4k (sLayoutXml), so do this:
                PreparedStatement pstmt = con.prepareStatement("UPDATE PORTAL_USERS SET LAYOUT_XML=? WHERE USER_NAME=?");
                pstmt.clearParameters ();
                pstmt.setCharacterStream (1, new StringReader (sLayoutXml), sLayoutXml.length ());
                pstmt.setString (2, sUserName);
                int iUpdated = pstmt.executeUpdate ();
                Logger.log (Logger.DEBUG, "Saving layout xml for " + sUserName + ". Updated " + iUpdated + " rows.");
                pstmt.close ();

            }

            // update the disk cache copy also
            writeToDisk(sUserName, sLayoutXml);

        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        } finally {
            sDbServices.releaseConnection (con);
        }

        // now that it's in the DB and the disk cache, we can cache it in memory
        cacheLayoutXml(layoutXml);
    }




    /** Retrieves the user's layout, either from the database (if it hasn't been yet) or from memory.
     * If the cache is set to claimable, it is possible that the layout has been garbage collected, and
     * thus needs to be retrieved from the database again.
     *
     * @author $Author$
     */
    public IXml getLayoutXml(String sPathToLayoutDtd, String sLayoutDtd) {
        Connection con = null;

        IXml layoutXml = getCachedLayoutXml();  // attempt to get it from the cached object

        try {
            if (layoutXml != null)
                return layoutXml;


            String sLayoutXml = readFromDisk(sUserName);
            if(sLayoutXml != null) {
                // prep it for use
                // Tack on the full path to layout.dtd
                int iInsertBefore = sLayoutXml.indexOf (sLayoutDtd);
                sLayoutXml = sLayoutXml.substring (0, iInsertBefore) + sPathToLayoutDtd + sLayoutXml.substring (iInsertBefore);

                String xmlFilePackage = "org.jasig.portal.layout";
                layoutXml = Xml.openDocument(xmlFilePackage, new StringReader (sLayoutXml));
                // we had to reload the layoutXml from the database, so we need to re-cache it
                cacheLayoutXml(layoutXml);

                // return the completed layout object
                return layoutXml;
            }


            // looks like it isn't in memory or on disk, load it from the db and update all caches

            con = sDbServices.getConnection ();
            Statement stmt = con.createStatement();

            String sQuery = "SELECT LAYOUT_XML FROM PORTAL_USERS WHERE USER_NAME='" + sUserName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);

            if (rs.next ()) {
                sLayoutXml = rs.getString ("LAYOUT_XML");

                // If user has no layout xml, get it from the default user
                if (sLayoutXml == null || sLayoutXml.length () <= 0) {

                    sQuery = "SELECT LAYOUT_XML FROM PORTAL_USERS WHERE USER_NAME='default'";
                    Logger.log (Logger.DEBUG, sQuery);
                    rs = stmt.executeQuery (sQuery);

                    if (rs.next ()) {
                        sLayoutXml = rs.getString ("LAYOUT_XML");
                    }
                }

                // write out our fresh XML document to the disk cache for later
                writeToDisk(sUserName, sLayoutXml);

                // Tack on the full path to layout.dtd
                int iInsertBefore = sLayoutXml.indexOf (sLayoutDtd);
                sLayoutXml = sLayoutXml.substring (0, iInsertBefore) + sPathToLayoutDtd + sLayoutXml.substring (iInsertBefore);

                String xmlFilePackage = "org.jasig.portal.layout";
                layoutXml = Xml.openDocument(xmlFilePackage, new StringReader (sLayoutXml));
            }
            stmt.close ();

            // we had to reload the layoutXml from the database, so we need to re-cache it
            cacheLayoutXml(layoutXml);


            // notice that we still have a strong reference to it, so it won't get collected from the cache before we are through
            return layoutXml;
        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        } finally  {
            sDbServices.releaseConnection (con);
        }

        // we default to retuning null if we didn't get anything
        return null;
    }




    /**
     * Lets you control the caching during runtime.  You can set this to false
     * when you are doing something that requires frequent use of the 
     * layout, and then back to true when you don't care if it get reclaimed.
     *
     * @author $Author$
     */
    public void setClaimable(boolean claimable) {

	
        if("no".equals(SessionManager.getConfiguration("session.memory.layoutxml_dynamic_loading"))) {
	    // don't bother since the session.properties file is set to not do dynamic_loading
	    return;
        }

        if(claimable) {
            // break the layoutXmlHolder's hold on the IXml object so that the soft reference is all that is left
            layoutXmlHolder = null;
        }

        useSoftReference = claimable;
    }



    /**
     * Indicates the cache's current claimable state.
     *
     * @author $Author$
     */
    public boolean isClaimable() {
        return useSoftReference;
    }





    /** Checks the cache for the layout, and if it is there returns it.  If not,
     * returns null.  This is used to simplify the locking/unlocking needed when
     * using SoftReferences (or not).
     *
     * @author $Author$
     */
    private IXml getCachedLayoutXml()  {
        IXml layoutXml = null;

        // we need to do a little juggling to keep the SoftReference around
        // We wrap the attempt to get the layoutXmlRef referent in a try block so that
        // it is atomic in nature.   If we used an if statement, there is a race
        // condition between the test of the if and the time we actually obtain the referent.

        try {
            layoutXml = (IXml)layoutXmlRef.get();
        } catch (NullPointerException e) {
            return null;
        }


        if(useSoftReference) {
            // make sure that we are not holding our layout
            layoutXmlHolder = null;
        } else {
            // make sure that layoutXmlHolder has a grip on the layout
            layoutXmlHolder = layoutXml;
        }


        return layoutXml;
    }



    /**
     * Resets the internal references (either Soft or not depending on settings) to
     * a new IXml object.
     *
     * @author $Author$
     */
    private void cacheLayoutXml(IXml layoutXml) {
        // we prevent the soft reference from working by pointing the layoutXmlHolder at it
        if(useSoftReference) {
            layoutXmlHolder = null;
        } else {
            layoutXmlHolder = layoutXml;
        }

        // no matter what, the our soft reference must be set
        layoutXmlRef = new SoftReference(layoutXml);  // using a soft reference lets the GC recover it

    }



    /**
     * This writes the layout to the disk cache, but only if the disk cache is enabled.
     */
    private void writeToDisk(String sUserName, String sLayoutDocument) {

        if(sLayoutCacheDir == null)
            return;

        File cacheFile = new File(sLayoutCacheDir, sUserName);


        try {

            // open the buffer for write and no appending
            BufferedWriter cacheWriter = new BufferedWriter(new FileWriter(cacheFile.toString(), false));

            cacheWriter.write(sLayoutDocument);
            cacheWriter.close();

            Logger.log(Logger.DEBUG, "LayoutXmlCache: wrote layout document to " + cacheFile);

        } catch (IOException ioe) {
            Logger.log(Logger.ERROR, "LayoutXmlCache:  could not write layout cache file " + cacheFile + ".  Exception: " + ioe);
            return;
        }

    }


    /**
     * This method is used to reset the layout after a series of in-memory modifications.  This is needed
     * in the personalizeTabs.jsp, personalizeLayout.jsp, subscribe.jsp, and SubscriberBean since they use
     * the in memory copy of the Layout to do their modifications.
     *
     * @author Zed A. Shaw
     */
    public void reloadLayoutXml() {
	layoutXmlHolder = null;
	layoutXmlRef = null;
    }


    /**
     * This reads the layout from the disk cache, but only if the disk cache is enabled.
     *
     * It returns null if the layout for the user is not currently cached on disk.
     */
    private String readFromDisk(String sUserName) {

        if(sLayoutCacheDir == null)
            return null;

        File cacheFile = new File(sLayoutCacheDir, sUserName);

        // don't bother doing anything if the file doesn't exist yet
        if(!cacheFile.exists()) return null;

        try {

            // open the buffer for write and no appending
            BufferedReader cacheReader = new BufferedReader(new FileReader(cacheFile));

            StringBuffer lineBuffer = new StringBuffer();
            String line = null;

            // keep reading lines till we run out
            while ( (line = cacheReader.readLine()) != null)
                lineBuffer.append(line);

            cacheReader.close();

            Logger.log(Logger.DEBUG, "LayoutXmlCache: Read " + cacheFile + " from disk cache");
            return lineBuffer.toString();

        } catch (IOException ioe) {
            Logger.log(Logger.ERROR, "LayoutXmlCache:  could not read layout cache file " + cacheFile + ".  Exception: " + ioe);
            return null;
        }

    }

}




