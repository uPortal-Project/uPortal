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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring-managed bean that produces {@link ILocaleManager} instances when needed.
 *
 * @since 5.0
 */
@Component
public class LocaleManagerFactory {

    @Value("${org.apereo.portal.i18n.LocaleManager.locale_aware:true}")
    private boolean localeAware;

    @Value(
            "${org.apereo.portal.i18n.LocaleManager.portal_locales:en_US,fr_FR,es_ES,ja_JP,sv_SE,de_DE,mk_MK,lv_LV}")
    private String portalLocalesProperty;

    private List<Locale> portalLocales;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Populate <code>portalLocales</code> from a comma-delimited locale string list, e.g.
     * "en_US,ja_JP"
     */
    @PostConstruct
    @SuppressWarnings("StringSplitter")
    public void init() {
        logger.info("Using localeAware={}", localeAware);
        logger.info("Using portalLocalesProperty='{}'", portalLocalesProperty);
        if (StringUtils.isNotBlank(portalLocalesProperty)) {
            final List<Locale> list = new ArrayList<>();
            for (String token : portalLocalesProperty.split(",")) {
                list.add(parseLocale(token.trim()));
            }
            portalLocales = Collections.unmodifiableList(list);
            logger.info("Loaded the following portalLocales:  {}", portalLocales);
        }
    }

    public boolean isLocaleAware() {
        return localeAware;
    }

    public List<Locale> getPortalLocales() {
        return portalLocales;
    }

    public LocaleManager createLocaleManager(IPerson person, List<Locale> userLocales) {
        logger.debug("Creating LocalManager for user '{}'", person.getUserName());
        return new LocaleManager(userLocales, portalLocales);
    }

    /**
     * Helper method to produce a <code>java.util.Locale</code> object from a locale string such as
     * en_US or ja_JP.
     *
     * @param localeString a locale string such as en_US
     * @return a java.util.Locale object representing the locale string
     */
    public Locale parseLocale(String localeString) {
        String language = null;
        String country = null;
        String variant = null;

        // Sometimes people specify "en-US" instead of "en_US", so
        // we'll try to clean that up.
        localeString = localeString.replaceAll("-", "_");

        StringTokenizer st = new StringTokenizer(localeString, "_");

        if (st.hasMoreTokens()) {
            language = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            country = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            variant = st.nextToken();
        }

        Locale locale = null;

        if (variant != null) {
            locale = new Locale(language, country, variant);
        } else if (country != null) {
            locale = new Locale(language, country);
        } else if (language != null) {
            locale = new Locale(language);
        }

        return locale;
    }
}
