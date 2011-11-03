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
package org.jasig.portal.security.xslt;

import java.util.Locale;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.dlm.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

/**
 * This class contains convenience methods for resolving localized layout structure element titles.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
@Service
public class XalanLayoutElementTitleHelper {
    
    private static MessageSource messageSource;
    
    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        XalanLayoutElementTitleHelper.messageSource = messageSource;
    }
    
    /**
     * This method checks whether id indicates that it is a layout owner's structure element (or at
     * least derived from it). If it is the case, then it asks {@link MessageSource} to resolve the
     * message using layout element's name. Otherwise it returns the name. Note that layout owner's
     * element identifier format is 'uXlYnZ' where X stands for layout owners id, Y stands for
     * layout id, and Z stands for element (node) identifier, hence identifiers that start with 'u'
     * are considered as derived from layout owner.
     * 
     * @param id - layout structure element's identifier.
     * @param language - locale identifier.
     * @param name - default layout strucute element's name.
     * @return localized title in case of layout owner's element or default name otherwise.
     */
    public static String getTitle(String id, String language, String name) {
        if (id != null && id.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)) {
            final Locale locale = LocaleManager.parseLocale(language);
            return messageSource.getMessage(name, new Object[] {}, name, locale);
        }
        return name;
    }
}
