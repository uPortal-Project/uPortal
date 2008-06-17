/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
          log.info("Parsing group service configuration.");
          parseAttributes(atts);
      }
      else if (qName.equals("service"))
      {
        log.debug("Parsing configuration for component service.");
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
        log.debug("Parsed configuration for " + svcDescriptor.getName());
      }
      else if (qName.equals("servicelist"))
          { log.debug("Done parsing group service configuration."); }
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
