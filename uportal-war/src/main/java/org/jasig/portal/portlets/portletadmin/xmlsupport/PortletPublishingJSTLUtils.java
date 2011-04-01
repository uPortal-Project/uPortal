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
