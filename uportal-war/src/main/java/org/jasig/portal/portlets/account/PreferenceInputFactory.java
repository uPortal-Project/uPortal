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
package org.jasig.portal.portlets.account;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.jasig.portal.portletpublishing.xml.MultiChoiceDisplay;
import org.jasig.portal.portletpublishing.xml.MultiChoicePreferenceInput;
import org.jasig.portal.portletpublishing.xml.MultiTextPreferenceInput;
import org.jasig.portal.portletpublishing.xml.Option;
import org.jasig.portal.portletpublishing.xml.Preference;
import org.jasig.portal.portletpublishing.xml.SingleChoiceDisplay;
import org.jasig.portal.portletpublishing.xml.SingleChoicePreferenceInput;
import org.jasig.portal.portletpublishing.xml.SingleTextPreferenceInput;
import org.jasig.portal.portletpublishing.xml.TextDisplay;

/**
 * PreferenceInputFactory provides factory methods for creating Preference
 * input objects.  This class may be used to ease XML configuration of forms. 
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class PreferenceInputFactory {

    /**
     * Define a single-valued text input preferences.  This method is a 
     * convenient wrapper for the most common expected use case and assumes
     * null values for the default value and a predictable label.
     * 
     * @param name
     * @param label
     * @return
     */
    public static Preference createSingleTextPreference(String name, String label) {
        return createSingleTextPreference(name, "attribute.displayName." + name, TextDisplay.TEXT, null);
    }
    
    /**
     * Craete a single-valued text input preference.
     * 
     * @param name
     * @param label
     * @param displayType
     * @param defaultValue
     * @return
     */
    public static Preference createSingleTextPreference(String name, String label, TextDisplay displayType, String defaultValue) {
        SingleTextPreferenceInput input = new SingleTextPreferenceInput();
        input.setDefault(defaultValue);
        input.setDisplay(displayType);
        
        Preference pref = new Preference();
        pref.setName(name);
        pref.setLabel(label);
        pref.setPreferenceInput(new JAXBElement<SingleTextPreferenceInput>(new QName("single-text-parameter-input"), SingleTextPreferenceInput.class, input));
     
        return pref;
    }
    
    /**
     * Create a single-valued choice preference input.
     * 
     * @param name
     * @param label
     * @param displayType
     * @param options
     * @param defaultValue
     * @return
     */
    public static Preference createSingleChoicePreference(String name, String label, SingleChoiceDisplay displayType, List<Option> options, String defaultValue) {
        SingleChoicePreferenceInput input = new SingleChoicePreferenceInput();
        input.setDefault(defaultValue);
        input.setDisplay(displayType);
        input.getOptions().addAll(options);
        
        Preference pref = new Preference();
        pref.setName(name);
        pref.setLabel(label);
        pref.setPreferenceInput(new JAXBElement<SingleChoicePreferenceInput>(new QName("single-choice-parameter-input"), SingleChoicePreferenceInput.class, input));
     
        return pref;
    }
    
    /**
     * Create a multi-valued text input preference.
     * 
     * @param name
     * @param label
     * @param displayType
     * @param defaultValues
     * @return
     */
    public static Preference createMultiTextPreference(String name, String label, TextDisplay displayType, List<String> defaultValues) {
        MultiTextPreferenceInput input = new MultiTextPreferenceInput();
        input.getDefaults().addAll(defaultValues);
        input.setDisplay(displayType);
        
        Preference pref = new Preference();
        pref.setName(name);
        pref.setLabel(label);
        pref.setPreferenceInput(new JAXBElement<MultiTextPreferenceInput>(new QName("multi-text-parameter-input"), MultiTextPreferenceInput.class, input));
     
        return pref;
    }

    /**
     * Create a multi-valued choice input preference.
     * 
     * @param name
     * @param label
     * @param displayType
     * @param options
     * @param defaultValues
     * @return
     */
    public static Preference createMultiChoicePreference(String name, String label, MultiChoiceDisplay displayType, List<Option> options, List<String> defaultValues) {
        MultiChoicePreferenceInput input = new MultiChoicePreferenceInput();
        input.getDefaults().addAll(defaultValues);
        input.setDisplay(displayType);
        input.getOptions().addAll(options);
        
        Preference pref = new Preference();
        pref.setName(name);
        pref.setLabel(label);
        pref.setPreferenceInput(new JAXBElement<MultiChoicePreferenceInput>(new QName("multi-choice-parameter-input"), MultiChoicePreferenceInput.class, input));
     
        return pref;
    }

}
