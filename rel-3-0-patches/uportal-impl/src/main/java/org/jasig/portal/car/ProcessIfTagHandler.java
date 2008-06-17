/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles a "processIf" tag looking for the following attributes and
 * blocking SAX events from reaching nested tags if the specified
 * requirements are not met.
 * 
 * <pre>
 *  fname = the functional name whose version is to be checked
 *  version = value is one of lessThan, greaterThan, lessThanOrEqual,
 *    greaterThanOrEqual, equalTo, or notEqualTo.
 *  major = the revision level major part
 *  minor = the revision level minor part
 *  micro = the revision level micro part
 *  setMajor = optional, the new revision level major part
 *  setMinor = optional, the new revision level minor part
 *  setMicro = optional, the new revision level micro part
 * </pre>
 * 
 * The contents of the processIf tag are only processed if the version
 * of the specified fname matches the requirements as specified in the
 * version, major, minor, and micro attributes. If the setMajor,
 * setMinor, and setMicro attributes are specified then and additional
 * test is performed to determine if processing will occur. This test
 * involves attempting to set the version of the fname to that
 * specified. If unable to then processing will be skipped for this
 * block. This is done to allow the same CAR to be deployed on all
 * servers in a multi-server deployment but only have on of the
 * servers perform the processing of the block and affect the
 * database.
 *
 * There is no limit on the number of processIf blocks that can occur
 * in a deployment descriptor. All other nested tags are supported
 * within the processIf tag. The processIf tag can not be nested
 * inside of itself.
 *   
 *
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class ProcessIfTagHandler
extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(ProcessIfTagHandler.class);
    private boolean enabled=false;
    private RoutingHandler routingHandler=null;
    private ParsingContext ctx = null;
    
    private static final String LESS_THAN = "lessThan";
    private static final String GTR_THAN = "greaterThan";
    private static final String LESS_THAN_OR_EQU = "lessThanOrEqual";
    private static final String GTR_THAN_OR_EQU = "greaterThanOrEqual";
    private static final String EQU = "equalTo";
    private static final String NOT_EQU = "notEqualTo";
    
    ProcessIfTagHandler(ParsingContext ctx, RoutingHandler routingHandler)
    {
        this.routingHandler = routingHandler;
        this.ctx = ctx;
    }

    /**
     * Passes the attributes of the processIf tag that is currently
     * being parsed to this handler allowing it to determine if it
     * should allow SAX events to be passed to the contained tag
     * handlers or not.
     * 
     * @param atts
     **/
    void setAttributes(Attributes atts)
    {
        // determine if process tag is enabled or not
        enabled = false;

        // aquire and validate major, minor, & micro        
        int major = -1;
        int minor = -1;
        int micro = -1;
        int setMajor = -1;
        int setMinor = -1;
        int setMicro = -1;
        String setDescription = null;
        
        try
        {
            major = Integer.parseInt(atts.getValue("major"));            
            minor = Integer.parseInt(atts.getValue("minor"));            
            micro = Integer.parseInt(atts.getValue("micro"));            
            if (major < 0 || minor < 0 || micro < 0)
                throw new IllegalArgumentException();            
        }
        catch(Exception e)
        {
            log.error(
                "The deployment descriptor "
                    + CarResources.DEPLOYMENT_DESCRIPTOR
                    + " in "
                    + ctx.getJarFile().getName()
                    + " contains an invalid "
                    + DescriptorHandler.PROCESS_TAG_NAME
                    + " tag. It must contain major, minor, and micro, "
                    + " and each must have zero or a positive"
                    + " integer value. Ignoring contents of block.");
            return;
        }
        
        setDescription = atts.getValue("setDescription");

        boolean newVersionIncluded =
            atts.getValue("setMajor") != null
                || atts.getValue("setMinor") != null
                || atts.getValue("setMicro") != null 
                || setDescription != null;
        
        try
        {
            if ( newVersionIncluded )
            {
                setMajor = Integer.parseInt(atts.getValue("setMajor"));            
                setMinor = Integer.parseInt(atts.getValue("setMinor"));            
                setMicro = Integer.parseInt(atts.getValue("setMicro"));            
                if (setMajor < 0
                    || setMinor < 0
                    || setMicro < 0
                    || setDescription == null)
                    throw new IllegalArgumentException();
            }
        }
        catch(Exception e)
        {
            log.error(
                "The deployment descriptor "
                    + CarResources.DEPLOYMENT_DESCRIPTOR
                    + " in "
                    + ctx.getJarFile().getName()
                    + " contains an invalid "
                    + DescriptorHandler.PROCESS_TAG_NAME
                    + " tag. If it contains setMajor, setMinor, setMicro,"
                    + " or setDescription attributes it must contain all four"
                    + " and each of the first three must have zero or a"
                    + " positive integer value. Ignoring contents of block.");
            return;
        }
        // now aquire and validate ifVersion
        String testType = atts.getValue("version");
        
        if (testType == null)
            testType = EQU;
            
        if (! testType.equals(EQU) &&
        ! testType.equals(LESS_THAN) &&
        ! testType.equals(LESS_THAN_OR_EQU) &&
        ! testType.equals(GTR_THAN) &&
        ! testType.equals(GTR_THAN_OR_EQU) &&
        ! testType.equals(NOT_EQU))
        {
            log.error(
                "The deployment descriptor "
                    + CarResources.DEPLOYMENT_DESCRIPTOR
                    + " in "
                    + ctx.getJarFile().getName()
                    + " contains an invalid "
                    + DescriptorHandler.PROCESS_TAG_NAME
                    + " tag. If it contains an 'version' attribute the"
                    + " value of that attribute must be one of '"
                    + EQU
                    + "', '"
                    + NOT_EQU
                    + "', '"
                    + GTR_THAN
                    + "', '"
                    + GTR_THAN_OR_EQU
                    + "', '"
                    + LESS_THAN
                    + "', or '"
                    + LESS_THAN_OR_EQU
                    + "'. Ignoring contents of block.");
            return;
        }
        
        // now aquire and validate fname
        String fname = atts.getValue("fname");
        if (fname == null || fname.equals(""))
        {
            log.error(
                "The deployment descriptor "
                    + CarResources.DEPLOYMENT_DESCRIPTOR
                    + " in "
                    + ctx.getJarFile().getName()
                    + " contains an invalid "
                    + DescriptorHandler.PROCESS_TAG_NAME
                    + " tag. It must contain a non-empty 'fname' attribute"
                    + " holding the functional name whose version is being"
                    + " tested. Ignoring contents of block.");
            return;
        }
        // now see if version test indicates that the block should be processed
        enabled = evaluate(testType, fname, major, minor, micro);
        
        // now see if we can set it to the new version if specified
        if (enabled && newVersionIncluded)
        {
            VersionsManager vMgr = VersionsManager.getInstance();
            enabled =
                vMgr.setVersion(
                    fname,
                    setDescription,
                    setMajor,
                    setMinor,
                    setMicro);
        }
    }
    
    /**
     * Determines if the version specified and the version recorded in the 
     * system indicate that the block should be processed.
     * 
     * @param testType
     * @param fname
     * @param major
     * @param minor
     * @param micro
     * @return true if the block should be processed.
     */
    private boolean evaluate(String testType, String fname, int major, int minor, int micro)
    {
        VersionsManager vMgr = VersionsManager.getInstance();
        Version currVer = vMgr.getVersion(fname);
        Version testVer = new Version(fname, "", major, minor, micro);
        
        if (currVer == null)
            return true;
            
        if (testType.equals(EQU))
            return currVer.equalTo(testVer);
        else if (testType.equals(NOT_EQU))
            return !currVer.equalTo(testVer);
        else if (testType.equals(GTR_THAN))
            return currVer.greaterThan(testVer);
        else if (testType.equals(GTR_THAN_OR_EQU))
            return currVer.greaterThan(testVer) || currVer.equalTo(testVer);
        else if (testType.equals(LESS_THAN))
            return currVer.lessThan(testVer);
        else if (testType.equals(LESS_THAN_OR_EQU))
            return currVer.lessThan(testVer) || currVer.equalTo(testVer);
            
        // should never get here
        return false;
    }

    
    /////// ContentHandler methods of interest

    public void startElement(java.lang.String namespaceURI,
                         java.lang.String localName,
                         java.lang.String qName,
                         Attributes atts)
        throws SAXException
    {
        if (enabled)
            routingHandler.startElement( namespaceURI, localName,
                                                    qName, atts );
    }
    
    public void endElement(java.lang.String namespaceURI,
                       java.lang.String localName,
                       java.lang.String qName)
                throws SAXException
    {
        if (enabled)
            routingHandler.endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException
    {
        if (enabled)
            routingHandler.characters( ch, start, length );
    }
}
