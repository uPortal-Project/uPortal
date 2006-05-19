/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.publisher;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DlmLayoutPublisher {

    // Static Members.
    private static final Log log = LogFactory.getLog(DlmLayoutPublisher.class);
    private static final int PROFILE_ID = 1;
    private static final int LAYOUT_ID = 1;
    private static final int STYLESHEET_ID = 4;
    private static final int THEME_ID = 3;

    public static void main(String[] args) {

        // PrintWriter out = new PrintWriter(System.out);
        log.info("DLM Layout Publisher Tool");
        log.info("");

        switch (args.length) {
            case 1:

                try {

                    // Ensure the argument is a valid path...
                    File f = new File(args[0]);
                    if (!f.exists()) {
                        log.error("The specified file does not exist:  " + f.toString());
                        throw new IllegalArgumentException();
                    }

                    // Make sure uPortal doesn't try to get a data source from the container...
                    RDBMServices.setGetDatasourceFromJndi(false);

                    // Set up the layout store...
                    IUserLayoutStore store = new RDBMDistributedLayoutStore();

                    // Choose between single or multiple files...
                    File[] layoutFiles = null;
                    if (f.isFile()) {
                        layoutFiles = new File[] { f };
                    } else if (f.isDirectory()) {
                        layoutFiles = f.listFiles();
                    }

                    for (int i=0; i < layoutFiles.length; i++) {

                        if (!layoutFiles[i].isFile()) {
                            continue;
                        }

                        // Read the document...
                        DocumentBuilderFactory builderFactory =
                        	  DocumentBuilderFactory.newInstance();
                        	DocumentBuilder builder =
                        	  builderFactory.newDocumentBuilder();
                        
                        Document doc = builder.parse(layoutFiles[i]);

                        // Obtain the specified user...
                        XPath xp = XPathFactory.newInstance().newXPath();
                        
                        Attr userAttr = (Attr)xp.evaluate("//layout/@user",doc,XPathConstants.NODE);
                        String uName = userAttr.getNodeValue();
                        System.out.print("Importing layout for user '" + uName + "'...");
                        int uId = lookupUserId(uName);
                        IPerson usr = PersonFactory.createGuestPerson();
                        usr.setAttribute(IPerson.USERNAME, uName);
                        usr.setID(uId);

                        // Construct the layout manager...
                        UserProfile prf = new UserProfile(PROFILE_ID, "User Profile for " + uName, "User Profile", LAYOUT_ID, STYLESHEET_ID, THEME_ID);
                        LocaleManager lm = new LocaleManager(usr);
                        prf.setLocaleManager(lm);
                        IUserLayoutManager dlm = ((TransientUserLayoutManagerWrapper) UserLayoutManagerFactory
                                                .getUserLayoutManager(usr, prf)).getOriginalLayoutManager();

                        // Retrieve the layout...
                        StructureStylesheetUserPreferences ssup = store.getStructureStylesheetUserPreferences(usr, PROFILE_ID, STYLESHEET_ID);
                        IUserLayout layout = dlm.getUserLayout();
                        IUserLayoutNodeDescription root = layout.getNodeDescription(layout.getRootId());

                        // First clear the existing layout (if any) down to the root folder...
                        for (Enumeration e = dlm.getChildIds(root.getId()); e.hasMoreElements();) {
                            String chldId = (String) e.nextElement();
                            IUserLayoutNodeDescription chldNd = dlm.getNode(chldId);
                            chldNd.setDeleteAllowed(true);  // just to be sure...
                            chldNd.setEditAllowed(true);
                            if (!dlm.updateNode(chldNd) || !dlm.deleteNode(chldId)) {
                                String msg = "Unable to update/delete a layout node:  " + chldNd.getName();
                                throw new RuntimeException(msg);
                            }
                        }

                        // Construct the new layout contents...
                        NodeList list = (NodeList)xp.evaluate("//layout/root-folder/folder",doc,XPathConstants.NODESET);
                        String idLast = null;
                        for (int j=list.getLength()-1; j >=0 ; j--) {
                            Element e = (Element) list.item(j);
                            IUserLayoutFolderDescription fld = createFolder(e, ssup, dlm, layout.getRootId(), idLast);
                            idLast = fld.getId();
                        }

                        // Save the layout...
                        dlm.saveUserLayout();
                        store.setStructureStylesheetUserPreferences(usr, PROFILE_ID, ssup);

                        System.out.println("done!");

                    }

                } catch (Throwable t) {
                    log.error("The publisher tool terminated unexpectedly.", t);
                    System.out.println("The publisher tool terminated unexpectedly.");
                    t.printStackTrace(System.out);
                    System.exit(7);
                }

                break;

            default:

                log.info("Usage:  java org.jasig.portal.layout.dlm.publisher <layout_definition_path>");
                log.info("");
                log.info("where <layout_definition_path> is the path (full or ");
                log.info("relative) to a valid layout definition XML file.");
                break;

        }

        log.info("The publisher tool completed successfully.");
        System.exit(0);

    }

    public static int lookupUserId(String uName) {

        // Assertions.
        if (uName == null) {
            String msg = "Argument 'uName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // NB:  It royally stinks that uPortal does not
        // provide an API call to obtaun this information.

        int rslt = -1;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            conn = RDBMServices.getConnection();

            final String sql = "SELECT user_id FROM up_user WHERE user_name = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uName);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                rslt = rs.getInt(1);
            } else {
                // this is a problem...
                String msg = "User name not found in the database:  " + uName;
                throw new RuntimeException(msg);
            }

        } catch (Throwable t) {
            String msg = "Error encountered looking up the specified user:  " + uName;
            throw new RuntimeException(msg, t);
        } finally {

            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException sqle) { /* not much we can do... */ }

            try {
                if (conn != null) RDBMServices.releaseConnection(conn);
            } catch (Throwable t) { /* not much we can do... */ }

        }

        return rslt;

    }

    private static IUserLayoutFolderDescription createFolder(Element e, StructureStylesheetUserPreferences ssup, IUserLayoutManager dlm, String parentId, String nextId) {

        XPath xp = XPathFactory.newInstance().newXPath();
        
        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getTagName().equals("folder")) {
            String msg = "Argument 'e [Element]' must be a <folder> element.";
            throw new IllegalArgumentException(msg);
        }
        if (ssup == null) {
            String msg = "Argument 'ssup' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (dlm == null) {
            String msg = "Argument 'dlm' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (parentId == null) {
            String msg = "Argument 'parentId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'nextId' may be null.

        UserLayoutFolderDescription rslt = new UserLayoutFolderDescription();

        try {

            // evaluate the folder type...
        	String typeName = e.getAttribute("type");
            int type = -1;
            if (typeName.equalsIgnoreCase("header")) {
                type = UserLayoutFolderDescription.HEADER_TYPE;
            } else if (typeName.equalsIgnoreCase("regular")) {
                type = UserLayoutFolderDescription.REGULAR_TYPE;
            } else if (typeName.equalsIgnoreCase("footer")) {
                type = UserLayoutFolderDescription.FOOTER_TYPE;
            }
            if (type != -1) {
                rslt.setFolderType(type);
            }

            // evaluate the other settings...
            rslt.setAddChildAllowed(true);
            rslt.setDeleteAllowed(e.getAttribute("removable").equalsIgnoreCase("true"));
            rslt.setEditAllowed(e.getAttribute("mutable").equalsIgnoreCase("true"));
            rslt.setImmutable(e.getAttribute("mutable").equalsIgnoreCase("false"));
            
            rslt.setName(xp.evaluate("name",e));
            rslt.setUnremovable(e.getAttribute("removable").equalsIgnoreCase("false"));

            // Add the node to the layout...
            rslt = (UserLayoutFolderDescription) dlm.addNode(rslt, parentId, nextId);

            // Add width if present...
            String width = e.getAttribute("width");
            if (!width.equals("")) {
                ssup.addFolder(rslt.getId());
                ssup.setFolderAttributeValue(rslt.getId(), "width", width);
            }

            // evaluate children...
            NodeList chld = (NodeList) xp.evaluate("folder",e,XPathConstants.NODESET);
            String idLast = null;
            for (int i= chld.getLength()-1; i >= 0; i--) {
                Element elmNow = (Element) chld.item(i);
                IUserLayoutFolderDescription fld = createFolder(elmNow, ssup, dlm, rslt.getId(), idLast);
                idLast = fld.getId();
            }

            // channels...
            chld = (NodeList)xp.evaluate("channel",e,XPathConstants.NODESET);
            idLast = null;
            for (int i= chld.getLength()-1; i >= 0; i--) {
                Element elmNow = (Element) chld.item(i);
                IUserLayoutChannelDescription chn = createChannel(elmNow, dlm, rslt.getId(), idLast);
                idLast = chn.getId();
            }

        } catch (Throwable t) {
            String msg = "Unable to create the specified folder:  " + e.getTextContent();
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    private static IUserLayoutChannelDescription createChannel(Element e, IUserLayoutManager dlm, String parentId, String nextId) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getNodeName().equals("channel")) {
            String msg = "Argument 'e [Element]' must be a <channel> element.";
            throw new IllegalArgumentException(msg);
        }
        if (dlm == null) {
            String msg = "Argument 'dlm' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (parentId == null) {
            String msg = "Argument 'parentId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'nextId' may be null.

        // First obtain the referenced ChannelDefinition...
        String fName = e.getAttribute("fName");
        ChannelDefinition def = null;
        try {

            IChannelRegistryStore store = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
            def = store.getChannelDefinition(fName);

            if (def == null) {
                String msg = "The specified channel definition does not exist in the system:  " + fName;
                throw new RuntimeException(msg);
            }

        } catch (Throwable t) {
            String msg = "Error encountered with the specified channel definition:  " + fName;
            throw new RuntimeException(msg, t);
        }

        // Create the ChannelDescription...
        UserLayoutChannelDescription rslt = new UserLayoutChannelDescription();
        try {

            // channel settings...
            rslt.setChannelPublishId(Integer.toString(def.getId()));
            rslt.setChannelTypeId(Integer.toString(def.getTypeId()));
            rslt.setClassName(def.getJavaClass());
            rslt.setDescription(def.getDescription());
            rslt.setEditable(def.isEditable());
            rslt.setFunctionalName(fName);
            rslt.setHasAbout(def.hasAbout());
            rslt.setHasHelp(def.hasHelp());
            rslt.setIsSecure(def.isSecure());
            rslt.setTimeout(def.getTimeout());
            rslt.setTitle(def.getTitle());

            // layout node settings...
            rslt.setAddChildAllowed(false);
            rslt.setDeleteAllowed(true);
            rslt.setEditAllowed(true);
            rslt.setImmutable(false);
            rslt.setMoveAllowed(true);
            rslt.setName(null);
            rslt.setUnremovable(false);

            // Add the node to the layout...
            rslt = (UserLayoutChannelDescription) dlm.addNode(rslt, parentId, nextId);

        } catch (Throwable t) {
            String msg = "Unable to create the specified channel:  " + fName;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

}
