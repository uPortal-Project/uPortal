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

package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jasig.portal.portletpublishing.xml.InputType;

public class PortletPublishingJSTLUtils {

    public static String getDisplayType(InputType input) {
        try {
            Method displayMethod = input.getClass().getMethod("getDisplay", new Class[]{});
            Enum displayEnum = (Enum) displayMethod.invoke(input, new Object[]{});
            Method valueMethod = displayEnum.getClass().getMethod("value", new Class[]{});
            String type = (String) valueMethod.invoke(displayEnum, new Object[]{});
            return type;
        } catch (Exception e) {
            System.out.println(e);
        }
        
//        if (type instanceof SingleChoiceParameterInput) {
//            
//        } else if (type instanceof SingleTextParameterInput) {
//            
//        } else if (type instanceof SingleChoicePreferenceInput) {
//            
//        } else if (type instanceof SingleTextPreferenceInput) {
//        
//        } else if (type instanceof Single)
        return null;
    }
    
}
