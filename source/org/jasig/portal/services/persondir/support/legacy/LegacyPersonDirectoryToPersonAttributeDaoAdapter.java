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
import org.jasig.portal.services.persondir.support.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.MergingPersonAttributeDaoImpl;
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
public class LegacyPersonDirectoryToPersonAttributeDaoAdapter implements IPersonAttributeDao {

    private IPersonAttributeDao delegate;
    
    public static final String PERSON_DIRS_FILE_PATH = "/properties/PersonDirs.xml";
    
    /**
     * This constructor builds a legacy PersonDirectory implementation suitable
     * for environments in which Spring configuration is not available.
     */
    public LegacyPersonDirectoryToPersonAttributeDaoAdapter() 
        throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        // build up from the bottom
        
        // we need PersonAttributeDaos implementing each of the
        // sources represented by the infos in the parsed PersonDirs.xml file.
        final List personAttributeDaos = new ArrayList();
        
        // Build a DOM tree out of uPortal/properties/PersonDirs.xml
        final Document doc = ResourceLoader.getResourceAsDocument(this.getClass(), PERSON_DIRS_FILE_PATH);
        
        final List personDirectoryInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        
        for (final Iterator pdInfoItr = personDirectoryInfos.iterator(); pdInfoItr.hasNext();){
            final PersonDirInfo info = (PersonDirInfo)pdInfoItr.next();
            
            final PersonDirInfoAdaptor adaptor = new PersonDirInfoAdaptor(info);
            
            personAttributeDaos.add(adaptor);
        }
        
        // build a merger over the list of sources
        final MergingPersonAttributeDaoImpl merger = new MergingPersonAttributeDaoImpl();
        merger.setPersonAttributeDaos(personAttributeDaos);
        
        // store the constructed PersonDirectory implementation.
        this.delegate = merger;
    }

    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getDefaultAttributeName()
     */
    public String getDefaultAttributeName() {
        return this.delegate.getDefaultAttributeName();
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.delegate.getPossibleUserAttributeNames();
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(Map seed) {
        return this.delegate.getUserAttributes(seed);
    }
    
    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public Map getUserAttributes(String uid) {
        return this.delegate.getUserAttributes(uid);
    }
}