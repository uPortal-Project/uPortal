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

package org.jasig.portal.channels.adminnav;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.car.CarResources;

/**
 * Provides an implementation of ILabelResolver that resolves labels from
 * ResourceBundles. A classpath located, base resource bundle property file name
 * like "/properties/adminNav" is passed in to the constructor. Based on the
 * user's locale, files with that name plus locale identifiers plus
 * ".properties" will be looked for for resolving lable names. For example, if a
 * call to getLabel passed a locale object for "de_DE" then the resource bundle
 * searched for would be "/properties/adminNav_de_DE.properties" followed by
 * "/properties/adminNav_de.properties" followed by
 * "/properties/adminNav.properties".
 * 
 * @author mboyd@sungardsct.com
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ResourceBundleResolver implements ILabelResolver
{
    private static Log LOG = LogFactory.getLog(AdminNavChannel.class);
    private String bundleBase = null;

    public ResourceBundleResolver(String bundleBase)
    {
        this.bundleBase = bundleBase;
    }
    public String getLabel(String labelId, Locale locale)
    {
        if (locale == null || labelId == null)
            return null;

        ResourceBundle rb = null;
        rb = ResourceBundle.getBundle(bundleBase, locale, CarResources
                .getInstance().getClassLoader());
        return rb.getString(labelId);
    }
    /**
     * Return the bundle base used for this resolver.
     * 
     * @see org.jasig.portal.channels.adminnav.ILabelResolver#getExternalForm()
     */
    public String getExternalForm()
    {
        return "bundleBase=" + bundleBase;
    }
}
