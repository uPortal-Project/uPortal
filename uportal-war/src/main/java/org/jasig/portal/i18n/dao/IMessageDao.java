package org.jasig.portal.i18n.dao;

import java.util.Locale;
import java.util.Set;

import org.jasig.portal.i18n.Message;

/**
 * Interface for creating/retrieving/updating/deleting {@link Message}s.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public interface IMessageDao {
    
    /**
     * Get a unique {@link Message} for specified code and locale.
     * 
     * @param code The code of the message to be retrieved.
     * @param locale The locale of the message to be retrieved.
     * @return The message for code and locale or null if such message could not be found.
     */
    Message getMessage(String code, Locale locale);
    
    /**
     * Creates, initializes and persists a new {@link Message} based on the spcified parameters.
     * 
     * @param code The message code that identifies the message.
     * @param locale The message localization.
     * @param value Localized message string for the locale.
     * @return A newly created, initialized and persisted {@link Message}.
     */
    Message createMessage(String code, Locale locale, String value);
    
    /**
     * Removes the {@link Message} from presistent store.
     * 
     * @param message The {@link Message} to remove.
     */
    void deleteMessage(Message message);
    
    /**
     * Perists changes of {@link Message}.
     * 
     * @param message The message to store the changes for.
     * @return Persisted message.
     */
    Message updateMessage(Message message);
    
    /**
     * Get all messages for specified locale;
     * 
     * @param locale The locale to retrieve messages for.
     * @return A list of all messages that are available for specific locale.
     */
    Set<Message> getMessagesByLocale(Locale locale);
    
    /**
     * Get all messages matching the specified code.
     * 
     * @param code The code to retrieve messages for.
     * @return A list of all translated messages for specific code.
     */
    Set<Message> getMessagesByCode(String code);
}
