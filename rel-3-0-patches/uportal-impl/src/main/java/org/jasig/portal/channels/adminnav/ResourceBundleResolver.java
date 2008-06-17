/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
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
