/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.adminnav;

import java.util.Locale;

/**
 * Provides a callback mechanism for the admin nav channel to aquire a
 * localized version of a lable for a registered link exposed in its interface.
 *
 * @author mboyd@sungardsct.com
 *
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface ILabelResolver
{
    /**
     * Returns the localized label for the given label identifier and locale
     * or null if one is not found.
     *
     * @param labelId
     * @param locale
     * @return String localized label
     */
    public String getLabel(String labelId, Locale locale);

    /**
     * Returns any information in String form that would assist in identifying
     * how the implementing resolver were configured. This method is typically
     * called when no label was available for a given labelId as part of
     * providing a label that provides troubleshooting semantics. Typically,
     * such a label will consist of the following concatenated information:
     * <pre>
     * "???" +
     * <resolver class name> +
     * "{" + <resolver external form> + "}" +
     * "[" + <labelId> + "]"
     * </pre>
     * The external form and wrapping braces are only included if this method
     * returns a non-null, non-empty String.
     *
     * @return String reslver configuration
     */
    public String getExternalForm();
}
