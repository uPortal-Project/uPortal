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
package org.apereo.portal.i18n;

import java.util.Locale;

/**
 * This interface represents localized messages used in database message source in order to enable
 * internationalization of dynamic messages like group names, tab titles, etc.
 *
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
