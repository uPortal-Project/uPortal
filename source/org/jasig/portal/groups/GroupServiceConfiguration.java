/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
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

package org.jasig.portal.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Parses service descriptions found in the file found at SERVICES_XML.  The
 * elements of each service are stored in a ComponentGroupServiceDescriptor.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class GroupServiceConfiguration
{
    private static final Log log = LogFactory.getLog(GroupServiceConfiguration.class);
    
    // The file containing the configuration:
    private static String SERVICES_XML = "/properties/groups/compositeGroupServices.xml";

    // Singleton instance.
    private static GroupServiceConfiguration configuration;

    private GroupConfigurationHandler serviceHandler;
    private List serviceDescriptors = new ArrayList();
    private Map attributes = new HashMap();

    // Handler for parsing the xml source.
    class GroupConfigurationHandler extends org.xml.sax.helpers.DefaultHandler {
      ComponentGroupServiceDescriptor svcDescriptor;
      String elementName;
      StringBuffer elementValue;

      public void startElement (String namespaceURI, String localName, String qName, Attributes atts) {
        elementName = qName;
        elementValue = new StringBuffer();

      if (qName.equals("servicelist"))
      {
          infoMessage("Parsing group service configuration.");
          parseAttributes(atts);
      }
      else if (qName.equals("service"))
      {
        debugMessage("Parsing configuration for component service.");
        svcDescriptor = new ComponentGroupServiceDescriptor();
        for(int i=0; i<atts.getLength(); i++)
        {
            String name = atts.getQName(i);
            String value = atts.getValue(i);
            svcDescriptor.put(name, value);
        }
      }
    }

    public void endElement (String namespaceURI, String localName, String qName) {
      String val = elementValue.toString();
      if (qName.equals("service"))
      {
        serviceDescriptors.add(svcDescriptor);
        debugMessage("Parsed configuration for " + svcDescriptor.getName());
      }
      else if (qName.equals("servicelist"))
          { debugMessage("Done parsing group service configuration."); }
      else if (qName.equals("internally_managed"))
          { svcDescriptor.setInternallyManaged("TRUE".equalsIgnoreCase(val)); }
      else if (qName.equals("caching_enabled"))
          { svcDescriptor.setCachingEnabled("TRUE".equalsIgnoreCase(val)); }
      else
          { svcDescriptor.setAttribute(elementName, val); }
    }

    public void characters (char ch[], int start, int length)
    {
        if (elementName == null || elementName.equals("service") || elementName.equals("servicelist"))
            return;
        String chValue = new String(ch, start, length);
        elementValue.append(chValue);
    }
  }
public GroupServiceConfiguration()
{
    super();
    serviceHandler = new GroupConfigurationHandler();
}

/**
 * Record a message at "Debug" level.
 * @deprecated see comment inside method
 */
protected void debugMessage(String msg) {
    /*
     * This method is not in keeping with best practices.
     * Prepending "Group services:" to the message is unnecessary because
     * Commons Logging is capable of reporting from where the logging message
     * originated when configured to do so.  The org.jasig.portal.groups package 
     * in its entirety can be mapped to its own logger.
     * Logging directly allows instutions wishing to take the performance hit of
     * doing so to configure log4j to report from exactly what line of code
     * each logging message originates.  Going through this method causes all
     * such messages to appear to come from here rather than from whereever
     * this method is being invoked from.
     */
    log.debug("Group services: " + msg);
}
/**
 *
 */
public Map getAttributes() {
    return attributes;
}
public static synchronized GroupServiceConfiguration getConfiguration() throws Exception
{
    if (configuration == null)
    {
        configuration = new GroupServiceConfiguration();
        configuration.parseXml();
    }
    return configuration;
}
/**
 *
 */
public String getDefaultService() {
    return (String)getAttributes().get("defaultService");
}
/**
 *
 */
public String getNodeSeparator() {
    Object nodeSeparator = getAttributes().get("nodeSeparator");
    return  ( nodeSeparator == null )
        ? IGroupConstants.NODE_SEPARATOR
        : (String)nodeSeparator;
}
public List getServiceDescriptors()
{
    return serviceDescriptors;
}

/**
 * Record a message at "info" level.
 * @deprecated see comment inside method
 */
protected void infoMessage(String msg) {
    /*
     * This method is not in keeping with best practices.
     * Prepending "Group services:" to the message is unnecessary because
     * Commons Logging is capable of reporting from where the logging message
     * originated when configured to do so.  The org.jasig.portal.groups package 
     * in its entirety can be mapped to its own logger.
     * Logging directly allows instutions wishing to take the performance hit of
     * doing so to configure log4j to report from exactly what line of code
     * each logging message originates.  Going through this method causes all
     * such messages to appear to come from here rather than from whereever
     * this method is being invoked from.
     */
    log.info( "Group services: " + msg);
}


/**
 *
 */
protected void parseAttributes(Attributes atts)
{
    String name, value;
    for(int i=0; i<atts.getLength(); i++)
    {
        name = atts.getQName(i);
        value = atts.getValue(i);
        getAttributes().put(name, value);
    }
}
protected void parseXml() throws Exception
{
    InputSource xmlSource =
      new InputSource(ResourceLoader.getResourceAsStream(GroupServiceConfiguration.class, SERVICES_XML));

    if (xmlSource != null)
    {
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        reader.setContentHandler(serviceHandler);
        reader.parse(xmlSource);
    }
}
public static synchronized void reset()
{
    configuration = null;
}
}
