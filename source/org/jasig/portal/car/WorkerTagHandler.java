/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles reading the worker tags in the descriptor. These tags
 * contain only attributes and hence only the startElement event is
 * needed.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
class WorkerTagHandler
    extends DefaultHandler
{
    private Properties workerProps = null;
        
    WorkerTagHandler(Properties workers)
    {
        this.workerProps = workers;
    }
        
    public void startElement(java.lang.String namespaceURI,
                             java.lang.String localName,
                             java.lang.String qName,
                             Attributes atts)
        throws SAXException
    {
        if ( !qName.equals( DescriptorHandler.WORKER_TAG_NAME ) )
            return;
        String workerClass = atts.getValue( "class" );
        
        if ( workerClass == null )
            return;
        
        workerProps.put( workerClass.replace( '.', '_' ), workerClass );
    }
}
