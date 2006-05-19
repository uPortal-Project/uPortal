/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.publisher;

import java.io.FileOutputStream;
import java.util.Enumeration;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import org.jasig.portal.PortalException;
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
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DlmLayoutExporter {

    // Static Members.
    private static final Log log = LogFactory.getLog(DlmLayoutExporter.class);
    private static final int PROFILE_ID = 1;
    private static final int LAYOUT_ID = 1;
    private static final int STYLESHEET_ID = 4;
    private static final int THEME_ID = 3;

    public static void main(String[] args) {

        // PrintWriter out = new PrintWriter(System.out);
        log.info("DLM Layout Exporter Tool");
        log.info("");

        switch (args.length) {
            case 2:

                try {

                    // Set up the layout store...
                    IUserLayoutStore store = new RDBMDistributedLayoutStore();

                    // The first argument should be a valid username...
                    String uName = (args[0]);
                    System.out.print("Exporting layout for user '" + uName + "'...");
                    int uId = DlmLayoutPublisher.lookupUserId(uName);
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
                    String rootId = layout.getRootId();
                    IUserLayoutNodeDescription root = layout.getNodeDescription(rootId);

                    // Set up the Document...
                    Document doc = DocumentFactory.getNewDocument();
                    Element layoutElement = doc.createElement("layout");
                    doc.appendChild(layoutElement);
                    layoutElement.setAttribute("user", uName);
                    Element rootFolderElement = doc.createElement("root-folder");
                    layoutElement.appendChild(rootFolderElement);

                    // Analyze the layout...
                    populateChildren(rootFolderElement, (IUserLayoutFolderDescription) root, dlm, ssup);

                    // Write to the indicated file...
                    // JAXP seralizing
                    TransformerFactory xformFactory 
                      = TransformerFactory.newInstance();
                    Transformer idTransform = xformFactory.newTransformer();
                    Source input = new DOMSource(doc);
                    Result output = new StreamResult(new FileOutputStream(args[1]));
                    idTransform.setOutputProperty(OutputKeys.INDENT,"yes");
                   	idTransform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                    idTransform.transform(input, output);       
                    
                } catch (Throwable t) {
                    log.error("The exporter tool terminated unexpectedly.", t);
                    System.out.println("The exporter tool terminated unexpectedly.");
                    t.printStackTrace(System.out);
                    System.exit(7);
                }

                break;

            default:

                log.info("Usage:  java org.jasig.portal.layout.dlm.publisher <username> <file_path>");
                log.info("");
                log.info("where <username> is a valid portal user and ");
                log.info("<file_path> is the name of a file NOT in existance.");
                break;

        }

        log.info("The publisher tool completed successfully.");
        System.exit(0);

    }

    private static void populateChildren(Element folderElement,
                IUserLayoutFolderDescription folder, IUserLayoutManager dlm,
                StructureStylesheetUserPreferences ssup) throws Exception {

        // Assertions.
        if (folderElement == null) {
            String msg = "Argument 'folderElement' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (folder == null) {
            String msg = "Argument 'folder' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (dlm == null) {
            String msg = "Argument 'dlm' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ssup == null) {
            String msg = "Argument 'ssup' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Child Nodes...
        for (Enumeration e = dlm.getChildIds(folder.getId()); e.hasMoreElements();) {

            String chldId = (String) e.nextElement();
            IUserLayoutNodeDescription chldNd = null;
            try {
                chldNd = dlm.getNode(chldId);
            } catch (PortalException pe) {
                // NB:  This really isn't a very satisfactory way to handle the
                // issue here, which is that DLM embeds "dlm:editSet" elements
                // in layouts to persist customization of layout fragments (I
                // think).  Anyhow uPortal (even via DLM) can't give you a node
                // description for one of these b/c it's neither a folder nor a
                // channel.  For now, we'll drop the editSet data on layout export.
                continue;
            }

        	Document doc = folderElement.getOwnerDocument();
            switch (chldNd.getType()) {

                case IUserLayoutNodeDescription.FOLDER:

                    Element childFolderElement = doc.createElement("folder");
                    folderElement.appendChild(childFolderElement);

                    // Type...
                    switch (((IUserLayoutFolderDescription) chldNd).getFolderType()) {
                        case UserLayoutFolderDescription.HEADER_TYPE:
                            childFolderElement.setAttribute("type", "header");
                            break;
                        case UserLayoutFolderDescription.FOOTER_TYPE:
                            childFolderElement.setAttribute("type", "footer");
                            break;
                        case UserLayoutFolderDescription.REGULAR_TYPE:
                            childFolderElement.setAttribute("type", "regular");
                            break;
                    }

                    // Removable...
                    if (chldNd.isUnremovable()) {
                        childFolderElement.setAttribute("removable", "False");
                    } else {
                        childFolderElement.setAttribute("removable", "True");
                    }

                    // Mutable...
                    if (chldNd.isEditAllowed()) {
                        childFolderElement.setAttribute("mutable", "True");
                    } else {
                        childFolderElement.setAttribute("mutable", "False");
                    }

                    // Width... where applicable...
                    String width = ssup.getDefinedFolderAttributeValue(chldNd.getId(), "width");
                    if (width != null && width.trim().length() != 0) {
                        childFolderElement.setAttribute("width", width);
                    }

                    // Name...
                    Element nameElement = doc.createElement("name");
                    childFolderElement.appendChild(nameElement);
                    
                    nameElement.appendChild(doc.createTextNode(chldNd.getName()));

                    populateChildren(childFolderElement, (IUserLayoutFolderDescription) chldNd, dlm, ssup);

                    break;

                case IUserLayoutNodeDescription.CHANNEL:

                    Element channelElement = doc.createElement("channel");
                    folderElement.appendChild(channelElement);
                    channelElement.setAttribute("fName", ((IUserLayoutChannelDescription) chldNd).getFunctionalName());

                    break;

            }

        }

    }

}
