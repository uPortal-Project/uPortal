package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.channels.rss.*;
import com.objectspace.xml.*;

import java.net.*;


/**
 * This is for defining parameter fields for a given channel.
 * It allows each channel type to return its required parameters
 * for display in a HTML publish form.
 * 
 * @author John Laker
 * @version $Revision$
 */

public class ParameterField {

  private String sLabel = null;
  private String sName = null;
  private String sLength = null;
  private String sMaxLength = null;
  private String sDesc = null;

  //default constructor
  public ParameterField() {
  }

 /* *
  * Sets fields for a given parameter to be displayed in an HTML form.
  *
  */
  public ParameterField(String label, String name, String length, String max, String desc)
  {
         sLabel = label;
         sName = name;
         sLength = length;
         sMaxLength = max;
         sDesc = desc;
  }

  public String getName() {
    return sName;
  }

  public void setName(String newSName) {
    sName = newSName;
  }

  public void setLabel(String newSLabel) {
    sLabel = newSLabel;
  }

  public String getLabel() {
    return sLabel;
  }

  public void setLength(String newSLength) {
    sLength = newSLength;
  }

  public String getLength() {
    return sLength;
  }

  public void setMaxLength(String newSMaxLength) {
    sMaxLength = newSMaxLength;
  }

  public String getMaxLength() {
    return sMaxLength;
  }

  public void setDesc(String newSDesc) {
    sDesc = newSDesc;
  }

  public String getDesc() {
    return sDesc;
  }

  public String writeField()
  {
      StringBuffer buf = new StringBuffer();

      buf.append(sDesc+ "<br>\n");
      buf.append("<table width=\"40%\" border=\"0\">\n");
      buf.append("<tr>\n");
      buf.append("<td width=\"19%\">"+sLabel+":</td>\n");
      buf.append("<td width=\"81%\">\n");
      buf.append("<input type=\"text\" name=\""+sName+"\" size=\""+sLength+"\" maxlength=\""+sMaxLength+"\" >\n");
      buf.append("</td>\n");
      buf.append("</tr>\n");
      buf.append("</table>\n");
      buf.append("<br>\n");

      return buf.toString();
  }

  public String writeUserFields(int num)
  {
      StringBuffer buf = new StringBuffer();

      buf.append("<input type=\"hidden\" name=\"numFields\" value=\""+num+"\"");
      buf.append("Enter custom parameters below.<br>\n");

      for(int i=1; i<num+1; i++) {
              buf.append("<table width=\"40%\" border=\"0\">\n");
              buf.append("<tr>\n");
              buf.append("<td width=\"10%\">Name "+i+":</td>\n");
              buf.append("<td width=\"20%\">\n");
              buf.append("<input type=\"text\" name=\"usrParam_"+i+"\" size=\"10\" maxlength=\"20\" >\n");
              buf.append("</td>\n");
              buf.append("<td width=\"10%\">Value "+i+":</td>\n");
              buf.append("<td width=\"60%\">\n");
              buf.append("<input type=\"text\" name=\"usrValue_"+i+"\" size=\"50\" maxlength=\"70\" >\n");
              buf.append("</td>\n");
              buf.append("</tr>\n");
              buf.append("</table>\n");
              buf.append("<br>\n");
      }

      return buf.toString();
  }

}