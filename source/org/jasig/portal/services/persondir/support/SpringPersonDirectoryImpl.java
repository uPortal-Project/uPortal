/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.services.persondir.IPersonDirectory;
import org.jasig.portal.utils.ResourceLoader;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * This IPersonDirectory implementation uses a Spring XmlBeanFactory to attempt
 * to receive an IPersonDirectory implementation as specified in the XML file to
 * which it delegates.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class SpringPersonDirectoryImpl implements IPersonDirectory {

    /**
     * IPersonDirectory implementation obtained from Spring beanfactory.
     */
    private IPersonDirectory delegate;

    /**
     * Instantiate a SpringPersonDirectoryImpl using an XML file declaring beans
     * (conforming to Spring beans.dtd) which include an implementation of
     * IPersonDirectory with name "personDirectory" to which this class will delegate.
     * 
     * Note: current implementation does a lot of work in the constructor, so
     * it is not appropriate to instantiate this class often.  This class is intended
     * to be instantiated once for each file path.
     * 
     * Throws InstantiationException if the XML file is not found or does not
     * contain a bean named "personDirectory" which implements IPersonDirectory.
     * 
     * @param xmlFilePath path to the config file
     * @throws InstantiationException on failure
     */
    public SpringPersonDirectoryImpl(String xmlFilePath)
        throws InstantiationException {
        InputStream is = null;
        try {
            is = ResourceLoader.getResourceAsStream(this.getClass(),
                    xmlFilePath);
        } catch (Throwable t) {
            throw new InstantiationException("Cannot load the resource at path [" + 
                    xmlFilePath + "]", t);
        }
        XmlBeanFactory factory = new XmlBeanFactory(is);
        
        Object objectDelegate = (IPersonDirectory) factory.getBean("personDirectory");
        
        if (objectDelegate == null)
            throw new InstantiationException("Spring config file from [" + 
                    xmlFilePath + "] did not declare an IPersonDirectory bean.");
        
        if (! (objectDelegate instanceof IPersonDirectory)) {
            throw new InstantiationException("Spring config file from [" + 
                    xmlFilePath + "] declared a 'personDirectory' bean which was " +
                            "not an instance of IPersonDirectory.");
        }
        this.delegate = (IPersonDirectory) objectDelegate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.IPersonDirectory#getUserDirectoryInformation(java.lang.String)
     */
    public Map getUserDirectoryInformation(String username) {
        return this.delegate.getUserDirectoryInformation(username);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.IPersonDirectory#getUserDirectoryInformation(java.lang.String,
     *         org.jasig.portal.security.IPerson)
     */
    public void getUserDirectoryInformation(String uid, IPerson person) {
        this.delegate.getUserDirectoryInformation(uid, person);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.PersonDirectory#getAttributeNames()
     */
    public Set getAttributeNames() {
        return this.delegate.getAttributeNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.IPersonDirectory#getRestrictedPerson(java.lang.String)
     */
    public RestrictedPerson getRestrictedPerson(String uid) {
        return this.delegate.getRestrictedPerson(uid);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.IPersonDirectory#cachePerson(java.lang.String, org.jasig.portal.security.IPerson)
     */
    public void cachePerson(String uid, IPerson person) {
        this.delegate.cachePerson(uid, person);
    }

}
