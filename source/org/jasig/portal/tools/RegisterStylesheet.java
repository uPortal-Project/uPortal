/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.tools;

import  org.jasig.portal.*;
import  java.io.File;


/**
 * Title:        uPortal 2.0
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class RegisterStylesheet {
  private static String portalBaseDir;

  /**
   * put your documentation comment here
   */
  private static void setPortalBaseDir () {
    String portalBaseDirParam = System.getProperty("portal.home");
    if (portalBaseDirParam != null) {
      portalBaseDir = portalBaseDirParam;
      PortalSessionManager.setPortalBaseDir(portalBaseDir);
      // Should obtain implementation in a different way!!
    }
    else {
      System.out.println("Please set the system parameter portal.home.  For example: java -Dportal.home=/usr/local/portal");
      java.lang.System.exit(1);
    }
  }

  /**
   * put your documentation comment here
   */
  public RegisterStylesheet () {
  }

  /**
   * put your documentation comment here
   * @param args
   */
  public static void main (String[] args) {
    setPortalBaseDir();
    String stylesheetURI = null;
    String stylesheetDescriptionURI = null;
    int stylesheetId = -1;
    boolean isTheme = true;
    boolean update = false;
    if (args.length < 3)
      printHelp();
    for (int i = 0; i < args.length; i++) {
      if (!args[i].startsWith("-")) {
        if (update) {
          if (i <= 1 || args.length < i + 3)
            printHelp();
          stylesheetURI = args[i];
          stylesheetDescriptionURI = args[++i];
          stylesheetId = Integer.parseInt(args[++i]);
        }
        else {
          if (i < 1 || args.length < i + 2)
            printHelp();
          stylesheetURI = args[i];
          stylesheetDescriptionURI = args[++i];
        }
      }
      else if (args[i].equals("-s")) {
        isTheme = false;
      }
      else if (args[i].equals("-t")) {
        isTheme = true;
      }
      else if (args[i].equals("-u")) {
        update = true;
      }
      else {
        printHelp();
        return;
      }
    }
    if (stylesheetURI == null || stylesheetDescriptionURI == null) {
      printHelp();
      return;
    }
    ICoreStylesheetDescriptionStore csdb = CoreStylesheetDescriptionStoreFactory.getCoreStylesheetDescriptionStoreImpl();
    if (update) {
      boolean success = false;
      if (isTheme) {
        success = csdb.updateThemeStylesheetDescription(stylesheetDescriptionURI, stylesheetURI, stylesheetId);
      }
      else {
        success = csdb.updateStructureStylesheetDescription(stylesheetDescriptionURI, stylesheetURI, stylesheetId);
      }
      if (success) {
        System.out.println("Update successful!");
      }
      else {
        System.out.println("Update failed!");
        return;
      }
    }
    else {
      Integer id = null;
      if (isTheme)
        id = csdb.addThemeStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);
      else
        id = csdb.addStructureStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);
      if (id == null) {
        System.out.println("Save failed!");
        return;
      }
      else {
        stylesheetId = id.intValue();
        System.out.println("Save successfull!");
      }
    }
    try {
      // verify
      if (isTheme) {
        ThemeStylesheetDescription tsd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getThemeStylesheetDescription(stylesheetId);
      }
      else {
        StructureStylesheetDescription ssd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getStructureStylesheetDescription(stylesheetId);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /**
   * put your documentation comment here
   */
  private static void printHelp () {
    System.out.println("Usage: registerStylesheet ([-s][-t]) [-u] stylesheetURI stylesheetDescriptionURI [stylesheetId]");
    System.exit(0);
  }
}



