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

package org.jasig.portal.layout.dlm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link ConfigurationLoader} that behaves exactly as DLM has 
 * always done:  load {@link FragmentDefinition} objects from dlm.xml.
 * 
 * @author awills
 * @deprecated
 */
public class LegacyConfigurationLoader implements ConfigurationLoader {
    private Resource configurationFile;
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final SingletonDoubleCheckedCreator<Boolean> loadedFlag = new SingletonDoubleCheckedCreator<Boolean>() {

        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator#createSingleton(java.lang.Object[])
         */
        @Override
        protected Boolean createSingleton(Object... args) {
            loadFragmentInfo();
            return true;
        }
    };
    
    private List<FragmentDefinition> fragments = null;
    private Map<String, FragmentDefinition> fragmentsByName = null;
    private Map<String, FragmentDefinition> fragmentsByOwnerId = null;
    
    /**
     * @param configurationFile The dlm.xml file to load configuration from
     */
    public void setConfigurationFile(Resource configurationFile) {
        this.configurationFile = configurationFile;
    }


    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    protected void loadFragmentInfo() {
        final InputStream inputStream;
        try {
            inputStream = this.configurationFile.getInputStream();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not open InputStream to dlm configuration resource "+ this.configurationFile, e);
        }
        
        final String configUrl;
        try {
            configUrl = this.configurationFile.getURL().toExternalForm();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not convert dlm configuration resource to URL "+ this.configurationFile, e);
        }
        
        final Document doc;
        try {
            doc = DocumentFactory.getDocumentFromStream(inputStream, configUrl);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could load dlm configuration resource "+ this.configurationFile, e);
        }
        catch (SAXException e) {
            throw new IllegalArgumentException("Could parse dlm configuration resource "+ this.configurationFile, e);
        }
        
        final NodeList fragmentNodes = doc.getElementsByTagName( "dlm:fragment" );
        final List<FragmentDefinition> localFragments = this.getFragments(fragmentNodes);
        if (localFragments != null) {
            // lastly sort according to precedence followed by index
            Collections.sort(localFragments, new FragmentComparator() );
            // show sort order in log file if debug is on. (Could check and
            // only build of on but do later.)
            if (logger.isDebugEnabled()) {
                StringBuilder bfr = new StringBuilder();
                for (final FragmentDefinition fragmentDefinition : localFragments) {
                    bfr.append( fragmentDefinition.getName() );
                    bfr.append( "[" );
                    bfr.append( fragmentDefinition.getPrecedence() );
                    bfr.append( "],\n" );
                }
                logger.debug("Fragments Sorted by Precedence and then index {\n" +
                    bfr.toString() + " }" );
            }
            
            //Store the fragments in a map by name & owner for easy access
            final Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<String, FragmentDefinition>();
            for (final FragmentDefinition fragmentDefinition : localFragments) {
                fragmentsByName.put(fragmentDefinition.getName(), fragmentDefinition);
            }
            final Map<String, FragmentDefinition> fragmentsByOwnerId = new LinkedHashMap<String, FragmentDefinition>();
            for (final FragmentDefinition fragmentDefinition : localFragments) {
                fragmentsByOwnerId.put(fragmentDefinition.getOwnerId(), fragmentDefinition);
            }
            
            this.fragments = Collections.unmodifiableList(localFragments);
            this.fragmentsByName = Collections.unmodifiableMap(fragmentsByName);
            this.fragmentsByOwnerId = Collections.unmodifiableMap(fragmentsByOwnerId);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dlm.ConfigurationLoader#getFragments()
     */
    @Override
    public List<FragmentDefinition> getFragments() {
        this.loadedFlag.get();
        return this.fragments;
    }
    

    @Override
    public FragmentDefinition getFragmentByName(String name) {
        this.loadedFlag.get();
        return this.fragmentsByName.get(name);
    }

    @Override
    public FragmentDefinition getFragmentByOwnerId(String ownerId) {
        this.loadedFlag.get();
        return this.fragmentsByOwnerId.get(ownerId);
    }

    protected List<FragmentDefinition> getFragments( NodeList frags )
    {
        if ( frags == null || frags.getLength() == 0 ) {
            return null;
        }
        
        final int fragmentCount = frags.getLength();
        this.fragments = new ArrayList<FragmentDefinition>(fragmentCount);
        for( int i=0; i<fragmentCount; i++ )
        {
            final Element fragmentElement = (Element) frags.item(i);
            try
            {
                FragmentDefinition fragment = new FragmentDefinition( fragmentElement );
                fragment.setIndex(i);

                this.fragments.add(fragment);

                if (logger.isInfoEnabled()) {
                    logger.info("DLM loaded fragment definition '" + fragment.getName() +
                            "' owned by '" + fragment.getOwnerId() +
                            "' with precedence " + fragment.getPrecedence() + 
                            ( fragment.isNoAudienceIncluded() ? " and no specified audience" +
                              ". It will be editable by '" +
                                fragment.getOwnerId() + "' but " +
                                "not included in any user's layout." :
                              ( fragment.isNoAudienceIncluded() ?
                                " with no audience. It will be editable by '" +
                                fragment.getOwnerId() + "' but " +
                                "not included in any user's layout." :
                                " with audiences defined" ) ));
                }
            }
            catch( Exception e ) 
            {
                final String msg = "Unable to load distributed layout fragment definition. Content from this fragment will not be avilable.";
                if (this.logger.isDebugEnabled()) {
                    logger.debug(msg + XmlUtilitiesImpl.toString(fragmentElement), e );
                }
                else {
                    logger.warn(msg + " Enable DEBUG logging for stack trace.\n\tCaused By: " + e.getMessage() + " " + XmlUtilitiesImpl.toString(fragmentElement));
                }
            }
        }   
        
        return fragments;
    }
    
    protected void logConfigFileInfo() {
        if (logger.isInfoEnabled()) {
            InputStream is = null;
            try {
                is = this.configurationFile.getInputStream();
                logger.info("DLM config file: " + this.configurationFile);
            }
            catch (Exception IOException) {
                // ignore. if we can't open here runtime will be thrown soon
                // after returning showing the same information.
            }
            finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

}
