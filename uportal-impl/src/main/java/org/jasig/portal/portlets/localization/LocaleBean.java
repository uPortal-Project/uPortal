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

package org.jasig.portal.portlets.localization;

import java.io.Serializable;
import java.util.Locale;

/**
 * LocaleBean wraps a java.util.Locale instance and provides simple getters for
 * the display properties.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class LocaleBean implements Serializable {
	
	private static final long serialVersionUID = 5551484745658125033L;

	private final String code;
	private final String displayName;
	private final String displayCountry;
	private final String displayLanguage;
	private final String displayVariant;
	private final Locale locale;
	
	/**
	 * Construct a new locale wrapper bean, setting all display values to the 
	 * currently specified locale.
	 * 
	 * @param locale
	 * @param currentLocale
	 */
	public LocaleBean(final Locale locale, final Locale currentLocale) {
		this.code = locale.toString();
		this.displayVariant = locale.getDisplayVariant(currentLocale);
		this.displayCountry = locale.getDisplayCountry(currentLocale);
		this.displayName = locale.getDisplayName(currentLocale);
		this.displayLanguage = locale.getDisplayLanguage(currentLocale);
		this.locale = locale;
	}

	/**
	 * Construct a new locale wrapper bean, using the default display values.
	 * 
	 * @param locale
	 */
	public LocaleBean(final Locale locale) {
		this.code = locale.toString();
		this.displayVariant = locale.getDisplayVariant();
		this.displayCountry = locale.getDisplayCountry();
		this.displayName = locale.getDisplayName();
		this.displayLanguage = locale.getDisplayLanguage();
		this.locale = locale;
	}
	
	/**
	 * Get the string representation of this locale.
	 * 
	 * @return
	 */
	public String getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getDisplayCountry() {
		return this.displayCountry;
	}

	public String getDisplayLanguage() {
		return this.displayLanguage;
	}

	public String getDisplayVariant() {
		return this.displayVariant;
	}

	public Locale getLocale() {
		return this.locale;
	}

}
