/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.jasig.portal.services.persondir.support.merger.ReplacingAttributeAdder;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This implementation attempts to recreate the uPortal 2.4.2
 * {@link org.jasig.portal.services.PersonDirectory} behavior of
 * configuration using PersonDirs.xml.
 * 
 * @author andrew.petro@yale.edu, Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class LegacyPersonAttributeDao implements IPersonAttributeDao {

    private IPersonAttributeDao delegate;
    
    public static final String PERSON_DIRS_FILE_PATH = "/properties/PersonDirs.xml";
    
    /**
     * This constructor builds a legacy PersonDirectory implementation suitable
     * for environments in which Spring configuration is not available.
     * @throws SAXException when PersonDirs.xml is not well formed
     * @throws ParserConfigurationException when ResourceLoader is misconfigured
     * @throws IOException - when PersonDirs.xml cannot be read
     * @throws ResourceMissingException - when /properties/PersonDirs.xml is missing
     */
    public LegacyPersonAttributeDao() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException  {
        // build up from the bottom
        
        // we need PersonAttributeDaos implementing each of the
        // sources represented by the infos in the parsed PersonDirs.xml file.
        final List personAttributeDaos = new ArrayList();
        
        // Build a DOM tree out of uPortal/properties/PersonDirs.xml
        final Document doc = ResourceLoader.getResourceAsDocument(this.getClass(), PERSON_DIRS_FILE_PATH);
        
        final List personDirectoryInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        
        for (final Iterator pdInfoItr = personDirectoryInfos.iterator(); pdInfoItr.hasNext();){
            final PersonDirInfo info = (PersonDirInfo)pdInfoItr.next();
            
            final IPersonAttributeDao adapted = PersonDirInfoAdaptor.adapt(info);
            
            personAttributeDaos.add(adapted);
        }
        
        // build a merger over the list of sources
        final MergingPersonAttributeDaoImpl merger = new MergingPersonAttributeDaoImpl();
        //Enforce the requisite merger
        merger.setMerger(new ReplacingAttributeAdder());
        merger.setPersonAttributeDaos(personAttributeDaos);
        merger.setDefaultAttributeName(PersonDirInfoAdaptor.QUERY_ATTRIBUTE);
        
        // store the constructed PersonDirectory implementation.
        this.delegate = merger;
    }

    public Set getPossibleUserAttributeNames() {
        return this.delegate.getPossibleUserAttributeNames();
    }
    
    public Map getUserAttributes(Map seed) {
        return this.delegate.getUserAttributes(seed);
    }
    
    public Map getUserAttributes(String uid) {
        return this.delegate.getUserAttributes(uid);
    }
}