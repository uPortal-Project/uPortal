/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * An interface that a multithreaded channel that wants to download MIME files must implement.
 * @author Alex Vigdor
 * @version $Revision$
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces
 */

public interface IMultithreadedMimeResponse {

    /**
     * Returns the MIME type of the content.
     */
    public java.lang.String getContentType (String uid);

    /**
     * Returns the MIME content in the form of an input stream.
     * Returns null if the code needs the OutputStream object
     */
    public java.io.InputStream getInputStream (String uid) throws IOException;

    /**
     * Pass the OutputStream object to the download code if it needs special handling
     * (like outputting a Zip file).
     */
    public void downloadData (OutputStream out, String uid) throws IOException;

    /**
     * Returns the name of the MIME file.
     */
    public java.lang.String getName (String uid);

    /**
     * Returns a list of header values that can be set in the HttpResponse.
     * Returns null if no headers need to be set.
     */
    public Map getHeaders (String uid);

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e);
}



