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

import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;

import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.services.persondir.IPersonDirectory;
import org.jasig.portal.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.jasig.portal.services.persondir.support.PersonDirectoryImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This legacy PersonDirectory implementation attempts to recreate the 
 * uPortal 2.4.2 PersonDirectory behavior of configuration using PersonDirs.xml.
 * In 2.4.2, caching of IPersons in PersonDirectory was removed because it was
 * leaking memory.  This implementation also eliminates caching of IPersons.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class LegacyPersonDirectoryImpl implements IPersonDirectory {

    private IPersonDirectory delegate;
    
    public static final String PERSON_DIRS_FILE_PATH 
        = "/properties/PersonDirs.xml";
    
    /**
     * This constructor builds a legacy PersonDirectory implementation suitable
     * for environments in which Spring configuration is not available.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public LegacyPersonDirectoryImpl() 
        throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        // build up from the bottom
        
        // we need PersonAttributeDaos implementing each of the
        // sources represented by the infos in the parsed PersonDirs.xml file.
        
        List personAttributeDaos = new ArrayList();
        
        // Build a DOM tree out of uPortal/properties/PersonDirs.xml
        Document doc = ResourceLoader.getResourceAsDocument(
                this.getClass(), PERSON_DIRS_FILE_PATH);
        
        List infos = PersonDirXmlParser.getPersonDirInfos(doc);
        
        for (Iterator iter = infos.iterator(); iter.hasNext();){
            PersonDirInfo info = (PersonDirInfo) iter.next();
            PersonDirInfoAdaptor adaptor = new PersonDirInfoAdaptor(info);
            personAttributeDaos.add(adaptor);
        }
        
        // build a merger over the list of sources
        MergingPersonAttributeDaoImpl merger = 
            new MergingPersonAttributeDaoImpl();
        
        merger.setPersonAttributeDaos(personAttributeDaos);
        
        // implement PersonDirectory using the merger
        PersonDirectoryImpl personDirectory = new PersonDirectoryImpl();
        personDirectory.setAttributeSource(merger);
        
        // you could re-introduce caching by wrapping personDirectory
        // with a caching wrapper.
        
        // store the constructed PersonDirectory implementation.
        this.delegate = personDirectory;
    }
    
    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getUserDirectoryInformation(java.lang.String)
     */
    public Map getUserDirectoryInformation(String username) {
        return this.delegate.getUserDirectoryInformation(username);
    }

    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getUserDirectoryInformation(java.lang.String, org.jasig.portal.security.IPerson)
     */
    public void getUserDirectoryInformation(String uid, IPerson person) {
        this.delegate.getUserDirectoryInformation(uid, person);
    }

    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getRestrictedPerson(java.lang.String)
     */
    public RestrictedPerson getRestrictedPerson(String uid) {
        return this.delegate.getRestrictedPerson(uid);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.IPersonDirectory#cachePerson(org.jasig.portal.security.IPerson)
     */
    public void cachePerson(String uid, IPerson person) {
        this.delegate.cachePerson(uid, person);
    }

}