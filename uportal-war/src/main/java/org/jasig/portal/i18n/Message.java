package org.jasig.portal.i18n;

import java.util.Locale;

/**
 * This interface represents localized messages used in database message source in order to enable
 * internationalization of dynamic messages like group names, tab titles, etc.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public interface Message {
    
    /**
     * Get the unique ID of this message.
     * 
     * @return unique ID.
     */
    public long getId();
    
    /**
     * Get the message code that identifies the messages with same semantic meaning across all
     * locales. This might be, for example, group name or tab title, that does not have direct
     * support for i18n.
     * 
     * @return message code.
     */
    public String getCode();
    
    /**
     * Get the locale specific message.
     * 
     * @return localized message.
     */
    public String getValue();
    
    /**
     * Set the locale specific message.
     * 
     * @param value localized message.
     */
    public void setValue(String value);
    
    /**
     * Get the locale of the message.
     * 
     * @return the locale of message.
     */
    public Locale getLocale();
}
