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
package org.jasig.portal.portlets.statistics;

import java.beans.PropertyEditorSupport;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

/**
 * Property editor for <code>org.joda.time.DateMidnight</code>, supporting
 * a custom <code>org.joda.time.format.DateTimeFormatter</code>.  This class
 * is modeled on Spring's CustomDateEditor.
 * 
 * <p>
 * This is not meant to be used as system PropertyEditor but rather as
 * locale-specific date editor within custom controller code, parsing
 * user-entered number strings into Date properties of beans and rendering them
 * in the UI form.
 * 
 * <p>
 * In web MVC code, this editor will typically be registered with
 * <code>binder.registerCustomEditor</code> calls in a custom
 * <code>initBinder</code> method.
 * 
 * @author Juergen Hoeller
 * @Author Jen Bourey
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 */
public class CustomDateMidnightEditor extends PropertyEditorSupport {

    private final DateTimeFormatter dateFormat;

    private final boolean allowEmpty;

    private final int exactDateLength;

    /**
     * Create a new CustomDateEditor instance, using the given DateFormat for
     * parsing and rendering.
     * <p>
     * The "allowEmpty" parameter states if an empty String should be allowed
     * for parsing, i.e. get interpreted as null value. Otherwise, an
     * IllegalArgumentException gets thrown in that case.
     * 
     * @param dateFormat
     *            DateFormat to use for parsing and rendering
     * @param allowEmpty
     *            if empty strings should be allowed
     */
    public CustomDateMidnightEditor(DateTimeFormatter dateFormat,
            boolean allowEmpty) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = -1;
    }

    /**
     * Create a new CustomDateEditor instance, using the given DateFormat for
     * parsing and rendering.
     * <p>
     * The "allowEmpty" parameter states if an empty String should be allowed
     * for parsing, i.e. get interpreted as null value. Otherwise, an
     * IllegalArgumentException gets thrown in that case.
     * <p>
     * The "exactDateLength" parameter states that IllegalArgumentException gets
     * thrown if the String does not exactly match the length specified. This is
     * useful because SimpleDateFormat does not enforce strict parsing of the
     * year part, not even with <code>setLenient(false)</code>. Without an
     * "exactDateLength" specified, the "01/01/05" would get parsed to
     * "01/01/0005".
     * 
     * @param dateFormat
     *            DateFormat to use for parsing and rendering
     * @param allowEmpty
     *            if empty strings should be allowed
     * @param exactDateLength
     *            the exact expected length of the date String
     */
    public CustomDateMidnightEditor(DateTimeFormatter dateFormat,
            boolean allowEmpty, int exactDateLength) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = exactDateLength;
    }

    /**
     * Parse the Date from the given text, using the specified DateFormat.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else if (text != null && this.exactDateLength >= 0
                && text.length() != this.exactDateLength) {
            throw new IllegalArgumentException(
                    "Could not parse date: it is not exactly"
                            + this.exactDateLength + "characters long");
        } else {
            setValue(this.dateFormat.parseDateTime(text).toDateMidnight());
        }
    }

    /**
     * Format the Date as String, using the specified DateFormat.
     */
    @Override
    public String getAsText() {
        DateMidnight value = (DateMidnight) getValue();
        return (value != null ? this.dateFormat.print(value) : "");
    }

}
