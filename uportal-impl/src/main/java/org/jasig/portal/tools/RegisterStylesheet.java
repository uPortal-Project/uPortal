/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.tools;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;


/**
 * A utility to manage core uPortal stylesheets.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class RegisterStylesheet {

    public RegisterStylesheet() {
    }

    public static void main(String[] args) {
        String stylesheetURI = null;
        String stylesheetDescriptionURI = null;
        int stylesheetId = -1;
        boolean isTheme = true;
        boolean update = false;
        boolean delete = false;
        if (args.length < 3)
            printHelp();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                if (update) {
                    if (i <= 1 || args.length < i + 3) {
                        printHelp();
                    }

                    stylesheetURI = args[i];
                    stylesheetDescriptionURI = args[++i];
                    stylesheetId = Integer.parseInt(args[++i]);
                } else if(delete) {
                    stylesheetId = Integer.parseInt(args[i]);
                } else {
                    if (i < 1 || args.length < i + 2) {
                        printHelp();
                    }

                    stylesheetURI = args[i];
                    stylesheetDescriptionURI = args[++i];
                }
            } else if (args[i].equals("-s")) {
                isTheme = false;
            } else if (args[i].equals("-t")) {
                isTheme = true;
            } else if (args[i].equals("-u")) {
                update = true;
            } else if (args[i].equals("-d")) {
                delete = true;
            } else {
                printHelp();
                return;
            }
        }
        if(delete) {
            if(stylesheetId==-1) {
                printHelp();
                return;
            }
        } else  if (stylesheetURI == null || stylesheetDescriptionURI == null) {
            printHelp();
            return;
        }
        IUserLayoutStore ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        try {
            if (update) {
                if (isTheme) {
                    ulsdb.updateThemeStylesheetDescription(stylesheetDescriptionURI, stylesheetURI, stylesheetId);
                } else {
                    ulsdb.updateStructureStylesheetDescription(stylesheetDescriptionURI, stylesheetURI, stylesheetId);
                } 
                // verify
                if (isTheme) {
                    ThemeStylesheetDescription tsd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getThemeStylesheetDescription(stylesheetId);
                } else {
                    StructureStylesheetDescription ssd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getStructureStylesheetDescription(stylesheetId);
                }
            } else if (delete) {
                if (isTheme) {
                    ulsdb.removeThemeStylesheetDescription(stylesheetId);
                } else {
                    ulsdb.removeStructureStylesheetDescription(stylesheetId);
                } 
            } else {
                Integer id = null;
                if (isTheme) {
                    id = ulsdb.addThemeStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);
                } else {
                    id = ulsdb.addStructureStylesheetDescription(stylesheetDescriptionURI, stylesheetURI);
                }

                if (id == null) {
                    System.out.println("Save failed!");
                    return;
                } else {
                    stylesheetId = id.intValue();
                    System.out.println("Save successfull! The new stylehseet was assigned Id="+id);
                }

                // verify
                if (isTheme) {
                    ThemeStylesheetDescription tsd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getThemeStylesheetDescription(stylesheetId);
                } else {
                    StructureStylesheetDescription ssd = UserLayoutStoreFactory.getUserLayoutStoreImpl().getStructureStylesheetDescription(stylesheetId);
                }
            }

        } catch (Exception e) {
            System.out.println("An error has been encountered:");
            e.printStackTrace();
        }
    }


    private static void printHelp() {
        System.out.println("Usage: RegisterStylesheet -(s|t) [-(u|d)] [stylesheetURI] [stylesheetDescriptionURI] [stylesheetId]");
        System.out.println("The stylesheetId must be specified for update and delete operations.");
        System.out.println("The URIs must be specified for add and update operations.");
        System.out.println("The CLASSPATH environment variable should be set up to include\n"+
                           "both \"uPortal/build\" and \"uPortal/build/WEB-INF/classes\" dirs.\n\n"+
                           "For deployment all stylesheets are moved under the \"/stylesheet/\" directory,\n"+
                           "so the URI for a stylesheet will always begin with \"/stylesheet/\", unless\n"+
                           "you're specifying a global URL (which is not recommended).\n"+
                           "Stylesheet description files (.sdf) are moved to the same location, so\n"+
                           "their URI should be specified in the same manner.\n\n"+
                           "For example to specify a URI for a tab-column.xsl (part of the distribution),\n"+
                           "use \"/stylesheets/org/jasig/portal/layout/tab-column/tab-column.xsl\".\n");
        System.out.println("Flag specification:");
        System.out.println("\t-s : process structure stylesheet (either \"-t\" or \"-s\" are required on the command line)");
        System.out.println("\t-t : process theme stylesheet (either \"-t\" or \"-s\" are required on the command line)");
        System.out.println("\t-u : update stylesheet definition");
        System.out.println("\t-d : delete stylesheet definition");
        System.exit(0);
    }
}



