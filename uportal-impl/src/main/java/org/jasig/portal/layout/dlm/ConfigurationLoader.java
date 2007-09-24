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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ConfigurationLoader
{
    public static final String RCS_ID = "@(#) $Header$";

    private static final String CONFIG_FILE_NAME = "/properties/dlm.xml";
    private static URL configFileURL = null;
    private static final Log LOG = LogFactory.getLog(ConfigurationLoader.class);
    
    /**
     * Load the distributed layout configuration.
     */
    public static void load ( RDBMDistributedLayoutStore dlManager )
    {
        try
        {
            Class c = ConfigurationLoader.class;
            configFileURL = c.getResource(CONFIG_FILE_NAME);
            logConfigFileInfo();

            InputStream inputStream = configFileURL.openStream();
            Document doc = DocumentFactory.getDocumentFromStream(inputStream,
                    configFileURL.toExternalForm());

            NodeList properties = doc.getElementsByTagName( "dlm:property" );
            NodeList definitions = doc.getElementsByTagName( "dlm:fragment" );

            dlManager.setProperties( getProperties( properties ) );
            dlManager.setDefinitions( getFragments( definitions ) );
        }
        catch( Exception e )
        {
            throw new RuntimeException(ConfigurationLoader.class.getName() +
                           " could not load distributed layout " +
                           "configuration.", e);
        }
    }

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

    private static FragmentDefinition[] getFragments( NodeList frags )
    {
        if ( frags == null || frags.getLength() == 0 )
            return null;

        FragmentDefinition[] fragments = null;

        for( int i=0; i<frags.getLength(); i++ )
        {
            try
            {
                FragmentDefinition f = new FragmentDefinition( (Element) frags.item(i) );
                fragments = appendDef( f, fragments);

                if (LOG.isInfoEnabled())
                    LOG.info("\n\nDLM loaded fragment definition '" + f.name +
                            "' owned by '" + f.ownerID +
                            "' with precedence " + f.precedence + 
                            ( f.noAudienceIncluded ? " and no specified audience" +
                              ". It will be editable by '" +
                                f.ownerID + "' but " +
                                "not included in any user's layout." :
                              ( f.evaluators == null ?
                                " with no audience. It will be editable by '" +
                                f.ownerID + "' but " +
                                "not included in any user's layout." :
                                " with " + f.evaluators.length + " audiences" ) ));
            }
            catch( Exception e ) 
            {
                LOG.error("\n\n---------- Warning ---------\nUnable to load " +
                      "distributed layout fragment " +
                      "definition from configuration file\n" +
                      configFileURL.toString() +
                      "\n Details: " + e.getMessage() +
                      "  \n----------------------------\n", e );
            }
        }   
        return fragments;
    }

    private static FragmentDefinition[] appendDef(
        FragmentDefinition f,
        FragmentDefinition[] frags
        )
    {
        if ( frags == null )
        {
            f.index = 0;
            return new FragmentDefinition[] { f };
        }
        f.index = frags.length;
        FragmentDefinition[] newArr = new FragmentDefinition[frags.length + 1];
        System.arraycopy( frags, 0, newArr, 0, frags.length );
        newArr[frags.length] = f;
        return newArr;
    }
}
