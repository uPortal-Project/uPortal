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

package org.jasig.portal;

/**
 * This exception would inform uPortal that a particular
 * resource required to complete channel operation is
 * missing.
 * @author Peter Kharchenko
 * @version $Revision$ $Date$
 */
public class ResourceMissingException extends PortalException {
    private static final long serialVersionUID = 1L;

    /**
     * URI of the missing resource
     */ 
    private String resourceURI = null;
    
    /**
     * Description of the missing resource.
     */
    private String description = null;
    
    public ResourceMissingException(ResourceMissingException exception) {
        super(exception.getMessage());
        this.resourceURI = exception.getResourceURI();
        this.description = exception.getResourceDescription();
    }
    /**
     * Instantiate a ResourceMissingException providing the URI of the missing resource,
     * a description of the missing resource, and a message.
     * @param resourceURI URI of the missing resource
     * @param resourceDescription description of the missing resource
     * @param msg message about the error condition
     */
    public ResourceMissingException(String resourceURI, String resourceDescription, String msg) {
        super(msg);
        this.resourceURI = resourceURI;
        this.description = resourceDescription;
    }

    /**
     * Instantiate a ResourceMissingException providing the URI of the missing resource,
     * a description of the missing resource, and a cause.
     * @param resourceURI URI of the missing resource
     * @param resourceDescription description of the missing resource
     * @param cause the cause of the error condition
     */
    public ResourceMissingException(String resourceURI, String resourceDescription, Throwable cause) {
        super(cause);
        this.resourceURI = resourceURI;
        this.description = resourceDescription;
    }
    
    /**
     * Instantiate a ResourceMissingException providing a URI of the missing resource,
     * a description of the missing resource, a message, and indicating whether
     * channel refresh and channel reinstantiation are appropriate responses to the
     * error condition.
     * @param resourceURI URI of the missing resource
     * @param resourceDescription description of the missing resource
     * @param msg message about the error condition
     * @param refresh true if refreshing is an appropriate response
     * @param reinstantiate true if reinstantiation is an appropriate response
     */
    public ResourceMissingException(String resourceURI, String resourceDescription, String msg, boolean refresh, boolean reinstantiate) {
        super(msg,refresh,reinstantiate);
        this.resourceURI=resourceURI;
        this.description=resourceDescription;
    }
    
    /**
     * Instantiate a ResourceMissingException providing the URI of the missing
     * resource, a description of the missing resource, a message, and an
     * underlying cause.
     * @param resourceUri URI of the missing resource
     * @param description description of the missing resource
     * @param message message about the error condition
     * @param cause underlying cause of this problem
     */
    public ResourceMissingException(String resourceUri, String description, 
            String message, Throwable cause) {
        super(message, cause);
        this.resourceURI = resourceUri;
        this.description = description;
    }

    /**
     * Get the URI of the missing resource.
     * @return the URI of the missing resource, or null if not specified.
     */
    public String getResourceURI() {
        return this.resourceURI;
    }

    /**
     * Get a description of the missing resource.
     * @return a description of the missing resource, or null if not specified
     */
    public String getResourceDescription() {
        return this.description;
    }

}
