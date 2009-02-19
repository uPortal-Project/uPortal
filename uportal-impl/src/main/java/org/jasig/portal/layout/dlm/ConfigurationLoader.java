/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public abstract class ConfigurationLoader
{
    public static final String RCS_ID = "@(#) $Header$";

    private static final String CONFIG_FILE_NAME = "/properties/dlm.xml";
    private static URL configFileURL = null;
    private static final Log LOG = LogFactory.getLog(ConfigurationLoader.class);
    
    // Instance Members.
    private Properties props;
        
    /*
     * Public API.
     */
        
    /**
     * Load the distributed layout configuration.
     */
    public static ConfigurationLoader load() {
        
        ConfigurationLoader rslt = null;
        
        try {
            configFileURL = ConfigurationLoader.class.getResource(CONFIG_FILE_NAME);
            logConfigFileInfo();

            InputStream inputStream = configFileURL.openStream();
            Document doc = DocumentFactory.getDocumentFromStream(inputStream,
                    configFileURL.toExternalForm());

            NodeList properties = doc.getElementsByTagName( "dlm:property" );
            Properties props = getProperties(properties);
            
            String impl = props.getProperty("ConfigurationLoader.impl");
            Class<? extends ConfigurationLoader> c = null;
            if (impl == null) {
                c = LegacyConfigurationLoader.class; 
            } else {
                c = (Class<? extends ConfigurationLoader>) Class.forName(impl);
            }

            rslt = c.newInstance();
            rslt.init(doc);
            rslt.setProperties(props);
            
        } catch( Exception e ) {
            throw new RuntimeException(ConfigurationLoader.class.getName() +
                           " could not load distributed layout " +
                           "configuration.", e);
        }
        
        return rslt;
        
    }
    
    public abstract void init(Document doc);
    
    public final String getProperty(String name) {
        return props.getProperty(name);
    }
    
    public final int getPropertyCount() {
        return props.size();
    }
    
    public abstract FragmentDefinition[] getFragments();
    
    /*
     * Implementation.
     */
    
    private static void logConfigFileInfo()
    {
        if (LOG.isInfoEnabled())
        {
            try
            {
                char[] buf = new char[4096];
                InputStream is = configFileURL.openStream();
                InputStreamReader isr = new InputStreamReader(is);
                StringWriter sw = new StringWriter();
                PrintWriter p = new PrintWriter(sw);

                p.println("\n\n---- Distributed Layout Management ----");
                p.println("config file: " + configFileURL.toString());
                p.println("\n---- CONTENTS ----\n");
                p.flush();

                int i = -1;
                while ((i = isr.read(buf, 0, 4096)) != -1)
                    sw.write(buf, 0, i);
                LOG.info(sw.toString() + "\n------------------\n");
            } catch (Exception IOException)
            {
                // ignore. if we can't open here runtime will be thrown soon
                // after returning showing the same information.
            }
        }
    }

    private static Properties getProperties( NodeList props )
    {
        if ( props == null || props.getLength() == 0 )
            return null;

        Properties properties = new Properties();

        for( int i=0; i<props.getLength(); i++ )
        {
            Node node = props.item(i);
            NamedNodeMap atts = node.getAttributes();
            Node name = atts.getNamedItem( "name" );
            Node value = atts.getNamedItem( "value" );
            if ( name == null || name.equals( "" ) )
            {
                if (LOG.isInfoEnabled())
                    LOG.info("\n\n---------- Warning ----------\nThe 'name'" +
                            " attribute of the " +
                            "property element is required and must not be empty " +
                            "in \n'" + XML.serializeNode(node) +
                            "'\nfrom distributed layout managment configuration " +
                            "file \n" + configFileURL.toString() +
                            "  \n-----------------------------\n");
            continue;
            }
            if ( value == null )
            properties.put( name.getNodeValue(), "" );
            else
            properties.put( name.getNodeValue(),
                    value.getNodeValue() );
        }
        return properties;
    }

    private final void setProperties(Properties props) {
        
        // Assertions.
        if (props == null) {
            String msg = "Argument 'props' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        this.props = props;
        
    }

}
