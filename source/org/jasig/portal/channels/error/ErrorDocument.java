/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error;

import org.jasig.portal.channels.error.error2xml.DelegatingThrowableToElement;
import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an error renderable by CError.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ErrorDocument {

    /**
     * A Throwable which was or caused the error represented by
     * this instance.
     */
    private Throwable throwable;
    
    /**
     * A message about the error represented by this instance.
     */
    private String message;
    
    /**
     * Identifies the channel the error state of which this object
     * instance represents.
     */
    private String channelSubscribeId;
    
    /**
     * The name of the channel the error state of which this object
     * instance represents.
     */
    private String channelName;
    
    /**
     * The error code to be presented in the document.
     */
    private ErrorCode code = ErrorCode.UNKNOWN_ERROR;
    
    /**
     * Translator from Throwable to Element of our XML production.
     * Here we instantiate a default, but this can be overridden.
     */
    private IThrowableToElement throwableToElement 
        = new DelegatingThrowableToElement();
    
    public ErrorDocument() {
        // do-nothing constructor
    }
    
    /**
     * Get a Document representing the Throwable.
     * @return a Document representing the Throwable.
     */
    public Document getDocument() {
        // XML of the following type is generated:
        // <error code="$errorID">
        //  <message>$message</message>
        //  <channel>
        //   <id>$channelID</id>
        //   <name>$channelName</name>
        //  </channel>
        //  <exception/>
        // </error>
        
        Document doc = DocumentFactory.getNewDocument();
        
        Element errorEl=doc.createElement("error");
        errorEl.setAttribute("code",Integer.toString( this.code.getCode() ));
        if (this.message != null) {
            Element messageEl=doc.createElement("message");
            messageEl.appendChild(doc.createTextNode(this.message));
            errorEl.appendChild(messageEl);
        }

        if (this.channelSubscribeId != null) {
            Element channelEl=doc.createElement("channel");
            Element idEl=doc.createElement("id");
            idEl.appendChild(doc.createTextNode(this.channelSubscribeId));
            channelEl.appendChild(idEl);

                if(this.channelName != null) {
                    Element nameEl=doc.createElement("name");
                    nameEl.appendChild(doc.createTextNode(this.channelName));
                    channelEl.appendChild(nameEl);
                }
                errorEl.appendChild(channelEl);
            }

        /*
         * If we have a throwable, include a representation of it in the XML.
         */
        if (this.throwable != null 
                && this.throwableToElement.supports(this.throwable.getClass())) {
            Element throwableElement = 
                this.throwableToElement.throwableToElement(this.throwable, doc);
            errorEl.appendChild(throwableElement);
        }
        
        doc.appendChild(errorEl);
        
        return doc;
    }
    
    /**
     * @return Returns the channelName.
     */
    public String getChannelName() {
        return this.channelName;
    }
    
    /**
     * @param channelName The channelName to set.
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    
    /**
     * @return Returns the channelSubscribeId.
     */
    public String getChannelSubscribeId() {
        return this.channelSubscribeId;
    }
    
    /**
     * @param channelSubscribeId The channelSubscribeId to set.
     */
    public void setChannelSubscribeId(String channelSubscribeId) {
        this.channelSubscribeId = channelSubscribeId;
    }
    
    /**
     * @return Returns the code.
     */
    public ErrorCode getCode() {
        return this.code;
    }
    
    /**
     * @param code The code to set.
     */
    public void setCode(ErrorCode code) {
        this.code = code;
    }
    
    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return this.message;
    }
    
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * @return Returns the throwable.
     */
    public Throwable getThrowable() {
        return this.throwable;
    }
    
    /**
     * @param throwable The throwable to set.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    /**
     * @return Returns the throwableToElement.
     */
    public IThrowableToElement getThrowableToElement() {
        return this.throwableToElement;
    }
    
    /**
     * @param throwableToElement The throwableToElement to set.
     */
    public void setThrowableToElement(IThrowableToElement throwableToElement) {
        this.throwableToElement = throwableToElement;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" Channel name: ").append(this.channelName);
        sb.append(" Channel subscribeId=[").append(this.channelSubscribeId).append("]");
        sb.append(" ErrorID=").append(this.code);
        sb.append(" throwable: [").append(this.throwable).append("]");
        sb.append(" throwableToElement: [").append(this.throwableToElement).append("]");
        return sb.toString();
    }
}