package org.jasig.portal;
import java.io.PrintWriter;
/**
 * A multithreaded version of a {@link ICharacterChannel}.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public interface IMultithreadedCharacterChannel extends IMultithreadedChannel {
    /**
     * Asks the channel to render its content as characters.
     * The method has the same call precedence as the IChannel.renderXML() method.
     * (i.e. if the channel also supports ICacheable, portal will try to find a cache entry prior calling this method)
     * @param pw a <code>PrintWriter</code> value into which the character output should be directed
     * @param uid a <code>String</code> identifying the "instance" being served
     * @exception PortalException if an error occurs
     */
    public void renderCharacters(PrintWriter pw, String uid) throws PortalException;
}
