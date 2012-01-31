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

package org.jasig.portal.dao.usertype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jadira.usertype.spi.shared.AbstractSingleColumnUserType;

/**
 * Uses a regular expression to validate strings coming to/from the database.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FunctionalNameType extends AbstractSingleColumnUserType<String, String, FunctionalNameColumnMapper> {
    private static final long serialVersionUID = 1L;
    
    public static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[^\\w-]");
    public static final Pattern VALID_CHARS_PATTERN = Pattern.compile("[\\w-]");
    public static final Pattern VALID_FNAME_PATTERN = Pattern.compile("^[\\w-]+$");
    
    public static void validate(String fname) {
        if (!isValid(fname)) {
            throw new IllegalArgumentException("'" + fname + "' does not validate against FunctionalName pattern: " + VALID_FNAME_PATTERN.pattern());
        }
    }
    public static boolean isValid(String fname) {
        if (fname == null) {
            return false;
        }

        final Matcher matcher = VALID_FNAME_PATTERN.matcher(fname);
        return matcher.matches();
    }
    public static String makeValid(String fname) {
        if (fname == null) {
            return "_";
        }
        
        return INVALID_CHARS_PATTERN.matcher(fname).replaceAll("_");
    }
}
