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
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
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
