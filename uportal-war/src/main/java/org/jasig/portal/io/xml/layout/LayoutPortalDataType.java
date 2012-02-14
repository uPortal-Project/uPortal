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

package org.jasig.portal.io.xml.layout;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.io.xml.AbstractPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.xml.StaxUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Describes a user's layout
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LayoutPortalDataType extends AbstractPortalDataType {
    public static final QName LEGACY_LAYOUT_QNAME = new QName("layout");
    
    public static final PortalDataKey IMPORT_32_DATA_KEY = new PortalDataKey(
            LEGACY_LAYOUT_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v3-2.crn",
            null);
    
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final PortalDataKey IMPORT_30_DATA_KEY = new PortalDataKey(
            LEGACY_LAYOUT_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v3-0.crn",
            null);
    
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final PortalDataKey IMPORT_26_DATA_KEY = new PortalDataKey(
            LEGACY_LAYOUT_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v2-6.crn",
            null);
    
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final QName LEGACY_LAYOUT_DEFAULT_USER_QNAME = new QName("default-user-layout");
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final PortalDataKey IMPORT_32_DEFAULT_USER_DATA_KEY = new PortalDataKey(
    		LEGACY_LAYOUT_DEFAULT_USER_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v3-2.crn",
            null);
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final PortalDataKey IMPORT_30_DEFAULT_USER_DATA_KEY = new PortalDataKey(
    		LEGACY_LAYOUT_DEFAULT_USER_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v3-0.crn",
            null);
    /**
     * @deprecated used for importing old data files
     */
    @Deprecated
    public static final PortalDataKey IMPORT_26_DEFAULT_USER_DATA_KEY = new PortalDataKey(
    		LEGACY_LAYOUT_DEFAULT_USER_QNAME, 
            "classpath://org/jasig/portal/io/import-layout_v2-6.crn",
            null);

    private static final List<PortalDataKey> PORTAL_DATA_KEYS = Arrays.asList(
    		IMPORT_26_DEFAULT_USER_DATA_KEY, IMPORT_30_DEFAULT_USER_DATA_KEY, IMPORT_32_DEFAULT_USER_DATA_KEY, 
    		IMPORT_26_DATA_KEY, IMPORT_30_DATA_KEY, IMPORT_32_DATA_KEY);
    
    private IUserIdentityStore userIdentityStore;
    
    public LayoutPortalDataType() {
        super(LEGACY_LAYOUT_QNAME);
    }
    
    @Autowired
	public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
		this.userIdentityStore = userIdentityStore;
	}

    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PORTAL_DATA_KEYS;
    }
    
    @Override
    public String getTitleCode() {
        return "Layout";
    }

    @Override
    public String getDescriptionCode() {
        return "User Layout";
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected PortalDataKey postProcessSinglePortalDataKey(String systemId, PortalDataKey portalDataKey, XMLEventReader reader) {

        /* Fragment layouts are differentiated _only_ by file extension;  these 
         * layouts must be imported BEFORE non-fragment layouts (i.e. layouts of 
         * regular users)
         */  
        if (systemId.endsWith(".fragment-layout.xml") || systemId.endsWith(".fragment-layout") /* legacy file extension */) {
            /* NOTE: In the future we could consider handling this case 
             * similarly to how "default" users are handled (below):  by reading 
             * the username attribute and checking it against the list of known 
             * fragment layout owners.
             */
            return convertToFragmentKey(portalDataKey);
        }
        
        //Check if the layout is for an older style that includes the profile data (only possible in 2.6 and 3.0 format layout files)
        if (IMPORT_26_DATA_KEY.equals(portalDataKey) || IMPORT_30_DATA_KEY.equals(portalDataKey)) {
	    	final StartElement startElement = StaxUtils.getRootElement(reader);
			final Attribute usernameAttr = startElement.getAttributeByName(new QName("username"));
			if (usernameAttr != null) {
				final String username = usernameAttr.getValue();
				if (this.userIdentityStore.isDefaultUser(username)) {
					return convertToDefaultUserKey(portalDataKey);
				}
			}
			else {
			    logger.warn("No username attribute on StartElement for {}", systemId);
			}
        }
        
        return portalDataKey;
    }

	private PortalDataKey convertToDefaultUserKey(PortalDataKey portalDataKey) {
		if (IMPORT_32_DATA_KEY.equals(portalDataKey)) {
		    return IMPORT_32_DEFAULT_USER_DATA_KEY;
		}
		else if (IMPORT_30_DATA_KEY.equals(portalDataKey)) {
		    return IMPORT_30_DEFAULT_USER_DATA_KEY;
		}
		else if (IMPORT_26_DATA_KEY.equals(portalDataKey)) {
		    return IMPORT_26_DEFAULT_USER_DATA_KEY;
		}
		
		throw new IllegalArgumentException("Portal Data Key " + portalDataKey + " has no default user equivalent");
	}

	private PortalDataKey convertToFragmentKey(PortalDataKey portalDataKey) {
		if (IMPORT_32_DATA_KEY.equals(portalDataKey)) {
		    return FragmentLayoutPortalDataType.IMPORT_32_DATA_KEY;
		}
		else if (IMPORT_30_DATA_KEY.equals(portalDataKey)) {
		    return FragmentLayoutPortalDataType.IMPORT_30_DATA_KEY;
		}
		else if (IMPORT_26_DATA_KEY.equals(portalDataKey)) {
		    return FragmentLayoutPortalDataType.IMPORT_26_DATA_KEY;
		}
		
		throw new IllegalArgumentException("Portal Data Key " + portalDataKey + " has no fragment layout equivalent");
	}
}
