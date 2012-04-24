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

package org.jasig.portal.layout.dlm;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.security.provider.BrokenSecurityContext;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.xpath.XPathOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Factory object responsible for creating DLM reference objects like 
 * {@link Pathref}s and {@link Noderef}s.  One instance of NodeReferenceFactory 
 * should be present in the Spring context, whence it also derives its 
 * dependencies.
 * 
 * @author awills
 */
@Component
public final class NodeReferenceFactory {
    
    private static final Pattern DLM_PATH_REF_DELIM = Pattern.compile("\\:");
    private final static Pattern USER_NODE_PATTERN = Pattern.compile("\\A([a-zA-Z]\\d*)\\z");
    private final static Pattern DLM_NODE_PATTERN = Pattern.compile("u(\\d+)l\\d+([ns]\\d+)");

    private final Log log = LogFactory.getLog(getClass());
    
    @Autowired
    private IUserLayoutStore layoutStore;
    
    @Autowired
    private IUserIdentityStore userIdentityStore;
    
    @Autowired
    private XmlUtilities xmlUtilities;
    
    @Autowired
    private XPathOperations xPathOperations;
    
    /*
     * Public API.
     */
    
    /**
     * Returns a valid {@link Noderef} based on the specified arguments or 
     * <code>null</null> if that's not possible.<br/>
     * 
     * <strong>This method returns <code>null</code> if the pathref cannot be 
     * resolved to a node on the specified layout.</strong>  It is the 
     * responsibility of calling code to handle this case appropriately.
     * 
     * @return a valid {@link Noderef} or <code>null</null>
     */
    public Noderef getNoderefFromPathref(String layoutOwner, String pathref, String fname, boolean isStructRef, org.dom4j.Element layoutElement) {
        
        Validate.notNull(layoutOwner, "Argument 'layoutOwner' cannot be null.");
        Validate.notNull(pathref, "Argument 'pathref' cannot be null.");

        if (log.isTraceEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("getDlmNoderef: [layoutOwner='").append(layoutOwner)
                            .append("', pathref='").append(pathref)
                            .append("', fname='").append(fname)
                            .append("', isStructRef='").append(isStructRef)
                            .append("']");
            log.trace(msg.toString());
            log.trace("getDlmNoderef: user layout document follows...\n"+layoutElement.asXML());
        }
        
        final String[] pathTokens = DLM_PATH_REF_DELIM.split(pathref);
        if (pathTokens.length <= 1) {
            this.log.warn("Invalid DLM PathRef, no delimiter: " + pathref);
            return null;
        }
        
        if (pathTokens[0].equals(layoutOwner)) {
            // This an internal reference (our own layout);  we have to 
            // use the layoutExment (instead of load-limited-layout) b/c 
            // our layout may not be in the db...
            final org.dom4j.Element target = (org.dom4j.Element) layoutElement.selectSingleNode(pathTokens[1]);
            if (target != null) {
                return new Noderef(target.valueOf("@ID"));
            }

            this.log.warn("Unable to resolve pathref '" + pathref + "' for layoutOwner '" + layoutOwner + "'");
            return null;
        }
        
        /*
         * We know this Noderef refers to a node on a DLM fragment
         */

        final String layoutOwnerName = pathTokens[0];
        final String layoutPath = pathTokens[1];
        
        final Integer layoutOwnerUserId = this.userIdentityStore.getPortalUserId(layoutOwnerName);
        if (layoutOwnerUserId == null) {
            this.log.warn("Unable to resolve pathref '" + pathref + "' for layoutOwner '" + layoutOwner + "', no userId found for userName: " + layoutOwnerName);
            return null;
        }
        
        final Tuple<String, DistributedUserLayout> userLayoutInfo = getUserLayoutTuple(layoutOwnerName, layoutOwnerUserId);
        final Document userLayout = userLayoutInfo.second.getLayout();
        
        final Node targetNode = this.xPathOperations.evaluate(layoutPath, userLayout, XPathConstants.NODE);
        if (targetNode == null) {
            this.log.warn("No layout node found for pathref: " + pathref);
            return null;
        }
        
        final NamedNodeMap attributes = targetNode.getAttributes();
        if (fname != null) {
            final Node fnameAttr = attributes.getNamedItem("fname");
            if (fnameAttr == null) {
                this.log.warn("Layout node for pathref does not have fname attribute: " + pathref);
                return null;
            }
            
            final String nodeFname = fnameAttr.getTextContent();
            if (!fname.equals(nodeFname)) {
                this.log.warn("fname '" + nodeFname + "' on layout node not match specified fname '" + fname + "' for pathref: " + pathref);
                return null;
            }
        }
        
        final Node structIdAttr = attributes.getNamedItem("struct-id");
        if (structIdAttr != null) {
            final String structId = structIdAttr.getTextContent();
    
            if (isStructRef) {
                return new Noderef(layoutOwnerUserId, 
                        1 /* TODO:  remove hard-coded layoutId=1 */, 
                        "s" + structId);
            }
            
            return new Noderef(layoutOwnerUserId,  
                        1 /* TODO:  remove hard-coded layoutId=1 */, 
                        "n" + structId);
        }

        final Node idAttr = attributes.getNamedItem("ID");
        return new Noderef(layoutOwnerUserId,
                    1 /* TODO:  remove hard-coded layoutId=1 */, 
                    idAttr.getTextContent());

    }

    /**
     * Returns a valid {@link Pathref} based on the specified arguments or 
     * <code>null</null> if that's not possible.
     * 
     * @param layoutOwnerUsername
     * @param dlmNoderef
     * @param layout
     * @return a valid {@link Pathref} or <code>null</null>
     */
    public Pathref getPathrefFromNoderef(String layoutOwnerUsername, String dlmNoderef, org.dom4j.Element layout) {
        
        Validate.notNull(layoutOwnerUsername, "Argument 'layoutOwnerUsername' cannot be null.");
        Validate.notNull(dlmNoderef, "Argument 'dlmNoderef' cannot be null.");
        
        if (log.isTraceEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("createPathref: [layoutOwnerUsername='").append(layoutOwnerUsername)
                        .append("', dlmNoderef='").append(dlmNoderef).append("']");
            log.trace(msg.toString());
        }
        
        Pathref rslt = null;  // default;  signifies we can't match a node

        final Matcher dlmNodeMatcher = DLM_NODE_PATTERN.matcher(dlmNoderef);
        if (dlmNodeMatcher.matches()) {
            final int userId = Integer.valueOf(dlmNodeMatcher.group(1));
            final String nodeId = dlmNodeMatcher.group(2);

            final String userName = this.userIdentityStore.getPortalUserName(userId);
            final Tuple<String, DistributedUserLayout> userLayoutInfo = getUserLayoutTuple(userName, userId);
            
            if (userLayoutInfo.second == null) {
                this.log.warn("no layout for fragment user '" + userLayoutInfo.first 
                                    + "' Specified dlmNoderef " + dlmNoderef 
                                    + " cannot be resolved.");
                return null;
            }
                                
            final Document fragmentLayout = userLayoutInfo.second.getLayout();
            final Node targetElement = this.xPathOperations.evaluate("//*[@ID = $nodeId]", Collections.singletonMap("nodeId", nodeId), fragmentLayout, XPathConstants.NODE);

            String xpath = this.xmlUtilities.getUniqueXPath(targetElement);
            // Pathref objects that refer to portlets are expected to include 
            // the fname as the 3rd element; other pathref objects should leave 
            // that element blank.
            String fname = null;
            Node fnameAttr = targetElement.getAttributes().getNamedItem("fname");
            if (fnameAttr != null) {
                fname = fnameAttr.getTextContent();
            }
            
            rslt = new Pathref(userLayoutInfo.first, xpath, fname);
        }

        final Matcher userNodeMatcher = USER_NODE_PATTERN.matcher(dlmNoderef);
        if (userNodeMatcher.find()) {
            // We need a pathref based on the new style of layout b/c on 
            // import this users own layout will not be in the database 
            // when the path is computed back to an Id...
            final String structId = userNodeMatcher.group(1);
            final org.dom4j.Node target = layout.selectSingleNode("//*[@ID = '" + structId + "']");
            if (target == null) {
                this.log.warn("no match found on layout for user '" + layoutOwnerUsername + "' for the specified dlmNoderef:  " + dlmNoderef);
                return null;
            }
            
            String fname = null;
            if (target.getName().equals("channel")) {
                fname = target.valueOf("@fname");
            }
                
            rslt = new Pathref(layoutOwnerUsername, target.getUniquePath(), fname);
        }
        
        
        return rslt;

    }
    
    /*
     * Implementation.
     */

    /**
     * Provides a {@link Tuple} containing the &quot;fragmentized&quot; version 
     * of a DLM fragment owner's layout, together with the username.  This 
     * version of the layout consistent with what DLM uses internally for 
     * fragments, and is created by FragmentActivator.fragmentizeLayout.  It's 
     * important that the version returned by this method matches what DLM uses 
     * internally because it will be used to establish relationships between 
     * fragment layout nodes and user customizations of DLM fragments.
     * 
     * @param userId
     * @return
     */
    /* TODO:  make private */ Tuple<String, DistributedUserLayout> getUserLayoutTuple(String userName, int userId) {
        
        final PersonImpl person = new PersonImpl();
        person.setUserName(userName);
        person.setID(userId);
        person.setSecurityContext(new BrokenSecurityContext());

        final IUserProfile profile = layoutStore.getUserProfileByFname(person, UserProfile.DEFAULT_PROFILE_FNAME);
        final DistributedUserLayout userLayout = layoutStore.getUserLayout(person, (UserProfile) profile);

        return new Tuple<String, DistributedUserLayout>(userName, userLayout);
    }

}
