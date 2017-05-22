/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.i18n.dao;

import java.util.Locale;
import java.util.Set;
import org.apereo.portal.i18n.Message;

/**
 * Interface for creating/retrieving/updating/deleting {@link Message}s.
 *
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

    /**
     * Get all message codes regardless of translation. This can be used in order to find out the
     * missing translations for specific locales.
     *
     * @return A set of all message codes.
     * @since 4.0.2
     */
    Set<String> getCodes();
}
