/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout;


import org.w3c.dom.Document;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import java.util.Random;

/**
 * A mock of IUserLayoutStore interface that works with a single user layout
 * DOM document.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class SingleDocumentUserLayoutStoreMock extends UserLayoutStoreMock {
    private static final Random rnd=new Random();
    Document userLayout=null;

    public SingleDocumentUserLayoutStoreMock(Document doc) {
        this.userLayout=doc;
    }
    public Document getUserLayout(org.jasig.portal.security.IPerson person, UserProfile profile) throws Exception {
        return this.userLayout;
    }

    public void setUserLayout(org.jasig.portal.security.IPerson person, UserProfile profile, org.w3c.dom.Document layoutXML, boolean channelsAdded) throws Exception {
        this.userLayout=layoutXML;
    }

    public String generateNewChannelSubscribeId(IPerson person) throws Exception {
        return new String("rid"+Integer.toString(rnd.nextInt()));
    }
    public String generateNewFolderId(IPerson person) throws Exception {
        return new String("rid"+Integer.toString(rnd.nextInt()));
    }
}
