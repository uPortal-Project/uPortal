/**
 * Copyright 2009 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link ConfigurationLoader} that behaves exactly as DLM has 
 * always done:  load {@link FragmentDefinition} objects from dlm.xml.
 * 
 * @author awills
 */
public final class LegacyConfigurationLoader extends ConfigurationLoader{

    // Instance Members.
    private FragmentDefinition[] fragments = null;
    private final Log LOG = LogFactory.getLog(ConfigurationLoader.class);

    /*
     * Public API.
     */
    
    public void init(Document doc) {
        
        // Assertions.
        if (doc == null) {
            String msg = "Argument 'doc' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (fragments != null) {
            String msg = "init() may only be called once.";
            throw new IllegalStateException(msg);
        }
        
        NodeList definitions = doc.getElementsByTagName( "dlm:fragment" );
        this.fragments = parseFragments(definitions);

    }
    
    public FragmentDefinition[] getFragments() {
        FragmentDefinition[] rslt = new FragmentDefinition[fragments.length];
        System.arraycopy(fragments, 0, rslt, 0, fragments.length);
        return rslt;
    }

    
    /*
     * Implementation.
     */
    
    private FragmentDefinition[] parseFragments( NodeList frags )
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
                    LOG.info("\n\nDLM loaded fragment definition '" + f.getName() +
                            "' owned by '" + f.getOwnerId() +
                            "' with precedence " + f.getPrecedence() + 
                            ( f.noAudienceIncluded ? " and no specified audience" +
                              ". It will be editable by '" +
                                f.getOwnerId() + "' but " +
                                "not included in any user's layout." :
                              ( f.noAudienceIncluded ?
                                " with no audience. It will be editable by '" +
                                f.getOwnerId() + "' but " +
                                "not included in any user's layout." :
                                " with audiences defined" ) ));
            }
            catch( Exception e ) 
            {
                LOG.error("\n\n---------- Warning ---------\nUnable to load " +
                      "distributed layout fragment " +
                      "definition from configuration file\n" +
                      "\n Details: " + e.getMessage() +
                      "  \n----------------------------\n", e );
            }
        }   
        return fragments;
    }

    private FragmentDefinition[] appendDef(
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
