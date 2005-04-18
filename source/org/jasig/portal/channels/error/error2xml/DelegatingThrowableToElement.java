/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.error2xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A wrapper for a List of child ThrowableToElements and the logic to poll
 * them in order, accepting the result from the first to support the given throwable.
 * Defaults to the standard List of ThrowableToElement implementations, but provides
 * API hooks to change the list.
 * 
 * You can add handling for other Throwables by either editing this implementation 
 * code to include additional ThrowableToElement implementations by default or
 * by injecting a different List of ThrowableToElement implementations where 
 * the instance of this class that CError actually uses is configured
 * in applicationContext.xml.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DelegatingThrowableToElement 
    implements IThrowableToElement{

    /**
     * List of ThrowableToElement implementations in order from most specific
     * to least specific.  These will be polled in order to make our response to our
     * interface methods.  
     * Our constructor currently defaults this to a basic list of such mappings.
     */
    private List throwableToElements;
    
    /**
     * Instantiate a DelegatingThrowableToElement with
     * a default List of delegates.
     */
    public DelegatingThrowableToElement() {
        List tempThrowableToElements = new ArrayList();
        // note that the list is populated most specific to least specific.
        tempThrowableToElements.add(new ResourceMissingExceptionToElement());
        tempThrowableToElements.add(new InternalTimeoutExceptionToElement());
        tempThrowableToElements.add(new AuthorizationExceptionToElement());
        tempThrowableToElements.add(new ThrowableToElement());
        this.throwableToElements = tempThrowableToElements;
    }
    
    /**
     * Get the List of ThrowableToElements delegates.
     * @return Returns the List of throwableToElements delegates..
     */
    public List getThrowableToElements() {
        return this.throwableToElements;
    }
    
    /**
     * Set the List of ThrowableToElement implementations which this 
     * implementation will poll in order.
     * The List should be in order from most specific to least specific.
     * @param throwableToElements List of ThrowableToElement impls.
     */
    public void setThrowableToElements(List throwableToElements) {
        this.throwableToElements = throwableToElements;
    }


    public boolean supports(Class c) throws IllegalArgumentException {
        if (c == null)
            throw new IllegalArgumentException("Cannot support a null class");
        if (!Throwable.class.isAssignableFrom(c)){
            throw new IllegalArgumentException("Cannot support a class which" +
                    "is not an instance of throwable: offending class: " + c.getName());
        }
        for (Iterator iter = this.throwableToElements.iterator(); iter.hasNext();){
            IThrowableToElement current = (IThrowableToElement) iter.next();
            if (current.supports(c)) {
                return true;
            }
        }
        return false;
    }

    public Element throwableToElement(Throwable t, Document parentDoc) 
        throws IllegalArgumentException {
        
        if (t == null)
            throw new IllegalArgumentException("Cannot translate a null throwable");
        for (Iterator iter = this.throwableToElements.iterator(); iter.hasNext();){
            IThrowableToElement current = (IThrowableToElement) iter.next();
            if (current.supports(t.getClass())) {
                return current.throwableToElement(t, parentDoc);
            }
        }
        throw new IllegalArgumentException("Throwable [" + t + "] is unsupported.");
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" delegates:").append(this.throwableToElements);
        return sb.toString();
    }
}