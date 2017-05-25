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
package org.apereo.portal.portlet.om;

import javax.servlet.http.Cookie;
import org.joda.time.DateTime;

/**
 * Internal interface for a JSR-286 Portlet cookie.
 *
 * <p>Mimics the Java EE {@link Cookie} class, with the addition of a few methods to help with
 * integration.
 *
 */
public interface IPortletCookie {

    /**
     * @see Cookie#getName()
     * @return the cookie name
     */
    String getName();

    /**
     * @see Cookie#getValue()
     * @return the cookie value
     */
    String getValue();

    /**
     * @see Cookie#getDomain()
     * @return the cookie domain
     */
    String getDomain();

    /**
     * @see Cookie#getPath()
     * @return the cookie path
     */
    String getPath();

    /**
     * @see Cookie#getComment()
     * @return the cookie comment
     */
    String getComment();
    /**
     * @see Cookie#getMaxAge()
     * @return the cookie max age (in minutes)
     */
    DateTime getExpires();

    /**
     * @see Cookie#getSecure()
     * @return the cookie secure flag
     */
    boolean isSecure();

    /**
     * @see Cookie#getVersion()
     * @return the cookie version
     */
    int getVersion();

    /**
     * Set the cookie value.
     *
     * @see Cookie#setValue(String)
     * @param value
     */
    void setValue(String value);

    /**
     * Set the cookie domain.
     *
     * @see Cookie#setDomain(String)
     * @param domain
     */
    void setDomain(String domain);

    /**
     * Set the cookie path.
     *
     * @see Cookie#setPath(String)
     * @param path
     */
    void setPath(String path);

    /**
     * Set the cookie comment.
     *
     * @see Cookie#setComment(String)
     * @param comment
     */
    void setComment(String comment);
    /**
     * Set the cookie max age (in minutes)
     *
     * @see Cookie#setMaxAge(int)
     * @param domain
     */
    void setExpires(DateTime expires);

    /**
     * Set the cookie secure flag.
     *
     * @see Cookie#setSecure(boolean)
     * @param secure
     */
    void setSecure(boolean secure);

    /**
     * Set the cookie version.
     *
     * @see Cookie#setVersion(int)
     * @param version
     */
    void setVersion(int version);

    /** Update all of the fields from the specified Cookie */
    void updateFromCookie(Cookie cookie);

    /** @return a new {@link Cookie} created from the values of the fields in this instance */
    Cookie toCookie();
}
