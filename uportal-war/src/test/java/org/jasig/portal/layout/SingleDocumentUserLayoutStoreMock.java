/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
