/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */



package org.jasig.portal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * CommonUtils class contains base useful utilities
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$ 
 */
public class CommonUtils {

   /**
    ** <code>nvl</code> replaces "sourceString" with "replaceString" if sourceString equals to null
        ** @param String sourceString - string to replace
        ** @param String replaceString - replacement
        ** @return String - processed string
    */
   public static String nvl( String sourceStr, String replaceStr ) {
     return (sourceStr != null)?sourceStr:replaceStr;
   }

   /**
    ** <code>nvl</code> replaces "sourceString" with "replaceString" if sourceString equals to null
        ** @param String sourceString - string to replace
        ** @param String replaceString - replacement
        ** @param String prefix - prefix
        ** @return String - processed string
    */
   public static String nvl( String sourceStr, String replaceStr, String prefix ) {
     String nvlStr = nvl(sourceStr,replaceStr);
     if ( !nvlStr.trim().equals("") )
       return new String(prefix+nvlStr);
     else
       return nvlStr;
   }


   /**
    ** <code>nvl</code> replaces "sourceString" with "" if sourceString equals to null
        ** @param String sourceString - string to replace
        ** @return String - processed string
    */
   public static String nvl( String sourceStr ) {
     return nvl(sourceStr,"");
   }


    /**
     * replaces "replacedString" with "newString" in "text"
     * @param text text where to replace
     * @param replacedString string to replace
     * @param newString new string
     * @return new text
     */
    public static String replaceText(String text, String replacedString, String newString) {
            int lastIndex =- newString.length();
            int replacedStringLength = replacedString.length();
            int newStringLength = newString.length();
            while ((lastIndex = text.indexOf(replacedString,
                lastIndex + newStringLength)) != -1) {
                    text = text.substring(0, lastIndex) + newString +
                        text.substring(lastIndex + replacedStringLength);
            }
            return text;
    }

    public static void replaceSubstVariables(Hashtable original, Hashtable subst) {

      for (Enumeration original_keys = original.keys(); original_keys.hasMoreElements(); ) {
        String original_key = (String)original_keys.nextElement();
        String original_value = (String)original.get(original_key);

        for (Enumeration subst_keys = subst.keys(); subst_keys.hasMoreElements(); ) {
          String subst_key = (String)subst_keys.nextElement();
          String subst_value = (String)subst.get(subst_key);

          original_value = replaceText(original_value, subst_key, subst_value);
        }

        original.put(original_key, original_value);

      }

    }

    public static String stackTraceToString(Exception e) {
      StringWriter strwrt = new StringWriter();
      e.printStackTrace(new PrintWriter(strwrt));
      return strwrt.toString();
    }


 /**
  * This method gets an array of strings from given string splitted by commas.
  * @param str - a string value
  * @param delim - a delimeter
  * @return an array of strings
  **/
 public static String[] getSplitStringByCommas ( String str, String delim ) {
   if ( str == null ) return null;
   StringTokenizer st = new StringTokenizer(str,delim);
   String[] strArray = new String[st.countTokens()];
   for ( int i = 0; st.hasMoreTokens(); i++ ) {
    strArray[i] = st.nextToken().trim();
   }
   return strArray;
 }

 /**
  * This method gets a properties of strings from given string splitted by commas.
  * @param keys - a string keys for properties
  * @param values - a string value
  * @param delim - a delimeter
  * @return an array of strings
  **/
 public static Properties getSplitStringByCommas ( String keys, String values, String delim ) {
   if ( values == null || keys == null ) return null;
   StringTokenizer stValues = new StringTokenizer(values,delim);
   StringTokenizer stKeys = new StringTokenizer(keys,delim);
   Properties props = new Properties();
   while ( stValues.hasMoreTokens() && stKeys.hasMoreTokens() ) {
    props.put(stKeys.nextToken().trim(),stValues.nextToken().trim());
   }
   return props;
 }

 /**
  * This method gets a properties of int from given string splitted by commas.
  * @param keys - a string keys for properties
  * @param values - a string value
  * @param delim - a delimeter
  * @param default - a default value
  * @return an array of int
  **/
 public static int[] getSplitIntByCommas ( String str, String delim, int def ) {
  if (str==null) str = "";
  String [] strarr = getSplitStringByCommas(str, delim);
  int[] intarr = new int[strarr.length];
  for (int i=0; i < strarr.length; i++) {
    intarr[i] = parseInt(strarr[i],def);
  }
  return intarr;


 }




 public static Hashtable getFamilyProps(Properties props, String prefix, String delim, Properties defaultProps) {
    Hashtable hash = new Hashtable();
    Enumeration en = props.propertyNames();
    String propName = null;
    String propShort = null;
    String itemName = null;
    String itemPropName = null;
    String itemPropValue = null;
    StringTokenizer st = null;
    Properties itemProps = null;
    while (en.hasMoreElements()) {
      propName = (String) en.nextElement();
      if (propName.startsWith(prefix)) {
        propShort = propName.substring(prefix.length());
        st = new StringTokenizer(propShort,delim);
        itemName = null;
        itemPropName = null;
        itemPropValue = null;
        if ( st.hasMoreTokens() ) {
          itemName = st.nextToken().trim();
          if ( st.hasMoreTokens() ) {
            itemPropName = st.nextToken().trim();
          }
        }
        if ((itemName!=null)&&(itemPropName!=null)) {
          itemPropValue = props.getProperty(propName, "");
          itemProps = (Properties)hash.get(itemName);
          if (itemProps==null) { itemProps = new Properties(defaultProps); }
          itemProps.put(itemPropName, itemPropValue);
          hash.put(itemName, itemProps);
        }
      }
    }
    return hash;
 }



 public static String[] getFamilyPropertyArrayString(Hashtable hash, String[] keys, String propName, String[] def) {
    String[] st = new String[keys.length];
    for (int i = 0;i<st.length;i++) {
      st[i] = getFamilyPropertyString(hash, keys[i], propName, null);
      if ((def!=null) && (st[i]==null) && (def.length>i)) { st[i] = def[i];}
    }
    return st;
 }

 public static String getFamilyPropertyString(Hashtable hash, String key, String propName, String def) {
      return ((Properties)hash.get(key)).getProperty(propName,def);
 }


 public static String[] getFamilyPropertyArrayString(Hashtable hash, String[] keys, String propName, String def) {
    String[] st = new String[keys.length];
    for (int i = 0;i<st.length;i++) {
      st[i] = def;
    }
    return getFamilyPropertyArrayString(hash, keys,propName,st);
 }

 public static String[] getFamilyPropertyArrayString(Hashtable hash, String[] keys, String propName) {
    return getFamilyPropertyArrayString(hash, keys,propName, (String[])null);
 }


 public static boolean[] getFamilyPropertyArrayBoolean(Hashtable hash, String[] keys, String propName, boolean def) {
    boolean[] st = new boolean[keys.length];
    for (int i = 0;i<st.length;i++) {
      st[i] = parseBoolean(((Properties)hash.get(keys[i])).getProperty(propName), def);
    }
    return st;
 }

 public static boolean[] getFamilyPropertyArrayBoolean(Hashtable hash, String[] keys, String propName) {
  return getFamilyPropertyArrayBoolean(hash, keys, propName, false);
 }


 public static int[] getFamilyPropertyArrayInt(Hashtable hash, String[] keys, String propName) {
    int[] st = new int[keys.length];
    for (int i = 0;i<st.length;i++) {
      st[i] = parseInt(((Properties)hash.get(keys[i])).getProperty(propName));
    }
    return st;
 }


  // parse "yes" and "no"
  public static boolean parseBoolean(String str, boolean defaultValue) {
       boolean res = defaultValue;
       if ("yes".equalsIgnoreCase(str))
          res = true;
       if ("no".equalsIgnoreCase(str))
          res = false;
       return res;
  }


  /**
  * This method returns the String representation of the given boolean value.
  * @param bool - a value of boolean type
  * @return "true" if bool is true, "false" - otherwise
  **/
  public static String boolToStr ( boolean bool ) {
    return (bool)?"true":"false";
  }

  /**
  * This method returns the boolean value for the given String representation ("true"-"false").
  * @param bool a <code>String</code> value
  * @return true if bool is "true", false - otherwise
  **/
  public static boolean strToBool ( String bool ) {
    return ("true".equalsIgnoreCase(bool))?true:false;
  }

  /**
  * This method returns the boolean value for the given String representation ("yes"-"no").
  * @param str a <code>String</code> value
  * @return true if str is "yes", false - otherwise
  **/
  public static boolean parseBoolean(String str) {
    return parseBoolean(str,false);
  }


 // Parse integer string value
  public static int parseInt(String str, int defaultValue) {
    try {
       return Integer.parseInt(str);
    } catch ( Exception e ) {
       return defaultValue;
      }
  }

  public static int parseInt(String str) {
    return parseInt(str,-1);
  }


  public static boolean odd(int i) {
     return (i >> 1 << 1 != i);
  }

  /**
  * This method checks if an array of objects is empty or not.
  * @param objects[] - an array of objects
  * @return true if array is empty, false - otherwise
  **/
  public static boolean isArrayEmpty ( Object objects[] ) {
   if ( objects == null ) return true;
   for ( int i = 0; i < objects.length; i++ ) {
    if ( objects[i] != null )
     return false;
   }
   return true;
  }

  /**
  * This method checks if an array of strings is empty or not.
  * @param objects[] - an array of strings
  * @return true if array is empty, false - otherwise
  **/
  public static boolean isArrayEmpty ( String objects[] ) {
   if ( objects == null ) return true;
   for ( int i = 0; i < objects.length; i++ ) {
    if ( objects[i] != null && !"".equals(objects[i]) )
     return false;
   }
     return true;
  }

}
