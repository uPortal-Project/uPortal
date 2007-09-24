package org.jasig.portal.layout;

/**
 * Defines an interface that the layout management implementation being used
 * can opt to support to provide localized names of folder nodes without
 * requiring such names to be embedded within the layouts for users. By using
 * this approach, when the user's locale changes the layout need not be
 * reloaded but the next rendering can inject the names.
 * 
 * @author Mark Boyd
 * 
 */
public interface IFolderLocalNameResolver
{
    /**
     * Returns the local folder label for the user's current locale as
     * determined by use of the LocaleManager. If no local version of the label
     * is available for the current locale nor for the default locale then null
     * is returned.
     * 
     * @param nodeId
     * @return
     */
    public String getFolderLabel(String nodeId);
}
