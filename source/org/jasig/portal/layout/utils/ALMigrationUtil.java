/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout.utils;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CommonUtils;

/**
 * The aggregated layout migration utility
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class ALMigrationUtil {

  private static final int ADD = 1;
  private static final int DELETE = 2;
  private static final int DEFAULT_SS_ID = 1;
  private static IUserLayoutStore ulsdb = null;
  private Properties props = new Properties();

  public ALMigrationUtil() throws Exception {
    if ( ulsdb == null )
     ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
     InputStream is = null; 
    try {
      is = this.getClass().getResourceAsStream( "/properties/al/al.properties");
      props.load(is);
     } finally {
     	if(is!=null)
     	 is.close();
     	else
     		LogService.log(LogService.ERROR, "ALMigrationUtil::line 73 Can not close InputStream");
     }
  }

  public void registerStylesheet ( String stylesheetURI, String stylesheetDescriptionURI, int stylesheetId, boolean isTheme, int command ) {
        try {
            switch ( command ) {
             case DELETE:
               if ( stylesheetId > 1 ) {
                 updateUserProfile ( CommonUtils.parseInt(props.getProperty("defaultSystemStylesheetId"),DEFAULT_SS_ID) );
                 if (isTheme)
                    ulsdb.removeThemeStylesheetDescription(stylesheetId);
                 else
                    ulsdb.removeStructureStylesheetDescription(stylesheetId);
               }
                break;
             case ADD:
                Integer id = null;
                if (isTheme)
                    id = ulsdb.addThemeStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);
                else
                    id = ulsdb.addStructureStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);

                if (id == null) {
                    System.out.println("Save failed: stylesheet ID = null");
                    return;
                } else {
                    stylesheetId = id.intValue();
                    System.out.println("Save successfull! The new " + ((isTheme)?"theme":"structure") + " stylesheet was assigned with Id="+id);
                  }
               updateUserProfile ( stylesheetId );
               break;
               default:
            }

        } catch (Exception e) {
            System.out.println("registerStylesheet: An error has been encountered:");
            e.printStackTrace();
        }

  }

  public void updateUserProfile ( int stylesheetId ) {
   try {
      System.out.println("Updating the system profile...");
      Hashtable profileList = ulsdb.getSystemProfileList();
      if ( profileList != null )
       for ( Enumeration profiles = profileList.elements(); profiles.hasMoreElements(); ) {
         UserProfile profile = (UserProfile) profiles.nextElement();
         profile.setStructureStylesheetId(stylesheetId);
         profile.setThemeStylesheetId(stylesheetId);
         ulsdb.updateSystemProfile(profile);
       }
      System.out.println("The profile is succesfully updated");
   } catch (Exception e) {
         System.out.println("updateUserProfile: An error has been encountered:");
         e.printStackTrace();
        }
  }

  public int getSystemStylesheetId() {
    try {
      Hashtable profileList = ulsdb.getSystemProfileList();
      if ( profileList != null )
       for ( Enumeration profiles = profileList.elements(); profiles.hasMoreElements(); ) {
         UserProfile profile = (UserProfile) profiles.nextElement();
         return profile.getStructureStylesheetId();
       }
    } catch (Exception e) {
         System.out.println("getSystemStylesheetId: An error has been encountered:");
         e.printStackTrace();
        }
      return -1;
  }

  public String getProperty ( String name ) {
     if ( props != null ) {
       String value = props.getProperty(name);
       if ( value != null )
        return value.trim();
     }
        return null;
  }

  public static void main(String[] args) {

    if ( args.length != 1 ) {
     System.out.println("Usage: ALMigrationUtil -Daction=[add|delete]");
     return;
    }

    /*String themeURI = "/org/jasig/portal/layout/AL_TabColumn/integratedModes/integratedModes.xsl";
    String themeDescriptionURI = "/org/jasig/portal/layout/AL_TabColumn/integratedModes/integratedModes.sdf";
    String structureURI = "/org/jasig/portal/layout/AL_TabColumn/AL_TabColumn.xsl";
    String structureDescriptionURI = "/org/jasig/portal/layout/AL_TabColumn/AL_TabColumn.sdf";*/

    int command = ("delete".equalsIgnoreCase(args[0]))?DELETE:ADD;
    try {
     ALMigrationUtil al = new ALMigrationUtil();
     String themeURI = al.getProperty("themeStylesheetURI");
     String themeDescriptionURI = al.getProperty("themeStylesheetDescURI");
     String structureURI = al.getProperty("structureStylesheetURI");
     String structureDescriptionURI = al.getProperty("structureStylesheetDescURI");
     int stylesheetId = al.getSystemStylesheetId();
     al.registerStylesheet( structureURI, structureDescriptionURI, stylesheetId, false, command );
     al.registerStylesheet( themeURI, themeDescriptionURI, stylesheetId, true, command );
    } catch ( Exception e ) {
        e.printStackTrace();
      }
  }
}
