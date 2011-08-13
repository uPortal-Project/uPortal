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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Static wrapper around {@link IXalanMessageHelper} to make calls from XSTLC easier
 */
@Service
public final class XalanMessageHelper {
    private static IXalanMessageHelper messageHelper;
    
    @Autowired
    public void setMessageHelper(IXalanMessageHelper messageHelper) {
        XalanMessageHelper.messageHelper = messageHelper;
    }

    public static String getMessage(String code, String language) {
        return messageHelper.getMessage(code, language);
    }
    
    public static String getMessage(String code, String language, String arg1) {
        return messageHelper.getMessage(code, language, arg1);
    }
    
    public static String getMessage(String code, String language, String arg1, String arg2) {
        return messageHelper.getMessage(code, language, arg1, arg2);
    }
    
    public static String getMessage(String code, String language, String arg1, String arg2, String arg3) {
        return messageHelper.getMessage(code, language, arg1, arg2, arg3);
    }
}
